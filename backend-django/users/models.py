from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin, Group, Permission
from django.db import models
from django.core.validators import RegexValidator


class UserManager(BaseUserManager):
    def create_user(self, login, full_name,email,nickname, password=None, is_organization=False,role="student"):
        if not login:
            raise ValueError("Логин обязателен")

        if not email:
            raise ValueError("Email обязателен")

        if not nickname:
            raise ValueError("Nickname обязателен")

        if not full_name:
            raise ValueError("Fullname обязателен")

        user = self.model(
            login=login,
            full_name=full_name,
            email=self.normalize_email(email),
            nickname=nickname,
            is_organization=is_organization,
            role=role,
        )
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, login, full_name,email,nickname, password,role="admin"):
        user = self.create_user(login, full_name,email,nickname, password)
        user.is_superuser = True
        user.is_staff = True
        user.save(using=self._db)
        return user


class User(AbstractBaseUser, PermissionsMixin):
    ROLE_CHOICES = [
        ("student", "Student"),
        ("teacher", "Teacher"),
        ("moderator", "Moderator"),
        ("admin", "Admin"),
    ]

    login_validator = RegexValidator(
        regex=r"^[SF]\d{8}$",
        message="Логин должен начинаться с 'S' или 'F', за которым следует 8 цифр (например, S22016275 или F12345678)."
    )

    id = models.BigAutoField(primary_key=True)
    login = models.CharField(max_length=10, unique=True, validators=[login_validator])
    full_name = models.CharField(max_length=100)
    email = models.EmailField(unique=True)
    nickname = models.CharField(max_length=50,unique=True)
    role = models.CharField(max_length=20, choices=ROLE_CHOICES, default="student")
    is_organization = models.BooleanField(default=False)
    avatar_path = models.CharField(max_length=255, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)

    groups = models.ManyToManyField(Group, related_name="custom_user_groups", blank=True)
    user_permissions = models.ManyToManyField(Permission, related_name="custom_user_permissions", blank=True)

    objects = UserManager()

    USERNAME_FIELD = "login"
    REQUIRED_FIELDS = ["full_name","email","nickname"]

    def __str__(self):
        org_status = " (Organization)" if self.is_organization else ""
        return f"{self.login} - {self.role}{org_status}"
