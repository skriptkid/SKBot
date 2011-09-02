package org.rsbot.script.wrappers;

import org.rsbot.client.HardReference;
import org.rsbot.client.SoftReference;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.awt.*;

/**
 * Represents an item (with an id and stack size). May or may not
 * wrap a component.
 */
public class RSItem extends MethodProvider implements RSTarget {
	private static final Point M1_POINT = new Point(-1, -1);

	private final int id;
	private final int stack;
	private RSComponent component;

	public RSItem(final MethodContext ctx, final int id, final int stack) {
		super(ctx);
		this.id = id;
		this.stack = stack;
	}

	public RSItem(final MethodContext ctx, final org.rsbot.client.RSItem item) {
		super(ctx);
		id = item.getID();
		stack = item.getStackSize();
	}

	public RSItem(final MethodContext ctx, final RSComponent item) {
		super(ctx);
		id = item.getComponentID();
		stack = item.getComponentStackSize();
		component = item;
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean interact(final String action) {
		return interact(action, null);
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 * @see org.rsbot.script.wrappers.RSItem#interact(String)
	 */
	@Deprecated
	public boolean doAction(final String action) {
		return interact(action);
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @param option The option of the action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean interact(final String action, final String option) {
		return component != null && component.interact(action, option);
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @param option The option of the action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 * @see org.rsbot.script.wrappers.RSItem#interact(String, String)
	 */
	@Deprecated
	public boolean doAction(final String action, final String option) {
		return interact(action, option);
	}

	/**
	 * Clicks the component wrapped by this RSItem if possible.
	 *
	 * @param left <tt>true</tt> if the component should be
	 *             left-click; <tt>false</tt> if it should be right-clicked.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean doClick(final boolean left) {
		return component != null && component.doClick(left);
	}

	/**
	 * Gets the component wrapped by this RSItem.
	 *
	 * @return The wrapped component or <code>null</code>.
	 */
	public RSComponent getComponent() {
		return component;
	}

	/**
	 * Gets this item's definition if available.
	 *
	 * @return The RSItemDef; or <code>null</code> if unavailable.
	 */
	public RSItemDef getDefinition() {
		try {
			final org.rsbot.client.Node ref = methods.nodes.lookup(methods.client.getRSItemDefLoader(), id);
			if (ref != null) {
				if (ref instanceof HardReference) {
					return new RSItemDef((org.rsbot.client.RSItemDef) ((HardReference) ref).get());
				} else if (ref instanceof SoftReference) {
					final Object def = ((SoftReference) ref).getReference().get();

					if (def != null) {
						return new RSItemDef((org.rsbot.client.RSItemDef) def);
					}
				}
			}
			return null;
		} catch (final ClassCastException e) {
			return null;
		}
	}

	/**
	 * Gets this item's id.
	 *
	 * @return The id.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the name of this item using the wrapped component's name
	 * if available, otherwise the definition if available.
	 *
	 * @return The item's name or <code>null</code> if not found.
	 */
	public String getName() {
		if (component != null) {
			return component.getComponentName().replaceAll("\\<.*?>", "");
		} else {
			final RSItemDef definition = getDefinition();
			if (definition != null) {
				return definition.getName().replaceAll("\\<.*?>", "");
			}
		}
		return null;
	}

	/**
	 * Gets this item's stack size.
	 *
	 * @return The stack size.
	 */
	public int getStackSize() {
		return stack;
	}

	/**
	 * Determines if this item contains the desired action
	 *
	 * @param action The item menu action to check.
	 * @return <tt>true</tt> if the item has the action; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean hasAction(final String action) {
		final RSItemDef itemDef = getDefinition();
		if (itemDef != null) {
			for (final String a : itemDef.getActions()) {
				if (a != null && a.equalsIgnoreCase(action)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether or not this item has an available definition.
	 *
	 * @return <tt>true</tt> if an item definition is available;
	 *         otherwise <tt>false</tt>.
	 */
	public boolean hasDefinition() {
		return getDefinition() != null;
	}

	/**
	 * Checks whether or not a valid component is being wrapped.
	 *
	 * @return <tt>true</tt> if there is a visible wrapped component.
	 */
	public boolean isComponentValid() {
		return component != null && component.isValid();
	}

	public Point getPoint() {
		return component != null ? component.getPoint() : M1_POINT;
	}

	public boolean contains(int x, int y) {
		return component != null && component.contains(x, y);
	}
}