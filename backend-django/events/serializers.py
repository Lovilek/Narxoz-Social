from django.utils import timezone
from rest_framework import serializers

from users.serializers import AnotherUserSerializer
from .models import Event, EventSubscription, EventReminder


class EventSerializer(serializers.ModelSerializer):
    created_by = serializers.StringRelatedField(read_only=True)

    class Meta:
        model = Event
        fields = '__all__'
        read_only_fields = ('created_at', 'updated_at', 'created_by')

    def validate(self, data):
        start_at = data.get('start_at') or getattr(self.instance, 'start_at', None)
        end_at = data.get('end_at') or getattr(self.instance, 'end_at', None)
        if start_at >= end_at:
            raise serializers.ValidationError("end_at должен быть позже start_at")
        if start_at <= timezone.localtime():
            raise serializers.ValidationError("Нельзя создавать события в прошлом")
        return data


class EventSubscriptionSerializer(serializers.ModelSerializer):
    event = EventSerializer(read_only=True)
    user = AnotherUserSerializer(read_only=True)
    stage = serializers.SerializerMethodField()  # 0/1/2/3

    class Meta:
        model = EventSubscription
        fields = ("id", "user", "event", "joined", "stage")
        read_only_fields = ('joined', "user")

    def _get_reminder(self, obj):
        return getattr(obj, "eventreminder", None)

    def get_stage(self, obj):
        reminder = self._get_reminder(obj)
        return reminder.stage if reminder else 0


class EventReminderSerializer(serializers.ModelSerializer):
    subscription = EventSubscriptionSerializer(read_only=True)

    class Meta:
        model = EventReminder
        fields = ("subscription", "stage")
        read_only_fields = ("subscription", "stage")

    def validate_stage(self, value):
        if value not in [0, 1, 2, 3]:
            raise serializers.ValidationError("Invalid stage value")

        return value
