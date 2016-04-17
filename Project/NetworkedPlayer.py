from direct.actor.Actor import Actor
from panda3d.core import PandaNode, NodePath, Camera, TextNode

#A player who communicates across the network
class NetworkedPlayer:

    def __init__(self):
        self.SetModel("")

    def SetModel(self, path):
        self.Actor = Actor("panda-model", {"walk": "panda-walk4"})
        self.Actor.setScale(0.005, 0.005, 0.005)

    #adds our character to scene
    def AddToScene(self, app):
        self.Actor.reparentTo(app.render)
        # Loop its animation.
        self.Actor.loop("walk")
