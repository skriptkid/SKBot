import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

/**
 * Change log:
 * v1.02: Includes click here to continue hotfix.
 * v1.01: Prioritize looting over combat. Safespotting + looting is now possible. Implemented new LoopAction structure internally.
 * v1.00: Milestone release.
 * 		Added:
 * 			-Safespot ability
 * 			-Central clicking
 * 			-Clicking continue
 * 			-Antiban - performs camera + mouse at same time! (sometimes)
 * 			-Declared stable
 * v0.96: Hopefully finally fixed food
 * v0.95: Small error, caused null pointer
 * v0.94: Fixed eating.
 * v0.93: Hide paint by clicking it.
 * v0.92: Loot support, mainly. Many small changes.
 * v0.91: Oops, forgot to add mouse speed settings!
 * v0.9: Initial release
 */
@ScriptManifest(name = "TFighter", authors = "!@!@!", keywords = {"universal", "fighter", "!@!@!"}, version = 1.02)
public class TFighter extends Script implements PaintListener, MouseListener {

	private final static ScriptManifest mani = TFighter.class.getAnnotation(ScriptManifest.class);

	private final Util u = new Util();


	private RSTile startTile;

	private long nextAntiban = 0;

	private int badFoodCount = 0;
	private int startTime = 0;
	private int mouseSpeedMin = 4, mouseSpeedMax = 7;

	private boolean startScript, showPaint;
	private boolean onlyInRadius = false;
	private boolean utilizeMultiwayCombat = false;
	private boolean useSafespot = false;
	private boolean useCentralClicking = true;
	private boolean prioritizeLoot = false;
	
	private List<LoopAction> loopActions = new LinkedList<LoopAction>();


	public boolean onStart() {
		if (!game.isLoggedIn()) {
			log("Start logged in.");
			return false;
		}
		showPaint = true;
		startScript = false;
		FighterGUI gui = new FighterGUI();
		while (!startScript) {
			if (!gui.isVisible())
				return false;
			sleep(100);
		}

		startTile = getMyPlayer().getLocation();
		u.sw.poll();
		startTime = (int) System.currentTimeMillis();
		
		LoopAction[] actions;
		if(prioritizeLoot)
			actions = new LoopAction[] { new LootLoop(), (useSafespot ? new SafespotLoop() : null), new InCombatLoop(), new AttackLoop() };
		else 
			actions = new LoopAction[] { (useSafespot ? new SafespotLoop() : null), new InCombatLoop(), new LootLoop(), new AttackLoop() };
		
		for(LoopAction a : actions)
			loopActions.add(a);
		
		return true;
	}

	@Override
	public int loop() {
		if (random(0, 3) == 0 || mouse.getSpeed() < mouseSpeedMin || mouse.getSpeed() > mouseSpeedMax) {
			mouse.setSpeed(random(mouseSpeedMin, mouseSpeedMax));
		}
		if (camera.getPitch() < 90) {
			camera.setPitch(true);
			return random(50, 100);
		}
		if (!walking.isRunEnabled() && walking.getEnergy() > random(60, 90)) {
			walking.setRun(true);
			return random(1200, 1600);
		}
		if(canContinue()) {
			clickContinue();
			return random(1200, 1600);
		}
		if (game.getCurrentTab() != Game.TAB_INVENTORY) {
			game.openTab(Game.TAB_INVENTORY);
			return random(700, 1500);
		}
		if (u.eat.needEat()) {
			if (u.eat.haveFood()) {
				badFoodCount = 0;
				u.eat.eatFood();
			} else if (u.eat.haveB2pTab() && u.eat.haveBones()) {
				u.eat.breakB2pTab();
				return random(2600, 3000);
			} else {
				badFoodCount++;
				if (badFoodCount > 5) {
					log("You ran out of food! Stopping.");
					stopScript();
				}
			}
			return random(1200, 1600);
		}
		for(LoopAction a : loopActions)
			if(a != null && a.activate())
				return a.loop();
		return random(50, 200);
	}
	
	private interface LoopAction {
		public int loop();
		public boolean activate();
	}
	
	private class InCombatLoop implements LoopAction {

		public int loop() {
			antiban();
			return random(50, 200);
		}

		public boolean activate() {
			return u.npcs.isInCombat();
		}
		
	}
	
	private class AttackLoop implements LoopAction {

