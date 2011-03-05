package scripts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.GEItemInfo;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = { "Taha" }, keywords = { "Smart", "Smelt", "Smelter",
		"Taha" }, name = "Smart Smelter", description = "Smelts all bars at most locations.", version = 3.01)
public class SmartSmelter extends Script implements PaintListener,
		ServerMessageListener {

	private boolean start, runScript, shutDown;
	private String bar, state = "Loading";
	private RSTile[] toBank, toFurnace;
	private int primaryOreID, secondaryOreID,
			numberOfPrimaryOresNeededForSmelting,
			numberOfSecondaryOresNeededForSmelting,
			numberOfPrimaryOresNeededForWithdrawal, numberToSmelt,
			furnaceObjectID, primaryChild, copperOreID = 436, tinOreID = 438,
			ironOreID = 440, silverOreID = 442, coalOreID = 453,
			goldOreID = 444, mithrilOreID = 447, adamantOreID = 449,
			runeOreID = 451, oreSelectionInterface = 916, startLvl, startExp,
			barsSmelted, nextStep = 18;
	private GEItemInfo profitPerBar;
	private long startTime;

	protected int getMouseSpeed() {
		return random(6, 11);
	}

	private enum State {
		BANK, SMELT, WALK_TO_BANK, WALK_TO_FURNACE, TOGGLE_RUN, WAIT, STOP, OPEN_DOOR
	}

	private State getState() {
		if (barsSmelted >= numberToSmelt) {
			return State.STOP;
		} else if ((walking.getEnergy() > 40 && random(0, 5) == 0 || walking
				.getEnergy() > 80)
				&& !bank.isOpen()
				&& !walking.isRunEnabled()
				&& !interfaces.get(oreSelectionInterface).isValid()) {
			return State.TOGGLE_RUN;
		} else if (objects.getNearest(5244) != null
				&& calc.tileOnScreen(objects.getNearest(5244).getLocation())) {
			return State.OPEN_DOOR;
		} else if (inventory.getCount(primaryOreID) >= numberOfPrimaryOresNeededForSmelting
				&& inventory.getCount(secondaryOreID) >= numberOfSecondaryOresNeededForSmelting
				&& calc.distanceTo(toBank[0]) > 3) {
			return State.WALK_TO_FURNACE;
		} else if (calc.distanceTo(toFurnace[0]) > 5
				&& (inventory.getCount(primaryOreID) < numberOfPrimaryOresNeededForSmelting || inventory
						.getCount(secondaryOreID) < numberOfSecondaryOresNeededForSmelting)) {
			return State.WALK_TO_BANK;
		} else if (inventory.getCount(primaryOreID) < numberOfPrimaryOresNeededForSmelting
				|| inventory.getCount(secondaryOreID) < numberOfSecondaryOresNeededForSmelting) {
			return State.BANK;
		} else if (!isSmelting()) {
			return State.SMELT;
		} else {
			return State.WAIT;
		}
	}

	public boolean onStart() {
		new SmartSmelterGUI().setVisible(true);
		while (!start) {
			sleep(100);
		}
		startTime = System.currentTimeMillis();
		return runScript;
	}

	public int loop() {
		try {
			if (game.isLoggedIn()) {
				camera.setPitch(true);
				switch (getState()) {
				case TOGGLE_RUN:
					state = "Enabling Run Mode";
					walking.setRun(true);
					break;

				case BANK:
					state = "Banking";
					if (!bank.isOpen()) {
						bank.open();
						sleep(random(500, 1000));
					} else {
						if (inventory.getCountExcept(primaryOreID,
								secondaryOreID) > 0) {
							bank.depositAll();
							sleep(random(200, 400));
						}
						if (inventory.getCount(primaryOreID) > numberOfPrimaryOresNeededForWithdrawal) {
							bank.deposit(
									primaryOreID,
									inventory.getCount(primaryOreID)
											- numberOfPrimaryOresNeededForWithdrawal);
						}
						if (inventory.getCount(secondaryOreID) > (28 - numberOfPrimaryOresNeededForWithdrawal)) {
							bank.deposit(
									secondaryOreID,
									inventory.getCount(secondaryOreID)
											- (28 - numberOfPrimaryOresNeededForWithdrawal));
						}
						if (!bankContainsEnoughOres()) {
							log("Not enough ores!");
							bank.close();
							stopScript();
							return -1;
						}
						if (inventory.getCount(primaryOreID) < oresToWithdraw()
								&& bankContainsEnoughOres()) {
							bank.withdraw(primaryOreID, oresToWithdraw()
									- inventory.getCount(primaryOreID));
						}
						if (!bar.equals("Iron") && !bar.equals("Silver")
								&& !bar.equals("Gold")
								&& inventory.getCount(primaryOreID) > 0) {
							bank.withdraw(secondaryOreID, 0);
						}
						sleep(random(200, 400));
					}
					break;

				case SMELT:
					state = "Smelting";
					RSObject furnace = objects.getNearest(furnaceObjectID);
					if (furnace != null) {
						if (!calc.tileOnScreen(furnace.getLocation())) {
							int t = 0;
							while (t < 6) {
								camera.turnToTile(furnace.getLocation());
								if (calc.tileOnScreen(furnace.getLocation())) {
									break;
								} else {
									t++;
								}
							}
							if (t > 5) {
								while (step(toFurnace) != toFurnace.length) {
									sleep(200, 400);
								}
							}
						}
						if (!interfaces.get(oreSelectionInterface).isValid()) {
							furnace.doAction("Smelt");
							sleep(random(100, 150));
						}
						if (interfaces.get(oreSelectionInterface).isValid()) {
							while (!interfaces
									.getComponent(oreSelectionInterface, 17)
									.getText().equals("All")) {
								interfaces.getComponent(oreSelectionInterface,
										19).doAction("+1");
								sleep(random(50, 100));
							}
							interfaces.getComponent(905, primaryChild)
									.doAction("Make all");
						}
					}
					break;

				case WALK_TO_BANK:
					state = "Walking to Bank";
					while (step(toBank) != toBank.length) {
						sleep(200, 400);
					}
					break;

				case WALK_TO_FURNACE:
					state = "Walking to Furnace";
					while (step(toFurnace) != toFurnace.length) {
						sleep(200, 400);
					}
					break;

				case OPEN_DOOR:
					state = "Opening Door";
					objects.getNearest(5244).doAction("Open");
					return random(1200, 1400);

				case WAIT:
					state = "Waiting";
					break;

				case STOP:
					stopScript();
					return -1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return random(300, 400);
	}

	public void onFinish() {
		log("Gained: " + (skills.getCurrentLevel(Skills.SMITHING) - startLvl)
				+ " Smithing Levels");
		log.info("Profit: " + barsSmelted * profitPerBar.getMarketPrice()
				+ " GP");
		if (shutDown) {
			try {
				Runtime.getRuntime()
						.exec("shutdown -s -t 120 -c \"Smart Smelter automatic shutdown has initiliazed. Please wait 120 seconds...\"");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private int oresToWithdraw() {
		return numberToSmelt - barsSmelted > numberOfPrimaryOresNeededForWithdrawal ? numberOfPrimaryOresNeededForWithdrawal
				: numberToSmelt - barsSmelted;
	}

	private boolean bankContainsEnoughOres() {
		if (bank.isOpen()
				&& bank.getCount(primaryOreID)
						+ inventory.getCount(primaryOreID) >= numberOfPrimaryOresNeededForSmelting
				&& bank.getCount(secondaryOreID)
						+ inventory.getCount(secondaryOreID) >= numberOfSecondaryOresNeededForSmelting) {
			return true;
		}
		return false;
	}

	private boolean isSmelting() {
		for (int i = 0; i < 8; i++) {
			if (inventory.getCount(primaryOreID) < numberOfPrimaryOresNeededForSmelting
					|| inventory.getCount(secondaryOreID) < numberOfSecondaryOresNeededForSmelting) {
				return false;
			} else if (getMyPlayer().getAnimation() == 3243) {
				return true;
			}
			sleep(250);
		}
		return false;
	}

	public void serverMessageRecieved(ServerMessageEvent e) {
		if (e.getMessage().contains("You retrieve a bar of")) {
			barsSmelted++;
		}
		if (e.getMessage().contains("magic of the Varrock armour")) {
			barsSmelted++;
		}
		if (e.getMessage().contains("members' server to use this furnace")) {
			log("Use a F2P furnace!");
			stopScript();
		}
	}

	public void onRepaint(Graphics g) {
		if (game.isLoggedIn()) {
			long runTime = System.currentTimeMillis() - startTime;
			int seconds = (int) (runTime / 1000 % 60);
			int minutes = (int) (runTime / 1000 / 60) % 60;
			int hours = (int) (runTime / 1000 / 60 / 60) % 60;

			StringBuilder b = new StringBuilder();
			if (hours < 10) {
				b.append('0');
			}
			b.append(hours);
			b.append(':');
			if (minutes < 10) {
				b.append('0');
			}
			b.append(minutes);
			b.append(':');
			if (seconds < 10) {
				b.append('0');
			}
			b.append(seconds);

			if (startLvl <= 0 || startExp <= 0) {
				startLvl = skills.getCurrentLevel(Skills.SMITHING);
				startExp = skills.getCurrentExp(Skills.SMITHING);
			}

			int x = 294;
			int y = 4;
			int xl = 222;
			int yl = 85;

			g.setColor(new Color(0, 0, 0, 120));
			g.fillRect(x, y, xl, yl);
			g.setColor(new Color(248, 237, 22));
			g.drawRect(x, y, xl, yl);

			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
			g.drawString(getClass().getAnnotation(ScriptManifest.class).name()
					+ " v"
					+ getClass().getAnnotation(ScriptManifest.class).version(),
					x + 10, y += 15);
			g.drawString(
					"Gained: "
							+ (skills.getCurrentExp(Skills.SMITHING) - startExp < 1000 ? skills
									.getCurrentExp(Skills.SMITHING) - startExp
									: Math.round((skills
											.getCurrentExp(Skills.SMITHING) - startExp) * 10) / 10)
							+ " Exp"
							+ " || Exp/Hour: "
							+ (int) ((skills.getCurrentExp(Skills.SMITHING) - startExp) * 3600000D / ((double) System
									.currentTimeMillis() - (double) startTime)),
					x + 10, y += 15);
			g.drawString("Smelted: " + barsSmelted + " " + bar + " Bars",
					x + 10, y += 15);
			g.drawString("Time Running: " + b, x + 10, y += 15);
			g.drawString("Current State: ", x + 10, y += 15);
			g.setColor(Color.RED);
			g.drawString(state, x + 95, y);
		}
	}

	private boolean tileInNextRange(RSTile t) {
		return calc.distanceBetween(t, getMyPlayer().getLocation()) < nextStep;
	}

	private int step(RSTile[] path) {
		if (calc.distanceTo(path[path.length - 1]) < 3
				|| (walking.getDestination() != null && calc.distanceBetween(
						walking.getDestination(), path[path.length - 1]) < 3)) {
			return path.length;
		}
		RSTile dest = walking.getDestination();
		int index = -1;
		int shortestDist = 0, dist, shortest = -1;
		if (dest != null)
			for (int i = 0; i < path.length; i++) {
				dist = (int) calc.distanceBetween(path[i], dest);
				if (shortest < 0 || shortestDist > dist) {
					shortest = i;
					shortestDist = dist;
				}
			}
		for (int i = path.length - 1; i >= 0; i--)
			if (tileInNextRange(path[i])) {
				index = i;
				break;
			}
		if (index != -1
				&& (dest == null || (index > shortest) || !getMyPlayer()
						.isMoving())) {
			walking.walkTileMM(path[index]);
			nextStep = random(16, 19);
			return index;
		}
		return -1;
	}

	private class SmartSmelterGUI extends JFrame {
		private File settingsFile = new File(new File(
				GlobalConfiguration.Paths.getSettingsDirectory()),
				"SmartSmelterSettings.txt");

		private static final long serialVersionUID = 1L;

		// GEN-BEGIN:variables
		private JLabel label1;
		private JPanel buttonPanel;
		private JButton startButton;
		private JButton exitButton;
		private JPanel panel1;
		private JLabel label14;
		private JComboBox locComboBox;
		private JLabel label3;
		private JComboBox barComboBox;
		private JLabel label4;
		private JTextField numberTextField;
		private JCheckBox shutDownCheckBox;
		private JEditorPane description;

		// GEN-END:variables
		private SmartSmelterGUI() {
			initComponents();
			description.setEditable(false);
		}

		private void barComboBoxActionPerformed(final ActionEvent e) {
			updateDescription();
		}

		private void exitButtonActionPerformed(final ActionEvent e) {
			if (exitButton.getText().equals("Exit!")) {
				dispose();
				runScript = false;
				start = true;
			} else {
				stopScript();
				dispose();
			}
		}

		private void initComponents() {
			// GEN-BEGIN:initComponents
			label1 = new JLabel();
			buttonPanel = new JPanel();
			startButton = new JButton();
			exitButton = new JButton();
			panel1 = new JPanel();
			label14 = new JLabel();
			locComboBox = new JComboBox();
			label3 = new JLabel();
			barComboBox = new JComboBox();
			label4 = new JLabel();
			numberTextField = new JTextField();
			shutDownCheckBox = new JCheckBox();
			description = new JEditorPane();

			// ======== this ========
			setTitle("Smart Smelter Script Options");
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			final Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout(0, 5));
			addWindowListener(new WindowAdapter() {
				public void windowClosing(final WindowEvent ev) {
					if (exitButton.getText().equals("Exit!")) {
						dispose();
						start = true;
					} else {
						setVisible(false);
					}
				}
			});

			// ---- label1 ----
			label1.setText("Smart Smelter Script Options");
			label1.setFont(new Font("Century Gothic", Font.PLAIN, 22));
			label1.setHorizontalAlignment(SwingConstants.CENTER);
			contentPane.add(label1, BorderLayout.NORTH);

			// ======== buttonPanel ========
			{
				buttonPanel.setLayout(new GridBagLayout());
				((GridBagLayout) buttonPanel.getLayout()).columnWidths = new int[] {
						90, 85 };
				((GridBagLayout) buttonPanel.getLayout()).rowHeights = new int[] {
						3, 0 };
				((GridBagLayout) buttonPanel.getLayout()).rowWeights = new double[] {
						1.0, 1.0E-4 };

				// ---- startButton ----
				startButton.setText("Start!");
				startButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
				startButton.setFocusable(false);
				startButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						startButtonActionPerformed(e);
					}
				});
				buttonPanel.add(startButton, new GridBagConstraints(0, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- exitButton ----
				exitButton.setText("Exit!");
				exitButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
				exitButton.setFocusable(false);
				exitButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						exitButtonActionPerformed(e);
					}
				});
				buttonPanel.add(exitButton, new GridBagConstraints(1, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			contentPane.add(buttonPanel, BorderLayout.SOUTH);

			// ======== panel1 ========
			{
				panel1.setFocusable(false);
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout) panel1.getLayout()).columnWidths = new int[] {
						47, 139, 35, 33, 0 };
				((GridBagLayout) panel1.getLayout()).rowHeights = new int[] {
						18, 10, 18, 16, 34, 0, 0 };
				((GridBagLayout) panel1.getLayout()).columnWeights = new double[] {
						0.0, 0.0, 0.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel1.getLayout()).rowWeights = new double[] {
						0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4 };

				// ---- label14 ----
				label14.setText("Location:");
				label14.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
				label14.setHorizontalAlignment(SwingConstants.RIGHT);
				panel1.add(label14, new GridBagConstraints(0, 0, 2, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- locComboBox ----
				locComboBox.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
				locComboBox.setFocusable(false);
				locComboBox.setModel(new DefaultComboBoxModel(new String[] {
						"Falador", "Al Kharid" }));
				panel1.add(locComboBox, new GridBagConstraints(3, 0, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- label3 ----
				label3.setHorizontalAlignment(SwingConstants.RIGHT);
				label3.setText("Bar:");
				label3.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
				panel1.add(label3, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

				// ---- barComboBox ----
				barComboBox.setFocusable(false);
				barComboBox.setModel(new DefaultComboBoxModel(new String[] {
						"Bronze", "Iron", "Silver", "Steel", "Gold", "Mithril",
						"Adamant", "Rune" }));
				barComboBox.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
				barComboBox.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						barComboBoxActionPerformed(e);
					}
				});
				panel1.add(barComboBox, new GridBagConstraints(3, 1, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- label4 ----
				label4.setText("Numbers of bars to smelt:");
				label4.setHorizontalAlignment(SwingConstants.RIGHT);
				label4.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
				panel1.add(label4, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

				// ---- numberTextField ----
				numberTextField.setFont(new Font("Comic Sans MS", Font.PLAIN,
						12));
				numberTextField.setText("100");
				numberTextField.addKeyListener(new KeyAdapter() {
					public void keyTyped(final KeyEvent e) {
						numberTextFieldKeyTyped(e);
					}

					public void keyReleased(final KeyEvent e) {
						numberTextFieldKeyReleased(e);
					}
				});
				panel1.add(numberTextField, new GridBagConstraints(3, 2, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- shutDownCheckBox ----
				shutDownCheckBox.setText("Turn off the computer when finished");
				shutDownCheckBox.setFont(new Font("Comic Sans MS", Font.PLAIN,
						12));
				shutDownCheckBox.setFocusable(false);
				shutDownCheckBox.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						shutDownCheckBoxActionPerformed(e);
					}
				});
				panel1.add(shutDownCheckBox, new GridBagConstraints(1, 4, 3, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- description ----
				description.setBorder(null);
				description.setOpaque(false);
				description.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
				panel1.add(description, new GridBagConstraints(0, 5, 4, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}

			contentPane.add(panel1, BorderLayout.CENTER);
			setSize(400, 300);
			setLocationRelativeTo(getOwner());
			// FINAL TOUCHES
			try {
				settingsFile.createNewFile();
				BufferedReader in;
				in = new BufferedReader(new FileReader(settingsFile));
				String line;
				String[] opts = {};
				while ((line = in.readLine()) != null) {
					if (line.contains(":")) {
						opts = line.split(":");
					}
				}
				in.close();
				if (opts.length == 4) {
					locComboBox.setSelectedItem(opts[0]);
					barComboBox.setSelectedItem(opts[1]);
					numberTextField.setText(opts[2]);
					if (opts[3].equals("true")) {
						shutDownCheckBox.setSelected(true);
					}
				}
			} catch (final IOException ignored) {
			}
			updateDescription();
			// GEN-END:initComponents
		}

		private void numberTextFieldKeyTyped(final KeyEvent e) {
			if (e.getKeyChar() != '0' && e.getKeyChar() != '1'
					&& e.getKeyChar() != '2' && e.getKeyChar() != '3'
					&& e.getKeyChar() != '4' && e.getKeyChar() != '5'
					&& e.getKeyChar() != '6' && e.getKeyChar() != '7'
					&& e.getKeyChar() != '8' && e.getKeyChar() != '9'
					|| numberTextField.getText().length() >= 6) {
				e.consume();
			}
		}

		private void numberTextFieldKeyReleased(final KeyEvent e) {
			updateDescription();
		}

		private void shutDownCheckBoxActionPerformed(final ActionEvent e) {
			updateDescription();
		}

		private void startButtonActionPerformed(final ActionEvent e) {
			setVisible(false);
			bar = barComboBox.getSelectedItem().toString();
			numberToSmelt = Integer.parseInt(numberTextField.getText());
			shutDown = shutDownCheckBox.isSelected();
			if (locComboBox.getSelectedItem().toString().equals("Falador")) {
				toFurnace = new RSTile[] { new RSTile(2945, 3368),
						new RSTile(2945, 3371), new RSTile(2946, 3375),
						new RSTile(2949, 3376), new RSTile(2952, 3378),
						new RSTile(2954, 3379), new RSTile(2957, 3379),
						new RSTile(2960, 3379), new RSTile(2962, 3379),
						new RSTile(2965, 3379), new RSTile(2968, 3379),
						new RSTile(2970, 3378), new RSTile(2971, 3376),
						new RSTile(2972, 3374), new RSTile(2973, 3372),
						new RSTile(2973, 3370) };
				toBank = walking.reversePath(toFurnace);
				furnaceObjectID = 11666;
			}
			if (locComboBox.getSelectedItem().toString().equals("Al Kharid")) {
				toFurnace = new RSTile[] { new RSTile(3269, 3167),
						new RSTile(3271, 3167), new RSTile(3273, 3167),
						new RSTile(3275, 3168), new RSTile(3276, 3170),
						new RSTile(3276, 3173), new RSTile(3277, 3175),
						new RSTile(3278, 3178), new RSTile(3279, 3181),
						new RSTile(3281, 3183), new RSTile(3280, 3185),
						new RSTile(3276, 3186) };
				toBank = walking.reversePath(toFurnace);
				furnaceObjectID = 11666;
			}
			if (bar.equals("Bronze")) {
				primaryOreID = copperOreID;
				secondaryOreID = tinOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfSecondaryOresNeededForSmelting = 1;
				numberOfPrimaryOresNeededForWithdrawal = 14;
				primaryChild = 14;
				profitPerBar = grandExchange.loadItemInfo(2349);
			}
			if (bar.equals("Iron")) {
				primaryOreID = ironOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfPrimaryOresNeededForWithdrawal = 28;
				primaryChild = 16;
				profitPerBar = grandExchange.loadItemInfo(2351);
			}
			if (bar.equals("Silver")) {
				primaryOreID = silverOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfPrimaryOresNeededForWithdrawal = 28;
				primaryChild = 17;
				profitPerBar = grandExchange.loadItemInfo(2355);
			}
			if (bar.equals("Steel")) {
				primaryOreID = ironOreID;
				secondaryOreID = coalOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfSecondaryOresNeededForSmelting = 2;
				numberOfPrimaryOresNeededForWithdrawal = 10;
				primaryChild = 18;
				profitPerBar = grandExchange.loadItemInfo(2353);
			}
			if (bar.equals("Gold")) {
				primaryOreID = goldOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfPrimaryOresNeededForWithdrawal = 28;
				primaryChild = 19;
				profitPerBar = grandExchange.loadItemInfo(2357);
			}
			if (bar.equals("Mithril")) {
				primaryOreID = mithrilOreID;
				secondaryOreID = coalOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfSecondaryOresNeededForSmelting = 4;
				numberOfPrimaryOresNeededForWithdrawal = 5;
				primaryChild = 20;
				profitPerBar = grandExchange.loadItemInfo(2359);
			}
			if (bar.equals("Adamant")) {
				primaryOreID = adamantOreID;
				secondaryOreID = coalOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfSecondaryOresNeededForSmelting = 6;
				numberOfPrimaryOresNeededForWithdrawal = 4;
				primaryChild = 21;
				profitPerBar = grandExchange.loadItemInfo(2361);
			}
			if (bar.equals("Rune")) {
				primaryOreID = runeOreID;
				secondaryOreID = coalOreID;
				numberOfPrimaryOresNeededForSmelting = 1;
				numberOfSecondaryOresNeededForSmelting = 8;
				numberOfPrimaryOresNeededForWithdrawal = 3;
				primaryChild = 22;
				profitPerBar = grandExchange.loadItemInfo(2363);
			}
			try {
				final BufferedWriter out = new BufferedWriter(new FileWriter(
						settingsFile));
				out.write(locComboBox.getSelectedItem().toString() + ":"
						+ barComboBox.getSelectedItem().toString() + ":"
						+ Integer.parseInt(numberTextField.getText()) + ":"
						+ (shutDownCheckBox.isSelected() ? "true" : "false"));
				out.close();
			} catch (final Exception z) {
				z.printStackTrace();
			}

			start = true;
			runScript = true;
		}

		private void updateDescription() {
			final String loc = locComboBox.getSelectedItem().toString();
			if (shutDownCheckBox.isSelected()) {
				description
						.setText("You will be smelting "
								+ numberTextField.getText()
								+ " "
								+ barComboBox.getSelectedItem().toString()
										.toLowerCase()
								+ " bars at "
								+ loc
								+ ". Your computer will shutdown itself when the task is finished.");
			} else {
				description.setText("You will be smelting "
						+ numberTextField.getText()
						+ " "
						+ barComboBox.getSelectedItem().toString()
								.toLowerCase() + " bars at " + loc + ".");
			}
		}
	}

}

