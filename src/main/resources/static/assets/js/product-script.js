/* assets/js/product-script.js */

let currentToyId = null;

document.addEventListener("DOMContentLoaded", () => {
    // 1. Get ID from URL
    const params = new URLSearchParams(window.location.search);
    currentToyId = params.get("id");

    if (!currentToyId) {
        alert("No toy specified! Returning to shop.");
        window.location.href = "shop.html";
        return;
    }

    // 2. Fetch Details
    fetchProductDetails(currentToyId);

    // 3. Fetch Recommendations (Bucket Analysis)
    fetchRelatedProducts(currentToyId);
});

// --- Fetch Single Product ---
async function fetchProductDetails(id) {
    try {
        const res = await fetch(`/api/toys/${id}`);
        if (!res.ok) throw new Error("Toy not found");
        
        const toy = await res.json();
        renderProduct(toy);
    } catch (err) {
        console.error(err);
        document.getElementById("loading").innerHTML = "<h3>Oops! Toy vanished using an invisibility cloak.</h3><a href='shop.html'>Back to Shop</a>";
    }
}

// --- Render Main Product ---
function renderProduct(t) {
    document.getElementById("loading").style.display = "none";
    document.getElementById("productContent").style.display = "flex";

    // Text Fields
    document.getElementById("prodName").innerText = t.name;
    document.getElementById("prodBrand").innerText = t.brand;

    // --- FIX: CLEAN DESCRIPTION ---
    // The DB description contains <img> tags which cause duplicates.
    // We strip them out using Regex so only the text paragraphs remain.
    let rawDesc = t.description || "";
    let cleanDesc = rawDesc.replace(/<img[^>]*>/gi, ""); // Removes <img ... >

    // Fallback if description becomes empty after cleaning
    if (!cleanDesc.trim()) {
        cleanDesc = "No description available for this magical item.";
    }

    document.getElementById("prodDesc").innerHTML = cleanDesc;

    document.getElementById("prodAge").innerText = (t.minAge || 0) + "+ Years";
    document.getElementById("prodGender").innerText = t.targetAudience || "Unisex";

    // Image (Main Product Image)
    const imgEl = document.getElementById("prodImg");
    imgEl.src = (t.imageUrl && t.imageUrl.length > 5) ? t.imageUrl : "assets/img/logo2.png";

    // Price & Sale Logic
    const isOnSale = (t.onSale === true || t.isOnSale === true) && (t.salePrice > 0);
    const priceEl = document.getElementById("prodPrice");
    const oldPriceEl = document.getElementById("oldPrice");
    const badge = document.getElementById("saleBadge");

    if (isOnSale) {
        priceEl.innerText = `PKR ${t.salePrice.toFixed(2)}`;
        oldPriceEl.innerText = `PKR ${t.price.toFixed(2)}`;
        oldPriceEl.classList.remove("d-none");
        badge.classList.remove("d-none");
    } else {
        priceEl.innerText = `PKR ${t.price.toFixed(2)}`;
        oldPriceEl.classList.add("d-none");
        badge.classList.add("d-none");
    }

    // Stock Logic
    const cartBtn = document.getElementById("addToCartBtn");
    const stockBtn = document.getElementById("outOfStockBtn");

    if (t.stockQuantity > 0) {
        cartBtn.classList.remove("d-none");
        stockBtn.classList.add("d-none");
    } else {
        cartBtn.classList.add("d-none");
        stockBtn.classList.remove("d-none");
    }
}

// --- Fetch Related Products ---
async function fetchRelatedProducts(id) {
    try {
        const res = await fetch(`/api/toys/related/${id}`);
        if (res.ok) {
            const toys = await res.json();
            renderRelatedGrid(toys);
        }
    } catch (err) {
        console.error("Failed to load recommendations", err);
    }
}

// --- Render Related Grid ---
function renderRelatedGrid(toys) {
    const grid = document.getElementById("relatedGrid");
    grid.innerHTML = "";

    if (toys.length === 0) {
        grid.innerHTML = "<p class='text-center'>No related magic found yet.</p>";
        return;
    }

    toys.forEach(t => {
        const image = (t.imageUrl && t.imageUrl.length > 5) ? t.imageUrl : "assets/img/logo2.png";
        const price = (t.onSale && t.salePrice) ? t.salePrice : t.price;
        
        const card = `
            <div class="col-6 col-md-3">
                <div class="card h-100 border-0 shadow-sm" style="transition: transform 0.2s;">
                    <a href="product.html?id=${t.id}" class="text-decoration-none text-dark">
                        <img src="${image}" class="card-img-top p-3" style="height: 150px; object-fit: contain;" alt="${t.name}">
                        <div class="card-body text-center">
                            <h6 class="card-title text-truncate">${t.name}</h6>
                            <div class="fw-bold text-danger">PKR ${price.toFixed(2)}</div>
                        </div>
                    </a>
                </div>
            </div>
        `;
        grid.insertAdjacentHTML("beforeend", card);
    });
}

// --- Add To Cart ---
async function addToCartCurrent() {
    if (!currentToyId) return;

    try {
        const response = await fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ toyId: currentToyId, quantity: 1 })
        });

        if (response.ok) {
            if (typeof updateCartCount === 'function') updateCartCount();
            
            if(confirm("Added to Bag! Go to Cart?")) {
                window.location.href = "cart.html";
            }
        } else if (response.status === 401) {
            alert("Please sign in to shop.");
            if (typeof window.openLogin === 'function') {
                window.openLogin();
            } else {
                window.location.href = "index.html";
            }
        } else {
            alert("Failed to add to cart.");
        }
    } catch (e) {
        console.error(e);
        alert("Error adding to cart.");
    }
}