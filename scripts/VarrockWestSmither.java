import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.util.Timer;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

@ScriptManifest(authors = "Rawr", name = "Rawr Smither", version = 1.01, 
description = ("AIO Smither at Varrock West Bank."))
              
public class VarrockWestSmither extends Script implements PaintListener, MessageListener, MouseMotionListener {
public int itemMade = 0;
public int expbarInfo;
public int extraInfo;
public int tBanked = 0;
public Point mPlayer;
public String location = "";  
public int barID;
public int hammerID = 2347, scHammerID = 14112;
public int anvilID = 2783;
public int itemComponentID, barsLeft = 0;
public int itemXMin, itemXMax = 0;
public int itemYMin, itemYMax = 0;
public boolean canMakeItem = true;
public boolean canScroll = false;
public Timer idleTimer;
public int startingLevel;
public int startexp, expGained;
public float expsec, expmin, exphour;
public long timeRunning = 0, hours = 0, minutes = 0, seconds = 0, startTime = 0;
public String strBar, strItem;
public boolean checkingSkill = false;
public boolean hasHammer = true;
public RSArea bankArea  = new RSArea(new RSTile(3182, 3433), new RSTile(3192, 3446));
public RSArea anvilArea = new RSArea(new RSTile(3185, 3420), new RSTile(3190, 3427));
public RSTile bankTile  = new RSTile(3189,3435);
private JComboBox comboBars, comboItems;
SCRIPTGUI gui;

	public boolean onStart() {
		gui = new SCRIPTGUI();
		gui.setVisible(true);
		while (gui.isVisible()) {
			sleep(2000);
		}
		strBar = comboBars.getSelectedItem().toString();
		if ( strBar.equals("Bronze") )
			barID = 2349;
		else if ( strBar.equals("Iron") )
			barID = 2351;
		else if ( strBar.equals("Steel") )
			barID = 2353; 
		else if ( strBar.equals("Mithril") )
			barID = 2359;
		else if ( strBar.equals("Adamant") )
			barID = 2361;
		else if ( strBar.equals("Rune") )
			barID = 2363;
		strItem = comboItems.getSelectedItem().toString();
		if ( strItem.equals("dagger") ) {
			itemComponentID = 21;
	    } else if ( strItem.equals("hatchet") ) {
	        itemComponentID = 29;
	    } else if ( strItem.equals("mace") ) {
	        itemComponentID = 37;
	    } else if ( strItem.equals("med helm") ) {
	        itemComponentID = 45;
	    } else if ( strItem.equals("crossbow bolts") ) {
	        itemComponentID = 53;
	    } else if ( strItem.equals("sword") ) {
	        itemComponentID = 61;
	    } else if ( strItem.equals("dart tips") ) {
	        itemComponentID = 69;
	    } else if ( strItem.equals("nails") ) {
	        itemComponentID = 77;
	    } else if ( strItem.equals("studs") ) {
	        itemComponentID = 101;
	    } else if ( strItem.equals("arrow tips") ) {
	        itemComponentID = 109;
	    } else if ( strItem.equals("scimitar") ) {
	        itemComponentID = 117;
	    barsLeft = 1;
	    } else if ( strItem.equals("crossbow limbs") ) {
	        itemComponentID = 125;
	    } else if ( strItem.equals("longsword") ) {
	        itemComponentID = 133;
	    barsLeft = 1;
	    } else if ( strItem.equals("throwing knives") ) {
	        itemComponentID = 141;
	    } else if ( strItem.equals("full helm") ) {
	        itemComponentID = 149;
	    barsLeft = 1;
	    } else if ( strItem.equals("square shield") ) {
	        itemComponentID = 157;
	    barsLeft = 1;
	    } else if ( strItem.equals("bullseye lantern") ) {
	        itemComponentID = 165;
	    } else if ( strItem.equals("grapple tips") ) {
	        itemComponentID = 173;
	    } else if ( strItem.equals("warhammer") ) {
	        itemComponentID = 181;
	    } else if ( strItem.equals("battleaxe") ) {
	        itemComponentID = 189;
	    } else if ( strItem.equals("chainbody") ) {
	        canScroll = true;
	        itemComponentID = 197;
	        itemXMin = 30;
	        itemXMax = 90;
	        itemYMin = 220;
	        itemYMax = 240;
	    } else if ( strItem.equals("kiteshield") ) {
	        canScroll = true;
	        itemComponentID = 205;
	        itemXMin = 140;
	        itemXMax = 200;
	        itemYMin = 220;
	        itemYMax = 240;
	    } else if ( strItem.equals("claws") ) {
	        canScroll = true;
	        itemComponentID = 213;
	        itemXMin = 260;
	        itemXMax = 320;
	        itemYMin = 220;
	        itemYMax = 240;
	    } else if ( strItem.equals("2h sword") ) {
	        canScroll = true;
	        itemComponentID = 221;
	        itemXMin = 400;
	        itemXMax = 460;
	        itemYMin = 220;
	        itemYMax = 240;
	    } else if ( strItem.equals("plate skirt") ) {
	        canScroll = true;
	        itemComponentID = 229;
	        itemXMin = 30;
	        itemXMax = 90;
	        itemYMin = 270;
	        itemYMax = 290;
	    } else if ( strItem.equals("plate legs") ) {
	        canScroll = true;
	        itemComponentID = 237;
	        itemXMin = 140;
	        itemXMax = 200;
	        itemYMin = 270;
	        itemYMax = 290;
	    } else if ( strItem.equals("plate body") ) {
	        canScroll = true;
	        itemComponentID = 245;
	        itemXMin = 260;
	        itemXMax = 320;
	        itemYMin = 270;
	        itemYMax = 290;
	        barsLeft = 2;
	    }
	    startingLevel = skills.getCurrentLevel(Skills.SMITHING);
	    startexp = skills.getCurrentExp(Skills.SMITHING);
	    startTime = System.currentTimeMillis();
	    idleTimer = new Timer(30000);   
	    return true;
	}

