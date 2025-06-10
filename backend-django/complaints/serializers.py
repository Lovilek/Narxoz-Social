from django.utils import timezone
from rest_framework import serializers
from django.contrib.contenttypes.models import ContentType
from rest_framework.exceptions import ValidationError

from .models import Complaint


class ComplaintSerializer(serializers.ModelSerializer):
    target_type = serializers.ChoiceField(
        choices=["user", "post", "event"], write_only=True,
        help_text="К какому объекту относится жалоба"
    )
    object_id=serializers.IntegerField()
    content_type = serializers.SerializerMethodField(read_only=True)
    author=serializers.StringRelatedField(read_only=True)
    processed_by=serializers.StringRelatedField(read_only=True)

    class Meta:
        model=Complaint
        fields=[
            "id",
            "target_type",
            "content_type",
            "object_id",
            "text",
            "attachment",
            "status",
            "processed_by",
            "processed_at",
            "author",
            "created_at",
        ]
        read_only_fields = ["status", "content_type","processed_by", "processed_at", "author", "created_at"]

    def _get_ctype(self, target_type: str) -> ContentType:
        app_map = {
            "user": ("users", "user"),
            "post": ("posts", "post"),
            "event": ("events", "event"),
        }
        app_label, model = app_map[target_type]
        return ContentType.objects.get(app_label=app_label, model=model)

    def get_content_type(self, obj):
        return obj.content_type.model

    def validate(self, attrs):
        request = self.context["request"]
        target_type = attrs["target_type"]
        object_id = attrs["object_id"]

        ctype = self._get_ctype(target_type)
        model_cls = ctype.model_class()

        if not model_cls.objects.filter(pk=object_id).exists():
            raise ValidationError(
                {"object_id": f"{target_type} with id={object_id} does not exist"}
            )
        duplicate_qs = Complaint.objects.filter(
            author=request.user,
            content_type=ctype,
            object_id=object_id,
            status=Complaint.STATUS_PENDING,
        )
        if duplicate_qs.exists():
            raise ValidationError(
                "Вы уже отправили жалобу на этот объект, она пока в ожидании."
            )
        attrs["content_type_obj"] = ctype
        return attrs

    def create(self, validated_data):
        validated_data.pop("target_type")
        ctype = validated_data.pop("content_type_obj")
        object_id = validated_data.pop("object_id")

        return Complaint.objects.create(
            author=self.context["request"].user,
            content_type=ctype,
            object_id=object_id,
            **validated_data,
        )


class ComplaintStatusSerializer(serializers.ModelSerializer):
    class Meta:
        model = Complaint
        fields = ["status"]

    def update(self, instance, validated_data):
        instance.status = validated_data["status"]
        instance.processed_by = self.context["request"].user
        instance.processed_at = timezone.localtime()
        instance.save(update_fields=["status", "processed_by", "processed_at"])
        return instance
