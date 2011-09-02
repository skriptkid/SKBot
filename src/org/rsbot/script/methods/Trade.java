package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Trade handling.
 *
 * @author Timer
 * @author kyleshay
 */
public class Trade extends MethodProvider {
	public static final int INTERFACE_TRADE_MAIN = 335;
	public static final int INTERFACE_TRADE_SECOND = 334;
	public static final int INTERFACE_TRADE_MAIN_NAME = 15;
	public static final int INTERFACE_TRADE_SECOND_NAME = 54;
	public static final int INTERFACE_TRADE_MAIN_OUR = 30;
	public static final int INTERFACE_TRADE_MAIN_THEIR = 33;
	public static final int INTERFACE_TRADE_MAIN_ACCEPT = 17;
	public static final int INTERFACE_TRADE_MAIN_DECLINE = 19;
	public static final int INTERFACE_TRADE_SECOND_ACCEPT = 36;
	public static final int INTERFACE_TRADE_SECOND_DECLINE = 37;
	public static final int INTERFACE_TRADE_OUR_AMOUNT = 43;
	public static final int INTERFACE_TRADE_THEIR_AMOUNT = 44;

	public final static int INTERFACE_TRADE_MAIN_INV_SLOTS = 21;

	public static final int TRADE_TYPE_MAIN = 0;
	public static final int TRADE_TYPE_SECONDARY = 1;
	public static final int TRADE_TYPE_NONE = 2;

	Trade(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Are we in the first stage of a trade?
	 *
	 * @return <tt>true</tt> if in first stage.
	 */
	public boolean inTradeMain() {
		final RSInterface tradeInterface = methods.interfaces.get(INTERFACE_TRADE_MAIN);
		return tradeInterface != null && tradeInterface.isValid();
	}

	/**
	 * Are we in the second stage of a trade?
	 *
	 * @return <tt>true</tt> if in second stage.
	 */
	public boolean inTradeSecond() {
		final RSInterface tradeInterface = methods.interfaces.get(INTERFACE_TRADE_SECOND);
		return tradeInterface != null && tradeInterface.isValid();
	}

	/**
	 * Checks if you're in a trade.
	 *
	 * @return <tt>true</tt> if you're trading; otherwise <tt>false</tt>.
	 */
	public boolean inTrade() {
		return inTradeMain() || inTradeSecond();
	}

	/**
	 * Trades a player.
	 *
	 * @param playerName The player's name.
	 * @param tradeWait  Timeout to wait for the trade.
	 * @return <tt>true</tt> if traded.
	 */
	public boolean tradePlayer(final String playerName, final int tradeWait) {
		if (!inTrade()) {
			final RSPlayer targetPlayer = methods.players.getNearest(playerName);
			return targetPlayer != null && targetPlayer.interact("Trade with", targetPlayer.getName()) && waitForTrade(TRADE_TYPE_MAIN, tradeWait);
		} else {
			return isTradingWith(playerName);
		}
	}

	/**
	 * Trades a player.
	 *
	 * @param playerName The player's name.
	 * @return <tt>true</tt> if traded.
	 */
	public boolean tradePlayer(final String playerName) {
		return tradePlayer(playerName, 15000);
	}

	/**
	 * Trades a player.
	 *
	 * @param targetPlayer The player you wish to trade.
	 * @param tradeWait    The time out for the trade.
	 * @return <tt>true</tt> if traded.
	 */
	public boolean tradePlayer(final RSPlayer targetPlayer, final int tradeWait) {
		if (!inTrade()) {
			return targetPlayer != null && targetPlayer.interact("Trade with", targetPlayer.getName()) && waitForTrade(TRADE_TYPE_MAIN, tradeWait);
		} else {
			return isTradingWith(targetPlayer.getName());
		}
	}

	/**
	 * Trades a player.
	 *
	 * @param targetPlayer The desired player.
	 * @return <tt>true</tt> if traded.
	 */
	public boolean tradePlayer(final RSPlayer targetPlayer) {
		return tradePlayer(targetPlayer, 15000);
	}

	/**
	 * Accepts a trade
	 *
	 * @return <tt>true</tt> on accept.
	 */
	public boolean acceptTrade() {
		if (inTradeMain()) {
			return methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_MAIN_ACCEPT).interact("Accept");
		} else {
			return inTradeSecond() && methods.interfaces.get(INTERFACE_TRADE_SECOND).getComponent(INTERFACE_TRADE_SECOND_ACCEPT).interact("Accept");
		}
	}

