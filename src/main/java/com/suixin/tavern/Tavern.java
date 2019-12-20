package com.suixin.tavern;

import com.suixin.tavern.command.*;
import com.suixin.tavern.entity.*;
import com.suixin.tavern.handler.BetDataHandler;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.handler.SoloDatabaseHandler;
import com.suixin.tavern.listener.HandlePlayerPrefix;
import com.suixin.tavern.handler.Ranks;
import com.suixin.tavern.util.JdbcUtil;
import com.suixin.tavern.util.VaultAPI;
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

public class Tavern extends JavaPlugin {
	public Logger log;
	private RankCommand rankCommand;
	private BetCommand betCommand;
	private DatabaseHandler databaseHandler;
	private BetDataHandler betDataHandler;
	private SoloDatabaseHandler soloDatabaseHandler;
	private Ranks ranks;
	private HandlePlayerPrefix handlePlayerPrefix;
	private LeaderBoardCommand ladder;
	private CashCommand cash;
	private Long timeCckMillis;//cck本期开奖日期
	private Long timeDbMillis;//db本期开奖日期
	private List<PlayerBetDate> winnerCck;//cck本期获奖名单
	private List<PlayerBetDate> winnerDb;//db本期获奖名单
	private TitleManagerAPI api;
	private PlayerPointsAPI playerPointsAPI;

	@Override
	public void onEnable() {
		this.log = getLogger();
		this.winnerCck = new ArrayList<>();
		this.winnerDb = new ArrayList<>();
		this.databaseHandler = new DatabaseHandler(this);
		this.betDataHandler = new BetDataHandler(this);
		this.soloDatabaseHandler = new SoloDatabaseHandler(this);
		this.ranks = new Ranks(this.databaseHandler, this);
		this.rankCommand = new RankCommand(this.databaseHandler, this.ranks);
		this.betCommand = new BetCommand(this.betDataHandler, this);
		this.handlePlayerPrefix = new HandlePlayerPrefix(this.databaseHandler, this.ranks, this);
		this.ladder = new LeaderBoardCommand(this);
		this.cash = new CashCommand(databaseHandler);
		getServer().getPluginManager().registerEvents(handlePlayerPrefix, this);
		getCommand("rank").setExecutor(this.rankCommand);
		getCommand("ladder").setExecutor(this.ladder);
		getCommand("cash").setExecutor(this.cash);
		getCommand("tn").setExecutor(this.betCommand);
		api = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManager");
		getLogger().info("==================[Tavern]==================");
		if (api == null) {
			getLogger().info("TitleManager连接失败!插件将使用聊天栏通知");
		}else{
			getLogger().info("TitleManager连接成功");
		}
        PlayerPoints playerPoints = (PlayerPoints) Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints");
        playerPointsAPI = playerPoints.getAPI();
        if (playerPointsAPI == null) {
			getLogger().info("PlayerPoints连接失败!插件将无法正常工作");
		}else{
			getLogger().info("PlayerPointsr连接成功");
		}
		boolean economy = VaultAPI.setupEconomy();
		if (economy) {
			getLogger().info("Vault连接成功");
		}else{
			getLogger().info("Vault连接失败!插件无法正常工作");
		}
		boolean connection = new JdbcUtil().openConnection(this.betDataHandler);//加载数据库驱动
		if (connection) {
			getLogger().info("Mysql连接成功");
		}else{
			getLogger().info("Mysql连接失败!请联系开发人员解决：QQ:2469012478");
		}
		getLogger().info("==================[Tavern]==================");
		timeTask ();//启动CCK任务
		timeTask2 ();//启动DB任务
	}


