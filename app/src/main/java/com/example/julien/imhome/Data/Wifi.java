package com.example.julien.imhome.Data;

/**
 * Created by julie on 22/10/2015.
 */


public class Wifi {

    private String libelle, ssid;
    private int hascode;
    private boolean favorite;


    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getHascode() {
        return hascode;
    }

    public void setHascode(int hascode) {
        this.hascode = hascode;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
