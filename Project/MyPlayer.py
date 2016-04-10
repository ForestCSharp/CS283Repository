
from direct.actor.Actor import Actor

class MyPlayer:

    def __init__(self):
        self.Actor = Actor("panda-model", {"walk": "panda-walk4"})
        self.Actor.setScale(0.005, 0.005, 0.005)


    def AddToScene(self, app):
        self.Actor.reparentTo(app.render)
        # Loop its animation.
        self.Actor.loop("walk")
