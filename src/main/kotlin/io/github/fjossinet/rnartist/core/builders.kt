package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileReader
import java.lang.Exception

class RNABuilder {
    var name:String = "A"
    var sequence:String? = null
    var length:Int? = null

    fun build():RNA? {
        this.sequence?.let {
            return RNA(name, it)
        }
        this.length?.let {
            val sequence = StringBuffer()
            sequence.append((1..it).map { listOf("A", "U", "G", "C").random()}.joinToString(separator = ""))
            return RNA(name, sequence.toString())
        }
        return null
    }
}

class SecondaryStructureBuilder {
    var rna:RNA? = null
    var bracket_notation:String? = null
    private var secondaryStructure:SecondaryStructure? = null

    fun build():SecondaryStructure? {
        this.secondaryStructure?.let {
            return it
        }
        this.bracket_notation?.let { bn ->
            this.rna?.let { rna ->
                return SecondaryStructure(rna, bracketNotation = bn)
            }
            val sequence = StringBuffer()
            sequence.append((1..bn.length).map { listOf("A", "U", "G", "C").random()}.joinToString(separator = ""))
            val ss = SecondaryStructure(RNA(seq = sequence.toString()), bracketNotation = bn)
            ss.randomizeSeq()
            return ss
        }
        return null
    }

    fun rna(setup:RNABuilder.() -> Unit) {
        val rnaBuilder = RNABuilder()
        rnaBuilder.setup()
        rna = rnaBuilder.build()
    }

    fun rfam(setup:RfamBuilder.() -> Unit) {
        val rfamBuilder = RfamBuilder()
        rfamBuilder.setup()
        secondaryStructure = rfamBuilder.build()
    }

    fun vienna(setup:ViennaBuilder.() -> Unit) {
        val viennaBuilder = ViennaBuilder()
        viennaBuilder.setup()
        secondaryStructure = viennaBuilder.build()
    }

    fun bpseq(setup:BPSeqBuilder.() -> Unit) {
        val bpSeqBuilder = BPSeqBuilder()
        bpSeqBuilder.setup()
        secondaryStructure = bpSeqBuilder.build()
    }

    fun ct(setup:CTBuilder.() -> Unit) {
        val ctBuilder = CTBuilder()
        ctBuilder.setup()
        secondaryStructure = ctBuilder.build()
    }

    fun pdb(setup:PDBBuilder.() -> Unit) {
        val pdbBuilder = PDBBuilder()
        pdbBuilder.setup()
        secondaryStructure = pdbBuilder.build()
    }

    fun stockholm(setup:StockholmBuilder.() -> Unit) {
        val stockholmBuilder = StockholmBuilder()
        stockholmBuilder.setup()
        secondaryStructure = stockholmBuilder.build()
    }

}

open abstract class FileBuilder {
    var file:String? = null

    abstract fun build():SecondaryStructure?
}

class PDBBuilder:FileBuilder() {

    var name:String? = null
    var id:String? = null

    override fun build(): SecondaryStructure? {
        if (this.id != null) {
            val pdbFile = java.io.File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.absolutePath
        }
        if (this.file != null) {
            var secondaryStructures = listOf<SecondaryStructure>()
            try {
                secondaryStructures = Rnaview().annotate(File(file))
            } catch (e:Exception) {
                println(e.message)
            }
            if (secondaryStructures.size == 1)
                return secondaryStructures.first()
            else if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name.equals(this.name))
                        return it
                }
            }
        }
        return null
    }
}

class ViennaBuilder:FileBuilder() {
    override fun build(): SecondaryStructure? {
        this.file?.let {
            return parseVienna(FileReader(this.file))
        }
        return null
    }
}

class BPSeqBuilder:FileBuilder() {
    override fun build(): SecondaryStructure? {
        this.file?.let {
            return parseBPSeq(FileReader(this.file))
        }
        return null
    }
}

class CTBuilder:FileBuilder() {
    override fun build(): SecondaryStructure? {
        this.file?.let {
            return parseCT(FileReader(this.file))
        }
        return null
    }
}

class StockholmBuilder:FileBuilder() {
    var name:String? = null

    override fun build(): SecondaryStructure? {
        this.file?.let {
            var secondaryStructures = parseStockholm(FileReader(this.file), withConsensus2D = true)
            if (secondaryStructures.size == 1)
                return secondaryStructures.first()
            else if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name.equals(this.name))
                        return it
                }
            }
        }
        return null
    }
}

open abstract class PublicDatabaseBuilder {
    var id:String? = null
    var name:String? = null

