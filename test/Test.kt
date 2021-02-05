import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import io.github.fjossinet.rnartist.core.ss
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.*

class Test {

    fun tDsl() {
        booquet {
            file = "/Users/fjossinet/myFavRNA.svg"
            junction_diameter = 15.0
            color = "#E033FF"
            ss {
                rna {
                    name = "My Fav RNA"
                    sequence =
                        "GGGACCGCCCGGGAAACGGGCGAAAAACGAGGUGCGGGCACCUCGUGACGACGGGAGUUCGACCGUGACGCAUGCGGAAAUUGGAGGUGAGUUCGCGAAUACAAAAGCAAGCGAAUACGCCCUGCUUACCGAAGCAAGCG"
                }
                bracket_notation =
                    ".....((((((.....))))))....((((((((....))))))))....((((........))))..(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
            }
        }
    }

    fun tPDB() {
        val pdb = PDB()
        val rnaview = Rnaview()
        pdb.query().forEach { pdbId ->
            val pdbFile = java.io.File.createTempFile(pdbId, ".pdb")
            pdbFile.writeText(pdb.getEntry(pdbId).readText())
            try {
                println("annotating ${pdbId}")
                rnaview.annotate(pdbFile).forEach {
                    println(it.rna.length)
                }
            } catch (e:FileNotFoundException) {
                println("No XML file")
            }
        }
    }

    fun tRNACentral() {
        val id = "URS000044DFF6"
        RNACentral().fetch(id)?.let {
            val drawing = SecondaryStructureDrawing(it)
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
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
            t.setConfigurationFor(SecondaryStructureType.PKnot, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(
                SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true"
            )
            t.setConfigurationFor(
                SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "false"
            )
            t.setConfigurationFor(
                SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "true"
            )
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true")

            RnartistConfig.colorSchemes.get("Persian Carolina")!!.forEach { elementType, config ->
                config.forEach {
                    t.setConfigurationFor(
                        SecondaryStructureType.valueOf(elementType),
                        DrawingConfigurationParameter.valueOf(it.key),
                        it.value
                    )
                }
            }

            drawing.applyTheme(t)
            val frame = Rectangle(0, 0, 1920, 1080)
            drawing.fitTo(frame)

            File(System.getProperty("user.home"), "${id}.svg").writeText(
                toSVG(
                    drawing,
                    frame.width.toDouble(),
                    frame.height.toDouble()
                )
            )
        }
    }
}