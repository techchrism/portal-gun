package me.techchrism.portalgun

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class PortalGun : JavaPlugin(), Listener {
    override fun onEnable() {
        portalGunKey = NamespacedKey(this, "portal-gun")
        server.pluginManager.registerEvents(this, this)
        val portalGunCommand: PortalGunCommand = PortalGunCommand()
        server.getPluginCommand("portalgun")?.setExecutor(portalGunCommand)
        server.getPluginCommand("portalgun")?.setTabCompleter(portalGunCommand)
    }

    companion object {
        lateinit var portalGunKey: NamespacedKey

        fun isPortalGun(item: ItemStack): Boolean {
            return item.itemMeta?.persistentDataContainer?.has(portalGunKey, PersistentDataType.BYTE) != null
        }
        
        fun generatePortalGun(): ItemStack {
            val item: ItemStack = ItemStack(Material.DIAMOND_HORSE_ARMOR, 1)
            val meta: ItemMeta? = item.itemMeta
            meta?.setDisplayName(ChatColor.AQUA.toString() + "Portal Gun")
            meta?.persistentDataContainer?.set(portalGunKey, PersistentDataType.BYTE, 1)
            item.setItemMeta(meta)
            return item
        }
    }
}