		public int loop() {
			RSNPC inter = u.npcs.getInteracting();
			RSNPC n = inter != null ? inter : u.npcs.getNPC();
			if (n != null) {
				int result = u.npcs.clickNPC(n, "Attack " + n.getName());
				if (result == 0) {
					if(!useSafespot) {
						waitWhileMoving();
					} else {
						waitForAnim();
					}
					return random(300, 500);
				} else if (result == 1) {
					waitWhileMoving();
					return random(0, 200);
				}
			} else {
				if (calc.distanceTo(startTile) > 5) {
					walking.walkTileMM(walking.getClosestTileOnMap(startTile));
					waitWhileMoving();
				} else {
					antiban();
				}
			}
			return random(50, 200);
		}

		public boolean activate() {
			return !u.npcs.isInCombat();
		}
		
	}
	
	private class SafespotLoop implements LoopAction {

		public int loop() {
			if (!calc.tileOnScreen(startTile)) {
				walking.walkTileMM(startTile);
			} else {
				tiles.doAction(startTile, "Walk");
			}
			waitWhileMoving();
			return random(200, 500);
		}

		public boolean activate() {
			return useSafespot && calc.distanceTo(startTile) > 0;
		}
		
	}
	
	private class LootLoop implements LoopAction {

		private RSGroundItem loot = null;

		@Override
		public int loop() {
			int origCount = inventory.getCount(true);
			String name = loot.getItem().getName();
			int count = loot.getItem().getStackSize();
			int result = u.loot.takeItem(loot);
			if (result == 0) {
				waitWhileMoving();
				if (waitForInvChange(origCount)) {
					u.loot.addItem(name, count);
				}
			} else if (result == 1) {
				waitWhileMoving();
			}
			return random(50, 200);
		}

		public boolean activate() {
			return (loot = u.loot.getLoot()) != null;
		}
		
	}

