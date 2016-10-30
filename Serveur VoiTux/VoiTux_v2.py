# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import socket
import sys
import json
import os, subprocess
import RPi.GPIO as GPIO
from xml.dom.minidom import parse, parseString

# #################################################################
# Paramétre et init 
# #################################################################

# servoblaster (direction)


os.system("sudo servod --p1pins=7")

servo_X = 0 # Servo Number 0 = GPIO 17 = PIN 11
SB_X_MiniWidth = 90 # Valeur min (buté gauche)
SB_X_MaxWidth = 160 # Valeur max (buté droite)

# moteur avec pont en H 'L298N' (avant/arriere)
GPIO.setmode(GPIO.BOARD)
GPIO.setup(13, GPIO.OUT, initial=GPIO.LOW) # = GPIO 27 = PIN 13
GPIO.setup(15, GPIO.OUT, initial=GPIO.LOW) # = GPIO 22 = PIN 15

motorPWM_avant = GPIO.PWM(15,50)	#Init pwm avant 
motorPWM_arriere = GPIO.PWM(13,50)	#Init pwm arriere 

SB_Y_MiniWidth = -100	# Valeur min de la marche arriere
SB_Y_MaxWidth  = 100	# Valeur max de la marche avant 

# Android 
Android_X_MiniWidth = -10 	# Valeur min a gauche 
Android_X_MaxWidth = 10		# Valeur max a droite 

Android_Y_MiniWidth = -10 	# Valeur min de la marche arriere
Android_Y_MaxWidth = 10		# Valeur max de la marche avant 


# Paramétre de connection pour les socket 
host = ''
# host = 'localhost' # to restrict to localhost connections only.
port = 8881
pending_connections = 1
buffer_size = 1024

## Ecoute de l'adress et du port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((host, port))
s.listen(pending_connections)


## Change la position du servomoteur 
## @servoChannel : numéro du chanel de servoblaster
## @position : position du servomoteur [50,250]
def setServo(servoChannel, position):
	try:
		# with open("/dev/servoblaster", "w") as f:
		#  	f.write("%d=%d" % (servoChannel, position))
		print "setServo = "+`servoChannel`+":"+`position`
		os.system("echo "+`servoChannel`+"="+`position`+" > /dev/servoblaster")
		
	except Exception, e:
		print "Err : Set Servo"
## 
def setMotor(position):
	global motorPWM_avant, motorPWM_arriere, SB_Y_MiniWidth, SB_Y_MaxWidth

	try:
		yMoy = (SB_Y_MaxWidth + SB_Y_MiniWidth) / 2.0
		if yMoy > position:
			print "Y motorPWM_arriere: " , position + 200.0
			motorPWM_arriere.ChangeDutyCycle(position + 200.0)
			motorPWM_avant.ChangeDutyCycle(100)

		elif yMoy < position:
			print "Y motorPWM_avant: " , position
			motorPWM_arriere.ChangeDutyCycle(100)
			motorPWM_avant.ChangeDutyCycle(position)

		else:
			print "Y motor STOP: " , position
			motorPWM_avant.ChangeDutyCycle(100)
			motorPWM_arriere.ChangeDutyCycle(100)


	except Exception, e:
		print "Err : Set Motor"
		print e

## equation a 2 inconnues pour convertir les valeurs des Joystick en position 
def eqJoystickToPosition(X, yMin, yMax, xMin, xMax):
	try:
		# Les moyenne
		yMoy = (yMax + yMin) / 2.0
		xMoy = (xMax + xMin) / 2.0
		# 1er inconnue
		a = (yMoy - yMin) / (-xMin + xMoy)
		# print "a:"+`a`+"=("+`yMoy`+"-"+`yMin`+")/(-"+`xMin`+"+"+`xMoy`+")"
		# 2e inconnue
		b = yMoy - xMoy*a
		# print "b:"+`b`
		
		# L'equation Y = aX+b
		return int(round( a*X + b ))
	except Exception, e:
		print "Err : Equation Joystick to Position"


