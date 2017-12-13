/**
 * 
 */
package top.qianxinyao.UserBasedCollaborativeRecommender.quartz;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import top.qianxinyao.UserBasedCollaborativeRecommender.MahoutUserBasedCollaborativeRecommender;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月23日
 * 每天定时根据用户当日的新闻浏览记录来更新用户的喜好关键词列表
 */
public class CFJob implements Job
{
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		List<Long> users=(List<Long>) arg0.getJobDetail().getJobDataMap().get("users");
		new MahoutUserBasedCollaborativeRecommender().recommend(users);
	}

}

