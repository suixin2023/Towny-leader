package com.suixin.tavern;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PvPTitles extends JavaPlugin {
	public Logger log;
	private RankCommand rankCommand;
	private DatabaseHandler databaseHandler;
	private Ranks ranks;
	private HandlePlayerPrefix handlePlayerPrefix;
	private LeaderBoardCommand ladder;
	private CashCommand cash;

	@Override
	public void onEnable() {
		this.log = getLogger();
		this.databaseHandler = new DatabaseHandler(this);
		this.ranks = new Ranks(this.databaseHandler, this);
		this.rankCommand = new RankCommand(this.databaseHandler, this.ranks);
		this.handlePlayerPrefix = new HandlePlayerPrefix(this.databaseHandler, this.ranks, this);
		this.ladder = new LeaderBoardCommand(this);
		this.cash = new CashCommand(databaseHandler);
		getServer().getPluginManager().registerEvents(handlePlayerPrefix, this);
		getCommand("rank").setExecutor(this.rankCommand);
		getCommand("ladder").setExecutor(this.ladder);
		getCommand("cash").setExecutor(this.cash);
	}
}
