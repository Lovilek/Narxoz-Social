import pytest
from django.urls import reverse
from rest_framework.test import APIClient
from django.conf import settings
from django.db import IntegrityError
from unittest.mock import patch

# Create your tests here.
from users.models import User
from .models import FriendRequest


@pytest.fixture
def create_users():
    user1 = User.objects.create_user(
        login="S00000002",
        full_name="User One",
        email="one@example.com",
        nickname="one",
        password="pass",
    )
    user2 = User.objects.create_user(
        login="S00000003",
        full_name="User Two",
        email="two@example.com",
        nickname="two",
        password="pass",
    )
    for u in (user1, user2):
        u.is_policy_accepted = True
        u.save()
    return user1, user2


@pytest.mark.django_db
def test_unique_friend_request(create_users):
    user1, user2 = create_users
    FriendRequest.objects.create(from_user=user1, to_user=user2)
    with pytest.raises(IntegrityError):
        FriendRequest.objects.create(from_user=user1, to_user=user2)


@pytest.mark.django_db
@patch("friends.views.send_friend_request_push.apply_async")
def test_send_friend_request(mock_async, create_users):
    user1, user2 = create_users
    settings.DATABASES["default"].setdefault("ATOMIC_REQUESTS", False)
    client = APIClient()
    client.force_authenticate(user1)
    url = reverse("send-request", kwargs={"user_id": user2.id})
    resp = client.post(url)
    assert resp.status_code == 201
    assert FriendRequest.objects.filter(from_user=user1, to_user=user2).exists()
    mock_async.assert_called()