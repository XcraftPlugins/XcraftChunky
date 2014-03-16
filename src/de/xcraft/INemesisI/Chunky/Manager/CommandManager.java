package de.xcraft.INemesisI.Chunky.Manager;

import net.milkbowl.vault.economy.Economy;
import de.xcraft.INemesisI.Chunky.Msg;
import de.xcraft.INemesisI.Chunky.Msg.Replace;
import de.xcraft.INemesisI.Chunky.XcraftChunky;
import de.xcraft.INemesisI.Chunky.Commands.AddChunkCommand;
import de.xcraft.INemesisI.Chunky.Commands.AddOwnerCommand;
import de.xcraft.INemesisI.Chunky.Commands.CreateRegionCommand;
import de.xcraft.INemesisI.Chunky.Commands.DeleteRegionCommand;
import de.xcraft.INemesisI.Chunky.Commands.InfoCommand;
import de.xcraft.INemesisI.Chunky.Commands.ListRegionsCommand;
import de.xcraft.INemesisI.Chunky.Commands.RemoveChunkCommand;
import de.xcraft.INemesisI.Chunky.Commands.RemoveMemberCommand;
import de.xcraft.INemesisI.Chunky.Commands.RemoveOwnerCommand;
import de.xcraft.INemesisI.Library.Manager.XcraftCommandManager;

public class CommandManager extends XcraftCommandManager {

	public CommandManager(XcraftChunky plugin) {
		super(plugin);
	}

	@Override
	protected void registerCommands() {
		this.registerBukkitCommand("chunkregion");
		ConfigManager cManager = ((ConfigManager) plugin.getConfigManager());
		Economy eco = ((XcraftChunky) plugin).getEconomy();
		this.registerCommand(new AddChunkCommand(this, "chunkregion", "addchunk", "a.*", "", //
				Msg.COMMAND_ADDCHUNK.toString(Replace.FEE(cManager.ChunkEquation)), "xcraftchunky.addchunk"));

		this.registerCommand(new RemoveChunkCommand(this, "chunkregion", "removechunk", "r.*", "", //
				Msg.COMMAND_REMOVECHUNK.toString(), "xcraftchunky.removechunk"));

		this.registerCommand(new CreateRegionCommand(this, "chunkregion", "createregion", "c.*", "", //
				Msg.COMMAND_CREATE.toString(Replace.FEE(eco.format(Math.abs(cManager.RegionCreate)))), "xcraftchunky.create"));

		this.registerCommand(new DeleteRegionCommand(this, "chunkregion", "deleteregion", "d.*", "<Name>", //
				Msg.COMMAND_DELETE.toString(Replace.FEE(eco.format(Math.abs(cManager.RegionDelete)))), "xcraftchunky.delete"));

		this.registerCommand(new InfoCommand(this, "chunkregion", "info", "i.*", "", Msg.COMMAND_INFO.toString(), "xcraftchunky.info"));

		this.registerCommand(new ListRegionsCommand(this, "chunkregion", "listregion", "l.*", "", //
				Msg.COMMAND_LISTREGION.toString(), "xcraftchunky.list"));

		this.registerCommand(new AddOwnerCommand(this, "chunkregion", "addowner", "addo.*", "<Name>", //
				Msg.COMMAND_ADDOWNER.toString(), "xcraftchunky.addowner"));

		this.registerCommand(new RemoveOwnerCommand(this, "chunkregion", "removeowner", "removeo.*", "<Name>", //
				Msg.COMMAND_REMOVEOWNER.toString(), "xcraftchunky.removeowner"));

		this.registerCommand(new AddOwnerCommand(this, "chunkregion", "addmember", "addm.*", "<Name>", //
				Msg.COMMAND_ADDMEMBER.toString(), "xcraftchunky.addmember"));

		this.registerCommand(new RemoveMemberCommand(this, "chunkregion", "removemember", "removem.*", "<Name>", //
				Msg.COMMAND_REMOVEMEMBER.toString(), "xcraftchunky.removemember"));
	}

}
