package io.github.fjossinet.rnartist.core.model.io

import io.github.fjossinet.rnartist.core.model.*
import org.dizitart.no2.*
import org.dizitart.no2.Document.createDocument
import org.dizitart.no2.IndexOptions.indexOptions
import org.dizitart.no2.filters.Filters.eq
import java.awt.Rectangle
import java.awt.geom.Rectangle2D
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

class EmbeddedDB() {

    private var pdbDB:Nitrite
    private var userDB:Nitrite
    var rootDir = File(getUserDir(),"db")

    init {
        if (!rootDir.exists()) {
            rootDir.mkdir()
            val images = File(rootDir,"images")
            images.mkdir()
            File(images,"pdb").mkdir()
            val userImages = File(images,"user")
            userImages.mkdir()
        }
        var dataFile = File(rootDir,"pdb")
        this.pdbDB = Nitrite.builder()
                .compressed()
                .filePath(dataFile.absolutePath)
                .openOrCreate()
        val coll = this.pdbDB.getCollection("SecondaryStructures");
        if (!coll.hasIndex("pdbId")) {
            coll.createIndex("pdbId", indexOptions(IndexType.NonUnique));
        }
        if (!coll.hasIndex("title")) {
            coll.createIndex("title", indexOptions(IndexType.Fulltext));
        }
        if (!coll.hasIndex("authors")) {
            coll.createIndex("authors", indexOptions(IndexType.Fulltext));
        }
        if (!coll.hasIndex("pubDate")) {
            coll.createIndex("pubDate", indexOptions(IndexType.Fulltext));
        }
        dataFile = File(rootDir,"user")
        this.userDB = Nitrite.builder()
                .compressed()
                .filePath(dataFile.absolutePath)
                .openOrCreate()
    }

