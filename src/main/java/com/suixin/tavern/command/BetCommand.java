package com.suixin.tavern.command;

import com.suixin.tavern.entity.PlayerBetDate;
import com.suixin.tavern.handler.BetDataHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			sender.sendMessage("错误，这是一个玩家指令!");
		}
		if (cmd.getName().equalsIgnoreCase("tn")) {
			if (args.length == 0) {
				player.sendMessage(ChatColor.GREEN + "/tn caicaikan <类型> <金额> ：猜猜看押注");
				player.sendMessage(ChatColor.GREEN + "/tn duobao <类型> <金额> ：点卷夺宝押注!");
				player.sendMessage(ChatColor.GREEN + "/tn time <类型> ：查看本期开奖时间!");
			}
			List<String> argsList = Arrays.asList(args);
			String arg1 = argsList.get(0);
			if (arg1.equals("caicaikan")) {
				//进行【猜猜看】金额押注
				try {
					return caicaikanBet (player, argsList);
				}catch (Exception e){

				}
			}else if (arg1.equals("open")){
				//打开GUI
				//TODO
			}else if (arg1.equals("time")){
				//查看本期开奖剩余时间
				//TODO
				if (argsList.size() == 1) {
					player.sendMessage(ChatColor.RED + "参数缺失：/tn time <类型>");
				}else if (argsList.size() > 2) {
					player.sendMessage(ChatColor.RED + "参数个数不正确：/tn time <类型>");
				}else if (argsList.get(1).equals("caicaikan")){
					//查询本期猜猜看的开奖时间
					String time = selectCaicaikanTime();
					player.sendMessage(ChatColor.YELLOW + "距离本期【猜猜看】开奖还有"+time);
					return true;
				}else if (argsList.get(1).equals("duobao")){
					//查询本期猜猜看的开奖时间
					String time = selectDuobaoTime();
					player.sendMessage(ChatColor.YELLOW + "距离本期【点卷夺宝】开奖还有"+time);
					return true;
				}
			}
		}
		return true;
	}

	private Boolean caicaikanBet (Player player, List<String> argsList){
		if (argsList.size() != 3) {
			player.sendMessage(ChatColor.RED + "参数不正确!");
			player.sendMessage(ChatColor.RED + "指令提示: tn bet <类型> <金额>");
		}
		String betType = argsList.get(1);
		if (!betList.contains(betType)) {
			player.sendMessage(ChatColor.RED + "押注类型必须是（大,小,单,双,豹子）中的一个");
		}
		String amountStr = argsList.get(2);
		Integer amount = 0;
		try {
			amount = Integer.valueOf(amountStr);
		}catch (Exception e){
			player.sendMessage(ChatColor.RED + "押注金额必须是整数");
		}
		if (amount < 10) {
			player.sendMessage(ChatColor.RED + "押注金额必须大于10");
		}
		PlayerBetDate playerBetDate = new PlayerBetDate();
		playerBetDate.setPlayerName(player.getDisplayName());
		playerBetDate.setBetAmount(amount);
		playerBetDate.setBetType(betType);
		List<PlayerBetDate> currentBetList = betDataHandler.getCurrentBetList();
		currentBetList.add(playerBetDate);
		player.sendMessage("§a§l押注成功！！！");
		player.sendMessage("§a§l输入：/tn time caicaikan 查询开奖时间！");
		return null;
	}

	private String selectCaicaikanTime () {
		String time = "5分12秒";//todo
		return time;
	}

	private String selectDuobaoTime () {
		String time = "5分12秒";//todo
		return time;
	}
}
