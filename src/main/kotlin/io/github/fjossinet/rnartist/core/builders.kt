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
    private var secondaryStructures = mutableListOf<SecondaryStructure>()

    fun build():List<SecondaryStructure> {
        this.bracket_notation?.let { bn ->
            this.rna?.let { rna ->
                secondaryStructures.add(SecondaryStructure(rna, bracketNotation = bn))
                return secondaryStructures
            }
            val sequence = StringBuffer()
            sequence.append((1..bn.length).map { listOf("A", "U", "G", "C").random()}.joinToString(separator = ""))
            val ss = SecondaryStructure(RNA(seq = sequence.toString()), bracketNotation = bn)
            ss.randomizeSeq()
            secondaryStructures.add(ss)
            return secondaryStructures
        }
        return secondaryStructures
    }

    fun rna(setup:RNABuilder.() -> Unit) {
        val rnaBuilder = RNABuilder()
        rnaBuilder.setup()
        rna = rnaBuilder.build()
    }

    fun rfam(setup:RfamBuilder.() -> Unit) {
        val rfamBuilder = RfamBuilder()
        rfamBuilder.setup()
        secondaryStructures.addAll(rfamBuilder.build())
    }

    fun vienna(setup:ViennaBuilder.() -> Unit) {
        val viennaBuilder = ViennaBuilder()
        viennaBuilder.setup()
        secondaryStructures.addAll(viennaBuilder.build())
    }

    fun bpseq(setup:BPSeqBuilder.() -> Unit) {
        val bpSeqBuilder = BPSeqBuilder()
        bpSeqBuilder.setup()
        secondaryStructures.addAll(bpSeqBuilder.build())
    }

    fun ct(setup:CTBuilder.() -> Unit) {
        val ctBuilder = CTBuilder()
        ctBuilder.setup()
        secondaryStructures.addAll(ctBuilder.build())
    }

    fun pdb(setup:PDBBuilder.() -> Unit) {
        val pdbBuilder = PDBBuilder()
        pdbBuilder.setup()
        secondaryStructures.addAll(pdbBuilder.build())
    }

    fun stockholm(setup:StockholmBuilder.() -> Unit) {
        val stockholmBuilder = StockholmBuilder()
        stockholmBuilder.setup()
        secondaryStructures.addAll(stockholmBuilder.build())
    }

}

open abstract class FileBuilder {
    var file:String? = null

    abstract fun build():List<SecondaryStructure>
}

class PDBBuilder:FileBuilder() {

    var name:String? = null
    var id:String? = null

    override fun build(): List<SecondaryStructure> {
        var secondaryStructures = listOf<SecondaryStructure>()
        if (this.id != null) {
            val pdbFile = java.io.File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.absolutePath
        }
        if (this.file != null) {
            try {
                secondaryStructures = Rnaview().annotate(File(file))
            } catch (e:Exception) {
                println(e.message)
            }
            if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name.equals(this.name))
                        return arrayListOf<SecondaryStructure>(it)
                }
            }
            return secondaryStructures
        }
        return listOf<SecondaryStructure>()
    }
}

class ViennaBuilder:FileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            return arrayListOf<SecondaryStructure>(parseVienna(FileReader(this.file)))
        }
        return listOf<SecondaryStructure>()
    }
}

class BPSeqBuilder:FileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            return arrayListOf<SecondaryStructure>(parseBPSeq(FileReader(this.file)))
        }
        return listOf<SecondaryStructure>()
    }
}

class CTBuilder:FileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            return arrayListOf<SecondaryStructure>(parseCT(FileReader(this.file)))
        }
        return listOf<SecondaryStructure>()
    }
}

class StockholmBuilder:FileBuilder() {
    var name:String? = null

    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            var secondaryStructures = parseStockholm(FileReader(this.file), withConsensus2D = true)
            if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name.equals(this.name))
                        arrayListOf<SecondaryStructure>(it)
                }
            } else
                return secondaryStructures
        }
        return listOf<SecondaryStructure>()
    }
}

