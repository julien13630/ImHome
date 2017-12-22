package com.dailyvery.apps.imhome.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by justefani on 21/12/2017.
 */

public class Location implements Parcelable {

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>()
    {
        @Override
        public Location createFromParcel(Parcel source)
        {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size)
        {
            return new Location[size];
        }
    };

    private String address, nick;
    private double latitude, longitude;

    public Location() {

    }

    public Location(Parcel in) {
        this.address = in.readString();
        this.nick = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public double getLat() {
        return latitude;
    }

    public void setLat(double latitude) {
        this.latitude = latitude;
    }

    public double getLong() {
        return longitude;
    }

    public void setLong(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(address);
        dest.writeString(nick);
    }

    @Override
    public String toString(){
        return this.getAddress();
    }
}

