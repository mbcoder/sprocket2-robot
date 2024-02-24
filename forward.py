import sys
import RPi.GPIO as GPIO
from time import sleep
import config

steps1000mm = 1672 #3184

distance = int(sys.argv[1])
# print(distance)

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

# function to ramp the motors up and down
rampLength = 50
startDelay = 0.010
minDelay = 0.003
increment: float = (startDelay - minDelay) / (rampLength)
#currentDelay = 0.010
def calc_step_delay(step):
    # ramp up?
    if step < rampLength:
        currentDelay = startDelay - (step*increment)
    else:
        # ramp down?
        if step > (stepsNeeded - rampLength):
            stepsFromEnd = stepsNeeded - step
            currentDelay = startDelay - (stepsFromEnd*increment)
        else:
            # top speed
            currentDelay = minDelay
    return currentDelay

# loop
for steps in range(stepsNeeded):
    GPIO.output(config.pulsePin, GPIO.HIGH)
    sleep(calc_step_delay(steps))
    GPIO.output(config.pulsePin, GPIO.LOW)
    sleep(calc_step_delay(steps))

# disable stepper
GPIO.output(config.enablePin, GPIO.LOW)

# return status
print("Success")
