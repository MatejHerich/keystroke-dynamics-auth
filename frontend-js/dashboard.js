const API_BASE = "/api/auth";
let currentUsername = null;
let behavioralProfileReady = false;
let trainingData = [];
let trainingDownTimes = {};
let trainingPreviousKeyUpTime = null;
let verificationData = [];
let verificationDownTimes = {};
let verificationPreviousKeyUpTime = null;

document.addEventListener("DOMContentLoaded",async ()=>{
    try{
        currentUsername = await loadSessionUser();
        const response = await fetch(`${API_BASE}/account-info`, {
            credentials: 'include'
        });
        const data = await response.json();
        if(!response.ok){
            throw new Error(data.message || "Session nie je platná.");
        }

        document.getElementById('welcomeUser').innerText = `Používateľ: ${data.username}`;
        updateBalanceDisplay(data.balance);
        document.getElementById('ibanDisplay').innerText = `IBAN: ${data.iban}`;

        await refreshBehavioralStatus();
        await loadBehavioralDebug();
        await loadTransactionHistory();
    }catch (e) {
        alert("Vaša relácia vypršala. Prihláste sa znova.");
        window.location.href = "login.html";
    }
});

document.getElementById('logoutBtn').addEventListener('click', async () => {
    try {
        await fetch(`${API_BASE}/logout`, {
            method: 'POST',
            credentials: 'include'
        });
    } finally {
        window.location.href = "login.html";
    }
});

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

