import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.methods.Magic;
 
@ScriptManifest(authors = "Aidden", name = "Aidden's Superheater", version = 1.12, description = "Superheat any ore")
public class ASuperHeat extends Script implements PaintListener, MouseListener, MouseMotionListener
{
 private final double version = 1.12;
 private String status = "Loading...";
 private int setSpeed,tempSpeed,totalBars,antibanFrequency;
 private String barType;
 private final int natureID = 561;
 private myGUI gui;
 private boolean Ready = false;
 private boolean Continue = false;
 private boolean screenshotOnFinish;
 private boolean useAntiban, statCheck, offScreen, moveMouse, rotateCamera, useFkeys;
 private final int copperID = 436;
 private final int tinID = 438;
 private final int ironID = 440;
 private final int silverID = 442;
 private final int coalID = 453;
 private final int goldID = 444;
 private final int mithrilID = 447;
 private final int adamantiteID = 449;
 private final int runiteID = 451;
 private final int bronzebarID = 2349;
 private final int ironbarID = 2351;
 private final int steelbarID = 2353; 
 private final int silverbarID = 2355;
 private final int goldbarID = 2357;
 private final int mithrilbarID = 2359;
 private final int adamantbarID = 2361;
 private final int runebarID = 2363;
 private int magicxpGained, magicxptolvl;
 private long magicxphour,smithingxphour;
 private int magicLevelsGained;
 private int smithingxpGained, smithxptolvl;
 private int smithingLevelsGained;
 private int range;
 private long millis,seconds,minutes,hours,magiclevelsec,magiclevelmin,magiclevelhour,smithlevelsec,smithlevelmin,smithlevelhour;
 private Rectangle tab1 = new Rectangle(495, 460, 17, 15);
 private Rectangle tab2 = new Rectangle(300, 390, 100, 10);
 private Rectangle tab3 = new Rectangle(300, 405, 100, 10);
 private int magicStartXP, smithingStartXP, magicStartLevel, smithingStartLevel;
 private Point p; 
 RSItem ore1;
 RSItem ore2;
 private enum Step {Unknown,Superheat,Openbank,Bank};
 private int ore1Amount;
 private boolean showPaint = true, showMagicXP = false, showSmithingXP = false;
 private int casts = 0;
 private int ratio = 1;
 private final Color color1 = new Color(0, 0, 0, 210);
 private final Color color2 = new Color(0, 0, 0);
 private final Color color3 = new Color(255, 255, 255);
 private final Font font1 = new Font("Lucida Handwriting", 1, 16);
 private final Font font2 = new Font("Arial", 0, 12);
 private Image getImage(String url) 
 {
  try
  {
   return ImageIO.read(new URL(url));
  } catch(IOException e) 
  {
   return null;
  }
 }
 private final Image img1 = getImage("http://www.zybez.net/img/idbimgb/1401.gif");
 private final Image img2 = getImage("http://images-mediawiki-sites.theful...1537412877.png");
 private  long startTime = System.currentTimeMillis();

