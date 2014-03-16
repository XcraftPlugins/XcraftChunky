package de.xcraft.INemesisI.Chunky.Commands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Chunky.Manager.ConfigManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;

public class InfoCommand extends XcraftCommand {

	public InfoCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		ChunkManager cManager = (ChunkManager) manager;
		if (!((ConfigManager) cManager.getPlugin().getConfigManager()).activeWorlds.contains(player.getWorld().getName())) {
			sendInfo(sender, Msg.ERR_WORLD_NOT_ACTIVE.toString(), true);
			return true;
		}
		Chunk chunk = player.getLocation().getChunk();
		if (!ChunkManager.hasChunkOwner(chunk) || ChunkManager.getChunkOwner(chunk).startsWith("!")) {
			sendInfo(sender, Msg.ERR_CHUNK_NOT_BOUGHT.toString(), true);
			return true;

		}
		ChunkRegion region = cManager.getData().get(player.getWorld().getName()).get(ChunkManager.getChunkOwner(chunk));
		if (region != null) {
			ProtectedPolygonalRegion pRegion = region.getRegion();
			if (!pRegion.isOwner(player.getName()) && !pRegion.isMember(player.getName()) && !player.hasPermission("xcraftchunky.info.all")) {
				sendInfo(sender, Msg.ERR_NOT_OWNER.toString(), true);
				return true;
			}
			sendInfo(sender, Msg.COMMAND_INFO_HEADER.toString(Replace.REGION(pRegion.getId())), true);
			sendInfo(sender, Msg.COMMAND_INFO_OWNER.toString(Replace.NAME(pRegion.getOwners().toPlayersString())), true);
			sendInfo(sender, Msg.COMMAND_INFO_MEMBER.toString(Replace.NAME(pRegion.getMembers().toPlayersString())), true);
			sendInfo(sender, Msg.COMMAND_INFO_SIZE.toString(Replace.MISC(String.valueOf(region.size())),//
					Replace.FEE(cManager.getPlugin().getEconomy().format((cManager.getChunkPrice(player, region))))), true);

			return true;
		}
		return false;
	}

}
