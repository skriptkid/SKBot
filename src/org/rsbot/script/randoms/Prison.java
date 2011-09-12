package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.*;

/*
 * Written by Iscream(Feb 4, 2010)
 * Updated by Iscream(Feb 17, 2010)
 * Updated by zzSleepzz(Mar 1, 2010 to remove false positives)
 * Updated by Iscream(Apr 15, 2010)
 * Updated by Iscream(Apr 23, 2010)
 * Updated by NoEffex(Nov 25, 2010 to convert to model checking)
 * Updated by Swipe && IronHide (Sept, 8, 2011)
 */
@ScriptManifest(authors = {"Iscream, Swipe"}, name = "PrisonPete", version = 1.6)
public class Prison extends Random {

	private static final int PRISON_MATE = 3118, LEVER_ID = 10817,
			DOOR_KEY = 6966;

	private int unlocked, state = 0;
	private RSNPC balloonToPop;
	private RSNPC PrisonPete;
	private boolean talkedtopete = false;
	private boolean hasKey = false;
	private boolean taskDone = false;

	private static class Balloons {
		static final short[] FAT = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1,
				2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7, 2, 2, 8, 8, 5, 5, 9, 9, 10,
				11, 11, 15, 16, 17, 13, 14, 20, 20, 20, 20, 20, 21, 21, 21, 22,
				22, 23, 23, 24, 24, 24, 25, 25, 26, 26, 26, 27, 28, 29, 29, 30,
				30, 31, 32, 33, 33, 36, 36, 36, 36, 36, 37, 37, 37, 38, 38, 39,
				39, 40, 40, 41, 42, 45, 45, 45, 45, 45, 46, 46, 46, 46, 46, 46,
				46, 46, 47, 47, 48, 48, 49, 49, 50, 51, 51, 52, 52, 47, 47, 53,
				53, 50, 50, 54, 54, 55, 22, 22, 59, 60, 61, 57, 58, 64, 64, 64,
				64, 64, 65, 65, 65, 65, 65, 65, 65, 65, 66, 66, 67, 67, 68, 68,
				43, 69, 69, 70, 70, 66, 66, 71, 71, 43, 43, 72, 72, 73, 74, 74,
				78, 79, 80, 76, 77, 83, 83, 83, 83, 83, 84, 84, 84, 85, 85, 86,
				86, 87, 87, 89, 90, 92, 92, 92, 92, 93, 93, 94, 94, 95, 5, 98,
				98, 98, 98, 98, 99, 99, 99, 22, 22, 100, 100, 101, 101, 101,
				102, 102, 103, 103, 103, 104, 105, 106, 106, 107, 107, 108,
				109, 110, 110, 113, 113, 113, 113, 113, 114, 114, 114, 22, 22,
				115, 115, 116, 116, 116, 117, 117, 118, 118, 118, 119, 120,
				121, 121, 122, 122, 123, 124, 125, 125, 128, 128, 128, 128,
				128, 129, 129, 129, 22, 22, 130, 130, 131, 131, 131, 132, 132,
				133, 133, 133, 134, 135, 136, 136, 137, 137, 138, 32, 139, 139};
		static final short[] HORNED = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1,
				2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7, 2, 2, 8, 8, 5, 5, 9, 9, 10,
				11, 11, 15, 16, 17, 13, 14, 20, 20, 20, 20, 20, 21, 21, 21, 22,
				22, 23, 23, 24, 24, 24, 25, 25, 26, 26, 26, 27, 28, 29, 29, 30,
				30, 31, 32, 33, 33, 36, 36, 36, 36, 36, 37, 37, 37, 38, 38, 39,
				39, 40, 40, 22, 41, 41, 42, 43, 44, 47, 47, 47, 47, 47, 48, 48,
				48, 48, 48, 48, 48, 48, 49, 49, 50, 50, 51, 51, 52, 53, 53, 54,
				54, 49, 49, 55, 55, 52, 52, 56, 56, 57, 22, 22, 61, 62, 63, 59,
				60, 66, 66, 66, 66, 66, 67, 67, 67, 67, 67, 67, 67, 67, 68, 68,
				69, 69, 70, 70, 43, 71, 71, 72, 72, 68, 68, 73, 73, 43, 43, 74,
				74, 75, 76, 76, 80, 81, 82, 78, 79, 85, 85, 85, 85, 85, 86, 86,
				86, 87, 87, 88, 88, 89, 89, 90, 91, 91, 92, 43, 93, 96, 96, 96,
				96, 97, 97, 98, 98, 99, 5, 102, 102, 102, 102, 102, 102, 103,
				103, 103, 103, 104, 104, 105, 105, 106, 106, 106, 107, 107,
				108, 109, 109, 110, 110, 110, 111, 112, 113, 117, 115, 119,
				119, 119, 119, 119, 119, 120, 120, 120, 120, 121, 121, 122,
				122, 123, 123, 123, 124, 124, 125, 126, 126, 127, 127, 127,
				128, 129, 130, 134, 132, 136, 136, 136, 136, 136, 137, 137,
				137, 22, 22, 138, 138, 139, 139, 139, 140, 140, 141, 141, 141,
				142, 143, 144, 144, 145, 145, 146, 147, 148, 148, 151, 151,
				151, 151, 151, 152, 152, 152, 22, 22, 153, 153, 154, 154, 154,
				155, 155, 156, 156, 156, 157, 158, 159, 159, 160, 160, 161,
				162, 163, 163};
		static final short[] SKINNY_BENT_TAIL = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
				1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7, 2, 2, 8, 8, 5, 5, 9,
				9, 10, 11, 11, 15, 16, 17, 13, 14, 20, 20, 20, 20, 20, 21, 21,
				21, 22, 22, 23, 23, 24, 24, 24, 25, 25, 26, 26, 26, 27, 28, 29,
				29, 30, 30, 31, 32, 33, 33, 36, 36, 36, 36, 36, 37, 37, 37, 38,
				38, 39, 39, 40, 40, 22, 41, 41, 42, 43, 44, 47, 47, 47, 47, 47,
				48, 48, 48, 48, 48, 48, 48, 48, 49, 49, 50, 50, 51, 51, 52, 53,
				53, 54, 54, 49, 49, 55, 55, 52, 52, 56, 56, 57, 22, 22, 61, 62,
				63, 59, 60, 66, 66, 66, 66, 66, 67, 67, 67, 67, 67, 67, 67, 67,
				68, 68, 69, 69, 70, 70, 43, 71, 71, 72, 72, 68, 68, 73, 73, 43,
				43, 74, 74, 75, 76, 76, 80, 81, 82, 78, 79, 85, 85, 85, 85, 85,
				86, 86, 86, 87, 87, 88, 88, 89, 89, 90, 91, 91, 92, 43, 93, 96,
				96, 96, 96, 96, 97, 97, 97, 98, 98, 99, 99, 100, 100, 5, 101,
				101, 102, 103, 104, 107, 107, 107, 107, 107, 108, 108, 108,
				109, 109, 110, 110, 111, 111, 103, 112, 112, 113, 114, 115};
		static final short[] SKINNY_NORMAL_TAIL = {0, 0, 0, 0, 0, 1, 1, 1, 1,
				1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7, 2, 2, 8, 8, 5, 5,
				9, 9, 10, 11, 11, 15, 16, 17, 13, 14, 20, 20, 20, 20, 20, 21,
				21, 21, 22, 22, 23, 23, 24, 24, 24, 25, 25, 26, 26, 26, 27, 28,
				29, 29, 30, 30, 31, 32, 33, 33, 36, 36, 36, 36, 37, 37, 38, 38,
				39, 22, 42, 42, 42, 42, 42, 43, 43, 43, 43, 43, 43, 43, 43, 44,
				44, 45, 45, 46, 46, 47, 48, 48, 49, 49, 44, 44, 50, 50, 47, 47,
				51, 51, 52, 22, 22, 56, 57, 58, 54, 55, 61, 61, 61, 61, 61, 62,
				62, 62, 62, 62, 62, 62, 62, 63, 63, 64, 64, 65, 65, 38, 66, 66,
				67, 67, 63, 63, 68, 68, 38, 38, 69, 69, 70, 71, 71, 75, 76, 77,
				73, 74, 80, 80, 80, 80, 80, 81, 81, 81, 82, 82, 82, 83, 83, 83,
				84, 84, 5, 85, 85, 86, 86, 87, 87, 88, 89, 89, 90, 90, 92, 93,
				96, 96, 96, 96, 97, 97, 98, 98, 99, 100, 103, 103, 103, 103,
				104, 104, 105, 105, 106, 107};
	}

	@Override
	public boolean activateCondition() {
		if (game.isLoggedIn()) {
			PrisonPete = npcs.getNearest("Prison Pete");
			if (PrisonPete != null) {
				return objects.getNearest(LEVER_ID) != null;
			}
		}
		return false;
	}

	@Override
	public int loop() {
		if (npcs.getNearest("Prison Pete") == null) {
			return -1;
		}
		if (!talkedtopete) {
			camera.setPitch(true);
			if (camera.getAngle() < 175 || camera.getAngle() > 185) {
				camera.setAngle(random(175, 185));
				return random(500, 750);
			}
		}
		switch (state) {
			case 0:
				PrisonPete = npcs.getNearest("Prison Pete");
				if (anInterfaceContains("Lucky you!")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
						sleep(random(150,325));
					}
					state = 4;
					taskDone = true;
					return random(500, 600);
				}
				if (anInterfaceContains("should leave")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					unlocked = 10;
					return random(500, 600);
				}
				if (inventory.getCount(false) == 28
						&& !inventory.containsAll(DOOR_KEY)) {
					log("Not enough space for this random. Depositing an Item");
					final RSObject depo = objects.getNearest(32924);
					if (depo != null) {
						if (!calc.tileOnScreen(depo.getLocation())) {
							if (!walking.walkTileMM(depo.getLocation().randomize(2,
									2))) {
								walking.getPath(depo.getLocation().randomize(2, 2))
										.traverse();
								return random(500, 700);
							}
							return random(700, 1500);
						}
						camera.turnTo(depo, 20);
						if (depo.interact("Deposit")) {
							sleep(random(1600, 2000));
							if (getMyPlayer().isMoving()) {
								sleep(random(200, 500));
							}
							if (interfaces.get(Bank.INTERFACE_DEPOSIT_BOX)
									.isValid()) {
								sleep(random(700, 1200));
								interfaces.get(11).getComponent(17)
										.getComponent(random(16, 17))
										.interact("All");
								sleep(random(700, 1200));
								interfaces.getComponent(11, 15).doClick();
							}
							return random(400, 500);
						}
						return random(500, 800);
					}
					return random(500, 600);
				}

				if (getMyPlayer().isMoving()) {
					return random(250, 500);
				}
				if (anInterfaceContains("minute")) {
					talkedtopete = true;
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
						return random(500, 600);
					}
					return random(500, 600);
				}

				if (interfaces.get(228).isValid()
						&& interfaces.get(228).containsText("How do")) {
					interfaces.get(228).getComponent(3).doClick();
					return random(500, 600);
				}
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					return random(1000, 1200);
				}
				if (!talkedtopete && PrisonPete != null
						&& !interfaces.get(228).isValid()
						&& !interfaces.canContinue()) {
					if (!calc.tileOnScreen(PrisonPete.getLocation())) {
						walking.walkTileMM(PrisonPete.getLocation());
						return random(1000, 1400);
					}
					if (PrisonPete.interact("talk")) {
						return random(1500, 1600);
					} else {
						camera.turnTo(PrisonPete.getLocation());
						return random(500, 600);
					}
				}
				if (unlocked == 3) {
					state = 4;
					return random(250, 500);
				}
				if (unlocked <= 2 && talkedtopete) {
					state = 1;
					return random(500, 600);
				}
				return random(350, 400);

			case 1:
				// Figures out the balloon
				if (anInterfaceContains("Lucky you!")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					taskDone = true;
					return random(500, 600);
				}
				if (anInterfaceContains("should leave")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					unlocked = 10;
					return random(500, 600);
				}
				if (interfaces.get(273).getComponent(3).isValid()) {
					if (atLever()) {
						if (balloonToPop != null
								&& interfaces.get(273).getComponent(4)
								.interact("Close")) {
							state = 2;
							return random(800, 900);
						}
						return random(500, 700);
					}
				}
				final RSObject lever = objects.getNearest(LEVER_ID);
				if (lever != null && talkedtopete) {
					if (!calc.tileOnScreen(lever.getLocation())) {
						walking.walkTileMM(lever.getLocation());
						return random(1000, 1200);
					}
					if (!getMyPlayer().isMoving()
							&& calc.tileOnScreen(lever.getLocation())) {
						// if (tiles.doAction(lever.getLocation(), 0.5, 0.5, 170,
						// "Pull")) {
						if (lever.interact("Pull")) {
							sleep(random(1400, 1600));
							if (atLever()) {
								if (balloonToPop != null
										&& interfaces.get(273).getComponent(4)
										.interact("Close")) {
									state = 2;
									return random(800, 900);
								}
								return random(500, 700);
							}
							return random(500, 600);
						} else {
							camera.turnTo(lever.getLocation());
							return random(500, 600);
						}
					}
				}
				if (!talkedtopete) {
					state = 0;
					return random(500, 600);
				}
				return random(500, 600);
			case 2:
				// Finds animal and pops it
				if (anInterfaceContains("Lucky you!")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					taskDone = true;
					return random(500, 600);
				}
				if (anInterfaceContains("should leave")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					unlocked = 10;
					return random(500, 600);
				}
				if (getMyPlayer().isMoving()) {
					return random(250, 500);
				}
				if (balloonToPop == null && unlocked <= 2) {
					state = 1;
					return random(500, 700);
				}
				if (unlocked == 3) {
					state = 4;
				}

				if (!inventory.containsAll(DOOR_KEY)) {
					if (calc.tileOnScreen(balloonToPop.getLocation())) {
						balloonToPop.interact("Pop");
						return random(1200, 1800);
					} else {
						if (!getMyPlayer().isMoving()) {
							walking.walkTileMM(balloonToPop.getLocation()
									.randomize(2, 2));
							return random(500, 750);
						}
						return random(500, 750);
					}
				}
				if (inventory.contains(DOOR_KEY)) {
					hasKey = false;
					state = 3;
					return random(500, 700);
				}
				return random(350, 400);

			case 3:
				// Goes to pete
				PrisonPete = npcs.getNearest("Prison Pete");
				if (getMyPlayer().isMoving()) {
					return random(250, 500);
				}
				if (anInterfaceContains("Lucky you!")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					taskDone = true;
					return random(500, 600);
				}
				if (anInterfaceContains("should leave")) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					state = 4;
					unlocked = 10;
					return random(500, 600);
				}
				if (anInterfaceContains("you got all the keys")) {
					hasKey = true;
					unlocked = 5;
					state = 4;
					balloonToPop = null;
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
						return random(500, 600);
					}
					return random(250, 500);
				}
				if (anInterfaceContains("Hooray")) {
					hasKey = true;
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
						return random(500, 600);
					}
				}
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					return random(500, 600);
				}
				if (PrisonPete != null && !calc.tileOnScreen(PrisonPete.getLocation())
						&& !interfaces.get(243).isValid()) {
					walking.walkTileMM(PrisonPete.getLocation());
					return random(400, 600);
				}
				if (!inventory.contains(DOOR_KEY)
						&& npcs.getNearest(PRISON_MATE) != null
						&& unlocked <= 2 && hasKey) {
					unlocked++;
					state = 0;
					balloonToPop = null;
					return random(350, 400);
				}

				if (inventory.contains(DOOR_KEY) && !getMyPlayer().isMoving()) {
					inventory.getItem(DOOR_KEY).interact("Return");
					return random(1000, 2000);
				}
				if (!inventory.contains(DOOR_KEY)
						&& npcs.getNearest(PRISON_MATE) != null
						&& unlocked <= 2 && !hasKey) {
					state = 0;
					balloonToPop = null;
					return random(350, 400);
				}

				return random(350, 400);
			case 4:
				// exits
				final RSTile doorTile = new RSTile(2086, 4459);
				if (unlocked <= 2 && !taskDone) {
					state = 0;
					return random(500, 600);
				}
				if (!calc.tileOnScreen(doorTile)) {
					walking.walkTileMM(doorTile);
					return random(400, 500);
				}
				if (calc.tileOnScreen(doorTile)) {
					final RSObject gate = objects.getNearest(11177, 11178);
					if (gate != null) {
						gate.interact("Open");
					}
					// tiles.doAction(new RSTile(2085, 4459), 1, 0, 30, "Open");
					return random(500, 600);
				}
				return random(200, 400);
		}
		return random(200, 400);
	}

	@Override
	public void onFinish() {
		if (taskDone) {
			log.info("Failed to complete Prison Pete. Stopping now.");
			sleep(5000, 10000);
			stopScript(false);
		}
		unlocked = state = 0;
		balloonToPop = null;
		PrisonPete = null;
		talkedtopete = false;
		hasKey = false;
		taskDone = false;
	}

	short[] setItemIDs(final int b2p) {
		// sets the proper balloon id
		switch (b2p) {
			case 10749: // skinny bend at end of tail
				return Balloons.SKINNY_BENT_TAIL;
			case 10750: // long tail, no bend at end of tail
				return Balloons.SKINNY_NORMAL_TAIL;
			case 10751: // fatty
				return Balloons.FAT;
			case 10752: // horny
				return Balloons.HORNED;
		}
		return new short[]{};
	}

	boolean anInterfaceContains(final String s) {
		return (interfaces.getAllContaining(s).length > 0);
	}

	boolean atLever() {
		if (interfaces.get(273).getComponent(3).isValid()) {
			final Filter<RSModel> filter = RSModel
					.newVertexFilter(setItemIDs(interfaces.get(273)
							.getComponent(3).getModelID()));
			balloonToPop = npcs.getNearest(new Filter<RSNPC>() {
				public boolean accept(final RSNPC n) {
					return filter.accept(n.getModel());
				}
			});
			if (balloonToPop != null) {
				return true;
			}
		}
		return false;
	}

}
