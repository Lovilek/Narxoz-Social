import pytest
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient

User = get_user_model()

@pytest.mark.django_db
def test_friend_request_and_accept(auth_client):
    other = User.objects.create_user(
        login="S00000004",
        full_name="Other",
        email="other@test.com",
        nickname="other",
        password="otherpass",
    )
    if hasattr(other, "is_policy_accepted"):
        other.is_policy_accepted = True
        other.save(update_fields=["is_policy_accepted"])

    send = auth_client.post(f"/api/friends/send/{other.id}/")
    assert send.status_code in (201, 200)

    # логинимся под second user
    c2 = APIClient()
    login = c2.post("/api/users/login/", {"login": other.login, "password": "otherpass"}, format="json")
    c2.credentials(HTTP_AUTHORIZATION=f'Bearer {login.data["access"]}')

    inc = c2.get("/api/friends/incoming/")
    req_id = inc.data[0]["id"]

    accept = c2.post(
        f"/api/friends/respond/{req_id}/",
        {"action": "accept"},
        format="multipart",  # ← было json
    )
    assert accept.status_code == 200