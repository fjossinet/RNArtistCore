package fr.unistra.rnartist.model

import fr.unistra.rnartist.model.io.Rnaview
import fr.unistra.rnartist.model.io.parseVienna
import java.awt.Rectangle
import java.io.StringReader

fun main(args:Array<String>) {
    val rnaview = Rnaview()
    println("Is docker installed: ${rnaview.isDockerInstalled()}");
    println("Is the image fjossinet/assemble2 installed (Docker needs to run for this test): ${rnaview.isAssemble2DockerImageInstalled()}");
    val ss = SecondaryStructure(RNA(name="myRNA",seq = "GGGAAACCC"),bracketNotation = "(((...)))")
    val drawing = SecondaryStructureDrawing(secondaryStructure = parseVienna(StringReader(">test\nUGCCAAXGCGCA\n(((.(...))))"))!!, frame = Rectangle(0,0,400,400))
    for (r in drawing.residues) {
        println(r.center)
    }
    for (f in NDB().listPDBFileNames())
        println(f)
}

