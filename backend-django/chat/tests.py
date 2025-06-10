import pytest
import mongoengine as me
import mongomock

# Create your tests here.
from chat.documents import Chat


@pytest.fixture(autouse=True)
def connect_mongo():
    me.disconnect()
    me.connect(
        "chattest",
        host="mongodb://localhost",
        mongo_client_class=mongomock.MongoClient,
    )
    yield
    me.disconnect()


def test_direct_chat_validation():
    chat = Chat(type="direct", members=[1, 2])
    chat.validate()

    bad = Chat(type="direct", members=[1])
    with pytest.raises(me.ValidationError):
        bad.validate()


def test_can_edit_avatar():
    class Dummy:
        def __init__(self, pk, role):
            self.id = pk
            self.role = role

    owner = Dummy(1, "student")
    chat = Chat(type="group", members=[1, 2], owner_id=1)
    assert chat.can_edit_avatar(owner)
    moderator = Dummy(2, "moderator")
    assert chat.can_edit_avatar(moderator)
    stranger = Dummy(3, "student")
    assert not chat.can_edit_avatar(stranger)