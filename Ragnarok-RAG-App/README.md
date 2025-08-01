# Ragnarok-RAG-App

**Ragnarok-RAG-App** je backendová aplikace postavená na frameworku **Spring Boot**. Implementuje přístup **Retrieval-Augmented Generation (RAG)**, který kombinuje výhody jazykových modelů s externím vyhledáváním ve vektorové databázi. Aplikace umožňuje efektivní práci s dotazy nad strukturovanými dokumenty, především v právním kontextu.

## Co aplikace dělá?

Aplikace zajišťuje:

- **Příjem a zpracování dotazů** od uživatelské aplikace
- **Vyhledávání relevantních informací** ve vektorové databázi na základě embedovaných dokumentů
- **Generování odpovědí** pomocí jazykového modelu s využitím kontextu vyhledaných dokumentů
- **Nahrávání a aktualizaci dokumentů**, které jsou získávány z datové aplikace a ukládány do vektorové databáze
- **Integraci s otevřenými modely**

## Architektura a provozní prostředí

Aplikace je provozována v prostředí **Kubernetes clusteru**, který je hostován v rámci infrastruktury **MetaCentrum**. Backendová komponenta systému RAG je nasazena ve formě Kubernetes Deploymentu a je dostupná prostřednictvím služby typu **Ingress**. Tímto způsobem jsou jednotlivé HTTP a websocket endpointy zpřístupněny mimo prostředí clusteru a umožňují integraci s dalšími systémy nebo frontendovou částí aplikace.

## Konfigurace

Konfigurace služby je řízena prostřednictvím souboru `application.yml`, kde se nastavují klíčové komponenty systému, především:

- Připojení k vektorové databázi **ChromaDB**
- Specifikace **embedovacího modelu**
- Konfigurace **jazykového modelu**
- Nastavení **autentizace** a komunikačních **endpointů**

### Klíčové části konfigurace:

| Klíč                          | Popis |
|------------------------------|-------|
| `api-key`                    | Autentizační klíč pro přístup k OpenAI-kompatibilní API |
| `base-url`                   | Adresa serveru poskytujícího rozhraní pro jazykový model |
| `embedding.base-url`         | URL adresa služby pro embedding textů pomocí modelu `multilingual-e5-large-instruct` |
| `embedding.options.model`    | Název použitého embedovacího modelu |
| `chat.completions-path`     | Cesta k endpointu pro generování odpovědí jazykovým modelem |
| `chat.options.model`         | Specifikace použitého jazykového modelu (např. `llama3.3`) |
| `httpHeaders.Authorization`  | Autorizační hlavička pro zabezpečený přístup |
| `address`                    | URL adresa instance vektorové databáze (např. ChromaDB) |
| `collection-id`              | Jedinečný identifikátor kolekce (namespace) |
| `collection-name`            | Čitelné pojmenování kolekce pro správu a orientaci |
| `data-app.api-key`          | Autentizační klíč pro přístup k aplikaci zpracovávající data                         |
| `vector-db.address:`        | Adresa vektorové databáze, kde jsou zpracovaná data ve vektorové podobě              |
| `vector-db.collection-id`   | ID konkrétní kolekce                                                                 |
| `vector-db.collection-name` | Název konkrétní kolekce                                                              |

## Použité technologie

- **Spring Boot** – backendový framework
- **Maven** – nástroj pro build a správu závislostí
- **Java 21** – jazyková verze s podporou moderních funkcí

## Jak projekt spustit?

### 1. Build aplikace
Nejprve se ujistěte, že máte nainstalovaný **JDK 17+** a **Maven**. Poté spusťte následující příkaz v kořenovém adresáři projektu:

```bash
mvn clean install
```

### 2. Spuštění aplikace
Po úspěšném sestavení se vytvoří spustitelný JAR soubor ve složce target. Spuštění:
```bash
java -jar target/Ragnarok-RAG-App-0.0.1-SNAPSHOT.jar
```
Aplikace se spustí s výchozí konfigurací ze souboru application.yml a poběží na adrese: http://localhost:7777

### 3. Dokumentace API
Po spuštění backendu je REST API dokumentace dostupná na: http://localhost:7777/swagger-ui/index.html

## Docker podpora
### Sestavení Docker image
Ve složce s Dockerfile spusťte:

```bash
docker build -t ragnarok-be .
```
Spuštění kontejneru

```bash
docker run -d -p 7777:7777 --name ragnarok-be-container ragnarok-be
```