 public boolean onStart() 
 {
  try {
   SwingUtilities.invokeAndWait(new Runnable() 
   { 
    public void run() 
    { 
     gui = new myGUI();
     gui.setVisible(true);
    } 
   });
  } 
  catch (InterruptedException e){} 
  catch (InvocationTargetException e){}
  while(gui.isVisible())
  {
   sleep(50);
  }
  setSpeed = tempSpeed;
  mouse.setSpeed(setSpeed);
  log("Aidden's SuperHeater Loaded.");
  return true;
 }
 public void onFinish() 
 {
  log("Bars made:"+casts+" in "+hours+":"+minutes+":"+seconds);
  log("Magic XP gained:"+magicxpGained+", Magic levels gained:"+magicLevelsGained);
  log("Smithing XP gained:"+smithingxpGained+", Smithing levels gained:"+smithingLevelsGained);
  log("Thankyou for using Aidden's SuperHeater.");
  if (screenshotOnFinish)
   env.saveScreenshot(false);
 }
 public int loop() 
 {
  if(game.isLoggedIn())
  {
   antiban();
   doAction();
  }
  return random(200, 300);
 }
 public void logout()
 {
  game.logout(true);
  stopScript();
 }
 public int getType(int ore)
 {
  if (barType == "Bronze")
  {
   ratio = 1;
   ore1Amount = 13;
   if (ore == 1)
    return copperID;
   if (ore == 2)
    return tinID;
   if (ore == 3)
    return bronzebarID;
  }
  if (barType == "Iron")
  {
   ratio = 1;
   ore1Amount = 27;
   if (ore == 1)
    return ironID;
   if (ore == 2)
    return ironID;
   if (ore == 3)
    return ironbarID;
  } 
  if (barType == "Silver")
  {
   ratio = 1;
   ore1Amount = 27;
   if (ore == 1)
    return silverID;
   if (ore == 2)
    return silverID;
   if (ore == 3)
    return silverbarID;
  }
  if (barType == "Steel")
  {
   ratio = 2;
   ore1Amount = 9;
   if (ore == 1)
    return ironID;
   if (ore == 2)
    return coalID;
   if (ore == 3)
    return steelbarID;
  }
  if (barType == "Gold")
  {
   ratio = 1;
   ore1Amount = 27;
   if (ore == 1)
    return goldID;
   if (ore == 2)
    return goldID;
   if (ore == 3)
    return goldbarID;
  }
  if (barType == "Mithril")
  { 
   ratio = 4;
   ore1Amount = 5;
   if (ore == 1)
    return mithrilID;
   if (ore == 2)
    return coalID;
   if (ore == 3)
    return mithrilbarID;
  }
  if (barType == "Adamant")
  { 
   ratio = 6;
   ore1Amount = 3;
   if (ore == 1)
    return adamantiteID;
   if (ore == 2)
    return coalID;
   if (ore == 3)
    return adamantbarID;
  }
  if (barType == "Rune")
  { 
   ratio = 8;
   ore1Amount = 3;
   if (ore == 1)
    return runiteID;
   if (ore == 2)
    return coalID;
   if (ore == 3)
    return runebarID;
  }
  return 0;
 }
 public void doAction()
 {
  switch(currentStep())
  {
  case Unknown:
   status = "Waiting...";
   break;
  case Superheat:
   status = "Casting Superheat item";
   Ready = false;
   if(game.getCurrentTab() != Game.TAB_MAGIC)
   {
    keyboard.pressKey((char) KeyEvent.VK_F4);
    sleep(random(50,200));
    keyboard.releaseKey((char) KeyEvent.VK_F4); 
   }
   try 
   {
    magic.castSpell(Magic.SPELL_SUPERHEAT_ITEM);
   }
   catch (Exception e) {}
   if (game.getCurrentTab() == Game.TAB_INVENTORY)
   {
    if (inventory.containsOneOf(natureID) && inventory.containsOneOf(getType(1)) && inventory.containsOneOf(getType(2)))
    {
     if (inventory.getCount(getType(2))/ratio >= inventory.getCount(getType(1)))
     {
      Continue = true;
      ore1 = inventory.getItemAt(inventory.getCount(natureID, getType(1), getType(2), getType(3))-inventory.getCount(getType(2))-1);
      ore2 = inventory.getItem(getType(2)); 
      try
      {
       ore1.doAction("cast");
      }
      catch(Exception e){}
     } 
    }
    else
    {
     Continue = false;
    }
   }
   else
   {
    Continue = false;
   }
   break;
  case Openbank:
   status = "Opening bank";
   if(!bank.isOpen())
   {
    bank.open();
    sleep(20, 30); 
   }
   break;
  case Bank:
   status = "Banking";
   if(bank.isOpen())
   {    
    if (inventory.containsOneOf(natureID))
    {
     if (bank.getItem(getType(1)) != null && bank.getItem(getType(2)) != null)
     {
      RSItem ore1 = bank.getItem(getType(1));
      RSItem ore2 = bank.getItem(getType(2));
      try
      {
       if (28-inventory.getCount(natureID,coalID) >= ratio)
       {
        bank.depositAllExcept(natureID,coalID, 0);
       }
       else
       {
        bank.depositAllExcept(natureID, 0);
       }
      }
      catch (Exception e) {}
      sleep(200, 400);  
      if (barType != "Iron" && barType != "Gold" && barType != "Silver")
      {
       if(!ore1.doAction("Withdraw-"+ore1Amount))
       {
        if (ore1.doAction("Withdraw-X"))
        {
         sleep(random(1300, 1600));
         keyboard.sendText("" + ore1Amount, true);
        }
       }
       sleep(400, 600);  
       try
       {
        ore2.doAction("Withdraw-All");
       }
       catch(Exception e){}
      }
      else
      {
       try
       {
        ore1.doAction("Withdraw-All");
       }
       catch(Exception e){}
      }
      sleep(200, 300);  
      mouse.move(490, 36, 3, 3);      
      sleep(200, 300);            
      mouse.click(true); 
      sleep(200,300);
      if (game.getCurrentTab() == Game.TAB_MAGIC);
      {
       Ready = true;
      }
     }
     else
      logout();
    }
    else
    {
     if (bank.getItem(natureID) != null)
     {
      RSItem nat = bank.getItem(natureID);
      try
      {
       bank.depositAll();
      }
      catch (Exception e) {}
      try
      {
       nat.doAction("Withdraw-All");
      }
      catch(Exception e){}
      sleep(200, 300);  
     }
     else
      logout();
    }
   }
   break;
  }
 }
 

