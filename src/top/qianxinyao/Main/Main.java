/**
 * 
 */
package top.qianxinyao.Main;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import top.qianxinyao.algorithms.PropGetKit;
import top.qianxinyao.dbconnection.DBKit;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月20日
 */
public class Main
{
	
	public static final Logger logger = Logger.getLogger(Main.class);
	
//    static ResultSet rs=null;
    
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException
	{
		//加载系统配置文件
		PropGetKit.loadProperties("paraConfig");
		
		//初始化操作：主要是数据库的连接
		DBKit.initalize();
		
		new JobSetter().executeInstantJob(true);
		
	}
}

