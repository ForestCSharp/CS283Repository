
from direct.actor.Actor import Actor
from panda3d.core import *
import sys
from Timer import Timer

class MyPlayer:

    def __init__(self, Connection):
        self.SetModel("")

        self.keyMap = {
            "left": 0, "right": 0, "forward": 0, "back": 0, "space": 0}

        self.render = 0
        self.connection = Connection
        self.connection.RegisterLocalPlayer(self)
        self.MyDamage = 20.0
        self.MyHealth = 100.0
        self.AttackDistance = 10.0
        self.AttackCooldown = 0.5
        self.AttackCooldownTimer = Timer()

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
        app.accept("space", self.setKey, ["space", True])
        app.accept("space-up", self.setKey, ["space", False])

        app.taskMgr.add(self.HandleGameplay, "gameplayHandler")


    def setKey(self, key, value):
        self.keyMap[key] = value

    def HandleGameplay(self, task):
        bHasMoved = False
        bAttack = False

        if self.keyMap["left"]:
            self.Actor.setH(self.Actor.getH() + 3.0)
            bHasMoved = True
        if self.keyMap["right"]:
            self.Actor.setH(self.Actor.getH() - 3.0)
            bHasMoved = True
        if self.keyMap["forward"]:
            self.Actor.setPos(self.Actor.getPos() - self.render.getRelativeVector(self.Actor,Vec3(0,1,0)) * 50)
            bHasMoved = True
        if self.keyMap["back"]:
            self.Actor.setPos(self.Actor.getPos() + self.render.getRelativeVector(self.Actor,Vec3(0,1,0)) * 50)
            bHasMoved = True
        if self.keyMap["space"]:
            bAttack = True

        if bHasMoved:
            pos = self.Actor.getPos()
            rot = self.Actor.getHpr()
            self.connection.SendMessage(self.connection.OP_POSITION
            + " " + str(pos.getX()) + " " + str(pos.getY()) + " " + str(pos.getZ())
            + " " + str(rot.getX()) + " " + str(rot.getY()) + " " + str(rot.getZ()))

        #Only Send Attack if close enough and not in attack cooldown (based on timer)
        if bAttack and self.AttackCooldownTimer.GetElapsed() > self.AttackCooldown:
            print "Attack"
            self.AttackCooldownTimer.reset()
            distanceToOpponent = self.connection.GetDistanceToOpponent()
            if distanceToOpponent < self.AttackDistance:
                MSG = self.connection.OP_ATTACK + " " + str(self.MyDamage)
                self.connection.SendMessage(MSG)


        return task.cont

    def TakeDamage(self, Dmg):
        self.MyHealth -= Dmg
        if self.MyHealth <= 0.0:
            print "You Lose!"
            MSG = self.connection.OP_DESTROYED
            self.connection.SendMessage(MSG)
            self.Actor.hide()
