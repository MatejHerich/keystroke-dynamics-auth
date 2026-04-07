<h1 align="center">Dokumentácia aplikácie</h1>

<p align="center"><b>Internet banking s behaviorálnou biometriou založenou na keystroke dynamics</b></p>

---

## 1. Úvod

<u><b>Základná myšlienka:</b></u>

Táto aplikácia predstavuje jednoduchý model internet bankingu, ktorý je rozšírený o behaviorálnu biometriu založenú na dynamike písania na klávesnici. Hlavnou myšlienkou projektu je spojiť bežné funkcie bankovej webovej aplikácie, ako je prihlásenie, zobrazenie účtu, odoslanie platby a história transakcií, s overovaním identity používateľa na základe jeho spôsobu písania.

Projekt je implementovaný v jazyku Java pomocou frameworku Spring Boot. Frontend je vytvorený pomocou HTML, CSS, Bootstrapu a JavaScriptu. Dáta sú ukladané do MySQL databázy. Celý systém je navrhnutý tak, aby sa dal postupne rozširovať. Najskôr bola vytvorená základná banková časť aplikácie. Následne boli doplnené biometrické údaje pri prihlasovaní, potom autorizácia platby, vykonanie samotnej transakcie, história transakcií a nakoniec aj začiatok behaviorálneho vyhodnocovania.

Z môjho pohľadu je tento projekt dôležitý najmä preto, že neslúži len na vytvorenie jednoduchej webovej stránky, ale prepája viacero oblastí:

- webovú aplikáciu,
- backendovú logiku,
- databázovú vrstvu,
- transakčné spracovanie,
- behaviorálnu analýzu používateľa.

> Dokumentácia je písaná tak, aby sa dala použiť nielen ako technický popis, ale aj ako študijný materiál pri ďalšom rozširovaní projektu.

---

## 2. Hlavný cieľ aplikácie

<u><b>Čo má aplikácia dosiahnuť:</b></u>

Hlavným cieľom aplikácie je vytvoriť systém, v ktorom sa používateľ:

- prihlási pomocou mena a hesla,
- počas prihlasovania sa zaznamenávajú jeho keystroke údaje,
- po prihlásení vidí svoj účet, zostatok a históriu transakcií,
- môže vytvoriť novú platbu,
- pred vykonaním platby musí potvrdiť špeciálnu frázu,
- systém vyhodnotí správanie pri písaní tejto frázy,
- až následne dovolí alebo zamietne vykonanie transakcie.

To znamená, že aplikácia nepracuje len s tým, čo používateľ napíše, ale aj s tým, ako to napíše.

> Inak povedané: systém neoveruje len obsah vstupu, ale aj štýl jeho zadania.

---

## 3. Technologický základ projektu

<u><b>Použité technológie:</b></u>

V projekte sú použité tieto technológie:

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MySQL
- HTML
- CSS
- Bootstrap
- JavaScript
- Maven

Backend je postavený ako monolitická Spring Boot aplikácia. To znamená, že backendové API aj statické frontend súbory sú uložené v jednom projekte. Zároveň existuje aj priečinok `frontend-js`, kde sú samostatne uložené frontendové súbory mimo Spring Boot static priečinka.

---

## 4. Štruktúra projektu

<u><b>Rozdelenie projektu:</b></u>

Projekt sa skladá z dvoch hlavných častí:

- `app-backend`
- `frontend-js`

Backendová logika je umiestnená v priečinku `app-backend/src/main/java/com/example/appbackend`. Táto časť je dnes ďalej rozdelená do logických balíkov:

- `controller`
- `service`
- `repository`
- `model`
- `behavior`

Toto rozdelenie pomáha odlíšiť webovú vrstvu, business logiku, databázové entity a samotnú behaviorálnu vrstvu.

Frontend existuje v dvoch umiestneniach:

- `app-backend/src/main/resources/static`
- `frontend-js`

V oboch prípadoch sa používajú rovnaké súbory:

- `login.html`
- `login.js`
- `dashboard.html`
- `dashboard.js`

Dôvod je praktický. Jedna verzia je priamo napojená na Spring Boot aplikáciu ako statický frontend a druhá verzia slúži ako samostatná frontendová kópia.

Z praktického pohľadu je dobré si celý projekt predstaviť ako tri vrstvy, ktoré nad sebou stoja:

- frontend, kde používateľ zadáva údaje a vidí výsledok,
- backend, kde sa robí rozhodovanie a logika aplikácie,
- databáza, kde sa trvalo ukladajú používatelia, účty, transakcie a biometrické vzorky.

Keď si túto základnú štruktúru udržím v hlave, oveľa ľahšie sa mi následne chápu aj jednotlivé triedy a ich vzájomné väzby.

Celý projekt sa dá veľmi zjednodušene predstaviť aj takto:

```text
Pouzivatel
   |
   v
Frontend (HTML + JS)
   |
   v
Backend (Controller + Service)
   |
   v
Repository vrstva
   |
   v
MySQL databaza
```

Tento základný diagram hovorí, že používateľ nikdy nekomunikuje priamo s databázou. Všetko ide cez frontend a backendovú logiku.

> <b>Dôležitý pohľad:</b> ak rozumiem tejto trojvrstvovej schéme, oveľa ľahšie pochopím aj správanie controllerov, services a repository vrstvy.

---

## 5. Databázový model aplikácie

<u><b>Jadro databázy:</b></u>

Databáza je navrhnutá v súbore `schema.sql`. V aktuálnom stave sú použité najmä tieto tabuľky:

- `users`
- `accounts`
- `biometric_samples`
- `behavior_attempts`
- `verification_samples`
- `behavioral_profiles`
- `transactions`

Tabuľka `users` obsahuje základné údaje o používateľovi, teda `id`, `username` a `password`. V aktuálnom stave sa heslo už neukladá ako plain text, ale ako hash. Tabuľka `accounts` reprezentuje bankový účet používateľa a obsahuje `id`, `user_id`, `iban` a `balance`. Medzi používateľom a účtom je vzťah jeden používateľ = jeden účet.

Tabuľka `biometric_samples` obsahuje biometrické dáta zaznamenané pri prihlasovaní. Ukladá, ku ktorému používateľovi vzorka patrí, z ktorého poľa pochádza, ktorý kláves bol stlačený, aká bola dĺžka držania klávesu a aký bol `flight_time`. Táto tabuľka teda slúži ako prvotný zdroj behaviorálnych dát.

Tabuľka `behavior_attempts` reprezentuje jednu behaviorálnu reláciu, teda jeden tréningový alebo platobný pokus. Ukladá sa sem používateľ, typ pokusu, výsledné skóre, požadovaný threshold, informácia o úspechu, počet vzoriek, detail evaluatorov a čas pokusu.

Tabuľka `verification_samples` obsahuje jednotlivé vzorky patriace ku konkrétnemu behaviorálnemu pokusu. Každá vzorka už má `attempt_id`, `sample_index`, `key_pressed`, `dwell_time` a `flight_time`. Vďaka tomu sa dá porovnávať celá relácia a nie len jeden dlhý zoznam kláves.

Tabuľka `behavioral_profiles` predstavuje normalizovaný profil používateľa. Ukladajú sa tu priemerné hodnoty `dwell time`, `flight time`, odchýlky, podiel dlhých pauz a počet referenčných pokusov a vzoriek.

Tabuľka `transactions` slúži na evidenciu vykonaných platieb. Obsahuje `id`, `account_id`, `recipient_iban`, `amount`, `description`, `transaction_type` a `transaction_date`. Pole `transaction_type` rozlišuje, či ide o odchádzajúcu alebo prichádzajúcu transakciu. Táto tabuľka je základom pre históriu transakcií v dashboarde.

Ak by som mal databázový model zhrnúť veľmi jednoducho, tak jadro aplikácie tvorí dvojica používateľ a účet. Na túto dvojicu sa potom pripájajú dva rôzne typy dát:

- finančné dáta, teda transakcie,
- behaviorálne dáta, teda biometrické vzorky, relácie pokusov a profil používateľa.

Práve toto rozdelenie je dôležité, pretože ukazuje, že aplikácia nie je len banková alebo len biometrická. Obe časti sú navzájom prepojené.

Databázové jadro aplikácie si môžem predstaviť aj týmto spôsobom:

```text
users ---- 1:1 ---- accounts
  |                    |
  |                    +---- 1:N ---- transactions
  |
  +---- 1:N ---- biometric_samples

username ---- 1:N ---- behavior_attempts ---- 1:N ---- verification_samples
   |
   +---- 1:1 ---- behavioral_profiles
```

Z diagramu je vidieť, že používateľ je naviazaný na účet a zároveň na svoje behaviorálne dáta. Tým sa spája klasická banková časť s biometrickou časťou systému.

> <b>Zhrnutie kapitoly:</b> databáza uchováva dve hlavné skupiny údajov: finančné údaje a behaviorálne údaje. Obe skupiny sú naviazané na používateľa.

---

## 6. Entity triedy

<u><b>Úloha entít:</b></u>

Entity sú triedy, ktoré reprezentujú databázové tabuľky. Trieda `User` reprezentuje používateľa systému. Trieda `Account` reprezentuje bankový účet používateľa. Trieda `BiometricSample` reprezentuje jednu zaznamenanú vzorku pri prihlasovaní. Trieda `VerificationSample` reprezentuje jednu vzorku pri overovaní platby. Trieda `Transaction` reprezentuje bankovú transakciu. Pri vykonaní platby sa vytvorí nový objekt tejto triedy a následne sa uloží do databázy.