open abstract class PublicDatabaseBuilder {
    var id:String? = null
    var name:String? = null

    abstract fun build():List<SecondaryStructure>
}


class RfamBuilder:PublicDatabaseBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.id?.let { id ->
            val secondaryStructures = parseStockholm(Rfam().getEntry(id), withConsensus2D = true)
            this.name?.let {
                if ("consensus".equals(name))
                    return arrayListOf<SecondaryStructure>(secondaryStructures.first())
                else {
                    secondaryStructures.forEach {
                        if (name.equals(it.rna.name))
                            return arrayListOf<SecondaryStructure>(it)
                    }
                }
            }
            return secondaryStructures
        }
        return listOf<SecondaryStructure>()
    }
}


class BooquetBuilder {
    var file:String? = null
    var width = 600.0
    var height = 600.0
    var junction_diameter = 25.0
    var color = getHTMLColorString(Color.BLACK)
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var line = 2.0

    fun build() {
        this.file?.let { outputFile ->
            this.secondaryStructures.forEach { ss ->
                    val svgOutput = Booquet(ss,
                        this.width,
                        this.height,
                        junction_diameter = junction_diameter,
                        lineWidth = line,
                        color = if (color.startsWith("#")) getAWTColor(color) else getAWTColor(getColorCode(color)))
                    val f = File("${outputFile.split(".svg").first()}_${ss.rna.name.replace("/", "_")}.svg")
                    f.createNewFile()
                    f.writeText(svgOutput)
                }
            }
    }

    fun ss(setup:SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build())
    }

}

class RNArtistBuilder {
    var file:String? = null
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var theme:AdvancedTheme? = null
    var data:MutableMap<String, Double> = mutableMapOf()

    fun build(): List<SecondaryStructureDrawing> {
        val drawings = mutableListOf<SecondaryStructureDrawing>()
        this.secondaryStructures.forEach { ss ->
            val drawing = SecondaryStructureDrawing(ss, WorkingSession())
            this.theme?.let { theme ->
                drawing.applyAdvancedTheme(theme)
            }
            this.file?.let { outputFile ->
                //the frame will have the size of the drawing
                val drawingFrame = drawing.getFrame().bounds2D
                val frame = if (drawingFrame.width < 1024 || drawingFrame.height < 768)
                    Rectangle2D.Double(0.0, 0.0, 1024.0, 768.0)
                else
                    Rectangle2D.Double(0.0, 0.0, drawingFrame.width, drawingFrame.height)
                drawing.fitTo(frame)
                val svgOutput = toSVG(drawing, frame.width, frame.height)
                val f = File("${outputFile.split(".svg").first()}_${ss.rna.name.replace("/", "_")}.svg")
                f.createNewFile()
                f.writeText(svgOutput)
            }
            drawings.add(drawing)
        }
        return drawings
    }

    fun ss(setup:SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build())
    }

    fun theme(setup:ThemeBuilder.() -> Unit) {
        val themeBuilder = ThemeBuilder(data)
        themeBuilder.setup()
        this.theme = themeBuilder.build()
    }

    fun data(setup:DataBuilder.() -> Unit) {
        val dataBuilder = DataBuilder()
        dataBuilder.setup()
        data = dataBuilder.data
    }

}

class DataBuilder {
    var file:String? = null
        set(value) {
            field = value
            File(value).readLines().forEach {
                val tokens = it.split(" ")
                data[tokens[0]] = tokens[1].toDouble()
            }
        }
    var data = mutableMapOf<String, Double>()

    infix fun String.to(i:Double) {
        data[this] = i
    }

}


class ThemeBuilder(data:MutableMap<String, Double> = mutableMapOf()) {
    var details_lvl:Int? = null
    val colors = mutableListOf<ColorBuilder>()
    val details = mutableListOf<DetailsBuilder>()
    val lines = mutableListOf<LineBuilder>()
    val hides = mutableListOf<HideBuilder>()
    val data = data.toMutableMap()

