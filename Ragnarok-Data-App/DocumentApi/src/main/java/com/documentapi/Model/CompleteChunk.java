/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.documentapi.Model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author brune
 */
@JsonPropertyOrder({ "main", "head", "part", "section", "title", "paragraph", "paragraphSubtitle", "content" })
public class CompleteChunk extends Chunk {

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
    
    
    
    private String paragraphSubtitle;
    private String section;
    private String part;
    private String head;
    private String main;

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }
        
    public CompleteChunk(){}

    public String getParagraphSubtitle() {
        return paragraphSubtitle;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setParagraphSubtitle(String paragraphSubtitle) {
        this.paragraphSubtitle = paragraphSubtitle;
    }

    
    public CompleteChunk(String title, String paragraph, String content, String paragraphSubtitle) {
        super(title, paragraph, content);
        this.paragraphSubtitle = paragraphSubtitle;
    }

   

    
    
}
