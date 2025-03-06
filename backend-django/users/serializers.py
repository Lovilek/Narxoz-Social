from rest_framework import serializers
from django.contrib.auth import authenticate
from .models import User
from django.contrib.auth import get_user_model
from django.contrib.auth.hashers import check_password


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'login', 'full_name', 'email', 'nickname', 'role', 'is_organization', 'avatar_path']


class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=6)
    role = serializers.ChoiceField(choices=User.ROLE_CHOICES)

    class Meta:
        model = User
        fields = ['login', 'full_name', 'email', 'nickname', 'password', 'is_organization', 'role']

    def validate(self, data):
        request = self.context.get("request")
        if not request.user.is_staff:
            raise serializers.ValidationError("Только администратор может регистрировать пользователей.")

        if data["role"] == "teacher" and not data["login"].startswith("F"):
            raise serializers.ValidationError(
                "Роль 'teacher' могут получить только пользователи с логином, начинающимся на 'F'.")

        if data["role"] == "moderator" and not data["login"].startswith("F"):
            raise serializers.ValidationError(
                "Роль 'moderator' могут получить только пользователи с логином, начинающимся на 'F'.")

        if data["role"] == "admin" and not data["login"].startswith("F"):
            raise serializers.ValidationError(
                "Роль 'admin' могут получить только пользователи с логином, начинающимся на 'F'.")

        if data["role"] == "student" and not data["login"].startswith("S"):
            raise serializers.ValidationError(
                "Роль 'student' могут получить только пользователи с логином, начинающимся на 'S'.")

        if data["is_organization"] == True and not data["login"].startswith("S"):
            raise serializers.ValidationError(
                "Права организаций могут получать только стдуенты.")
        return data

    def create(self, validated_data):
        return User.objects.create_user(**validated_data)


User = get_user_model()


class LoginSerializer(serializers.Serializer):
    login = serializers.CharField()
    password = serializers.CharField(write_only=True)

    def validate(self, data):
        try:
            user = User.objects.get(login=data["login"])
        except User.DoesNotExist:
            raise serializers.ValidationError("Неверный логин")

        if not check_password(data["password"], user.password):
            raise serializers.ValidationError("Неверный пароль")

        return user
