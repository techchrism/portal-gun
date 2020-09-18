package me.techchrism.portalgun

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class PreviewTask : BukkitRunnable() {
    var pair = PortalPair()

    private var ticks = 0
    override fun run() {
        ticks++
        if (ticks > 100) {
            ticks = 0
        }
        // Preview portal location
        for (p in Bukkit.getServer().onlinePlayers) {
            val main = p.inventory.itemInMainHand
            if (PortalGun.isPortalGun(main)) {
                PortalCandidate.checkFor(p)
            }
        }
        // Draw particles 4 and reset sand times a second
        if (ticks % 5 == 0) {
            pair.drawParticles()
            pair.resetFallingTimer()
        }
        // Perform teleportation, prediction, and suctioning checks every tick
        pair.checkTeleportation()
        pair.checkSuctioning()
        pair.predictTeleportation()
    }
}