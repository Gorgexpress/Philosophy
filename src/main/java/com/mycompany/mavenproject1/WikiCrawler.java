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
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Michael
 */
public class WikiCrawler {
    private static final int DELAY_IN_MS = 200;
    /**
     * 
     * @param articleName String used to search for wikipedia article
     * @return Name of wikipedia article if one was found, or null
     *      if nothing was found.
     * @throws MalformedURLException
     * @throws IOException 
     */
    public String search(String articleName) throws MalformedURLException, IOException {
        articleName = articleName.replaceAll(" ", "_");
        URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + articleName + "&srlimit=1&format=json");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
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
    public String firstLowercaseArticle(String url) throws IOException {
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
    public String firstLowercaseArticle(String articleName) throws IOException {
        //section of the current article
        int section = 0; 
        //So far, the result has always been in the first section. If section
        //gets to 5 we're most likely in an infinite loop. Has yet to happen though.
        while(section < 5) {
            //Make api call and grab the article text from it
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=parse&redirects=true&format=json&prop=text&page=" + articleName +"&section=" + section);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            JsonObject parse = rootobj.getAsJsonObject("parse");
            if(parse == null) return null;
            String text = parse.getAsJsonObject("text").get("*").getAsString();
            //the paragraph element is where the main text of the article normally begins
            int index = text.indexOf("<p>");
            //If it's followed immediately by a span, we aren't in the main article yet.
            while((index != -1  && text.length() >= index + 8 && text.substring(index + 3, index + 8).equals("<span"))) {
                index = text.indexOf("<p>", index + 1);
            }
            while((section == 0 && index != -1  && text.indexOf("<b>", index) > text.indexOf("</p>", index))) {
                index = text.indexOf("<p>", index + 1);
            }
            if(index == -1) continue;
            while(true) {
                //get index of next link
                index = text.indexOf("<a href=", index + 1);
                if(index == -1) break;
                //find position of the start of the link's displayed text
                int startOfLinkText = text.indexOf("\">", index) + 2;
                //If our link text is parenthesized, skip the current link.
                int nextClosingParen = text.indexOf(')', startOfLinkText);
                int lastOpeningParen = text.indexOf('(');
                while(lastOpeningParen != -1 && text.indexOf('(', lastOpeningParen + 1) < startOfLinkText)
                    lastOpeningParen = text.indexOf('(', lastOpeningParen + 1);
                if(lastOpeningParen != -1 && lastOpeningParen < startOfLinkText) {
                    if (nextClosingParen != -1 && text.indexOf(')', lastOpeningParen) == nextClosingParen)
                        continue;
                }
                //repeat for italicized 
                int nextClosingItalics = text.indexOf("</i>", startOfLinkText);
                int lastOpeningItalics = text.indexOf("<i>");
                while(lastOpeningItalics != -1 && text.indexOf("<i>", lastOpeningItalics + 1) < startOfLinkText)
                    lastOpeningItalics = text.indexOf("<i>", lastOpeningItalics + 1);
                if(lastOpeningItalics != -1 && lastOpeningItalics < startOfLinkText) {
                    if (nextClosingItalics != -1 && text.indexOf("</i>", lastOpeningItalics) == nextClosingItalics)
                        continue;
                }
                //grab name of the next article
                index = text.indexOf("href", index);
                index = text.indexOf('"', index) + 1;
                String nextArticle = text.substring(index, text.indexOf('"',index));
                //Links to other articles on wikipedia do not contain colons, and start with /wiki/
                if (nextArticle.contains(":"))
                    continue;
                if (nextArticle.contains("/wiki/"))
                    return nextArticle.substring(6);
                    
                

            }
            section++;
        }
        System.out.println("Exiting loop in MyCrawler.search. Most likely was an infinite loop");
        return null;
    }
    
   

    
    /**
     * Removes any text in parenthesis from the input. Not using this for now.
     * @param html text to remove parenthesis from
     * @return string with all text within parenthesis removed
     */
    private String removeParenthesized(String html) {
        List<Integer> indicesToInclude = new ArrayList<>();
        indicesToInclude.add(0);
        int count = 0;
        for(int i = 0; i < html.length(); i++) {
            if(html.charAt(i) == '(') {
                if(count == 0 && i > 0 && html.charAt(i - 1) == '_') 
                    i = html.indexOf(')', i);
                else {
                    if(count == 0 && i > 0)
                        indicesToInclude.add(i);
                    count++;
                }
            }
            else if(html.charAt(i) == ')') {
                count--;
                if(count == 0 && i < html.length() - 1)
                    indicesToInclude.add(i + 1);
            }
            
        }
        indicesToInclude.add(html.length());
        StringBuilder newHtml = new StringBuilder();
        for(int i = 0; i < indicesToInclude.size(); i += 2) {
            newHtml.append(html.substring(indicesToInclude.get(i), indicesToInclude.get(i + 1)));
        }
        return newHtml.toString();
    }
    
  
}
