package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.*
import io.github.fjossinet.rnartist.core.model.*
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileReader
import java.lang.Exception

class RNABuilder {
    var name: String = "A"
    var seq: String? = null
    var length: Int? = null

    fun build(): RNA? {
        this.seq?.let {
            return RNA(name, it)
        }
        this.length?.let {
            val sequence = StringBuffer()
            sequence.append((1..it).map { listOf("A", "U", "G", "C").random() }.joinToString(separator = ""))
            return RNA(name, sequence.toString())
        }
        return null
    }
}

class PartsBuilder {

    private val helixBuilders = mutableListOf<HelixBuilder>()
    private val interactionBuilders = mutableListOf<InteractionBuilder>() //tertiary interactions
    private var rnaBuilder: RNABuilder? = null
    var source: String? = null

    fun rna(setup: RNABuilder.() -> Unit) {
        this.rnaBuilder = RNABuilder()
        this.rnaBuilder?.setup()
    }

    fun helix(setup: HelixBuilder.() -> Unit) {
        val helixBuilder = HelixBuilder()
        helixBuilder.setup()
        helixBuilders.add(helixBuilder)
    }

    fun interaction(setup: InteractionBuilder.() -> Unit) {
        val interactionBuilder = InteractionBuilder()
        interactionBuilder.setup()
        interactionBuilders.add(interactionBuilder)
    }

    fun build(): SecondaryStructure? {
        this.rnaBuilder?.let { rnaBuilder ->
            var helices = mutableListOf<Helix>()
            helixBuilders.forEach {
                helices.add(it.build())
            }
            helices.sortBy { it.start }
            rnaBuilder.build()?.let { rna ->
                val ss = SecondaryStructure(rna, helices = helices)
                if (rnaBuilder.seq == null) {
                    //this means that the sequence is a random one, but then nnot fitting the structural constraints. So we generate a new one fitting the constraints
                    ss.randomizeSeq()
                }
                source?.let {
                    ss.source = getSource(it)
                }
                return ss
            }
        }
        return null
    }

}

class BracketNotationBuilder {
    var value: String = "(((...)))"
    var name: String = "A"
    var seq: String? = null

    fun build(): SecondaryStructure? {
        this.seq?.let {
            return SecondaryStructure(RNA(name, it), bracketNotation = value.trim(), source = BracketNotation())
        }
        val sequence = StringBuffer()
        sequence.append((1..value.trim().length).map { listOf("A", "U", "G", "C").random() }
            .joinToString(separator = ""))
        val ss = SecondaryStructure(
            RNA(name = name, seq = sequence.toString()),
            bracketNotation = value.trim(),
            source = BracketNotation()
        )
        ss.randomizeSeq()
        return ss
    }
}

class InteractionBuilder() {

}

class HelixBuilder() {

    val locationBuilder = LocationBuilder()
    var name: String? = null
    private val interactionBuilders = mutableListOf<InteractionBuilder>() //non canonical secondary interactions

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    fun interaction(setup: InteractionBuilder.() -> Unit) {
        val interaction = InteractionBuilder()
        interaction.setup()
        interactionBuilders.add(interaction)
    }

    fun build(): Helix {
        val h = Helix(name ?: "MyHelix")
        val location = locationBuilder.build()
        for (i in location.start..location.start + location.length / 2 - 1) {
            val l = Location(Location(i), Location(location.end - (i - location.start)))
            h.secondaryInteractions.add(BasePair(l, Edge.WC, Edge.WC, Orientation.cis))
        }
        return h
    }

}

class SecondaryStructureBuilder {

    private var secondaryStructures = mutableListOf<SecondaryStructure>()
    private var tertiaryStructures = mutableListOf<TertiaryStructure>()

    fun build(): Pair<List<SecondaryStructure>, List<TertiaryStructure>> {
        return Pair(secondaryStructures, tertiaryStructures)
    }

    fun parts(setup: PartsBuilder.() -> Unit) {
        val partsBuilder = PartsBuilder()
        partsBuilder.setup()
        partsBuilder.build()?.let {
            secondaryStructures.add(it)
        }
    }

    fun bn(setup: BracketNotationBuilder.() -> Unit) {
        val bnBuilder = BracketNotationBuilder()
        bnBuilder.setup()
        bnBuilder.build()?.let {
            secondaryStructures.add(it)
        }
    }

    fun rfam(setup: RfamBuilder.() -> Unit) {
        val rfamBuilder = RfamBuilder()
        rfamBuilder.setup()
        secondaryStructures.addAll(rfamBuilder.build())
    }

    fun vienna(setup: ViennaBuilder.() -> Unit) {
        val viennaBuilder = ViennaBuilder()
        viennaBuilder.setup()
        secondaryStructures.addAll(viennaBuilder.build())
    }

