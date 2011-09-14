package org.rsbot.script.wrappers;

import java.awt.*;
import java.util.ArrayList;

/**
 * Represents a shape made of RSTiles.
 *
 * @author SpeedWing, Emeleo
 */
public class RSArea {
	private final Polygon area;
	private final int plane;
	private final RSTile[] tiles;

	/**
	 * @param tiles An Array containing of <b>RSTiles</b> forming a polygon shape.
	 * @param plane The plane of the <b>RSArea</b>.
	 */
	public RSArea(final RSTile[] tiles, final int plane) {
		this.tiles = tiles;
		area = tileArrayToPolygon(this.tiles);
		this.plane = plane;
	}

	/**
	 * @param tiles An Array containing of <b>RSTiles</b> forming a polygon shape.
	 */
	public RSArea(final RSTile[] tiles) {
		this(tiles, 0);
	}

	/**
	 * @param sw    The <i>South West</i> <b>RSTile</b> of the <b>RSArea</b>
	 * @param ne    The <i>North East</i> <b>RSTile</b> of the <b>RSArea</b>
	 * @param plane The plane of the <b>RSArea</b>.
	 */
	public RSArea(final RSTile sw, final RSTile ne, final int plane) {
		this(new RSTile[]{sw, new RSTile(ne.getX() + 1, sw.getY()),
				new RSTile(ne.getX() + 1, ne.getY() + 1),
				new RSTile(sw.getX(), ne.getY() + 1)}, plane);
	}

	/**
	 * @param sw The <i>South West</i> <b>RSTile</b> of the <b>RSArea</b>
	 * @param ne The <i>North East</i> <b>RSTile</b> of the <b>RSArea</b>
	 */
	public RSArea(final RSTile sw, final RSTile ne) {
		this(sw, ne, 0);
	}

	/**
	 * @param swX The X axle of the <i>South West</i> <b>RSTile</b> of the
	 *            <b>RSArea</b>
	 * @param swY The Y axle of the <i>South West</i> <b>RSTile</b> of the
	 *            <b>RSArea</b>
	 * @param neX The X axle of the <i>North East</i> <b>RSTile</b> of the
	 *            <b>RSArea</b>
	 * @param neY The Y axle of the <i>North East</i> <b>RSTile</b> of the
	 *            <b>RSArea</b>
	 */
	public RSArea(final int swX, final int swY, final int neX, final int neY) {
		this(new RSTile(swX, swY), new RSTile(neX, neY), 0);
	}

	/**
	 * @param x The x location of the <b>RSTile</b> that will be checked.
	 * @param y The y location of the <b>RSTile</b> that will be checked.
	 * @return True if the <b>RSArea</b> contains the given <b>RSTile</b>.
	 */
	public boolean contains(final int x, final int y) {
		return this.contains(new RSTile(x, y));
	}

	/**
	 * @param plane The plane to check.
	 * @param tiles The <b>RSTile(s)</b> that will be checked.
	 * @return True if the <b>RSArea</b> contains the given <b>RSTile(s)</b>.
	 */
	public boolean contains(final int plane, final RSTile... tiles) {
		return this.plane == plane && this.contains(tiles);
	}

