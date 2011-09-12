package org.rsbot.script.methods;

import org.rsbot.client.TileData;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;

/**
 * Game world and projection calculations.
 */
public class Calculations extends MethodProvider {
	public static class Render {
		public float absoluteX1 = 0, absoluteX2 = 0;
		public float absoluteY1 = 0, absoluteY2 = 0;
		public int xMultiplier = 512, yMultiplier = 512;
		public int zNear = 50, zFar = 3500;
	}

	public static class RenderData {
		public float xOff = 0, xX = 32768, xY = 0, xZ = 0;
		public float yOff = 0, yX = 0, yY = 32768, yZ = 0;
		public float zOff = 0, zX = 0, zY = 0, zZ = 32768;
	}

	public static final int[] SIN_TABLE = new int[16384];
	public static final int[] COS_TABLE = new int[16384];

	static {
		final double d = 0.00038349519697141029D;
		for (int i = 0; i < 16384; i++) {
			Calculations.SIN_TABLE[i] = (int) (32768D * Math.sin(i * d));
			Calculations.COS_TABLE[i] = (int) (32768D * Math.cos(i * d));
		}
	}

	public final Render render = new Render();
	public final RenderData renderData = new RenderData();

	Calculations(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Returns the angle to a given tile in degrees anti-clockwise from the
	 * positive x axis (where the x-axis is from west to east).
	 *
	 * @param t The target tile
	 * @return The angle in degrees
	 */
	public int angleToTile(final RSTile t) {
		final RSTile me = methods.players.getMyPlayer().getLocation();
		final int angle = (int) Math.toDegrees(Math.atan2(t.getY() - me.getY(), t.getX() - me.getX()));
		return angle >= 0 ? angle : 360 + angle;
	}

	/**
	 * Determines whether or not a given RSTile is reachable by the player.
	 *
	 * @param dest     The <code>RSTile</code> to check.
	 * @param isObject True if an instance of <code>RSObject</code>.
	 * @return <tt>true</tt> if a path can be made to the specified tile; otherwise <tt>false</tt>.
	 */
	public boolean canReach(final RSTile dest, final boolean isObject) {
		return pathLengthTo(dest, isObject) != -1;
	}

	/**
	 * Calculates the distance between two points.
	 *
	 * @param curr The first point.
	 * @param dest The second point.
	 * @return The distance between the two points, using the distance formula.
	 * @see #distanceBetween(RSTile, RSTile)
	 */
	public double distanceBetween(final Point curr, final Point dest) {
		return Math.sqrt((curr.x - dest.x) * (curr.x - dest.x) + (curr.y - dest.y) * (curr.y - dest.y));
	}

	/**
	 * Returns the diagonal distance (hypot) between two RSTiles.
	 *
	 * @param curr The starting tile.
	 * @param dest The destination tile.
	 * @return The diagonal distance between the two <code>RSTile</code>s.
	 * @see #distanceBetween(Point, Point)
	 */
	public double distanceBetween(final RSTile curr, final RSTile dest) {
		return Math.sqrt((curr.getX() - dest.getX()) * (curr.getX() - dest.getX()) + (curr.getY() - dest.getY()) * (curr.getY() - dest.getY()));
	}

	/**
	 * Returns the diagonal distance to a given RSCharacter.
	 *
	 * @param c The destination character.
	 * @return Distance to <code>RSCharacter</code>.
	 * @see #distanceTo(RSTile)
	 */
	public int distanceTo(final RSCharacter c) {
		return c == null ? -1 : distanceTo(c.getLocation());
	}

	/**
	 * Returns the diagonal distance to a given RSObject.
	 *
	 * @param o The destination object.
	 * @return Distance to <code>RSObject</code>.
	 * @see #distanceTo(RSTile)
	 */
	public int distanceTo(final RSObject o) {
		return o == null ? -1 : distanceTo(o.getLocation());
	}

	/**
	 * Returns the diagonal distance to a given RSTile.
	 *
	 * @param t The destination tile.
	 * @return Distance to <code>RSTile</code>.
	 */
	public int distanceTo(final RSTile t) {
		return t == null ? -1 : (int) distanceBetween(methods.players.getMyPlayer().getLocation(), t);
	}

	/**
	 * Will return the closest tile that is on screen to the given tile.
	 *
	 * @param tile Tile you want to get to.
	 * @return <code>RSTile</code> that is onScreen.
	 */
	public RSTile getTileOnScreen(final RSTile tile) {
		try {
			if (tileOnScreen(tile)) {
				return tile;
			} else {
				final RSTile loc = methods.players.getMyPlayer().getLocation();
				final RSTile halfWayTile = new RSTile((tile.getX() + loc.getX()) / 2, (tile.getY() + loc.getY()) / 2);
				if (tileOnScreen(halfWayTile)) {
					return halfWayTile;
				} else {
					return getTileOnScreen(halfWayTile);
				}
			}
		} catch (final StackOverflowError soe) {
			return null;
		}
	}

	/**
	 * Returns the screen location of a given point on the ground. This accounts
	 * for the height of the ground at the given location.
	 *
	 * @param x      x value based on the game plane.
	 * @param y      y value based on the game plane.
	 * @param height height offset (normal to the ground).
	 * @return <code>Point</code> based on screen; otherwise <code>new Point(-1, -1)</code>.
	 */
	public Point groundToScreen(final int x, final int y, final int height) {
		if (methods.client.getGroundByteArray() == null || methods.client.getTileData() == null || x < 512 || y < 512 || x > 52224 || y > 52224) {
			return new Point(-1, -1);
		}
		final int z = tileHeight(x, y) + height;
		return worldToScreen(x, y, z);
	}

	/**
	 * Returns the length of the path generated to a given RSTile.
	 *
	 * @param dest     The destination tile.
	 * @param isObject <tt>true</tt> if reaching any tile adjacent to the destination should be accepted.
	 * @return <tt>true</tt> if reaching any tile adjacent to the destination should be accepted.
	 */
	public int pathLengthTo(final RSTile dest, final boolean isObject) {
		final RSTile curPos = methods.players.getMyPlayer().getLocation();
		return pathLengthBetween(curPos, dest, isObject);
	}

	/**
	 * Returns the length of the path generates between two RSTiles.
	 *
	 * @param start    The starting tile.
	 * @param dest     The destination tile.
	 * @param isObject <tt>true</tt> if reaching any tile adjacent to the destination should be accepted.
	 * @return <tt>true</tt> if reaching any tile adjacent to the destination should be accepted.
	 */
	public int pathLengthBetween(final RSTile start, final RSTile dest, final boolean isObject) {
		return dijkstraDist(start.getX() - methods.client.getBaseX(), // startX
				start.getY() - methods.client.getBaseY(), // startY
				dest.getX() - methods.client.getBaseX(), // destX
				dest.getY() - methods.client.getBaseY(), // destY
				isObject); // if it's an object, accept any adjacent tile
	}

	/**
	 * Checks whether a point is within the rectangle that determines the bounds
	 * of game screen. This will work fine when in fixed mode. In resizable mode
	 * it will exclude any points that are less than 253 pixels from the right
	 * of the screen or less than 169 pixels from the bottom of the screen,
	 * giving a rough area.
	 *
	 * @param check The point to check.
	 * @return <tt>true</tt> if the point is within the rectangle; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean pointOnScreen(final Point check) {
		final int x = check.x, y = check.y;
		if (methods.game.isFixed()) {
			return x > 4 && x < methods.game.getWidth() - 253 && y > 4 && y < methods.game.getHeight() - 169;
		} else {
			return x > 0 && x < methods.game.getWidth() - 260 && y > 0 && y < methods.game.getHeight() - 149;
		}
	}

	/**
	 * Returns a random double in a specified range
	 *
	 * @param min Minimum value (inclusive).
	 * @param max Maximum value (exclusive).
	 * @return The random <code>double</code> generated.
	 */
	@Override
	public double random(final double min, final double max) {
		return Math.min(min, max) + methods.random.nextDouble() * Math.abs(max - min);
	}

	/**
	 * Returns the height of the ground at the given location in the game world.
	 *
	 * @param x x value based on the game plane.
	 * @param y y value based on the game plane.
	 * @return The ground height at the given location; otherwise <code>0</code>.
	 */
	public int tileHeight(final int x, final int y) {
		int p = methods.client.getPlane();
		final int x1 = x >> 9;
		final int y1 = y >> 9;
		final byte[][][] settings = methods.client.getGroundByteArray();
		if (settings != null && x1 >= 0 && x1 < 104 && y1 >= 0 && y1 < 104) {
			if (p <= 3 && (settings[1][x1][y1] & 2) != 0) {
				++p;
			}
			final TileData[] planes = methods.client.getTileData();
			if (planes != null && p < planes.length && planes[p] != null) {
				final int[][] heights = planes[p].getHeights();
				if (heights != null) {
					final int x2 = x & 512 - 1;
					final int y2 = y & 512 - 1;
					final int start_h = heights[x1][y1] * (512 - x2) + heights[x1 + 1][y1] * x2 >> 9;
					final int end_h = heights[x1][1 + y1] * (512 - x2) + heights[x1 + 1][y1 + 1] * x2 >> 9;
					return start_h * (512 - y2) + end_h * y2 >> 9;
				}
			}
		}
		return 0;
	}

	/**
	 * Checks whether or not a given tile is on the minimap.
	 *
	 * @param t The RSTile to check.
	 * @return <tt>true</tt> if the RSTile is on the minimap; otherwise <tt>false</tt>.
	 * @see #tileToMinimap(RSTile)
	 */
	public boolean tileOnMap(final RSTile t) {
		return distanceTo(t) < 15;
	}

	/**
	 * Checks whether or not the centroid of a given tile is on the screen.
	 *
	 * @param t The RSTile to check.
	 * @return <tt>true</tt> if the RSTile is on the screen; otherwise <tt>false</tt>.
	 */
	public boolean tileOnScreen(final RSTile t) {
		return pointOnScreen(tileToScreen(t, 0.5, 0.5, 0));
	}

	/**
	 * Returns the Point on screen where a given tile is shown on the minimap.
	 *
	 * @param t The RSTile to check.
	 * @return <tt>Point</tt> within minimap; otherwise <tt>new Point(-1, -1)</tt>.
	 */
	public Point tileToMinimap(final RSTile t) {
		return worldToMinimap(t.getX(), t.getY());
	}

	/**
	 * Returns the screen location of a Tile with given 3D x, y and height
	 * offset values.
	 *
	 * @param tile   RSTile for which the screen location should be calculated.
	 * @param dX     Distance from bottom left of the tile to bottom right. Ranges from 0-1;
	 * @param dY     Distance from bottom left of the tile to top left. Ranges from 0-1;
	 * @param height Height offset (normal to the ground) to return the <code>Point</code> at.
	 * @return <code>Point</code> based on position on the game plane; otherwise <code>new Point(-1, -1)</code>.
	 */
	public Point tileToScreen(final RSTile tile, final double dX, final double dY, final int height) {
		return groundToScreen((int) ((tile.getX() - methods.client.getBaseX() + dX) * 512),
				(int) ((tile.getY() - methods.client.getBaseY() + dY) * 512), height);
	}

	/**
	 * Returns the screen location of a Tile with a given 3D height offset.
	 *
	 * @param tile   RSTile for which the screen location should be calculated.
	 * @param height Height offset (normal to the ground) to return the <code>Point</code> at.
	 * @return <code>Point</code> based on position on the game plane; if null <code>new Point(-1, -1)</code>.
	 * @see #tileToScreen(RSTile, double, double, int)
	 */
	public Point tileToScreen(final RSTile tile, final int height) {
		return tileToScreen(tile, 0.5, 0.5, height);
	}

	/**
	 * Returns the screen location of the south-west corner of the given tile.
	 *
	 * @param tile RSTile for which the screen location should be calculated.
	 * @return Center <code>Point</code> of the RSTile at a height of 0; if null <code>new Point(-1, -1)</code>.
	 * @see #tileToScreen(RSTile, int)
	 */
	public Point tileToScreen(final RSTile tile) {
		return tileToScreen(tile, 0);
	}

	/**
	 * Updates the rendering data. For internal use only.
	 *
	 * @param r  The client graphics toolkit.
	 * @param rd The client viewport.
	 */
	public void updateRenderInfo(final org.rsbot.client.Render r, final org.rsbot.client.RenderData rd) {
		if (r == null || rd == null) {
			return;
		}
		render.absoluteX1 = r.getAbsoluteX1();
		render.absoluteX2 = r.getAbsoluteX2();
		render.absoluteY1 = r.getAbsoluteY1();
		render.absoluteY2 = r.getAbsoluteY2();
		render.xMultiplier = r.getXMultiplier();
		render.yMultiplier = r.getYMultiplier();
		render.zNear = r.getZNear();
		render.zFar = r.getZFar();
		renderData.xOff = rd.getXOff();
		renderData.xX = rd.getXX();
		renderData.xY = rd.getXY();
		renderData.xZ = rd.getXZ();
		renderData.yOff = rd.getYOff();
		renderData.yX = rd.getYX();
		renderData.yY = rd.getYY();
		renderData.yZ = rd.getYZ();
		renderData.zOff = rd.getZOff();
		renderData.zX = rd.getZX();
		renderData.zY = rd.getZY();
		renderData.zZ = rd.getZZ();
	}

	/**
	 * Returns the screen Point of given absolute x and y values in the game's
	 * 3D plane.
	 *
	 * @param x x value based on the game plane.
	 * @param y y value based on the game plane.
	 * @return <code>Point</code> within minimap; otherwise <tt>new Point(-1, -1)</tt>.
	 */
	public Point worldToMinimap(double x, double y) {
		if (distanceBetween(methods.players.getMyPlayer().getLocation(), new RSTile((int) x, (int) y)) > 17) {
			return new Point(-1, -1);
		}
		x -= methods.client.getBaseX();
		y -= methods.client.getBaseY();
		final int calculatedX = (int) (x * 4 + 2) - methods.client.getMyRSPlayer().getX() / 128;
		final int calculatedY = (int) (y * 4 + 2) - methods.client.getMyRSPlayer().getY() / 128;
		try {
			final org.rsbot.client.RSInterface mm = methods.gui.getMinimapInterface();
			if (mm == null) {
				return new Point(-1, -1);
			}
			final RSComponent mm2 = methods.interfaces.getComponent(mm.getID());
			final int actDistSq = calculatedX * calculatedX + calculatedY * calculatedY;
			final int mmDist = 10 + Math.max(mm2.getWidth() / 2, mm2.getHeight() / 2);
			if (mmDist * mmDist >= actDistSq) {
				int angle = 0x3fff & (int) methods.client.getMinimapAngle();
				if (methods.client.getMinimapSetting() != 4) {
					angle = 0x3fff & methods.client.getMinimapOffset() + (int) methods.client.getMinimapAngle();
				}
				int cs = Calculations.SIN_TABLE[angle];
				int cc = Calculations.COS_TABLE[angle];
				if (methods.client.getMinimapSetting() != 4) {
					final int fact = 256 + methods.client.getMinimapScale();
					cs = 256 * cs / fact;
					cc = 256 * cc / fact;
				}
				final int calcCenterX = cc * calculatedX + cs * calculatedY >> 15;
				final int calcCenterY = cc * calculatedY - cs * calculatedX >> 15;
				final int screenx = calcCenterX + mm2.getAbsoluteX() + mm2.getWidth() / 2;
				final int screeny = -calcCenterY + mm2.getAbsoluteY() + mm2.getHeight() / 2;
				return new Point(screenx, screeny);
			}
		} catch (final NullPointerException ignored) {
		}

		return new Point(-1, -1);
	}

	/**
	 * Returns the screen location of a given 3D point in the game world.
	 *
	 * @param x x value on the game plane.
	 * @param y y value on the game plane.
	 * @param z z value on the game plane.
	 * @return <code>Point</code> based on screen; otherwise <code>new Point(-1, -1)</code>.
	 */
	public Point worldToScreen(final int x, final int y, final int z) {
		// perspective projection: hooked viewport values are calculated in
		// client based on camera state
		// (so no need to project using camera values and sin/cos)
		// old developers named these fields very poorly
		final float _z = renderData.zOff + (int) (renderData.zX * x + renderData.zY * z + renderData.zZ * y);
		if (_z >= render.zNear && _z <= render.zFar) {
			final int _x = (int) (render.xMultiplier * ((int) renderData.xOff + (int) (renderData.xX * x + renderData.xY
					* z + renderData.xZ * y)) / _z);
			final int _y = (int) (render.yMultiplier * ((int) renderData.yOff + (int) (renderData.yX * x + renderData.yY
					* z + renderData.yZ * y)) / _z);
			if (_x >= render.absoluteX1 && _x <= render.absoluteX2 && _y >= render.absoluteY1 && _y <= render.absoluteY2) {
				if (methods.game.isFixed()) {
					return new Point((int) (_x - render.absoluteX1) + 4, (int) (_y - render.absoluteY1) + 4);
				} else {
					final int sx = (int) (_x - render.absoluteX1), sy = (int) (_y - render.absoluteY1);
					return new Point(sx, sy);
				}
			}
		}
		return new Point(-1, -1);
	}

	/**
	 * @param startX       the startX (0 < startX < 104)
	 * @param startY       the startY (0 < startY < 104)
	 * @param destX        the destX (0 < destX < 104)
	 * @param destY        the destY (0 < destY < 104)
	 * @param findAdjacent if it's an object, it will find path which touches it.
	 * @return The distance of the shortest path to the destination; or -1 if no valid path to the destination was found.
	 */
	private int dijkstraDist(final int startX, final int startY, final int destX, final int destY, final boolean findAdjacent) {
		try {
			final int[][] prev = new int[104][104];
			final int[][] dist = new int[104][104];
			final int[] path_x = new int[4000];
			final int[] path_y = new int[4000];
			for (int xx = 0; xx < 104; xx++) {
				for (int yy = 0; yy < 104; yy++) {
					prev[xx][yy] = 0;
					dist[xx][yy] = 99999999;
				}
			}
			int curr_x = startX;
			int curr_y = startY;
			prev[startX][startY] = 99;
			dist[startX][startY] = 0;
			int path_ptr = 0;
			int step_ptr = 0;
			path_x[path_ptr] = startX;
			path_y[path_ptr++] = startY;
			final int blocks[][] = methods.client.getRSGroundDataArray()[methods.game.getPlane()].getBlocks();
			final int pathLength = path_x.length;
			boolean foundPath = false;
			while (step_ptr != path_ptr) {
				curr_x = path_x[step_ptr];
				curr_y = path_y[step_ptr];
				if (Math.abs(curr_x - destX) + Math.abs(curr_y - destY) == (findAdjacent ? 1 : 0)) {
					foundPath = true;
					break;
				}
				step_ptr = (step_ptr + 1) % pathLength;
				final int cost = dist[curr_x][curr_y] + 1;
				// south
				if (curr_y > 0 && prev[curr_x][curr_y - 1] == 0 && (blocks[curr_x + 1][curr_y] & 0x1280102) == 0) {
					path_x[path_ptr] = curr_x;
					path_y[path_ptr] = curr_y - 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x][curr_y - 1] = 1;
					dist[curr_x][curr_y - 1] = cost;
				}
				// west
				if (curr_x > 0 && prev[curr_x - 1][curr_y] == 0 && (blocks[curr_x][curr_y + 1] & 0x1280108) == 0) {
					path_x[path_ptr] = curr_x - 1;
					path_y[path_ptr] = curr_y;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x - 1][curr_y] = 2;
					dist[curr_x - 1][curr_y] = cost;
				}
				// north
				if (curr_y < 104 - 1 && prev[curr_x][curr_y + 1] == 0 && (blocks[curr_x + 1][curr_y + 2] &
						0x1280120) == 0) {
					path_x[path_ptr] = curr_x;
					path_y[path_ptr] = curr_y + 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x][curr_y + 1] = 4;
					dist[curr_x][curr_y + 1] = cost;
				}
				// east
				if (curr_x < 104 - 1 && prev[curr_x + 1][curr_y] == 0 && (blocks[curr_x + 2][curr_y + 1] &
						0x1280180) == 0) {
					path_x[path_ptr] = curr_x + 1;
					path_y[path_ptr] = curr_y;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x + 1][curr_y] = 8;
					dist[curr_x + 1][curr_y] = cost;
				}
				// south west
				if (curr_x > 0 && curr_y > 0 && prev[curr_x - 1][curr_y - 1] == 0 && (blocks[curr_x][curr_y] &
						0x128010e) == 0 && (blocks[curr_x][curr_y + 1] & 0x1280108) == 0 && (blocks[curr_x +
						1][curr_y] & 0x1280102) == 0) {
					path_x[path_ptr] = curr_x - 1;
					path_y[path_ptr] = curr_y - 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x - 1][curr_y - 1] = 3;
					dist[curr_x - 1][curr_y - 1] = cost;
				}
				// north west
				if (curr_x > 0 && curr_y < 104 - 1 && prev[curr_x - 1][curr_y + 1] == 0 && (blocks[curr_x][curr_y + 2] & 0x1280138) == 0 && (blocks[curr_x][curr_y + 1] & 0x1280108) ==
						0 && (blocks[curr_x + 1][curr_y + 2] & 0x1280120) == 0) {
					path_x[path_ptr] = curr_x - 1;
					path_y[path_ptr] = curr_y + 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x - 1][curr_y + 1] = 6;
					dist[curr_x - 1][curr_y + 1] = cost;
				}
				// south east
				if (curr_x < 104 - 1 && curr_y > 0 && prev[curr_x + 1][curr_y - 1] == 0 && (blocks[curr_x +
						2][curr_y] & 0x1280183) == 0 && (blocks[curr_x + 2][curr_y + 1] & 0x1280180) == 0 && (blocks[curr_x + 1][curr_y] & 0x1280102) == 0) {
					path_x[path_ptr] = curr_x + 1;
					path_y[path_ptr] = curr_y - 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x + 1][curr_y - 1] = 9;
					dist[curr_x + 1][curr_y - 1] = cost;
				}
				// north east
				if (curr_x < 104 - 1 && curr_y < 104 - 1 && prev[curr_x + 1][curr_y + 1] == 0 && (blocks[curr_x
						+ 2][curr_y + 2] & 0x12801e0) == 0 && (blocks[curr_x + 2][curr_y + 1] & 0x1280180) == 0 && (blocks[curr_x + 1][curr_y + 2] & 0x1280120) == 0) {
					path_x[path_ptr] = curr_x + 1;
					path_y[path_ptr] = curr_y + 1;
					path_ptr = (path_ptr + 1) % pathLength;
					prev[curr_x + 1][curr_y + 1] = 12;
					dist[curr_x + 1][curr_y + 1] = cost;
				}
			}
			return foundPath ? dist[curr_x][curr_y] : -1;
		} catch (Exception e) {
			return -1;
		}
	}
}
