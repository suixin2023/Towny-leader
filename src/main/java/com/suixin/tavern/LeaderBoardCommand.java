package com.suixin.tavern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LeaderBoardCommand implements CommandExecutor {
	private final PvPTitles pvpTitles;
	private final TreeMap<Integer, String> RankedPlayers;

	public LeaderBoardCommand(final PvPTitles pvpTitles) {
		this.pvpTitles = pvpTitles;
		this.RankedPlayers = new TreeMap<Integer, String>();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("ladder")) {
			this.LadderCmd(player);
		}
		if (args.length > 0) {
			player.sendMessage(ChatColor.RED + "过多的参数!");
		}
		return true;
	}

	private void LadderCmd(final Player player) {
		this.SetTopTenPlayers(player);

		player.sendMessage(ChatColor.AQUA + "天梯 - 前五的玩家");
		player.sendMessage(ChatColor.AQUA + "------------------------");
		final NavigableMap<Integer, String> sortedMap = this.RankedPlayers.descendingMap();
		int number = 0;
		for (final Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
			if (number != 5) {
				player.sendMessage((number + 1) + ". " + entry.getValue() + " (" + entry.getKey() + ")");
			} else {
				break;
			}
			number++;
		}

		sortedMap.clear();
		this.RankedPlayers.clear();
	}

	private void SetTopTenPlayers(final Player player) {
		final File file = new File((new StringBuilder()).append(this.pvpTitles.getDataFolder()).append(File.separator).append("players").toString());
		final File[] allFiles = file.listFiles();
		for (final File item : allFiles) {
			final File ladderFile = new File((new StringBuilder())
					.append(this.pvpTitles.getDataFolder())
					.append(File.separator)
					.append("players")
					.append(File.separator)
					.append(item.getName())
					.toString());
			final FileConfiguration config = YamlConfiguration.loadConfiguration(ladderFile);
			final int fame = config.getInt("Fame");
			this.RankedPlayers.put(fame, item.getName().replaceAll(".yml", ""));
		}
	}
}