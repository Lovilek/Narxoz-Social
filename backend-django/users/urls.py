from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView
from .views import *
from users.views import CustomPasswordResetView, CustomPasswordResetConfirmView

urlpatterns = [
    path('register/', RegisterView.as_view(), name='register'),
    path('login/', LoginView.as_view(), name='login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('profile/', UserProfileView.as_view(), name='profile'),
    path('logout/', LogoutView.as_view(), name='logout'),
    path('password-reset/', CustomPasswordResetView.as_view(), name='password-reset'),
    path('password-reset/confirm/', CustomPasswordResetConfirmView.as_view(), name='password-reset-confirm'),

]
