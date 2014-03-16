package de.xcraft.INemesisI.Chunky;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import de.xcraft.INemesisI.Library.XcraftPlugin;
import de.xcraft.INemesisI.Library.Message.Messenger;

public enum Msg {
	//@formatter:off
	//general stuff
	NONE("none"),
	ERR_NOT_FROM_CONSOLE("&cYou can't use this command from the console."),
	ERR_NOT_OWNER("&cERROR: You need to be a owner of that region!"),
	ERR_NOT_ENOUGH_MONEY("&cYou dont have enough money to buy that!"),
	ERR_WORLD_NOT_ACTIVE("&cYou cant do that in this world!"),
	ERR_CHUNK_ALREADY_BOUGHT("&cThis chunk has already been bought!"),
	ERR_CHUNK_NOT_BOUGHT("You have not bought this chunk. You cant do this"),
	ERR_REGION_OVERLAPPING("&cThere is a region overlapping this Chunk, which does not belong to you (Region: $REGION$, Owners: $NAME$)"),
	ERR_REGION_NEARBY("&cThere is a region from another player nearby. You cannot buy a chunk here! (Region: $REGION$, Owners: $NAME$)"),
	ERR_PLAYER_NEVER_SEEN("&cA player named $NAME$ was never on this server!"),
	//COMMAND MESSAGES
	COMMAND_CREATE("Creates a new ChunkRegion. (CAUTION: Costs $FEE$)"),
	COMMAND_CREATE_SUCCESSFULL("&a You successfully created the region \"$REGION$\" for $FEE$!"),
	COMMAND_CREATE_FAIL("&cThere is another region nearby. You cant create a region here!"),
	COMMAND_CREATE_REFER_ADDCHUNK("&cThere is a region from you next to you. You probably wanted to use /chunkregion addchunk"),
	//
	COMMAND_DELETE("Deletes a ChunkRegion. (Gives you $FEE$ back)"),
	COMMAND_DELETE_SUCCESSFULL("&aYou successfully deleted the region $REGION$"),
	COMMAND_DELETE_REGION_NOT_FOUND("&cCould not find a region with that name!"),
	COMMAND_DELETE_NOT_IN_REGION("&cYou must stand in that region, to delete it!"),
	//
	COMMAND_ADDCHUNK("Adds the chunk you are standing on your ChunkRegion"),
	COMMAND_ADDCHUNK_SUCESSFULL("&aYou have bought a Chunk for region $REGION$ for $FEE$!"),
	COMMAND_ADDCHUNK_FAIL("&cThere is more than one region nearby. You cant buy a chunk here!"),
	COMMAND_ADDCHUNK_REFER_CREATE("&cCould not find a region belonging to you nearby. Use /chunkregion create to create a new region!"),
	//
	COMMAND_REMOVECHUNK("Removes the chunk you are standing on from you ChunkRegion"),
	COMMAND_REMOVECHUNK_SUCESSFULL("&aSuccessuflly sold the Chunk"),
	COMMAND_REMOVE_NOT_POSSIBLE("&cYou cannot remove this chunk!"),
	COMMAND_REMOVECHUNK_FAIL("&cCould not find a chunk belonging to you!"),
	//
	COMMAND_ADDMEMBER("Adds a member to the region you are standing in"),
	COMMAND_ADDMEMBER_SUCCESSFULL("&aSuccessfully added $NAME$ as a member to your region $REGION$"),
	COMMAND_ADDMEMBER_FAIL("&cFailed adding $NAME$ as a member to your region $REGION$"),
	COMMAND_REMOVEMEMBER("Removes a member from the region you are standing on"),
	COMMAND_REMOVEMEMBER_SUCCESSFULL("&aSuccessfully removed $NAME$ as a member from your region $REGION$"),
	COMMAND_REMOVEMEMBER_FAIL("&cFailed removing $NAME$ as a member from your region $REGION$"),
	//
	COMMAND_ADDOWNER("Adds a owner to the region you are standing in"),
	COMMAND_ADDOWNER_SUCCESSFULL("&aSuccessfully added $NAME$ as a owner to your region $REGION$"),
	COMMAND_ADDOWNER_FAIL("&cFailed adding $NAME$ as a owner to your region $REGION$"),
	COMMAND_REMOVEOWNER("Removes a owner from the region you are standing on"),
	COMMAND_REMOVEOWNER_SUCCESSFULL("&aSuccessfully removed $NAME$ as a owner from your region $REGION$"),
	COMMAND_REMOVEOWNER_FAIL("&cFailed removing $NAME$ as a owner from your region $REGION$"),
	//
	COMMAND_INFO("Lists information about the region you are standing in"),
	COMMAND_INFO_HEADER("&9Region info for region \"&6$REGION$&9\""),
	COMMAND_INFO_OWNER("&9Owners: &e$NAME$"),
	COMMAND_INFO_MEMBER("&9Members: &e$NAME$"),
	COMMAND_INFO_SIZE("&9Chunks: &6$MISC$&9, price for next Chunk: &6$FEE$"),
	//
	COMMAND_LISTREGION("Lists all chunks at your location."),
	
