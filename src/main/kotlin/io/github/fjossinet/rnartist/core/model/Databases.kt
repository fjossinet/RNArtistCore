package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import io.github.fjossinet.rnartist.core.io.writeVienna
import io.github.fjossinet.rnartist.core.ss
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.HashMap
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name

class RNArtistDB(val rootInvariantSeparatorsPath:String) {
    val indexedDirs = mutableListOf<String>()
    private val drawingsDirName = "drawings"
    var name:String = "No database selected"

    val indexFile: File
        get() {
            val f = File(File(this.rootInvariantSeparatorsPath), ".rnartist_db_index")
            if (!f.exists())
                f.createNewFile()
            else {
                f.readLines().forEach {
                    if (!this.indexedDirs.contains(it))
                        this.indexedDirs.add(it)
                }
            }
            return f
        }

    init {
        this.name = this.rootInvariantSeparatorsPath.split("/").last()
        this.indexFile //to read the index file or create an empty one
    }

    /**
     * This function search for all the path and field properties in the script to change them to fit this database location
     */
    fun importScript(script:File) {
        val lines = mutableListOf<String>()
        script.readLines().forEach { line ->
            if (line.trim().startsWith("file") || line.trim().startsWith("path")) {
                val path = line.trim().split("=").last().trim().removePrefix("\"").removeSuffix("\"")
                if (!path.startsWith(rootInvariantSeparatorsPath)) //the DB is coming from another location
                    lines.add("${line.split("=").first()} = \"${rootInvariantSeparatorsPath}${path.split(name).last()}\"")
                else
                    lines.add(line)
            } else
                lines.add(line)
        }
        val pw = PrintWriter(script)
        pw.write(lines.joinToString(separator = "\n"))
        pw.close()
    }

    /**
     * if init is true, the indexing is done like a first load. If some script exists & init is true, we check their path to fit this database location
     * @param withSVG the instruction to generate SVG files will be added to the scripts
     * @param createScriptForDataFiles generate the script for each data file now. Otherwise they will be generated when the script for the dataDir will be evaluated
     */
    fun indexDatabase(init:Boolean = false, createScriptForDataFiles:Boolean = false, noPNG:Boolean = false, withSVG:Boolean = false):List<File> {
        if (init) {
            this.indexedDirs.clear()
            this.indexFile.delete()
        }
        File(this.rootInvariantSeparatorsPath).listFiles()?.forEach {
            if (it.name.endsWith(".json")) {
                val jsonDir = File(File(this.rootInvariantSeparatorsPath), it.name.removeSuffix(".json"))
                if (!jsonDir.exists()) {
                    println("Processing ${it.name}...")
                    jsonDir.mkdir()
                    //we have json data to dump
                    val rnas = Gson().fromJson(it.readText(), Any::class.java)
                    (rnas as? LinkedTreeMap<String, Any>)?.forEach { rnaName, rnaDetails ->
                        var rna: RNA? = null
                        var basePairs: MutableList<BasePair>? = null
                        var reactivities: MutableList<Double>? = null
                        (rnaDetails as? LinkedTreeMap<Any, Any>)?.forEach { key, value ->
                            when (key) {
                                "sequence" -> {
                                    rna = RNA(rnaName, value as String)
                                }

                                "paired_bases" -> {
                                    basePairs = mutableListOf()
                                    (value as? ArrayList<ArrayList<Double>>)?.forEach { basePair ->
                                        val pos1 = basePair.get(0).toInt() + 1 //0-indexed
                                        val pos2 = basePair.get(1).toInt() + 1 //0-indexed
                                        basePairs!!.add(
                                            BasePair(
                                                Location(
                                                    Location(pos1, pos1),
                                                    Location(pos2, pos2)
                                                )
                                            )
                                        )
                                    }
                                }

                                "dms" -> {
                                    reactivities = mutableListOf()
                                    (value as? ArrayList<Double>)?.forEach { reactivity ->
                                        reactivities!!.add(reactivity)
                                    }
                                }
                            }
                        }
                        rna?.let { rna ->
                            basePairs?.let { basePairs ->
                                val viennaFile = File(jsonDir, "${rnaName}.vienna")
                                if (!viennaFile.exists())
                                    writeVienna(
                                        SecondaryStructure(rna = rna, basePairs = basePairs),
                                        viennaFile.writer()
                                    )
                            }
                            reactivities?.let { reactivities ->
                                val reactivitiesFile = File(jsonDir, "${rnaName}.txt")
                                if (!reactivitiesFile.exists())
                                    reactivitiesFile.writeText(reactivities.mapIndexed { index, reactivity -> "${index + 1} $reactivity" }
                                        .joinToString(separator = "\n"))
                            }
                        }
                    }
                }
            }
        }
        val dataDirs = this.searchForNonIndexedDirs()
        val pw =  PrintWriter(FileWriter(this.indexFile, !init)) //!init means no append if we reinit the indexing as a first load
        dataDirs.forEach { dataDir ->
            this.indexedDirs.add(dataDir.invariantSeparatorsPath)
            pw.println(dataDir.invariantSeparatorsPath)
            val scriptDataDir = File(File(dataDir.invariantSeparatorsPath).parent, "${dataDir.invariantSeparatorsPath.split("/").last()}.kts")
            if (!scriptDataDir.exists())
                this.getScriptForDataDir(dataDir, createScriptForDataFiles, noPNG, withSVG)
            else if (scriptDataDir.exists() && init)
                importScript(scriptDataDir)
            dataDir.listFiles()?.let { files ->
                files.forEach { file ->
                    if (file.name.endsWith("vienna")) {
                        val scriptVienna = File("${file.invariantSeparatorsPath.split(".vienna").first()}.kts")
                        if (scriptVienna.exists() && init)
                            importScript(scriptVienna)
                    }
                }
            }
        }
        pw.close()
        return dataDirs
    }

