from django.db import transaction
from django.utils import timezone
from events.utils import calc_initial_stage
from django.shortcuts import render
from rest_framework import viewsets, mixins, status
from rest_framework.decorators import action
from rest_framework.generics import *
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response

from users.models import User
from users.permissions import IsAcceptPrivacy
from .models import Event, EventSubscription, EventReminder
from .serializers import EventSerializer, EventSubscriptionSerializer, EventReminderSerializer
from .permissions import IsEventOwnerOrModerator, IsModeratorOrAdmin


class EventListCreateView(ListCreateAPIView):
    queryset = Event.objects.all().select_related("created_by")
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def post(self, request, *args, **kwargs):
        if request.user.role not in ("teacher", "organization", "admin", "moderator"):
            return Response({"error": "У вас нет прав создавать события"}, status=status.HTTP_403_FORBIDDEN)
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(created_by=request.user)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


class ActiveEventListView(ListAPIView):
    queryset = Event.objects.filter(start_at__gt=timezone.localtime()).select_related("created_by")
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        return super().get_queryset().order_by("start_at")


class FinishedEventListView(ListAPIView):
    queryset = Event.objects.filter(start_at__lt=timezone.localtime()).select_related("created_by")
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        return super().get_queryset().order_by("-start_at")


class MyEventListView(ListAPIView):
    queryset = Event.objects.all().select_related("created_by")
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        return super().get_queryset().filter(created_by=self.request.user).order_by("-start_at")


class EventListByUserView(ListAPIView):
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def get_queryset(self):
        user = get_object_or_404(User, id=self.kwargs["user_id"])
        return (
            Event.objects.filter(created_by=user)
            .select_related("created_by")
            .order_by("-start_at")
        )


class EventDetailView(RetrieveUpdateDestroyAPIView):
    queryset = Event.objects.all().select_related("created_by")
    serializer_class = EventSerializer
    permission_classes = [IsAuthenticated, IsEventOwnerOrModerator, IsAcceptPrivacy]


class EventSubscribeView(GenericAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    @transaction.atomic
    def post(self, request, pk):
        event = get_object_or_404(Event, pk=pk)

        if event.created_by == request.user:
            return Response({"error": "Вы не можете подписаться на собственное событие"},
                            status=status.HTTP_400_BAD_REQUEST)

        if event.start_at <= timezone.localtime():
            return Response({"error": "Невозможно подписаться на событие, которое уже началось или прошло"},
                            status=status.HTTP_400_BAD_REQUEST)

        sub, created = EventSubscription.objects.get_or_create(event=event, user=request.user)
        if not created:
            return Response({"error": "Вы уже подписаны на это событие"}, status=status.HTTP_400_BAD_REQUEST)

        delta = event.start_at - timezone.localtime()
        stage = calc_initial_stage(delta)
        EventReminder.objects.get_or_create(subscription=sub, defaults={"stage": stage})
        return Response(self.get_serializer(sub).data, status=201)


class EventUnsubscribeView(GenericAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]

    def post(self, request, pk):
        qs = EventSubscription.objects.filter(event_id=pk, user=request.user)
        if not qs.exists():
            return Response({"error": "Вы не подписаны"}, 400)

        qs.delete()
        return Response({"message": "Вы отписались от события"}, status=204)


class MySubscriptionListView(ListAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    def get_queryset(self):
        return (
            EventSubscription.objects.filter(user=self.request.user)
            .select_related("event", "event__created_by", "eventreminder")
            .prefetch_related("event__subscriptions")
        )


class SubscriptionDetailView(RetrieveAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    def get_object(self):
        return get_object_or_404(EventSubscription, pk=self.kwargs['pk'], user=self.request.user)


class MyActiveSubscriptionListView(ListAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    def get_queryset(self):
        return (
            EventSubscription.objects
            .filter(user=self.request.user, event__start_at__gt=timezone.localtime())
            .select_related("event", "user", "eventreminder")
        )


class MyFinishedSubscriptionListView(ListAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    def get_queryset(self):
        return (
            EventSubscription.objects
            .filter(user=self.request.user, event__start_at__lt=timezone.localtime())
            .select_related("event", "user", "eventreminder")
        )


class AllRemindedListView(ListAPIView):
    permission_classes = [IsModeratorOrAdmin, IsAcceptPrivacy]
    serializer_class = EventReminderSerializer

    def get_queryset(self):
        return (
            EventReminder.objects
            .all()
        )


class SubscriptionByEventView(ListAPIView):
    permission_classes = [IsAuthenticated, IsAcceptPrivacy]
    serializer_class = EventSubscriptionSerializer

    def get_queryset(self):
        event = get_object_or_404(Event, pk=self.kwargs['event_id'])
        return (
            event.subscriptions.select_related("user", "event", "eventreminder")
            .all()
        )
