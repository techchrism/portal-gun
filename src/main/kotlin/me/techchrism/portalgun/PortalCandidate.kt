package me.techchrism.portalgun

import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator
import kotlin.math.roundToInt

class PortalCandidate() {
    var top: Block? = null
    var bottom: Block? = null
    var face: BlockFace? = null
    var valid: Boolean = false
    var invalidReason: String = "Uninitialized"

    constructor(top: Block, bottom: Block, face: BlockFace) : this() {
        this.top = top
        this.bottom = bottom
        this.face = face
        this.valid = true
        this.invalidReason = ""
    }

    companion object {
        /**
         * Check if the block can support a portal
         * @param block Block the block to check
         * @return Boolean true if the block can support a portal
         */
        private fun isPortalableBlock(block: Block): Boolean {
            return block.type.isOccluding
        }

        /**
         * Checks if the provided block and direction are valid for a full portal (including edges)
         * @param target Block the block to check
         * @param face BlockFace the direction of the second block in the portal
         * @return String indicates any problems with the portal; empty if no problems
         */
        private fun isViableVerticalBlock(target: Block, face: BlockFace): String {
            if (!isPortalableBlock(target)) {
                return "Target block is not valid for portals"
            }

            // Front must be clear
            if (target.getRelative(face).type.isSolid) {
                return "Front must be clear"
            }

            // Back must be solid
            if (!target.getRelative(face.oppositeFace).type.isOccluding) {
                return "Blocks behind the portal blocks must be solid"
            }

            // Sides must be solid
            if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                if (!target.getRelative(BlockFace.WEST).type.isOccluding || !target.getRelative(BlockFace.EAST).type.isOccluding) {
                    return "Sides must be solid blocks"
                }
            } else {
                if (!target.getRelative(BlockFace.NORTH).type.isOccluding || !target.getRelative(BlockFace.SOUTH).type.isOccluding) {
                    return "Sides must be solid blocks"
                }
            }

            // Top and bottom must be solid
            if (!target.getRelative(BlockFace.UP).type.isOccluding) {
                return "Block above must be solid"
            }
            return if (!target.getRelative(BlockFace.DOWN).type.isOccluding) {
                "Block below must be solid"
            } else ""
        }

        /**
         * Checks if the provided block and direction are valid for a full portal (including edges)
         * @param target Block the block to check
         * @param face BlockFace the direction of the second block in the portal
         * @return String indicates any problems with the portal; empty if no problems
         */
        private fun isViableHorizontalBlock(target: Block, face: BlockFace): String {
            if (!isPortalableBlock(target)) {
                return "Target block is not valid for portals"
            }

            // Front must be clear
            if (target.getRelative(face).type.isSolid) {
                return "Front block must be clear"
            }

            // Back must be solid
            if (!target.getRelative(face.oppositeFace).type.isOccluding) {
                return "Blocks behind the portal blocks must be solid"
            }

            // Sides must be solid
            if (!target.getRelative(BlockFace.NORTH).type.isOccluding) {
                return "Sides must be solid"
            }
            if (!target.getRelative(BlockFace.SOUTH).type.isOccluding) {
                return "Sides must be solid"
            }
            if (!target.getRelative(BlockFace.EAST).type.isOccluding) {
                return "Sides must be solid"
            }
            return if (!target.getRelative(BlockFace.WEST).type.isOccluding) {
                "Sides must be solid"
            } else ""
        }

        /**
         * Tries to get a portal candidate for the provided block, targeted face, and target direction
         * @param target Block the block to try to establish a candidate on
         * @param face BlockFace the face of the block to use for the portal
         * @param playerDirection BlockFace the closest BlockFace representing the player direction
         * @return PortalCandidate an attempt at a generated portal candidate. may be invalid
         */
        private fun getPortalCandidate(target: Block, face: BlockFace, playerDirection: BlockFace): PortalCandidate {
            val candidate = PortalCandidate()
            candidate.face = face
            val dir = face.direction
            if(dir.y == 0.0) {
                var viable = isViableVerticalBlock(target, face)
                if(viable.isNotEmpty()) {
                    candidate.valid = false
                    candidate.invalidReason = viable
                    return candidate
                }

                // Check above/below to see if the target is the top or the bottom
                viable = isViableVerticalBlock(target.getRelative(BlockFace.DOWN), face)
                if (viable.isNotEmpty()) {
                    viable = isViableVerticalBlock(target.getRelative(BlockFace.UP), face)
                    if (viable.isNotEmpty()) {
                        candidate.valid = false
                        candidate.invalidReason = viable
                        return candidate
                    } else {
                        candidate.top = target.getRelative(BlockFace.UP)
                        candidate.bottom = target
                    }
                } else {
                    candidate.top = target
                    candidate.bottom = target.getRelative(BlockFace.DOWN)
                }
            } else {
                var viable = isViableHorizontalBlock(target, face)
                if (viable.isNotEmpty()) {
                    candidate.valid = false
                    candidate.invalidReason = viable
                    return candidate
                }

                // Check above/below to see if the target is the top or the bottom
                viable = isViableHorizontalBlock(target.getRelative(playerDirection.oppositeFace), face)
                if (viable.isNotEmpty()) {
                    viable = isViableHorizontalBlock(target.getRelative(playerDirection), face)
                    if (viable.isNotEmpty()) {
                        candidate.valid = false
                        candidate.invalidReason = viable
                        return candidate
                    } else {
                        candidate.top = target.getRelative(playerDirection)
                        candidate.bottom = target
                    }
                } else {
                    candidate.top = target
                    candidate.bottom = target.getRelative(playerDirection.oppositeFace)
                }
            }
            candidate.valid = true
            return candidate
        }

        /**
         * Tries to get a PortalCandidate for the player based on where they're looking
         * @param player Player the player to check for
         * @return PortalCandidate an attempt at a generated portal candidate. may be invalid
         */
        fun checkFor(player: Player): PortalCandidate {
            val candidate: PortalCandidate
            val axis: Array<BlockFace> = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)

            val i = BlockIterator(player.eyeLocation, 0.0, 50)
            var previous: Block? = null
            while (i.hasNext()) {
                val current = i.next()

                if (previous == null || !isPortalableBlock(current)) {
                    previous = current
                    continue
                }
                val playerFacing = axis[(player.location.yaw / 90.0f).roundToInt() and 0x3]
                candidate = getPortalCandidate(current, current.getFace(previous)!!, playerFacing)
                if (!candidate.valid) {
                    candidate.face = current.getFace(previous)
                    candidate.bottom = current
                    candidate.top = current.getRelative(BlockFace.SOUTH)
                    ParticleUtils.drawX(player, candidate)
                } else {
                    ParticleUtils.drawEllipse(player, candidate, 0.45, 0.9, Color.LIME)
                }
                return candidate
            }

            candidate = PortalCandidate()
            candidate.valid = false
            candidate.invalidReason = "No block in range"
            return candidate
        }
    }
}