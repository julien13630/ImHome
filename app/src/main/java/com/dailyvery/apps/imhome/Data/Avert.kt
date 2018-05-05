package com.dailyvery.apps.imhome.Data

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

/**
 * Created by julie on 22/10/2015.
 */
class Avert : Parcelable {

    var id: String? = null
    var label: String? = null
    var ssid: String? = null
    var contactName: String? = null
    var contactNumber: String? = null
    var messageText: String? = null
    var hashcode: Int = 0
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    var addDate = Date()
    private var flagReccurence: Boolean = false

    constructor() {

    }


    constructor(`in`: Parcel) {
        this.id = `in`.readString()
        this.label = `in`.readString()
        this.ssid = `in`.readString()
        this.contactName = `in`.readString()
        this.contactNumber = `in`.readString()
        this.messageText = `in`.readString()
        this.hashcode = `in`.readInt()
        this.latitude = `in`.readDouble()
        this.longitude = `in`.readDouble()
        val tmpDate = Date()
        android.text.format.DateFormat.format(`in`.readString(), tmpDate)
        this.addDate = tmpDate
        this.flagReccurence = `in`.readInt() != 0     //flagReccurence == true if byte != 0
    }

    fun getFlagReccurence(): Int {
        return if (flagReccurence) 1 else 0
    }

    fun setFlagReccurence(flagReccurence: Boolean) {
        this.flagReccurence = flagReccurence
    }

    fun setFlagReccurence(flagReccurence: Int) {
        this.flagReccurence = flagReccurence == 1
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(label)
        dest.writeString(ssid)
        dest.writeString(contactName)
        dest.writeString(contactNumber)
        dest.writeString(messageText)
        dest.writeInt(hashcode)
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeString(addDate.toString())
        dest.writeInt(if (flagReccurence) 1 else 0)
    }

    override fun toString(): String? {
        return this.contactNumber
    }

    companion object {

        val CREATOR: Parcelable.Creator<Avert> = object : Parcelable.Creator<Avert> {
            override fun createFromParcel(source: Parcel): Avert {
                return Avert(source)
            }

            override fun newArray(size: Int): Array<Avert> {
                return arrayOfNulls(size)
            }
        }
    }
}
