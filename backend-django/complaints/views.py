from django.shortcuts import render
from rest_framework.generics import CreateAPIView, ListAPIView, RetrieveUpdateAPIView, RetrieveUpdateDestroyAPIView
from rest_framework.permissions import IsAuthenticated

from events.permissions import IsModeratorOrAdmin
from users.permissions import IsAcceptPrivacy
from .models import Complaint
from .serializers import ComplaintSerializer, ComplaintStatusSerializer


class ComplaintCreateView(CreateAPIView):
    queryset = Complaint.objects.all()
    serializer_class = ComplaintSerializer
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]


class MyComplaintListView(ListAPIView):
    serializer_class = ComplaintSerializer
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def get_queryset(self):
        return Complaint.objects.filter(author=self.request.user)


class ComplaintListView(ListAPIView):
    serializer_class = ComplaintSerializer
    permission_classes = [IsAuthenticated,IsAcceptPrivacy,IsModeratorOrAdmin]

    def get_queryset(self):
        return Complaint.objects.all().select_related("author","processed_by")

class ComplaintDetailView(RetrieveUpdateDestroyAPIView):
    queryset = Complaint.objects.all().select_related("author","processed_by")
    permission_classes = [IsAuthenticated,IsAcceptPrivacy,IsModeratorOrAdmin]
    def get_serializer_class(self):
        if self.request.method in ["PATCH","PUT"]:
            return ComplaintStatusSerializer
        return ComplaintSerializer
