import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Objects;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Store;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;
import org.rsbot.util.GlobalConfiguration;

/**
 *
 * @author Matt
 */

@ScriptManifest(authors = { "dpedroia15" }, keywords = {"Mining"}, name = "Miner", version = 2.46, description = "Fixed camera twitching.")
public class Miner extends Script implements MessageListener, PaintListener, MouseListener, MouseMotionListener {

    private boolean isBanking, isStealing, isSuperheating;

    private RSArea bankArea;
    private RSArea mineArea;
    private RSTilePath[] path;      //from mine to bank

    private RSObject[] rockStore;
    private RSObject currentRock, nextRock;

    private Ore oreA, oreB, oreC, currentOre;
    private int gemsMined, gemProfit;
    
    private Timer switchRockTimer;
    
    private GUI gui;

    @Override
    public boolean onStart() {
        log("dpedroia15's Miner");
        path = new RSTilePath[5];
        gui = new GUI();
        while(gui.isVisible())
            sleep(500);
        if(walker == null)
            return false;
        new ItemPriceLoader().start();
        new ImageLoader().start();
        switchRockTimer = new Timer(2000);
        cameraTimer = new Timer(0);
        cameraTimer.start();
        generalBounds = new Rectangle(-1,-1);
        expBounds = new Rectangle(-1,-1);
        profitBounds = new Rectangle(-1,-1);
        currentOre = oreA;
        startLevel = skills.getRealLevel(Skills.MINING);
        startXP = skills.getCurrentExp(Skills.MINING);
        showPaint = true;
        clock = new Clock();
        return true;
    }

    @Override
    public int loop() {
        walker.tryToRun();
        if(interfaces.canContinue()) {
            interfaces.clickContinue();
            sleep(500,1000);
        }
        switch(getState()) {
            case DISPOSING:
                return disposeInventory(isBanking);
            case WALKING:
                return walker.doWalk(false, path);
            case MINING:
                return mine();
            default:
                return 0;
        }
    }

    private enum State { DISPOSING, WALKING, MINING };
    private State getState() {
        if(inventory.isFull())
            return State.DISPOSING;
        else if(!atMine())
            return State.WALKING;
        else
            return State.MINING;
    }
    
    private int disposeInventory(boolean banking) {
        if(!banking) {
            while(inventory.getCountExcept(Constants.PICKAXE_IDS) != 0)
                dropAllExcept(false,Constants.PICKAXE_IDS);
            return 0;
        }
        else if(isSuperheating)
            return 0;
        else {
            rockStore = null;
            nextRock = null;
            currentRock = null;
            if(!atBank())
                return walker.doWalk(true, path);
            else
                return bank();
        }
    }

    public void dropAllExcept(boolean leftToRight, int... items) {
        while (inventory.getCountExcept(items) != 0) {
            if (!leftToRight) {
                for (int c = 0; c < 4; c++) {
                    for (int r = 0; r < 7; r++) {
			boolean found = false;
			for (int i = 0; i < items.length && !found; ++i) {
                            found = items[i] == inventory.getItems()[c + r * 4].getID();
			}
			if (!found) {
                            dropItem(c, r);
			}
                    }
		}
            }
            else {
                for (int r = 0; r < 7; r++) {
                    for (int c = 0; c < 4; c++) {
			boolean found = false;
			for (int i = 0; i < items.length && !found; ++i) {
                            found = items[i] == inventory.getItems()[c + r * 4].getID();
			}
			if (!found) {
                            dropItem(c, r);
			}
                    }
		}
            }
            sleep(random(500, 800));
        }
    }

    public void dropItem(int col, int row) {
        if (interfaces.get(210).getComponent(2).getText().equals("Click here to continue")) {
            sleep(random(800, 1300));
            if (interfaces.get(210).getComponent(2).getText().equals("Click here to continue")) {
                interfaces.get(210).getComponent(2).doClick();
                sleep(random(150, 200));
            }
        }
	if (game.getCurrentTab() != Game.TAB_INVENTORY
            && !interfaces.get(Bank.INTERFACE_BANK).isValid()
            && !interfaces.get(Store.INTERFACE_STORE).isValid()) {
            game.openTab(Game.TAB_INVENTORY);
        }
	if (col < 0 || col > 3 || row < 0 || row > 6)
            return;
	if(inventory.getItems()[col + row * 4].getID() == -1)
            return;
	Point p;
            p = mouse.getLocation();
	if (p.x < 563 + col * 42 || p.x >= 563 + col * 42 + 32
            || p.y < 213 + row * 36 || p.y >= 213 + row * 36 + 32) {
            mouse.hop(inventory.getInterface().getComponents()[row * 4 + col].getCenter(), 10, 10);
	}
	mouse.click(false);
	sleep(random(10, 25));
	menu.doAction("drop");
	sleep(random(25, 50));
    }

    private int bank() {
        if(!bank.isOpen()) {
            if(!openBank())
                return 0;
            sleep(500,750);
        }
        if(!bank.isOpen())
            return 0;
        else {
            while(inventory.getCountExcept(false,Constants.PICKAXE_IDS) > 0) {
                try {
                    bank.depositAllExcept(Constants.PICKAXE_IDS);
                }
                catch(NullPointerException e) {
                    break;
                }
            }
            return 0;
        }
    }

