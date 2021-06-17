
fun main(args: Array<String>) {
    val game=Branch("You are standing in front of a large castle, what will you do?"){
        WalkBack() leadsTo TerminalBranch("You walked back and went home, Game Over.")
        WalkInside() leadsTo Branch("""In front of you is the main hall, will you open the main door? It looks spooky. Or will you go to the left doorway""".trimMargin()){
            OpenAction() leadsTo TerminalBranch("A guard sees you and cuts you down, Game Over.")
            WalkBack() leadsTo TerminalBranch(" You left the castle and went home. Game Over.")
            val startOfLeftRoomBranch:RefBranch=RefBranch(Branch())
            val startOfLeftRoom=Branch("You're in a room with four doors."){
                WalkInside() leadsTo Branch("You are now in the main hall, it's to dark see anything."){
                    WalkBack() leadsTo startOfLeftRoomBranch
                    WalkRight() leadsTo TerminalBranch("You can only see darkness and lost the door you came in from and died by a guard. Game Over")
                    WalkForward() leadsTo Branch("You walked into the second hall, there are three more doors here."){
                        WalkForward() leadsTo TerminalBranch("You walked into the boss room unprepared and died, Game Over.")
                        WalkRight() leadsTo TerminalBranch("You walked into a guard and he cut you down, Game Over.")
                        WalkLeft() leadsTo Branch("You walked into the armory, there might be something you want here."){
                            PickupAction() leadsTo Branch("You see a sword and shield and picked them up,this will help keep you safe."){
                                WalkBack() leadsTo Branch("You walked into the second hall, there are two more doors here."){
                                    WalkRight() leadsTo Branch("There is a guard in the room, You overpowered him and walked back into the hall."){
                                        WalkForward() leadsTo Branch("You walked into the boss room, Will you fight or flee?"){
                                            FightAction() leadsTo TerminalBranch("You fought the boss and came out victorious, Congratulations!")
                                            FleeAction() leadsTo TerminalBranch("You got scared and ran away, Game Over.")
                                        }
                                    }
                                    WalkForward() leadsTo Branch("You walked into the boss room, Will you fight or flee?"){
                                        FightAction() leadsTo TerminalBranch("You fought the boss and came out victorious, Congratulations!")
                                        FleeAction() leadsTo TerminalBranch("You got scared and ran away, Game Over.")
                                    }
                                }
                            }
                        }
                    }
                }
                WalkLeft() leadsTo TerminalBranch("U walked back into the room that was in the main room u died.")
                WalkRight() leadsTo Branch("You stumbled into the weapon room, look around you might find something."){
                    OpenAction() leadsTo TerminalBranch("You picked up a sword, unfortunately it was poisoned..")
                    WalkBack() leadsTo startOfLeftRoomBranch
                }
            }
            startOfLeftRoomBranch.ref=startOfLeftRoom

            WalkLeft() leadsTo startOfLeftRoomBranch
        }
    }
    game.process()
}

interface Action{
    val label:String
    fun accept(input :String):Boolean
}

abstract class SimpleAction(override val label: String):Action{
    override fun accept(input: String) = label == input
}

class OpenAction:SimpleAction("OPEN")
class CloseAction:SimpleAction("CLOSE")
class HitAction:SimpleAction("HIT")
class PickupAction:SimpleAction("PICKUP")
class FightAction:SimpleAction("FIGHT")
class FleeAction:SimpleAction("FLEE")

abstract class WalkAction(val direction:String):Action{
    override val label: String = "WALK $direction"
    override fun accept(input: String): Boolean {
        val (iaction,idirection)=input.replace(Regex.fromLiteral(" {2,}")," ").split(" ")
        return iaction=="WALK" && direction==idirection
    }
}

class WalkInside:WalkAction("INSIDE")
class WalkRight:WalkAction("RIGHT")
class WalkBack:WalkAction("BACKWARDS")
class WalkLeft:WalkAction("LEFT")
class WalkForward:WalkAction("FORWARD")

interface IBranch {
    val description: String
    val fn: IBranch.() -> Unit
    fun process()

    infix fun Action.leadsTo(brc: IBranch)
}


open class Branch(override val description:String="", override val fn:IBranch.()->Unit={}) : IBranch {
    val actions:MutableList<Pair<Action,IBranch>> = mutableListOf()
    var visited=false
    override fun process()  {
        if (!visited) this.fn()
        visited=true

        println(description)
        print("The following are available:  \n- ${actions.map { it.first.label }.joinToString("\n- ")}\n> ")
        var input= readLine() ?: ""
        var processed  = actions.find { it.first.accept(input) }?.second?.run { process(); true }?:false
        while (!processed){
            input= readLine() ?: ""
            processed = actions.find { it.first.accept(input) }?.second?.run { process(); true }?:false
        }
    }
    override infix fun Action.leadsTo(brc:IBranch){
        actions.add(this to brc)
    }
}

class RefBranch(var ref:Branch=Branch()):IBranch {
    override val fn: IBranch.() -> Unit
        get() = ref.fn
    override val description: String
        get() = ref.description

    override fun process() {
        ref.process()
    }

    override fun Action.leadsTo(brc: IBranch) {
        ref.actions.add(this to brc)
    }

}

class TerminalBranch(val ending:String):Branch(){
    override fun process(){
        println(ending)
    }
}