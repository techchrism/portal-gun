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

        fun drawLine(start: Location, finish: Location, space: Double, color: Color) {
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

        fun outlineBoundingBox(box: BoundingBox, w: World?, color: Color) {
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