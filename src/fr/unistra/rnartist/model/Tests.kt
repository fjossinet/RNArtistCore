package fr.unistra.rnartist.model

import fr.unistra.rnartist.model.io.parseVienna
import java.awt.Rectangle
import java.io.StringReader

fun main(args:Array<String>) {
    var ss = SecondaryStructureDrawing(parseVienna(StringReader(">test\nUGCCAAXGCGCA\n(((.(...))))"))!!, Rectangle(0,0,400,400), Theme(null))
    for (r in ss.residues) {
        println(r.center)
    }
    for (f in NDB().listPDBFileNames())
        println(f)
}

