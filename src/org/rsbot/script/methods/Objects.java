package org.rsbot.script.methods;

import org.rsbot.client.RSAnimableNode;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides access to in-game physical objects.
 */
public class Objects extends MethodProvider {
	public static final int TYPE_INTERACTABLE = 1;
	public static final int TYPE_FLOOR_DECORATION = 2;
	public static final int TYPE_BOUNDARY = 4;
	public static final int TYPE_WALL_DECORATION = 8;

	/**
	 * A filter that accepts all matches.
	 */
	public static final Filter<RSObject> ALL_FILTER = new Filter<RSObject>() {
		public boolean accept(final RSObject obj) {
			return true;
		}
	};

	Objects(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region.
	 *
	 * @return An <tt>RSObject[]</tt> of all objects in the loaded region.
	 */
	public RSObject[] getAll() {
		return getAll(Objects.ALL_FILTER);
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region within specified distance.
	 *
	 * @param distance The range to search (box-like).
	 * @return An <tt>RSObject[]</tt> of all objects in the loaded region within specified range.
	 */
	public RSObject[] getAll(final int distance) {
		final Set<RSObject> objects = new LinkedHashSet<RSObject>();
		final RSTile currTile = methods.players.getMyPlayer().getLocation(), baseTile = methods.game.getMapBase();
		final int sX = Math.max(0, currTile.getX() - baseTile.getX() - distance);
		final int sY = Math.max(0, currTile.getY() - baseTile.getY() - distance);
		final int eX = Math.min(104, currTile.getX() - baseTile.getX() + distance);
		final int eY = Math.min(104, currTile.getY() - baseTile.getY() + distance);
		for (int x = sX; x < eX; x++) {
			for (int y = sY; y < eY; y++) {
				for (final RSObject o : getAtLocal(x, y, -1)) {
					if (o != null) {
						objects.add(o);
					}
				}
			}
		}
		return objects.toArray(new RSObject[objects.size()]);
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region within specified distance.
	 *
	 * @param distance The range to search (box-like).
	 * @param filter   Filters out unwanted objects.
	 * @return An <tt>RSObject[]</tt> of all objects in the loaded region within specified range.
	 */
	public RSObject[] getAll(final int distance, final Filter<RSObject> filter) {
		final Set<RSObject> objects = new LinkedHashSet<RSObject>();
		final RSTile currTile = methods.players.getMyPlayer().getLocation(), baseTile = methods.game.getMapBase();
		final int sX = Math.max(0, currTile.getX() - baseTile.getX() - distance);
		final int sY = Math.max(0, currTile.getY() - baseTile.getY() - distance);
		final int eX = Math.min(104, currTile.getX() - baseTile.getX() + distance);
		final int eY = Math.min(104, currTile.getY() - baseTile.getY() + distance);
		for (int x = sX; x < eX; x++) {
			for (int y = sY; y < eY; y++) {
				for (final RSObject o : getAtLocal(x, y, -1)) {
					if (o != null && filter.accept(o)) {
						objects.add(o);
					}
				}
			}
		}
		return objects.toArray(new RSObject[objects.size()]);
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region accepted by the
	 * provided Filter.
	 *
	 * @param filter Filters out unwanted objects.
	 * @return An <tt>RSObject[]</tt> of all the accepted objects in the loaded
	 *         region.
	 */
	public RSObject[] getAll(final Filter<RSObject> filter) {
		final Set<RSObject> objects = new LinkedHashSet<RSObject>();
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				for (final RSObject o : getAtLocal(x, y, -1)) {
					if (o != null && filter.accept(o)) {
						objects.add(o);
					}
				}
			}
		}
		return objects.toArray(new RSObject[objects.size()]);
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region with the provided
	 * ID(s).
	 *
	 * @param ids Allowed object IDs.
	 * @return An array of the region's RSObjects matching the provided ID(s).
	 */
	public RSObject[] getAll(final int... ids) {
		return getAll(new Filter<RSObject>() {
			public boolean accept(final RSObject o) {
				for (final int id : ids) {
					if (o.getID() == id) {
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Returns all the <tt>RSObject</tt>s in the local region with the provided
	 * name(s).
	 *
	 * @param names Allowed object names.
	 * @return An array of the region's RSObjects matching the provided name(s).
	 */
	public RSObject[] getAll(final String... names) {
		return getAll(new Filter<RSObject>() {
			public boolean accept(final RSObject o) {
				final String name = o.getName();
				if (!name.isEmpty()) {
					for (final String n : names) {
						if (n != null && n.equalsIgnoreCase(name)) {
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	/**
	 * Returns the <tt>RSObject</tt>s which are on the specified <tt>RSTile</tt>.
	 *
	 * @param t The tile on which to search.
	 * @return An RSObject[] of the objects on the specified tile.
	 */
	public RSObject[] getAllAt(final RSTile t) {
		return getAt(t, -1);
	}

	/**
	 * Returns the <tt>RSObject</tt>s which are on the specified <tt>RSTile</tt>
	 * matching types specified by the flags in the provided mask.
	 *
	 * @param t    The tile on which to search.
	 * @param mask The type flags.
	 * @return An RSObject[] of the objects on the specified tile.
	 */
	public RSObject[] getAt(final RSTile t, final int mask) {
		final Set<RSObject> objects = getAtLocal(t.getX() - methods.client.getBaseX(), t.getY() - methods.client.getBaseY(), mask);
		return objects.toArray(new RSObject[objects.size()]);
	}

	private Set<RSObject> getAtLocal(int x, int y, final int mask) {
		final org.rsbot.client.Client client = methods.client;
		final Set<RSObject> objects = new LinkedHashSet<RSObject>();
		if (client.getRSGroundArray() == null) {
			return objects;
		}

		try {
			final int plane = client.getPlane();
			final org.rsbot.client.RSGround rsGround = client.getRSGroundArray()[plane][x][y];

			if (rsGround != null) {
				org.rsbot.client.RSObject rsObj;
				org.rsbot.client.RSInteractable obj;

				// Interactable (e.g. Trees)
				if ((mask & TYPE_INTERACTABLE) != 0) {
					for (RSAnimableNode node = rsGround.getRSAnimableList(); node != null; node = node.getNext()) {
						obj = node.getRSAnimable();
						if (obj != null
								&& obj instanceof org.rsbot.client.RSObject) {
							rsObj = (org.rsbot.client.RSObject) obj;
							if (rsObj.getID() != -1) {
								objects.add(new RSObject(methods, rsObj, RSObject.Type.INTERACTABLE, plane));
							}
						}
					}
				}

				// Ground Decorations
				if ((mask & TYPE_FLOOR_DECORATION) != 0) {
					obj = rsGround.getFloorDecoration();
					if (obj != null) {
						rsObj = (org.rsbot.client.RSObject) obj;
						if (rsObj.getID() != -1) {
							objects.add(new RSObject(methods, rsObj, RSObject.Type.FLOOR_DECORATION, plane));
						}
					}
				}

				// Boundaries / Doors / Fences / Walls
				if ((mask & TYPE_BOUNDARY) != 0) {
					obj = rsGround.getBoundary1();
					if (obj != null) {
						rsObj = (org.rsbot.client.RSObject) obj;
						if (rsObj.getID() != -1) {
							objects.add(new RSObject(methods, rsObj, RSObject.Type.BOUNDARY, plane));
						}
					}

					obj = rsGround.getBoundary2();
					if (obj != null) {
						rsObj = (org.rsbot.client.RSObject) obj;
						if (rsObj.getID() != -1) {
							objects.add(new RSObject(methods, rsObj, RSObject.Type.BOUNDARY, plane));
						}
					}
				}

				// Wall Decorations
				if ((mask & TYPE_WALL_DECORATION) != 0) {
					obj = rsGround.getWallDecoration1();
					if (obj != null) {
						rsObj = (org.rsbot.client.RSObject) obj;
						if (rsObj.getID() != -1) {
							objects.add(new RSObject(methods, rsObj, RSObject.Type.WALL_DECORATION, plane));
						}
					}

					obj = rsGround.getWallDecoration2();
					if (obj != null) {
						rsObj = (org.rsbot.client.RSObject) obj;
						if (rsObj.getID() != -1) {
							objects.add(new RSObject(methods, rsObj, RSObject.Type.WALL_DECORATION, plane));
						}
					}
				}
			}
		} catch (final Exception ignored) {
		}
		return objects;
	}

	/**
	 * Returns the <tt>RSObject</tt> that is nearest out of all objects that are
	 * accepted by the provided Filter.
	 *
	 * @param filter Filters out unwanted objects.
	 * @return An <tt>RSObject</tt> representing the nearest object that was
	 *         accepted by the filter; or null if there are no matching objects
	 *         in the current region.
	 */
	public RSObject getNearest(final Filter<RSObject> filter) {
		RSObject cur = null;
		double dist = -1;
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				final Set<RSObject> objs = getAtLocal(x, y, -1);
				for (final RSObject o : objs) {
					if (o != null && filter.accept(o)) {
						final double distTmp = methods.calc.distanceBetween(methods.players.getMyPlayer().getLocation(), o.getLocation());
						if (cur == null) {
							dist = distTmp;
							cur = o;
						} else if (distTmp < dist) {
							cur = o;
							dist = distTmp;
						}
						break;
					}
				}
			}
		}
		return cur;
	}

	/**
	 * Returns the <tt>RSObject</tt> that is nearest, out of all of the
	 * RSObjects with the provided ID(s).
	 *
	 * @param ids The ID(s) of the RSObject that you are searching.
	 * @return An <tt>RSObject</tt> representing the nearest object with one of
	 *         the provided IDs; or null if there are no matching objects in the
	 *         current region.
	 */
	public RSObject getNearest(final int... ids) {
		return getNearest(new Filter<RSObject>() {
			public boolean accept(final RSObject o) {
				for (final int id : ids) {
					if (o.getID() == id) {
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Returns the <tt>RSObject</tt> that is nearest, out of all of the
	 * RSObjects with the provided name(s).
	 *
	 * @param names The name(s) of the RSObject that you are searching.
	 * @return An <tt>RSObject</tt> representing the nearest object with one of
	 *         the provided names; or null if there are no matching objects in
	 *         the current region.
	 */
	public RSObject getNearest(final String... names) {
		return getNearest(new Filter<RSObject>() {
			public boolean accept(final RSObject o) {
				final String name = o.getName();
				if (!name.isEmpty()) {
					for (final String n : names) {
						if (n != null && n.equalsIgnoreCase(name)) {
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	/**
	 * Returns the top <tt>RSObject</tt> on the specified tile.
	 *
	 * @param t The tile on which to search.
	 * @return The top RSObject on the provided tile; or null if none found.
	 */
	public RSObject getTopAt(final RSTile t) {
		return getTopAt(t, -1);
	}

	/**
	 * Returns the top <tt>RSObject</tt> on the specified tile matching types
	 * specified by the flags in the provided mask.
	 *
	 * @param t    The tile on which to search.
	 * @param mask The type flags.
	 * @return The top RSObject on the provided tile matching the specified
	 *         flags; or null if none found.
	 */
	public RSObject getTopAt(final RSTile t, final int mask) {
		final RSObject[] objects = getAt(t, mask);
		return objects.length > 0 ? objects[0] : null;
	}
}
