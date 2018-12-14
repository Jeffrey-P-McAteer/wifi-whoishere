# Misc. Roomba API playground.
# sudo pip2 install git+https://github.com/NickWaterton/Roomba980-Python.git
# run the command 'roomba'
# sudo pip2 install pyttsx gTTS

from roomba import Roomba
import json, time

# Speech outputs
import pyttsx

engine = pyttsx.init()

# voices = engine.getProperty('voices')
# for voice in voices:
#   engine.setProperty('voice', voice.id)  # changes the voice
#   print "voice.id = ", voice.id
#   engine.say('The quick brown fox jumped over the lazy dog.')
#   engine.runAndWait()
#   time.sleep(1)
# x = 5 / 0 # Testing stopper

def say(x):
  engine.say(x)
  engine.runAndWait()

address = "192.168.86.41"
blid = "3144400081327670"
roombaPassword = ":1:1534556072:ETpEBibRsipXd4ce"

myroomba = Roomba(address, blid, roombaPassword, continuous=True)
#or myroomba = Roomba() #if you have a config file - will attempt discovery if you don't
myroomba.connect()

say('I am starting the roomba.')
myroomba.send_command("start")

time.sleep(14)
say('I am docking the roomba.')
print("Docking...")

myroomba.send_command("stop")
time.sleep(2)
say('I am docking the roomba.')
myroomba.send_command("dock")
time.sleep(2)
say('I am docking the roomba.')

time.sleep(1)
say('I am observing the roomba.')

while True:
    time.sleep(2)
    string = json.dumps(myroomba.master_state, indent=2)
    print string
    
    print "myroomba.current_state=", myroomba.current_state
    
    # co_ords are always 0,0, 180 on model 895
    #co_ords = json.dumps(myroomba.co_ords, indent=2)
    #print co_ords
    
    print "=============================="
    
    if "Charging" in myroomba.current_state:
      say('The roomba has completed docking.')
      time.sleep(5)
      say('The roomba has completed docking.')
      break

myroomba.disconnect()


