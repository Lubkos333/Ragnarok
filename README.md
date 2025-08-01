# Ragnarok

**Ragnarok** je systém využívající metody Retrieval-Augmented Generation (RAG) pro právní poradenství.

Testovací verze systému je deploynuta v rámci infrastruktury Metacentra a je dostupná skrze UI na následující adrese: [http://ragnarok-ui.dyn.cloud.e-infra.cz/](http://ragnarok-ui.dyn.cloud.e-infra.cz/)



## Složky projektu
- **Ragnarok-UI**: Testovací frontendová aplikace.
- **Ragnarok-RAG-App**: Spring boot backendová aplikace zajišťující AI dotazy, práci s vektorovou databází a vystavuje API systému.
- **Ragnarok-Data-App**: Spring Boot backendové řešení pro zpracování dat a jejich aktualizaci.

##
Doporučené pořadí akcí pro deployment

- Deploynout **MongoDB**
- Spustit **OpenDataPasrer** ze složky **Ragnarok-Data-App**
- Deploynout **DocumentApi** ze složky **Ragnarok-Data-App**
- Deploynout **ChromaDB**
- Deploynout **Ragnarok-RAG-App**
- Provolat **Ragnarok-RAG-App** endpoint pro naplnění vektorové databáze
- Deploynout **Ragnarok-UI**

Pozn: README soubory v jednotlivých složkách ukazují způsob deploymentu jednotlivých komponent.





