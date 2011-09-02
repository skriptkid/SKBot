package org.rsbot.script.methods;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.util.io.ScreenshotUtil;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Bot environment related operations.
 */
public class Environment extends MethodProvider {
	public static final int INPUT_MOUSE = 1, INPUT_KEYBOARD = 2;
	public static final int LOGIN_LOBBY = 1, LOGIN_GAME = 2;

	private static final Logger log = Logger.getLogger("Environment");

	public Environment(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Controls the available means of user input when user input is disabled.
	 * <p/>
	 * <br />
	 * Disable all: <code>setUserInput(0);</code> <br />
	 * Enable keyboard only:
	 * <code>setUserInput(Environment.INPUT_KEYBOARD);</code> <br />
	 * Enable mouse & keyboard:
	 * <code>setUserInput(Environment.INPUT_MOUSE | Environment.INPUT_KEYBOARD);</code>
	 *
	 * @param mask flags indicating which types of input to allow
	 */
	public void setUserInput(final int mask) {
		methods.bot.getScriptHandler().updateInput(methods.bot, mask);
	}

	/**
	 * Takes and saves a screenshot.
	 *
	 * @param hideUsername <tt>true</tt> to cover the player's username; otherwise
	 *                     <tt>false</tt>
	 */
	public void saveScreenshot(final boolean hideUsername) {
		ScreenshotUtil.saveScreenshot(methods.bot, hideUsername);
	}

	public void saveScreenshot(final boolean hideUsername, final String filename) {
		ScreenshotUtil.saveScreenshot(methods.bot, hideUsername, filename);
	}

	/**
	 * Takes a screenshot.
	 *
	 * @param hideUsername <tt>true</tt> to cover the player's username; otherwise
	 *                     <tt>false</tt>
	 * @return The screen capture image.
	 */
	public BufferedImage takeScreenshot(final boolean hideUsername) {
		return ScreenshotUtil.takeScreenshot(methods.bot, hideUsername);
	}

	/**
	 * Enables a random event solver.
	 *
	 * @param name the anti-random's (manifest) name (case insensitive)
	 * @return <tt>true</tt> if random was found and set to enabled; otherwise
	 *         <tt>false</tt>
	 */
	public boolean enableRandom(final String name) {
		for (final Random random : methods.bot.getScriptHandler().getRandoms()) {
			if (random.getClass().getAnnotation(ScriptManifest.class).name().toLowerCase().equals(name.toLowerCase())) {
				if (random.isEnabled()) {
					return true;
				} else {
					random.setEnabled(true);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Disables a random event solver.
	 *
	 * @param name the anti-random's (manifest) name (case insensitive)
	 * @return <tt>true</tt> if random was found and set to disabled; otherwise
	 *         <tt>false</tt>
	 */
	public boolean disableRandom(final String name) {
		for (final Random random : methods.bot.getScriptHandler().getRandoms()) {
			if (random.getClass().getAnnotation(ScriptManifest.class).name().toLowerCase().equals(name.toLowerCase())) {
				if (!random.isEnabled()) {
					return true;
				} else {
					random.setEnabled(false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Enables all random event solvers.
	 */
	public void enableRandoms() {
		for (final Random random : methods.bot.getScriptHandler().getRandoms()) {
			if (!random.isEnabled()) {
				random.setEnabled(true);
			}
		}
	}

	/**
	 * Disables all random event solvers.
	 */
	public void disableRandoms() {
		for (final Random random : methods.bot.getScriptHandler().getRandoms()) {
			if (random.isEnabled()) {
				random.setEnabled(false);
			}
		}
	}

	/**
	 * Sets the world for the bot to login to. -1 logs in to the current world.
	 *
	 * @param world The world to login to.
	 */
	public void setWorld(final int world) {
		try {
			methods.bot.getLoginBot().setWorld(world);
		} catch (NullPointerException ignored) {
			log.info("Client is not yet loaded.");
		}
	}

	/**
	 * Sets the login mask.
	 * Only lobby:
	 * env.setLoginFlags(Environment.LOGIN_LOBBY);
	 * <p/>
	 * Only game from lobby:
	 * env.setLoginFlags(Environment.LOGIN_GAME);
	 * <p/>
	 * Login to lobby and game:
	 * env.setLoginFlags(Environment.LOGIN_LOBBY | Environment.LOGIN_GAME);
	 *
	 * @param mask The mask
	 */
	public void setLoginMask(final int mask) {
		try {
			methods.bot.getLoginBot().setMask(mask);
		} catch (NullPointerException ignored) {
			log.info("Client is not yet loaded.");
		}
	}
}
