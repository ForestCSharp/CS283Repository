import socket

#Represents our Peer-to-Peer Connection
#1: Responsible for sending mesages about our player to other players
#2: Responsible for receiving messages and updating their representation in our game
class P2PConnection:
    OP_ATTACK = "OP_ATK"
    OP_POSITION = "OP_POS"
    OP_DESTROYED = "OP_DES"

    def __init__(self):
        self.port = 5000
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.bind(("", port))
        print "waiting on port:", port
        #self.ConnectionLoop

    def ConnectionLoop():
        while 1:
            data, addr = s.recvfrom(1024)
            print data
