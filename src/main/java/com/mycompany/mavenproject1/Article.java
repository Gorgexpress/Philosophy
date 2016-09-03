/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;
//<mapping resource="hibernate.hbm.xml"/>
/**
 *
 * @author Michael
 */
public class Article {
    
    private String articleName;
    private Integer clicks;
    
    public Article() {}
    public Article(String articleName, Integer clicks) {
        this.articleName = articleName;
        this.clicks = clicks;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }
    
}
