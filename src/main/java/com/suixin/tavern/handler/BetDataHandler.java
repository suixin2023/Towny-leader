package com.suixin.tavern.handler;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.entity.HistoryLotteryResults;
import com.suixin.tavern.entity.PlayerBetDate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class BetDataHandler {
    private Tavern tavern;
    private HistoryLotteryResults historyLotteryResults;
    List<PlayerBetDate> currentBetList =new ArrayList<>();

    public BetDataHandler(final Tavern tavern) {
        this.tavern = tavern;
        this.historyLotteryResults = new HistoryLotteryResults();
    }
    public BetDataHandler() {

    }
    //保存本期【猜猜看】开奖信息
    public void SaveCckBetDate(HistoryLotteryResults historyLotteryResults) {
        final File file = new File((new StringBuilder())
                .append(this.tavern.getDataFolder())
                .append(File.separator)
                .append("HistoryLotteryResults")
                .append(File.separator)
                .append(historyLotteryResults.getGameType())
                .append(".yml")
                .toString());
        if (!file.exists()) {
            this.tavern.getDataFolder().mkdir();
            try {
                file.createNewFile();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.addDefault("开奖期数", historyLotteryResults.getPeriods());
        config.addDefault("玩法类型", historyLotteryResults.getGameType());
        config.addDefault("开奖结果", historyLotteryResults.getResult());
        try {
            config.save(file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    //读取玩家的数据
    public void LoadCckBetData(HistoryLotteryResults historyLotteryResults) {
        final File file = new File((new StringBuilder())
                .append(this.tavern.getDataFolder())//返回存放插件文件数据的文件夹.
                .append(File.separator)//反斜杆
                .append("HistoryLotteryResults")
                .append(File.separator)
                .append(historyLotteryResults.getGameType())
                .append(".yml")
                .toString());
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config == null) {
            return;
        }
        this.historyLotteryResults.setGameType(config.getString("玩法类型"));
        this.historyLotteryResults.setPeriods(config.getString("开奖期数"));
        this.historyLotteryResults.setResult(config.getString("开奖结果"));
    }

    public List<PlayerBetDate> getCurrentBetList() {
        return currentBetList;
    }

    public void setCurrentBetList(List<PlayerBetDate> currentBetList) {
        this.currentBetList = currentBetList;
    }

    public HistoryLotteryResults getHistoryLotteryResults() {
        return historyLotteryResults;
    }

    public void setHistoryLotteryResults(HistoryLotteryResults historyLotteryResults) {
        this.historyLotteryResults = historyLotteryResults;
    }
}
