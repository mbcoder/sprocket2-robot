import RPi.GPIO as GPIO
from time import sleep

# Pin Definitons:
enablePin = 2
directionPinLeft = 3
directionPinRight = 17
pulsePin = 4

# setup gpio
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(enablePin, GPIO.OUT)
GPIO.setup(directionPinLeft, GPIO.OUT)
GPIO.setup(directionPinRight, GPIO.OUT)
GPIO.setup(pulsePin, GPIO.OUT)

# enable stepper
GPIO.output(enablePin, GPIO.HIGH)

# set direction
GPIO.output(directionPinLeft, GPIO.LOW)
GPIO.output(directionPinRight, GPIO.LOW)

# loop
for steps in range(800):
    GPIO.output(pulsePin, GPIO.HIGH)
    sleep(0.004)
    GPIO.output(pulsePin, GPIO.LOW)
    sleep(0.004)

# disable stepper
GPIO.output(enablePin, GPIO.LOW)
