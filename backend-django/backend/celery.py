# backend/celery.py
import os
from datetime import timedelta
from celery import Celery

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")  # <-- исправили

app = Celery("narxoz_social")
app.config_from_object("django.conf:settings", namespace="CELERY")
app.autodiscover_tasks()

app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
)

app.conf.beat_schedule = {
    "event-reminders-every-1-min": {
        "task": "events.tasks.send_event_reminders",
        "schedule": timedelta(minutes=1),
    },
}