    abstract fun build():SecondaryStructure?
}


class RfamBuilder:PublicDatabaseBuilder() {
    override fun build(): SecondaryStructure? {
        this.id?.let { id ->
            this.name?.let {
                val secondaryStructures = parseStockholm(Rfam().getEntry(id), withConsensus2D = true)
                if ("consensus".equals(name))
                    return secondaryStructures.first()
                else {
                    secondaryStructures.forEach {
                        if (name.equals(it.rna.name))
                            return it
                    }
                    return null
                }
            }
            return null
        }
        return null
    }
}


class BooquetBuilder {
    var file:String? = null
    var width = 300.0
    var height = 300.0
    var junction_diameter = 25.0
    var color = getHTMLColorString(Color.BLACK)
    var secondaryStructure: SecondaryStructure? = null
    var line = 2.0

    fun build() {
        this.file?.let { outputFile ->
            this.secondaryStructure?.let { ss ->
                val svgOutput = Booquet(ss, this.width, this.height, junction_diameter = junction_diameter, lineWidth = line, color = getAWTColor(color))
                val f = File(outputFile)
                f.createNewFile()
                f.writeText(svgOutput)
            }
        }
    }

    fun ss(setup:SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructure = secondaryStructureBuilder.build()
    }

}

class RNArtistBuilder {
    var file:String? = null
    var secondaryStructure: SecondaryStructure? = null
    var themeBuilder:ThemeBuilder? = null

    fun build(): SecondaryStructureDrawing? {
        this.secondaryStructure?.let { ss ->
            val drawing = SecondaryStructureDrawing(ss, WorkingSession())
            //the frame will have the size of the drawing
            val drawingFrame = drawing.getFrame().bounds2D
            val frame = if (drawingFrame.width < 1024 || drawingFrame.height < 768)
                Rectangle2D.Double(0.0, 0.0, 1024.0, 768.0)
            else
                Rectangle2D.Double(0.0, 0.0, drawingFrame.width, drawingFrame.height)

            this.themeBuilder?.let { themeBuilder ->
                themeBuilder.colors.forEach { colorBuilder ->
                    if (colorBuilder.location != null) {
                        val location = Location(colorBuilder.location!!)
                        val t = AdvancedTheme()
                        colorBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                val selection = {e:DrawingElement -> location.positions.any {e.location.contains(it)} && e.type == it}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.color, colorBuilder.value.toString())
                            }
                        }
                        drawing.applyAdvancedTheme(t)
                    }
                    else {
                        val t = Theme()
                        colorBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                t.setConfigurationFor(it, DrawingConfigurationParameter.color, colorBuilder.value.toString())
                            }
                        }
                        drawing.applyTheme(t)
                    }
                }
                themeBuilder.details.forEach { detailsBuilder ->
                    if (detailsBuilder.location != null) {
                        val location = Location(detailsBuilder.location!!)
                        val t = AdvancedTheme()
                        detailsBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                val selection = {e:DrawingElement -> location.positions.any {e.location.contains(it)} && e.type == it}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, detailsBuilder.value.equals("full").toString())
                            }
                        }
                        drawing.applyAdvancedTheme(t)
                    }
                    else {
                        val t = Theme()
                        detailsBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                t.setConfigurationFor(it, DrawingConfigurationParameter.fulldetails, detailsBuilder.value.equals("full").toString())
                            }
                        }
                        drawing.applyTheme(t)
                    }
                }
                themeBuilder.lines.forEach { lineBuilder ->
                    if (lineBuilder.location != null) {
                        val location = Location(lineBuilder.location!!)
                        val t = AdvancedTheme()
                        lineBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                val selection = {e:DrawingElement -> location.positions.any {e.location.contains(it)} && e.type == it}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
                            }
                        }
                        drawing.applyAdvancedTheme(t)
                    }
                    else {
                        val t = Theme()
                        lineBuilder.type?.let { type ->
                            val types = getSecondaryStructureType(type)
                            types.forEach {
                                t.setConfigurationFor(it, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
                            }
                        }
                        drawing.applyTheme(t)
                    }
                }
            }
            drawing.fitTo(frame)
            this.file?.let { outputFile ->
                val svgOutput = toSVG(drawing, frame.width, frame.height)
                val f = File(outputFile)
                f.createNewFile()
                f.writeText(svgOutput)
            }
            return drawing
        }
        return null
    }

    fun ss(setup:SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructure = secondaryStructureBuilder.build()
    }

    fun theme(setup:ThemeBuilder.() -> Unit) {
        this.themeBuilder = ThemeBuilder()
        this.themeBuilder!!.setup()
    }

}

