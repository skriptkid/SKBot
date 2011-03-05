package scripts;

import java.util.Map;
import org.rsbot.script.wrappers.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.methods.*;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


@ScriptManifest(authors = {"ishitpants/cookiemonster"}, name = "iSalamander", version = 1.0, description = "Hunts salamanders")
            
public class iCookieHuntah extends Script implements PaintListener, ServerMessageListener {
    private long startTime = System.currentTimeMillis();int totalcaught = 0;int totalreleased = 0;int[] livingorangesalID = {5114,5115,5117};int minsleep = 200;int maxsleep = 450;int orangesalID[] = {10146,10147,10149};int netID = 303;int ropeID = 954;int[] tiedID = {19650,19662,19678};int[] emptyID = {19663,19679,19652};int[] catchedID = {19659,19675,19654};int actionID = 0;int[] fall1ID = {19660,19676,19656};int[] fall2ID ={19661,19657,19677};RSObject tied; RSObject empty; RSNPC sala; RSObject fallen1; RSObject fallen2; RSObject catched;RSTile MiddleRed = new RSTile(2449,3224);public boolean walktoprefallen = true;public boolean checkfortools = true;public boolean antibanz = true;public boolean checkforimps = false;
    public RSArea HuntArea = new RSArea(new RSTile(2446, 3222), new RSTile(2455, 3226));int percent = 0;int curxp = 0;int[] stuffz = {954,303};String status;public int releaseafter = 9;public RSArea HuntArea2 = new RSArea(new RSTile(3410, 3071), new RSTile(3416, 3079));int curlvl = 0;public Image paintRed;public Image paintOrange;public Image paintGreen;public Image salamanderimage;public huntergui gui;boolean guidone = false;
    public RSArea HuntArea3 = new RSArea(new RSTile(2446, 3222), new RSTile(2455, 3226));RSTile MiddleOrange = new RSTile(3413,3075);
    public RSArea HuntArea4 = new RSArea(new RSTile(2446, 3222), new RSTile(2455, 3226));
    public RSArea HuntArea5 = new RSArea(new RSTile(2446, 3222), new RSTile(2455, 3226));
      public void DoMouse(int speedcase){switch(speedcase){case 1:mouse.setSpeed(random(1, 3));  break;case 2:mouse.setSpeed(random(3, 5));  break;case 3:mouse.setSpeed(random(5, 7));  break;case 4:mouse.setSpeed(random(7, 9));  break;case 5:mouse.setSpeed(random(9, 11));  break;case 6:mouse.setSpeed(random(3,5));  break;case 7:mouse.setSpeed(random(5,6));break;case 8:mouse.setSpeed(random(6,7));break;}}
    
    @Override
    public boolean onStart(){ 
    gui = new huntergui();gui.setVisible(true);while (guidone == false){sleep(100);}    
    curlvl = skills.getCurrentLevel(Skills.HUNTER);curxp = skills.getCurrentExp(Skills.HUNTER);
            try {
                paintRed = ImageIO.read(new URL("http://img710.imageshack.us/img710/1940/paintwha2.png"));
                paintOrange = ImageIO.read(new URL("http://img576.imageshack.us/img576/5967/paintwha.png"));
                paintGreen = ImageIO.read(new URL("http://img51.imageshack.us/img51/8089/paintwha1.png"));
                salamanderimage = ImageIO.read(new URL("http://images1.wikia.nocookie.net/__cb20080417222821/runescape/images/7/7e/Red_Salamander.PNG"));
                } catch (final java.io.IOException e) {
                log("Error on getting images: ");
                e.printStackTrace();
                }  
    DoMouse(2);    
    return guidone;
    //http://images2.wikia.nocookie.net/__cb20090125092717/runescape/images/e/e1/Swamplizard.PNG

//http://images1.wikia.nocookie.net/__cb20080417222821/runescape/images/7/7e/Red_Salamander.PNG

//http://images4.wikia.nocookie.net/__cb20080417222744/runescape/images/5/57/Orange_Salamander.PNG

    }
 

