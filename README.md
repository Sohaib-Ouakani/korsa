# Setup

Per fare andare sha:
cp ./keystore/debug.keystore ~/.android/debug.keystore

# Features

**Non** Fatte:
- Widget per interagire rapidamente con parti dell'app
- Eventualmente integrazione con dispositivi wearable, che è particolarmente adatta al vostro progetto

Fatte:
- Registrazione e Login tramite l'utilizzo di Supabase, login classico e tramite provider come Google e GitHub.
- Si può iniziare una corsa. Durante la corsa verrà registrato il percorso. A fine corsa verrà aggiunta la corsa nello storico, dove si potranno visualizzare le statistiche e il percorso fatto (overlay sulla mappa).
- Statistiche come il dislivello o il meteo durante una corsa verranno calcolate/ottenute attraverso api esterne.
- Si possono cercare altri utenti e iniziare a seguirli. In base alle persone che si seguono si possono visualizzare diverse classifiche in base alle statistiche.
- All'utente è assegnato un livello determinato delle sue statistiche. Questo livello è visibile insieme al nome dell'utente quando visualizzato.
- Giornalmente vengono proposte, ad ogni utente, delle sfide, che variano in base al livello. Nella pagina delle statistiche è visualizzabile il numero delle sfide completate.
- Quando è disponibile una nuova sfida viene notificato l'utente.
- Cronometraggio dei percorsi, se non l'avevate già pianificato
- Possibilità di pubblicare e condividere i percorsi con supporto ai deep link per aprire il percorso in-app
- Possibilità di mettere like o reaction e commentare un percorso
- Feed con le attività degli amici