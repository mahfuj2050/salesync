# ReturnRelation.md

# Return Relation System in Sales & Inventory Management

This document summarizes all important discussions, finalized suggestions, examples, and workflow related to handling **Customer Returns** and **Supplier Returns** in your existing web application.

---

## 1. Existing Entities

The following existing entities are relevant:
- **Products** (with stock)
- **SalesOrder**
- **SaleOrderItems**
- **PurchaseOrder**
- **PurchaseOrderItems**
- **Payment**
- **Customers**
- **Suppliers**
- **FinancialAccount**

These will be extended to handle returns.

⚙️ 7️⃣ Recommended Flow Example (Customer Side)

1️⃣ Sale saved →
LedgerService.recordCustomerTransaction(customer, "INV-001", "SALE", 5000, 0, "Invoice Created")

2️⃣ Payment received →
LedgerService.recordCustomerTransaction(customer, "PAY-001", "PAYMENT", 0, 2000, "Partial Payment")

3️⃣ Return processed →
LedgerService.recordCustomerTransaction(customer, "RET-001", "RETURN", 0, 1000, "Product Returned")

Customer Ledger balance = 5000 - 2000 - 1000 = 2000 due

✅ Summary Table Overview
| Layer        | New | Update | Purpose                                  |
| ------------ | --- | ------ | ---------------------------------------- |
| Entities     | 6   | 0–1    | Ledger + Return tracking                 |
| Controllers  | 2   | 4      | Sale, Purchase, Payment integrate ledger |
| Services     | 2   | 1      | Ledger + Return + Financial sync         |
| Repositories | 6   | 1      | CRUD & filters                           |
| HTML Pages   | 5   | 2      | Return & Ledger UI                       |

🔗 6️⃣ How They All Interconnect

| Action                            | Affects                            | Description                                              |
| --------------------------------- | ---------------------------------------------------------------------------------- |
| Save **Sale Order**               | Customer Ledger                    | Adds *Debit* entry (Customer owes money)                 |
| **Receive Payment** from customer | Customer Ledger, Financial Account | Adds *Credit* entry (Customer pays), increases Cash/Bank |
| Save **Purchase Order**           | Supplier Ledger                    | Adds *Debit* entry (Supplier owed money)                 |
| **Pay Supplier**                  | Supplier Ledger, Financial Account | Adds *Credit* entry (Payment made), decreases Cash/Bank  |
| **Customer Return**               | Customer Ledger, Stock             | Adds *Credit* entry (you owe customer), increases stock  |
| **Supplier Return**               | Supplier Ledger, Stock             | Adds *Credit* entry (supplier owes you), decreases stock |


---

## 2. New Tables / Entity Classes Needed

### 2.1 CustomerReturn
```java
@Entity
@Table(name = "customer_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReturn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String returnRefNo; // e.g., RET-CUST-1001
    private Long salesOrderId;
    private Long customerId;
    private LocalDateTime returnDate;
    private Double totalReturnAmount;
    private String status; // PENDING / COMPLETED
}
```

### 2.2 CustomerReturnItem
```java
@Entity
@Table(name = "customer_return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReturnItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long returnId;
    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
}
```

### 2.3 SupplierReturn
```java
@Entity
@Table(name = "supplier_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierReturn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String returnRefNo; // e.g., RET-SUP-1001
    private Long purchaseOrderId;
    private Long supplierId;
    private LocalDateTime returnDate;
    private Double totalReturnAmount;
    private String status; // PENDING / COMPLETED
}
```

### 2.4 SupplierReturnItem
```java
@Entity
@Table(name = "supplier_return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierReturnItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long returnId;
    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
}
```

---

## 3. Controllers to Update / Create

### 3.1 CustomerReturnController
- `saveCustomerReturn()` - Save return request from customer.
- `getCustomerReturns()` - Fetch returns with filters.

### 3.2 SupplierReturnController
- `saveSupplierReturn()` - Save return to supplier.
- `getSupplierReturns()` - Fetch returns with filters.

