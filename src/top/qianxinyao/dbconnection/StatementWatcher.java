/**
 * 
 */
package top.qianxinyao.dbconnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月23日
 */
public class StatementWatcher extends Thread
{
	private Statement watchedStmt;
	public StatementWatcher(Statement watchedStmt){
		this.watchedStmt=watchedStmt;
	}
	public void run(){
		//防止还未开始
		try
		{
			Thread.sleep(20000);
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			try
			{
				if(this.watchedStmt.isClosed()){
					break;
				}
				else{
				    this.watchedStmt.close();
				}
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}

