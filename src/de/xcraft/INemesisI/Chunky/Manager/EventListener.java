package de.xcraft.INemesisI.Chunky.Manager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import de.xcraft.INemesisI.Library.XcraftEventListener;
import de.xcraft.INemesisI.Library.XcraftPlugin;

public class EventListener extends XcraftEventListener {

	public EventListener(XcraftPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		((ConfigManager) plugin.getConfigManager()).loadWorld(event.getWorld());
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		((ConfigManager) plugin.getConfigManager()).saveWorld(event.getWorld());
		((ConfigManager) plugin.getConfigManager()).unloadWorld(event.getWorld());

	}

}
