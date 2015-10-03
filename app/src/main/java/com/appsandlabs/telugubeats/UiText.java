package com.appsandlabs.telugubeats;

/**
 * Created by abhinav on 9/27/15.
 */
public enum UiText {
    COPY_RIGHTS("Copyrights."),
    CONNECTING("Connecting"),
    CHECKING_FOR_FRIENDS("Fetching friends"),
    NEW_TEXT_AVAILABLE("New notification from samosa");

    String value = null;
    UiText(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
    public String getValue(Object...args){
        return String.format(value,args);
    }


}