Z pohľadu pochopenia projektu je dôležité uvedomiť si, že entity nie sú len „obyčajné Java triedy“. Sú to objekty, na ktorých stojí celý model aplikácie. Každý controller, repository alebo service pracuje práve s nimi. Preto keď rozumiem entitám, ľahšie rozumiem aj celému backendu.

---

## 7. Repository a komunikácia s databázou

<u><b>Úloha repository vrstvy:</b></u>

Repository rozhrania sú použité na komunikáciu s databázou pomocou Spring Data JPA. V tejto časti nie je potrebné rozoberať každú bežnú databázovú operáciu samostatne, pretože veľkú časť z nich poskytuje Spring automaticky. Podstatné je skôr to, aké dotazy sú dôležité pre logiku aplikácie.

`UserRepository` obsahuje metódu `findByUsername`, vďaka ktorej sa vie celý systém orientovať podľa mena používateľa. `AccountRepository` obsahuje metódy `findByUser` a `findByIban`, čo je potrebné pre načítanie účtu prihláseného používateľa aj pre spracovanie transakcie. `BiometricRepository` obsahuje metódu `findByUserOrderByIdAsc`, teda vie načítať biometrické vzorky používateľa v správnom poradí. `VerificationSampleRepository` dnes už nepracuje len s používateľským menom, ale vie načítať vzorky aj podľa konkrétneho pokusu. `BehaviorAttemptRepository` pracuje s jednotlivými reláciami behaviorálneho overenia a `BehavioralProfileRepository` ukladá normalizovaný profil používateľa.

Pri `TransactionRepository` je z pohľadu funkcionality najdôležitejšia nasledujúca metóda:

```java
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Vracia transakcie konkrétneho účtu od najnovšej po najstaršiu.
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);
}
```

Táto metóda vracia transakcie konkrétneho účtu od najnovšej po najstaršiu. Práve preto je použitá pri histórii transakcií na dashboarde. Repository vrstva tak tvorí spojenie medzi databázou a business logikou bez toho, aby controller alebo service triedy museli priamo písať SQL.

> <b>Praktický význam:</b> repository vrstva oddeľuje databázovú komunikáciu od aplikačnej logiky. Vďaka tomu je kód čistejší a prehľadnejší.

---

## 8. Backendový controller

<u><b>Hlavný vstup do backendu:</b></u>

Hlavným controllerom aplikácie je `AuthController`. Tento controller momentálne zabezpečuje prihlasovanie, načítanie údajov o účte, načítanie histórie transakcií, overenie platby a spustenie behaviorálneho overenia. Aj keď názov `AuthController` naznačuje hlavne autentifikáciu, v aktuálnom stave projektu obsahuje aj logiku súvisiacu s účtom a platbami.

Inými slovami, `AuthController` je aktuálne hlavný vstupný bod do backendu. Všetko, čo používateľ robí vo fronte, sa vo väčšine prípadov skončí práve tu. Preto je to jedna z najdôležitejších tried v celom projekte.

### 8.1 Endpoint `/api/auth/register`

<b>Úloha endpointu:</b> vytvoriť nového používateľa a automaticky mu založiť účet.

Najnovšie bola do aplikácie doplnená aj registrácia používateľa. Tento endpoint prijíma používateľské meno, heslo a voliteľný počiatočný zostatok. Backend najprv skontroluje, či boli zadané povinné údaje a či používateľ s rovnakým menom už neexistuje. Ak všetko prejde, vytvorí sa nový `User`, následne nový `Account` a používateľ dostane automaticky vygenerovaný IBAN. Ešte pred uložením sa však heslo zahashuje, takže databáza neuchováva jeho pôvodný textový tvar.

Najprv sa spracujú vstupné údaje a základné validácie:

```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Map<String, Object> data) {
    String username = (String) data.get("username");
    String password = (String) data.get("password");
    Double balance = data.get("balance") == null ? 0.0 : Double.parseDouble(data.get("balance").toString());

    if (username == null || username.isBlank() || password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(Map.of("message", "Meno a heslo sú povinné."));
    }
    if (userRepository.findByUsername(username) != null) {
        return ResponseEntity.status(409).body(Map.of("message", "Používateľ s týmto menom už existuje."));
    }
```

Potom sa vytvorí používateľ, účet a vygeneruje sa jeho IBAN:

```java
    User user = new User();
    user.setUsername(username);
    user.setPassword(hashPassword(password));
    userRepository.save(user);

    String iban = generateIban(user.getId());
    Account account = new Account();
    account.setUser(user);
    account.setIban(iban);
    account.setBalance(balance);
    accountRepository.save(account);

    return ResponseEntity.ok(Map.of(
            "message", "Registrácia úspešná. Môžete sa prihlásiť.",
            "iban", iban
    ));
}
```

Pomocná metóda pre IBAN je momentálne jednoduchá a deterministická:

```java
private String generateIban(Long userId) {
    String accountNumber = String.format("%016d", userId);
    return "SK00" + "1100" + accountNumber;
}
```

Táto registrácia je dôležitá najmä z pohľadu kompletizácie aplikácie. Projekt už nie je odkázaný len na ručne vložených používateľov v databáze. Nový účet si vie vytvoriť aj samotný používateľ cez frontend rozhranie.

Zároveň je to dôležitý bezpečnostný posun. Aj keď systém zatiaľ nepoužíva plnohodnotný Spring Security stack, aspoň základná vrstva ochrany hesiel už funguje korektnejšie než pôvodné plain text riešenie.

### 8.2 Endpoint `/api/auth/login`

<b>Úloha endpointu:</b> spracovanie prihlásenia a prvotný zber behaviorálnych dát.

Tento endpoint spracováva prihlasovanie používateľa. Frontend pošle `username`, `password` a pole biometrických vzoriek. Backend následne nájde používateľa podľa mena, porovná zadané heslo s uloženým hashom, pri úspešnom prihlásení uloží biometrické vzorky do `biometric_samples` a vráti textovú správu o výsledku. Tento endpoint je základom prvého behaviorálneho zberu dát.

Najprv sa z requestu načítajú základné údaje:

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, Object> data, HttpSession session, HttpServletRequest request) {
    // Hodnoty prídu z frontend formulára vo forme JSON objektu.
    String username = (String) data.get("username");
    String password = (String) data.get("password");
    List<Map<String, Object>> biometrics = (List<Map<String, Object>>) data.get("biometrics");
    User existingUser = userRepository.findByUsername(username);
```

Potom nasleduje samotné overenie a uloženie behaviorálnych vzoriek:

```java
    boolean authenticated = false;
    if (existingUser != null) {
        if (isHashedPassword(existingUser.getPassword())) {
            authenticated = matchesPassword(password, existingUser.getPassword());
        } else if (existingUser.getPassword().equals(password)) {
            authenticated = true;
            existingUser.setPassword(hashPassword(password));
            userRepository.save(existingUser);
        }
    }

    if (authenticated) {
        session.invalidate();
        HttpSession authenticatedSession = request.getSession(true);
        authenticatedSession.setAttribute("loggedUser", existingUser.getUsername());

        for (Map<String, Object> sampleData : biometrics == null ? List.<Map<String, Object>>of() : biometrics) {
            BiometricSample sample = new BiometricSample();
            // Každá frontendom zachytená vzorka sa prevedie na entitu.
            sample.setUser(existingUser);
            sample.setFieldName((String) sampleData.get("field"));
            sample.setKeyPressed((String) sampleData.get("key"));
            sample.setDwellTime(Double.parseDouble(sampleData.get("dwell").toString()));
            sample.setFlightTime(sampleData.get("flight") == null ? null : Double.parseDouble(sampleData.get("flight").toString()));
            biometricRepository.save(sample);
        }
        return ResponseEntity.ok(Map.of(
                "message", "Prihlásenie úspešné. Dáta pre biometriu boli zaznamenané.",
                "username", existingUser.getUsername()
        ));
    } else {
        return ResponseEntity.status(401).body(Map.of("message", "Chyba: Nesprávne meno alebo heslo!"));
    }
}
```

Z tejto implementácie je vidieť, že `AuthController` pri login procese nerieši iba autentifikáciu, ale zároveň aj ukladanie behaviorálnych vzoriek a vytvorenie serverovej session. Frontend pošle meno, heslo a zoznam objektov s biometrickými údajmi. Backend najprv bezpečne porovná heslo s hashom, prípadne automaticky premigruje starší plain text účet na hash, a až potom uloží biometrické dáta a identitu používateľa do session.

Toto je zároveň prvý moment, kde sa ukazuje hlavná filozofia aplikácie. Systém nekontroluje len to, či používateľ zadal správne údaje, ale zároveň si začína ukladať aj spôsob, akým ich zadal.

Tok prihlasovania si viem predstaviť aj takto:

```text
Login formular
   |
   +-- username
   +-- password
   +-- keystroke data
            |
            v
POST /api/auth/login
            |
            v
AuthController
            |
            +-- overenie mena a hesla
            +-- ulozenie BiometricSample
            |
            v
