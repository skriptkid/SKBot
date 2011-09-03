package org.rsbot.script.web;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;
import org.rsbot.script.wrappers.RSTile;

/**
 * Teleportation base information.
 *
 * @author Timer
 */
public abstract class Teleport extends MethodProvider implements Transportation {
	public final RSTile teleportationLocation;

	public Teleport(final MethodContext ctx, final RSTile teleportationLocation) {
		super(ctx);
		this.teleportationLocation = teleportationLocation;
	}

	public RSTile teleportationLocation() {
		return teleportationLocation;
	}
}