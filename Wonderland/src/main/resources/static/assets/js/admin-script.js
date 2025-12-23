/* admin-script.js - Complete & Fixed Version */

(function() {
    let currentPage = 1;
    const itemsPerPage = 20;
    let currentCategory = 'All';
    
    // Global Chart Instances
    let revenueChartInstance = null;
    let statusChartInstance = null;

    // --- 1. Admin Route Guard ---
    const role = sessionStorage.getItem('role');
    if (!role || role !== 'ADMIN') {
        alert("Access Denied: Admins Only");
        window.location.href = 'index.html';
        return;
    }

    // --- 2. Initialization ---
    document.addEventListener("DOMContentLoaded", function() {
        showTab('analytics');
        loadAnalytics();
        loadToys();
        setupFormHandlers();
    });

    // ==========================================
    //  SECTION: ANALYTICS (Charts & Hot Sellers)
    // ==========================================
    function loadAnalytics() {
        fetch('/api/admin/analytics')
            .then(res => res.json())
            .then(data => {
                // KPIs
                const safeText = (id, val) => { 
                    const el = document.getElementById(id); 
                    if(el) el.textContent = val; 
                };
                safeText('totalRevenue', `Rs ${data.totalRevenue?.toFixed(2) || '0.00'}`);
                safeText('totalOrders', data.totalOrders || 0);
                safeText('pendingOrders', data.pendingOrders || 0);
                safeText('lowStockCount', data.lowStockItems || 0);
                safeText('totalItemsSold', data.totalItemsSold || 0);

                // Render Visuals
                if (data.hotSellers) renderHotSellers(data.hotSellers);
                if (data.revenueChart) renderRevenueChart(data.revenueChart);
                if (data.statusChart) renderStatusChart(data.statusChart);
            })
            .catch(err => console.error('Analytics Error:', err));
    }

    function renderHotSellers(items) {
        const list = document.getElementById('hotSellersList');
        if (!list) return;

        if (!items || items.length === 0) {
            list.innerHTML = '<li class="list-group-item text-center p-3">No sales data yet.</li>';
            return;
        }

        list.innerHTML = items.map((toy, index) => `
            <li class="list-group-item d-flex align-items-center p-2">
                <span class="badge bg-warning text-dark me-2">#${index+1}</span>
                <img src="${toy.imageUrl || 'assets/img/logo2.png'}" style="width:40px; height:40px; object-fit:contain; margin-right:10px;">
                <div class="flex-grow-1">
                    <h6 class="mb-0 small fw-bold">${toy.name}</h6>
                    <small class="text-muted">Rs ${toy.price}</small>
                </div>
            </li>
        `).join('');
    }

    function renderRevenueChart(data) {
        const ctx = document.getElementById('revenueChart');
        if(!ctx) return;
        
        const dates = Object.keys(data).sort();
        const values = dates.map(d => data[d]);

        if (revenueChartInstance) revenueChartInstance.destroy();

        revenueChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: dates,
                datasets: [{
                    label: 'Revenue',
                    data: values,
                    borderColor: '#4e73df',
                    tension: 0.3,
                    fill: true
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    function renderStatusChart(data) {
        const ctx = document.getElementById('statusChart');
        if(!ctx) return;
        
        const labels = Object.keys(data);
        const values = Object.values(data);
        const colors = { 'PENDING':'#f6c23e', 'SHIPPED':'#4e73df', 'DELIVERED':'#1cc88a', 'CANCELLED':'#e74a3b' };
        
        if (statusChartInstance) statusChartInstance.destroy();

        statusChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: labels.map(l => colors[l] || '#ccc')
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    // ==========================================
    //  SECTION: ORDERS
    // ==========================================
    function loadOrders(searchId = null) {
        const url = searchId 
            ? `/api/admin/orders/search?orderId=${searchId}`
            : '/api/admin/orders';
            
        fetch(url)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP Error: ${res.status}`);
                return res.json();
            })
            .then(orders => renderOrdersTable(orders))
            .catch(err => {
                console.error('Error loading orders:', err);
                const tbody = document.getElementById('orders-table-body');
                if(tbody) tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger p-4">Error: ${err.message}</td></tr>`;
            });
    }

    function renderOrdersTable(orders) {
        const tbody = document.getElementById('orders-table-body');
        if (!tbody) return;
        
        if (!orders || orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px;">No orders found</td></tr>';
            return;
        }

        tbody.innerHTML = orders.map(order => {
            const rawStatus = order.orderStatus || 'PENDING';
            const currentStatus = rawStatus.toUpperCase();
            const isFinal = ['DELIVERED', 'CANCELLED'].includes(currentStatus);
            const isLocked = ['SHIPPED', 'DELIVERED'].includes(currentStatus);
            const dateStr = order.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'N/A';

            let statusOptions = `
                <option value="">Update Status</option>
                <option value="PROCESSING" ${currentStatus === 'PROCESSING' ? 'selected' : ''} ${isLocked ? 'disabled' : ''}>Processing</option>
                <option value="SHIPPED" ${currentStatus === 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                <option value="DELIVERED" ${currentStatus === 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                <option value="CANCELLED" ${currentStatus === 'CANCELLED' ? 'selected' : ''} ${isLocked ? 'disabled' : ''}>Cancelled</option>
            `;

            return `
                <tr style="${isFinal ? 'background: #f9f9f9; opacity: 0.8;' : ''}">
                    <td><strong>#${order.id}</strong></td>
                    <td>${order.firstname} ${order.lastname}<br><small class="text-muted">${order.email}</small></td>
                    <td>${dateStr}</td>
                    <td>${order.items ? order.items.length : 0} items</td>
                    <td><strong>Rs ${order.total ? order.total.toFixed(2) : '0.00'}</strong></td>
                    <td><span class="badge" style="background: ${getStatusColor(currentStatus)}; color: white;">${currentStatus}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="viewOrderDetails(${order.id}, event)">👁️</button>
                        <select onchange="updateOrderStatus(${order.id}, this.value, '${currentStatus}')" style="margin-left:5px;" ${isFinal ? 'disabled' : ''}>
                            ${statusOptions}
                        </select>
                    </td>
                </tr>
            `;
        }).join('');
    }

    // --- [FIXED] View Order Details ---
    function viewOrderDetails(orderId, event) {
        if (event) event.preventDefault();
        console.log("View clicked for Order ID:", orderId);

        fetch(`/api/admin/orders/${orderId}`)
            .then(response => {
                if (!response.ok) throw new Error('Order not found on server');
                return response.json();
            })
            .then(order => {
                document.getElementById('orderDetailId').textContent = order.id;
                document.getElementById('orderCustomerName').textContent = `${order.firstname} ${order.lastname}`;
                document.getElementById('orderCustomerEmail').textContent = order.email;
                document.getElementById('orderCustomerPhone').textContent = order.phone || 'N/A';
                document.getElementById('orderCustomerAddress').textContent = order.address;
                document.getElementById('orderCustomerCity').textContent = order.city;
                document.getElementById('orderCustomerCountry').textContent = order.country;
                document.getElementById('orderDate').textContent = new Date(order.orderDate).toLocaleString();
                
                const statusBadge = document.getElementById('orderStatus');
                statusBadge.textContent = order.orderStatus;
                const statusColors = { 'PENDING':'warning', 'PROCESSING':'info', 'SHIPPED':'primary', 'DELIVERED':'success', 'CANCELLED':'danger' };
                statusBadge.className = `badge bg-${statusColors[order.orderStatus.toUpperCase()] || 'secondary'}`;
                
                document.getElementById('orderPaymentMethod').textContent = order.paymentMethod || 'N/A';
                document.getElementById('orderSubtotal').textContent = `Rs ${order.subtotal.toFixed(2)}`;
                document.getElementById('orderShipping').textContent = `Rs ${order.shippingCost.toFixed(2)}`;
                document.getElementById('orderTotal').textContent = `Rs ${order.total.toFixed(2)}`;

                const itemsBody = document.getElementById('orderItemsBody');
                if (order.items && order.items.length > 0) {
                    itemsBody.innerHTML = order.items.map(item => `
                        <tr>
                            <td>${item.toyName || 'Item'}</td>
                            <td>Rs ${item.price.toFixed(2)}</td>
                            <td>${item.quantity}</td>
                            <td>Rs ${(item.price * item.quantity).toFixed(2)}</td>
                        </tr>
                    `).join('');
                } else {
                    itemsBody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No items found</td></tr>';
                }

                // [FIX] Use classList.add('active') instead of style.display
                const modal = document.getElementById('orderDetailsModal');
                
                // 1. Add the active class (triggers CSS opacity/visibility)
                modal.classList.add('active'); 
                
                // 2. Ensure display is flex (just in case)
                modal.style.display = 'flex'; 
            })
            .catch(error => {
                console.error('Error fetching order details:', error);
                alert(`Failed to load details for Order #${orderId}.\nCheck console for errors.`);
            });
    }

    // --- [FIXED] Close Order Details ---
    function closeOrderDetailsModal() {
        const modal = document.getElementById('orderDetailsModal');
        // [FIX] Remove active class to hide
        modal.classList.remove('active');
        
        // Optional: wait for animation to finish before hiding display, or just hide immediately
        setTimeout(() => { modal.style.display = 'none'; }, 300); 
    }

    function getStatusColor(status) {
        const colors = { 'PENDING':'#f39c12', 'PROCESSING':'#3498db', 'SHIPPED':'#9b59b6', 'DELIVERED':'#27ae60', 'CANCELLED':'#e74c3c' };
        return colors[status] || '#95a5a6';
    }

    // ==========================================
    //  SECTION: INVENTORY
    // ==========================================
    function loadToys() {
        const spinner = document.getElementById('loadingSpinner');
        if(spinner) spinner.style.display = 'inline';
        
        fetch(`/api/inventory?page=${currentPage}&limit=${itemsPerPage}&category=${encodeURIComponent(currentCategory)}`)
            .then(res => res.json())
            .then(data => {
                if(spinner) spinner.style.display = 'none';
                renderTable(data.toys);
                updatePaginationControls(data);
            })
            .catch(err => {
                console.error(err);
                if(spinner) spinner.style.display = 'none';
            });
    }

    function renderTable(toys) {
        const tbody = document.getElementById('inventory-table-body');
        if (!tbody) return;
        tbody.innerHTML = ''; 

        if (toys.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center p-3">No toys found.</td></tr>';
            return;
        }

        toys.forEach(toy => {
            let imgUrl = toy.imageUrl ? toy.imageUrl : 'assets/img/logo2.png';
            let stockColor = toy.stockQuantity < 5 ? '#e74c3c' : '#27ae60';
            
            tbody.innerHTML += `
                <tr>
                    <td><img src="${imgUrl}" class="toy-thumbnail" alt="toy"></td>
                    <td><strong>${toy.name}</strong><div class="small text-muted">${toy.brand}</div></td>
                    <td><span class="badge bg-light text-dark border">${toy.category}</span></td>
                    <td>Rs ${toy.price.toFixed(2)}</td>
                    <td style="color:${stockColor}; font-weight:bold;">${toy.stockQuantity}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-primary me-1" onclick="editToy(${toy.id})">Edit</button>
                        <button class="btn btn-sm btn-warning me-1" onclick="openRestockModal(${toy.id})">Stock</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteToy(${toy.id})">Del</button>
                    </td>
                </tr>
            `;
        });
    }

    function updatePaginationControls(data) {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const pageInfo = document.getElementById('pageInfo');
        if (pageInfo) pageInfo.innerText = `Page ${data.currentPage} of ${data.totalPages}`;
        if (prevBtn) prevBtn.disabled = data.currentPage <= 1;
        if (nextBtn) nextBtn.disabled = data.currentPage >= data.totalPages;
    }

    // ==========================================
    //  SECTION: FORMS & HELPERS
    // ==========================================
    function setupFormHandlers() {
        const toyForm = document.getElementById('toyForm');
        if (toyForm) {
            toyForm.onsubmit = function(e) {
                e.preventDefault();
                const id = document.getElementById('toyId').value;
                if (id) {
                    // Update
                    const updateData = {
                        name: document.getElementById('toyName').value,
                        brand: document.getElementById('toyBrand').value,
                        category: document.getElementById('toyCategory').value,
                        price: parseFloat(document.getElementById('toyPrice').value),
                        stockQuantity: parseInt(document.getElementById('toyStock').value),
                        minAge: parseInt(document.getElementById('toyMinAge').value),
                        targetAudience: document.getElementById('toyAudience').value,
                        description: document.getElementById('toyDescription').value
                    };
                    fetch(`/api/toys/update/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(updateData)
                    }).then(res => { if(res.ok) { alert("Updated!"); location.reload(); } });
                } else {
                    // Add
                    const formData = new FormData();
                    formData.append("name", document.getElementById('toyName').value);
                    formData.append("brand", document.getElementById('toyBrand').value);
                    formData.append("category", document.getElementById('toyCategory').value);
                    formData.append("price", document.getElementById('toyPrice').value);
                    formData.append("stock_quantity", document.getElementById('toyStock').value);
                    formData.append("min_age", document.getElementById('toyMinAge').value);
                    formData.append("target_audience", document.getElementById('toyAudience').value);
                    formData.append("description", document.getElementById('toyDescription').value);
                    formData.append("item_type", "Physical");
                    const fileInput = document.getElementById('toyImageFile');
                    if (fileInput.files.length > 0) formData.append("image_file", fileInput.files[0]);

                    fetch('/api/toys/add', { method: 'POST', body: formData })
                        .then(res => res.json())
                        .then(d => { if(d.status === "success") { alert("Added!"); location.reload(); } else alert("Error: "+d.message); });
                }
            };
        }
        
        const restockForm = document.getElementById('restockForm');
        if(restockForm) {
            restockForm.onsubmit = function(e) {
                e.preventDefault();
                const payload = { id: document.getElementById('restockId').value, quantity: document.getElementById('restockQty').value };
                fetch('/api/restock', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload)
                }).then(res => { if(res.ok) { alert("Stock Added!"); location.reload(); } });
            };
        }
    }

    // Expose helpers globally
    window.showTab = function(tabName) {
        ['analytics', 'inventory', 'orders', 'marketing'].forEach(t => {
            const sec = document.getElementById(t + 'Section');
            const link = document.getElementById(t + 'Tab');
            if(sec) sec.style.display = 'none';
            if(link) link.classList.remove('active');
        });
        document.getElementById(tabName + 'Section').style.display = 'block';
        document.getElementById(tabName + 'Tab').classList.add('active');
        
        if (tabName === 'analytics') loadAnalytics();
        if (tabName === 'inventory') loadToys();
        if (tabName === 'orders') loadOrders();
    };

    window.updateOrderStatus = function(orderId, newStatus, currentStatus) {
        if (!newStatus) return;
        if (['SHIPPED', 'DELIVERED'].includes(currentStatus) && ['PENDING', 'PROCESSING', 'CANCELLED'].includes(newStatus)) {
            alert(`Cannot move order from ${currentStatus} back to ${newStatus}.`);
            loadOrders(); 
            return;
        }
        if(!confirm(`Update Order #${orderId} to ${newStatus}?`)) { loadOrders(); return; }

        fetch(`/api/admin/orders/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        }).then(res => { if (res.ok) { alert("Order Updated!"); loadOrders(); } });
    };

    window.searchOrders = function() {
        const val = document.getElementById('orderSearchInput').value.trim();
        loadOrders(val || null);
    };
    window.clearOrderSearch = function() { document.getElementById('orderSearchInput').value = ''; loadOrders(); };
    window.changeCategory = () => { currentCategory = document.getElementById('adminCategoryFilter').value; loadToys(); };
    window.changePage = (d) => { currentPage += d; loadToys(); };
    window.exportToCSV = () => { window.location.href = '/api/admin/orders/export'; };
    window.filterOrders = () => {
        const s = document.getElementById('orderStatusFilter').value;
        const url = s !== 'All' ? `/api/admin/orders?status=${s}` : '/api/admin/orders';
        fetch(url).then(r=>r.json()).then(renderOrdersTable);
    };
    window.editToy = (id) => {
        fetch(`/api/toys/${id}`).then(res => res.json()).then(toy => {
            document.getElementById('toyId').value = toy.id;
            document.getElementById('toyName').value = toy.name;
            document.getElementById('toyBrand').value = toy.brand;
            document.getElementById('toyCategory').value = toy.category;
            document.getElementById('toyPrice').value = toy.price;
            document.getElementById('toyStock').value = toy.stockQuantity;
            document.getElementById('toyMinAge').value = toy.minAge;
            document.getElementById('toyAudience').value = toy.targetAudience;
            document.getElementById('toyDescription').value = toy.description;
            document.getElementById('modalTitle').innerText = "Edit Toy";
            document.getElementById('imageUploadGroup').style.display = "none";
            document.getElementById('toyModal').classList.add('active');
            document.getElementById('toyDrawer').classList.add('active');
        });
    };
    window.deleteToy = (id) => { if(confirm("Delete?")) fetch(`/api/toys/delete/${id}`, { method: 'DELETE' }).then(res => { if(res.ok) location.reload(); }); };
    window.openAddModal = () => { document.getElementById('toyForm').reset(); document.getElementById('toyId').value=""; document.getElementById('toyModal').classList.add('active'); document.getElementById('toyDrawer').classList.add('active'); };
    window.closeToyModal = () => { document.getElementById('toyModal').classList.remove('active'); document.getElementById('toyDrawer').classList.remove('active'); };
    window.openRestockModal = (id) => { document.getElementById('restockId').value = id; document.getElementById('restockModal').classList.add('active'); document.getElementById('restockDrawer').classList.add('active'); };
    window.closeRestockModal = () => { document.getElementById('restockModal').classList.remove('active'); document.getElementById('restockDrawer').classList.remove('active'); };

    // EXPOSE VIEW MODAL GLOBALLY
    window.viewOrderDetails = viewOrderDetails;
    window.closeOrderDetailsModal = closeOrderDetailsModal;

})();

// ============================================================
//  SECTION D: MARKETING ACTIONS
// ============================================================

window.startSale = function(e) {
    e.preventDefault();
    const category = document.getElementById('saleCategory').value;
    const discount = document.getElementById('saleDiscount').value;

    if(!confirm(`Start a ${discount}% sale on ${category}? This will email all users.`)) return;

    fetch('/api/admin/marketing/sale', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ category, discount })
    })
    .then(res => res.json())
    .then(data => alert(data.message || data.error));
};

window.createCoupon = function(e) {
    e.preventDefault();
    const code = document.getElementById('couponCode').value;
    const discount = document.getElementById('couponDiscount').value;
    const expiry = document.getElementById('couponExpiry').value;

    fetch('/api/admin/marketing/coupon', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ code, discount, expiry })
    })
    .then(res => res.json())
    .then(data => alert(data.message || data.error));
};