package scripts;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.util.Filter;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSItem;

@SuppressWarnings("ALL")
@ScriptManifest(authors = "Radioactive, FA212", name = "Fish Cashed", version = 1.0, description = "Converts fish to cash at Musa Point fishing docks")
public class FishCashed extends Script implements PaintListener {

    private String[] fishName = {"Raw Lobster", "Lobster", "Raw Swordfish",
            "Swordfish", "Raw Tuna", "Tuna"};
    private int[] fishID = {359, 361, 379, 377, 371, 373, 360, 372, 378, 380,
            362, 374};
    private int[] shopKeepers = {11674, 11677};

    private int countMoney = inventory.getCount(true, 995);
    private int countGP;
    private int countK = countMoney / 1000;
    private int countM = countMoney / 10000;
    private int valueGP = 0;
    private int progressGP = (countGP / 999) * 95;
    private int progressK = (countK / 999) * 95;
    private int progressM = (countM / 999) * 95;

    private boolean shortBreak = false;
    private boolean longBreak = false;
    private boolean moveOffScreen = false;

    long startTime;
    Gui gui;

    @Override
    public boolean onStart() {
        gui = new Gui();
        gui.setVisible(true);
        while (gui.isVisible()) {
            sleep(50);
        }
        mouse.setSpeed(random(5, 10));
        startTime = System.currentTimeMillis();
        return true;
    }

    public int loop() {
        if (game.isLoggedIn()) {
            if (dockArea() && inventory.isFull()) {
                toStore();
            } else if (storeArea() && inventory.isFull()) {
                sellFish();
            } else if (storeArea() && !inventory.isFull()) {
                toDocks();
            }
            if (dockArea() && !inventory.isFull()) {
                pickupFish();
            }
        }
        return random(800, 1200);
    }

    private void pickupFish() {
        groundItems.getNearest(new Filter<RSGroundItem>() {
            public boolean accept(RSGroundItem i) {
                for (String name : fishName) {
                    if (name == null || !name.equals(fishName)) {
                        random(0, 5);
                        return true;
                    }
                }
                for (int id : fishID) {
                    for (int f = 0; f < fishID.length; f++) {
                        if (id == -1 || id != fishID[f]) {
                            return true;
                        }
                        return true;
                    }
                }
                return false;
            }
        }).doAction("Take");
    }

    private void trade() {
        RSNPC volnpc = npcs.getNearest(shopKeepers);
        if (volnpc != null) {
            if (volnpc.isOnScreen()) {
                volnpc.doAction("Trade");
                sleep(1000, 2000);
            } else {
                walking.walkTileMM(volnpc.getLocation(), random(0, 3),
                        random(0, 3));
                sleep(1000, 2000);
            }
        }
    }

    private void sell() {
        RSItem x = inventory.getItem(fishID);
        if (store.isOpen()) {
            x.doAction("Sell 50");
            sleep(250, 550);
        }
    }

    private void closeStore() {
        if (store.isOpen()) {
            if (interfaces.getComponent(620, 18).isValid()) {
                interfaces.getComponent(620, 18).doClick();
                sleep(200, 500);
                mouse.click(true);
            }
        }
    }

    private void sellFish() {
        trade();
        sell();
        closeStore();
    }

    RSTile[] StorefromDocks = {new RSTile(2905, 3147)};

    RSTile[] DocksfromStore = {new RSTile(2925, 3178)};

    private boolean toStore() {
        return walking.walkPathMM(StorefromDocks);
    }

    private boolean toDocks() {
        return walking.walkPathMM(DocksfromStore);
    }

    public boolean customRest(int stopEnergy) {
        int energy = walking.getEnergy();
        for (int d = 0; d < 5; d++) {
            interfaces.getComponent(walking.INTERFACE_RUN_ORB, 1).doAction("Rest");
            mouse.moveSlightly();
            sleep(random(400, 600));
            int anim = getMyPlayer().getAnimation();
            if (anim == 12108 || anim == 2033 || anim == 2716 || anim == 11786
                    || anim == 5713) {
                break;
            }
            if (d == 4) {
                return false;
            }
        }
        while (energy < stopEnergy) {
            sleep(random(250, 500));
            {
                return true;
            }

        }
        return true;
    }