	/**
	 * @param tiles The <b>RSTile(s)</b> that will be checked.
	 * @return True if the <b>RSArea</b> contains the given <b>RSTile(s)</b>.
	 */
	public boolean contains(final RSTile... tiles) {
		final RSTile[] areaTiles = getTileArray();
		for (final RSTile check : tiles) {
			for (final RSTile space : areaTiles) {
				if (check.equals(space)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return The bounding box of the <b>RSArea</b>.
	 */
	public Rectangle getBounds() {
		return new Rectangle(area.getBounds().x + 1, area.getBounds().y + 1, getWidth(), getHeight());
	}

	/**
	 * @return The central <b>RSTile</b> of the <b>RSArea</b>.
	 */
	public RSTile getCentralTile() {
		if (area.npoints < 1) {
			return null;
		}
		int totalX = 0, totalY = 0;
		for (int i = 0; i < area.npoints; i++) {
			totalX += area.xpoints[i];
			totalY += area.ypoints[i];
		}
		return new RSTile(Math.round(totalX / area.npoints), Math.round(totalY / area.npoints));
	}

	/**
	 * @param base The base tile to measure the closest tile off of.
	 * @return The nearest <b>RSTile</b> in the <b>RSArea</b>
	 *         to the given <b>RSTile</b>.
	 */
	public RSTile getNearestTile(final RSTile base) {
		RSTile currTile = null;
		for (final RSTile tile : getTileArray()) {
			if (currTile == null || distanceBetween(base, tile)
					< distanceBetween(currTile, tile)) {
				currTile = tile;
			}
		}
		return currTile;
	}

	/**
	 * @return The plane of the <b>RSArea</b>.
	 */
	public int getPlane() {
		return plane;
	}

	/**
	 * @return The <b>RSTiles</b> the <b>RSArea</b> contains.
	 */
	public RSTile[] getTileArray() {
		final ArrayList<RSTile> list = new ArrayList<RSTile>();
		for (int x = getX(); x <= getX() + getWidth(); x++) {
			for (int y = getY(); y <= getY() + getHeight(); y++) {
				if (area.contains(x, y)) {
					list.add(new RSTile(x, y, plane));
				}
			}
		}
		return list.toArray(new RSTile[list.size()]);
	}

	/**
	 * @return The <b>RSTiles</b> the <b>RSArea</b> contains.
	 */
	public RSTile[][] getTiles() {
		final RSTile[][] tiles = new RSTile[getWidth()][getHeight()];
		for (int i = 0; i < getWidth(); ++i) {
			for (int j = 0; j < getHeight(); ++j) {
				if (area.contains(getX() + i, getY() + j)) {
					tiles[i][j] = new RSTile(getX() + i, getY() + j);
				}
			}
		}
		return tiles;
	}

	/**
	 * @return The distance between the the <b>RSTile</b> that's most
	 *         <i>East</i> and the <b>RSTile</b> that's most <i>West</i>.
	 */
	public int getWidth() {
		return area.getBounds().width;
	}

	/**
	 * @return The distance between the the <b>RSTile</b> that's most
	 *         <i>South</i> and the <b>RSTile</b> that's most <i>North</i>.
	 */
	public int getHeight() {
		return area.getBounds().height;
	}

	/**
	 * @return The X axle of the <b>RSTile</b> that's most <i>West</i>.
	 */
	public int getX() {
		return area.getBounds().x;
	}

	/**
	 * @return The Y axle of the <b>RSTile</b> that's most <i>South</i>.
	 */
	public int getY() {
		return area.getBounds().y;
	}

	/**
	 * @return An array of <b>RSTile<b>'s that make the area.
	 */
	public RSTile[] getAreaTiles() {
		return tiles;
	}

	/**
	 * Converts an shape made of <b>RSTile</b> to a polygon.
	 *
	 * @param tiles The <b>RSTile</b> of the Polygon.
	 * @return The Polygon of the <b>RSTile</b>.
	 */
	private Polygon tileArrayToPolygon(final RSTile[] tiles) {
		final Polygon poly = new Polygon();
		for (final RSTile t : tiles) {
			poly.addPoint(t.getX(), t.getY());
		}
		return poly;
	}

	/**
	 * @param curr first <b>RSTile</b>
	 * @param dest second <b>RSTile</b>
	 * @return the distance between the first and the second rstile
	 */
	private double distanceBetween(final RSTile curr, final RSTile dest) {
		return Math.sqrt((curr.getX() - dest.getX())
				* (curr.getX() - dest.getX()) + (curr.getY() - dest.getY())
				* (curr.getY() - dest.getY()));
	}
}