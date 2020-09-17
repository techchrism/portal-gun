package me.techchrism.portalgun

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs

class Portal {
    var topID: UUID
    var bottomID: UUID
    var topLoc: Location
    var bottomLoc: Location
    var facing: BlockFace
    var topData: BlockData
    var bottomData: BlockData

    var topEntity: FallingBlock? = null
    var bottomEntity: FallingBlock? = null
    var topBlock: Block? = null
    var bottomBlock: Block? = null

    var color: Color? = null

    private var funnelBoundingBox: BoundingBox? = null
    private var predictiveBoundingBox: BoundingBox? = null

    val isLoaded: Boolean
        get() = topEntity != null && bottomEntity != null

    constructor(
        topID: UUID,
        bottomID: UUID,
        topLoc: Location,
        bottomLoc: Location,
        facing: BlockFace,
        topData: BlockData,
        bottomData: BlockData,
        color: Color
    ) {
        this.topID = topID
        this.bottomID = bottomID
        this.topLoc = topLoc
        this.bottomLoc = bottomLoc
        this.facing = facing
        this.topData = topData
        this.bottomData = bottomData
        this.color = color
    }

    constructor(candidate: PortalCandidate) {
        if(candidate.top == null || candidate.bottom == null) {
            throw IllegalArgumentException()
        }
        candidate.bottom!!.chunk.load()
        candidate.top!!.chunk.load()
        topLoc = candidate.top!!.location.add(0.5, 0.0, 0.5)
        bottomLoc = candidate.bottom!!.location.add(0.5, 0.0, 0.5)
        topBlock = candidate.top!!
        bottomBlock = candidate.bottom!!
        topData = candidate.top!!.blockData
        bottomData = candidate.bottom!!.blockData
        facing = candidate.face!!
        candidate.top!!.type = Material.AIR
        candidate.bottom!!.type = Material.AIR
        topEntity = candidate.top!!.world.spawnFallingBlock(topLoc, topData)
        bottomEntity = candidate.bottom!!.world.spawnFallingBlock(bottomLoc, bottomData)
        topEntity!!.setGravity(false)
        bottomEntity!!.setGravity(false)
        topEntity!!.velocity = Vector(0, 0, 0)
        bottomEntity!!.velocity = Vector(0, 0, 0)
        topEntity!!.teleport(topLoc)
        bottomEntity!!.teleport(bottomLoc)
        topEntity!!.dropItem = false
        bottomEntity!!.dropItem = false
        //topEntity.setInvulnerable(true);
        //bottomEntity.setInvulnerable(true);
        topID = topEntity!!.uniqueId
        bottomID = bottomEntity!!.uniqueId
        topLoc.add(0.0, 0.5, 0.0)
        bottomLoc.add(0.0, 0.5, 0.0)
        funnelBoundingBox = getBoundingBox(1.0, 7.0, 4.5)
        predictiveBoundingBox = getBoundingBox(0.2, 1.2, 1.3)
    }

    fun load() {
        topLoc.chunk.load()
        bottomLoc.chunk.load()
    }

    fun destroy() {
        if (!isLoaded) {
            load()
        }
        topEntity!!.remove()
        bottomEntity!!.remove()
        topLoc.block.type = topData.material
        topLoc.block.blockData = topData
        bottomLoc.block.type = bottomData.material
        bottomLoc.block.blockData = bottomData
    }

    fun teleportHere(entity: Entity, from: BlockFace, top: Boolean) {
        if (!isLoaded) {
            load()
        }
        val tall = entity.height > 1.0
        val to: Location
        to = if (top && !tall) {
            topLoc.clone()
        } else {
            bottomLoc.clone()
        }
        to.add(facing.direction)
        if (tall && facing == BlockFace.DOWN) {
            to.subtract(0.0, 1.5, 0.0)
        } else {
            to.subtract(0.0, 0.5, 0.0)
        }
        to.direction = Portal.normalizeVector(entity.location.direction.clone(), facing, from)
        val velocity: Vector = Portal.normalizeVector(entity.velocity.clone(), facing, from)
        entity.teleport(to)
        entity.velocity = velocity
        if (entity is Player) {
            entity.playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
        }
    }

    fun drawParticles() {
        if (!isLoaded || color == null) {
            return
        }
        val candidate = PortalCandidate(topBlock!!, bottomBlock!!, facing)
        ParticleUtils.drawEllipse(candidate, 0.45, 0.9, color!!)

        //ParticleUtils.drawLine(bottomLoc, bottomLoc.clone().add(facing.getDirection().multiply(5)), 0.5);
        ParticleUtils.outlineBoundingBox(funnelBoundingBox!!, bottomLoc.world, Color.AQUA)
        ParticleUtils.outlineBoundingBox(predictiveBoundingBox!!, bottomLoc.world, Color.YELLOW)
    }

