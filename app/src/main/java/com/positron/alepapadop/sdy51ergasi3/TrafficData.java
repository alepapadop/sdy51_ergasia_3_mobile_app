package com.positron.alepapadop.sdy51ergasi3;

/**
 * Created by alepapadop on 1/30/18.
 */

public class TrafficData {
    public Double latitude;
    public Double longitude;
    public Integer neg_feedback;
    public Integer pos_feedback;
    public String timestamp;
    public String traffic;


    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public TrafficData() {
    }

    public TrafficData(String timestamp, String traffic, Double longitude, Double latitude) {
        this.timestamp = timestamp;
        this.traffic = traffic;
        this.longitude = longitude;
        this.latitude = latitude;
        this.neg_feedback = 0;
        this.pos_feedback = 0;
    }

}
