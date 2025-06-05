This repository contains a Django backend and a React frontend.

## Docker
- Start the services defined in `backend-django/docker-compose.yml` with:
  ```sh
  docker-compose up -d
  ```
  Make sure the Docker daemon is running.
- To stop and remove the containers:
  ```sh
  docker-compose down
  ```

## Backend (Django)
- Apply database migrations:
  ```sh
  python manage.py migrate
  ```
- Run the test suite:
  ```sh
  python manage.py test
  ```

## Frontend
- Run ESLint checks:
  ```sh
  npm run lint
  ```
  (Execute in `frontend/narxoz-social`).

## Environment variables
The backend reads these optional variables for MongoDB connection:
- `MONGO_HOST`
- `MONGO_PORT`
- `MONGO_DB`
- `MONGO_USER`
- `MONGO_PASS`
They default to localhost and development credentials if unset.
