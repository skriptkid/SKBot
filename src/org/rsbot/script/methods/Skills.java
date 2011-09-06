package org.rsbot.script.methods;

import org.rsbot.script.util.SkillData;

/**
 * This class is for all the skill calculations.
 * <p/>
 * Example usage: skills.getRealLevel(Skills.ATTACK);
 */
public class Skills extends MethodProvider {
	public static final String[] SKILL_NAMES = {"attack", "defence", "strength", "constitution", "range", "prayer",
			"magic", "cooking", "woodcutting", "fletching", "fishing", "firemaking", "crafting", "smithing", "mining",
			"herblore", "agility", "thieving", "slayer", "farming", "runecrafting", "hunter", "construction",
			"summoning", "dungeoneering", "-unused-"};

	/**
	 * A table containing the experiences that begin each level.
	 */
	public static final int[] XP_TABLE = {0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107,
			2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
			16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983,
			75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742,
			302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
			1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594,
			3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629,
			11805606, 13034431, 14391160, 15889109, 17542976, 19368992, 21385073, 23611006, 26068632, 28782069,
			31777943, 35085654, 38737661, 42769801, 47221641, 52136869, 57563718, 63555443, 70170840, 77474828,
			85539082, 94442737, 104273167};

	public static final int ATTACK = 0;
	public static final int DEFENSE = 1;
	public static final int STRENGTH = 2;
	public static final int CONSTITUTION = 3;
	public static final int RANGE = 4;
	public static final int PRAYER = 5;
	public static final int MAGIC = 6;
	public static final int COOKING = 7;
	public static final int WOODCUTTING = 8;
	public static final int FLETCHING = 9;
	public static final int FISHING = 10;
	public static final int FIREMAKING = 11;
	public static final int CRAFTING = 12;
	public static final int SMITHING = 13;
	public static final int MINING = 14;
	public static final int HERBLORE = 15;
	public static final int AGILITY = 16;
	public static final int THIEVING = 17;
	public static final int SLAYER = 18;
	public static final int FARMING = 19;
	public static final int RUNECRAFTING = 20;
	public static final int HUNTER = 21;
	public static final int CONSTRUCTION = 22;
	public static final int SUMMONING = 23;
	public static final int DUNGEONEERING = 24;

	public static final int INTERFACE_TAB_STATS = 320;
	public static final int INTERFACE_ATTACK = 1;
	public static final int INTERFACE_DEFENSE = 22;
	public static final int INTERFACE_STRENGTH = 4;
	public static final int INTERFACE_CONSTITUTION = 2;
	public static final int INTERFACE_RANGE = 46;
	public static final int INTERFACE_PRAYER = 70;
	public static final int INTERFACE_MAGIC = 87;
	public static final int INTERFACE_COOKING = 62;
	public static final int INTERFACE_WOODCUTTING = 102;
	public static final int INTERFACE_FLETCHING = 95;
	public static final int INTERFACE_FISHING = 38;
	public static final int INTERFACE_FIREMAKING = 85;
	public static final int INTERFACE_CRAFTING = 78;
	public static final int INTERFACE_SMITHING = 20;
	public static final int INTERFACE_MINING = 3;
	public static final int INTERFACE_HERBLORE = 30;
	public static final int INTERFACE_AGILITY = 12;
	public static final int INTERFACE_THIEVING = 54;
	public static final int INTERFACE_SLAYER = 112;
	public static final int INTERFACE_FARMING = 120;
	public static final int INTERFACE_RUNECRAFTING = 104;
	public static final int INTERFACE_HUNTER = 136;
	public static final int INTERFACE_CONSTRUCTION = 128;
	public static final int INTERFACE_SUMMONING = 144;
	public static final int INTERFACE_DUNGEONEERING = 152;

