package me.techchrism.portalgun

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class PortalGun : JavaPlugin(), Listener {
    private var previewTask = PreviewTask()

    override fun onEnable() {
        portalGunKey = NamespacedKey(this, "portal-gun")
        server.pluginManager.registerEvents(this, this)
        val portalGunCommand = PortalGunCommand()
        server.getPluginCommand("portalgun")?.setExecutor(portalGunCommand)
        server.getPluginCommand("portalgun")?.setTabCompleter(portalGunCommand)
        previewTask.runTaskTimer(this, 0L, 1L)
    }

    override fun onDisable() {
        // I'm pretty sure all tasks belonging to a plugin get cancelled when the plugin is disabled
        // But I'm also kinda dumb so this is here to be sure
        previewTask.cancel()
    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        // Check if interacted with portal gun weapon
        if (!event.hasItem() || !isPortalGun(event.item!!)) {
            return
        }
        event.isCancelled = true
        val candidate = PortalCandidate.checkFor(event.player)
        if (candidate.valid) {
            val act = event.action
            if (act == Action.LEFT_CLICK_AIR || act == Action.LEFT_CLICK_BLOCK) {
                previewTask.pair.fireFirst(candidate)
            } else {
                previewTask.pair.fireSecond(candidate)
            }
            event.player.playSound(event.player.location, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 0.7f)
        } else {
            event.player.sendMessage(candidate.invalidReason)
        }
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        // Use the swap event (default as "f" key) as a keybind to clear portals
        if (isPortalGun(event.offHandItem!!)) {
            event.isCancelled = true
            previewTask.pair.clear()
            event.player.playSound(event.player.location, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.9f)
        }
    }

    companion object {
        lateinit var portalGunKey: NamespacedKey

        /**
         * Checks if the provided item is a valid portal gun
         * @param item ItemStack the item to check
         * @return Boolean true if the item is a valid portal gun
         */
        fun isPortalGun(item: ItemStack): Boolean {
            return item.itemMeta?.persistentDataContainer?.get(portalGunKey, PersistentDataType.BYTE) != null
        }

        /**
         * Generates a new portal gun
         * @return ItemStack the generated portal gun
         */
        fun generatePortalGun(): ItemStack {
            val item = ItemStack(Material.DIAMOND_HORSE_ARMOR, 1)
            val meta: ItemMeta? = item.itemMeta
            meta?.setDisplayName(ChatColor.AQUA.toString() + "Portal Gun")
            meta?.persistentDataContainer?.set(portalGunKey, PersistentDataType.BYTE, 1)
            meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.setItemMeta(meta)
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            return item
        }
    }
}