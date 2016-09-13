/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;




/**
 * Contains methods used to search for a wikipedia article using a given string,
 * 
 * @author Michael
 */
public class WikiCrawler {
    private static final int DELAY_IN_MS = 200;
    private final Logger logger = Logger.getLogger(WikiCrawler.class);
    private static final String SEARCH_FORMAT = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=%s&srlimit=1&format=json";
    private static final String PARSE_FORMAT = "https://en.wikipedia.org/w/api.php?action=parse&redirects=true&format=json&prop=text&page=%s&section=%s";
    //unused, but should be set in an environmental variable
    private static final String USER_AGENT = "WikiCrawlerProj/1.0 (mabramowski3@gmail.com)";
    /**
     * 
     * @param articleName String used to search for wikipedia article
     * @return Name of wikipedia article if one was found, or null
     *      if nothing was found.
     * @throws MalformedURLException
     * @throws IOException 
     */
    public String search(String articleName) throws MalformedURLException, IOException {
        articleName = articleName.replaceAll(" ", "+");
        URL url = new URL(String.format(SEARCH_FORMAT, articleName));
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        //request.setRequestProperty("User-Agent", USER_AGENT);
        request.connect();
        
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
        JsonObject rootobj = root.getAsJsonObject();
        JsonObject query = rootobj.getAsJsonObject("query");
        int totalHits = query.getAsJsonObject("searchinfo").get("totalhits").getAsInt();
        if(totalHits == 0)
            return null;
        
        String title = query.getAsJsonArray("search").get(0).getAsJsonObject().get("title").getAsString();
        return title.replaceAll(" ", "_");
        
    }
    /*
    This version of the method just grabs the html of an article as you 
    were viewing it normally. It's much faster than the current method,
    but likely costs wikipedia more bandwidth. 
    */
    /*
    public String nextArticle(String url) throws IOException {
        long startTime = System.currentTimeMillis();
        Document doc = Jsoup.connect(baseUrl + url).get();
        System.out.println("time to parse: " + (System.currentTimeMillis() - startTime));
        Elements paragraphs = doc.getElementsByTag("p");
        for(Element paragraph: paragraphs) {
            paragraph.html(Jsoup.clean(removeParenthesized(paragraph.html()), italicsBlacklist));
            Elements links = paragraph.getElementsByTag("a");
            for(Element link : links) {
                if(!link.ownText().equals(url) && link.ownText().charAt(0) != '[') {
                    return link.attr("href").substring(6);
                }
            }
            
        }
        
        return null;
    }  
    */
 
