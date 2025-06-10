# from django.test import TestCase
#
# # Create your tests here.
# from django.test import TestCase, RequestFactory
#
# from posts.models import Post
# from users.models import User
#
# from .serializers import PostSearchSerializer, UserSearchSerializer
#
#
# class PostSearchSerializerTests(TestCase):
#     def setUp(self):
#         self.user = User.objects.create_user(
#             login="s22000099",
#             full_name="Test Poster",
#             email="poster@example.com",
#             nickname="poster",
#             password="secret",
#         )
#         self.post = Post.objects.create(author=self.user, content="hello world")
#
#     def test_post_serializer_fields(self):
#         serializer = PostSearchSerializer(self.post, context={})
#         data = serializer.data
#         self.assertEqual(data["author"], "poster")
#         self.assertEqual(data["author_id"], self.user.id)
#         self.assertIsNone(data["author_avatar_path"])
#
#
# class UserSearchSerializerTests(TestCase):
#     def test_user_serializer_fields(self):
#         user = User.objects.create_user(
#             login="s22000100",
#             full_name="Some User",
#             email="user@example.com",
#             nickname="simple",
#             password="secret",
#         )
#
#         serializer = UserSearchSerializer(user)
#         data = serializer.data
#         self.assertEqual(data["nickname"], "simple")
#         self.assertEqual(data["email"], "user@example.com")