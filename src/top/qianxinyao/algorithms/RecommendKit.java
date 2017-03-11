/**
 * 
 */
package top.qianxinyao.algorithms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import top.qianxinyao.contentbasedrecommend.CustomizedHashMap;
import top.qianxinyao.dbconnection.ConnectionFactory;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月21日 提供推荐算法通用的一些方法
 */
public class RecommendKit
{
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
	public static void filterOutDateNews(Collection<String> col, String userId)
	{
		try
		{
			String newsids = getInQueryString(col.iterator());
			if (!newsids.equals("()"))
			{
				ResultSet rs = ConnectionFactory.getStatement()
						.executeQuery("select newsid,ntime from news where newsid in " + newsids);
				while (rs.next())
				{
					if (rs.getTimestamp(2).before(getInRecTimestamp(beforeDays)))
					{
						col.remove(rs.getString(1));
					}
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 过滤方法filterBrowsedNews() 过滤掉已经用户已经看过的新闻
	 */
	public static void filterBrowsedNews(Collection<String> col, String userId)
	{
		try
		{
			Statement stmt = ConnectionFactory.getNewStatement();
			ResultSet rs;
			rs = stmt.executeQuery("select nlnewsid from newslogs where nluserid='" + userId + "'");
			while (rs.next())
			{
				if (col.contains(rs.getString(1)))
				{
					col.remove(rs.getString(1));
				}
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 过滤方法filterReccedNews() 过滤掉已经推荐过的新闻（在recommend表中查找）
	 */
	public static void filterReccedNews(Collection<String> col, String userId)
	{
		try
		{
			Statement stmt = ConnectionFactory.getNewStatement();
			ResultSet rs;
			rs = stmt.executeQuery("select rnewsid from recommend where ruserid='" + userId + "' and rrectime>"+getInRecDate());
			while (rs.next())
			{
				if (col.contains(rs.getString(1)))
				{
					col.remove(rs.getString(1));
				}
			}
		}
		catch (SQLException e)
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
	public static ArrayList<String> getUserList()
	{
		ArrayList<String> users = new ArrayList<String>();
		try
		{
			ResultSet rs = ConnectionFactory.getNewStatement().executeQuery("select userid from users");
			while (rs.next())
			{
				users.add(rs.getString(1));
			}
		}
		catch (SQLException e)
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
	public static HashMap<String, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> getUserPrefListMap(
			Collection<String> userSet)
	{
		ResultSet rs = null;
		HashMap<String, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = null;
		try
		{
			String userPrefListQuery = getInQueryStringWithSingleQuote(userSet.iterator());
			if (!userPrefListQuery.equals("()"))
			{
				rs = ConnectionFactory.getNewStatement()
						.executeQuery("select userid,upreflist from users where userid in " + userPrefListQuery);
				userPrefListMap = new HashMap<String, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>>();
				while (rs.next())
				{
					userPrefListMap.put(rs.getString(1), JsonKit.jsonPrefListtoMap(rs.getString(2)));
				}
			}
		}
		catch (SQLException e)
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
	public static void insertRecommend(String userId, Iterator<String> newsIte, int recAlgo)
	{
		try
		{
			String insertValues = "";
			while (newsIte.hasNext())
			{
				insertValues += "(" + userId + "," + newsIte.next() + ",'" + new Timestamp(System.currentTimeMillis())
						+ "'," + recAlgo + "),";
			}
			if (insertValues.length() > 0)
			{
				insertValues = insertValues.substring(0, insertValues.length() - 1);
				ConnectionFactory.getNewStatement()
						.execute("insert into recommend (ruserid,rnewsid,rrectime,rrecalgo) values " + insertValues);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Acquire list of "active" users' ids
	 * "Active" means who use app recently(determined by method getInRecDate())
	 * 
	 * @return
	 */
	public static List<String> getActiveUsers()
	{
		try
		{
			int activeDay=PropGetKit.getInt("activeDay");
			ResultSet rs1;
			rs1 = ConnectionFactory.getNewStatement()
					.executeQuery("select distinct nluserid from newslogs where nltime>" + getInRecDate(activeDay));
			List<String> users = new ArrayList<String>();
			while (rs1.next())
			{
				users.add(rs1.getString(1));
			}
			return users;
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("return null");
		return null;
	}

	/**
	 * 去除数量上超过为算法设置的推荐结果上限值的推荐结果
	 * 
	 * @param set
	 * @param N
	 * @return
	 */
	public static void removeOverNews(Set<String> set, int N)
	{
		int i = 0;
		Iterator<String> ite = set.iterator();
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
