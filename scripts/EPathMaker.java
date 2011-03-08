import org.rsbot.script.*;
import org.rsbot.script.wrappers.*;
import org.rsbot.event.listeners.PaintListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.*;
import javax.swing.AbstractListModel;
import javax.swing.JList;

@ScriptManifest(authors = { "Enfilade" }, keywords={"development"}, name = "Enfilade's Path Maker", version = 2.35)
public class EPathMaker extends Script implements PaintListener {

    private ArrayList<RSTile> path;
    private boolean quit;
    private PathFrame frame;

    public boolean onStart() {
        frame = new PathFrame();
        frame.setVisible(true);
        path = frame.list.getArrayList();
        return true;
    }

    public int loop() {
        if(quit)
            return -1;
        return 1000;
    }

    public void onFinish() {
        frame.dispose();
    }

    private Point tileToMinimap(RSTile t) {
        RSTile player = getMyPlayer().getLocation();
        Point a = calc.tileToMinimap(player);
        Point b = calc.tileToMinimap(new RSTile(player.getX(), player.getY() + 16));
        Point c = calc.tileToMinimap(new RSTile(player.getX() + 16, player.getY()));

        double bDistX = (b.x - a.x)/16.0;
        double bDistY = (b.y - a.y)/16.0;

        double cDistX = (c.x - a.x)/16.0;
        double cDistY = (c.y - a.y)/16.0;

        int xDist = t.getX() - player.getX();
        int yDist = t.getY() - player.getY();

        return new Point(a.x + (int)Math.round(cDistX*xDist + bDistX*yDist),
                a.y + (int)Math.round(bDistY*yDist + cDistY*xDist));
    }

    private final Color WHITE_TRANS = new Color(255, 255, 255, 150), GREEN_TRANS = new Color(0, 255, 0, 150);
    public void onRepaint(Graphics g1) {
        if(path == null)
            return;
        final Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Point before = null, p, player = calc.tileToMinimap(players.getMyPlayer().getLocation());
        RSTile selected = frame.getSelectedTile();
        for(RSTile t : path) {
            p = tileToMinimap(t);
            if(selected != null && t.equals(selected)) {
                g.setColor(WHITE_TRANS);
                g.drawLine(p.x, p.y, player.x, player.y);
                if(before != null)
                    g.drawLine(before.x, before.y, player.x, player.y);
                g.setColor(Color.WHITE);
            } else
                g.setColor(Color.ORANGE);
            g.drawLine(p.x - 3, p.y - 3, p.x + 3, p.y + 3);
            g.drawLine(p.x - 3, p.y + 3, p.x + 3, p.y - 3);
            if(before != null) {
                g.setColor(Color.BLUE);
                g.drawLine(p.x, p.y, before.x, before.y);
            }
            before = p;
        }
        if(path.size() > 0 && calc.tileOnMap(path.get(path.size() - 1))) {
            g.setColor(GREEN_TRANS);
            g.drawLine(player.x, player.y, before.x, before.y);
        }
    }

    public class PathFrame extends javax.swing.JFrame {

