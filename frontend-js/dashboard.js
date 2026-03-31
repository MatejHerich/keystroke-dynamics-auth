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
        updateBalanceDisplay(data.balance);
        document.getElementById('ibanDisplay').innerText = `IBAN: ${data.iban}`;

        await loadTransactionHistory(username);
    }catch (e) {
        alert("Chyba pri komunikacii s DB: ",e);
    }
});

let verificationData = [];
let verificationDownTimes = {};

function updateBalanceDisplay(balance) {
    document.getElementById('balanceDisplay').innerText = `${Number(balance).toLocaleString('sk-SK')} €`;
}

function formatTransactionDate(value) {
    if (!value) {
        return "-";
    }
    return new Date(value).toLocaleString('sk-SK');
}

function renderTransactionHistory(transactions) {
    const tableBody = document.getElementById('transactionHistoryBody');
    const counter = document.getElementById('transactionCount');

    counter.innerText = `${transactions.length} záznamov`;

    if (!transactions.length) {
        tableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">Zatiaľ tu nie sú žiadne transakcie.</td></tr>';
        return;
    }

    tableBody.innerHTML = transactions.map((transaction) => `
        <tr>
            <td>${formatTransactionDate(transaction.transactionDate)}</td>
            <td class="fw-semibold">${transaction.recipientIban}</td>
            <td>${transaction.description || '-'}</td>
            <td class="text-end fw-bold text-danger">-${Number(transaction.amount).toLocaleString('sk-SK', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €</td>
        </tr>
    `).join('');
}

async function loadTransactionHistory(username) {
    const response = await fetch(`http://localhost:8080/api/auth/transactions/${username}`);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa načítať transakcie.');
    }

    renderTransactionHistory(data);
}

function verifyFields(){
  const iban = document.getElementById("recipientIban").value.trim();
  const amount = document.getElementById("amount").value.trim();
  const variableSymbol = document.getElementById("variableSymbol").value.trim();
  if(!iban){
      alert("Nezadaný iban");
      return false;
  }
  if(!amount || amount <=0){
      alert("Zle zadaná suma");
      return false;
  }
  if(!variableSymbol){
      alert("Zle zadaný symbol")
      return false;
  }
  return true;


}

function openPaymentModal(){
    if(verifyFields()){
        document.getElementById('paymentModal').style.display = 'block';
        verificationData = [];
        verificationDownTimes = {};
        document.getElementById('verificationInput').value = '';
        document.getElementById('verificationInput').focus();
    }
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
   const keyDownTime = verificationDownTimes[e.code];
   if(keyDownTime){
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
   const recipientIban = document.getElementById("recipientIban").value.trim().replace(/\s+/g, '').toUpperCase();
   const amount = Number(document.getElementById("amount").value);
   const variableSymbol = document.getElementById("variableSymbol").value.trim();
   const paymentNote = document.getElementById("paymentNote").value.trim();
   const paymentPayload = {
       username:sessionStorage.getItem('loggedUser'),
       recipientIban,
       amount,
       variableSymbol,
       paymentNote,
       phraseBiometrics: verificationData
   };
   try{
       const response = await fetch("http://localhost:8080/api/auth/verify-payment",{
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify(paymentPayload)
       });
       const result = await response.json();
       if(response.ok){
           alert(result.message);
           if (typeof result.updatedBalance !== "undefined") {
               updateBalanceDisplay(result.updatedBalance);
           }
           await loadTransactionHistory(sessionStorage.getItem('loggedUser'));
           closeModal();
           document.getElementById("transactionForm").reset();
       }else {
           alert("Chyba zo servera: " + result.message);
        }
   }catch (e) {
       alert("Chyba pri overovaní: " + e);
   }
});
