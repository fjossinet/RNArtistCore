import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.parseVienna
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.StringReader

class Test {
    fun testReadmeCode() {
        //load the saved options and/or create default ones
        RnartistConfig.load()
        RnartistConfig.exportSVGWithBrowserCompatibility(true)
        var ss:SecondaryStructure? = null
        //load from a Vienna String
        ss = parseVienna(StringReader(">myRNA\nCGCUGAAUUCAGCG\n((((......))))"))
        //create object directly
        ss = SecondaryStructure(RNA(name = "myRNA", seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
        ss?.let {
            val theme = Theme()
            theme.fontName = "Futura"
            theme.secondaryInteractionWidth = 4.0
            theme.residueBorder = 1.0
            theme.GColor = Color(223, 1, 1)
            var drawing = SecondaryStructureDrawing(secondaryStructure = ss, theme = theme)

            var writer = FileWriter("media/myRNA.svg")
            writer.write(drawing.asSVG())
            writer.close()
        }
        //load from a Vienna file
        val viennaFile = File("media/rna.vienna")
        var ss2:SecondaryStructure? = null
        if (viennaFile.exists())
            ss2 = parseVienna(FileReader(viennaFile))
        ss2?.let {
            val theme = Theme()
            theme.fontName = "Arial"
            theme.phosphoDiesterWidth = 3.0
            theme.secondaryInteractionWidth = 1.0
            theme.residueBorder = 0.5
            theme.UColor = Color.WHITE
            theme.UChar = Color.BLACK
            theme.CColor = Color.RED
            theme.CChar = Color.WHITE
            var drawing = SecondaryStructureDrawing(secondaryStructure = ss2, theme = theme)
            var writer = FileWriter("media/rna.svg")
            writer.write(drawing.asSVG())
            writer.close()
        }
    }
}