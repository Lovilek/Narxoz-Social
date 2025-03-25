from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin, Group, Permission
from django.db import models
from django.core.validators import RegexValidator


class UserManager(BaseUserManager):
    def create_user(self, login, full_name,email,nickname,avatar_path=None, password=None,role="student"):
        if not login:
            raise ValueError("Логин обязателен")

        if not email:
            raise ValueError("Email обязателен")

        if not nickname:
            raise ValueError("Nickname обязателен")

        if not full_name:
            raise ValueError("Fullname обязателен")

        user = self.model(
            login=login.upper(),
            full_name=full_name,
            email=self.normalize_email(email),
            nickname=nickname,
            role=role,
            avatar_path=avatar_path,
        )
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, login, full_name,email,nickname, password,avatar_path=None,role="admin"):
        user = self.create_user(login.upper(), full_name,email,nickname, password,avatar_path)
        user.is_superuser = True
        user.is_staff = True
        user.save(using=self._db)
        return user


class User(AbstractBaseUser, PermissionsMixin):
    ROLE_CHOICES = [
        ("student", "Student"),
        ("teacher", "Teacher"),
        ("moderator", "Moderator"),
        ("organization", "Organization"),
        ("admin", "Admin"),
    ]

    login_validator = RegexValidator(
        regex=r"^[SFG]\d{8}$",
        message="Логин должен начинаться с 'S' или 'F' или 'G', за которым следует 8 цифр (например, S22016275 или F12345678)."
    )

    id = models.BigAutoField(primary_key=True)
    login = models.CharField(max_length=10, unique=True, validators=[login_validator])
    full_name = models.CharField(max_length=100)
    email = models.EmailField(unique=True)
    nickname = models.CharField(max_length=50,unique=True)
    role = models.CharField(max_length=20, choices=ROLE_CHOICES, default="student")
    avatar_path = models.ImageField(upload_to="avatars/", blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)

    groups = models.ManyToManyField(Group, related_name="custom_user_groups", blank=True)
    user_permissions = models.ManyToManyField(Permission, related_name="custom_user_permissions", blank=True)

    objects = UserManager()

    USERNAME_FIELD = "login"
    REQUIRED_FIELDS = ["full_name","email","nickname"]

    def save(self, *args, **kwargs):
        self.login=self.login.upper()
        super().save(*args, **kwargs)


    def __str__(self):
        return f"{self.login} - {self.role}"
