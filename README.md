# NewsRecommendSystem
A recommend system involved **collaborative filtering**,**content-based recommendation** and **hot news recommendation**, can be adapted easily to be put into use in other circumstances.

***Lib: [Ansj](https://github.com/NLPchina/ansj_seg), [Quartz](http://www.quartz-scheduler.org/), [Mahout](http://mahout.apache.org/)***

# Instructions for quickly being into use (Two Steps)

## Construct Required Database(Only for MySQL now)
Just Run the code below in MySQL command line.
    
    Drop TABLE comments;
    CREATE TABLE comments
    (
    id VARCHAR(20) PRIMARY KEY,
    content VARCHAR(0),
    date TIMESTAMP null,
    tagId VARCHAR(20),
    appendContent VARCHAR(0),
    appendDate TIMESTAMP null,
    gid VARCHAR(20)
    )
## Set Runtime Properties

