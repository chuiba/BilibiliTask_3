package org.example.domain;

public class Cookie {
    private static String DedeUserID;
    private static String bili_jct;
    private static String SESSDATA;

    public Cookie(String dedeUserID, String bili_jct, String SESSDATA) {
        this.DedeUserID = dedeUserID;
        this.bili_jct = bili_jct;
        this.SESSDATA = SESSDATA;
    }

    public Cookie() {

    }

    public String getDedeUserID() {
        return DedeUserID;
    }

    public void setDedeUserID(String dedeUserID) {
        this.DedeUserID = dedeUserID;
    }

    public String getBili_jct() {
        return bili_jct;
    }

    public void setBili_jct(String bili_jct) {
        this.bili_jct = bili_jct;
    }

    public String getSESSDATA() {
        return SESSDATA;
    }

    public void setSESSDATA(String SESSDATA) {
        this.SESSDATA = SESSDATA;
    }
}
