from django.contrib.contenttypes.fields import GenericForeignKey
from django.db import models

from users.models import User
from django.contrib.contenttypes.models import ContentType


class Complaint(models.Model):
    STATUS_PENDING="pending"
    STATUS_CANCELED="canceled"
    STATUS_RESOLVED="resolved"
    STATUS_CHOICES=[
        (STATUS_PENDING,"Pending"),
        (STATUS_CANCELED,"Canceled"),
        (STATUS_RESOLVED,"Resolved")
    ]
    author=models.ForeignKey(User,on_delete=models.CASCADE,related_name="complaints")
    content_type=models.ForeignKey(ContentType,on_delete=models.CASCADE)
    object_id=models.PositiveIntegerField()
    content_object=GenericForeignKey('content_type','object_id')

    text=models.TextField()
    attachment=models.FileField(upload_to="complaints/",null=True,blank=True)

    status=models.CharField(max_length=10,choices=STATUS_CHOICES,default=STATUS_PENDING)
    processed_at=models.DateTimeField(null=True,blank=True)
    processed_by=models.ForeignKey(
        User,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="processed_complaints")
    created_at=models.DateTimeField(auto_now_add=True)

    class Meta:
        ordering=['-created_at']

    def __str__(self):
        return f"{self.author.nickname} - {self.status} - {self.content_type} -id {self.object_id}"