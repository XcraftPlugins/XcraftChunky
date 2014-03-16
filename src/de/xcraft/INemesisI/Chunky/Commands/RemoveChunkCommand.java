package de.xcraft.INemesisI.Chunky.Commands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;

public class RemoveChunkCommand extends XcraftCommand {

	public RemoveChunkCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		ChunkManager pManager = (ChunkManager) manager;
		Player player = (Player) sender;
		Chunk chunk = player.getLocation().getChunk();
		if (ChunkManager.hasChunkOwner(chunk) && !ChunkManager.getChunkOwner(chunk).startsWith("!")) {
			ChunkRegion region = pManager.getData().get(chunk.getWorld().getName()).get(ChunkManager.getChunkOwner(chunk));
			if (region.size() == 1) {
				sendInfo(sender, Msg.COMMAND_REMOVE_NOT_POSSIBLE.toString(), true);
				return true;
			}
			if (region.getRegion().isOwner(player.getName()) && !player.hasPermission("xcraftchunky.removechunk.all")) {
				region.remove(chunk);
				pManager.selectRegion(player, region);
				sendInfo(sender, Msg.COMMAND_REMOVECHUNK_SUCESSFULL.toString(), true);
			} else {
				sendInfo(sender, Msg.ERR_NOT_OWNER.toString(), true);
			}
		} else {
			sendInfo(sender, Msg.COMMAND_REMOVECHUNK_FAIL.toString(), true);
		}
		return true;
	}
}
