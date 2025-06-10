
from django.urls import path

from .views import ComplaintCreateView, ComplaintListView, ComplaintDetailView, MyComplaintListView

urlpatterns = [
    path("create/", ComplaintCreateView.as_view(), name="complaint-create"),
    path("my/", MyComplaintListView.as_view(), name="my-complaint-list"),
    path("all/", ComplaintListView.as_view(), name="complaint-list"),
    path("<int:pk>/", ComplaintDetailView.as_view(), name="complaint-detail"),
]