	public void timeTask () {
		new BukkitRunnable() {
			@Override
			public void run() {
				//刷新开奖时间
				timeCckMillis = System.currentTimeMillis() + 300000l;
				//开奖
				int res = (int)(Math.random() * 6) + 1;
				int res2 = (int)(Math.random() * 6) + 1;
				int res3 = (int)(Math.random() * 6) + 1;
				HistoryLotteryResults historyLotteryResults = new HistoryLotteryResults();
				historyLotteryResults.setGameType("猜猜看");
				//查询上期猜猜看开奖记录
				betDataHandler.LoadCckBetData(historyLotteryResults);
				HistoryLotteryResults lotteryResults = betDataHandler.getHistoryLotteryResults();
				String periods1 = lotteryResults.getPeriods();
				Integer periods = 0;
				if (periods1 != null) {
					periods = Integer.valueOf(periods1);
				}
				historyLotteryResults.setPeriods(periods+1+"");
				if (res == res2 && res2 == res3) {
					historyLotteryResults.setResult(res+"、"+res2+"、"+res3+" "+"豹子");
					lottery(historyLotteryResults);
					//覆盖最近开奖记录
					betDataHandler.SaveCckBetDate(historyLotteryResults);
					return;
				}

				if ((res + res2 + res3) <= 10) {
					historyLotteryResults.setResult(res+"、"+res2+"、"+res3+" "+"小");
				}else{
					historyLotteryResults.setResult(res+"、"+res2+"、"+res3+" "+"大");
				}

				if ((res + res2 + res3) % 2 == 0) {
					String result = historyLotteryResults.getResult();
					historyLotteryResults.setResult(result + ","+"双");
				}else {
					String result = historyLotteryResults.getResult();
					historyLotteryResults.setResult(result + ","+"单");
				}
				lottery(historyLotteryResults);
				//覆盖最近开奖记录
				betDataHandler.SaveCckBetDate(historyLotteryResults);
				return;
			}

		}.runTaskTimer(this, 0L, 5*60*20L);
		// 插件主类  延时  定时
	}

	public void timeTask2 () {
		new BukkitRunnable() {
			@Override
			public void run() {
				//刷新开奖时间
				timeDbMillis = System.currentTimeMillis() + 600000l;
				//开奖
				int res = (int)(Math.random() * 6) + 1;
				int res2 = (int)(Math.random() * 6) + 1;
				int res3 = (int)(Math.random() * 6) + 1;
				HistoryLotteryResults historyLotteryResults = new HistoryLotteryResults();
				historyLotteryResults.setGameType("点卷夺宝");
				//查询上期猜猜看开奖记录
				betDataHandler.LoadCckBetData(historyLotteryResults);
				HistoryLotteryResults lotteryResults = betDataHandler.getHistoryLotteryResults();
				String periods1 = lotteryResults.getPeriods();
				Integer periods = 0;
				if (periods1 != null) {
					periods = Integer.valueOf(periods1);
				}
				historyLotteryResults.setPeriods(periods+1+"");
				if (res == res2 && res2 == res3) {
					historyLotteryResults.setResult("龙");
					lottery2(historyLotteryResults);
					//覆盖最近开奖记录
					betDataHandler.SaveCckBetDate(historyLotteryResults);
					return;
				}

				if ((res + res2 + res3) <= 10) {
					historyLotteryResults.setResult("小");
				}else{
					historyLotteryResults.setResult("大");
				}

				lottery2(historyLotteryResults);
				//覆盖最近开奖记录
				betDataHandler.SaveCckBetDate(historyLotteryResults);
				return;
			}

		}.runTaskTimer(this, 0L, 1*60*20L);
		// 插件主类  延时  定时
	}

