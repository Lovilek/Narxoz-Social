# from celery import shared_task
# from django.utils import timezone
# from django.core.mail import send_mail
# from django.conf import settings
# from datetime import timedelta
# from .models import EventSubscription, EventReminder
# import logging
# from django.core.mail import get_connection, EmailMessage
# from asgiref.sync import async_to_sync
# from channels.layers import get_channel_layer
# channel_layer = get_channel_layer()
#
# logger = logging.getLogger(__name__)
#
# # → запускаем каждые 5 минут (beat_schedule ниже)
# WINDOW = timedelta(minutes=5)
#
#
# @shared_task(bind=True, max_retries=3, default_retry_delay=60)
# def send_event_reminders(self):
#     """
#     Логика:
#       stage 0 → отправить, если до события 2h55–3h05
#       stage 1 → отправить, если до события 55–65 мин
#       stage 2 → отправить, если до события 15–25 мин
#       stage 3 → больше ничего.
#     """
#     now_local = timezone.localtime()
#
#     rules = [
#         # (current_stage, lower_bound, upper_bound, next_stage)
#         (0, timedelta(hours=3) - WINDOW, timedelta(hours=3) + WINDOW, 1),
#         (1, timedelta(hours=1) - WINDOW, timedelta(hours=1) + WINDOW, 2),
#         (2, timedelta(minutes=20) - WINDOW, timedelta(minutes=20) + WINDOW, 3),
#     ]
#
#     for cur_stage, low, high, next_stage in rules:
#         start  = now_local + low
#         finish = now_local + high
#
#         subs = (
#             EventSubscription.objects
#             .filter(
#                 event__start_at__gte=start,
#                 event__start_at__lte=finish,
#                 eventreminder__stage=cur_stage
#             )
#             .select_related("event", "user", "eventreminder")
#         )
#
#         logger.debug("Stage %s → нашли %s подписок", cur_stage, subs.count())
#
#         if subs:
#             with get_connection(timeout=45) as conn:
#                 for sub in subs:
#                     try:
#                         EmailMessage(
#                             subject=f"Скоро начинается: {sub.event.title}",
#                             body=f"{sub.event.start_at:%d.%m %H:%M} — {sub.event.description or ''}",
#                             from_email=settings.DEFAULT_FROM_EMAIL,
#                             to=[sub.user.email],
#                             connection=conn,
#                         ).send()
#                         sub.eventreminder.stage = next_stage
#                         sub.eventreminder.save(update_fields=["stage"])
#
#                         async_to_sync(channel_layer.group_send)(
#                             f"user-{sub.user_id}",
#                             {
#                                 "type": "event_reminder",  # имя хэндлера в consumer
#                                 "event_id": sub.event_id,
#                                 "description":sub.event.description,
#                                 "location": sub.event.location,
#                                 "title": sub.event.title,
#                                 "start_at": sub.event.start_at.isoformat(),
#                                 "stage": cur_stage,  # 0/1/2 (3 уже не шлём)
#                             }
#                         )
#                     except Exception as exc:
#                         logger.exception("SMTP fail (%s): %s", sub.id, exc)
#                         self.retry(exc=exc)
#
#
#
#     logger.debug("✅ send_event_reminders завершён")
#
#
#
#
#

# events/tasks.py
from celery import shared_task
from celery.utils.log import get_task_logger
from django.utils import timezone
from django.core.mail import get_connection, EmailMessage
from django.conf import settings
from datetime import timedelta
from .models import EventSubscription
from asgiref.sync import async_to_sync
from channels.layers import get_channel_layer

logger = get_task_logger(__name__)
channel_layer = get_channel_layer()

WINDOW = timedelta(minutes=10)

@shared_task(bind=True, max_retries=3, default_retry_delay=60)
def send_event_reminders(self):
    now_local = timezone.localtime()
    logger.info("→ send_event_reminders START at %s", now_local.isoformat())

    rules = [
        (0, timedelta(hours=3)  - WINDOW, timedelta(hours=3)  + WINDOW, 1),
        (1, timedelta(hours=1)  - WINDOW, timedelta(hours=1)  + WINDOW, 2),
        (2, timedelta(minutes=20)- WINDOW, timedelta(minutes=20)+ WINDOW, 3),
    ]

    total = 0
    for cur_stage, low, high, next_stage in rules:
        start  = now_local + low
        finish = now_local + high
        logger.info(
            "  Rule stage=%d: window [%s → %s]",
            cur_stage, start.isoformat(), finish.isoformat()
        )

        subs = EventSubscription.objects.filter(
            event__start_at__gte=start,
            event__start_at__lte=finish,
            eventreminder__stage=cur_stage
        ).select_related("event", "user", "eventreminder")
        count = subs.count()
        logger.info("    Found %d subs for stage %d", count, cur_stage)

        for sub in subs:
            logger.info("    Scheduling for sub_id=%s (event=%s)", sub.pk, sub.event.pk)
            send_event_email.apply_async((sub.pk,), queue="mail")
            send_event_push .apply_async((sub.pk,), queue="push")

            sub.eventreminder.stage = next_stage
            sub.eventreminder.save(update_fields=["stage"])
            total += 1

    logger.info("← send_event_reminders FINISHED, scheduled %d tasks", total)


@shared_task(queue="mail", bind=True, max_retries=3, default_retry_delay=60)
def send_event_email(self, sub_id: int) -> None:
    logger.info("→ send_event_email START for sub_id=%s", sub_id)
    try:
        sub = EventSubscription.objects.select_related("event", "user").get(pk=sub_id)
    except EventSubscription.DoesNotExist:
        logger.error("    NOT FOUND subscription %s", sub_id)
        return

    ev, user = sub.event, sub.user
    subject = f"Скоро начинается: {ev.title}"
    # body    = f"{ev.start_at:%d.%m %H:%M} — {ev.description or ''}"
    local_start = timezone.localtime(ev.start_at)
    body=(
    f"Здравствуйте, {user.full_name or user.nickname}!\n\n"
    f"Напоминаем вам о предстоящем мероприятии «{ev.title}».\n"
    f"Описание: {ev.description or 'не указано'}\n"
    f"Дата и время: {local_start:%d.%m.%Y %H:%M}\n"
    f"Место проведения: {ev.location or 'не указано'}\n\n"
    "Будем рады видеть вас на нашем мероприятии!"
    )
    with get_connection(fail_silently=False) as conn:
        EmailMessage(subject, body, settings.DEFAULT_FROM_EMAIL, [user.email], connection=conn).send()
    logger.info("← send_event_email DONE for sub_id=%s → sent to %s", sub_id, user.email)


@shared_task(queue="push", bind=True, max_retries=3, default_retry_delay=60)
def send_event_push(self, sub_id: int) -> None:

    logger.info("→ send_event_push START for sub_id=%s", sub_id)
    try:
        sub = EventSubscription.objects.select_related("event", "user").get(pk=sub_id)
    except EventSubscription.DoesNotExist:
        logger.error("    NOT FOUND subscription %s", sub_id)
        return

    ev, user = sub.event, sub.user
    local_start = timezone.localtime(ev.start_at)

    payload = {
        "type": "event_reminder",
        "event": {
            "id":       ev.pk,
            "title":    ev.title,
            "start_at": local_start.isoformat(),
            "description":     ev.description,
            "location": ev.location,
        },
    }
    async_to_sync(channel_layer.group_send)(
        f"user-{user.pk}", {"type": "event_reminder", "data": payload}
    )
    logger.info("← send_event_push DONE for sub_id=%s → user %s", sub_id, user.pk)
