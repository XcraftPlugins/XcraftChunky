package de.xcraft.INemesisI.Chunky.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;

public class DeleteRegionCommand extends XcraftCommand {

	public DeleteRegionCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		ChunkManager pManager = (ChunkManager) manager;
		Player player = (Player) sender;
		String name = args[0];

		ChunkRegion chunkRegion = pManager.getData().get(player.getWorld().getName()).get(name);
		if (chunkRegion != null) {
			if (!chunkRegion.getRegion().isOwner(player.getName()) && !player.hasPermission("xcraftchunky.delete.all")) {
				sendInfo(sender, Msg.ERR_NOT_OWNER.toString(), true);
			} else if (!chunkRegion.contains(player.getLocation().getChunk())) {
				sendInfo(sender, Msg.COMMAND_DELETE_NOT_IN_REGION.toString(), true);
			} else {
				int price = pManager.getRegionPrice(false);
				if (price > 0) {
					if (pManager.getPlugin().getEconomy().has(player.getName(), price))
						pManager.getPlugin().getEconomy().withdrawPlayer(player.getName(), price);
					else {
						sendInfo(sender, Msg.ERR_NOT_ENOUGH_MONEY.toString(), true);
						return true;
					}
				} else
					pManager.getPlugin().getEconomy().depositPlayer(player.getName(), price);

				String id = chunkRegion.getRegion().getId();
				chunkRegion.unMark();
				pManager.worldguard.getRegionManager(player.getWorld()).removeRegion(id);
				pManager.getPlugin().getConfigManager().save();
				pManager.getData().get(player.getWorld().getName()).remove(name);
				pManager.selectRegion(player, null);
				sendInfo(sender, Msg.COMMAND_DELETE_SUCCESSFULL.toString(Replace.REGION(id)), true);
			}
		} else
			sendInfo(sender, Msg.COMMAND_DELETE_REGION_NOT_FOUND.toString(), true);
		return true;
	}

}
