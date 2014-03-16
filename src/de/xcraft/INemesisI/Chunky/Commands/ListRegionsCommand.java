package de.xcraft.INemesisI.Chunky.Commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class ListRegionsCommand extends XcraftCommand {
	// @formatter:off
	public static final BlockFace[] radial = { BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST };
	private String[] legend = {
			"----&f&lN&8----",
			"--&f&lW&7-+-&f&lE&8--",
			"----&f&lS&8----",
			"",
			"&6&nLegend:                ",
			"" + ChatColor.GRAY +ChatColor.BOLD+ "✖" + ChatColor.GRAY+ " - your position",
			"" + ChatColor.GRAY + "      (colored)",
			"" + ChatColor.DARK_GREEN + "▒" + ChatColor.GRAY + " - owner of chunk",
			"" + ChatColor.GOLD + "▒" + ChatColor.GRAY + " - member of chunk",
			"" + ChatColor.DARK_RED + "▒" + ChatColor.GRAY + " - not your chunk",
			"" + ChatColor.BLACK + "▒" + ChatColor.GRAY + " - unclaimed chunk",
	};
	// @formatter:on
	public ListRegionsCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		ChunkManager cManaer = (ChunkManager) manager;
		Player player = (Player) sender;
		Block block = player.getLocation().getBlock();
		if (cManaer.getData().get(player.getWorld().getName()) == null) {
			sendInfo(sender, Msg.ERR_WORLD_NOT_ACTIVE.toString(), true);
			return true;
		}
		String line = "";
		int zr = 5;
		int xr = 2 * zr;
		long time = System.nanoTime();
		boolean wasBlack = false;
		Map<String, ChunkRegion> map = new HashMap<String, ChunkRegion>();
		String chunkOwner;
		Chunk chunk;
		ChunkRegion region;
		sendInfo(sender, " &6Region Map &7 -- You are facing: &f" + radial[Math.round(player.getLocation().getYaw() / 45f) & 0x7].toString(), true);
		for (int z = -zr; z <= zr; z++) {
			for (int x = -xr; x <= xr; x++) {
				chunk = block.getRelative(x * 16, 0, z * 16).getChunk();
				if (ChunkManager.hasChunkOwner(chunk)) {
					chunkOwner = ChunkManager.getChunkOwner(chunk);
						region = map.get(chunkOwner);
						if (region == null) {
							region = cManaer.getData().get(player.getWorld().getName()).get(chunkOwner);
						}
						if (region != null) {
							if (region.getRegion().isOwner(player.getName())) {
								line += ChatColor.DARK_GREEN;
							} else if (region.getRegion().isMember(player.getName())) {
								line += ChatColor.GOLD;
							} else {
								line += ChatColor.DARK_RED;
							}
						} else {
							line += ChatColor.DARK_GRAY;
						}
						wasBlack = false;
				} else if (!wasBlack) {
					line += ChatColor.BLACK;
					wasBlack = true;
				}
				if (x == 0 && z == 0) {
					line += ChatColor.BOLD + "✖" + ChatColor.RESET;
					wasBlack = false;
				} else
					line += "▒";
				continue;

			}
			if (legend.length > z + zr) {
				line += ChatColor.DARK_GRAY + " |" + legend[z + zr];
			}
			sendInfo(sender, "" + line, false);
			wasBlack = false;
			line = "";
		}
		Messenger.info(cManaer.getPlugin().getName() + ": List regions took " + ((float) (System.nanoTime() - time) / 1000000f) + "ms!");
		return true;
	}
}
