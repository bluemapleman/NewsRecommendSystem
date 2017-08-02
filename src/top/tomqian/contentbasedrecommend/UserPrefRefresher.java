/**
 * 
 */
package top.qianxinyao.contentbasedrecommend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.ansj.app.keyword.Keyword;

import top.qianxinyao.algorithms.JsonKit;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.dbconnection.ConnectionFactory;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月3日 每次用户浏览新的新闻时，用以更新用户的喜好关键词列表
 */
public class UserPrefRefresher
{
	ResultSet rs=null;
	
	//设置TFIDF提取的关键词数目
	private static final int KEY_WORDS_NUM = 10;
	
	//每日衰减系数
	private static final double DEC_COEE=0.7;

	public void refresh(){
			refresh(RecommendKit.getUserList());
	}
	
	@SuppressWarnings("unchecked")
	public void refresh(Collection<String> userIdsCol){
			//首先对用户的喜好关键词列表进行衰减更新
			autoDecRefresh(userIdsCol);
			//用户浏览新闻纪录：userBrowsexMap:<String(userid),ArrayList<String>(newsid List)>
			HashMap<String,ArrayList<String>> userBrowsedMap=getBrowsedHistoryMap();
			
			//用户喜好关键词列表：userPrefListMap:<String(userid),String(json))>
			HashMap<String,CustomizedHashMap<Integer,CustomizedHashMap<String,Double>>> userPrefListMap=RecommendKit.getUserPrefListMap(userBrowsedMap.keySet());
			//新闻对应关键词列表与模块ID：newsTFIDFMap:<String(newsid),List<Keyword>>,<String(newsModuleId),Integer(moduleid)>
			HashMap<String,Object> newsTFIDFMap=getNewsTFIDFMap();
			
			//开始遍历用户浏览记录，更新用户喜好关键词列表
			//对每个用户（外层循环），循环他所看过的每条新闻（内层循环），对每个新闻，更新它的关键词列表到用户的对应模块中
			Iterator<String> ite=userBrowsedMap.keySet().iterator();
			
			while(ite.hasNext()){
				String userId=ite.next();
				ArrayList<String> newsList=userBrowsedMap.get(userId);
				for(String news:newsList){
					Integer moduleId=(Integer) newsTFIDFMap.get(news+"moduleid");
					//获得对应模块的（关键词：喜好）map
					CustomizedHashMap<String,Double> rateMap=userPrefListMap.get(userId).get(moduleId);
					//获得新闻的（关键词：TFIDF值）map
					List<Keyword> keywordList=(List<Keyword>) newsTFIDFMap.get(news);
					Iterator<Keyword> keywordIte=keywordList.iterator();
					while(keywordIte.hasNext()){
						Keyword keyword=keywordIte.next();
						String name=keyword.getName();
						if(rateMap.containsKey(name)){
							rateMap.put(name, rateMap.get(name)+keyword.getScore());
						}
						else{
							rateMap.put(name,keyword.getScore());
						}
					}
					userPrefListMap.get(userId);
				}
			}
			Iterator<String> iterator=userBrowsedMap.keySet().iterator();
			while(iterator.hasNext()){
				String userId=iterator.next();
				try
				{
					ConnectionFactory.getStatement().executeUpdate("update users set upreflist='"+userPrefListMap.get(userId)+"' where userid='"+userId+"'");
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
			
	}
	
	/**
	 * 所有用户的喜好关键词列表TFIDF值随时间进行自动衰减更新
	 */
	public void autoDecRefresh(){
		autoDecRefresh(RecommendKit.getUserList());
	}
	
	/**
	 * 所有用户的喜好关键词列表TFIDF值随时间进行自动衰减更新
	 */
	public void autoDecRefresh(Collection<String> userIdsCol){
		try
		{
			String inQuery=RecommendKit.getInQueryStringWithSingleQuote(userIdsCol.iterator());
			if(inQuery.equals("()")){
				return;
			}
			ResultSet rs=ConnectionFactory.getStatement().executeQuery("select userid,upreflist from users where userid in "+inQuery);
			//用以更新的用户喜好关键词map的json串
			//用于删除喜好值过低的关键词
			ArrayList<String> keywordToDelete=new ArrayList<String>();
			while(rs.next()){
				String newPrefList="{";
				HashMap<Integer,CustomizedHashMap<String,Double>> map=JsonKit.jsonPrefListtoMap(rs.getString(2));
				Iterator<Integer> ite=map.keySet().iterator();
				while(ite.hasNext()){
					//用户对应模块的喜好不为空
					Integer moduleId=ite.next();
					CustomizedHashMap<String,Double> moduleMap=map.get(moduleId);
					newPrefList+="\""+moduleId+"\":";
					//N:{"X1":n1,"X2":n2,.....}
					if(!(moduleMap.toString().equals("{}"))){
						Iterator<String> inIte=moduleMap.keySet().iterator();
						while(inIte.hasNext()){
							String key=inIte.next();
							//累计TFIDF值乘以衰减系数
							double result=moduleMap.get(key)*DEC_COEE;
							if(result<10){
								keywordToDelete.add(key);
							}
							moduleMap.put(key,result);
						}
					}
					for(String deleteKey:keywordToDelete){
						moduleMap.remove(deleteKey);
					}
					keywordToDelete.clear();
					newPrefList+=moduleMap.toString()+",";
				}
				newPrefList="'"+newPrefList.substring(0,newPrefList.length()-1)+"}'";
				ConnectionFactory.getNewStatement().executeUpdate("update users set upreflist="+newPrefList+" where userid='"+rs.getString(1)+"'");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 提取出当天所有用户浏览新闻纪录
	 * @return
	 */
	private HashMap<String,ArrayList<String>> getBrowsedHistoryMap(){
		HashMap<String, ArrayList<String>> userBrowsedMap=null;
		try
		{
			userBrowsedMap=new HashMap<String,ArrayList<String>>();
			ResultSet rs = ConnectionFactory.getStatement().executeQuery("select * from newslogs where nltime>"+RecommendKit.getSpecificDayFormat(0));
			while(rs.next()){
				if(userBrowsedMap.containsKey(rs.getString(2))){
					userBrowsedMap.get(rs.getString(2)).add(rs.getString(3));
				}
				else{
					userBrowsedMap.put(rs.getString(2), new ArrayList<String>());
					userBrowsedMap.get(rs.getString(2)).add(rs.getString(3));
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return userBrowsedMap;
	}
	
	private HashSet<String> getBrowsedNewsSet(){
		HashMap<String,ArrayList<String>> browsedMap=getBrowsedHistoryMap();
		HashSet<String> newsIdSet=new HashSet<String>();
		Iterator<String> ite=getBrowsedHistoryMap().keySet().iterator();
		while(ite.hasNext()){
			Iterator<String> inIte=browsedMap.get(ite.next()).iterator();
			while(inIte.hasNext()){
				newsIdSet.add(inIte.next());
			}
		}
		return newsIdSet;
	}
	
	/**
	 * 将所有当天被浏览过的新闻提取出来，以便进行TFIDF求值操作，以及对用户喜好关键词列表的更新。
	 * @return
	 */
	private HashMap<String,Object> getNewsTFIDFMap(){
		HashMap<String,Object> newsTFIDFMap=null;
		try
		{
			Iterator<String> ite=getBrowsedNewsSet().iterator();
			String newsIdListQuery="(";
			while(ite.hasNext()){
				newsIdListQuery+=ite.next()+",";
			}
			//用户如果当天没看新闻
			if(newsIdListQuery.length()>1){
				newsIdListQuery=newsIdListQuery.substring(0, newsIdListQuery.length()-1)+")";
				//提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
				rs=ConnectionFactory.getStatement().executeQuery("select newsid,ntitle,ncontent,nmoduleid from news where newsid in "+newsIdListQuery);
				
				newsTFIDFMap=new HashMap<String,Object>();
				while(rs.next()){
					newsTFIDFMap.put(rs.getString(1), TFIDF.getTFIDE(rs.getString(2), rs.getString(3),KEY_WORDS_NUM));
					newsTFIDFMap.put(rs.getString(1)+"moduleid", rs.getInt(4));
				}
			}
			else
				return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return newsTFIDFMap;
	}
}
