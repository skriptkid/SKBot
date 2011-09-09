package org.rsbot.gui.component;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;


/**
 * @author Mothma
 */
public class BotKeyboardShortcuts {
	private static final HashMap<Object, String> SHORTCUT_MAP = new HashMap<Object, String>();

	static {
		SHORTCUT_MAP.put(KeyEvent.VK_N, Messages.FILE + "." + Messages.NEWBOT);
		SHORTCUT_MAP.put(KeyEvent.VK_W, Messages.FILE + "." + Messages.CLOSEBOT);
		SHORTCUT_MAP.put(KeyEvent.VK_A, Messages.FILE + "." + Messages.ADDSCRIPT);
		SHORTCUT_MAP.put(KeyEvent.VK_R, Messages.FILE + "." + Messages.RUNSCRIPT);
		SHORTCUT_MAP.put(KeyEvent.VK_S, Messages.FILE + "." + Messages.STOPSCRIPT);
		SHORTCUT_MAP.put(KeyEvent.VK_P, Messages.FILE + "." + Messages.PAUSESCRIPT);
		SHORTCUT_MAP.put(KeyEvent.VK_I, Messages.FILE + "." + Messages.SAVESCREENSHOT);
		SHORTCUT_MAP.put(KeyEvent.VK_H, Messages.FILE + "." + Messages.HIDE);
		SHORTCUT_MAP.put(KeyEvent.VK_Q, Messages.FILE + "." + Messages.EXIT);
		SHORTCUT_MAP.put(KeyEvent.VK_U, Messages.EDIT + "." + Messages.ACCOUNTS);
		SHORTCUT_MAP.put(KeyEvent.VK_O, Messages.TOOLS + "." + Messages.OPTIONS);
	}

	public BotKeyboardShortcuts(final KeyboardFocusManager manager, final ActionListener listener) {
		manager.addKeyEventDispatcher(new KeyDispatcher(manager, listener));
	}

	private class KeyDispatcher implements KeyEventDispatcher {
		final KeyboardFocusManager manager;
		final ActionListener listener;

		public KeyDispatcher(final KeyboardFocusManager manager, final ActionListener listener) {
			this.manager = manager;
			this.listener = listener;
		}

		@Override
		public boolean dispatchKeyEvent(final KeyEvent e) {
			if (e.isControlDown()) {
				if (SHORTCUT_MAP.containsKey(e.getKeyCode())) {
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, SHORTCUT_MAP.get(e.getKeyCode())));
					}
				}
				return false;
			} else {
				manager.dispatchKeyEvent(e);
			}
			return true;
		}
	}
}