    fun createNewFolder(absPathFolder:String): File? {
        val folder = File(absPathFolder)
        if (folder.invariantSeparatorsPath.startsWith(this.rootInvariantSeparatorsPath)) {
            folder.mkdir()
            val fw = FileWriter(this.indexFile, true)
            fw.appendLine(absPathFolder)
            fw.close()

            indexedDirs.add(absPathFolder)

            this.getScriptForDataDir(folder) //we create the script file
            return folder
        }
        return null
    }

    /**
     * Create and return the dsl script to save and plot a secondary structure in the database.
     * An RNArtistEl root can be provided (containing custom layout, theme,...) otherwie a default one will be generated.
     * A PNGEl will be added to this root to generate the png thumbnail
     * A ViennaEl  will be added to this root to generate the vienna file
     * @return a file for the dsl script. This file has been created in the dataDir given as argument. This script needs now to be evaluated in order to produce the vienna and png files at the right places.
     */
    fun addNewStructure(dataDir:File, ss:SecondaryStructure, rnArtistEl: RNArtistEl? = null):File {

        var fileName = ss.name
        var i = 1
        if (dataDir.listFiles().map { it.name }.contains("$fileName.vienna")) {
            fileName = "${fileName}_$i"
            i++
        }

        ss.name = fileName
        ss.rna.name = fileName

        val viennaFile = File(dataDir, "$fileName.vienna")
        writeVienna(
            ss,
            PrintWriter(viennaFile)
        )

        val rnartistEl = rnArtistEl ?: initScript()

        with (rnartistEl.addPNG()) {
            this.setPath(getDrawingsDirForDataDir(dataDir).invariantSeparatorsPath)
            this.setWidth(250.0)
            this.setHeight(250.0)
        }

        val viennaEl = rnartistEl.addSS().addVienna()
        viennaEl.setFile(viennaFile.toPath().invariantSeparatorsPathString)

        val scriptContent = rnartistEl.dump().toString()

        val script = File(dataDir, "${fileName}.kts")
        script.createNewFile()
        script.writeText(scriptContent)
        return script
    }

    fun getDrawingsDirForDataDir(dataDir:File):File {
        val path = Paths.get(
                this.rootInvariantSeparatorsPath,
                this.drawingsDirName,
                *dataDir.invariantSeparatorsPath.split(this.rootInvariantSeparatorsPath).last().removePrefix(System.getProperty("file.separator"))
                        .removeSuffix(System.getProperty("file.separator")).split(System.getProperty("file.separator")).toTypedArray()
        ).invariantSeparatorsPathString
        return File(path)
    }

    fun getScriptForDataDir(dataDir:File, createScriptForDataFiles:Boolean = false, noPNG:Boolean = false, withSVG:Boolean = false):File {
        val script = File(dataDir.parent, "${dataDir.name}.kts")
        if (!script.exists()) {
            script.createNewFile()
            val rnartistEl = initScript()
            val outputDir = getDrawingsDirForDataDir(dataDir)

            if (!noPNG)
                with(rnartistEl.addPNG()) {
                    this.setPath(outputDir.invariantSeparatorsPath)
                    this.setWidth(250.0)
                    this.setHeight(250.0)
                }

            if (withSVG)
                with(rnartistEl.addSVG()) {
                    this.setPath(outputDir.invariantSeparatorsPath)
                    this.setWidth(1000.0)
                    this.setHeight(1000.0)
                }

            with (rnartistEl.addSS()) {
                this.addVienna().setPath(dataDir.invariantSeparatorsPath)
                this.addCT().setPath(dataDir.invariantSeparatorsPath)
                this.addBPSeq().setPath(dataDir.invariantSeparatorsPath)
            }

            script.writeText(rnartistEl.dump().toString())
        }

        if (createScriptForDataFiles) {
            dataDir.listFiles(FileFilter {
                it.name.endsWith(".ct") || it.name.endsWith(".vienna") || it.name.endsWith(
                    ".bpseq"
                ) || it.name.endsWith(".pdb")
            })?.forEach { dataFile ->
                getScriptForDataFile(dataFile, withSVG)
            }
        }

        return script
    }

