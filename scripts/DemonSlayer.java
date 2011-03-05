package scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.methods.Magic;
import org.rsbot.script.wrappers.RSGroundItem;

/*  This script is an edit of Wituz's Daemon Killer, so part of the credit  goes to him. His topic:  http://www.powerbot.org/vb/showthread.php?t=503078 */
@ScriptManifest(authors  = { "Oddie" }, name = "Lesser Demon Slayer", description = "Kill lesser  demons at Wizards tower, telegrab and high alchemy!")
public class DemonSlayer extends Script implements PaintListener {



    private int enemyNPC = 82;
    public String statuS = "Awaiting";
    public boolean hasChecked = false;
    public int startXP = 0;
    public int currentXP = 0;
    public long counteR = System.currentTimeMillis();
    public long timeRunning = 0, hours = 0, minutes = 0, seconds = 0,
            startTime = System.currentTimeMillis();;
    int[] teleIDs = {560, 562, 1181, 1109, 1195, 1313, 1147, 1619, 1617};
    int[] alchIDs = {1181, 1109, 1195, 1313, 1147};
    public boolean activeTeleGrab = true;
    public boolean activeHighAlch = true;

    private boolean attackenemy() {
        int i = random(1,10);
        if (!hasChecked) {
            startXP = skills.getCurrentExp(Skills.MAGIC);
            hasChecked = true;
        }
        currentXP = skills.getCurrentExp(Skills.MAGIC);
        RSNPC enemy = npcs.getNearest(enemyNPC);
        if (!(enemy == null)) {
            if (enemy.getInteracting() == null) {
                if (enemy.doAction("Attack")) {
                statuS = "Attacking";
                sleep(1700, 1850);
                return true;
                        }
        }
        }
if (enemy == null) statuS = "Awaiting/Antiban";
{
    if (i==3){
    game.openTab(game.TAB_STATS);
    skills.doHover(skills.INTERFACE_MAGIC);
    sleep(1000, 2000);
    game.openTab(game.TAB_INVENTORY);
}
    if (i==4){
        camera.setAngle(random(1,360));
    }
    if (i>=8) {
        mouse.moveSlightly();
    }
sleep (random(7500, 10000));

}
 return false;
    }

        private boolean teleGrab() {
            RSGroundItem item = groundItems.getNearest(teleIDs);
            if (activeTeleGrab == false) {
            return false;
            }
            if (item == null || item.isOnScreen() == false){
                return false;
            }
            magic.castSpell(Magic.SPELL_TELEKINETIC_GRAB);
            item.doAction("Cast");
            sleep(4000, 5000);
            return true;
            }

        private boolean highAlch() {
            if (activeHighAlch == false) {
            return false;
            }
            if (inventory.getItem(561) == null){
                return false;
            }
            if (inventory.getItem(alchIDs) != null) {
            magic.castSpell(Magic.SPELL_HIGH_LEVEL_ALCHEMY );
            inventory.getItem(alchIDs).doAction("Cast");
            sleep(4000, 5000);
            return true;}
            return false;
            }
        private void skillAdvance() {
        if(interfaces.canContinue()) interfaces.clickContinue(); }

    public int loop() {
    skillAdvance();
    attackenemy();
    teleGrab();
    highAlch();
        return 10;
    }

    public void onRepaint(Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (game.isLoggedIn()) {
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

            g.setColor(new Color(200, 210, 215, 180));
            g.fillRoundRect(291, 346, 205, 71, 10, 10);
            g.setColor(Color.black);     
            g.drawRoundRect(291, 346, 205, 71, 10, 10);
            g.drawString("Running for: "+ hours +" hr. "+ minutes +" min. "+ seconds +" sec.", 294, 359);
            g.drawString("Magic EXP gained: "+ (currentXP - startXP)+".", 294, 373);
            g.drawString("Status:", 294, 387);
            g.drawString("Percent TNL: ", 294, 400);
    final int percent = skills.getPercentToNextLevel(Skills.MAGIC);
    final int length = (percent*199)/100;
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(294, 402, 199, 10, 6, 6);
            g.setColor(new Color(55, 75, 95));
            g.fillRoundRect(294, 402, length, 10, 6, 6);
            g.setColor(Color.black);
            g.drawRoundRect(294, 402, 199, 10, 6, 6);
            g.drawRoundRect(294, 402, length, 10, 6, 6);
            g.setFont(new Font("Verdana", Font.BOLD, 12));
            g.drawString(""+ statuS +"...", 335, 387);
            g.drawString(""+ percent +"%", 379, 412);
        }
    }
}