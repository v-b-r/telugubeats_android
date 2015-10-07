package com.appsandlabs.telugubeats.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abhinav on 10/3/15.
 */
public class User extends BaseModel{
        public String name;
        public String device_id;
        public String email_id;
        public String picture_url;
        public String cover_url;
        public String gender;
        public double birthday;
        public String place;
        public double created_at;
        public String country;
        private String status;
        public String google_plus_token;
        public String facebook_token;
        private ArrayList<String> badges;
        public HashMap<String,Integer> stats;
        public HashMap<String, Integer[]> winsLosses;
        private int userType = 0;
        public String google_plus_uid;
        public String fb_uid;
        public List<String> google_plus_friend_uids = new ArrayList<>();
        public List<String> fb_friend_uids = new ArrayList<>();

        public String auth_key;

        public void setName(String name) {
        this.name = name;
    }

        public boolean isSame(User eventUser) {
                return eventUser!=null&&eventUser.id.getId().equalsIgnoreCase(id.getId());
        }
}
