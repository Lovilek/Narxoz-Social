from django.conf import settings
from django.db import models
from django.utils import timezone
from users.models import User

class Event(models.Model):
    title = models.CharField(max_length=155)
    description = models.TextField(blank=True, null=True)
    start_at = models.DateTimeField()
    end_at = models.DateTimeField()
    location = models.CharField(max_length=255, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_events')
    image = models.ImageField(upload_to="events/", blank=True, null=True)


    def __str__(self):
        return self.title

    class Meta:
        ordering = ['start_at']


class EventSubscription(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    event = models.ForeignKey(Event, on_delete=models.CASCADE, related_name='subscriptions')
    joined = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('user', 'event')

    def __str__(self):
        return f"{self.user} subscribed to {self.event}"

class EventReminder(models.Model):
    STAGE_CHOICES = [
        (0, "none"),
        (1, "3h"),
        (2, "1h"),
        (3,"20m")
    ]
    subscription = models.OneToOneField(EventSubscription, on_delete=models.CASCADE,primary_key=True)
    stage=models.PositiveSmallIntegerField(choices=STAGE_CHOICES,default=0)

    def __str__(self):
        return f"{self.subscription} -> stage {self.stage}"
