# events/tasks.py
from celery import shared_task
from django.utils import timezone
from django.core.mail import send_mail
from django.conf import settings
from datetime import timedelta
from .models import EventSubscription, EventReminder
import logging
from django.core.mail import get_connection, EmailMessage

logger = logging.getLogger(__name__)

# → запускаем каждые 5 минут (beat_schedule ниже)
WINDOW = timedelta(minutes=5)


@shared_task(bind=True, max_retries=3, default_retry_delay=60)
def send_event_reminders(self):
    """
    Логика:
      stage 0 → отправить, если до события 2h55–3h05
      stage 1 → отправить, если до события 55–65 мин
      stage 2 → отправить, если до события 15–25 мин
      stage 3 → больше ничего.
    """
    now_local = timezone.localtime()

    rules = [
        # (current_stage, lower_bound, upper_bound, next_stage)
        (0, timedelta(hours=3) - WINDOW, timedelta(hours=3) + WINDOW, 1),
        (1, timedelta(hours=1) - WINDOW, timedelta(hours=1) + WINDOW, 2),
        (2, timedelta(minutes=20) - WINDOW, timedelta(minutes=20) + WINDOW, 3),
    ]

    for cur_stage, low, high, next_stage in rules:
        start  = now_local + low
        finish = now_local + high

        subs = (
            EventSubscription.objects
            .filter(
                event__start_at__gte=start,
                event__start_at__lte=finish,
                eventreminder__stage=cur_stage
            )
            .select_related("event", "user", "eventreminder")
        )

        logger.debug("Stage %s → нашли %s подписок", cur_stage, subs.count())

        if subs:
            with get_connection(timeout=45) as conn:
                for sub in subs:
                    try:
                        EmailMessage(
                            subject=f"Скоро начинается: {sub.event.title}",
                            body=f"{sub.event.start_at:%d.%m %H:%M} — {sub.event.description or ''}",
                            from_email=settings.DEFAULT_FROM_EMAIL,
                            to=[sub.user.email],
                            connection=conn,
                        ).send()
                        sub.eventreminder.stage = next_stage
                        sub.eventreminder.save(update_fields=["stage"])
                    except Exception as exc:
                        logger.exception("SMTP fail (%s): %s", sub.id, exc)
                        self.retry(exc=exc)

    logger.debug("✅ send_event_reminders завершён")
