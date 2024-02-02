import sys
from time import sleep

distance = int(sys.argv[1])

# sleep to simulate robot delay
sleep(3)

if distance > 1000:
    print("Fail: 900")
else:
    print("Success")