# Tom's News Recommender -- personal news recommendation system

## Basic Information

This recommender needs to cooperate with a [News Module]. Here the definition of [News Module] is an application with following features:

- can collect news regularly;
- can display and feed latest news to users.

The recommendation algorithms that the recommender use include **collaborative filtering, content-based recommendation and hot news recommendation**.

- Collaborative filtering is realized based on Java library Mahout.
- Content-based recommendation is realized based on relevant papers with personal improvements.
- Hot news recommendation, as it name suggests, simply recommend news that is read by most of people recently.

**For more information on recommendation algorithm, please refer to [推荐系统介绍.pdf]**

**Libs used:**

- [Ansj](https://github.com/NLPchina/ansj_seg): word segmenting function in content-based recommendation, and its TFIDF algorithm.
- [Quartz](http://www.quartz-scheduler.org/)： to support cron recommendation job.
- [Mahout](http://mahout.apache.org/)：realization of collaborative filtering。
- [Jfinal](http://www.jfinal.com/)：Use its ActiveRecord and Db tool to map data tables to Java class, so as to simplify database relevant code realizations.


## Quick Start

### Before You Start

#### Database Tables

**The recommender only support MySQL for now!!!**

The recommender needs to interacts with five tables: users, news, newsmodules, newslogs, recommendations.

- users

Store users' basic informations. Should has at least two columns: user id (id), user's preference list (pref_list), user's latest logging in time (timestamp).

|Column|Type|Not Null|Primary Key|Foreign Key|Auto Increment|Default Value|
|-|-|-|-|-|-|-|
|id|bigint|yes|yes||yes||
|pref_list|text|yes||||{"moduleid1":{},"moduleid2":{},...}|
|latest_log_time|timestamp|yes|||||


- news

Store basic information of news. Should has at least three columns: news id (id), news content (content), id of the module that the news belongs to (module_id).


|Column|Type|Not Null|Primary Key|Foreign Key|Auto Increment|Default Value|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|title|text|yes|||||
|content|text|yes|||||
|module_id|int|yes||yes|||


- newsmodules

Store information of news module. Should has at least two columns: module id (id), module name (name), time for fetching news (news_time).

|Column|Type|Not Null|Primary Key|Foreign Key|Auto Increment|Default Value|
|--|--|--|--|--|--|--|
|id|int|yes|yes||yes||
|name|text|yes|||||
|news_time|timestamp|yes|||||


- newslogs

Store history of users browsing behaviors. Should has at least three columns: browsing behavior id (id), user id (user_id), news id (news_id), browsing time (view_time), user's preference degree for the news (prefer_degree [0：browse only，1：give review，2：like]).

|Column|Type|Not Null|Primary Key|Foreign Key|Auto Increment|Default Value|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|user_id|bigint|yes||yes|||
|news_id|bigint|yes||yes|||
|view_time|timestamp|yes|||||
|prefer_degree|int|yes|||||



- Recommendations

Store recommendation result and users' feedbacks. Should has at least five columns: recommendation result id (id), user id (user_id), news id (news_id), time when recommendation result come into being (derive_time), user's feedback (feedback [0:no browsing，1：browsed]), number of algorithm that derive the recommendation result (derive_algorithm [0:collaborative filtering，1:content-based recommendation，2：hot news recommendation]).


|Column|Type|Not Null|Primary Key|Foreign Key|Auto Increment|Default Value|
|--|--|--|--|--|--|--|
|id|bigint|yes|yes||yes||
|user_id|bigint|yes||yes|||
|news_id|bigint|yes||yes|||
|derive_time|timestamp|yes|||||
|feedback|bit|||||0|
|derive_algorithm|int|yes|||||


**In fact, all of above tables can be derived by running test_data.sql!!!**


#### Database Configuration

In *res* directory under project root directory, customize *dbconfig.properties* file:

```
url = jdbc:mysql://[database server ip]/[database name]?useUnicode=true&characterEncoding=utf8
user = [username]
password = [password]
```

**Attention: database's encoding should be UTF8.**



### Quickly Start the Recommender

Four steps:

1. Find main class *Main* under package com.qianxinyao.TomNewsRecommender.

2. Choose which algorithm to use when executing recommendation. Set boolean variable *enableCF, enableCB, enableHR*, which respectively refer to using collaborative filtering, content-based recommendation and hot news recommendation. If all three variables are assigned true, then three algorithms would work simultaneously to derive recommendations.

3. Choose goal users. There are three types of users: all users, active users (logged into the news module recently), customized users (users indicated by yourself). If you only want to derive recommendations for customized users, then you need to assign a List<Long> which contains all goal users' id.

4. Choose the way how recommender works. There are two ways: one-time recommendation and cron recommendation. one-time recommendation only derives recommendation results for users once, and the system would stop after the recommendation, and if you want to derive recommendation again, then you need to restart recommender. While cron recommendation allows you to derive recommendations on a regular basis. If you don't stop the system manually, it will keep running. (Relevant settings are in paraConfig.properties).

Below are sample codes:

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
 * Main class for recommender system.
 */
public class Main
{
    
    public static final Logger logger = Logger.getLogger(Main.class);
    
    /**
     * Main method for recommender system.
     * @param args
     */
    public static void main(String[] args)
    {
        // Choose recommendation algorithm to use
        boolean enableCF=true,enableCB=false,enableHR=false;
        
        List<Long> userList=new ArrayList<Long>();
        userList.add(1l);
        userList.add(2l);
        userList.add(3l);
        
        // execute recommending only for indicated users once.
        new JobSetter(enableCF,enableCB,enableHR).executeInstantJobForCertainUsers(userList);
        // execute recommending only for active users on a regular basis.
//      new JobSetter(enableCF,enableCB,enableHR).executeQuartzJob(forActiveUsers);
    }
}
```

### Attention

All system running parameters are in src/main/res/paraConfig.properties and can be customized by yourself. Default values are recommended.

To ensure enough effective recommendations can be derived each time the recommender works, [News Module] need to fetch news into the database's table *news* regularly before the recommender works. (The frequency should be the same as that of recommender's start, normally it should be done at least once everyday.)

All fetched news should be assigned a module_id. For information about module_id, you can refer to data table *newsmodules* and code in NewsScraper.java under com.qianxinyao.TomNewsRecommender.

## Test Data

Run data.sql in a empty database in MySQL, then all needed tables would automatically appear.

Test data includes:

- users：seven users.
- news：306 pieces of news that collected from Netease news website on 2017-12-12.
- newsmodules：17 test modules.
- newslogs：9 browsing history set for testing performance of recommendation algorithms.

If you want to see the recommender's performance on test data , just execute under main method:


```
new TestDataRunner().runTestData();
```

expected recommendation results are as follows：

- If executing collaborative filtering once, 0 recommendations would be derived.
- If executing content-based recommendation once, recommender would recommend news with id=85,87,89 and 104 to user1(id=1), news with id=89 to user2, news with id=75,87,100 to user3.
- If executing how news recommendation once, recommender would recommend news with id=103,104 to user1, news with id=100,104 to user2, news with id=100,104 to user3. (Because 100,101,102,103,104 are news that are browsed by most people recently.)






