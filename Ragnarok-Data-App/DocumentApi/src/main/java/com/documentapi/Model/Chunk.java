/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Model;

/**
 *
 * @author brune
 */
public class Chunk extends CompleteDocument {
    private String paragraph;

    public Chunk(String title, String paragraph, String content) {
        super(title,content);
        this.paragraph = paragraph;
    }
    
    public Chunk(){}

    public String getParagraph() {
        return paragraph;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}