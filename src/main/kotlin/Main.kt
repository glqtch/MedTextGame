
fun main(args: Array<String>) {
    val game=Branch("U are in front of the castle what will u do"){
        WalkBack() leadsTo TerminalBranch("You walked back and stopped with it ")
        WalkInside() leadsTo Branch("""In front of u is the main hall, will u open the main door
It looks kinda sus. Or will u go to the left doorway"""){
            OpenAction() leadsTo TerminalBranch("a guard pops out and kills u")
            WalkBack() leadsTo TerminalBranch(" you ended ur story at the first room u do be kinda scared tho.")
            val startOfLeftRoomBranch:RefBranch=RefBranch(Branch())
            val startOfLeftRoom=Branch("U are in a big room with 4 doors"){
                WalkInside() leadsTo Branch("U are now in the main hall and u can't see anything"){
                    WalkBack() leadsTo startOfLeftRoomBranch
                    WalkRight() leadsTo Branch("U are now in fully in the dark and u lost the door where u came in from")
                }
                WalkLeft() leadsTo TerminalBranch("U walked back into the room that was in the main room u died.")
                WalkRight() leadsTo Branch("U walked into the weapon room, there might be something in here for u"){
                    OpenAction() leadsTo TerminalBranch("U open the weapon box, bad choose it was poisoned")
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