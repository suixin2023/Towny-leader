package com.suixin.tavern.handler;

import com.suixin.tavern.Tavern;
import com.suixin.tavern.entity.SoloEntity;
import com.suixin.tavern.util.JdbcUtil;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;

import java.util.*;
import java.util.Date;


public class SoloDatabaseHandler {
    private static Tavern tavern;
    static Boolean doCreate = true;

    public SoloDatabaseHandler(final Tavern tavern) {
        this.tavern = tavern;
    }

    //对局写入
    public static int soloInsert(SoloEntity soloEntity){
        JdbcUtil db = new JdbcUtil();
        db.openConnection(tavern.getBetDataHandler());
        String sql = "insert into solo(player_name, type, money , state , result , draw , status , created )"
                + " values(?, ?, ?, ?, ?, ?, ?, ?)";
        Object [] params = new Object[8];
        params[0]= soloEntity.getPlayerName();
        params[1]= soloEntity.getType();
        params[2]= soloEntity.getMoney();
        params[3]= soloEntity.getState();
        params[4]= soloEntity.getResult();
        params[5]= soloEntity.getDraw();
        params[6]= soloEntity.getStatus();
        params[7]= soloEntity.getCreated();
        try {
            ResultSet rst= db.getInsertObjectIDs(sql, params);
            if (rst != null) {
                return 1;
            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            if (doCreate) {
                //写入异常的時候创建表
                SoloDatabaseHandler.createTable();
                doCreate = false;
                soloInsert(soloEntity);//重新写入
            }
            e.printStackTrace();
        }
        return 0;

    }
    //创建表
    public static void createTable() {
        JdbcUtil db = new JdbcUtil();
        db.openConnection(tavern.getBetDataHandler());
            String tableSql = "CREATE TABLE `solo` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `player_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '玩家名字'," +
                    "  `type` varchar(255) DEFAULT NULL COMMENT '对局类型'," +
                    "  `money` double(14,0) DEFAULT NULL COMMENT '金额'," +
                    "  `state` int(2) DEFAULT NULL COMMENT '状态：1待挑战2已被挑战'," +
                    "  `result` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '挑战结果：输or赢'," +
                    "  `draw` int(11) DEFAULT NULL COMMENT '领取状态：1待领取 2已领取'," +
                    "  `status` int(2) DEFAULT NULL COMMENT '有效性：1有效-1无效'," +
                    "  `created` timestamp NULL DEFAULT NULL COMMENT '创建时间'," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8; ";
        try {
            db.execCommand(tableSql);
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //测试lob数据写入
    private static void test_insert_lob(){

        String sql = "insert into play(play_type_id, play_lang_id, play_name, play_ticket_price, play_image )"
                + " values(?,?, ?, ?, ?)";
        Object [] params = new Object[5];
        params[0]=null;
        params[1]=null;
        params[2]=new String("just a test");
        params[3]=new Float(5);

        FileInputStream fis = null;
        File file = new File("resource/image/header.jpg"); //测试写图片

        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection(tavern.getBetDataHandler());
            fis = new FileInputStream(file);
            params[4]=fis;

            ResultSet rst= db.getInsertObjectIDs(sql, params);

            if (rst!=null && rst.first()) {
                System.out.println(rst.getInt(1));
            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //查询对局数据
    public static List<SoloEntity> selectSoloData(Integer limit){
        String sql = "select * from solo where state = 1 ORDER BY id desc limit "+limit+",5";
        List<SoloEntity> list = new ArrayList<>();
        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection(tavern.getBetDataHandler());
            ResultSet rst = db.execQuery(sql);
            if (rst!=null) {
                while (rst.next()){
                    SoloEntity soloEntity = new SoloEntity();
                    soloEntity.setId(rst.getInt("id"));
                    soloEntity.setPlayerName(rst.getString("player_name"));
                    soloEntity.setMoney(rst.getDouble("money"));
                    list.add(soloEntity);
                }
            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    //查询对局编号
    public static SoloEntity selectSoloDataNum(Integer solonum){
        String sql = "select * from solo where state = 1 and id = "+solonum;
        SoloEntity soloEntity = new SoloEntity();
        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection(tavern.getBetDataHandler());
            ResultSet rst = db.execQuery(sql);
            if (rst!=null && rst.next()) {
                soloEntity.setId(rst.getInt("id"));
                soloEntity.setType(rst.getString("type"));
                soloEntity.setPlayerName(rst.getString("player_name"));
                soloEntity.setMoney(rst.getDouble("money"));

            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return soloEntity;
    }

    //修改对局状态
    public static void updateSoloDataNum(Integer solonum,String state){
        String sql;
            sql = "update solo set state = 2,result ='"+state+"' where id = "+solonum;
        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection(tavern.getBetDataHandler());
            db.execCommand(sql);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //查询总页数
    public static Integer selectSoloDataCount(){
        String sql = "select count(*) as datacount from solo where state = 1";
        Integer datacount=0;
        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection(tavern.getBetDataHandler());
            ResultSet rst = db.execQuery(sql);
            rst.next();
            datacount = rst.getInt("datacount");
            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return datacount;
    }

    public static void main(String[] args) {
//        createTable();
//        SoloEntity soloEntity = new SoloEntity();
//        soloEntity.setPlayerName("测试");
//        soloEntity.setType("老虎");
//        soloEntity.setMoney(new Double("0.00"));
//        soloEntity.setNum(10);
//        soloEntity.setState(1);
//        soloEntity.setResult("输");
//        soloEntity.setStatus(1);
//        soloEntity.setCreated(new Date());
//        soloInsert(soloEntity);
        String str = "15.0";
        String substring = str.substring(0, str.indexOf("."));
        System.out.println(substring);
    }

}