async function loadSessionUser() {
    const response = await fetch(`${API_BASE}/session`, {
        credentials: 'include'
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa overiť session.');
    }

    return data.username;
}

async function loadTransactionHistory() {
    const response = await fetch(`${API_BASE}/transactions`, {
        credentials: 'include'
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa načítať transakcie.');
    }

    renderTransactionHistory(data);
}

async function refreshBehavioralStatus() {
    const response = await fetch(`${API_BASE}/behavioral-status`, {
        credentials: 'include'
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa načítať stav behaviorálneho profilu.');
    }

    renderBehavioralStatus(data);
}

async function loadBehavioralDebug() {
    const response = await fetch(`${API_BASE}/behavioral-debug`, {
        credentials: 'include'
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa načítať behaviorálny debug prehľad.');
    }

    renderBehavioralDebug(data);
}

function renderBehavioralStatus(status) {
    behavioralProfileReady = Boolean(status.paymentEnabled);

    document.getElementById('behavioralStatusHeadline').innerText = behavioralProfileReady
        ? 'Profil pripravený na autorizáciu'
        : 'Profil sa ešte učí';
    document.getElementById('behavioralStatusDetails').innerText = status.message;
    document.getElementById('trainingProgressText').innerText = behavioralProfileReady
        ? `Nazbierané vzorky: ${status.collectedSamples}/${status.requiredSamples}.`
        : `Nazbierané vzorky: ${status.collectedSamples}/${status.requiredSamples}. Zostáva doplniť ešte ${status.remainingSamples}.`;

    document.getElementById('trainingSection').style.display = behavioralProfileReady ? 'none' : 'block';
    document.getElementById('paymentSection').style.display = behavioralProfileReady ? 'block' : 'none';
}

function renderBehavioralDebug(debug) {
    const thresholds = debug.thresholds;
    document.getElementById('debugThresholdInfo').innerText =
        `Platba: ${thresholds.payment}, vyššia suma: ${thresholds.largePayment}, veľká suma: ${thresholds.veryLargePayment}`;

    const profile = debug.profile || {};
    document.getElementById('profileSummary').innerHTML = Object.keys(profile).length
        ? [
            `Priemerný dwell: ${formatMetric(profile.averageDwellTime)} ms`,
            `Priemerný flight: ${formatMetric(profile.averageFlightTime)} ms`,
            `Odchýlka dwell: ${formatMetric(profile.dwellDeviation)} ms`,
            `Odchýlka flight: ${formatMetric(profile.flightDeviation)} ms`,
            `Podiel dlhých pauz: ${formatMetric(profile.longPauseRatio)}`,
            `Referenčné pokusy: ${profile.referenceAttempts}`,
            `Referenčné vzorky: ${profile.referenceSamples}`
        ].join('<br>')
        : 'Profil zatiaľ nie je vytvorený.';

    document.getElementById('weightSummary').innerHTML = Object.entries(debug.weights)
        .map(([name, weight]) => `${name}: ${formatMetric(weight)}`)
        .join('<br>');

    const latestAttempt = debug.attempts && debug.attempts.length ? debug.attempts[0] : null;
    document.getElementById('latestAttemptSummary').innerHTML = latestAttempt
        ? [
            `Typ: ${latestAttempt.attemptType}`,
            `Výsledok: ${latestAttempt.authenticated ? 'úspech' : 'zlyhanie'}`,
            `Score: ${formatMetric(latestAttempt.confidenceScore)}`,
            `Threshold: ${formatMetric(latestAttempt.requiredThreshold)}`,
            `Samples: ${latestAttempt.sampleCount}`
        ].join('<br>')
        : 'Zatiaľ tu nie sú žiadne behaviorálne pokusy.';

    renderBehaviorAttempts(debug.attempts || []);
}

function renderBehaviorAttempts(attempts) {
    const tableBody = document.getElementById('behaviorAttemptBody');
    if (!attempts.length) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Zatiaľ tu nie sú žiadne behaviorálne pokusy.</td></tr>';
        return;
    }

    tableBody.innerHTML = attempts.map((attempt) => `
        <tr>
            <td>${formatTransactionDate(attempt.createdAt)}</td>
            <td>${attempt.attemptType}</td>
            <td class="${attempt.authenticated ? 'text-success' : 'text-danger'} fw-semibold">${attempt.authenticated ? 'úspech' : 'zlyhanie'}</td>
            <td>${formatMetric(attempt.confidenceScore)}</td>
            <td>${formatMetric(attempt.requiredThreshold)}</td>
            <td>${attempt.sampleCount}</td>
            <td class="small">${(attempt.evaluatorDetails || '').replaceAll('\n', '<br>')}</td>
        </tr>
    `).join('');
}

function formatMetric(value) {
    if (typeof value === 'undefined' || value === null) {
        return '-';
    }
    return Number(value).toFixed(2);
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
    if (!behavioralProfileReady) {
        alert("Platby budú dostupné až po dokončení pseudo-autorizácie a nazbieraní behaviorálnych vzoriek.");
        return;
    }
    if(verifyFields()){
        document.getElementById('paymentModal').style.display = 'block';
        verificationData = [];
        verificationDownTimes = {};
        verificationPreviousKeyUpTime = null;
        document.getElementById('verificationInput').value = '';
        document.getElementById('verificationInput').focus();
    }
}

function closeModal(){
    document.getElementById('paymentModal').style.display = 'none';
    document.getElementById('verificationInput').value = '';
    verificationData = [];
    verificationDownTimes = {};
    verificationPreviousKeyUpTime = null;
}

const trainingInput = document.getElementById('trainingVerificationInput');
trainingInput.addEventListener('keydown',(e) => {
   if(!trainingDownTimes[e.code]){
       trainingDownTimes[e.code] = performance.now();
   }
});

trainingInput.addEventListener('keyup',(e) => {
   const keyUpTime = performance.now();
   const keyDownTime = trainingDownTimes[e.code];
   if(keyDownTime){
       const dwellTime = keyUpTime - keyDownTime;
       trainingData.push({
           key: e.key,
           dwell: dwellTime.toFixed(2),
           flight: trainingPreviousKeyUpTime ? (keyDownTime - trainingPreviousKeyUpTime).toFixed(2) : null
       });
       trainingPreviousKeyUpTime = keyUpTime;
       delete trainingDownTimes[e.code];
   }
});

document.getElementById('submitTrainingBtn').addEventListener('click', async () => {
   const typedText = trainingInput.value.trim();
   if(typedText !== "potvrdzujem platbu"){
       alert("Fráza pre pseudo-autorizáciu nie je napísaná správne.");
       return;
   }
   if(!trainingData.length){
       alert("Chýbajú behaviorálne dáta pre tréning profilu.");
       return;
   }

   try{
       const response = await fetch(`${API_BASE}/behavioral-training`,{
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify({
               phraseBiometrics: trainingData
           }),
           credentials: 'include'
       });
       const result = await response.json();
       alert(result.message);
       if(response.ok){
           trainingInput.value = '';
           trainingData = [];
           trainingDownTimes = {};
           trainingPreviousKeyUpTime = null;
           await refreshBehavioralStatus();
           await loadBehavioralDebug();
       }
   }catch (e) {
       alert("Chyba pri ukladaní pseudo-autorizácie: " + e);
   }
});

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
           dwell: dwellTime.toFixed(2),
           flight: verificationPreviousKeyUpTime ? (keyDownTime - verificationPreviousKeyUpTime).toFixed(2) : null
       });
       verificationPreviousKeyUpTime = keyUpTime;
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
       recipientIban,
       amount,
       variableSymbol,
       paymentNote,
       phraseBiometrics: verificationData
   };
   try{
       const response = await fetch(`${API_BASE}/verify-payment`,{
           method: 'POST',
           headers: {'Content-Type': 'application/json'},
           body: JSON.stringify(paymentPayload),
           credentials: 'include'
       });
       const result = await response.json();
       if(response.ok){
           alert(result.message);
           if (typeof result.updatedBalance !== "undefined") {
               updateBalanceDisplay(result.updatedBalance);
           }
           await loadTransactionHistory();
           await refreshBehavioralStatus();
           await loadBehavioralDebug();
           closeModal();
           document.getElementById("transactionForm").reset();
       }else {
           alert("Chyba zo servera: " + result.message);
           await loadBehavioralDebug();
           if (result.paymentEnabled === false) {
               await refreshBehavioralStatus();
           }
        }
   }catch (e) {
       alert("Chyba pri overovaní: " + e);
   }
});
