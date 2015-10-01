package com.appsandlabs.models;

import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class Song extends  BaseModel{
    public String title;
    public List<String> lyricists;
    public List<String> genre;
    public List<String> singers;
    public int rating;
    public Album album;
}
