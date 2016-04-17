import socket

#Represents our Peer-to-Peer Connection
#1: Responsible for sending mesages about our player to other players
#2: Responsible for receiving messages and updating their representation in our game
class P2PConnection:
    OP_ATTACK = "OP_ATK" #FORMAT: "OP_ATK DMG"
    OP_POSITION = "OP_POS" #FORMAT: "OP_POS X Y Z H P R"
    OP_DESTROYED = "OP_DES" #FORMAT: "OP_DES"

    def __init__(self):
        self.IPs = ['127.0.0.1']
        self.Ports = [5000]
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.bind(("", port))
        print "waiting on port:", port
        #self.ConnectionLoop

    #establishes connection to IP and Port and begins connection loop
    def EstablishConnection(IP, Port):
        self.IPs.append(IP)
        self.IPs.append(Port)

    #clears connections
    def ClearConnections():
        self.IPs = []
        self.Ports = []


    def ConnectionLoop():
        while 1:
            data, addr = s.recvfrom(1024)
            print data
