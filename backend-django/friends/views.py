from django.db import transaction
from django.db.models import Q
from django.shortcuts import render
from rest_framework import status
from rest_framework.generics import get_object_or_404, ListAPIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from friends.models import FriendRequest
from friends.serializers import FriendRequestSerializer
from users.models import User
from users.serializers import UserSerializer


class SendFriendRequestView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, user_id):
        to_user = get_object_or_404(User, id=user_id)
        if request.user == to_user:
            return Response({"error": "Нельзя отправить себе запрос"}, status=400)

        if to_user in request.user.friends.all():
            return Response({"error": "Вы уже друзья"}, status=400)

        if FriendRequest.objects.filter(
            (Q(from_user=request.user, to_user=to_user) |
             Q(from_user=to_user,    to_user=request.user)),
            status="pending"
        ).exists():
            return Response({"error": "Уже существует активный запрос"}, status=400)

        declined = FriendRequest.objects.filter(
            (Q(from_user=request.user, to_user=to_user) |
             Q(from_user=to_user,    to_user=request.user)),
            status="declined"
        ).first()

        if declined:
            declined.from_user = request.user
            declined.to_user   = to_user
            declined.status    = "pending"
            declined.save()
            return Response({"message": "Запрос отправлен повторно"}, status=201)

        FriendRequest.objects.create(
            from_user=request.user,
            to_user=to_user,
            status="pending",
        )
        return Response({"message": "Запрос отправлен"}, status=201)


class RespondFriendRequestView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request,request_id):
        friend_request=get_object_or_404(FriendRequest,id=request_id)
        if friend_request.to_user != request.user:
            return Response({"error": "Вы не можете управлять чужим запросом в друзья."}, status=403)
        action=request.data.get("action")

        if friend_request.status == "accepted":
            return Response({"error": "Запрос уже принят"}, status=400)

        if action=="accept":
            friend_request.status="accepted"
            friend_request.save()
            request.user.friends.add(friend_request.from_user)
            friend_request.from_user.friends.add(request.user)
            return Response({"message":"Запрос принят"})

        elif action=="decline":
            friend_request.status="declined"
            friend_request.save()
            return Response({"message":"Запрос отклонен"})

        return Response({"error":"Неправильное значение"},status=400)


class CancelFriendRequestView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request,request_id):
        friend_request=get_object_or_404(FriendRequest,id=request_id)
        if friend_request.from_user!=request.user:
            return Response({"error":"Вы не можете отменить чужой запрос"},status=403)
        if friend_request.status=="accepted":
            return Response({"error":"Запрос уже принят"},status=400)
        friend_request.delete()
        return Response({"message":"Запрос удален"})


class RemoveFriendView(APIView):
    permission_classes = [IsAuthenticated]

    @transaction.atomic
    def delete(self, request,user_id):
        friend=get_object_or_404(User,id=user_id)
        if friend==request.user:
            return Response({"error":"Нельзя удалить себя"},status=400)

        if friend in request.user.friends.all():
            request.user.friends.remove(friend)
            friend.friends.remove(request.user)

            FriendRequest.objects.filter(
                Q(from_user=request.user,to_user=friend) |
                Q(from_user=friend, to_user=request.user)
            ).delete()

            return Response({"message":"Пользователь удален из друзей"})

        return Response({"error":"Этот пользователь не ваш друг"},status=403)

class FriendsListView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        friends=request.user.friends.all()
        return Response(UserSerializer(friends,many=True).data)



class IncomingRequestsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        requests=FriendRequest.objects.filter(to_user=request.user,status="pending")
        return Response(FriendRequestSerializer(requests,many=True).data)


class DeclinedRequestsView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        requests=FriendRequest.objects.filter(to_user=request.user,status="declined")
        return Response(FriendRequestSerializer(requests,many=True).data)


class OutgoingRequestsView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        requests=FriendRequest.objects.filter(from_user=request.user,status="pending")
        return Response(FriendRequestSerializer(requests,many=True).data)


class FriendshipStatusView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request,user_id):
        other_user=get_object_or_404(User,id=user_id)

        if request.user==other_user:
            return Response({"status":"self"})

        if other_user in request.user.friends.all():
            return Response({"status":"friends"})

        if FriendRequest.objects.filter(from_user=request.user,to_user=other_user,status="pending").exists():
            return Response({"status":"outgoing_request"})

        if FriendRequest.objects.filter(from_user=other_user, to_user=request.user, status="pending").exists():
            return Response({"status": "incoming_request"})

        if FriendRequest.objects.filter(from_user=request.user, to_user=other_user, status="declined").exists():
            return Response({"status": "outgoing_declined_request"})

        if FriendRequest.objects.filter(from_user=other_user, to_user=request.user, status="declined").exists():
            return Response({"status": "incoming_declined_request"})

        return Response({"status":"None"})




