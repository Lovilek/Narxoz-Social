from django.db import models
from users.models import User
# Create your models here.
class Notification(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="notifications")
    type = models.CharField(max_length=30)
    data=models.JSONField()
    created_at = models.DateTimeField(auto_now_add=True)
    is_read = models.BooleanField(default=False)

    class Meta:
        ordering=['-created_at']

    def __str__(self):
        return f"{self.user.nickname} - {self.type}"