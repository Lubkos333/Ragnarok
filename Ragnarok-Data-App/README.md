# Ragnarok-Data-App

Backendová část aplikace Ragnarok, postavená na Spring Boot. 

**Data writer** komponenta je určena pro natažení metadat právních aktů z portálu e-Sbirka, jejich převedení do jednotné podoby a uložení do dokumentové databáze. Je doporučené spouštět tuto komponentu periodicky.

**Data reader** komponenta vystavuje své API, skrze které na základě metadat uložených v dokumentové databázi poskytuje strojově zpracovatelný obsah jednotlivých právních aktů ve formátu JSON.

## Data writer
Pro spuštění je nutné vybuildit přiložený dockerfile. Kromě běhu v OCI image lze aplikaci zkompilovat do spustitelného .jar souboru. Minimální požadovaná verze JDK je 17.
Spustit:

```
mvn clean install
```

v adresáří obsahujícím soubor src

Ve stejném adresáři spustit 
```
java -jar target/OpenDataParser-1.0.jar
```
Docker image služby je možné spustit s následující sadou proměnných 

```
   MONGO_ADDRESS - Adresa pro dokumentovou databázi. Defaultně mongodb://localhost:27017
   MOGNO_DATABASE_NAME - Jméno databáze. Defaultně DataDB
   MOGNO_COLLECTION_AKTY_ZNENI - Cílová kolekce, do které se nahraje datová sada. Využívá se pro následnou práci nad staženou datovou sadou. Defaultně PravniAktZneni  
   MOGNO_COLLECTION_AKTY_FINAL - Cílová kolekce, do které se zanesou pouze aktuální konsolidované verze metadat jednotlivých právních aktů. Defaultně PravniAktZneniOdkazyQuick
   MONGO_USER - Jméno k přístupu do dokumentové databáze. Defaultně root
   MONGO_PASSWORD - Heslo k přístupu do dokumentové databáze. Defaultně root
   SBIRKA_URL_AKTY_ZNENI - Zdroj pro stažení metadat právních aktů. Defaultně https://opendata.eselpoint.cz/datove-sady-esbirka/001PravniAktZneni.json.gz
   THREAD_NUMBER - Maximální počet vláken, jež se využije při stažení a filtraci jednotlivých aktů. Defaultně 50
```

Aplikaci lze poté kromě přímého spuštění skrze .jar spustit následovně skrze přiložený Dockerfile: 

```
docker build -t data-writer:latest .
docker run -d -e MONGO_ADDRESS="…" -e MONGO_DATABASE_NAME="…" -e MONGO_COLLECTION_AKTY_ZNENI="…" -e MONGO_COLLECTION_AKTY_FINAL="…" -e MONGO_USER="…" -e MONGO_PASSWORD="…" -e SBIRKA_URL_AKTY_ZNENI="…" -e THREAD_NUMBER="…" --name data-writer data-writer:latest
```

Jednotlivé env. proměnné nejsou povinné, v případě jejích neuvedení se použijí defaultní hodnoty.
Služba poté běží na pozadí dokud nedokončí zpracování právních aktů.

## Data reader

Pro spuštění je nutné vybuildit přiložený dockerfile.
Kromě běhu v OCI image lze aplikaci zkompilovat do spustitelného .jar souboru. Minimální požadovaná verze JDK je 18.
Spustit:

```mvn clean install```

ve adresáři obsahujícím soubor src

Ve stejném adresáři spustit:

```
java -jar target/DocumentApi-1.0.jar
```

Aplikaci lze poté kromě přímého spuštění skrze .jar deploynout následovně pomocí přiloženého Dockerfile:
```
docker build -t data-reader:latest .
docker run -d -p 9090:9090 -e MONGO_ADDRESS="…" -e MONGO_DATABASE_NAME="…" -e MONGO_COLLECTION_AKTY_FINAL="…" -e MONGO_COLLECTION_AKTY_ZNENI="…" -e MONGO_USER="…" -e MONGO_PASSWORD="…" --name data-reader data-reader:latest
```
Jednotlivé env. proměnné nejsou povinné, v případě jejích neuvedení se použijí defaultní hodnoty.
Služba pak běží na [http://localhost:9090](http://localhost:9090) a její dokumentace k REST API je dostupná na [http://localhost:9090/swagger-ui/index.html](http://localhost:9090/swagger-ui/index.html).


Docker image služby lze spustit s následující sadou proměnných

```
   MONGO_ADDRESS - adresa pro dokumentovou databázi. Defaultně mongodb://localhost:27017
   MOGNO_DATABASE_NAME jméno databáze. Defaultně DataDB
   MOGNO_COLLECTION_AKTY_FINAL - Zdrojová kolekce obsahující vyfiltrovaná znění. Defaultně PravniAktZneniOdkazyQuick
   MOGNO_COLLECTION_AKTY_ZNENI - Zdrojová kolekce obsahující původní datovou sadu. Využívá se pro hledání relevancí mezi právními akty. Defaultně PravniAktZneni  
   MONGO_USER -jméno k přístupu do dokumentové databáze Defaultně root
   MONGO_PASSWORD - heslo k přístupu do dokumentové databáze. Defaultně root
```

Pro běh není nutné vytvářet ručně jednotlivé kolekce. Komponenta Data writeru si při spuštění vytvoří databázi definovanou v properties a stejně tak i jednotlivé kolekce.

## MongoDB

Pro vytvoření databázové image a jejího spuštění  v lokálním prostředí stačí zadat následující příkazy : 

```
docker pull -t mongo:6.0
docker run -d -p 27017:27017 mongo:6.0
```
Dokumentová databáze je poté dostupná na [http://localhost:27017](http://localhost:27017)



