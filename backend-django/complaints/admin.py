from django.contrib import admin

from .models import Complaint


@admin.register(Complaint)
class ComplaintAdmin(admin.ModelAdmin):
    list_display = ("id", "author", "content_type", "object_id", "status", "processed_by")
    list_filter = ("status", "content_type")
    search_fields = ("text",)


