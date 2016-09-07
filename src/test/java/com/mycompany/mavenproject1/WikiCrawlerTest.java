/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael
 */
public class WikiCrawlerTest {
    
    public WikiCrawlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of search method, of class WikiCrawler.
     * Input is the exact name of the article.
     */
    @org.junit.Test
    public void testSearch1() throws Exception {
        System.out.println("search");
        String articleName = "Composer";
        WikiCrawler instance = new WikiCrawler();
        String expResult = "Composer";
        String result = instance.search(articleName);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of search method, of class WikiCrawler.
     * Tests the response if the input string is something wikipedia won't
     * find a corresponding article to. 
     */
    @org.junit.Test
    public void testSearch2() throws Exception {
        System.out.println("search");
        String articleName = "gregregregprkoeogpqkrghrwhtrhbb5r4e";
        WikiCrawler instance = new WikiCrawler();
        String expResult = null;
        String result = instance.search(articleName);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of search method, of class WikiCrawler.
     * Tests the response if the input string is not an exact article name,
     * but wikipedia gives us results for what it thinks we meant to search for.
     */
    @org.junit.Test
    public void testSearch3() throws Exception {
        System.out.println("testSearch3");
        String articleName = "List of";
        WikiCrawler instance = new WikiCrawler();
        String expResult = "List_of_lists_of_lists";
        String result = instance.search(articleName);
        assertEquals(expResult, result);
    }

    /**
     * Test if the method successfully ignores text in parenthesis.
     */
    @org.junit.Test
    public void testFirstLowercaseArticle1() throws Exception {
        System.out.println("firstLowercaseArticle1");
        String articleName = "Physics";
        WikiCrawler instance = new WikiCrawler();
        String expResult = "Natural_science";
        String result = instance.firstLowercaseArticle(articleName);
        assertEquals(expResult, result);
    }
    
    /**
     * Tests if the method successfully ignores paragraphs and links
     * before the main article
     */
    @org.junit.Test
    public void testFirstLowercaseArticle2() throws Exception {
        System.out.println("firstLowercaseArticle2");
        String articleName = "Polymer";
        WikiCrawler instance = new WikiCrawler();
        String expResult = "Molecule";
        String result = instance.firstLowercaseArticle(articleName);
        assertEquals(expResult, result);
    }
    
     /**
     * Tests the method's response to the input being an article
     * that does not exist
     */
    @org.junit.Test
    public void testFirstLowercaseArticle3() throws Exception {
        System.out.println("");
        String articleName = "";
        WikiCrawler instance = new WikiCrawler();
        String expResult = null;
        String result = instance.firstLowercaseArticle(articleName);
        assertEquals(expResult, result);
        articleName = "GVFERgokregpgregkqergpokregpreqgphoerhbqhjhty";
        result = instance.firstLowercaseArticle(articleName);
        assertEquals(expResult, result);
    }
    
     /**
     * Tests the method on an article that is a disambiguation
     */
    @org.junit.Test
    public void testFirstLowercaseArticle4() throws Exception {
        System.out.println("");
        String articleName = "Music_(disambiguation)";
        WikiCrawler instance = new WikiCrawler();
        String expResult = "Music";
        String result = instance.firstLowercaseArticle(articleName);
        assertEquals(expResult, result);
    }
    
}
