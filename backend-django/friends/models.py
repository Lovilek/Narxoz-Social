from django.db import models
from users.models import User


# Create your models here.

class FriendRequest(models.Model):
    from_user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="sent_requests")
    to_user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="received_requests")
    created_at = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=10, default="pending", choices=[
        ("pending", "pending"),
        ("accepted", "accepted"),
        ("declined", "declined")
    ])

    class Meta:
        unique_together = ('from_user', 'to_user')

    def __str__(self):
        return f"{self.id} - Request from {self.from_user.nickname} to {self.to_user.nickname}"
