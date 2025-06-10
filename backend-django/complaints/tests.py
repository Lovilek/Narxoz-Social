import pytest
from rest_framework.test import APIRequestFactory
from rest_framework.exceptions import ValidationError

# Create your tests here.
from users.models import User
from posts.models import Post
from .serializers import ComplaintSerializer


@pytest.fixture
def factory():
    return APIRequestFactory()


@pytest.fixture
def user(db):
    u = User.objects.create_user(
        login="S00000004",
        full_name="Auth",
        email="auth@example.com",
        nickname="auth",
        password="pass",
    )
    u.is_policy_accepted = True
    u.save()
    return u


def _make_request(factory, user):
    request = factory.post("/")
    request.user = user
    return request


@pytest.mark.django_db
def test_duplicate_validation(factory, user):
    post = Post.objects.create(author=user, content="hello")
    data = {"target_type": "post", "object_id": post.id, "text": "spam"}
    ser = ComplaintSerializer(data=data, context={"request": _make_request(factory, user)})
    ser.is_valid(raise_exception=True)
    ser.save()

    ser2 = ComplaintSerializer(data=data, context={"request": _make_request(factory, user)})
    with pytest.raises(ValidationError):
        ser2.is_valid(raise_exception=True)


@pytest.mark.django_db
def test_get_ctype_user(factory, user):
    ser = ComplaintSerializer(context={"request": _make_request(factory, user)})
    ctype = ser._get_ctype("user")
    assert ctype.model == "user"