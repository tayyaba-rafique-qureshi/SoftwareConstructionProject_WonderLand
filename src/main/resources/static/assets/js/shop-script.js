/* shop-script.js - Optimized Filtering + Rendering + Pagination */

let currentPage = 1;
const itemsPerPage = 15;
let currentCategory = "All";

/* ==========================
   ON PAGE LOAD (UNIFIED)
========================== */
document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    const searchInput = document.getElementById("searchKeyword");

    // 1. Handle Search Query
    const searchQuery = params.get('q') || params.get('search');
    if (searchQuery && searchInput) {
        searchInput.value = searchQuery;
    } else if (searchInput) {
        searchInput.value = ""; 
    }

    // 2. Handle Category from URL
    if (params.has("category")) {
        currentCategory = params.get("category");
        const catFilter = document.getElementById("catFilter");
        if (catFilter) catFilter.value = currentCategory;
    }

    // 3. Handle Age Filter from URL
    if (params.has("age")) {
        let ageVal = params.get("age");
        if (ageVal.trim() === "12") ageVal = "12+"; 
        
        const targetRadio = document.querySelector(`input[name="age"][value="${ageVal}"]`);
        if (targetRadio) {
            targetRadio.checked = true;
        }
    }

    // 4. Update UI Elements
    updateCartBadge();
    
    // 5. Trigger Filter
    if (document.getElementById('catFilter')) {
        applyFilters();
    }
});

/* ==========================
   PRICE UI LABEL
========================== */
function updatePriceLabel(val) {
    const lbl = document.getElementById("priceLabel");
    if (lbl) lbl.innerText = val;
}

/* ==========================
   PAGINATION
========================== */
function changePage(dir) {
    currentPage += dir;
    applyFilters();
    window.scrollTo(0, 0);
}

/* ==========================
   MAIN FILTER FUNCTION
========================== */
async function applyFilters() {
    const catEl = document.getElementById("catFilter");
    const genderEl = document.getElementById("genderFilter");
    const priceEl = document.getElementById("priceRange");
    const keywordEl = document.getElementById("searchKeyword");

    // Read radio age range
    const ageRange = document.querySelector("input[name='age']:checked")?.value || "";

    if (!catEl || !genderEl || !priceEl || !keywordEl) {
        console.error("Missing filter elements");
        return;
    }

    const keyword = keywordEl.value.trim();
    const gender = genderEl.value; 
    currentCategory = catEl.value;
    
    const maxPrice = parseFloat(priceEl.value) || 50000;

    console.log("Applying filters:", { maxPrice, category: currentCategory, age: ageRange, search: keyword, audience: gender });

    const apiURL = `/api/inventory?page=${currentPage}&limit=${itemsPerPage}&category=${encodeURIComponent(currentCategory)}&search=${encodeURIComponent(keyword)}&age=${encodeURIComponent(ageRange)}&audience=${encodeURIComponent(gender)}`;

    try {
        const res = await fetch(apiURL);
        if (!res.ok) throw new Error(`API error ${res.status}`);

        const data = await res.json();
        const toys = Array.isArray(data.toys) ? data.toys : [];
        
        // Client-side filtering (Only Price now)
        const filtered = toys.filter(t => {
            const price = parseFloat(t.price) || 0;
            return price <= maxPrice;
        });

        if (filtered.length === 0 && currentPage > 1) {
            currentPage = 1;
            applyFilters();
            return;
        }

        renderGrid(filtered);
        updatePaginationInfo(data.currentPage || 1, data.totalPages || 1);

    } catch (err) {
        console.error("Filter Error:", err);
        renderGrid([]);
        updatePaginationInfo(1, 1);
    }
}
/* shop-script.js - Optimized Filtering + Rendering + Pagination */

// ... (Keep the filter logic exactly as it was) ...

