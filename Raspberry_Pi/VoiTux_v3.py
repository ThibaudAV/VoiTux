# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import socket
import threading
import sys
import time
import json
import os, subprocess
import RPi.GPIO as GPIO
from RPIO import PWM
from xml.dom.minidom import parse, parseString


# Mise à l'échelle : conversion d’une variable d’un intervalle initial, en une variable appartenant à un nouvel intervalle
# Methode avec equation a 2 inconnues
def eqScaling2Intervalle(X, yMin, yMax, xMin, xMax):
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
      print "Err [eqScaling2Intervalle] :", e



# Obj Servo : Utilise Servoblaster pour commander le servo-moteur de la direction 
class Servo(object):

   def __init__(self, servo, directionMinGauche, directionMaxDroite):
      self.servo = servo
      self.directionMinGauche = directionMinGauche
      self.directionMaxDroite = directionMaxDroite
      print "Init servo-moteur direction"

   def __del__(self):
      pass

   def set(self, position):
      try:
         print "Direction "+`self.servo`+": "+`position`
         os.system("echo "+`self.servo`+"="+`position`+" > /dev/servoblaster")
         time.sleep(0.01)
         
      except Exception, e:
         print "Err [Set Servo] : ", e


# Onj Motor : Permets la marche avant et arrière avec 2 PWM. Utilisé pour
# commander un pont en H (L298N)
class Motor(object):
   SENS_AVANT = "Avant"
   SENS_ARIERRE = "Arriere"
   SENS_STOP = "Stop"

   def __init__(self, motorPin_avant, motorPin_arriere, vitesseMax_avant, vitesseMax_arriere):
      try:
         self.vitesseMax_avant = vitesseMax_avant
         self.vitesseMax_arriere = vitesseMax_arriere
         GPIO.setmode(GPIO.BOARD)
         # Init GPIO 
         GPIO.setup(motorPin_avant, GPIO.OUT, initial=GPIO.HIGH)
         GPIO.setup(motorPin_arriere, GPIO.OUT, initial=GPIO.LOW)
         # Init PWM
         self.motorPWM_avant = GPIO.PWM(motorPin_avant,50) #Init pwm avant 
         self.motorPWM_avant.start(0) 
         self.motorPWM_arriere = GPIO.PWM(motorPin_arriere,50)  #Init pwm arriere
         self.motorPWM_arriere.start(0) 
         print "Init moteur marche avant/arrière"
      except Exception, e:
         print "Err [Init Motor] : ", e

   def __del__(self):
      self.motorPWM_avant.stop()
      self.motorPWM_arriere.stop()
      GPIO.cleanup()

   def set(self, position, sens):
      try:
         if sens == self.SENS_AVANT:
            print "Motor marche avant: " , position
            self.motorPWM_arriere.ChangeDutyCycle(0)
            self.motorPWM_avant.ChangeDutyCycle(position)

         elif sens == self.SENS_ARIERRE:
            print "Motor marche  arrière: " , position
            self.motorPWM_arriere.ChangeDutyCycle(position)
            self.motorPWM_avant.ChangeDutyCycle(0)

         else:
            print "Motor STOP: " , position
            self.motorPWM_avant.ChangeDutyCycle(0)
            self.motorPWM_arriere.ChangeDutyCycle(0)
         
      except Exception, e:
         print "Err [Set Motor] : ", e


# Nouveaux Client Socket
class ClientThread(threading.Thread):
   def __init__(self, ip, port, clientsocket,voiTuxServer):

      threading.Thread.__init__(self)
      self.ip = ip
      self.port = port
      self.clientsocket = clientsocket
      self.voiTuxServer = voiTuxServer
      print("[+] Nouveau thread pour %s %s" % (self.ip, self.port, ))

   def __del__(self):
      # On stop la voiture
      self.voiTuxServer.commande("X", 0)
      self.voiTuxServer.commande("Y", 0)
      self.clientsocket.close()

   def run(self): 
   
      print("Connection de %s %s" % (self.ip, self.port, ))

      while True:
         datas = self.clientsocket.recv(1024)
         if not datas or chr(3) in datas:
            break
         elif chr(24) in datas:
            sys.exit(0)
         # On traiteles données recupérè par la socket 
         self.voiTuxServer.parseDatasXML("<root>"+datas+"</root>")

      self.clientsocket.close()
      print("Client %s déconnecté..." % (self.ip))



