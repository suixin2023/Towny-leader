package com.suixin.tavern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
	private final DatabaseHandler databaseHandler;
	private final Ranks ranks;

	public RankCommand(final DatabaseHandler databaseHandler, final Ranks ranks) {
		this.databaseHandler = databaseHandler;
		this.ranks = ranks;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("rank")) {
			this.HandleRankCmd(player);
		}
		if (args.length > 0) {
			player.sendMessage(ChatColor.RED + "过多的参数!");
		}
		return true;
	}

	private void HandleRankCmd(final Player player) {
		this.databaseHandler.LoadPlayerData(player.getName());
		this.databaseHandler.LoadConfig();
		final int fame = this.databaseHandler.getFame();
		final String rank = this.ranks.GetRank(fame);
		final int rankup = this.ranks.FameToRankUp();
		final String tag = this.databaseHandler.getTag();
		final int point = this.databaseHandler.getPoints();
		if (rank == "") {
			player.sendMessage("等级: 无");
		} else {
			player.sendMessage("等级: " + rank);
		}

		player.sendMessage(tag + ": " + fame);
		player.sendMessage("点数: " + point);
		if (rankup == 999999) {
			player.sendMessage("您已到达最大等级.");
		} else {
			player.sendMessage("下一级: " + rankup);
		}
	}
}
