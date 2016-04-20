from direct.showbase.ShowBase import ShowBase
from P2PConnection import P2PConnection
from MyPlayer import MyPlayer
from NetworkedPlayer import NetworkedPlayer
from panda3d.core import *
from direct.task import Task
import sys

class MyApp(ShowBase):

    def __init__(self):
        ShowBase.__init__(self)
        # Load the environment model.
        self.scene = self.loader.loadModel("environment")
        # Reparent the model to render.
        self.scene.reparentTo(self.render)
        # Apply scale and position transforms on the model.
        self.scene.setScale(0.25, 0.25, 0.25)
        self.scene.setPos(-8, 42, 0)

        #Our Character (Sends updates through packets)
        self.character = MyPlayer()
        self.character.AddToScene(self)

        #Our Opponent (Receives updates via packets)
        self.opponent = NetworkedPlayer()
        self.opponent.AddToScene(self)

        self.camera.reparentTo(self.character.CamLoc)
        self.camera.lookAt(self.character.Actor)
        self.disableMouse()










#connection = P2PConnection()
app = MyApp()
app.useDrive()
app.run()