    //----------------
    public int GetSalamanders(){int keuze = 0;if(HuntArea.contains(getMyPlayer().getLocation())){keuze = 1;}else if(HuntArea2.contains(getMyPlayer().getLocation())){keuze = 2;}else if(HuntArea3.contains(getMyPlayer().getLocation())){keuze = 3;}else if(HuntArea4.contains(getMyPlayer().getLocation())){keuze = 4;}else if(HuntArea5.contains(getMyPlayer().getLocation())){keuze = 5;}return keuze;}
    private boolean moveToScreenTile(final RSTile t){Point p = calc.tileToScreen(t);if (calc.pointOnScreen(p)){mouse.move(p.x, p.y, 5, 5);p = calc.tileToScreen(t);if (calc.pointOnScreen(p)){mouse.move(2, p.x, p.y, 5, 5);mouse.click(true);return true;} else {return false; }} else {return false;}}
    public boolean atTile(final RSTile tile, final int h, final double xd,final double yd, final String action) {try {final Point location = calc.tileToScreen(tile, .5, .5, h);if (location.x == -1 || location.y == -1) {return false;}mouse.move(location, 3, 3);String[] menuItems = menu.getItems();for (String menuItem : menuItems)if (menuItem.toLowerCase().contains(action))if (menuItems[0].toLowerCase().contains(action.toLowerCase())) {mouse.click(true);return true;} else {mouse.click(false);return menu.doAction(action);}}catch (final Exception e) {return false;}return true;}
    private Point randomPoint(Point click){int dif = 2;return new Point(click.x + random(-dif, dif), click.y + random(-dif, dif));}
    public boolean SpinTehCamerah(){camera.setAngle(random(0,359));return true;}
    private Polygon getPolygonFromTile(final RSTile tile) {if (tile == null) return null;final Point[] corners = {calc.tileToScreen(tile, 0, 1, 0), calc.tileToScreen(tile, 1, 1, 0),calc.tileToScreen(tile, 1, 0, 0),calc.tileToScreen(tile, 0, 0, 0)};final int[] xPoints = { corners[0].x, corners[1].x, corners[2].x, corners[3].x };final int[] yPoints = { corners[0].y, corners[1].y, corners[2].y, corners[3].y };return new Polygon(xPoints, yPoints, 4);}
    private boolean ESPTile(Graphics g1,final RSTile tile,String Imp) {Graphics2D g = (Graphics2D)g1;if (tile == null) return false;final Point X = calc.tileToScreen(tile,1,0,0);final Point Y = calc.tileToScreen(tile,0,1,0);g.setColor(new Color(0,0,0,0));g.drawString(Imp, X.x - 6 - 1, Y.y - 1);g.setColor(new Color(255,255,255,0));g.drawString(Imp, X.x - 5 - 1, Y.y - 1);return true;}
    //----------------

   boolean DropSalamander()
   {
   int numberofsalas = inventory.getCount(orangesalID);
   for (int i = 0; i < numberofsalas; i++) {
   RSItem item = inventory.getItem(orangesalID);
   item.doAction("Release");
   status = "Releasing(1)";
   sleep(random(850,900));
   }
   numberofsalas = inventory.getCount(orangesalID);
   for (int i = 0; i < numberofsalas; i++) {
   RSItem item = inventory.getItem(orangesalID);
   item.doAction("Release");
   status = "Releasing(2)";
   sleep(random(850,900));
   }
   
   numberofsalas = inventory.getCount(orangesalID);
   for (int i = 0; i < numberofsalas; i++) {
   RSItem item = inventory.getItem(orangesalID);
   item.doAction("Release");
   status = "Releasing(3)";
   sleep(random(850,900));
   }
   return true;
   }
 
