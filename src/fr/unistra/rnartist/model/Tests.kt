package fr.unistra.rnartist.model

import fr.unistra.rnartist.model.io.Rnaview
import fr.unistra.rnartist.model.io.parseVienna
import java.awt.Color
import java.awt.Rectangle
import java.io.FileWriter
import java.io.StringReader

fun main(args:Array<String>) {
    val rnaview = Rnaview()
    println("Is docker installed: ${rnaview.isDockerInstalled()}");
    println("Is the image fjossinet/assemble2 installed (Docker needs to run for this test): ${rnaview.isAssemble2DockerImageInstalled()}");
    val ss = SecondaryStructure(RNA(name="myRNA",seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
    val drawing = SecondaryStructureDrawing(secondaryStructure = parseVienna(StringReader(">test\nCGCUGAAUUCAGCG\n((((......))))"))!!)
    drawing.theme.fontName = "Courier New"
    drawing.theme.residueBorder = 3
    drawing.theme.AColor = Color.RED
    /*val writer = FileWriter("media/myRNA.svg")
    writer.write(drawing.asSVG())
    writer.close()*/
    /*for (f in NDB().listPDBFileNames())
        println(f)*/
}