odpoved pre frontend
```

### 8.3 Endpoint `/api/auth/account-info`

<b>Úloha endpointu:</b> načítať údaje o účte pre dashboard na základe aktívnej session.

Tento endpoint načíta údaje o účte prihláseného používateľa. Na rozdiel od staršej verzie už frontend neposiela používateľské meno v URL adrese. Backend si najprv z `HttpSession` vyžiada atribút `loggedUser`, podľa neho nájde používateľa a následne aj jeho účet. Vracia používateľské meno, zostatok a IBAN. Tieto údaje sa potom zobrazujú v dashboarde.

### 8.4 Endpoint `/api/auth/transactions`

<b>Úloha endpointu:</b> dodať dashboardu zoznam vykonaných transakcií prihláseného používateľa.

Tento endpoint načíta históriu transakcií používateľa. Postup je nasledovný:

1. backend si zo session načíta identitu prihláseného používateľa,
2. podľa používateľa nájde účet,
3. podľa účtu načíta transakcie zoradené od najnovšej,
4. vytvorí zoznam odpovedí pre frontend.

Najprv sa overí session a následne účet:

```java
@GetMapping("/transactions")
public ResponseEntity<?> transactionHistory(HttpSession session) {
    // Identita používateľa už nie je v URL, ale v serverovej session.
    User user = getAuthenticatedUser(session);
    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }

    // Potom sa vyhľadá účet patriaci tomuto používateľovi.
    Account account = accountRepository.findByUser(user);
    if (account == null) {
        return ResponseEntity.badRequest().body(Map.of("message", "Účet používateľa neexistuje."));
    }
```

Až potom sa transakcie prevedú na odpoveď pre frontend:

```java
    List<Map<String, Object>> response = transactionRepository.findByAccountOrderByTransactionDateDesc(account)
            .stream()
            .map(transaction -> {
                Map<String, Object> item = new HashMap<>();
                String transactionType = transaction.getTransactionType() == null
                        ? "OUTGOING"
                        : transaction.getTransactionType().trim().toUpperCase(Locale.ROOT);
                double amount = transaction.getAmount() == null ? 0.0 : transaction.getAmount();
                double signedAmount = "INCOMING".equals(transactionType) ? amount : -amount;
                // Frontend potrebuje len vybrané polia, nie celý objekt entity.
                item.put("id", transaction.getId());
                item.put("recipientIban", transaction.getRecipientIban());
                item.put("amount", amount);
                item.put("signedAmount", signedAmount);
                item.put("description", transaction.getDescription());
                item.put("transactionType", transactionType);
                item.put("transactionDate", transaction.getTransactionDate());
                return item;
            })
            .toList();

    return ResponseEntity.ok(response);
}
```

Controller teda nepracuje iba s jednou tabuľkou, ale využíva logické väzby medzi entitami. Zároveň je tu dôležité, že frontend nedostáva celý objekt `Transaction`, ale iba tie údaje, ktoré naozaj potrebuje na vykreslenie tabuľky. Backend si dnes zároveň sám dopočíta `signedAmount`, takže frontend sa nemusí spoliehať len na textový typ transakcie.

> <b>Princíp:</b> backend vracia frontendu iba tie údaje, ktoré sú potrebné na zobrazenie.

Aj história transakcií má svoj vlastný jednoduchý tok:

```text
Dashboard
   |
   v
GET /api/auth/transactions
   |
   v
AuthController
   |
   +-- HttpSession
   +-- UserRepository
   +-- AccountRepository
   +-- TransactionRepository
   |
   v
JSON pole transakcii
   |
   v
tabulka vo fronte
```

Tento princíp je dôležitý aj architektonicky. Backend nemá frontendu vracať viac údajov, než je potrebné. Tým je odpoveď jednoduchšia, prehľadnejšia a ľahšie sa ďalej spracováva v JavaScripte.

### 8.5 Endpoint `/api/auth/verify-payment`

<b>Úloha endpointu:</b> spojiť behaviorálne overenie a vykonanie platby do jedného bezpečnostného toku.

Toto je jeden z najdôležitejších endpointov v aplikácii. Frontend sem pošle IBAN príjemcu, sumu, variabilný symbol, poznámku a biometrické vzorky z potvrdzovacej frázy. Meno používateľa sa už neposiela v requeste, pretože backend si ho načíta zo session. Následne sa zavolá behaviorálna vrstva, ktorá už dnes nepoužíva len jednoduchý priemer evaluatorov, ale váhované skóre, dynamické thresholdy podľa sumy a normalizovaný profil používateľa. Pri každom pokuse sa navyše vytvorí samostatná behaviorálna relácia v `behavior_attempts`.

Najprv sa spustí behaviorálne vyhodnotenie:

```java
// Hlavná behaviorálna služba vyhodnotí aktuálnu vzorku písania.
BehavioralAuthenticationResult authenticationResult =
        behavioralBiometricService.evaluatePaymentBehavior(username, amount, biometrics);

if (!authenticationResult.authenticated()) {
    // Ak používateľ neprejde, platba sa zastaví ešte pred transakčnou logikou.
    return ResponseEntity.status(403).body(Map.of(
            "message", authenticationResult.message(),
            "confidenceScore", authenticationResult.confidenceScore(),
            "requiredThreshold", authenticationResult.requiredThreshold(),
            "details", authenticationResult.evaluatorSummaries()
    ));
}
```

Táto časť je zásadná, pretože rozhoduje, či sa vôbec pristúpi k vykonaniu platby. Pokiaľ behaviorálna vrstva vráti negatívny výsledok, controller platbu okamžite zastaví.

Presne tu sa najlepšie ukazuje, že behaviorálna vrstva nie je len experimentálna nadstavba. Má reálny dopad na to, či sa peniaze pošlú alebo nie.

Najdôležitejší backendový tok pri platbe vyzerá takto:

```text
POST /api/auth/verify-payment
   |
   v
AuthController
   |
   +-- BehavioralBiometricService
   |       |
   |       +-- evaluatory
   |
   +-- ak neuspech -> 403
   |
   +-- ulozenie BehaviorAttempt
   |
   +-- ak uspech -> TransactionService
                   |
                   +-- zmena zostatku
                   +-- ulozenie Transaction
                   +-- adaptivne preucenie behavioralneho profilu
```

Po úspešnom overení a úspešnej transakcii backend vracia frontendovej časti aj behaviorálne údaje:

```java
Map<String, Object> response = new HashMap<>();

// Základný výsledok platby.
response.put("message", result.message());
response.put("transactionId", result.transactionId());
response.put("updatedBalance", result.updatedBalance());

// Doplňujúce behaviorálne informácie pre ďalšie spracovanie alebo diagnostiku.
response.put("behavioralMessage", authenticationResult.message());
response.put("confidenceScore", authenticationResult.confidenceScore());
response.put("requiredThreshold", authenticationResult.requiredThreshold());
response.put("enrollmentMode", authenticationResult.enrollmentMode());
response.put("details", authenticationResult.evaluatorSummaries());

