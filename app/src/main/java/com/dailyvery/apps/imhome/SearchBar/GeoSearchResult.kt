package com.dailyvery.apps.imhome.SearchBar

import android.location.Address

/**
 * Created by SidenessPC on 10/02/2017.
 */

class GeoSearchResult(private val address: Address) {

    fun getAddress(): String {

        var display_address = ""

        display_address += address.getAddressLine(0) + "\n"

        for (i in 1 until address.maxAddressLineIndex) {
            display_address += address.getAddressLine(i) + ", "
        }

        display_address = display_address.substring(0, display_address.length - 2)

        return display_address
    }

    override fun toString(): String {
        var display_address = ""

        if (address.featureName != null) {
            display_address += address.toString() + ", "
        }

        for (i in 0 until address.maxAddressLineIndex) {
            display_address += address.getAddressLine(i)
        }

        return display_address
    }
}
