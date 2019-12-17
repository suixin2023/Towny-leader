package com.suixin.tavern.handler;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.entity.HistoryLotteryResults;
import com.suixin.tavern.entity.PlayerBetDate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BetDataHandler {
    private final Tavern tavern;
    private HistoryLotteryResults historyLotteryResults;
    List<PlayerBetDate> currentBetList =new ArrayList<>();

    public BetDataHandler(final Tavern tavern) {
        this.tavern = tavern;
        this.historyLotteryResults = new HistoryLotteryResults();
        this.SaveDbConfig();
        this.initializationYmal();
    }
    //保存本期开奖信息
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
            }
        }
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("开奖期数", historyLotteryResults.getPeriods());
        config.set("玩法类型", historyLotteryResults.getGameType());
        config.set("开奖结果", historyLotteryResults.getResult());
        try {
            config.save(file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    //初始化配置文件
    public void initializationYmal() {
        HistoryLotteryResults historyLotteryResults = new HistoryLotteryResults();
        historyLotteryResults.setPeriods("0");
        historyLotteryResults.setGameType("猜猜看");
        historyLotteryResults.setResult("无");
        SaveCckBetDate(historyLotteryResults);
        historyLotteryResults.setGameType("点卷夺宝");
        SaveCckBetDate(historyLotteryResults);

    }

    //读取开奖数据
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
        this.historyLotteryResults.setGameType(config.getString("玩法类型"));
        this.historyLotteryResults.setPeriods(config.getString("开奖期数"));
        this.historyLotteryResults.setResult(config.getString("开奖结果"));
    }

    //读取数据库配置数据
    public FileConfiguration LoadDbData( ) {
        final File file = new File(this.tavern.getDataFolder(), "db.yml");
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config;
    }

    public void SaveDbConfig() {
        final File file = new File(this.tavern.getDataFolder(), "db.yml");

        if (!file.exists()) {
            this.tavern.getDataFolder().mkdirs();
            final FileConfiguration config = new YamlConfiguration();
            config.set("Mysql.dbDriver", "com.mysql.cj.jdbc.Driver");
            config.set("Mysql.dbURL", "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone = GMT");
            config.set("Mysql.userName", "root");
            config.set("Mysql.password", "1234");
            try {
                config.save(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
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