### 3.3 FinancialAccountController (existing)
- Update to log return transactions for both customer and supplier.
- Example: deduct or credit cash/bank/mobile accounts.

---

## 4. Repository Interfaces

### 4.1 CustomerReturnRepository
- Standard `JpaRepository<CustomerReturn, Long>`
- Optional: `findBySalesOrderId(Long salesOrderId)`

### 4.2 CustomerReturnItemRepository
- Standard `JpaRepository<CustomerReturnItem, Long>`
- Optional: `findByReturnId(Long returnId)`

### 4.3 SupplierReturnRepository
- Standard `JpaRepository<SupplierReturn, Long>`
- Optional: `findByPurchaseOrderId(Long purchaseOrderId)`

### 4.4 SupplierReturnItemRepository
- Standard `JpaRepository<SupplierReturnItem, Long>`
- Optional: `findByReturnId(Long returnId)`

---

## 5. Service Classes (Optional for business logic)
- **CustomerReturnService** - Calculate totals, validate stock, update ledger.
- **SupplierReturnService** - Validate purchase order, update stock, ledger.

---

## 6. HTML Views

### 6.1 customer-ledger.html
- Filter by customer, from-date, to-date.
- Table: Date, Reference No, Description, Debit, Credit, Balance.
- Button: Print / Export.
- AJAX to `/ledger/customer`.

### 6.2 supplier-ledger.html
- Filter by supplier, from-date, to-date.
- Table: Date, Reference No, Description, Debit, Credit, Balance.
- Button: Print / Export.
- AJAX to `/ledger/supplier`.

### 6.3 Return Forms
- **Customer Return Form**: Select SalesOrder, products, quantity.
- **Supplier Return Form**: Select PurchaseOrder, products, quantity.
- Hidden fields for ledger and financial account updates.

---

## 7. JavaScript

### 7.1 Ledger Filter & Print Example
```javascript
// Filter ledger
$(document).on('click', '.btn-filter', function() { ... });

// Print ledger
$(document).on('click', '#printSupplierLedgerBtn', function() { ... });
```

### 7.2 Payment Method & Account Selection
```javascript
$(document).on('change', '#paymentMethodSelect', function() {
    const type = $(this).val();
    $.ajax({ url: '/accounts/by-type', data: { type }, success: function(accounts){ ... } });
});
```

---

## 8. Workflow Summary

1. **Customer Return**
   - Select sales order → choose items → enter quantity → submit return.
   - Stock is updated, ledger transaction created.

2. **Supplier Return**
   - Select purchase order → choose items → enter quantity → submit return.
   - Stock is deducted, ledger transaction created.

3. **Ledger / Reports**
   - Customer Ledger: Shows sales, payments, returns.
   - Supplier Ledger: Shows purchases, payments, returns.

4. **Financial Accounts**
   - For each return, relevant financial account is updated (Cash, Bank, Mobile).
   - AJAX fetch accounts based on payment method.

5. **Print / Export**
   - Ledger tables can be printed or exported to CSV/Excel.

---

## 9. Sample Ledger Table Columns

| Date       | Reference No | Description           | Debit  | Credit | Balance |
|------------|--------------|---------------------|-------:|-------:|--------:|
| 2025-10-24 | INV-1001     | Sale Invoice         | 500.00 | 0.00   | 500.00  |
| 2025-10-25 | RET-CUST-1   | Customer Return      | 0.00   | 100.00 | 400.00  |
| 2025-10-26 | PAY-2001     | Payment Received     | 0.00   | 400.00 | 0.00    |

---

## 10. Final Notes

- All new returns features integrate with existing **Products, SalesOrder, PurchaseOrder, Payment, FinancialAccount**.
- Ensure **stock validation** during return operations.
- AJAX is used for **dynamic account selection** based on payment method.
- Ledger tables provide real-time **balance calculations** including returns.
- Print/Export functions are available for professional reporting.