    fun bpseq(setup: BPSeqBuilder.() -> Unit) {
        val bpSeqBuilder = BPSeqBuilder()
        bpSeqBuilder.setup()
        secondaryStructures.addAll(bpSeqBuilder.build())
    }

    fun ct(setup: CTBuilder.() -> Unit) {
        val ctBuilder = CTBuilder()
        ctBuilder.setup()
        secondaryStructures.addAll(ctBuilder.build())
    }

    fun pdb(setup: PDBBuilder.() -> Unit) {
        val pdbBuilder = PDBBuilder()
        pdbBuilder.setup()
        val result = pdbBuilder.build()
        secondaryStructures.addAll(result.map {it.second})
        tertiaryStructures.addAll(result.map {it.first})
    }

    fun stockholm(setup: StockholmBuilder.() -> Unit) {
        val stockholmBuilder = StockholmBuilder()
        stockholmBuilder.setup()
        secondaryStructures.addAll(stockholmBuilder.build())
    }

    fun rnacentral(setup: RNACentralBuilder.() -> Unit) {
        val rnaCentralBuilder = RNACentralBuilder()
        rnaCentralBuilder.setup()
        secondaryStructures.addAll(rnaCentralBuilder.build())
    }

}

open abstract class OutputFileBuilder {
    var path: String? = null

    var width: Double = 800.0
    var height: Double = 800.0
    var locationBuilder: LocationBuilder = LocationBuilder()

    abstract fun build(drawing: SecondaryStructureDrawing)

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }
}

class PNGBuilder : OutputFileBuilder() {

    override fun build(drawing: SecondaryStructureDrawing) {
        path?.let { path ->
            if (!locationBuilder.isEmpty()) {
                drawing.getFrame(locationBuilder.build())?.let { selectionFrame ->
                    drawing.asPNG(
                        frame = Rectangle2D.Double(0.0, 0.0, width, height),
                        selectionFrame = selectionFrame,
                        outputFile = File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.png")
                    )
                }
            } else {
                drawing.asPNG(
                    frame = Rectangle2D.Double(0.0, 0.0, width, height),
                    outputFile = File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.png")
                )
            }
        }
    }

}

class SVGBuilder : OutputFileBuilder() {

    override fun build(drawing: SecondaryStructureDrawing) {
        path?.let { path ->
            if (!locationBuilder.isEmpty()) {
                drawing.getFrame(locationBuilder.build())?.let { selectionFrame ->
                    drawing.asSVG(
                        frame = Rectangle2D.Double(0.0, 0.0, width, height),
                        selectionFrame = selectionFrame,
                        outputFile = File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.svg")
                    )
                }
            } else {
                drawing.asSVG(
                    frame = Rectangle2D.Double(0.0, 0.0, width, height),
                    outputFile = File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.svg")
                )
            }
        }
    }

}

class ChimeraBuilder {

    var path: String? = null

    fun build(drawing: SecondaryStructureDrawing, tertiaryStructures: List<TertiaryStructure>) {
        path?.let { path ->
            drawing.asChimeraScript(File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.cxc"))
        }
    }

}

open abstract class InputFileBuilder {
    var file: String? = null

    abstract fun build(): List<SecondaryStructure>
}

class PDBBuilder {

    var file: String? = null
    var name: String? = null
    var id: String? = null

    fun build(): List<Pair<TertiaryStructure, SecondaryStructure>> {
        var structures = mutableListOf<Pair<TertiaryStructure, SecondaryStructure>>()
        if (this.id != null) {
            val pdbFile = File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.absolutePath
        }
        if (this.file != null) {
            try {
                structures.addAll(Annotate3D().annotate(File(file)))
                structures.forEach {
                    it.first.source = if (this.id != null) PDBSource(this.id!!) else FileSource(this.file!!)
                    it.second.source = if (this.id != null) PDBSource(this.id!!) else FileSource(this.file!!)

                }
            } catch (e: Exception) {
                println(e.message)
            }
            if (this.name != null) {
                structures.forEach {
                    if (it.second.rna.name.equals(this.name))
                        return arrayListOf(it)
                }
            }
            return structures
        }
        return listOf()
    }
}

class ViennaBuilder : InputFileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            val ss = parseVienna(FileReader(it))
            ss.source = FileSource(it)
            return arrayListOf(ss)
        }
        return listOf()
    }
}

class BPSeqBuilder : InputFileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            val ss = parseBPSeq(FileReader(it))
            ss.source = FileSource(it)
            return arrayListOf(ss)
        }
        return listOf<SecondaryStructure>()
    }
}

class CTBuilder : InputFileBuilder() {
    override fun build(): List<SecondaryStructure> {
        this.file?.let {
            val ss = parseCT(FileReader(it))
            ss.source = FileSource(it)
            return arrayListOf(ss)

        }
        return listOf()
    }
}

class StockholmBuilder : InputFileBuilder() {
    var name: String? = null
    val use = Use()
    private var useAlignmentNumbering = false

