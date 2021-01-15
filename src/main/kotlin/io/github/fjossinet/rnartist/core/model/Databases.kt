package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.core.model.io.parsePDB
import io.github.fjossinet.rnartist.core.model.io.Rnaview
import io.github.fjossinet.rnartist.core.model.io.getUserDir
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import kotlin.collections.HashMap

class RNAGallery {
    fun getEntry(pdbID: String, chain:String): StringReader {
        val url = if (RnartistConfig.useOnlineRNAGallery) URL("https://raw.githubusercontent.com/fjossinet/RNAGallery/main/PDB/${pdbID}_${chain}.json") else File("${RnartistConfig.rnaGalleryPath}/PDB/${pdbID}_${chain}.json").toURI().toURL()
        return StringReader(url.readText())
    }
}

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

enum class PDBQueryField {
    MINRES,
    MAXRES,
    MINDATE,
    MAXDATE,
    KEYWORDS,
    AUTHORS,
    PDBIDS,
    TITLE_CONTAINS,
    CONTAINS_RNA,
    CONTAINS_PROTEIN,
    CONTAINS_DNA,
    CONTAINS_HYBRID,
    EXPERIMENTAL_METHOD
}

data class PDBQuery(
    val query:Map<String, Any> = mapOf(
        "type" to "group",
        "logical_operator" to "and",
        "nodes" to listOf<Map<String, Any>>(
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to  "in",
                    "value" to listOf("X-RAY DIFFRACTION", "ELECTRON MICROSCOPY"),
                    "attribute" to "exptl.method"
                )
            ),
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to "less_or_equal",
                    "value" to 3.0,
                    "attribute" to "rcsb_entry_info.resolution_combined"
                )
            ),
            mapOf(
                "type" to "terminal",
                "service" to "text",
                "parameters" to mapOf(
                    "operator" to "greater_or_equal",
                    "value" to 1,
                    "attribute" to "rcsb_entry_info.polymer_entity_count_RNA"
                )
            )
        )
    ),
    val request_options:Map<String,Any> =  mapOf(
        "pager" to mapOf(
            "start" to 0
        )
    ),
    val return_type:String = "entry"
)

data class PDBResponse(
    val query_id:String,
    val result_type:String,
    val total_count:Int,
    val explain_meta_data:Map<String,Any>,
    val result_set:List<Map<String,Any>>
)

class PDB() {

    fun getEntry(pdbID: String): StringReader {
        val url = URL("https://files.rcsb.org/download/${pdbID}.pdb")
        return StringReader(url.readText())
    }

    fun query():List<String> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        var totalHits = 0
        var currentPage = 0
        var ids = mutableSetOf<String>()
        do {
            var reqParam =
                URLEncoder.encode("json", "UTF-8") + "=" + URLEncoder.encode(gson.toJson(PDBQuery(request_options = mapOf(
                    "pager" to mapOf(
                        "start" to currentPage*10,
                        "rows" to 10
                    )
                ))), "UTF-8")
            val url = URL("https://search.rcsb.org/rcsbsearch/v1/query?" + reqParam)

            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    val hits = gson.fromJson(response.toString(), PDBResponse::class.java)

                    totalHits = hits.total_count

                    hits.result_set.forEach {
                        ids.add(it.get("identifier") as String)
                    }

                }
            }
            currentPage++
        } while ((currentPage-1)*10 < totalHits)

        return ids.toList().sorted()
    }


}

class NDB {

