from datetime import timedelta
import pytest

# Create your tests here.
from events.utils import calc_initial_stage


def test_calc_initial_stage():
    assert calc_initial_stage(timedelta(hours=4)) == 0
    assert calc_initial_stage(timedelta(hours=2)) == 1
    assert calc_initial_stage(timedelta(minutes=30)) == 2
    assert calc_initial_stage(timedelta(minutes=10)) == 3