 public void antiban() 
 {
  int s = random(0, 5);
  switch (s) 
  {
  case 1:
   changeSpeed();
   break;
  case 3:
   changeSpeed();
   break;
  }

  if (useAntiban)
  {
   double b = random(0, 5/(double)(antibanFrequency/100.00));
   switch ((int)b) 
   {
   case 1:
    if (moveMouse)
    {
     if (random(0, 8) == 4) 
     {
      status = "[Antiban] Moving mouse";
      mouse.moveSlightly();
      sleep(600, 1500);
      mouse.moveRandomly(150, 350);
      sleep(200, 2000);
      mouse.moveRandomly(150, 350);
     }
    }
    else
    {
     antiban();
    }
    break;
   case 2:
    if (rotateCamera)
    {
     if (random(0, 8) == 3) 
     {
      status = "[Antiban] Rotating camera";
      int temp = random(1, 3);
      for (int i = 0; i<temp;i++)
      {
       if (random(1, 3) == 1)
       {
        camera.setPitch(random(50, 100));
        if (random(1, 3) == 1)
         sleep(200, 300);
        camera.setAngle(random(10, 180));
       }
       else
       {
        camera.setPitch(random(50, 70));
        if (random(1, 3) == 1)
         sleep(200, 300);
        camera.setAngle(random(10, 90));
       }
      }
     }
    }
    else
    {
     antiban();
    }
    break;
   case 3:
    if (offScreen)
    {
     if (random(0, 16) == 6) 
     {
      status = "[Antiban] Moving mouse off screen";
      mouse.moveOffScreen();
      sleep(random(800, random(1200, 3000)));
     }
    }
    else
    {
     antiban();
    }
    break;
   case 4:
    if (!bank.isOpen() && statCheck)
    {
     if (random(0, 23) == 3) 
     {
      status = "[antiban] Checking Xp";
      game.openTab(Game.TAB_STATS);
      sleep(random(20, 30));
      if(game.getCurrentTab() != Game.TAB_STATS)
      {
       game.openTab(Game.TAB_STATS);
      }
      skills.doHover(Skills.INTERFACE_MAGIC);              
      sleep(random(2000, 3000));
      game.openTab(Game.TAB_INVENTORY);
     }
     if (random(0, 23) == 3) 
     {
      status = "[antiban] Checking Xp";
      game.openTab(Game.TAB_STATS);
      sleep(random(20, 30));
      if(game.getCurrentTab() == Game.TAB_INVENTORY)
      {
       game.openTab(Game.TAB_STATS);
      }
      skills.doHover(Skills.INTERFACE_SMITHING);                
      sleep(random(2000, 3000));
      game.openTab(Game.TAB_INVENTORY);
     }
    }
    else
    {
     antiban();
    }
    break; 
   default:
    break;
   }
  } 

 }
 public void changeSpeed()
 {
  setSpeed = tempSpeed+random(-range, range+1);
  if (setSpeed < 1 || setSpeed > 10)
   setSpeed = tempSpeed;
  mouse.setSpeed(setSpeed);
 }
 public void onRepaint(final Graphics g) 
 {
  millis = System.currentTimeMillis() - startTime;
  hours = millis / (1000 * 60 * 60);
  millis -= hours * (1000 * 60 * 60);
  minutes = millis / (1000 * 60);
  millis -= minutes * (1000 * 60);
  seconds = millis / 1000;
  casts = magicxpGained/53;

  Point mouseLoc = mouse.getLocation();
  int mouseX = (int)mouseLoc.getX();
  int mouseY = (int)mouseLoc.getY();
  g.setColor(Color.white);
  g.drawLine(mouseX-10, mouseY, mouseX+10, mouseY);
  g.drawLine(mouseX, mouseY-10, mouseX, mouseY+10);
  if (showPaint)
  {
   g.setColor(color1);
   g.fillRect(7, 345, 505, 128);
   g.setColor(color2);
   g.drawRect(7, 345, 505, 128);
   g.setFont(font1);
   g.setColor(color3);
   g.drawString("Aidden's SuperHeater", 15, 370);
   g.drawString("Ver. "+version, 340, 370);
   g.setFont(font2);
   g.drawString("Runtime: "+hours+": "+minutes+": "+seconds, 20, 400);
   float castshour = (((float) casts)/(float)(seconds + (minutes*60) + (hours*60*60))*60*60);
   g.drawString("Bars/H: "+casts+" / "+(int)castshour, 20, 415);
   g.drawString("Bar Type: "+barType, 20, 430);
   g.drawString("Mouse Speed: "+setSpeed, 20, 445);
   g.drawString("Status: "+status, 20, 460);
   g.drawImage(img1, 370, 380, null);
   g.drawImage(img2, 420, 420, null);
   //MAGIC PROGRESS BAR
   final int magicPercent = skills.getPercentToNextLevel(Skills.MAGIC);
   g.drawString("Magic level " + skills.getCurrentLevel(Skills.MAGIC) + ":", 200, 400);
   g.setColor(Color.red);
   g.fillRect(tab2.x, tab2.y, tab2.width, tab2.height);
   g.setColor(Color.green);
   g.fillRect(tab2.x, tab2.y, magicPercent, tab2.height);

   g.setColor(Color.black);
   g.drawString(magicPercent+"%", 340, 400); 
   g.drawRect(tab2.x, tab2.y, tab2.width, tab2.height);
   g.drawRect(tab2.x, tab2.y, magicPercent, tab2.height);
   //XP GAINED
   g.setColor(Color.white);

   if (magicStartXP == 0) 
   {
    magicStartXP = skills.getCurrentExp(Skills.MAGIC);
   }
   magicxpGained = skills.getCurrentExp(Skills.MAGIC) - magicStartXP;
   magicxphour = ((magicxpGained)/(seconds + (minutes*60) + (hours*60*60))*60*60);

   //LEVELS GAINED
   if (magicStartLevel == 0)
   {
    magicStartLevel = skills.getCurrentLevel(Skills.MAGIC);
   }
   magicLevelsGained = skills.getCurrentLevel(Skills.MAGIC) - magicStartLevel;
   if (showMagicXP)
   {
    magicxptolvl = skills.getExpToNextLevel(Skills.MAGIC);
    magiclevelsec =  magicxptolvl / (magicxphour / 3600);
    magiclevelhour = magiclevelsec / (60*60);
    magiclevelsec -= magiclevelhour * (60*60);
    magiclevelmin = magiclevelsec /  60;
    magiclevelsec -= magiclevelmin * 60;
    g.drawString("Magic XP/H: " + magicxpGained + " / " + (int)magicxphour, 200, 430);
    g.drawString("Magic levels gained: " + magicLevelsGained, 200, 445);
    g.drawString("TTL: "+magiclevelhour+": "+magiclevelmin+": "+magiclevelsec, 200, 460);
   }
 
   //SMITHING PROGRESS BAR
   final int smithingPercent = skills.getPercentToNextLevel(Skills.SMITHING);
   g.drawString("Smithing level " + skills.getCurrentLevel(Skills.SMITHING) + ":", 200, 415);
   g.setColor(Color.red);
   g.fillRect(tab3.x, tab3.y, tab3.width, tab3.height);
   g.setColor(Color.green);
   g.fillRect(tab3.x, tab3.y, smithingPercent, tab3.height);
   g.setColor(Color.black);
   g.drawString(smithingPercent+"%", 340, 415); 
   g.drawRect(tab3.x, tab3.y, tab3.width, tab3.height);
   g.drawRect(300, 405, smithingPercent, 10);
   //XP GAINED
   g.setColor(Color.white);
   smithingxpGained = 0;
   if (smithingStartXP == 0) 
   {
    smithingStartXP = skills.getCurrentExp(Skills.SMITHING);
   }
   smithingxpGained = skills.getCurrentExp(Skills.SMITHING) - smithingStartXP;
   smithingxphour = ((smithingxpGained)/(seconds + (minutes*60) + (hours*60*60))*60*60);

   //LEVELS GAINED
   if (smithingStartLevel == 0)
   {
    smithingStartLevel = skills.getCurrentLevel(Skills.SMITHING);
   }
   smithingLevelsGained = skills.getCurrentLevel(Skills.SMITHING) - smithingStartLevel;
   if (showSmithingXP)
   {
    smithxptolvl = skills.getExpToNextLevel(Skills.SMITHING);
    smithlevelsec =  smithxptolvl / (smithingxphour / 3600);
    smithlevelhour = smithlevelsec / (60*60);
    smithlevelsec -= smithlevelhour * (60*60);
    smithlevelmin = smithlevelsec /  60;
    smithlevelsec -= smithlevelmin * 60;
    g.drawString("Smithing XP/H: " + smithingxpGained + " / " + (int)smithingxphour, 200, 430);
    g.drawString("Smithing levels gained: " + smithingLevelsGained, 200, 445);
    g.drawString("TTL: "+smithlevelhour+": "+smithlevelmin+": "+smithlevelsec, 200, 460);
   }

  }
  g.setColor(Color.red);
  g.fillRect(tab1.x, tab1.y, tab1. width, tab1.height);
  g.setColor(Color.white);
  g.drawString("X", tab1.x+3, tab1.y+tab1.height-2);
 }