    fun details(setup:DetailsBuilder.() -> Unit) {
        val detailsBuilder = DetailsBuilder(this.data)
        detailsBuilder.setup()
        this.details.add(detailsBuilder)
    }

    fun color(setup:ColorBuilder.() -> Unit) {
        val colorBuilder = ColorBuilder(this.data)
        colorBuilder.setup()
        this.colors.add(colorBuilder)
    }

    fun line(setup:LineBuilder.() -> Unit) {
        val lineBuilder = LineBuilder(this.data)
        lineBuilder.setup()
        this.lines.add(lineBuilder)
    }

    fun hide(setup:HideBuilder.() -> Unit) {
        val hideBuilder = HideBuilder(this.data)
        hideBuilder.setup()
        this.hides.add(hideBuilder)
    }

    fun build(): AdvancedTheme {
        val t = AdvancedTheme()
        this.details_lvl?.let {
            when (it) {
                1 -> {
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Helix},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SecondaryInteraction},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Junction},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SingleStrand},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.PhosphodiesterBond},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.AShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.A}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.UShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.U}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.GShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.G}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.CShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.C}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.XShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.X}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.InteractionSymbol},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t
                }
                2 -> {
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Helix},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SecondaryInteraction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Junction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SingleStrand},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.PhosphodiesterBond},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.AShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.A}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.UShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.U}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.GShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.G}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.CShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.C}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.XShape},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.X}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.InteractionSymbol},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t
                }
                3 -> {
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Helix},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SecondaryInteraction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Junction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SingleStrand},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.PhosphodiesterBond},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.AShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.A}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.UShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.U}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.GShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.G}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.CShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.C}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.XShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.X}, DrawingConfigurationParameter.fulldetails, "false")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.InteractionSymbol},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t
                }
                4 -> {
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Helix},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SecondaryInteraction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Junction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SingleStrand},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.PhosphodiesterBond},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.AShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.A}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.UShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.U}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.GShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.G}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.CShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.C}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.XShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.X}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.InteractionSymbol},
                        DrawingConfigurationParameter.fulldetails,
                        "false")
                    t
                }
                else -> {
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Helix},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SecondaryInteraction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.Junction},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.SingleStrand},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.PhosphodiesterBond},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.AShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.A}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.UShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.U}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.GShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.G}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.CShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.C}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.XShape},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.X}, DrawingConfigurationParameter.fulldetails, "true")
                    t.setConfigurationFor({el -> el.type == SecondaryStructureType.InteractionSymbol},
                        DrawingConfigurationParameter.fulldetails,
                        "true")
                    t
                }
            }
        }
        this.hides.forEach { hideBuilder ->
            if (hideBuilder.data.size != data.size) { //meaning that they have been filtered
                hideBuilder.data.forEach { position, value ->
                    when (hideBuilder.type) {
                        "A" , "a" -> {
                            var types = getSecondaryStructureType("A")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("a")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "U" , "u" -> {
                            var types = getSecondaryStructureType("U")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("u")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "G" , "g" -> {
                            var types = getSecondaryStructureType("G")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("g")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "C" , "c" -> {
                            var types = getSecondaryStructureType("C")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("c")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "X" , "x" -> {
                            var types = getSecondaryStructureType("X")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("x")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "R" , "r" -> {
                            var types = getSecondaryStructureType("R")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("r")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "Y" , "y" -> {
                            var types = getSecondaryStructureType("Y")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("y")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt()}
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        else -> {
                            var types = getSecondaryStructureType("N")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt() }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("n")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && e.location.start == position.toInt() }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                    }
                }
            }
            else if (hideBuilder.location != null) {
                hideBuilder.location?.let {
                    val location = Location(it)
                    when (hideBuilder.type) {
                        "A" , "a" -> {
                            var types = getSecondaryStructureType("A")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("a")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "U" , "u" -> {
                            var types = getSecondaryStructureType("U")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("u")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "G" , "g" -> {
                            var types = getSecondaryStructureType("G")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("g")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "C" , "c" -> {
                            var types = getSecondaryStructureType("C")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("c")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "X" , "x" -> {
                            var types = getSecondaryStructureType("X")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("x")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "R" , "r" -> {
                            var types = getSecondaryStructureType("R")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("r")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        "Y" , "y" -> {
                            var types = getSecondaryStructureType("Y")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("y")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                        else -> {
                            var types = getSecondaryStructureType("N")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                            types = getSecondaryStructureType("n")
                            types.forEach { type ->
                                val selection =
                                    { e: DrawingElement -> e.type == type && location.contains(e.location.start) }
                                t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                            }
                        }
                    }

                }
            } else {
                when (hideBuilder.type) {
                    "A" , "a" -> {
                        var types = getSecondaryStructureType("A")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("a")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "U" , "u" -> {
                        var types = getSecondaryStructureType("U")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("u")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "G" , "g" -> {
                        var types = getSecondaryStructureType("G")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("g")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "C" , "c" -> {
                        var types = getSecondaryStructureType("C")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("c")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "X" , "x" -> {
                        var types = getSecondaryStructureType("X")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("x")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "R" , "r" -> {
                        var types = getSecondaryStructureType("R")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("r")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    "Y" , "y" -> {
                        var types = getSecondaryStructureType("Y")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("y")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                    else -> {
                        var types = getSecondaryStructureType("N")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                        types = getSecondaryStructureType("n")
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, "none")
                        }
                    }
                }
            }
        }
        this.colors.forEach { colorBuilder ->
            if (colorBuilder.data.size != data.size || colorBuilder.to != null) { //meaning that they have been filtered or that we want a gradient (even with non filtered data)
                colorBuilder.data.forEach { position, value ->
                    var colorCode: String = getHTMLColorString(Color.BLACK)
                    colorBuilder.value?.let { from ->
                        val fromColor = getAWTColor(from)
                        colorBuilder.to?.let { to ->
                            val toColor = getAWTColor(to)
                            val min = colorBuilder.data.values.min()
                            val max = colorBuilder.data.values.max()
                            val p = (value - min!!) / (max!! - min!!)
                            val r = (fromColor.red * (1 - p) + toColor.red * p).toInt()
                            val g = (fromColor.green * (1 - p) + toColor.green * p).toInt()
                            val b = (fromColor.blue * (1 - p) + toColor.blue * p).toInt()
                            colorCode = getHTMLColorString(Color(r, g, b))
                        }
                    }
                    colorBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.location.start == position.toInt() }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.color, colorCode)
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.location.start == position.toInt() }, DrawingConfigurationParameter.color, colorCode)
                    }
                }
            }
            else if (colorBuilder.location != null) {
                colorBuilder.location?.let {
                    val location = Location(it)
                    colorBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.inside(location)}
                            t.setConfigurationFor(selection,
                                DrawingConfigurationParameter.color,
                                colorBuilder.value.toString())
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.inside(location) }, DrawingConfigurationParameter.color, colorBuilder.value.toString())
                    }
                }
            }
            else if (colorBuilder.type != null) {
                colorBuilder.getSecondaryStructureTypes()?.let { types ->
                    types.forEach { type ->
                        val selection = { e: DrawingElement -> e.type == type }
                        t.setConfigurationFor(selection,
                            DrawingConfigurationParameter.color,
                            colorBuilder.value.toString())
                    }
                }
            } else
                t.setConfigurationFor({ e: DrawingElement -> true }, DrawingConfigurationParameter.color, colorBuilder.value.toString())
        }
        this.details.forEach { detailsBuilder ->
            if (detailsBuilder.data.isNotEmpty()) {
                detailsBuilder.data.forEach { position, value ->
                    detailsBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.location.start == position.toInt() }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.fulldetails, detailsBuilder.value.equals("full").toString())
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.location.start == position.toInt() }, DrawingConfigurationParameter.fulldetails, detailsBuilder.value.equals("full").toString())
                    }
                }
            }
            else if (detailsBuilder.location != null) {
                detailsBuilder.location?.let {
                    val location = Location(it)
                    detailsBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.inside(location) }
                            t.setConfigurationFor(selection,
                                DrawingConfigurationParameter.fulldetails,
                                detailsBuilder.value.equals("full").toString())
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.inside(location) }, DrawingConfigurationParameter.fulldetails, detailsBuilder.value.equals("full").toString())
                    }
                }
            }
            else if (detailsBuilder.type != null) {
                detailsBuilder.getSecondaryStructureTypes()?.let { types ->
                    types.forEach { type ->
                        val selection = { e: DrawingElement -> e.type == type }
                        t.setConfigurationFor(selection,
                            DrawingConfigurationParameter.fulldetails,
                            detailsBuilder.value.equals("full").toString())
                    }
                }
            } else
                t.setConfigurationFor({ e: DrawingElement -> true }, DrawingConfigurationParameter.fulldetails,  detailsBuilder.value.equals("full").toString())
        }
        this.lines.forEach { lineBuilder ->
            if (lineBuilder.data.size != data.size) { //meaning that they have been filtered
                lineBuilder.data.forEach { position, value ->
                    lineBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.location.start == position.toInt() }
                            t.setConfigurationFor(selection, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.location.start == position.toInt() }, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
                    }
                }
            }
            else if (lineBuilder.location != null) {
                lineBuilder.location?.let {
                    val location = Location(it)
                    lineBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            val selection =
                                { e: DrawingElement -> e.type == type && e.inside(location) }
                            t.setConfigurationFor(selection,
                                DrawingConfigurationParameter.linewidth,
                                lineBuilder.value.toString())
                        }
                    } ?: run {
                        t.setConfigurationFor({ e: DrawingElement -> e.inside(location) }, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
                    }
                }
            }
            else if (lineBuilder.type != null) {
                lineBuilder.getSecondaryStructureTypes()?.let { types ->
                    types.forEach { type ->
                        val selection = { e: DrawingElement -> e.type == type }
                        t.setConfigurationFor(selection,
                            DrawingConfigurationParameter.linewidth,
                            lineBuilder.value.toString())
                    }
                }
            } else
                t.setConfigurationFor({ e: DrawingElement -> true }, DrawingConfigurationParameter.linewidth, lineBuilder.value.toString())
        }
        return t
    }

}