class ThemeBuilder {
    val colors = mutableListOf<ColorBuilder>()
    val details = mutableListOf<DetailsBuilder>()
    val lines = mutableListOf<LineBuilder>()

    fun build():Theme {
        val t = Theme()
        return t
    }

    fun details(setup:DetailsBuilder.() -> Unit) {
        val detailsBuilder = DetailsBuilder()
        detailsBuilder.setup()
        this.details.add(detailsBuilder)
    }

    fun color(setup:ColorBuilder.() -> Unit) {
        val colorBuilder = ColorBuilder()
        colorBuilder.setup()
        this.colors.add(colorBuilder)
    }

    fun line(setup:LineBuilder.() -> Unit) {
        val lineBuilder = LineBuilder()
        lineBuilder.setup()
        this.lines.add(lineBuilder)
    }
}

open class ThemeConfigurationBuilder {
    var location:String? = null
    var type:String? = null
}

class DetailsBuilder: ThemeConfigurationBuilder() {
    var value = "full"
}

class ColorBuilder: ThemeConfigurationBuilder() {
    var value = getHTMLColorString(Color.BLACK)
}

class LineBuilder:ThemeConfigurationBuilder() {
    var value = 2.0
}

fun ss(setup:SecondaryStructureBuilder.() -> Unit): SecondaryStructure? {
    val ssBuilder = SecondaryStructureBuilder()
    ssBuilder.setup()
    return ssBuilder.build()
}

fun booquet(setup:BooquetBuilder.() -> Unit) {
    val booquetBuilder = BooquetBuilder()
    booquetBuilder.setup()
    booquetBuilder.build()
}

fun rnartist(setup:RNArtistBuilder.() -> Unit): SecondaryStructureDrawing? {
    val rnartistBuilder = RNArtistBuilder()
    rnartistBuilder.setup()
    return rnartistBuilder.build()
}

private fun getSecondaryStructureType(type:String):List<SecondaryStructureType> {
    var types = mutableListOf<SecondaryStructureType>()
    when (type) {
        "A" -> types.add(SecondaryStructureType.AShape)
        "U" -> types.add(SecondaryStructureType.UShape)
        "G" -> types.add(SecondaryStructureType.GShape)
        "C" -> types.add(SecondaryStructureType.CShape)
        "X" -> types.add(SecondaryStructureType.XShape)
        "N" -> {
            types.add(SecondaryStructureType.AShape)
            types.add(SecondaryStructureType.GShape)
            types.add(SecondaryStructureType.UShape)
            types.add(SecondaryStructureType.CShape)
            types.add(SecondaryStructureType.XShape)
        }
        "R" -> {
            types.add(SecondaryStructureType.AShape)
            types.add(SecondaryStructureType.GShape)
        }
        "Y" -> {
            types.add(SecondaryStructureType.UShape)
            types.add(SecondaryStructureType.CShape)
        }
        "a" -> types.add(SecondaryStructureType.A)
        "u" -> types.add(SecondaryStructureType.U)
        "g" -> types.add(SecondaryStructureType.G)
        "c" -> types.add(SecondaryStructureType.C)
        "x" -> types.add(SecondaryStructureType.X)
        "n" -> {
            types.add(SecondaryStructureType.A)
            types.add(SecondaryStructureType.G)
            types.add(SecondaryStructureType.U)
            types.add(SecondaryStructureType.C)
            types.add(SecondaryStructureType.X)
        }
        "r" -> {
            types.add(SecondaryStructureType.A)
            types.add(SecondaryStructureType.G)
        }
        "y" -> {
            types.add(SecondaryStructureType.U)
            types.add(SecondaryStructureType.C)
        }
        "helix" -> types.add(SecondaryStructureType.Helix)
        "single_strand" -> types.add(SecondaryStructureType.SingleStrand)
        "junction" -> types.add(SecondaryStructureType.Junction)
        "secondary_interaction" -> types.add(SecondaryStructureType.SecondaryInteraction)
        "tertiary_interaction" -> types.add(SecondaryStructureType.TertiaryInteraction)
        "phosphodiester_bond" -> types.add(SecondaryStructureType.PhosphodiesterBond)
        "interaction_symbol" -> types.add(SecondaryStructureType.InteractionSymbol)
        "pknot" -> types.add(SecondaryStructureType.PKnot)
    }
    return types
}