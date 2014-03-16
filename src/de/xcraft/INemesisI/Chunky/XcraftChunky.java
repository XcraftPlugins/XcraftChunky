package de.xcraft.INemesisI.Chunky;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Chunky.Manager.CommandManager;
import de.xcraft.INemesisI.Chunky.Manager.ConfigManager;
import de.xcraft.INemesisI.Chunky.Manager.EventListener;
import de.xcraft.INemesisI.Library.XcraftEventListener;
import de.xcraft.INemesisI.Library.XcraftPlugin;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftConfigManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class XcraftChunky extends XcraftPlugin {
	private Messenger messenger;
	private ChunkManager pManager;
	private ConfigManager cManager;
	private CommandManager cmdManager;
	private EventListener eventListener;
	private WorldGuardPlugin worldguard;
	private Economy economy;
	private Permission permission;

	@Override
	protected void setup() {
		messenger = Messenger.getInstance(this);
		setupVault();
		setupWorldGuard();
		pManager = new ChunkManager(this);
		cManager = new ConfigManager(this);
		cManager.load();
		cmdManager = new CommandManager(this);
		eventListener = new EventListener(this);


	}

	private void setupWorldGuard() {
		worldguard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
	}

	private void setupVault() {
		RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		RegisteredServiceProvider<Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
	}

	@Override
	public XcraftPluginManager getPluginManager() {
		return pManager;
	}

	@Override
	public XcraftConfigManager getConfigManager() {
		return cManager;
	}

	@Override
	public XcraftCommandManager getCommandManager() {
		return cmdManager;
	}

	@Override
	public XcraftEventListener getEventListener() {
		return eventListener;
	}

	@Override
	public Messenger getMessenger() {
		return messenger;
	}

	public WorldGuardPlugin getWorldGuard() {
		return worldguard;
	}

	public Economy getEconomy() {
		return economy;
	}

	public Permission getPermission() {
		return permission;
	}

}
