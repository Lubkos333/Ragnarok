package com.mongoiswriter.Model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author brune
 */
public class Segments {
    private final String typSbirky;
    private final String cisloAktu;

    public Segments(String typSbirky, String cisloAktu) {
        if (typSbirky == null || typSbirky.trim().isEmpty()) {
            throw new IllegalArgumentException("typSbirky cannot be null or empty.");
        }
        if (cisloAktu == null || cisloAktu.trim().isEmpty()) {
            throw new IllegalArgumentException("cisloAktu cannot be null or empty.");
        }
        this.typSbirky = typSbirky;
        this.cisloAktu = cisloAktu;
    }

    public String getTypSbirky() {
        return typSbirky;
    }

    public String getCisloAktu() {
        return cisloAktu;
    }

    @Override
    public String toString() {
        return "Segments{" +
                "typSbirky='" + typSbirky + '\'' +
                ", cisloAktu='" + cisloAktu + '\'' +
                '}';
    }
}
