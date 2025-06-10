# from django.test import TestCase
# from django.contrib.auth import get_user_model
#
# from .models import Post
#
#
# class PostModelTests(TestCase):
#     def test_post_str_contains_author_nickname(self):
#         User = get_user_model()
#         user = User.objects.create_user(
#             login="S00000001",
#             full_name="Test User",
#             email="test@example.com",
#             nickname="tester",
#             password="pass",
#         )
#         post = Post.objects.create(author=user, content="Hello")
#         self.assertIn(user.nickname, str(post))