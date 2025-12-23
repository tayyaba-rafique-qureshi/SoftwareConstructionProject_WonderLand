// Wonderland/src/main/resources/static/assets/js/script.js

// --- Route Guard: Security & Redirection ---
(function() {
    const role = sessionStorage.getItem('role');
    const path = window.location.pathname;

    if (role === 'ADMIN' && (path.endsWith('index.html') || path.endsWith('/'))) {
         // window.location.href = 'admin.html';
    }

    if (role === 'CUSTOMER' && path.includes('admin.html')) {
        window.location.href = 'index.html';
    }
})();

/* --- Slideshow Logic --- */
(function() {
    let slideIndex = 1;
    if (document.getElementsByClassName("slide").length > 0) {
        showSlides(slideIndex);
    }

    window.moveSlide = function(n) {
        showSlides(slideIndex += n);
    };

    window.currentSlide = function(n) {
        showSlides(slideIndex = n);
    };

    function showSlides(n) {
        let i;
        let slides = document.getElementsByClassName("slide");
        let dots = document.getElementsByClassName("dot");

        if (slides.length === 0) return;
        if (n > slides.length) { slideIndex = 1; }
        if (n < 1) { slideIndex = slides.length; }

        for (i = 0; i < slides.length; i++) {
            slides[i].style.display = "none";
        }
        for (i = 0; i < dots.length; i++) {
            dots[i].className = dots[i].className.replace(" active", "");
        }

        if (slides.length > 0) slides[slideIndex - 1].style.display = "block";
        if (dots.length > 0) dots[slideIndex - 1].className += " active";
    }
})();

/* --- Authentication & UI Persistence Logic --- */

document.addEventListener("DOMContentLoaded", function() {
    checkLoginStatus();
    updateCartCount();
    setupSidebarEvents();
    loadBrands();
});

function checkLoginStatus() {
    const username = sessionStorage.getItem('username');
    const userEmail = sessionStorage.getItem('userEmail');
    
    const loginBtn = document.getElementById('loginBtn');
    const accountBtn = document.getElementById('accountBtn');
    
    if (username) {
        if (loginBtn) loginBtn.style.display = 'none';
        if (accountBtn) {
            accountBtn.style.display = 'inline-block';
            const displaySpan = document.getElementById('displayUsername');
            if (displaySpan) displaySpan.innerText = username;
        }

        const accName = document.getElementById('acc-name');
        const accEmail = document.getElementById('acc-email');
        if (accName) accName.innerText = username;
        if (accEmail) accEmail.innerText = userEmail || "No email found";
    } else {
        if (loginBtn) loginBtn.style.display = 'inline-block';
        if (accountBtn) accountBtn.style.display = 'none';
    }
}

async function updateCartCount() {
    try {
        const response = await fetch('/api/cart/count');
        const cartCountBadge = document.getElementById('cartCount');
        
        if (response.ok && cartCountBadge) {
            const data = await response.json();
            const count = data.count || 0;
            
            if (count > 0) {
                cartCountBadge.textContent = count;
                cartCountBadge.style.display = 'inline-block';
            } else {
                cartCountBadge.style.display = 'none';
            }
        }
    } catch (err) {
        console.log("Cart count fetch failed (user likely logged out).");
    }
}

/* --- Sidebar Toggle Logic --- */

// NEW: Globally accessible function to open login
window.openLogin = function() {
    const loginSidebar = document.getElementById("loginSidebar");
    const overlay = document.getElementById("overlay");
    if(loginSidebar) {
        loginSidebar.classList.add("active");
        if(overlay) overlay.classList.add("active");
    } else {
        // Fallback if sidebar is missing on current page
        window.location.href = "index.html";
    }
};

function setupSidebarEvents() {
    const loginSidebar = document.getElementById("loginSidebar");
    const accountSidebar = document.getElementById("accountSidebar");
    const overlay = document.getElementById("overlay");
    const loginBtnElement = document.getElementById("loginBtn");
    const accountBtnElement = document.getElementById("accountBtn");

    if (loginBtnElement) {
        loginBtnElement.addEventListener("click", function(e) {
            e.preventDefault();
            window.openLogin(); // Use the global function
        });
    }

    if (accountBtnElement) {
        accountBtnElement.addEventListener("click", function(e) {
            e.preventDefault();
            if(accountSidebar) {
                accountSidebar.classList.add("active");
                if(overlay) overlay.classList.add("active");
            }
        });
    }
}

