package top.qianxinyao.Main;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import top.qianxinyao.UserBasedCollaborativeRecommender.MahoutUserBasedCollaborativeRecommender;
import top.qianxinyao.UserBasedCollaborativeRecommender.quartz.CFCronTriggerRunner;
import top.qianxinyao.algorithms.PropGetKit;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.contentbasedrecommend.ContentBasedRecommender;
import top.qianxinyao.contentbasedrecommend.quartz.CBCronTriggerRunner;
import top.qianxinyao.dbconnection.DBKit;
import top.qianxinyao.hotrecommend.HotRecommender;
import top.qianxinyao.hotrecommend.quartz.HRCronTriggerRunner;

/**
 * @author Tom Qian
 * @email tomqianmaple@outlook.com
 * @github https://github.com/bluemapleman
 * @date 2017年12月11日
 * 设定/启动推荐任务的类
 */
public class JobSetter
{
	
	public static final Logger logger=Logger.getLogger(JobSetter.class);
	
	boolean enableCF,enableCB,enableHR;
	
	/**
	 * 
	 * @param enableCF 是否启用协同过滤(Collaborative Filtering)
	 * @param enableCB 是否启用基于内容的推荐(Content-Based Recommendation)
	 * @param enableHR 是否启用热点新闻推荐(Hot News Recommendation)
	 */
	public JobSetter(boolean enableCF,boolean enableCB,boolean enableHR) {
		//加载系统配置文件
		PropGetKit.loadProperties("paraConfig");
		//初始化操作：主要是数据库的连接
		DBKit.initalize();
		this.enableCF=enableCF;
		this.enableCB=enableCB;
		this.enableHR=enableHR;
	}
	
	
	/**
	 * 使用Quartz的表达式进行时间设定（默认为每天0点开始工作），详情请参照：http://www.quartz-scheduler.org/api/2.2.1/index.html(CronExpression)
	 * 当启用该方法时，推荐系统可以保持运行，直到被强制关闭。
	 * @param userList
	 */
	private void executeQuartzJob(List<Long> userList) {
		//设定推荐任务每天的执行时间
		String cronExpression=PropGetKit.getString("startAt");
		
		try
		{
			if(enableCF)
				new CFCronTriggerRunner().task(userList,cronExpression);
			if(enableCB)
				new CBCronTriggerRunner().task(userList,cronExpression);
			if(enableHR)
				new HRCronTriggerRunner().task(userList,cronExpression);
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		System.out.println("本次推荐结束于"+new Date());
	}
	
	
	/**
	 * 为指定用户执行定时新闻推荐
	 * @param goalUserList 目标用户的id列表
	 */
	public void executeQuartzJobForCertainUsers(List<Long> goalUserList) {
		executeQuartzJob(goalUserList);
	}
	
	/**
	 * 为所有用户执行定时新闻推荐
	 */
	public void executeQuartzJobForAllUsers() {
		executeQuartzJob(RecommendKit.getAllUsers());
	}
	
	/**
	 * 为活跃用户进行定时新闻推荐。
	 * @param goalUserList
	 */
	public void executeQuartzJobForActiveUsers() {
		executeQuartzJob(RecommendKit.getActiveUsers());
	}
	
	
	/**
	 * 执行一次新闻推荐
	 * 参数forActiveUsers表示是否只针对活跃用户进行新闻推荐，true为是，false为否。
	 * @param forActiveUsers
	 */
	private void executeInstantJob(List<Long> userIDList) {
		//让热点新闻推荐器预先生成今日的热点新闻
		HotRecommender.formTodayTopHotNewsList();
		
		if(enableCF)
			new MahoutUserBasedCollaborativeRecommender().recommend(userIDList);
		if(enableCB)
			new ContentBasedRecommender().recommend(userIDList);
		if(enableHR)
			new HotRecommender().recommend(userIDList);
		
		System.out.println("本次推荐结束于"+new Date());
	}
	
	/**
	 * 执行一次新闻推荐
	 * 参数forActiveUsers表示是否只针对活跃用户进行新闻推荐，true为是，false为否。
	 * @param forActiveUsers
	 */
	public void executeInstantJobForCertainUsers(List<Long> goalUserList) {
		executeInstantJob(goalUserList);
	}
	
	/**
	 * 为所用有用户执行一次新闻推荐
	 */
	public void executeInstantJobForAllUsers() {
		executeInstantJob(RecommendKit.getAllUsers());
	}
	
	/**
	 * 为活跃用户进行一次推荐。
	 * @param goalUserList
	 */
	public void executeInstantJobForActiveUsers() {
		executeInstantJob(RecommendKit.getActiveUsers());
	}
}

