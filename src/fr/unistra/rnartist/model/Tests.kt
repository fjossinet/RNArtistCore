package fr.unistra.rnartist.model

import fr.unistra.rnartist.model.io.Rnaview
import fr.unistra.rnartist.model.io.parseVienna
import java.awt.Color
import java.io.FileWriter
import java.io.StringReader

fun main(args:Array<String>) {
    val rnaview = Rnaview()
    println("Is docker installed: ${rnaview.isDockerInstalled()}");
    println("Is the image fjossinet/assemble2 installed (Docker needs to run for this test): ${rnaview.isAssemble2DockerImageInstalled()}");
    var ss = SecondaryStructure(RNA(name="myRNA",seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
    var drawing = SecondaryStructureDrawing(secondaryStructure = parseVienna(StringReader(">test\nCGCUGAAUUCAGCG\n((((......))))"))!!)
    drawing.theme.fontName = "Arial"
    drawing.theme.residueBorder = 3
    drawing.theme.AColor = Color.RED
    var writer = FileWriter("media/myRNA.svg")
    writer.write(drawing.asSVG())
    writer.close()

    ss = SecondaryStructure(RNA(name="myRNA2",seq = "GGGACCGCCCGGGAAACGGGCGAAAAACGAGGUGCGGGCACCUCGUGACGACGGGAGUUCGACCGUGACGCAUGCGGAAAUUGGAGGUGAGUUCCCUGCUUACCGAAGCAAGCG"), bracketNotation = ".....((((((.....))))))....((((((((....))))))))....((((........))))..(((.(((..........(((((((.....)))))))...))).)))")
    drawing = SecondaryStructureDrawing(secondaryStructure = ss)
    drawing.theme.residueBorder = 1
    writer = FileWriter("media/myRNA2.svg")
    writer.write(drawing.asSVG())
    writer.close()

    /*for (f in NDB().listPDBFileNames())
        println(f)*/
}

