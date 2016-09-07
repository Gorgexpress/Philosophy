/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;


import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;


/**
 *
 * @author Michael
 */
public class ClickCounterTest {
    
    private WikiCrawler crawler;
    private ClickCounter instance;
    private List<String> pathNormal = Arrays.asList("Natural_science", "Science", "Knowledge", 
                "Awareness", "Consciousness", "Quality_(philosophy)", "Philosophy");
    private List<String> pathLoop = Arrays.asList("Natural_science", "Science", "Knowledge", 
                "Awareness", "Consciousness", "Knowledge");
    int index;
    
    public ClickCounterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {     
        crawler = mock(WikiCrawler.class);
        instance = new ClickCounter(crawler); 
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of countClicks method, of class ClickCounter.
     * Input should reach philosophy in 7 clicks.
     */
    @Test
    public void testCountClicks() throws Exception {
        System.out.println("countClicks");
        index = 0;
        String source = "Physics";
        when(crawler.search(any())).thenReturn("Physics");
        when(crawler.firstLowercaseArticle(any(String.class))).thenReturn("Natural_science", "Science", "Knowledge", 
                "Awareness", "Consciousness", "Quality_(philosophy)", "Philosophy");
        Integer result = instance.countClicks(source);
        Integer expResult = 7;
        assertEquals(expResult, result);

    }
    
     /**
     * Test of countClicks method, of class ClickCounter.
     * Tests ability to handle an infinite loop.
     */
    @Test
    public void testCountClicksLoop() throws Exception {
        System.out.println("countClicks");
        index = 0;
        String source = "Physics";
        when(crawler.search(any())).thenReturn("Physics");
        when(crawler.firstLowercaseArticle(any(String.class))).thenReturn("Natural_science", "Science", "Knowledge", 
                "Awareness", "Consciousness", "Knowledge");
        Integer result = instance.countClicks(source);
        Integer expResult = -6;
        assertEquals(expResult, result);
        
    }
    
     /**
     * Test of countClicks method, of class ClickCounter.
     * Should not be able to even find an article with input string.
     */
    @Test
    public void testCountClicksNull() throws Exception {
        System.out.println("countClicks");
        index = 0;
        String source = "egfwk0gwreogpkmwrpgmwrpgowmrgpmrwgm4rewgfewg";
        when(crawler.search(any())).thenReturn(null);
        when(crawler.firstLowercaseArticle(any(String.class))).thenReturn(pathNormal.get(index++));
        Integer result = instance.countClicks(source);
        Integer expResult = null;
        assertEquals(expResult, result);
        
    }


    
}