	/**
	 * Waits until we are no longer moving.
	 */
	private void waitWhileMoving() {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 1500 && !getMyPlayer().isMoving()) {
			sleep(random(50, 200));
		}
		while (getMyPlayer().isMoving()) {
			sleep(random(20, 50));
		}
	}
	
	/**
	 * True if click continue interface is valid.
	 * @return True if you can click continue.
	 */
	private boolean canContinue() {
		return getContinueInterface() != null;
	}
	
	/**
	 * True if we successfully clicked continue.
	 * @return True if we clicked continue.
	 */
	private boolean clickContinue() {
		RSComponent c = getContinueInterface();
		if(c != null)
			return c.doClick();
		return false;
	}
	
	/**
	 * Gets the "Click here to continue" button on any interface.
	 * @return The "Click here to continue" button.
	 */
	private RSComponent getContinueInterface() {
		for(RSInterface iface : interfaces.getAll()) {
			//skip chat
			if(iface.getIndex() == 137)
				continue;
			for(RSComponent c : iface.getComponents()) {
				if(c != null && c.isValid() && c.containsText("Click here to continue") 
						&& c.getAbsoluteX() > 100 && c.getAbsoluteY() > 300)
					return c;
			}
		}
		return null;
	}

	/**
	 * Waits until the inventory count changes
	 */
	private boolean waitForInvChange(int origCount) {
		long start = System.currentTimeMillis();
		while (inventory.getCount(true) == origCount && System.currentTimeMillis() - start < 2000) {
			sleep(random(20, 70));
		}
		return inventory.getCount(true) != origCount;
	}

	/**
	 * Used in safe spotting. Waits for an animation.
	 */
	private void waitForAnim() {
		long timer = System.currentTimeMillis();
		while(System.currentTimeMillis() - timer < 2500 && getMyPlayer().getAnimation() == -1 
				&& (System.currentTimeMillis() - timer < 1000 || getMyPlayer().getInteracting() != null))
			sleep(random(50, 100));
	}

	/**
	 * Performs a random action, always.
	 * Actions: move mouse, move mouse off screen, move camera.
	 */
	private void antiban() {
		if (System.currentTimeMillis() > nextAntiban) {
			nextAntiban = System.currentTimeMillis() + random(2000, 30000);
		} else {
			return;
		}
		Thread mouseThread = new Thread() {
			public void run() {
				switch(random(0, 5)) {
				case 0:
					mouse.moveOffScreen();
					break;
				case 1:
					mouse.move(random(0, game.getWidth()), random(0, game.getHeight()));
					break;
				case 2:
					mouse.move(random(0, game.getWidth()), random(0, game.getHeight()));
					break;
				}
			}
		};
		Thread keyThread = new Thread() {
			public void run() {
				switch(random(0, 4)) {
				case 0:
					camera.setAngle(camera.getAngle() + random(-100, 100));
					break;
				case 1:
					camera.setAngle(camera.getAngle() + random(-100, 100));
					break;
				case 2:
					camera.setAngle(camera.getAngle() + random(-100, 100));
					break;
				}
			}
		};
		if(random(0, 2) == 0) {
			keyThread.start();
			sleep(random(0, 600));
			mouseThread.start();
		} else {
			mouseThread.start();
			sleep(random(0, 600));
			keyThread.start();
		}
		while(keyThread.isAlive() || mouseThread.isAlive())
			sleep(random(30, 100));
	}

	@Override
	public void onRepaint(Graphics g) {
		if (showPaint) {
			final NumberFormat nf = NumberFormat.getIntegerInstance();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			//Variables
			RSComponent inter = interfaces.get(137).getComponent(0);
			int x = inter.getLocation().x;
			int y = inter.getLocation().y;

			//Counters
			int runTime = (int) System.currentTimeMillis() - startTime;

			//Background
			g.setColor(new Color(198, 226, 255));
			g.fillRect(x, y, inter.getWidth() + 5, inter.getHeight() + 5);

			//Simple things
			g.setColor(Color.BLACK);
			g.setFont(new Font("Arial", Font.PLAIN, 18));
			g.drawString("TFighter by !@!@! (v" + mani.version() + ")", x + 10, y += g.getFontMetrics().getMaxAscent() + 10);
			g.setFont(new Font("Arial", Font.PLAIN, 12));
			g.drawString("Run time: " + millisToTime(runTime), x + 20, y += g.getFontMetrics().getMaxAscent() + 5);

			//Exp gains
			g.setFont(new Font("Arial", Font.BOLD, 12));
			g.drawString("Experience gained:", x + 20, y += g.getFontMetrics().getMaxAscent() + 15);
			g.setFont(new Font("Arial", Font.PLAIN, 11));
			for (Map.Entry<String, Integer> entry : u.sw.getExpGainedMap().entrySet()) {
				double expPerSec = entry.getValue() / (double) (runTime / 1000);
				int expPerHour = (int) Math.round(expPerSec * 3600);
				g.drawString(entry.getKey() + ": " + nf.format(entry.getValue()) +
						" (p/hr: " + nf.format(expPerHour) + ")", x + 25, y += g.getFontMetrics().getMaxAscent());
			}

			//Loot
			y = inter.getLocation().y;
			g.setFont(new Font("Arial", Font.BOLD, 12));
			g.drawString("Loot taken:", x + 280, y += g.getFontMetrics().getMaxAscent() + 15);
			g.setFont(new Font("Arial", Font.PLAIN, 11));
			Map<String, Integer> loot = u.loot.getLootTaken();
			for (Map.Entry<String, Integer> entry : loot.entrySet()) {
				g.drawString(entry.getKey() + " x" + entry.getValue(), x + 285, y += g.getFontMetrics().getMaxAscent());
			}
		}
		drawMouse(g);
	}

	private void drawMouse(Graphics g) {
		int x = mouse.getLocation().x, y = mouse.getLocation().y;
		g.setColor(System.currentTimeMillis() - mouse.getPressTime() < 300 ? Color.CYAN : Color.RED);
		g.fillOval(x - 6, y - 6, 12, 12);
		g.setColor(Color.ORANGE);
		g.fillOval(x - 3, y - 3, 6, 6);
		g.drawLine(x - 10, y - 10, x + 10, y + 10);
		g.drawLine(x - 10, y + 10, x + 10, y - 10);
	}

	/**
	 * Formats the given value into a clock format
	 * that follows the form of 00:00:00
	 *
	 * @param millis The total millis to be evaluated
	 * @return A String representation of millis, formatted as a clock
	 */
	private String millisToTime(int millis) {
		int hours = millis / (60 * 1000 * 60);
		int minutes = (millis - (hours * 60 * 1000 * 60)) / (60 * 1000);
		int seconds = (millis - (hours * 60 * 1000 * 60) - (minutes * 60 * 1000)) / 1000;
		return (hours >= 10 ? hours + ":" : "0" + hours + ":")
		+ (minutes >= 10 ? minutes + ":" : "0" + minutes + ":")
		+ (seconds >= 10 ? seconds : "0" + seconds);
	}

	private class Util {
		private final NPCs npcs = new NPCs();
		private final Eating eat = new Eating();
		private final Loot loot = new Loot();
		private final SkillWatcher sw = new SkillWatcher();
	}

	private class NPCs {

		private int[] npcIDs = new int[0];
		private String[] npcNames = new String[0];

		private int maxRadius = 10;

		/**
		 * Checks if we are in combat.
		 *
		 * @return True if we are in combat.
		 */
		private boolean isInCombat() {
			return getMyPlayer().getInteracting() instanceof RSNPC;
		}

		/**
		 * Clicks an NPC based on its model.
		 *
		 * @param npc    The NPC to click.
		 * @param action The action to perform.
		 * @return 0 if the NPC was clicked, 1 if we walked to it, or -1 if nothing happened.
		 */
		private int clickNPC(RSNPC npc, String action) {
			for (int i = 0; i < 10; i++) {
				if (isPartiallyOnScreen(npc.getModel())) {
					Point p = useCentralClicking ? getCentralPoint(npc.getModel()) : getPointOnScreen(npc.getModel(), false);
					if (p == null || !calc.pointOnScreen(p)) {
						continue;
					}
					mouse.move(p, useCentralClicking ? 3 : 0, useCentralClicking ? 3 : 0);
					String[] items = menu.getItems();
					if (items.length > 0 && items[0].contains(action)) {
						mouse.click(true);
						return 0;
					} else if (menu.contains(action)) {
						mouse.click(false);
						sleep(random(100, 200));
						for (int x = 0; x < 4; x++) {
							if (!menu.contains(action)) {
								break;
							}
							if (menu.doAction(action)) {
								return 0;
							}
						}
					}
				} else {
					if(!useSafespot) {
						walking.walkTileMM(closerTile(npc.getLocation(), 1), 2, 2);
						return 1;
					} else {
						int angle = camera.getCharacterAngle(npc);
						if (calc.distanceTo(npc) < 10 && Math.abs(angle - camera.getAngle()) > 20) {
							camera.setAngle(angle + random(-20, 20));
						}
					}
				}
			}
			return -1;
		}

		/**
		 * Checks if a model is partially on screen.
		 *
		 * @param m The RSModel to check.
		 * @return True if any point on the model is on screen.
		 */
		private boolean isPartiallyOnScreen(RSModel m) {
			return getPointOnScreen(m, true) != null;
		}

		/**
		 * Gets a point on a model that is on screen.
		 *
		 * @param m     The RSModel to test.
		 * @param first If true, it will return the first point that it finds on screen.
		 * @return A random point on screen of an object.
		 */
		private Point getPointOnScreen(RSModel m, boolean first) {
			if (m == null)
				return null;
			ArrayList<Point> list = new ArrayList<Point>();
			try {
				Polygon[] tris = m.getTriangles();
				for (int i = 0; i < tris.length; i++) {
					Polygon p = tris[i];
					for (int j = 0; j < p.xpoints.length; j++) {
						Point pt = new Point(p.xpoints[j], p.ypoints[j]);
						if (calc.pointOnScreen(pt)) {
							if (first)
								return pt;
							list.add(pt);
						}
					}
				}
			} catch (Exception e) {
			}
			return list.size() > 0 ? list.get(random(0, list.size())) : null;
		}

		/**
		 * Generates a rough central point. Performs the calculation
		 * by first generating a rough point, and then finding the point
		 * closest to the rough point that is actually on the RSModel.
		 * 
		 * @param m The RSModel to test.
		 * @return The rough central point.
		 */
		private Point getCentralPoint(RSModel m) {
			if(m == null)
				return null;
			try {
				/* Add X and Y of all points, to get a rough central point */
				int x = 0, y = 0, total = 0;
				for(Polygon poly : m.getTriangles()) {
					for(int i = 0; i < poly.npoints; i++) {
						x += poly.xpoints[i];
						y += poly.ypoints[i];
						total++;
					}
				}
				Point central = new Point(x / total, y / total);
				/* Find a real point on the NPC that is closest to the central point */
				Point curCentral = null;
				double dist = 20000;
				for(Polygon poly : m.getTriangles()) {
					for(int i = 0; i < poly.npoints; i++) {
						Point p = new Point(poly.xpoints[i], poly.ypoints[i]);
						if(!calc.pointOnScreen(p))
							continue;
						double dist2 = distanceBetween(central, p);
						if(curCentral == null || dist2 < dist) {
							curCentral = p;
							dist = dist2;
						}
					}
				}
				return curCentral;
			} catch (Exception e) {}
			return null;
		}

		/**
		 * Calculates the distance between two points.
		 * 
		 * @param p1 The first point.
		 * @param p2 The second point.
		 * @return The distance between the two points, using the distance formula.
		 */
		private double distanceBetween(Point p1, Point p2) {
			return Math.sqrt(((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y)));
		}

		/**
		 * Gets a closer tile to us within dist.
		 *
		 * @param t    The tile to start with.
		 * @param dist The max dist.
		 * @return A closer tile.
		 */
		private RSTile closerTile(RSTile t, int dist) {
			RSTile loc = getMyPlayer().getLocation();
			int newX = t.getX(), newY = t.getY();
			for (int i = 1; i < dist; i++) {
				newX = t.getX() != loc.getX() ? (t.getX() < loc.getX() ? newX-- : newX++) : newX;
				newY = t.getY() != loc.getY() ? (t.getY() < loc.getY() ? newY-- : newY++) : newY;
			}
			return new RSTile(newX, newY);
		}

		/**
		 * Returns the nearest NPC.
		 *
		 * @return The nearest NPC that matches the filter.
		 */
		private RSNPC getNPC() {
			RSNPC onScreen = npcs.getNearest(npcOnScreenFilter);
			if(onScreen != null)
				return onScreen;
			return npcs.getNearest(npcFilter);
		}

		/**
		 * Returns the interacting NPC that matches our description, if any.
		 *
		 * @return The closest interacting NPC that matches the filter.
		 */
		private RSNPC getInteracting() {
			RSNPC npc = null;
			int dist = 20;
			for (RSNPC n : npcs.getAll()) {
				if (!isOurNPC(n))
					continue;
				RSCharacter inter = n.getInteracting();
				if (inter != null && inter instanceof RSPlayer && inter.equals(getMyPlayer()) && calc.distanceTo(n) < dist) {
					dist = calc.distanceTo(n);
					npc = n;
				}
			}
			return npc;
		}

		private boolean isOurNPC(RSNPC t) {
			int id = t.getID();
			String name = t.getName();
			boolean good = false;
			for (int i : npcIDs) {
				if (id == i)
					good = true;
			}
			for (String s : npcNames) {
				if (name.toLowerCase().contains(s.toLowerCase()))
					good = true;
			}
			return good;
		}

		/**
		 * The filter we use!
		 */
		 private final Filter<RSNPC> npcFilter = new Filter<RSNPC>() {
			public boolean accept(RSNPC t) {
				return (isOurNPC(t) && t.isValid() && (!onlyInRadius || calc.distanceBetween(t.getLocation(), startTile) < maxRadius) 
						&& (utilizeMultiwayCombat || !t.isInCombat() && t.getInteracting() == null) && t.getHPPercent() != 0);
			}
		 };
		 
		 /**
		  * Will only return an on screen NPC. Based on npcFilter.
		  */
		 private final Filter<RSNPC> npcOnScreenFilter = new Filter<RSNPC>() {
			 public boolean accept(RSNPC n) {
				 return npcFilter.accept(n) && getPointOnScreen(n.getModel(), true) != null;
			 }
		 };
	}

	private class Eating {

		private final int[] B2P_TAB_ID = new int[]{8015};
		private final int[] BONES_ID = new int[]{526, 532, 530, 528, 3183, 2859};
		
		private int toEatAtPercent = getRandomEatPercent();
		
		/**
		 * Returns a random integer of when to eat.
		 * @return A random integer of the percent to eat at.
		 */
		private int getRandomEatPercent() {
			return random(45, 60);
		}

		/**
		 * Checks if we have at least one B2P tab.
		 *
		 * @return True if we have a tab.
		 */
		private boolean haveB2pTab() {
			return inventory.getCount(B2P_TAB_ID) > 0;
		}

		/**
		 * Breaks a B2P tab.
		 */
		private void breakB2pTab() {
			RSItem i = inventory.getItem(B2P_TAB_ID);
			if (i != null)
				i.doClick(true);
		}

		/**
		 * Checks if the inventory contains bones, for B2P.
		 *
		 * @return True if we have bones.
		 */
		private boolean haveBones() {
			return inventory.getCount(BONES_ID) > 0;
		}

		/**
		 * Checks if we have food.
		 *
		 * @return True if we have food.
		 */
		private boolean haveFood() {
			return getFood() != null;
		}

		/**
		 * Finds food based on inventory actions.
		 *
		 * @return The RSItem of food, or null if none was found.
		 */
		private RSItem getFood() {
			for (RSItem i : inventory.getItems()) {
				if (i == null || i.getID() == -1)
					continue;
				if (i.getComponent().getActions() == null || i.getComponent().getActions()[0] == null)
					continue;
				if (i.getComponent().getActions()[0].contains("Eat"))
					return i;
			}
			return null;
		}

		/**
		 * Attempts to eat food.
		 *
		 * @return True if we ate.
		 */
		private boolean eatFood() {
			RSItem i = getFood();
			for (int j = 0; j < 3; j++) {
				if (i == null)
					break;
				if (i.doAction("Eat")) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks whether you need to eat or not.
		 *
		 * @return True if we need to eat.
		 */
		private boolean needEat() {
			if(getHPPercent() <= toEatAtPercent) {
				toEatAtPercent = getRandomEatPercent();
				return true;
			}
			return false;
		}
		
		/**
		 * Returns an integer representing the current health percentage.
		 * @return The current health percentage.
		 */
		public int getHPPercent() {
			try {
				return ((int) ((Integer.parseInt(interfaces.get(748).getComponent(8).getText().trim()) / (double)(skills.getRealLevel(Skills.CONSTITUTION) * 10)) * 100));
			} catch (Exception e) {
				return 100;
			}
		}
	}

	private class Loot {

		private int[] lootIDs = new int[0];
		private String[] lootNames = new String[0];

		private Map<String, Integer> lootTaken = new HashMap<String, Integer>();

		/**
		 * Gets the nearest loot, based on the filter
		 *
		 * @return The nearest item to loot, or null if none.
		 */
		private RSGroundItem getLoot() {
			return groundItems.getNearest(lootFilter);
		}

		/**
		 * Attempts to take an item.
		 *
		 * @param item The item to take.
		 * @return -1 if error, 0 if taken, 1 if walked
		 */
		private int takeItem(RSGroundItem item) {
			if (item == null)
				return -1;
			String action = "Take " + item.getItem().getName();
			if (item.isOnScreen()) {
				for (int i = 0; i < 5; i++) {
					if (menu.isOpen())
						mouse.moveRandomly(300, 500);
					Point p = calc.tileToScreen(item.getLocation(), random(0.48, 0.52), random(0.48, 0.52), 0);
					if (!calc.pointOnScreen(p))
						continue;
					mouse.move(p, 3, 3);
					if (menu.contains(action)) {
						if (menu.getItems()[0].contains(action)) {
							mouse.click(true);
							return 0;
						} else {
							mouse.click(false);
							sleep(random(100, 200));
							if (menu.doAction(action))
								return 0;
						}
					}
				}
			} else {
				walking.walkTileMM(walking.getClosestTileOnMap(item.getLocation()));
				return 1;
			}
			return -1;
		}

		private void addItem(String name, int count) {
			if (lootTaken.get(name) != null) {
				int newCount = count + lootTaken.get(name);
				lootTaken.remove(name);
				lootTaken.put(name, newCount);
			} else {
				lootTaken.put(name, count);
			}
		}

		private Map<String, Integer> getLootTaken() {
			HashMap<String, Integer> m = new HashMap<String, Integer>();
			m.putAll(lootTaken);
			return m;
		}

		private final Filter<RSGroundItem> lootFilter = new Filter<RSGroundItem>() {
			public boolean accept(RSGroundItem t) {
				//Skip if we can't hold it
				RSItem i;
				if (inventory.isFull() && ((i = inventory.getItem(t.getItem().getID())) == null || i.getStackSize() <= 1)) {
					return false;
				}
				//Skip if its out of radius or far away
				if (onlyInRadius && calc.distanceBetween(t.getLocation(), startTile) > u.npcs.maxRadius
						|| calc.distanceTo(t.getLocation()) > 25) {
					return false;
				}
				//Check ID/name
				boolean good = false;
				int id = t.getItem().getID();
				for (int iD : lootIDs) {
					if (iD == id)
						good = true;
				}
				String name = t.getItem().getName();
				for (String s : lootNames) {
					if (name != null && name.toLowerCase().contains(s.toLowerCase()))
						good = true;
				}
				return good;
			}
		};

	}

	private class SkillWatcher {

		private Map<Integer, Integer> startExpMap = new HashMap<Integer, Integer>();
		private final int[] SKILLS_TO_WATCH = new int[]{Skills.SLAYER, Skills.CONSTITUTION, Skills.ATTACK, Skills.STRENGTH, Skills.DEFENSE, Skills.RANGE, Skills.MAGIC};

		/**
		 * Basically sets start exp for all skills we are watching.
		 */
		private void poll() {
			for (int skill : SKILLS_TO_WATCH) {
				if (startExpMap.containsKey(skill))
					startExpMap.remove(skill);
				startExpMap.put(skill, skills.getCurrentExp(skill));
			}
		}

		/**
		 * Returns the amount of exp gained in the specified skill.
		 *
		 * @param skill The skill see Skills.*
		 * @return
		 */
		private int getExpGainedIn(int skill) {
			if (startExpMap.get(skill) == null)
				return -1;
			return skills.getCurrentExp(skill) - startExpMap.get(skill);
		}

		/**
		 * Returns a map of skill names and exp gained.
		 *
		 * @return A map of exp gains and skill names.
		 */
		private Map<String, Integer> getExpGainedMap() {
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (int i : SKILLS_TO_WATCH) {
				int gained = getExpGainedIn(i);
				if (gained != 0)
					map.put(Skills.SKILL_NAMES[i], gained);
			}
			return map;
		}

	}

	@SuppressWarnings("serial")
	private class FighterGUI extends JFrame {

		private final File file = new File(GlobalConfiguration.Paths.getSettingsDirectory() + System.getProperty("file.separator") + "TFighterProps.txt");

		private JCheckBox useMulti, useRadius, useSafe, useCentral, prioritizeLoot;
		private JTextField npcBox, lootBox, mouseSpeedBox;

		private FighterGUI() {
			init();
			pack();
			setVisible(true);
		}

		private void init() {
			Properties props = loadProperties();
			JPanel north = new JPanel(new FlowLayout());
			north.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			{
				JLabel title = new JLabel("TFighter by !@!@!");
				title.setFont(new Font("Arial", Font.PLAIN, 28));
				north.add(title);
			}
			add(north, BorderLayout.NORTH);

			JPanel center = new JPanel();
			center.setLayout(new BoxLayout(center, BoxLayout.PAGE_AXIS));
			center.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			{
				mouseSpeedBox = new JTextField("4,7");
				useMulti = new JCheckBox("Utilize multiway combat");
				useRadius = new JCheckBox("Only attack within a radius");
				useSafe = new JCheckBox("Use safespot?");
				useCentral = new JCheckBox("Use central point on NPC? (instead of random)");
				npcBox = new JTextField("2,5,1,Chicke");
				lootBox = new JTextField("arrow,feather");
				prioritizeLoot = new JCheckBox("Prioritize loot over combat?");

				mouseSpeedBox.setAlignmentX(JTextField.CENTER_ALIGNMENT);
				useMulti.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
				useRadius.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
				npcBox.setAlignmentX(JTextField.CENTER_ALIGNMENT);
				lootBox.setAlignmentX(JTextField.CENTER_ALIGNMENT);
				useSafe.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
				useCentral.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);

				if (props.getProperty("mouseSpeed") != null) {
					mouseSpeedBox.setText(props.getProperty("mouseSpeed"));
				}
				if (props.getProperty("useMulti") != null) {
					if (props.getProperty("useMulti").equals("true"))
						useMulti.setSelected(true);
				}
				if (props.getProperty("useRadius") != null) {
					if (props.getProperty("useRadius").equals("true"))
						useRadius.setSelected(true);
				}
				if (props.getProperty("npcBox") != null) {
					npcBox.setText(props.getProperty("npcBox"));
				}
				if (props.getProperty("lootBox") != null) {
					lootBox.setText(props.getProperty("lootBox"));
				}
				if(props.getProperty("useSafe") != null) {
					if(props.getProperty("useSafe").equals("true"))
						useSafe.setSelected(true);
				}
				if(props.getProperty("useCentral") != null) {
					if(props.getProperty("useCentral").equals("true"))
						useCentral.setSelected(true);
				}
				if(props.getProperty("prioritizeLoot") != null) {
					if(props.get("prioritizeLoot").equals("true"))
						prioritizeLoot.setSelected(true);
				}

				JLabel lbl1 = new JLabel("Enter your desired mouse speed (max,min)");
				lbl1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				center.add(lbl1);
				center.add(mouseSpeedBox);
				center.add(useMulti);
				center.add(useRadius);
				center.add(useSafe);
				center.add(useCentral);
				center.add(new JLabel(" "));
				JLabel lbl2 = new JLabel("Enter the IDs and/or names of the NPCs to fight.");
				lbl2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				center.add(lbl2);
				JLabel lbl3 = new JLabel("You can mix and match these, all in the same box!");
				lbl3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				center.add(lbl3);
				center.add(npcBox);
				JLabel lbl4 = new JLabel("Enter the IDs and/or names of items to loot.");
				lbl4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				center.add(lbl4);
				center.add(lootBox);
				center.add(prioritizeLoot);
				prioritizeLoot.setAlignmentX(JComboBox.CENTER_ALIGNMENT);
				JLabel lbl5 = new JLabel("If selected, you will loot while in combat.");
				lbl5.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				center.add(lbl5);
			}
			add(center, BorderLayout.CENTER);

			JPanel south = new JPanel(new FlowLayout());
			south.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			{
				JButton start = new JButton("Start script!");
				start.setAlignmentX(JButton.CENTER_ALIGNMENT);
				start.addActionListener(onStart);
				south.add(start);
			}
			add(south, BorderLayout.SOUTH);

			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setTitle("TFighter GUI");
		}

		private Properties loadProperties() {
			try {
				if (!file.exists())
					file.createNewFile();
				Properties p = new Properties();
				p.load(new FileInputStream(file));
				return p;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private void saveProperties() {
			Properties p = new Properties();
			p.put("mouseSpeed", mouseSpeedBox.getText());
			p.put("useMulti", Boolean.toString(useMulti.isSelected()));
			p.put("useRadius", Boolean.toString(useRadius.isSelected()));
			p.put("useSafe", Boolean.toString(useSafe.isSelected()));
			p.put("useCentral", Boolean.toString(useCentral.isSelected()));
			p.put("npcBox", npcBox.getText());
			p.put("lootBox", lootBox.getText());
			p.put("prioritizeLoot", Boolean.toString(prioritizeLoot.isSelected()));
			try {
				p.store(new FileOutputStream(file), "");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private ActionListener onStart = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveProperties();
				startScript = true;
				mouseSpeedMin = Integer.parseInt(mouseSpeedBox.getText().split(",")[0]);
				mouseSpeedMax = Integer.parseInt(mouseSpeedBox.getText().split(",")[0]);
				utilizeMultiwayCombat = useMulti.isSelected();
				onlyInRadius = useRadius.isSelected();
				useSafespot = useSafe.isSelected();
				useCentralClicking = useCentral.isSelected();
				TFighter.this.prioritizeLoot = prioritizeLoot.isSelected();
				if (onlyInRadius) {
					u.npcs.maxRadius = Integer.parseInt(JOptionPane.showInputDialog("Enter the max radius. Example: 10"));
				}
				String[] ids = npcBox.getText().split(",");
				ArrayList<Integer> idList = new ArrayList<Integer>();
				ArrayList<String> nameList = new ArrayList<String>();
				for (int i = 0; i < ids.length; i++) {
					if (ids[i] != null && !ids[i].equals("")) {
						try {
							int id = Integer.parseInt(ids[i]);
							idList.add(id);
						} catch (Exception e1) {
							nameList.add(ids[i]);
						}
					}
				}
				u.npcs.npcIDs = idList.size() > 0 ? toIntArray(idList.toArray(new Integer[0])) : new int[0];
				u.npcs.npcNames = nameList.size() > 0 ? nameList.toArray(new String[0]) : new String[0];

				ids = lootBox.getText().split(",");
				idList = new ArrayList<Integer>();
				nameList = new ArrayList<String>();
				for (int i = 0; i < ids.length; i++) {
					if (ids[i] != null && !ids[i].equals("")) {
						try {
							int id = Integer.parseInt(ids[i]);
							idList.add(id);
						} catch (Exception e1) {
							nameList.add(ids[i]);
						}
					}
				}
				u.loot.lootIDs = idList.size() > 0 ? toIntArray(idList.toArray(new Integer[0])) : new int[0];
				u.loot.lootNames = nameList.size() > 0 ? nameList.toArray(new String[0]) : new String[0];
				dispose();
			}
		};

		private int[] toIntArray(Integer[] ints) {
			int[] done = new int[ints.length];
			for (int i = 0; i < done.length; i++) {
				done[i] = ints[i].intValue();
			}
			return done;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		RSComponent inter = interfaces.get(137).getComponent(0);
		if (inter.getArea().contains(e.getPoint())) {
			showPaint = !showPaint;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}