	/**
	 * Declines a trade
	 *
	 * @return <tt>true</tt> on decline
	 */
	public boolean declineTrade() {
		if (inTradeMain()) {
			return methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_MAIN_DECLINE).interact("Decline");
		} else {
			return inTradeSecond() && methods.interfaces.get(INTERFACE_TRADE_SECOND).getComponent(INTERFACE_TRADE_SECOND_DECLINE).interact("Decline");
		}
	}

	/**
	 * Waits for trade type to be true.
	 *
	 * @param tradeType The trade type.
	 * @param timeOut   Time out of waiting.
	 * @return <tt>true</tt> if true, otherwise false.
	 */
	public boolean waitForTrade(final int tradeType, final long timeOut) {
		final long timeCounter = System.currentTimeMillis() + timeOut;
		while (timeCounter - System.currentTimeMillis() > 0) {
			switch (tradeType) {
				case TRADE_TYPE_MAIN:
					if (inTradeMain()) {
						return true;
					}
					break;
				case TRADE_TYPE_SECONDARY:
					if (inTradeSecond()) {
						return true;
					}
					if (!inTrade()) {
						sleep(1000);
						if (!inTrade()) {
							return false;
						}
					}
					break;
				case TRADE_TYPE_NONE:
					if (!inTrade()) {
						return true;
					}
					break;
			}
			sleep(5);
		}
		return false;
	}

	/**
	 * Gets who you're trading with.
	 *
	 * @return The person's name you're trading with.
	 */
	public String getTradingWith() {
		if (inTradeMain()) {
			final String name = methods.interfaces.getComponent(INTERFACE_TRADE_MAIN, INTERFACE_TRADE_MAIN_NAME).getText();
			return name.substring(name.indexOf(": ") + 2);
		} else if (inTradeSecond()) {
			return methods.interfaces.getComponent(INTERFACE_TRADE_SECOND, INTERFACE_TRADE_SECOND_NAME).getText();
		}
		return null;
	}

	/**
	 * Checks if you're trading with someone.
	 *
	 * @param name The person's name.
	 * @return <tt>true</tt> if true; otherwise <tt>false</tt>.
	 */
	public boolean isTradingWith(final String name) {
		return getTradingWith().equals(name);
	}

	/**
	 * Returns the total number of items offered by another player
	 *
	 * @return The number of items offered.
	 */
	public int getNumberOfItemsOffered() {
		int number = 0;
		for (int i = 0; i < 28; i++) {
			if (methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_MAIN_THEIR).getComponent(i).getComponentStackSize() != 0) {
				++number;
			}
		}
		return number;
	}

	/**
	 * Returns the items offered by another player
	 *
	 * @return The items offered.
	 */
	public RSItem[] getItemsOffered() {
		List<RSItem> items = new ArrayList<RSItem>();
		for (int i = 0; i < 28; i++) {
			RSComponent component = methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_MAIN_THEIR).getComponent(i);
			if (component != null && component.getComponentStackSize() != 0) {
				items.add(new RSItem(methods, component));
			}
		}
		return items.toArray(new RSItem[items.size()]);
	}

	/**
	 * Returns the total number of free slots the other player has
	 *
	 * @return The number of free slots.
	 */
	public int getFreeSlots() {
		if (inTradeMain()) {
			String text = methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_MAIN_INV_SLOTS).getText().substring(4, 6);
			text = text.trim();
			try {
				return Integer.parseInt(text);
			} catch (final Exception ignored) {
			}
		}
		return 0;
	}

	/**
	 * Checks if you have offered any item.
	 *
	 * @return <tt>true</tt> if something has been offered; otherwise <tt>false</tt>.
	 */
	public boolean isWealthOffered() {
		return inTradeMain() && methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_OUR_AMOUNT).getText().indexOf("Nothing") == -1;
	}

	/**
	 * Checks if other player has offered any item.
	 *
	 * @return <tt>true</tt> if something has been offered; otherwise <tt>false</tt>.
	 */
	public boolean isWealthReceived() {
		return inTradeMain() && methods.interfaces.get(INTERFACE_TRADE_MAIN).getComponent(INTERFACE_TRADE_THEIR_AMOUNT).getText().indexOf("Nothing") == -1;
	}

	/**
	 * If trade main is open, offers specified amount of an item.
	 *
	 * @param itemID The ID of the item.
	 * @param number The amount to offer. 0 deposits All. 1,5,10 offer
	 *               corresponding amount while other numbers offer X.
	 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>.
	 */
	public boolean offer(final int itemID, final int number) {
		if (!inTradeMain()) {
			return false;
		}
		if (number < 0) {
			throw new IllegalArgumentException("number < 0 (" + number + ")");
		}
		RSComponent item = methods.inventory.getItem(itemID).getComponent();
		final int itemCount = methods.inventory.getCount(true, itemID);
		if (item == null) {
			return true;
		}
		switch (number) {
			case 0:
				item.interact(itemCount > 1 ? "Offer-All" : "Offer");
				break;
			case 1:
				item.interact("Offer");
				break;
			default:
				if (!item.interact("Offer-" + number)) {
					if (item.interact("Offer-X")) {
						sleep(random(1000, 1300));
						methods.inputManager.sendKeys(String.valueOf(number), true);
					}
				}
				break;
		}
		sleep(300);
		return (methods.inventory.getCount(itemID) < itemCount) || (methods.inventory.getCount() == 0);
	}
}
