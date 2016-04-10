from direct.showbase.ShowBase import ShowBase
from P2PConnection import P2PConnection
from MyPlayer import MyPlayer

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
        self.character = MyPlayer()
        self.character.AddToScene(self)



#connection = P2PConnection()
app = MyApp()
app.useDrive()
app.run()
