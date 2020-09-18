package me.techchrism.portalgun

import org.bukkit.Color
import org.bukkit.entity.FallingBlock

class PortalPair {
    private var first: Portal? = null
    private var second: Portal? = null

    /**
     * Fires the first portal
     * @param candidate PortalCandidate the valid candidate location to fire to
     */
    fun fireFirst(candidate: PortalCandidate) {
        if (first != null) {
            first!!.destroy()
        }
        first = Portal(candidate)
        first!!.color = Color.BLUE
    }

    /**
     * Fires the second portal
     * @param candidate PortalCandidate the valid candidate location to fire to
     */
    fun fireSecond(candidate: PortalCandidate) {
        if (second != null) {
            second!!.destroy()
        }
        second = Portal(candidate)
        second!!.color = Color.ORANGE
    }

    /**
     * Removes both portals
     */
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

    /**
     * Ensures the falling sand doesn't drop as an item
     */
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

    /**
     * Draws particles for the portals
     */
    fun drawParticles() {
        if (first != null) {
            first!!.drawParticles()
        }
        if (second != null) {
            second!!.drawParticles()
        }
    }

    /**
     * Runs teleportation checks if both portals are formed
     */
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

    /**
     * Checks suctioning for both portals
     */
    fun checkSuctioning() {
        if (first != null) {
            first!!.checkSuctioning()
        }
        if (second != null) {
            second!!.checkSuctioning()
        }
    }

    /**
     * Predicts teleportation if both portals are formed
     */
    fun predictTeleportation() {
        if (first != null && second != null) {
            first!!.predictTeleportation(second!!)
            second!!.predictTeleportation(first!!)
        }
    }

    /**
     * Checks for and teleports nearby entities
     * @param falling FallingBlock the falling sand used as a bounding box to find nearby entities
     * @param to Portal the portal being teleported to
     * @param from Portal the portal being teleported from
     * @param top Boolean whether the falling sand is from the top or bottom of the portal
     */
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