    inner class Use {

        infix fun alignment(ns: numbering) {
            useAlignmentNumbering = true
        }

        infix fun species(ns: numbering) {
            useAlignmentNumbering = false
        }

    }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            var secondaryStructures = parseStockholm(FileReader(this.file), withConsensus2D = true)
            secondaryStructures.forEach {
                it.source = FileSource(file)
                it.rna.useAlignmentNumberingSystem = useAlignmentNumbering
            }
            if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name.equals(this.name))
                        return arrayListOf(it)
                }
            } else
                return secondaryStructures
        }
        return listOf()
    }
}

abstract class PublicDatabaseBuilder {
    var id: String? = null
    var name: String? = null

    abstract fun build(): List<SecondaryStructure>
}

object numbering


class RfamBuilder : PublicDatabaseBuilder() {

    val use = Use()
    private var useAlignmentNumbering = false

    inner class Use {

        infix fun alignment(ns: numbering) {
            useAlignmentNumbering = true
        }

        infix fun species(ns: numbering) {
            useAlignmentNumbering = false
        }

    }

    override fun build(): List<SecondaryStructure> {
        this.id?.let { id ->
            val rfam = Rfam()
            val secondaryStructures = parseStockholm(rfam.getEntry(id), withConsensus2D = true)
            var ids = secondaryStructures.map { it.name.split("/").first() }
            if (rfam.nameAsAccessionNumbers) {
                secondaryStructures.forEach { ss ->
                    ss.source = RfamSource(id)
                    ss.rna.useAlignmentNumberingSystem = useAlignmentNumbering
                }
            } else {
                val ncbi = NCBI()
                val titles = ncbi.getSummaryTitle(*ids.toTypedArray())
                secondaryStructures.forEach { ss ->
                    ss.source = RfamSource(id)
                    ss.name = titles[ss.name.split("/").first()] ?: ss.name
                    ss.rna.useAlignmentNumberingSystem = useAlignmentNumbering
                }
            }
            this.name?.let {
                if ("consensus".equals(name))
                    return arrayListOf(secondaryStructures.first())
                else {
                    secondaryStructures.forEach {
                        if (name.equals(it.name))
                            return arrayListOf(it)
                    }
                }
            }
            return secondaryStructures
        }
        return listOf()
    }
}

class RNACentralBuilder : PublicDatabaseBuilder() {

    override fun build(): List<SecondaryStructure> {
        this.id?.let { id ->
            val secondaryStructure = RNACentral().fetch(id)
            secondaryStructure?.let {
                it.source = RnaCentralSource(id)
                return listOf(secondaryStructure)
            }
        }
        return listOf()
    }
}

class OpenScadBuilder {
    var output: String? = null
    var annotatedStructures = mutableListOf<Pair<TertiaryStructure, SecondaryStructure>>()

    fun build() {
        this.output?.let { outputFile ->
            val f = File("${outputFile}")
            f.createNewFile()
            this.annotatedStructures.forEach { annotatedStructure ->
                var i = 0
                annotatedStructure.second.helices.forEach { helix ->
                    i++
                    val firstbp = helix.secondaryInteractions.first()
                    val lastbp = helix.secondaryInteractions.last()
                    f.appendText(
                        """
translate([${i * 40},0,0])
    cylinder(h = ${helix.length * 10}, r1 = 20, r2 = 20, center = true);
"""
                    )
                }
            }
        }
    }

    fun input(setup: OpenscadInputBuilder.() -> Unit) {
        val openscadInputBuilder = OpenscadInputBuilder()
        openscadInputBuilder.setup()
        annotatedStructures.addAll(openscadInputBuilder.build())
    }

}

class OpenscadInputBuilder {
    var name: String? = null
    var id: String? = null
    var file: String? = null

    fun build(): List<Pair<TertiaryStructure, SecondaryStructure>> {
        var annotatedStructures = mutableListOf<Pair<TertiaryStructure, SecondaryStructure>>()
        if (this.id != null) {
            val pdbFile = File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.absolutePath
        }
        if (this.file != null) {
            try {
                annotatedStructures.addAll(Annotate3D().annotate(File(file)))
            } catch (e: Exception) {
                println(e.message)
            }
            if (this.name != null) {
                annotatedStructures.forEach {
                    if (it.first.rna.name.equals(this.name))
                        return arrayListOf(it)
                }
            }
            return annotatedStructures
        }
        return listOf()
    }
}

class BooquetBuilder {
    var file: String? = null
    var width = 600.0
    var height = 600.0
    var junction_diameter = 25.0
    var color = getHTMLColorString(Color.BLACK)
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var line = 2.0

    fun build() {
        this.file?.let { outputFile ->
            this.secondaryStructures.forEach { ss ->
                val svgOutput = Booquet(
                    ss,
                    this.width,
                    this.height,
                    junction_diameter = junction_diameter,
                    lineWidth = line,
                    color = if (color.startsWith("#")) getAWTColor(color) else getAWTColor(getColorCode(color))
                )
                val f = File("${outputFile.split(".svg").first()}_${ss.rna.name.replace("/", "_")}.svg")
                f.createNewFile()
                f.writeText(svgOutput)
            }
        }
    }