# Le serveur VoiTux :)
class VoiTuxServer(object):
   def __init__(self):
      try:
         print "VoiTux"
         # Socket Server 
         self.tcpsock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
         self.tcpsock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
         self.tcpsock.bind(("",8881)) # host et port 
         self.tcpsock.listen(1) # Max connexion 

         # Direction : Servo moteur
         os.system("sudo servod --p1pins=7 --idle-timeout=2000 --pcm")
         directionServo           = 0   # Servo Number 0 = GPIO 4 = PIN 7
         self.directionMinGauche  = 95  # Valeur min (buté gauche)
         self.directionMaxDroite  = 165 # Valeur max (buté droite)
         self.direction = Servo(directionServo, self.directionMinGauche, self.directionMaxDroite)

         # Vitesse : Avant/Ariere avec moteur 
         self.vitesseMax_avant       = 65   # Valeur min de la marche arriere
         self.vitesseMax_arriere     = 60    # Valeur max de la marche avant 
         motorPin_avant    = 13     # = GPIO 27 = PIN 13
         motorPin_arriere      = 15     # = GPIO 22 = PIN 15
         self.vitesse = Motor(motorPin_avant, motorPin_arriere, self.vitesseMax_avant, self.vitesseMax_arriere)

         # Android 
         self.Android_X_MiniWidth = -10  # Valeur min a gauche 
         self.Android_X_MaxWidth = 10    # Valeur max a droite 

         self.Android_Y_MiniWidth = -10  # Valeur min de la marche arriere
         self.Android_Y_MaxWidth = 10    # Valeur max de la marche avant 
      except Exception, e:
         print "Err [Init VoiTux] : ", e

   def __del__(self):
      os.system("sudo killall servod")
      self.tcpsock.close()

   ## Commande les servomoteurs avec les données recus par socket 
   def commande(self, refServo, value):
      try:
         if value == "released":
            pass
         elif value == "stopped":
            pass
         else:
            
            if refServo == "X":  # Direction
               # On recupére la position 
               position =  eqScaling2Intervalle(int(value), self.directionMinGauche, self.directionMaxDroite, self.Android_X_MiniWidth, self.Android_X_MaxWidth)
               # On envoie la nouvelle position au servomoteur
               self.direction.set(position)
            
            if refServo == "Y":  # Vitesse (Avant/Arriere)

               if(int(value) > 0): # Marche avant 
                  position =  eqScaling2Intervalle(int(value), 0, self.vitesseMax_avant, self.Android_Y_MiniWidth, self.Android_Y_MaxWidth)
                  self.vitesse.set(position, Motor.SENS_AVANT)

               elif(int(value) < 0): # Marche Arriere
                  position =  eqScaling2Intervalle(int(value), self.vitesseMax_arriere, 0, self.Android_Y_MiniWidth, self.Android_Y_MaxWidth)
                  self.vitesse.set(position, Motor.SENS_ARIERRE)

               else: # Stop
                  self.vitesse.set(0, Motor.SENS_STOP)
 
            if refServo == "MaxX":
               self.directionMaxDroite = int(value)
               self.commande("X",0)
            if refServo == "MinX":
               self.directionMinGauche = int(value)
               self.commande("X",0)

            if refServo == "MaxY":
               self.vitesseMax_avant = 100 if int(value) > 100 else int(value)
               self.commande("Y",0)
            if refServo == "MinY":
               self.vitesseMax_arriere = 100 if int(value) > 100 else int(value)
               self.commande("Y",0)

      except Exception, e:
         print "Err [Commande Servo] : ", e

   ## Récupère dans les datas recu par socket les commandes des servomoteurs
   def parseDatasXML(self, datas):
      try:
         dom = parseString(datas)
         servo = dom.getElementsByTagName('servo')
         for node in servo:
            # print "in="+node.attributes['id'].value +":"+ node.childNodes[0].data
            self.commande(node.attributes['id'].value, node.childNodes[0].data)

      except Exception, e:
         print "Err [parseDatasXML] : ", e


   def run(self):
      try:
         # Serveur Socket
         while True:
            print("Init Serveur socket: En écoute...")
            (clientsocket, (ip, port)) = self.tcpsock.accept()
            newthread = ClientThread(ip, port, clientsocket, self).run()

      except Exception, e:
         print "Err [VoiTux Run] : ", e
      finally:
         self.tcpsock.close()

os.system("raspivid -t 0 -w 640 -h 360 -fps 15 -b 1200000 -p 0,0,640,480 -o - | gst-launch -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.0.19 port=5000 &")


# os.system("raspivid -t 0 -w 640 -h 360 -fps 20 -b 1000000 -p 0,0,640,360 -o - | gst-launch -v fdsrc ! h264parse ! queue ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=192.168.0.19 port=5000 &")


# Main
if __name__ == "__main__":
   try:
      VoiTuxServer().run()
   except Exception , e:
      print e
   except KeyboardInterrupt:
      pass








