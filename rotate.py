import sys
import RPi.GPIO as GPIO
from time import sleep
import config

steps360 = 2281

angle = int(sys.argv[1])
print(angle)

stepsNeeded = abs(int((steps360 / 360) * angle))

# setup gpio
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(config.enablePin, GPIO.OUT)
GPIO.setup(config.directionPinLeft, GPIO.OUT)
GPIO.setup(config.directionPinRight, GPIO.OUT)
GPIO.setup(config.pulsePin, GPIO.OUT)

# enable stepper
GPIO.output(config.enablePin, GPIO.HIGH)

# work out direction of rotation (+ve = clockwise)
if angle > 0:
    print("clockwise")
    GPIO.output(config.directionPinLeft, GPIO.LOW)
    GPIO.output(config.directionPinRight, GPIO.LOW)
else:
    print("anti cw")
    GPIO.output(config.directionPinLeft, GPIO.HIGH)
    GPIO.output(config.directionPinRight, GPIO.HIGH)

print(stepsNeeded)

# loop
for steps in range(stepsNeeded):
    GPIO.output(config.pulsePin, GPIO.HIGH)
    sleep(0.004)
    GPIO.output(config.pulsePin, GPIO.LOW)
    sleep(0.004)

# disable stepper
GPIO.output(config.enablePin, GPIO.LOW)
