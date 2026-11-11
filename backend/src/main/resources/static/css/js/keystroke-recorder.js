(function () {
    'use strict';

    const input = document.querySelector('[data-keystroke-input]');
    const output = document.querySelector('[data-keystroke-output]');
    const message = document.querySelector('[data-keystroke-message]');
    const submitButton = document.querySelector('[data-keystroke-submit]');

    if (!input || !output || !submitButton) {
        return;
    }
    const form = input.form;
    const CORRECTION_KEYS = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'];
    const RELEASE_TIMEOUT_MS = 1000;

    let presses = [];
    const held = new Map();
    let submitWaiting = false;

    function showMessage(text) {
        if (message) {
            message.textContent = text;
        }
    }

    function reset(text) {
        presses = [];
        held.clear();
        submitWaiting = false;
        input.value = '';
        output.value = '';
        showMessage(text);
    }

    function caretAtEnd() {
        return input.selectionStart === input.value.length && input.selectionEnd === input.value.length;
    }

    function isShortcut(event) {
        return (event.ctrlKey || event.metaKey) && !event.getModifierState('AltGraph');
    }

    input.addEventListener('keydown', function (event) {
        if (event.isComposing || event.key === 'Process') {
            event.preventDefault();
            reset('Píšte bežne, bez zadávania špeciálnych znakov cez systémové okno.');
            return;
        }
        if (event.repeat) {
            event.preventDefault();
            return;
        }
        if (CORRECTION_KEYS.includes(event.key)) {
            event.preventDefault();
            reset('Pri oprave sa heslo píše odznova. Napíšte ho celé bez opráv.');
            return;
        }
        if (isShortcut(event)) {
            event.preventDefault();
            return;
        }
        if (event.key.length !== 1) {
            return;
        }
        if (held.has(event.code) || !caretAtEnd()) {
            event.preventDefault();
            reset('Začnite písať heslo odznova.');
            return;
        }
        const press = {code: event.code, down: event.timeStamp, up: null};
        presses.push(press);
        held.set(event.code, press);
        showMessage('');
    });

    input.addEventListener('keyup', function (event) {
        const press = held.get(event.code);
        if (!press) {
            return;
        }
        press.up = event.timeStamp;
        held.delete(event.code);
        if (submitWaiting && held.size === 0) {
            submitWaiting = false;
            finishAndSubmit();
        }
    });

    input.addEventListener('input', function (event) {
        if (event.inputType !== 'insertText' || input.value.length !== presses.length) {
            reset('Heslo napíšte ručne. Vkladanie ani automatické vyplnenie nie je povolené.');
        }
    });

    ['paste', 'drop', 'cut'].forEach(function (type) {
        input.addEventListener(type, function (event) {
            event.preventDefault();
            showMessage('Heslo napíšte ručne. Vkladanie nie je povolené.');
        });
    });

    input.addEventListener('compositionstart', function () {
        reset('Píšte bežne, bez zadávania špeciálnych znakov cez systémové okno.');
    });

    function round(value) {
        return Math.round(value * 1000) / 1000;
    }

    function buildSample() {
        if (presses.length < 2 || presses.length !== input.value.length) {
            return null;
        }
        const start = presses[0].down;
        return presses.map(function (press) {
            return {downMs: round(press.down - start), upMs: round(press.up - start)};
        });
    }

    function finishAndSubmit() {
        const sample = buildSample();
        if (sample === null) {
            reset('Napíšte celé heslo bez opráv a potom ho odošlite.');
            return;
        }
        output.value = JSON.stringify(sample);
        form.submit();
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        if (held.size > 0) {
            submitWaiting = true;
            setTimeout(function () {
                if (submitWaiting) {
                    reset('Začnite písať heslo odznova.');
                }
            }, RELEASE_TIMEOUT_MS);
            return;
        }
        finishAndSubmit();
    });
    submitButton.disabled = false;
})();