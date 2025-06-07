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

Launch the services defined in `backend-django/docker-compose.yml`:

```bash
docker-compose -f backend-django/docker-compose.yml up -d
```

Update the database settings in `backend-django/backend/settings.py` if the container
hosts differ:

- `DATABASES` for PostgreSQL
- `MONGODB_DATABASES` or `MONGO_*` environment variables for MongoDB
- `CELERY_BROKER_URL` and `CELERY_RESULT_BACKEND` for Redis

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
