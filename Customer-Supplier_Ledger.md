🧾 1. What is a Customer Ledger?

A Customer Ledger is a detailed financial record that tracks all transactions between the business and a customer — primarily showing sales, receipts, payments, and outstanding balances.

It helps monitor how much a customer owes (Accounts Receivable) or has paid.

✅ Customer Ledger Table — Suggested Name

customer_ledger

🧱 Structure (Fields & Description)
Field Name	Type	Description
id	BIGINT (PK)	Unique identifier
customer_id	BIGINT (FK → customers.id)	Linked customer
customer_name	VARCHAR(150)	Customer’s name
trn_ref_no	VARCHAR(100)	Transaction reference (e.g. INV-00045)
trn_date	DATETIME	Date of the transaction
invoice_total	DECIMAL(12,2)	Invoice amount
amount_received	DECIMAL(12,2)	Amount paid by customer
balance_due	DECIMAL(12,2)	Remaining balance
transaction_type	VARCHAR(50)	SALE / PAYMENT / RETURN
remarks	VARCHAR(255)	Optional comments
created_at	DATETIME	Record creation time
created_by	VARCHAR(100)	User who entered data
💡 Sample Data (Customer Ledger)
id	customer_id	customer_name	trn_ref_no	trn_date	invoice_total	amount_received	balance_due	transaction_type	remarks
1	101	Rahim Traders	INV-00045	2025-10-22	12,000.00	12,000.00	0.00	SALE	Full payment received
2	101	Rahim Traders	INV-00046	2025-10-23	8,500.00	5,000.00	3,500.00	SALE	Partial payment
3	101	Rahim Traders	PAY-00025	2025-10-24	0.00	3,500.00	0.00	PAYMENT	Paid remaining balance
4	102	Karim Electronics	INV-00047	2025-10-24	25,000.00	0.00	25,000.00	SALE	Pending payment
🧾 2. What is a Supplier Ledger?

A Supplier Ledger (also known as Vendor Ledger) is a record of all financial transactions between the business and its suppliers or vendors.
It shows purchases, payments made, outstanding dues, and balances owed to suppliers.

✅ Supplier Ledger Table — Suggested Name

supplier_ledger

🧱 Structure (Fields & Description)
Field Name	Type	Description
id	BIGINT (PK)	Unique identifier
supplier_id	BIGINT (FK → suppliers.id)	Linked supplier
supplier_name	VARCHAR(150)	Supplier name
trn_ref_no	VARCHAR(100)	Purchase order or payment reference
trn_date	DATETIME	Transaction date
purchase_total	DECIMAL(12,2)	Total purchase amount
amount_paid	DECIMAL(12,2)	Amount paid to supplier
amount_due	DECIMAL(12,2)	Remaining payable balance
transaction_type	VARCHAR(50)	PURCHASE / PAYMENT / RETURN
remarks	VARCHAR(255)	Optional comments
created_at	DATETIME	Record creation time
created_by	VARCHAR(100)	User who entered data
💡 Sample Data (Supplier Ledger)
id	supplier_id	supplier_name	trn_ref_no	trn_date	purchase_total	amount_paid	amount_due	transaction_type	remarks
1	201	Mizan Agro Supply	PO-00033	2025-10-20	18,000.00	10,000.00	8,000.00	PURCHASE	Partial payment
2	201	Mizan Agro Supply	PAY-00033	2025-10-22	0.00	8,000.00	0.00	PAYMENT	Paid full balance
3	202	Rina Foods Ltd	PO-00034	2025-10-23	9,500.00	0.00	9,500.00	PURCHASE	Awaiting payment
4	203	Pubali Packaging	PO-00035	2025-10-23	5,000.00	5,000.00	0.00	PURCHASE	Fully paid
🧮 3. Key Difference Between Customer & Supplier Ledger
Feature	Customer Ledger	Supplier Ledger
Tracks	Money coming in (sales receipts)	Money going out (payments to suppliers)
Entity	Customer	Supplier
Purpose	Shows who owes the company	Shows whom the company owes
Type of Balance	Debit (receivable)	Credit (payable)
Related Modules	Sales, Payment Received	Purchase, Payment Made
🔗 Optional Integration

Both ledgers can be linked to a common table financial_transactions or your existing
app_acct_financial_accounts table —
where ref_type = 'CUSTOMER_LEDGER' or 'SUPPLIER_LEDGER' for unified reporting.

Would you like me to show how to auto-generate these ledgers dynamically (via a controller and query from your FinancialAccount table)? That way, you won’t need to maintain separate tables — it can generate Customer or Supplier ledgers on demand.

Existing 'FinancialAccount':
@Entity
@Table(name = "app_acct_financial_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fin_acc_id", unique = true, nullable = false)
    private String finAccId; // FIN-1001-90001

    @Column(name = "fin_acc_name", nullable = false, length = 100)
    private String finAccName; // Cash, Pubali Bank, bKash

    @Column(name = "fin_acc_type", length = 50)
    private String finAccType; // CASH, BANK, MOBILE, etc.

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "debit_amount")
    private Double debitAmount;

    @Column(name = "credit_amount")
    private Double creditAmount;

    @Column(name = "balance_after_trn")
    private Double balanceAfterTransaction;

    @Column(name = "current_balance", nullable = false)
    private Double currentBalance;

    @Column(name = "trn_date")
    private LocalDateTime trnDate;

    @Column(name = "trn_ref_no")
    private String trnRefNo; // Invoice number, Expense Ref, Payment Ref

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "ref_type", length = 50)
    private String refType; // SALE_ORDER, PURCHASE_ORDER, EXPENSE, PAYMENT

    @Column(name = "transaction_type", length = 50)
    private String transactionType; // CASH_IN / CASH_OUT

    @Column(name = "entity_name", length = 150)
    private String entityName; // Customer or Supplier name

    @Column(name = "entity_type", length = 50)
    private String entityType; // CUSTOMER / SUPPLIER / EXPENSE / INTERNAL

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Cash / bKash / Bank Transfer

    @Column(name = "financial_year")
    private String financialYear;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "is_posted")
    private Boolean isPosted = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.trnDate == null) this.trnDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


