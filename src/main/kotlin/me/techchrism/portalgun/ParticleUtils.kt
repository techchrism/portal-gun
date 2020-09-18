package me.techchrism.portalgun

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import kotlin.math.cos
import kotlin.math.sin

class ParticleUtils {
    companion object {
        /**
         * Get a relative 3d coordinate from a portal candidate using the exit surface as a plane
         * @param candidate PortalCandidate the portal candidate to use as a surface
         * @param relativeX Double the x coordinate on the plane
         * @param relativeY Double the y coordinate on the plane
         * @return Location the calculated 3d location
         */
        private fun getRelativePosition(candidate: PortalCandidate, relativeX: Double, relativeY: Double): Location {
            if(candidate.top == null || candidate.bottom == null) {
                throw IllegalArgumentException()
            }
            val loc: Location = candidate.bottom!!.location.clone()
            if (candidate.face === BlockFace.UP || candidate.face === BlockFace.DOWN) {
                val facing: BlockFace = candidate.bottom!!.getFace(candidate.top!!)!!
                if (facing == BlockFace.NORTH) {
                    loc.add(relativeX, if (candidate.face === BlockFace.UP) 1.1 else -0.1, relativeY - 1.0)
                } else if (facing == BlockFace.SOUTH) {
                    loc.add(relativeX, if (candidate.face === BlockFace.UP) 1.1 else -0.1, relativeY)
                } else if (facing == BlockFace.WEST) {
                    loc.add(relativeY - 1.0, if (candidate.face === BlockFace.UP) 1.1 else -0.1, relativeX)
                } else if (facing == BlockFace.EAST) {
                    loc.add(relativeY, if (candidate.face === BlockFace.UP) 1.1 else -0.1, relativeX)
                }
            } else if (candidate.face === BlockFace.WEST || candidate.face === BlockFace.EAST) {
                loc.add(if (candidate.face === BlockFace.EAST) 1.1 else -0.1, relativeY, relativeX)
            } else {
                loc.add(relativeX, relativeY, if (candidate.face === BlockFace.SOUTH) 1.1 else -0.1)
            }
            return loc
        }

        /**
         * Draws a portal for the player
         * @param p Player the player to draw a portal for
         * @param candidate PortalCandidate the candidate to use as a portal drawing location
         */
        fun drawX(p: Player, candidate: PortalCandidate) {
            var i = 0.1
            while (i < 0.9) {
                p.spawnParticle(
                    Particle.REDSTONE,
                    getRelativePosition(candidate, i, i),
                    0, DustOptions(Color.RED, 0.45f)
                )
                p.spawnParticle(
                    Particle.REDSTONE,
                    getRelativePosition(candidate, i, 1 - i),
                    0, DustOptions(Color.RED, 0.45f)
                )
                i += 0.1
            }
        }

        /**
         * Draws an ellipse for the provided player
         * @param p Player the player to display particles for
         * @param candidate PortalCandidate the candidate to use as a location base
         * @param width Double the width of the ellipse
         * @param height Double the height of the ellipse
         * @param color Color the color of the particles
         */
        fun drawEllipse(p: Player, candidate: PortalCandidate, width: Double, height: Double, color: Color) {
            var i = 0.0
            while (i < 2 * Math.PI) {
                val relativeX = width * sin(i) + width
                val relativeY = height * cos(i) + height
                p.spawnParticle(
                    Particle.REDSTONE,
                    getRelativePosition(candidate, relativeX, relativeY),
                    0, DustOptions(color, 0.65f)
                )
                i += 0.25
            }
        }

        /**
         * Draws an ellipse in the world
         * @param candidate PortalCandidate the candidate to use as a location
         * @param width Double the width of the ellipse
         * @param height Double the height of the ellipse
         * @param color Color the color of the particles
         */
        fun drawEllipse(candidate: PortalCandidate, width: Double, height: Double, color: Color) {
            var i = 0.0
            while (i < 2 * Math.PI) {
                val relativeX = width * sin(i) + width
                val relativeY = height * cos(i) + height
                candidate.top!!.world.spawnParticle(
                    Particle.REDSTONE,
                    getRelativePosition(candidate, relativeX, relativeY),
                    0, DustOptions(color, 0.65f)
                )
                i += 0.25
            }
        }

        /**
         * Draws a line between two locations
         * @param start Location the starting location
         * @param finish Location the finishing location
         * @param space Double the space between particles on the line
         * @param color Color the particle color
         */
        private fun drawLine(start: Location, finish: Location, space: Double, color: Color) {
            val world = start.world
            val distance = start.distance(finish)
            val p1 = start.toVector()
            val p2 = finish.toVector()
            val vector = p2.clone().subtract(p1).normalize().multiply(space)
            var length = 0.0
            while (length < distance) {
                world!!.spawnParticle(Particle.REDSTONE, p1.x, p1.y, p1.z, 0, DustOptions(color, 1f))
                length += space
                p1.add(vector)
            }
        }

        /**
         * Outlines a bounding box with particles
         * @param box BoundingBox the bounding box to outline
         * @param w World the world to draw the particles in
         * @param color Color the color of the particles
         */
        fun outlineBoundingBox(box: BoundingBox, w: World, color: Color) {
            val s = 0.8
            drawLine(Location(w, box.maxX, box.maxY, box.maxZ), Location(w, box.minX, box.maxY, box.maxZ), s, color)
            drawLine(Location(w, box.maxX, box.minY, box.maxZ), Location(w, box.minX, box.minY, box.maxZ), s, color)
            drawLine(Location(w, box.maxX, box.maxY, box.maxZ), Location(w, box.maxX, box.minY, box.maxZ), s, color)
            drawLine(Location(w, box.minX, box.maxY, box.maxZ), Location(w, box.minX, box.minY, box.maxZ), s, color)
            drawLine(Location(w, box.maxX, box.maxY, box.minZ), Location(w, box.minX, box.maxY, box.minZ), s, color)
            drawLine(Location(w, box.maxX, box.minY, box.minZ), Location(w, box.minX, box.minY, box.minZ), s, color)
            drawLine(Location(w, box.maxX, box.maxY, box.minZ), Location(w, box.maxX, box.minY, box.minZ), s, color)
            drawLine(Location(w, box.minX, box.maxY, box.minZ), Location(w, box.minX, box.minY, box.minZ), s, color)
            drawLine(Location(w, box.minX, box.maxY, box.maxZ), Location(w, box.minX, box.maxY, box.minZ), s, color)
            drawLine(Location(w, box.minX, box.minY, box.maxZ), Location(w, box.minX, box.minY, box.minZ), s, color)
            drawLine(Location(w, box.maxX, box.maxY, box.maxZ), Location(w, box.maxX, box.maxY, box.minZ), s, color)
            drawLine(Location(w, box.maxX, box.minY, box.maxZ), Location(w, box.maxX, box.minY, box.minZ), s, color)
        }
    }
}