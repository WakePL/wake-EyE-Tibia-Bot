
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import com.melloware.jintellitype.*;

public class GUI extends javax.swing.JFrame implements HotkeyListener {

    //--------------------------------------------------------------------------Visual Elements

    //--------------------------------------------------------------------------Registration window stuff
    //--------------------------------------------------------------------------Loading/Saving of settings.
    JFileChooser fileChooser = new JFileChooser();
    BufferedReader reader;
    BufferedWriter writer;
    File fileDir = new File(System.getProperty("user.home") + "\\Documents\\");
    File file = new File(fileDir, "EyeBotSet.txt");
    //--------------------------------------------------------------------------Mouse and Keyboard Manipulation and Data
    Robot robot;
    PointerInfo pointer = MouseInfo.getPointerInfo();
    Point coord = pointer.getLocation();
    //--------------------------------------------------------------------------Color comparions
    Color hpHighCurColor;
    Color hpHighOldColor;
    Color mpRestoreCurColor;
    Color mpRestoreOldColor;
    Color hpLowCurColor;
    Color hpLowOldColor;
    Color mpCriticalCurColor;
    Color mpCriticalOldColor;
    Color monsterCorpseCurColor;
    Color monsterCorpseOldColor;
    Color sq1;
    Color sq1Old;
    Color sq2;
    Color sq2Old;
    Color sq3;
    Color sq3Old;
    //--------------------------------------------------------------------------Size Objects
    final static double pixelPercent = 1.77;
    //--------------------------------------------------------------------------Locations
    int hpLeftX;
    int hpLeftY;
    int mpLeftX;
    int mpLeftY;
    int hPotionX;
    int hPotionY;
    int mPotionX;
    int mPotionY;
    int highHealPercent;
    int lowHealPercent;
    int manaRestorePercent;
    int criticalManaPercent;
    int ammunitionBPX;
    int ammunitionBPY;
    int arrowSpotX;
    int arrowSpotY;
    int lootBPX;
    int lootBPY;
    int lootAreaX;
    int lootAreaY;
    int spellCost;
    //--------------------------------------------------------------------------Timing devices
    long lastHealTime;
    long lastPotTime;
    long curTime;
    long lastReload;
    //--------------------------------------------------------------------------Safeties
    boolean canPot = true;
    boolean canCast = true;
    boolean isPaused = false;
    //--------------------------------------------------------------------------Timers for automation.
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            pointer = MouseInfo.getPointerInfo();
            coord = pointer.getLocation();
            wCoordBox.setText("X:" + coord.x + ", Y:" + coord.y);
            hCoordBox.setText("X:" + coord.x + ", Y:" + coord.y);
        }
    };
    Timer healTimer = new Timer();
    TimerTask healTask = new TimerTask() {
        @Override
        public void run() {

            //------------------------------------------------------------------Check to see if we can cast spells again.
            curTime = System.currentTimeMillis();
            if ((curTime - lastHealTime) >= 1000) {
                canCast = true;
            }
            //------------------------------------------------------------------Check to see if we can use potions again.
            if ((curTime - lastPotTime) >= 1000) {
                canPot = true;
            }

            //------------------------------------------------------------------Check to see if we need health.
            if (!isPaused && needHP()) {
                heal();
            }

            //------------------------------------------------------------------Check to see if we need mana.
            if (!isPaused && needMana()) {
                mana();
            }

            //------------------------------------------------------------------Check to see if there is anything to loot.
            if (!isPaused && canLoot() && lootCheck.isSelected()) {
                loot();
            }

            //------------------------------------------------------------------Check to see if we can to reload.
            if (!isPaused && reloadCheck.isSelected()) {
                if (curTime - lastReload >= 60000) {
                    reload();
                }
            }

            //------------------------------------------------------------------Check to see if we can spelltrain
            if (!isPaused && spellTrainerCheck.isSelected() && canCast) {
                if (curTime - lastHealTime >= spellCost * 1000) {
                    spellTrain();
                }
            }
        }

        //----------------------------------------------------------------------Checks to see if user enabled healing, and if so, heals the user.
        private void heal() {
            int oldDelay = robot.getAutoDelay();
            robot.setAutoDelay(0);
            //------------------------------------------------------------------If we need to heal low, do it first.
            if (needLowHeal() && hpLowCheck.isSelected()) {
                if (canCast && useSpells.isSelected()) {
                    robot.keyPress(KeyEvent.VK_F12);
                    robot.keyRelease(KeyEvent.VK_F12);
                    lastHealTime = System.currentTimeMillis();
                    canCast = false;
                } else if (canPot && usePotions.isSelected()) {
                    healPotion();
                }
            }
            if (!needLowHeal() && needHP() && hpHighCheck.isSelected()) {
                if (canCast && useSpells.isSelected()) {
                    robot.keyPress(KeyEvent.VK_F11);
                    robot.keyRelease(KeyEvent.VK_F11);
                    lastHealTime = System.currentTimeMillis();
                    canCast = false;
                }
            }
            robot.setAutoDelay(oldDelay);
        }

        //----------------------------------------------------------------------Checks to see if user enabled mana restore, and if so, manas the user.
        private void mana() {
            int oldDelay = robot.getAutoDelay();
            robot.setAutoDelay(0);
            if (needCriticalMana() && mpCritCheck.isSelected()) {
                manaPotion();
            }
            if (!needHP() && mpRestoreCheck.isSelected()) {
                manaPotion();
            }
            robot.setAutoDelay(oldDelay);
        }

        //----------------------------------------------------------------------Uses a health potion at the specified location.
        private void healPotion() {
            int oldX = MouseInfo.getPointerInfo().getLocation().x;
            int oldY = MouseInfo.getPointerInfo().getLocation().y;
            robot.mouseMove(hPotionX, hPotionY);
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            robot.mouseMove(oldX, oldY);
            lastPotTime = System.currentTimeMillis();
            canPot = false;
        }

        //----------------------------------------------------------------------Uses a mana potion at the specified location.
        private void manaPotion() {
            int oldX = MouseInfo.getPointerInfo().getLocation().x;
            int oldY = MouseInfo.getPointerInfo().getLocation().y;
            robot.mouseMove(mPotionX, mPotionY);
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            robot.mouseMove(oldX, oldY);
            lastPotTime = System.currentTimeMillis();
            canPot = false;
        }

        //----------------------------------------------------------------------Returns true if needLowHeal() or health less than high heal %
        private boolean needHP() {
            hpLowCurColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) lowHealPercent), hpLeftY);
            hpHighCurColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) highHealPercent), hpLeftY);
            if (hpLowCurColor.getRGB() != hpLowOldColor.getRGB()) {
                return true;
            }
            if (hpHighCurColor.getRGB() != hpHighOldColor.getRGB()) {
                return true;
            } else {
                return false;
            }
        }

        //----------------------------------------------------------------------Returns true if health is less than low heal %
        private boolean needLowHeal() {
            hpLowCurColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) lowHealPercent), hpLeftY);
            if (hpLowCurColor.getRGB() != hpLowOldColor.getRGB()) {
                return true;
            } else {
                return false;
            }
        }

        //----------------------------------------------------------------------Returns true if needCriticalMana() or mana less than mana %
        private boolean needMana() {
            mpRestoreCurColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) manaRestorePercent), mpLeftY);
            mpCriticalCurColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) criticalManaPercent), mpLeftY);
            if (mpCriticalCurColor.getRGB() != mpCriticalOldColor.getRGB()) {
                return true;
            }
            if (mpRestoreCurColor.getRGB() != mpRestoreOldColor.getRGB()) {
                return true;
            } else {
                return false;
            }
        }

        //----------------------------------------------------------------------Returns true if mana is less than critical %
        private boolean needCriticalMana() {
            mpCriticalCurColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) criticalManaPercent), mpLeftY);
            if (mpCriticalCurColor.getRGB() != mpCriticalOldColor.getRGB()) {
                return true;
            } else {
                return false;
            }
        }

        //----------------------------------------------------------------------Drags loot from specified loot area into specified loot backpack.
        private void loot() {
            int oldX = MouseInfo.getPointerInfo().getLocation().x;
            int oldY = MouseInfo.getPointerInfo().getLocation().y;
            if (canLoot()) {
                robot.mouseMove(lootAreaX, lootAreaY);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.mousePress(InputEvent.BUTTON1_MASK);

                robot.mouseMove(lootBPX, lootBPY);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                robot.delay(25);
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_CONTROL);
            }
            robot.mouseMove(oldX, oldY);
        }

        //----------------------------------------------------------------------Return true if there is something to loot.
        private boolean canLoot() {
            int low = 20;
            int high = 30;
            boolean falseAlarm = false;

            //------------------------------------------------------------------This while loop checks for clients normal background color and disregards.
            while (low <= high) {
                monsterCorpseCurColor = robot.getPixelColor(lootAreaX, lootAreaY);
                if (monsterCorpseCurColor.getRGB() == new Color(low, low, low).getRGB()) {
                    falseAlarm = true;
                }
                low++;
            }
            if (!falseAlarm) {
                return true;
            } else {
                return false;
            }
        }

        //----------------------------------------------------------------------Drag arrows from arrow backpack into specified hand location.
        private void reload() {
            int oldX = MouseInfo.getPointerInfo().getLocation().x;
            int oldY = MouseInfo.getPointerInfo().getLocation().y;

            robot.mouseMove(ammunitionBPX, ammunitionBPY);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.mousePress(InputEvent.BUTTON1_MASK);

            robot.mouseMove(arrowSpotX, arrowSpotY);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(25);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            robot.mouseMove(oldX, oldY);
            lastReload = System.currentTimeMillis();
        }

        //----------------------------------------------------------------------Cast our training spell.
        private void spellTrain() {
            robot.keyPress(KeyEvent.VK_F10);
            robot.keyRelease(KeyEvent.VK_F10);
            canCast = false;
            lastHealTime = System.currentTimeMillis();
        }
    };

    //--------------------------------------------------------------------------Constructors
    public GUI() throws IOException, AWTException {

        robot = new Robot();
        robot.setAutoDelay(0);

        int bitType = Integer.valueOf(System.getProperty("sun.arch.data.model"));
        if(bitType==32){
            
        }
        if(bitType==64){
           
            
        }
       
        

        initComponents();

        timer.schedule(task, 0, 1);
        
        if (file.canRead()) {
            load();
        }

        activateAll();
        pauseCheck.setVisible(false);

    }

    //--------------------------------------------------------------------------listen for hotkey
    @Override
    public void onHotKey(int aIdentifier) {
        if (aIdentifier == 1) {
            combo();
        }
        if (aIdentifier == 2) {
            togglePause();
        }
    }

    //--------------------------------------------------------------------------cast attack combo
    private void combo() {
        robot.keyPress(KeyEvent.VK_F1);
        canCast = false;
        robot.keyPress(KeyEvent.VK_F2);
        canCast = false;
        robot.keyPress(KeyEvent.VK_F3);
        canCast = false;
        robot.keyPress(KeyEvent.VK_F4);
        canCast = false;
        robot.keyRelease(KeyEvent.VK_F1);
        robot.keyRelease(KeyEvent.VK_F2);
        robot.keyRelease(KeyEvent.VK_F3);
        robot.keyRelease(KeyEvent.VK_F4);
    }

    private void load() throws IOException {
        reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

        //----------------------------------------------------------------------Load health coords tab
        hpX.setText(reader.readLine());
        hpY.setText(reader.readLine());
        hPotX.setText(reader.readLine());
        hPotY.setText(reader.readLine());
        mPotY.setText(reader.readLine());
        mPotX.setText(reader.readLine());

        //----------------------------------------------------------------------Load hunting tab
        ammoX.setText(reader.readLine());
        ammoY.setText(reader.readLine());
        arrowHandX.setText(reader.readLine());
        arrowHandY.setText(reader.readLine());
        gpX.setText(reader.readLine());
        gpY.setText(reader.readLine());
        lootZoneX.setText(reader.readLine());
        lootZoneY.setText(reader.readLine());
        hpHighBox.setText(reader.readLine());
        hpLowBox.setText(reader.readLine());
        mpRestoreBox.setText(reader.readLine());
        mpCriticalBox.setText(reader.readLine());
        reader.close();

    }

    private void save() throws IOException {
        try {
            file.createNewFile();
        } catch (IOException ex) {
            fileDir.mkdirs();
        }
        writer = new BufferedWriter(new FileWriter(file));

        //----------------------------------------------------------------------Write health coords tab
        writer.write(hpX.getText() + "\n");
        writer.write(hpY.getText() + "\n");
        writer.write(hPotX.getText() + "\n");
        writer.write(hPotY.getText() + "\n");
        writer.write(mPotY.getText() + "\n");
        writer.write(mPotX.getText() + "\n");

        //----------------------------------------------------------------------Write hunting tab
        writer.write(ammoX.getText() + "\n");
        writer.write(ammoY.getText() + "\n");
        writer.write(arrowHandX.getText() + "\n");
        writer.write(arrowHandY.getText() + "\n");
        writer.write(gpX.getText() + "\n");
        writer.write(gpY.getText() + "\n");
        writer.write(lootZoneX.getText() + "\n");
        writer.write(lootZoneY.getText() + "\n");

        writer.write(hpHighBox.getText() + "\n");
        writer.write(hpLowBox.getText() + "\n");
        writer.write(mpRestoreBox.getText() + "\n");
        writer.write(mpCriticalBox.getText());

        writer.flush();
        writer.close();
    }

    private void getInput() {
        boolean isRegistered = true;
            activateAll();
    }

    private void activateAll() {
        boolean isRegistered = true;


        //----------------------------------------------------------------------Enable menu items
        menuLoad.setEnabled(isRegistered);
        menuSave.setEnabled(isRegistered);

        //----------------------------------------------------------------------Enable healing tab
        hpHighBox.setEditable(isRegistered);
        hpLowBox.setEditable(isRegistered);
        mpRestoreBox.setEditable(isRegistered);
        mpCriticalBox.setEditable(isRegistered);
        startButton.setEnabled(isRegistered);
        //----------------------------------------------------------------------Enable healing coords tab
        hpX.setEditable(isRegistered);
        hpY.setEditable(isRegistered);
        hPotX.setEditable(isRegistered);
        hPotY.setEditable(isRegistered);
        mPotY.setEditable(isRegistered);
        mPotX.setEditable(isRegistered);

        //----------------------------------------------------------------------Enable hunting tab
        ammoX.setEditable(isRegistered);
        ammoY.setEditable(isRegistered);
        arrowHandX.setEditable(isRegistered);
        arrowHandY.setEditable(isRegistered);
        gpX.setEditable(isRegistered);
        gpY.setEditable(isRegistered);
        lootZoneX.setEditable(isRegistered);
        lootZoneY.setEditable(isRegistered);

        //----------------------------------------------------------------------Enable training tab
        spellManaCost.setEditable(isRegistered);
    }

    private void togglePause() {
        if (!isPaused) {
            isPaused = true;
            pauseCheck.setSelected(true);
        } else {
            isPaused = false;
            pauseCheck.setSelected(false);
            //------------------------------------------------------------------Since we paused, we need to redo Healing Coord Tab Variables
            hpLeftX = Integer.valueOf(hpX.getText());
            hpLeftY = Integer.valueOf(hpY.getText());
            mpLeftX = hpLeftX;
            mpLeftY = hpLeftY + 22;
            hPotionX = Integer.valueOf(hPotX.getText());
            hPotionY = Integer.valueOf(hPotY.getText());
            mPotionX = Integer.valueOf(mPotX.getText());
            mPotionY = Integer.valueOf(mPotY.getText());

            //------------------------------------------------------------------Since we paused, we need to redo Healing Tab Variables
            highHealPercent = Integer.valueOf(hpHighBox.getText());
            lowHealPercent = Integer.valueOf(hpLowBox.getText());
            manaRestorePercent = Integer.valueOf(mpRestoreBox.getText());
            criticalManaPercent = Integer.valueOf(mpCriticalBox.getText());

            //------------------------------------------------------------------Since we paused, we need to redo Hunting Tab Variables
            ammunitionBPX = Integer.valueOf(ammoX.getText());
            ammunitionBPY = Integer.valueOf(ammoY.getText());
            arrowSpotX = Integer.valueOf(arrowHandX.getText());
            arrowSpotY = Integer.valueOf(arrowHandY.getText());

            lootBPX = Integer.valueOf(gpX.getText());
            lootBPY = Integer.valueOf(gpY.getText());
            lootAreaX = Integer.valueOf(lootZoneX.getText());
            lootAreaY = Integer.valueOf(lootZoneY.getText());

            //------------------------------------------------------------------Since we paused, we need to redo Training Tab Variables
            spellCost = Integer.valueOf(spellManaCost.getText());

            //------------------------------------------------------------------Finally, update all the colors
            hpHighOldColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) highHealPercent), hpLeftY);
            mpRestoreOldColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) manaRestorePercent), mpLeftY);
            hpLowOldColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) lowHealPercent), hpLeftY);
            mpCriticalOldColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) criticalManaPercent), mpLeftY);

            monsterCorpseCurColor = robot.getPixelColor(lootAreaX, lootAreaY);
            monsterCorpseOldColor = robot.getPixelColor(lootAreaX, lootAreaY);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        healPanel = new javax.swing.JPanel();
        hpHighBox = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        hpLowBox = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        mpRestoreBox = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        pauseCheck = new javax.swing.JCheckBox();
        startButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        mpCriticalBox = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        hpHighCheck = new javax.swing.JCheckBox();
        mpRestoreCheck = new javax.swing.JCheckBox();
        hpLowCheck = new javax.swing.JCheckBox();
        mpCritCheck = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        usePotions = new javax.swing.JCheckBox();
        useSpells = new javax.swing.JCheckBox();
        posPanel = new javax.swing.JPanel(){};
        wCoordBox = new javax.swing.JTextField(){};
        hpX = new javax.swing.JTextField();
        hpY = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        hPotX = new javax.swing.JTextField();
        hPotY = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        mPotY = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        mPotX = new javax.swing.JTextField();
        huntingPanel = new javax.swing.JPanel();
        hCoordBox = new javax.swing.JTextField(){};
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        ammoX = new javax.swing.JTextField();
        ammoY = new javax.swing.JTextField();
        reloadCheck = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        gpX = new javax.swing.JTextField();
        gpY = new javax.swing.JTextField();
        lootCheck = new javax.swing.JCheckBox();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        lootZoneX = new javax.swing.JTextField();
        lootZoneY = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        arrowHandX = new javax.swing.JTextField();
        arrowHandY = new javax.swing.JTextField();
        trainingPanel = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        spellManaCost = new javax.swing.JTextField();
        spellTrainerCheck = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuSave = new javax.swing.JMenuItem();
        menuLoad = new javax.swing.JMenuItem();
        menuExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("wake EyE");
        setResizable(false);

        tabbedPane.setBackground(new java.awt.Color(204, 204, 204));
        tabbedPane.setForeground(new java.awt.Color(51, 51, 51));
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        healPanel.setFocusable(false);

        hpHighBox.setText("10");
        hpHighBox.setToolTipText("Enter the % you want to heal at here.");

        jLabel3.setText("HP High %");
        jLabel3.setToolTipText("% you want to heal with high spell at.");

        hpLowBox.setText("10");
        hpLowBox.setToolTipText("Enter the % you want to heal at here.");

        jLabel5.setText("HP Low %");
        jLabel5.setToolTipText("% you want to use your strong healing spell at.");

        mpRestoreBox.setText("10");
        mpRestoreBox.setToolTipText("Enter the % you want to restore mana at here.");

        jLabel12.setText("MP Restore %");
        jLabel12.setToolTipText("% that you want to start restoring mana at.");

        pauseCheck.setText("Paused");
        pauseCheck.setToolTipText("If enabled, pauses bot. When unchecked, bot reinitializes positions, and starts healing again.");
        pauseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseCheckActionPerformed(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        jLabel4.setBackground(new java.awt.Color(102, 204, 0));
        jLabel4.setForeground(new java.awt.Color(255, 0, 55));
        jLabel4.setText("Change F11 to Your High Heal Spell");

        mpCriticalBox.setText("10");
        mpCriticalBox.setToolTipText("Enter the % you want to restore mana, no matter what, at here.");

        jLabel13.setText("MP Crit %");
        jLabel13.setToolTipText("% that you can no longer cast healing spell at.");

        hpHighCheck.setText("Enabled");

        mpRestoreCheck.setText("Enabled");

        hpLowCheck.setText("Enabled");

        mpCritCheck.setText("Enabled");
        mpCritCheck.setToolTipText("If enabled, once you reach the percent set, it will restore mana, regardless of anything else.");

        jLabel14.setBackground(new java.awt.Color(102, 204, 0));
        jLabel14.setForeground(new java.awt.Color(255, 0, 55));
        jLabel14.setText("Change F12 to Your Low Heal Spell");

        usePotions.setText("Use Health Potions");

        useSpells.setText("Use Healing Spells");

        javax.swing.GroupLayout healPanelLayout = new javax.swing.GroupLayout(healPanel);
        healPanel.setLayout(healPanelLayout);
        healPanelLayout.setHorizontalGroup(
            healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(healPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(healPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, healPanelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pauseCheck))
                    .addGroup(healPanelLayout.createSequentialGroup()
                        .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useSpells)
                            .addComponent(usePotions)
                            .addGroup(healPanelLayout.createSequentialGroup()
                                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(healPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addGap(95, 95, 95))
                                        .addGroup(healPanelLayout.createSequentialGroup()
                                            .addComponent(hpHighBox)
                                            .addGap(4, 4, 4)
                                            .addComponent(hpHighCheck)
                                            .addGap(2, 2, 2)))
                                    .addGroup(healPanelLayout.createSequentialGroup()
                                        .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5)
                                            .addGroup(healPanelLayout.createSequentialGroup()
                                                .addComponent(hpLowBox, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(hpLowCheck)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mpCriticalBox)
                                    .addComponent(mpRestoreBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mpCritCheck)
                                    .addComponent(mpRestoreCheck))))
                        .addGap(0, 108, Short.MAX_VALUE)))
                .addContainerGap())
        );
        healPanelLayout.setVerticalGroup(
            healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(healPanelLayout.createSequentialGroup()
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(healPanelLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mpRestoreBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mpRestoreCheck)))
                    .addGroup(healPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hpHighBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hpHighCheck))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hpLowBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mpCriticalBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hpLowCheck)
                    .addComponent(mpCritCheck))
                .addGap(18, 18, 18)
                .addComponent(usePotions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(useSpells)
                .addGap(18, 18, 18)
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pauseCheck, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(healPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(startButton))
                .addGap(11, 11, 11))
        );

        tabbedPane.addTab("Healing", healPanel);

        posPanel.setFocusable(false);
        posPanel.setOpaque(false);

        wCoordBox.setEditable(false);
        wCoordBox.setText("X:__ Y:__");
        wCoordBox.setAutoscrolls(false);
        wCoordBox.setFocusable(false);
        wCoordBox.setRequestFocusEnabled(false);

        hpX.setText("0");
        hpX.setToolTipText("Enter here the X value of the left edge of the health bar in the character status window.");
        hpX.setAutoscrolls(false);

        hpY.setText("0");
        hpY.setToolTipText("Enter here the Y value of the left, centered, edge of the health bar in the character status window.");
        hpY.setAutoscrolls(false);

        jLabel1.setText("Left Edge Of Health Bar X");

        jLabel2.setText("Left Edge of Health Bar Y");

        hPotX.setText("0");
        hPotX.setToolTipText("Enter here the X value of the mouse, centered ontop of the first square of potions, in your health potion backpack.");
        hPotX.setAutoscrolls(false);

        hPotY.setText("0");
        hPotY.setToolTipText("Enter here the Y value of the mouse, centered ontop of the first square of potions, in your health potion backpack.");
        hPotY.setAutoscrolls(false);

        jLabel6.setText("Center of HP Pot Square X");

        jLabel7.setText("Center of HP Pot Y");

        mPotY.setText("0");
        mPotY.setToolTipText("Enter here the Y value of the mouse, centered ontop of the first square of potions, in your mana potion backpack.");
        mPotY.setAutoscrolls(false);

        jLabel8.setText("Center of MP Pot Square Y");

        jLabel9.setText("Center of MP Pot Square X");

        mPotX.setText("0");
        mPotX.setToolTipText("Enter here the X value of the mouse, centered ontop of the first square of potions, in your mana potion backpack.");
        mPotX.setAutoscrolls(false);

        javax.swing.GroupLayout posPanelLayout = new javax.swing.GroupLayout(posPanel);
        posPanel.setLayout(posPanelLayout);
        posPanelLayout.setHorizontalGroup(
            posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(posPanelLayout.createSequentialGroup()
                        .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(mPotX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hPotY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(mPotY, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(hPotX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(posPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(posPanelLayout.createSequentialGroup()
                        .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(hpX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(hpY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(132, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, posPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(wCoordBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        posPanelLayout.setVerticalGroup(
            posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, posPanelLayout.createSequentialGroup()
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hpX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hpY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(5, 5, 5)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hPotX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hPotY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mPotX)
                    .addComponent(mPotY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(wCoordBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab("Healing Coords", posPanel);

        huntingPanel.setFocusable(false);

        hCoordBox.setEditable(false);
        hCoordBox.setText("X:__ Y:__");
        hCoordBox.setAutoscrolls(false);
        hCoordBox.setFocusable(false);
        hCoordBox.setRequestFocusEnabled(false);

        jLabel15.setText("Ammunition BP X");
        jLabel15.setToolTipText("First square of your ammunition backpack.");

        jLabel16.setText("Ammunition BP Y");
        jLabel16.setToolTipText("First square of your ammunition backpack.");

        ammoX.setText("0");
        ammoX.setToolTipText("First square of your ammunition backpack.");

        ammoY.setText("0");
        ammoY.setToolTipText("First square of your ammunition backpack.");
        ammoY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ammoYActionPerformed(evt);
            }
        });

        reloadCheck.setText("Reloading Enabled ");
        reloadCheck.setToolTipText("Restocks ammo every 60 seconds.");
        reloadCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadCheckActionPerformed(evt);
            }
        });

        jLabel18.setText("GP Loot BP X");
        jLabel18.setToolTipText("First square of the backpack that your gold will go into.");

        jLabel19.setText("GP Loot BP Y");
        jLabel19.setToolTipText("First square of the backpack that your gold will go into.");

        gpX.setText("0");
        gpX.setToolTipText("First square of the backpack that your gold will go into.");

        gpY.setText("0");
        gpY.setToolTipText("First square of the backpack that your gold will go into.");

        lootCheck.setText("Loot Enabled     ");

        jLabel22.setText("Looting Zone X");
        jLabel22.setToolTipText("The first square of the location that corpses you open will appear at.");

        jLabel23.setText("Looting Zone Y");
        jLabel23.setToolTipText("The first square of the location that corpses you open will appear at.");

        lootZoneX.setText("0");
        lootZoneX.setToolTipText("The first square of the location that corpses you open will appear at.");

        lootZoneY.setText("0");
        lootZoneY.setToolTipText("The first square of the location that corpses you open will appear at.");

        jLabel20.setText("Arrow Spot X");

        jLabel21.setText("Arrow Spot Y");

        arrowHandX.setText("0");

        arrowHandY.setText("0");

        javax.swing.GroupLayout huntingPanelLayout = new javax.swing.GroupLayout(huntingPanel);
        huntingPanel.setLayout(huntingPanelLayout);
        huntingPanelLayout.setHorizontalGroup(
            huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(huntingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(huntingPanelLayout.createSequentialGroup()
                        .addComponent(arrowHandX, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(huntingPanelLayout.createSequentialGroup()
                        .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(huntingPanelLayout.createSequentialGroup()
                                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(huntingPanelLayout.createSequentialGroup()
                                        .addGap(108, 108, 108)
                                        .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel23)
                                            .addComponent(lootZoneY, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(lootZoneX, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(hCoordBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel22)
                            .addComponent(jLabel15)
                            .addComponent(ammoX, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(huntingPanelLayout.createSequentialGroup()
                                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(huntingPanelLayout.createSequentialGroup()
                                        .addGap(108, 108, 108)
                                        .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(gpY, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel19)))
                                    .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(gpX, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(lootCheck))
                            .addGroup(huntingPanelLayout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addGap(45, 45, 45)
                                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel21)
                                    .addComponent(ammoY, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(huntingPanelLayout.createSequentialGroup()
                                        .addComponent(arrowHandY, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(reloadCheck)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        huntingPanelLayout.setVerticalGroup(
            huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, huntingPanelLayout.createSequentialGroup()
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ammoX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ammoY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(arrowHandX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(arrowHandY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reloadCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gpX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lootCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(huntingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lootZoneX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lootZoneY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hCoordBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabbedPane.addTab("Hunting", huntingPanel);

        trainingPanel.setFocusable(false);

        jLabel17.setText("Spell Mana Cost");

        spellManaCost.setText("0");

        spellTrainerCheck.setText("Spell Trainer Enabled");

        jLabel10.setForeground(new java.awt.Color(255, 0, 55));
        jLabel10.setText("Change F10 To Your Training Spell");

        javax.swing.GroupLayout trainingPanelLayout = new javax.swing.GroupLayout(trainingPanel);
        trainingPanel.setLayout(trainingPanelLayout);
        trainingPanelLayout.setHorizontalGroup(
            trainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trainingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trainingPanelLayout.createSequentialGroup()
                        .addGroup(trainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spellManaCost))
                        .addGap(18, 18, 18)
                        .addComponent(spellTrainerCheck))
                    .addComponent(jLabel10))
                .addContainerGap(183, Short.MAX_VALUE))
        );
        trainingPanelLayout.setVerticalGroup(
            trainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trainingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spellManaCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spellTrainerCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addContainerGap())
        );

        tabbedPane.addTab("Training", trainingPanel);

        jLabel11.setText("Pause button toggles pause for the whole program.");

        jLabel24.setText("Insert button sends warlock combo on keys F1,F2,F3,F4");

        jLabel25.setText("mode enabled.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel24)
                    .addComponent(jLabel25))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addContainerGap(171, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Global HotKeys", jPanel1);

        jMenuBar1.setForeground(new java.awt.Color(51, 51, 51));

        jMenu1.setText("File");

        menuSave.setText("Save");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        jMenu1.add(menuSave);

        menuLoad.setText("Load");
        menuLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadActionPerformed(evt);
            }
        });
        jMenu1.add(menuLoad);

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        jMenu1.add(menuExit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
      
        System.exit(0);
    }//GEN-LAST:event_menuExitActionPerformed

    private void menuLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadActionPerformed
        try {
            load();
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_menuLoadActionPerformed

    private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
        try {
            save();
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_menuSaveActionPerformed

    private void pauseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseCheckActionPerformed
        togglePause();
    }//GEN-LAST:event_pauseCheckActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        if (hpX.getText() != null && hpY.getText() != null) {

            //------------------------------------------------------------------Initialize all variables
            lastHealTime = System.currentTimeMillis();
            lastPotTime = System.currentTimeMillis();
            //------------------------------------------------------------------Healing Coord Tab Variables
            hpLeftX = Integer.valueOf(hpX.getText());
            hpLeftY = Integer.valueOf(hpY.getText());
            mpLeftX = hpLeftX;
            mpLeftY = hpLeftY + 22;
            hPotionX = Integer.valueOf(hPotX.getText());
            hPotionY = Integer.valueOf(hPotY.getText());
            mPotionX = Integer.valueOf(mPotX.getText());
            mPotionY = Integer.valueOf(mPotY.getText());

            //------------------------------------------------------------------Healing Tab Variables
            highHealPercent = Integer.valueOf(hpHighBox.getText());
            lowHealPercent = Integer.valueOf(hpLowBox.getText());
            manaRestorePercent = Integer.valueOf(mpRestoreBox.getText());
            criticalManaPercent = Integer.valueOf(mpCriticalBox.getText());

            //------------------------------------------------------------------Hunting Tab Variables
            ammunitionBPX = Integer.valueOf(ammoX.getText());
            ammunitionBPY = Integer.valueOf(ammoY.getText());
            arrowSpotX = Integer.valueOf(arrowHandX.getText());
            arrowSpotY = Integer.valueOf(arrowHandY.getText());
            lootBPX = Integer.valueOf(gpX.getText());
            lootBPY = Integer.valueOf(gpY.getText());
            lootAreaX = Integer.valueOf(lootZoneX.getText());
            lootAreaY = Integer.valueOf(lootZoneY.getText());

            lastReload = System.currentTimeMillis();

            //------------------------------------------------------------------Training Tab Variables
            spellCost = Integer.valueOf(spellManaCost.getText());

            //------------------------------------------------------------------Initialize colors
            hpHighOldColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) highHealPercent), hpLeftY);
            mpRestoreOldColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) manaRestorePercent), mpLeftY);
            hpLowOldColor = robot.getPixelColor(hpLeftX + (int) (pixelPercent * (double) lowHealPercent), hpLeftY);
            mpCriticalOldColor = robot.getPixelColor(mpLeftX + (int) (pixelPercent * (double) criticalManaPercent), mpLeftY);

            monsterCorpseCurColor = robot.getPixelColor(lootAreaX, lootAreaY);
            monsterCorpseOldColor = robot.getPixelColor(lootAreaX, lootAreaY);

            //------------------------------------------------------------------Start healing checks and remove start button.
            healTimer.schedule(healTask, 0, 1);
            pauseCheck.setVisible(true);
            startButton.setVisible(false);
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void ammoYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ammoYActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ammoYActionPerformed

    private void reloadCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadCheckActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reloadCheckActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new GUI().setVisible(true);
                } catch (IOException | AWTException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ammoX;
    private javax.swing.JTextField ammoY;
    private javax.swing.JTextField arrowHandX;
    private javax.swing.JTextField arrowHandY;
    private javax.swing.JTextField gpX;
    private javax.swing.JTextField gpY;
    private javax.swing.JTextField hCoordBox;
    private javax.swing.JTextField hPotX;
    private javax.swing.JTextField hPotY;
    private javax.swing.JPanel healPanel;
    private javax.swing.JTextField hpHighBox;
    private javax.swing.JCheckBox hpHighCheck;
    private javax.swing.JTextField hpLowBox;
    private javax.swing.JCheckBox hpLowCheck;
    private javax.swing.JTextField hpX;
    private javax.swing.JTextField hpY;
    private javax.swing.JPanel huntingPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox lootCheck;
    private javax.swing.JTextField lootZoneX;
    private javax.swing.JTextField lootZoneY;
    private javax.swing.JTextField mPotX;
    private javax.swing.JTextField mPotY;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem menuLoad;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JCheckBox mpCritCheck;
    private javax.swing.JTextField mpCriticalBox;
    private javax.swing.JTextField mpRestoreBox;
    private javax.swing.JCheckBox mpRestoreCheck;
    private javax.swing.JCheckBox pauseCheck;
    private javax.swing.JPanel posPanel;
    private javax.swing.JCheckBox reloadCheck;
    private javax.swing.JTextField spellManaCost;
    private javax.swing.JCheckBox spellTrainerCheck;
    private javax.swing.JButton startButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel trainingPanel;
    private javax.swing.JCheckBox usePotions;
    private javax.swing.JCheckBox useSpells;
    private javax.swing.JTextField wCoordBox;
    // End of variables declaration//GEN-END:variables
}
