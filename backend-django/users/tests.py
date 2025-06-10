# from django.test import TestCase
# from django.utils import timezone
#
# from users.models import User
# from users.serializers import LoginSerializer
#
#
# class UserModelTests(TestCase):
#     def test_create_user_uppercase_login(self):
#         user = User.objects.create_user(
#             login="s22000000",
#             full_name="Test User",
#             email="test@example.com",
#             nickname="tester",
#             password="secret",
#         )
#         self.assertEqual(user.login, "S22000000")
#         self.assertTrue(user.check_password("secret"))
#
#     def test_create_superuser_flags(self):
#         admin = User.objects.create_superuser(
#             login="f12345678",
#             full_name="Admin User",
#             email="admin@example.com",
#             nickname="admin",
#             password="secret",
#         )
#         self.assertTrue(admin.is_staff)
#         self.assertTrue(admin.is_superuser)
#
#     def test_is_online_property(self):
#         user = User.objects.create_user(
#             login="s22000001",
#             full_name="Another User",
#             email="another@example.com",
#             nickname="another",
#             password="secret",
#         )
#         user.last_seen = timezone.now()
#         user.save()
#         self.assertTrue(user.is_online)
#
#         user.last_seen = timezone.now() - timezone.timedelta(minutes=11)
#         user.save()
#         self.assertFalse(user.is_online)
#
#
# class LoginSerializerTests(TestCase):
#     def test_login_serializer_success(self):
#         user = User.objects.create_user(
#             login="s22000002",
#             full_name="Login User",
#             email="login@example.com",
#             nickname="loginuser",
#             password="secret",
#         )
#
#         serializer = LoginSerializer(
#             data={"login": "s22000002", "password": "secret"}
#         )
#         self.assertTrue(serializer.is_valid(), serializer.errors)
#         self.assertEqual(serializer.validated_data, user)
#
#
# from django.test import TestCase
#
# # Create your tests here.
