from rest_framework import permissions


class IsAcceptPrivacy(permissions.BasePermission):
    message = 'Необходимо принять Политику конфиденциальности.'

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False

        if view.__class__.__name__ == 'AcceptPrivacyPolicyView':
            return True

        return user.is_policy_accepted
