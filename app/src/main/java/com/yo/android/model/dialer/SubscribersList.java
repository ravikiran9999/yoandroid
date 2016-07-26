package com.yo.android.model.dialer;

public class SubscribersList {

    private String uaip;
    private String sipdestination;

    private String duration;

    //    2014­10­09 10:05:15
    private String time;
    private String username;

    private String calltype;

    private String callcost;

    private String country;

    private String destination;

    private boolean arrowDown;

    public String getUaip() {
        return uaip;
    }

    public void setUaip(String uaip) {
        this.uaip = uaip;
    }

    public String getSipdestination() {
        return sipdestination;
    }

    public void setSipdestination(String sipdestination) {
        this.sipdestination = sipdestination;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getCallcost() {
        return callcost;
    }

    public void setCallcost(String callcost) {
        this.callcost = callcost;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "ClassPojo [uaip = " + uaip + ", sipdestination = " + sipdestination + ", duration = " + duration + ", time = " + time + ", username = " + username + ", calltype = " + calltype + ", callcost = " + callcost + ", country = " + country + ", destination = " + destination + "]";
    }

    public boolean isArrowDown() {
        return arrowDown;
    }

    public void setArrowDown(boolean arrowDown) {
        this.arrowDown = arrowDown;
    }
}