from django.db.models import Q
from django.shortcuts import render
from rest_framework import viewsets, filters
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView

from chat.documents import Chat
from posts.models import Post
from search.serializers import UserSearchSerializer, ChatSearchSerializer
from users.models import User
from rest_framework.response import Response

from users.permissions import IsAcceptPrivacy
from .serializers import *

class UserSearchView(viewsets.ReadOnlyModelViewSet):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    queryset = User.objects.all()
    serializer_class = UserSearchSerializer

    filter_backends = (filters.SearchFilter,)
    search_fields = ("full_name","email","nickname")


class ChatSearchView(viewsets.ReadOnlyModelViewSet):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]
    serializer_class = ChatSearchSerializer

    def get_queryset(self):
       user_id=self.request.user.id
       return Chat.objects.filter(members=user_id)

    def list(self, request, *args, **kwargs):
        query=request.query_params.get('search','').strip()
        base_qs=self.get_queryset()

        if not query:
            return super().list(request, *args, **kwargs)

        groups=base_qs.filter(type='group',name__icontains=query)

        directs=[]
        for chat in base_qs.filter(type='direct'):
            other_id=next((int(uid) for uid in chat.members if int(uid)!=request.user.id), None)
            if not other_id:
                continue

            if User.objects.filter(id=int(other_id),nickname__icontains=query).exists():
                directs.append(chat)

        filtered=list(groups)+directs

        page=self.paginate_queryset(filtered)

        if page is not None:
            serializer=self.get_serializer(page,many=True)
            return self.get_paginated_response(serializer.data)

        serializer=self.get_serializer(filtered,many=True)
        return Response(serializer.data)


class GlobalSearchView(APIView):
    permission_classes = [IsAuthenticated,IsAcceptPrivacy]

    def get(self, request):
        q=request.query_params.get('q','').strip()

        posts_qs=Post.objects.filter(content__icontains=q) if q else Post.objects.none()
        posts_data=PostSearchSerializer(posts_qs,many=True,context={"request":request}).data

        friends_qs = request.user.friends.all()
        if q:

            friends_qs = friends_qs.filter(
                Q(nickname__icontains=q) |
                Q(full_name__icontains=q) |
                Q(email__icontains=q)
            )
        else:
            friends_qs = friends_qs.none()
        friends_data=UserSearchSerializer(friends_qs,many=True).data

        base_chats = None

        if q:
            base_chats = Chat.objects.filter(members=str(request.user.id))

            groups = base_chats.filter(type='group', name__icontains=q)
            directs = []
            for chat in base_chats.filter(type='direct'):
                other_id = next((int(uid) for uid in chat.members if int(uid) != request.user.id), None)
                if other_id:
                    other = User.objects.filter(id=int(other_id), nickname__icontains=q).first()
                    if other:
                        directs.append(chat)

            chats_qs = list(groups) + directs
        else:
            chats_qs = base_chats


        chats_data=ChatSearchSerializer(chats_qs,many=True,context={"request":request}).data


        orgs_qs=User.objects.filter(role='organization')
        if q:
            orgs_qs=orgs_qs.filter(
                Q(nickname__icontains=q) |
                Q(full_name__icontains=q) |
                Q(email__icontains=q)
            )
        else:
            orgs_qs=orgs_qs.none()
        orgs_data=UserSearchSerializer(orgs_qs,many=True).data


        return Response({
            'posts': posts_data,
            'friends': friends_data,
            'chats': chats_data,
            'organizations': orgs_data,
        })