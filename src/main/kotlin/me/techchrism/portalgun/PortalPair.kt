package me.techchrism.portalgun

import org.bukkit.Color
import org.bukkit.entity.FallingBlock
import java.util.*

class PortalPair {
    private var first: Portal? = null
    private var second: Portal? = null

    fun fireFirst(candidate: PortalCandidate) {
        if (first != null) {
            first!!.destroy()
        }
        first = Portal(candidate)
        first!!.color = Color.BLUE
    }

    fun fireSecond(candidate: PortalCandidate) {
        if (second != null) {
            second!!.destroy()
        }
        second = Portal(candidate)
        second!!.color = Color.ORANGE
    }

    fun clear() {
        if (first != null) {
            first!!.destroy()
            first = null
        }
        if (second != null) {
            second!!.destroy()
            second = null
        }
    }

    fun resetFallingTimer() {
        if (first != null) {
            if (first!!.topEntity != null) {
                first!!.topEntity!!.ticksLived = 1
            }
            if (first!!.bottomEntity != null) {
                first!!.bottomEntity!!.ticksLived = 1
            }
        }
        if (second != null) {
            if (second!!.topEntity != null) {
                second!!.topEntity!!.ticksLived = 1
            }
            if (second!!.bottomEntity != null) {
                second!!.bottomEntity!!.ticksLived = 1
            }
        }
    }

    fun drawParticles() {
        if (first != null) {
            first!!.drawParticles()
        }
        if (second != null) {
            second!!.drawParticles()
        }
    }

    fun checkTeleportation() {
        if (first == null || second == null) {
            return
        }
        if (first!!.bottomEntity != null) {
            checkEntity(first!!.bottomEntity!!, second!!, first!!, false)
        }
        if (first!!.topEntity != null) {
            checkEntity(first!!.topEntity!!, second!!, first!!, true)
        }
        if (second!!.bottomEntity != null) {
            checkEntity(second!!.bottomEntity!!, first!!, second!!, false)
        }
        if (second!!.topEntity != null) {
            checkEntity(second!!.topEntity!!, first!!, second!!, true)
        }
    }

    fun checkSucktioning() {
        if (first != null) {
            first!!.checkSucktioning()
        }
        if (second != null) {
            second!!.checkSucktioning()
        }
    }

    fun predictTeleportation() {
        if (first != null && second != null) {
            first!!.predictTeleportation(second!!)
            second!!.predictTeleportation(first!!)
        }
    }

    private fun checkEntity(falling: FallingBlock, to: Portal, from: Portal, top: Boolean) {
        for (e in falling.getNearbyEntities(0.0, 0.0, 0.0)) {
            val id = e.uniqueId
            if (first!!.topID === id) {
                continue
            }
            if (first!!.bottomID === id) {
                continue
            }
            if (second!!.topID === id) {
                continue
            }
            if (second!!.bottomID === id) {
                continue
            }
            to.teleportHere(e, from.facing, top)
        }
    }
}