 public void mouseEntered(MouseEvent e){}
 public void mouseMoved(MouseEvent e)
 {
  p = e.getPoint();
  if(tab2.contains(p))
   showMagicXP=true;
  else
   showMagicXP=false;
  if(tab3.contains(p))
   showSmithingXP=true;
  else
   showSmithingXP=false;
 }
 public void mouseExited(MouseEvent arg0) {}
 public void mousePressed(MouseEvent e) 
 {
  p = e.getPoint();
  if(tab1.contains(p))
  {
   if (showPaint)
    showPaint=false;
   else
    showPaint=true;
  }
 }
 public void mouseReleased(MouseEvent arg0) {}
 public void mouseClicked(MouseEvent arg0) {}
 public void mouseDragged(MouseEvent arg0) {}
 
 public class myGUI extends JPanel 
 {
  private JFrame myGUI;
  private JLabel label1;
  private JButton start;
  private JTabbedPane tabbedPane1;
  private JPanel general;
  private JTextField toMake;
  private JLabel label2;
  private JLabel label3;
  private JComboBox setType;
  private JSlider mouseSpeed;
  private JLabel label4;
  private JCheckBox screenshot;
  private JLabel label6;
  private JTextField mouserange;
  private JPanel antiban;
  private JCheckBox useantiban;
  private JCheckBox statcheck;
  private JCheckBox offscreen;
  private JCheckBox movemouse;
  private JCheckBox rotatecamera;
  private JLabel label5;
  private JSlider antibanfrequency;
  private JCheckBox fkeys;
  public myGUI() 
  {
   initComponents();
  }
  private void startActionPerformed(ActionEvent e) 
  {
   tempSpeed = mouseSpeed.getValue();
   barType = (String)setType.getSelectedItem();
   totalBars = Integer.parseInt(toMake.getText());
   screenshotOnFinish = screenshot.isSelected();
   range = Integer.parseInt(mouserange.getText());
   useAntiban = useantiban.isSelected();
   statCheck = statcheck.isSelected();
   moveMouse = movemouse.isSelected();
   offScreen = offscreen.isSelected();
   rotateCamera = rotatecamera.isSelected();
   antibanFrequency = antibanfrequency.getValue();
   useFkeys = fkeys.isSelected();
   gui.setVisible(false);
   myGUI.dispose();
  }

