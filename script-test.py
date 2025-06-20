import sys
import time

if len(sys.argv) != 2:
    print("Usage: python script.py <your_input>")
    sys.exit(1)

time.sleep(1)

print(sys.argv[1])