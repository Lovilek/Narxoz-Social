README.md
+61
-0

# Narxoz Social

## Requirements

- Python 3.10+
- Node.js 18+
- Docker & Docker Compose

## Installation

### Backend

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r backend-django/requirements.txt
```

### Frontend

```bash
cd frontend/narxoz-social
npm install
```

## Running Databases
Чтобы обойти отсутствие Docker, можно поднять сервисы локально из пакетов/бинарников:

PostgreSQL

Установить пакет postgresql и инициализировать каталог БД:

apt-get install -y postgresql
sudo -u postgres /usr/lib/postgresql/16/bin/initdb -D /tmp/pgdata
Запустить сервер на нужном порту:

sudo -u postgres /usr/lib/postgresql/16/bin/pg_ctl -D /tmp/pgdata \
    -o "-p 5431" -l logfile start
Redis

Установить redis-server и запустить его вручную:

apt-get install -y redis-server
redis-server --port 6379 --daemonize yes
MongoDB

В репозиториях Ubuntu нет полноценного пакета сервера, но можно скачать готовый архив с сайта MongoDB:

curl -L -o mongodb.tgz \
    https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu2204-7.0.8.tgz
tar -xzf mongodb.tgz -C /tmp
/tmp/mongodb-linux-x86_64-ubuntu2204-7.0.8/bin/mongod \
    --dbpath /tmp/mongodb-data --fork --logpath /tmp/mongod.log
После запуска всех трёх процессов можно выполнить миграции и стартовать Django. Такой подход позволяет обойти необходимость Docker, но сервисы придётся стартовать вручную при каждом запуске среды.

## Running the Backend

Apply migrations and start the development server:

```bash
cd backend-django
python manage.py migrate
python manage.py runserver
```

Start Celery with the built‑in beat scheduler:

```bash
celery -A backend beat  -l INFO
celery -A backend worker -l INFO -Q celery --pool solo  --concurrency 1 -n sched@%h
celery -A backend worker -l INFO -Q mail --pool solo --concurrency 1 -n mailer@%h
celery -A backend worker -l INFO -Q push --pool solo --concurrency 1 -n pusher@%h
```

## Running the Frontend

```bash
cd frontend/narxoz-social
npm react-router-dom
npm run dev
```