    private void handleWalking(boolean toStore) {
        if (walking.getEnergy() <= 30) {
            customRest(random(80, 99));
        } else {
            if (!walking.isRunEnabled()) {
                walking.setRun(true);
                sleep(random(1000, 1250));
            }
        }
    }

    private boolean dockArea() {
        RSArea area = new RSArea(new RSTile(2923, 3174), new RSTile(2926, 3181));
        return (area.contains(getMyPlayer().getLocation()));
    }

    private boolean jungleArea() {
        RSArea area = new RSArea(new RSTile(2927, 3173), new RSTile(2899, 3154));
        return (area.contains(getMyPlayer().getLocation()));
    }

    private boolean storeArea() {
        RSArea area = new RSArea(new RSTile(2911, 3153), new RSTile(2900, 3143));
        return (area.contains(getMyPlayer().getLocation()));
    }

    public int antiban() {
        int antiban = random(0, 10);
        if (antiban == 1) {
            int angle = camera.getAngle() + random(-90, 90);
            if (angle < 0) {
                angle = random(0, 15);
            }
            if (angle > 359) {
                angle = random(0, 15);
            }
            camera.setAngle(angle);
            log("Moving camera");
        }
        if (antiban == 2) {
            camera.setCompass('n');
            sleep(500, 1000);
            log("Moving camera");
        }
        if (antiban == 3) {
            camera.setCompass('s');
            sleep(500, 1000);
            log("Moving camera");
        }
        if (antiban == 4) {
            mouse.move(random(50, 1000), 2, 2);
            sleep(500, 1000);
            log("Moving mouse");
        }
        if (antiban == 5) {
            mouse.moveSlightly();
            sleep(50, 100);
            log("Moving mouse");
        }
        if (antiban == 6) {
            mouse.setSpeed(random(5, 10));
            sleep(50, 100);
            log("Setting mouse speed");
        }
        if (antiban == 6) {
            if (shortBreak) {
                sleep(30000, 60000);
                log("Taking a shorter break");
            }
        }
        if (antiban == 7) {
            if (longBreak) {
                sleep(30000, 180000);
                log("Taking a longer break");
            }
        }
        if (antiban == 8) {
            if (moveOffScreen) {
                mouse.moveOffScreen();
                sleep(15000, 30000);
                log("Moving mouse off screen");
            }
        }
        if (antiban == 9) {
            if (moveOffScreen) {
                mouse.moveOffScreen();
                sleep(30000, 60000);
                log("Moving mouse off screen");
            }
        }
        return random(500, 1000);
    }

    final Color color1 = new Color(209, 209, 209);
    final Color color2 = new Color(0, 0, 0);
    final Color color3 = new Color(161, 142, 66);
    final Color color4 = new Color(150, 150, 150);
    final Color color5 = new Color(1, 0, 0);
    final Color color6 = new Color(0, 202, 255);
    final Color color7 = new Color(12, 247, 14);

    final BasicStroke stroke1 = new BasicStroke(1);

    final Font font1 = new Font("Arial", 0, 9);

