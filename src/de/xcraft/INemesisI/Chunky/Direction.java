package de.xcraft.INemesisI.Chunky;

import org.bukkit.block.BlockFace;

public enum Direction {
	FRONT(0),
	FRONTRIGHT(1),
	RIGHT(2),
	BACKRIGHT(3),
	BACK(4),
	BACKLEFT(5),
	LEFT(6),
	FRONTLEFT(7);

	public final int value;

	Direction(int value) {
		this.value = value;
	}

	public int getModX() {
		if (value == 5 || value == 6 || value == 7) {
			return -1;
		} else if (value == 1 || value == 2 || value == 3) {
			return 1;
		} else
			return 0;
	}

	public int getModZ() {
		if (value == 7 || value == 0 || value == 1) {
			return -1;
		} else if (value == 5 || value == 4 || value == 3) {
			return 1;
		} else
			return 0;
	}

	public Direction getDirectionInFace(BlockFace face) {
		int v = value;
		switch (face) {
		case NORTH:
			v += 0;
			break;
		case EAST:
			v += 2;
			break;
		case SOUTH:
			v += 4;
			break;
		case WEST:
			v += 6;
			break;

		default:
			break;
		}
		v %= 8;
		for (Direction d : Direction.values()) {
			if (d.value == v)
				return d;
		}
		return null;
	}
}