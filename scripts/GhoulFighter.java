package scripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.*;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = { "Swooper" }, name = "Ghoul Fighter", version = 1.4, description = "Fights ghouls and banks!")
public class GhoulFighter extends Script implements PaintListener,
		ServerMessageListener {

	protected int startXPatt;
	protected int startXPstr;
	protected int startXPdef;
	private long starttime;
	private long mintime;
	private long hrtime;
	private long secsstart;
	private int width;
	private RSTile fightTile;
	private RSTile bankTile;
	private RSNPC ghoul;
	private RSGroundItem tileDraw;
	private int mousetimer = 0;
	private int cameratimer = 0;
	private int foodid = Integer.parseInt((String) JOptionPane.showInputDialog(
			null, "Enter Food ID:", null).trim());

	public boolean onStart() {
		randomDestTiles();
		startXPatt = skills.getCurrentExp(Skills.ATTACK);
		startXPstr = skills.getCurrentExp(Skills.STRENGTH);
		startXPdef = skills.getCurrentExp(Skills.DEFENSE);
		starttime = System.currentTimeMillis();
		mintime = System.currentTimeMillis();
		hrtime = System.currentTimeMillis();
		mouse.setSpeed(7);
		return true;
	}

	public boolean readyToFight() {
		if (inventory.contains(foodid)) {
			return true;
		} else {
			return false;
		}
	}

	public void running() {
		if (walking.getEnergy() > random(40, 80) && !walking.isRunEnabled()) {
			walking.setRun(true);
		}
	}

	public boolean atFightZone() {
		RSArea fightZone = new RSArea(3415, 3473, 3443, 3454);
		if (fightZone.contains(getMyPlayer().getLocation())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean atBankZone() {
		RSArea bankZone = new RSArea(3509, 3479, 3512, 3480);
		if (bankZone.contains(getMyPlayer().getLocation())) {
			return true;
		} else {
			return false;
		}
	}

	public void randomDestTiles() {
		bankTile = new RSTile(random(3509, 3512), random(3480, 3479));
		fightTile = new RSTile(random(3428, 3434), random(3467, 3463));
	}

	public void walkToFight() {
		running();
		randomDestTiles();
		// RSTile[] startPath = walking.findPath(fightTile);
		// RSTile[] randomPath = walking.randomizePath(startPath, 1, 1);
		while (calc.distanceTo(fightTile) > 6 && isActive()) {
			// RSTile nextTile = walking.nextTile(randomPath);
			walking.walkTo(walking.getClosestTileOnMap(fightTile));
			sleep(750, 1500);
		}
		walking.walkTileOnScreen(fightTile);
	}

	public void walkToBank() {
		running();
		randomDestTiles();
		// RSTile[] startPath = walking.findPath(bankTile);
		// RSTile[] randomPath = walking.randomizePath(startPath, 1, 1);
		while (calc.distanceTo(bankTile) > 5 && isActive()) {
			// /RSTile nextTile = walking.nextTile(randomPath);
			walking.walkTo(walking.getClosestTileOnMap(bankTile));
			sleep(750, 1200);
		}
		walking.walkTileOnScreen(bankTile);
		sleep(500);
	}

	public void bank() {
		RSNPC banker = npcs.getNearest(Bank.BANKERS);
		if (inventory.getCount(foodid) < 20) {
			if (!bank.isOpen()) {
				banker.doAction("Bank Banker");
				sleep(1000, 1750);
			}
			if (inventory.getCount() > 0) {
				bank.depositAll();
				sleep(500, 750);
			}
			if (bank.isOpen() && (bank.getItem(foodid) == null)) {
				log("Out of Food");
				stopScript(false);
			}
			bank.withdraw(foodid, 0);
			sleep(500, 750);
			bank.close();
		}
	}

	public void pickupCharms() {
		RSGroundItem cScroll = groundItems.getNearest(6799);
		tileDraw = cScroll;
		if (inventory.isFull() && cScroll != null) {
			eat();
			pickupCharms();
		}
		if (cScroll != null) {
			if (cScroll.isOnScreen()) {
				parseClick(cScroll, "Take", 20);
				sleep(1000);
				while (getMyPlayer().isMoving()) {
					sleep(100);
				}
			}
			if (calc.distanceTo(cScroll.getLocation()) < 12
					&& !cScroll.isOnScreen()) {
				walking.walkTo(cScroll.getLocation());
				sleep(1000);
				while (getMyPlayer().isMoving() && isActive()) {
					sleep(100);
				}
				pickupCharms();
			}
		}
		RSGroundItem effigy = groundItems.getNearest(6799);
		tileDraw = effigy;
		if (inventory.isFull() && effigy != null) {
			eat();
			pickupCharms();
		}
		if (effigy != null) {
			if (effigy.isOnScreen()) {
				parseClick(effigy, "Take", 20);
				sleep(1000);
				while (getMyPlayer().isMoving() && isActive()) {
					sleep(100);
				}
			}
			if (calc.distanceTo(effigy.getLocation()) < 12
					&& !effigy.isOnScreen()) {
				walking.walkTo(effigy.getLocation());
				sleep(1000);
				while (getMyPlayer().isMoving() && isActive()) {
					sleep(100);
				}
				pickupCharms();
			}
		}
		if (!inventory.isFull() || inventory.contains(12160)) {
			RSGroundItem crimsonCharm = groundItems.getNearest(12160);
			tileDraw = crimsonCharm;
			if (crimsonCharm != null) {
				if (crimsonCharm.isOnScreen()) {
					parseClick(crimsonCharm, "Take", 20);
					sleep(1000);
					while (getMyPlayer().isMoving() && isActive()) {
						sleep(100);
					}
				}
				if (calc.distanceTo(crimsonCharm.getLocation()) < 12
						&& !crimsonCharm.isOnScreen()) {
					walking.walkTo(crimsonCharm.getLocation());
					sleep(1000);
					while (getMyPlayer().isMoving() && isActive()) {
						sleep(100);
					}
					pickupCharms();
				}
			}
		}
		if (!inventory.isFull() || inventory.contains(12158)) {
			RSGroundItem goldCharm = groundItems.getNearest(12158);
			tileDraw = goldCharm;
			if (goldCharm != null) {
				if (goldCharm.isOnScreen()) {
					parseClick(goldCharm, "Take", 20);
					sleep(1000);
					while (getMyPlayer().isMoving() && isActive()) {
						sleep(100);
					}
				} else {
					if (calc.distanceTo(goldCharm.getLocation()) < 12
							&& !goldCharm.isOnScreen()) {
						walking.walkTo(goldCharm.getLocation());
						sleep(1000);
						while (getMyPlayer().isMoving() && isActive()) {
							sleep(100);
						}
						pickupCharms();
					}
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fight() {

		Filter freeghoultoattack = new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				return !npc.isInCombat() && npc.getName().equals("Ghoul")
						&& npc.getHPPercent() > 80;
			}
		};

		ghoul = npcs.getNearest(freeghoultoattack);

		if (ghoul.isOnScreen()) {
			modelClick(ghoul.getModel(), "Attack Ghoul (level: 42)");
		} else {
			walking.walkTo(ghoul.getLocation());
			sleep(1750, 2000);
		}
		sleep(4500, 5000);
		Timer sTimer = new Timer(50000);
		sTimer.reset();
		while (((getMyPlayer().getInteracting() != null && sTimer.isRunning()) || getMyPlayer().isMoving())
				&& (ghoul.isValid() && ghoul.getAnimation() != 836) && isActive()) {
			if (mousetimer > random(60, 70)) {
				mouse.moveSlightly();
				mousetimer = 0;
			}
			if (cameratimer > random(120, 140)) {
				camera.moveRandomly(2500);
				cameratimer = 0;
			}
			if (getMyPlayer().getHPPercent() < 75) {
				eat();
			}
			mousetimer++;
			cameratimer++;
			sleep(100);
		}
	}

	public void eat() {
		if (getMyPlayer().getHPPercent() < 75 && readyToFight()) {
			RSItem invfood = inventory.getItem(foodid);
			invfood.doClick(true);
			sleep(3500, 4500);
		}
	}

	public int loop() {
		try {
			if (readyToFight()) {
				if (!atFightZone()) {
					walkToFight();
				} else {
					eat();
					fight();
					pickupCharms();
				}
			} else {
				if (!atBankZone()) {
					walkToBank();
				} else {
					bank();
				}
			}
		} catch (Exception x) {
		}
		return 0;
	}

	public void onRepaint(Graphics g) {
		long xpperhr = 0;
		long secs = (System.currentTimeMillis() - mintime) / 1000;
		long mins = ((System.currentTimeMillis() - hrtime) / 60000);
		long hrs = (secsstart / 3600);

		Font myfont = new Font("sansserif", Font.PLAIN, 13);
		g.setFont(myfont);
		FontMetrics fm = g.getFontMetrics(myfont);

		int CurrentXPatt = skills.getCurrentExp(Skills.ATTACK) - startXPatt;
		int CurrentXPstr = skills.getCurrentExp(Skills.STRENGTH) - startXPstr;
		int CurrentXPdef = skills.getCurrentExp(Skills.DEFENSE) - startXPdef;
		int[] CurrentXPArray = { CurrentXPatt, CurrentXPstr, CurrentXPdef };
		Arrays.sort(CurrentXPArray);
		int CurrentXP = CurrentXPArray[CurrentXPArray.length - 1];

		int varwidth1 = fm.stringWidth("TIME RUNNING: " + hrs + ":" + mins
				+ ":" + secs);
		int varwidth2 = fm.stringWidth("EXP/HR: " + xpperhr);
		int varwidth3 = fm.stringWidth("EXP GAINED" + CurrentXP);
		int[] vararray = { varwidth1, varwidth2, varwidth3 };
		Arrays.sort(vararray);
		int varwidth = vararray[vararray.length - 1];

		secsstart = (System.currentTimeMillis() - starttime) / 1000;

		if (secs >= 60) {
			mintime = System.currentTimeMillis();
		}

		if (mins >= 60) {
			hrtime = System.currentTimeMillis();
		}
		if ((mins > 0 || hrs > 0 || secs > 0) && CurrentXP > 0) {
			xpperhr = CurrentXP * 3600 / secsstart;
		}

		if (varwidth > 150) {
			width = varwidth + 8;
		} else {
			width = 158;
		}
		g.setColor(new Color(72, 209, 204, 120));
		g.fillRect(5, 273, width, 61);
		g.setColor(Color.blue);
		g.drawRect(5, 273, width, 61);
		g.setColor(Color.black);
		g.drawString("Swooper's Ghoul Fighter", 9, 287);
		g.drawString("TIME RUNNING: " + hrs + ":" + mins + ":" + secs, 9, 301);
		g.drawString("EXP/HR: " + xpperhr, 9, 315);
		g.drawString("EXP GAINED: " + CurrentXP, 9, 329);

		if (ghoul != null) {
			// Model Paint By Kimbers
			Polygon[] model = ghoul.getModel().getTriangles();
			int i = 0;
			for (Polygon p : model) {
				if (i >= 1){
				g.setColor(new Color(72, 209, 204, 100));
				g.drawPolygon(p);
				i=-5;
				}
				i++;
			}
		}
	}

	public void serverMessageRecieved(ServerMessageEvent e) {
		if (e.getMessage().toString().contains("advanced a Constitution level")) {
			log("Leveled up Constitution!");
		}
		if (e.getMessage().toString().contains("advanced an Attack level")) {
			log("Leveled up Attack!");
		}
		if (e.getMessage().toString().contains("advanced a Strength level")) {
			log("Leveled up Strength!");
		}
		if (e.getMessage().toString().contains("advanced a Defence level")) {
			log("Leveled up Defense!");
		}
	}

	public void modelClick(RSModel myModel, String actionMsg) {
		if (myModel != null) {
			boolean complete = false;
			while (!complete && isActive()) {
				Point modelPoint = myModel.getPoint();
				mouse.move(modelPoint);
				String[] menuActions = menu.getItems();
				for (String y : menuActions) {
					if (y.contains(actionMsg)) {
						menu.doAction(actionMsg);
						complete = true;
					}
				}
			}
		}
	}

	public void parseClick(RSGroundItem myItem, String useTxt, int searchRadius) {
		boolean complete = false;
		Timer safetyTimer = new Timer(15000);
		safetyTimer.reset();
		while (!complete && safetyTimer.isRunning() && isActive()) {
			if (myItem.isOnScreen()) {
				if (menu.contains(useTxt)) {
					mouse.click(true);
					complete = true;
				} else {
					mouse.move(calc.tileToScreen(myItem.getLocation()), 12, 12);
					mouse.moveRandomly(searchRadius);
					sleep(400);
				}
			}
		}
	}
}
