import pytest

@pytest.mark.django_db
class TestUsers:
    def test_register_by_admin(self, auth_admin_client):
        data = {
            "login": "S87654321",
            "full_name": "New Student",
            "email": "stud@test.com",
            "nickname": "stud",
            "password": "studpass12!",
            "role": "student",
            "avatar_path": "",  # ← добавили
        }
        resp = auth_admin_client.post("/api/users/register/", data, format="multipart")
        assert resp.status_code == 201
