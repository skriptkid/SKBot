import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.*;
import org.rsbot.script.*;
import org.rsbot.script.methods.*;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.*;

@ScriptManifest(name = "AMP", authors = "Killa", keywords = "Mining", version = 1.5, description = "Global Mining Script!")
public class AMP extends Script implements PaintListener, MouseListener,
		MessageListener {

	private RSTile bankTile;
	private boolean powerMine, canStart, smart, mineXDropX, switchingWorlds,
			worldSwitching, miningRune;
	private String location = "Varrock";
	private int[] rockIDs, emptyRockIDs, inventoryItemIDs;
	private RSArea mineArea;
	private int startEXP, startLevel, inventoryCount, lastCount,
			experiencePerRock, totalProfit, oresMined, setAmount, maxPlayers;
	public volatile int minedBeforeDrop;
	private long startTime;

	private Actions action;
	private Set<Actions> actionSet;
	private Set<Particle> particles;
	private ArrayList<OreType> oreTypes = new ArrayList<OreType>();

	AMPgui gui;

	public static interface Constants {
		RSArea LIVING_CAVERN_BANK_AREA = new RSArea(3654, 5112, 3657, 5116);
		RSArea VARROCK_EAST_MINE = new RSArea(3280, 3359, 3290, 3372);
		RSArea VARROCK_BANK_AREA = new RSArea(3250, 3419, 3257, 3423);
		RSArea YANILLE_BANK_AREA = new RSArea(2609, 3095, 2613, 3089);
		RSArea ALKARID_MINE_AREA = new RSArea(3293, 3286, 3304, 3318);
		RSArea DRAYNOR_MINE_AREA = new RSArea(3142, 3144, 3149, 3154);
		RSArea DRAYNOR_BANK_AREA = new RSArea(3092, 3240, 3097, 3246);
		RSArea ALKARID_BANK_AREA = new RSArea(3269, 3161, 3272, 3173);
		RSArea ESS_MINE = new RSArea(2870, 4790, 2950, 4870);
		RSArea FALADOR_BANK_AREA = new RSArea(3009, 3355, 3018, 3358);
		RSArea GUILD_ENTRANCE_AREA = new RSArea(3015, 3336, 3024, 3342);
		RSArea GUILD_EXIT_AREA = new RSArea(3017, 9736, 3022, 9742);
		RSArea GUILD_MINE_AREA = new RSArea(3016, 9730, 3055, 9756);
		RSArea RIMMINGTON_MINE_AREA = new RSArea(2966, 3230, 2988, 3251);
		RSArea ARDOUGNE_BANK_AREA = new RSArea(2649, 3280, 2656, 3287);
		RSArea ARDOUGNE_MINE_AREA = new RSArea(2689, 3328, 2715, 3338);
		RSArea DWARVEN_MINE_ENTRANCE = new RSArea(3058, 3376, 3062, 3379);
		RSArea DWARVEN_MINE_AREA = new RSArea(3035, 9759, 3058, 9785);
		RSArea AUBURY_AREA = new RSArea(new RSTile[] { new RSTile(3252, 3404),
				new RSTile(3253, 3404), new RSTile(3255, 3401),
				new RSTile(3253, 3399), new RSTile(3252, 3399) });
		RSArea WIZARD_TOWER_AREA = new RSArea(new RSTile[] {
				new RSTile(2597, 3089), new RSTile(2597, 3087),
				new RSTile(2592, 3082), new RSTile(2589, 3083),
				new RSTile(2585, 3086), new RSTile(2585, 3089),
				new RSTile(2589, 3093), new RSTile(2592, 3093) });

		int GUILD_ENTRANCE_LADDER = 2113;
		int GUILD_EXIT_LADDER = 6226;
		int ESSENCE_PORTAL = 2492;
		int VARROCK_CLOSED_DOOR = 24381, DWARVEN_MINE_DOOR = 11714,
				DWARVEN_STAIRS_DOWN = 35921, DWARVEN_STAIRS_UP = 30943;
		int AUBURY = 553;
		int DISTENTOR = 462;
		int ROCK_CAVERN_DEPOSIT_BOX = 45079;
		int WORLD_SELECTION_INTERFACE = 910, LOBBY_AREA_INTERFACE = 906;

		RSTile WIZARD_TOWER_DOOR_TILE = new RSTile(2598, 3087);
		RSTile VARROCK_DOOR_TILE = new RSTile(3253, 3398);

		int[] FREE_WORLDS = { 169, 167, 165, 161, 152, 153, 154, 155, 141, 149,
				134, 135, 118, 120, 123, 105, 106, 108, 96, 102, 85, 87, 90,
				81, 80, 75, 74, 73, 61, 62, 55, 49, 50, 43, 41, 40, 38, 37, 35,
				34, 33, 32, 29, 30, 25, 20, 19, 13, 11, 16, 14, 10, 8, 7, 5, 4,
				3, 1 };
		int[] MEMBER_WORLDS = { 2, 6, 9, 12, 15, 22, 23, 24, 27, 28, 31, 36,
				39, 42, 44, 45, 46, 48, 51, 52, 53, 54, 56, 58, 59, 60 };
		int[] COAL_ROCKS = { 5770, 5771, 5772, 11930, 11932, 11931, 2096, 2097 };
		int[] COAL_EMPTY = { 5763, 5764, 5765, 11552, 11554, 452, 450 };
		int[] PICKAXES = { 1265, 1267, 1269, 1296, 1273, 1271, 1275, 15259 };
		int[] IRON_EMPTY = { 11557, 11555, 11556, 11554, 11552, 11553, 9725,
				9723, 9724, 450, 452 };
		int[] IRON_ROCKS = { 11954, 11956, 11955, 37307, 37309, 37308, 9717,
				9718, 9719, 2093, 2092 };
		int[] COPPER_ROCKS = { 11960, 11962, 11961 };
		int[] COPPER_EMPTY = { 11557, 11555, 11556 };
		int[] SILVER_ROCKS = { 37306, 37305, 37304 };
		int[] SILVER_EMPTY = { 11554, 11553, 11552 };
		int[] MITHRIL_ROCKS = { 11944, 11943, 11942, 5786, 5784, 5785 };
		int[] MITHRIL_EMPTY = { 11554, 11552, 11553 };
		int[] ADAMANTITE_ROCKS = { 11941, 11939 };
		int[] ADAMANTITE_EMPTY = { 11554, 11552 };
		int[] GOLD_ROCKS = { 37312, 37310 };
		int[] GOLD_EMPTY = { 11552, 11554 };
		int[] TIN_ROCKS = { 11959, 11957, 11958 };
		int[] TIN_EMPTY = { 11557, 11555, 11556 };
		int[] WIZARD_TOWER_DOORS = { 1600, 1601 };
		int[] C_GOLD_ROCK = { 45076 };
		int[] C_COAL_ROCK = { 5999 };
		int[] C_EMPTY = { 46201, 45075, 5990 };
		int[] ESSENCE_ROCK = { 2491 };

		int[] EXPERIENCE_RATES = { 5, 18, 35, 40, 50, 65, 80, 95, 125 };

		String[] ORE_NAMES = { "Essence", "Clay", "Copper", "Tin", "Iron",
				"Silver", "Coal", "Granite", "Gold", "Mithril", "Adamantite",
				"Runite", "C Gold", "C Coal" };
		String[] Locations = { "Varrock", "Mining Guild", "Yanille",
				"Al Karid", "Draynor", "Crafting Guild", "Rimmington",
				"Living Rock Caverns", "Dwarven Mines" };
		String[] STRATEGY_NAMES = { "Powermine", "Bank", "Avoid other players",
				"MXDX" };

	}

	public static abstract class Actions {
		public abstract void process();

		public abstract boolean isValid();

		public abstract String getDescription();

		public void complete() {

		}

		public void paint(Graphics g) {

		}
	}

	public class WorldSwitch extends Actions {
		int[] worldIDs;
		int ran, fail;

		public WorldSwitch(int[] world) {
			this.worldIDs = world;
		}

		@Override
		public void process() {
			if (interfacePresent(Constants.LOBBY_AREA_INTERFACE)
					&& canChooseWorld()) {
				if (interfacePresent(Constants.WORLD_SELECTION_INTERFACE)) {
					selectTopWorld();
					enterWorld(getSelectedWorld(), worldIDs);
				} else {
					interfaces
							.getComponent(Constants.LOBBY_AREA_INTERFACE, 197)
							.doClick();
					sleep(random(750, 1200));
				}
			} else {
				game.logout(true);
				sleep(random(1000, 2000));
			}
		}

		@Override
		public boolean isValid() {
			return interfaces.get(906).isValid() || switchingWorlds;
		}

		@Override
		public String getDescription() {
			return "Switching Worlds to " + worldIDs[ran];
		}

		private boolean canChooseWorld() {
			if (interfaces.getComponent(906, 221).isValid()) {
				if (interfaces.getComponent(906, 221).getText()
						.contains("not logged out")
						|| interfaces.getComponent(906, 221).getText()
								.contains("member's"))
					mouse.click(random(300, 460), random(325, 340), true);
				sleep(600);
				return false;
			} else if (interfaces.get(596).isValid()) {
				return false;
			} else if (interfaces.getComponent(906, 36).getText()
					.contains("full")) {
				mouse.click(random(280, 460), random(302, 322), true);
				sleep(590);
				return false;
			}
			return true;
		}

		private boolean interfacePresent(int main) {
			return interfaces.get(main).isValid();
		}

		private int getSelectedWorld() {
			return Integer.parseInt(interfaces.getComponent(910, 10).getText()
					.replace("World", "").trim());
		}

		private void selectTopWorld() {
			Point pn = new Point(random(101, 500), random(140, 158));
			mouse.click(pn, true);
		}

		private int getApproximateY(int world) {
			if (world < 16) {
				return 160;
			} else if (world > 15 && world < 31) {
				return 193;
			} else if (world >= 31 && world < 46) {
				return 221;
			} else if (world >= 46 && world < 61) {
				return 248;
			} else if (world >= 61 && world < 77) {
				return 275;
			} else if (world >= 77 && world < 92) {
				return 302;
			} else if (world >= 92 && world < 114) {
				return 330;
			} else if (world >= 114 && world < 134) {
				return 357;
			} else if (world >= 134 && world < 154) {
				return 382;
			} else if (world >= 154) {
				return 412;
			}
			return 0;
		}

		private void enterWorld(int topWorld, int[] world) {
			ran = random(0, world.length);
			while (world[ran] - getSelectedWorld() > 16
					|| world[ran] - getSelectedWorld() < 0) {
				if (getSelectedWorld() != world[ran]) {
					mouse.click(random(695, 703), getApproximateY(world[ran]),
							true);
					sleep(random(400, 600));
					selectTopWorld();
					sleep(random(500, 1200));
					topWorld = getSelectedWorld();
					fail++;
					if (fail > 6)
						break;
				}
			}
			mouse.click(
					new Point(random(100, 450), (149 + Math.abs(topWorld
							- world[ran]) * 19)), true);
			sleep(random(600, 1200));
			if (world[ran] == getSelectedWorld()) {
				fail = 0;
				interfaces.getComponent(906, 145).doClick();
				switchingWorlds = false;
			} else {
				fail++;
				if (fail >= 15) {
					log.severe("Error when world switching!");
					stopScript();
				}
			}
		}

	}

	public abstract class WalkingAction extends Actions {

		private RSArea dest;
		private String name;
		private RSTile last;

		public WalkingAction(RSArea dest, String name) {
			this.dest = dest;
			this.name = name;
		}

		protected abstract boolean isTargetValid();

		public void process() {
			setRun();
			RSTile tile = dest.getCentralTile();
			if (last == null || getMyPlayer().isIdle()
					|| (calc.distanceTo(last) < 10 && !dest.contains(last))) {
				if (calc.tileOnMap(tile)) {
					walking.walkTileMM(tile, 2, 2);
				} else if (!walking.walkTo(tile)) {
					walking.walkTileMM(walking.getClosestTileOnMap(tile));
				}
				last = walking.getDestination();
				sleep(random(1000, 1800));
			}
		}

		public boolean isValid() {
			return !dest.contains(getMyPlayer().getLocation())
					&& walking.getDestination() == null && !switchingWorlds
					&& isTargetValid() && !interfaces.get(906).isValid();
		}

		public void complete() {
			last = null;
		}

		public void paint(Graphics g) {
			g.setColor(new Color(0, 0, 255, 100));
			for (RSTile tile : dest.getTileArray()) {
				if (calc.tileOnMap(tile)) {
					Point loc = calc.tileToMinimap(tile);
					g.drawRect(loc.x, loc.y, 3, 3);
					g.fillRect(loc.x, loc.y, 3, 3);
				}
			}
		}

		public String getDescription() {
			return "Walking to " + name + ".";
		}

	}

	public abstract class BankingAction extends Actions {
		RSObject highlight;
		int fail;

		@Override
		public void process() {
			RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
			RSObject cavernBox = objects
					.getNearest(Constants.ROCK_CAVERN_DEPOSIT_BOX);
			if (bankBooth != null) {
				if (bank.isOpen()) {
					highlight = null;
					bank.depositAllExcept(Constants.PICKAXES);
				} else {
					if (bankBooth.isOnScreen()) {
						highlight = bankBooth;
						fail++;
						if (!getMyPlayer().isMoving())
							bankBooth.doAction("Use-quickly");
					} else
						walking.walkTileMM(bankBooth.getLocation());
				}
			} else if (cavernBox != null) {
				if (bank.isOpen()) {
					highlight = null;
					bank.depositAllExcept(Constants.PICKAXES);
				} else {
					if (cavernBox.isOnScreen()) {
						highlight = cavernBox;
						fail++;
						if (!getMyPlayer().isMoving())
							cavernBox.doAction("Deposit");
					} else
						walking.walkTileMM(cavernBox.getLocation());
				}
			}
		}

		public void complete() {
			fail = 0;
		}

		public void paint(Graphics g) {
			g.setColor(new Color(0, 255, 0, 100));
			if (highlight != null)
				for (Polygon p : highlight.getModel().getTriangles())
					g.drawPolygon(p);
		}

		@Override
		public boolean isValid() {
			return !canMine() && calc.distanceTo(bankTile) < 14
					&& !interfaces.getComponent(906).isValid();
		}

		@Override
		public String getDescription() {
			return "Processing Bank Procedures.";
		}

	}

	public abstract class MiningAction extends Actions {
		public static final int DIST_EXPONENT = 2;
		public static final int MAX_OTHER_DIST = 10;
		public static final int MY_PLAYER_WEIGHT = 2;

		RSTile[] rock_tiles;
		int[] rockID, emptyID;
		int fail;
		RSArea area;
		boolean analyze;
		RSObject last, highlight;
		LinkedList<RSTile> rockList = new LinkedList<RSTile>();

		public MiningAction(int[] rockID, int[] emptyID, RSArea area,
				boolean analyze) {
			this.analyze = analyze;
			this.rockID = rockID;
			this.emptyID = emptyID;
			this.area = area;
		}

		public MiningAction(int[] rockIDs, int[] emptyID) {
			this.rockID = rockIDs;
			this.emptyID = emptyID;
		}

		public void process() {
			RSObject rock;
			if (analyze) {
				RSObject[] rocks = getNearestBestRocks();
				rock = rocks[0];
			} else
				rock = objects.getNearest(rockIDs);

			if (rock != null) {
				if (last != null) {
					if (accept(last)) {
						if (rock.isOnScreen()) {
							highlight = rock;
							rock.doAction("Mine");
							sleep(random(450, 540));
							last = rock;
							modifyRockList(rock.getLocation());
							fail++;
						} else {
							highlight = null;
							if (!getMyPlayer().isMoving())
								walking.walkTileMM(walking
										.getClosestTileOnMap(rock.getLocation()));
						}
					} else {
						fail = 0;
					}
				} else {
					if (players.getMyPlayer().getInteracting() == null) {
						if (rock.isOnScreen()) {
							setRun();
							rock.doAction("Mine");
							sleep(random(350, 450));
							fail++;
							last = rock;
						} else {
							if (!getMyPlayer().isMoving())
								walking.walkTileMM(walking
										.getClosestTileOnMap(rock.getLocation()));
						}
					}
				}
			} else {
				if (calc.distanceTo(rockList.getFirst()) > 1)
					if (calc.tileOnScreen(rockList.getFirst()))
						walking.walkTileOnScreen(rockList.getFirst());
					else
						walking.walkTileMM(rockList.getFirst());
				idle();
			}
		}

		public void complete() {
			fail = 0;
		}

		public boolean isValid() {
			if (area != null)
				return area.contains(players.getMyPlayer().getLocation())
						&& canMine();
			else
				return canMine();
		}

		public String getDescription() {
			return "Processing & Executing Mining Procedures";
		}

		public void paint(Graphics g) {
			g.setColor(new Color(255, 0, 0, 100));
			for (Polygon p : highlight.getModel().getTriangles()) {
				g.drawPolygon(p);
			}
		}

		private void modifyRockList(RSTile cur) {
			for (int i = 0; i < rockList.size(); i++) {
				if (rockList.get(i).equals(cur))
					rockList.remove(i);
			}
			rockList.add(cur);
		}

		private RSObject[] getNearestBestRocks() {
			List<RSObject> rocks = getRocks();
			if (rocks.size() == 0) {
				return new RSObject[0];
			}
			final RSPlayer me = getMyPlayer();
			RSPlayer[] nearby = players.getAll(new Filter<RSPlayer>() {
				public boolean accept(RSPlayer player) {
					return !player.equals(me);
				}
			});
			int lowest_cost = 999999, next_cost = 999999;
			int lowest_ptr = 0, next_ptr = 0;
			double max = Math.pow(MAX_OTHER_DIST, DIST_EXPONENT);
			for (int i = 0; i < rocks.size(); ++i) {
				RSObject rock = rocks.get(i);
				RSTile loc = rock.getLocation();
				int cost = (int) Math.pow(calc.distanceTo(loc), DIST_EXPONENT)
						* MY_PLAYER_WEIGHT;
				for (RSPlayer player : nearby) {
					double dist = calc.distanceBetween(player.getLocation(),
							loc);
					if (dist < MAX_OTHER_DIST) {
						cost += max / Math.pow(dist, DIST_EXPONENT);
					}
				}
				if (cost < lowest_cost) {
					next_cost = lowest_cost;
					next_ptr = lowest_ptr;
					lowest_cost = cost;
					lowest_ptr = i;
				}
			}
			if (next_cost == 999999) {
				return new RSObject[] { rocks.get(lowest_ptr) };
			}
			RSObject[] nearest = new RSObject[] { rocks.get(lowest_ptr),
					rocks.get(next_ptr) };
			if (nearest[1].equals(last)) {
				RSObject temp = nearest[0];
				nearest[0] = nearest[1];
				nearest[1] = temp;
			}
			return nearest;
		}

		private List<RSObject> getRocks() {
			if (rock_tiles == null) { // objects.getAll each exec would be too
										// expensive
				RSObject[] rocks = objects.getAll(new Filter<RSObject>() {
					public boolean accept(RSObject o) {
						if (area.contains(o.getLocation())) {
							int oid = o.getID();
							for (int id : rockID) {
								if (id == oid) {
									return true;
								}
							}
							for (int id : emptyID) {
								if (id == oid) {
									return true;
								}
							}
						}
						return false;
					}
				});
				if (rocks.length > 0) {
					rock_tiles = new RSTile[rocks.length];
					for (int i = 0, rocksLength = rocks.length; i < rocksLength; i++) {
						rockList.add(rocks[i].getLocation());
						rock_tiles[i] = rocks[i].getLocation();
					}
				} else {
					return new ArrayList<RSObject>(0);
				}
			}
			// loop appropriate tiles only since rock tiles don't change
			ArrayList<RSObject> rocks = new ArrayList<RSObject>();
			for (RSTile t : rock_tiles) {
				RSObject obj = objects.getTopAt(t);
				if (obj != null) {
					int oid = obj.getID();
					for (int id : rockID) {
						if (id == oid) {
							rocks.add(obj);
							break;
						}
					}
				}
			}
			return rocks;
		}

		private boolean accept(RSObject rock) {
			if (objects.getTopAt(last.getLocation()) == null)
				return true;
			if (listContainsInt(rockIDs, objects.getTopAt(last.getLocation())
					.getID())) {
				for (int i = 1;; ++i) {
					if (!getMyPlayer().isIdle()) {
						return false;
					}
					if (i == 10) {
						break;
					}
					sleep(100);
				}
			}
			return true;
		}
	}

	InventoryController control;

	public boolean onStart() {
		gui = new AMPgui();
		particles = new HashSet<Particle>();
		gui.setVisible(true);
		actionSet = new HashSet<Actions>();
		control = new InventoryController();
		control.start();
		while (!canStart)
			sleep(20);
		if (!location.contains("Living"))
			actionSet.add(new MiningAction(rockIDs, emptyRockIDs, mineArea,
					smart) {

			});
		actionSet.add(new BankingAction() {
		});
		startEXP = skills.getCurrentExp(Skills.MINING);
		startTime = System.currentTimeMillis();
		startLevel = skills.getRealLevel(Skills.MINING);
		log("Loaded Action Queue, Total Loaded Actions: " + actionSet.size());
		camera.setPitch(true);
		return true;
	}

	private int getPlayersInArea(RSArea area) {
		int count = 0;
		RSPlayer[] all = players.getAll();
		for (RSPlayer p : all) {
			if (area.contains(p.getLocation()))
				count++;
		}
		return count;
	}

	@Override
	public int loop() {
		if (!canStart)
			return 200;

		if (worldSwitching && game.isLoggedIn()) {
			if (miningRune) {

			} else {
				if (switchingWorlds && !interfaces.get(906).isValid())
					if (game.logout(true))
						return 1000;
				if (getPlayersInArea(mineArea) > maxPlayers) {
					switchingWorlds = true;
					if (!interfaces.get(906).isValid())
						game.logout(true);
					return 200;
				}
			}
		}

		if (powerMine) {
			if (!canMine() || (mineXDropX && minedBeforeDrop >= setAmount)) {
				if (random(1, 3) == 2)
					inventory.dropAllExcept(true, Constants.PICKAXES);
				else
					inventory.dropAllExcept(false, Constants.PICKAXES);
				minedBeforeDrop = 0;
				return random(150, 250);
			}
		}

		for (Actions act : actionSet) {
			if (act.isValid()) {
				action = act;
				if (act.getDescription().contains("Walking"))
					lastMovedTimer(10);
				act.process();
			}
		}
		return random(300, 500);
	}

	@Override
	public void onRepaint(Graphics g) {
		if (mouse.isPressed()) {
			Particle pl = Particle.newParticle(mouse.getLocation().x,
					mouse.getLocation().y);
			pl.b = random(200, 255);
			particles.add(pl);
		}
		Iterator<Particle> i = particles.iterator();
		while (i.hasNext()) {
			Particle p = i.next();
			if (p.a <= 0 || p.x < 0) {
				i.remove();
			} else {
				g.setColor(new Color(p.r, p.g, p.b, p.a));
				g.fillOval(p.x - p.rad, p.y - p.rad, p.rad * 2, p.rad * 2);
				p.a -= 10;
				p.y += 1;
				p.x += p.v;
			}
		}
		long runTime = System.currentTimeMillis() - startTime;
		int XPGained = skills.getCurrentExp(Skills.MINING) - startEXP;
		int XPToLevel = skills.getExpToNextLevel(Skills.MINING);
		double gpPerHour = 0, oresPerSecond = 0, XPPerSecond = 0;
		action.paint(g);
		g.drawRect(8, 345, 505, 112);
		g.setColor(new Color(15, 15, 25, 250));
		g.fillRect(8, 345, 505, 112);
		g.setColor(Color.red);
		g.setFont(new Font("Times New Roman", Font.ITALIC, 16));
		g.drawString("Cyclic AMP - Revolutionizing Mining", 160, 360);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		g.setColor(Color.WHITE);
		g.drawString("Script statistics", 15, 370);
		g.drawString("Current Process: " + action.getDescription(), 15, 380);
		g.drawString("Runtime: " + Timer.format(runTime), 15, 390);
		g.setColor(Color.GREEN);
		if (game.isLoggedIn()) {
			if (XPGained > 1000) {
				XPPerSecond = (XPGained * 1000 / runTime);
				oresPerSecond = ((oresMined * 1000.0) / runTime);
				gpPerHour = totalProfit / (runTime / 3600000.0);
			}
			g.drawString("Financial Statistics", 15, 410);
			g.drawString("Average gp/hour: " + (int) gpPerHour, 15, 420);
			g.drawString("Average gp/sec: " + (int) (gpPerHour / 3600), 15, 430);
			g.drawString("Projected Current Profit: " + totalProfit, 15, 440);
			g.setColor(Color.BLUE);
			g.drawString("Skilling Statistics", 180, 400);
			g.drawString("Average exp/hour: " + (int) XPPerSecond * 3600, 180,
					410);
			g.drawString("Average exp/sec: " + (int) XPPerSecond, 180, 420);
			g.drawString("Average ore/hour: " + (int) (oresPerSecond * 3600),
					180, 430);
			g.drawString("Experience Until Level: " + XPToLevel, 180, 440);
			g.drawString("Ores Until Level: "
					+ (int) (XPToLevel / experiencePerRock), 180, 450);

			g.setColor(Color.MAGENTA);
			g.drawString("Total Ores Mined: " + oresMined, 370, 400);
			g.drawString("Total exp Gained: " + XPGained, 370, 410);

			g.setColor(Color.CYAN);
			g.drawString("Start Level: " + startLevel, 370, 430);
			g.drawString(
					"Current Level: " + skills.getRealLevel(Skills.MINING),
					370, 440);
		}
	}

	private boolean canMine() {
		return !inventory.isFull();
	}

	private boolean inShop() {
		return Constants.AUBURY_AREA.contains(getMyPlayer().getLocation());
	}

	private void addOres(int[] oreArray, boolean empty) {
		if (empty) {
			if (emptyRockIDs == null)
				emptyRockIDs = oreArray;
			else {
				int[] temp = new int[oreArray.length + emptyRockIDs.length];
				int i;
				for (i = 0; i < oreArray.length; i++)
					temp[i] = oreArray[i];
				for (; i < oreArray.length + emptyRockIDs.length; i++)
					temp[i] = emptyRockIDs[i - oreArray.length];
				emptyRockIDs = temp;
			}
		} else {
			if (rockIDs == null)
				rockIDs = oreArray;
			else {
				int[] temp = new int[oreArray.length + rockIDs.length];
				int i;
				for (i = 0; i < oreArray.length; i++)
					temp[i] = oreArray[i];
				for (; i < oreArray.length + rockIDs.length; i++)
					temp[i] = rockIDs[i - oreArray.length];
				rockIDs = temp;
			}
		}
	}

	private void idle() {
		if (random(0, 50) == 0) {
			int rand2 = random(1, 3);
			for (int i = 0; i < rand2; i++) {
				mouse.move(random(100, 700), random(100, 500));
				sleep(random(200, 700));
			}
			mouse.move(random(0, 800), 647, 50, 100);
			sleep(random(100, 1500));
			mouse.move(random(75, 400), random(75, 400), 30);
		}
		if (random(0, 50) == 0) {
			Point curPos = mouse.getLocation();
			mouse.move(random(0, 750), random(0, 500), 20);
			sleep(random(100, 300));
			mouse.move(curPos, 20, 20);
		}
		if (random(0, 50) == 0) {
			int angle = camera.getAngle() + random(-40, 40);
			if (angle < 0) {
				angle += 359;
			}
			if (angle > 359) {
				angle -= 359;
			}
			camera.setAngle(angle);
		}
		if (random(0, 50) == 0) {
			if (random(0, 4) == 0) {
				camera.setPitch(random(50, 80));
			} else {
				camera.setPitch(true);
			}
		}
	}

	private int[] extractIntegers(String text) {
		int[] ints = null;
		try {
			text = text.replaceAll(" ", "");
			final String[] strInts = text.split(",");
			ints = new int[strInts.length];
			for (int a = 0; a < strInts.length; a++) {
				ints[a] = Integer.parseInt(strInts[a]);
			}
		} catch (final Exception e) {
			log.severe("ERROR!");
		}
		return ints;
	}

	public class AMPgui extends JFrame {
		public AMPgui() {
			initComponents();
		}

		private void initalizationButtonActionPerformed(ActionEvent e) {
			String ws = preferredWorldTextField.getText();

			if (!ws.contains("List")) {
				worldSwitching = true;
				if (ws.contains("f2p")) {
					actionSet.add(new WorldSwitch(Constants.FREE_WORLDS));
				} else if (ws.contains("p2p")) {
					actionSet.add(new WorldSwitch(Constants.MEMBER_WORLDS));
				} else {
					actionSet.add(new WorldSwitch(extractIntegers(ws)));
				}
			}

			String mp = playerExceedsTextField.getText();
			if (!mp.isEmpty()) {
				if (!mp.equals("0"))
					maxPlayers = Integer.parseInt(mp);
				else
					miningRune = true;
			}

			location = locationComboBox.getSelectedItem().toString();
			for (int i = 0; i < strategyList.getSelectedIndices().length; i++) {
				String temp = strategyList.getSelectedValues()[i].toString();
				if (temp.contains("Avoid"))
					smart = true;
				if (temp.contains("Power"))
					powerMine = true;
				if (temp.contains("MXDX")) {
					mineXDropX = true;
					powerMine = true;
					if (!setAmountTextField.getText().isEmpty()) {
						setAmount = Integer.parseInt(setAmountTextField
								.getText());
					}
				}
			}

			for (int i = 0; i < oreList.getSelectedValues().length; i++) {
				String name = oreList.getSelectedValues()[i].toString();
				if (name.contains("Gold")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Gold ore").getMarketPrice(), grandExchange
							.getItemID("Gold ore"),
							Constants.EXPERIENCE_RATES[5]));
					if (name.equals("C Gold")) {
						addOres(Constants.C_GOLD_ROCK, false);
						addOres(Constants.C_EMPTY, true);
					}
				}
				if (name.contains("ron")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Iron ore").getMarketPrice(), grandExchange
							.getItemID("Iron ore"),
							Constants.EXPERIENCE_RATES[2]));
					addOres(Constants.IRON_ROCKS, false);
					addOres(Constants.IRON_EMPTY, true);
				}
				if (name.contains("oal")) {
					oreTypes.add(new OreType(name, grandExchange.lookup("Coal")
							.getMarketPrice(), grandExchange.getItemID("Coal"),
							Constants.EXPERIENCE_RATES[4]));
					if (!name.equals("C Coal")) {
						addOres(Constants.COAL_ROCKS, false);
						addOres(Constants.COAL_EMPTY, true);
					} else {
						addOres(Constants.C_COAL_ROCK, false);
						addOres(Constants.C_EMPTY, true);
					}
				}
				if (name.equals("Tin")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Tin ore").getMarketPrice(), grandExchange
							.getItemID("Tin ore"),
							Constants.EXPERIENCE_RATES[1]));
					addOres(Constants.TIN_ROCKS, false);
					addOres(Constants.TIN_EMPTY, true);
				}
				if (name.contains("opper")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Copper ore").getMarketPrice(), grandExchange
							.getItemID("Copper ore"),
							Constants.EXPERIENCE_RATES[1]));
					addOres(Constants.COPPER_ROCKS, false);
					addOres(Constants.COPPER_EMPTY, true);
				}
				if (name.equals("Essence")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Pure essence").getMarketPrice(), grandExchange
							.getItemID("Pure essence"),
							Constants.EXPERIENCE_RATES[0]));
					rockIDs = Constants.ESSENCE_ROCK;
					emptyRockIDs = Constants.ESSENCE_ROCK;
				}
				if (name.equals("Silver")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Silver ore").getMarketPrice(), grandExchange
							.getItemID("Silver ore"),
							Constants.EXPERIENCE_RATES[3]));
					addOres(Constants.SILVER_ROCKS, false);
					addOres(Constants.SILVER_EMPTY, true);
				}
				if (name.equals("Mithril")) {
					oreTypes.add(new OreType(name, grandExchange.lookup(
							"Mithril ore").getMarketPrice(), grandExchange
							.getItemID("Mithril ore"),
							Constants.EXPERIENCE_RATES[5]));
					addOres(Constants.MITHRIL_ROCKS, false);
					addOres(Constants.MITHRIL_EMPTY, true);
				}
			}
			int exp = 0;
			for (OreType o : oreTypes) {
				exp += o.getExpRate();
			}

			experiencePerRock = exp / (oreTypes.size() + 1);

			if (location.contains("Varro")) {
				bankTile = Constants.VARROCK_BANK_AREA.getCentralTile();
				if (!rockIDs.equals(Constants.ESSENCE_ROCK)) {
					mineArea = Constants.VARROCK_EAST_MINE;
					actionSet.add(new WalkingAction(
							Constants.VARROCK_BANK_AREA, "Varrock Bank") {
						protected boolean isTargetValid() {
							return !canMine();
						}
					});
					actionSet.add(new WalkingAction(
							Constants.VARROCK_EAST_MINE, "Varrock East Mines") {
						protected boolean isTargetValid() {
							return canMine();
						}
					});
				} else {
					mineArea = Constants.ESS_MINE;
					actionSet.add(new WalkingAction(Constants.ESS_MINE,
							"Essence Mine") {

						protected boolean isTargetValid() {
							return canMine();
						}

						public void process() {
							RSNPC arby = npcs.getNearest(Constants.AUBURY);
							if (inShop()) {
								if (arby != null)
									if (arby.isOnScreen())
										if (arby.doAction("Teleport"))
											sleep(random(1300, 1500));
							} else if (calc
									.distanceTo(Constants.VARROCK_DOOR_TILE) < 2) {
								if (objects
										.getAllAt(Constants.VARROCK_DOOR_TILE) != null) {
									RSObject door = null;
									for (RSObject o : objects
											.getAllAt(Constants.VARROCK_DOOR_TILE)) {
										if (o.getID() == Constants.VARROCK_CLOSED_DOOR)
											door = o;
									}
									if (door != null)
										door.doAction("Open");
									else {
										super.process();
									}
								}
							} else {
								if (calc.distanceTo(Constants.ESS_MINE
										.getCentralTile()) > 100)
									walking.walkTo(Constants.AUBURY_AREA
											.getCentralTile());
								else
									super.process();
							}
						}
					});

					actionSet.add(new WalkingAction(
							Constants.VARROCK_BANK_AREA, "Varrock East Bank") {

						@Override
						protected boolean isTargetValid() {
							return !canMine();
						}

						public void process() {
							RSObject obj = objects
									.getNearest(Constants.ESSENCE_PORTAL);
							if (obj != null) {
								if (calc.distanceTo(obj) > 3)
									walking.walkTileMM(obj.getLocation());
								if (obj.isOnScreen()) {
									if (obj.doAction("Enter"))
										sleep(random(1200, 1500));
								} else {
									walking.walkTileOnScreen(obj.getLocation());
								}
							} else {
								super.process();
							}
						}

					});
				}
			} else if (location.contains("Mining")) {
				mineArea = Constants.GUILD_MINE_AREA;
				bankTile = Constants.FALADOR_BANK_AREA.getCentralTile();
				actionSet.add(new WalkingAction(Constants.FALADOR_BANK_AREA,
						"Falador Bank") {
					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}

					public void process() {
						final RSPlayer me = players.getMyPlayer();
						if (Constants.GUILD_MINE_AREA.contains(me.getLocation())) {
							if (Constants.GUILD_EXIT_AREA.contains(me
									.getLocation())) {
								if (me.isIdle()) {
									RSObject ladder = objects
											.getNearest(Constants.GUILD_EXIT_LADDER);
									if (ladder != null) {
										ladder.doAction("Climb-up");
									}
								}
							} else {
								walking.walkTileMM(walking
										.getClosestTileOnMap(Constants.GUILD_EXIT_AREA
												.getCentralTile()));
							}
						} else {
							super.process();
						}
					}
				});

				actionSet.add(new WalkingAction(Constants.GUILD_MINE_AREA,
						"Mining Guild") {

					@Override
					protected boolean isTargetValid() {
						return canMine();
					}

					public void process() {
						RSObject ladder = objects
								.getNearest(Constants.GUILD_ENTRANCE_LADDER);
						if (ladder == null
								&& objects.getNearest(rockIDs) == null) {
							walking.walkTo(Constants.GUILD_ENTRANCE_AREA
									.getCentralTile());
						} else if (calc.tileOnMap(Constants.GUILD_ENTRANCE_AREA
								.getCentralTile()) || ladder != null) {
							if (ladder != null) {
								if (ladder.isOnScreen())
									ladder.doAction("Climb-down");
								else
									walking.walkTo(Constants.GUILD_ENTRANCE_AREA
											.getCentralTile());
							}
						} else {
							super.process();
						}
					}
				});
			} else if (location.equals("Al Karid")) {
				mineArea = Constants.ALKARID_MINE_AREA;
				bankTile = Constants.ALKARID_BANK_AREA.getCentralTile();
				actionSet.add(new WalkingAction(Constants.ALKARID_BANK_AREA,
						"Al Karid Bank") {
					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}
				});
				actionSet.add(new WalkingAction(Constants.ALKARID_MINE_AREA,
						"Al Karid Mines") {
					@Override
					protected boolean isTargetValid() {
						return canMine();
					}
				});
			} else if (location.equals("Draynor")) {
				mineArea = Constants.DRAYNOR_MINE_AREA;
				bankTile = Constants.DRAYNOR_BANK_AREA.getCentralTile();
				actionSet.add(new WalkingAction(Constants.DRAYNOR_BANK_AREA,
						"Draynor Bank") {
					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}
				});
				actionSet.add(new WalkingAction(Constants.DRAYNOR_MINE_AREA,
						"Draynor Mines") {
					@Override
					protected boolean isTargetValid() {
						return canMine();
					}
				});
			} else if (location.equals("Rimmington")) {
				mineArea = Constants.RIMMINGTON_MINE_AREA;
				bankTile = Constants.FALADOR_BANK_AREA.getCentralTile();
				actionSet.add(new WalkingAction(Constants.FALADOR_BANK_AREA,
						"Falador Bank") {
					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}
				});
				actionSet.add(new WalkingAction(Constants.RIMMINGTON_MINE_AREA,
						"Rimmington Mines") {
					@Override
					protected boolean isTargetValid() {
						return canMine();
					}
				});
			} else if (location.equals("Ardougne")) {
				mineArea = Constants.ARDOUGNE_MINE_AREA;
				bankTile = Constants.ARDOUGNE_BANK_AREA.getCentralTile();
				actionSet.add(new WalkingAction(Constants.ARDOUGNE_BANK_AREA,
						"Ardougne Bank") {
					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}
				});
				actionSet.add(new WalkingAction(Constants.ARDOUGNE_MINE_AREA,
						"Ardougne Mines") {
					@Override
					protected boolean isTargetValid() {
						return canMine();
					}
				});
			} else if (location.contains("Living")) {
				bankTile = Constants.LIVING_CAVERN_BANK_AREA.getCentralTile();
				JOptionPane.showMessageDialog(null,
						"Please make sure you are in world 84.");
				actionSet.add(new MiningAction(rockIDs, emptyRockIDs) {
				});

				actionSet.add(new WalkingAction(
						Constants.LIVING_CAVERN_BANK_AREA, "Deposit Box") {
					protected boolean isTargetValid() {
						return !canMine();
					}
				});

			} else if (location.contains("Dwarven")) {
				bankTile = Constants.FALADOR_BANK_AREA.getCentralTile();
				mineArea = Constants.DWARVEN_MINE_AREA;
				actionSet.add(new WalkingAction(
						Constants.DWARVEN_MINE_ENTRANCE,
						"Dwarven Mine Entrance") {

					@Override
					protected boolean isTargetValid() {
						return canMine()
								&& objects
										.getNearest(Constants.DWARVEN_STAIRS_DOWN) != null;
					}

					public void process() {
						RSObject stairs = objects
								.getNearest(Constants.DWARVEN_STAIRS_DOWN), door = objects
								.getNearest(Constants.DWARVEN_MINE_DOOR);
						if (door != null) {
							if (calc.distanceTo(door) <= 2) {
								door.doAction("Open");
							} else {
								if (calc.distanceTo(Constants.DWARVEN_MINE_ENTRANCE
										.getCentralTile()) < 7)
									stairs.doAction("Climb-down");
								else
									super.process();
							}
						}
					}

				});

				actionSet.add(new WalkingAction(Constants.FALADOR_BANK_AREA,
						"Falador Bank") {

					@Override
					protected boolean isTargetValid() {
						return !canMine();
					}

					public void process() {
						RSObject stairs = objects
								.getNearest(Constants.DWARVEN_STAIRS_UP);
						if (stairs != null) {
							if (stairs.isOnScreen())
								stairs.doAction("Climb-up");
							else
								walking.walkTileMM(walking
										.getClosestTileOnMap(stairs
												.getLocation()));
						} else {
							RSObject door = objects
									.getNearest(Constants.DWARVEN_MINE_DOOR);
							if (door != null) {
								if (calc.distanceTo(door) <= 4)
									door.doAction("Open");
								else
									super.process();
							} else {
								super.process();
							}
						}
					}
				});
			} else {
				JOptionPane.showMessageDialog(null,
						"Location Not Supported Yet.");
			}
			canStart = true;
			this.setVisible(false);
		}

		private void initComponents() {
			initalizationButton = new JButton();
			tabbedPane1 = new JTabbedPane();
			panel1 = new JPanel();
			label1 = new JLabel();
			label2 = new JLabel();
			locationComboBox = new JComboBox();
			label3 = new JLabel();
			label4 = new JLabel();
			label5 = new JLabel();
			scrollPane1 = new JScrollPane();
			oreList = new JList();
			scrollPane2 = new JScrollPane();
			strategyList = new JList();
			setValueLabel = new JLabel();
			setAmountTextField = new JTextField();
			label6 = new JLabel();
			panel2 = new JPanel();
			label7 = new JLabel();
			label8 = new JLabel();
			preferredWorldTextField = new JTextField();
			label9 = new JLabel();
			label10 = new JLabel();
			playerExceedsTextField = new JTextField();
			label11 = new JLabel();
			strategyList.setListData(Constants.STRATEGY_NAMES);
			oreList.setListData(Constants.ORE_NAMES);

			// ======== this ========
			setTitle("AMP Cyclic Edition");
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());

			// ---- initalizationButton ----
			initalizationButton.setText("Initialize");
			initalizationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					initalizationButtonActionPerformed(e);
				}
			});
			contentPane.add(initalizationButton, BorderLayout.SOUTH);

			// ======== tabbedPane1 ========
			{

				// ======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());

					// ---- label1 ----
					label1.setText("Mining Settings");
					label1.setForeground(Color.red);
					label1.setFont(new Font("Times New Roman", Font.ITALIC, 13));
					panel1.add(label1, new GridBagConstraints(0, 0, 4, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));

					// ---- label2 ----
					label2.setText("Mine Location:");
					label2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
					panel1.add(label2, new GridBagConstraints(0, 1, 4, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));
					panel1.add(locationComboBox, new GridBagConstraints(4, 1,
							5, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));
					locationComboBox
							.setModel(new javax.swing.DefaultComboBoxModel(
									Constants.Locations));

					// ---- label3 ----
					label3.setText("Please Select Options Below From List.");
					label3.setForeground(Color.red);
					label3.setFont(new Font("Times New Roman", Font.ITALIC, 12));
					panel1.add(label3, new GridBagConstraints(0, 2, 9, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- label4 ----
					label4.setText("Ore Selection");
					panel1.add(label4, new GridBagConstraints(0, 3, 4, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));

					// ---- label5 ----
					label5.setText("Stratergy");
					panel1.add(label5, new GridBagConstraints(5, 3, 4, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ======== scrollPane1 ========
					{
						scrollPane1.setViewportView(oreList);
					}
					panel1.add(scrollPane1, new GridBagConstraints(0, 4, 4, 8,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));

					// ======== scrollPane2 ========
					{
						scrollPane2.setViewportView(strategyList);
					}
					panel1.add(scrollPane2, new GridBagConstraints(5, 4, 4, 8,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- setValueLabel ----
					setValueLabel.setText("Set Value for X:");
					panel1.add(setValueLabel, new GridBagConstraints(0, 12, 5,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));
					panel1.add(setAmountTextField, new GridBagConstraints(5,
							12, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- label6 ----
					label6.setText("Note: Will mine ore selection RANDOMLY!");
					label6.setFont(new Font("Times New Roman", Font.ITALIC, 10));
					label6.setForeground(Color.red);
					panel1.add(label6, new GridBagConstraints(0, 13, 9, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
							0));
				}
				tabbedPane1.addTab("Mining Settings", panel1);

				// ======== panel2 ========
				{
					panel2.setLayout(new GridBagLayout());

					// ---- label7 ----
					label7.setText("World Switching");
					label7.setForeground(Color.red);
					label7.setFont(new Font("Times New Roman", Font.ITALIC, 13));
					panel2.add(label7, new GridBagConstraints(0, 0, 5, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));

					// ---- label8 ----
					label8.setText("Preferred worlds:");
					label8.setFont(new Font("Times New Roman", Font.PLAIN, 12));
					panel2.add(label8, new GridBagConstraints(0, 1, 5, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));

					// ---- preferredWorldTextField ----
					preferredWorldTextField.setText("List as: 11,23,44");
					panel2.add(preferredWorldTextField, new GridBagConstraints(
							5, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- label9 ----
					label9.setText("Leave  as is = disable, p2p = p2p only, f2p = f2p only");
					label9.setFont(new Font("Times New Roman", Font.ITALIC, 10));
					label9.setForeground(Color.red);
					panel2.add(label9, new GridBagConstraints(0, 2, 10, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- label10 ----
					label10.setText("Switch when players exceed:");
					label10.setFont(new Font("Times New Roman", Font.PLAIN, 12));
					panel2.add(label10, new GridBagConstraints(0, 3, 7, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0,
							0));
					panel2.add(playerExceedsTextField, new GridBagConstraints(
							7, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));

					// ---- label11 ----
					label11.setText("Input an integer above or 0 for rune mining");
					label11.setFont(new Font("Times New Roman", Font.ITALIC, 10));
					label11.setForeground(Color.red);
					panel2.add(label11, new GridBagConstraints(0, 4, 10, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0,
							0));
				}
				tabbedPane1.addTab("Advanced", panel2);

			}
			contentPane.add(tabbedPane1, BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization
			// //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY
		// //GEN-BEGIN:variables
		// Generated using JFormDesigner Open Source Project license - unknown
		private JButton initalizationButton;
		private JTabbedPane tabbedPane1;
		private JPanel panel1;
		private JLabel label1;
		private JLabel label2;
		private JComboBox locationComboBox;
		private JLabel label3;
		private JLabel label4;
		private JLabel label5;
		private JScrollPane scrollPane1;
		private JList oreList;
		private JScrollPane scrollPane2;
		private JList strategyList;
		private JLabel setValueLabel;
		private JTextField setAmountTextField;
		private JLabel label6;
		private JPanel panel2;
		private JLabel label7;
		private JLabel label8;
		private JTextField preferredWorldTextField;
		private JLabel label9;
		private JLabel label10;
		private JTextField playerExceedsTextField;
		private JLabel label11;
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}

	private static class Particle {

		private int r, g, b, a, x, y, v, rad;

		public static Particle newParticle(int x, int y) {
			Particle p = new Particle();
			int random = (int) (Math.random() * 0xffffff);
			p.rad = ((random >> 1) & 1) + 1;
			p.v = (random & 3) - 1;
			p.x = x;
			p.y = y;
			p.r = random & 0xff;
			p.g = (random >> 8) & 0xff;
			p.b = (random >> 16) & 0xff;
			p.a = 200;
			return p;
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	private int[] toIntArray(ArrayList<Integer> integerList) {
		int[] intArray = new int[integerList.size()];
		for (int i = 0; i < integerList.size(); i++) {
			intArray[i] = integerList.get(i);
		}
		return intArray;
	}

	ArrayList<Integer> list = new ArrayList<Integer>();

	class InventoryController extends Thread {
		public void run() {
			while (true) {
				if (!switchingWorlds) {
					inventoryCount = inventory.getCount()
							- inventory.getCount(Constants.PICKAXES);
					for (RSItem item : inventory.getItems()) {
						if (item.getID() != -1)
							list.add(item.getID());
					}
					inventoryItemIDs = toIntArray(list);
					if (inventoryCount > lastCount) {
						for (OreType ore : oreTypes) {
							if (ore.getItemID() == inventoryItemIDs[inventoryItemIDs.length - 1]) {
								totalProfit += ore.getPrice();
								minedBeforeDrop++;
								oresMined++;
							}
						}
					}
					lastCount = inventoryCount;
					list = new ArrayList<Integer>();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	class OreType {
		String name;
		int price, itemID, rate;

		public OreType(String name, int price, int itemID, int rate) {
			this.price = price;
			this.itemID = itemID;
			this.rate = rate;
			this.name = name;
		}

		public int getPrice() {
			return this.price;
		}

		public int getItemID() {
			return this.itemID;
		}

		public String getName() {
			return this.name;
		}

		public int getExpRate() {
			return this.rate;
		}
	}

	public void onFinish() {
		control.stop();
	}

	private boolean listContainsInt(int[] ints, int id) {
		for (int i : ints) {
			if (id == i)
				return true;
		}
		return false;
	}

	private long lastMoved;

	private void lastMovedTimer(int sec) {
		if (getMyPlayer().isMoving()) {
			lastMoved = System.currentTimeMillis() / 1000;
		} else if (((System.currentTimeMillis() / 1000) - lastMoved) > sec) {
			walking.walkTileMM(getMyPlayer().getLocation().randomize(2, 2));
			lastMoved = System.currentTimeMillis() / 1000;
		}
	}

	private void setRun() {
		if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
			walking.setRun(true);
			sleep(500);
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		// TODO Auto-generated method stub

	}
}

