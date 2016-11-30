package com.dailyvery.apps.imhome.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by julie on 22/10/2015.
 */
public class Avert implements Parcelable {

    public static final Parcelable.Creator<Avert> CREATOR = new Parcelable.Creator<Avert>()
    {
        @Override
        public Avert createFromParcel(Parcel source)
        {
            return new Avert(source);
        }

        @Override
        public Avert[] newArray(int size)
        {
            return new Avert[size];
        }
    };

    private String label, ssid, contactName, contactNumber, messageText;
    private int hashcode;
    private double latitude, longitude;
    private Date addDate = new Date();
    private boolean flagReccurence;

    public Avert() {

    }



    public Avert(Parcel in) {
        this.label = in.readString();
        this.ssid = in.readString();
        this.contactName = in.readString();
        this.contactNumber = in.readString();
        this.messageText = in.readString();
        this.hashcode = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        Date tmpDate = new Date();
        android.text.format.DateFormat.format(in.readString(),tmpDate);
        this.addDate = tmpDate ;
        this.flagReccurence = in.readInt() != 0;     //flagReccurence == true if byte != 0
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getHashcode() {
        return hashcode;
    }

    public void setHashcode(int hashcode) {
        this.hashcode = hashcode;
    }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }

    public int getFlagReccurence() {
        return flagReccurence ? 1 : 0;
    }

    public void setFlagReccurence(boolean flagReccurence){
        this.flagReccurence = flagReccurence;
    }

    public void setFlagReccurence(int flagReccurence){
        this.flagReccurence = (flagReccurence == 1);
    }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(label);
        dest.writeString(ssid);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(messageText);
        dest.writeInt(hashcode);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(addDate.toString());
        dest.writeInt(flagReccurence ? 1 : 0);
    }

    @Override
    public String toString(){
        return this.getContactNumber();
    }
}