    public void onRepaint(Graphics g) {
        String geepee = Integer.toString(countMoney);
        valueGP = Integer.parseInt(geepee.substring(
                geepee.length() - 3, geepee.length()));
        countGP = valueGP;

        if (game.isLoggedIn()) {
            return;
        }
        long millis = System.currentTimeMillis() - startTime;
        long hours = millis / (1000 * 60 * 60);
        millis -= hours * (1000 * 60 * 60);
        long minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        long seconds = millis / 1000;

        Graphics2D graphics = (Graphics2D) g;
        g.setColor(color1);
        g.fillRect(222, 314, 294, 24);
        g.setColor(color2);
        g.drawRect(222, 314, 294, 24);
        g.setColor(color1);
        g.fillRect(418, 278, 98, 36);
        g.setColor(color2);
        g.drawRect(418, 278, 98, 36);
        g.drawLine(418, 338, 418, 314);
        g.setColor(color3);
        g.fillRect(420, 316, progressM, 21);
        g.fillRect(322, 316, progressK, 21);
        g.fillRect(224, 316, progressGP, 21);
        g.setColor(color4);
        g.fillRect(420, 316, 95, 21);
        g.fillRect(322, 316, 95, 21);
        g.fillRect(224, 316, 95, 21);
        g.setFont(font1);
        g.setColor(color5);
        g.drawString("Profit: " + countMoney, 431, 306);
        g.setColor(color6);
        g.drawString("Profit: " + countMoney, 430, 305);
        g.setColor(color5);
        g.drawString("Time: " + hours + ":" + minutes + ":" + seconds, 431, 295);
        g.setColor(color6);
        g.drawString("Time: " + hours + ":" + minutes + ":" + seconds, 430, 294);
        g.setColor(color5);
        g.drawString("M: " + countM, 258, 331);
        g.setColor(color7);
        g.drawString("M: " + countM, 257, 330);
        g.setColor(color5);
        g.drawString("K: " + countK, 356, 331);
        g.setColor(color7);
        g.drawString("K: " + countK, 355, 330);
        g.setColor(color5);
        g.drawString("GP: " + countGP, 451, 331);
        g.setColor(color7);
        g.drawString("GP: " + countGP, 450, 330);
    }

    @SuppressWarnings("serial")
    class Gui extends JFrame {

        public Gui() {
            initComponents();
        }

        private JButton Start;
        private JButton Thread;
        private JLabel Antiban;
        private JCheckBox CheckBox1;
        private JCheckBox CheckBox2;
        private JCheckBox CheckBox3;
        private JLabel Author;

        private void initComponents() {
            Start = new JButton();
            Thread = new JButton();
            Antiban = new JLabel();
            CheckBox1 = new JCheckBox();
            CheckBox2 = new JCheckBox();
            CheckBox3 = new JCheckBox();
            Author = new JLabel();

            setTitle("Fish Cashed");
            setResizable(false);
            Container contentPane = getContentPane();
            contentPane.setLayout(null);

            Start.setText("Start");
            Start.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
            contentPane.add(Start);
            Start.setBounds(5, 5, 75, 25);
            Thread.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            Thread.setText("Thread");
            Thread.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
            contentPane.add(Thread);
            Thread.setBounds(85, 5, 75, 25);
            Thread.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        java.awt.Desktop
                                .getDesktop()
                                .browse(new URL(
                                        "http://www.powerbot.org/vb/showthread.php?t=123")
                                        .toURI());
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            Antiban.setText("Antiban:");
            contentPane.add(Antiban);
            Antiban.setBounds(new Rectangle(new Point(5, 35), Antiban
                    .getPreferredSize()));

            CheckBox1.setText("Take short breaks");
            CheckBox1.setSelected(true);
            CheckBox1.setBorderPaintedFlat(true);
            contentPane.add(CheckBox1);
            CheckBox1.setBounds(new Rectangle(new Point(5, 50), CheckBox1
                    .getPreferredSize()));
            CheckBox1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    shortBreak = true;
                }
            });

            CheckBox2.setText("Take long breaks");
            CheckBox2.setSelected(true);
            CheckBox2.setBorderPaintedFlat(true);
            contentPane.add(CheckBox2);
            CheckBox2.setBounds(5, 70, 110, 23);
            CheckBox2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    longBreak = true;
                }
            });

            CheckBox3.setText("Move mouse off screen");
            CheckBox3.setSelected(true);
            CheckBox3.setBorderPaintedFlat(true);
            contentPane.add(CheckBox3);
            CheckBox3.setBounds(5, 90, 145, 23);
            CheckBox3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveOffScreen = true;
                }
            });

            Author.setText("Fish Cashed by: Radioactive, FA212");
            contentPane.add(Author);
            Author.setBounds(new Rectangle(new Point(5, 115), Author
                    .getPreferredSize()));
            {
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < contentPane.getComponentCount(); i++) {
                    Rectangle bounds = contentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width,
                            preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height,
                            preferredSize.height);
                }
                Insets insets = contentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                contentPane.setMinimumSize(preferredSize);
                contentPane.setPreferredSize(preferredSize);
            }
            setSize(195, 160);
            setLocationRelativeTo(getOwner());
        }
    }
}

