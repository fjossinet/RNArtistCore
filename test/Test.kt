import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.getUserDir
import io.github.fjossinet.rnartist.core.model.io.parseBPSeq
import io.github.fjossinet.rnartist.core.model.io.parseVienna
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.StringReader

class Test {
    fun testReadmeCode() {
        //load the saved options and/or create default ones
        RnartistConfig.loadConfig()
        var ss:SecondaryStructure? = null
        //load from a BPSEQ file
        val bpseqFile = File(getUserDir(),"my_file.bpseq")
        if (bpseqFile.exists()) parseBPSeq(FileReader(bpseqFile))
        else {
            //load from a Vienna String
            ss = parseVienna(StringReader(">myRNA\nCGCUGAAUUCAGCG\n((((......))))"))
            //create object directly
            ss = SecondaryStructure(RNA(name = "myRNA", seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
        }
        ss?.let {
            val theme = Theme()
            theme.fontName = "Futura"
            theme.secondaryInteractionWidth = 4
            theme.residueBorder = 1
            theme.GColor = Color(223, 1, 1)
            var drawing = SecondaryStructureDrawing(secondaryStructure = ss, theme = theme)

            var writer = FileWriter("media/myRNA.svg")
            writer.write(drawing.asSVG())
            writer.close()
        }
    }
}