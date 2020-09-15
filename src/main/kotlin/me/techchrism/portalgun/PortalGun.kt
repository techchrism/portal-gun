package me.techchrism.portalgun

import org.bukkit.plugin.java.JavaPlugin

class PortalGun : JavaPlugin() {
    override fun onEnable() {
        this.logger.info("Loaded PortalGun plugin!")
    }
}