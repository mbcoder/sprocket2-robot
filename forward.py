import sys
import RPi.GPIO as GPIO
from time import sleep
import config

steps1000mm = 879 #3184

distance = int(sys.argv[1])
print(distance)

stepsNeeded = abs(int((steps1000mm / 1000) * distance))

# setup gpio
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(config.enablePin, GPIO.OUT)
GPIO.setup(config.directionPinLeft, GPIO.OUT)
GPIO.setup(config.directionPinRight, GPIO.OUT)
GPIO.setup(config.pulsePin, GPIO.OUT)

# enable stepper
GPIO.output(config.enablePin, GPIO.HIGH)

# set direction
GPIO.output(config.directionPinLeft, GPIO.HIGH)
GPIO.output(config.directionPinRight, GPIO.LOW)

print(stepsNeeded)

# loop
for steps in range(stepsNeeded):
    GPIO.output(config.pulsePin, GPIO.HIGH)
    sleep(0.010)
    GPIO.output(config.pulsePin, GPIO.LOW)
    sleep(0.010)

# disable stepper
GPIO.output(config.enablePin, GPIO.LOW)
