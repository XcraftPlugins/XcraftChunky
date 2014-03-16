package de.xcraft.INemesisI.Chunky.Commands;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Chunky.Manager.ConfigManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;
import de.xcraft.INemesisI.Library.Message.Messenger;

public class AddOwnerCommand extends XcraftCommand {

	public AddOwnerCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		ChunkManager cManager = (ChunkManager) manager;
		OfflinePlayer owner = manager.getPlugin().getServer().getOfflinePlayer(args[0]);
		if (!owner.hasPlayedBefore()) {
			sendInfo(sender, Msg.ERR_PLAYER_NEVER_SEEN.toString(Replace.NAME(args[0])), true);
			return true;
		}
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
			if (!region.getRegion().isOwner(player.getName())) {
				sendInfo(sender, Msg.ERR_NOT_OWNER.toString(), true);
				return true;
			}
			region.getRegion().getOwners().addPlayer(owner.getName());
			try {
				cManager.worldguard.getRegionManager(player.getWorld()).save();
			} catch (ProtectionDatabaseException e) {
				Messenger.severe("ERROR while saving regions. A owner was just added!");
				e.printStackTrace();
			}
			sendInfo(sender, Msg.COMMAND_ADDOWNER_SUCCESSFULL.toString(Replace.NAME(owner.getName()), Replace.REGION(region.getRegion().getId())), true);
			return true;
		}
		sendInfo(sender, Msg.COMMAND_ADDOWNER_FAIL.toString(), true);
		return false;
	}

}
