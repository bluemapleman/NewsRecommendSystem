/**
 * 
 */
package top.qianxinyao.hotrecommend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import top.qianxinyao.algorithms.RecommendAlgorithm;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.dbconnection.ConnectionFactory;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月30日 基于“热点新闻”生成的推荐，一般用于在CF和CB算法推荐结果数较少时进行数目的补充
 */
public class HotRecommender implements RecommendAlgorithm
{
	// 热点新闻的有效时间
	public static int beforeDays = -10;
	// 推荐系统每日为每位用户生成的推荐结果的总数，当CF与CB算法生成的推荐结果数不足此数时，由该算法补充
	public static int TOTAL_REC_NUM = 20;
	// 将每天生成的“热点新闻”ID，按照新闻的热点程度从高到低放入此List
	private static ArrayList<String> topHotNewsList = new ArrayList<String>();

	@Override
	public void recommend(List<String> users)
	{
		System.out.println("HR start at "+new Date());
		int count=0;
		Timestamp timestamp = getCertainTimestamp(0, 0, 0);
		for (String userId : users)
		{
			try
			{
				ResultSet rs = ConnectionFactory.getNewStatement()
						.executeQuery("select ruserid,count(*) as recnums from recommend where rrectime>'" + timestamp
								+ "' and ruserid='" + userId + "' group by ruserid");
				boolean flag=rs.next();
				int delta=flag?TOTAL_REC_NUM - rs.getInt("recnums"):TOTAL_REC_NUM;
				Set<String> toBeRecommended = new HashSet<String>();
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
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("HR has contributed " + (count/users.size()) + " recommending news on average");
		System.out.println("HR end at "+new Date());

	}

	public static void formTodayTopHotNewsList()
	{
		topHotNewsList.clear();
		ArrayList<String> hotNewsTobeReccommended = new ArrayList<String>();
		try
		{
			ResultSet rs = ConnectionFactory.getNewStatement()
					.executeQuery("select nlnewsid,count(*) as visitNums from newslogs where nltime>"
							+ RecommendKit.getInRecDate(beforeDays) + " group by nlnewsid order by visitNums desc");
			while (rs.next())
			{
				hotNewsTobeReccommended.add(rs.getString(1));
			}
			for (String news : hotNewsTobeReccommended)
			{
				topHotNewsList.add(news);
			}
			System.out.println(topHotNewsList);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static List<String> getTopHotNewsList()
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