   boolean CheckNetz()
   {
   catched = objects.getNearest(catchedID);                
            if (catched != null){
            if(HuntArea.contains(catched.getLocation()) || HuntArea2.contains(catched.getLocation()) || HuntArea3.contains(catched.getLocation()) || HuntArea4.contains(catched.getLocation()) || HuntArea5.contains(catched.getLocation()))
            {
            if (!calc.tileOnScreen(catched.getLocation())) {
                walking.walkTo(catched.getLocation());
                }
                while (getMyPlayer().isMoving()) 
                {
                sleep(random(50,70));
                }
                if(getMyPlayer().getAnimation() == -1) { 
                if(!calc.tileOnScreen(catched.getLocation()))
                {
                camera.turnToTile(catched.getLocation());
                }
                //atObject(catched,"Check");
                catched.doAction("Check");
                status = "Checking trap";
                sleep(random(500,600));
                status = "waiting...";
                }
            }
   }
   return true;
   }
   boolean SetupTrapz()
   {
   empty = objects.getNearest(emptyID);        
            if (empty != null){
            if(HuntArea.contains(empty.getLocation()) || HuntArea2.contains(empty.getLocation()) || HuntArea3.contains(empty.getLocation()) || HuntArea4.contains(empty.getLocation()) || HuntArea5.contains(empty.getLocation()))
            {
            if (!calc.tileOnScreen(empty.getLocation())) {
                walking.walkTo(empty.getLocation());
                }
                while (getMyPlayer().isMoving()) 
                {
                sleep(random(50,70));
                }
                if(getMyPlayer().getAnimation() == -1) { 
                if(!calc.tileOnScreen(empty.getLocation()))
                {
                camera.turnToTile(empty.getLocation());
                }
                empty.doAction("Set-trap");
                status = "Setting trap";
                sleep(random(500,600));
                status = "waiting...";
                }
            }
   }
   return true;
   }
   
    
    public void ThinkAboutAnAction()
    {
    empty = objects.getNearest(emptyID);    catched = objects.getNearest(catchedID);tied = objects.getNearest(tiedID);fallen1 = objects.getNearest(fall1ID);fallen2 = objects.getNearest(fall2ID);RSGroundItem stuff = groundItems.getNearest(303,954);int numberofsalas2 = inventory.getCount(orangesalID);int ropes = inventory.getCount(ropeID);int netz = inventory.getCount(netID);
    if(ropes < 1 || netz < 1 && empty == null && catched == null && tied == null && fallen1 == null && fallen2 == null && stuff == null && checkfortools)
    {
    log("We havent got enough ropes nor nets to continue, logging out.");
    stopScript();
    game.logout(true);
    }
    else if(numberofsalas2 > releaseafter || inventory.getCount() > 23)
    {
    actionID = 1;
    }
    else if(fallen1 != null || fallen2 != null && walktoprefallen)
    {
    actionID = 7;
    }
    else if(stuff != null)
    {
    actionID = 4;
    }
    /*else if(!HuntArea.contains(getMyPlayer().getLocation()) || !HuntArea2.contains(getMyPlayer().getLocation()) || !HuntArea3.contains(getMyPlayer().getLocation()) || !HuntArea4.contains(getMyPlayer().getLocation()) || !HuntArea5.contains(getMyPlayer().getLocation()))
    {
    actionID = 5;
    }
    */
    else if(catched != null)
    {
    actionID = 3;
    }
    else if(empty != null)
    {
    actionID = 2;
    }
    else
    {
    actionID = 8;
    }
    }
    
    public boolean AntiBan(int chance)
    {
    final int woopwoop = random(1,100);
    if(chance > 0 && chance < 30){SpinTehCamerah();log("ANTIBANNEZ - Spinning camera");if(woopwoop > 15 && woopwoop < 20){game.openTab(random(1,6));sleep(random(750,1000));game.openTab(game.TAB_INVENTORY);log("ANTIBANNEZ - Opened a random tab");}else{}
    if(chance > 500 && chance < 515){
    if(woopwoop > 25 && woopwoop < 50){
    mouse.move(random(0,500),random(0,500));
    log("ANTIBANNEZ - move mouse totally random");
    }
    else if(woopwoop > 45 && woopwoop < 66)
    {
    }
    }
    }return true;}
    