	private Boolean lottery(HistoryLotteryResults historyLotteryResults) {
		//获取押注的玩家数据
		List<PlayerBetDate> currentCckBetList = betCommand.getCurrentCckBetList();
		if (currentCckBetList == null) {
			return false;
		}
		for (PlayerBetDate playerBetDate : currentCckBetList) {
			Integer betAmount = playerBetDate.getBetAmount();
			Double betAmountRes = 0.00;
			String betType = playerBetDate.getBetType();
			if (!betType.equals("豹子") && (historyLotteryResults.getResult()).contains(betType)) {
				betAmountRes = betAmount + (betAmount * 0.95);
			}else if (betType.equals("豹子") && (historyLotteryResults.getResult()).contains(betType)){
				betAmountRes = betAmount + (betAmount * 9.5);
			}
			//发放奖励
			String playerName = playerBetDate.getPlayerName();
			Player player = Bukkit.getServer().getPlayer(playerName);
//			UUID uniqueId = player.getUniqueId();//金币插件暂时不支持UUID
			if (betAmountRes > 0.00) {
				winnerCck.add(playerBetDate);
				VaultAPI.giveMoney(playerName,betAmountRes);
				player.sendMessage("§a恭喜您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"金"+"§6]"+"§a在本期【猜猜看】中获得："+betAmountRes+"金");
			}else {
				player.sendMessage("§b很遗憾...您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"金"+"§6]"+"§b在本期【猜猜看】中没有中奖");
			}
		}
		//排序
		for(int i=0;i<winnerCck.size();i++) {
			for (int j = 0; j < winnerCck.size() - 1 - i; j++) {
				if (winnerCck.get(j).getBetAmount() < winnerCck.get(j + 1).getBetAmount()) {
					Integer temp = winnerCck.get(j).getBetAmount();
					winnerCck.get(j).setBetAmount(winnerCck.get(j + 1).getBetAmount());
					winnerCck.get(j + 1).setBetAmount(temp);
				}
			}
		}
		//取前三
		List<PlayerBetDate> list = new ArrayList<>();
		if (winnerCck.size() > 3) {
			list.add(winnerCck.get(0));
			list.add(winnerCck.get(1));
			list.add(winnerCck.get(2));
		}else {
			list.addAll(winnerCck);
		}
		//通报前三名
		//发送title消息
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		Iterator<? extends Player> iterator = onlinePlayers.iterator();
		if (winnerCck.size() > 0) {
			while(iterator.hasNext()) {
				Player player = iterator.next();
				String value ="§6§l";
				for (PlayerBetDate playerBetDate : list) {
					value = value +"【"+playerBetDate.getPlayerName()+"】"+" ";
				}
				player.sendMessage("§a第§6"+historyLotteryResults.getPeriods()+"§a期【猜猜看】结果：§6"+historyLotteryResults.getResult());
				if (api == null) {
					player.sendMessage("§a[§c公告§a]:§a恭喜以下玩家在§6【猜猜看】§a中获得前三甲！");
					player.sendMessage("§a[§c公告§a]:" + value);
				}else{
					api.sendSubtitle(player,value,100,100,100);
					api.sendTitle(player,"§a恭喜以下玩家在§6【猜猜看】§a中获得前三甲！",100,100,100);
					api.sendActionbar(player,"§a第§6"+historyLotteryResults.getPeriods()+"§a期【猜猜看】结果：§6"+historyLotteryResults.getResult());
				}
			}
		}else {
			while(iterator.hasNext()) {
				Player player = iterator.next();
				player.sendMessage("§a第§6"+historyLotteryResults.getPeriods()+"§a期【猜猜看】结果：§6"+historyLotteryResults.getResult());
			}
		}

		winnerCck.clear();
		betCommand.clearCurrentCckBetList();
		return true;
	}

