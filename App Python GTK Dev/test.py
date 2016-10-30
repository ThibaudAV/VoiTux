# -*- coding: utf-8 -*-
#!/usr/bin/env python
 
import sys
import time
from datetime import datetime, timedelta



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
      return round( a*X + b )
   except Exception, e:
      print "Err : Equation Joystick to Position"





def eqJoystickToPosition2(X, yMin, yMax, xMin, xMax):
   try:
		OldRange = (xMax - xMin)
		if (OldRange == 0):
			NewValue = yMin
		else:
			NewRange = (yMax - yMin)  
			NewValue = (((X - xMin) * NewRange) / OldRange) + yMin
		return round( NewValue )

   except Exception, e:
      print "Err : Equation Joystick to Position ", e




print eqJoystickToPosition(-5.0,50,0,-10,10)
print eqJoystickToPosition2(-5.0,50,0,-10,10)

value = -20

print 100 if int(value) > 100 else int(value)


lastTime = datetime.now()
# time.sleep(0.5)


print lastTime , "<" , datetime.now()


if ( lastTime + timedelta(microseconds=1000) > datetime.now() ):
	att = lastTime + timedelta(microseconds=1000) - datetime.now()
	print "Non. ",att.total_seconds()
	time.sleep(att.total_seconds())
else:
	print "ok" 













