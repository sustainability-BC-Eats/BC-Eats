package com.example.bc_eats;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Food {
    private String date;
    private String notificationTitle;
    private String building;
    private String room;
    private String notificationBody;
    private String key;

    public Food() {    }

    public Food(String notificationTitle, String building, String room, String notificationBody, String key){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd, yyyy", Locale.getDefault());
        this.date = dateFormat.format(new Date());
        this.notificationTitle = notificationTitle;
        this.building = building;
        this.room = room;
        this.notificationBody = notificationBody;
        this.key = key;
    }

    //getters
    public String getBuilding() {
        return building;
    }
    public String getNotificationBody() {
        return notificationBody;
    }
    public String getNotificationTitle() {
        return notificationTitle;
    }
    public String getRoom() {
        return room;
    }

    public String getDate() {
        return date;
    }

    public String getKey() {
        return this.key;
    }



    //setters
    public void setBuilding(String building) {
        this.building = building;
    }

    public void setNotificationBody(String notificationBody) {
        this.notificationBody = notificationBody;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setKey(String key) {
        this.key = key;
    }



    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.notificationTitle + " " + this.building + " " + this.room + " "
                + this.date + " " + this.notificationBody);
        return sb.toString();
    }
}