/* ==========================
   RENDER GRID (UPDATED)
========================== */
function renderGrid(toys) {
    const grid = document.getElementById("toyGrid");
    const noRes = document.getElementById("noResults");
    const resultCounter = document.getElementById("resultCount");

    if (!grid || !noRes || !resultCounter) return;

    grid.innerHTML = "";
    resultCounter.innerText = `Found ${toys.length} items`;

    if (toys.length === 0) {
        noRes.classList.remove("d-none");
        return;
    }

    noRes.classList.add("d-none");

    toys.forEach(t => {
        const name = escapeText(t.name || "Mystery Toy");
        const brand = escapeText(t.brand || "Wonderland");
        const age = escapeText(t.minAge || "0");
        const image = t.imageUrl && t.imageUrl.length > 4 ? t.imageUrl : "assets/img/logo2.png";
        const stock = t.stockQuantity || 0;

        const isOnSale = (t.onSale === true || t.isOnSale === true) && (t.salePrice > 0);

        let priceHTML = '';
        if (isOnSale) {
            priceHTML = `
                <div class="d-flex align-items-center">
                    <span style="text-decoration: line-through; color: #999; font-size: 0.9rem; margin-right: 8px;">
                        PKR ${(t.price || 0).toFixed(2)}
                    </span>
                    <span style="color: #dc3545; font-weight: bold; font-size: 1.1rem;">
                        PKR ${(t.salePrice || 0).toFixed(2)}
                    </span>
                </div>
            `;
        } else {
            priceHTML = `<span style="font-weight: bold; font-size: 1.1rem;">PKR ${(t.price || 0).toFixed(2)}</span>`;
        }

        const soldOut = stock <= 0;
        const actionBtn = soldOut
            ? `<button class="btn btn-dark w-100 rounded-pill" onclick="triggerNotify(${t.id})">Notify Me 🔔</button>`
            : `<button class="btn btn-add-cart w-100" onclick="addToCart(${t.id})">Add to Bag 🛍️</button>`;

        let badge = '';
        if (soldOut) {
            badge = `<div class="sold-out-badge">Sold Out</div>`;
        } else if (isOnSale) {
            badge = `<div class="sold-out-badge" style="background: #dc3545;">SALE 🔥</div>`;
        }

        // UPDATE: Added <a href="product.html?id=${t.id}"> wrapper around image
        const card = `
            <div class="col-md-6 col-lg-4">
                <div class="card toy-card h-100">
                    ${badge}
                    <div class="toy-img-container">
                        <a href="product.html?id=${t.id}">
                            <img src="${image}" alt="${name}">
                        </a>
                    </div>
                    <div class="card-body">
                        <small class="text-muted text-uppercase fw-bold">${brand}</small>
                        <a href="product.html?id=${t.id}" class="text-decoration-none text-dark">
                            <h5 class="card-title">${name}</h5>
                        </a>
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            ${priceHTML}
                            <span class="badge bg-light text-dark rounded-pill">${age}+</span>
                        </div>
                    </div>
                    <div class="hover-overlay">
                        ${actionBtn}
                    </div>
                </div>
            </div>
        `;
        grid.insertAdjacentHTML("beforeend", card);
    });
}

// ... (Rest of the file remains the same)
/* ==========================
   PAGINATION LABEL & BUTTON CONTROL
========================== */
function updatePaginationInfo(current, total) {
    const info = document.getElementById("pageInfo");
    const prev = document.getElementById("prevBtn");
    const next = document.getElementById("nextBtn");

    if (info) info.innerText = `Page ${current} of ${total}`;
    if (prev) prev.disabled = current <= 1;
    if (next) next.disabled = current >= total;
}

/* ==========================
   FILTER RESET
========================== */
function resetFilters() {
    location.href = "shop.html";
}

/* ==========================
   NOTIFY ME
========================== */
function triggerNotify(id) {
    alert("We'll notify you when magic returns ✨");
}

/* ==========================
   ADD TO CART
========================== */
async function addToCart(toyId) {
    try {
        const response = await fetch('/api/cart/add', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ toyId: toyId, quantity: 1 })
        });
        
        if (response.ok) {
            // Update the cart count
            updateCartBadge();
            // Optional: Open the cart drawer instead of redirecting
            // For now, redirect to cart page as per previous logic
             window.location.href = 'cart.html';
        } else if (response.status === 401) {
            // FIX: Open Sidebar instead of redirecting to 404 page
            alert('Please log in to add items to cart.');
            if (typeof window.openLogin === 'function') {
                window.openLogin();
            } else {
                window.location.href = 'index.html'; // Fallback
            }
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || 'Failed to add item to cart');
        }
    } catch (error) {
        console.error('Cart Error:', error);
        alert('Unable to add item. Please try again.');
    }
}

/* ==========================
   UPDATE CART BADGE
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
        console.log("Could not fetch cart count (user may be logged out)");
    }
}

/* ==========================
   XSS SAFE TEXT ESCAPE
========================== */
function escapeText(str) {
    return String(str).replace(/[&<>"']/g, s => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[s]);
}