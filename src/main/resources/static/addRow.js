// static/addRow.js

console.log("✅ addRow.js loaded successfully!");

let products = [];

function initPurchaseOrder(productsData) {
	console.log("🎯 initPurchaseOrder called");
	console.log("📦 Received products data:", productsData);
    products = productsData || [];
    console.log("📦 Products loaded:", products.length);
	if (products.length === 0) {
	      console.warn("⚠️ No products available");
	      // You can show a message to the user
	      // alert("No products available. Please add products first.");
	  } else {
	      console.log("✅ Products loaded successfully:", products.length);
	  }
    setupEventHandlers();
	// Auto-add first row if no rows exist
	 if ($('#itemsBody tr').length === 0 && products.length > 0) {
	     console.log("🔄 Auto-adding first row");
	     addNewRow();
	 }
}

function setupEventHandlers() {
    console.log("🔧 Setting up event handlers");
    
    // Add Row Button
    $('#addRowBtn').off('click').on('click', function() {
        console.log("🎯 Add Row button clicked!");
        addNewRow();
    });
    
    // Remove row
    $(document).on('click', '.remove-row', function() {
        $(this).closest('tr').remove();
        calculateTotals();
    });
    
    // Update stock on product select
    $(document).on('change', '.productSelect', function() {
        const stock = $(this).find('option:selected').data('stock') || 0;
        $(this).closest('tr').find('.stock').val(stock);
    });
    
    // Recalculate on input
    $(document).on('input', '.quantity, .purchasePrice, .vat, #discount, #amountPaid', function() {
        if ($(this).closest('tr').length) {
            calculateRow($(this).closest('tr'));
        } else {
            calculateTotals();
        }
    });
}

function addNewRow() {
	console.log("🔄 addNewRow executing");
	console.log("📦 Available products:", products.length);
    
    if (!products || products.length === 0) {
        alert("No products available. Please add products first.");
        return;
    }
    
    let optionsHtml = '<option value="">Select Product</option>';
    products.forEach(product => {
        optionsHtml += `<option value="${product.id}" data-stock="${product.quantity}">${product.name}</option>`;
    });
    
    const rowHtml = `
        <tr>
            <td><select class="form-select productSelect" required>${optionsHtml}</select></td>
            <td><input type="text" class="form-control stock" readonly></td>
            <td><input type="number" class="form-control quantity" min="1" value="1"></td>
            <td><input type="number" class="form-control purchasePrice" min="0" step="0.01"></td>
            <td><input type="number" class="form-control sellingPrice" min="0" step="0.01"></td>
            <td><input type="text" class="form-control subtotal" readonly></td>
            <td><input type="number" class="form-control vat" min="0" value="0"></td>
            <td><input type="text" class="form-control subtotalVat" readonly></td>
            <td class="text-center"><i class="fa-solid fa-delete-left fa-fade fa-2xl remove-row" style="color: #f00000;"></i></span></td>
        </tr>
    `;
    
    $('#itemsBody').append(rowHtml);
    console.log("✅ Row added successfully!");
}

function calculateRow(row) {
    const qty = parseFloat(row.find('.quantity').val()) || 0;
    const purchase = parseFloat(row.find('.purchasePrice').val()) || 0;
    const vatPercent = parseFloat(row.find('.vat').val()) || 0;

    const subtotal = qty * purchase;
    const vatAmount = subtotal * (vatPercent / 100);
    const subtotalVat = subtotal + vatAmount;

    row.find('.subtotal').val(subtotal.toFixed(2));
    row.find('.subtotalVat').val(subtotalVat.toFixed(2));
    calculateTotals();
}

function calculateTotals() {
    let total = 0;
    $('#itemsBody tr').each(function() {
        total += parseFloat($(this).find('.subtotalVat').val()) || 0;
    });
    
    $('#totalAmount').val(total.toFixed(2));

    const discount = parseFloat($('#discount').val()) || 0;
    const grandTotal = total - discount;
    $('#grandTotal').val(grandTotal.toFixed(2));

    const paid = parseFloat($('#amountPaid').val()) || 0;
    const due = grandTotal - paid;
    $('#amountDue').val(due.toFixed(2));
}

// Form submission
$(document).ready(function() {
    $('#purchaseForm').on('submit', function() {
        // Clear previous hidden inputs
        $('#purchaseForm input[name="productIds"], #purchaseForm input[name="quantities"], #purchaseForm input[name="purchasePrices"], #purchaseForm input[name="sellingPrices"], #purchaseForm input[name="vatAmounts"]').remove();
        
        // Add hidden inputs for each row
        $('#itemsBody tr').each(function(i, rowEl) {
            const row = $(rowEl);
            $('#purchaseForm').append(
                $('<input>').attr({type: 'hidden', name: 'productIds', value: row.find('.productSelect').val()}),
                $('<input>').attr({type: 'hidden', name: 'quantities', value: row.find('.quantity').val()}),
                $('<input>').attr({type: 'hidden', name: 'purchasePrices', value: row.find('.purchasePrice').val()}),
                $('<input>').attr({type: 'hidden', name: 'sellingPrices', value: row.find('.sellingPrice').val()}),
                $('<input>').attr({type: 'hidden', name: 'vatAmounts', value: row.find('.vat').val()})
            );
        });
    });
});