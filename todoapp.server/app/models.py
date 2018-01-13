from __future__ import unicode_literals

from django.db import models


class Todo(models.Model):

    text = models.CharField(max_length=200)
    deadline = models.DateField(null=True, blank=True)
    priority = models.IntegerField(null=True, blank=True)
    done = models.BooleanField(default=False)
    memo = models.TextField(null=True, blank=True)

    def __str__(self):
        return 'Todo: ' + self.text
