package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abhinav on 10/1/15.
 */


public class BaseModel {

    public static class Id {
        String $oid;

        @Override
        public String toString() {
            return $oid;
        }
    }

    @SerializedName("_id")
    public Id id;
}
