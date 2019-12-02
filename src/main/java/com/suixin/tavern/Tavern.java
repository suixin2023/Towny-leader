package com.suixin.tavern;

import com.suixin.tavern.command.CashCommand;
import com.suixin.tavern.command.GuiCommand;
import com.suixin.tavern.command.LeaderBoardCommand;
import com.suixin.tavern.command.RankCommand;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.listener.HandlePlayerPrefix;
import com.suixin.tavern.handler.Ranks;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Tavern extends JavaPlugin {
	public Logger log;
	private RankCommand rankCommand;
	private GuiCommand guiCommand;
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
		this.guiCommand = new GuiCommand();
		this.handlePlayerPrefix = new HandlePlayerPrefix(this.databaseHandler, this.ranks, this);
		this.ladder = new LeaderBoardCommand(this);
		this.cash = new CashCommand(databaseHandler);
		getServer().getPluginManager().registerEvents(handlePlayerPrefix, this);
		getCommand("rank").setExecutor(this.rankCommand);
		getCommand("ladder").setExecutor(this.ladder);
		getCommand("cash").setExecutor(this.cash);
		getCommand("tave").setExecutor(this.guiCommand);
	}
}
