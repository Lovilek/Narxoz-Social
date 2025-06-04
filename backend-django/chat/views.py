import os
from urllib.parse import unquote

from django.contrib.messages.storage import default_storage
from django.core.files.base import ContentFile
from rest_framework.parsers import MultiPartParser,FormParser
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from django.core.files.storage import default_storage

from rest_framework import status, viewsets, filters
from django.shortcuts import get_object_or_404

from django.conf import settings
from users.models import User
from users.permissions import IsAcceptPrivacy
from .documents import Chat, Message
from .serializers import *
from bson import ObjectId


ALLOWED_ROLES=("teacher", "organization", "moderator", "admin")
class AllChatsListAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def get(self, request):
        chats = Chat.objects(members=request.user.id)
        serializer=ChatShortSerializer(chats, many=True,context={"request": request})
        return Response(serializer.data)


class DirectListAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    def get(self, request):
        chats = Chat.objects(members=request.user.id,type='direct')
        serializer=ChatShortSerializer(chats, many=True,context={"request": request})
        return Response(serializer.data)


class GroupListAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    def get(self, request):
        chats = Chat.objects(members=request.user.id,type='group')
        serializer=ChatShortSerializer(chats, many=True,context={"request": request})
        return Response(serializer.data)



class GroupCreateAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    parser_classes = [MultiPartParser, FormParser]

    def post(self, request):
        if request.user.role not in ALLOWED_ROLES:
            return Response({"error":"Недостаточно прав "},status=403)

        serializer=GroupCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data=serializer.validated_data
        all_members = list(set(data['members']+[request.user.id]))
        avatar_file=data.get('avatar')
        avatar_url=None
        if avatar_file:
            filename=default_storage.save(f"group_avatars/{avatar_file.name}",avatar_file)
            avatar_url = default_storage.url(filename)

        chat=Chat(
            type="group",
            name=data['name'],
            owner_id=request.user.id,
            members=all_members,
            avatar_url=avatar_url,

        )
        chat.save()
        return Response({"group_id":str(chat.id)},status=201)


class UpdateGroupAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    parser_classes = [MultiPartParser, FormParser]
    def patch(self, request,chat_id):
        chat=Chat.objects(id=ObjectId(chat_id),type='group').first()
        if not chat:
            return Response({"error":"Группа не найдена"},status=404)

        user=request.user
        if user.id != chat.owner_id and user.role not in ALLOWED_ROLES:
            return Response({"error":"Недостаточно прав "},status=403)

        name=request.data.get('name')
        avatar=request.data.get('avatar_url')

        if not name or not avatar:
            return Response({"error":"Полe name или avatar_url пустые"},status=404)

        if name:
            chat.name=name

        if avatar:
            filename=default_storage.save(f"group_avatars/{avatar.name}",avatar)
            avatar_url = default_storage.url(filename)
            chat.avatar_url = avatar_url

        chat.save()
        return Response({"message":"Группа обновлена","name":chat.name,"avatar_url":chat.avatar_url},status=200)

class AddUserToGroupAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def post(self, request,chat_id):
        try:
            chat=Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error":"Чат не найден"},status=404)

        if chat.type!="group":
            return Response({"error":"Добавлять учатсников можно только в группы"},status=400)

        if not (request.user.role in ALLOWED_ROLES or request.user.id==chat.owner_id):
            return Response({"error":"Не достаточно прав"},status=403)

        user_id=request.data.get('user_id')

        if user_id is None:
            return Response({"error":"user_id обязателен"},status=400)

        try:
            user_id=int(user_id)
        except ValueError:
            return Response({"error":"user_id должен быть числом"},status=400)


        if user_id in chat.members:
            return Response({"error":"Пользователь уже в группе"},status=400)

        chat.members.append(user_id)
        chat.save()

        return Response({"message":"Пользователь добавлен"},status=200)



class RemoveUserFromGroupAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def post(self, request, chat_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if chat.type != "group":
            return Response({"error": "Удалять учатсников можно только из группы"}, status=400)

        if not (request.user.role in ALLOWED_ROLES or request.user.id == chat.owner_id):
            return Response({"error": "Не достаточно прав"}, status=403)

        user_id = request.data.get('user_id')

        if user_id is None:
            return Response({"error": "user_id обязателен"}, status=400)

        try:
            user_id = int(user_id)
        except ValueError:
            return Response({"error": "user_id должен быть числом"}, status=400)

        if user_id not in chat.members:
            return Response({"error": "Пользователь не в группе"},status=400)

        if user_id==chat.owner_id:
            return Response({"error":"Нельзя удалить владельца группы"},status=400)

        chat.members.remove(user_id)
        chat.save()

        return Response({"message": "Пользователь удален из группы"}, status=200)


class LeaveGroupAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def post(self, request, chat_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if chat.type != "group":
            return Response({"error": "Выйти можно только из группы"}, status=400)

        user_id=request.user.id

        if user_id == chat.owner_id:
            return Response({"error":"Владелец не может покинуть группу"},status=403)

        if user_id not in chat.members:
            return Response({"error":"Вы не участник этой группы"},status=400)

        chat.members.remove(user_id)
        chat.save()
        return Response({"message":"Вы покинули группу"},status=200)



class DeleteGroupAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def delete(self, request, chat_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if chat.type=="direct":
            return Response({"error":"Нельза удалять личные чаты"},status=400)

        if request.user.id != chat.owner_id and request.user.role!="admin":
            return Response({"error":"Удалять чат может только владелец или админ"},status=403)

        chat.delete()
        return Response({"message":"Группа удалена"},status=200)


class ChatDetailAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def get(self, request, chat_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if request.user.id not in chat.members:
            return Response({"error":"Вы не участник чата"},status=403)

        serializer = ChatDetailSerializer(chat,context={"request": request})
        return Response(serializer.data,status=200)



class ChatMessageListAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def get(self, request, chat_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if request.user.id not in chat.members:
            return Response({"error":"Вы не участник чата"},status=403)

        limit=int(request.query_params.get('limit',2))

        before_id=request.query_params.get('before')

        query=Message.objects(chat=chat)

        if before_id:
            try:
                query=query.filter(id__lt=ObjectId(before_id))
            except:
                return Response({"error": "Некорректный параметр before"}, status=400)

        messages=query.order_by('-id')[:limit]
        messages=list(reversed(messages))
        serializer=MessageSerializer(messages,many=True)
        return Response(serializer.data,status=200)



class DeleteMessageAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def delete(self, request, chat_id, message_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        if request.user.role not in ["moderator","admin"]:
            return Response({"error":"Только модераторы и админы могут удалять сообщения"},status=403)

        # if request.user.id not in chat.members:
        #     return Response({"error":"Вы не участник чата"},status=403)

        try:
            message=Message.objects.get(id=ObjectId(message_id),chat=chat)
        except Message.DoesNotExist:
            return Response({"error":"Сообщение не найдено"}, status=404)




        message.delete()

        return Response({"message":"Сообщение удалено"},status=200)


class EditMessageAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def patch(self, request, chat_id, message_id):
        try:
            chat = Chat.objects.get(id=ObjectId(chat_id))
        except Chat.DoesNotExist:
            return Response({"error": "Чат не найден"}, status=404)

        # if request.user.id not in chat.members:
        #     return Response({"error": "Вы не участник чата"}, status=403)

        if request.user.role not in ["moderator","admin"]:
            return Response({"error":"Только модераторы и админы могут редактировать сообщения"},status=403)

        try:
            message = Message.objects.get(id=ObjectId(message_id), chat=chat)
        except Message.DoesNotExist:
            return Response({"error": "Сообщение не найдено"}, status=404)



        new_text=request.data.get('text')
        if not new_text:
            return Response({"error":"Поле text обязательно"},status=400)
        message.text=new_text
        message.save()

        return Response({"message":"Сообщение отредактировано "},status=200)


class DirectChatView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    def post(self, request, user_id):
        try:
            other_user = User.objects.get(id=user_id)

        except User.DoesNotExist:
            return Response({"error":"Пользователь не найден"}, status=404)


        if request.user.id == other_user.id:
            return Response({"error":"Нельзя создать чат с самим собой"},status=400)

        existing_chat = Chat.objects(
            type="direct",
            members__all=[request.user.id,other_user.id],
            members__size=2
        ).first()
        if existing_chat:
            return Response({"chat_id": str(existing_chat.id)},status=200)

        chat=Chat(
            type="direct",
            members=[request.user.id,other_user.id],
        )
        chat.save()
        return Response({"chat_id": str(chat.id)},status=201)



class ChatMarkReadAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def post(self, request, chat_id):
        chat=Chat.objects(id=ObjectId(chat_id)).first()
        if not chat or request.user.id not in chat.members:
            return Response({"error":"Не найдено"},status=404)

        chat.unread_counters[str(request.user.id)] =0
        chat.save()

        Message.objects(chat=chat,read_by__ne=request.user.id).update(add_to_set__read_by=request.user.id)

        return Response({"status":"okay"},status=200)





class ChatFileUploadAPIView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    parser_classes = [MultiPartParser, FormParser]

    def post(self, request, *args, **kwargs):
        chat_id = request.data.get("chat_id")
        files=request.FILES.getlist("files")
        if not chat_id or not files:
            return Response({"error": "chat_id и files  обязательны, как минимум один файл"}, status=400)

        result=[]
        for file_obj in files:
            path=default_storage.save(f"chat/{chat_id}/{file_obj.name}", ContentFile(file_obj.read()))
            raw_url=default_storage.url(path)
            decode_url=unquote(raw_url)
            result.append({
                "file_url": decode_url,
                "filename": file_obj.name
            })
        return Response({"files": result}, status=201)