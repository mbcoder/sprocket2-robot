import sys
import RPi.GPIO as GPIO
from time import sleep
import config

stepsPerDegree = 5

angle = int(sys.argv[1])
print(angle)

# work out direction of rotation (+ve = clockwise)


