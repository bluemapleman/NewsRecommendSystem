package top.qianxinyao.Main;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import top.qianxinyao.UserBasedCollaborativeRecommender.MahoutUserBasedCollaborativeRecommender;
import top.qianxinyao.UserBasedCollaborativeRecommender.quartz.CFCronTriggerRunner;
import top.qianxinyao.algorithms.PropGetKit;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.contentbasedrecommend.ContentBasedRecommender;
import top.qianxinyao.contentbasedrecommend.quartz.CBCronTriggerRunner;
import top.qianxinyao.hotrecommend.HotRecommender;
import top.qianxinyao.hotrecommend.quartz.HRCronTriggerRunner;
import top.qianxinyao.model.Users;

/**
 * @author Tom Qian
 * @email tomqianmaple@outlook.com
 * @github https://github.com/bluemapleman
 * @date 2017年12月11日
 * 使用Quartz库设定推荐系统每天固定的工作时间（默认为每天0点开始工作）
 * 当启用该类时，推荐系统可以保持运行，直到被强制关闭。
 */
public class JobSetter
{
	
	public static final Logger logger=Logger.getLogger(JobSetter.class);
	
	/**
	 * 使用Quartz的表达式进行时间设定，详情请参照：http://www.quartz-scheduler.org/api/2.2.1/index.html(CronExpression)
	 * 参数forActiveUsers表示是否只针对活跃用户进行新闻推荐，true为是，false为否。
	 * @param forActiveUsers
	 */
	public void executeQuartzJob(boolean forActiveUsers) {
		//加载系统配置文件
		PropGetKit.loadProperties("paraConfig");
		
		List<Users> userList=forActiveUsers?RecommendKit.getActiveUsers():RecommendKit.getAllUsers();
		
		//设定推荐任务每天的执行时间
		String cronExpression=PropGetKit.getString("startAt");
		
		try
		{
			new CFCronTriggerRunner().task(userList,cronExpression);
			new CBCronTriggerRunner().task(userList,cronExpression);
			new HRCronTriggerRunner().task(userList,cronExpression);
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		logger.info("本次推荐结束！");
	}
	
	/**
	 * 执行一次新闻推荐
	 * 参数forActiveUsers表示是否只针对活跃用户进行新闻推荐，true为是，false为否。
	 * @param forActiveUsers
	 */
	public void executeInstantJob(boolean forActiveUsers) {
		//加载系统配置文件
		PropGetKit.loadProperties("paraConfig");
		
		List<Users> userList=forActiveUsers?RecommendKit.getActiveUsers():RecommendKit.getAllUsers();
		
		List<Long> userIDList=new ArrayList<Long>();
		
		for(Users user:userList)
			userIDList.add(user.getId());
		
		HotRecommender.formTodayTopHotNewsList();
		new MahoutUserBasedCollaborativeRecommender().recommend(userIDList);
		new ContentBasedRecommender().recommend(userIDList);
		new HotRecommender().recommend(userIDList);
		
		logger.info("本次推荐结束！");
	}
}