	public void antiBan() {
		if (random(1, 20) == 1) {
			camera.setAngle(random(1, 359));
		}
		if (random(1, 20) == 1) {
			final int x = (int) mouse.getLocation().getX();
			final int y = (int) mouse.getLocation().getY();
			mouse.move(x + random(-100, 100), y + random(-50, 50));
		}
		if (random(1, 100) == 1) {
			game.openTab(Game.TAB_STATS);
			skills.doHover(Skills.INTERFACE_SMITHING);
			sleep(1500, 2000);
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	private boolean atBank(){
		return bankArea.contains(getMyPlayer().getLocation());
	}

	private boolean atAnvil(){
		return anvilArea.contains(getMyPlayer().getLocation());
	}

	private void depositItems() {
		camera.setPitch(true);
		if (players.getMyPlayer().getAnimation() == -1 && !players.getMyPlayer().isMoving() && !bank.isOpen()) {
			if (bank.open())
				sleep(200, 400);
		}
		if (bank.isOpen()) {
			if (bank.depositAllExcept(hammerID, scHammerID, barID))
				sleep(200, 400); 
		}
		if (bank.isOpen() && bank.getCount(barID) == 0) {
			log.info("All of of supplies, stopping script!");
			sleep(1500, 3000);
			stopScript(true);
		}
	}

	private void withdrawBars() {
		if (players.getMyPlayer().getAnimation() == -1 && !getMyPlayer().isMoving() && !bank.isOpen()) {
			if (bank.open())
				sleep(200, 400);
		}

		if(bank.isOpen() && !hasHammer) {
			if (bank.getCount(scHammerID) > 0) {
				if (bank.withdraw(scHammerID, 1))
					hasHammer = true;
			} else if (bank.getCount(hammerID) > 0) {
				if (bank.withdraw(hammerID, 1))
					hasHammer = true;
			} else {
				log.info("Out of hammers, stopping script!");
				sleep(1500, 3000);
				stopScript(true);
			}
			sleep(200, 400);
		}
		
		if(bank.isOpen() && inventory.getCount(barID) != 27) {
			if (bank.withdraw(barID, 0))
				sleep(200, 400);
		}
		canMakeItem = true;
	}

	private void clickAnvil() {
		RSItem bar = inventory.getItem(barID);
		RSObject anvil = objects.getNearest(anvilID);
		sleep(200, 400);
		if (!interfaces.get(300).isValid()) {
			if (bar != null && anvil != null) {
				if (!inventory.isItemSelected())
					bar.doClick(true);
				anvil.doAction("Use " + strBar + " bar -> Anvil");
			}
		}
	}

	private void makeItem() {
		if (interfaces.get(300).isValid()) {
			canMakeItem = false;
			if (canScroll) {
				mouse.click(500, 270, 4, 20, true);
            	canScroll = false; 
			}
        
			if (itemXMin == 0)
				interfaces.getComponent(300, itemComponentID).doAction("Make All");
			else {
				mouse.move(random(itemXMin,itemXMax), random(itemYMin,itemYMax));
				sleep(200, 400);
				mouse.click(false);
				menu.clickIndex(3);
			}
		}
		sleep(1500, 2500);
	}
    
	@Override
	public int loop() {
		mouse.setSpeed(random(6, 8));
		if (idleTimer.getRemaining() < 3000)
			canMakeItem = true;    

		if (!players.getMyPlayer().isIdle())
			idleTimer.reset();

		if (interfaces.canContinue()) {
			interfaces.clickContinue();
			canMakeItem = true;
		}

		if (!atAnvil() && !walking.isRunEnabled() && walking.getEnergy() > 60)
			walking.setRun(true);

		if (hasHammer && canMakeItem && atAnvil() && players.getMyPlayer().isIdle() 
				&& !interfaces.get(300).isValid()) {
			clickAnvil();
		}

		if (hasHammer && canMakeItem && atAnvil() && players.getMyPlayer().isIdle() 
				&& interfaces.get(300).isValid() ) {
			makeItem();
		}

		if (hasHammer && inventory.getCount(barID) > barsLeft && !atAnvil() && players.getMyPlayer().isIdle()) { 
			walking.walkTileMM(new RSTile(random(3186,3190), random(3423, 3427)), 0, 0);
		}

		if (!atBank() && (inventory.getCount(barID) == barsLeft || !hasHammer) && players.getMyPlayer().isIdle() ) { 
			walking.walkTileMM(bankTile, 0, 0);
		}

		if (inventory.getCountExcept(true, barID, hammerID, scHammerID) > 0 && atBank()) {
			depositItems();
		}

		if (inventory.getCount(barID) < 27 && atBank() 
				&& inventory.getCountExcept(true, barID, hammerID, scHammerID) == 0) {
			withdrawBars();
		}
		antiBan();
		return 100;
	}

	public class SCRIPTGUI extends JFrame {
		private static final long serialVersionUID = 1L;
		public SCRIPTGUI() {
			createAndDisplayGUI();
		}

		@SuppressWarnings("unused")
		private void StartButtonActionPerformed(ActionEvent e) {
			dispose();
		}

		private void createAndDisplayGUI() {
			setTitle("Rawr Smither");
			JPanel pane = new JPanel(new BorderLayout());
			JButton start = new JButton("Start");
			start.setActionCommand("Start");
			start.addActionListener(new ButtonListener());
			pane.add(start, BorderLayout.SOUTH);
			comboBars = new JComboBox( new Object[] { "Bronze", "Iron", "Steel", "Mithril", "Adamant", "Rune" } );
			comboBars.setSelectedIndex(0);
			pane.add(comboBars, BorderLayout.WEST);
			comboItems = new JComboBox( new Object[] { "dagger", "hatchet", 
					"mace", "med helm", "crossbow bolts", "sword",
                    "dart tips", "nails", "studs", "arrow tips",
                    "scimitar", "crossbow limbs", "longsword", "throwing knives",
                    "full helm", "square shield", "bullseye lantern", "grapple tips",
                    "warhammer", "battleaxe", "chainbody", "kiteshield",
                    "claws", "2h sword", "plate skirt", "plate legs", "plate body" } );
			comboItems.setSelectedIndex(0);
			pane.add(comboItems, BorderLayout.EAST);
			add(pane);
			pack();
			setVisible(true);
            setLocationRelativeTo(getOwner());
		}
		
		class ButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				JButton button = (JButton) e.getSource();
				if (e.getActionCommand().equals("Start"))
					dispose();
				}
			}
		}

