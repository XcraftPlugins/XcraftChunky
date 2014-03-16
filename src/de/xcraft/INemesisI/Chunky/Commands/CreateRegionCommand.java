package de.xcraft.INemesisI.Chunky.Commands;

import java.util.ArrayList;
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

public class CreateRegionCommand extends XcraftCommand {

	public CreateRegionCommand(XcraftCommandManager cManager, String command, String name, String pattern, String usage, String desc, String permission) {
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
			int price = pManager.getRegionPrice(true);
			if (price > 0) {
				if (pManager.getPlugin().getEconomy().has(player.getName(), price))
					pManager.getPlugin().getEconomy().withdrawPlayer(player.getName(), price);
				else {
					sendInfo(sender, Msg.ERR_NOT_ENOUGH_MONEY.toString(), true);
					return true;
				}
			} else
				pManager.getPlugin().getEconomy().depositPlayer(player.getName(), price);

			List<Chunk> chunklist = new ArrayList<Chunk>();
			chunklist.add(chunk);
			ChunkRegion region = new ChunkRegion(pManager.getPlugin(), pManager.worldguard.getRegionManager(chunk.getWorld()), chunklist, player.getName());
			pManager.getData().get(chunk.getWorld().getName()).put(region.getRegion().getId(), region);
			pManager.markChunk(player, chunk);
			pManager.selectRegion(player, region);
			sendInfo(sender, Msg.COMMAND_CREATE_SUCCESSFULL.toString(Replace.FEE(pManager.getPlugin().getEconomy().format(price)), Replace.REGION(region.getRegion().getId())), true);
		} else {
			for (ChunkRegion region : regions) {
				if (region.getRegion().isOwner(player.getName())) {
					sendInfo(sender, Msg.COMMAND_CREATE_REFER_ADDCHUNK.toString(), true);
					return true;
				}
			}
			sendInfo(player, Msg.COMMAND_CREATE_FAIL.toString(), true);
		}

		return true;
	}
}
