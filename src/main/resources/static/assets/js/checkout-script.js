// Wonderland/src/main/resources/static/assets/js/checkout-script.js

// Constants
const SHIPPING_COST = 200.00;
const MINIMUM_ORDER_AMOUNT = 5000.00;

// Global variables
let cartData = null;
let currentDiscount = 0;
let appliedCouponCode = null; // Store the code here

// Load checkout data on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Checkout page loaded');
    
    try {
        // ROBUSTNESS: Check if user is logged in
        const username = sessionStorage.getItem('username');
        const userEmail = sessionStorage.getItem('userEmail');
        
        if (!username) {
            console.warn('Unauthorized access attempt to checkout.');
            alert('Please log in to proceed with checkout');
            window.location.href = 'index.html';
            return;
        }

        // Pre-fill form details
        const emailDisplay = document.getElementById('userEmailDisplay');
        if (emailDisplay) emailDisplay.textContent = userEmail || 'user@example.com';
        
        const firstnameInput = document.getElementById('firstname');
        const lastnameInput = document.getElementById('lastname');
        if (firstnameInput) firstnameInput.value = sessionStorage.getItem('firstname') || '';
        if (lastnameInput) lastnameInput.value = sessionStorage.getItem('lastname') || '';
        
        // Fetch cart from server
        loadCheckoutItems();
        
    } catch (err) {
        console.error("Initialization Error:", err);
        alert("An error occurred while loading the checkout page. Please refresh.");
    }
});

/* ==========================
   FETCH CART DATA
========================== */
async function loadCheckoutItems() {
    console.log('Fetching cart from server for checkout...');
    try {
        const response = await fetch('/api/cart');
        if (!response.ok) {
            if (response.status === 401) throw new Error("Session expired. Please log in again.");
            throw new Error(`Server responded with status: ${response.status}`);
        }
        
        const data = await response.json();
        cartData = data;
        
        if (!data.items || data.items.length === 0) {
            alert('Your cart is empty! Please add items before checkout.');
            window.location.href = 'shop.html';
            return;
        }
        
        renderCheckoutItems(data);
    } catch (error) {
        console.error('Error loading cart:', error);
        alert(`Failed to load cart: ${error.message}`);
        window.location.href = 'cart.html';
    }
}

/* ==========================
   RENDER ITEMS
========================== */
function renderCheckoutItems(data) {
    const itemsList = document.getElementById('checkoutItemsList');
    const itemCount = document.getElementById('itemCount');
    if (!itemsList) return;

    const items = data.items || [];
    const subtotal = data.totalPrice || 0;
    
    let html = '';
    items.forEach(item => {
        const toy = item.toy || {};
        // Use effective price (sale price if active) for display
        const price = (toy.onSale && toy.salePrice) ? toy.salePrice : toy.price;
        const itemTotal = price * item.quantity;
        
        html += `
            <div class="d-flex align-items-center mb-3 pb-3 border-bottom">
                <div class="position-relative">
                    <img src="${toy.imageUrl || 'assets/img/logo2.png'}" 
                         alt="${toy.name}" 
                         style="width: 60px; height: 60px; object-fit: cover; border-radius: 8px;"
                         onerror="this.src='assets/img/logo2.png'">
                    <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-secondary" style="font-size: 0.7rem;">
                        ${item.quantity}
                    </span>
                </div>
                <div class="ms-3 flex-grow-1">
                    <h6 class="mb-0" style="font-size: 0.9rem;">${toy.name}</h6>
                    <small class="text-muted">Rs ${price.toFixed(2)} each</small>
                </div>
                <div>
                    <strong>Rs ${itemTotal.toFixed(2)}</strong>
                </div>
            </div>
        `;
    });
    
    itemsList.innerHTML = html;
    if (itemCount) itemCount.textContent = items.length;
    updateCheckoutSummary(subtotal);
}

/* ==========================
   COUPON LOGIC (Added)
========================== */
async function applyCoupon() {
    const codeInput = document.getElementById('couponCodeInput');
    const messageEl = document.getElementById('couponMessage');
    const code = codeInput.value.trim();

    if (!code) {
        messageEl.textContent = "Please enter a coupon code.";
        messageEl.className = "form-text mt-2 text-danger";
        return;
    }

    try {
        const response = await fetch('/api/coupons/validate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code })
        });

        const data = await response.json();

        if (response.ok && data.valid) {
            // Success: Store code and calculate discount
            appliedCouponCode = data.code; 
            const discountPercent = data.discountPercent;
            const subtotal = cartData.totalPrice || 0;
            currentDiscount = (subtotal * discountPercent) / 100;

            // Update UI
            messageEl.textContent = `Success! ${discountPercent}% off applied.`;
            messageEl.className = "form-text mt-2 text-success";
            
            // Disable input
            codeInput.disabled = true;
            document.querySelector('.discount-input-group button').disabled = true;
            document.querySelector('.discount-input-group button').innerText = "Applied";
            
            updateCheckoutSummary(subtotal);
        } else {
            throw new Error(data.message || "Invalid coupon");
        }
    } catch (error) {
        messageEl.textContent = error.message;
        messageEl.className = "form-text mt-2 text-danger";
        currentDiscount = 0;
        appliedCouponCode = null;
        updateCheckoutSummary(cartData ? cartData.totalPrice : 0);
    }
}

