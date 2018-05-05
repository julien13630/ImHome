package com.dailyvery.apps.imhome.Data

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

/**
 * Created by justefani on 21/12/2017.
 */

class Location : Parcelable {

    var address: String? = null
    var nick: String? = null
    var lat: Double = 0.toDouble()
    var long: Double = 0.toDouble()

    constructor() {

    }

    constructor(`in`: Parcel) {
        this.address = `in`.readString()
        this.nick = `in`.readString()
        this.lat = `in`.readDouble()
        this.long = `in`.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(address)
        dest.writeString(nick)
    }

    override fun toString(): String? {
        return this.address
    }

    companion object {

        val CREATOR: Parcelable.Creator<Location> = object : Parcelable.Creator<Location> {
            override fun createFromParcel(source: Parcel): Location {
                return Location(source)
            }

            override fun newArray(size: Int): Array<Location> {
                return arrayOfNulls(size)
            }
        }
    }
}

