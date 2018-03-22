/**
 * 
 */
package top.qianxinyao.algorithms;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import top.qianxinyao.contentbasedrecommend.CustomizedHashMap;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月21日
 */
public class JsonKit
{
	public static String test(){
		String json=null;
		try {	        
	        Map<Integer, Object> moduleidMap = new HashMap<Integer, Object>();
	        Map<String, Double> keywordRateMap = new HashMap<String, Double>();
	        keywordRateMap.put("政治", 123.1);
	        keywordRateMap.put("金融", 35.2);
	        moduleidMap.put(1,keywordRateMap);
	        keywordRateMap.put("电影", 351.1);
	        moduleidMap.put(2,keywordRateMap);
	        ObjectMapper objectMapper=new ObjectMapper();
	        json=objectMapper.writeValueAsString(moduleidMap);
	        
	        String test="{\"1\":{},\"2\":{},\"3\":{},\"4\":{}}";
	        return test;
	    } catch (IOException e) {

	        e.printStackTrace();

	    }
        return json;
	}
	
	/**
	 * 获取用户所关注的模板的id的set
	 * @param srcJson
	 * @return
	 */
	public static Set<Integer> getUserModuleIdSet(String srcJson){
		
		//java的擦除机制不允许直接获取泛型类的class,但是这样会使得jackson的readValue自动将键转换为String，于是需要使用jackson提供的TypeReference来解决这个问题
		Map<Integer, Object> map=null;
		try
		{
			ObjectMapper objectMapper=new ObjectMapper();
			map = objectMapper.readValue(srcJson, new TypeReference<Map<Integer,Object>>(){});
		}
		catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map.keySet();
	}
	
	/**
	 * 获得用户对应module的喜好关键词列表的map
	 * @param srcJson
	 * @param moduleId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String,Double> getModulePrefMap(String srcJson,int moduleId){
		
		LinkedHashMap<String,Double> keyWordsRateMap=null;
		try
		{
			ObjectMapper objectMapper=new ObjectMapper();
			//java的擦除机制不允许直接获取泛型类的class,但是这样会使得jackson的readValue自动将键转换为String，于是需要使用jackson提供的TypeReference来解决这个问题
			Map<Integer,Object> map=objectMapper.readValue(srcJson, new TypeReference<Map<Integer,Object>>(){});
			keyWordsRateMap=(LinkedHashMap<String,Double>) map.get(moduleId);
		}
		catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyWordsRateMap;
	}
	
	/**
	 * 将用户的喜好关键词列表字符串转换为map
	 * @param srcJson
	 * @return
	 */
	public static CustomizedHashMap<Integer,CustomizedHashMap<String,Double>> jsonPrefListtoMap(String srcJson){
		ObjectMapper objectMapper=new ObjectMapper();
		CustomizedHashMap<Integer,CustomizedHashMap<String,Double>> map=null;
		try
		{			
			map=objectMapper.readValue(srcJson, new TypeReference<CustomizedHashMap<Integer,CustomizedHashMap<String,Double>>>(){});
		}
		catch (JsonParseException e)
		{
			e.printStackTrace();
		}
		catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
    
}
