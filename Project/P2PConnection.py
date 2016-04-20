import socket

#Represents our Peer-to-Peer Connection
#1: Responsible for sending mesages about our player to other players
#2: Responsible for receiving messages and updating their representation in our game
class P2PConnection:
    OP_ATTACK = "OP_ATK" #FORMAT: "OP_ATK DMG"
    OP_POSITION = "OP_POS" #FORMAT: "OP_POS X Y Z H P R"
    OP_DESTROYED = "OP_DES" #FORMAT: "OP_DES"

    def __init__(self):
        self.myIP = '127.0.0.1'
        self.myPort = 5005
        self.dstIP = '127.0.0.1'
        self.dstPort = 5005
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        #self.s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        #self.s.bind((self.dstIP, self.dstPort)) #bind to opponent
        #self.ConnectionLoop()

    #establishes connection to IP and Port and begins connection loop
    def SetConnection(IP, Port):
        print "Establishing Connection"



    #Receives opponent Packets
    def ConnectionLoop(self):
        while True:
            data, addr = self.s.recvfrom(1024)
            print data

    #Sends Packets about our own state
    def SendMessage(self,MSG):
        print "Sending " + MSG
        self.s.sendto(MSG, (self.dstIP, self.dstPort))
