document.addEventListener('DOMContentLoaded', () => {
    loadUserOrders();
});

async function loadUserOrders() {
    const container = document.getElementById('ordersContainer');
    const noOrdersMsg = document.getElementById('noOrdersMessage');

    // Check if logged in
    const username = sessionStorage.getItem('username');
    if (!username) {
        alert("Please log in to view your orders.");
        window.location.href = 'index.html';
        return;
    }

    try {
        const response = await fetch('/api/orders/user');
        
        if (response.status === 401) {
            alert("Session expired. Please log in again.");
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) throw new Error('Failed to fetch orders');

        const orders = await response.json();

        if (orders.length === 0) {
            container.innerHTML = '';
            noOrdersMsg.classList.remove('d-none');
            return;
        }

        renderOrders(orders);

    } catch (error) {
        console.error('Error loading orders:', error);
        container.innerHTML = `<div class="alert alert-danger">Failed to load order history. Please try again later.</div>`;
    }
}

function renderOrders(orders) {
    const container = document.getElementById('ordersContainer');
    container.innerHTML = '';

    orders.forEach(order => {
        const date = new Date(order.orderDate).toLocaleDateString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute:'2-digit'
        });

        // Determine badge color
        let badgeClass = 'status-pending';
        const status = order.orderStatus.toUpperCase();
        if(status === 'PROCESSING') badgeClass = 'status-processing';
        else if(status === 'SHIPPED') badgeClass = 'status-shipped';
        else if(status === 'DELIVERED') badgeClass = 'status-delivered';
        else if(status === 'CANCELED') badgeClass = 'status-canceled';

        // Generate Items HTML
        let itemsHtml = '';
        order.items.forEach(item => {
            itemsHtml += `
                <div class="d-flex align-items-center mb-3">
                    <img src="${item.toyImage || 'assets/img/logo2.png'}" 
                         class="order-item-thumb" 
                         onerror="this.src='assets/img/logo2.png'"
                         alt="Product">
                    <div class="ms-3 flex-grow-1">
                        <h6 class="mb-0 text-dark">${item.toyName || 'Unknown Item'}</h6>
                        <small class="text-muted">Qty: ${item.quantity} × Rs ${item.price.toFixed(2)}</small>
                    </div>
                    <div class="fw-bold">Rs ${(item.price * item.quantity).toFixed(2)}</div>
                </div>
            `;
        });

        const cardHtml = `
            <div class="order-card">
                <div class="order-header">
                    <div>
                        <span class="text-muted small">ORDER PLACED</span><br>
                        <strong>${date}</strong>
                    </div>
                    <div>
                        <span class="text-muted small">TOTAL</span><br>
                        <strong>Rs ${order.total.toFixed(2)}</strong>
                    </div>
                    <div>
                        <span class="text-muted small">SHIP TO</span><br>
                        <strong>${order.firstname} ${order.lastname}</strong>
                    </div>
                    <div class="ms-auto text-end">
                        <div class="mb-1">Order # ${order.id}</div>
                        <span class="order-status ${badgeClass}">${order.orderStatus}</span>
                    </div>
                </div>
                <div class="order-body">
                    <div class="row">
                        <div class="col-md-8">
                            <h6 class="mb-3 border-bottom pb-2">Items in Order</h6>
                            ${itemsHtml}
                        </div>
                        <div class="col-md-4 border-start">
                            <h6 class="mb-3 border-bottom pb-2">Shipping Details</h6>
                            <p class="small text-muted mb-1">Address:</p>
                            <p class="mb-3 small">
                                ${order.address}<br>
                                ${order.city}, ${order.postalCode}<br>
                                ${order.country}
                            </p>
                            <p class="small text-muted mb-1">Payment Method:</p>
                            <p class="mb-3 small fw-bold">${order.paymentMethod}</p>
                            
                            <div class="d-grid mt-4">
                                ${status !== 'DELIVERED' && status !== 'CANCELED' 
                                    ? `<button class="btn btn-outline-primary btn-sm" onclick="alert('Tracking functionality coming soon!')">Track Package</button>` 
                                    : ''}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        container.innerHTML += cardHtml;
    });
}