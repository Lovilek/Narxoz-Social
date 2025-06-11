import pytest

@pytest.mark.django_db
def test_create_and_read_post(auth_client):
    resp_create = auth_client.post("/api/posts/create/", {"content": "Hello"}, format="json")
    assert resp_create.status_code == 201
    post_id = resp_create.data["id"]

    detail = auth_client.get(f"/api/posts/{post_id}/", format="json")
    assert detail.status_code == 200
    assert detail.data["id"] == post_id

