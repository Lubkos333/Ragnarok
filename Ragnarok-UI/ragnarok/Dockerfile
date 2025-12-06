FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app

COPY --from=builder /app ./
RUN chown -R node:node /app
USER node

EXPOSE 3000

CMD ["npm", "start"]
