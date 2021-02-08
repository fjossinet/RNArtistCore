/**
 * This script has been implemented to generate the data for the project RNAGallery (https://github.com/fjossinet/RNAGallery)
 * It needs Docker and the container Assemble2Docker (https://github.com/fjossinet/Assemble2Docker) both installed on your computer
 * To run this script:
 * kotlin -cp location_of_your_rnartistcore-jar-with-dependencies.jar fetchPDB.kts your_output_dir
 */

import java.io.*
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import io.github.fjossinet.rnartist.core.rnartist
import java.awt.Rectangle

if (args.size < 1) {
    println("To run this script, precise the output directory as argument")
    System.exit(0)
}

val outputdir = args[0]

val colorScheme = "Persian Carolina"

val t = Theme()
t.setConfigurationFor(
    SecondaryStructureType.Helix,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.SecondaryInteraction,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.SingleStrand,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.PKnot,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.InteractionSymbol,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.PhosphodiesterBond,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.Junction,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.AShape,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.UShape,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.GShape,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.CShape,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.XShape,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.A,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.U,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.G,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.C,
    DrawingConfigurationParameter.fulldetails,
    "true"
)
t.setConfigurationFor(
    SecondaryStructureType.X,
    DrawingConfigurationParameter.fulldetails,
    "true"
)

t.setConfigurationFor(
    SecondaryStructureType.TertiaryInteraction,
    DrawingConfigurationParameter.fulldetails,
    "true"
)

RnartistConfig.colorSchemes[colorScheme]!!.forEach { elementType, config ->
    config.forEach {
        t.setConfigurationFor(
            SecondaryStructureType.valueOf(elementType),
            DrawingConfigurationParameter.valueOf(it.key),
            it.value
        )
    }
}

val pdb = PDB()
val rnaview = Rnaview()
val ids = pdb.query()
val statusFile = File("${outputdir}/status.md")
val status = mutableListOf<List<String>>()
if (statusFile.exists()) {
    statusFile.readLines().forEach { line ->
        val tokens = line.split('|')
        if (tokens.size > 1 &&  !tokens[1].contains(Regex("(PDB|-+)"))) {
            status.add(tokens.slice(1..tokens.size - 1))
        }
    }
}
var non_annotated = 0
var non_helices = 0
var stored = 0
var pdb_not_available = 0
ids.forEach { pdbId ->
    try {
        if (!status.any { it.first().trim().equals("[${pdbId}](https://www.rcsb.org/structure/${pdbId})")}) {
            Thread.sleep(2000) //avoid flood
            val pdbFile = File("${outputdir}/${pdbId}.pdb")
            if (!pdbFile.exists()) {
                println("Downloading ${pdbId} (${ids.indexOf(pdbId) + 1}/${ids.size})")
                pdbFile.writeText(pdb.getEntry(pdbId).readText())
            }
            println("Annotating ${pdbId} (${ids.indexOf(pdbId) + 1}/${ids.size})")
            val tertiaryStructures = parsePDB(FileReader(pdbFile))
            rnaview.annotate(pdbFile).forEach {
                val s = mutableListOf("[${pdbId}](https://www.rcsb.org/structure/${pdbId})")
                s.add(it.rna.name)
                var found = false
                for (ts in tertiaryStructures)
                    if (it.rna.name.equals(ts.rna.name)) {
                        found = true
                        if (it.rna.length == ts.rna.length)
                            s.add("N")
                        else
                            s.add("Y")
                        break
                    }
                if (!found)
                    s.add("Y")  //should never happen
                if (!it.helices.isEmpty()) {
                    rnartist {
                        secondaryStructure = it
                    }?.let { drawing ->
                        s.add("v1")
                        val drawingFrame = drawing.getFrame().bounds
                        val frame = if (drawingFrame.width < 1024 || drawingFrame.height < 768)
                            Rectangle(0, 0, 1024, 768)
                        else
                            Rectangle(0, 0, drawingFrame.width, drawingFrame.height)
                        drawing.applyTheme(t)
                        drawing.fitTo(frame)

                        File("${outputdir}/${pdbId}_${it.rna.name}.json").writeText(toJSON(drawing))
                        s.add("[View](https://raw.githubusercontent.com/fjossinet/RNAGallery/main/PDB/${pdbId}_${it.rna.name}.json)")
                        status.add(s)
                        stored++
                    }
                } else {
                    non_helices++
                }
            }
        } else {
            println("Already done ${pdbId}")
            //open the json file (if any) and get the current version
        }
    } catch (e: FileNotFoundException) {
        non_annotated++
        //status.add(listOf("[${pdbId}](https://www.rcsb.org/structure/${pdbId})", "", "", "No annotation", "", "", ""))
    } catch (e:Exception) {
        pdb_not_available++
        println("Entry not available: ${pdbId}")
        println(e.message)
    }
}

statusFile.writeText("""
List of RNA 3D structures from the Protein DataBank annotated with the algorithm [RNAVIEW](http://ndbserver.rutgers.edu/ndbmodule/services/download/rnaview.html) and drawn with the framework [RNArtistCore](https://github.com/fjossinet/RNArtistCore).

For each entry, the list provides the following details:

- PDB ID: click on the link to view the PDB entry
- Chain identifier: name of the RNA chain found in the 3D structure
- Chain modified: the RNAVIEW algorithm can exclude residues described in the PDB file if it has difficulties to use them. 'Y' means that the chain has been modified during the 2D annotation, 'N' not.
- status: the status of the JSON files. v1 means first rough output from the 3D structure annotation. Above 1, the drawing has been improved with the graphical tool [RNArtist](https://github.com/fjossinet/RNArtist)
- JSON: a file to be opened with [RNArtist](https://github.com/fjossinet/RNArtist)

Statistics: 

${ids.size} PDB entries processed (X-ray structures containing at least one RNA chain and with a resolution less or equal to 3.0 Å). They are divided into:

- ${ids.size - pdb_not_available - non_annotated} entries annotated successfully
- ${non_helices + stored} molecular chains annotated successfully
- ${stored} molecular chains containing at least one helix and selected for the gallery


""".trimIndent())

statusFile.appendText("| PDB ID | Chain identifier  | Chain modified | Status |JSON |\n")
statusFile.appendText("| :------: | :------: | :------: | :----: | :----: |\n")
status.sortBy { it.first() }
status.forEach {
    statusFile.appendText("|${it[0]}|${it[1]}|${it[2]}|${it[3]}|${it[4]}|\n")
}