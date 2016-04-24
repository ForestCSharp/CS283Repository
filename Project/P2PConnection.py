import socket
import thread
import random
from panda3d.core import *

#Represents our Peer-to-Peer Connection and local game state
#1: Responsible for sending mesages about our player to other players
#2: Responsible for receiving messages and updating their representation in our game
class P2PConnection:
    OP_ATTACK = "OP_ATK" #FORMAT: "OP_ATK DMG PlayerID"
    OP_POSITION = "OP_POS" #FORMAT: "OP_POS X Y Z H P R PlayerID"
    OP_DESTROYED = "OP_DES" #FORMAT: "OP_DES PlayerID"

    def __init__(self):
        self.myIP = '127.0.0.1'
        self.myPort = 5005
        self.dstIP = '127.0.0.1'
        self.dstPort = 5005
        self.PlayerID = random.randrange(0,10000000)
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        self.s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

        self.s.bind((self.dstIP, self.dstPort)) #bind to opponent
        thread.start_new_thread(self.ConnectionLoop, ())
        #self.ConnectionLoop()

    #establishes connection to IP and Port and begins connection loop
    def SetConnection(IP, Port):
        print "Establishing Connection"
        self.dstIP = IP
        self.dstPort = Port

    def RegisterLocalPlayer(self, LocalPlayer):
        self.LocalPlayer = LocalPlayer

    def RegisterOpponent(self, Opponent):
        self.Opponent = Opponent

    #Receives opponent Packets
    def ConnectionLoop(self):
        while True:
            data, addr = self.s.recvfrom(1024)
            tokens = data.split(" ")

            op = tokens.pop(0)
            if op == self.OP_POSITION:
                print "OP_POS Received"
                x = float(tokens[0])
                y = float(tokens[1])
                z = float(tokens[2])
                h = float(tokens[3])
                p = float(tokens[4])
                r = float(tokens[5])
                ID = int(tokens[6])
                if ID != self.PlayerID:
                    self.Opponent.SetPosition(x,y,z,h,p,r)
            elif op == self.OP_ATTACK:
                print "OP_ATK Received"
                ID = int(tokens[1])
                if ID != self.PlayerID:
                    self.LocalPlayer.TakeDamage(float(tokens[0]))
            elif op == self.OP_DESTROYED:
                print "OP_DES Received"
                ID = int(tokens[0])
                if ID != self.PlayerID:
                    print "YOU WIN!!!"
                    self.Opponent.Actor.Hide()



            print data

    #Sends Packets about our own state
    def SendMessage(self,MSG):
        print "Sending " + MSG
        MSG += (" " + str(self.PlayerID))
        self.s.sendto(MSG, (self.dstIP, self.dstPort))

    def GetDistanceToOpponent(self):
        Dist = (self.Opponent.Actor.getPos() - self.LocalPlayer.Actor.getPos()).length()
        return Dist
