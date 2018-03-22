/**
 * 
 */
package top.qianxinyao.algorithms;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import top.qianxinyao.contentbasedrecommend.CustomizedHashMap;
import top.qianxinyao.model.News;
import top.qianxinyao.model.Newslogs;
import top.qianxinyao.model.Recommendations;
import top.qianxinyao.model.Users;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月21日 提供推荐算法通用的一些方法
 */
public class RecommendKit
{
	
	public static final Logger logger=Logger.getLogger(RecommendKit.class);
	
	/**
	 * 推荐新闻的时效性天数，即从推荐当天开始到之前beforeDays天的新闻属于仍具有时效性的新闻，予以推荐。
	 */
	private static int beforeDays = PropGetKit.getInt("beforeDays");

	/**
	 * @return the inRecDate 返回时效时间的"year-month-day"的格式表示，方便数据库的查询
	 */
	public static String getInRecDate()
	{
		return getSpecificDayFormat(beforeDays);
	}
	
	/**
	 * @return the inRecDate 返回时效时间的"year-month-day"的格式表示，方便数据库的查询
	 */
	public static String getInRecDate(int beforeDays)
	{
		return getSpecificDayFormat(beforeDays);
	}

	/**
	 * @return the inRecDate 返回时效时间timestamp形式表示，方便其他推荐方法在比较时间先后时调用
	 */
	public static Timestamp getInRecTimestamp(int before_Days)
	{
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.add(Calendar.DAY_OF_MONTH, before_Days); // 设置为前beforeNum天
		return new Timestamp(calendar.getTime().getTime());
	}

