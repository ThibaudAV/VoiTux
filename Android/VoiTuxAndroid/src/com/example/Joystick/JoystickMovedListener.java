package com.example.Joystick;

public interface JoystickMovedListener {
	public void OnMoved(int pan, int tilt, int angle);
	public void OnReleased();
	public void OnReturnedToCenter();
}