        public PathFrame() {
            initComponents();
            list = (RSTileListModel)tileList.getModel();
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            jScrollPane1 = new javax.swing.JScrollPane();
            tileList = new javax.swing.JList();
            addButton = new javax.swing.JButton();
            removeButton = new javax.swing.JButton();
            insertButton = new javax.swing.JButton();
            generateButton = new javax.swing.JButton();
            clearButton = new javax.swing.JButton();
            setButton = new javax.swing.JButton();
            importButton = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
            setTitle("Enfilade's Path Maker");
            setResizable(false);
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    formWindowClosing(evt);
                }
            });

            jLabel1.setText("Path Tiles:");

            tileList.setModel(new RSTileListModel(tileList));
            jScrollPane1.setViewportView(tileList);

            addButton.setText("Add Tile");
            addButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    addButtonActionPerformed(evt);
                }
            });

            removeButton.setText("Remove Selected");
            removeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeButtonActionPerformed(evt);
                }
            });

            insertButton.setText("Insert Before Selected");
            insertButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    insertButtonActionPerformed(evt);
                }
            });

            generateButton.setText("Generate Code");
            generateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    generateButtonActionPerformed(evt);
                }
            });

            clearButton.setText("Clear List");
            clearButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    clearButtonActionPerformed(evt);
                }
            });

            setButton.setText("Set Selected to Current Tile");
            setButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setButtonActionPerformed(evt);
                }
            });

            importButton.setText("Import Tiles");
            importButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    importButtonActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addGap(18, 18, 18)
                                    .addComponent(importButton))
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(generateButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(clearButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(removeButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(setButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(insertButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(importButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(insertButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(setButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(removeButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(clearButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                    .addComponent(generateButton)
                    .addContainerGap())
            );

            pack();
        }// </editor-fold>

        private RSTile getSelectedTile() {
            int index = tileList.getSelectedIndex();
            if(index >= 0 && index < list.getSize())
                return list.get(index);
            return null;
        }

        private void formWindowClosing(java.awt.event.WindowEvent evt) {
            quit = true;
            dispose();
        }

        private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {
            new ImportFrame(this, true, list).setVisible(true);
        }

        private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
            list.add(players.getMyPlayer().getLocation());
        }

        private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {
            int index = tileList.getSelectedIndex();
            if(index >= 0 && index < list.getSize())
                list.remove(index);
        }

        private void setButtonActionPerformed(java.awt.event.ActionEvent evt) {
            int index = tileList.getSelectedIndex();
            if(index >= 0 && index < list.getSize())
                list.set(index, players.getMyPlayer().getLocation());
        }

        private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {
            int index = tileList.getSelectedIndex();
            if(index >= 0 && index < list.getSize())
                list.add(index, players.getMyPlayer().getLocation());
        }

        private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
            list.clear();
        }

        private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {
            String code = "{";
            for(int i = 0; i < list.getSize(); i++) {
                code += " new RSTile(" + list.get(i).getX() + ", " + list.get(i).getY() + ")";
                if(i < list.getSize() - 1)
                    code += ",";
            }
            new CodeFrame(code + " }").setVisible(true);
        }

        private javax.swing.JButton addButton;
        private javax.swing.JButton clearButton;
        private javax.swing.JButton generateButton;
        private javax.swing.JButton insertButton;
        private javax.swing.JButton importButton;
        private javax.swing.JButton setButton;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JList tileList;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JButton removeButton;
        private RSTileListModel list;

    }

    public class RSTileListModel extends AbstractListModel {

        private ArrayList<RSTile> list;
        private JList parent;

        public RSTileListModel(JList parent) {
            super();
            list = new ArrayList<RSTile>();
            this.parent = parent;
        }

        public ArrayList<RSTile> getArrayList() { return list; }

        public void add(RSTile t) {
            list.add(t);
            parent.updateUI();
        }

        public void add(int index, RSTile t) {
            list.add(index, t);
            parent.updateUI();
        }

        public void set(int index, RSTile t) {
            list.set(index, t);
            parent.updateUI();
        }

        public RSTile remove(int index) {
            RSTile removed = list.remove(index);
            parent.updateUI();
            return removed;
        }

        public void clear() {
            list.clear();
            parent.updateUI();
        }

        public int getSize() {
            return list.size();
        }

        /* Do not use, use get instead! */
        public Object getElementAt(int index) {
            return list.get(index).getX() + ", " + list.get(index).getY();
        }

        public RSTile get(int index) {
            return list.get(index);
        }

    }

    public class CodeFrame extends javax.swing.JFrame {

            public CodeFrame(String code) {
                initComponents();
                codeBox.setText(code);
            }

            @SuppressWarnings("unchecked")
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                codeBox = new javax.swing.JTextArea();
                jLabel1 = new javax.swing.JLabel();
                copyButton = new javax.swing.JButton();

                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                setTitle("Generated Code");

                codeBox.setColumns(20);
                codeBox.setRows(5);
                jScrollPane1.setViewportView(codeBox);

                jLabel1.setText("Generated Code:");

                copyButton.setText("Copy to Clipboard");
                copyButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        copyButtonActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .addComponent(jLabel1)
                            .addComponent(copyButton))
                        .addContainerGap())
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copyButton)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                pack();
            }// </editor-fold>

            private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {
                codeBox.selectAll();
                codeBox.copy();
            }

            private javax.swing.JTextArea codeBox;
            private javax.swing.JButton copyButton;
            private javax.swing.JLabel jLabel1;
            private javax.swing.JScrollPane jScrollPane1;

        }

    public class ImportFrame extends javax.swing.JDialog {

        private final Pattern pattern = Pattern.compile("new\\s+RSTile\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
        private RSTileListModel list;
        private PathFrame parent;
        public ImportFrame(PathFrame parent, boolean modal, RSTileListModel list) {
            super(parent, modal);
            this.parent = parent;
            this.list = list;
            initComponents();
        }
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jScrollPane1 = new javax.swing.JScrollPane();
            codeBox = new javax.swing.JTextArea();
            jLabel1 = new javax.swing.JLabel();
            importButton = new javax.swing.JButton();
            cancelButton = new javax.swing.JButton();
            pasteButton = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            codeBox.setColumns(20);
            codeBox.setRows(5);
            jScrollPane1.setViewportView(codeBox);

            jLabel1.setText("Paste an already created definition of RSTiles in the box to add them to the list.");

            importButton.setText("Import!");
            importButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    importButtonActionPerformed(evt);
                }
            });

            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed(evt);
                }
            });

            pasteButton.setText("Paste");
            pasteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pasteButtonActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(importButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pasteButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 183, Short.MAX_VALUE)
                            .addComponent(cancelButton)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(importButton)
                        .addComponent(cancelButton)
                        .addComponent(pasteButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>

        private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {
            Matcher matcher = pattern.matcher(codeBox.getText());
            while(matcher.find())
                list.add(new RSTile(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
            dispose();
        }

        private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
            dispose();
        }

        private void pasteButtonActionPerformed(java.awt.event.ActionEvent evt) {
            codeBox.paste();
        }

        // Variables declaration - do not modify
        private javax.swing.JButton cancelButton;
        private javax.swing.JTextArea codeBox;
        private javax.swing.JButton importButton;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JButton pasteButton;
        // End of variables declaration

    }


}