	/**
	 * 过滤方法filterOutDateNews() 过滤掉失去时效性的新闻（由beforeDays属性控制）
	 */
	public static void filterOutDateNews(Collection<Long> col, Long userId)
	{
		try
		{
			String newsids = getInQueryString(col.iterator());
			if (!newsids.equals("()"))
			{
				List<News> newsList = News.dao.find("select id,news_time from news where id in " + newsids);
				for(News news:newsList)
				{
					if (news.getNewsTime().before(getInRecTimestamp(beforeDays)))
					{
						col.remove(news.getId());
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 过滤方法filterBrowsedNews() 过滤掉已经用户已经看过的新闻
	 */
	public static void filterBrowsedNews(Collection<Long> col, Long userId)
	{
		try
		{
			List<Newslogs> newslogsList = Newslogs.dao.find("select news_id from newslogs where user_id=?",userId);
			for (Newslogs newslog:newslogsList)
			{
				if (col.contains(newslog.getNewsId()))
				{
					col.remove(newslog.getNewsId());
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 过滤方法filterReccedNews() 过滤掉已经推荐过的新闻（在recommend表中查找）
	 */
	public static void filterReccedNews(Collection<Long> col, Long userId)
	{
		try
		{
			//但凡近期已经给用户推荐过的新闻，都过滤掉
			List<Recommendations> recommendationList = Recommendations.dao.find("select news_id from recommendations where user_id=? and derive_time>?",userId,getInRecDate());
			for (Recommendations recommendation:recommendationList)
			{
				if (col.contains(recommendation.getNewsId()))
				{
					col.remove(recommendation.getNewsId());
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有用户的Id列表
	 * 
	 * @return
	 */
	public static ArrayList<Long> getUserList()
	{
		ArrayList<Long> users = new ArrayList<Long>();
		try
		{
			List<Users> userList = Users.dao.find("select id from users");
			for (Users user:userList)
			{
				users.add(user.getId());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return users;
	}

	public static int getbeforeDays()
	{
		return beforeDays;
	}

	public static void setbeforeDays(int beforeDays)
	{
		RecommendKit.beforeDays = beforeDays;
	}

	public static String getSpecificDayFormat(int before_Days)
	{
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance(); // 得到日历
		calendar.add(Calendar.DAY_OF_MONTH, before_Days); // 设置为前beforeNum天
		Date d = calendar.getTime();
		return "'" + date_format.format(d) + "'";
	}

	/**
	 * 获取所有用户的喜好关键词列表
	 * 
	 * @return
	 */
	public static HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> getUserPrefListMap(
			Collection<Long> userSet)
	{
		HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = null;
		try
		{
			String userPrefListQuery = getInQueryStringWithSingleQuote(userSet.iterator());
			if (!userPrefListQuery.equals("()"))
			{
				List<Users> userList = Users.dao.find("select id,pref_list from users where id in " + userPrefListQuery);
				userPrefListMap = new HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>>();
				for (Users user:userList)
				{
					userPrefListMap.put(user.getId(), JsonKit.jsonPrefListtoMap(user.getPrefList()));
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userPrefListMap;
	}

	/**
	 * 用以select语句中使用in (n1，n2,n3...)范围查询的字符串拼接
	 * 
	 * @param ite
	 *            待查询对象集合的迭代器
	 * @return 若迭代集合不为空:"(n1,n2,n3)"，若为空："()"
	 */
	public static <T> String getInQueryString(Iterator<T> ite)
	{
		String inQuery = "(";
		while (ite.hasNext())
		{
			inQuery += ite.next() + ",";
		}
		if (inQuery.length() > 1)
		{
			inQuery = inQuery.substring(0, inQuery.length() - 1);
		}
		inQuery += ")";
		return inQuery;
	}

	public static <T> String getInQueryStringWithSingleQuote(Iterator<T> ite)
	{
		String inQuery = "(";
		while (ite.hasNext())
		{
			inQuery += "'" + ite.next() + "',";
		}
		if (inQuery.length() > 1)
		{
			inQuery = inQuery.substring(0, inQuery.length() - 1);
		}
		inQuery += ")";
		return inQuery;
	}

	/**
	 * 将推荐结果插入recommend表
	 * 
	 * @param userId
	 *            推荐目标用户id
	 * @param newsIte
	 *            待推荐新闻集合的迭代器
	 * @param recAlgo
	 *            标明推荐结果来自哪个推荐算法(RecommendAlgorithm.XX)
	 */
	public static void insertRecommend(Long userId, Iterator<Long> newsIte, int recAlgo)
	{
		try
		{
			while (newsIte.hasNext())
			{
				Recommendations rec=new Recommendations();
				rec.setUserId(userId);
				rec.setDeriveAlgorithm(recAlgo);
				rec.setNewsId(newsIte.next());
				rec.save();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Acquire a list of active users 
	 * "Active" means who read news recently ('recent' determined by method getInRecDate(), default in a month)
	 * 
	 * @return
	 */
	public static List<Long> getActiveUsers()
	{
		try
		{
			int activeDay=PropGetKit.getInt("activeDay");
			List<Users> userList=Users.dao.find("select distinct id,name from users where latest_log_time>" + getInRecDate(activeDay));
			List<Long> userIDList=new ArrayList<Long>();
			for(Users user:userList)
				userIDList.add(user.getId());
			return userIDList;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("获取活跃用户异常！");
		return null;
	}
	
	public static List<Long> getAllUsers(){
		try
		{
			List<Users> userList=Users.dao.find("select distinct id,name from users");
			List<Long> userIDList=new ArrayList<Long>();
			for(Users user:userList)
				userIDList.add(user.getId());
			return userIDList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.info("获取全体用户异常！");
		return null;
	}
	

	/**
	 * 去除数量上超过为算法设置的推荐结果上限值的推荐结果
	 * 
	 * @param set
	 * @param N
	 * @return
	 */
	public static void removeOverNews(Set<Long> set, int N)
	{
		int i = 0;
		Iterator<Long> ite = set.iterator();
		while (ite.hasNext())
		{
			if (i >= N)
			{
				ite.remove();
				ite.next();
			}
			else
			{
				ite.next();
			}
			i++;
		}
	}
}
