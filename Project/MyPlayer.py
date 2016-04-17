
from direct.actor.Actor import Actor
from panda3d.core import *
import sys

class MyPlayer:

    def __init__(self):
        self.SetModel("")

        self.keyMap = {
            "left": 0, "right": 0, "forward": 0, "back": 0}

        self.render = 0
        self.bHasMoved = False

    #TODO: sets this actor the model set by path
    def SetModel(self, path):
        self.Actor = Actor("panda-model", {"walk": "panda-walk4"})
        self.Actor.setScale(0.005, 0.005, 0.005)

        self.CamLoc = NodePath(PandaNode("floater"))
        self.CamLoc.reparentTo(self.Actor)
        self.CamLoc.setZ(1000.0)
        self.CamLoc.setY(1000.0)
        self.CamLoc.setH(180)
        self.CamLoc.setP(-15)

    #adds our character to scene and sets up input
    def AddToScene(self, app):

        self.render = app.render
        self.Actor.reparentTo(app.render)
        # Loop its animation.
        self.Actor.loop("walk")

        app.accept("escape", sys.exit)
        app.accept("a", self.setKey, ["left", True])
        app.accept("d", self.setKey, ["right", True])
        app.accept("w", self.setKey, ["forward", True])
        app.accept("s", self.setKey, ["back", True])
        app.accept("a-up", self.setKey, ["left", False])
        app.accept("d-up", self.setKey, ["right", False])
        app.accept("w-up", self.setKey, ["forward", False])
        app.accept("s-up", self.setKey, ["back", False])

        app.taskMgr.add(self.move, "moveTask")


    def setKey(self, key, value):
        self.keyMap[key] = value

    def move(self, task):
        self.bHasMoved = False

        if self.keyMap["left"]:
            self.Actor.setH(self.Actor.getH() + 3.0)
            self.bHasMoved = True
        if self.keyMap["right"]:
            self.Actor.setH(self.Actor.getH() - 3.0)
            self.bHasMoved = True
        if self.keyMap["forward"]:
            self.Actor.setPos(self.Actor.getPos() - self.render.getRelativeVector(self.Actor,Vec3(0,1,0)) * 50)
            self.bHasMoved = True
        if self.keyMap["back"]:
            self.Actor.setPos(self.Actor.getPos() + self.render.getRelativeVector(self.Actor,Vec3(0,1,0)) * 50)
            self.bHasMoved = True

        #if self.bHasMoved:
            #TODO: Send Move Update Packet


        return task.cont