open class ThemeConfigurationBuilder(data:MutableMap<String, Double>) {
    var location:String? = null
    var type:String? = null
    val data = data.toMutableMap()

    infix fun MutableMap<String,Double>.gt(min:Double) {
        val excluded = this.filter { it.value <= min }
        excluded.forEach {
            remove(it.key)
        }
    }

    infix fun MutableMap<String,Double>.lt(max:Double) {
        val excluded = this.filter { it.value >= max }
        excluded.forEach {
            remove(it.key)
        }
    }

    infix fun MutableMap<String,Double>.between(range:ClosedFloatingPointRange<Double>) {
        val excluded = this.filter { it.value !in range }
        excluded.forEach {
            remove(it.key)
        }
    }

    fun getSecondaryStructureTypes() = this.type?.split(" ")?.flatMap { getSecondaryStructureType(it) }
}

class DetailsBuilder(data:MutableMap<String, Double>): ThemeConfigurationBuilder(data) {
    var value = "full"
}

class ColorBuilder(data:MutableMap<String, Double>): ThemeConfigurationBuilder(data) {
    var value:String? = null
        set(value) {
            field = value?.let {
                if (!value.startsWith("#")) getColorCode(value) else value
            }
        }
    var to:String? = null
        set(value) {
            field = value?.let {
                if (!value.startsWith("#")) getColorCode(value) else value
            }
        }

}

