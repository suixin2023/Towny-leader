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
	private Long timeMillis;//cck本期开奖日期
	private List<PlayerBetDate> winner;//cck本期获奖名单
	private TitleManagerAPI api;

	@Override
	public void onEnable() {
		this.log = getLogger();
		this.winner = new ArrayList<>();
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
		if (api != null) {
			getLogger().info("TitleManager连接成功");
		}else{
			getLogger().info("TitleManager连接失败!插件将使用聊天栏通知");
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
				timeMillis = System.currentTimeMillis() + 300000l;
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
					Boolean lottery = lottery(historyLotteryResults);
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
				Boolean lottery = lottery(historyLotteryResults);
				if (lottery) {
					//覆盖最近开奖记录
					betDataHandler.SaveCckBetDate(historyLotteryResults);
				}
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
				timeMillis = System.currentTimeMillis() + 600000l;
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
					Boolean lottery = lottery(historyLotteryResults);
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
				Boolean lottery = lottery(historyLotteryResults);
				if (lottery) {
					//覆盖最近开奖记录
					betDataHandler.SaveCckBetDate(historyLotteryResults);
				}
				return;
			}

		}.runTaskTimer(this, 0L, 5*60*20L);
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
				winner.add(playerBetDate);
				VaultAPI.giveMoney(playerName,betAmountRes);
				player.sendMessage("§a恭喜您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"金"+"§6]"+"§a在本期【猜猜看】中获得："+betAmountRes+"金");
			}else {
				player.sendMessage("§b很遗憾...您押注的"+"§6["+playerBetDate.getBetType()+playerBetDate.getBetAmount()+"金"+"§6]"+"§b在本期【猜猜看】中没有中奖");
			}
		}
		//排序
		for(int i=0;i<winner.size();i++) {
			for (int j = 0; j < winner.size() - 1 - i; j++) {
				if (winner.get(j).getBetAmount() < winner.get(j + 1).getBetAmount()) {
					Integer temp = winner.get(j).getBetAmount();
					winner.get(j).setBetAmount(winner.get(j + 1).getBetAmount());
					winner.get(j + 1).setBetAmount(temp);
				}
			}
		}
		//取前三
        List<PlayerBetDate> list = new ArrayList<>();
        if (winner.size() > 0 && winner.size() > 3) {
            list.add(winner.get(0));
            list.add(winner.get(1));
            list.add(winner.get(2));
        }else {
            list.addAll(winner);
        }
		//通报前三名
		//发送title消息
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		Iterator<? extends Player> iterator = onlinePlayers.iterator();
		if (winner.size() > 0) {
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
					api.sendActionbar(player,"§a第§6"+historyLotteryResults.getPeriods()+"§a期猜猜看结果：§6"+historyLotteryResults.getResult());
				}
			}
		}else {
			while(iterator.hasNext()) {
				Player player = iterator.next();
				player.sendMessage("§a第§6"+historyLotteryResults.getPeriods()+"§a期【猜猜看】结果：§6"+historyLotteryResults.getResult());
			}
		}

		winner.clear();
		betCommand.clearCurrentCckBetList();
		return true;
	}


	public Long getTimeMillis() {
		return timeMillis;
	}

	public void setTimeMillis(Long timeMillis) {
		this.timeMillis = timeMillis;
	}

	public BetDataHandler getBetDataHandler() {
		return betDataHandler;
	}

	public void setBetDataHandler(BetDataHandler betDataHandler) {
		this.betDataHandler = betDataHandler;
	}
}
