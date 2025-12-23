/* assets/js/quiz-script.js */

let step = 0;
let answers = {
    ageGroup: "3-5", // Default
    budget: 5000, 
    interest: ""
};

const questions = [
    {
        id: "ageGroup",
        title: "How old is the lucky child? 🎂",
        options: [
            { text: "Toddler (0-2)", value: "0-2" },
            { text: "Preschool (3-5)", value: "3-5" },
            { text: "School Age (6-11)", value: "6-11" },
            { text: "Big Kid (12+)", value: "12+" }
        ]
    },
    {
        id: "interest",
        title: "What is their favorite theme? ❤️",
        options: [] // Will be populated dynamically
    },
    {
        id: "budget",
        title: "What is your budget? 💰",
        options: [
            { text: "Under PKR 2,000", value: 2000 },
            { text: "Under PKR 5,000", value: 5000 },
            { text: "Under PKR 10,000", value: 10000 },
            { text: "No Limit", value: 500000 }
        ]
    }
];

function initQuiz() {
    renderQuestion();
}

function renderQuestion() {
    const q = questions[step];
    if (!q) return;

    document.getElementById("progressText").innerText = `Question ${step + 1} of ${questions.length}`;
    document.getElementById("progressBar").style.width = `${((step + 1) / questions.length) * 100}%`;
    document.getElementById("questionTitle").innerText = q.title;

    const grid = document.getElementById("optionsGrid");
    grid.innerHTML = ""; 

    if (q.options.length === 0) {
        grid.innerHTML = `<div class="spinner-border text-warning"></div>`;
    }

    q.options.forEach(opt => {
        const btn = document.createElement("div");
        btn.className = "quiz-option";
        btn.innerText = opt.text;
        
        if(answers[q.id] === opt.value) {
            btn.classList.add("selected");
        }

        btn.onclick = () => selectOption(q.id, opt.value, btn);
        grid.appendChild(btn);
    });

    const hasSelection = answers[q.id] !== undefined && answers[q.id] !== "";
    document.getElementById("nextBtn").disabled = !hasSelection;
}

function selectOption(key, value, btnElement) {
    answers[key] = value;
    document.querySelectorAll(".quiz-option").forEach(el => el.classList.remove("selected"));
    btnElement.classList.add("selected");
    document.getElementById("nextBtn").disabled = false;
}

async function nextStep() {
    const nextBtn = document.getElementById("nextBtn");
    
    if (step === 0) {
        const age = answers.ageGroup;
        
        if (age === "0-2") {
            answers.interest = ""; 
            submitQuizToBackend();
            return;
        }

        nextBtn.disabled = true;
        nextBtn.innerText = "Loading...";
        
        try {
            const res = await fetch(`/api/quiz/categories?ageGroup=${encodeURIComponent(age)}`);
            const categories = await res.json();
            questions[1].options = categories.map(cat => ({ text: cat, value: cat }));
            step++;
            renderQuestion();
        } catch (err) {
            console.error(err);
            alert("Could not load categories.");
        } finally {
            nextBtn.innerText = "Next ➜";
            nextBtn.disabled = false; 
        }
        return;
    }

    if (step < questions.length - 1) {
        step++;
        renderQuestion();
    } else {
        submitQuizToBackend();
    }
}

function prevStep() {
    if (step > 0) {
        step--;
        renderQuestion();
    }
}

function submitQuizToBackend() {
    document.getElementById("questionContainer").classList.add("d-none");
    document.getElementById("navButtons").classList.add("d-none");
    document.getElementById("loadingState").classList.remove("d-none");

    fetch('/api/quiz/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(answers)
    })
    .then(response => response.json())
    .then(toys => showResults(toys))
    .catch(error => {
        console.error('Error:', error);
        document.getElementById("questionTitle").innerText = "Connection Error. Please try again.";
    });
}

/**
 * Fisher-Yates Shuffle Algorithm
 */
function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
}

/**
 * Pick a random subset of items from an array
 */
function getRandomSubset(arr, size) {
    const shuffled = shuffleArray([...arr]);
    return shuffled.slice(0, Math.min(size, arr.length));
}

function showResults(toys) {
    document.getElementById("loadingState").classList.add("d-none");
    const container = document.getElementById("resultsContainer");
    const grid = document.getElementById("resultsGrid");
    
    container.classList.remove("d-none");
    grid.innerHTML = "";

    if(!toys || toys.length === 0) {
        grid.innerHTML = `<p class="text-center text-muted">No exact magic matches found. Try increasing your budget! 🪄</p>`;
        return;
    }

    // Filter toys by budget
    const filteredToys = toys.filter(toy => toy.price <= answers.budget);

    // Pick up to 4 random toys
    const displayToys = getRandomSubset(filteredToys, 4);

    displayToys.forEach(toy => {
        const imgUrl = (toy.imageUrl && toy.imageUrl.length > 5) ? toy.imageUrl : "assets/img/logo2.png";
        
        const card = `
            <div class="col-md-3 col-6">
                <div class="product-card h-100 shadow-sm border-0" style="background: white; border-radius: 10px; overflow: hidden; padding-bottom: 10px; transition: transform 0.3s;">
                    <a href="product.html?id=${toy.id}" style="text-decoration:none; color:inherit;">
                        <img src="${imgUrl}" class="card-img-top" alt="${toy.name}" style="height: 150px; object-fit: contain; padding: 10px;">
                        <div class="card-body text-center">
                            <h6 class="card-title" style="font-size: 0.9rem; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${toy.name}</h6>
                            <p class="text-danger fw-bold mb-2">PKR ${toy.price}</p>
                            <span class="btn btn-sm btn-outline-danger w-75 rounded-pill">View Details</span>
                        </div>
                    </a>
                </div>
            </div>
        `;
        grid.innerHTML += card;
    });

    if (window.confetti) {
        window.confetti({ particleCount: 150, spread: 80, origin: { y: 0.6 } });
    }
}

function restartQuiz() {
    step = 0;
    answers = { ageGroup: "", budget: 5000, interest: "" };
    questions[1].options = []; 
    
    document.getElementById("resultsContainer").classList.add("d-none");
    document.getElementById("questionContainer").classList.remove("d-none");
    document.getElementById("navButtons").classList.remove("d-none");
    document.getElementById("navButtons").style.display = "flex";
    initQuiz();
}

initQuiz();
