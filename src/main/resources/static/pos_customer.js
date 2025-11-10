document.addEventListener("DOMContentLoaded", function() {
    const customerInput = document.getElementById("customerInput");
    const hiddenInput = document.getElementById("customerIdHidden");
    const customersList = document.querySelectorAll("#customersList option");

    customerInput.addEventListener("input", function() {
        // Find matching option
        const selected = Array.from(customersList).find(opt => opt.value === customerInput.value);
        if (selected) {
            hiddenInput.value = selected.dataset.id; // store ID
        } else {
            hiddenInput.value = ""; // clear if no match
        }
    });
});



document.addEventListener("DOMContentLoaded", function() {
    const addCustomerBtn = document.getElementById("addCustomerBtn");
    const customerModalEl = document.getElementById("customerModal");
    
    const customerModal = new bootstrap.Modal(customerModalEl); // initialize modal
    
    addCustomerBtn.addEventListener("click", function() {
        customerModal.show();
    });
});


document.addEventListener("DOMContentLoaded", function() {
    const submitBtn = document.getElementById("submitCustomerBtn");
    const customerForm = document.getElementById("customerForm");
    const customerModalEl = document.getElementById("customerModal");
    const customerModal = new bootstrap.Modal(customerModalEl);

    submitBtn.addEventListener("click", function() {
        // HTML5 validation
        if (!customerForm.checkValidity()) {
            customerForm.reportValidity();
            return;
        }

        // Serialize form data
        const formData = new FormData(customerForm);

        // AJAX POST to /customer
        fetch(customerForm.action, {
            method: "POST",
            body: formData,
        })
        .then(response => {
            if (response.ok) {
                // ✅ Close the modal
                customerModal.hide();

                // ✅ Redirect to /pos to reload the page
                window.location.href = "/pos";
            } else {
                return response.text().then(text => {
                    alert("Error saving customer: " + text);
                });
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Error saving customer. See console.");
        });
    });
});

