/**
 * 
 */
package top.qianxinyao.contentbasedrecommend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.ansj.app.keyword.Keyword;

import com.jfinal.plugin.activerecord.Db;

import top.qianxinyao.algorithms.JsonKit;
import top.qianxinyao.algorithms.RecommendKit;
import top.qianxinyao.model.News;
import top.qianxinyao.model.Newslogs;
import top.qianxinyao.model.Users;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月3日 
 * 每次用户浏览新的新闻时，用以更新用户的喜好关键词列表
 */
public class UserPrefRefresher
{	
	//设置TFIDF提取的关键词数目
	private static final int KEY_WORDS_NUM = 10;
	
	//每日衰减系数
	private static final double DEC_COEE=0.7;

	public void refresh(){
			refresh(RecommendKit.getUserList());
	}
	
	/**
	 * 按照推荐频率调用的方法，一般为一天执行一次。
	 * 定期根据前一天所有用户的浏览记录，在对用户进行喜好关键词列表TFIDF值衰减的后，将用户前一天看的新闻的关键词及相应TFIDF值更新到列表中去。
	 * @param userIdsCol
	 */
	@SuppressWarnings("unchecked")
	public void refresh(Collection<Long> userIdsCol){
			//首先对用户的喜好关键词列表进行衰减更新
			autoDecRefresh(userIdsCol);
			
			//用户浏览新闻纪录：userBrowsexMap:<Long(userid),ArrayList<String>(newsid List)>
			HashMap<Long,ArrayList<Long>> userBrowsedMap=getBrowsedHistoryMap();
			//如果前一天没有浏览记录（比如新闻门户出状况暂时关停的情况下，或者初期用户较少的时候均可能出现这种情况），则不需要执行后续更新步骤
			if(userBrowsedMap.size()==0)
				return;
			
			//用户喜好关键词列表：userPrefListMap:<String(userid),String(json))>
			HashMap<Long,CustomizedHashMap<Integer,CustomizedHashMap<String,Double>>> userPrefListMap=RecommendKit.getUserPrefListMap(userBrowsedMap.keySet());
			//新闻对应关键词列表与模块ID：newsTFIDFMap:<String(newsid),List<Keyword>>,<String(newsModuleId),Integer(moduleid)>
			HashMap<String,Object> newsTFIDFMap=getNewsTFIDFMap();
			
			//开始遍历用户浏览记录，更新用户喜好关键词列表
			//对每个用户（外层循环），循环他所看过的每条新闻（内层循环），对每个新闻，更新它的关键词列表到用户的对应模块中
			Iterator<Long> ite=userBrowsedMap.keySet().iterator();
			
			while(ite.hasNext()){
				Long userId=ite.next();
				ArrayList<Long> newsList=userBrowsedMap.get(userId);
				for(Long news:newsList){
					Integer moduleId=(Integer) newsTFIDFMap.get(news+"moduleid");
					//获得对应模块的（关键词：喜好）map
					CustomizedHashMap<String,Double> rateMap=userPrefListMap.get(userId).get(moduleId);
					//获得新闻的（关键词：TFIDF值）map
					List<Keyword> keywordList=(List<Keyword>) newsTFIDFMap.get(news.toString());
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
			Iterator<Long> iterator=userBrowsedMap.keySet().iterator();
			while(iterator.hasNext()){
				Long userId=iterator.next();
				try
				{
					Db.update("update users set pref_list='"+userPrefListMap.get(userId)+"' where id=?",userId);
				}
				catch (Exception e)
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
	public void autoDecRefresh(Collection<Long> userIdsCol){
		try
		{
			String inQuery=RecommendKit.getInQueryStringWithSingleQuote(userIdsCol.iterator());
			if(inQuery.equals("()")){
				return;
			}
			List<Users> userList=Users.dao.find("select id,pref_list from users where id in "+inQuery);
			//用以更新的用户喜好关键词map的json串
			//用于删除喜好值过低的关键词
			ArrayList<String> keywordToDelete=new ArrayList<String>();
			for(Users user:userList){
				String newPrefList="{";
				HashMap<Integer,CustomizedHashMap<String,Double>> map=JsonKit.jsonPrefListtoMap(user.getPrefList());
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
				Db.update("update users set pref_list="+newPrefList+" where id=?",user.getId());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 提取出当天有浏览行为的用户及其各自所浏览过的新闻id列表
	 * @return
	 */
	private HashMap<Long,ArrayList<Long>> getBrowsedHistoryMap(){
		HashMap<Long, ArrayList<Long>> userBrowsedMap=new HashMap<Long,ArrayList<Long>>();
		try
		{
			List<Newslogs> newslogsList=Newslogs.dao.find("select * from newslogs where view_time>"+RecommendKit.getSpecificDayFormat(0));
			for(Newslogs newslogs:newslogsList){
				if(userBrowsedMap.containsKey(newslogs.getUserId())){
					userBrowsedMap.get(newslogs.getUserId()).add(newslogs.getNewsId());
				}
				else{
					userBrowsedMap.put(newslogs.getUserId(), new ArrayList<Long>());
					userBrowsedMap.get(newslogs.getUserId()).add(newslogs.getNewsId());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return userBrowsedMap;
	}
	
	
	/**
	 * 获得浏览过的新闻的集合
	 * @return
	 */
	private HashSet<Long> getBrowsedNewsSet(){
		HashMap<Long,ArrayList<Long>> browsedMap=getBrowsedHistoryMap();
		HashSet<Long> newsIdSet=new HashSet<Long>();
		Iterator<Long> ite=getBrowsedHistoryMap().keySet().iterator();
		while(ite.hasNext()){
			Iterator<Long> inIte=browsedMap.get(ite.next()).iterator();
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
		HashMap<String,Object> newsTFIDFMap=new HashMap<String,Object>();;
		try
		{
			Iterator<Long> ite=getBrowsedNewsSet().iterator();
			String newsIdListQuery="(";
			while(ite.hasNext()){
				long next=ite.next();
				newsIdListQuery+=next+",";
			}
			
//			//当天存在用户浏览记录
//			if(newsIdListQuery.length()>1){
//				newsIdListQuery=newsIdListQuery.substring(0, newsIdListQuery.length()-1)+")";
//				//提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
//				List<News> newsList=News.dao.find("select id,title,content,module_id from news where id in "+newsIdListQuery);
//				System.out.println("newsIdListQuery:"+newsIdListQuery);
//				for(News news:newsList){
//					newsTFIDFMap.put(String.valueOf(news.getId()), TFIDF.getTFIDE(news.getTitle(), news.getContent(),KEY_WORDS_NUM));
//					newsTFIDFMap.put(news.getId()+"moduleid", news.getModuleId());
//				}
//				for()
//			}
			
			newsIdListQuery=newsIdListQuery.substring(0, newsIdListQuery.length()-1)+")";
			//提取出所有新闻的关键词列表及对应TF-IDf值，并放入一个map中
			List<News> newsList=News.dao.find("select id,title,content,module_id from news where id in "+newsIdListQuery);
			for(News news:newsList){
				newsTFIDFMap.put(String.valueOf(news.getId()), TFIDF.getTFIDE(news.getTitle(), news.getContent(),KEY_WORDS_NUM));
				newsTFIDFMap.put(news.getId()+"moduleid", news.getModuleId());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newsTFIDFMap;
	}
}