return ResponseEntity.ok(response);
```

To je dôležité z dvoch dôvodov. Po prvé, frontend vie hneď reagovať na výsledok platby. Po druhé, systém už teraz vracia aj údaje, ktoré sa dajú neskôr využiť na zobrazovanie behaviorálneho skóre, diagnostiku alebo výskumné porovnanie rôznych prístupov.

> <b>Kľúčový moment:</b> ak behaviorálna vrstva používateľa neakceptuje, k transakčnej logike sa systém vôbec nedostane.

---

## 9. Implementácia transakcie

<u><b>Obchodná logika platby:</b></u>

Samotná transakčná logika je implementovaná v triede `TransactionService`. Táto trieda obsahuje metódu `executeTransaction`. Do tejto metódy vstupuje používateľské meno, príjemcov IBAN, suma, variabilný symbol a poznámka k platbe.

Táto metóda pracuje v niekoľkých krokoch. Najskôr sa skontroluje, či používateľské meno existuje, či bol zadaný IBAN príjemcu a či je suma väčšia ako nula.

```java
@Transactional
public TransactionResult executeTransaction(
        String username,
        String recipientIban,
        Double amount,
        String variableSymbol,
        String paymentNote
) {
    // Bez používateľa, IBANu a kladnej sumy nemá zmysel pokračovať.
    if (username == null || username.isBlank()) {
        throw new IllegalArgumentException("Chýba používateľ transakcie.");
    }
    if (recipientIban == null || recipientIban.isBlank()) {
        throw new IllegalArgumentException("IBAN príjemcu je povinný.");
    }
    if (amount == null || amount <= 0) {
        throw new IllegalArgumentException("Suma musí byť väčšia ako 0.");
    }
```

Potom systém podľa `username` nájde používateľa a následne jeho účet:

```java
    User user = userRepository.findByUsername(username);
    if (user == null) {
        throw new IllegalArgumentException("Používateľ neexistuje.");
    }

    Account senderAccount = accountRepository.findByUser(user);
    if (senderAccount == null) {
        throw new IllegalArgumentException("Účet odosielateľa neexistuje.");
    }
```

Následne sa spraví kontrola na vlastný účet, kontrola zostatku a odpočítanie sumy:

```java
    String normalizedRecipientIban = normalizeIban(recipientIban);

    // Blokuje sa prevod na vlastný účet.
    if (normalizedRecipientIban.equals(normalizeIban(senderAccount.getIban()))) {
        throw new IllegalArgumentException("Nie je možné odoslať platbu na vlastný účet.");
    }

    // Platba prejde len vtedy, ak má účet dostatok prostriedkov.
    if (senderAccount.getBalance() == null || senderAccount.getBalance() < amount) {
        throw new IllegalArgumentException("Nedostatočný zostatok na účte.");
    }

    // Odpočítanie sumy zo zostatku odosielateľa.
    senderAccount.setBalance(roundToCents(senderAccount.getBalance() - amount));
    accountRepository.save(senderAccount);
```

Ak ide o interný prevod medzi dvoma účtami, backend dnes už nepripíše len zostatok príjemcovi, ale uloží aj samostatný prichádzajúci záznam do jeho histórie:

```java
    Account recipientAccount = accountRepository.findByIban(normalizedRecipientIban);
    if (recipientAccount != null) {
        Double recipientBalance = recipientAccount.getBalance() == null ? 0.0 : recipientAccount.getBalance();
        recipientAccount.setBalance(roundToCents(recipientBalance + amount));
        accountRepository.save(recipientAccount);
    }

    transaction.setTransactionType("OUTGOING");
    Transaction savedTransaction = transactionRepository.save(transaction);

    if (recipientAccount != null) {
        Transaction incomingTransaction = new Transaction();
        incomingTransaction.setAccount(recipientAccount);
        incomingTransaction.setRecipientIban(senderIban);
        incomingTransaction.setAmount(roundToCents(amount));
        incomingTransaction.setDescription(description);
        incomingTransaction.setTransactionType("INCOMING");
        transactionRepository.save(incomingTransaction);
    }
```

Táto zmena rieši dôležitý praktický problém. V staršej verzii sa síce príjemcovi správne zvýšil zostatok, ale v histórii transakcií nevidel žiadny `+` záznam. Teraz už systém eviduje pohyb pre obe strany interného prevodu.

Anotácia `@Transactional` je dôležitá z dôvodu konzistencie databázy. Ak by sa v strede procesu niečo pokazilo, zmeny sa nemajú zapísať len čiastočne.

> <b>Význam tejto vrstvy:</b> `TransactionService` obsahuje pravidlá, ktoré majú zabezpečiť správnosť finančnej operácie.

---

Prakticky to znamená, že transakcia má byť vykonaná buď celá správne, alebo vôbec. Z pohľadu bankovej aplikácie je to veľmi dôležité, pretože nekonzistentný stav účtov by bol vážny problém.

Spracovanie transakcie si viem predstaviť aj cez tento mini diagram:

```text
vstupne udaje platby
   |
   v
validacia
   |
   v
najdenie pouzivatela a uctu
   |
   v
kontrola zostatku a IBANu
   |
   v
odpocitanie sumy
   |
   v
ulozenie Transaction
   |
   v
odpoved pre frontend
```

## 10. Frontendová časť aplikácie

<u><b>Viditeľná časť systému pre používateľa:</b></u>

Frontend je rozdelený na dve obrazovky:

- login stránka,
- dashboard stránka.

### 10.1 Login stránka

<b>Úloha stránky:</b> prihlásiť používateľa a zároveň zachytiť jeho spôsob písania.

Login obrazovka obsahuje pole pre meno, pole pre heslo a formulár na prihlásenie. JavaScript v `login.js` sleduje `keydown` a `keyup`. Pri každom klávese si aplikácia zapamätá čas stlačenia a po uvoľnení vypočíta `dwell time`. Zároveň si pamätá aj čas predchádzajúceho uvoľnenia klávesu, takže vie vypočítať aj `flight time`.

Súčasťou login stránky je dnes už aj registrácia nového účtu. V Spring Boot statickej verzii frontendu je vyriešená cez Bootstrap modal, ktorý sa otvorí po kliknutí na odkaz `Nemáte účet? Zaregistrujte sa`. Používateľ v ňom zadá nové meno, heslo a počiatočný zostatok.

Najprv sa vo fronte pripraví samotný modal a formulár:

```javascript
const registerForm = document.getElementById('registerForm');
const toggleFormLink = document.getElementById('toggleFormLink');
const registerModal = new bootstrap.Modal(document.getElementById('registerModal'));

toggleFormLink.addEventListener('click', (e) => {
    e.preventDefault();
    registerModal.show();
});
```

Po odoslaní formulára sa registrácia pošle na backend:

```javascript
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        username: document.getElementById('regUsername').value,
        password: document.getElementById('regPassword').value,
        balance: document.getElementById('regBalance').value
    };

    const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
```

Ak registrácia prejde úspešne, modal sa zavrie a formulár sa vyčistí. Z pohľadu používateľa je teda login stránka už zároveň aj vstupným bodom na vytvorenie nového účtu.

Treba však dodať, že v priečinku `frontend-js` je registrácia riešená trochu inak. Tam sa nepoužíva Bootstrap modal, ale prepínanie medzi prihlasovacím a registračným formulárom priamo v rámci stránky. Funkčne však ide o tú istú logiku a volá sa ten istý backend endpoint `/api/auth/register`.

Najprv sa pri stlačení klávesu uloží jeho štartovací čas:

```javascript
let keyData = [];
let keyDownTimes = {};
let previousKeyUpTimes = {};

