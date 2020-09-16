package me.techchrism.portalgun

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

    constructor(top: Block, bottom: Block, face: BlockFace) {
        this.top = top
        this.bottom = bottom
        this.face = face
        this.valid = true
        this.invalidReason = ""
    }

    companion object {
        private fun isPortalableBlock(block: Block): Boolean {
            return block.type.isOccluding
        }

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
                    //TODO ParticleUtils.drawX(player, candidate)
                } else {
                    //TODO ParticleUtils.drawElipse(player, candidate, 0.45, 0.9, Color.LIME)
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