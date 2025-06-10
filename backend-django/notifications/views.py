from django.shortcuts import render
from rest_framework import status
from rest_framework.generics import ListAPIView, get_object_or_404
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from notifications.models import Notification
from notifications.serializers import NotificationSerializer
from users.permissions import IsAcceptPrivacy


class NotificationListView(ListAPIView):
    serializer_class = NotificationSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        return Notification.objects.filter(user=self.request.user, is_read=False).select_related("user").order_by(
            '-created_at')


class ReadNotificationView(APIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def post(self, request, notification_id):
        notification = get_object_or_404(Notification, pk=notification_id)
        if notification.is_read:
            return Response({"error": "Уведомление уже прочитано."}, status=status.HTTP_400_BAD_REQUEST)
        notification.is_read = True
        notification.save(update_fields=["is_read"])
        return Response({"message": "Уведомление прочитано."}, status=status.HTTP_200_OK)
