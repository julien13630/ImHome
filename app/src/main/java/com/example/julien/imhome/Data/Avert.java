package com.example.julien.imhome.Data;

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

    private String libelle, ssid, contactName, contactNumber, messageText;
    private int hashcode;
    private Date addDate = new Date();

    public Avert() {

    }



    public Avert(Parcel in) {
        this.libelle = in.readString();
        this.ssid = in.readString();
        this.contactName = in.readString();
        this.contactNumber = in.readString();
        this.messageText = in.readString();
        this.hashcode = in.readInt();
        Date tmpDate = new Date();
        android.text.format.DateFormat.format(in.readString(),tmpDate);
        this.addDate = tmpDate ;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(libelle);
        dest.writeString(ssid);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(messageText);
        dest.writeInt(hashcode);
        dest.writeString(addDate.toString());
    }

    @Override
    public String toString(){
        return this.getContactNumber();
    }
}