    public void mouseDragged(MouseEvent e) {
        mPlayer = e.getPoint();
    }

    public void mouseMoved(MouseEvent e) {
        mPlayer = e.getPoint();

    }
    
    public void onRepaint(Graphics g) {
    	Graphics2D G = (Graphics2D)g;
    	if (game.isLoggedIn()) {
    		Point m = mouse.getLocation();
    		final Color Black = new Color(0, 0, 0);
    		final Color White = new Color(255, 255, 255);
    		final Color Blue = new Color(0, 0, 204, 225);
    		final Color BlackTrans = new Color(10, 10, 10, 150);
    		final int percent = skills.getPercentToNextLevel(Skills.SMITHING);
    		final int x = 16;
    		int y = 40;
            G.setColor(new Color(0, 0, 204, 225));
            G.drawLine(0, m.y, 1024, m.y);
            G.drawLine(m.x, 0, m.x, 1024);
            G.setColor(Black);
            G.drawLine(mPlayer.x - 8, mPlayer.y, mPlayer.x + 8, mPlayer.y);
            G.drawLine(mPlayer.x, mPlayer.y - 8, mPlayer.x, mPlayer.y + 8);
            timeRunning = System.currentTimeMillis() - startTime; 
            seconds = timeRunning / 1000;
            if (seconds >= 60) { 
                minutes = seconds / 60; 
                seconds -= minutes * 60; 
            } 
            if (minutes >= 60) { 
                hours = minutes / 60; 
                minutes -= hours * 60; 
            }     
            expGained = skills.getCurrentExp(Skills.SMITHING) - startexp;
            if (expGained > 0) {
                expsec = ((float) expGained)/(float)(seconds + (minutes*60) + (hours*60*60));
            }
            expmin = expsec * 60;
            exphour = expmin * 60;
            G.setColor(Blue);
            G.drawRect(8, 24, 168, 112);
            G.setColor(BlackTrans);
            G.fillRect(9, 25, 167, 111);
            G.setColor(Blue);
            G.drawRect(125, 24, 51, 15);
            G.setFont(new Font("SansSerif", 0, 12));
            G.setColor(White);
            G.drawString("Info", 140, 36);
            G.drawString("Smithing Level:" + skills.getCurrentLevel(Skills.SMITHING), x, y);
            y += 15;
            G.drawString("Bars Smithed:" +itemMade, x, y);
            y += 15;
            G.drawString("Remaning Exp:" + skills.getExpToNextLevel(Skills.SMITHING), x, y);
            y += 8;
            G.drawLine(x - 2, y, 167, y);
            y += 15;
            G.drawString("Runtime: " + hours + ":" + minutes + ":" + seconds, x, y);
            y += 15;
            G.drawString("Times Banked:" + tBanked, x, y);
            y += 15;
            G.setColor(Color.RED);
            G.fillRoundRect(x, y, 150, 10, 0, 0);
            G.setColor(Color.GREEN);
            G.fillRoundRect(x, y,  (int) (percent * 150 / 100.0), 10, 0, 0);
            if (mPlayer.x >= x && mPlayer.x < x + 150 && mPlayer.y >= y && mPlayer.y < y + 10) {
                expbarInfo = 1;
            } else {
                expbarInfo = 0;
            }
            if (mPlayer.x >= 125 && mPlayer.x < 125 + 51 && mPlayer.y >= 24 && mPlayer.y < 24 + 15) {
                extraInfo = 1;
            } else {
                extraInfo = 0;
            }
            if (expbarInfo == 1) {
            	G.setColor(Blue);
            	G.drawRect(mPlayer.x, mPlayer.y - 19, 220, 19);
            	G.setColor(BlackTrans);
            	G.fillRect(mPlayer.x, mPlayer.y -18, 220, 18);
            	G.setColor(White);
            	G.drawString(skills.getPercentToNextLevel(Skills.SMITHING) + "% until Level " + (skills.getCurrentLevel(Skills.SMITHING) + 1), mPlayer.x + 4, mPlayer.y - 6);
            	G.drawString("Levels Gained:" + (skills.getCurrentLevel(Skills.SMITHING) - startingLevel), mPlayer.x + 116, mPlayer.y - 6);
            }
            if (extraInfo == 1) {
            	G.setColor(Blue);
            	G.drawRect(180, 24, 176, 51);
            	G.setColor(BlackTrans);
            	G.fillRect(181, 25, 175, 50);
            	G.setFont(new Font("SansSerif", 0, 12));
            	G.setColor(White);
            	G.drawString("Making: " + strBar + " " + strItem, 188, 38);
            	if (atBank()) { 
            		location = "Bank"; 
            	} else if (atAnvil()) { 
            		location = "Anvil"; 
            	} else { 
            		location = "Between";
            	}
            	G.drawString("Location: " + location, 188, 53);
            	G.drawString("Exp/Hr: "+ (int) exphour, 188, 68);
            }
    	}
    }

    public void messageReceived(MessageEvent e) {   
    	String message = e.getMessage();
    	if (message.contains("item crumbles")) {
    		hasHammer = false;
    	}

    	if (message.contains("make")) {
    		itemMade++;
    	}

    	if (message.contains("You don't have")) {
    		tBanked++;
    	}
    }
}

