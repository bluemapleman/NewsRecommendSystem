/**
 * 
 */
package top.qianxinyao.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import top.qianxinyao.UserBasedCollaborativeRecommender.MahoutUserBasedCollaborativeRecommender;
import top.qianxinyao.algorithms.PropGetKit;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.contentbasedrecommend.ContentBasedRecommender;
import top.qianxinyao.hotrecommend.HotRecommender;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月20日
 */
public class Main
{
    static ResultSet rs=null;
    
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException
	{
		//加载系统配置文件
		PropGetKit.loadProperties("paraConfig");
		//仅给最近一个月有活动的用户进行推荐动作
		List<String> users=RecommendKit.getActiveUsers();
		//设定推荐任务每天的执行时间
//		String cronExpression=PropGetKit.getString("startAt");
//		try
//		{
//			new CFCronTriggerRunner().task(users,cronExpression);
//			new CBCronTriggerRunner().task(users,cronExpression);
//			new HRCronTriggerRunner().task(users,cronExpression);
//		}
//		catch (SchedulerException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		HotRecommender.formTodayTopHotNewsList();
//		new MahoutUserBasedCollaborativeRecommender().recommend(users);
//		new ContentBasedRecommender().recommend(users);
		new HotRecommender().recommend(users);
		
		
		
		
		
	}
}

