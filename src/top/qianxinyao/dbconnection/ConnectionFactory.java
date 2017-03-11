/**
 * 
 */
package top.qianxinyao.dbconnection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLBooleanPrefJDBCDataModel;
import org.postgresql.jdbc3.Jdbc3SimpleDataSource;


/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月21日
 */
public class ConnectionFactory
{
	public static final Logger logger = Logger.getLogger(ConnectionFactory.class);
	//偏好表表名
	public static final String PREF_TABLE="newslogs";  
	//用户id列名
	public static final String PREF_TABLE_USERID="nlonguserid";
	//新闻id列名
	public static final String PREF_TABLE_NEWSID="nlnewsid";
	//偏好值列名
	public static final String PREF_TABLE_PREFVALUE="nprefer";
	//用户浏览时间列名
	public static final String PREF_TABLE_TIME="nltime";
	
	public static final String MYSQL="com.mysql.jdbc.Driver";
	public static final String POSTGRE="org.postgresql.Driver";
	
	public static Connection conn;
	
	public static Statement stmt;

//	private static void initalize(String database)
//	{
//		try
//		{
//			Class.forName(database).newInstance();
//			HashMap<String, String> info = getDBInfo();
//			conn = (Connection) DriverManager.getConnection(info.get("url"), info.get("user"), info.get("password"));
//		}
//		catch (ClassNotFoundException e)
//		{
//			logger.error("找不到驱动程序类 ，加载驱动失败！");
//		}
//		catch (SQLException se)
//		{
//			logger.error("数据库连接失败！");
//		}
//		catch (Exception e)
//		{
//			logger.error("数据库连接初始化错误！");
//		}
//		return;
//	}

	public static Connection getConnection()
	{
		if (null == conn)
		{
			try
			{
				conn=getDataSource().getConnection();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return conn;
	}
	
	public static Statement getStatement(){
		if(null==stmt){
			try
			{
				stmt=getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return stmt;
	}
	
	public static Statement getNewStatement(){
		Statement watchedStmt=null;
		try
		{
			watchedStmt=getConnection().createStatement();
			new StatementWatcher(watchedStmt).start();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return watchedStmt;
	}

	public static DataSource getDataSource()
	{
		Jdbc3SimpleDataSource dataSource=new Jdbc3SimpleDataSource();
		
		HashMap<String,String> info=getDBInfo();
		dataSource.setUrl(info.get("url"));
		dataSource.setUser(info.get("user"));
		dataSource.setPassword(info.get("password"));
		return dataSource;
	}
	
	public static PostgreSQLBooleanPrefJDBCDataModel getPostgreSQLBooleanPrefJDBCDataModel(){
		return new PostgreSQLBooleanPrefJDBCDataModel(ConnectionFactory.getDataSource(), PREF_TABLE, PREF_TABLE_USERID,
				PREF_TABLE_NEWSID,PREF_TABLE_TIME);
	}
	
	public static MySQLJDBCDataModel getMySQLJDBCDataModel(){
		return new MySQLJDBCDataModel(ConnectionFactory.getDataSource(), "user_likes", "uid",
				"nid", "likes", "recording_time");
	}

	private static HashMap<String, String> getDBInfo()
	{
		HashMap<String, String> info = null;
		try
		{
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir") + "/res/dbconfig.properties"));
			info = new HashMap<String, String>();
			info.put("url", p.getProperty("url"));
			info.put("user", p.getProperty("user"));
			info.put("password", p.getProperty("password"));
		}
		catch (FileNotFoundException e)
		{
			logger.error("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在");
		}
		catch (IOException e)
		{
			logger.error("装载文件--->失败!");
		}
		return info;
	}

}
