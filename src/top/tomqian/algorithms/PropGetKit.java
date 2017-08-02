/**
 * 
 */
package top.qianxinyao.algorithms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年11月30日 用以读取配置文件，获取对应属性
 */
public class PropGetKit
{
	private static final Logger logger = Logger.getLogger(PropGetKit.class);

	public static Properties propGetKit = new Properties();;

	public static void loadProperties(String configFileName)
	{
		try
		{
			propGetKit.load(new FileInputStream(System.getProperty("user.dir") + "/res/" + configFileName + ".properties"));
		}
		catch (FileNotFoundException e)
		{
			logger.error("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在");
		}
		catch (IOException e)
		{
			logger.error("装载文件--->失败!");
		}
	}

	public static String getString(String key)
	{
		return propGetKit.getProperty(key);
	}
	
	public static int getInt(String key)
	{
		return Integer.valueOf(propGetKit.getProperty(key));
	}
	
}
