/**
 * 
 */
package top.qianxinyao.UserBasedCollaborativeRecommender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLBooleanPrefJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import top.qianxinyao.algorithms.PropGetKit;
import top.qianxinyao.algorithms.RecommendAlgorithm;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.dbconnection.ConnectionFactory;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月18日 协同过滤
 */

/**
 * Collaborative-Based Filter 基于用户的协同过滤
 * 
 */
public class MahoutUserBasedCollaborativeRecommender implements RecommendAlgorithm
{
	public static final Logger logger = Logger.getLogger(MahoutUserBasedCollaborativeRecommender.class);

	/**
	 * 对应计算相似度时的时效天数
	 */
	private static int inRecDays = PropGetKit.getInt("CFValidDay");

	/**
	 * 给每个用户推荐的新闻的条数
	 */
	public static int N =PropGetKit.getInt("CFRecNum");

	/**
	 * 给特定的一批用户进行新闻推荐
	 * 
	 * @param 目标用户的id列表
	 */
	@SuppressWarnings("unused")
	@Override
	public void recommend(List<String> users)
	{
		int count=0;
		try
		{
			System.out.println("CF start at "+new Date());

			PostgreSQLBooleanPrefJDBCDataModel dataModel = ConnectionFactory.getPostgreSQLBooleanPrefJDBCDataModel();

			Statement stmt = ConnectionFactory.getNewStatement();

			ResultSet rs1 = stmt.executeQuery("select " + ConnectionFactory.PREF_TABLE_USERID + ","
					+ ConnectionFactory.PREF_TABLE_NEWSID + "," + ConnectionFactory.PREF_TABLE_TIME + " from newslogs");

			// 移除过期的用户浏览新闻行为，这些行为对计算用户相似度不再具有较大价值
			while (rs1.next())
			{
				if (rs1.getTimestamp(3).before(RecommendKit.getInRecTimestamp(inRecDays)))
				{
					dataModel.removePreference(Long.parseLong(rs1.getString(1)), Long.parseLong(rs1.getString(2)));
				}
			}

			UserSimilarity similarity = new LogLikelihoodSimilarity(dataModel);

			// NearestNeighborhood的数量有待考察
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, similarity, dataModel);

			Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

			for (String user : users)
			{
				long start = System.currentTimeMillis();

				Long userid = Long.parseLong(user);

				List<RecommendedItem> recItems = recommender.recommend(userid, N);

				Set<String> hs = new HashSet<String>();

				for (RecommendedItem recItem : recItems)
				{
					hs.add(String.valueOf(recItem.getItemID()));
				}

				// 过滤掉已推荐新闻和已过期新闻
				RecommendKit.filterOutDateNews(hs, String.valueOf(userid));
				RecommendKit.filterReccedNews(hs, String.valueOf(userid));

				// 无可推荐新闻
				if (hs == null)
				{
					continue;
				}

				if(hs.size()>N){
					RecommendKit.removeOverNews(hs, N);
				}
				
				RecommendKit.insertRecommend(String.valueOf(userid), hs.iterator(),RecommendAlgorithm.CF);
				
				count+=hs.size();
			}
		}
		catch (TasteException e)
		{
			logger.error("CB算法构造偏好对象失败！");
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			logger.error("CB算法数据库操作失败！");
			e.printStackTrace();
		}
		System.out.println("CF has contributed " + (count/users.size()) + " recommending news on average");
		System.out.println("CF finish at "+new Date());
		return;
	}

	public int getRecNums()
	{
		return N;
	}

	public void setRecNums(int recNums)
	{
		N = recNums;
	}
}
