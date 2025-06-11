import pytest
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from django.utils import timezone
from datetime import timedelta
from django.core.files.uploadedfile import SimpleUploadedFile

User = get_user_model()

# Fixtures
@pytest.fixture
def api_client():
    return APIClient()

@pytest.fixture
def admin_user(db):
    return User.objects.create_superuser(
        login='S00000001', full_name='Admin', email='admin@test.com', nickname='adminnick', password='adminpass'
    )

@pytest.fixture
def normal_user(db):
    return User.objects.create_user(
        login='S00000002', full_name='Normal', email='user@test.com', nickname='usernick', password='userpass'
    )

@pytest.fixture
def auth_client(api_client, normal_user):
    resp = api_client.post(
        '/api/users/login/', {'login': normal_user.login, 'password': 'userpass'}, format='json'
    )
    assert resp.status_code == 200
    token = resp.data['access']
    api_client.credentials(HTTP_AUTHORIZATION=f'Bearer {token}')
    return api_client

@pytest.fixture
def auth_admin_client(api_client, admin_user):
    resp = api_client.post(
        '/api/users/login/', {'login': admin_user.login, 'password': 'adminpass'}, format='json'
    )
    assert resp.status_code == 200
    token = resp.data['access']
    api_client.credentials(HTTP_AUTHORIZATION=f'Bearer {token}')
    return api_client

# Tests for Users
@pytest.mark.django_db
class TestUsers:
    def test_register_by_admin(self, auth_admin_client):
        data = {
            'login': 'S00000003',
            'full_name': 'Test User',
            'email': 'testuser@test.com',
            'nickname': 'testnick',
            'password': 'testpass',
            'role': 'student'
        }
        resp = auth_admin_client.post('/api/users/register/', data, format='json')
        assert resp.status_code == 201
        assert resp.data['login'] == 'S00000003'

    def test_register_by_non_admin_forbidden(self, auth_client):
        data = {
            'login': 'S00000004',
            'full_name': 'Another User',
            'email': 'another@test.com',
            'nickname': 'anothernick',
            'password': 'anotherpass',
            'role': 'student'
        }
        resp = auth_client.post('/api/users/register/', data, format='json')
        assert resp.status_code == 403

# Tests for Posts
@pytest.mark.django_db
def test_create_and_list_posts(auth_client):
    # Create a post
    data = {'content': 'Hello world'}
    create_resp = auth_client.post('/api/posts/create/', data, format='json')
    assert create_resp.status_code == 201
    assert create_resp.data['content'] == 'Hello world'

    # List posts
    list_resp = auth_client.get('/api/posts/', format='json')
    assert list_resp.status_code == 200
    assert any(post['content'] == 'Hello world' for post in list_resp.data.get('results', []))

# Tests for Events
@pytest.mark.django_db
def test_event_list_and_create(auth_client):
    # Ensure no events initially
    list_resp = auth_client.get('/api/events/', format='json')
    assert list_resp.status_code == 200
    assert list_resp.data.get('results') == []

    # Create an event
    start = timezone.now()
    end = start + timedelta(hours=2)
    data = {
        'title': 'PyTest Event',
        'description': 'Testing events',
        'start_at': start.isoformat(),
        'end_at': end.isoformat(),
        'location': 'Online'
    }
    create_resp = auth_client.post('/api/events/', data, format='json')
    assert create_resp.status_code == 201
    assert create_resp.data['title'] == 'PyTest Event'

# Tests for Friends
@pytest.mark.django_db
def test_friend_request_and_accept(api_client, auth_client, normal_user):
    # Create another user
    User = get_user_model()
    other = User.objects.create_user(
        login='S00000005', full_name='Other', email='other@test.com', nickname='othernick', password='otherpass'
    )

    # Send friend request
    send_resp = auth_client.post(f'/api/friends/send/{other.id}/', format='json')
    assert send_resp.status_code == 201

    # Check incoming request for other user
    client_other = APIClient()
    login_resp = client_other.post(
        '/api/users/login/', {'login': other.login, 'password': 'otherpass'}, format='json'
    )
    assert login_resp.status_code == 200
    token = login_resp.data['access']
    client_other.credentials(HTTP_AUTHORIZATION=f'Bearer {token}')

    incoming = client_other.get('/api/friends/incoming/', format='json')
    assert incoming.status_code == 200
    req_list = incoming.data
    assert len(req_list) == 1
    req_id = req_list[0]['id']

    # Accept friend request
    accept_resp = client_other.post(f'/api/friends/respond/{req_id}/', {'action': 'accept'}, format='json')
    assert accept_resp.status_code == 200
    assert 'Принят' in accept_resp.data.get('message', '')

    # Check friendship status
    status_resp = client_other.get(f'/api/friends/status/{normal_user.id}/', format='json')
    assert status_resp.status_code == 200
    assert status_resp.data['status'] == 'friends'

# Tests for Chat file upload
@pytest.mark.django_db
def test_chat_file_upload(auth_client):
    # Missing parameters
    bad_resp = auth_client.post('/api/chats/upload-file/', {}, format='multipart')
    assert bad_resp.status_code == 400

    # Valid file upload
    test_file = SimpleUploadedFile('test.txt', b'hello pytest')
    upload_resp = auth_client.post(
        '/api/chats/upload-file/', {'chat_id': 'chat123', 'files': [test_file]}, format='multipart'
    )
    assert upload_resp.status_code == 201
    result = upload_resp.data.get('files', [])
    assert len(result) == 1
    assert result[0]['filename'] == 'test.txt'

# Tests for Global Search
@pytest.mark.django_db
def test_global_search_empty(auth_client):
    resp = auth_client.get('/api/search/global/', format='json')
    assert resp.status_code == 200
    data = resp.data
    assert 'posts' in data and data['posts'] == []
    assert 'friends' in data
    assert 'chats' in data
    assert 'organizations' in data
