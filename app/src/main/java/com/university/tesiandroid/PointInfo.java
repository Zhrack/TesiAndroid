package com.university.tesiandroid;

/**
 * Created by Davide on 31/03/2016.
 */
public class PointInfo {

    public static final int WIKI_NOT_PRESENT = -1;
    public static final int WIKI_TO_PROCESS = 0;
    public static final int WIKI_READY = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getWikiText() {
        return wikiText;
    }

    public void setWikiText(String wikiText) {
        this.wikiText = wikiText;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private String name;
    private double latitude;
    private double longitude;
    private String wikiText;
    private String language;
    private int distance;
}
