from datetime import timedelta

def calc_initial_stage(delta):
    """
    Calculate the initial stage based on the time delta.
    Returns:
        int: The initial stage (0, 1, 2, or 3).
    """
    if delta > timedelta(hours=3):
        return 0
    elif delta > timedelta(hours=1):
        return 1
    elif delta > timedelta(minutes=20):
        return 2
    else:
        return 3