    fun onChunkLoad(chunk: Chunk) {
        for (e in chunk.entities) {
            val id = e.uniqueId
            if (id === topID) {
                topEntity = e as FallingBlock
                topBlock = topLoc.block
            } else if (id === bottomID) {
                bottomEntity = e as FallingBlock
                bottomBlock = bottomLoc.block
            } else {
                continue
            }

            // Replace the block with air
            e.getLocation().block.type = Material.AIR
        }
    }

    private fun getBoundingBox(expand: Double, extend: Double, shift: Double): BoundingBox {
        val box = BoundingBox.of(bottomBlock!!, topBlock!!)
        val basics = arrayOf(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST)
        val opp = facing.oppositeFace
        for (current in basics) {
            if (facing == current || opp == current) {
                continue
            }
            box.expand(current, expand)
            box.expand(current.oppositeFace, expand)
        }
        box.expand(facing, extend)
        box.shift(facing.direction.multiply(shift))
        return box
    }

    fun onChunkUnload(chunk: Chunk) {
        for (e in chunk.entities) {
            val id = e.uniqueId
            if (id === topID) {
                topEntity = null
                topBlock!!.type = topData.material
                topBlock!!.blockData = topData
                topBlock = null
            } else if (id === bottomID) {
                bottomEntity = null
                bottomBlock!!.type = bottomData.material
                bottomBlock!!.blockData = bottomData
                bottomBlock = null
            }
        }
    }

    fun predictTeleportation(other: Portal) {
        val entities = bottomLoc.world!!.getNearbyEntities(predictiveBoundingBox!!)
        for (e in entities) {
            // Get entity velocity direction
            val velocity = e.velocity
            if (velocity.lengthSquared() < 0.9 && e !is Projectile) {
                continue
            }

            // Check if they're moving in a direction towards the portal
            if (getVectorDirection(velocity) == facing.oppositeFace) {
                // Teleport them
                other.teleportHere(e, facing, true)
            }
        }
    }

    fun checkSucktioning() {
        val entities = bottomLoc.world!!.getNearbyEntities(funnelBoundingBox!!)
        val midpoint = Location(
            topLoc.world,
            (topLoc.x + bottomLoc.x) / 2.0,
            (topLoc.y + bottomLoc.y) / 2.0,
            (topLoc.z + bottomLoc.z) / 2.0
        )
        for (e in entities) {
            // Get entity velocity direction
            val velocity = e.velocity
            if (velocity.lengthSquared() < 0.9) {
                continue
            }

            // Check if they're moving in a direction towards the portal
            if (getVectorDirection(velocity) == facing.oppositeFace) {
                // Redirect the vector towards the center of the portal
                val length = velocity.length()
                val revised = midpoint.toVector().subtract(e.location.toVector()).normalize()
                revised.multiply(length)
                e.velocity = revised
            }
        }
    }

    companion object {
        fun normalizeVector(vector: Vector, inFace: BlockFace, outFace: BlockFace): Vector {
            val mod = vector.clone()
            if(inFace.oppositeFace == outFace)
            {
                return mod
            }

            if(inFace == outFace)
            {
                // There's not enough information to determine horizontal portal orientation from the normal vector
                // So instead, just flip the y-axis
                return if (inFace == BlockFace.UP || inFace == BlockFace.DOWN) {
                    mod.setY(mod.y * -1)
                } else {
                    mod.rotateAroundY(Math.PI)
                }
            }

            val cross = inFace.direction.crossProduct(outFace.direction)
            return mod.rotateAroundAxis(cross, Math.PI / 2)
        }

        fun getVectorDirection(v: Vector): BlockFace {
            // Get component of largest magnitude
            return if (abs(v.x) > abs(v.y)) {
                if (abs(v.x) > abs(v.z)) {
                    if (v.x > 0) BlockFace.EAST else BlockFace.WEST
                } else {
                    if (v.z > 0) BlockFace.SOUTH else BlockFace.NORTH
                }
            } else {
                if (abs(v.y) > abs(v.z)) {
                    if (v.y > 0) BlockFace.UP else BlockFace.DOWN
                } else {
                    if (v.z > 0) BlockFace.SOUTH else BlockFace.NORTH
                }
            }
        }
    }
}