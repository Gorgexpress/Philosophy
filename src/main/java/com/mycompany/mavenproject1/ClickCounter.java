/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Class used to number of clicks it takes to get to Philosophy from a wikipedia
 * article, whose name is inferred by the input string. Uses a database to cache
 results. Initialize a mSession with the database with the init method, and 
 close it with the close method. Using the countClicks method without initializing
 the database will ignore the cache.
 * @author Michael
 */
public class ClickCounter {
    
    private final String DESTINATION = "Philosophy";
    private static final int DELAY_IN_MS = 200;
    private Session mSession;
    private final WikiCrawler mCrawler;
    
    ClickCounter() {
        this.mCrawler = new WikiCrawler();
    }
    
    ClickCounter(WikiCrawler crawler) {
        this.mCrawler = crawler;
    }
    
    
    /**
     * @param source string used to search for a wikipedia article title
     * @return If it's possible to get to philosophy, returns a positive integer
     * whose value is the # of clicks to get to philsoophy. If there is a loop,
     * returns a negative number whose absolute value is the number of clicks
     * to see an article repeated. If no article was found using the input string,
     * returns null.
     * @throws IOException
     * @throws InterruptedException 
     */
    public Integer countClicks(String source) throws IOException, InterruptedException {
        //shorten string to 250 characters if its larger. Largest wiki article
        // title is a bit over 200.
        if(source.length() > 250)
            source = source.substring(0, 250);
        //replace spaces with underscores, as spaces aren't allowed in the urls
        //to wikipedia articles.
        source = source.replaceAll(" ", "_");
        //If we were given our destination as the source just return.
        if(source.equals(DESTINATION)) return 0;
        
        //If our database mSession is initialized, query the database to see
        //if the results have been cached, and return the results if they exist.
        if(mSession != null) {
            Article article = mSession.get(Article.class, source);
            if(article != null) return article.getClicks();
        }
        
        //Search for an article matching our source string. If no article
        //is found, return null.
        String currentArticleName = mCrawler.search(source);
        if(currentArticleName == null) {
            System.out.println("Could not find article");
            saveResults(source, null, null);
            return null;
        }
        System.out.println(source + " -> " + currentArticleName);
        //Set used to detect infinite loops
        Set<String> set = new HashSet<>(); 
        //Keeps track of the articled we visited in the order we visited them
        List<String> list = new ArrayList<>(); //
        
        //Query again using the article name given by mCrawler.search
        if(mSession != null) {
            Article article = mSession.get(Article.class, source);
            if(article != null) {
                saveResults(source, list, article.getClicks());
                return article.getClicks();
            }
            
        }
        //All the long variables are used to log performance
        long totalTimeCrawling = 0;
        long startTime = System.currentTimeMillis();
        Integer counter = 0; //tracks the # of clicks made
        
        while(!currentArticleName.equals(DESTINATION)) {
            long startTimeOfLoop = System.currentTimeMillis();
            
            //add current article to our set and list, then find the next article.
            set.add(currentArticleName);
            list.add(currentArticleName);
            currentArticleName = mCrawler.nextArticle(currentArticleName);
            counter++;
            //check for loop
            if(set.contains(currentArticleName)) {
                //a negative count indiciates an infinite loop
                counter = -counter; 
                list.add(currentArticleName);
                System.out.println("Infinite loop starting on article: " + currentArticleName);
                break;
            }
            //query database for cached results using the current article name
            if(mSession != null) {
                Article article = mSession.get(Article.class, currentArticleName);
                if(article != null) {
                    counter += article.getClicks();
                    break;
                }
            }
            long timeToGetNextArticle = System.currentTimeMillis() - startTimeOfLoop;
            totalTimeCrawling += timeToGetNextArticle;
            if(timeToGetNextArticle < DELAY_IN_MS)
                Thread.sleep(DELAY_IN_MS - timeToGetNextArticle);
            
        }
        System.out.println("Time spent getting and processing article data in MS: " + totalTimeCrawling);
        System.out.println("Time spent sleeping in MS: " + (System.currentTimeMillis() - startTime - totalTimeCrawling));
        System.out.println("Saving results into the database...");
        saveResults(source, list, counter);
        System.out.println("Done.");
        return counter;
    }
    
    /** 
     * 
     * @param source the original string we used to find our first article
     * @param list list of article names
     * @param clicks # of clicks
     * @return  true of results were successfully saved, false if not
     */
    private boolean saveResults(String source, List<String> list, Integer clicks) {
        if(mSession == null) return false;
        Transaction transaction = mSession.beginTransaction();
        
        if(list == null || list.isEmpty() || !source.equals(list.get(0))) {
            Article sourceName = new Article(source, clicks);
            mSession.save(sourceName);
        }
        if(clicks != null) {
            int clicksInt = clicks;
            if(clicks < 0) {
                String loopBase= list.get(list.size() - 1);
                int loopOffset = 0;
                while(!list.get(loopOffset).equals(loopBase))
                    loopOffset += 1;
                for(int i = 0; i < loopOffset; i++) {
                    Article article = new Article(list.get(i), clicksInt++);
                    mSession.save(article);
                }
                for(int i = loopOffset; i < list.size(); i++) {
                    Article article = new Article(list.get(i), clicksInt);
                    mSession.save(article);
                }
            }    
            else {
                for(String articleName : list) { 
                    Article newArticle = new Article(articleName, clicksInt--);
                    mSession.save(newArticle);
                }
            }
        }
        transaction.commit();
        return true;
    
    }
    /**
     * initialize our database mSession
     */
    public void init() {
        mSession = HibernateUtil.getSessionFactory().openSession();
    }
    
    /**
     * close our database mSession and database factory.
     */
    public void close() {
        if(mSession != null) {
            mSession.close();
            HibernateUtil.shutdown();
        }
    }
}
