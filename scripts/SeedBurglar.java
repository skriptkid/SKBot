package scripts;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.script.ScriptManifest;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.*;
import org.rsbot.util.GlobalConfiguration;
 
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Graphics2D;
 
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
 
@ScriptManifest(authors = { "endoskeleton" }, keywords = "Thieving", name = "SeedBurglar", version = 2.67,
        description = "Burgles seeds at Draynor")
 
 
public class SeedBurglar extends Script implements MessageListener, PaintListener {
    private final ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);
 
    private int[] junkSeeds = new int[100];
    private int[] goodSeeds = new int[100];
 
    private RSTile[] currentTiles = { new RSTile(3081,3250), new RSTile(3092, 3243) };
 
    private boolean dropping = false;
    private boolean usingGloves = false;
    private boolean shouldRun = false;
    private boolean equipNewGloves;
    private boolean canPick = true;
    private boolean isBanking = true;
    private boolean isEating = true;
 
    private int priceCount = 0;
    private int pickPocketCount = 0;
    private int caughtCount = 0;
    private int startLevel;
    private int withdrawAmount;
    private int foodID = -1;
    private int eatAT = -1;
    private int nextCheck = -1;
    private int pickTimerCount;
    private long startXP;
 
    private Timer stunned = new Timer(1);
    private Timer startTime;
    private Timer pickTimer = new Timer(1);
    private Timer eatTimer = new Timer(1);
    private Timer dropTimer = new Timer(1);
    private Timer bankTimer = new Timer(1);
    private Timer onScreenTimer = new Timer(1);
    //private KTimer worldSwitch = new KTimer(1);
 
    private String exitReason;
 
    private Seed[] seedArray;
 
    private State lastState;
 
    private final CameraHandler cameraHandler = new CameraHandler();
    private final Timer randomCamera = new Timer(random(10000,20000));
 
    private final Filter<RSNPC> GUARD_FILTER = new Filter<RSNPC>() {
        @Override
        public boolean accept(RSNPC n) {
            RSCharacter c = n.getInteracting();
            return c != null && c instanceof RSPlayer && c.equals(getMyPlayer()) && (n.getID() == 2236 || n.getID() == 8435);
        }
    };
 
    private boolean powerMode = false;
    private int[] powerModeItems;
    private boolean runFromCombat = true;
 
    private int timeRandoms = 0;
    private int[] timeRange = new int[2];
 
    private static final int[] ALL_SEED_IDS = {
            5319, 5307, 5305, 5322, 5099, 5310, 5308, 5102, 5101, 5096, 5324, 5306, 5291, 5103, 5292, 5097,
            5281, 5098, 5294, 5105, 5106, 5280, 5297, 5311, 5104, 5293, 5318, 5282, 5309, 5304, 5296, 5300,
            5295, 5303, 5302, 5100, 5323, 5299, 5301, 5298, 5320, 5321
    };
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#####.##");
    private static final DecimalFormat DECIMAL_FORMAT_COMMA = new DecimalFormat("###,###");
    private static final int[] FARMER_ID = { 2234, 2235 };
    private static final int[] BANK_BOOTH_IDS = { 2213, 34752, 11402 };
    private static final int[] FOOD_IDS = {
            315, 325, 319, 347, 355, 333, 339, 351, 329, 361, 10135, 5003, 379, 361, 373, 7946, 385, 397, 391,
            2309, 2003, 2011, 2289, 2293, 2297, 2301, 1891, 1897, 7068, 6701, 6703, 7054, 6705, 7060, 1993, 1978,
            1971, 4608,  1883, 1901, 1895, 1899, 1893, 7919 };
    private static final int GLOVE_ID = 10075;
 
 
 
    enum State {
        PICK("Picking"),
        BANK("Banking"),
        EAT("Eating"),
        DROP("Dropping"),
        RUN("Running aray"),
        LOGOUT("Logging out"),
        EQUIP("Equipping gloves"),
        WALK_TO_BANK("Walking to Bank"),
        WALK_TO_FARMER("Walking to Farmer"),
        TRAPPED("Trapped"),
        FIGHT("Fighting back"),
        ANTIBAN("Antiban"),
        SWITCH_WORLD("Switching world"),
        TELEPORT("Teleporting back");
 
        String name;
 
        State(String name) {
            this.name = name;
        }
    }
 
    public void onRepaint(Graphics g) {
        g.setColor(Color.black);
        if (!game.isLoggedIn()) {
            return;
        }
        Graphics2D graphics = (Graphics2D)g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (startTime != null) {
            long runTime = startTime.getElapsed();
            String r = Timer.format(startTime.getElapsed());
            r = r.replace(" ", "");
            r = r.trim();
            double percent = skills.getMaxLevel(Skills.THIEVING) == 99 ? 100 : skills.getPercentToNextLevel(Skills.THIEVING);
            int levelGain = (skills.getMaxLevel(Skills.THIEVING) - startLevel);
            int secs = ((int) ((runTime / 1000) % 60));
            int mins = ((int) (((runTime / 1000) / 60) % 60));
            int hours = ((int) ((((runTime / 1000) / 60) / 60) % 60));
            int expGain = (int)(skills.getCurrentExp(Skills.THIEVING) - startXP);
            double xpPH = 0;
            double profitPH = 0;
            if ((mins > 0 || hours > 0 || secs > 0) && expGain > 0) {
                xpPH = ((((double) expGain)/(double)(secs + (mins * 60) + (hours * 60 * 60)) * 60) * 60);
                if (priceCount > 0) {
                    profitPH = ((((double) priceCount)/(double)(secs + (mins * 60) + (hours * 60 * 60)) * 60) * 60);
                }
            }
 
            ArrayList<String> paintStrings = new ArrayList<String>();
            paintStrings.add("Status: " + (lastState == null ? "" : lastState.name));
            paintStrings.add("Runtime: " + r);
            paintStrings.add("Exp Gained: " + DECIMAL_FORMAT_COMMA.format(expGain) + "(" + startLevel + "+" + levelGain + ")");
            paintStrings.add("Exp/hour: " + DECIMAL_FORMAT_COMMA.format((int)xpPH));
            paintStrings.add("Pickpocket Count: " + DECIMAL_FORMAT_COMMA.format(pickPocketCount));
            paintStrings.add("Times caught: " + DECIMAL_FORMAT_COMMA.format(caughtCount));
 
            paintStrings.add("Profit: " + DECIMAL_FORMAT_COMMA.format(priceCount));
            paintStrings.add("Profit/hour: " + DECIMAL_FORMAT_COMMA.format((int)profitPH));
 
            Utility.drawDefaultPaint(graphics, "SeedBurglar " + properties.version(), 15, 75, 150, (int)percent, paintStrings);
            if (game.getCurrentTab() == Game.TAB_INVENTORY) {
                graphics.setFont(new Font(null, Font.BOLD, 8));
                graphics.setColor(new Color(255, 255, 255, 75));
                for (RSItem i : getItems(goodSeeds)) {
                    Rectangle bounds = i.getComponent().getArea();
                    String[] name = i.getName().replace("seed", "").trim().split(">");
                    graphics.draw(bounds);
                    graphics.drawString(name[1], bounds.x, bounds.y+bounds.height-5);
 
                }
            }
            if (dropping && game.getCurrentTab() == Game.TAB_INVENTORY) {
                graphics.setColor(new Color(255, 0, 0, 75));
                for (RSItem i : getItems(junkSeeds)) {
                    graphics.draw(i.getComponent().getArea());
                }
            }
            if (stunned.isRunning()) {
                double perc = (((double)stunned.getRemaining() / 4000));
                Point p = calc.tileToScreen(getMyPlayer().getLocation());
                p.setLocation(p.x - 20, p.y + 30);
                graphics.setColor(new Color(255, 0, 0));
                graphics.fillRoundRect(p.x, p.y, 40, 6, 5, 5);
                graphics.setColor(new Color(0, 255, 0));
                graphics.fillRoundRect(p.x, p.y, (int)(perc * 40), 6, 5, 5);
                graphics.setColor(Color.BLACK);
                graphics.drawRoundRect(p.x, p.y, 40, 6, 5, 5);
                graphics.drawRoundRect(p.x, p.y, (int)(perc * 40), 6, 5, 5);
            }
            if (pickTimer.isRunning()) {
                double perc = (((double)pickTimer.getRemaining() / pickTimerCount));
                Point p = mouse.getLocation();
                graphics.setColor(new Color(0, 0, 0, 150));
                graphics.fillArc(p.x - 10, p.y - 10, 20, 20, 90, (int)(perc*360));
            }
            Stroke originalStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(1.2f));
            Point p = mouse.getLocation();
            g.setColor(Color.black);
            graphics.drawArc(p.x - 10, p.y - 10, 20, 20, 50, 90);
            graphics.drawArc(p.x - 10, p.y - 10, 20, 20, -50, -90);
            graphics.setColor(Color.red);
            graphics.drawLine(p.x - 10, p.y, p.x + 10, p.y);
            graphics.setStroke(originalStroke);
        }
    }
 
    @Override
    public void messageReceived(MessageEvent ev) {
        if (ev.getSender().equals("")) {
            String s = ev.getMessage();
            if (s.contains("You've been stunned")) {
                stunned = new Timer(4000);
                caughtCount++;
                canPick = true;
            }
            if (s.contains("You steal")) {
                canPick = true;
                int picked;
                if (s.contains("2")) {
                    picked = 2;
                } else if (s.contains("3")) {
                    picked = 3;
                } else if (s.contains("4")) {
                    picked = 4;
                } else {
                    picked = 1;
                }
                for (Seed element : seedArray) {
                    if (s.toLowerCase().contains(element.getName().toLowerCase().replace(" seed", "").trim())) {
                        priceCount += (element.getPrice() * picked);
                    }
                }
                pickPocketCount++;
            }
            if (s.contains("You don't have enough space in your inventory")) {
                canPick = true;
                pickPocketCount++;
            }
            if (s.toLowerCase().contains("gloves of silence have worn out")) {
                equipNewGloves = true;
                nextCheck += random(50, 60);
            }
            if (s.contains("You attempt")) {
                canPick = false;
 
            }
        }
    }
 
    int getHealth() {
        RSComponent i = interfaces.getComponent(748, 8);
        try {
            return Integer.parseInt(i.getText());
        } catch(NumberFormatException e) {
            return -1;
        }
    }
 
    State getState() {
        // if (worldSwitch.isDone()) {
        //     return STATE.SWITCH_WORLD;
        //}
        RSNPC farmer = npcs.getNearest(FARMER_ID);
        if (getHealth() < eatAT) {
            if (!isEating) {
                return State.LOGOUT;
            }
            if (game.getCurrentTab() == Game.TAB_INVENTORY && inventory.containsOneOf(FOOD_IDS) && !bank.isOpen() && isEating && !eatTimer.isRunning()) {
                return State.EAT;
            }
            if (game.getCurrentTab() == Game.TAB_INVENTORY && !inventory.containsOneOf(FOOD_IDS) && !isBanking) {
                return State.LOGOUT;
            }
        }
        if (guardCheck()) {
            return runFromCombat ? State.RUN : State.FIGHT;
        }
        if (usingGloves && equipNewGloves && !bank.isOpen() && game.getCurrentTab() == Game.TAB_INVENTORY && inventory.contains(GLOVE_ID)) {
            return State.EQUIP;
        }
        if (calc.distanceTo(new RSTile(3441, 3693)) <= 50 || calc.distanceTo(new RSTile(2707, 5727)) <= 50) {
            return State.TELEPORT;
        }
        if (dropping && !powerMode && !bank.isOpen() && !dropTimer.isRunning() && game.getCurrentTab() == Game.TAB_INVENTORY &&
                (inventory.containsOneOf(junkSeeds) && stunned.isRunning() || (!inventory.containsOneOf(FOOD_IDS) &&
                        inventory.containsOneOf(junkSeeds)) || inventory.isFull() && inventory.containsOneOf((junkSeeds)))) {
            return State.DROP;
        }
        if (isBanking) {
            if (/*bankTimer.isDone() && */game.getCurrentTab() == Game.TAB_INVENTORY && (!inventory.containsOneOf(FOOD_IDS) || inventory.isFull() && !powerMode)) {
                if (game.getCurrentTab() == Game.TAB_INVENTORY && inventory.containsOneOf(FOOD_IDS) && getHealth() < (skills.getMaxLevel(Skills.CONSTITUTION) * 10)) {
                    return State.EAT;
                }
                if (foodID != -1) {
                    if (calc.distanceTo((currentTiles[1])) > 5) {
                        return State.WALK_TO_BANK;
                    }
                    return State.BANK;
                }
            }
        }
        if (farmer == null && calc.distanceTo(currentTiles[0]) > 5) {
            return State.WALK_TO_FARMER;
        }
        return random(0, 200) == 20 ? State.ANTIBAN : State.PICK;
 
    }
 
    public synchronized int loop() {
        try {
            if (!shouldRun) {
                return -1;
            }
            if (!game.isLoggedIn()) {
                return 100;
            }
            mouse.setSpeed(random(4, 7));
 
            if (!walking.isRunEnabled() && walking.getEnergy() > 30) {
                walking.setRun(true);
            }
 
            if (inventory.isItemSelected()) {
                mouse.click(true);
            }
 
            if (!canPick && !pickTimer.isRunning()) {
                canPick = true;
            }
            if (interfaces.get(620).isValid()) {
                interfaces.getComponent(620, 18).doClick();
                return random(1000, 2000);
            }
            if (interfaces.get(468).isValid()) {
                interfaces.getComponent(468, 11).doClick();
                return random(1000, 2000);
            }
            if (usingGloves && caughtCount > nextCheck && !bank.isOpen()) {
                if (!equipment.containsOneOf(GLOVE_ID)) {
                    equipNewGloves = true;
 
                    sleep(random(500, 800));
                }
                nextCheck += random(55, 85);
            }
 
            if (game.getCurrentTab() != Game.TAB_INVENTORY && !bank.isOpen()) {
                game.openTab(Game.TAB_INVENTORY);
                sleep(random(300, 600));
            }
 
 
            if (walking.getDestination() != null && calc.distanceTo(walking.getDestination()) > 5 &&
                    calc.distanceTo(walking.getDestination()) < 15) {
                return random(100, 200);
            }
 
 
            switch((lastState = getState())) {
                case TELEPORT:
                    RSObject cabinet = objects.getNearest(12258);
                    if (cabinet != null) {
                        if (!cabinet.isOnScreen()) {
                            walking.walkTileMM(walking.getClosestTileOnMap(cabinet.getLocation()));
                            return random(500, 600);
                        }
                        if (cabinet.doClick()) {
                            return random(5000, 8000);
                        }
                    } else {
                        RSItem glory = equipment.getItem(Equipment.NECK);
                        if (glory != null && glory.getName().contains("glory (")) {
                            glory.doAction("Draynor");
                            return random(5000, 6000);
                        } else {
                            exitReason = "Stuck in daemonheim, no glory to teleport back! (Equip one next time)";
                            shouldRun = false;
                            return -1;
                        }
                    }
                    break;
 
                case SWITCH_WORLD:
                    env.disableRandom("login");
                    game.switchWorld(28);
                    //worldSwitch = new KTimer(30000000);
                    env.enableRandom("login");
                    break;
 
                case EQUIP:
                    if (inventory.getItem(GLOVE_ID).doAction("Wear")) {
                        equipNewGloves = false;
                    }
                    break;
 
                case LOGOUT:
                    game.logout(true);
                    break;
 
                case RUN:
                    if (walking.getDestination() != null) break;
                    walking.walkTileMM(getNearestTile(currentTiles[1]));
                    //RSTile myLoc = getMyPlayer().getLocation();
                    //walking.walkTileMM(getNearestTile(new RSTile(myLoc.getX() + random(10, 15), myLoc.getY() + random(10, 15))));
                    // walking.walkPathMM(generateRegionPath(getMyPlayer().getLocation().randomizeTile(3, 3), new RSTile(myLoc.getX() + random(10, 15), myLoc.getY() + random(10, 15))));
 
                    break;
 
                case BANK:
                    RSObject bankBooth = objects.getNearest(BANK_BOOTH_IDS);
                    if (bankBooth == null || bankTimer.isRunning()) {
                        break;
                    } else if (calc.distanceTo(bankBooth.getLocation()) < 10 && bankBooth.isOnScreen()) {
                        if (bank.isOpen()) {
                            if (inventory.getCount() > 0) {
                                if (powerMode) {
                                    int count = inventory.getCount();
                                    bank.depositAllExcept(powerModeItems);
                                    waitForInventoryChange(count);
                                } else {
                                    int count = inventory.getCount();
                                    bank.depositAll();
                                    waitForInventoryChange(count);
                                }
                            }
                            if (powerMode) {
                                if (usingGloves && inventory.getCount(GLOVE_ID) < 3) {
                                    int count = inventory.getCount();
                                    bank.withdraw(GLOVE_ID, 3 - inventory.getCount(GLOVE_ID));
                                    waitForInventoryChange(count);
                                }
                                if (!inventory.isFull()) {
                                    int count = inventory.getCount();
                                    bank.withdraw(foodID, 0);
                                    waitForInventoryChange(count);
                                }
                            } else {
                                if (inventory.getCount(false, foodID) != withdrawAmount) {
                                    if (bank.getCount(foodID) == 0) {
                                        exitReason = "Out of food";
                                        shouldRun = false;
                                        return -1;
                                    }
                                    bank.withdraw(foodID, withdrawAmount);
                                    if (usingGloves && bank.getCount(GLOVE_ID) > 0) {
                                        bank.withdraw(GLOVE_ID, 1);
                                    }
                                    bankTimer = new Timer(5000);
                                }
                            }
                            break;
 
                        } else {
 
                            bankBooth.doAction("Use-quickly");
                            waitForBank(true);
 
                            break;
                        }
 
                    }
 
                    break;
 
 
                case EAT:
                    RSItem[] food = getItems(FOOD_IDS);
                    if (food != null && food.length > 0 && !eatTimer.isRunning()) {
                        food[0].doClick(true);
                        eatTimer = new Timer(1000);
                    }
                    break;
 
 
                case DROP:
                    RSItem[] drop = getItems(junkSeeds);
                    if (drop != null && drop.length > 0) {
                        if (random(0, 10) >= 5) {
                            boolean full = inventory.isFull();
                            for (RSItem aDrop : drop) {
                                if (inventory.contains(foodID) && !stunned.isRunning() && !full || isPaused()) {
                                    break;
                                }
                                aDrop.doAction("Drop");
                            }
                        } else {
                            dropVertical(junkSeeds);
                        }
                        dropTimer = new Timer(2000);
                    }
 
                    break;
 
                case PICK:
                    final RSNPC farmer = npcs.getNearest(FARMER_ID);
                    if (farmer == null) {
                        return random(300, 600);
                    } else {
                        onScreenTimer = new Timer(5000);
                    }
 
                    if (calc.distanceTo(farmer.getLocation()) > 6 || bank.isOpen() || !onScreenTimer.isRunning()) {
                        if (getMyPlayer().isMoving()) {
                            break;
                        }
                        if (bank.isOpen()) {
                            bank.close();
                            waitForBank(false);
                        }
                        walking.walkTileMM(getNearestTile(farmer.getLocation()));
                        //walkPathOnScreen(generateRegionPath(getMyPlayer().getLocation(), farmer.getLocation()), 15);
                        break;
                    } else if (farmer.isOnScreen()) {
                        if (stunned != null && stunned.isRunning()) {
                            while (stunned != null && stunned.isRunning() && getState().equals(State.PICK)) {
                                Point p = farmer.getScreenLocation();
                                if (stunned.getRemaining() < 1000 && p.distance(mouse.getLocation()) > 40) {
                                    //mouse.move()
                                    Point point = mouse.getLocation();
 
                                    Point p2 = new Point((int)point.getX() + random(-30, 30), (int)point.getY() + random(-30, 30));
                                    if (calc.pointOnScreen(p2)) {
                                        mouse.move(p2);
                                    }
 
                                }
                                sleep(random(100,200));
                            }
                            break;
                        } else if (!pickTimer.isRunning()) {
                            for (Polygon p : farmer.getModel().getTriangles()) {
                                if (p.contains(mouse.getLocation()) || menu.contains("Pickpocket Master Farmer")) {
                                    mouse.moveRandomly(5);
                                    menu.doAction("Pickpocket");
                                    pickTimerCount = timeGenerate();
                                    pickTimer = new Timer(pickTimerCount);
                                    return random(100, 300);
                                }
                            }
                            if (farmer.doAction("Pickpocket")) {
                                pickTimerCount = timeGenerate();
                                pickTimer = new Timer(pickTimerCount);
                            }
                        }
                        break;
                    }
                    break;
 
                case WALK_TO_BANK:
                    if (walking.getDestination() != null) break;
                    walking.walkTileMM(getNearestTile(currentTiles[1]));
                    //walkPathOnScreen(generateRegionPath(getMyPlayer().getLocation(), currentTiles[1]), 15);
                    //walking.walkTo(walking.getClosestTileOnMap(currentTiles[1]));
                    break;
 
                case WALK_TO_FARMER:
                    if (bank.isOpen()) {
                        bank.close();
                        waitForBank(false);
                    }
                    if (walking.getDestination() != null) break;
                    walking.walkTileMM(getNearestTile(currentTiles[0]));
                    //walkPathOnScreen(generateRegionPath(new RSTile(3092, 3248), currentTiles[0]), 15);
                    //walking.walkTo(walking.getClosestTileOnMap(currentTiles[0]));
                    break;
 
                case FIGHT:
                    if (!combat.isAutoRetaliateEnabled()) {
                        combat.setAutoRetaliate(true);
                        return random(1000, 1500);
                    }
                    break;
 
                case ANTIBAN:
                    int gamble = random(0, 70);
                    if (gamble < 10) {
                        mouse.moveOffScreen();
                        sleep(random(1000, 10000));
                    } else if (gamble >= 10 && gamble < 20) {
                        sleep(random(1000, 30000));
                    } else if (gamble >= 20 && gamble < 30) {
                        mouse.moveRandomly(random(0, 200));
                        sleep(random(0, 2000));
                    } else if (gamble >= 30 && gamble < 40) {
                        RSNPC[] randomNPC = npcs.getAll(new Filter<RSNPC>() {
                            @Override
                            public boolean accept(RSNPC rsnpc) {
                                return rsnpc.isOnScreen();
                            }
                        });
                        if (randomNPC != null && randomNPC.length > 0) {
                            mouse.move(randomNPC[random(0, randomNPC.length)].getScreenLocation());
                            sleep(random(0, 2000));
                        }
                    } else if (gamble >= 40 && gamble < 50) {
                        RSPlayer[] randomPlayer = players.getAll(new Filter<RSPlayer>() {
                            @Override
                            public boolean accept(RSPlayer rsplayer) {
                                return rsplayer.isOnScreen();
                            }
                        });
                        if (randomPlayer != null && randomPlayer.length > 0) {
                            mouse.move(randomPlayer[random(0, randomPlayer.length)].getScreenLocation());
                            sleep(random(0, 2000));
                        }
                    } else if (gamble >= 50 && gamble < 60) {
                        RSObject[] randomObject = objects.getAll(new Filter<RSObject>() {
                            @Override
                            public boolean accept(RSObject rsObject) {
                                return rsObject.isOnScreen();
                            }
                        });
                        if (randomObject != null && randomObject.length > 0) {
                            randomObject[random(0, randomObject.length)].doHover();
                            sleep(random(0, 2000));
                        }
                    } else if (gamble >= 60 && gamble < 70) {
                        game.openTab(Game.TAB_STATS);
                        interfaces.getComponent(Skills.INTERFACE_TAB_STATS, Skills.INTERFACE_THIEVING).doHover();
                        sleep(random(0, 5000));
                    }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return random(100, 300);
    }
    public void switchWorld(int worldToSwitchTo) {
        if (game.isLoggedIn()) {
            game.logout(true);
            sleep(random(5000, 10000));
        }
        int currentWorld = Integer.parseInt(interfaces.getComponent(910, 10).getText());
        RSComponent[] worldsAvailable = interfaces.getComponent(910, 68).getComponents();
 
        for (RSComponent world : worldsAvailable) {
            if (!world.isValid()) {
                interfaces.getComponent(910, 85).getComponent(5).doAction("");
            }
            if (Integer.parseInt(world.getText()) == worldToSwitchTo
                    && Integer.parseInt(world.getText()) != currentWorld) {
                world.doAction("Select");
                sleep(1000);
                if (currentWorld == Integer.parseInt(world
                        .getText())) {
                    interfaces.getComponent(906, 181).doAction("Click Here To Play");
                    break;
                }
            }
        }
    }
    int cameraLoop() {
        if (isPaused()) {
            return 1000;
        }
        if (bank.isOpen()) {
            return 100;
        }
        switch(getState()) {
            case PICK:
                RSNPC farmer = npcs.getNearest(FARMER_ID);
                if (farmer != null && !farmer.isOnScreen() && calc.distanceTo(farmer.getLocation()) <= 8) {
                    camera.turnToTile(farmer.getLocation());
                    camera.setPitch(true);
                }
                break;
 
            case BANK:
                RSObject booth = objects.getNearest(2213);
                if (booth != null && !booth.isOnScreen() && calc.distanceTo(booth.getLocation()) <= 5) {
                    camera.turnToTile(booth.getLocation());
                    camera.setPitch(true);
                }
                break;
 
            case EAT:
            case TRAPPED:
            case WALK_TO_BANK:
            case WALK_TO_FARMER:
            case DROP:
            case RUN:
            case EQUIP:
                if (!randomCamera.isRunning() && random(0,100) <= 3) {
                    camera.setAngle(random(0,360));
                }
                break;
        }
        return random(500,600);
    }
 
    RSItem[] getItems(int... IDs) {
        List<RSItem> out = new ArrayList<RSItem>();
        RSItem[] items = inventory.getItems();
        for (RSItem item : items) {
            if (item == null) {
                continue;
            }
            for (int ID : IDs) {
                if (item.getID() == ID) {
                    out.add(item);
                    break;
                }
            }
        }
        return out.toArray(new RSItem[out.size()]);
    }
 
    void dropVertical(final int... IDs) {
        if (game.getCurrentTab() != Game.TAB_INVENTORY)
            game.openTab(Game.TAB_INVENTORY);
        if (!inventory.containsOneOf(IDs))
            return;
        RSItem[] invenItems = inventory.getItems();
        if (invenItems.length == 0)
            return;
        for (int i = 0; i < 4; i++) {
            for (int current = i; current < 28; current += 4) {
                if (isPaused() || !shouldRun) {
                    break;
                }
                boolean dropIt = false;
                for (int ID : IDs) {
                    if (invenItems[current].getID() == ID)
                        dropIt = true;
                }
                if (dropIt) {
                    if (invenItems[current].getComponent().getArea().contains(mouse.getLocation())) {
                        mouse.click(false);
                        sleep(10, 50);
                        if (menu.isOpen()) {
                            menu.doAction("Drop");
                        }
                    } else {
                        invenItems[current].doAction("Drop");
                    }
                    sleep(100, 200);
                }
            }
        }
    }
 
    int timeGenerate() {
        if (timeRandoms <= 0) {
            timeRandoms = random(1, 200);
            timeRange[0] = random(300, 600);
            timeRange[1] = timeRange[0]+random(50, 200);
        }
        timeRandoms--;
        return random(timeRange[0], timeRange[1]);
    }
 
    boolean guardCheck() {
        RSNPC[] guards = npcs.getAll(GUARD_FILTER);
        for (RSNPC guard : guards) {
            RSCharacter interacting = guard.getInteracting();
            if (interacting == null) {
                continue;
            }
            if (interacting.equals(getMyPlayer())) {
                return true;
            }
        }
        return false;
    }
 
    void waitForBank(boolean open) {
        int ms = random(3000, 4000);
        for (int i = 0; i < ms; i+=20) {
            if (bank.isOpen() == open) {
                return;
            }
            sleep(20);
        }
    }
 
    void waitForInventoryChange(int count) {
        int ms = random(3000, 4000);
        for (int i = 0; i < ms; i += 20) {
            if (inventory.getCount() != count) {
                break;
            }
            sleep(20);
        }
    }
 
    public void onFinish() {
        shouldRun = false;
        log("Script stopped after: "+Timer.format(startTime.getElapsed()));
        if (exitReason != null) {
            log("Reason we stopped:" + exitReason);
        }
        log("Gained: "+DECIMAL_FORMAT.format((int)(skills.getCurrentExp(Skills.THIEVING) - startXP))+" Experience.");
        log("Gained: "+DECIMAL_FORMAT_COMMA.format(priceCount)+ "gp in seeds.");
    }
 
    public boolean onStart() {
        try {
            BufferedReader version = new BufferedReader(new InputStreamReader(new URL("http://home.comcast.net/~endoskeleton/version.txt").openConnection().getInputStream()));
            if (Double.parseDouble(version.readLine()) > properties.version()) {
                if (JOptionPane.showConfirmDialog(null, "An update has been found. Would you like to update now?", "Update!", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JFileChooser fc = new JFileChooser();
                    fc.setCurrentDirectory(new File(GlobalConfiguration.Paths.getScriptsSourcesDirectory()));
                    fc.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.getAbsolutePath().endsWith("SeedBurglar.java") || f.isDirectory();
                        }
                        @Override
                        public String getDescription() {
                            return "Please choose the SeedBurglar.java file.";
                        }
                    });
                    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        fc.getSelectedFile().delete();
                        fc.getSelectedFile().createNewFile();
                        BufferedReader in =  new BufferedReader(new InputStreamReader(new URL("http://home.comcast.net/~endoskeleton/SeedBurglar.java").openConnection().getInputStream()));
                        BufferedWriter out = new BufferedWriter(new FileWriter(fc.getSelectedFile().getAbsolutePath()));
                        String lineIn;
                        while ((lineIn = in.readLine()) != null) {
                            out.write(lineIn);
                            out.newLine();
                            out.flush();
                        }
                        log("Updated to latest version, please compile your scripts!");
                        return false;
                    }
 
                }
                return false;
            }
        } catch(Exception e) {
            log.log(Level.SEVERE, "Update failed!");
        }
        SeedBurglarGUI gui = new SeedBurglarGUI(this);
        while (gui.isVisible()) {
            sleep(100);
        }
        if (!gui.guiDone) {
            return false;
        }
        startXP = skills.getCurrentExp(Skills.THIEVING);
        startLevel = skills.getMaxLevel(Skills.THIEVING);
        eatAT = (int)(skills.getMaxLevel(Skills.CONSTITUTION)*random(4.0, 6.0));
        new PriceThread().start();
        startTime = new Timer(0);
        cameraHandler.start();
        //worldSwitch = new KTimer(100);
        shouldRun = gui.guiDone;
        return gui.guiDone;
    }
 
    public RSTile getNearestTile(RSTile target) {
        if (calc.tileOnMap(target)) {
            return target;
        }
        RSTile t = getMyPlayer().getLocation();
        return new RSTile(t.getX() + ((target.getX() - t.getX())/2), t.getY() + ((target.getY() - t.getY()) / 2));
    }
 
    class Seed {
 
        final String name;
        int price = -1;
        final int id;
 
        public Seed(String name, int id) {
            this.name = name;
            this.id = id;
        }
 
        public String getName() {
            return name;
        }
 
        public int getPrice() {
            return price;
        }
 
        public int getID() {
            return id;
        }
 
        public void setPrice(int price) {
            this.price = price;
        }
 
        public void setPrice() {
            this.setPrice(grandExchange.loadItemInfo(id).getMarketPrice());
        }
    }
 
    private class CameraHandler extends Thread {
        @Override
        public void run() {
            try {
                while (shouldRun) {
                    int loop = cameraLoop();
                    if (loop < 0) {
                        break;
                    }
                    Thread.sleep(loop);
                }
            } catch(Exception e) {
                log("Camera handling thread died.");
            }
        }
    }
 
    private class PriceThread extends Thread {
        @Override
        public void run() {
            for (Seed s : seedArray) {
                s.setPrice();
            }
        }
    }
 
    static class Utility {
        static RoundRectangle2D mainPaintBox;
        static GradientPaint gp;
        static void drawDefaultPaint(Graphics2D g, String scriptName, int x, int y, int width,
                                     int percent, ArrayList<String> strings) {
            int height = strings.size() * 15;
            if (mainPaintBox == null) {
                mainPaintBox = new RoundRectangle2D.Double(x, y, width, height, 5, 5);
                gp = new GradientPaint(x, y, new Color(0, 0, 0, 50), x, y + height, new Color(0, 0, 0, 200), false);
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawLogo(scriptName, g, x, y-20);
            g.setFont(new Font("Arial", Font.BOLD, 10));
 
            g.setPaint(gp);
            g.fill(mainPaintBox);
            g.setColor(Color.black);
            g.draw(mainPaintBox);
 
            g.setColor(Color.white);
            x +=2;
            y += 10;
            for (String s : strings) {
                g.drawString(s, x, y);
                y += 15;
            }
            drawPercentBar(x-14, y-(10+(15*strings.size())), 10, height, percent, g);
            g.setColor(Color.white);
            g.rotate(90.0 * Math.PI / 180.0, x-11, y-1);
            g.drawString(percent+"%", x-100, y);
            g.rotate(270.0 * Math.PI / 180.0, x-11, y-1);
 
        }
        static void drawLogo(String s, Graphics2D g, int x, int y) {
            drawWord("endoskeleton's", new Point(x, y), g);
            drawWord(s, new Point(x+10, y+15), g);
        }
        static void drawWord(String s, Point p, Graphics2D g) {
            g.setFont(new Font("Georgia", Font.BOLD, 14));
            g.setColor(Color.BLACK);
            for(int i = -1; i < 4; i++) {
                g.drawString(s, p.x+i, p.y+i);
            }
            g.setColor(Color.WHITE);
            g.drawString(s, p.x, p.y);
        }
        static void drawPercentBar(int x, int y, int width, int height, int percent, Graphics2D g) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRoundRect(x, y, width, height, 5, 5);
            g.setColor(new Color(0, 255, 0, 100));
            g.fillRoundRect(x, y+height-(int)((float)percent/(float)100*height), width, (int)((float)percent/(float)100*height), 5, 5);
 
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y+height-(int)((float)percent/(float)100*height), width, (int)((float)percent/(float)100*height), 5, 5);
            g.drawRoundRect(x, y, width, height, 5, 5);
        }
    }
 
    class SeedBurglarGUI extends JFrame {
        boolean start = false;
 
        final int[] FOOD_IDS = {
                315, 325, 319, 347, 355, 333, 339, 351, 329, 361, 10135, 5003, 379, 361, 373, 7946, 385, 397, 391,
                2309, 2003, 2011, 2289, 2293, 2297, 2301, 1891, 1897, 7068, 6701, 6703, 7054, 6705, 7060, 1993, 1978,
                1971, 4608,  1883};
        final String[] FOOD_NAMES = { "Shrimps", "Sardine", "Anchovies", "Herring", "Mackerel", "Trout", "Cod", "Pike", "Salmon",
                "Tuna", "Rainbow Fish", "Cave Eel", "Lobster", "Bass", "Swordfish", "Monkfish",
                "Shark", "Sea turtle", "Manta ray", "Bread", "Stew", "Curry", "Plain pizza", "Meat pizza", "Anchovy pizza",
                "Pineapple pizza", "Cake", "Chocolate cake", "Tuna and corn", "Baked potato", "Potato with butter",
                "Chilli potato", "Potato with cheese", "Tuna potato", "Jug of wine", "Cup of tea", "Kebab", "Super kebab",
                "Ugthanki kebab", "Rocktail" };
 
        final int[] JUNK_SEED_IDS = { 5319, 5307, 5305, 5322, 5099, 5310, 5308, 5102, 5101, 5096, 5324, 5306, 5291, 5103, 5292, 5097,
                5281, 5098, 5294, 5105, 5106, 5280, 5297, 5311, 5104, 5293, 5318, 5282, 5309, 5320, 5323 };
        final String[] JUNK_SEED_NAMES = { "Onion seed", "Hammerstone seed", "Barley seed", "Tomato seed", "Woad seed", "Krandorian seed",
                "Asgarnian seed", "Cadavaberry seed", "Redberry seed", "Marigold seed", "Cabbage seed", "Jute seed", "Guam seed",
                "Dwellberry seed", "Marrentill seed", "Rosemary seed", "Belladonna seed", "Nasturnium seed", "Harralander seed",
                "Whiteberry seed", "Poison ivy seed", "Cactus seed", "Irit seed", "Wildblood seed", "Jangerberry seed", "Tarromin seed",
                "Potato seed", "Mushroom seed", "Yanillian seed", "Sweetcorn seed", "Strawberry seed"};
 
        final int[] GOOD_SEED_IDS = { 5304, 5296, 5300, 5295, 5303, 5302, 5100, 5299, 5301, 5298, 5321 };
        final String[] GOOD_SEED_NAMES = { "Torstol seed", "Toadflax seed", "Snapdragon seed", "Ranarr seed", "Dwarf weed seed",
                "Lantadyme seed", "Limpwurt seed", "Kwuarm seed", "Cadantine seed", "Avantoe seed",
                "Watermelon seed" };
 
        JTabbedPane jTabbedPane;
        JButton startButton;
        JPanel contentPane;
        JLabel foodLabel;
        JCheckBox foodCheckBox;
        JList foodList;
        JScrollPane jScrollPane8;
        JPanel foodPanel;
        JCheckBox seedCheckBox;
        JList junkSeeds;
        JScrollPane jScrollPane12;
        JList goodSeeds;
        JScrollPane jScrollPane14;
        JPanel seedPanel;
        JCheckBox bankCheckBox;
        JCheckBox glovesOfSilenceCheckBox;
        JCheckBox antiBanCheckBox;
        JComboBox withdrawComboBox;
        JCheckBox powerModeComboBox;
        JCheckBox combatCheckBox;
        JPanel jPanel16;
        JPanel changeLogPanel;
        private JScrollPane changeLogScroll;
        private JTextArea changeLogText;
 
        final DefaultListModel goodModel = new DefaultListModel();
        final DefaultListModel junkModel = new DefaultListModel();
 
        final SeedBurglar script;
 
        boolean guiDone = false;
 
        public SeedBurglarGUI(SeedBurglar script) {
            super();
            this.script = script;
            initializeComponent();
            this.setVisible(true);
            while (!guiDone) {
                if (!this.isVisible()) {
                    break;
                }
                script.sleep(100);
            }
            this.setVisible(false);
        }
 
        void initializeComponent() {
            jTabbedPane = new JTabbedPane();
            startButton = new JButton();
            contentPane = (JPanel)this.getContentPane();
            foodLabel = new JLabel();
            foodCheckBox = new JCheckBox();
            foodList = new JList();
            jScrollPane8 = new JScrollPane();
            foodPanel = new JPanel();
            seedCheckBox = new JCheckBox();
            junkSeeds = new JList(junkModel);
            jScrollPane12 = new JScrollPane();
            goodSeeds = new JList(goodModel);
            jScrollPane14 = new JScrollPane();
            seedPanel = new JPanel();
            bankCheckBox = new JCheckBox();
            glovesOfSilenceCheckBox = new JCheckBox();
            antiBanCheckBox = new JCheckBox();
            withdrawComboBox = new JComboBox();
            powerModeComboBox = new JCheckBox();
            combatCheckBox = new JCheckBox();
            changeLogPanel = new JPanel();
            jPanel16 = new JPanel();
            changeLogScroll = new JScrollPane();
            changeLogText = new JTextArea();
 
            jTabbedPane.addTab("Food", foodPanel);
            jTabbedPane.addTab("Seeds", seedPanel);
            jTabbedPane.addTab("Misc", jPanel16);
            jTabbedPane.addTab("Change log", changeLogPanel);
 
            changeLogText.setColumns(20);
            changeLogText.setEditable(false);
            changeLogText.setRows(5);
            changeLogText.setText(changeLog);
            changeLogScroll.setViewportView(changeLogText);
 
            //changeLogPanel.add(changeLogScroll, BorderLayout.CENTER);
            GroupLayout changeLogPanelLayout = new GroupLayout(changeLogPanel);
            changeLogPanel.setLayout(changeLogPanelLayout);
            changeLogPanelLayout.setHorizontalGroup(
                    changeLogPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(changeLogPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(changeLogScroll, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                            .addContainerGap())
            );
            changeLogPanelLayout.setVerticalGroup(
                    changeLogPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(changeLogPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(changeLogScroll, GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                            .addContainerGap())
            );
 
            startButton.setText("Start");
            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    start();
                }
 
            });
 
            contentPane.setLayout(new BorderLayout(0, 0));
            contentPane.add(jTabbedPane, BorderLayout.CENTER);
            contentPane.add(startButton, BorderLayout.SOUTH);
 
            foodLabel.setHorizontalAlignment(SwingConstants.CENTER);
            foodLabel.setText("Select the food you would like to use.");
 
            foodCheckBox.setBorderPaintedFlat(true);
            foodCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            foodCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            foodCheckBox.setText("Use food");
            foodCheckBox.setSelected(true);
            foodCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    foodList.setEnabled(foodCheckBox.isSelected());
                }
 
            });
 
            foodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            String[] foods = new String[FOOD_IDS.length];
            for (int i = 0; i < FOOD_IDS.length; i++) {
                foods[i] = FOOD_NAMES[i]  + " - " + FOOD_IDS[i];
            }
            foodList.setListData(foods);
            if (game.getCurrentTab() == Game.TAB_INVENTORY && inventory.containsOneOf(FOOD_IDS)) {
                for (int i = 0; i < FOOD_IDS.length; i++) {
                    if (inventory.contains(FOOD_IDS[i])) {
                        foodList.setSelectedIndex(i);
                        foodList.ensureIndexIsVisible(i);
                        break;
                    }
                }
            }
 
            jScrollPane8.setViewportView(foodList);
 
            foodPanel.setLayout(new BorderLayout(0, 0));
            foodPanel.add(jScrollPane8, BorderLayout.CENTER);
            foodPanel.add(foodCheckBox, BorderLayout.NORTH);
            foodPanel.add(foodLabel, BorderLayout.SOUTH);
            foodPanel.setOpaque(false);
 
            seedCheckBox.setBorderPaintedFlat(true);
            seedCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            seedCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            seedCheckBox.setText("Drop Junk Seeds");
            seedCheckBox.setSelected(false);
            seedCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e)
                {
                    goodSeeds.setEnabled(seedCheckBox.isSelected());
                    junkSeeds.setEnabled(seedCheckBox.isSelected());
                }
 
            });
 
            junkSeeds.setEnabled(false);
            junkModel.addElement("Junk seeds - Click to move to keep pile");
            for (int i = 0; i < JUNK_SEED_IDS.length; i++) {
                junkModel.addElement(JUNK_SEED_NAMES[i] + " - " + JUNK_SEED_IDS[i]);
            }
            junkSeeds.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {}
 
                public void mousePressed(MouseEvent e) {
                    if (junkSeeds.getSelectedIndex() == 0) {
                        junkSeeds.setSelectedIndex(-1);
                    }
                }
 
                public void mouseReleased(MouseEvent e) {
                    if (junkSeeds.getSelectedIndex() != 0) {
                        goodModel.addElement(junkModel.get(junkSeeds.getSelectedIndex()));
                        junkModel.remove(junkSeeds.getSelectedIndex());
                    }
                }
 
                public void mouseEntered(MouseEvent e) {}
 
                public void mouseExited(MouseEvent e) {}
            });
 
            jScrollPane12.setViewportView(junkSeeds);
 
            goodSeeds.setEnabled(false);
            goodModel.addElement("Good seeds - Click to move to junk pile");
            for (int i = 0; i < GOOD_SEED_IDS.length; i++) {
                goodModel.addElement(GOOD_SEED_NAMES[i] + " - " + GOOD_SEED_IDS[i]);
            }
            goodSeeds.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {}
 
                public void mousePressed(MouseEvent e) {
                    if (goodSeeds.getSelectedIndex() == 0) {
                        goodSeeds.setSelectedIndex(-1);
                    }
                }
 
                public void mouseReleased(MouseEvent e) {
 
                    if (goodSeeds.getSelectedIndex() != 0) {
                        junkModel.addElement(goodModel.get(goodSeeds.getSelectedIndex()));
                        goodModel.remove(goodSeeds.getSelectedIndex());
                    }
                }
 
                public void mouseEntered(MouseEvent e) {}
 
                public void mouseExited(MouseEvent e) {}
            });
 
            jScrollPane14.setViewportView(goodSeeds);
 
            seedPanel.setLayout(new BorderLayout(0, 0));
            seedPanel.add(seedCheckBox, BorderLayout.NORTH);
            seedPanel.add(jScrollPane12, BorderLayout.SOUTH);
            seedPanel.add(jScrollPane14, BorderLayout.CENTER);
            seedPanel.setOpaque(false);
 
            bankCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            bankCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            bankCheckBox.setText("Bank for Food");
            bankCheckBox.setSelected(true);
            bankCheckBox.setBorderPaintedFlat(true);
            bankCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    withdrawComboBox.setEnabled(bankCheckBox.isSelected());
                }
 
            });
            withdrawComboBox.addItem("Withdraw-1");
            withdrawComboBox.addItem("Withdraw-5");
            withdrawComboBox.addItem("Withdraw-10");
            withdrawComboBox.setSelectedIndex(1);
 
            glovesOfSilenceCheckBox.setEnabled(skills.getRealLevel(Skills.HUNTER) >= 54);
            glovesOfSilenceCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            glovesOfSilenceCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            glovesOfSilenceCheckBox.setText("Use Gloves of Silence");
            glovesOfSilenceCheckBox.setBorderPaintedFlat(true);
            glovesOfSilenceCheckBox.setSelected(game.getCurrentTab() == Game.TAB_INVENTORY && inventory.contains(GLOVE_ID));
 
            antiBanCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            antiBanCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            antiBanCheckBox.setText("Use Anti-Ban");
            antiBanCheckBox.setSelected(true);
            antiBanCheckBox.setBorderPaintedFlat(true);
 
            powerModeComboBox.setHorizontalAlignment(SwingConstants.CENTER);
            powerModeComboBox.setHorizontalTextPosition(SwingConstants.LEADING);
            powerModeComboBox.setText("Power Mode");
            powerModeComboBox.setSelected(false);
            powerModeComboBox.setBorderPaintedFlat(true);
            powerModeComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    withdrawComboBox.setEnabled(!powerModeComboBox.isSelected());
                }
 
            });
 
            combatCheckBox.setText("Run from combat");
            combatCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            combatCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
            combatCheckBox.setSelected(false);
            combatCheckBox.setBorderPaintedFlat(true);
 
            jPanel16.setLayout(new GridLayout(10, 1, 0, 4));
            jPanel16.add(bankCheckBox, 0);
            jPanel16.add(withdrawComboBox, 1);
            jPanel16.add(glovesOfSilenceCheckBox, 2);
            jPanel16.add(combatCheckBox, 3);
            jPanel16.add(powerModeComboBox, 4);
            jPanel16.setOpaque(false);
 
            this.setTitle("Seed Burglar "+properties.version());
            this.setLocationRelativeTo(this.getOwner());
            this.setSize(new Dimension(330, 341));
            this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            this.setResizable(false);
        }
 
        String changeLog = "* 2.66:\n" +
                "Changelog tab added.\n" +
                "If accidently teleports to daemonheim, it will use an equipped amulet of glory to teleport back. Otherwise, stop script.\n" +
                "\n" +
                "* 2.67:\n" +
                "Added teleporting back to draynor if you have completed the christmas event.";
 
 
        void start() {
            if (foodList.isSelectionEmpty()) {
                return;
            }
            junkModel.remove(0);
            goodModel.remove(0);
            script.dropping = seedCheckBox.isSelected();
 
            script.junkSeeds = new int[junkModel.getSize()];
            for (int i = 0; i < junkModel.getSize(); i++) {
                script.junkSeeds[i] = Integer.parseInt(junkModel.get(i).toString().split(" - ")[1]);
            }
            if (script.dropping) {
                script.seedArray = new Seed[goodModel.getSize()];
                script.goodSeeds = new int[goodModel.getSize()];
                for (int i = 0; i < goodModel.getSize(); i++) {
                    String[] s = goodModel.get(i).toString().split(" - ");
                    script.seedArray[i] = new Seed(s[0], Integer.parseInt(s[1]));
                    script.goodSeeds[i] = Integer.parseInt(s[1]);
                }
            } else {
                ArrayList<Seed> s = new ArrayList<Seed>();
                for (int i = 0; i < goodModel.getSize(); i++) {
                    String[] str = goodModel.get(i).toString().split(" - ");
                    s.add(new Seed(str[0], Integer.parseInt(str[1])));
                }
                for (int i = 0; i < junkModel.getSize(); i++) {
                    String[] str = junkModel.get(i).toString().split(" - ");
                    s.add(new Seed(str[0], Integer.parseInt(str[1])));
                }
 
                script.seedArray = new Seed[s.size()];
                script.goodSeeds = new int[s.size()];
                for (int i = 0; i < s.size(); i++) {
                    script.seedArray[i] = s.get(i);
                    script.goodSeeds[i] = s.get(i).getID();
                }
            }
            script.usingGloves = glovesOfSilenceCheckBox.isSelected();
            script.withdrawAmount = Integer.parseInt(withdrawComboBox.getSelectedItem().toString().split("-")[1]);
            script.isEating = foodCheckBox.isSelected();
            if (script.isEating) {
                script.foodID = Integer.parseInt(foodList.getSelectedValue().toString().split(" - ")[1]);
            }
            script.isBanking = bankCheckBox.isSelected();
            if ((script.powerMode = powerModeComboBox.isSelected())) {
                RSItem[] items = getItems(ALL_SEED_IDS);
                script.powerModeItems = new int[items.length + 1];
                for (int i = 0; i < items.length; i++) {
                    powerModeItems[i] = items[i].getID();
                }
                script.goodSeeds = powerModeItems;
                script.powerModeItems[powerModeItems.length-1] = GLOVE_ID;
            }
            script.runFromCombat = combatCheckBox.isSelected();
            guiDone = true;
        }
    }
}