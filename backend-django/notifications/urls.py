from django.urls import path
from .views import NotificationListView, ReadNotificationView

urlpatterns = [
    path('', NotificationListView.as_view(), name='notification-list'),
    path('read/<int:notification_id>/', ReadNotificationView.as_view(), name='read-notification'),
]
