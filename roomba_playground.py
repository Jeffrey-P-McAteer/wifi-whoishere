# Misc. Roomba API playground.
# sudo pip2 install git+https://github.com/NickWaterton/Roomba980-Python.git
# run the command 'roomba'
# Visit 

from roomba import Roomba
import json, time

#uncomment the option you want to run, and replace address, blid and roombaPassword with your own values

address = "192.168.86.41"
blid = "3144400081327670"
roombaPassword = ":1:1534556072:ETpEBibRsipXd4ce"

myroomba = Roomba(address, blid, roombaPassword)
#or myroomba = Roomba() #if you have a config file - will attempt discovery if you don't
myroomba.connect()

#print("Starting...")
#myroomba.send_command("start")

#time.sleep(20)
#print("Stopping...")

#myroomba.send_command("stop")
myroomba.send_command("dock")

#import json, time
#for i in range(5):
#    print json.dumps(myroomba.master_state, indent=2)
#    time.sleep(1)

myroomba.disconnect()