    public boolean openBank() {
        try {
            if (!bank.isOpen()) {
                if (menu.isOpen()) {
                    mouse.moveSlightly();
                    sleep(random(20, 30));
		}
		RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
		RSNPC banker = npcs.getNearest(Bank.BANKERS);
		final RSObject bankChest =objects.getNearest(Bank.BANK_CHESTS);
		int lowestDist = calc.distanceTo(bankBooth);
		if ((banker != null) && (calc.distanceTo(banker) < lowestDist)) {
                    lowestDist = calc.distanceTo(banker);
                    bankBooth = null;
		}
		if ((bankChest != null) && (calc.distanceTo(bankChest) < lowestDist)) {
                    bankBooth = null;
                    banker = null;
		}
		if (((bankBooth != null) && (calc.distanceTo(bankBooth) < 5) &&
                    calc.tileOnMap(bankBooth.getLocation()) &&
                    calc.canReach(bankBooth.getLocation(), true)) ||
                    ((banker != null) &&
                    (calc.distanceTo(banker) < 8) &&
                    calc.tileOnMap(banker.getLocation()) &&
                    calc.canReach(banker.getLocation(), true)) ||
                    ((bankChest != null) &&
                    (calc.distanceTo(bankChest) < 8) &&
                    calc.tileOnMap(bankChest.getLocation()) &&
                    calc.canReach(bankChest.getLocation(), true) && !bank.isOpen())) {
                    if (bankBooth != null) {
                        if (bankBooth.doAction("Use-Quickly")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count < 10) {
                                sleep(random(200, 400));
				if (players.getMyPlayer().isMoving())
                                    count = 0;
                            }
			}
                        else
                            turnToObject(bankBooth);
                    }
                    else if (banker != null) {
                        if (banker.doAction("Bank ")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count < 10) {
                                sleep(random(200, 400));
				if (players.getMyPlayer().isMoving())
                                    count = 0;
                            }
			}
                        else
                            turnToCharacter(banker, 20);
                    }
                    else if (bankChest != null) {
                        if (bankChest.doAction("Bank") || menu.doAction("Use")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count < 10) {
                                sleep(random(200, 400));
				if (players.getMyPlayer().isMoving()) 
                                    count = 0;
								
                            }
			}
                        else
                            turnToObject(bankChest);
                    }
            }
            else {
                if (bankBooth != null)
                    walker.walk(bankBooth.getLocation());
                else if (banker != null)
                    walker.walk(banker.getLocation());
		else if (bankChest != null)
                    walker.walk(bankChest.getLocation());
            }
	}
	return bank.isOpen();
        }
        catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean atBank() {
        return bankArea.contains(getMyPlayer().getLocation());
    }

    private Walker walker;
    private abstract class Walker {

        public abstract int doWalk(boolean toBank, RSTilePath... path);

        public boolean walk(RSTile tile) {
            return walk(false,tile);
        }
        
        public boolean walk(boolean onScreen, RSTile tile) {
            return walking.getPath(tile).traverse();
        }

        public boolean tryToRun() {
            if(!walking.isRunEnabled() && walking.getEnergy() > random(35,65)) {
                walking.setRun(true);
                sleep(1000,1250);
            }
            return walking.isRunEnabled();
        }

        public void tryToRest() {
            if(!tryToRun())
                walking.rest();
        }
    }

    private RSTilePath getReverse(RSTilePath path) {
        RSTilePath reversed = walking.newTilePath(path.toArray());
        return reversed.reverse();
    }
    
    private class RegularWalker extends Walker {

        public int doWalk(boolean toBank, RSTilePath... path) {
            RSTilePath walkingPath = path[0];
            if(!toBank)
                walkingPath = getReverse(walkingPath);
            if(walkingPath.traverse())
                return randomAntiban();
            return 0;
        }

    }

    private class MiningGuildWalker extends Walker {

        @Override
        public int doWalk(boolean toBank, RSTilePath[] path) {
            if(isAboveMiningGuild()) {
                if(toBank)
                    path[0].traverse();
                else {
                    RSObject ladder = objects.getNearest(Constants.ABOVE_MINING_GUILD_LADDER_ID);
                    if(ladder == null || calc.distanceTo(ladder) > 7)
                        getReverse(path[0]).traverse();
                    else {
                        if(!ladder.isOnScreen())
                            turnToObject(ladder);
                        try {
                             if(!getMyPlayer().isMoving()) {
                                if(ladder.doAction("Climb")) {
                                    if(waitToMove(Constants.MAX_TIMEOUT)) {
                                        if(waitForIdle(Constants.MAX_TIMEOUT))
                                            return randomAntiban();
                                    }
                                }
                             }
                             else if(!isMouseHoveringObject(ladder))
                                 hoverMouseOverObject(ladder);
                        }
                        catch (NullPointerException npe) {}
                    }
                }
            }
            else {
                if(toBank) {
                    RSObject ladder = objects.getNearest(Constants.BELOW_MINING_GUILD_LADDER_ID);
                    if(ladder == null || calc.distanceTo(ladder) > 7)
                        path[1].traverse();
                    else {
                        if(!ladder.isOnScreen())
                            turnToObject(ladder);
                        try {
                            if(!getMyPlayer().isMoving()) {
                                if(ladder.doAction("Climb")) {
                                    if(waitForAnimations(Constants.MAX_TIMEOUT,828))
                                        return randomAntiban();
                                }
                            }
                            else if(!isMouseHoveringObject(ladder))
                                hoverMouseOverObject(ladder);
                        }
                        catch (NullPointerException npe) {}
                    }
                }
                else
                    getReverse(path[1]).traverse();
            }
            return randomAntiban();
        }

        private boolean isAboveMiningGuild() {
            //Hacky :-/
            int y = getMyPlayer().getLocation().getY();
            return y < 9000;
        }

    }

    private boolean atMine() {
        return mineArea.contains(getMyPlayer().getLocation());
    }

    private int mine() {
        if(inventory.isFull())
            return 0;
        if(isMining()) {
            if(isRockStillValid(currentRock, currentOre.getRockIDs()))
                return randomAntiban();
        }
        RSTile dest = walking.getDestination();
        if(dest != null) {
            if(walking.isLocal(dest)) {
                if(calc.distanceTo(dest) > 3)
                    return randomAntiban();
            }
        }
        RSObject rock = getNextRock();
        if(rock != null) {
            nextRock = rock;
            if(!rock.isOnScreen()) {
                if(!turnToObject(rock,10))
                    camera.moveRandomly(500);
            }
            if(!rock.isOnScreen()) {
                if(!isRockStillValid(rock, currentOre.getRockIDs())) {
                    if(currentRock != null) {
                        if(currentRock.equals(rock))
                            currentRock = null;
                    }
                }
                else if(walker.walk(walking.getClosestTileOnMap(rock.getLocation())))
                    sleep(1000,2000);
                return 0;
            }
            if(!isMouseHoveringObject(rock))
                hoverMouseOverObject(rock);
            else {
                if(!isRockStillValid(rock, currentOre.getRockIDs()))
                    return 0;
                if(!isRockStillValid(currentRock, currentOre.getRockIDs())) {
                    if(rock.doAction("Mine")) {
                        sleep(250);
                        currentRock = rock;
                        if(calc.distanceTo(rock) > 1) {
                            if(!waitToMove(Constants.MAX_TIMEOUT))
                                return 0;
                        }
                        if(waitForAnimations(Constants.MAX_TIMEOUT,Constants.MINING_ANIMATION_IDS))
                            return randomAntiban();
                        else
                            return 0;
                    }
                }
                else if(!isMining()) {
                    if(!switchRockTimer.isFinished())
                        return randomAntiban();
                    else if(!switchRockTimer.isRunning)
                        switchRockTimer.start();
                    else {
                        currentRock = null;
                        switchRockTimer.reset();
                        return 0;
                    }

                }
                else
                    switchRockTimer.reset();
            }        
        }
        return 0;
    }

    private boolean turnToObject(RSObject object) {
        return turnToObject(object, 0);
    }
    
    private boolean turnToCharacter(RSCharacter character) {
        return turnToCharacter(character, 0);
    }
    
    private boolean turnToCharacter(RSCharacter character, int deviation) {
        if(character != null) {
            int angle = camera.getCharacterAngle(character);
            angle = random(angle - deviation, angle + deviation + 1);
            setAngle(angle);
        }
        int pointsOnScreen = 0;
        for(int i = 0; i < 20; ++i) {
            if(character.isOnScreen())
                ++pointsOnScreen;
        }
        return pointsOnScreen > 5;
    }

    private boolean turnToObject(RSObject object, int deviation) {
        if(object != null) {
            int angle = camera.getObjectAngle(object);
            angle = random(angle - deviation, angle + deviation + 1);
            setAngle(angle);
        }
        int pointsOnScreen = 0;
        for(int i = 0; i < 20; ++i) {
            if(object.isOnScreen())
                ++pointsOnScreen;
        }
        return pointsOnScreen > 5;
    }

    public void setAngle(int degrees) {
        setAngle(degrees, Constants.MAX_TIMEOUT*3);
    }
    public void setAngle(int degrees, int timeout) {
        timeout /= 10;
        int fail = 0;
        if (camera.getAngleTo(degrees) > 5) {
            keyboard.pressKey((char) KeyEvent.VK_LEFT);
            while (camera.getAngleTo(degrees) > 5) {
                if(fail > timeout)
                    break;
                sleep(10);
                ++fail;
            }
            keyboard.releaseKey((char) KeyEvent.VK_LEFT);
	}
        else if (camera.getAngleTo(degrees) < -5) {
            keyboard.pressKey((char) KeyEvent.VK_RIGHT);
            while (camera.getAngleTo(degrees) < -5) {
                if(fail > timeout)
                    break;
                sleep(10);
                ++fail;
            }
            keyboard.releaseKey((char) KeyEvent.VK_RIGHT);
        }
    }

    private class Timer {

        private int time;
        private long startTime;
        private boolean isRunning;
        
        private Timer(int time) {
            this.time = time;
        }

        private void start() {
            this.startTime = nanosToMillis(System.nanoTime());
            isRunning = true;
        }

        private void reset() {
            isRunning = false;
            this.startTime = 0;
        }
        
        private boolean isFinished() {
            if(!isRunning)
                return true;
            return (nanosToMillis(System.nanoTime()) > (this.startTime + this.time));
        }
        
        private long getTimeTillFinished() {
            return (this.startTime + this.time) - nanosToMillis(System.nanoTime());
        }

        private long nanosToMillis(long nanos) {
            return nanos/1000000;
        }
    }

    private void hoverMouseOverObject(RSObject o) {
        RSModel model = o.getModel();
        if(model != null) {
            Point p = model.getPoint();
            if(p != null) {
                mouse.move(p,3,3);
            }
        }
    }

    private RSObject getNextRock() {
        ArrayList<RSObject> rocks = getRocks(oreA.getRockIDs());
        currentOre = oreA;
        RSObject rock = getNearest(rocks);
        if(rock == null) {
            rocks = getRocks(oreB.getRockIDs());
            currentOre = oreB;
            rock = getNearest(rocks);
        }
        if(rock == null) {
            rocks = getRocks(oreC.getRockIDs());
            currentOre = oreC;
            rock = getNearest(rocks);
        }
        return rock;
    }

    private boolean isMining() {
        final int animation = getMyPlayer().getAnimation();
        for(int anim : Constants.MINING_ANIMATION_IDS) {
            if(animation == anim)
                return true;
        }
        return false;
    }

    private boolean isMouseHoveringObject(RSObject o) {
        RSModel model = o.getModel();
        if(model != null) {
            Point p = mouse.getLocation();
            for( Polygon poly : model.getTriangles()) {
                if(poly.contains(p))
                    return true;
            }
            if(model != null) {
                p = model.getPoint();
                if(p != null) {
                    mouse.move(p,3,3);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean waitForAnimations(int mills, final int... anim) {
        int fail = 0;
        while(isActive()) {
            int playerAnimation = getMyPlayer().getAnimation();
            for( int animation : anim) {
                if(playerAnimation == animation)
                    return true;
            }
            if(fail > mills/10)
                break;
            if(!getMyPlayer().isMoving())
                ++fail;
            sleep(10);
        }
        fail = 0;
        return false;
    }

    //Jacmob
    private ArrayList<RSObject> getRocks(final int[] rockID) {
        if (rockStore == null) {
            RSObject[] rocks = objects.getAll(new Filter<RSObject>() {
		public boolean accept(RSObject o) {
                    if (mineArea.contains(o.getLocation())) {
			int oid = o.getID();
			for (int id : rockID) {
                            if (id == oid) {
				return true;
                            }
			}
                    }
                    return false;
		}
            });
            if (rocks.length > 0)
                rockStore = rocks;
            else
		return new ArrayList<RSObject>(0);
	}
	// loop appropriate tiles only since rock tiles don't change
	ArrayList<RSObject> rocks = new ArrayList<RSObject>();
	for (RSObject o : rockStore) {
            if (o != null) {
                if(isRockStillValid(o, rockID))
                    rocks.add(o);
            }
	}
	return rocks;
    }

    private boolean isRockStillValid(RSObject o, int[] rockID) {
        if(o == null)
            return false;
        RSTile loc = o.getLocation();
        if(loc == null)
            return false;
        RSObject realO = objects.getTopAt(loc, Objects.TYPE_INTERACTABLE);
        if(realO == null)
            return false;
        int currentRockIds = realO.getID();
        for( int id : rockID) {
            if(currentRockIds == id)
                return true;
        }
        return false;
    }

    private boolean isRockFree(RSObject o) {
        if(isStealing)
            return true;
        int min = 20;
        RSTile tile = o.getLocation();
        ArrayList<RSPlayer> closest = new ArrayList<RSPlayer>();
        RSPlayer[] localPlayers = players.getAll(new Filter<RSPlayer>(){
            public boolean accept(RSPlayer p) {
                if(p.equals(getMyPlayer()))
                    return false;
                return true;
            }
        });
        for (int i = 0; i < localPlayers.length; ++i) {
            if (localPlayers[i] == null) {
		continue;
            }
            int distance = (int) calc.distanceBetween(tile,localPlayers[i].getLocation());
            if (distance < min) {
                closest.clear();
                min = distance;
                closest.add(localPlayers[i]);
            }
            else if(distance == min) {
                closest.add(localPlayers[i]);
            }
        }
        if(closest.isEmpty())
            return true;
        for(int i = 0; i < closest.size(); ++i) {
            RSPlayer p = closest.get(i);
            if(p.getAnimation() != -1) {
                if(isFacingObject(o,p))
                    return false;
            }
        }
        return true;
    }

    private boolean isFacingObject(RSObject o, RSPlayer p) {
        int orientation = p.getOrientation();
        RSTile oLoc = o.getLocation();
        RSTile pLoc = p.getLocation();
        if(isWithinRange(orientation,-1,1)) { //EAST
            if(pLoc.getY() == oLoc.getY()) {
                if(pLoc.getX() < oLoc.getX()) //WEST OF ROCK
                    return true;
            }
        }
        else if(isWithinRange(orientation,89,91)) { //NORTH
            if(pLoc.getX() == oLoc.getX()) {
                if(pLoc.getY() < oLoc.getY()) //SOUTH_OF_ROCK
                    return true;
            }
        }
        else if(isWithinRange(orientation,179,181)) { //WEST
            if(pLoc.getY() == oLoc.getY()) {
                if(pLoc.getX() > oLoc.getX())
                    return true;
            }
        }
        else if(isWithinRange(orientation,269,271)) {    //SOUTH
            if(pLoc.getX() == oLoc.getX()) {
                if(pLoc.getY() > oLoc.getY())
                    return true;
            }
        }
        return false;
    }

    private boolean isWithinRange(int value, int min, int max) {
        return ((value >= min) && (value <= max));
    }

    private boolean waitForIdle(int timeout) {
        int fail = 0;
        while(!getMyPlayer().isIdle()) {
            if(fail > timeout/10)
                return false;
            ++fail;
            sleep(10);
        }
        return true;
    }

    private boolean waitToMove(int timeout) {
        int fail = 0;
        while(!getMyPlayer().isMoving()) {
            if(fail > timeout/10)
                return false;
            ++fail;
            sleep(10);
        }
        return true;
    }

    private RSObject getNearest(ArrayList<RSObject> rocks) {
        if(rocks == null || rocks.isEmpty()) {
            rockStore = null;
            return null;
        }
        int dist = 99;
        RSObject closest = null;
        for(int i = 0; i < rocks.size(); ++i) {
            RSObject tmp = rocks.get(i);
            if(tmp == null)
                continue;
            int tmpDist = calc.distanceTo(tmp);
            if(tmpDist < dist) {
                if(isRockFree(tmp)) {
                    dist = tmpDist;
                    closest = tmp;
                }
            }
        }
        return closest;
    }

    private int getProfit() {
        return (oreA.getMined() * oreA.getPrice()) + (oreB.getMined() * oreB.getPrice()) + (oreC.getMined() * oreC.getPrice()) + gemProfit;
    }

    private BufferedImage getImage(String url)  {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException ex) {
            log("Could not load image for paint.");
        }
        return null;
    }

    private int startLevel, startXP, profit, oresMined, xpGained;
    private float profitSec, xpSec;
    
    private boolean showPaint;
    private Rectangle interactivePaint = new Rectangle(0,0,240,160);
    private Rectangle hideRect = new Rectangle(228,2,10,10);

    private Clock clock;
    private class Clock {

        private final long startTime;

        private Clock() {
            startTime = System.nanoTime()/1000000;
        }

        private long getElapsedMillis() {
            return System.nanoTime()/1000000 - startTime;
        }

        private float getElapsedSeconds() {
            return (float) (getElapsedMillis() / 1000);
        }

        private int getElapsedMinutes() {
            return (int) (getElapsedSeconds() / 60);
        }

        private int getElapsedHours() {
            return getElapsedMinutes() / 60;
        }

        private int getElapsedDays() {
            return getElapsedHours() / 24;
        }

    }

    private String formatMillis(long millis) {
            if(millis == 0)
                return "0:0:0";
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = seconds / 3600;
            seconds = seconds % 60;
            minutes = minutes % 60;
            return hours + ":" + minutes + ":" + seconds;
        }

    private String formatMillis(int millis) {
        return formatMillis((long)millis);
    }

    private String formatCommas(double number) {
        Locale locale = new Locale(System.getProperty("user.language"));
        if(locale == null)
            locale = Locale.ENGLISH;
        return DecimalFormat.getInstance(locale).format(number);
    }

    private String formatCommas(long number) {
        return formatCommas((double)number);
    }

    private void updatePaintValues() {
        profit = getProfit();
        oresMined = getOresMined();
        xpGained = skills.getCurrentExp(Skills.MINING) - startXP;
        profitSec = (float) profit / clock.getElapsedSeconds();
        xpSec = (float) xpGained / clock.getElapsedSeconds();
    }

    @Override
    public void onRepaint(Graphics g) {   
        if(gui.isVisible())
            return;
        if(!game.isLoggedIn())
            return;
        paint((Graphics2D)g);
    }

    private void paint(Graphics2D g) {
        g.setRenderingHints(Constants.ANTI_ALIAS_RH);
        paintObject(g, nextRock, Constants.COLOR_RUNITE_BLUE.darker().darker().darker(), 0.25f);
        paintObject(g, currentRock, Constants.COLOR_RUNITE_BLUE, 0.25f);
        if(showPaint) {
            updatePaintValues();
            int x = interactivePaint.x;
            int y = interactivePaint.y;
            int width = interactivePaint.width;
            int height = interactivePaint.height;
            paintBackground(g, x, y, width, height, 0.5f, Constants.COLOR_RUNITE_BLUE, Constants.COLOR_LIGHT_GREY);
            int centeredWidth = width*19/20;
            int centeredX = x + width/2 - (centeredWidth/2);
            paintImage(g, titleImage, centeredX, y, centeredWidth, height/4);
            paintTextPanel(g, x + width/20, y + height*2/9, width - 2*(width/20), height*33/48, Constants.COLOR_LIGHT_GREY);
            paintBar(g, centeredX, y + height - (height/14), centeredWidth, height/20);
        }
        paintHideButton(g, hideRect.x, hideRect.y, hideRect.width, hideRect.height, Constants.COLOR_DARK_GREY, Constants.COLOR_LIGHT_GREY, showPaint);
        paintMouse(g);
    }

    private int getOresMined() {
        return oreA.getMined() + oreB.getMined() + oreC.getMined();
    }

    private void paintBackground(Graphics2D render, int x, int y, int width, int height, float alpha, Color fill, Color outline) {
        Color color = render.getColor();
        Stroke stroke = render.getStroke();
        Composite composite = render.getComposite();
        render.setStroke(new BasicStroke(width/50, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Shape roundRect = new RoundRectangle2D.Float(x, y, width, height, 10, 10);
        render.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        render.setColor(fill);
        render.fill(roundRect);
        render.setComposite(composite);
        render.setColor(outline);
        render.draw(roundRect);
        render.setStroke(stroke);
        render.setColor(color);
    }

    private void paintHideButton(Graphics2D render, int x, int y, int width, int height, Color foreground, Color background, boolean hide) {
        Color color = render.getColor();
        Stroke stroke = render.getStroke();
        render.setColor(background);
        render.fillRoundRect(x, y, width, height, 5, 5);
        render.setColor(foreground);
        render.setStroke(new BasicStroke(width*0.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if(hide) {
            render.draw(new Line2D.Float(x + (width/8), y + (height/8), x + width*7/8, y + height*7/8));
            render.draw(new Line2D.Float(x + width*7/8, y + (height/8), x + (width/8), y + height*7/8));
        }
        else
            render.draw(new Ellipse2D.Float(x + (width/8),y + (height/8),width*3/4,height*3/4));
        render.setStroke(stroke);
        render.setColor(color);
    }

    private Rectangle generalBounds, expBounds, profitBounds;
    private void paintTextPanel(Graphics2D render, int x, int y, int width, int height, Color outline) {
        Color color = render.getColor();
        Stroke stroke = render.getStroke();
        render.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        render.setColor(outline);
        render.draw(new RoundRectangle2D.Float(x,y,width,height,5,5));
        paintText(render, currentTextType, x + 5, y, height, Constants.FONT_CALIBRI, Constants.COLOR_LIGHT_GREY);
        setBounds(generalBounds, x + width - generalImage.getWidth()*10/9, y + generalImage.getHeight()/9, generalImage.getWidth(),
                generalImage.getHeight());
        paintImage(render, (generalBounds.contains(mousePoint)) ? brightenImage(generalImage, Constants.DEFAULT_BRIGHTEN):generalImage,
                x + width - generalImage.getWidth()*10/9, y + generalImage.getHeight()/9,
                generalImage.getWidth(), generalImage.getHeight());
        setBounds(expBounds, x + width - expImage.getWidth()*10/9, y + height - height/2 - expImage.getHeight()/2,
                expImage.getWidth(), expImage.getHeight());
        paintImage(render, (expBounds.contains(mousePoint)) ? brightenImage(expImage, Constants.DEFAULT_BRIGHTEN):expImage,
                x + width - expImage.getWidth()*10/9, y + height - height/2 - expImage.getHeight()/2,
                expImage.getWidth(), expImage.getHeight());
        setBounds(profitBounds, x + width - profitImage.getWidth()*10/9, y + height - profitImage.getHeight()*10/9,
                profitImage.getWidth(), profitImage.getHeight());
        paintImage(render, (profitBounds.contains(mousePoint)) ? brightenImage(profitImage, Constants.DEFAULT_BRIGHTEN):profitImage,
                x + width - profitImage.getWidth()*10/9, y + height - profitImage.getHeight()*10/9,
                profitImage.getWidth(), profitImage.getHeight());
        render.setColor(color);
        render.setStroke(stroke);
    }

    private void setBounds(Rectangle rect, int x, int y, int width, int height) {
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
    }

    //too lazy to make an awesome oop paint
    private enum TextType { GENERAL, EXP, PROFIT, ALL};
    private TextType currentTextType = TextType.GENERAL;
    private void paintText(Graphics2D render, TextType type, int x, int y, int height, Font font, Color color) {
        Color c = render.getColor();
        Font f = render.getFont();
        render.setColor(color);
        render.setFont(font);
        String[] text = null;
        switch(type) {
            case GENERAL:
                text = getGeneralText();
                break;
            case EXP:
                text = getExpText();
                break;
            case PROFIT:
                text = getProfitText();
                break;
        }
        int lineHeight = height / text.length;
        int count = 1;
        for(String string : text) {
            render.drawString(string, x, y + (lineHeight*count));
            ++count;
        }
        render.setFont(f);
        render.setColor(c);
    }

    private String[] getGeneralText() {
        String[] array = { "Time running: " + formatMillis(clock.getElapsedMillis()),
        "Ores mined: " + oresMined,
        "Gems mined: " + gemsMined };
        return array;
    }

    private String[] getExpText() {
        String[] array = { "XP Gained: " + (skills.getCurrentExp(Skills.MINING) - startXP),
        "XP/Hour: " + formatCommas(xpSec * 3600),
        "Current Level: " + skills.getCurrentLevel(Skills.MINING),
        "Levels Gained: " + (skills.getCurrentLevel(Skills.MINING) - startLevel),
        "Percent to level: " + getPercentToLevel(Skills.MINING)*100 + "%",
        "Time to level: " + formatMillis((xpSec == 0) ? 0:(int)(skills.getExpToNextLevel(Skills.MINING) / xpSec * 1000))};
        return array;
    }

    private String[] getProfitText() {
        String[] array = { "Profit: " + profit,
        "Profit/Hour: " + formatCommas(profitSec * 3600)};
        return array;
    }

    private void paintImage(Graphics2D render, BufferedImage image, int x, int y, int height, int width) {
        if(image != null)
            render.drawImage(image, x, y, height, width, null);
    }

    private void paintBar(Graphics2D render, int x, int y, int width, int height) {
        render.setPaint(Constants.COLOR_RED_GRADIENT);
        render.fillRoundRect(x, y, width, height, 7, 7);
        render.setPaint(Constants.COLOR_GREEN_GRADIENT);
        width *= getPercentToLevel(Skills.MINING);
        render.fillRoundRect(x, y, width, height, 7, 7);
    }

    private float getPercentToLevel(int skill) {
        int level = skills.getRealLevel(skill);
        float xpTotal = Skills.XP_TABLE[level + 1] - Skills.XP_TABLE[level];
        float xpDone = skills.getCurrentExp(Skills.MINING) - (Skills.XP_TABLE[level]);
        return xpDone / xpTotal;
    }

    private void paintObject(Graphics2D g, RSObject object, Color color) {
        paintObject(g, object, color, 1.0f);
    }
    private void paintObject(Graphics2D render, RSObject object, Color color, float alpha) {
        if(object == null)
            return;
        if(!object.isOnScreen())
            return;
        if(!walking.isLocal(object.getLocation()))
            return;
        RSModel model = object.getModel();
        ArrayList<Polygon> objectPolys = new ArrayList<Polygon>();
        if(model != null)
            objectPolys.addAll(Arrays.asList(model.getTriangles()));
        else
            return;
        Composite composite = render.getComposite();
        render.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        for(int i = 0; i < objectPolys.size(); ++i) {
            Polygon objectPoly = objectPolys.get(i);
            render.setColor(color.darker());
            render.fillPolygon(objectPoly);
            render.setColor(color.brighter());
            render.drawPolygon(objectPoly);
        }
        render.setComposite(composite);
    }

    public void paintMouse(Graphics2D g) {
       if (mouse != null) {
            Point mouseLocation = mouse.getLocation();
            Point mousePressLocation = mouse.getPressLocation();
            long mouse_press_time = mouse.getPressTime();

            g.setColor(Color.GREEN);
            g.fillRect(mouseLocation.x - 1, mouseLocation.y - 7, 3, 15);
            g.fillRect(mouseLocation.x - 7, mouseLocation.y - 1, 15, 3);
            if (System.currentTimeMillis() - mouse_press_time < 1000) {
                g.setColor(Color.RED);
                g.fillRect(mousePressLocation.x - 1, mousePressLocation.y - 7, 3, 15);
                g.fillRect(mousePressLocation.x - 7, mousePressLocation.y - 1, 15, 3);
            }
        }
    }

    private Point mousePoint;
    public void mouseDragged(MouseEvent e) {
        if(!showPaint)
            return;
        Point point = e.getPoint();
        if(interactivePaint.contains(mousePoint))
            translatePaint(point);
        mousePoint = point;
    }

    private void translatePaint(Point point) {
        int x1 = interactivePaint.x + point.x - mousePoint.x;
        int y1 = interactivePaint.y + point.y - mousePoint.y;
        int x2 = hideRect.x + point.x - mousePoint.x;
        int y2 = hideRect.y + point.y - mousePoint.y;
        if(x1 >= 0 && x1 + interactivePaint.width <= Constants.MAX_WIDTH) {
            interactivePaint.x = x1;
            hideRect.x = x2;
        }
        if(y1 >= 0 && y1 + interactivePaint.height <= Constants.MAX_HEIGHT) {
            interactivePaint.y = y1;
            hideRect.y = y2;
        }
    }

    public void mouseMoved(MouseEvent e) {
        mousePoint = e.getPoint();
    }

    public void mouseClicked(MouseEvent e) {
        Point point = e.getPoint();
        if(hideRect.contains(point))
            showPaint = !showPaint;
        if(generalBounds.contains(point))
            currentTextType = TextType.GENERAL;
        else if(expBounds.contains(point))
            currentTextType = TextType.EXP;
        else if(profitBounds.contains(point))
            currentTextType = TextType.PROFIT;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    @Override
    public void messageReceived(MessageEvent e) {
        if(e.getID() != MessageEvent.MESSAGE_ACTION)
            return;
        String text = e.getMessage().toLowerCase();
        if(text.contains("mine some"))
            currentOre.increaseMined(1);
        else if(text.contains("just found a")) {
            if(text.contains("sapphire"))
                gemProfit += gemPrices[Gems.SAPPHIRE];
            else if(text.contains("emerald"))
                gemProfit += gemPrices[Gems.EMERALD];
            else if(text.contains("ruby"))
                gemProfit += gemPrices[Gems.RUBY];
            else if(text.contains("diamond"))
                gemProfit += gemPrices[Gems.DIAMOND];
            ++gemsMined;
        }
    }

    @Override
    public void onFinish() {
        log("Ores mined: " + getOresMined());
        log("Total XP: " + (skills.getCurrentExp(Skills.MINING) - startXP));
        log("Total Profit: " + getProfit());
    }

    private int randomAntiban() {
        try {
            int randomAntiban = random(0,1000);
            if(randomAntiban < 800)
                return 0;
            else {
                randomAntiban = random(0,1000);
                    if(cameraTimer != null && cameraTimer.isFinished()) {
                        cameraMove = new CameraMove(random(500, 2000));
                        cameraMove.start();
                        cameraTimer = new Timer(random(5000,20000));
                        cameraTimer.start();
                    }
                return 0;
            }

        }
        catch(ArrayIndexOutOfBoundsException aiaobe) {}
        return 0;
    }

    private CameraMove cameraMove;
    private Timer cameraTimer;
    private class CameraMove extends Thread {

        private final int millisToMove;

        public CameraMove(int millisToMove) {
            this.millisToMove = millisToMove;
        }
        @Override
        public void run() {
            try {
                moveRandomly(millisToMove);
            }
            catch (InterruptedException ie) {}
        }

        private void moveRandomly(int timeout) throws InterruptedException {
            int mouseSpeed = mouse.getSpeed();
            mouse.setSpeed(random(2,4));
            Timer timeToHold = new Timer(timeout);
            camera.getAngle();
            int vertical = random(0, 20) < 15 ? KeyEvent.VK_UP : KeyEvent.VK_DOWN;
            int horizontal = random(0, 20) < 5 ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT;
            if (random(0, 10) < 8)
            	keyboard.pressKey((char) vertical);
            if (random(0, 10) < 8)
            	keyboard.pressKey((char) horizontal);
            timeToHold.start();
            while (!timeToHold.isFinished() && !isInterrupted()) {
            	sleep(10);
            }
            keyboard.releaseKey((char) vertical);
            keyboard.releaseKey((char) horizontal);
            mouse.setSpeed(mouseSpeed);
        }
    }

    private class ItemPriceLoader extends Thread {

        @Override
        public void run() {
            gemPrices[Gems.SAPPHIRE] = grandExchange.lookup(Gems.SAPPHIRE_ID).getMarketPrice();
            gemPrices[Gems.EMERALD] = grandExchange.lookup(Gems.EMERALD_ID).getMarketPrice();
            gemPrices[Gems.RUBY] = grandExchange.lookup(Gems.RUBY_ID).getMarketPrice();
            gemPrices[Gems.DIAMOND] = grandExchange.lookup(Gems.DIAMOND_ID).getMarketPrice();

            oreA.retrievePrice();
            oreB.retrievePrice();
            oreC.retrievePrice();
        }
    }

    private BufferedImage titleImage, generalImage, expImage, profitImage;
    private class ImageLoader extends Thread {

        @Override
        public void run() {
            titleImage = getImage("http://supremewow.com/dpedroia15/rsbot/scripts/miner/resources/miner_title.png");
            generalImage = getImage("http://supremewow.com/dpedroia15/rsbot/scripts/miner/resources/inventory.png");
            generalImage = rescale(generalImage,interactivePaint.width/10,interactivePaint.width/10);
            expImage = getImage("http://supremewow.com/dpedroia15/rsbot/scripts/miner/resources/statistics.png");
            expImage = rescale(expImage,interactivePaint.width/10,interactivePaint.width/10);
            profitImage = getImage("http://supremewow.com/dpedroia15/rsbot/scripts/miner/resources/coins.png");
            profitImage = rescale(profitImage,interactivePaint.width/10,interactivePaint.width/10);
        }
    }

    private BufferedImage rescale(BufferedImage image, int width, int height) {
        AffineTransformOp rescale = new AffineTransformOp(AffineTransform.getScaleInstance((double)width/image.getWidth(),(double) height/image.getHeight()), AffineTransformOp.TYPE_BILINEAR);
        BufferedImage temp = null;
        temp = rescale.filter(image, temp);
        return temp;
    }

    private BufferedImage brightenImage(BufferedImage image, float brightness) {
        byte[] data = new byte[256];
        for(int i = 0; i < data.length; ++i)
            data[i] = (byte) (Math.sqrt((float)(i/brightness)) * brightness);
        ByteLookupTable table = new ByteLookupTable(0, data);
        LookupOp op = new LookupOp(table,null);
        return op.filter(image, null);
    }

    private int[] gemPrices = new int[4];
    private static final class Gems {
        public static final int SAPPHIRE = 0;
        public static final int EMERALD = 1;
        public static final int RUBY = 2;
        public static final int DIAMOND = 3;

        public static final int SAPPHIRE_ID = 1623;
        public static final int EMERALD_ID = 1621;
        public static final int RUBY_ID = 1619;
        public static final int DIAMOND_ID = 1617;

    }

    private class Ore {

        private final int id;
        private final int[] rockIDs;
        private final float xp;
        
        private int price;
        private int mined;

        private Ore(int id, int[] rockIDs) {
            this.id = id;
            this.rockIDs = rockIDs;
            xp = getXPForOreID(this.id);
        }

        private int getID() {
            return id;
        }

        private int[] getRockIDs() {
            return rockIDs;
        }

        private int getPrice() {
            return price;
        }

        private synchronized void retrievePrice() {
            price = grandExchange.lookup(id).getMarketPrice();
        }

        private float getXP() {
            return xp;
        }

        private float getXPForOreID(int oreId) {
            switch(oreId) { 
                case 434: //Clay
                    return 5.0f;
                case 436: //Tin
                case 439: //Copper
                    return 17.5f;
                case 440: //Iron
                case 441:
                    return 35.0f;   
                case 442: //Silver
                    return 40.0f;    
                case 453: //Coal
                    return 50.0f;    
                case 444: //Gold
                    return 65.0f;   
                case 447: //Mithril
                    return 80.0f;
                case 449: //Adamantite
                    return 95.0f;
                default:
                    return 0;
            }
        }

        private int getMined() {
            return mined;
        }

        private void increaseMined(int amount) {
            mined += amount;
        }
    }

    private static final class Constants {

        public static final int[] CLAY_ROCK_IDS = { 711, 9713, 15503, 15504, 15505, 31062, 31063 };
        public static final int[] COPPER_ROCK_IDS = { 9708, 9709, 9710, 11936, 11937, 11938, 11960, 11961,11962, 11963, 31080, 31082 };
        public static final int[] TIN_ROCK_IDS = { 9714, 9716, 11933, 11934, 11935, 11957, 11958, 11959,31077, 31078, 31079 };
        public static final int[] IRON_ROCK_IDS = { 2092, 2093, 5773, 5774, 5775, 9717, 9718, 9719, 11954, 11955,11956, 14913, 14914, 31071, 31072, 31073, 37307, 37308, 37309 };
        public static final int[] SILVER_ROCK_IDS = { 2311, 9713, 9714, 9716, 11948, 11949, 11950, 37304,37305, 37306 };
        public static final int[] COAL_ROCK_IDS = { 2096, 2097, 5770, 5771, 5772, 11930, 11931, 11932, 11963, 11964, 14850,14851, 14852, 31068, 31069, 31070, 32426 };
        public static final int[] GOLD_ROCK_IDS = { 9720, 9722, 11183, 11184, 11185, 15503, 15505, 31065,31066, 37310, 37312, 37313 };
        public static final int[] MITHRIL_ROCK_IDS = { 5784, 5785, 5786, 11942, 11943, 11944, 32438, 32439, 31086, 31088 };
        public static final int[] ADAMANTITE_ROCK_IDS = { 11939, 11940, 11941, 31083, 31084, 31085, 32435,32436 };
        public static final int[] RUNITE_ROCK_IDS = { 14895, 33078, 33079 };

        public static final int[] PICKAXE_IDS = { 1265, 1267, 1269, 1271, 1273, 1275, 14107, 15259 };
        public static final int[] MINING_ANIMATION_IDS = { 624, 625, 626, 627, 628 };

        public static final int ABOVE_MINING_GUILD_LADDER_ID = 2113;
        public static final int BELOW_MINING_GUILD_LADDER_ID = 6226;

        public static final int CLAY_ORE_ID = 434;
        public static final int TIN_ORE_ID = 436;
        public static final int COPPER_ORE_ID = 436;
        public static final int IRON_ORE_ID = 440;
        public static final int SILVER_ORE_ID = 442;
        public static final int COAL_ORE_ID = 453;
        public static final int GOLD_ORE_ID = 444;
        public static final int MITHRIL_ORE_ID = 447;
        public static final int ADAMANTITE_ORE_ID = 449;

        public static final int MAX_WIDTH = 766;
        public static final int MAX_HEIGHT = 503;

        public static final int MAX_TIMEOUT = 2000;

        public static final RenderingHints ANTI_ALIAS_RH = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        public static final Color COLOR_DARK_GREY = new Color(100,100,100);
        public static final Color COLOR_LIGHT_GREY = new Color(195,195,195);
        public static final Color COLOR_RUNITE_BLUE = new Color(10,238,250);
        public static final GradientPaint COLOR_RED_GRADIENT = new GradientPaint(0,0,Color.RED.darker().darker(),100,0,Color.RED,true);
        public static final GradientPaint COLOR_GREEN_GRADIENT = new GradientPaint(0,0,Color.GREEN,100,0,Color.GREEN.darker().darker(),true);
        public static final Font FONT_CALIBRI = new Font("Calibri",Font.PLAIN,16);
        public static final float DEFAULT_BRIGHTEN = 100.0f;

        public static final String SETTINGS_FILE_PATH = GlobalConfiguration.Paths.getSettingsDirectory() + File.separator + "miner-properties.ini";
    }
    private class GUI extends JFrame implements ActionListener, WindowListener {

        private JComboBox oreTypeOption1, oreTypeOption2, oreTypeOption3, mineOption;
        private JCheckBox isBankingOption,isStealingRocks;
        private JButton startButton;

        private Font centuryGothic;

        private final String[] oreTypeStrings = { "Clay","Tin","Copper","Iron","Silver","Coal","Gold","Mithril","Adamantite","Runite" };
        private final String[] bankOptions = { "Varrock East","Varrock West","Lumbridge Swamp West","Rimmington","Barbarian Village","Al Kharid","Mining Guild","East Ardougne","Yanille" };

        private Properties settings;

        public GUI() {
            centuryGothic = new Font("Century Gothic", 0, 14);
            settings = new Properties();
            createGUI();
        }

        private void createGUI() {
            FileInputStream in = null;
            try {
                in = new FileInputStream(Constants.SETTINGS_FILE_PATH);
                settings.load(in);
            }
            catch (IOException ex1) {
                log("Error loading settings file, displaying default settings.");
            }
            finally {
                try {
                    if(in != null)
                        in.close();
                } catch (IOException ex) { }
            }
            boolean settingsLoaded = settings.containsKey("ore1");

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException ex) {}
            catch (InstantiationException ex) {}
            catch (IllegalAccessException ex) {}
            catch (UnsupportedLookAndFeelException ex) {}
            setLayout(new BorderLayout());

            JPanel orePanel = new JPanel();
            JPanel bankPanel = new JPanel();
            JPanel mainPanel = new JPanel();

            mainPanel.setLayout(new BorderLayout());
            orePanel.setLayout(new FlowLayout());
            bankPanel.setLayout(new FlowLayout());

            mainPanel.add(orePanel,BorderLayout.NORTH);
            mainPanel.add(bankPanel,BorderLayout.SOUTH);
            mainPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

            add(mainPanel, BorderLayout.CENTER);
            
            setTitle("dpedroia15's Miner");

            oreTypeOption1 = new JComboBox(oreTypeStrings);
            oreTypeOption2 = new JComboBox(oreTypeStrings);
            oreTypeOption3 = new JComboBox(oreTypeStrings);
            oreTypeOption1.setFont(centuryGothic);
            oreTypeOption2.setFont(centuryGothic);
            oreTypeOption3.setFont(centuryGothic);
            oreTypeOption1.setToolTipText("Choose the main ore you'd like to mine.");
            oreTypeOption2.setToolTipText("Choose a second ore you'd like to mine.");
            oreTypeOption3.setToolTipText("Choose a second ore you'd like to mine.");
            if(settingsLoaded) {
                oreTypeOption1.setSelectedItem(settings.getProperty("ore1"));
                oreTypeOption2.setSelectedItem(settings.getProperty("ore2"));
                oreTypeOption3.setSelectedItem(settings.getProperty("ore3"));
            }

            isStealingRocks = new JCheckBox();
            isStealingRocks.setText("Steal rocks");
            isStealingRocks.setFont(centuryGothic);
            isStealingRocks.setToolTipText("Check if you want your character to mine rocks that other players are already mining.");
            if(settingsLoaded)
                isStealingRocks.setSelected(Boolean.valueOf(settings.getProperty("stealing")));

            orePanel.add(oreTypeOption1);
            orePanel.add(oreTypeOption2);
            orePanel.add(oreTypeOption3);
            orePanel.add(isStealingRocks);

            mineOption = new JComboBox(bankOptions);
            mineOption.setFont(centuryGothic);
            mineOption.setToolTipText("Choose where you are going to mine.");
            if(settingsLoaded)
                mineOption.setSelectedItem(settings.getProperty("mine"));

            isBankingOption = new JCheckBox();
            isBankingOption.setText("Bank ores?");
            isBankingOption.setFont(centuryGothic);
            isBankingOption.setToolTipText("Check if you'd like to bank ores mined.");
            if(settingsLoaded)
                isBankingOption.setSelected(Boolean.valueOf(settings.getProperty("banking")));

            JLabel area = new JLabel("Area:");
            area.setFont(centuryGothic);
            bankPanel.add(area);
            bankPanel.add(mineOption);
            bankPanel.add(isBankingOption);

            startButton = new JButton("Start");
            startButton.setFont(centuryGothic.deriveFont(16.0f));
            startButton.addActionListener(this);
            startButton.setToolTipText("Click to begin!");
            bankPanel.add(startButton);
            
            pack();
            addWindowListener(this);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationByPlatform(true);
            setVisible(true);
        }

        private int[] getIdsForRock(String rockName) {
            if(rockName.equals("Clay"))
                return Constants.CLAY_ROCK_IDS;
            else if(rockName.equals("Tin"))
                return Constants.TIN_ROCK_IDS;
            else if(rockName.equals("Copper"))
                return Constants.COPPER_ROCK_IDS;
            else if(rockName.equals("Iron"))
                return Constants.IRON_ROCK_IDS;
            else if(rockName.equals("Silver"))
                return Constants.SILVER_ROCK_IDS;
            else if(rockName.equals("Coal"))
                return Constants.COAL_ROCK_IDS;
            else if(rockName.equals("Gold"))
                return Constants.GOLD_ROCK_IDS;
            else if(rockName.equals("Mithril"))
                return Constants.MITHRIL_ROCK_IDS;
            else if(rockName.equals("Adamantite"))
                return Constants.ADAMANTITE_ROCK_IDS;
            else {
                log("Rock type not supported yet.");
                return null;
            }
        }

        private int getOreIDForRock(String rockName) {
            if(rockName.equals("Clay"))
                return Constants.CLAY_ORE_ID;
            else if(rockName.equals("Tin"))
                return Constants.TIN_ORE_ID;
            else if(rockName.equals("Copper"))
                return Constants.COPPER_ORE_ID;
            else if(rockName.equals("Iron"))
                return Constants.IRON_ORE_ID;
            else if(rockName.equals("Silver"))
                return Constants.SILVER_ORE_ID;
            else if(rockName.equals("Coal"))
                return Constants.COAL_ORE_ID;
            else if(rockName.equals("Gold"))
                return Constants.GOLD_ORE_ID;
            else if(rockName.equals("Mithril"))
                return Constants.MITHRIL_ORE_ID;
            else if(rockName.equals("Adamantite"))
                return Constants.ADAMANTITE_ORE_ID;
            else {
                log("Rock type not supported yet.");
                return 0;
            }
        }

        //Dynamic object creation for better memory usage
        private void setBank(String bankName) {
            if(!bankName.equals("Mining Guild")) {
                if(bankName.equals("Varrock East")) {
                    bankArea = new RSArea(new RSTile(3250,3423),new RSTile(3257,3419));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(3285, 3367), new RSTile(3287, 3371), new RSTile(3289, 3374),
                    new RSTile(3293, 3377), new RSTile(3294, 3381), new RSTile(3292, 3387), new RSTile(3292, 3393),
                    new RSTile(3290, 3398), new RSTile(3290, 3403), new RSTile(3289, 3408), new RSTile(3286, 3413),
                    new RSTile(3285, 3419), new RSTile(3283, 3423), new RSTile(3277, 3427), new RSTile(3275, 3428),
                    new RSTile(3266, 3428), new RSTile(3259, 3428), new RSTile(3253, 3426), new RSTile(3253, 3420)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(3285, 3372), new RSTile(3291, 3372),
                    new RSTile(3291, 3360), new RSTile(3281, 3361), new RSTile(3281, 3371)});
                }
                else if(bankName.equals("Varrock West")) {
                    bankArea = new RSArea(new RSTile(3182, 3446),new RSTile(3189, 3432));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(3179, 3370), new RSTile(3184, 3374), new RSTile(3183, 3379),
                    new RSTile(3179, 3384), new RSTile(3176, 3388), new RSTile(3173, 3393), new RSTile(3171, 3398),
                    new RSTile(3171, 3404), new RSTile(3171, 3409), new RSTile(3171, 3414), new RSTile(3171, 3419),
                    new RSTile(3171, 3424), new RSTile(3176, 3429), new RSTile(3182, 3430), new RSTile(3186, 3434)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(3182, 3381), new RSTile(3186, 3377),
                    new RSTile(3186, 3373), new RSTile(3184, 3368), new RSTile(3180, 3364), new RSTile(3175, 3362),
                    new RSTile(3170, 3366), new RSTile(3172, 3370), new RSTile(3176, 3374), new RSTile(3178, 3377),
                    new RSTile(3180, 3380)});
                }
                else if(bankName.equals("Lumbridge Swamp West")) {
                    bankArea = new RSArea(new RSTile(3091,3246),new RSTile(3097,3240));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(3146, 3149), new RSTile(3148, 3152), new RSTile(3148, 3157),
                    new RSTile(3148, 3164), new RSTile(3146, 3169), new RSTile(3144, 3174), new RSTile(3141, 3180),
                    new RSTile(3140, 3185), new RSTile(3140, 3190), new RSTile(3138, 3196), new RSTile(3138, 3201),
                    new RSTile(3130, 3206), new RSTile(3122, 3207), new RSTile(3119, 3212), new RSTile(3116, 3217),
                    new RSTile(3113, 3222), new RSTile(3108, 3228), new RSTile(3104, 3231), new RSTile(3108, 3233),
                    new RSTile(3100, 3238), new RSTile(3098, 3242), new RSTile(3093, 3244)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(3146, 3156), new RSTile(3151, 3152),
                    new RSTile(3150, 3147), new RSTile(3149, 3142), new RSTile(3145, 3143), new RSTile(3142, 3144),
                    new RSTile(3143, 3154)});
                }
                else if(bankName.equals("Rimmington")) {
                    bankArea = new RSArea(new RSTile(3009,3358),new RSTile(3018,3355));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(2977, 3240), new RSTile(2975, 3245), new RSTile(2976, 3250),
                    new RSTile(2977, 3255), new RSTile(2977, 3260), new RSTile(2983, 3265), new RSTile(2986, 3270),
                    new RSTile(2989, 3275), new RSTile(2991, 3280), new RSTile(2998, 3286), new RSTile(3003, 3291),
                    new RSTile(3006, 3296), new RSTile(3006, 3301), new RSTile(3006, 3307), new RSTile(3006, 3313),
                    new RSTile(3007, 3319), new RSTile(3007, 3324), new RSTile(3007, 3329), new RSTile(3007, 3335),
                    new RSTile(3007, 3341), new RSTile(3007, 3347), new RSTile(3007, 3353), new RSTile(3007, 3358),
                    new RSTile(3012, 3356)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(2977, 3251), new RSTile(2984, 3248),
                    new RSTile(2988, 3243), new RSTile(2989, 3235), new RSTile(2983, 3229), new RSTile(2972, 3230),
                    new RSTile(2964, 3239), new RSTile(2971, 3249)});
                }
                else if(bankName.equals("Barbarian Village")) {
                    bankArea = new RSArea(new RSTile(3091,3498),new RSTile(3098,3488));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(3082, 3422), new RSTile(3085, 3425), new RSTile(3090, 3431),
                    new RSTile(3091, 3436), new RSTile(3092, 3442), new RSTile(3090, 3447), new RSTile(3090, 3452),
                    new RSTile(3088, 3458), new RSTile(3087, 3462), new RSTile(3081, 3466), new RSTile(3080, 3472),
                    new RSTile(3080, 3478), new RSTile(3080, 3483), new RSTile(3086, 3486), new RSTile(3088, 3490),
                    new RSTile(3093, 3491)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(3079, 3425), new RSTile(3085, 3424),
                    new RSTile(3086, 3420), new RSTile(3083, 3416), new RSTile(3077, 3417), new RSTile(3077, 3422)});
                }
                else if(bankName.equals("Al Kharid")) {
                    bankArea = new RSArea(new RSTile(3269, 3173), new RSTile(3272, 3161));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(3300, 3314), new RSTile(3300, 3310), new RSTile(3300, 3306),
                    new RSTile(3300, 3302), new RSTile(3300, 3298), new RSTile(3300, 3294), new RSTile(3300, 3290),
                    new RSTile(3298, 3286), new RSTile(3298, 3282), new RSTile(3298, 3278), new RSTile(3298, 3274),
                    new RSTile(3295, 3270), new RSTile(3293, 3266), new RSTile(3293, 3262), new RSTile(3293, 3258),
                    new RSTile(3293, 3254), new RSTile(3293, 3250), new RSTile(3293, 3246), new RSTile(3293, 3242),
                    new RSTile(3293, 3238), new RSTile(3293, 3234), new RSTile(3296, 3229), new RSTile(3296, 3225),
                    new RSTile(3295, 3220), new RSTile(3294, 3217), new RSTile(3290, 3213), new RSTile(3287, 3209),
                    new RSTile(3284, 3205), new RSTile(3282, 3199), new RSTile(3281, 3195), new RSTile(3281, 3190),
                    new RSTile(3281, 3186), new RSTile(3281, 3182), new RSTile(3277, 3178), new RSTile(3277, 3174),
                    new RSTile(3275, 3170), new RSTile(3270, 3167)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(3298, 3284), new RSTile(3304, 3286),
                    new RSTile(3305, 3298), new RSTile(3305, 3318), new RSTile(3294, 3318), new RSTile(3294, 3307),
                    new RSTile(3289, 3299), new RSTile(3294, 3291), new RSTile(3293, 3285)});
                }
                else if(bankName.equals("East Ardougne")) {
                    bankArea = new RSArea(new RSTile(2649, 3287), new RSTile(2658, 3278));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(2701, 3331), new RSTile(2699, 3325), new RSTile(2697, 3321),
                    new RSTile(2694, 3317), new RSTile(2692, 3313), new RSTile(2691, 3310), new RSTile(2691, 3306),
                    new RSTile(2686, 3305), new RSTile(2681, 3305), new RSTile(2678, 3301), new RSTile(2677, 3297),
                    new RSTile(2672, 3295), new RSTile(2668, 3295), new RSTile(2663, 3294), new RSTile(2663, 3290),
                    new RSTile(2657, 3290), new RSTile(2652, 3290), new RSTile(2647, 3290), new RSTile(2643, 3289),
                    new RSTile(2643, 3285), new RSTile(2647, 3284), new RSTile(2651, 3284)});
                    mineArea = new RSArea(new RSTile[]{new RSTile(2694, 3338), new RSTile(2691, 3339),
                    new RSTile(2688, 3336), new RSTile(2689, 3331), new RSTile(2691, 3327), new RSTile(2711, 3327),
                    new RSTile(2716, 3329), new RSTile(2716, 3333), new RSTile(2711, 3334), new RSTile(2699, 3335)});
                }
                else if(bankName.equals("Yanille")) {
                    bankArea = new RSArea(new RSTile(2609, 3097), new RSTile(2613, 3088));
                    path[0] = walking.newTilePath(new RSTile[]{new RSTile(2631, 3141), new RSTile(2631, 3137), new RSTile(2629, 3133),
                    new RSTile(2627, 3129), new RSTile(2624, 3125), new RSTile(2623, 3121), new RSTile(2621, 3117),
                    new RSTile(2619, 3114), new RSTile(2617, 3111), new RSTile(2617, 3107), new RSTile(2615, 3103),
                    new RSTile(2611, 3100), new RSTile(2607, 3098), new RSTile(2607, 3093), new RSTile(2612, 3093)});
                    mineArea = new RSArea(new RSTile[]{ new RSTile(2625, 3129), new RSTile(2632, 3132),
                    new RSTile(2636, 3136), new RSTile(2640, 3137), new RSTile(2638, 3140), new RSTile(2634, 3143),
                    new RSTile(2632, 3149), new RSTile(2630, 3152), new RSTile(2624, 3152), new RSTile(2627, 3145),
                    new RSTile(2625, 3142), new RSTile(2628, 3138), new RSTile(2624, 3135) });
                }
                walker = new RegularWalker();
            }
            else if(bankName.equals("Mining Guild")){
                bankArea = new RSArea(new RSTile(3009,3358),new RSTile(3018,3355));
                mineArea = new RSArea(new RSTile[]{new RSTile(3022, 9741), new RSTile(3026, 9741),
                new RSTile(3031, 9743), new RSTile(3037, 9747), new RSTile(3043, 9754), new RSTile(3045, 9756),
                new RSTile(3047, 9756), new RSTile(3048, 9755), new RSTile(3053, 9748), new RSTile(3056, 9745),
                new RSTile(3055, 9739), new RSTile(3050, 9732), new RSTile(3045, 9732), new RSTile(3035, 9732),
                new RSTile(3027, 9733), new RSTile(3021, 9737)});
                path[0] = walking.newTilePath(new RSTile[]{new RSTile(3020, 3338), new RSTile(3026, 3337), new RSTile(3031, 3342),
                new RSTile(3029, 3348), new RSTile(3023, 3353), new RSTile(3013, 3356)});
                path[1] = walking.newTilePath(new RSTile[]{new RSTile(3046, 9751), new RSTile(3046, 9747), new RSTile(3043, 9743), new RSTile(3039, 9739), new RSTile(3034, 9738), new RSTile(3028, 9738), new RSTile(3022, 9740)});
                walker = new MiningGuildWalker();
            }
            else {
                log("Bank not supported yet.");
                dispose();
            }
        }

        private void saveSettings() {
            settings.put("ore1",oreTypeOption1.getSelectedItem().toString());
            settings.put("ore2",oreTypeOption2.getSelectedItem().toString());
            settings.put("ore3",oreTypeOption3.getSelectedItem().toString());
            settings.put("mine",mineOption.getSelectedItem().toString());
            settings.put("banking",String.valueOf(isBankingOption.isSelected()));
            settings.put("stealing",String.valueOf(isStealingRocks.isSelected()));
            FileOutputStream  fos = null;
            try {
                fos = new FileOutputStream(Constants.SETTINGS_FILE_PATH);
            } catch (FileNotFoundException ex) {
                File settingsFile = new File(Constants.SETTINGS_FILE_PATH);
                try {
                    settingsFile.createNewFile();
                    fos = new FileOutputStream(settingsFile);
                } catch (IOException ex1) { }
            }
            if(fos != null) {
                try {
                    settings.store(fos, "---dpedroia15's Miner GUI Settings---");
                } catch (IOException ex) { }
                finally {
                    try {
                        if(fos != null) {
                            fos.flush();
                            fos.close();
                        }
                    }
                    catch (IOException ex) { }
                }
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(startButton)) {
                oreA = new Ore(getOreIDForRock((String)oreTypeOption1.getSelectedItem()),getIdsForRock((String)oreTypeOption1.getSelectedItem()));
                oreB = new Ore(getOreIDForRock((String)oreTypeOption2.getSelectedItem()),getIdsForRock((String)oreTypeOption2.getSelectedItem()));
                oreC = new Ore(getOreIDForRock((String)oreTypeOption3.getSelectedItem()),getIdsForRock((String)oreTypeOption3.getSelectedItem()));
                setBank((String)mineOption.getSelectedItem());
                isBanking = isBankingOption.isSelected();
                isStealing = isStealingRocks.isSelected();
                saveSettings();
                setPaused(false);
                setVisible(false);
            }
        }


        public void windowOpened(WindowEvent e) { }

        public void windowClosing(WindowEvent e) {
            stopScript();
        }

        public void windowClosed(WindowEvent e) { }

        public void windowIconified(WindowEvent e) { }

        public void windowDeiconified(WindowEvent e) { }

        public void windowActivated(WindowEvent e) {
            if(!isPaused())
                setPaused(true);
        }

        public void windowDeactivated(WindowEvent e) {
            if(isPaused() && !isVisible())
                setPaused(false);
        }
    }
}

