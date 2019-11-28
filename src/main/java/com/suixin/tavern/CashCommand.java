package com.suixin.tavern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CashCommand implements CommandExecutor {
	private final DatabaseHandler databaseHandler;

	public CashCommand(final DatabaseHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (!(sender instanceof Player)) {
			return true;
		}
		player = (Player) sender;
		if (args.length == 1) {
			this.databaseHandler.LoadPlayerData(player.getName());
			this.databaseHandler.LoadConfig();
			final int point = this.databaseHandler.getPoints();
			final String strnum = args[0];
			int num = 0;
			try {
				num = Integer.valueOf(strnum);
			} catch (final Exception e) {
				sender.sendMessage("§c参数必须为数字!");
				return true;
			}
			if (num > point) {
				sender.sendMessage("§c您的点数不足以购买!");
				return true;
			}
			if (!databaseHandler.getCashList().containsKey(num)) {
				sender.sendMessage("§c不存在当前的物品!");
				return true;
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), databaseHandler.getCashList().get(num).replace("{player}", player.getName()));
			databaseHandler.SavePlayerPoint(sender.getName(), point - num);
			sender.sendMessage("§a兑换成功!");
		} else {
			player.sendMessage(ChatColor.RED + "不正确的参数!");
		}
		return true;
	}
}