class LineBuilder(data:MutableMap<String, Double>):ThemeConfigurationBuilder(data) {
    var value = 2.0
}

class HideBuilder(data:MutableMap<String, Double>):ThemeConfigurationBuilder(data) {
}

fun ss(setup:SecondaryStructureBuilder.() -> Unit): List<SecondaryStructure> {
    val ssBuilder = SecondaryStructureBuilder()
    ssBuilder.setup()
    return ssBuilder.build()
}

fun booquet(setup:BooquetBuilder.() -> Unit) {
    val booquetBuilder = BooquetBuilder()
    booquetBuilder.setup()
    booquetBuilder.build()
}

fun rnartist(setup:RNArtistBuilder.() -> Unit): List<SecondaryStructureDrawing> {
    val rnartistBuilder = RNArtistBuilder()
    rnartistBuilder.setup()
    return rnartistBuilder.build()
}

fun theme(setup:ThemeBuilder.() -> Unit): AdvancedTheme {
    val themeBuilder = ThemeBuilder()
    themeBuilder.setup()
    return themeBuilder.build()
}

private fun getColorCode(name:String):String {
    return when (name) {
        "white" -> "#FFFFFF"
        "ivory" -> "#FFFFF0"
        "lightyellow" -> "#FFFFE0"
        "yellow" -> "#FFFF00"
        "snow" -> "#FFFAFA"
        "floralwhite" -> "#FFFAF0"
        "lemonchiffon" -> "#FFFACD"
        "cornsilk" -> "#FFF8DC"
        "seashell" -> "#FFF5EE"
        "lavenderblush" -> "#FFF0F5"
        "papayawhip" -> "#FFEFD5"
        "blanchedalmond" -> "#FFEBCD"
        "mistyrose" -> "#FFE4E1"
        "bisque" -> "#FFE4C4"
        "moccasin" -> "#FFE4B5"
        "navajowhite" -> "#FFDEAD"
        "peachpuff" -> "#FFDAB9"
        "gold" -> "#FFD700"
        "pink" -> "#FFC0CB"
        "lightpink" -> "#FFB6C1"
        "orange" -> "#FFA500"
        "lightsalmon" -> "#FFA07A"
        "darkorange" -> "#FF8C00"
        "coral" -> "#FF7F50"
        "hotpink" -> "#FF69B4"
        "tomato" -> "#FF6347"
        "orangered" -> "#FF4500"
        "deeppink" -> "#FF1493"
        "fuchsia" -> "#FF00FF"
        "magenta" -> "#FF00FF"
        "red" -> "#FF0000"
        "oldlace" -> "#FDF5E6"
        "lightgoldenrodyellow" -> "#FAFAD2"
        "linen" -> "#FAF0E6"
        "antiquewhite" -> "#FAEBD7"
        "salmon" -> "#FA8072"
        "ghostwhite" -> "#F8F8FF"
        "mintcream" -> "#F5FFFA"
        "whitesmoke" -> "#F5F5F5"
        "beige" -> "#F5F5DC"
        "wheat" -> "#F5DEB3"
        "sandybrown" -> "#F4A460"
        "azure" -> "#F0FFFF"
        "honeydew" -> "#F0FFF0"
        "aliceblue" -> "#F0F8FF"
        "khaki" -> "#F0E68C"
        "lightcoral" -> "#F08080"
        "palegoldenrod" -> "#EEE8AA"
        "violet" -> "#EE82EE"
        "darksalmon" -> "#E9967A"
        "lavender" -> "#E6E6FA"
        "lightcyan" -> "#E0FFFF"
        "burlywood" -> "#DEB887"
        "plum" -> "#DDA0DD"
        "gainsboro" -> "#DCDCDC"
        "crimson" -> "#DC143C"
        "palevioletred" -> "#DB7093"
        "goldenrod" -> "#DAA520"
        "orchid" -> "#DA70D6"
        "thistle" -> "#D8BFD8"
        "lightgrey" -> "#D3D3D3"
        "tan" -> "#D2B48C"
        "chocolate" -> "#D2691E"
        "peru" -> "#CD853F"
        "indianred" -> "#CD5C5C"
        "mediumvioletred" -> "#C71585"
        "silver" -> "#C0C0C0"
        "darkkhaki" -> "#BDB76B"
        "rosybrown" -> "#BC8F8F"
        "mediumorchid" -> "#BA55D3"
        "darkgoldenrod" -> "#B8860B"
        "firebrick" -> "#B22222"
        "powderblue" -> "#B0E0E6"
        "lightsteelblue" -> "#B0C4DE"
        "paleturquoise" -> "#AFEEEE"
        "greenyellow" -> "#ADFF2F"
        "lightblue" -> "#ADD8E6"
        "darkgray" -> "#A9A9A9"
        "brown" -> "#A52A2A"
        "sienna" -> "#A0522D"
        "yellowgreen" -> "#9ACD32"
        "darkorchid" -> "#9932CC"
        "palegreen" -> "#98FB98"
        "darkviolet" -> "#9400D3"
        "mediumpurple" -> "#9370DB"
        "lightgreen" -> "#90EE90"
        "darkseagreen" -> "#8FBC8F"
        "saddlebrown" -> "#8B4513"
        "darkmagenta" -> "#8B008B"
        "darkred" -> "#8B0000"
        "blueviolet" -> "#8A2BE2"
        "lightskyblue" -> "#87CEFA"
        "skyblue" -> "#87CEEB"
        "gray" -> "#808080"
        "olive" -> "#808000"
        "purple" -> "#800080"
        "maroon" -> "#800000"
        "aquamarine" -> "#7FFFD4"
        "chartreuse" -> "#7FFF00"
        "lawngreen" -> "#7CFC00"
        "mediumslateblue" -> "#7B68EE"
        "lightslategray" -> "#778899"
        "slategray" -> "#708090"
        "olivedrab" -> "#6B8E23"
        "slateblue" -> "#6A5ACD"
        "dimgray" -> "#696969"
        "mediumaquamarine" -> "#66CDAA"
        "cornflowerblue" -> "#6495ED"
        "cadetblue" -> "#5F9EA0"
        "darkolivegreen" -> "#556B2F"
        "indigo" -> "#4B0082"
        "mediumturquoise" -> "#48D1CC"
        "darkslateblue" -> "#483D8B"
        "steelblue" -> "#4682B4"
        "royalblue" -> "#4169E1"
        "turquoise" -> "#40E0D0"
        "mediumseagreen" -> "#3CB371"
        "limegreen" -> "#32CD32"
        "darkslategray" -> "#2F4F4F"
        "seagreen" -> "#2E8B57"
        "forestgreen" -> "#228B22"
        "lightseagreen" -> "#20B2AA"
        "dodgerblue" -> "#1E90FF"
        "midnightblue" -> "#191970"
        "aqua" -> "#00FFFF"
        "cyan" -> "#00FFFF"
        "springgreen" -> "#00FF7F"
        "lime" -> "#00FF00"
        "mediumspringgreen" -> "#00FA9A"
        "darkturquoise" -> "#00CED1"
        "deepskyblue" -> "#00BFFF"
        "darkcyan" -> "#008B8B"
        "teal" -> "#008080"
        "green" -> "#008000"
        "darkgreen" -> "#006400"
        "blue" -> "#0000FF"
        "mediumblue" -> "#0000CD"
        "darkblue" -> "#00008B"
        "navy" -> "#000080"
        "black" -> "#000000"
        else -> "#000000"
    }
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