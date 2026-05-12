FROM node:22-bookworm-slim

RUN apt-get update && apt-get install -y git

RUN git clone "https://github.com/morosystems/todo-be.git" /app

WORKDIR /app

RUN npm install

CMD npm start
