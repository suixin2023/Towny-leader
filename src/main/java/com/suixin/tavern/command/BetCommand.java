package com.suixin.tavern.command;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.entity.PlayerBetDate;
import com.suixin.tavern.entity.SoloEntity;
import com.suixin.tavern.handler.BetDataHandler;
import com.suixin.tavern.handler.SoloDatabaseHandler;
import com.suixin.tavern.util.VaultAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BetCommand implements CommandExecutor {
	private final BetDataHandler betDataHandler;
	private final Tavern tavern;
	private List<PlayerBetDate> currentCckBetList;
	private List<PlayerBetDate> currentDbBetList;
	String[] strs = {"大","小","单","双","豹子"};
	private final List<String> betCckList = Arrays.asList(strs);
	String[] strs1 = {"大","小","龙"};
	private final List<String> betDbList = Arrays.asList(strs1);
	String[] strs2 = {"老虎","棒子","鸡"};
	private final List<String> typeList = Arrays.asList(strs2);

	public BetCommand(final BetDataHandler databaseHandler,final Tavern tavern) {
		this.betDataHandler = databaseHandler;
		this.tavern = tavern;
		currentCckBetList = new ArrayList<>();
		currentDbBetList = new ArrayList<>();
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String arg, final String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}else {
			sender.sendMessage("错误，这是一个玩家指令!");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("tn")) {
			if (args.length == 0) {
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				player.sendMessage("§6●§a/tn cck <大|小|单|双|豹子> <金额> ：猜猜看押注");
				player.sendMessage("§6●§a/tn db <大|小|龙> <金额> ：点卷夺宝押注!");
				player.sendMessage("§6●§a/tn solo create <老虎|棒子|鸡> <金额> ：创建对局!");
				player.sendMessage("§6●§a/tn solo <对局编号> <老虎|棒子|鸡> ：挑战对局!");
				player.sendMessage("§6●§a/tn solo list <页码>：查看对局列表!");
				player.sendMessage("§6●§a/tn time <cck|db> ：查看本期开奖时间!");
				player.sendMessage("§6●§a/tn rule <cck|db|solo> ：了解游戏规则!");
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				return true;
			}
			List<String> argsList = Arrays.asList(args);
			String arg1 = argsList.get(0);
			if (arg1.equals("cck")) {
				//进行【猜猜看】金额押注
				try {
					return caicaikanBet (player, argsList);
				}catch (Exception e){

				}
			}else if (arg1.equals("db")){
				//进行【点卷夺宝】金额押注
				try {
					return duobaoBet (player, argsList);
				}catch (Exception e){

				}
			}else if (arg1.equals("open")){
				//打开GUI
				//TODO
			}else if (arg1.equals("rule")){
				//了解游戏规则
				return selectRule(player, argsList);
			}else if (arg1.equals("time")){
				//查看本期开奖剩余时间
				return selectTime(player, argsList);
			}else if (arg1.equals("solo")){
				//老虎棒子鸡
				return createdSolo(player, argsList);
			}
		}
		return true;
	}


	/**
	 * 猜猜看押注
	 * @param player
	 * @param argsList
	 * @return
	 */
	private Boolean caicaikanBet (Player player, List<String> argsList){
		if (argsList.size() != 3) {
			player.sendMessage(ChatColor.RED + "参数不正确!");
			player.sendMessage(ChatColor.RED + "指令提示: tn cck <类型> <金额>");
			return true;
		}
		String betType = argsList.get(1);
		if (!betCckList.contains(betType)) {
			player.sendMessage(ChatColor.RED + "押注类型必须是（大,小,单,双,豹子）中的一个");
			return true;
		}
		String amountStr = argsList.get(2);
		Integer amount = 0;
		try {
			amount = Integer.valueOf(amountStr);
		}catch (Exception e){
			player.sendMessage(ChatColor.RED + "押注金额必须是整数");
			return true;
		}
		if (amount < 10) {
			player.sendMessage(ChatColor.RED + "押注金额必须大于10");
			return true;
		}
		//判断是否有足够的金钱
		double money = VaultAPI.getMoney(player.getName());
		if (money < amount) {
			player.sendMessage(ChatColor.RED + "你没有足够的金币！");
			return true;
		}
        String time = selectCaicaikanTime();
		if (time == null) {
            player.sendMessage(ChatColor.RED + "奖励发放中！请稍后再押注");
            return true;
        }
        //扣除金币
        VaultAPI.removeMoney(player.getName(),amount);
        PlayerBetDate playerBetDate = new PlayerBetDate();
        playerBetDate.setPlayerName(player.getName());
        playerBetDate.setBetAmount(amount);
        playerBetDate.setGameType("猜猜看");
        playerBetDate.setBetType(betType);
        currentCckBetList.add(playerBetDate);
        player.sendMessage("§a§l押注成功！输入：/tn time cck 查询开奖时间！");
        return true;
	}

	/**
	 * 点卷夺宝押注
	 * @param player
	 * @param argsList
	 * @return
	 */
	private Boolean duobaoBet (Player player, List<String> argsList){
		if (argsList.size() != 3) {
			player.sendMessage(ChatColor.RED + "参数不正确!");
			player.sendMessage(ChatColor.RED + "指令提示: tn db <类型> <点卷>");
			return true;
		}
		String betType = argsList.get(1);
		if (!betCckList.contains(betType)) {
			player.sendMessage(ChatColor.RED + "押注类型必须是（大,小,龙）中的一个");
			return true;
		}
		String amountStr = argsList.get(2);
		Integer amount = 0;
		try {
			amount = Integer.valueOf(amountStr);
		}catch (Exception e){
			player.sendMessage(ChatColor.RED + "押注点卷必须是整数");
			return true;
		}
		if (amount < 100) {
			player.sendMessage(ChatColor.RED + "押注点卷必须大于100");
			return true;
		}
		//判断是否有足够的点卷
		UUID uniqueId = player.getUniqueId();
		int money = tavern.getPlayerPointsAPI().look(uniqueId);
		if (money < amount) {
			player.sendMessage(ChatColor.RED + "你没有足够的点卷！");
			return true;
		}
        String time = selectCaicaikanTime();
		if (time == null) {
            player.sendMessage(ChatColor.RED + "奖励发放中！请稍后再押注");
            return true;
        }
        //扣除点卷
        tavern.getPlayerPointsAPI().take(uniqueId,amount);
        PlayerBetDate playerBetDate = new PlayerBetDate();
        playerBetDate.setPlayerName(player.getName());
        playerBetDate.setBetAmount(amount);
        playerBetDate.setGameType("点卷夺宝");
        playerBetDate.setBetType(betType);
        currentDbBetList.add(playerBetDate);
        player.sendMessage("§a§l押注成功！输入：/tn time db 查询开奖时间！");
        return true;
	}

	/**
	 * 对局创建和挑战
	 * @param player
	 * @param argsList
	 * @return
	 */
	private Boolean createdSolo (Player player, List<String> argsList){
		if (argsList.size() == 1) {
			player.sendMessage(ChatColor.RED + "参数缺失：输入/tn 查看指令帮助");
			return true;
		}

		if (argsList.size() >= 2 && argsList.get(1).equals("create")) {
			if (argsList.size() != 4) {
				player.sendMessage(ChatColor.RED + "参数个数不正确：/tn solo create <老虎|棒子|鸡> <金额>");
				return true;
			}
			String type = argsList.get(2);
			if (!typeList.contains(type)) {
				player.sendMessage(ChatColor.RED + "对局类型必须是（老虎，棒子，鸡）中的一个");
				return true;
			}

			String money = argsList.get(3);
			Integer amount = 0;
			try {
				amount = Integer.valueOf(money);
			}catch (Exception e){
				player.sendMessage(ChatColor.RED + "创建对局的金额必须是整数");
				return true;
			}
			if (amount < 10) {
				player.sendMessage(ChatColor.RED + "对局金额必须大于10");
				return true;
			}

			//判断是否有足够的金钱
			double money1 = VaultAPI.getMoney(player.getName());
			if (money1 < amount) {
				player.sendMessage(ChatColor.RED + "你没有足够的金币！");
				return true;
			}

			//创建对局
			SoloEntity soloEntity = new SoloEntity();
			soloEntity.setPlayerName(player.getName());//对局创建人
			soloEntity.setType(type);//类型
			soloEntity.setMoney(new Double(amount));//金额
			soloEntity.setState(1);//状态：待挑战
			soloEntity.setResult("待挑战");//结果
			soloEntity.setDraw(1);
			soloEntity.setStatus(1);
			soloEntity.setCreated(new Date());
			int res = SoloDatabaseHandler.soloInsert(soloEntity);
			//扣除金币
			if (res == 1) {
				VaultAPI.removeMoney(player.getName(),amount);
				player.sendMessage("§a创建对局成功！输入：/tn solo list <页码>！");
			}
			return true;

		}else if (argsList.size() >= 2 && argsList.get(1).equals("list")){
			//查询对局
			if (argsList.size() > 3) {
				player.sendMessage(ChatColor.RED + "参数个数不正确：/tn solo list <页码>");
				return true;
			}
			Integer count = SoloDatabaseHandler.selectSoloDataCount();
			int j = count / 5;
			int i = count % 5;
			Integer countnum;
			if (i == 0) {
				countnum = j;
			}else {
				countnum = j + 1;
			}
			if (count < 5) {
				countnum = 1;
			}
			if (argsList.size() == 2) {
				//查询第一页
				List<SoloEntity> soloEntities = SoloDatabaseHandler.selectSoloData(0);
				if (1 > countnum) {
					player.sendMessage(ChatColor.RED + "没有更多的对局了");
				}
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §e【§a§l对局列表§e】 §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				for (SoloEntity soloEntity : soloEntities) {
					Integer id = soloEntity.getId();
					String playerName = soloEntity.getPlayerName();
					Double money = soloEntity.getMoney();
					player.sendMessage("§6●§3对局编号:§a"+id+"  §3发起人:§a"+playerName+"  §3金额:§6"+money);
				}
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §a§l<页码>  "+1+"\\"+countnum+" §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				return true;
			}
			if (argsList.size() == 3) {
				//获取页数
				String num = argsList.get(2);
				Integer limit = 0;
				try {
					limit = Integer.valueOf(num);
				}catch (Exception e){
					player.sendMessage(ChatColor.RED + "查询的页数必须是整数");
					return true;
				}
				if (limit > countnum) {
					player.sendMessage(ChatColor.RED + "没有更多的对局了");
					return true;
				}
				limit =(limit-1)*5;
				List<SoloEntity> soloEntities = SoloDatabaseHandler.selectSoloData(limit);
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §e【§a§l对局列表§e】 §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				for (SoloEntity soloEntity : soloEntities) {
					Integer id = soloEntity.getId();
					String playerName = soloEntity.getPlayerName();
					Double money = soloEntity.getMoney();
					player.sendMessage("§6●§3对局编号:§a"+id+"  §3发起人:§a"+playerName+"  §3金额:§6"+money);
				}
				player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §a§l§m §a§l§m §a§l§m §a§l§m §b§l§m §b§l§m §b§l§m §a§l<页码>  "+num+"\\"+countnum+" §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §e§l§m §e§l§m §e§l§m §e§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
				return true;
			}
		}else{
			//挑战对局
			if (argsList.size() != 3) {
				player.sendMessage(ChatColor.RED + "参数个数不正确：/tn solo <对局编号> <老虎|棒子|鸡>");
				return true;
			}
			String id = argsList.get(1);
			Integer solonum = null;
			try {
				solonum = Integer.valueOf(id);
			}catch (Exception e){
				player.sendMessage(ChatColor.RED + "挑战的对局编号必须是整数");
				return true;
			}
			String type = argsList.get(2);
			if (!typeList.contains(type)) {
				player.sendMessage(ChatColor.RED + "对局类型必须是（老虎，棒子，鸡）中的一个");
				return true;
			}
			//查询挑战编号是否存在
			SoloEntity soloEntity = SoloDatabaseHandler.selectSoloDataNum(solonum);
			if (soloEntity.getId() == null) {
				player.sendMessage(ChatColor.RED + "对局编号不存在或已经被挑战，请重新输入");
				return true;
			}else {
				//判断金额是否足够发起挑战
				double money = VaultAPI.getMoney(player.getName());
				Double amount = soloEntity.getMoney();
				if (money < amount) {
					player.sendMessage(ChatColor.RED + "你没有足够的金币！");
					return true;
				}
				String soloType  = soloEntity.getType();
				if (soloType.equals(type)) {
					//和局，修改对局信息
					drawn(player,solonum);
				}else if (soloType.equals("老虎") && type.equals("鸡")) {
					//挑战者输
					lose(player, soloEntity, amount, solonum);
				}else if (soloType.equals("老虎") && type.equals("棒子")) {
					//挑战者赢
					win(player, amount,solonum);
				}else if (soloType.equals("棒子") && type.equals("鸡")) {
					//挑战者赢
					win(player, amount,solonum);
				}else if (soloType.equals("棒子") && type.equals("老虎")) {
					//挑战者输
					lose(player, soloEntity, amount,solonum);
				}else if (soloType.equals("鸡") && type.equals("老虎")) {
					//挑战者输
					lose(player, soloEntity, amount,solonum);
				}else if (soloType.equals("鸡") && type.equals("棒子")) {
					//挑战者赢
					win(player, amount,solonum);
				}

			}
		}
		return true;
	}
    private void win (Player player,Double amount,Integer solonum) {
		//增加发起者金币
		VaultAPI.giveMoney(player.getName(),amount);
		player.sendMessage("§a恭喜您赢得对局的胜利，获得§6"+amount+"金");
		//修改发起者的对局状态为输
		SoloDatabaseHandler.updateSoloDataNum(solonum,"输");
	}

	private void lose (Player player,SoloEntity soloEntity,Double amount, Integer solonum) {
		//扣除挑战者金币
		VaultAPI.removeMoney(player.getName(),amount);
		player.sendMessage("§b很遗憾，您没有干过对手，本局输掉§6"+amount+"金");
		//修改发起者的对局状态为赢
		SoloDatabaseHandler.updateSoloDataNum(solonum,"赢");
		//发送邮件通知
		//TODO
	}

	private void drawn (Player player, Integer solonum) {
		player.sendMessage("§a本次挑战为和局，您可以继续挑战其他对局");
		//修改发起者的对局状态为和局
		SoloDatabaseHandler.updateSoloDataNum(solonum,"和");
		//发送邮件通知
		//TODO
	}
	/**
	 * 查询开奖时间（猜猜看和电卷夺宝）
	 * @param player
	 * @param argsList
	 * @return
	 */
	private Boolean selectTime (Player player, List<String> argsList){
		if (argsList.size() == 1) {
			player.sendMessage(ChatColor.RED + "参数缺失：/tn time <cck|db>");
		}else if (argsList.size() > 2) {
			player.sendMessage(ChatColor.RED + "参数个数不正确：/tn time <cck|db>");
		}else if (argsList.get(1).equals("cck")){
			//查询本期猜猜看的开奖时间
			String time = selectCaicaikanTime();
			if (time == null) {
				player.sendMessage(ChatColor.YELLOW + "奖励发放中，请稍等片刻！");
			}else {
				player.sendMessage(ChatColor.YELLOW + "距离本期【猜猜看】开奖还有"+time);
			}
			return true;
		}else if (argsList.get(1).equals("db")){
			//查询本期猜猜看的开奖时间
			String time = selectDuobaoTime();
			if (time == null) {
				player.sendMessage(ChatColor.YELLOW + "奖励发放中，请稍等片刻！");
			}else {
				player.sendMessage(ChatColor.YELLOW + "距离本期【点卷夺宝】开奖还有" + time);
			}
			return true;
		}
		return true;
	}

	/**
	 * 查询游戏规则
	 * @param player
	 * @param argsList
	 * @return
	 */
	Boolean selectRule (Player player, List<String> argsList){
		if (argsList.size() == 1) {
			player.sendMessage(ChatColor.RED + "参数缺失：/tn rule <cck|db>");
		}else if (argsList.size() > 2) {
			player.sendMessage(ChatColor.RED + "参数个数不正确：/tn rule <cck|db>");
		}else if (argsList.get(1).equals("cck")){

			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e【§a§l猜猜看§e】§6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			player.sendMessage("§6●§a游戏规则：");
			player.sendMessage("§6●§3大小单双，赔率两倍，豹子赔率十倍");
			player.sendMessage("§6●§3最低押注10金币");
			player.sendMessage("§6●§3每局五分钟，押注结果请查看邮箱");
			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			return true;
		}else if (argsList.get(1).equals("db")){
			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e【§a§l点券夺宝§e】§6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			player.sendMessage("§6●§a游戏规则：");
			player.sendMessage("§6●§3大小，赔率两倍，龙赔率十倍");
			player.sendMessage("§6●§3最低押注100点券");
			player.sendMessage("§6●§3每局十分钟，押注结果请查看邮箱");
			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			return true;
		}else if (argsList.get(1).equals("solo")){
			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e【§a§l老虎棒子鸡§e】§6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			player.sendMessage("§6●§a游戏规则：");
			player.sendMessage("§6●§3老虎吃鸡，鸡吃棒，棒打老虎");
			player.sendMessage("§6●§3玩家可以发起对局，也可以挑战对局");
			player.sendMessage("§6●§3挑战结果请查看邮箱");
			player.sendMessage("§c§l§m §c§l§m §c§l§m §c§l§m §6§l§m §6§l§m §6§l§m §6§l§m §e§l§m §e§l§m §e§l§m §e§l§m §b§l§m §b§l§m §b§l§m §a§l§m §a§l§m §a§l§m §a§l§m §6§l§m §6§l§m §6§l§m §6§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m §c§l§m");
			return true;
		}
		return true;
	}

	private String selectCaicaikanTime () {
		long millis = System.currentTimeMillis();
		long betTime = tavern.getTimeCckMillis();
		long l = betTime - millis;
		long mu = l / (60 * 1000);
		long sec = (l % (60 * 1000))/1000;
		String time;
		if (mu < 0 || sec <= 0){
			time = null;
		}else if (mu == 0){
			time = sec+"秒";
		}else {
			time = mu+"分"+sec+"秒";
		}
		return time;
	}

	private String selectDuobaoTime () {
		long millis = System.currentTimeMillis();
		long betTime = tavern.getTimeDbMillis();
		long l = betTime - millis;
		long mu = l / (60 * 1000);
		long sec = (l % (60 * 1000))/1000;
		String time;
		if (mu < 0 || sec <= 0){
			time = null;
		}else if (mu == 0){
			time = sec+"秒";
		}else {
			time = mu+"分"+sec+"秒";
		}
		return time;
	}

	public List<PlayerBetDate> getCurrentCckBetList() {
		return currentCckBetList;
	}

	public void setCurrentCckBetList(List<PlayerBetDate> currentCckBetList) {
		this.currentCckBetList = currentCckBetList;
	}

	public void clearCurrentCckBetList() {
		this.currentCckBetList = new ArrayList<>();
	}

	public void clearCurrentDbBetList() {
		this.currentDbBetList = new ArrayList<>();
	}

	public List<PlayerBetDate> getCurrentDbBetList() {
		return currentDbBetList;
	}

	public void setCurrentDbBetList(List<PlayerBetDate> currentDbBetList) {
		this.currentDbBetList = currentDbBetList;
	}
}
