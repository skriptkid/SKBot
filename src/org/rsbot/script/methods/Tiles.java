package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSTile;

import java.awt.*;

/**
 * Tile related operations.
 */
public class Tiles extends MethodProvider {
	Tiles(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Clicks a tile if it is on screen with given offsets in 3D space.
	 *
	 * @param tile   The <code>RSTile</code> to do the action at.
	 * @param xd     Distance from bottom left of the tile to bottom right. Ranges
	 *               from 0-1.
	 * @param yd     Distance from bottom left of the tile to top left. Ranges from
	 *               0-1.
	 * @param h      Height to click the <code>RSTile</code> at. Use 1 for tables,
	 *               0 by default.
	 * @param action The action to perform at the given <code>RSTile</code>.
	 * @return <tt>true</tt> if no exceptions were thrown; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean interact(final RSTile tile, final double xd, final double yd, final int h, final String action) {
		return methods.tiles.interact(tile, xd, yd, h, action, null);
	}

	/**
	 * Clicks a tile if it is on screen with given offsets in 3D space.
	 *
	 * @param tile   The <code>RSTile</code> to do the action at.
	 * @param xd     Distance from bottom left of the tile to bottom right. Ranges
	 *               from 0-1.
	 * @param yd     Distance from bottom left of the tile to top left. Ranges from
	 *               0-1.
	 * @param h      Height to click the <code>RSTile</code> at. Use 1 for tables,
	 *               0 by default.
	 * @param action The action to perform at the given <code>RSTile</code>.
	 * @return <tt>true</tt> if no exceptions were thrown; otherwise
	 *         <tt>false</tt>.
	 * @see org.rsbot.script.methods.Tiles#interact(org.rsbot.script.wrappers.RSTile, double, double, int, String)
	 */
	@Deprecated
	public boolean doAction(final RSTile tile, final double xd, final double yd, final int h, final String action) {
		return interact(tile, xd, yd, h, action);
	}

	/**
	 * Clicks a tile if it is on screen with given offsets in 3D space.
	 *
	 * @param tile   The <code>RSTile</code> to do the action at.
	 * @param xd     Distance from bottom left of the tile to bottom right. Ranges
	 *               from 0-1.
	 * @param yd     Distance from bottom left of the tile to top left. Ranges from
	 *               0-1.
	 * @param h      Height to click the <code>RSTile</code> at. Use 1 for tables,
	 *               0 by default.
	 * @param action The action to perform at the given <code>RSTile</code>.
	 * @param option The option to perform at the given <code>RSTile</code>.
	 * @return <tt>true</tt> if no exceptions were thrown; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean interact(final RSTile tile, final double xd, final double yd, final int h, final String action, final String option) {
		final Point location = methods.calc.tileToScreen(tile, xd, yd, h);
		if (location.x != -1 && location.y != -1) {
			methods.mouse.move(location, 3, 3);
			sleep(random(20, 100));
			return methods.menu.doAction(action, option);
		}
		return false;
	}

	/**
	 * Clicks a tile if it is on screen with given offsets in 3D space.
	 *
	 * @param tile   The <code>RSTile</code> to do the action at.
	 * @param xd     Distance from bottom left of the tile to bottom right. Ranges
	 *               from 0-1.
	 * @param yd     Distance from bottom left of the tile to top left. Ranges from
	 *               0-1.
	 * @param h      Height to click the <code>RSTile</code> at. Use 1 for tables,
	 *               0 by default.
	 * @param action The action to perform at the given <code>RSTile</code>.
	 * @param option The option to perform at the given <code>RSTile</code>.
	 * @return <tt>true</tt> if no exceptions were thrown; otherwise
	 *         <tt>false</tt>.
	 * @see org.rsbot.script.methods.Tiles#interact(org.rsbot.script.wrappers.RSTile, double, double, int, String, String)
	 */
	@Deprecated
	public boolean doAction(final RSTile tile, final double xd, final double yd, final int h, final String action, final String option) {
		return interact(tile, xd, yd, h, action, option);
	}

	/**
	 * Clicks a tile if it is on screen. It will left-click if the action is
	 * available as the default option, otherwise it will right-click and check
	 * for the action in the context methods.menu.
	 *
	 * @param tile   The RSTile that you want to click.
	 * @param action Action command to use click
	 * @return <tt>true</tt> if the tile was clicked; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean interact(final RSTile tile, final String action) {
		return methods.tiles.interact(tile, action, null);
	}

	/**
	 * Clicks a tile if it is on screen. It will left-click if the action is
	 * available as the default option, otherwise it will right-click and check
	 * for the action in the context methods.menu.
	 *
	 * @param tile   The RSTile that you want to click.
	 * @param action Action command to use click
	 * @return <tt>true</tt> if the tile was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see org.rsbot.script.methods.Tiles#interact(org.rsbot.script.wrappers.RSTile, String)
	 */
	@Deprecated
	public boolean doAction(final RSTile tile, final String action) {
		return interact(tile, action);
	}

	/**
	 * Clicks a tile if it is on screen. It will left-click if the action is
	 * available as the default menu action, otherwise it will right-click and check
	 * for the action in the context methods.menu.
	 *
	 * @param tile   The RSTile that you want to click.
	 * @param action Action of the menu entry to click
	 * @param option Option of the menu entry to click
	 * @return <tt>true</tt> if the tile was clicked; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean interact(final RSTile tile, final String action, final String option) {
		try {
			for (int i = 0; i++ < 5; ) {
				final Point location = methods.calc.tileToScreen(tile);
				if (location.x == -1 || location.y == -1) {
					return false;
				}
				methods.mouse.move(location, 5, 5);
				if (methods.menu.doAction(action, option)) {
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Clicks a tile if it is on screen. It will left-click if the action is
	 * available as the default menu action, otherwise it will right-click and check
	 * for the action in the context methods.menu.
	 *
	 * @param tile   The RSTile that you want to click.
	 * @param action Action of the menu entry to click
	 * @param option Option of the menu entry to click
	 * @return <tt>true</tt> if the tile was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see org.rsbot.script.methods.Tiles#interact(org.rsbot.script.wrappers.RSTile, String, String)
	 */
	@Deprecated
	public boolean doAction(final RSTile tile, final String action, final String option) {
		return interact(tile, action, option);
	}

	/**
	 * Returns the RSTile under the mouse.
	 *
	 * @return The <code>RSTile</code> under the mouse, or null if the mouse is
	 *         not over the viewport.
	 */
	public RSTile getTileUnderMouse() {
		final Point p = methods.mouse.getLocation();
		if (!methods.calc.pointOnScreen(p)) {
			return null;
		}
		return getTileUnderPoint(p);
	}

	/**
	 * Gets the tile under a point.
	 *
	 * @param p The point.
	 * @return RSTile at the point's location
	 */
	public RSTile getTileUnderPoint(final Point p) {
		if (!methods.calc.pointOnScreen(p)) {
			return null;
		}
		RSTile close = null;
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				final RSTile t = new RSTile(x + methods.client.getBaseX(), y + methods.client.getBaseY());
				final Point s = methods.calc.tileToScreen(t);
				if (s.x != -1 && s.y != -1) {
					if (close == null) {
						close = t;
					}
					if (methods.calc.tileToScreen(close).distance(p) > methods.calc.tileToScreen(t).distance(p)) {
						close = t;
					}
				}
			}
		}
		return close;
	}

	/**
	 * Checks if the tile "t" is closer to the player than the tile "tt"
	 *
	 * @param t  First tile.
	 * @param tt Second tile.
	 * @return True if the first tile is closer to the player than the second
	 *         tile, otherwise false.
	 */
	public boolean isCloser(final RSTile t, final RSTile tt) {
		return methods.calc.distanceTo(t) < methods.calc.distanceTo(tt);
	}

	public boolean doHover(final RSTile tile) {
		final Point p = methods.calc.tileToScreen(tile);
		if (p.getX() != -1) {
			methods.mouse.move(p);
			return true;
		}
		return false;
	}
}