    fun addPDBSecondaryStructure(ss: SecondaryStructure) {
        ss.source?.split("db:pdb:")?.last()?.let { pdbId ->
            val doc = createDocument("pdbId", pdbId)
            doc.put("title", ss.title)
            doc.put("authors", ss.authors)
            doc.put("pubDate", ss.pubDate)
            doc.put("chain", ss.rna.name)
            doc.put("ss", ss)
            this.pdbDB.getCollection("SecondaryStructures").insert(doc)

            val ouputFile = File(File(File(rootDir, "images"), "pdb"), pdbId + ".jpg")
            if (!ouputFile.exists()) {
                try {
                    val url = URL(
                        "https://cdn.rcsb.org/images/rutgers/" +pdbId.substring(1, 3)
                            .toLowerCase() + "/" + pdbId.toLowerCase() + "/" + pdbId.toLowerCase() + ".pdb1-250.jpg"
                    )
                    val image = ImageIO.read(url)
                    ouputFile.createNewFile()
                    ImageIO.write(image, "jpg", ouputFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addPDBTertiaryStructure(ts: TertiaryStructure) {
        val doc = createDocument("pdbId",ts.pdbId)
        doc.put("title",ts.title)
        doc.put("authors",ts.authors)
        doc.put("pubDate",ts.pubDate)
        doc.put("chain",ts.rna.name)
        doc.put("ts",ts)
        this.pdbDB.getCollection("TertiaryStructures").insert(doc)

        val ouputFile = File(File(File(rootDir,"images"),"pdb"), ts.pdbId + ".jpg")
        if (!ouputFile.exists()) {
            try {
                val url = URL("https://cdn.rcsb.org/images/rutgers/" + ts.pdbId!!.substring(1, 3).toLowerCase() + "/" + ts.pdbId!!.toLowerCase() + "/" + ts.pdbId!!.toLowerCase() + ".pdb1-250.jpg")
                val image = ImageIO.read(url)
                ouputFile.createNewFile()
                ImageIO.write(image, "jpg", ouputFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPDBSecondaryStructure(pdbID:String):List<Document> {
        return this.pdbDB.getCollection("SecondaryStructures").find(eq("pdbId", pdbID)).toList();
    }

    fun getPDBSecondaryStructure(pdbID:String, chain:String):Document? {
        for (doc in this.getPDBSecondaryStructure(pdbID)) {
            if (doc.get("chain")!!.equals(chain)) {
                return doc
            }
        }
        return null
    }

    fun getPDBTertiaryStructures():NitriteCollection {
        return this.pdbDB.getCollection("TertiaryStructures")
    }

    fun getPDBTertiaryStructure(pdbID:String):List<Document> {
        return this.pdbDB.getCollection("TertiaryStructures").find(eq("pdbId", pdbID)).toList();
    }

    fun getPDBTertiaryStructure(pdbID:String, chain:String):Document? {
        for (doc in this.getPDBTertiaryStructure(pdbID)) {
            if (doc.get("chain")!!.equals(chain)) {
                return doc
            }
        }
        return null
    }

    fun getPDBSecondaryStructures():NitriteCollection {
        return this.pdbDB.getCollection("SecondaryStructures")
    }

    fun getProject(id: NitriteId): Project {
        val doc = this.userDB.getCollection("Projects").getById(id) as Document
        val rna = doc.get("rna") as Map<String,String>
        val secondaryStructure = SecondaryStructure(
            RNA(
                rna["name"] as String,
                rna["seq"] as String
            ), source = "db:rnartist:${id.toString()}")

        val structure = doc.get("structure") as Map<String, Map<String,Map<String, String>>>
        val helices = structure.get("helices") as Map<String,Map<String, String>>
        val secondaries = structure.get("secondaries") as Map<String,Map<String, String>>
        val tertiaries = structure.get("tertiaries") as Map<String,Map<String, String>>

        for ((location, tertiary) in tertiaries) {
            secondaryStructure.tertiaryInteractions.add(BasePair(Location(location), Edge.valueOf(tertiary.get("edge5")!!), Edge.valueOf(tertiary.get("edge3")!!), Orientation.valueOf(tertiary.get("orientation")!!)))
        }

        for ((_, helix) in helices) {
            val h = Helix(helix["name"]!!)
            val location = Location(helix["location"]!!)
            for (i in location.start..location.start+location.length/2-1) {
                val l = Location(Location(i), Location(location.end-(i-location.start)))
                val secondary = secondaries.get(l.toString())!!
                h.secondaryInteractions.add(BasePair(l, Edge.valueOf(secondary.get("edge5")!!), Edge.valueOf(secondary.get("edge3")!!), Orientation.valueOf(secondary.get("orientation")!!)))
            }
            secondaryStructure.helices.add(h)
        }

        val junctions = structure.get("junctions") as Map<String,Map<String, String>>
        for ((_, junction) in junctions) {
            val linkedHelices = mutableListOf<Helix>()
            junction["linked-helices"]!!.split(" ").forEach {
                for (h in secondaryStructure.helices) {
                    if (h.start == Integer.parseInt(it)) {
                        linkedHelices.add(h)
                        break
                    }
                }
            }
            val location = Location(junction["location"]!!)
            val j = Junction(junction["name"]!!, location, linkedHelices)
            secondaryStructure.junctions.add(j)
        }

        //We link the helices to their junctions
        for ((start, helix) in helices) {
            val h = secondaryStructure.helices.find { it.start == Integer.parseInt(start) }!!
            helix.get("first-junction-linked")?.let { startPos ->
                h.setJunction(secondaryStructure.junctions.find { it.location.start == Integer.parseInt(startPos) }!!)
            }

            helix.get("second-junction-linked")?.let { startPos ->
                h.setJunction(secondaryStructure.junctions.find { it.location.start == Integer.parseInt(startPos) }!!)
            }
        }

        val pknots = structure.get("pknots") as Map<String,Map<String, String>>

        for ((_, pknot) in pknots) {

            val pk = Pknot(pknot.get("name")!!)

            for (h in secondaryStructure.helices) {
                if (h.start ==  Integer.parseInt(pknot.get("helix")!!)) {
                    pk.helix = h
                    break
                }
            }
            pknot.get("tertiaries")?.split(" ")?.forEach { l ->
                val location = Location(l)
                for (tertiary in secondaryStructure.tertiaryInteractions) {
                    if (tertiary.location.start == location.start && tertiary.location.end == location.end) {
                        pk.tertiaryInteractions.add(tertiary)
                        break;
                    }
                }
            }
            secondaryStructure.pknots.add(pk)
            secondaryStructure.tertiaryInteractions.removeAll(pk.tertiaryInteractions)
        }

        secondaryStructure.source = "Project ${doc.get("name")}"

        val layout = doc.get("layout") as Map<String, Map<String, String>>

        val theme = doc.get("theme") as Map<String, Map<String, Map<String, String>>>

        val workingSession = doc.get("session") as Map<String, String>

        val ws = WorkingSession()
        ws.viewX = workingSession.get("view-x")!!.toDouble()
        ws.viewY = workingSession.get("view-y")!!.toDouble()
        ws.finalZoomLevel = workingSession.get("final-zoom-lvl")!!.toDouble()
        ws.frame = Rectangle(0, 0, workingSession.get("frame-width")!!.toInt(), workingSession.get("frame-height")!!.toInt())

        return Project(secondaryStructure, layout, theme, ws,null)
    }

    fun addProject(name: String, secondaryStructureDrawing: SecondaryStructureDrawing):NitriteId {
        val doc = createDocument("name",name)

        doc.put("rna", mutableMapOf<String,String>(
                "name" to secondaryStructureDrawing.secondaryStructure.rna.name,
                "seq" to secondaryStructureDrawing.secondaryStructure.rna.seq))

        //STRUCTURE
        doc.put("structure", dumpSecondaryStructure(secondaryStructureDrawing))

        //LAYOUT (the size and orientation of junctions)
        doc.put("layout", dumpLayout(secondaryStructureDrawing))

        //THEME (colors, line width, full details,...)
        doc.put("theme", dumpTheme(secondaryStructureDrawing))

        //WORKING SESSION
        doc.put("session", dumpWorkingSession(secondaryStructureDrawing))

        val r = this.userDB.getCollection("Projects").insert(doc)
        return r.first()
    }


    fun getProjects():NitriteCollection {
        return this.userDB.getCollection("Projects")
    }


}