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
    private String subTitle;

    public Chunk(String title, String subTitle, String content) {
        super(title,content);
        this.subTitle = subTitle;

    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

}