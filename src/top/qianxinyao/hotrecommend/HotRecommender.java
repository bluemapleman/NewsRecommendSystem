/**
 * 
 */
package top.qianxinyao.hotrecommend;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import top.qianxinyao.algorithms.RecommendAlgorithm;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.model.Newslogs;
import top.qianxinyao.model.Recommendations;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月30日 基于“热点新闻”生成的推荐，一般用于在CF和CB算法推荐结果数较少时进行数目的补充
 */
public class HotRecommender implements RecommendAlgorithm
{
	
	public static final Logger logger=Logger.getLogger(HotRecommender.class);
	
	// 热点新闻的有效时间
	public static int beforeDays = -10;
	// 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
	public static int TOTAL_REC_NUM = 20;
	// 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
	private static ArrayList<Long> topHotNewsList = new ArrayList<Long>();

	@Override
	public void recommend(List<Long> users)
	{
		System.out.println("HR start at "+new Date());
		int count=0;
		Timestamp timestamp = getCertainTimestamp(0, 0, 0);
		for (Long userId : users)
		{
			try
			{
				//获得已经预备为当前用户推荐的新闻，若数目不足达不到单次的最低推荐数目要求，则用热点新闻补充
				Recommendations recommendation=Recommendations.dao.findFirst("select user_id,count(*) as recnums from recommendations where derive_time>'" + timestamp
								+ "' and user_id='" + userId + "' group by user_id");
				
				boolean flag=(recommendation!=null);
				
				int delta=flag?TOTAL_REC_NUM - recommendation.getInt("recnums"):TOTAL_REC_NUM;
				Set<Long> toBeRecommended = new HashSet<Long>();
				if (delta > 0)
				{
					int i = topHotNewsList.size() > delta ? delta : topHotNewsList.size();
					while (i-- > 0)
						toBeRecommended.add(topHotNewsList.get(i));
				}
				RecommendKit.filterBrowsedNews(toBeRecommended, userId);
				RecommendKit.filterReccedNews(toBeRecommended, userId);
				RecommendKit.insertRecommend(userId, toBeRecommended.iterator(), RecommendAlgorithm.HR);
				count+=toBeRecommended.size();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("HR has contributed " + (users.size()==0?0:count/users.size()) + " recommending news on average");
		System.out.println("HR end at "+new Date());

	}

	public static void formTodayTopHotNewsList()
	{
		topHotNewsList.clear();
		ArrayList<Long> hotNewsTobeReccommended = new ArrayList<Long>();
		try
		{
			List<Newslogs> newslogsList=Newslogs.dao.find("select news_id,count(*) as visitNums from newslogs where view_time>"
							+ RecommendKit.getInRecDate(beforeDays) + " group by news_id order by visitNums desc");
			for (Newslogs newslog:newslogsList)
			{
				hotNewsTobeReccommended.add(newslog.getNewsId());
			}
			for (Long news : hotNewsTobeReccommended)
			{
				topHotNewsList.add(news);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static List<Long> getTopHotNewsList()
	{
		return topHotNewsList;
	}

	public static int getTopHopNewsListSize()
	{
		return topHotNewsList.size();
	}

	private Timestamp getCertainTimestamp(int hour, int minute, int second)
	{
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.set(Calendar.HOUR_OF_DAY, hour); // 设置为前beforeNum天
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		return new Timestamp(calendar.getTime().getTime());
	}
}
