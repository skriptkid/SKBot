package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSInterface;

@ScriptManifest(authors = {"ToshiXZ"}, name = "TeleotherCloser", version = 1.0)
public class TeleotherCloser extends Random {

	@Override
	public boolean activateCondition() {
		final RSInterface iface = interfaces.get(326);
		return iface.isValid() && iface.getComponent(2).getText().contains("wants to teleport");
	}

	@Override
	public int loop() {
		interfaces.get(326).getComponent(8).doClick();
		sleep(random(500, 750));
		log.info("Disabling accept aid");
		game.disableAid();
		return -1;
	}
}
