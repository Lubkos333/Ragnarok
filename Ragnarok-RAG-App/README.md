# Ragnarok-RAG-App

**Ragnarok-RAG-App** je backendová aplikace postavená na frameworku Spring Boot. Implementuje přístup **Retrieval-Augmented Generation (RAG)**, který kombinuje výhody jazykových modelů s externím vyhledáváním ve vektorové databázi. Aplikace umožňuje efektivní práci s dotazy nad strukturovanými dokumenty, především v právním kontextu.

## Co aplikace dělá?

Aplikace zajišťuje:

- **Příjem a zpracování dotazů** od uživatelské aplikace
- **Vyhledávání relevantních informací** ve vektorové databázi na základě embedovaných dokumentů
- **Generování odpovědí** pomocí jazykového modelu s využitím kontextu vyhledaných dokumentů
- **Nahrávání a aktualizaci dokumentů**, které jsou získávány z datové aplikace a ukládány do vektorové databáze
- **Integraci s otevřenými modely**

## Použité technologie

- **Spring Boot** – framework pro vývoj backendové aplikace
- **Maven** – buildovací nástroj pro správu závislostí a sestavení projektu
- **Java 21** – moderní verze jazyka Java s podporou nových jazykových funkcí

## Jak projekt spustit?

### Build aplikace
```bash
mvn clean install
```
### Spuštění aplikace
```bash
java -jar target/Ragnarok-RAG-App-0.0.1-SNAPSHOT.jar
```