	/**
	 * Gets the index of the skill with a given name. This is not case
	 * sensitive.
	 *
	 * @param statName The skill's name.
	 * @return The index of the specified skill; otherwise -1.
	 */
	public static int getIndex(final String statName) {
		for (int i = 0; i < Skills.SKILL_NAMES.length; i++) {
			if (Skills.SKILL_NAMES[i].equalsIgnoreCase(statName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets the level at the given experience.
	 *
	 * @param exp The experience.
	 * @return The level based on the experience given.
	 * @see #XP_TABLE
	 */
	public static int getLevelAt(final int exp) {
		for (int i = Skills.XP_TABLE.length - 1; i > 0; i--) {
			if (exp > Skills.XP_TABLE[i]) {
				return i;
			}
		}
		return 1;
	}

	/**
	 * Gets the experience at the given level.
	 *
	 * @param lvl The level.
	 * @return The level based on the experience given.
	 */
	public static int getExpAt(final int lvl) {
		if (lvl > 120) {
			return 1;
		}
		return Skills.XP_TABLE[lvl - 1];
	}

	/**
	 * Gets the experience required for the given level.
	 *
	 * @param lvl The level.
	 * @return The level based on the experience given.
	 */
	public static int getExpRequired(final int lvl) {
		if (lvl > 120) {
			return 1;
		}
		return Skills.XP_TABLE[lvl];
	}

	/**
	 * Gets the skill name of an index.
	 *
	 * @param index The index.
	 * @return The name of the skill for that index.
	 */
	public static String getSkillName(final int index) {
		if (index > Skills.SKILL_NAMES.length - 1) {
			return null;
		}
		return Skills.SKILL_NAMES[index];
	}

	Skills(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Gets the current experience for the given skill.
	 *
	 * @param index The index of the skill.
	 * @return -1 if the skill is unavailable
	 */
	public int getCurrentExp(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		final int[] skills = methods.client.getSkillExperiences();

		if (index > skills.length - 1) {
			return -1;
		}

		return methods.client.getSkillExperiences()[index];
	}

	/**
	 * Gets the effective level of the given skill (accounting for temporary
	 * boosts and reductions).
	 *
	 * @param index The index of the skill.
	 * @return The current level of the given Skill.
	 */
	public int getCurrentLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		return methods.client.getSkillLevels()[index];
	}

	/**
	 * Gets the player's current level in a skill based on their experience in
	 * that skill.
	 *
	 * @param index The index of the skill.
	 * @return The real level of the skill.
	 * @see #getRealLevel(int)
	 */
	public int getRealLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		return Skills.getLevelAt(getCurrentExp(index));
	}

	/**
	 * Gets the percentage to the next level in a given skill.
	 *
	 * @param index The index of the skill.
	 * @return The percent to the next level of the provided skill or 0 if level
	 *         of skill is 99.
	 */
	public int getPercentToNextLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		final int lvl = getRealLevel(index);
		return getPercentToLevel(index, lvl + 1);
	}

	/**
	 * Gets the percentage to the a level in a given skill.
	 *
	 * @param index  The index of the skill.
	 * @param endLvl The level for the percent.
	 * @return The percent to the level provided of the provided skill or 0 if level
	 *         of skill is 99.
	 */
	public int getPercentToLevel(final int index, final int endLvl) {
		if (!isSkill(index)) {
			return -1;
		}
		final int lvl = getRealLevel(index);
		if (index == Skills.DUNGEONEERING && (lvl == 120 || endLvl > 120)) {
			return 0;
		} else if (lvl == 99 || endLvl > 99) {
			return 0;
		}
		final int xpTotal = Skills.XP_TABLE[endLvl] - Skills.XP_TABLE[lvl];
		if (xpTotal == 0) {
			return 0;
		}
		final int xpDone = getCurrentExp(index) - Skills.XP_TABLE[lvl];
		return 100 * xpDone / xpTotal;
	}

	/**
	 * Gets the maximum level of a given skill.
	 *
	 * @param index The index of the skill.
	 * @return The max level of the skill.
	 */
	public int getMaxLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		return methods.client.getSkillLevelMaxes()[index];
	}

	/**
	 * Gets the maximum experience of a given skill.
	 *
	 * @param index The index of the skill.
	 * @return The max experience of the skill.
	 */
	public int getMaxExp(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		return methods.client.getSkillExperiencesMax()[index];
	}

	/**
	 * Gets the experience remaining until reaching the next level in a given
	 * skill.
	 *
	 * @param index The index of the skill.
	 * @return The experience to the next level of the skill.
	 */
	public int getExpToNextLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		final int lvl = getRealLevel(index);
		return getExpToLevel(index, lvl + 1);
	}

	/**
	 * Gets the experience remaining until reaching the a level in a given
	 * skill.
	 *
	 * @param index  The index of the skill.
	 * @param endLvl The level for the experience remaining.
	 * @return The experience to the level provided of the skill.
	 */
	public int getExpToLevel(final int index, final int endLvl) {
		if (!isSkill(index)) {
			return -1;
		}
		final int lvl = getRealLevel(index);
		if (index == Skills.DUNGEONEERING && (lvl == 120 || endLvl > 120)) {
			return 0;
		} else if (lvl == 99 || endLvl > 99) {
			return 0;
		}
		return Skills.XP_TABLE[endLvl] - getCurrentExp(index);
	}

	/**
	 * Gets the time remaining until the next level.
	 *
	 * @param index The index of the skill.
	 * @param exp   The start Exp.
	 * @param time  The time the script has been running.
	 * @return The time till the next level of the skill.
	 */
	public long getTimeTillNextLevel(final int index, final int exp, final long time) {
		if (!isSkill(index)) {
			return -1;
		}
		final int level = getRealLevel(index);
		return getTimeTillLevel(index, exp, level + 1, time);
	}

	/**
	 * Gets the time remaining until the level provided.
	 *
	 * @param index  The index of the skill.
	 * @param exp    The start Exp.
	 * @param endLvl The level to get the time till for.
	 * @param time   The time the script has been running.
	 * @return The time till the level provided of the skill.
	 */
	public long getTimeTillLevel(final int index, final int exp, final int endLvl, final long time) {
		if (!isSkill(index)) {
			return -1;
		}
		final int level = getRealLevel(index);
		final int currentExp = getCurrentExp(index);
		if (index == Skills.DUNGEONEERING && level == 120) {
			return 0;
		} else if (level == 99) {
			return 0;
		}
		try {
			return time * getExpToLevel(index, endLvl) / (currentExp - exp);
		} catch (final Exception e) {
			return -1;
		}
	}

