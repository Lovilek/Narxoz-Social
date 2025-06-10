import os
import pytest

os.environ.setdefault("MONGO_URI", "mongomock://localhost")
os.environ.setdefault("MONGO_DB", "testdb")