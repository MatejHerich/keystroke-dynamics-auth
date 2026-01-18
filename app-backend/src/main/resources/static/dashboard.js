(function() {
    const user = sessionStorage.getItem('loggedUser');
    const loginTime = sessionStorage.getItem('loginTimestamp');
    if(!user){
        window.location.href = "login.html";
        return;
    }
    const now = new Date().getTime();
    const expireLimit = 30*60*1000;
    if (!loginTime || (now - Number(loginTime) > expireLimit)) {
        sessionStorage.removeItem('loggedUser');
        sessionStorage.removeItem('loginTimestamp');
        alert("Vaša relácia vypršala. Prihláste sa znova.");
        window.location.href = "login.html";
    }
})();

document.addEventListener("DOMContentLoaded",async ()=>{
    const username = sessionStorage.getItem('loggedUser');
    if(!username){
        window.location.href = "login.html";
        return;
    }
    try{
        const response = await fetch(`http://localhost:8080/api/auth/account-info/${username}`);
        const data = await response.json();

        document.getElementById('welcomeUser').innerText = `Používateľ: ${data.username}`;
        document.getElementById('balanceDisplay').innerText = `${data.balance.toLocaleString('sk-SK')} €`;
        document.getElementById('ibanDisplay').innerText = `IBAN: ${data.iban}`;
        
    }catch (e) {
        alert("Chyba pri komunikacii s DB: ",e);
    }
});

let verificationData = [];
let verificationDownTimes = {};

function openPaymentModal(){
    document.getElementById('paymentModal').style.display = 'block';
    document.getElementById('verificationInput').focus();
}

function closeModal(){
    document.getElementById('paymentModal').style.display = 'none';
    document.getElementById('verificationInput').value = '';
    verificationData = [];
}

const vInput = document.getElementById('verificationInput');

vInput.addEventListener('keydown',(e) => {
   if(!verificationDownTimes[e.code]){
       verificationDownTimes[e.code] = performance.now();
   }
});

vInput.addEventListener('keyup',(e) => {
   const keyUpTime = performance.now();
   const kedDownTime = verificationDownTimes[e.code];
   if(kedDownTime){
       const dwellTime = keyUpTime - keyDownTime;
       verificationData.push({
           key: e.key,
           dwell: dwellTime.toFixed(2)
       });
       delete verificationDownTimes[e.code];
   }
});

document.getElementById('confirmPaymentBtn').addEventListener('click', async () =>{
   const typedtext = vInput.value;
   if(typedtext !== "potvrdzujem platbu"){
       alert("Fráza nie je napísaná správne!");
       return;
   }
   const paymentPayload = {
       username:sessionStorage.getItem('loggedUser'),
       phraseBiometrics: verificationData
   };
   try{
       const response = await fetch("api/auth/verify-payment",{
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify(paymentPayload)
       });
       const result = await response.text();
       alert(result)
       closeModal();
   }catch (e) {
       alert("Chyba pri overovaní: "+e);
   }
});