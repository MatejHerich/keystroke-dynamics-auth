const API_BASE = "/api/auth";

let keyData = [];
let keyDownTimes = {};
let previousKeyUpTimes = {};

const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const loginForm = document.getElementById('loginForm');

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
