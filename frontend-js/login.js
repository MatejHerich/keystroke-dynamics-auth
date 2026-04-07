const API_BASE = "/api/auth";

let keyData = [];
let keyDownTimes = {};
let previousKeyUpTimes = {};

const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const toggleFormLink = document.getElementById('toggleFormLink');
const formSubtitle = document.getElementById('formSubtitle');

toggleFormLink.addEventListener('click', (e) => {
    e.preventDefault();
    const showRegister = loginForm.style.display !== 'none';
    loginForm.style.display = showRegister ? 'none' : 'block';
    registerForm.style.display = showRegister ? 'block' : 'none';
    toggleFormLink.textContent = showRegister ? 'Máte účet? Prihláste sa' : 'Nemáte účet? Zaregistrujte sa';
    formSubtitle.textContent = showRegister ? 'Vytvorte si nový účet' : 'Zadajte údaje pre prístup k účtu';
});

passwordInput.addEventListener('keydown',(e) =>{
    if(!keyDownTimes[e.code]){
        keyDownTimes[e.code] = performance.now();
    }
});

usernameInput.addEventListener('keydown',(e)=>{
    if(!keyDownTimes[e.code]){
        keyDownTimes[e.code] = performance.now();
    }
})

usernameInput.addEventListener('keyup',(e)=>{
   const keyUpTime = performance.now();
   const keyDownTime = keyDownTimes[e.code];
   if(keyDownTime){
       const dwellTime = keyUpTime - keyDownTime;
       const previousKeyUpTime = previousKeyUpTimes.username;
       const record = {
           field: "username",
           key: e.key,
           dwell: dwellTime.toFixed(2),
           flight: previousKeyUpTime ? (keyDownTime - previousKeyUpTime).toFixed(2) : null,
           timestamp: Date.now()
       };
       keyData.push(record);
       previousKeyUpTimes.username = keyUpTime;
       delete keyDownTimes[e.code];
   }
});

passwordInput.addEventListener('keyup',(e)=>{
    const keyUpTime = performance.now();
    const keyDownTime = keyDownTimes[e.code];
    if(keyDownTime){
        const dwellTime = keyUpTime - keyDownTime;
        const previousKeyUpTime = previousKeyUpTimes.password;
        const record = {
            field: "password",
            key: e.key,
            dwell: dwellTime.toFixed(2),
            flight: previousKeyUpTime ? (keyDownTime - previousKeyUpTime).toFixed(2) : null,
            timestamp: Date.now()
        };
        keyData.push(record);
        previousKeyUpTimes.password = keyUpTime;
        delete keyDownTimes[e.code];
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        username: document.getElementById('regUsername').value,
        password: document.getElementById('regPassword').value,
        balance: document.getElementById('regBalance').value
    };
    try {
        const response = await fetch(`${API_BASE}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        alert(result.message);
        if (response.ok) {
            toggleFormLink.click();
        }
    } catch (error) {
        alert(error);
    }
});

loginForm.addEventListener('submit',async (e) => {
   e.preventDefault();
   const authPayload = {
       username: usernameInput.value,
       password: passwordInput.value,
       biometrics: keyData
   };
   try{
       const response = await fetch(`${API_BASE}/login`,{
           method: 'POST',
           headers: { 'Content-Type': 'application/json'},
           body: JSON.stringify(authPayload),
           credentials: 'include'
       });
       const result = await response.json();
       alert(result.message);
       if(response.ok){
           window.location.href = "dashboard.html";
       }
   }catch (error){
       alert(error);
   }
});
