package com.suixin.tavern;

import com.suixin.tavern.command.*;
import com.suixin.tavern.entity.*;
import com.suixin.tavern.handler.BetDataHandler;
import com.suixin.tavern.handler.DatabaseHandler;
import com.suixin.tavern.listener.HandlePlayerPrefix;
import com.suixin.tavern.handler.Ranks;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.List;
import java.util.Random;
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
	private Date date;

	@Override
	public void onEnable() {
		this.log = getLogger();
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
				date = new Date();
				//开奖
				int res = (new Random()).nextInt(6);
				int res2 = (new Random()).nextInt(6);
				int res3 = (new Random()).nextInt(6);
				HistoryLotteryResults historyLotteryResults = new HistoryLotteryResults();
				historyLotteryResults.setGameType("猜猜看");
				//查询上期猜猜看开奖记录
				betDataHandler.LoadCckBetData(historyLotteryResults);
				HistoryLotteryResults lotteryResults = betDataHandler.getHistoryLotteryResults();
				Integer periods = Integer.valueOf(lotteryResults.getPeriods());
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
			Integer betAmountRes = new Integer(0);
			String betType = playerBetDate.getBetType();
			if (!betType.equals("豹子") && betType.equals(historyLotteryResults.getResult())) {
				Integer betAmount = playerBetDate.getBetAmount();
				betAmountRes = betAmount*2;
			}else if (betType.equals("豹子") && betType.equals(historyLotteryResults.getResult())){
				Integer betAmount = playerBetDate.getBetAmount();
				betAmountRes = betAmount*10;
			}
			//发放奖励

		}
		//通报前三名
		return null;
	}
}
