# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import socket
import sys
import json
import os, subprocess
from xml.dom.minidom import parse, parseString


## Paramétre des servomoteurs
# servoblaster
servo_X = 1 # Servo Number 1 = GPIO 17 = PIN 11
SB_X_MiniWidth = 100
SB_X_MaxWidth = 230
servo_Y = 2 # Servo Number 2 = GPIO 18 = PIN 12

SB_Y_MiniWidth = 200
SB_Y_MaxWidth = 60

# Android 
Android_X_MiniWidth = -10 
Android_X_MaxWidth = 10

Android_Y_MiniWidth = -10 
Android_Y_MaxWidth = 10



## Paramétre de connection pour les socket 
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
	global servo_X, SB_X_MiniWidth, SB_X_MaxWidth, servo_Y, SB_Y_MiniWidth, SB_Y_MaxWidth, Android_X_MiniWidth, Android_X_MaxWidth, Android_Y_MiniWidth, Android_Y_MaxWidth
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
				position =  eqJoystickToPosition(int(value), SB_Y_MiniWidth, SB_Y_MaxWidth, Android_Y_MiniWidth, Android_Y_MaxWidth)
				setServo(servo_Y,position)

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

setCameraStream()
## Main
try:
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
finally:
	s.close()



