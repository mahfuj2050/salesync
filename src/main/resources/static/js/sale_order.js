$(document).ready(function() {
  console.log('Orders page loaded');
  console.log('jQuery:', typeof $ !== 'undefined');
  console.log('Bootstrap:', typeof bootstrap !== 'undefined');

  // Test if modals exist
  console.log('View modal exists:', $('#viewOrderModal').length > 0);
  console.log('Payment modal exists:', $('#paymentModal').length > 0);

  // ==========================================
  // DELETE MODAL
  // ==========================================
  $('#deleteConfirmationModal').on('show.bs.modal', function (event) {
    const button = $(event.relatedTarget);
    const orderId = button.data('id');
    $(this).find('input[name="id"]').val(orderId);
  });

  // ==========================================
  // VIEW ORDER MODAL
  // ==========================================

$(document).on("click", ".view-order-details", function () {
    const orderId = $(this).data('id');
    $('#orderDetailsBody').empty();
    $('#modalTotalAmount').text('');
    $('#modalCustomerName').text('');
    $('#modalCustomerPhone').text('');
    $('#modalCustomerAddress').text('');
    $('#modalOrderId').text(orderId);
    $('#modalOrderDate').text('');

    $.ajax({
        url: '/order/details/' + orderId,
        method: 'GET',
        success: function (data) {
            let total = 0;
            $("#modalInvoiceNumber").text(data.invoiceNumber);
            $('#modalCustomerName').text(data.customerName);
            $('#modalCustomerPhone').text(data.customerPhone);
            $('#modalCustomerAddress').text(data.customerAddress);
            $('#modalOrderDate').text(data.orderDate);

            data.items.forEach(function (item) {
                const itemTotal = item.quantity * item.price;
                total += itemTotal;

                $('#orderDetailsBody').append(`
                    <tr class="text-center">
                        <td class="text-start">${item.productName}</td>
                        <td>${item.quantity}</td>
                        <td>${parseFloat(item.price).toFixed(2)}</td>
                        <td>${itemTotal.toFixed(2)}</td>
                    </tr>
                `);
            });



            $('#modalTotalAmount').text(total.toFixed(2));

            // ✅ FIX HERE — use data.amountPaid and data.amountDue
            $("#modalAmountPaid").text(parseFloat(data.amountPaid).toFixed(2));
            $("#modalAmountDue").text(parseFloat(data.amountDue).toFixed(2));

            $('#viewOrderModal').modal('show');
        }
    });
});



  // ==========================================
  // PAYMENT MODAL
  // ==========================================
  $(document).on("click", ".receive-payment-btn", function(e) {
    e.preventDefault();
    e.stopPropagation();
    
    const orderId = $(this).data("id");
    const invoice = $(this).data("invoice");
    const grandTotal = parseFloat($(this).data("grandtotal")) || 0;
    const amountDue = parseFloat($(this).data("amountdue")) || 0;

    console.log('>>> Payment clicked:', { orderId, invoice, grandTotal, amountDue });

    const modalEl = document.getElementById('paymentModal');
    let modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);

    $("#paymentOrderId").val(orderId);
    $("#displayInvoiceNumber").val(invoice);
    $("#modalGrandTotal").val(grandTotal.toFixed(2));
    $("#currentAmountDue").val(amountDue.toFixed(2));
    $("#amountPaidInput").val("");
    $("#remainingDue").val(amountDue.toFixed(2));
    
    modal.show();
  });

  // Calculate remaining due
  $(document).on("input", "#amountPaidInput", function() {
    const paid = parseFloat($(this).val()) || 0;
    const due = parseFloat($("#currentAmountDue").val()) || 0;
    $("#remainingDue").val(Math.max(due - paid, 0).toFixed(2));
  });

  // Submit payment
  $("#receivePaymentForm").on("submit", function(e) {
    e.preventDefault();
    
    const orderId = $("#paymentOrderId").val();
    const amountPaid = parseFloat($("#amountPaidInput").val());
    
    if (!amountPaid || amountPaid <= 0) {
      alert("Please enter a valid amount");
      return;
    }
    
    const submitBtn = $(this).find('button[type="submit"]');
    submitBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm"></span> Processing...');
    
    $.ajax({
      url: '/pos/receivePayment',
      method: 'POST',
      data: { orderId, amountPaid },
      success: function() {
        alert(`Payment of ${amountPaid.toFixed(2)} received!`);
        location.reload();
      },
      error: function() {
        alert("Error processing payment");
        submitBtn.prop('disabled', false).html('<i class="bi bi-check-circle"></i> Confirm');
      }
    });
  });

  // Other handlers (pagination, filters, etc.)
  const pageSizeSelector = document.getElementById('pageSizeSelector');
  if (pageSizeSelector) {
    pageSizeSelector.addEventListener('change', function() {
      const urlParams = new URLSearchParams(window.location.search);
      urlParams.set('size', this.value);
      urlParams.set('page', '0');
      window.location.href = '/orders?' + urlParams.toString();
    });
  }
});