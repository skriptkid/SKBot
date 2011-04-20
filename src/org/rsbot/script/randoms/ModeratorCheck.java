package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSPlayer;

public class ModeratorCheck extends Random {
    private int MAX_DISTANCE = 18;
    private String[] MOD_NAMES = {"Paul", "Ian", "Andrew", "Zezima", "Uffins", "Nexus Origin", "Nercychlidae", "Pure kq pax", "Newtinboots", "Egyptian 56", "Milkweedpod", "King Runite1", "Keugademon", "Princesseuss", "Aja Wyndward", "Mystress", "Roflsnarf", "Amypond", "Resoun", "K o R c A t", "Nats", "Sub-Zero", "Joe Jones000", "Ic Lb", "Gohan 16", "Cmdr Chaos", "Meal", "Gavindade", "A Wh1te Wolf", "Kaze", "Badger binlord", "Zarfot", "Evilchoob", "Avidspark", "Hrick90", "Altus Invita", "Ele Cambria", "Pup", "S diamant y", "Makin Pyro", "Thee Ace", "Miz Ace", "Light Arcana", "Samanthanz", "TuskanSlayer", "Lilyuffie88"};

    Filter<RSPlayer> MOD_FILTER = new Filter<RSPlayer>() { // This is the Filter I talked about.
        @Override
        public boolean accept(RSPlayer p) {
            if (calc.distanceTo(p) <= MAX_DISTANCE) {
                for (String mod : MOD_NAMES) {
                    mod = getRealName(mod);
                    if (mod.toLowerCase().equals(p.getName().toLowerCase()) ||p.getName().toLowerCase().contains("mod")) return true;
                }
            }
            return false;
        }
    };

    @Override
    public boolean activateCondition() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int loop() {
        // TODO Auto-generated method stub
        return 0;
    }

    private String getRealName(String name) {
        return name.replace(' ', '\u00A0');
    }
}
