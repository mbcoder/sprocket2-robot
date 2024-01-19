import sys
import RPi.GPIO as GPIO
from time import sleep
import config

steps360 = 2281

angle = int(sys.argv[1])
print(angle)

stepsNeeded = int((steps360/360)*angle)

print(stepsNeeded)

if angle>0:
    print("clockwise")
else:
    print("anti cw")


print(stepsNeeded)
# work out direction of rotation (+ve = clockwise)


