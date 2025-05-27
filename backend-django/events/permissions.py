from rest_framework.permissions import BasePermission, SAFE_METHODS

class IsEventOwnerOrModerator(BasePermission):
    def has_object_permission(self, request, view,obj):
        if request.method in SAFE_METHODS:
            return True
        return (
            obj.created_by == request.user
            or request.user.role in ['moderator', 'admin']
        )


class IsModeratorOrAdmin(BasePermission):
    def has_permission(self, request, view):
        return request.user.role in ['moderator', 'admin'] and request.user.is_authenticated