    fun ss(setup: SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build().first)
    }

}

class RNArtistBuilder {
    private var svgOutputBuilder: SVGBuilder? = null
    private var pngOutputBuilder: PNGBuilder? = null
    private var chimeraOutputBuilder: ChimeraBuilder? = null
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var tertiaryStructures = mutableListOf<TertiaryStructure>()
    var theme: Theme? = null
    var data: MutableMap<String, Double> = mutableMapOf()
    private var layout: Layout? = null

    fun build(): List<SecondaryStructureDrawing> {
        val drawings = mutableListOf<SecondaryStructureDrawing>()
        this.secondaryStructures.forEach { ss ->
            val drawing = SecondaryStructureDrawing(ss, WorkingSession())
            this.theme?.let { theme ->
                drawing.applyTheme(theme)
            }
            this.layout?.let { layout ->
                drawing.applyLayout(layout)
            }
            this.pngOutputBuilder?.build(drawing)
            this.svgOutputBuilder?.build(drawing)
            this.chimeraOutputBuilder?.build(drawing, tertiaryStructures)
            drawings.add(drawing)
        }
        return drawings
    }

    fun ss(setup: SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build().first)
        tertiaryStructures.addAll(secondaryStructureBuilder.build().second)
    }

    fun theme(setup: ThemeBuilder.() -> Unit) {
        val themeBuilder = ThemeBuilder(data)
        themeBuilder.setup()
        this.theme = themeBuilder.build()
    }

    fun layout(setup: LayoutBuilder.() -> Unit) {
        val layoutBuilder = LayoutBuilder()
        layoutBuilder.setup()
        this.layout = layoutBuilder.build()
    }

    fun data(setup: DataBuilder.() -> Unit) {
        val dataBuilder = DataBuilder()
        dataBuilder.setup()
        data = dataBuilder.data
    }

    fun svg(setup: SVGBuilder.() -> Unit) {
        this.svgOutputBuilder = SVGBuilder()
        this.svgOutputBuilder?.setup()
    }

    fun png(setup: PNGBuilder.() -> Unit) {
        this.pngOutputBuilder = PNGBuilder()
        this.pngOutputBuilder?.setup()
    }

    fun chimera(setup: ChimeraBuilder.() -> Unit) {
        this.chimeraOutputBuilder = ChimeraBuilder()
        this.chimeraOutputBuilder?.setup()
    }

}

class DataBuilder {
    var file: String? = null
        set(value) {
            field = value
            File(value).readLines().forEach {
                val tokens = it.split(" ")
                data[tokens[0]] = tokens[1].toDouble()
            }
        }
    var data = mutableMapOf<String, Double>()

    infix fun String.to(i: Double) {
        data[this] = i
    }

}

class LayoutBuilder {
    private val junctionLayoutBuilders = mutableListOf<JunctionLayoutBuilder>()

    fun junction(setup: JunctionLayoutBuilder.() -> Unit) {
        val junctionLayoutBuilder = JunctionLayoutBuilder()
        junctionLayoutBuilder.setup()
        this.junctionLayoutBuilders.add(junctionLayoutBuilder)
    }