passwordInput.addEventListener('keydown',(e) =>{
    if(!keyDownTimes[e.code]){
        // performance.now() dáva presnejší čas než bežný Date.
        keyDownTimes[e.code] = performance.now();
    }
});
```

Pri uvoľnení klávesu sa už vypočíta dwell time a záznam sa uloží:

```javascript
usernameInput.addEventListener('keyup',(e)=>{
   const keyUpTime = performance.now();
   const keyDownTime = keyDownTimes[e.code];
   if(keyDownTime){
       const dwellTime = keyUpTime - keyDownTime;
       const previousKeyUpTime = previousKeyUpTimes.username;
       const record = {
           // Zisťujeme, či kláves prišiel z poľa pre meno alebo heslo.
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
```

V aktuálnej implementácii sa podobná logika používa aj pre pole hesla. To je dôležité preto, že frontend nesleduje len jeden spoločný tok kláves, ale udržiava si oddelené časovanie pre `username` a `password`.

Samotné odoslanie login payloadu je opäť lepšie chápať po dvoch krokoch. Najprv sa pripraví payload:

```javascript
const API_BASE = "/api/auth";

loginForm.addEventListener('submit',async (e) => {
   e.preventDefault();

   // Frontend balí prihlasovacie a biometrické dáta do jedného objektu.
   const authPayload = {
       username: usernameInput.value,
       password: passwordInput.value,
       biometrics: keyData
   };
```

Potom sa objekt odošle na backend. V aktuálnej verzii je dôležité, že request používa `credentials: 'include'`. Tým sa po úspešnom prihlásení prenesie session cookie a frontend už nemusí ukladať meno používateľa do `sessionStorage`.

```javascript
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
```

### 10.2 Dashboard stránka

<b>Úloha stránky:</b> zobraziť stav účtu, históriu transakcií a spracovať novú platbu.

Dashboard je hlavná obrazovka aplikácie po prihlásení. Obsahuje informácie o účte, históriu transakcií, tréning behaviorálneho profilu, formulár novej platby a debug prehľad behaviorálnych rozhodnutí.

Z pohľadu používateľa je dashboard miesto, kde sa celá aplikácia „deje“. Z pohľadu implementácie je to zas miesto, kde sa spájajú takmer všetky dôležité časti systému:

- session,
- načítanie údajov o účte,
- história transakcií,
- pseudo-autorizácia a budovanie profilu,
- odoslanie platby,
- behaviorálne overenie,
- debug prehľad evaluatorov, profilov a pokusov.

Dashboard sa preto dá zjednodušene chápať takto:

```text
Dashboard
   |
   +-- nacitanie uctu
   |
   +-- nacitanie historie transakcii
   |
   +-- trening behavioralneho profilu
   |
   +-- formular novej platby
           |
           v
      biometricky modal
           |
           v
      overenie + vykonanie platby
   |
   +-- debug prehlad profilov a pokusov
```

Po načítaní stránky sa najprv kontroluje session a potom sa načítajú údaje o účte:

```javascript
document.addEventListener("DOMContentLoaded",async ()=>{
    try{
        // Najprv sa overí, či backend eviduje platnú session.
        currentUsername = await loadSessionUser();

        const response = await fetch(`${API_BASE}/account-info`, {
            credentials: 'include'
        });
        const data = await response.json();
        if(!response.ok){
            throw new Error(data.message || "Session nie je platná.");
        }

        // Po úspešnom načítaní sa aktualizujú údaje v hlavičke dashboardu.
        document.getElementById('welcomeUser').innerText = `Používateľ: ${data.username}`;
        updateBalanceDisplay(data.balance);
        document.getElementById('ibanDisplay').innerText = `IBAN: ${data.iban}`;

        // Hneď po účte sa načíta stav behaviorálneho profilu, debug panel a história.
        await refreshBehavioralStatus();
        await loadBehavioralDebug();
        await loadTransactionHistory();
    }catch (e) {
        alert("Vaša relácia vypršala. Prihláste sa znova.");
        window.location.href = "login.html";
    }
});
```

Načítanie histórie je oddelené do samostatnej funkcie:

```javascript
async function loadTransactionHistory() {
    const response = await fetch(`${API_BASE}/transactions`, {
        credentials: 'include'
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Nepodarilo sa načítať transakcie.');
    }

    // Až po úspešnom načítaní sa dáta pošlú do vykresľovacej funkcie.
    renderTransactionHistory(data);
}
```

Samotné vykreslenie histórie dnes už pracuje s hodnotou `signedAmount`, ktorú pripraví backend:

```javascript
function formatSignedCurrency(value) {
    const number = Number(value);
    const formatted = Math.abs(number).toLocaleString('sk-SK', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    return `${number >= 0 ? '+' : '-'}${formatted} €`;
}

tableBody.innerHTML = transactions.map((transaction) => `
    <tr>
        <td>${formatTransactionDate(transaction.transactionDate)}</td>
        <td class="fw-semibold">${transaction.recipientIban}</td>
        <td>${transaction.description || '-'}</td>
        <td class="text-end fw-bold ${Number(transaction.signedAmount) >= 0 ? 'text-success' : 'text-danger'}">${formatSignedCurrency(transaction.signedAmount)}</td>
    </tr>
`).join('');
```

Vďaka tomu používateľ na dashboarde jasne vidí rozdiel medzi:

- odoslanou platbou, ktorá sa zobrazuje červeno so znakom `-`
- prijatou platbou, ktorá sa zobrazuje zeleno so znakom `+`

Ak profil ešte nie je pripravený, dashboard nezobrazí reálnu platbu, ale tréningovú sekciu. Používateľ tam viackrát napíše frázu `potvrdzujem platbu` a tým buduje behaviorálny profil. Až keď je nazbieraný dostatočný počet vzoriek, tréningová časť sa skryje a zobrazí sa reálna platba.

Pred vykonaním platby sa otvorí modal, v ktorom musí používateľ napísať frázu `potvrdzujem platbu`. Toto miesto je dôležité, pretože sa tu zbierajú behaviorálne údaje pre autorizáciu transakcie.

Najprv sa pripraví modal:

```javascript
function openPaymentModal(){
    if (!behavioralProfileReady) {
        alert("Platby budú dostupné až po dokončení pseudo-autorizácie a nazbieraní behaviorálnych vzoriek.");
        return;
    }
    if(verifyFields()){
        document.getElementById('paymentModal').style.display = 'block';
        // Vyčistenie starých dát z predchádzajúceho pokusu.
        verificationData = [];
        verificationDownTimes = {};
        verificationPreviousKeyUpTime = null;
        document.getElementById('verificationInput').value = '';
        document.getElementById('verificationInput').focus();
    }
}
```

Potom sa počas písania zbierajú nové vzorky:

```javascript
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
           // Ukladá sa konkrétny kláves a čas jeho držania.
           key: e.key,
           dwell: dwellTime.toFixed(2),
           flight: verificationPreviousKeyUpTime ? (keyDownTime - verificationPreviousKeyUpTime).toFixed(2) : null
       });
       verificationPreviousKeyUpTime = keyUpTime;
       delete verificationDownTimes[e.code];
   }
});
```

Ešte pred týmto krokom však v aktuálnom dashboarde prebieha samostatný tréning profilu. Kým používateľ nemá dostatok referenčných vzoriek, odosiela sa pseudo-autorizácia na endpoint `/api/auth/behavioral-training`:

```javascript
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

   const response = await fetch(`${API_BASE}/behavioral-training`,{
       method: 'POST',
       headers: {'Content-Type': 'application/json'},
       body: JSON.stringify({
           phraseBiometrics: trainingData
       }),
       credentials: 'include'
   });
```

Táto ukážka je dôležitá preto, že vysvetľuje prechod medzi tréningovým režimom a reálnou autorizáciou. Najprv sa profil len buduje a až po dosiahnutí požadovaného počtu vzoriek dashboard odomkne skutočné odoslanie platby.

Samotné odoslanie platby na backend je opäť vhodné čítať po častiach. Najprv sa validuje fráza a pripraví sa payload:

```javascript
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

   // V jednom objekte sa spoja finančné aj behaviorálne dáta.
   const paymentPayload = {
       recipientIban,
       amount,
       variableSymbol,
       paymentNote,
       phraseBiometrics: verificationData
   };
```

Potom nasleduje komunikácia s backendom a aktualizácia používateľského rozhrania:

```javascript
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
               // Zostatok sa mení hneď bez reloadu stránky.
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
```

> <b>Zhrnutie frontend vrstvy:</b> frontend nielen zobrazuje dáta, ale zároveň ich aktívne zbiera a pripravuje pre backendové vyhodnotenie.

Okrem toho dnes frontend obsahuje aj samostatný debug panel. V ňom sa zobrazujú:

- aktuálny normalizovaný profil používateľa,
- váhy evaluatorov,
- prahy pre jednotlivé typy platieb,
- posledné behaviorálne pokusy,
- detail evaluatorov pre každý pokus.

---

## 11. Aktuálny stav behaviorálnej autorizácie

<u><b>Behaviorálna vrstva už nie je len experiment, ale samostatný subsystém:</b></u>

Behaviorálna autorizácia je dnes implementovaná ako samostatná rozhodovacia vrstva nad transakčným tokom. Už nejde len o jednoduché porovnanie niekoľkých vzoriek, ale o systém, ktorý pracuje s viacerými typmi metrík, so session-based pokusmi, s normalizovaným profilom používateľa a s debug údajmi pre ďalšiu analýzu.

Jadrom celej vrstvy je stále trieda `BehavioralBiometricService`, ale jej úloha je dnes širšia:

- spracovať vstupné keystroke dáta,
- vyhodnotiť, či je profil používateľa dostatočne pripravený,
- načítať referenčné relácie používateľa,
- spustiť viacero evaluatorov,
- vypočítať váhované skóre,
- aplikovať threshold podľa typu operácie a výšky sumy,
- uložiť výsledok pokusu do databázy,
- priebežne aktualizovať profil používateľa.

Z architektonického pohľadu sa súčasná behavioral vrstva dá zobraziť takto:

```text
BehavioralBiometricService
   |
   +-- referencne behavior_attempts
   +-- behavioral_profile
   +-- evaluatory
   +-- vahovanie score
   +-- threshold podla operacie
   |
   v
BehavioralAuthenticationResult
   |
   v
ulozenie BehaviorAttempt + VerificationSample
```

Už samotný konštruktor hlavnej služby pekne ukazuje, že behaviorálna vrstva je zložená z viacerých menších častí a nie z jedného veľkého algoritmu:

```java
public BehavioralBiometricService(
        UserRepository userRepository,
        BiometricRepository biometricRepository,
        VerificationSampleRepository verificationSampleRepository,
        BehaviorAttemptRepository behaviorAttemptRepository,
        BehavioralProfileRepository behavioralProfileRepository,
        KeySequenceEvaluator keySequenceEvaluator,
        DwellTimeEvaluator dwellTimeEvaluator,
        KeyHoldStabilityEvaluator keyHoldStabilityEvaluator,
        FlightTimeEvaluator flightTimeEvaluator,
        RhythmCadenceEvaluator rhythmCadenceEvaluator,
        PausePatternEvaluator pausePatternEvaluator,
        DigraphConsistencyEvaluator digraphConsistencyEvaluator,
        CorrectionBehaviorEvaluator correctionBehaviorEvaluator,
        BoundaryStabilityEvaluator boundaryStabilityEvaluator,
        WordTransitionEvaluator wordTransitionEvaluator
) {
    // Do jednej služby sa poskladajú repository aj jednotlivé evaluatory.
    this.behaviorAttemptRepository = behaviorAttemptRepository;
    this.behavioralProfileRepository = behavioralProfileRepository;
    this.evaluators = List.of(
            keySequenceEvaluator,
            dwellTimeEvaluator,
            keyHoldStabilityEvaluator,
            flightTimeEvaluator,
            rhythmCadenceEvaluator,
            pausePatternEvaluator,
            digraphConsistencyEvaluator,
            correctionBehaviorEvaluator,
            boundaryStabilityEvaluator,
            wordTransitionEvaluator
    );
}
```

Táto ukážka je dôležitá preto, že ukazuje celú filozofiu návrhu. `BehavioralBiometricService` nie je evaluator sám o sebe. Je to skôr koordinátor, ktorý načíta referenčné dáta, spustí jednotlivé evaluatory, dopočíta profilovú zhodu, aplikuje váhy a na konci rozhodne, či používateľ prešiel alebo nie.

## 12. Relácie a behaviorálne pokusy

<u><b>Najdôležitejší posun bol prechod z dlhého zoznamu na relácie:</b></u>

V staršej verzii sa `verification_samples` čítali len ako jeden dlhý zoznam kláves. To bolo vhodné na prvý prototyp, ale z hľadiska reálneho behaviorálneho vyhodnotenia to nebolo ideálne. Dnes je každý tréningový alebo platobný pokus uložený ako samostatná relácia v tabuľke `behavior_attempts`.

To znamená, že systém už pracuje s dvoma úrovňami behaviorálnych dát:

- `BehaviorAttempt` ako hlavička jedného pokusu,
- `VerificationSample` ako jednotlivé klávesové vzorky v rámci tohto pokusu.

Týmto sa zlepšilo najmä:

- zachovanie poradia vzoriek v konkrétnej fráze,
- porovnávanie celých pokusov medzi sebou,
- možnosť ukladať výsledok autentifikácie ku konkrétnej relácii,
- možnosť robiť históriu pokusov a debug prehľad.

Každý pokus dnes obsahuje:

- používateľa,
- typ pokusu (`TRAINING` alebo `PAYMENT`),
- výsledné skóre,
- požadovaný threshold,
- úspech alebo neúspech,
- počet vzoriek,
- detail evaluatorov,
- čas pokusu.

Každá vzorka v `verification_samples` obsahuje:

- kláves,
- `dwell_time`,
- `flight_time`,
- poradie v pokuse cez `sample_index`,
- väzbu na konkrétny `attempt_id`.

V kóde je to rozdelenie na hlavičku pokusu a jednotlivé vzorky viditeľné priamo v entitách:

```java
@Entity
@Table(name = "behavior_attempts")
public class BehaviorAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String attemptType;
    private Boolean authenticated;
    private Double confidenceScore;
    private Double requiredThreshold;
    private Integer sampleCount;

    @Column(columnDefinition = "TEXT")
    private String evaluatorDetails;

    private LocalDateTime createdAt = LocalDateTime.now();
}
```

Táto trieda predstavuje jednu celú behaviorálnu reláciu. Inak povedané, je to hlavička pokusu. Neobsahuje každé jednotlivé stlačenie klávesu, ale najmä súhrnné informácie o tom, čo sa pri danom pokuse stalo. Práve sem sa ukladá, či používateľ prešiel, aké mal skóre a aký threshold sa od neho vyžadoval.

Na túto hlavičku sa potom viažu konkrétne klávesové vzorky:

```java
@Entity
@Table(name = "verification_samples")
public class VerificationSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String keyPressed;
    private Double dwellTime;
    private Double flightTime;
    private Integer sampleIndex;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private BehaviorAttempt attempt;
}
```

Tu je dôležité hlavne pole `sampleIndex`, pretože zachováva presné poradie klávesov v rámci frázy, a väzba `attempt`, pretože vďaka nej vie backend povedať, do ktorého pokusu daná vzorka patrí. Bez tejto väzby by sa všetky vzorky miešali do jedného dlhého zoznamu a systém by strácal informáciu o tom, čo bolo napísané v rámci jednej konkrétnej autorizácie.

Samotné uloženie pokusu potom prebieha tak, že sa najprv vytvorí hlavička a až následne sa pod ňu uložia jednotlivé vzorky:

```java
private void saveBehaviorAttempt(
        String username,
        String attemptType,
        boolean authenticated,
        double confidenceScore,
        double requiredThreshold,
        List<String> evaluatorDetails,
        List<KeystrokeSample> samples
) {
    BehaviorAttempt attempt = new BehaviorAttempt();
    attempt.setUsername(username);
    attempt.setAttemptType(attemptType);
    attempt.setAuthenticated(authenticated);
    attempt.setConfidenceScore(roundScore(confidenceScore));
    attempt.setRequiredThreshold(roundScore(requiredThreshold));
    attempt.setSampleCount(samples.size());
    attempt.setEvaluatorDetails(String.join("\n", evaluatorDetails));
    BehaviorAttempt savedAttempt = behaviorAttemptRepository.save(attempt);

    for (int index = 0; index < samples.size(); index++) {
        KeystrokeSample sample = samples.get(index);
        VerificationSample verificationSample = new VerificationSample();
        verificationSample.setUsername(username);
        verificationSample.setKeyPressed(sample.key());
        verificationSample.setDwellTime(sample.dwellTime());
        verificationSample.setFlightTime(sample.flightTime());
        verificationSample.setSampleIndex(index);
        verificationSample.setAttempt(savedAttempt);
        verificationSampleRepository.save(verificationSample);
    }
}
```

Tento postup je dôležitý aj z databázového pohľadu. Najprv musí vzniknúť rodičovská entita `BehaviorAttempt`, aby mali jednotlivé `VerificationSample` kam smerovať cez `attempt_id`. Až potom vie systém spoľahlivo evidovať celý priebeh jedného tréningu alebo jednej autorizácie platby.

## 13. Evaluátory a behaviorálne metriky

<u><b>Systém dnes sleduje viac pohľadov na štýl písania:</b></u>

Behaviorálna vrstva už nie je postavená len na `dwell time`. V aktuálnom stave sa používajú tieto evaluátory:

- `KeySequenceEvaluator`
- `DwellTimeEvaluator`
- `KeyHoldStabilityEvaluator`
- `FlightTimeEvaluator`
- `RhythmCadenceEvaluator`
- `PausePatternEvaluator`
- `DigraphConsistencyEvaluator`
- `CorrectionBehaviorEvaluator`
- `BoundaryStabilityEvaluator`
- `WordTransitionEvaluator`

Okrem týchto evaluatorov sa v hlavnej službe ešte dopočítava aj samostatná profilová zhoda voči `BehavioralProfile`. Tá síce nie je implementovaná ako samostatná komponentová trieda, ale vstupuje do finálneho skóre rovnako ako ostatné výsledky.

Každý evaluator sa pozerá na inú vlastnosť správania:

- sekvencia kláves kontroluje, či používateľ píše frázu rovnakým spôsobom,
- `dwell time` sleduje dĺžku držania klávesu,
- `flight time` sleduje prechod medzi klávesmi,
- `cadence` sleduje rytmus písania ako kombináciu `dwell + flight`,
- `pause pattern` sleduje podiel dlhších pauz,
- `digraph` porovnáva dvojice po sebe idúcich kláves,
- `corrections` sleduje správanie pri opravách, napríklad `Backspace`,
- `boundary stability` sleduje stabilitu začiatku a konca frázy,
- `word tempo` sleduje tempo pri prechode medzi slovami,
- profilová zhoda porovnáva aktuálny pokus s normalizovaným profilom používateľa.

Táto kombinácia je dôležitá preto, že behaviorálna identita používateľa nie je daná jednou číselnou hodnotou, ale súborom viacerých návykov pri písaní.

To, že systém naozaj pracuje s viacerými metrikami naraz, je vidieť aj na jednoduchom príklade evaluátora pre `flight time`:

```java
@Override
public EvaluatorResult evaluate(List<KeystrokeSample> referenceSamples, List<KeystrokeSample> candidateSamples) {
    int comparableLength = Math.min(referenceSamples.size(), candidateSamples.size());
    if (comparableLength == 0) {
        return new EvaluatorResult("FlightTime", 0.0, "Nebolo možné porovnať flight time.");
    }

    double totalDeviation = 0.0;
    int measuredSamples = 0;
    for (int index = 0; index < comparableLength; index++) {
        Double expected = referenceSamples.get(index).flightTime();
        Double actual = candidateSamples.get(index).flightTime();
        if (expected == null || actual == null) {
            continue;
        }
        totalDeviation += Math.abs(expected - actual);
        measuredSamples++;
    }
```

Tento evaluator ide po jednotlivých pozíciách v rámci frázy a porovnáva, ako veľmi sa aktuálny `flight time` odlišuje od referenčných dát. Čím je priemerná odchýlka menšia, tým vyššie skóre používateľ získa. V praxi to znamená, že sa nesleduje len to, čo používateľ napísal, ale aj tempo prechodu medzi klávesmi.

Podobne funguje aj `DigraphConsistencyEvaluator`, ktorý namiesto samotných časov sleduje dvojice po sebe idúcich kláves:

```java
private List<String> buildDigraphs(List<KeystrokeSample> samples) {
    List<String> result = new ArrayList<>();
    for (int index = 1; index < samples.size(); index++) {
        String previous = normalize(samples.get(index - 1).key());
        String current = normalize(samples.get(index).key());
        result.add(previous + "->" + current);
    }
    return result;
}
```

Tu je pekne vidieť, že jeden evaluator môže byť založený na časových údajoch a iný zasa na štruktúre písania. Práve kombinácia takýchto rôznych pohľadov robí behaviorálne vyhodnotenie robustnejším.

## 14. Váhovanie evaluatorov a dynamické prahy

<u><b>Finálne skóre sa už nepočíta obyčajným priemerom:</b></u>

V staršej verzii sa z evaluatorov bral jednoduchý priemer. V aktuálnej implementácii sa používa váhované skóre. Nie všetky evaluátory majú rovnaký význam. Napríklad pri fixnej fráze nemá sekvencia kláves až takú silu ako `flight time`, `cadence` alebo profilová zhoda.

Približne sa zvýrazňujú najmä:

- `FlightTime`
- `Cadence`
- `Profile`

a menšiu váhu majú napríklad:

- `KeySequence`
- `Corrections`
- `Boundary`
- `WordTempo`

To znamená, že finálne rozhodnutie viac ovplyvňuje rytmus a časovanie písania než samotný text frázy, ktorý je vopred známy.

Okrem toho systém používa aj dynamické thresholdy podľa operácie:

- tréningový pokus má threshold `0.00`,
- bežná platba má threshold `0.74`,
- vyššia suma má threshold `0.82`,
- veľmi vysoká suma má threshold `0.88`.

Tým sa behaviorálna vrstva správa rozumnejšie. Pri bežnej operácii je tolerantnejšia, pri väčšom riziku je prísnejšia.

V zdrojovom kóde sú váhy zapísané priamo ako mapa:

```java
private static final Map<String, Double> EVALUATOR_WEIGHTS = Map.ofEntries(
        Map.entry("KeySequence", 0.08),
        Map.entry("DwellTime", 0.16),
        Map.entry("Stability", 0.10),
        Map.entry("FlightTime", 0.18),
        Map.entry("Cadence", 0.18),
        Map.entry("PausePattern", 0.10),
        Map.entry("Digraph", 0.10),
        Map.entry("Corrections", 0.04),
        Map.entry("Boundary", 0.04),
        Map.entry("WordTempo", 0.04),
        Map.entry("Profile", 0.16)
);
```

Tento blok je veľmi dôležitý na pochopenie finálneho rozhodovania. Keďže fráza je fixná, samotná zhoda kláves má menšiu výpovednú hodnotu než rytmus a časovanie. Preto majú `FlightTime`, `Cadence` a `Profile` vyššiu váhu ako napríklad `Corrections` alebo `Boundary`.

Následne sa výsledky evaluatorov nespočítajú obyčajným priemerom, ale váhovaným spôsobom:

```java
private double calculateWeightedScore(List<BehaviorEvaluator.EvaluatorResult> results) {
    double weightedScore = 0.0;
    double totalWeight = 0.0;

    for (BehaviorEvaluator.EvaluatorResult result : results) {
        double weight = EVALUATOR_WEIGHTS.getOrDefault(result.name(), 0.1);
        weightedScore += result.score() * weight;
        totalWeight += weight;
    }

    return totalWeight == 0.0 ? 0.0 : weightedScore / totalWeight;
}
```

Z logiky tejto metódy je vidieť, že každý evaluator dodá svoje skóre, ale jeho reálny vplyv na finálny výsledok závisí od priradenej váhy. Tým pádom sa systém dá jemne ladiť bez toho, aby sa musela meniť vnútorná logika každého evaluátora.

Prahy podľa typu operácie sa určujú samostatne:

```java
private double resolveThreshold(String operationType, Double amount) {
    if ("TRAINING".equals(operationType)) {
        return 0.0;
    }
    if (amount != null && amount >= 5000) {
        return VERY_LARGE_PAYMENT_THRESHOLD;
    }
    if (amount != null && amount >= 1000) {
        return LARGE_PAYMENT_THRESHOLD;
    }
    return BASE_PAYMENT_THRESHOLD;
}
```

Toto je prakticky veľmi užitočné. Pri tréningu používateľ nič finančne neriskuje, preto sa threshold nenastavuje prísne. Pri vyšších sumách je však systém opatrnejší a vyžaduje silnejšiu zhodu s referenčným profilom.

## 15. Normalizovaný profil používateľa a adaptívne učenie

<u><b>Dôležitou súčasťou systému je už aj profil používateľa:</b></u>

Okrem raw historických vzoriek sa dnes ukladá aj `BehavioralProfile`. Tento profil neukladá jednotlivé klávesy, ale už agregované vlastnosti správania používateľa. Konkrétne ide o:

- priemerný `dwell time`,
- priemerný `flight time`,
- priemernú odchýlku `dwell time`,
- priemernú odchýlku `flight time`,
- podiel dlhších pauz,
- počet referenčných pokusov,
- počet referenčných vzoriek.

Výhoda tohto prístupu je v tom, že systém už nemusí vždy porovnávať len surový zoznam kláves. Vie sa pozrieť aj na to, či aktuálny pokus sedí na dlhodobejší profil používateľa.

Zároveň je v systéme prítomné aj adaptívne učenie. To znamená, že:

- po úspešnom tréningovom pokuse sa profil prepočíta,
- po úspešnej platbe sa profil znovu prepočíta,
- systém sa tak vie postupne prispôsobovať jemným zmenám v písaní používateľa.

Tento prístup je podľa mňa dôležitý aj prakticky. Používateľ nepíše úplne rovnako každý deň. Adaptívne učenie preto pomáha tomu, aby sa systém časom neodtrhol od reálneho správania používateľa.

Štruktúra samotného profilu je uložená v entite `BehavioralProfile`:

```java
@Entity
@Table(name = "behavioral_profiles")
public class BehavioralProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private Double averageDwellTime;
    private Double averageFlightTime;
    private Double dwellDeviation;
    private Double flightDeviation;
    private Double longPauseRatio;
    private Integer referenceAttempts;
    private Integer referenceSamples;
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

Z tejto triedy je vidieť, že profil už nie je súbor surových klávesových dát, ale skôr sumarizácia dlhodobejšieho správania používateľa. To je dôležité, lebo systém sa pri každom pokuse nemusí spoliehať len na priamu zhodu s historickými vzorkami, ale vie sa pozrieť aj na to, či používateľ stále zapadá do svojho dlhodobého profilu.

Profil sa po úspešných pokusoch prepočítava takto:

```java
private BehavioralProfile rebuildProfile(String username) {
    List<BehaviorAttempt> successfulAttempts =
            behaviorAttemptRepository.findByUsernameAndAuthenticatedTrueOrderByCreatedAtDesc(username);
    List<KeystrokeSample> referenceSamples =
            normalizeAttempts(successfulAttempts.stream().limit(MAX_REFERENCE_ATTEMPTS).toList());

    BehavioralProfile profile = behavioralProfileRepository.findByUsername(username);
    if (profile == null) {
        profile = new BehavioralProfile();
        profile.setUsername(username);
    }

    profile.setAverageDwellTime(defaultZero(average(referenceSamples.stream().map(KeystrokeSample::dwellTime).toList())));
    profile.setAverageFlightTime(defaultZero(average(referenceSamples.stream().map(KeystrokeSample::flightTime).toList())));
    profile.setLongPauseRatio(calculateLongPauseRatio(referenceSamples));
    profile.setReferenceAttempts(successfulAttempts.size());
    profile.setReferenceSamples(referenceSamples.size());
    return behavioralProfileRepository.save(profile);
}
```

V tejto metóde sa pekne ukazuje adaptívne učenie v praxi. Backend si vezme úspešné historické pokusy, znormalizuje ich a z nich vypočíta nový profil. Tým pádom sa profil neaktualizuje pri každom neúspechu, ale len vtedy, keď systém dostane dôvod veriť, že nové dáta naozaj patria legitímnemu používateľovi.

Keď príde nový pokus, služba ho porovná s profilom takto:

```java
private BehaviorEvaluator.EvaluatorResult evaluateAgainstProfile(
        BehavioralProfile profile,
        List<KeystrokeSample> candidateSamples
) {
    double avgDwell = defaultZero(average(candidateSamples.stream().map(KeystrokeSample::dwellTime).toList()));
    double avgFlight = defaultZero(average(candidateSamples.stream().map(KeystrokeSample::flightTime).toList()));
    double pauseRatio = calculateLongPauseRatio(candidateSamples);

    double dwellScore = scoreByDeviation(avgDwell, profile.getAverageDwellTime(), Math.max(30.0, profile.getDwellDeviation() + 20.0));
    double flightScore = scoreByDeviation(avgFlight, profile.getAverageFlightTime(), Math.max(40.0, profile.getFlightDeviation() + 30.0));
    double pauseScore = Math.max(0.0, 1.0 - Math.abs(pauseRatio - profile.getLongPauseRatio()));
```

Táto časť je zaujímavá tým, že neporovnáva jednotlivé klávesy jeden po druhom, ale porovnáva agregované charakteristiky kandidátskeho pokusu s agregovanými charakteristikami profilu. Práve preto sa v dokumentácii hovorí o normalizovanom profile a nie len o ďalšej sade raw vzoriek.

## 16. Debug a diagnostický prehľad

<u><b>Behaviorálna vrstva už má aj vlastný diagnostický pohľad:</b></u>

Do backendu bol doplnený endpoint `/api/auth/behavioral-debug`, ktorý vracia:

- stav profilu používateľa,
- aktuálne váhy evaluatorov,
- nastavené thresholdy,
- normalizovaný profil používateľa,
- zoznam posledných behaviorálnych pokusov.

V dashboarde sa tieto údaje zobrazujú v samostatnej debug sekcii. Používateľ alebo vývojár tak vie vidieť:

- či je profil pripravený na autorizáciu,
- koľko vzoriek už bolo nazbieraných,
- aký je priemerný `dwell` a `flight`,
- aké váhy používajú evaluátory,
- aké thresholdy systém používa,
- ktoré pokusy boli úspešné a ktoré zlyhali,
- aký detail vrátili jednotlivé evaluátory.

Tento debug pohľad je veľmi užitočný aj z pohľadu bakalárskej práce, pretože ukazuje, že behaviorálne rozhodovanie nie je „čierna skrinka“, ale dá sa rozobrať a vysvetliť.

Backend tieto údaje skladá v jednej samostatnej metóde:

```java
public Map<String, Object> getDebugOverview(String username) {
    ProfileStatus status = getProfileStatus(username);
    BehavioralProfile profile = ensureBehavioralProfile(username);
    List<Map<String, Object>> attempts = behaviorAttemptRepository
            .findTop20ByUsernameOrderByCreatedAtDesc(username)
            .stream()
            .map(attempt -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("attemptType", attempt.getAttemptType());
                item.put("authenticated", attempt.getAuthenticated());
                item.put("confidenceScore", attempt.getConfidenceScore());
                item.put("requiredThreshold", attempt.getRequiredThreshold());
                item.put("sampleCount", attempt.getSampleCount());
                item.put("evaluatorDetails", attempt.getEvaluatorDetails());
                return item;
            })
            .toList();
```

Táto ukážka je užitočná hlavne preto, že ukazuje, aké údaje si systém necháva pre diagnostiku. Nie je to len jedno finálne `true` alebo `false`, ale celý balík informácií, vďaka ktorému sa dá analyzovať, prečo autorizácia prešla alebo zlyhala.

Controller potom tento debug pohľad iba sprístupní cez endpoint:

```java
@GetMapping("/behavioral-debug")
public ResponseEntity<?> behavioralDebug(HttpSession session) {
    String username = getSessionUsername(session);
    if (username == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }
    return ResponseEntity.ok(behavioralBiometricService.getDebugOverview(username));
}
```

Aj z tejto malej ukážky je vidieť dobré rozdelenie zodpovednosti. `AuthController` tu nič nepočíta. Len overí session a zavolá service vrstvu. Samotné zostavenie diagnostických dát zostáva v `BehavioralBiometricService`, kde k nim existuje aj kompletný kontext.

## 17. Aktuálne silné stránky a reálne limity riešenia

<u><b>Čo je už v projekte silné a čo ešte zostáva otvorené:</b></u>

Za silné stránky aktuálneho riešenia považujem:

- aplikácia má kompletný tok od prihlásenia po vykonanie platby,
- behaviorálne dáta sa zbierajú pri logine, tréningu aj platbe,
- systém používa relácie pokusov, nie len dlhý zoznam vzoriek,
- evaluatorov je viac a pokrývajú rôzne typy metrík,
- výsledok sa počíta váhovane,
- prahy sa menia podľa rizikovosti operácie,
- profil používateľa sa priebežne aktualizuje,
- existuje debug prehľad pre analýzu a prezentáciu výsledkov.

Na druhej strane stále platí, že ide o pravidlový a heuristický systém, nie o plnohodnotný ML model. Projekt je vhodný ako akademický prototyp, ale ešte stále má svoje limity:

- heslá sa už ukladajú ako hash, ale stále tam nie je plnohodnotná bezpečnostná vrstva typu Spring Security,
- nie je použitý Spring Security,
- thresholdy aj váhy sú nastavované ručne,
- normalizovaný profil je stále pomerne jednoduchý,
- zatiaľ sa nepracuje s plnohodnotnými session clustering alebo pokročilým ML učením,
- automatické testovanie je obmedzené závislosťou na lokálnej MySQL.

---

## 18. Session vrstva

<u><b>Prechod zo sessionStorage na server-side session:</b></u>

V ďalšej fáze vývoja bola do projektu doplnená aj **server-side session logika**. Pôvodne frontend pracoval hlavne s `sessionStorage`, kde si ukladal meno používateľa a čas prihlásenia. Toto riešenie bolo použiteľné na jednoduchý prototyp, ale nebolo ideálne z pohľadu architektúry ani bezpečnosti.

Po novej úprave je hlavný zdroj pravdy o prihlásenom používateľovi uložený na backende v `HttpSession`. To znamená, že po úspešnom prihlásení si backend zapamätá používateľa cez session atribút `loggedUser` a ďalšie requesty už nemusia posielať používateľské meno ako hlavný identifikátor.

Zjednodušený princíp fungovania vyzerá takto:

```text
Login formular
   |
   v
POST /api/auth/login
   |
   v
Backend overi meno a heslo
   |
   v
Backend vytvori HttpSession
   |
   v
Do session ulozi "loggedUser"
   |
   v
Frontend pri dalsich requestoch posiela session cookie
```

Táto zmena je dôležitá preto, že backend už nespolieha len na to, čo mu frontend pošle v requeste, ale pracuje s vlastnou session informáciou.

### 18.1 Session pri prihlásení

Po úspešnom prihlásení backend vytvorí novú session a uloží do nej identitu používateľa.

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, Object> data,
                               HttpSession session,
                               HttpServletRequest request) {
    String username = (String) data.get("username");
    String password = (String) data.get("password");
    User existingUser = userRepository.findByUsername(username);

    boolean authenticated = false;
    if (existingUser != null) {
        if (isHashedPassword(existingUser.getPassword())) {
            authenticated = matchesPassword(password, existingUser.getPassword());
        } else if (existingUser.getPassword().equals(password)) {
            authenticated = true;
            existingUser.setPassword(hashPassword(password));
            userRepository.save(existingUser);
        }
    }

    if (authenticated) {
        session.invalidate();
        HttpSession authenticatedSession = request.getSession(true);
        authenticatedSession.setAttribute("loggedUser", existingUser.getUsername());
        ...
    }
}
```

Táto ukážka ukazuje, že po úspešnom overení hesla voči hash hodnote sa stará session zruší a vytvorí sa nová čistá session. Do nej sa uloží používateľské meno. To znamená, že backend si od tohto momentu pamätá, kto je prihlásený.

### 18.2 Session endpointy

Kvôli práci s reláciou pribudli aj dva nové endpointy:

- `GET /api/auth/session`
- `POST /api/auth/logout`

Ich úloha je jednoduchá:

- `GET /session` slúži na overenie, či session ešte existuje,
- `POST /logout` session ukončí.

Implementácia vyzerá takto:

```java
@GetMapping("/session")
public ResponseEntity<?> sessionInfo(HttpSession session) {
    String username = getSessionUsername(session);
    if (username == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }
    return ResponseEntity.ok(Map.of("username", username));
}

@PostMapping("/logout")
public ResponseEntity<?> logout(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok(Map.of("message", "Odhlásenie úspešné."));
}
```

Tieto endpointy pomáhajú frontendu pracovať s prihlásením bez toho, aby si musel stav používateľa udržiavať len lokálne v JavaScripte.

### 18.3 Session pri načítaní účtu a transakcií

Po doplnení session sa zmenili aj endpointy pre účet a históriu transakcií. Pôvodne sa v URL posielalo používateľské meno. Teraz backend zisťuje používateľa priamo zo session.

Zjednodušený príklad:

```java
@GetMapping("/account-info")
public ResponseEntity<?> accountInfo(HttpSession session) {
    User user = getAuthenticatedUser(session);
    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }
    ...
}

