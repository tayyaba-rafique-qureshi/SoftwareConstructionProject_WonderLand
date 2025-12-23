/* ==========================
   LOAD CART ON PAGE LOAD
========================== */
document.addEventListener('DOMContentLoaded', () => {
    console.log('Cart page loaded, fetching cart data...');
    loadCartData();
    updateCartBadge();
});

/* ==========================
   FETCH & RENDER CART (Main Function)
========================== */
async function loadCartData() {
    try {
        console.log('Fetching cart from /api/cart...');
        const response = await fetch('/api/cart', {
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        console.log('Cart API response status:', response.status);
        
        if (response.status === 401) {
            console.log('User not authenticated, redirecting to login...');
            alert('Please log in to view your cart');
            window.location.href = '/login.html';
            return;
        }
        
        if (response.ok) {
            const cart = await response.json();
            console.log('Cart data received:', cart);
            console.log('Number of items:', cart.items?.length || 0);
            renderCart(cart);
        } else {
            const errorText = await response.text();
            console.error('Failed to load cart:', response.status, errorText);
            showEmptyCart();
        }
    } catch (error) {
        console.error('Cart Load Error:', error);
        showEmptyCart();
    }
}

/* ==========================
   RENDER CART ITEMS
========================== */
function renderCart(cart) {
    const container = document.getElementById('cartItemsContainer');
    const emptyMessage = document.getElementById('emptyCartMessage');
    
    console.log('Rendering cart with items:', cart.items?.length || 0);
    
    if (!cart.items || cart.items.length === 0) {
        showEmptyCart();
        return;
    }
    
    emptyMessage.style.display = 'none';
    container.innerHTML = '';
    
    let subtotal = 0;
    
    cart.items.forEach(item => {

       
        const toy = item.toy || {};
        const effectivePrice = (toy.onSale && toy.salePrice) ? toy.salePrice : toy.price;
        const itemTotal = effectivePrice * item.quantity;
        subtotal += itemTotal;
        
        const itemHTML = `
            <div class="cart-item" data-item-id="${item.id}">
                <div class="cart-item-image">
                    <img src="${toy.imageUrl || 'assets/img/logo2.png'}" alt="${escapeHTML(toy.name || 'Product')}">
                </div>
                <div class="cart-item-details">
                    <div class="cart-item-info">
                        <div class="cart-item-title">${escapeHTML(toy.name || 'Unknown Item')}</div>
                        <div class="cart-item-brand">${escapeHTML(toy.brand || 'Toys & Toys')}</div>
                    </div>
                    <div class="cart-item-price">
                        Rs ${(toy.price || 0).toFixed(2)}
                    </div>
                    <div class="cart-item-quantity">
                        <div class="quantity-controls">
                            <button onclick="updateQuantity(${item.id}, ${item.quantity - 1})" ${item.quantity <= 1 ? 'disabled' : ''}>−</button>
                            <input type="text" value="${item.quantity}" readonly>
                            <button onclick="updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
                        </div>
                    </div>
                    <div class="cart-item-total">
                        Rs ${itemTotal.toFixed(2)}
                    </div>
                    <div class="cart-item-remove">
                        <button class="btn-remove" onclick="removeItem(${item.id})" title="Remove item">×</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += itemHTML;
    });
    
    updateSummary(subtotal);
}

/* ==========================
   UPDATE ORDER SUMMARY
========================== */
function updateSummary(subtotal) {
    const subtotalEl = document.getElementById('cartSubtotal');
    const totalEl = document.getElementById('cartTotal');
    
    if (subtotalEl) subtotalEl.textContent = `Rs ${subtotal.toFixed(2)}`;
    if (totalEl) totalEl.textContent = `Rs ${subtotal.toFixed(2)}`;
}

/* ==========================
   SHOW EMPTY CART MESSAGE
========================== */
function showEmptyCart() {
    const container = document.getElementById('cartItemsContainer');
    const emptyMessage = document.getElementById('emptyCartMessage');
    
    if (container) container.innerHTML = '';
    if (emptyMessage) emptyMessage.style.display = 'block';
    
    updateSummary(0);
}

/* ==========================
   UPDATE ITEM QUANTITY
========================== */
async function updateQuantity(itemId, newQuantity) {
    if (newQuantity < 1) return;
    
    try {
        const response = await fetch('/api/cart/update', {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ itemId, quantity: newQuantity })
        });
        
        if (response.ok) {
            console.log("Quantity updated successfully");
            loadCartData();
            updateCartBadge();
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || 'Failed to update quantity');
        }
    } catch (error) {
        console.error('Update Error:', error);
        alert('An error occurred while updating quantity');
    }
}

/* ==========================
   REMOVE ITEM FROM CART
========================== */
async function removeItem(itemId) {
    if (!confirm("Are you sure you want to remove this item?")) return;

    try {
        console.log(`Attempting to remove item ID: ${itemId}`);
        const response = await fetch(`/api/cart/remove/${itemId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            console.log("Item removed successfully from cart");
            loadCartData();
            updateCartBadge();
        } else {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || errorData.message || "Failed to remove item");
        }
    } catch (error) {
        console.error("Removal Error:", error);
        alert(`⚠️ Could not remove item: ${error.message}`);
    }
}

/* ==========================
   UPDATE CART BADGE (Navbar)
========================== */
async function updateCartBadge() {
    try {
        const response = await fetch('/api/cart/count', {
            credentials: 'include'
        });
        if (response.ok) {
            const data = await response.json();
            const badge = document.getElementById('cartCount');
            if (badge) {
                badge.innerText = data.count || 0;
                badge.style.display = (data.count > 0) ? 'inline-block' : 'none';
            }
        }
    } catch (err) {
        console.log("Could not fetch cart count:", err);
    }
}

/* ==========================
   PROCEED TO CHECKOUT
========================== */
function proceedToCheckout() {
    window.location.href = 'checkout.html';
}

/* ==========================
   XSS PROTECTION
========================== */
function escapeHTML(str) {
    return String(str).replace(/[&<>"']/g, s => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[s]);
}
