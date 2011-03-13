package org.rsbot.script.randoms;

/**
 *
 * @author debauchery & Danielhatzor
 */

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.wrappers.RSComponent;


@ScriptManifest(authors={"Debauchery & Danielhatzor"}, name="Level Up", version=1.0)
public class LevelUp extends Random {

    private final static int CONGRATS_INDEX_ID = 741;
    private final static int CONGRATS_CLOSE_ID = 9;

    private enum Skills{
        ATTACK("Attack", org.rsbot.script.methods.Skills.INTERFACE_ATTACK),
	DEFENSE("Defence", org.rsbot.script.methods.Skills.INTERFACE_DEFENSE),
	STRENGTH("Strength", org.rsbot.script.methods.Skills.INTERFACE_STRENGTH),
	CONSTITUTION("Constitution", org.rsbot.script.methods.Skills.INTERFACE_CONSTITUTION),
	RANGE("Range", org.rsbot.script.methods.Skills.INTERFACE_RANGE),
	PRAYER("Prayer", org.rsbot.script.methods.Skills.INTERFACE_PRAYER),
	MAGIC("Magic", org.rsbot.script.methods.Skills.INTERFACE_MAGIC),
	COOKING("Cooking", org.rsbot.script.methods.Skills.INTERFACE_COOKING),
	WOODCUTTING("Woodcutting", org.rsbot.script.methods.Skills.INTERFACE_WOODCUTTING),
	FLETCHING("Fletching", org.rsbot.script.methods.Skills.INTERFACE_FLETCHING),
	FISHING("Fishing", org.rsbot.script.methods.Skills.INTERFACE_FISHING),
	FIREMAKING("Firemaking", org.rsbot.script.methods.Skills.INTERFACE_FIREMAKING),
	CRAFTING("Crafting", org.rsbot.script.methods.Skills.INTERFACE_CRAFTING),
	SMITHING("Smithing", org.rsbot.script.methods.Skills.INTERFACE_SMITHING),
	MINING("Mining", org.rsbot.script.methods.Skills.INTERFACE_MINING),
	HERBLORE("Herblore", org.rsbot.script.methods.Skills.INTERFACE_HERBLORE),
	AGILITY("Agility", org.rsbot.script.methods.Skills.INTERFACE_AGILITY),
	THIEVING("Thieving", org.rsbot.script.methods.Skills.INTERFACE_THIEVING),
	SLAYER("Slayer", org.rsbot.script.methods.Skills.INTERFACE_SLAYER),
	FARMING("Farming", org.rsbot.script.methods.Skills.INTERFACE_FARMING),
	RUNECRAFTING("Runecrafting", org.rsbot.script.methods.Skills.INTERFACE_RUNECRAFTING),
	HUNTER("Hunter", org.rsbot.script.methods.Skills.INTERFACE_HUNTER),
        CONSTRUCTION("Construction", org.rsbot.script.methods.Skills.INTERFACE_CONSTRUCTION),
	SUMMONING("Summoning", org.rsbot.script.methods.Skills.INTERFACE_SUMMONING),
	DUNGEONEERING("Dungeoneering", org.rsbot.script.methods.Skills.INTERFACE_DUNGEONEERING);
        private String name;
        private int id;

        Skills(String name, int id) {
            this.name = name;
            this.id = id;
        }

        String getName(){
            return name;
        }

        int getInterfaceID(){
            return id;
        }
    }

    public boolean activateCondition() {
        return (interfaces.get(Game.INTERFACE_LEVEL_UP).isValid());
    }

    private Skills getLeveledSkill(){
        for (Skills skill : Skills.values()) {
            if (interfaces.get(Game.INTERFACE_LEVEL_UP).containsText(skill.getName())) {
                return skill;
            }
        }
        return null;
    }

    @Override
    public int loop() {
		if (getMyPlayer().isInCombat()) {
			return -1;
		}
        if(game.getCurrentTab() != Game.TAB_STATS){
            game.openTab(Game.TAB_STATS);
        }
        Skills current = getLeveledSkill();
        if(current != null){
            RSComponent skillComp = interfaces.getComponent(320,current.getInterfaceID());
            if(skillComp != null){
                mouse.click(skillComp.getAbsoluteX(),skillComp.getAbsoluteY(), skillComp.getWidth(), skillComp.getHeight(), true);
                sleep(5000,7000);
            }
            if(interfaces.get(CONGRATS_INDEX_ID) != null){
                RSComponent close = interfaces.get(CONGRATS_INDEX_ID).getComponent(CONGRATS_CLOSE_ID);
                if(close != null){
                    close.doClick();
                }
            }
        }
        if(interfaces.get(Game.INTERFACE_LEVEL_UP).isValid()){
            interfaces.clickContinue();
        }
        log("Random level up Complete: " + current.getName());
        return -1;
    }
}
