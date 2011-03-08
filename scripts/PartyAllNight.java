import java.awt.*;
import java.awt.event.*;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.*;
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = { "QuietCrystal" }, keywords = "party", name = "Party All Night", version = 1.01, description = ("Falador Party Room. Burst and Bank."))
public class PartyAllNight extends Script implements PaintListener, KeyListener,
		ServerMessageListener {

	public String noticeMessage = "";
	public String status = "Banking";
	//Statuses: "Party Room", "Walking to bank", "Banking", "Walking to Party Room"
	public int totalProfit = 0;
	
	public RSArea bankArea = new RSArea(3009, 3355, 3018, 3358);
	public RSArea partyArea = new RSArea(3037, 3372, 3054, 3384);
	public RSArea slackWalkArea = new RSArea(3039, 3374, 3052, 3382);
	public RSTile[] walkToBank =  {new RSTile(3046, 3370), new RSTile(3032, 3368), new RSTile(3022, 3364), new RSTile(3015, 3362), new RSTile(3013, 3357)};
	//public RSTilePath walkToBank = null;
	public RSTile[] walkToParty = {new RSTile(3015, 3362), new RSTile(3022, 3364), new RSTile(3032, 3368), new RSTile(3046, 3370), new RSTile(3046, 3374)};
	//public RSTilePath walkToParty = null;
	
	//(int swX, int swY, int neX, int neY) 
	
	int bankID = 11758;

	@Override
	public void serverMessageRecieved(ServerMessageEvent e) {
	}

	@Override
	public void onRepaint(Graphics render) {
		Graphics2D g = (Graphics2D) render;
		try{
			RSComponent chatIntrf = interfaces.getComponent(137, 0);
			g.drawString(noticeMessage, chatIntrf.getAbsoluteX() + 5, chatIntrf.getAbsoluteY() - 10);
		} catch (Exception ex){
		}
	}
	
	public boolean onStart() {
		if (bankArea.contains(players.getMyPlayer().getLocation())) {
			status = "Banking";
		} else if (partyArea.contains(players.getMyPlayer().getLocation())) {
			status = "Party Room";
		} else {
			log("Please start the script in Falador Bank or Party Room");
			return false;
		}
		//walkToBank = walking.newTilePath(tempWalkToBank);
		//walkToParty = walking.newTilePath(tempWalkToParty);
		return true;
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(4, 7));
		if (status == "Party Room") {
			RSGroundItem toLoot = detectNearestLoot();
			if (toLoot != null) {
				if (verifyItem(toLoot.getItem())) {
					notify("Looting item: " + toLoot.getItem().getName());
					toLoot.doAction("Take " + toLoot.getItem().getName());
					return random(300, 600);
				}
			}
			
			RSObject balloon = detectNearestBalloon();
			if (balloon != null) {
				notify("Bursting balloon");
				mouse.setSpeed(random(1, 3));
				balloon.doAction("Burst Party Balloon");
				return random(300, 1000);
			}
			
			if (inventory.getCount() >= 20) {
				notify("Beginning walk to bank");
				status = "Walking to bank";
				return random(100, 200);
			}
			
			activateAntiban(0);
		} else if (status == "Walking to bank") {
			while (!bankArea.contains(players.getMyPlayer().getLocation())) {
				notify("Walking to bank. Do not stop script while walking!");
				activateAntiban(2);
				RSTilePath currentPath = randomizeMyPath(walkToBank);
				walking.walkTo(currentPath.getNext());
				sleep(500, 900);
			}
			status = "Banking";
		} else if (status == "Banking") {
			notify("Banking");
			RSObject bankBooth = objects.getNearest(bankID);
			if (!bank.isOpen() && inventory.getCount() != 0) {
				camera.turnToObject(bankBooth);
				bankBooth.doAction("Use-quickly");
			//} else if (bank.isOpen()) {
			} else {
				//calculateProfit();
				bank.depositAll();
				bank.close();
				status = "Walking to Party Room";
				activateAntiban(1);
			}
			return random(200, 500);
		} else if (status == "Walking to Party Room") {
			while (!partyArea.contains(players.getMyPlayer().getLocation())) {
				notify("Walking to Party Room. Do not stop script while walking!");
				activateAntiban(2);
				RSTilePath currentPath = randomizeMyPath(walkToParty);
				walking.walkTo(currentPath.getNext());
				sleep(500, 900);
			}
			status = "Party Room";
		}
		notify("Idle");
		activateAntiban(1);
		return random(300, 400);
	}
	
	public RSTilePath randomizeMyPath(RSTile[] myPath) {
		RSTile[] tempPath = myPath;
		/*for (int i = 0; i < tempPath.length; i++) {
			tempPath[i] = tempPath[i].randomize(3, 3);
		}*/
		return walking.newTilePath(tempPath);
	}
	
	public void notify(String message) {
		noticeMessage = message;
	}
	
	public RSObject detectNearestBalloon() {
		return objects.getNearest("Party Balloon"); 
	}
	
	public RSGroundItem detectNearestLoot() {
		try {
			RSGroundItem[] loot = groundItems.getAll(2);
			if (loot[0] != null) {
				return loot[0];
			} else {
				return null;
			}
		} catch (Exception ex) {
		}
		return null;
	}
	
	public void turnTo(RSObject object) {
		if (!object.isOnScreen()) {
			camera.turnToTile(object.getLocation(), random(-20, 21));
		}
	}
	
	public void turnTo(RSGroundItem object) {
		if (!object.isOnScreen()) {
			camera.turnToTile(object.getLocation(), random(-20, 21));
		}
	}
	
	public void calculateProfit() {
		//To be added in the future
		for (int i = 0; i < inventory.getItems().length; i++) {
			if (inventory.getItems()[i] != null) {
				RSItem toValue = inventory.getItems()[i];
				int quantity = 1;
				try {
					quantity = toValue.getStackSize();
				} catch (Exception ex) {
					quantity = 1;
				}
				/* if (toValue.getStackSize() == 0 || toValue.getStackSize() == 1) {
					quantity = 1;
				} else {
					quantity = toValue.getStackSize();
				} */
				int value = 0;
				try {
					GEItemInfo checker = grandExchange.loadItemInfo(toValue.getID());
					value = checker.getMarketPrice();
				} catch (Exception ex) {
					notify("ERROR: CAN'T GET GE PRICE");
				}

				totalProfit += value * quantity;
			}
		}
		notify("Total profit: " + totalProfit);
		sleep(3000, 3500);
	}
	
	public void activateAntiban(int mode) {
		if (random(1, 14) == 1 && mode <= 2) {
			notify("Antiban - Camera movement");
			camera.moveRandomly(random(1000, 2000));
		}
		if (random(1, 5) == 1 && mode <= 1) {
			notify("Antiban - Mouse movement");
			camera.moveRandomly(random(1500, 2500));
		}
		if (random(1, 16) == 1 && mode <= 0) {
			notify("Antiban - Random walking");
			RSTile[] toWalk = slackWalkArea.getTileArray();
			walking.walkTileMM(toWalk[random(0, toWalk.length)]);
		}
		sleep(1500, 2000);
	}
	
	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F12) {
			notify("Overriding script to dump inventory to bank.");
			status = "Walking to bank";
		}
	}
	
	public boolean verifyItem(RSItem item) {
		String name = item.getName().toLowerCase();
		if (name.indexOf("afro") != -1 || name.indexOf("10th anniversary") != -1) {
 			return false;
		}
		return true;
	}
}
