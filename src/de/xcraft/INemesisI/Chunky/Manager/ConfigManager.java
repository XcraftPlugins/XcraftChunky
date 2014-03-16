package de.xcraft.INemesisI.Chunky.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.XcraftChunky;
import de.xcraft.INemesisI.Library.Manager.XcraftConfigManager;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class ConfigManager extends XcraftConfigManager {
	private XcraftChunky plugin;
	private ChunkManager manager;
	private WorldGuardPlugin worldguard;
	private FileConfiguration data;
	private File dataFile;

	public List<String> activeWorlds;

	public int RegionCreate;
	public int RegionDelete;

	public String ChunkEquation;
	public int ChunkMin;
	public int ChunkMax;
	public Map<String, Integer> multiplier;

	public boolean useMultiplier;

	public ConfigManager(XcraftChunky plugin) {
		super(plugin);
		this.plugin = plugin;
		manager = (ChunkManager) plugin.getPluginManager();
		worldguard = plugin.getWorldGuard();
	}

	@Override
	public void load() {
		// load locale
		Msg.init(plugin);
		// load config
		this.activeWorlds = config.getStringList("Worlds");

		this.RegionCreate = config.getInt("Price.Region.create");
		this.RegionDelete = config.getInt("Price.Region.delete");

		this.ChunkEquation = config.getString("Price.Chunk.equation");
		this.ChunkMin = config.getInt("Price.Chunk.min");
		this.ChunkMax = config.getInt("Price.Chunk.max");

		if (config.getBoolean("Multiplier.use")) {
			this.multiplier = new HashMap<String, Integer>();
			for (String key : config.getConfigurationSection("Multiplier").getKeys(false)) {
				for (String multiplier : config.getConfigurationSection("Multiplier" + key).getKeys(false)) {
					this.multiplier.put(key + ":" + multiplier, config.getInt(key + "." + multiplier));
				}
			}
		}

		// load data
		dataFile = new File(plugin.getDataFolder(), "data.yml");
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		data = YamlConfiguration.loadConfiguration(dataFile);

		for (World world : plugin.getServer().getWorlds()) {
			if (activeWorlds.contains(world.getName()))
				loadWorld(world);
		}
	}

	@Override
	public void save() {
		for (World world : plugin.getServer().getWorlds()) {
			saveWorld(world);
		}
		try {
			data.save(dataFile);
		} catch (IOException e) {
			Messenger.warning(plugin.getDescription().getFullName() + " Error while saving data.yml");
			e.printStackTrace();
		}
	}

	public void loadWorld(World world) {
		if (world == null || !activeWorlds.contains(world.getName()))
			return;
		String worldName = world.getName();
		Map<String, ChunkRegion> worldData = new HashMap<String, ChunkRegion>();
		if (data.isConfigurationSection(world.getName())) {
			RegionManager regionManager = worldguard.getRegionManager(world);
			List<Chunk> chunks;
			Chunk chunkAt;
			for (String id : data.getConfigurationSection(worldName).getKeys(false)) {
				chunks = new ArrayList<Chunk>();
				for (String chunk : data.getString(worldName + "." + id).split("; ")) {
					String[] c = chunk.split(",");
					int x = Integer.parseInt(c[0]);
					int z = Integer.parseInt(c[1]);
					chunkAt = world.getChunkAt(x, z);
					if (!chunks.contains(chunkAt))
						chunks.add(chunkAt);
					String[] idsplit = id.split("-");

					worldData.put(id, new ChunkRegion(plugin, regionManager, chunks, idsplit[0], Integer.parseInt(idsplit[1]), (ProtectedPolygonalRegion) regionManager.getRegion(id)));
				}
			}
		}
		manager.getData().put(worldName, worldData);
	}

	public void saveWorld(World world) {
		if (world != null && manager.getData().containsKey(world.getName())) {
			for (String id : manager.getData().get(world.getName()).keySet()) {
				saveChunkRegion(world, manager.getData().get(world.getName()).get(id));
			}
		}
	}

	public void saveChunkRegion(World world, ChunkRegion region) {
		if (region != null) {
			String[] regionData = region.toString().split(" Chunks: ");
			data.set(world.getName() + "." + regionData[0].replace("ID: ", ""), regionData[1]);
		}
	}

	public void unloadWorld(World world) {
		manager.getData().remove(world.getName());
	}
}
