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
        for (p in Bukkit.getServer().onlinePlayers) {
            val main = p.inventory.itemInMainHand
            if (PortalGun.isPortalGun(main)) {
                PortalCandidate.checkFor(p)
            }
        }
        if (ticks % 5 == 0) {
            pair.drawParticles()
            pair.resetFallingTimer()
        }
        pair.checkTeleportation()
        pair.checkSucktioning()
        pair.predictTeleportation()
    }
}