    fun listPDBFileNames():List<String> {
        var files = mutableSetOf<String>()
        val pattern = "([0-9].+?\\.pdb[0-9])".toRegex()
        val matches = pattern.findAll(URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/").readText())
        matches.forEach { matchResult ->
            files.add(matchResult.value)
        }
        return files.toList()
    }

    fun getEntry(pdbFileName: String): Reader {
        val url = URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/$pdbFileName")
        return StringReader(url.readText())
    }

    fun feedEmbeddedDatabase(embeddedDB:EmbeddedDB) {
        val pdbFileNames: List<String> = this.listPDBFileNames()
        val filesWithPbs = mutableListOf<String>()
        val filesParsed = mutableListOf<String>()
        val idsWithPb = File(File(getUserDir(), "db"), "idsWithPb.txt")
        if (!idsWithPb.exists()) idsWithPb.createNewFile() else {
            val filesWithPbBuff = BufferedReader(FileReader(idsWithPb))
            var l: String?
            while (filesWithPbBuff.readLine().also { l = it } != null) {
                filesWithPbs.add(l!!.trim { it <= ' ' })
            }
            filesWithPbBuff.close()
        }
        val filesProcessed = File(File(getUserDir(), "db"), "filesProcessed.txt")
        if (!filesProcessed.exists()) filesProcessed.createNewFile() else {
            val filesProcessedBuff = BufferedReader(FileReader(filesProcessed))
            var l: String?
            while (filesProcessedBuff.readLine().also { l = it } != null) {
                filesParsed.add(l!!.trim { it <= ' ' })
            }
            filesProcessedBuff.close()
        }
        val filesWithPbWriter = BufferedWriter(FileWriter(idsWithPb, true))
        val filesProcessedWriter = BufferedWriter(FileWriter(filesProcessed, true))
        val rnaview = Rnaview()
        for (fileName in pdbFileNames) {
            println("Parsing $fileName")
            val id = fileName.split(".pdb".toRegex()).toTypedArray()[0].toUpperCase()
            if (filesParsed.contains(fileName)) {
                println("File Already Stored")
                continue
            } else if (filesWithPbs.contains(fileName)) {
                println("File with problem")
                continue
            }
            try {
                for (ts in parsePDB(this.getEntry(fileName)).iterator()) {
                    ts.pdbId = id
                    if (embeddedDB.getPDBSecondaryStructure(id, ts.rna.name) != null) {
                        println("chain " + ts.rna.name + " for " + id + " already stored") //we can have several times the same chain in the PDB
                        if (!filesParsed.contains(fileName)) {
                            filesParsed.add(fileName)
                            filesProcessedWriter.write("$fileName\n")
                            filesProcessedWriter.flush()
                        }
                        continue
                    }
                    try {
                        rnaview.annotate(ts).forEach { ss ->
                            ss.source = "db:pdb:${ts.pdbId}"
                            ss.title = ts.title
                            ss.authors = ts.authors
                            ss.pubDate = ts.pubDate
                            ss.rna.name = ts.rna.name
                            embeddedDB.addPDBSecondaryStructure(ss) //RNAVIEW can remove some residues silently
                            if (ss.rna.length != ts.rna.length)
                                embeddedDB.addPDBTertiaryStructure(ts)
                            println("############# Secondary Structure Stored ################")
                            println("############# ${embeddedDB.getPDBSecondaryStructures().find().size()} Secondary Structures Stored ################")
                            if (!filesParsed.contains(fileName)) {
                                filesParsed.add(fileName)
                                filesProcessedWriter.write("$fileName\n")
                                filesProcessedWriter.flush()
                            }
                        }
                    } catch (ex: Exception) {
                        if (!filesWithPbs.contains(fileName)) {
                            filesWithPbs.add(fileName)
                            filesWithPbWriter.write("$fileName\n")
                            filesWithPbWriter.flush()
                            println("File with problem")
                        }
                    }
                }
            } catch (e: Exception) {
                if (!filesWithPbs.contains(fileName)) {
                    filesWithPbs.add(fileName)
                    filesWithPbWriter.write("$fileName\n")
                    filesWithPbWriter.flush()
                    println("File with problem")
                }
            }
        }
    }
}

class Rfam {
    fun getEntry(rfamID:String): Reader {
        val url = URL("https://rfam.xfam.org/family/RF00010/alignment?acc=$rfamID&format=stockholm&download=0")
        return StringReader(url.readText())
    }
}