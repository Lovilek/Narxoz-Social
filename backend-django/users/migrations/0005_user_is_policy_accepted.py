# Generated by Django 4.2.20 on 2025-06-01 18:50

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('users', '0004_user_friends'),
    ]

    operations = [
        migrations.AddField(
            model_name='user',
            name='is_policy_accepted',
            field=models.BooleanField(default=False),
        ),
    ]
