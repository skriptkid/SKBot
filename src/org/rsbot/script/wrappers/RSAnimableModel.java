package org.rsbot.script.wrappers;

import org.rsbot.client.Model;
import org.rsbot.client.RSAnimable;
import org.rsbot.script.methods.MethodContext;

/**
 */
class RSAnimableModel extends RSModel {
	private final RSAnimable animable;

	RSAnimableModel(final MethodContext ctx, final Model model, final RSAnimable animable) {
		super(ctx, model);
		this.animable = animable;
	}

	@Override
	protected void update() {

	}

	@Override
	protected int getLocalX() {
		return animable.getX();
	}

	@Override
	protected int getLocalY() {
		return animable.getY();
	}
}
