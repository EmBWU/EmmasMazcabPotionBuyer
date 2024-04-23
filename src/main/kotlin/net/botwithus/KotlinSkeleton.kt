package net.botwithus

import net.author.Shop
import net.botwithus.api.game.hud.inventories.Backpack
import net.botwithus.api.game.hud.inventories.Bank
import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.Coordinate
import net.botwithus.rs3.game.hud.interfaces.Interfaces
import net.botwithus.rs3.game.minimenu.MiniMenu
import net.botwithus.rs3.game.minimenu.actions.ComponentAction
import net.botwithus.rs3.game.movement.Movement
import net.botwithus.rs3.game.movement.NavPath
import net.botwithus.rs3.game.movement.TraverseEvent
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import java.util.*
import java.util.regex.Pattern

class KotlinSkeleton(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript(name, scriptConfig, scriptDefinition) {

    private val random: Random = Random()
    var botState: BotState = BotState.IDLE
    val clickedPotions: MutableList<MazcabPotions> = mutableListOf()
    val shopPosition = Coordinate(4376, 787, 0)
    var timeRemaining: Long = 0

    enum class BotState {
        //define your bot states here
        IDLE,
        THINKING,
        BANKING,
        BUYING,
        MOVING,
    }

    override fun initialize(): Boolean {
        super.initialize()
        this.sgc = KotlinSkeletonGraphicsContext(this, console)
        return true
    }

    override fun onLoop() {
        val player = Client.getLocalPlayer()
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(2500, 5500))
            return
        }
        when (botState) {
            BotState.THINKING -> {
                Execution.delay(handleThinking(player))
                return
            }

            BotState.BANKING -> {
                Execution.delay(handleBanking(player))
                return
            }

            BotState.IDLE -> {
                println("We're idle!")
                Execution.delay(random.nextLong(1500, 5000))
            }

            BotState.MOVING -> {
                Execution.delay(handleMoving(player))
                return
            }

            BotState.BUYING -> {
                Execution.delay(handleBuying(player))
                return
            }

            else -> {
                println("Unexpected bot state, report to author!")
            }
        }
        return
    }

    private fun handleMoving(player: LocalPlayer): Long {
        if (player.coordinate.regionId == 17420) {
            botState = BotState.THINKING
            return random.nextLong(1500, 3000)
        } else {
            val coordinate = NavPath.resolve(shopPosition)
            val result = Movement.traverse(coordinate)
            if (result == TraverseEvent.State.NO_PATH) {
                println("No path to shop")
            } else if (result == TraverseEvent.State.FINISHED) {
                println("Arrived at shop")
                botState = BotState.THINKING
            }
        }
        return random.nextLong(1500, 3000)
    }


    private fun handleBanking(player: LocalPlayer): Long {
        if (Bank.isOpen()) {
            Bank.loadPreset(1)
            botState = BotState.THINKING
            return random.nextLong(1500, 3000)
        } else {
            if (getMerchant() != null)
                getMerchant()?.interact("Bank")
            else
                println("No Bank found")
        }
        return random.nextLong(1500, 3000)
    }

    private fun getMerchant(): Npc? {
        return NpcQuery.newQuery().name("Goebie supplier").results().firstOrNull()
    }

    private fun handleBuying(player: LocalPlayer): Long {
        if (!Interfaces.isOpen(1265)) {
            val shop = getMerchant()
            if (shop != null) {
                shop.interact("Shop")
                Execution.delay(random.nextLong(1000, 3000))
            } else {
                println("No shop found")
            }
        } else {
            clickedPotions.forEach() {
                val pattern = Pattern.compile(Pattern.quote(it.name))
                val predicate = Shop.nameMatcher(pattern)
                if (Shop.contains(predicate) && Shop.getAmount(predicate) > 0) {
                    Execution.delay(500)
                    println("Buying ${it.name}")
                    Shop.buyAll(predicate)
                }
            }
            if (true) {
                println("Closing Shop and setting Time")
                MiniMenu.interact(ComponentAction.COMPONENT.type, 1, -1, 82903257)
                botState = BotState.THINKING
                timeRemaining = System.currentTimeMillis() + 2 * 60 * 1000
                return random.nextLong(1500, 3000)
            }
        }

        return random.nextLong(1500, 3000)
    }

    private fun handleThinking(player: LocalPlayer): Long {
        if (player.coordinate.regionId != 17420) {
            botState = BotState.MOVING
            return random.nextLong(1500, 3000)
        }
        if (Backpack.isFull() || timeRemaining > System.currentTimeMillis() && !Backpack.isEmpty()) {
            botState = BotState.BANKING
            return random.nextLong(1500, 3000)
        }
        if (!Backpack.isFull() && getMerchant() != null && timeRemaining < System.currentTimeMillis()) {
            botState = BotState.BUYING
            return random.nextLong(1500, 3000)
        }
        return random.nextLong(1500, 3000)
    }
}