@GetMapping("/transactions")
public ResponseEntity<?> transactionHistory(HttpSession session) {
    User user = getAuthenticatedUser(session);
    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }
    ...
}
```

Táto zmena je dôležitá preto, že používateľ už nemusí byť identifikovaný cez URL. Backend si používateľa nájde sám zo session.

### 18.4 Session pri vykonaní platby

Rovnaký princíp sa použil aj pri samotnej platbe. Frontend už neposiela `username` v `paymentPayload`. Backend si ho zistí zo session.

Začiatok endpointu vyzerá takto:

```java
@PostMapping("/verify-payment")
public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> payload, HttpSession session) {
    String username = getSessionUsername(session);
    if (username == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Neplatná alebo expirovaná session."));
    }
    ...
}
```

Z pohľadu návrhu je to dôležité, pretože klient už backendu nemusí pri každej citlivej operácii znovu hovoriť, kto je. O identite rozhoduje session vrstva na serveri.

### 18.5 Zmeny vo fronte

Aj frontend bol upravený tak, aby sa opieral o backend session a nie o `sessionStorage`.

Pri logine sa teraz používa:

```javascript
const response = await fetch(`${API_BASE}/login`,{
    method: 'POST',
    headers: { 'Content-Type': 'application/json'},
    body: JSON.stringify(authPayload),
    credentials: 'include'
});
```

Parameter `credentials: 'include'` zabezpečí, že prehliadač bude pracovať so session cookie.

Na dashboarde sa session najprv overuje:

```javascript
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
```

A pri odhlásení sa session ukončí cez backend:

```javascript
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
```

To znamená, že frontend už nie je hlavný správca prihlásenia. Je len klient, ktorý si od backendu pýta informáciu, či je session platná.

### 18.6 Význam session vrstvy

Z pohľadu celej aplikácie je táto zmena dôležitá z viacerých dôvodov:

- backend si sám drží informáciu o identite používateľa,
- frontend nemusí posielať `username` v každom requeste,
- účet, transakcie a platba sú pevnejšie naviazané na prihlásenú reláciu,
- systém sa tým posúva bližšie k reálnej webovej aplikácii.

Je pravda, že stále nejde o plnohodnotnú bezpečnostnú vrstvu typu Spring Security, ale ide o výrazne lepší stav než pôvodné riešenie založené hlavne na `sessionStorage`.

---

## 19. Zhrnutie

<u><b>Konečný pohľad na aktuálny stav:</b></u>

Táto aplikácia v aktuálnom stave predstavuje funkčný základ internet bankingu s behaviorálnou biometriou. Používateľ sa vie prihlásiť, vie si pozrieť zostatok a IBAN, vie vytvoriť platbu, vie si pozrieť históriu transakcií a pri autorizácii platby je vyhodnocovaný aj podľa štýlu písania.

Najdôležitejšie je podľa mňa to, že behaviorálna časť už nie je iba nápad alebo samostatná ukážka. Je reálne napojená na proces autorizácie platby a zasahuje do rozhodovania systému.

Za najsilnejší prvok súčasného návrhu považujem architektúru behaviorálnej vrstvy:

- jedna hlavná služba,
- viac menších evaluatorov,
- jednotný výsledok,
- možnosť ďalšieho rozšírenia.

Takýto návrh je pre ďalšiu bakalársku prácu veľmi vhodný, pretože sa na ňom dá ďalej budovať. Je dostatočne jednoduchý na pochopenie, ale zároveň už obsahuje reálnu logiku, ktorú je možné ďalej analyzovať, zlepšovať a experimentálne porovnávať.

> <b>Finálne zhrnutie:</b> aplikácia už dnes funguje ako prepojenie internet bankingu a behaviorálnej biometrie. Zároveň je navrhnutá tak, aby sa z nej dal ďalej vybudovať kvalitnejší a presnejší autentifikačný systém.