	private Boolean lottery2(HistoryLotteryResults historyLotteryResults) {
		//获取押注的玩家数据
		List<PlayerBetDate> currentDbBetList = betCommand.getCurrentDbBetList();
		if (currentDbBetList == null) {
			return false;
		}
		for (PlayerBetDate playerBetDate : currentDbBetList) {
			Integer betAmount = playerBetDate.getBetAmount();
			Double betAmountRes = 0.00;
			String betType = playerBetDate.getBetType();
			if (!betType.equals("龙") && (historyLotteryResults.getResult()).contains(betType)) {
				betAmountRes = betAmount + (betAmount * 1.0);
			}else if (betType.equals("龙") && (historyLotteryResults.getResult()).contains(betType)){
				betAmountRes = betAmount + (betAmount * 10.0);
			}
			//发放奖励
			String playerName = playerBetDate.getPlayerName();
			Player player = Bukkit.getServer().getPlayer(playerName);
			UUID uniqueId = player.getUniqueId();
			if (betAmountRes > 0.00) {
				winnerDb.add(playerBetDate);
                String str = betAmountRes.toString();
				playerPointsAPI.give(uniqueId,Integer.valueOf(str.substring(0,str.indexOf("."))));
				player.sendMessage("§a恭喜您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"点卷"+"§6]"+"§a在本期【点卷夺宝】中获得："+betAmountRes+"点卷");
			}else {
				player.sendMessage("§b很遗憾...您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"点卷"+"§6]"+"§b在本期【点卷夺宝】中没有中奖");
			}
		}
		//排序
		for(int i=0;i<winnerDb.size();i++) {
			for (int j = 0; j < winnerDb.size() - 1 - i; j++) {
				if (winnerDb.get(j).getBetAmount() < winnerDb.get(j + 1).getBetAmount()) {
					Integer temp = winnerDb.get(j).getBetAmount();
					winnerDb.get(j).setBetAmount(winnerDb.get(j + 1).getBetAmount());
					winnerDb.get(j + 1).setBetAmount(temp);
				}
			}
		}
		//取前三
		List<PlayerBetDate> list = new ArrayList<>();
		if (winnerDb.size() > 3) {
			list.add(winnerDb.get(0));
			list.add(winnerDb.get(1));
			list.add(winnerDb.get(2));
		}else {
			list.addAll(winnerDb);
		}
		//通报前三名
		//发送title消息
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		Iterator<? extends Player> iterator = onlinePlayers.iterator();
		if (winnerDb.size() > 0) {
			while(iterator.hasNext()) {
				Player player = iterator.next();
				String value ="§6§l";
				for (PlayerBetDate playerBetDate : list) {
					value = value +"【"+playerBetDate.getPlayerName()+"】"+" ";
				}
				player.sendMessage("§a第§6"+historyLotteryResults.getPeriods()+"§a期【点卷夺宝】结果：§6"+historyLotteryResults.getResult());
				if (api == null) {
					player.sendMessage("§a[§c公告§a]:§a恭喜以下玩家在§6【点卷夺宝】§a中获得前三甲！");
					player.sendMessage("§a[§c公告§a]:" + value);
				}else{
					api.sendSubtitle(player,value,100,100,100);
					api.sendTitle(player,"§a恭喜以下玩家在§6【点卷夺宝】§a中获得前三甲！",100,100,100);
					api.sendActionbar(player,"§a第§6"+historyLotteryResults.getPeriods()+"§a期【点卷夺宝】结果：§6"+historyLotteryResults.getResult());
				}
			}
		}else {
			while(iterator.hasNext()) {
				Player player = iterator.next();
				player.sendMessage("§a第§6"+historyLotteryResults.getPeriods()+"§a期【点卷夺宝】结果：§6"+historyLotteryResults.getResult());
			}
		}

		winnerDb.clear();
		betCommand.clearCurrentDbBetList();
		return true;
	}

	public Long getTimeCckMillis() {
		return timeCckMillis;
	}

	public void setTimeCckMillis(Long timeCckMillis) {
		this.timeCckMillis = timeCckMillis;
	}

	public Long getTimeDbMillis() {
		return timeDbMillis;
	}

	public void setTimeDbMillis(Long timeDbMillis) {
		this.timeDbMillis = timeDbMillis;
	}

	public BetDataHandler getBetDataHandler() {
		return betDataHandler;
	}

	public void setBetDataHandler(BetDataHandler betDataHandler) {
		this.betDataHandler = betDataHandler;
	}

	public PlayerPointsAPI getPlayerPointsAPI() {
		return playerPointsAPI;
	}

	public void setPlayerPointsAPI(PlayerPointsAPI playerPointsAPI) {
		this.playerPointsAPI = playerPointsAPI;
	}
}
