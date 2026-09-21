const ENROLL_TOTAL = 5;

const registrationForm = document.getElementById('registration-form');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const confirmInput = document.getElementById('password-confirm');
const message = document.getElementById('message');

const modalElement = document.getElementById('enrollModal');
const enrollInput = document.getElementById('enroll-password');
const enrollCount = document.getElementById('enroll-count');
const enrollMessage = document.getElementById('enroll-message');

const enrollModal = new bootstrap.Modal(modalElement);

let samples = [];
let events = [];
let startTime = null;

function resetAttempt() {
    events = [];
    startTime = null;
    enrollInput.value = '';
}

function recordEvent(e, type) {
    if (e.repeat) return;

    if (e.code === 'Enter' || e.code === 'NumpadEnter') return;

    if (e.code === 'Backspace' || e.code === 'Delete') {
        if (type === 'down') resetAttempt();
        return;
    }

    const now = performance.now();
    if (startTime === null) startTime = now;

    events.push({ code: e.code, type: type, t: now - startTime });
}

function toKeyPresses(events) {
    const pressed = new Map();
    const presses = [];

    for (const ev of events) {
        if (ev.type === 'down') {
            pressed.set(ev.code, ev.t);
        } else if (pressed.has(ev.code)) {
            presses.push({ down_ms: pressed.get(ev.code), up_ms: ev.t });
            pressed.delete(ev.code);
        }
    }

    presses.sort((a, b) => a.down_ms - b.down_ms);

    return presses.map((p, i) => ({
        position: i,
        down_ms: p.down_ms,
        up_ms: p.up_ms
    }));
}

registrationForm.addEventListener('submit', (e) => {
    e.preventDefault();
    message.textContent = '';

    if (passwordInput.value !== confirmInput.value) {
        message.textContent = 'Heslá sa nezhodujú.';
        return;
    }

    samples = [];
    enrollCount.textContent = 1;
    enrollMessage.textContent = '';
    resetAttempt();
    enrollModal.show();
});
modalElement.addEventListener('shown.bs.modal', () => enrollInput.focus());

enrollInput.addEventListener('paste', (e) => e.preventDefault());

enrollInput.addEventListener('keydown', (e) => {
    if (e.code === 'Enter' || e.code === 'NumpadEnter') {
        e.preventDefault();
        handleEnter();
        return;
    }
    recordEvent(e, 'down');
});

enrollInput.addEventListener('keyup', (e) => recordEvent(e, 'up'));

function handleEnter() {
    if (enrollInput.value !== passwordInput.value) {
        enrollMessage.textContent = 'Heslo sa nezhoduje, skús znova.';
        resetAttempt();
        return;
    }

    samples.push(toKeyPresses(events));
    enrollMessage.textContent = '';
    resetAttempt();

    if (samples.length < ENROLL_TOTAL) {
        enrollCount.textContent = samples.length + 1;
    } else {
        finishRegistration();
    }
}

function finishRegistration() {
    const payload = {
        username: usernameInput.value.trim(),
        password: passwordInput.value,
        samples: samples
    };

    console.log(payload);
    // TODO: odoslať payload na backend cez fetch


    window.location.href = "login.html";
    enrollModal.hide();
}