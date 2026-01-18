let keyData = [];
let keyDownTimes = {};

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
       const record = {
           field: "username",
           key: e.key,
           dwell: dwellTime.toFixed(2),
           timestamp: Date.now()
       };
       keyData.push(record);
       delete keyDownTimes[e.code];
   }
});

passwordInput.addEventListener('keyup',(e)=>{
    const keyUpTime = performance.now();
    const keyDownTime = keyDownTimes[e.code];
    if(keyDownTime){
        const dwellTime = keyUpTime - keyDownTime;
        const record = {
            field: "password",
            key: e.key,
            dwell: dwellTime.toFixed(2),
            timestamp: Date.now()
        };
        keyData.push(record);
        delete keyDownTimes[e.code];
    }
});

loginForm.addEventListener('submit',async (e) => {
   e.preventDefault();
   const username = document.getElementById('username').value;
   const password = passwordInput.value;
   const authPayload = {
       username: usernameInput.value,
       password: passwordInput.value,
       biometrics: keyData
   };
   try{
       const response = await fetch("http://localhost:8080/api/auth/login",{
           method: 'POST',
           headers: { 'Content-Type': 'application/json'},
           body: JSON.stringify(authPayload)
       });
       const resultText = await  response.text();
       alert(resultText);
       if(resultText.includes("úspešné")){
           localStorage.setItem('loggedUser',usernameInput.value);
           window.location.href = "dashboard.html";
       }
   }catch (error){
       alert(error);
   }
});