    fun build(): Layout? {
        val layout = Layout()
        junctionLayoutBuilders.forEach { junctionLayoutBuilder ->
            if (!junctionLayoutBuilder.locationBuilder.isEmpty()) {
                val l = junctionLayoutBuilder.locationBuilder.build()
                junctionLayoutBuilder.radius?.let { radius ->
                    val selection =
                        { e: DrawingElement ->
                            Pair(
                                e is JunctionDrawing && e.inside(l) && l.blocks.size == e.location.blocks.size,
                                null
                            )
                        }
                    layout.setConfigurationFor(selection, LayoutParameter.radius, radius.toString())
                }
                junctionLayoutBuilder.out_ids?.let { out_ids ->

                    val selection =
                        { e: DrawingElement ->
                            val new_out_ids = StringBuilder()
                            (e as? JunctionDrawing)?.let { junctionDrawing ->
                                if (e.inside(l) && l.blocks.size == e.location.blocks.size) {
                                    new_out_ids.append(out_ids)
                                } else if (e.inside(l)) {
                                    var i = 0
//                                if needed, to debug this code section, use Rfam entry RF00011 and RNA X69982.1/45-449. It has a 6-way junction in the consensus that becomes 5-way for this RNA.
//                                if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88)
//                                    println("Search to apply out_ids ${out_ids} to junction ${junctionDrawing.location}")
                                    while (i < l.blocks.size - 1) {
                                        val helicalBpInJunction =
                                            Location(Location(l.blocks[i].end), Location(l.blocks[i + 1].start))
//                                    if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                        println("############## bp in junction ${helicalBpInJunction}")
//                                    }
                                        junctionDrawing.outHelices.forEach { helixDrawing ->
                                            var helixLocation: Location? = null
                                            junctionDrawing.ssDrawing.secondaryStructure.rna.alignment_numbering_system?.let { ns ->
                                                helixLocation = Location(
                                                    Location(
                                                        ns[helixDrawing.secondaryInteractions.first().location.start - 1]!!,
                                                        ns[helixDrawing.secondaryInteractions.last().location.start + 1]!!
                                                    ),
                                                    Location(
                                                        ns[helixDrawing.secondaryInteractions.last().location.end - 1]!!,
                                                        ns[helixDrawing.secondaryInteractions.first().location.end + 1]!!
                                                    )
                                                )
                                            } ?: run {
                                                helixLocation = Location(
                                                    Location(
                                                        helixDrawing.secondaryInteractions.first().location.start - 1,
                                                        helixDrawing.secondaryInteractions.last().location.start + 1
                                                    ),
                                                    Location(
                                                        helixDrawing.secondaryInteractions.last().location.end - 1,
                                                        helixDrawing.secondaryInteractions.first().location.end + 1
                                                    )
                                                )
                                            }

//                                        if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                            println("According to the ns this bp should be located in ${helixLocation}")
//                                        }
                                            helixLocation?.let {
                                                //If the basepair in junction is contained in this helix location, we catched the helix that should have the out_id orientation at i
                                                if (it.contains(helicalBpInJunction)) {
//                                                if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                                    println("-------> Got it")
//                                                }
                                                    new_out_ids.append(out_ids.split(" ")[i])
                                                    new_out_ids.append(" ")
                                                }
                                            }
                                        }
                                        i++
                                    }
//                                if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88)
//                                    println("out ids recomputed to ${new_out_ids.toString().trim()}")
                                    if (new_out_ids.toString().trim()
                                            .split(" ").size != junctionDrawing.outHelices.size
                                    ) {
                                        new_out_ids.clear()
                                        new_out_ids.append(out_ids)
                                    } else {

                                    }
                                } else {

                                }
                            }
                            if (new_out_ids.isNotEmpty()) {
                                Pair(
                                    new_out_ids.toString().trim()
                                        .split(" ").size == (e as JunctionDrawing).outHelices.size,
                                    new_out_ids.toString().trim()
                                )
                            } else
                                Pair(false, null)
                        }
                    layout.setConfigurationFor(selection, LayoutParameter.out_ids, out_ids)
                }
            } else {
                junctionLayoutBuilder.name?.let { name ->
                    junctionLayoutBuilder.radius?.let { radius ->
                        val selection =
                            { e: DrawingElement -> Pair(e is JunctionDrawing && e.name.equals(name), null) }
                        layout.setConfigurationFor(selection, LayoutParameter.radius, radius.toString())
                    }
                    junctionLayoutBuilder.out_ids?.let { out_ids ->
                        val selection =
                            { e: DrawingElement -> Pair(e is JunctionDrawing && e.name.equals(name), null) }
                        layout.setConfigurationFor(selection, LayoutParameter.out_ids, out_ids)
                    }
                } ?: run {
                    //if the type has been defined, we change the default behavior for all junctions in this type before to plot 2D
                    junctionLayoutBuilder.type?.let { type ->
                        val junctionType = when (type) {
                            1 -> JunctionType.ApicalLoop
                            2 -> JunctionType.InnerLoop
                            3 -> JunctionType.ThreeWay
                            4 -> JunctionType.FourWay
                            5 -> JunctionType.FiveWay
                            6 -> JunctionType.SixWay
                            7 -> JunctionType.SevenWay
                            8 -> JunctionType.EightWay
                            9 -> JunctionType.NineWay
                            10 -> JunctionType.TenWay
                            11 -> JunctionType.ElevenWay
                            12 -> JunctionType.TwelveWay
                            13 -> JunctionType.ThirteenWay
                            14 -> JunctionType.FourteenWay
                            15 -> JunctionType.FifthteenWay
                            16 -> JunctionType.SixteenWay
                            else -> JunctionType.Flower
                        }
                        junctionsBehaviors[junctionType] = { junctionDrawing: JunctionDrawing, helixRank: Int ->
                            val newLayout = junctionLayoutBuilder.out_ids!!.split(" ")?.map {
                                ConnectorId.valueOf(it)
                            }?.toList()

                            ConnectorId.values()
                                .first { it.value == (junctionDrawing.inId.value + newLayout[helixRank - 1].value) % ConnectorId.values().size }
                            //newLayout[helixRank - 1]
                        }

                    }

                }
            }
        }
        return layout
    }

}

class JunctionLayoutBuilder() {
    var name: String? = null
    val locationBuilder = LocationBuilder()
    var type: Int? = null
    var out_ids: String? = null
    var radius: Double? = null

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }
}