/* ==========================
   UPDATE SUMMARY
========================== */
function updateCheckoutSummary(subtotal) {
    const total = subtotal + SHIPPING_COST - currentDiscount; // Apply discount
    
    const subtotalEl = document.getElementById('checkoutSubtotal');
    const shippingEl = document.getElementById('checkoutShipping');
    const totalEl = document.getElementById('checkoutTotal');
    const discountEl = document.getElementById('checkoutDiscount');
    const discountRow = document.getElementById('discountRow');
    const couponDisplay = document.getElementById('couponCodeDisplay');
    
    if (subtotalEl) subtotalEl.textContent = `Rs ${subtotal.toFixed(2)}`;
    if (shippingEl) shippingEl.textContent = `Rs ${SHIPPING_COST.toFixed(2)}`;
    if (totalEl) totalEl.textContent = `Rs ${total.toFixed(2)}`;
    
    // Show/Hide Discount Row
    if (currentDiscount > 0) {
        discountRow.style.display = 'flex';
        discountEl.textContent = `- Rs ${currentDiscount.toFixed(2)}`;
        couponDisplay.innerText = appliedCouponCode;
    } else {
        discountRow.style.display = 'none';
    }
    
    // Minimum Order Warning
    const existingWarning = document.getElementById('minimumOrderWarning');
    if (existingWarning) existingWarning.remove();
    
    if (subtotal < MINIMUM_ORDER_AMOUNT) {
        const summaryCard = document.querySelector('.col-lg-5 .card');
        if (summaryCard) {
            const warningHtml = `
                <div id="minimumOrderWarning" class="alert alert-warning mt-3" role="alert">
                    <strong>⚠️ Minimum Order Required:</strong> 
                    Your subtotal is Rs ${subtotal.toFixed(2)}. 
                    Min order is Rs ${MINIMUM_ORDER_AMOUNT.toFixed(2)}. 
                </div>
            `;
            summaryCard.insertAdjacentHTML('beforeend', warningHtml);
        }
    }
}

/* ==========================
   COMPLETE ORDER
========================== */
async function completeOrder() {
    const checkoutBtn = document.getElementById('completeOrderBtn');
    if (!checkoutBtn) return;

    try {
        if (!cartData || !cartData.items || cartData.items.length === 0) {
            throw new Error("Your cart is empty!");
        }
        
        const subtotal = cartData.totalPrice || 0;
        if (subtotal < MINIMUM_ORDER_AMOUNT) {
            throw new Error(`Minimum order amount is Rs ${MINIMUM_ORDER_AMOUNT.toFixed(2)}. Please add more items.`);
        }
        
        const form = document.getElementById('checkoutForm');
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }
        
        checkoutBtn.disabled = true;
        checkoutBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Processing...';

        const orderData = {
            firstname: document.getElementById('firstname').value.trim(),
            lastname: document.getElementById('lastname').value.trim(),
            email: sessionStorage.getItem('userEmail'),
            phone: document.getElementById('phone').value.trim(),
            address: document.getElementById('address').value.trim(),
            apartment: document.getElementById('apartment').value.trim(),
            city: document.getElementById('city').value.trim(),
            postalCode: document.getElementById('postalCode').value.trim(),
            country: document.getElementById('country').value,
            shippingMethod: document.querySelector('input[name="shippingMethod"]:checked').value,
            paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value,
            subtotal: subtotal,
            shippingCost: SHIPPING_COST,
            
            // --- UPDATED FIELDS FOR COUPON ---
            couponCode: appliedCouponCode, // Sending this is CRITICAL for your backend logic
            discount: currentDiscount,     // Sent for display/record, but backend recalculates it
            
            items: cartData.items.map(item => ({
                id: item.toy.id,
                quantity: item.quantity,
                price: item.toy.price
            }))
        };

        const orderResponse = await fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        const orderResult = await orderResponse.json();

        if (!orderResponse.ok) {
            throw new Error(orderResult.error || "Order placement failed.");
        }

        await fetch('/api/cart/clear', { method: 'DELETE' });

        alert(`✅ Order placed successfully!\nOrder ID: ${orderResult.orderId}\nTotal: Rs ${orderResult.total.toFixed(2)}`);
        window.location.href = 'index.html';

    } catch (error) {
        console.error('Checkout Error:', error);
        alert(`⚠️ Checkout Failed: ${error.message}`);
        checkoutBtn.disabled = false;
        checkoutBtn.textContent = 'Complete order';
    }
}