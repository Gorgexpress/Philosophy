/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael
 */
public class Main {
    
    
    public static void main(String[] args) {
        ClickCounter counter = new ClickCounter();
        //opens session with database. If you don't call init, the program
        //will still work correctly. It just won't query or save results into
        //the database. 
        //counter.init();
        System.out.print("Enter number of strings to input N, followed by N strings: ");
        Scanner scanner = new Scanner(System.in);
        int N = Integer.parseInt(scanner.nextLine());
        try {
            for(int i = 0; i < N; i++) {
                try {
                    String article = scanner.nextLine().trim();
                    Integer clicks = counter.countClicks(article);
                    if(clicks == null)
                        System.out.println("No wikipedia article found for source article: " + article);
                    else if(clicks >= 0) 
                        System.out.println("Clicks to get from " + article + " to Philosophy: " + clicks);
                    else {
                        System.out.println("Infinite loop detected! # of articles until repeating article: " + Math.abs(clicks)); 
                    }
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } 
            } 
        } finally {
            counter.close();    
        }
    }
    
}