	/**
	 * Gets the number of actions needed until the next level.
	 *
	 * @param index The index.
	 * @param exp   The exp each action gives.
	 * @return How many you need to do until the next level.
	 */
	public int ammountTillNextLevel(final int index, final double exp) {
		if (!isSkill(index)) {
			return -1;
		}
		final int level = getRealLevel(index);
		return ammountTillLevel(index, exp, level + 1);
	}

	/**
	 * Gets the number of actions needed until the level provided.
	 *
	 * @param index The index.
	 * @param exp   The exp each action gives.
	 * @param lvl   The level to get the ammount till for.
	 * @return How many you need to do until leveling to the level provided.
	 */
	public int ammountTillLevel(final int index, final double exp, final int lvl) {
		if (!isSkill(index)) {
			return -1;
		}
		return getExpToLevel(index, lvl) != -1 ? (int) (getExpToLevel(index, lvl) / exp) : 0;
	}

	/**
	 * Gets the total/overall level.
	 *
	 * @return The total/overall level.
	 */
	public int getTotalLevel() {
		int total = 0;
		for (int i = 0; i < Skills.SKILL_NAMES.length - 1; i++) {
			total += getRealLevel(i);
		}
		return total;
	}

	/**
	 * Gets the percent to max level in the specified skill index
	 *
	 * @param index Skill index.
	 * @return Percent to level max level.
	 */
	public int getPercentToMaxLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		int lvl = 99;
		if (index == Skills.DUNGEONEERING) {
			lvl = 120;
		}
		return getPercentToLevel(index, lvl);
	}

	/**
	 * Gets the experience needed to the max level in the specified skill index
	 *
	 * @param index Skill index.
	 * @return Experience to level max level.
	 */
	public int getExpToMaxLevel(final int index) {
		if (!isSkill(index)) {
			return -1;
		}
		int lvl = 99;
		if (index == Skills.DUNGEONEERING) {
			lvl = 120;
		}
		return getExpToLevel(index, lvl);
	}

	/**
	 * Gets the time remaining until the max level.
	 *
	 * @param index The index of the skill.
	 * @param exp   The start Exp.
	 * @param time  The time the script has been running.
	 * @return The time till the max level of the skill.
	 */
	public long getTimeTillMaxLevel(final int index, final int exp, final long time) {
		if (!isSkill(index)) {
			return -1;
		}
		int lvl = 99;
		if (index == Skills.DUNGEONEERING) {
			lvl = 120;
		}
		return getTimeTillLevel(index, exp, lvl, time);
	}

	/**
	 * Gets the number of actions needed until the max level.
	 *
	 * @param index The index.
	 * @param exp   The exp each action gives.
	 * @return How many you need to do until the max level.
	 */
	public int ammountTillMaxLevel(final int index, final double exp) {
		if (!isSkill(index)) {
			return -1;
		}
		int lvl = 99;
		if (index == Skills.DUNGEONEERING) {
			lvl = 120;
		}
		return ammountTillLevel(index, exp, lvl);
	}

	/**
	 * Moves the mouse over a given component in the stats tab.
	 *
	 * @param component The component index.
	 * @return <tt>true</tt> if the mouse was moved over the given component
	 *         index.
	 */
	public boolean doHover(final int component) {
		methods.game.openTab(Game.Tab.STATS);
		sleep(random(10, 100));
		return methods.interfaces.getComponent(INTERFACE_TAB_STATS, component).doHover();
	}

	/**
	 * Checks if one of the given skills is boosted.
	 *
	 * @param index The index of the skill.
	 * @return <tt>true</tt> if one the given skills is boosted.
	 */
	public boolean isSkillBoosted(final int... index) {
		if (!isSkill(index)) {
			return false;
		}
		for (int i : index) {
			int realLevel = getRealLevel(i);
			if (realLevel > getMaxLevel(i)) {
				switch (i) {
					case Skills.DUNGEONEERING:
						realLevel = 120;
						break;
					default:
						realLevel = 99;
						break;
				}
			}
			if (realLevel == getCurrentLevel(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if this index is not out of range.
	 *
	 * @param index The index of the skill.
	 * @return <tt>true</tt> if this index is not out of range.
	 */
	private boolean isSkill(final int... index) {
		for (int i : index) {
			if (i > Skills.SKILL_NAMES.length - 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets a skill data class.
	 *
	 * @return The <tt>SkillData</tt> class.
	 */
	public SkillData getSkillDataInstance() {
		return new SkillData(methods, null);
	}

	/**
	 * Gets the current experience for all skills.
	 *
	 * @return int array of all skill experiences
	 */
	public int[] getCurrentExp() {
		return methods.client.getSkillExperiences();
	}
}
