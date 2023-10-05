import io.github.fjossinet.rnartist.core.io.parseStockholm
import io.github.fjossinet.rnartist.core.io.writeVienna
import io.github.fjossinet.rnartist.core.model.Rfam
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

val rfam = Rfam()

val rootDB = "/Volumes/T7/Projets/RfamForRNArtist"

val dir = File(rootDB)
if (!dir.exists())
    Files.createDirectory(Paths.get(rootDB))

(1..4108).forEach {
    val rfamID = "RF${"%05d".format(it)}"

    if (!(rfamID in listOf("RF03072"))) {

        rfam.getEntry(rfamID)?.let { reader ->
            println("Processing $rfamID...")
            val result = parseStockholm(reader)
            val (familyDescr, familyType) = result.first
            val familyTypeDir = File(
                Paths.get(
                    rootDB,
                    *familyType.split(";").map { it.replace(" ", "_").removePrefix("_").removeSuffix("_") }
                        .toTypedArray()
                ).toUri()
            )
            if (!familyTypeDir.exists())
                Files.createDirectories(familyTypeDir.toPath())
            val familyDescrDir = File(familyTypeDir, familyDescr.replace(" ", "_").replace("/", ""))
            if (!familyDescrDir.exists()) {
                Files.createDirectory(familyDescrDir.toPath())
                result.third.forEach {
                    val f = File(familyDescrDir, "${it.name.replace("/", "_")}.vienna")
                    if (!f.exists()) {
                        println("Dumping 2D ${f.name} in $rfamID (${familyDescrDir.invariantSeparatorsPath}) ")
                        val pw = FileWriter(f)
                        writeVienna(it, pw)
                    }
                }
            } else {
                println("...already done!")
            }
        }
    }
}