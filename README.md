[toc]

# 个性化新闻推荐系统--TomRecommenderSystem

## 说明

本推荐系统需要基于【新闻模块】使用，此处对于【新闻模块】的定义是：**有规律地进行新闻采集，并通过公共平台对用户进行新闻展示与推送的应用。**

本推荐系统使用的推荐算法包括协同过滤（Collaborative Filtering）、基于内容相似度的推荐（Content-based Recommendation）与热点新闻推荐（Hot News Recommendation）：

- 协同过滤的实现依托于Mahout的提供库；
- 基于内容的相似度推荐在原始算法上基于相关论文做了自主的改进；
- 热点新闻推荐顾名思义是取最近被最多用户浏览过的新闻进行推荐。

**推荐算法的具体细节可参考文件[推荐系统介绍.pdf]**

**主要使用的库（Lib）:**

- [Ansj](https://github.com/NLPchina/ansj_seg)：基于内容的推荐部分用以分词，以及其内含的TFIDF算法。
- [Quartz](http://www.quartz-scheduler.org/)：推荐系统定时运行的设定。
- [Mahout](http://mahout.apache.org/)：使用内置的协同过滤算法。
- [Jfinal](http://www.jfinal.com/)：使用内置的ActiveRecord与Db工具，对推荐系统中的数据库表做了实体类映射，以简化数据库相关操作。


## 使用

### 预备工作

#### 数据库配合

**本推荐系统目前只支持与MYSQL数据库进行交互**

本系统需要与五个表进行交互：用户表（users）,新闻表（news），新闻模块表(newsmodules)，浏览记录表（newslogs），推荐结果表（Recommendations）。

- 用户表users

存储用户基本信息的表。要求至少拥有两个字段：用户id（id:bigint），用户喜好关键词列表（pref_list:json），用户最近登录时间（latest_log_time:timestamp）。

|字段名|类型|非空|主键|外键|自增|默认值|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|pref_list|text|yes||||{"moduleid1":{},"moduleid2":{},...}|
|latest_log_time|timestamp|yes|||||

- 新闻表news

存储新闻基本信息的表。要求至少拥有三个字段：新闻id（id：bigint），新闻文本内容（content:text），所属模块(module_id)。

|字段名|类型|非空|主键|外键|自增|默认值|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|title|text|yes|||||
|content|text|yes|||||
|module_id|int|yes||yes|||


- 新闻模块表newsmodules

存储新闻模块信息的表。要求至少拥有两个字段：模块id（id:int），模块名称（name:text），抓取时间/新闻日期(news_time:timestamp)。

|字段名|类型|非空|主键|外键|自增|默认值|
|--|--|--|--|--|--|--|
|id|int|yes|yes||yes||
|name|text|yes|||||
|news_time|timestamp|yes|||||


- 浏览记录表newslogs

存储用户浏览新闻记录的表。要求至少拥有三个字段：记录id（id:bigint），用户id（user_id：bigint），新闻id（news_id：bigint），浏览时间(view_time:timestamp)，用户对新闻的偏好程度(prefer_degree[0：仅仅浏览，1：评论，2：收藏])。

|字段名|类型|非空|主键|外键|自增|默认值|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|user_id|bigint|yes||yes|||
|news_id|bigint|yes||yes|||
|view_time|timestamp|yes|||||
|prefer_degree|int|yes|||||



- 推荐结果表Recommendations

存储推荐系统为用户生成的推荐结果及用户反馈的表。要求至少拥有五个字段：推荐结果id（id：bigint），用户id（user_id：bigint），新闻id（news_id：bigint），推荐结果生成时间戳（derive_time:timestamp）,用户反馈(feedback:bit[0:用户未浏览，1：用户进行了浏览])，结果生成的对应推荐算法(derive_algorithm:int[0:协同过滤，1:基于内容的推荐，2：热点新闻推荐])

|字段名|类型|非空|主键|外键|自增|默认值|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|user_id|bigint|yes||yes|||
|news_id|bigint|yes||yes|||
|derive_time|timestamp|yes|||||
|feedback|bit|||||0|
|derive_algorithm|int|yes|||||



#### 数据库配置

在项目根目录下的res目录下，修改dbconfig.properties文件中有关数据库的配置：

```
url = jdbc:mysql://[数据库ip]/[数据库名]?useUnicode=true&characterEncoding=utf8
user = [登录用户名]
password = [登录密码]
```

**注意，数据库的编码设置应为UTF8。**

### 系统启动-Quick Start

四个步骤：

1.在com.qianxinyao.TomNewsRecommender包下，找到类Main；

2.选择推荐算法。设置boolean类型的enableCB,enableCF,enableHR变量，分别代表推荐过程中是否启用协同过滤推荐算法、基于内容的推荐算法、基于热点新闻的推荐算法。若均设为true，表示三种算法均工作，一起为用户生成推荐结果；

3.选择推荐对象。推荐对象分为三种：全体用户，活跃用户（最近一段时间有登录行为）与自定义用户（自己指定的用户），若选择自定义用户，需要构建包含目标用户id(long)的List<Long>；

4.选择系统运行方式。运行方式分为两种：一次运行和定时运行。一次运行即只为用户进行一次推荐生成，生成结束后则系统停止，若要再生成推荐，需要重新启动系统。而定时运行则可以定时为用户生成推荐结果，若不强制停止系统，则系统会一直运行下去。（定时运行时间在paraConfig.properties文件中设定）

以下是示例代码：

```
package com.qianxinyao.TomNewsRecommender;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author qianxinyao
 * @email tomqianmaple@gmail.com
 * @github https://github.com/bluemapleman
 * @date 2016年10月20日
 * 推荐系统入口类，在此启动推荐系统。
 */
public class Main
{
    
    public static final Logger logger = Logger.getLogger(Main.class);
    
    /**
     * 推荐系统运行入口
     * @param args
     */
    public static void main(String[] args)
    {
        //选择要在推荐系统中运行的推荐算法
        boolean enableCF=true,enableCB=false,enableHR=false;
        
        List<Long> userList=new ArrayList<Long>();
        userList.add(1l);
        userList.add(2l);
        userList.add(3l);
        
        //为指定用户执行一次推荐
        new JobSetter(enableCF,enableCB,enableHR).executeInstantJobForCertainUsers(userList);
        //定时执行推荐
//      new JobSetter(enableCF,enableCB,enableHR).executeQuartzJob(forActiveUsers);
    }
}
```

### 日常使用

系统运行的各类参数都可以在根目录下src/main/res目录下的paraConfig.properties文件中进行配置。默认配置是推荐配置。

若需要推荐系统能在每次生成有效的推荐，只要【新闻模块】保持以一定频率抓取一定量的新闻并入库news表。（最好与推荐系统定时推荐的频率相同，并在推荐系统运行之前完成一次抓取，推荐每天抓取一次新闻，并进行一次推荐生成。）

**注意：入库的新闻要标注module_id，详情可参见数据库表与com.qianxinyao.TomNewsRecommender包下的NewsScraper类中的代码。**


## 测试数据

在Mysql数据库中运行data.sql中的sql语句，可生成数据库结构与测试数据。

测试数据中包含以下几个部分：

- users表：7个测试用户
- news表：306个2017-12-12日从网易首页抓取的测试新闻
- newsmodules表：17个测试模块
- newslogs：测试推荐算法效果用的9条浏览记录

要查看推荐系统在测试数据上运行的效果，只需在Main类下执行：

```
//在测试数据上运行
new TestDataRunner().runTestData();
```

预期的推荐生成结果如下：

- 若对测试数据进行一次协同过滤，将生成0条推荐。
- 若对测试数据进行一次基于内容的推荐，将为用户1（id=1）推荐85，87，89，104这四条新闻(有重复标题的新闻，新闻标题中的“合同”关键词匹配上了用户的喜好关键词)，为用户2推荐89新闻（重复标题的新闻），推荐用户3推荐87，85，100这三条新闻（新闻标题中的“合同”关键词匹配上了用户的喜好关键词）。
- 若对测试数据进行一次基于热点新闻的推荐，将分别为用户1推荐103，104，为用户2推荐100，104，为用户3推荐100，101，因为最近被浏览得最多的新闻就是这三个拥有浏览记录的用户看过的那些新闻（100，101，102，103，104）。


## 额外说明

1.com.qianxinyao.TomNewsRecommender下的NewsScraper类是抓取网易的测试新闻时用的类，大家也可以用这个类继续采集新闻。该类默认对网易新闻首页的所有新闻进行一次抓取入库。

2.协同过滤的效果目前不太稳定/可控，因为采用的是Mahout内置的协同过滤工具。一般来说，新闻模块的活跃用户越多，则协同过滤效果越好，也越明显。若有需求，我会在后期自己实现能稳定生成指定数量的推荐结果的协同过滤算法。

3.一般当协同过滤与基于内容的推荐算法生成的推荐数目不足时，可以用基于热点新闻的推荐进行数量补充。






