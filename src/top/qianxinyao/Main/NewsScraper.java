package top.qianxinyao.Main;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import top.qianxinyao.dbconnection.DBKit;
import top.qianxinyao.model.News;
import top.qianxinyao.model.Newsmodules;

public class NewsScraper{
	
	public static final Logger logger=Logger.getLogger(NewsScraper.class);
	
	/**
	 * 从新闻门户抓取一次新闻
	 * 目前使用的新闻门户是网易新闻
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	
	public static void main(String[] args) throws IOException, SQLException
	{
		DBKit.initalize();
		
		String url="http://www.163.com/";
		Document docu1=Jsoup.connect(url).get();
		Elements lis=docu1.getElementsByTag("li");
		for(Element li: lis) {
			if(li.getElementsByTag("a").size()==0)
				continue;
			else {
				Element a=li.getElementsByTag("a").get(0);
				String title=a.text();
				//去除标题小于5个字的、非新闻的<li>标签
				String regex=".{10,}";
				Pattern pattern=Pattern.compile(regex);
				Matcher match=pattern.matcher(title);
				if(!match.find())
					continue;
				String newsUrl=a.attr("href");

				
				//图集类忽略，Redirect表示广告类忽略
				if(newsUrl.contains("photoview") || newsUrl.contains("Redirect") || newsUrl.contains("{"))
					continue;
				
				try
				{
					Document docu2=Jsoup.connect(newsUrl).get();
					Elements eles=docu2.getElementsByClass("post_crumb");
					//没有面包屑导航栏的忽略：不是正规新闻
					if(eles.size()==0)
						continue;
					String moduleName=eles.get(0).getElementsByTag("a").get(1).text();
					
					System.out.println(title+"("+moduleName+"):"+newsUrl);
					
					News news=new News();
					news.set("title",title).set("module_id", getModuleID(moduleName))
						.set("url",newsUrl).set("news_time", new Date()).save();
					
				}
				catch (SocketTimeoutException e)
				{
					continue;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("本次新闻抓取完毕！");
	}
	
	/**
	 * 初次使用，填充新闻模块信息：将默认RSS源所有模块填入。
	 */
	private static int getModuleID(String moduleName) {
		int mododuleID=-1;
		try {
				String sql="select id from newsmodules where name=?";
				Newsmodules newsmodule=Newsmodules.dao.findFirst(sql,moduleName);
				if(newsmodule==null) {
					Newsmodules module=new Newsmodules();
					module.setName(moduleName);
					module.save();
					return Newsmodules.dao.findFirst(sql,moduleName).getId();
				}
				else 
					return newsmodule.getId();
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}
		return mododuleID;
	}
}