#!/bin/bash

python manage.py makemigrations --noinput

python manage.py migrate --noinput

python manage.py collectstatic --noinput

python manage.py createsuperuser --noinput \
    --login "F00000000" \
    --email "narxozsocial@gmail.com" \
    --nickname "admin" \
    --full_name "admin"

daphne -b 0.0.0.0 -p 8000 backend.asgi:application
