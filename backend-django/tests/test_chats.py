import pytest
from django.core.files.uploadedfile import SimpleUploadedFile

@pytest.mark.django_db
def test_chat_file_upload(auth_client):
    dummy = SimpleUploadedFile("hello.txt", b"hi")
    r = auth_client.post(
        "/api/chats/upload-file/",
        {"chat_id": "test-chat", "files": [dummy]},
        format="multipart",
    )
    assert r.status_code in (200, 201)
