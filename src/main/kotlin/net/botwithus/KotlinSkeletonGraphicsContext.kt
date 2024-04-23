package net.botwithus

import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext

class KotlinSkeletonGraphicsContext(
    private val script: KotlinSkeleton,
    console: ScriptConsole
) : ScriptGraphicsContext(console) {

    override fun drawSettings() {
        super.drawSettings()
        ImGui.Begin("Emmas Mazcab Potion Buyer", 0)
        ImGui.SetWindowSize(500f, 260f)
        if (ImGui.Button("Start")) {
            if (script.botState == KotlinSkeleton.BotState.IDLE) {
                script.botState = KotlinSkeleton.BotState.THINKING
            } else {
                script.botState = KotlinSkeleton.BotState.IDLE
                script.timeRemaining = 0
            }
        }
        ImGui.Text("My scripts state is: " + script.botState)
        val remainingTime = kotlin.math.max(0L, script.timeRemaining - System.currentTimeMillis())
        val minutes = (remainingTime / 60000).toInt()
        val seconds = ((remainingTime % 60000) / 1000).toInt()
        ImGui.Text("Time remaining: ${"%02d:%02d".format(minutes, seconds)}")
        ImGui.BeginChild("Child1", 480f, -60f, true, 1)
        var count = 0
        for (potions: MazcabPotions in Potions) {
            if (potions in script.clickedPotions) {
                ImGui.PushStyleColor(21, 0f, 60f, 0f, 0.8f)
                if (ImGui.Button(potions.name)) {
                    if (potions in script.clickedPotions) {
                        script.clickedPotions.remove(potions)
                    } else {
                        script.clickedPotions.add(potions)
                    }
                }
                ImGui.PopStyleColor()
            } else {
                if (ImGui.Button(potions.name)) {
                    if (potions in script.clickedPotions) {
                        script.clickedPotions.remove(potions)
                    } else {
                        script.clickedPotions.add(potions)
                    }
                }
            }
            count++
            if (count % 3 != 0) {
                ImGui.SameLine()
            }
        }

        ImGui.End()
    }


    override fun drawOverlay() {
        super.drawOverlay()
    }

}