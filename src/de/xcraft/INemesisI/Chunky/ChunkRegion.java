package de.xcraft.INemesisI.Chunky;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Chunky.Manager.ConfigManager;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class ChunkRegion {
	private XcraftChunky plugin;
	private RegionManager regionManager;
	private List<Chunk> chunks;
	private String owner;
	private int suffix;
	private ProtectedPolygonalRegion region;

	public ChunkRegion(XcraftChunky plugin, RegionManager regionManager, List<Chunk> chunks, String owner) {
		super();
		this.plugin = plugin;
		this.regionManager = regionManager;
		this.chunks = chunks;
		this.owner = owner.toLowerCase();
		suffix = 1;
		while (true) {
			if (regionManager.getRegion(owner + "-" + suffix) == null)
				break;
			else
				suffix++;
		}
		rebuild();
	}

	public ChunkRegion(XcraftChunky plugin, RegionManager regionManager, List<Chunk> chunks, String owner, int suffix, ProtectedPolygonalRegion region) {
		super();
		this.plugin = plugin;
		this.regionManager = regionManager;
		this.chunks = chunks;
		this.owner = owner.toLowerCase();
		this.suffix = suffix;
		this.region = region;
		if (region == null)
			this.rebuild();
		else
			this.mark();
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getSuffix() {
		return suffix;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}

	public ProtectedPolygonalRegion getRegion() {
		return region;
	}

	public void setRegion(ProtectedPolygonalRegion region) {
		this.region = region;
	}

	protected List<Chunk> getChunks() {
		return chunks;
	}

	public void add(Chunk chunk) {
		chunks.add(chunk);
		rebuild();
	}

	public void combine(ChunkRegion region) {
		chunks.addAll(region.getChunks());
		rebuild();
	}

	public void remove(Chunk chunk) {
		Iterator<Chunk> i = chunks.iterator();
		while(i.hasNext()) {
			Chunk c = i.next();
			if (c.getX() == chunk.getX() && c.getZ() == chunk.getZ()) {
				i.remove();
				break;
			}
		}
		ChunkManager.removeChunkOwner(plugin, chunk);
		rebuild();
	}

	public void mark() {
		String id = owner + "-" + suffix;
		for (Chunk chunk : chunks) {
			ChunkManager.setChunkOwner(plugin, chunk, id);
		}
	}

	public void unMark() {
		for (Chunk chunk : chunks) {
			ChunkManager.removeChunkOwner(plugin, chunk);
		}
	}

	public int size() {
		return chunks.size();
	}

	public boolean touches(Chunk chunk) {
		int r = 1;
		mark();
		for (int x = -r; x < r; x++) {
			for (int z = -r; z < r; z++) {
				Chunk check = chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z);
				if (ChunkManager.isChunkOwner(check, region.getId())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(Chunk chunk) {
		return chunks.contains(chunk);
	}

	public boolean isOwner(Chunk chunk) {
		return ChunkManager.isChunkOwner(chunk, region.getId());
	}

	public void rebuild() {
		//@formatter:off
		/* This is how the region is defined:
		 * +----**----+   +, x and * are marking thepolygons of the region.
		 * |    ||    |   + polygons are defined by the first getPoints() call.
		 * |  x-**-x  |   Then x polygons are defined by another getPoints() call
		 * |  |    |  |   with chunks, that needs to be unprotected.
		 * |  x----x  |   To combine those regions and unprotect the inner region
		 * |          |   from the outter one, we "cut" both region at one point
		 * +----------+   and "glue" both region as shown in the image together
		 */
		// @formatter:on
		long time = System.nanoTime();
		String regionName = owner + "-" + suffix;
		// mark the chunks
		mark();
		// get the outter border of the new region
		List<BlockVector2D> points = getPoints(chunks, regionName);

		// create the new region, to be able to use region.contains(block);
		long time2 = System.nanoTime();
		ProtectedPolygonalRegion newRegion = new ProtectedPolygonalRegion(regionName, points, 0, chunks.get(0).getWorld().getMaxHeight());
		if (this.region != null) {
			newRegion.setOwners(this.region.getOwners());
			newRegion.setMembers(this.region.getMembers());
			newRegion.setFlags(this.region.getFlags());
			newRegion.setPriority(this.region.getPriority());
			try {
				newRegion.setParent(this.region.getParent());
			} catch (CircularInheritanceException e1) {
				e1.printStackTrace();
			}
		} else {
			newRegion.getOwners().addPlayer(owner);
		}
		Messenger.info(plugin.getName() + ": creating empty region took " + ((float) (System.nanoTime() - time2) / 1000000f) + "ms");

		validateRegion(newRegion, chunks, regionName);
		time2 = System.nanoTime();
		if (this.region != null)
			regionManager.removeRegion(this.region.getId());
		regionManager.addRegion(newRegion);
		this.region = newRegion;
		try {
			regionManager.save();
			((ConfigManager) plugin.getConfigManager()).saveChunkRegion(chunks.get(0).getWorld(), this);
		} catch (ProtectionDatabaseException e) {
			Messenger.severe("ERROR while saving regions. " + newRegion.getId() + " was just rebuilt");
			e.printStackTrace();
		}
		Messenger.info(plugin.getName() + ": saving took " + ((float) (System.nanoTime() - time2) / 1000000f) + "ms (blame WG)");
		Messenger.info(plugin.getName() + ": Region rebuild took " + ((float) (System.nanoTime() - time) / 1000000f) + "ms with " + chunks.size() + " chunks and " + region.getPoints().size()
				+ " points!");
	}

	private List<BlockVector2D> getPoints(List<Chunk> chunks, String chunkOwner) {
		long time = System.nanoTime();
		List<BlockVector2D> points = new ArrayList<BlockVector2D>();
		Chunk chunk = chunks.get(0);
		BlockFace face = BlockFace.NORTH;
		for (Chunk c : chunks) {
			ChunkManager.setChunkOwner(plugin, c, chunkOwner);
			if ((c.getZ() >= chunk.getZ() && c.getX() > chunk.getX()) || c.getZ() > chunk.getZ())
				chunk = c;
		}
		while (true) {
			BlockVector2D bv;
			Chunk check = ChunkManager.getChunkInDirection(chunk, Direction.FRONTRIGHT, face);
			if (ChunkManager.isChunkOwner(check, chunkOwner)) {
				Chunk check2 = ChunkManager.getChunkInDirection(chunk, Direction.FRONT, face);
				if (ChunkManager.isChunkOwner(check2, chunkOwner)) {
					bv = getBlockVectorAtChunkCorner(check2, Direction.BACKRIGHT, face);
					if (points.contains(bv))
						break;
					points.add(bv);
				} else {
					points.add(getBlockVectorAtChunkCorner(chunk, Direction.FRONTRIGHT, face));
					points.add(getBlockVectorAtChunkCorner(check, Direction.BACKLEFT, face));
				}
				chunk = check;
				face = getFaceInDirection(face, Direction.RIGHT);
			} else {
				check = ChunkManager.getChunkInDirection(chunk, Direction.FRONT, face);
				if (ChunkManager.isChunkOwner(check, chunkOwner)) {
					chunk = check;
				} else {
					bv = getBlockVectorAtChunkCorner(chunk, Direction.FRONTRIGHT, face);
					if (points.contains(bv))
						break;
					points.add(bv);
					face = getFaceInDirection(face, Direction.LEFT);
				}
			}
		}
		Messenger.info(plugin.getName() + ": getPoints() call took " + ((float) (System.nanoTime() - time) / 1000000f) + "ms with " + chunks.size() + " chunks and " + points.size() + " points!");
		return points;
	}

	private void validateRegion(ProtectedPolygonalRegion region, List<Chunk> chunks, String regionName) {
		// validation: there can be chunks in the region, which the player has not bought, since
		// getPoints() only checks the chunks, that borders the region
		Messenger.info(plugin.getName() + ": starting validation....");
		long time = System.nanoTime();
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		World world = chunks.get(0).getWorld();
		for (Chunk chunk : chunks) {
			Block block = chunk.getBlock(8, 0, 8);
			for (BlockFace face : faces) {
				Block relativeBlock = block.getRelative(face, 16);
				Chunk relativeChunk = world.getChunkAt(relativeBlock);
				// if a chunk is not in the list of chunks, the player bought, but in his border
				if (!ChunkManager.isChunkOwner(relativeChunk, regionName) && region.contains(new BlockVector2D(relativeBlock.getX(), relativeBlock.getZ()))) {
					// a list of chunks, we need to exclude from the region
					List<Chunk> needToUnprotect = new ArrayList<Chunk>();
					needToUnprotect.add(relativeChunk);
					// mark the region as NOT from the owner
					ChunkManager.setChunkOwner(plugin, relativeChunk, "!" + regionName);
					// we found one chunk, which needs to be unprotected.. lets look for neighbours!
					for (int o = 0; o < needToUnprotect.size(); o++) {
						Chunk c1 = needToUnprotect.get(o);
						Block b1 = c1.getBlock(8, 0, 8);
						for (BlockFace f : faces) {
							Block b2 = b1.getRelative(f, 16);
							Chunk c2 = world.getChunkAt(b2);
							if (!ChunkManager.isChunkOwner(c2, regionName) && !needToUnprotect.contains(c2) && region.contains(new BlockVector2D(b2.getX(), b2.getZ()))) {
								needToUnprotect.add(c2);
								// mark the region as NOT from the owner
								ChunkManager.setChunkOwner(plugin, c2, "!" + regionName);
							}
						}
					}
					// get the Points of the region, we want to unprotect
					combinePoints(region, getPoints(needToUnprotect, "!" + regionName));
					for (Chunk c : needToUnprotect) {
						ChunkManager.removeChunkOwner(plugin, c);
					}
				}
			}
		}
		Messenger.info(plugin.getName() + ": ...validation took " + ((float) (System.nanoTime() - time) / 1000000f) + "ms with " + chunks.size() + " chunks");
	}

	private void combinePoints(ProtectedPolygonalRegion newRegion, List<BlockVector2D> points) {
		long time = System.nanoTime();
		BlockVector2D unprotectedCut = points.get(0);
		BlockVector2D protectedCut = newRegion.getPoints().get(0);
		double distance = unprotectedCut.distance(protectedCut);
		for (BlockVector2D up : points) {
			for (BlockVector2D pp : newRegion.getPoints()) {
				if (up.distance(pp) < distance) {
					distance = up.distance(pp);
					unprotectedCut = up;
					protectedCut = pp;
				}
			}
		}
		int upIndex = points.indexOf(unprotectedCut);
		int ppIndex = newRegion.getPoints().indexOf(protectedCut);
		newRegion.getPoints().addAll(ppIndex, points.subList(0, upIndex + 1));
		newRegion.getPoints().addAll(ppIndex, points.subList(upIndex, points.size()));
		newRegion.getPoints().add(ppIndex, protectedCut);
		Messenger.info(plugin.getName() + ": combinePoints() call took " + ((float) (System.nanoTime() - time) / 1000000f) + "ms with " + newRegion.getPoints().size() + " Points now. (+"
				+ points.size() + ")");
	}

	private BlockFace getFaceInDirection(BlockFace face, Direction dir) {
		switch (face) {
		case NORTH:
			if (dir == Direction.LEFT) {
				return BlockFace.WEST;
			} else if (dir == Direction.RIGHT) {
				return BlockFace.EAST;
			}
		case WEST:
			if (dir == Direction.LEFT) {
				return BlockFace.SOUTH;
			} else if (dir == Direction.RIGHT) {
				return BlockFace.NORTH;
			}
		case SOUTH:
			if (dir == Direction.LEFT) {
				return BlockFace.EAST;
			} else if (dir == Direction.RIGHT) {
				return BlockFace.WEST;
			}
		case EAST:
			if (dir == Direction.LEFT) {
				return BlockFace.NORTH;
			} else if (dir == Direction.RIGHT) {
				return BlockFace.SOUTH;
			}
		default:
			break;
		}
		return null;
	}

	private BlockVector2D getBlockVectorAtChunkCorner(Chunk chunk, Direction corner, BlockFace face) {
		Direction c = corner.getDirectionInFace(face);
		Block b = chunk.getBlock((int) (c.getModX() * 7.5 + 7.5), 0, (int) (c.getModZ() * 7.5 + 7.5));
		return new BlockVector2D(b.getX(), b.getZ());
	}

	@Override
	public String toString() {
		String chunkData = "ID: " + owner + "-" + suffix + " Chunks: ";
		for (Chunk chunk : chunks) {
			chunkData = chunkData + chunk.getX() + "," + chunk.getZ() + "; ";
		}
		return chunkData;
	}

}
