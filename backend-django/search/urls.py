from rest_framework.routers import DefaultRouter
from .views import *

from django.urls import path,include


router = DefaultRouter()
router.register(r'chats',ChatSearchView,basename='search-chats')
router.register(r'users', UserSearchView, basename='search-users')



urlpatterns = [
    path('',include(router.urls)),
    path('global/',GlobalSearchView.as_view(),name='global-search'),
]