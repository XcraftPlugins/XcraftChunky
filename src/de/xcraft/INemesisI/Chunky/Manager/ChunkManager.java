package de.xcraft.INemesisI.Chunky.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Direction;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.XcraftChunky;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;

public class ChunkManager extends XcraftPluginManager {
	private XcraftChunky plugin;
	public WorldGuardPlugin worldguard;
	private Map<String, Map<String, ChunkRegion>> data = new HashMap<String, Map<String, ChunkRegion>>();

	private ScriptEngineManager mgr;
	private ScriptEngine engine;

	public ChunkManager(XcraftChunky plugin) {
		super(plugin);
		this.plugin = plugin;
		this.data = new HashMap<String, Map<String, ChunkRegion>>();
		this.worldguard = plugin.getWorldGuard();
		this.mgr = new ScriptEngineManager();
		this.engine = mgr.getEngineByName("JavaScript");
	}

	@Override
	public XcraftChunky getPlugin() {
		return plugin;
	}

	public WorldGuardPlugin getWorldGuard() {
		return worldguard;
	}

	public Map<String, Map<String, ChunkRegion>> getData() {
		return data;
	}

	public List<ChunkRegion> getApplicableRegions(Player player, Chunk chunk) {
		String world = chunk.getWorld().getName();
		if (data.get(world) == null) {
			plugin.getMessenger().sendInfo(player, Msg.ERR_WORLD_NOT_ACTIVE.toString(), true);
			return null;
		}
		RegionManager regionManager = worldguard.getRegionManager(chunk.getWorld());

		// Check, if this Chunk has already been bought.
		if (ChunkManager.hasChunkOwner(chunk) && !ChunkManager.getChunkOwner(chunk).startsWith("!")) {
			plugin.getMessenger().sendInfo(player, Msg.ERR_CHUNK_ALREADY_BOUGHT.toString(), true);
			return null;
		}

		// Check, if there is a already a region in this chunk (at all 4 corners)
		Block[] blocks = { chunk.getBlock(0, 64, 0), chunk.getBlock(15, 64, 0), chunk.getBlock(0, 64, 15), chunk.getBlock(15, 64, 15) };
		ApplicableRegionSet regions;
		for (Block block : blocks) {
			regions = regionManager.getApplicableRegions(block.getLocation());
			if (regions.size() != 0) {
				for (ProtectedRegion region : regions) {
					if (!region.isOwner(player.getName())) {
						plugin.getMessenger().sendInfo(player, Msg.ERR_REGION_OVERLAPPING.toString(Replace.NAME(region.getOwners().toPlayersString()), Replace.REGION(region.getId())), true);
						return null;
					}
				}
			}
		}

		// Search for applicable regions nearby
		List<ChunkRegion> chunkRegions = new ArrayList<ChunkRegion>();
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, //
				BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST };

		for (BlockFace face : faces) {
			Chunk relativeChunk = chunk.getBlock(8, 0, 8).getRelative(face, 16).getChunk();
			if (ChunkManager.hasChunkOwner(relativeChunk)) {
				ChunkRegion chunkRegion = data.get(world).get(ChunkManager.getChunkOwner(relativeChunk));
				if (chunkRegion == null)
					continue;
				if (chunkRegion.getRegion().isOwner(player.getName())) {
					if (!chunkRegions.contains(chunkRegion))
						chunkRegions.add(chunkRegion);
				} else {
					plugin.getMessenger().sendInfo(player,
							Msg.ERR_REGION_NEARBY.toString(Replace.REGION(chunkRegion.getRegion().getId()), Replace.NAME(chunkRegion.getRegion().getOwners().toPlayersString())), true);
					return null;
				}
			}
		}
		return chunkRegions;
	}

	public void markChunk(Player player, Chunk chunk) {
		int y = player.getLocation().getBlockY() + 10;
		Block[] corners = { chunk.getBlock(0, y, 0), chunk.getBlock(0, y, 15), chunk.getBlock(15, y, 0), chunk.getBlock(15, y, 15) };
		for (Block corner : corners) {
			for (int i = 0; i < 100; i++) {
				Block relative = corner.getRelative(BlockFace.DOWN, i);
				if (!relative.getType().equals(Material.AIR) && !relative.getPistonMoveReaction().equals(PistonMoveReaction.BREAK)) {
					relative.getRelative(BlockFace.UP, 1).setType(Material.JACK_O_LANTERN);
					break;
				}
			}
		}
	}

	public void selectRegion(Player player, ChunkRegion region) {
		if (player.isOp() || player.hasPermission("worldedit.selection.pos"))
			if (region == null)
				player.performCommand("sel");
			else
				player.performCommand("region sel " + region.getRegion().getId());
	}

	public int getRegionPrice(boolean create) {
		ConfigManager cManager = ((ConfigManager) plugin.getConfigManager());
		if (create)
			return cManager.RegionCreate;
		else
			return cManager.RegionDelete;

	}

	public double getChunkPrice(Player player, ChunkRegion chunkRegion) {
		ConfigManager cManager = ((ConfigManager) plugin.getConfigManager());
		String eqation = cManager.ChunkEquation;
		try {
			engine.put("c", String.valueOf(chunkRegion.size()));
			double price = Double.parseDouble(engine.eval(eqation).toString());
			if (price > cManager.ChunkMax)
				price = cManager.ChunkMax;
			else if (price < cManager.ChunkMin)
				price = cManager.ChunkMin;
			if (cManager.useMultiplier) {
				if (cManager.multiplier.containsKey("World:" + player.getWorld().getName()))
					price *= cManager.multiplier.get("World:" + player.getWorld().getName());
				if (cManager.multiplier.containsKey("Biome:" + player.getLocation().getChunk().getBlock(8, 0, 8).getBiome().toString()))
					price *= cManager.multiplier.get("Biome:" + player.getLocation().getChunk().getBlock(8, 0, 8).getBiome().toString());
				if (cManager.multiplier.containsKey("Group:" + plugin.getPermission().getPrimaryGroup(player)))
					price *= cManager.multiplier.get("Group:" + plugin.getPermission().getPrimaryGroup(player));
			}
			return price;
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return Double.MIN_VALUE;
	}

	public static void setChunkOwner(JavaPlugin plugin, Chunk chunk, String owner) {
		chunk.getBlock(0, 0, 0).setMetadata("ChunkOwner", new FixedMetadataValue(plugin, owner));
	}

	public static void removeChunkOwner(JavaPlugin plugin, Chunk chunk) {
		chunk.getBlock(0, 0, 0).removeMetadata("ChunkOwner", plugin);
	}

	public static boolean isChunkOwner(Chunk chunk, String owner) {
		if (hasChunkOwner(chunk)) {
			return chunk.getBlock(0, 0, 0).getMetadata("ChunkOwner").get(0).asString().equals(owner);
		}
		return false;
	}

	public static boolean hasChunkOwner(Chunk chunk) {
		return (chunk.getBlock(0, 0, 0).hasMetadata("ChunkOwner"));
	}

	public static String getChunkOwner(Chunk chunk) {
		return chunk.getBlock(0, 0, 0).getMetadata("ChunkOwner").get(0).asString();
	}

	public static Chunk getChunkInDirection(Chunk chunk, Direction direction, BlockFace face) {
		Direction d = direction.getDirectionInFace(face);
		return chunk.getWorld().getChunkAt(chunk.getX() + d.getModX(), chunk.getZ() + d.getModZ());
	}

}
