# Ragnarok-UI

Front-end aplikace využívající Ragnarok-BE API.

## Spuštění

V první řadě je nutné spustit development server:

```bash
npm run dev
# nebo
yarn dev
# nebo
pnpm dev
# nebo
bun dev
```

Poté bude dostupný na této adrese: [http://localhost:3000](http://localhost:3000)

## Deployment

Nejjednoduší způsob je skrze Vercel: [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme)

Pro lokální spuštění lze využít přiložený Dockerfile

```docker build . -t Ragnarok-UI```

```docker run Ragnarok-UI```

## Proměnné prostředí

V kořenovém adresáři je nutné vytvořit env. soubor, do které se vloží následujcí proměnné:

``` [Typescript]
# Production: 
# NEXT_PUBLIC_RAGNAROK_UI_URL="ragnarok-ui.dyn.cloud.e-infra.cz" - adresa na které je vystavené UI
# NEXT_PUBLIC_RAGNAROK_APP_URL="http://ragnarok-be.dyn.cloud.e-infra.cz" - adresa API poskytovaného backend serverem

# COMPLETELY LOCAL:
# NEXT_PUBLIC_RAGNAROK_UI_URL="localhost:3000" - adresa na které je vystavené UI
# NEXT_PUBLIC_RAGNAROK_APP_URL="http://localhost:7777" - adresa API poskytovaného backend serverem

# REMOTE BACKEND: 
NEXT_PUBLIC_RAGNAROK_UI_URL="localhost:3000" - adresa na které je vystavené UI
NEXT_PUBLIC_RAGNAROK_APP_URL="http://ragnarok-be.dyn.cloud.e-infra.cz" - adresa API poskytovaného backend serverem
```