## Commande les servomoteurs avec les données recus par socket 
def commandeServo(refServo,value):
	global servo_X, SB_X_MiniWidth, SB_X_MaxWidth, SB_Y_MiniWidth, SB_Y_MaxWidth, Android_X_MiniWidth, Android_X_MaxWidth, Android_Y_MiniWidth, Android_Y_MaxWidth
	try:
		if value == "released":
			pass
		elif value == "stopped":
			pass
		else:
			if refServo == "X":
				# On recupére la position 
				position =  eqJoystickToPosition(int(value), SB_X_MiniWidth, SB_X_MaxWidth, Android_X_MiniWidth, Android_X_MaxWidth)
				# On envoie la nouvelle position au servomoteur
				setServo(servo_X,position)

			if refServo == "Y":
				## En Y il n'y a plus de servo mais une pwm de moteur 
				position =  eqJoystickToPosition(int(value), SB_Y_MiniWidth, SB_Y_MaxWidth, Android_Y_MiniWidth, Android_Y_MaxWidth)
				#setServo(servo_Y,position)
				setMotor(position)

			if refServo == "MaxX":
				SB_X_MaxWidth = int(value)
				commandeServo("X",0)
			if refServo == "MinX":
				SB_X_MiniWidth = int(value)
				commandeServo("X",0)

			if refServo == "MaxY":
				SB_Y_MaxWidth = int(value)
				commandeServo("Y",0)
			if refServo == "MinY":
				SB_Y_MiniWidth = int(value)
				commandeServo("Y",0)

	except Exception, e:
		print "Err : Commande Servo"
		print e



## Récupère dans les datas recu par socket les commandes des servomoteurs
def parseServoDatasXML(datas):
	try:
		dom = parseString(datas)
		servo = dom.getElementsByTagName('servo')
		for node in servo:
			# print node.toxml()
			print "in="+node.attributes['id'].value +":"+ node.childNodes[0].data
			commandeServo(node.attributes['id'].value, node.childNodes[0].data)
	except Exception, e:
		print "Err : parse servo datas"

tt = "<servo id='X'>0</servo><servo id='Y'>-10</servo>"
parseServoDatasXML("<root>"+tt+"</root>")


def setCameraStream():
	try:
		# with open("/dev/servoblaster", "w") as f:
		#  	f.write("%d=%d" % (servoChannel, position))
		print "Set Camera Stream"
		# os.system("echo "+`servoChannel`+"="+`position`+" > /dev/servoblaster")
		# 
		# raspivid -t 0 -w 1280 -h 720 -fps 25 -b 2500000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000
		# 
		# SD High: -w 960 -h 540 -fps 25 -b  1800000
		# raspivid -t 0 -w 960 -h 540 -fps 25 -b 1800000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000
		# 
		os.system("raspivid -t 0 -w 640 -h 360 -fps 25 -b 1200000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000 &")
		# os.spawnl(os.P_NOWAIT,"raspivid -t 0 -w 960 -h 540 -fps 25 -b 1800000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.1.48 port=5000")
		
	except Exception, e:
		print "Err : Set Camera Stream"
		print e

#setCameraStream()




## Main
try:
	motorPWM_avant = GPIO.PWM(15,50)	#Init pwm avant 
	motorPWM_arriere = GPIO.PWM(13,50)	#Init pwm arriere 

	while True:
		print "\n\nWaiting for connection..."
		client, address = s.accept()
		print "Client connected: " + str(address)
		while True:
			datas = client.recv(buffer_size)
			if not datas or chr(3) in datas:
				break
			elif chr(24) in datas:
				sys.exit(0)
			# On traiteles données recupérè par la socket 
			parseServoDatasXML("<root>"+datas+"</root>")
		client.close()
		print "\nClient disconnected: " + str(address)
except Exception, e:
	print "Err : Set Camera Stream"
	print e
except KeyboardInterrupt:
        pass
finally:
	s.close()
	client.close()
	motorPWM_avant.stop()
	motorPWM_arriere.stop()
	GPIO.cleanup()



