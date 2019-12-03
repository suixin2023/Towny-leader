package com.suixin.tavern.entity;

public class HistoryLotteryResults {
    private String periods;//开奖期数
    private String gameType;//玩法类型
    private String result;//开奖结果


    public String getPeriods() {
        return periods;
    }

    public void setPeriods(String periods) {
        this.periods = periods;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