class ThemeBuilder(data: MutableMap<String, Double> = mutableMapOf()) {
    private var details = mutableListOf<DetailsBuilder>()
    private val colors = mutableListOf<ColorBuilder>()
    private val shows = mutableListOf<ShowBuilder>()
    private val lines = mutableListOf<LineBuilder>()
    private val hides = mutableListOf<HideBuilder>()
    private val data = data.toMutableMap()

    fun details(setup: DetailsBuilder.() -> Unit) {
        val detailsBuilder = DetailsBuilder()
        detailsBuilder.setup()
        this.details.add(detailsBuilder)
    }

    fun show(setup: ShowBuilder.() -> Unit) {
        val showBuilder = ShowBuilder(this.data)
        showBuilder.setup()
        this.shows.add(showBuilder)
    }

    fun color(setup: ColorBuilder.() -> Unit) {
        val colorBuilder = ColorBuilder(this.data)
        colorBuilder.setup()
        this.colors.add(colorBuilder)
    }

    fun line(setup: LineBuilder.() -> Unit) {
        val lineBuilder = LineBuilder(this.data)
        lineBuilder.setup()
        this.lines.add(lineBuilder)
    }

    fun hide(setup: HideBuilder.() -> Unit) {
        val hideBuilder = HideBuilder(this.data)
        hideBuilder.setup()
        this.hides.add(hideBuilder)
    }

