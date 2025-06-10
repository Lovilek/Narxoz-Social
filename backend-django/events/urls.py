from django.urls import path

from .views import *

urlpatterns = [
    path("", EventListCreateView.as_view(), name="event-list"),
    path("active/", ActiveEventListView.as_view(), name="active-event-list"),
    path("finished/", FinishedEventListView.as_view(), name="finished-event-list"),
    path("my/", MyEventListView.as_view(), name="my-event-list"),
    path("by-user/<int:user_id>/", EventListByUserView.as_view(), name="event-list-by-user"),
    # id пользователя нужно указывать
    path("<int:pk>/", EventDetailView.as_view(), name="event-detail"),  # id ивента нужно указывать
    path("subscribe/<int:pk>/", EventSubscribeView.as_view(), name="event-subscribe"),  # id ивента нужно указывать
    path("unsubscribe/<int:pk>/", EventUnsubscribeView.as_view(), name="event-unsubscribe"),
    # id ивента нужно указывать
    path('my-subscriptions/', MySubscriptionListView.as_view(), name='my-subscriptions'),
    path("subscription/<int:pk>/", SubscriptionDetailView.as_view(), name="subscription-detail"),
    # id подписки нужно указывать
    path('my/active-subscriptions/', MyActiveSubscriptionListView.as_view(), name='my-active-subscriptions'),
    path('my/finished-subscriptions/', MyFinishedSubscriptionListView.as_view(), name='my-finished-subscriptions'),
    path('all-reminded/', AllRemindedListView.as_view(), name='all-reminded'),
    path('subscriptions/by-event/<int:event_id>/', SubscriptionByEventView.as_view(), name='subscriptions-by-event'),

]
