# CinemaApp — Οδηγίες Εκτέλεσης 

## Α. Απαιτήσεις 

- Java 17+
- Maven 3.8+
- Node.js 18+ (με npm)



## Β. Τρέξιμο Backend (Spring Boot)

Από το root του repository:

1) Άνοιξε terminal στο root folder
2) Τρέξε:

mvn spring-boot:run

Αν όλα πάνε καλά, το backend σηκώνεται στο:
http://localhost:8080



## Γ. Seed / Αρχικοποίηση δεδομένων στη H2 (manual paste)

Για να δουλέψει το demo σενάριο (users/roles/programs/screenings), χρειάζεται seed.

1) Άνοιξε:
http://localhost:8080/h2-console

2) JDBC URL:
Βάλε ΤΟ ΙΔΙΟ που έχει το backend στο `application.properties` (spring.datasource.url).
(Συνήθως είναι κάτι σαν `jdbc:h2:mem:testdb` ή `jdbc:h2:file:...`)

3) Πάτα Connect

4) Άνοιξε το αρχείο:
scripts/script.sql

5) Κάνε copy–paste ΟΛΟ το script στο H2 Console και πάτα Run.

Με αυτό:
- γίνεται reset (DROP + CREATE)
- μπαίνουν demo users
- μπαίνουν demo programs
- μπαίνουν demo screenings
- γίνεται restart των identity IDs για να μη “χτυπάνε” νέα inserts



## Δ. Τρέξιμο Frontend (React)

Άνοιξε 2ο terminal στο root και τρέξε:

cd cinema-frontend
npm install
npm run dev

Το frontend ανοίγει στο:
http://localhost:5173



## Ε. Στοιχεία Login (password = username)

Οι κωδικοί στη βάση είναι αποθηκευμένοι κρυπτογραφημένοι (BCrypt),
αλλά για την αξιολόγηση/δοκιμή το demo είναι στημένο ώστε ο plain κωδικός να είναι ίδιος με το username.

USER:
- username: user1
- password: user1

SUBMITTER:
- username: submitter
- password: submitter

PROGRAMMER:
- username: prog1
- password: prog1

STAFF:
- username: staff1
- password: staff1

VISITOR:
- username: visitor
- password: visitor



## ΣΤ. Γρήγορο Demo Σενάριο (τι να κάνω για να “δείξει” λειτουργία)

### 1) Submitter δημιουργεί screening (μόνο όταν το Program είναι SUBMISSION)
- login ως submitter
- άνοιγμα ενός Program σε state SUBMISSION (π.χ. “Submission Program”)
- Create Screening (τίτλος, αίθουσα, start/end, duration)
- Submit Screening

Αναμενόμενο: το screening αποθηκεύεται και φαίνεται στον submitter.

### 2) Programmer διαχειρίζεται state Program
- login ως prog1
- άνοιγμα Program
- Advance / Change State σε επόμενα στάδια (SUBMISSION → ASSIGNMENT → REVIEW → ...)

Αναμενόμενο: ο prog1 μπορεί να αλλάξει το state.

### 3) Programmer αναθέτει handler (ASSIGNMENT)
- login ως prog1
- program state = ASSIGNMENT
- σε SUBMITTED screening: assign handler username = staff1

Αναμενόμενο: το screening “δένει” με staff1 σαν handler.

### 4) Staff κάνει review (REVIEW)
- login ως staff1
- program state = REVIEW
- open screening που είναι assigned στον staff1
- γράφει score/comments → Review

Αναμενόμενο: σώζονται review_score/review_comments και state → REVIEWED.



## Ζ. Γνωστός περιορισμός 

Η εφαρμογή δεν είναι 100% σωστή στο κομμάτι visibility:
- Ο submitter μπορεί να δημιουργεί screenings.
- Ο programmer (prog1) μπορεί να τα βλέπει/διαχειρίζεται και να αλλάζει states.
- Ο staff μπορεί να κάνει review σε screenings που είναι διαθέσιμα/seeded και (όταν φαίνονται/είναι assigned).

Όμως, σε συγκεκριμένο σενάριο, screenings που δημιουργεί ο submitter δεν εμφανίζονται στον staff,
οπότε ο staff δεν μπορεί να τα ανοίξει/αξιολογήσει από το UI (θέμα visibility/λίστας screenings).

Για αυτό το demo δουλεύει πλήρως με τα seeded screenings μέσω `scripts/script.sql`.



Τέλος.
Αν ακολουθήσεις τα βήματα με αυτή τη σειρά (Backend → H2 seed → Frontend),
το project ανοίγει σε “γνωστή” κατάσταση και μπορείς να δείξεις τη ροή του συστήματος.