    fun build(): Theme {
        val t = Theme()
        this.details.forEach { detailsBuilder ->
            var location: Location? = null
            if (!detailsBuilder.locationBuilder.isEmpty()) {
                location = detailsBuilder.locationBuilder.build()
            }
            val typesSelected = mutableListOf<SecondaryStructureType>()
            detailsBuilder.getSecondaryStructureTypes()?.let { types ->
                types.forEach { type ->
                    typesSelected.add(type)
                }
            }
            if (typesSelected.isNotEmpty()) {
                typesSelected.forEach { typesSelected ->
                    when (typesSelected) {
                        SecondaryStructureType.Helix -> {
                            when (detailsBuilder.value) {
                                1 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Helix
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SecondaryInteraction
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is HelixDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                }
                                2 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Helix
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SecondaryInteraction
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                }
                                3 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Helix
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SecondaryInteraction
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                }
                                4, 5 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Helix
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SecondaryInteraction
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SecondaryInteractionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "true"}
                                    )
                                }
                            }
                        }
                        SecondaryStructureType.Junction -> {
                            when (detailsBuilder.value) {
                                1 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Junction
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                        { el -> "false"}
                                    )

                                }
                                2 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Junction
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                }
                                3 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Junction
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                }
                                4, 5 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.Junction
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is JunctionDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                }
                            }

                        }
                        SecondaryStructureType.SingleStrand -> {
                            when (detailsBuilder.value) {
                                1 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SingleStrand
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                }
                                2 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SingleStrand
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                }
                                3 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SingleStrand
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "false"}                                    )
                                }
                                4, 5 -> {
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.SingleStrand
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.AShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.A && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.UShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.U && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.GShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.G && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.CShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.C && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.XShape && el.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                    t.setConfigurationFor(
                                        { el ->
                                            el.type == SecondaryStructureType.X && el.parent?.parent is SingleStrandDrawing
                                        },
                                        ThemeParameter.fulldetails,
                                       { el -> "true"}                                    )
                                }
                            }
                        }
                    }
                }
            } else
                when (detailsBuilder.value) {
                    1 -> {
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Helix && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SecondaryInteraction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Junction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SingleStrand && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.PhosphodiesterBond && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.AShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.A && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.UShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.U && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.GShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.G && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.CShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.C && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.XShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.X && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.InteractionSymbol && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t
                    }
                    2 -> {
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Helix && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SecondaryInteraction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Junction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SingleStrand && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.PhosphodiesterBond && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.AShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.A && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.UShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.U && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.GShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.G && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.CShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.C && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.XShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.X && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.InteractionSymbol && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t
                    }
                    3 -> {
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Helix && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SecondaryInteraction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Junction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SingleStrand && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.PhosphodiesterBond && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.AShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.A && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.UShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.U && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.GShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.G && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.CShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.C && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "false"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.XShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el -> el.type == SecondaryStructureType.X },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.InteractionSymbol && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t
                    }
                    4 -> {
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Helix && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SecondaryInteraction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Junction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SingleStrand && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.PhosphodiesterBond && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.AShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.A && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.UShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.U && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.GShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.G && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.CShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.C && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.XShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.X && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.InteractionSymbol && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "false"}                        )
                        t
                    }
                    else -> {
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Helix && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SecondaryInteraction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.Junction && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.SingleStrand && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.PhosphodiesterBond && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.AShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.A && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.UShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.U && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.GShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.G && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.CShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.C && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.XShape && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t.setConfigurationFor({ el ->
                            el.type == SecondaryStructureType.X && (if (location == null) true else el.inside(
                                location as Location
                            ))
                        }, ThemeParameter.fulldetails,{ el -> "true"})
                        t.setConfigurationFor(
                            { el ->
                                el.type == SecondaryStructureType.InteractionSymbol && (if (location == null) true else el.inside(
                                    location as Location
                                ))
                            },
                            ThemeParameter.fulldetails,
                           { el -> "true"}                        )
                        t
                    }
                }
        }
        this.colors.forEach { colorBuilder ->
            colorBuilder.scheme?.let { schemeName ->
                RnartistConfig.colorSchemes.get(schemeName)?.let { scheme ->

                    scheme.forEach { selection, color ->
                        t.setConfigurationFor(
                            selection,
                            ThemeParameter.color,
                            color
                        )
                    }

                }
            } ?: run {
                colorBuilder.value?.let {
                    var color2Apply = colorBuilder.value.toString()
                    var location: Location? = null
                    val typesSelected = mutableListOf<SecondaryStructureType>()
                    if (!colorBuilder.locationBuilder.isEmpty()) {
                        location = colorBuilder.locationBuilder.build()
                    }
                    colorBuilder.getSecondaryStructureTypes()?.let { types ->
                        types.forEach { type ->
                            typesSelected.add(type)
                        }
                    }

                    if (data.isNotEmpty() && (colorBuilder.filtered || colorBuilder.to != null)) { //if we have some data and the user filtered them OR the color to has been set
                        colorBuilder.data.forEach { position, value ->
                            val fromColor = getAWTColor(colorBuilder.value.toString())
                            val min = colorBuilder.data.values.minOrNull()
                            val max = colorBuilder.data.values.maxOrNull()
                            colorBuilder.to?.let { to ->
                                val toColor = getAWTColor(to)
                                if (min != max) {
                                    val p = (value - min!!) / (max!! - min!!)
                                    val r = fromColor.red * (1 - p) + toColor.red * p
                                    val g = fromColor.green * (1 - p) + toColor.green * p
                                    val b = fromColor.blue * (1.toFloat() - p) + toColor.blue * p
                                    color2Apply = getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt()))
                                }
                            }
                            val selection =
                                { e: DrawingElement ->
                                    (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && ((if (location == null) true else e.inside(
                                        location as Location
                                    )) && e.inside(Location(position.toInt())))
                                }
                            t.setConfigurationFor(
                                selection,
                                ThemeParameter.color,
                                {e -> color2Apply}
                            )
                        }
                    } else {
                        val selection =
                            { e: DrawingElement ->
                                (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && (if (location == null) true else e.inside(
                                    location as Location
                                ))
                            }
                        t.setConfigurationFor(
                            selection,
                            ThemeParameter.color,
                            {e -> color2Apply}
                        )
                    }
                }
            }
        }
        this.shows.forEach { showBuilder ->
            var location: Location? = null
            val typesSelected = mutableListOf<SecondaryStructureType>()
            if (!showBuilder.locationBuilder.isEmpty()) {
                location = showBuilder.locationBuilder.build()
            }
            showBuilder.getSecondaryStructureTypes()?.let { types ->
                types.forEach { type ->
                    typesSelected.add(type)
                }
            }

            if (data.isNotEmpty() && showBuilder.filtered) { //if we have some data and the user filtered them
                showBuilder.data.forEach { position, value ->
                    val selection =
                        { e: DrawingElement ->
                            (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && ((if (location == null) true else e.inside(
                                location as Location
                            )) && e.inside(Location(position.toInt())))
                        }
                    t.setConfigurationFor(
                        selection,
                        ThemeParameter.fulldetails,
                       { el -> "true"}                    )
                }
            } else if (typesSelected.isNotEmpty() || location != null) {
                val selection =
                    { e: DrawingElement ->
                        (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && (if (location == null) true else e.inside(
                            location as Location
                        ))
                    }
                t.setConfigurationFor(
                    selection,
                    ThemeParameter.fulldetails,
                   { el -> "true"}                )
            }
        }
        this.hides.forEach { hideBuilder ->
            var location: Location? = null
            val typesSelected = mutableListOf<SecondaryStructureType>()
            if (!hideBuilder.locationBuilder.isEmpty()) {
                location = hideBuilder.locationBuilder.build()
            }
            hideBuilder.getSecondaryStructureTypes()?.let { types ->
                types.forEach { type ->
                    typesSelected.add(type)
                }
            }

            if (data.isNotEmpty() && hideBuilder.filtered) { //if we have some data and the user filtered them
                hideBuilder.data.forEach { position, value ->
                    val selection =
                        { e: DrawingElement ->
                            (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && ((if (location == null) true else e.inside(
                                location as Location
                            )) && e.inside(Location(position.toInt())))
                        }
                    t.setConfigurationFor(
                        selection,
                        ThemeParameter.fulldetails,
                        {e -> "none"}
                    )
                }
            } else if (typesSelected.isNotEmpty() || location != null) {
                val selection =
                    { e: DrawingElement ->
                        (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && (if (location == null) true else e.inside(
                            location as Location
                        ))
                    }
                t.setConfigurationFor(
                    selection,
                    ThemeParameter.fulldetails,
                    {e -> "none"}
                )
            }
        }
        this.lines.forEach { lineBuilder ->
            var location: Location? = null
            val typesSelected = mutableListOf<SecondaryStructureType>()
            if (!lineBuilder.locationBuilder.isEmpty()) {
                location = lineBuilder.locationBuilder.build()
            }
            lineBuilder.getSecondaryStructureTypes()?.let { types ->
                types.forEach { type ->
                    typesSelected.add(type)
                }
            }

            if (data.isNotEmpty() && lineBuilder.filtered) { //if we have some data and the user filtered them
                lineBuilder.data.forEach { position, value ->
                    val selection =
                        { e: DrawingElement ->
                            (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && ((if (location == null) true else e.inside(
                                location as Location
                            )) && e.inside(Location(position.toInt())))
                        }
                    t.setConfigurationFor(
                        selection,
                        ThemeParameter.linewidth,
                        { e -> lineBuilder.value.toString()}
                    )
                }
            } else {
                val selection =
                    { e: DrawingElement ->
                        (if (typesSelected.isEmpty()) true else typesSelected.contains(e.type)) && (if (location == null) true else e.inside(
                            location as Location
                        ))
                    }
                t.setConfigurationFor(
                    selection,
                    ThemeParameter.linewidth,
                    { e -> lineBuilder.value.toString()}
                )
            }
        }
        return t
    }

}

open class ThemeConfigurationBuilder(data: MutableMap<String, Double>) {
    val locationBuilder = LocationBuilder()
    var type: String? = null
    val data = data.toMutableMap()
    var filtered = false

    infix fun MutableMap<String, Double>.gt(min: Double) {
        val excluded = this.filter { it.value <= min }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
    }

    infix fun MutableMap<String, Double>.lt(max: Double) {
        val excluded = this.filter { it.value >= max }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
    }

    infix fun MutableMap<String, Double>.eq(value: Double) {
        val excluded = this.filter { it.value != value }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
    }

    infix fun MutableMap<String, Double>.between(range: ClosedFloatingPointRange<Double>) {
        val excluded = this.filter { it.value !in range }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
    }

    fun getSecondaryStructureTypes() = this.type?.split(" ")?.flatMap { getSecondaryStructureType(it) }

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

}

class DetailsBuilder {
    val locationBuilder = LocationBuilder()
    var value: Int = 1
    var type: String? = null

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    fun getSecondaryStructureTypes() = this.type?.split(" ")?.flatMap { getSecondaryStructureType(it) }
}

class ShowBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data)

class ColorBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    var value: String? = null
        set(value) {
            field = value?.let {
                if (!value.startsWith("#")) getColorCode(value) else value
            }
        }
    var to: String? = null
        set(value) {
            field = value?.let {
                if (!value.startsWith("#")) getColorCode(value) else value
            }
        }
    var scheme:String? = null

}

class LineBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    var value = 2.0
}

class HideBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data)

class LocationBuilder {

    val blocks = mutableMapOf<Int, Int>()

    infix fun Int.to(i: Int) {
        blocks[this] = i
    }

    fun build(): Location {
        return Location(this.blocks.map { "${it.key}:${it.value - it.key + 1}" }.joinToString(","))
    }

    fun isEmpty(): Boolean {
        return this.blocks.isEmpty()
    }

}

fun ss(setup: SecondaryStructureBuilder.() -> Unit) = SecondaryStructureBuilder().apply { setup() }.build()

fun bn(setup: BracketNotationBuilder.() -> Unit) = BracketNotationBuilder().apply { setup() }.build()

fun booquet(setup: BooquetBuilder.() -> Unit) = BooquetBuilder().apply { setup() }.build()

fun rnartist(setup: RNArtistBuilder.() -> Unit) = RNArtistBuilder().apply { setup() }.build()

fun openscad(setup: OpenScadBuilder.() -> Unit) = OpenScadBuilder().apply { setup() }.build()

fun theme(setup: ThemeBuilder.() -> Unit) = ThemeBuilder().apply { setup() }.build()

fun layout(setup: LayoutBuilder.() -> Unit) = LayoutBuilder().apply { setup() }.build()

private fun getColorCode(name: String): String {
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

private fun getSecondaryStructureType(type: String): List<SecondaryStructureType> {
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
