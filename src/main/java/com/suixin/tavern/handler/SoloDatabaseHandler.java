package com.suixin.tavern.handler;

import com.suixin.tavern.entity.SoloEntity;
import com.suixin.tavern.util.JdbcUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;


public class SoloDatabaseHandler {

    //对局写入
    public static int soloInsert(SoloEntity soloEntity){
        JdbcUtil db = new JdbcUtil();
        db.openConnection();
        String sql = "insert into solo(player_name, type, money, num , state , result , status , created )"
                + " values(?, ?, ?, ?, ?, ?, ?, ?)";
        Object [] params = new Object[8];
        params[0]= soloEntity.getPlayerName();
        params[1]= soloEntity.getType();
        params[2]= soloEntity.getMoney();
        params[3]= soloEntity.getNum();
        params[4]= soloEntity.getState();
        params[5]= soloEntity.getResult();
        params[6]= soloEntity.getStatus();
        params[7]= soloEntity.getCreated();
        try {
            ResultSet rst= db.getInsertObjectIDs(sql, params);

            if (rst!=null && rst.first()) {
                return 1;
            }else {
                //创建表
                SoloDatabaseHandler.createTable();
                soloInsert(soloEntity);//重新写入
            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;

    }
    //创建表
    public static void createTable() {
        JdbcUtil db = new JdbcUtil();
        db.openConnection();
            String tableSql = "CREATE TABLE `solo` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `player_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '玩家名字'," +
                    "  `type` varchar(255) DEFAULT NULL COMMENT '对局类型'," +
                    "  `money` double(14,0) DEFAULT NULL COMMENT '金额'," +
                    "  `num` int(11) DEFAULT NULL COMMENT '挑战次数'," +
                    "  `state` int(2) DEFAULT NULL COMMENT '状态：1待挑战2已被挑战'," +
                    "  `result` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '挑战结果：输or赢'," +
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
            db.openConnection();
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
    private static void selectSoloData(Integer id){
        String sql = "select * from solo where id = ?";

        Object [] params = new Object[1];
        params[0]=id;
        try {
            JdbcUtil db = new JdbcUtil();
            db.openConnection();
            ResultSet rst = db.execQuery(sql);
            if (rst!=null) {
                while(rst.next()){
                    System.out.println(rst.getString("play_name"));
                    System.out.println(rst.getFloat("play_ticket_price"));
                    int playID=rst.getInt("play_id");

                    byte[] buf = new byte[256];
                    Blob blob = rst.getBlob("play_image");
                    if(blob!=null ){
                        //需要在在工程目录下建立路径Cache/Play_Image/，然后将照片缓存到该路径下
                        File file = new File("Cache/Play_Image/"+ playID + ".jpg");
                        FileOutputStream sout = new FileOutputStream(file);
                        InputStream in = blob.getBinaryStream();//获取BLOB数据的输入数据流

                        for (int i = in.read(buf); i != -1;) {
                            sout.write(buf);
                            i = in.read(buf);
                        }
                        in.close();
                        sout.close();
                    }

                }
            }

            db.close(rst);
            db.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
//        createTable();
        SoloEntity soloEntity = new SoloEntity();
        soloEntity.setPlayerName("测试");
        soloEntity.setType("老虎");
        soloEntity.setMoney(new Double("0.00"));
        soloEntity.setNum(10);
        soloEntity.setState(1);
        soloEntity.setResult("输");
        soloEntity.setStatus(1);
        soloEntity.setCreated(new Date());
        soloInsert(soloEntity);
    }

}