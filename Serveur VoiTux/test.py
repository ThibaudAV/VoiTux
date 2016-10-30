# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import RPi.GPIO as GPIO
import time

servo_pin = 7  # gpio18

depart = 3      

arrivee = 11    # rapport cyclique pour que le servo 
                # soit à la fin de son mouvement
                # à ajuster pour votre servo!
     
GPIO.setmode(GPIO.BOARD)  # notation board plutôt que BCM

GPIO.setup(servo_pin, GPIO.OUT)  # pin configurée en sortie

pwm = GPIO.PWM(servo_pin, 50)  # pwm à une fréquence de 50 Hz

position = depart   # on commence à la position de départ

pwm.start(depart)  # on commence le signal pwm


DesiredAngle = 180
print float(1/18.0* (DesiredAngle) + 2)
pwm.ChangeDutyCycle(float(1/18.0* (DesiredAngle) + 2))
time.sleep (2)


DesiredAngle = 120
print float(1/18.0* (DesiredAngle) + 2)
pwm.ChangeDutyCycle(float(1/18.0* (DesiredAngle) + 2))
time.sleep (0.2)


DesiredAngle = 230
print float(1/18.0* (DesiredAngle) + 2)
pwm.ChangeDutyCycle(float(1/18.0* (DesiredAngle) + 2))
time.sleep (2)



# while True:

#     if position < arrivee:  # si nous ne sommes pas pas arrivés, 
#                             # nous avançons un peu
#          pwm.ChangeDutyCycle(float(position))  
#          position = position + 0.1
#          time.sleep (0.1)
#     else:
#          position = depart  # si nous sommes arrivés, 
#                             # retour à la position de départ



pwm.stop()