    public int loop() {
    if(antibanz)
    {
    AntiBan(random(0,5000));
    }
    ThinkAboutAnAction();
    switch(actionID) 
    {
        case 1:    //Release salamander
        DropSalamander();
        break;
        
        case 2://Setup trap
            SetupTrapz();
            break;
            
        case 3://Check net
            CheckNetz();
            break;
            
        case 4://pickup stuff
        RSGroundItem stuff = groundItems.getNearest(303,954);
        if(stuff != null)
        {
        if(HuntArea.contains(stuff.getLocation()) || HuntArea2.contains(stuff.getLocation()) || HuntArea3.contains(stuff.getLocation()) || HuntArea4.contains(stuff.getLocation()) || HuntArea5.contains(stuff.getLocation()))
        {
        if (!calc.tileOnScreen(stuff.getLocation())) {
                walking.walkTo(stuff.getLocation());
                }
                while (getMyPlayer().isMoving()) 
                {
                sleep(random(50,70));
                }

        if(!calc.tileOnScreen(stuff.getLocation()))
                {
                camera.turnToTile(stuff.getLocation());
                }
        stuff.doAction("Take");
        status = "Pickup stuff";
        sleep(random(100,170));
        status = "waiting...";
        }
        }
        break;
        
        case 5://Walk back
        status = "Walking back";
        if(GetSalamanders() == 1){walking.walkTo(MiddleRed);}else if(GetSalamanders() == 2){walking.walkTo(MiddleOrange);}else if(GetSalamanders() == 3){walking.walkTo(MiddleOrange);}else if(GetSalamanders() == 4){walking.walkTo(MiddleOrange);}else if(GetSalamanders() == 5){walking.walkTo(MiddleOrange);}
        break;
            
        case 6://drop
        DropSalamander();
        break;
        
        case 7://walk to prefallen things
        fallen1 = objects.getNearest(fall1ID);
        fallen2 = objects.getNearest(fall2ID);
        if(fallen1 != null)
        {
        if(HuntArea.contains(fallen1.getLocation()) || HuntArea2.contains(fallen1.getLocation()) || HuntArea3.contains(fallen1.getLocation()) || HuntArea4.contains(fallen1.getLocation()) || HuntArea5.contains(fallen1.getLocation()))
        {
        moveToScreenTile(fallen1.getLocation());
        status = "Stage 1 falling";
        sleep(random(1250,1750));
        status = "waiting...";
        }
        else if(fallen2 != null)
        {
        if(HuntArea.contains(fallen2.getLocation()) || HuntArea2.contains(fallen2.getLocation()) || HuntArea3.contains(fallen2.getLocation()) || HuntArea4.contains(fallen2.getLocation()) || HuntArea5.contains(fallen2.getLocation()))
        {
        moveToScreenTile(fallen2.getLocation());
        status = "Stage 2 falling";
        sleep(random(1250,1750));
        status = "waiting...";
        }
        }
        }
        break;
        
        case 8://standing still
        /*
        log("Case 8 has been called");
        tied = objects.getNearest(tiedID);
        sala = npcs.getNearest(livingorangesalID);
        final int dowhut = random(1,3);
        if(dowhut == 1)
        {
        if(tied != null)
        {
        Point tts = calc.tileToScreen(tied.getLocation());
        mouse.move(tts);
        status = "Hover trap";
        }
        }
        else if(dowhut == 2)
        {
        if(sala != null)
        {
        Point tts = calc.tileToScreen(sala.getLocation());
        mouse.move(tts);
        status = "Hover sala";
        }
        }
        else if(dowhut == 3)
        {
        if(sala != null)
        {
        Point tts = calc.tileToScreen(sala.getLocation());
        mouse.move(tts);
        status = "Hover sala";
        }
        }
        */

        break;
            
            
    }return  random(minsleep,maxsleep);}

        
@Override
public void onRepaint(Graphics g1) {

    Graphics2D g = (Graphics2D)g1;long timePlayed = System.currentTimeMillis() - startTime;long hours = timePlayed / (1000 * 60 * 60);timePlayed -= hours * (1000 * 60 * 60);long minutes = timePlayed / (1000 * 60);timePlayed -= minutes * (1000 * 60);long seconds = timePlayed / 1000;String hrStr = "00",minStr = "00",secStr = "00";hrStr = (hours < 10) ? ("0" + hours):(""+hours);minStr = (minutes < 10) ? ("0" + minutes):(""+minutes);secStr = (seconds < 10) ? ("0" + seconds):(""+seconds);int expGain = skills.getCurrentExp(Skills.HUNTER) - curxp;int huntedph = (int) ((totalcaught) * 3600000D / (System.currentTimeMillis() - startTime));int expPerHr = (int) ((expGain) * 3600000D / (System.currentTimeMillis() - startTime));
        
        if(GetSalamanders() == 1){g.drawImage(paintRed, 8, 278, null);}else if(GetSalamanders() == 2){g.drawImage(paintOrange, 8, 278, null);}else if(GetSalamanders() == 3){g.drawImage(paintGreen, 8, 278, null);}
        
        g.setColor(Color.WHITE);
        g.drawString("Hunted for : "+hrStr+":"+minStr+":"+secStr + "  Status : " + status, 116, 390);
        g.drawString("Caught : " + totalcaught + "(" + huntedph + "/h) - Released : " + totalreleased,116,405);
        g.drawString("Current level : "  + skills.getCurrentLevel(Skills.HUNTER) + "(" + (skills.getCurrentLevel(Skills.HUNTER) - curlvl) + ") - XP/H : " + expPerHr ,116,420);
        g.drawString("Cookiemonster in vent, ishitpants on forums.",116,435);
            Color color1 = Color.BLACK;Color color2 = Color.RED;Color color3 = Color.GREEN;
            g.setColor(color1);g.drawRect(8, 456, 488, 16);g.drawString("" + percent,240,464);
            g.setColor(color2);g.fillRect(8, 457, 488, 16);
            g.setColor(color3);percent = skills.getPercentToNextLevel(Skills.HUNTER);g.fillRect(8, 457, (int) (percent * 488 / 100.0), 16);g.drawString("Cookiemonster in vent, ishitpants on forums.",116,435);
            g.setColor(color1);g.drawString("%" + percent + " - To next level.",200,467);
          if(catched != null){if (calc.tileOnScreen(catched.getLocation())){final Point gfz = calc.tileToScreen(catched.getLocation());g.drawImage(salamanderimage,gfz.x,gfz.y,null);}}
          if(checkforimps){for (RSNPC npc :  npcs.getAll()) {{
          if(npc.getID() == 5115){//Orange salamander
          }
          if(npc.getID() == 6055){//baby impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Baby");}
          else if(npc.getID() == 6056){//young impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Young");}
          else if(npc.getID() == 6057){//Gourmet impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Gourmet");}
          else if(npc.getID() == 6058){//Earth impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Earth");}
          else if(npc.getID() == 6059){//Essence impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Essence");}
          else if(npc.getID() == 6060){//electric impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Electric");}
          else if(npc.getID() == 7904){//Spirit impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Spirit");}
          else if(npc.getID() == 6061){//nature impling
          g.setColor(new Color(0,255,0,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Nature");}
          else if(npc.getID() == 6062){//Magpie impling
          g.setColor(new Color(139,69,19,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Magpie");}
          else if(npc.getID() == 6063){//Ninja impling
          g.setColor(new Color(0,0,0,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Ninja");}
          else if(npc.getID() == 7846){//Pirate impling
          g.setColor(new Color(255,255,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Pirate");}
          else if(npc.getID() == 6064){//Dragon impling
          g.setColor(new Color(255,0,0,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Dragon");}
          else if(npc.getID() == 7905){//Zombie impling
          g.setColor(new Color(205,201,201,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Zombie");}
          else if(npc.getID() == 7906){//Kingly impling
          g.setColor(new Color(0,0,255,0));g.fillPolygon(getPolygonFromTile(npc.getLocation()));ESPTile(g,npc.getLocation(),"Kingly");}}}
        }
        }
public void serverMessageRecieved(ServerMessageEvent e) {     
    String msg = e.getMessage();
        if(msg.contains("darts")){
            totalreleased += 1;
        }
        else if(msg.contains("caught")){
            totalcaught += 1;
        }
}


public class huntergui extends JFrame implements ActionListener {
    public huntergui() {
        initComponents();
    }


    private void Start(ActionEvent e) {
        guidone = true;
        setVisible(false);
    }

    @Override

        public void actionPerformed(ActionEvent e) {
        if (e.getSource() == comboBox1) {
        /*
                if (comboBox1.getSelectedItem().equals("1")) {
                    releaseafter = 1;
                } else if (comboBox1.getSelectedItem().equals("2")) {
                    releaseafter = 2;
                } else if (comboBox1.getSelectedItem().equals("3")) {
                    releaseafter = 3;
                } else if (comboBox1.getSelectedItem().equals("4")) {
                    releaseafter = 4;
                } else if (comboBox1.getSelectedItem().equals("5")) {
                    releaseafter = 5;
                } else if (comboBox1.getSelectedItem().equals("6")) {
                    releaseafter = 6;
                } else if (comboBox1.getSelectedItem().equals("7")) {
                    releaseafter = 7;
                } else if (comboBox1.getSelectedItem().equals("8")) {
                    releaseafter = 8;
                } else if (comboBox1.getSelectedItem().equals("9")) {
                    releaseafter = 9;
                
                
                */

            } else if (e.getSource() == checkBox1) {
                if (checkBox1.isSelected()) {
                    antibanz = true;
                } else {
                    antibanz = false;
                }
            } else if (e.getSource() == checkBox2) {
                if (checkBox2.isSelected()) {
                    checkfortools = true;
                } else {
                    checkfortools = false;
                }
            } else if (e.getSource() == checkBox3) {
                if (checkBox3.isSelected()) {
                    walktoprefallen = true;
                } else {
                    walktoprefallen = false;
                }
            } else if (e.getSource() == okButton) {
                guidone = true;
                Start(e);
                dispose();
            } else if (e.getSource() == cancelButton) {
                setVisible(false);
                dispose();
                guidone = false;
                stopScript();
            }
        }


    private void initComponents() {
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        checkBox1 = new JCheckBox();
        checkBox2 = new JCheckBox();
        checkBox3 = new JCheckBox();
        checkBox4 = new JCheckBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        comboBox1 = new JComboBox();
        cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(null);

                
                //---- comboBox1 ----
                comboBox1.setModel(new DefaultComboBoxModel(new String[] {
                    "9",
                    "8",
                    "7",
                    "6",
                    "5",
                    "4",
                    "3",
                    "2",
                    "1"
                }));
                comboBox1.setFont(new Font("Tahoma", Font.BOLD, 11));
                contentPanel.add(comboBox1);
                comboBox1.setBounds(40, 0, 60, 25);
                
                //---- label1 ----
                label1.setText("After..");
                label1.setFont(new Font("Tahoma", Font.BOLD, 11));
                contentPanel.add(label1);
                label1.setBounds(0, 0, 75, 25);

                //---- label2 ----
                label2.setText("Release my salamanders");
                label2.setFont(new Font("Tahoma", Font.BOLD, 11));
                contentPanel.add(label2);
                label2.setBounds(100, 0, 165, 25);

                //---- checkBox1 ----
                checkBox1.setText("Antiban");
                checkBox1.setSelected(true);
                checkBox1.addActionListener(this);
                contentPanel.add(checkBox1);
                checkBox1.setBounds(-5, 30, 140, checkBox1.getPreferredSize().height);

                //---- checkBox2 ----
                checkBox2.setText("Check tools");
                checkBox2.setSelected(true);
                checkBox2.addActionListener(this);
                contentPanel.add(checkBox2);
                checkBox2.setBounds(-5, 50, 140, 23);

                //---- checkBox3 ----
                checkBox3.setText("Walk to prefallen traps");
                checkBox3.setSelected(true);
                checkBox3.addActionListener(this);
                contentPanel.add(checkBox3);
                checkBox3.setBounds(-5, 70, 140, 23);

                //---- checkBox4 ----
                checkBox4.setText("Overlay imps");
                checkBox4.addActionListener(this);
                contentPanel.add(checkBox4);
                checkBox4.setBounds(-5, 90, 140, 23);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < contentPanel.getComponentCount(); i++) {
                        Rectangle bounds = contentPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = contentPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    contentPanel.setMinimumSize(preferredSize);
                    contentPanel.setPreferredSize(preferredSize);
                }
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText("Hunt cookies");
                okButton.addActionListener(this);

                buttonBar.add(okButton, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("nevermind");
                cancelButton.addActionListener(this);

                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTextField textField1;
    private JLabel label1;
    private JLabel label2;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JCheckBox checkBox4;
    private JPanel buttonBar;
    private JComboBox comboBox1;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
    
}