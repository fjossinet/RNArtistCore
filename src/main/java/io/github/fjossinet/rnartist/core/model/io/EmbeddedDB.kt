package io.github.fjossinet.rnartist.core.model.io

import io.github.fjossinet.rnartist.core.model.*
import org.dizitart.no2.*
import org.dizitart.no2.Document.createDocument
import org.dizitart.no2.IndexOptions.indexOptions
import org.dizitart.no2.filters.Filters.eq
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

class EmbeddedDB() {

    var pdbDB:Nitrite
    var userDB:Nitrite
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
        val doc = createDocument("pdbId",ss.pdbId)
        doc.put("title",ss.title)
        doc.put("authors",ss.authors)
        doc.put("pubDate",ss.pubDate)
        doc.put("chain",ss.rna.name)
        doc.put("ss",ss)
        this.pdbDB.getCollection("SecondaryStructures").insert(doc)

        val ouputFile = File(File(File(rootDir,"images"),"pdb"), ss.pdbId + ".jpg")
        if (!ouputFile.exists()) {
            try {
                val url = URL("https://cdn.rcsb.org/images/rutgers/" + ss.pdbId!!.substring(1, 3).toLowerCase() + "/" + ss.pdbId!!.toLowerCase() + "/" + ss.pdbId!!.toLowerCase() + ".pdb1-250.jpg")
                val image = ImageIO.read(url)
                ouputFile.createNewFile()
                ImageIO.write(image, "jpg", ouputFile)
            } catch (e: Exception) {
                e.printStackTrace()
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
        val interactions = mutableListOf<BasePair>()
        for (interaction in doc.get("interactions") as MutableList<Map<String,String>>) {
                interactions.add(
                    BasePair(
                        Location(
                            (interaction.get("start") as String).toInt(),
                            (interaction.get("end") as String).toInt()
                        ),
                        Edge.valueOf(interaction.get("edge5") as String),
                        Edge.valueOf(interaction.get("edge3") as String),
                        Orientation.valueOf(interaction.get("orientation") as String)
                    )
                )
        }
        //we set the current drawingConfiguration as it was during the saving of this project
        val theme = doc.get("theme") as Map<String,String>

        //we set the current graphicsContext as it was during the saving of this project
        val graphicsContext = doc.get("graphicsContext") as Map<String,String>
        return Project(
            SecondaryStructure(
                RNA(
                    rna.get("name") as String,
                    rna.get("seq") as String
                ),
                basePairs = interactions
            ), null, theme, graphicsContext
        )
    }

    fun addTheme(name:String, author:String, theme: Theme):NitriteId {
        val doc = createDocument("name",name)
        doc.put("author", author)
        doc.put("theme", theme)
        val r = this.userDB.getCollection("Themes").insert(doc)
        return r.first()
    }

    fun getThemes():NitriteCollection {
        return this.userDB.getCollection("Themes")
    }

    fun addProject(name: String, secondaryStructureDrawing: SecondaryStructureDrawing):NitriteId {
        val doc = createDocument("name",name)

        doc.put("rna", mutableMapOf<String,String>(
                "name" to secondaryStructureDrawing.secondaryStructure.rna.name,
                "seq" to secondaryStructureDrawing.secondaryStructure.rna.seq
        ))
        val interactions = mutableListOf<Map<String,String>>();

        for (i in secondaryStructureDrawing.secondaryStructure.secondaryInteractions) {
            interactions.add(mapOf(
                    "start" to "${i.start}",
                    "end" to "${i.end}",
                    "edge5" to i.edge5.name,
                    "edge3" to i.edge3.name,
                    "orientation" to i.orientation.name
            ))
        }

        for (i in secondaryStructureDrawing.secondaryStructure.tertiaryInteractions) {
            interactions.add(mapOf(
                    "start" to "${i.start}",
                    "end" to "${i.end}",
                    "edge5" to i.edge5.name,
                    "edge3" to i.edge3.name,
                    "orientation" to i.orientation.name
            ))
        }
        doc.put("interactions", interactions)

        //save infos to clone the layout
        for (j in secondaryStructureDrawing.allJunctions) {
            //j.junction.location //to have the junction identity
            //j.layout
            //j.radius
        }
        //save infos to clone the design (DrawingConfiguration)
        doc.put("drawingConfiguration", secondaryStructureDrawing.theme)
        //save infos to clone the GraphicsContext (zoom, translation)
        doc.put("graphicsContext", mutableMapOf<String,String>(
                "viewX" to "${secondaryStructureDrawing.workingSession.viewX}",
                "viewY" to "${secondaryStructureDrawing.workingSession.viewY}",
                "finalZoomLevel" to "${secondaryStructureDrawing.workingSession.finalZoomLevel}"
        ))
        val r = this.userDB.getCollection("Projects").insert(doc)
        return r.first()
    }

    fun getProjects():NitriteCollection {
        return this.userDB.getCollection("Projects")
    }

}