	;

	//@formatter:on

	public enum Replace {
		$NAME$("Name of a Player"),
		$MESSAGE$("Message provided in a command"),
		$FEE$("Fee of a command"),
		$REGION$("Name of a region"),
		$MISC$("");

		private String key;

		Replace(String key) {
			this.set(key);
		}

		private void set(String output) {
			key = output;
		}

		private String get() {
			return key;
		}

		public static Replace NAME(String replace) {
			$NAME$.set(replace);
			return $NAME$;
		}

		public static Replace MESSAGE(String replace) {
			$MESSAGE$.set(replace);
			return $MESSAGE$;
		}

		public static Replace FEE(String replace) {
			$FEE$.set(replace);
			return $FEE$;
		}

		public static Replace REGION(String replace) {
			$REGION$.set(replace);
			return $REGION$;
		}

		public static Replace MISC(String replace) {
			$MISC$.set(replace);
			return $MISC$;
		}
	}

	private String msg;

	Msg(String msg) {
		this.set(msg);
	}

	private void set(String output) {
		msg = output;
	}

	private String get() {
		return msg;
	}

	@Override
	public String toString() {
		String message = msg.replaceAll("&([0-9a-z])", "\u00a7$1");
		message = message.replace("\\n", "\n");
		return message;
	}

	public String toString(Replace r1) {
		String message = toString();
		message = message.replace(r1.name(), r1.get());
		return message;
	}

	public String toString(Replace r1, Replace r2) {
		String message = toString();
		message = message.replace(r1.name(), r1.get());
		message = message.replace(r2.name(), r2.get());
		return message;
	}

	public String toString(Replace[] repl) {
		String message = toString();
		for (Replace r : repl) {
			message = message.replace(r.name(), r.get());
		}
		return message;
	}

	public static void init(XcraftPlugin plugin) {
		File msgFile = new File(plugin.getDataFolder(), "locale.yml");
		if (!load(msgFile))
			return;
		parseFile(msgFile);
	}

	private static boolean load(File file) {
		if (file.exists())
			return true;
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			for (Msg m : Msg.values()) {
				String msg = m.get();
				if (msg.contains("\n")) {
					msg = msg.replace("\n", "\\n");
				}
				bw.write(m.name() + ": " + msg);
				bw.newLine();
			}
			bw.close();
			return true;
		} catch (Exception e) {
			Messenger.warning("Couldn't initialize locale.yml. Using defaults.");
			return false;
		}
	}

	private static void parseFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			// Check for BOM character.
			br.mark(1);
			int bom = br.read();
			if (bom != 65279) {
				br.reset();
			}
			String s;
			while ((s = br.readLine()) != null) {
				process(s);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			Messenger.warning("Problem with locale.yml. Using defaults.");
			return;
		}
	}

	/**
	 * Helper-method for parsing the strings from the announcements-file.
	 */
	private static void process(String s) {
		String[] split = s.split(": ", 2);
		try {
			Msg msg = Msg.valueOf(split[0]);
			msg.set(split[1]);
		} catch (Exception e) {
			Messenger.warning(split[0] + " is not a valid key. Check locale.yml.");
			return;
		}
	}

}
