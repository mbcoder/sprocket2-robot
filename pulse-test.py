import sys
import RPi.GPIO as GPIO
from time import sleep
import config

pulses = int(sys.argv[1])

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

# loop
for steps in range(pulses):
    GPIO.output(config.pulsePin, GPIO.HIGH)
    sleep(0.004)
    GPIO.output(config.pulsePin, GPIO.LOW)
    sleep(0.004)

# disable stepper
GPIO.output(config.enablePin, GPIO.LOW)
