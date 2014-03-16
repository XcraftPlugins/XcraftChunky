package de.xcraft.INemesisI.Chunky.Commands;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xcraft.INemesisI.Chunky.ChunkRegion;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.Manager.ChunkManager;
import de.xcraft.INemesisI.Library.Command.XcraftCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;
import de.xcraft.INemesisI.Library.Manager.XcraftPluginManager;

public class AddChunkCommand extends XcraftCommand {

	public AddChunkCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
		super(cManager, command, name, pattern, usage, desc, permission);
	}

	@Override
	public boolean execute(XcraftPluginManager manager, CommandSender sender, String[] args) {
		ChunkManager pManager = (ChunkManager) manager;
		Player player = (Player) sender;
		Chunk chunk = player.getLocation().getChunk();

		List<ChunkRegion> regions = pManager.getApplicableRegions(player, chunk);
		if (regions == null)
			return true;
		if (regions.size() == 0) {
			sendInfo(player, Msg.COMMAND_ADDCHUNK_REFER_CREATE.toString(), true);
			return true;
		} else if (regions.size() == 1) {
			// add a chunk to a chunkregion
			ChunkRegion region = regions.get(0);
			double price = pManager.getChunkPrice(player, region);
			if (price > 0) {
				if (pManager.getPlugin().getEconomy().has(player.getName(), price))
					pManager.getPlugin().getEconomy().withdrawPlayer(player.getName(), price);
				else {
					sendInfo(sender, Msg.ERR_NOT_ENOUGH_MONEY.toString(), true);
					return true;
				}
			} else
				pManager.getPlugin().getEconomy().depositPlayer(player.getName(), price);

			region.add(chunk);
			pManager.markChunk(player, chunk);
			pManager.selectRegion(player, region);
			sendInfo(sender, Msg.COMMAND_ADDCHUNK_SUCESSFULL.toString(Replace.FEE(pManager.getPlugin().getEconomy().format(price)), Replace.REGION(region.getRegion().getId())), true);
		} else {
			// combining two or more chunkregions
			sendInfo(player, Msg.COMMAND_ADDCHUNK_FAIL.toString(), true);
			return true;
		}
		return true;
	}
}
