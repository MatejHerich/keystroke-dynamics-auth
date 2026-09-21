# Behaviorálna autentifikácia používateľa vo webovej aplikácii

## Cieľ práce

Cieľom práce je navrhnúť softvérové riešenie behaviorálnej autentifikácie, ktoré správne identifikuje možné hrozby a zamedzí im prístup. Získané dáta bude možné ďalej analyzovať.

Nezameriavam sa na to, čo používateľ píše, ale na to, ako to píše. Vďaka tomu vieme určiť, či sa prihlasuje skutočný používateľ, alebo bot či iný neželaný aktér.

## Základná myšlienka

Testovacím prostredím je jednoduchá webová bankovná aplikácia. Používateľ sa prihlási menom a heslom, pričom heslo sa neukladá ako plain text, ale iba v hashovanej forme.

Po prihlásení sa dostane do bankovej aplikácie, kde môže robiť vklady, výbery a ďalšie akcie. Pri každej akcii musí znovu zadať heslo. Počas jeho zadávania sa zbierajú údaje o spôsobe písania a porovnajú sa s údajmi v databáze.

## Evaluatory

Chcem vytvoriť viacero evaluatorov, z ktorých každý vyhodnocuje inú hrozbu. Ich výsledky sa spoja do jedného skóre a podľa stanovenej hranice sa určí, či ide o nášho používateľa alebo nie.

## Návrh štruktúry

Klient v prehliadači pracuje s našou webovou stránkou (HTML a JS). Údaje získané pomocou JavaScriptu sa posielajú na backend, kde sa uložia do databázy alebo vyhodnotia, a klientovi sa pošle odpoveď.