    /**
     * Given a wikipedia article name, finds the first non italicized, non
     * parenthesized article in that article. 
     * @param articleName name of wikipedia article to process
     * @return name of the first non italicized, non parenthesized article
     *      in the input article
     * @throws IOException 
     */
    public String nextArticle(String articleName) throws IOException, InterruptedException {
        //section of the current article
        int section = 0; 
        //So far, the result has always been in the first section. If section
        //gets to 5 we're most likely in an infinite loop. Has yet to happen though.
        while(section < 5) {
            //Make api call and grab the article text from it
            URL url = new URL(String.format(PARSE_FORMAT, articleName, section));
            //URL url = new URL("https://en.wikipedia.org/w/api.php?action=parse&redirects=true&format=json&prop=text&page=" + articleName +"&section=" + section);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            //request.setRequestProperty("User-Agent", USER_AGENT);
            request.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            JsonObject parse = rootobj.getAsJsonObject("parse");
            if(parse == null) return null;
            String text = parse.getAsJsonObject("text").get("*").getAsString();
            //the paragraph element is where the main text of the article normally begins
            int index = text.indexOf("<p>");
            //Sometimes there are p tags with links before the main article text. We'll search for the bold tag,
            //as the first sentence of an article usually contains the name of the article in bold.
            while((section == 0 && index != -1  && text.indexOf("<b>", index) > text.indexOf("</p>", index))) 
                index = text.indexOf("<p>", index + 1);
    
            //couldn't find article text
            if(index == -1) continue;
            
            int lastOpeningParen = text.indexOf('(', index);
            int lastOpeningItalics = text.indexOf("<i>", index);
            int lastOpeningDiv = text.indexOf("<div");
            int lastOpeningSpan = text.indexOf("<span");
            while(true) {
                //get index of next link
                index = text.indexOf("<a href=", index + 1);
                if(index == -1) break;
                //find position of the start of the link's displayed text
                int startOfLinkText = text.indexOf("\">", index) + 2;
                String sub = text.substring(startOfLinkText, startOfLinkText + 10);
                //If our link text is parenthesized, skip the current link.
                IsInsideResults result;
                result = isInside(text, startOfLinkText, startOfLinkText, "(", ")", lastOpeningParen);
                lastOpeningParen = result.lastOpeningIndex;
                if(result.isInside) {
                    index = result.nextClosingIndex;
                    continue;
                }
                //same for italics
                result = isInside(text, startOfLinkText, startOfLinkText, "(", ")", lastOpeningItalics);
                lastOpeningItalics = result.lastOpeningIndex;
                if(result.isInside) {
                    index = result.nextClosingIndex;;
                    continue;
                }
                //Sometimes there will be divs/spans with links with the float:right
                //attribute. They won't be the first clickable links when
                //viewing the page normally, but they will appear earlier than the
                //main article text in the html itself. We should avoid links contained
                //in divs and spans as a result. Main article text is normally only be 
                //a child of the body tag.
                result = isInside(text, startOfLinkText, startOfLinkText, "<span", "</span>", lastOpeningSpan);
                lastOpeningSpan = result.lastOpeningIndex;
                if(result.isInside) {
                    index = result.nextClosingIndex;;
                    continue;
                }
                result = isInside(text, startOfLinkText, startOfLinkText, "<div", "</div>", lastOpeningDiv);
                lastOpeningDiv = result.lastOpeningIndex;
                if(result.isInside) {
                    index = result.nextClosingIndex;;
                    continue;
                }
                
                //grab name of the next article
                index = text.indexOf("href", index);
                index = text.indexOf('"', index) + 1;
                String nextArticle = text.substring(index, text.indexOf('"',index));
                //Links to other articles on wikipedia do not contain colons, start with /wiki/
                //and are not red links
                if (nextArticle.contains(":"))
                    continue;
                if (nextArticle.contains("/wiki/") && !nextArticle.contains("redlink=1"))
                    return nextArticle.substring(6);
                    
                

            }
            section++;
            Thread.sleep(DELAY_IN_MS);
        }
        System.out.println("Exiting loop in MyCrawler.search. Most likely was an infinite loop");
        return null;
    }
    
    /**
     * Checks if the text between two indices are between a specified opening
     * and closing string.
     * @param text text to search in
     * @param start starting index
     * @param end ending index 
     * @param opening opening pattern ex. <i>
     * @param closing closing pattern ex. </i>
     * @param lastOpeningIndex last known index of the opening pattern, so we don't
     * have to search the entire string for it everytime this method is called
     * @return  an IsInsideResults object where isInside is true if the text
     * was inside the opening and closing string, and false if not. lastOpeningIndex
     * will be the last index of the opening string seen. next closing will be
     * the index of the next closing string following the text indices. 
     * Indices will be -1 if no matching string is found.
     */
    private IsInsideResults isInside(String text, int start, int end, String opening, String closing, int lastOpeningIndex) {
        if(lastOpeningIndex > end || lastOpeningIndex == -1) return new IsInsideResults(false, lastOpeningIndex, end);
        //If our link text is parenthesized, skip the current link.
        int nextClosingIndex = text.indexOf(closing, end);
        while(lastOpeningIndex != -1 && text.indexOf(opening, lastOpeningIndex  + 1) < start)
                    lastOpeningIndex  = text.indexOf(opening, lastOpeningIndex  + 1);
        if(lastOpeningIndex  != -1 && lastOpeningIndex < start) 
            if (nextClosingIndex != -1 && text.indexOf(closing, lastOpeningIndex) == nextClosingIndex)
                return new IsInsideResults(true, lastOpeningIndex, nextClosingIndex);
        return new IsInsideResults(false, lastOpeningIndex, nextClosingIndex);
         
    }
    //stores results of above method
    private class IsInsideResults {
        public boolean isInside; //true if text is inside opening and closing string patterns
        public int lastOpeningIndex; //index of last index of opening pattern seen
        public int nextClosingIndex; //index of next closing pattern from the text indices.
        IsInsideResults(boolean isInside, int lastOpeningIndex, int nextClosingIndex) {
            this.isInside = isInside;
            this.lastOpeningIndex = lastOpeningIndex;
            this.nextClosingIndex = nextClosingIndex;
            
        }
    }
  
}
