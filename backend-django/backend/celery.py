import os
from datetime import timedelta
from celery import Celery
from kombu import Queue, Exchange

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")

app = Celery("narxoz_social")
app.config_from_object("django.conf:settings", namespace="CELERY")
app.autodiscover_tasks()

app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",

)

default_ex = Exchange("celery", type="direct")
app.conf.task_queues = (
    Queue("celery", default_ex, routing_key="celery"),
    Queue("mail", default_ex, routing_key="mail"),
    Queue("push", default_ex, routing_key="push"),
)

app.conf.task_routes = {
    "events.tasks.send_event_reminders": {"queue": "celery", "routing_key": "celery"},
    "events.tasks.send_event_email": {"queue": "mail", "routing_key": "mail"},
    "events.tasks.send_event_push": {"queue": "push", "routing_key": "push"},
    "friends.tasks.send_friend_request_push": {"queue": "push", "routing_key": "push"},
}

app.conf.beat_schedule = {
    "event-reminders-every-3-min": {
        "task": "events.tasks.send_event_reminders",
        "schedule": timedelta(minutes=3),
        "options": {"queue": "celery", "routing_key": "celery"},
    },
    "friend-request-every-1-min": {
        "task": "friends.tasks.send_friend_request_push",
        "schedule": timedelta(minutes=1),
        "options": {"queue": "push", "routing_key": "push"},
    }
}
