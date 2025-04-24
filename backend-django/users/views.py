from django.contrib.auth.tokens import default_token_generator
from django.core.mail import send_mail
from django.utils.encoding import force_str
from django.utils.http import urlsafe_base64_encode, urlsafe_base64_decode
from jwt.utils import force_bytes
from rest_framework import generics, status
from rest_framework.generics import UpdateAPIView, get_object_or_404
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth import authenticate

from backend import settings
from posts.permissions import IsOwnerOrReadOnly
from .models import User
from .serializers import *
from rest_framework.permissions import AllowAny, IsAuthenticated,IsAdminUser

class RegisterView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = RegisterSerializer
    permission_classes = [IsAdminUser]
    parser_classes = (MultiPartParser, FormParser)




class LoginView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = LoginSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.validated_data
            refresh = RefreshToken.for_user(user)
            return Response({
                "access": str(refresh.access_token),
                "refresh": str(refresh),
                "user": UserSerializer(user).data
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class LogoutView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        try:
            refresh_token=request.data["refresh"]
            token=RefreshToken(refresh_token)
            token.blacklist()
            return Response({"detail": "Вы успешно вышли из системы."}, status=status.HTTP_205_RESET_CONTENT)
        except Exception as e:
            return Response({"error": "Невалидный токен"}, status=status.HTTP_400_BAD_REQUEST)



class UserProfileView(generics.RetrieveAPIView):
    serializer_class = UserSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user

class UserUpdateView(generics.UpdateAPIView):
    serializer_class = UserSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        return self.request.user

class AnotherUserProfileView(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = AnotherUserSerializer
    permission_classes = [IsAuthenticated]

    def get_object(self):
        user_id=self.kwargs.get("pk")
        return get_object_or_404(User,pk=user_id)



class CustomPasswordResetView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        email = request.data.get('email')
        login=request.data.get('login')
        if not email and not login:
            return Response({"error":"Login и Email обязательны."},status=status.HTTP_400_BAD_REQUEST)

        try:
            user=User.objects.get(login__iexact=login)
        except User.DoesNotExist:
            return Response({"error":"Пользователь не найден"},status=status.HTTP_404_NOT_FOUND)

        if user.email != email:
            return Response({"error":"Пользователь не найден"},status=status.HTTP_404_NOT_FOUND)

        uid=urlsafe_base64_encode(force_bytes(str(user.pk)))
        token=default_token_generator.make_token(user)

        # reset_url=request.build_absolute_uri(f"/reset/{uid}/{token}")
        reset_url = f"http://localhost:3000/reset-password/{uid}/{token}"

        send_mail(
            subject="Сброс пароля Narxoz Social",
            message=f"Ссылка для сброса пароля: {reset_url}",
            from_email=settings.DEFAULT_FROM_EMAIL,
            recipient_list=[user.email,],
        )
        return Response({"message": "Ссылка для сброса отправлена на почту."}, status=status.HTTP_200_OK)


class CustomPasswordResetConfirmView(APIView):
    permission_classes = [AllowAny]

    def post(self,request):
        uid=request.data.get('uid')
        token=request.data.get('token')
        new_password=request.data.get('new_password')
        confirm_new_password=request.data.get('confirm_new_password')

        if new_password != confirm_new_password:
            return Response({"error":"Пароли не совпадают"},status=status.HTTP_400_BAD_REQUEST)

        if not uid or not token or not new_password:
            return Response({"error":"Заполните все поля"},status=status.HTTP_400_BAD_REQUEST)

        try:
            uid_decoded=force_str(urlsafe_base64_decode(uid))
            user=User.objects.get(pk=uid_decoded)
        except (TypeError, ValueError, OverflowError, User.DoesNotExist):
            return Response({"error":"Невалидный uid"},status=status.HTTP_400_BAD_REQUEST)

        if not default_token_generator.check_token(user,token):
            return Response({"error":"Ссылка недействительна или устарела."},status=status.HTTP_400_BAD_REQUEST)

        user.set_password(new_password)
        user.save()

        return Response({"message": "Пароль успешно сброшен."},status=status.HTTP_200_OK)

class OrganizationsListView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        organizations=User.objects.filter(role="organization")
        serializer=OrganizationSerializer(organizations,many=True)
        return Response(serializer.data)