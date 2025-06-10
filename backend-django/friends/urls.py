from django.urls import path

from .views import *

urlpatterns = [
    path("send/<int:user_id>/", SendFriendRequestView.as_view(), name="send-request"),
    path("respond/<int:request_id>/", RespondFriendRequestView.as_view(), name="respond-request"),
    path("cancel/<int:request_id>/", CancelFriendRequestView.as_view(), name="cancel-request"),
    path("remove/<int:user_id>/", RemoveFriendView.as_view(), name="remove-friend"),
    path("list/", FriendsListView.as_view(), name="friends-list"),
    path("incoming/", IncomingRequestsView.as_view(), name="incoming-requests"),
    path("outgoing/", OutgoingRequestsView.as_view(), name="outgoing-requests"),
    path("status/<int:user_id>/", FriendshipStatusView.as_view(), name="friendship-status"),
    path("declined/", DeclinedRequestsView.as_view(), name="declined-requests"),
]
