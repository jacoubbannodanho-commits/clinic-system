FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
ENV PORT=8080
ENV DATA_DIR=/data
EXPOSE 8080
CMD ["npm","start"]
