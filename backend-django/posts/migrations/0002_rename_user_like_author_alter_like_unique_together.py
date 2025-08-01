# Generated by Django 5.1.6 on 2025-03-12 19:48

from django.conf import settings
from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('posts', '0001_initial'),
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.RenameField(
            model_name='like',
            old_name='user',
            new_name='author',
        ),
        migrations.AlterUniqueTogether(
            name='like',
            unique_together={('author', 'post')},
        ),
    ]
