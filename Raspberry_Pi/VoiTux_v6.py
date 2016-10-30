# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import socket
import threading
import sys
import time
import json
import os, subprocess
import RPi.GPIO as GPIO
import pigpio
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
      return float(a*X + b)
   except Exception, e:
      print "Err [eqScaling2Intervalle] :", e



# Obj Servo : Utilise Servoblaster pour commander le servo-moteur de la direction 
class Servo(object):

   def __init__(self, directionGPIO, directionMinGauche, directionMaxDroite):
      try:
         self.directionGPIO = directionGPIO
         self.directionMinGauche = directionMinGauche
         self.directionMaxDroite = directionMaxDroite

         # GPIO.setup(self.directionGPIO, GPIO.OUT)
         self.pi = pigpio.pi()
         self.pi.set_mode(self.directionGPIO, pigpio.OUTPUT)
         print "Init servo-moteur direction"
      except Exception, e:
         raise "Err [Init Servo] : ", e
           

   def __del__(self):
      self.pi.stop()

   def set(self, position):
      try:
         print "Direction "+`self.directionGPIO`+": "+`position`

         # PWM.add_channel_pulse(0, self.directionGPIO, 0, position)
         self.pi.set_servo_pulsewidth(self.directionGPIO, float(position))
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
         raise "Err [Init Motor] : ", e

   def __del__(self):
      self.motorPWM_avant.stop()
      self.motorPWM_arriere.stop()

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
      # On stop GStreamer
      os.system("sudo killall gst-launch-1.0")

   def run(self): 
   
      print("Connection de %s %s" % (self.ip, self.port, ))

      #demarage de GStreamer 
      os.system("raspivid -t 999999 -w 640 -h 360 -fps 25 -b 2000000 -p 0,0,640,480 -o - | gst-launch-1.0 -v fdsrc"
                " ! 'video/x-h264,width=640,height=480'"
                " ! h264parse"
                " ! queue"
                " ! rtph264pay config-interval=1 pt=96"
                " ! udpsink host="+self.ip+" port=5000 &")

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
         # Init GPIO mode 
         GPIO.setmode(GPIO.BOARD)
         os.system("sudo pigpiod")

         # Socket Server 
         self.tcpsock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
         self.tcpsock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
         self.tcpsock.bind(("",8881)) # host et port 
         self.tcpsock.listen(1) # Max connexion 

         # Direction : Servo moteur
         directionGPIO            = 4     # Servo GPIO 4 = PIN 7
         self.directionMinGauche  = 1300  # Valeur min (buté gauche)  (max max = 700)
         self.directionMaxDroite  = 1770  # Valeur max (buté droite)  (max max = 2500)
         self.direction = Servo(directionGPIO, self.directionMinGauche, self.directionMaxDroite)

         # Vitesse : Avant/Ariere avec moteur 
         self.vitesseMax_avant       = 65   # Valeur min de la marche arriere
         self.vitesseMax_arriere     = 60   # Valeur max de la marche avant 
         motorPin_avant        = 13     # = GPIO 27 = PIN 13
         motorPin_arriere      = 15     # = GPIO 22 = PIN 15
         self.vitesse = Motor(motorPin_avant, motorPin_arriere, self.vitesseMax_avant, self.vitesseMax_arriere)

         # Android 
         self.Android_X_MiniWidth = -10  # Valeur min a gauche 
         self.Android_X_MaxWidth = 10    # Valeur max a droite 

         self.Android_Y_MiniWidth = -10  # Valeur min de la marche arriere
         self.Android_Y_MaxWidth = 10    # Valeur max de la marche avant 

         # init commande a 0
         self.commande("X",0)
         self.commande("Y",0)
      except Exception, e:
         print "Err [Init VoiTux] : ", e
         exit()

   def __del__(self):
      self.tcpsock.close()
      GPIO.cleanup()

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
               position =  eqScaling2Intervalle(float(value), self.directionMinGauche, self.directionMaxDroite, self.Android_X_MiniWidth, self.Android_X_MaxWidth)
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
         print "Err [parseDatasXML] : ", e, datas


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


# Main
if __name__ == "__main__":
   try:
      if len(sys.argv) > 1:
          ipServer = sys.argv[1]
      else:
         # ipServer = '192.168.0.19'
         cmd = os.popen('ifconfig eth0 | grep "inet\ addr" | cut -d: -f2 | cut -d" " -f1')
         ipServer=cmd.read()
         ipCleints = ipServer.rsplit('.', 1)[0]+".255"
         print ipServer
         print ipCleints
      ## Solution avec TCP
      # os.system("raspivid -t 999999 -w 640 -h 360 -fps 15 -b 2000000 -p 0,0,640,480 -o - | " 
      # "gst-launch-1.0 -v fdsrc ! h264parse ! rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host="+ipServer+" port=5000 &")

      VoiTuxServer().run()
   except Exception , e:
      print e
   except KeyboardInterrupt:
      pass




