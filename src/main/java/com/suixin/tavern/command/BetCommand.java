package com.suixin.tavern.command;

import com.suixin.tavern.entity.PlayerBetDate;
import com.suixin.tavern.handler.BetDataHandler;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.handler.Ranks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BetCommand implements CommandExecutor {
	private final BetDataHandler betDataHandler;
	String[] strs = {"大","小","单","双","豹子"};
	private final List<String> betList = Arrays.asList(strs);

	public BetCommand(final BetDataHandler databaseHandler) {
		this.betDataHandler = databaseHandler;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else {
			sender.sendMessage("sdada!");
			return false;
		}
		if (args.length != 2) {
			player.sendMessage(ChatColor.RED + "参数不正确!");
			player.sendMessage(ChatColor.RED + "指令提示: tn bet <类型> <金额>");
			return false;
		}
		if (cmd.getName().equalsIgnoreCase("tnbet")) {
			List<String> argsList = Arrays.asList(args);
			String betType = argsList.get(0);
			if (!betList.contains(betType)) {
				player.sendMessage(ChatColor.RED + "押注类型必须是（大,小,单,双,豹子）中的一个");
				return false;
			}
			String amountStr = argsList.get(1);
			Integer amount = 0;
			try {
				amount = Integer.valueOf(amountStr);
			}catch (Exception e){
				player.sendMessage(ChatColor.RED + "押注金额必须是整数");
				return false;
			}
			if (amount < 10) {
				player.sendMessage(ChatColor.RED + "押注金额必须大于10");
				return false;
			}
			PlayerBetDate playerBetDate = new PlayerBetDate();
			playerBetDate.setPlayerName(player.getDisplayName());
			playerBetDate.setBetAmount(amount);
			playerBetDate.setBetType(betType);
			List<PlayerBetDate> currentBetList = betDataHandler.getCurrentBetList();
			currentBetList.add(playerBetDate);
			player.sendMessage("§a§l押注成功！！！");
			player.sendMessage("§a§l输入：/tn time 查询开奖时间！");
		}

		return true;
	}
}