    fun getPreviewForDataFile(dataFile:File):File  = File(getDrawingsDirForDataDir(dataFile.parentFile), "${dataFile.name.split(".kts").first()}.png")

    fun getScriptForDataFile(dataFile:File, noPNG:Boolean = false, withSVG:Boolean = false, minColor: String = "lightyellow", minValue:Double = 0.0, maxColor:String = "firebrick", maxValue:Double = 1.0):File {
        return io.github.fjossinet.rnartist.core.io.getScriptForDataFile(dataFile, getDrawingsDirForDataDir(dataFile.parentFile), noPNG, withSVG, minColor, minValue, maxColor, maxValue)
    }

    fun containsStructuralData(path: Path): Boolean {
        var containsStructuralData = false
        path.toFile().listFiles(FileFilter {
            it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct") || it.name.endsWith(
                ".pdb"
            )
        })?.let {
            containsStructuralData = it.isNotEmpty()
        }
        return containsStructuralData
    }

    private fun searchForNonIndexedDirs(dirs: MutableList<File> = mutableListOf<File>(), dir: Path = File(this.rootInvariantSeparatorsPath).toPath()): MutableList<File> {
        Files.newDirectoryStream(dir).use { stream ->
            for (path in stream) {
                if (path.toFile().isDirectory() && path.name != drawingsDirName) {
                    val containsStructuralData = containsStructuralData(path)
                    if (containsStructuralData && !indexedDirs.contains(path.invariantSeparatorsPathString) && !dirs.contains(path.toFile()) && path.invariantSeparatorsPathString.startsWith(rootInvariantSeparatorsPath))
                        dirs.add(path.toFile())
                    searchForNonIndexedDirs(dirs, path)
                }
            }
        }
        return dirs
    }
}

class NCBI {

    val baseURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"

    /**
     * Return a map whose key is the entry id asked for and value is the title found (or null if nothing found)
     */
    fun getSummaryTitle(vararg nucleotideEntryId:String):Map<String,String?> {
        val titles = mutableMapOf<String, String?>()
        try {
            val ids = nucleotideEntryId.toList()
            ids.chunked(20).forEach { subids -> //to avoid to send to many ids in a single request
                subids.forEach {
                    titles[it] = null
                }
                var currentId: String? = null
                URL("${baseURL}/esummary.fcgi?db=nuccore&id=${subids.joinToString(",")}").readText()
                    .split("<Item Name=\"").forEach { line ->
                    if (line.startsWith("Caption")) {
                        Regex(".+>(.+)<.+").find(line)?.groups?.get(1)?.let { match ->
                            subids.find { it.startsWith(match.value) }?.let { id ->
                                currentId = id
                            }
                        }
                    }
                    if (line.startsWith("Title")) {
                        Regex(".+>(.+)<.+").find(line)?.groups?.get(1)?.let { match ->
                            currentId?.let {
                                titles[it] = match.value
                            }
                        }
                    }
                }
                Thread.sleep(1000)
            }

        } catch (e:Exception) {
            println(nucleotideEntryId)
            e.printStackTrace()
        }
        return titles
    }

}

class RNACentral {

    val baseURL = "https://rnacentral.org/api/v1/rna"

    fun fetch(id:String):SecondaryStructure? {
        //the sequence
        var text = URL("${baseURL}/${id}?format=json").readText()
        var data = Gson().fromJson(text, HashMap<String, String>().javaClass)
        val sequence = data["sequence"]
        sequence?.let {
            text = URL("${baseURL}/${id}/2d/1/?format=json").readText()
            data = Gson().fromJson(text, HashMap<String, String>().javaClass)
            val bn = (data["data"] as Map<String, String>)["secondary_structure"]
            bn?.let {
               ss {
                    bn {
                        value = bn
                        seq = sequence
                        name = id
                    }

                }.forEach {
                   it.source = RfamSource(id)
                   return it
               }
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

    fun getEntry(pdbID: String) = StringReader(URL("https://files.rcsb.org/download/${pdbID}.pdb").readText())

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
            val url = URL("https://search.rcsb.org/rcsbsearch/v1/query?$reqParam")

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
                        ids.add(it["identifier"] as String)
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

    fun getEntry(pdbFileName: String) = StringReader(URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/$pdbFileName").readText())

}

class Rfam(var nameAsAccessionNumbers:Boolean = true) {
    fun getEntry(rfamID:String):StringReader? {
        try {
            val url = URI("https://rfam.org/family/$rfamID/alignment?acc=$rfamID&format=stockholm&download=0").toURL()
            return StringReader(url.readText())
        } catch (e:Exception) {
            return null
        }

    }
}