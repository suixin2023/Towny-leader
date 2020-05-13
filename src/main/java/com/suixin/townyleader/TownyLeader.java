package com.suixin.townyleader;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.sql.*;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class TownyLeader extends JavaPlugin implements CommandExecutor {
	public Logger log;
	private BetDataHandler betDataHandler;

	@Override
	public void onEnable() {
		this.log = getLogger();
		this.betDataHandler = new BetDataHandler(this);
		getCommand("townyleader").setExecutor(this);
		getLogger().info("==================[TownyLeader]==================");
		try {
			boolean connection = new JdbcUtil().openConnection(this.betDataHandler);//加载数据库驱动
			if (connection) {
				getLogger().info("Mysql连接成功");
			}else{
				getLogger().info("Mysql连接失败!请在配置文件中修改数据库信息后使用");
			}
		}catch (Exception e){
			getLogger().info("Mysql连接失败!请在配置文件中修改数据库信息后使用");
		}
		getLogger().info("==================[TownyLeader]==================");
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
		if (cmd.getName().equalsIgnoreCase("townyleader")) {
			List<String> argsList = Arrays.asList(args);
			if (argsList.size() != 4) {
				player.sendMessage("§c请输入/townyleader set mayor 城镇名 玩家名");
				return true;
			}
			String arg1 = argsList.get(0);
			String arg2 = argsList.get(1);
			String arg3 = argsList.get(2);
			String arg4 = argsList.get(3);
			if (arg1.equals("set") && arg2.equals("mayor")) {

				//设置玩家或者npc为城镇所有者
				setLeader(sender,player,arg3, arg4);
			}

		}
		return true;
	}

	private void setLeader (CommandSender sender,Player player, String arg3, String arg4){
		//判断玩家是否是国家领导人
		Integer num = selectMayorBetDate(player.getName());
		//权限判断
		if(num == 0){
			player.sendMessage("§c无权限，你不是国家领导人");
			return;
		}
		if(num == -1){
			player.sendMessage("§c数据库连接失败，请检查配置文件");
			return;
		}
		//查询玩家所在国家
		MayorBetDate mayorBetDate = selectMayorBetDateByplayerName(player.getName());
		MayorBetDate arg4mayorBetDate = new MayorBetDate();
		arg4mayorBetDate = selectMayorBetDateByplayerName(arg4);
		if (mayorBetDate == null) {
			player.sendMessage("§c您还没有国家");
			return;
		}
		if (arg4mayorBetDate.getTownyName() != null) {
			player.sendMessage("§c玩家必须退出原城镇");
			return;
		}
		//判断输入的城镇名是否属于这个国家
		MayorBetDate mayorBetDate1 = selectMayorBetDateByName(arg3);
		if (mayorBetDate1.getTownyName() == null || mayorBetDate1.getTownyName().equals("")) {
			player.sendMessage("§c输入的城镇不存在");
			return;
		}
		if (!mayorBetDate1.getNation().equals(mayorBetDate.getNation())) {
			player.sendMessage("§c您输入的城镇不属于这个国家");
			return;
		}
/*
		//查询国家所有居民
		List<String> list = new ArrayList<>();
		List<MayorBetDate> mayorBetDates = selectMayorBetDateByResident(mayorBetDate.getNation());
		for (MayorBetDate betDate : mayorBetDates) {
			String residents = betDate.getResidents();
			String[] allResidents = residents.split("#");
			for (String allResident : allResidents) {
				list.add(allResident);
			}
		}
		int usernum = 0;
		for (String s : list) {
			if (s.equals(arg4)) {
				usernum += 1;
			}
		}
		*/
		//修改城镇所有者
		Server server = getServer();
		ConsoleCommandSender consoleSender = server.getConsoleSender();
		Bukkit.getServer().dispatchCommand(consoleSender,"townyadmin set mayor "+arg3+" "+arg4);
//		updateMayor(arg4,arg3);
	}

    //查询玩家是否是国家领导人
	public Integer selectMayorBetDate(String name){
		String sql = "select count(*) as datacount from towny_towns tt" +
				" join towny_nations tn on tt.name = tn.capital" +
				" where tt.mayor = " + "'"+name+"'";
		Integer datacount=0;
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst == null) {
				return 0;
			}
			rst.next();
			String str = rst.getString("datacount");
			datacount = Integer.valueOf(str);
			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return datacount;
	}

	//查询玩家所在国家
	public MayorBetDate selectMayorBetDateByplayerName(String name){
		String sql = "select * from towny_towns where residents like '%"+name+"%'";
		MayorBetDate mayorBetDate = new MayorBetDate();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null && rst.next()) {
				mayorBetDate.setTownyName(rst.getString("name"));
				mayorBetDate.setNation(rst.getString("nation"));
				mayorBetDate.setMayor(rst.getString("mayor"));
				mayorBetDate.setResidents(rst.getString("residents"));
			}

			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDate;
	}

	//查询输入的城镇名是否属于这个国家
	public MayorBetDate selectMayorBetDateByName(String town){
		String sql = "select * from towny_towns where name = "+"'"+town+"'";
		MayorBetDate mayorBetDate = new MayorBetDate();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null && rst.next()) {
				mayorBetDate.setTownyName(rst.getString("name"));
				mayorBetDate.setNation(rst.getString("nation"));
				mayorBetDate.setMayor(rst.getString("mayor"));
				mayorBetDate.setResidents(rst.getString("residents"));
			}

			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDate;
	}

	//查询城镇所有居民
	public List<MayorBetDate> selectMayorBetDateByResident(String nation){
		String sql = "select * from towny_towns where nation = "+"'"+nation+"'";
		List<MayorBetDate> mayorBetDates = new ArrayList<>();
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			ResultSet rst = db.execQuery(sql);
			if (rst!=null) {
				while (rst.next()){
					MayorBetDate mayorBetDate = new MayorBetDate();
					mayorBetDate.setTownyName(rst.getString("name"));
					mayorBetDate.setNation(rst.getString("nation"));
					mayorBetDate.setMayor(rst.getString("mayor"));
					mayorBetDate.setResidents(rst.getString("residents"));
					mayorBetDates.add(mayorBetDate);
				}
			}
			db.close(rst);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mayorBetDates;
	}

	//修改城镇所有者
	public void updateMayor(String mayor,String name){
		String sql;
		sql = "update towny_towns set mayor = "+"'"+mayor+"'"+" where name = "+"'"+name+"'";
		try {
			JdbcUtil db = new JdbcUtil();
			db.openConnection(this.getBetDataHandler());
			db.execCommand(sql);
			db.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BetDataHandler getBetDataHandler() {
		return betDataHandler;
	}

	public void setBetDataHandler(BetDataHandler betDataHandler) {
		this.betDataHandler = betDataHandler;
	}
}