/**
 * this Script creates the data for the project
 */

import io.github.fjossinet.rnartist.core.io.randomColor
import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.core.numbering
import io.github.fjossinet.rnartist.core.rnartist
import java.awt.datatransfer.SystemFlavorMap
import java.awt.geom.Rectangle2D
import java.nio.file.Files
import java.nio.file.Paths

if (args.size < 1) {
    println(
        """To run this script:
kotlin -cp location_of_your_rnartistcore-jar-with-dependencies.jar outputdir
        """.trimMargin())
    System.exit(0)
}

val outputDir = Paths.get(args[0]).toFile()

if (!outputDir.exists())
    outputDir.mkdir()

var totalNts = 0

(1..4069).forEach {
    if (!arrayOf(9,10,1072,1728,2680,3079,3086,3087).contains(it)) {
        val rfamID = "RF${"%05d".format(it)}"
        println(rfamID)
        val familyDir = outputDir.toPath().resolve(rfamID).toFile()
        if (!familyDir.exists()) {
            familyDir.mkdir()
            try {
                //first we generate the structure to have all the structural domains
                var structures = rnartist {
                    png {
                        path = "${familyDir.path}"
                        height = 200.0
                        width = 200.0
                    }

                    ss {
                        rfam {
                            id = rfamID
                            name = "consensus"
                            use alignment numbering
                        }
                        theme {
                            details {
                                value = 3
                            }
                            color {
                                scheme = "Structural Domains"
                            }
                        }
                    }
                }

                totalNts += structures.first().secondaryStructure.rna.length

                val helixColors = StringBuilder()
                structures.first().allHelices.forEach {
                    helixColors.append("""
        color {
            location {
                ${it.location.blocks.first().start} to ${it.location.blocks.first().end}
                ${it.location.blocks.last().start} to ${it.location.blocks.last().end}
            }
            value = "${getHTMLColorString(it.getColor())}"
        }
""")
                    helixColors.append(System.lineSeparator())
                }

                val junctionColors = StringBuilder()
                structures.first().allJunctions.forEach {
                    junctionColors.append("""
        color {
            location {
${it.location.blocks.map { "                ${it.start} to ${it.end}" }.joinToString(System.lineSeparator())}
            }
            value = "${getHTMLColorString(it.getColor())}"
        }
""")
                    junctionColors.append(System.lineSeparator())
                }

                val singleStrandColors = StringBuilder()
                structures.first().singleStrands.forEach {
                    singleStrandColors.append("""
        color {
            location {
                ${it.location.start} to ${it.location.end}
            }
            value = "${getHTMLColorString(it.getColor())}"
        }
""")
                    singleStrandColors.append(System.lineSeparator())
                }

                Files.move(familyDir.toPath().resolve("consensus.png"), familyDir.toPath().resolve("preview.png"))
                val script = familyDir.toPath().resolve("rnartist.kts").toFile()
                script.createNewFile()
                script.writeText(
                    """
import io.github.fjossinet.rnartist.core.*      

rnartist {
    ss {
        rfam {
            id = "$rfamID"
            name = "consensus"
            use alignment numbering
        }
    }
    theme {
        details { 
            value = 3
        }
$helixColors
$junctionColors
$singleStrandColors
    }
}           
    """.trimIndent()
                )
            } catch (e: Exception) {
                println("Exception for $rfamID: ${e.message}")
                familyDir.delete()
            }
        }
    }
}

println("Total Nts: $totalNts")