function closeLogin() {
    const loginSidebar = document.getElementById("loginSidebar");
    const overlay = document.getElementById("overlay");
    if (loginSidebar) loginSidebar.classList.remove("active");
    if (overlay) overlay.classList.remove("active");
}

function closeAccount() {
    const accountSidebar = document.getElementById("accountSidebar");
    const overlay = document.getElementById("overlay");
    if (accountSidebar) accountSidebar.classList.remove("active");
    if (overlay) overlay.classList.remove("active");
}

window.closeAllDrawers = function() {
    closeLogin();
    closeAccount();
    // Close cart if it exists (shop.html specific)
    const cartSidebar = document.getElementById("cartSidebar");
    if (cartSidebar) cartSidebar.classList.remove("active");
};

/* --- Account Actions --- */

function handleLogout() {
    sessionStorage.clear();
    localStorage.removeItem('cart'); 
    window.location.href = "/logout"; 
}

/* ==========================
   CHANGE PASSWORD MODAL
========================== */

document.addEventListener("DOMContentLoaded", () => {
    const modalHTML = `
    <div id="changePassModal" class="modal-overlay" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:2000; justify-content:center; align-items:center;">
        <div class="modal-content" style="background:white; padding:30px; border-radius:10px; width:90%; max-width:400px; position:relative;">
            <span onclick="closeChangePassModal()" style="position:absolute; top:10px; right:15px; cursor:pointer; font-size:1.5rem;">&times;</span>
            <h3 class="mb-4">Change Password</h3>
            <form id="changePassForm" onsubmit="handlePasswordChange(event)">
                <div class="mb-3">
                    <label>Old Password</label>
                    <input type="password" id="cpOldPass" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label>New Password</label>
                    <input type="password" id="cpNewPass" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-dark w-100">Update Password</button>
            </form>
            <div id="cpMessage" class="mt-3 text-center small"></div>
        </div>
    </div>`;
    document.body.insertAdjacentHTML('beforeend', modalHTML);
});

window.openChangePassModal = function() {
    if (!sessionStorage.getItem('username')) {
        alert("Please log in to change your password.");
        return;
    }
    document.getElementById('changePassModal').style.display = 'flex';
};

window.closeChangePassModal = function() {
    document.getElementById('changePassModal').style.display = 'none';
    document.getElementById('cpMessage').innerText = "";
    document.getElementById('changePassForm').reset();
};

window.handlePasswordChange = async function(e) {
    e.preventDefault();
    
    const email = sessionStorage.getItem('userEmail');
    const oldPass = document.getElementById('cpOldPass').value;
    const newPass = document.getElementById('cpNewPass').value;
    const msgBox = document.getElementById('cpMessage');

    msgBox.innerText = "Processing...";
    msgBox.style.color = "black";

    try {
        const formData = new FormData();
        formData.append('email', email);
        formData.append('oldPassword', oldPass);
        formData.append('newPassword', newPass);

        const response = await fetch('/api/change-password', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            msgBox.style.color = "green";
            msgBox.innerText = "✅ " + result.message;
            setTimeout(() => {
                closeChangePassModal();
            }, 2000);
        } else {
            msgBox.style.color = "red";
            msgBox.innerText = "⚠️ " + result.message;
        }

    } catch (error) {
        console.error(error);
        msgBox.innerText = "Server error. Please try again.";
    }
};

/* ==========================
   MOBILE MENU TOGGLE
========================== */
window.toggleMobileMenu = function() {
    const navList = document.getElementById('mainNav');
    if (navList) navList.classList.toggle('active');
};

/* ==========================
   LOAD BRANDS
========================== */
function loadBrands() {
    const brandDropdown = document.getElementById("brandDropdown");
    if (!brandDropdown) return;

    fetch('/api/brands') 
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch brands");
            return response.json();
        })
        .then(brands => {
            brandDropdown.innerHTML = ""; 
            brands.forEach(brand => {
                const link = document.createElement("a");
                link.href = `shop.html?search=${encodeURIComponent(brand)}`; 
                link.textContent = brand;
                brandDropdown.appendChild(link);
            });
        })
        .catch(error => {
            console.error("Error loading brands:", error);
            brandDropdown.innerHTML = '<a href="#">Brands Unavailable</a>';
        });
}