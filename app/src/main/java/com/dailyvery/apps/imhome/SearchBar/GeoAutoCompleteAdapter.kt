package com.dailyvery.apps.imhome.SearchBar

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

import com.dailyvery.apps.imhome.R

import java.io.IOException
import java.util.ArrayList

/**
 * Created by SidenessPC on 10/02/2017.
 */

class GeoAutoCompleteAdapter(private val mContext: Context) : BaseAdapter(), Filterable {
    private var resultList: List<*> = ArrayList()

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): GeoSearchResult {
        return resultList[index] as GeoSearchResult
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.geo_search_result, parent, false)
        }

        (convertView!!.findViewById<View>(R.id.geo_search_result_text) as TextView).text = getItem(position).address

        return convertView
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    val locations = findLocations(mContext, constraint.toString())

                    // Assign the data to the FilterResults
                    filterResults.values = locations
                    filterResults.count = locations.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    resultList = results.values as List<*>
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun findLocations(context: Context, query_text: String): List<GeoSearchResult> {

        val geo_search_results = ArrayList<GeoSearchResult>()

        val geocoder = Geocoder(context, context.resources.configuration.locale)
        var addresses: List<Address>? = null

        try {
            // Getting a maximum of 15 Address that matches the input text
            addresses = geocoder.getFromLocationName(query_text, 15)

            for (i in addresses!!.indices) {
                val address = addresses[i]
                if (address.maxAddressLineIndex != -1) {
                    geo_search_results.add(GeoSearchResult(address))
                }
            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

        return geo_search_results
    }

    companion object {

        private val MAX_RESULTS = 10
    }
}
