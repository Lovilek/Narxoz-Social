from rest_framework import serializers
from .models import Notification

class NotificationSerializer(serializers.ModelSerializer):
    user_nickname=serializers.SerializerMethodField()
    class Meta:
        model = Notification
        fields=("id","user","user_nickname","type","data","created_at","is_read")
        read_only_fields=("id","user","created_at")


    def get_user_nickname(self,obj):
        return obj.user.nickname