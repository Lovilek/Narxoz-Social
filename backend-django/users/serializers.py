from rest_framework import serializers
from django.contrib.auth import authenticate
from .models import User
from django.contrib.auth import get_user_model
from django.contrib.auth.hashers import check_password

class FriendShortSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'nickname']

class UserSerializer(serializers.ModelSerializer):
    friends = serializers.SerializerMethodField()
    class Meta:
        model = User
        fields = ['id', 'login', 'full_name', 'email', 'nickname', 'role','friends', 'avatar_path','is_policy_accepted']
        read_only_fields = ['id', 'login', 'full_name', 'email', 'role','friends']

    def get_friends(self, obj):
        friends = obj.friends.all()
        return FriendShortSerializer(friends, many=True).data



class OrganizationSerializer(serializers.ModelSerializer):

    class Meta:
        model=User
        fields = ['id', 'full_name', 'nickname', 'avatar_path']
        read_only_fields = ['id', 'full_name', 'nickname', 'avatar_path']


class AnotherUserSerializer(serializers.ModelSerializer):
    friends = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ['id', 'full_name', 'nickname', 'avatar_path','friends']

    def get_friends(self, obj):
        friends = obj.friends.all()
        return FriendShortSerializer(friends, many=True).data



class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=6)
    role = serializers.ChoiceField(choices=User.ROLE_CHOICES)

    class Meta:
        model = User
        fields = ['login', 'full_name', 'email', 'nickname', 'password', 'role','avatar_path']

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

        if data["role"] == "organization" and not data["login"].startswith("G"):
            raise serializers.ValidationError(
                "Роль 'organization' могут получить только пользователи с логином, начинающимся на 'G'.")
        return data

    def create(self, validated_data):
        return User.objects.create_user(**validated_data)


User = get_user_model()


class LoginSerializer(serializers.Serializer):
    login = serializers.CharField()
    password = serializers.CharField(write_only=True)

    def validate(self, data):
        login = data["login"].upper()

        try:

            user = User.objects.get(login__iexact=login)
        except User.DoesNotExist:
            raise serializers.ValidationError("Неверный логин")

        if not check_password(data["password"], user.password):
            raise serializers.ValidationError(f"Неверный пароль, то что написали вы:{data['password']}, ожидаемый:{user.password}")

        return user