  private void initComponents() {
   // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
   // Generated using JFormDesigner Evaluation license - aidden mazey
   myGUI = new JFrame();
   label1 = new JLabel();
   start = new JButton();
   tabbedPane1 = new JTabbedPane();
   general = new JPanel();
   toMake = new JTextField();
   label2 = new JLabel();
   label3 = new JLabel();
   setType = new JComboBox();
   mouseSpeed = new JSlider();
   label4 = new JLabel();
   screenshot = new JCheckBox();
   label6 = new JLabel();
   mouserange = new JTextField();
   antiban = new JPanel();
   useantiban = new JCheckBox();
   statcheck = new JCheckBox();
   offscreen = new JCheckBox();
   movemouse = new JCheckBox();
   rotatecamera = new JCheckBox();
   label5 = new JLabel();
   antibanfrequency = new JSlider();
   fkeys = new JCheckBox();
   //======== myGUI ========
   {
    myGUI.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    myGUI.setVisible(true);
    Container myGUIContentPane = myGUI.getContentPane();
    myGUIContentPane.setLayout(null);
    //---- label1 ----
    label1.setText("    Aidden's Super Heater");
    label1.setFont(new Font("Segoe Script", Font.BOLD, 22));
    label1.setForeground(new Color(153, 0, 255));
    myGUIContentPane.add(label1);
    label1.setBounds(0, 15, 345, 55);
    //---- start ----
    start.setText("Start");
    start.setFont(new Font("Tahoma", Font.BOLD, 18));
    start.addActionListener(new ActionListener() {
     @Override
     public void actionPerformed(ActionEvent e) {
      startActionPerformed(e);
     }
    });
    myGUIContentPane.add(start);
    start.setBounds(25, 335, 295, 45);
    //======== tabbedPane1 ========
    {
     //======== general ========
     {
      // JFormDesigner evaluation mark

      general.setLayout(null);
      general.add(toMake);
      toMake.setBounds(145, 5, 170, toMake.getPreferredSize().height);
      //---- label2 ----
      label2.setText("Number of bars to make:");
      general.add(label2);
      label2.setBounds(new Rectangle(new Point(10, 5), label2.getPreferredSize()));
      //---- label3 ----
      label3.setText("Bar type:");
      general.add(label3);
      label3.setBounds(10, 30, 50, 14);
      //---- setType ----
      setType.setModel(new DefaultComboBoxModel(new String[] {
        "Bronze",
        "Iron",
        "Silver",
        "Steel",
        "Gold",
        "Mithril",
        "Adamant",
        "Rune"
      }));
      general.add(setType);
      setType.setBounds(new Rectangle(new Point(60, 30), setType.getPreferredSize()));
      //---- mouseSpeed ----
      mouseSpeed.setMinimum(1);
      mouseSpeed.setMaximum(10);
      mouseSpeed.setValue(5);
      mouseSpeed.setMajorTickSpacing(1);
      mouseSpeed.setPaintLabels(true);
      mouseSpeed.setPaintTicks(true);
      mouseSpeed.setSnapToTicks(true);
      general.add(mouseSpeed);
      mouseSpeed.setBounds(new Rectangle(new Point(70, 185), mouseSpeed.getPreferredSize()));
      //---- label4 ----
      label4.setText("Mouse speed ( Lower = Faster )");
      general.add(label4);
      label4.setBounds(new Rectangle(new Point(90, 165), label4.getPreferredSize()));
      //---- screenshot ----
      screenshot.setText("Take screenshot when finished");
      screenshot.setSelected(true);
      general.add(screenshot);
      screenshot.setBounds(new Rectangle(new Point(145, 30), screenshot.getPreferredSize()));

      //---- label6 ----
      label6.setText("Mouse speed range:");
      general.add(label6);
      label6.setBounds(new Rectangle(new Point(10, 120), label6.getPreferredSize()));
      //---- mouserange ----
      mouserange.setText("1");
      general.add(mouserange);
      mouserange.setBounds(115, 120, 40, mouserange.getPreferredSize().height);
      /*---- fkeys ----
          fkeys.setText("F-keys ( Fast )");
          fkeys.setSelected(true);
          general.add(fkeys);
          fkeys.setBounds(new Rectangle(new Point(5, 90), fkeys.getPreferredSize()));*/
      { // compute preferred size
       Dimension preferredSize = new Dimension();
       for(int i = 0; i < general.getComponentCount(); i++) {
        Rectangle bounds = general.getComponent(i).getBounds();
        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
       }
       Insets insets = general.getInsets();
       preferredSize.width += insets.right;
       preferredSize.height += insets.bottom;
       general.setMinimumSize(preferredSize);
       general.setPreferredSize(preferredSize);
      }
     }
     tabbedPane1.addTab("General", general);

     //======== antiban ========
     {
      antiban.setLayout(null);
      //---- useantiban ----
      useantiban.setText("Use antiban");
      useantiban.setSelected(true);
      antiban.add(useantiban);
      useantiban.setBounds(new Rectangle(new Point(130, 10), useantiban.getPreferredSize()));
      //---- statcheck ----
      statcheck.setText("Stat check");
      statcheck.setSelected(true);
      antiban.add(statcheck);
      statcheck.setBounds(20, 55, 83, 23);
      //---- offscreen ----
      offscreen.setText("Mouse off screen");
      offscreen.setSelected(true);
      antiban.add(offscreen);
      offscreen.setBounds(20, 90, 120, 23);
      //---- movemouse ----
      movemouse.setText("Move mouse");
      movemouse.setSelected(true);
      antiban.add(movemouse);
      movemouse.setBounds(175, 55, 130, 23);
      //---- rotatecamera ----
      rotatecamera.setText("Rotate camera");
      rotatecamera.setSelected(true);
      antiban.add(rotatecamera);
      rotatecamera.setBounds(175, 90, 130, 23);
      //---- label5 ----
      label5.setText("Antiban frequency ( % )");
      antiban.add(label5);
      label5.setBounds(new Rectangle(new Point(115, 165), label5.getPreferredSize()));
      //---- antibanfrequency ----
      antibanfrequency.setValue(40);
      antibanfrequency.setMajorTickSpacing(10);
      antibanfrequency.setPaintLabels(true);
      antibanfrequency.setPaintTicks(true);
      antibanfrequency.setSnapToTicks(true);
      antibanfrequency.setMinimum(10);
      antiban.add(antibanfrequency);
      antibanfrequency.setBounds(70, 180, 200, 50);
      { // compute preferred size
       Dimension preferredSize = new Dimension();
       for(int i = 0; i < antiban.getComponentCount(); i++) {
        Rectangle bounds = antiban.getComponent(i).getBounds();
        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
       }
       Insets insets = antiban.getInsets();
       preferredSize.width += insets.right;
       preferredSize.height += insets.bottom;
       antiban.setMinimumSize(preferredSize);
       antiban.setPreferredSize(preferredSize);
      }
     }
     tabbedPane1.addTab("Antiban", antiban);
    }
    myGUIContentPane.add(tabbedPane1);
    tabbedPane1.setBounds(1, 60, 348, 265);
    { // compute preferred size
     Dimension preferredSize = new Dimension();
     for(int i = 0; i < myGUIContentPane.getComponentCount(); i++) {
      Rectangle bounds = myGUIContentPane.getComponent(i).getBounds();
      preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
      preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
     }
     Insets insets = myGUIContentPane.getInsets();
     preferredSize.width += insets.right;
     preferredSize.height += insets.bottom;
     myGUIContentPane.setMinimumSize(preferredSize);
     myGUIContentPane.setPreferredSize(preferredSize);
    }
    myGUI.pack();
    myGUI.setLocationRelativeTo(myGUI.getOwner());
   }
   // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }
 

 }
 public Step currentStep()
 {
  if(casts == totalBars)
  {
   logout();
  }
  if (bank.isOpen())
  {
   return Step.Bank;
  }
  if (Ready == true || Continue == true)
  {
   return Step.Superheat;
  }else
   if (Ready == false && Continue == false)
   {
    if(game.getCurrentTab() != Game.TAB_INVENTORY)
    {
     keyboard.pressKey((char) KeyEvent.VK_F1);
     sleep(random(50,200));
     keyboard.releaseKey((char) KeyEvent.VK_F1); 
    }
    if (inventory.containsOneOf(natureID) && inventory.containsOneOf(getType(1)) && inventory.containsOneOf(getType(2)))
    {
     if (inventory.getCount(getType(2))/ratio >= inventory.getCount(getType(1)))
     {
      return Step.Superheat;
     }
     else
     {
      return Step.Openbank;
     }
    }
    else
    {
     return Step.Openbank;
    } 
   }
  return Step.Unknown;
 }
}