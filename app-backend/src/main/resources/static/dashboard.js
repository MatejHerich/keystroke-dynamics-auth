(function() {
    const user = localStorage.getItem('loggedUser');
    if (!user) {
        alert("Nepovolený prístup! Musíte sa prihlásiť.");
        window.location.href = "login.html";
    }
})();

document.addEventListener("DOMContentLoaded",async ()=>{
    const username = localStorage.getItem('loggedUser');
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