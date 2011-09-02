package org.rsbot.script.methods;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.script.internal.InputManager;
import org.rsbot.script.task.executor.ScriptPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * For internal use to link MethodProviders.
 */
public class MethodContext {
	/**
	 * The instance of {@link java.util.Random} for random number generation.
	 */
	public final java.util.Random random = new java.util.Random();

	/**
	 * The singleton of skills
	 */
	public final Skills skills = new Skills(this);

	/**
	 * The singleton of Settings
	 */
	public final Settings settings = new Settings(this);

	/**
	 * The singleton of magic
	 */
	public final Magic magic = new Magic(this);

	/**
	 * The singleton of bank
	 */
	public final Bank bank = new Bank(this);

	/**
	 * The singleton of players
	 */
	public final Players players = new Players(this);

	/**
	 * The singleton of store
	 */
	public final Store store = new Store(this);

	/**
	 * The singleton of Grand Exchange
	 */
	public final GrandExchange grandExchange = new GrandExchange(this);

	/**
	 * The singletion of Hiscores
	 */
	public final Hiscores hiscores = new Hiscores();

	/**
	 * The singleton of ClanChat
	 */
	public final ClanChat clanChat = new ClanChat(this);

	/**
	 * The singleton of Camera
	 */
	public final Camera camera = new Camera(this);

	/**
	 * The singleton of NPCs
	 */
	public final NPCs npcs = new NPCs(this);

	/**
	 * The singleton of GameScreen
	 */
	public final Game game = new Game(this);

	/**
	 * The singleton of Combat
	 */
	public final Combat combat = new Combat(this);

	/**
	 * The singleton of Interfaces
	 */
	public final Interfaces interfaces = new Interfaces(this);

	/**
	 * The singleton of Mouse
	 */
	public final Mouse mouse = new Mouse(this);

	/**
	 * The singleton of Keyboard
	 */
	public final Keyboard keyboard = new Keyboard(this);

	/**
	 * The singleton of Menu
	 */
	public final Menu menu = new Menu(this);

	/**
	 * The singleton of Tile
	 */
	public final Tiles tiles = new Tiles(this);

	/**
	 * The singleton of Objects
	 */
	public final Objects objects = new Objects(this);

	/**
	 * The singleton of Walking
	 */
	public final Walking walking = new Walking(this);

	/**
	 * The singleton of Calculations
	 */
	public final org.rsbot.script.methods.Calculations calc = new Calculations(this);

	/**
	 * The singleton of Inventory
	 */
	public final Inventory inventory = new Inventory(this);

	/**
	 * The singleton of Equipment
	 */
	public final Equipment equipment = new Equipment(this);

	/**
	 * The singleton of GroundItems
	 */
	public final GroundItems groundItems = new GroundItems(this);

	/**
	 * The singleton of DynamicConstants
	 */
	public final GameGUI gui = new GameGUI(this);

	/**
	 * The singleton of Nodes
	 */
	public final Nodes nodes = new Nodes(this);

	/**
	 * the singleton of Account
	 */
	public final Account account = new Account(this);

	/**
	 * The singleton of Summoning
	 */
	public final Summoning summoning = new Summoning(this);

	/**
	 * The singleton of Environment
	 */
	public final Environment env = new Environment(this);

	/**
	 * The singleton of Prayer
	 */
	public final Prayer prayer = new Prayer(this);

	/**
	 * The singleton of Quests
	 */
	public final Quests quests = new Quests(this);

	/**
	 * The singleton of Prayer
	 */
	public final FriendChat friendChat = new FriendChat(this);

	/**
	 * The singleton of Trade
	 */
	public final Trade trade = new Trade(this);

	/**
	 * The singleton of Lobby
	 */
	public final Lobby lobby = new Lobby(this);

	/**
	 * The Bot's input manager
	 */
	public final InputManager inputManager;

	/**
	 * The client
	 */
	public final Client client;

	/**
	 * The Web
	 */
	public final Web web = new Web(this);

	/**
	 * The ExecutorService
	 */
	public final ExecutorService service;

	public final Bot bot;

	public MethodContext(final Bot bot) {
		this.bot = bot;
		client = bot.getClient();
		inputManager = bot.getInputManager();
		service = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ScriptPool(), new ThreadPoolExecutor.AbortPolicy());
	}
}
