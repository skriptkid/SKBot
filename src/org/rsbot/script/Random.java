package org.rsbot.script;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;

import java.awt.*;
import java.util.logging.Level;

public abstract class Random extends Methods implements PaintListener {
	private String name;
	private volatile boolean enabled = true;
	private Script script;
	private final long timeout = random(240, 300);

	/**
	 * Detects whether or not this anti-random should
	 * activate.
	 *
	 * @return <tt>true</tt> if the current script
	 *         should be paused and control passed to this
	 *         anti-random's loop.
	 */
	public abstract boolean activateCondition();

	protected abstract int loop();


	/**
	 * Called after the method providers for this Random
	 * become available for use in initialization.
	 */
	void onStart() {

	}

	protected void onFinish() {

	}

	/**
	 * Override to provide a time limit in seconds for
	 * this anti-random to complete.
	 *
	 * @return The number of seconds after activateCondition
	 *         returns <tt>true</tt> before the anti-random should be
	 *         detected as having failed. If this time is reached
	 *         the random and running script will be stopped.
	 */
	long getTimeout() {
		return timeout;
	}

	@Override
	public final void init(final MethodContext ctx) {
		super.init(ctx);
		onStart();
	}

	public final boolean isActive() {
		return script != null;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Stops the current script; player can be logged out before
	 * the script is stopped.
	 *
	 * @param logout <tt>true</tt> if the player should be logged
	 *               out before the script is stopped.
	 */
	protected void stopScript(final boolean logout) {
		script.stopScript(logout);
	}

	public final void run(final Script ctx) {
		script = ctx;
		name = getClass().getAnnotation(ScriptManifest.class).name();
		ctx.ctx.bot.getEventManager().removeListener(ctx);
		for (final Script s : ctx.delegates) {
			ctx.ctx.bot.getEventManager().removeListener(s);
		}
		ctx.ctx.bot.getEventManager().addListener(this);
		log("Random event started: " + name);
		long timeout = getTimeout();
		if (timeout > 0) {
			timeout *= 1000;
			timeout += System.currentTimeMillis();
		}
		while (ctx.isRunning()) {
			try {
				final int wait = loop();
				if (wait == -1) {
					break;
				} else if (timeout > 0 && System.currentTimeMillis() >= timeout) {
					log.warning("Time limit reached for " + name + ".");
					ctx.stopScript();
				} else {
					sleep(wait);
				}
			} catch (final Throwable ex) {
				log.log(Level.SEVERE, "Uncaught exception: ", ex);
				break;
			}
		}
		script = null;
		onFinish();
		log("Random event finished: " + name);
		ctx.ctx.bot.getEventManager().removeListener(this);
		sleep(1000);
		ctx.ctx.bot.getEventManager().addListener(ctx);
		for (final Script s : ctx.delegates) {
			ctx.ctx.bot.getEventManager().addListener(s);
		}
	}

	public final void onRepaint(final Graphics g) {
		final Point p = mouse.getLocation();
		final int w = game.getWidth(), h = game.getHeight();
		g.setColor(new Color(28, 107, 160, 100));
		g.fillRect(0, 0, p.x - 1, p.y - 1);
		g.fillRect(p.x + 1, 0, w - (p.x + 1), p.y - 1);
		g.fillRect(0, p.y + 1, p.x - 1, h - (p.y - 1));
		g.fillRect(p.x + 1, p.y + 1, w - (p.x + 1), h - (p.y - 1));
		g.setColor(Color.RED);
		g.drawString(name, 560, 20);
		final String[] authors = getClass().getAnnotation(ScriptManifest.class).authors();
		g.drawString(authors.length > 1 ? "Authors: " + constructStringArray(authors) : authors.length > 0 ? "Author: " + authors[0] : "?", 560, 35);
	}

	private String constructStringArray(final String[] arrayOfString) {
		String r = "";
		for (int i = 0; i < arrayOfString.length; i++) {
			r += arrayOfString[i] + (i + 1 < arrayOfString.length ? ", " : "");
		}
		return r;
	}
}
