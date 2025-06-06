import mongoengine as me
from datetime import datetime


class Chat(me.Document):
    CHAT_TYPES=("direct","group")

    type = me.StringField(choices=CHAT_TYPES,required=True)
    owner_id=me.IntField()
    name=me.StringField()
    members=me.ListField(me.IntField(),required=True)
    created_at=me.DateTimeField(default=datetime.utcnow)
    avatar_url=me.StringField()

    unread_counters=me.MapField(field=me.IntField(),default=dict)

    meta={
        "indexes": ["members", "owner_id"]
    }
    def clean(self):
        if self.type == "direct":
            if len(self.members) !=2:
                raise me.ValidationError("Для чата только 2 участника")
            if self.name:
                raise me.ValidationError("У чата нет названия")
            if self.owner_id:
                raise me.ValidationError("У чата нет владельца")

        elif self.type == "group":
            if not self.owner_id:
                raise me.ValidationError("У группы должен быть владелец")
            if len(self.members) <2:
                raise me.ValidationError("У группы должно быть больше 1 участника")
        else:
            raise me.ValidationError("Не правильный тип")


    def can_edit_avatar(self,user):
        if self.type!="group":
            return False
        if user.id == self.owner_id:
            return True
        if user.role in ("moderator","admin"):
            return True
        return False


class Message(me.Document):
    chat=me.ReferenceField(Chat,reverse_delete_rule=me.CASCADE,required=True)
    sender=me.IntField(required=True)
    text=me.StringField()
    file_url=me.StringField()
    filename=me.StringField()
    created_at=me.DateTimeField()
    read_by=me.ListField(me.IntField(),default=list)
    share_type=me.StringField(choices=("message","profile","post"))
    share_id=me.DictField(null=True)
    meta = {
        "indexes": ["chat", "sender", "created_at"]
    }