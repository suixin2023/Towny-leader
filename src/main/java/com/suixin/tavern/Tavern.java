package com.suixin.tavern;

import com.suixin.tavern.command.*;
import com.suixin.tavern.entity.*;
import com.suixin.tavern.handler.BetDataHandler;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.listener.HandlePlayerPrefix;
import com.suixin.tavern.handler.Ranks;
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
	private Ranks ranks;
	private HandlePlayerPrefix handlePlayerPrefix;
	private LeaderBoardCommand ladder;
	private CashCommand cash;
	private Long timeMillis;//cck本期开奖日期
	private TreeMap<Double, String> topPlayers;//cck本期获奖名单
	private TitleManagerAPI api;

	@Override
	public void onEnable() {
		this.log = getLogger();
		api = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManager");
		this.topPlayers = new TreeMap<>();
		this.databaseHandler = new DatabaseHandler(this);
		this.betDataHandler = new BetDataHandler(this);
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
		timeTask ();
	}


	public void timeTask () {
		new BukkitRunnable() {
			@Override
			public void run() {
				//刷新开奖时间
				long timeMillis = System.currentTimeMillis() + 30000l;
				//开奖
				int res = (new Random()).nextInt(6);
				int res2 = (new Random()).nextInt(6);
				int res3 = (new Random()).nextInt(6);
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
					if (lottery) {
						//覆盖最近开奖记录
						betDataHandler.SaveCckBetDate(historyLotteryResults);
					}
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
//			Player player = Bukkit.getServer().getPlayer(playerName);
//			UUID uniqueId = player.getUniqueId();//金币插件暂时不支持UUID
			VaultAPI.giveMoney(playerName,betAmountRes);
		}
		//通报前三名
		NavigableMap<Double, String> sortedMap = this.topPlayers.descendingMap();
		List<Map.Entry<Double, String>> list = new ArrayList<>();
		int number = 0;
		for (final Map.Entry<Double, String> entry : sortedMap.entrySet()) {
			if (number != 3) {
				list.add(entry);
			} else {
				break;
			}
			number++;
		}

		//发送title消息
		Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
		Iterator<? extends Player> iterator = onlinePlayers.iterator();
		while(iterator.hasNext()) {
			Player player = iterator.next();
			String value ="§6§l";
			for (Map.Entry<Double, String> entry : list) {
				value = value +"【"+entry.getValue()+"】"+" ";
			}
			api.sendSubtitle(player,value);
			api.sendTitle(player,"§a恭喜以下玩家在§6【猜猜看】§a中获得前三甲！");
			api.sendActionbar(player,"§a第§6"+historyLotteryResults.getPeriods()+"§a期猜猜看结果：§6"+historyLotteryResults.getResult());
		}

		sortedMap.clear();
		this.topPlayers.clear();
		return true;
	}


	public Long getTimeMillis() {
		return timeMillis;
	}

	public void setTimeMillis(Long timeMillis) {
		this.timeMillis = timeMillis;
	}
}
