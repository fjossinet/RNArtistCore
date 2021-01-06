package io.github.fjossinet.rnartist.core.model.io

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import java.net.URL

class RNACentral {

    val baseURL = "https://rnacentral.org/api/v1/rna"

    fun fetch(id:String):SecondaryStructure? {
        //the sequence
        var text = URL("${baseURL}/${id}?format=json").readText()
        var data = Gson().fromJson(text, HashMap<String, String>().javaClass)
        val sequence = data.get("sequence")
        sequence?.let {
            val rna = RNA(id, seq = sequence, source="db:rnacentral:${id}")
            text = URL("${baseURL}/${id}/2d/1/?format=json").readText()
            data = Gson().fromJson(text, HashMap<String, String>().javaClass)
            val bn = (data.get("data") as Map<String, String>).get("secondary_structure")
            bn?.let {
               return SecondaryStructure(rna, bracketNotation = bn, source="db:rnacentral:${id}")
            }
        }

        return null
    }
}