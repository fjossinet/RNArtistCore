package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.*
import io.github.fjossinet.rnartist.core.model.*
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.Exception
import java.nio.file.FileSystems.*
import kotlin.random.Random

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
            sequence.append((1..it).joinToString(separator = "") { listOf("A", "U", "G", "C").random() })
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
            val helices = mutableListOf<Helix>()
            helixBuilders.forEach {
                it.build()?.let { h ->
                    helices.add(h)
                }
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

    fun build(): SecondaryStructure {
        this.seq?.let {
            return SecondaryStructure(RNA(name, it), bracketNotation = value.trim(), source = BracketNotation())
        }
        val sequence = StringBuffer()
        sequence.append((1..value.trim().length).joinToString(separator = "") { listOf("A", "U", "G", "C").random() })
        val ss = SecondaryStructure(
            RNA(name = name, seq = sequence.toString()),
            bracketNotation = value.trim(),
            source = BracketNotation()
        )
        ss.randomizeSeq()
        return ss
    }
}

class InteractionBuilder

class HelixBuilder {

    private val locationBuilder = LocationBuilder()
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

    fun build(): Helix? {
        locationBuilder.build()?.let { location ->
            val h = Helix(name ?: "MyHelix")
            for (i in location.start..location.start + location.length / 2 - 1) {
                val l = Location(Location(i), Location(location.end - (i - location.start)))
                h.secondaryInteractions.add(BasePair(l, Edge.WC, Edge.WC, Orientation.cis))
            }
            return h
        }
        return null
    }

}

class SecondaryStructureBuilder {

    private var secondaryStructures = mutableListOf<SecondaryStructure>()
    val dslElement = SSEl()
        get() {
            return field
        }

    fun build(): List<SecondaryStructure> {
        return secondaryStructures
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
        bnBuilder.build().let {
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
        this.dslElement.addVienna(viennaBuilder.dslElement)
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
        secondaryStructures.addAll(result)
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

abstract class OutputFileBuilder {
    abstract val dslElement: DSLElement
    var path: String? = null
    var name: String? = null
    var width: Double = 800.0
    var height: Double = 800.0
    var locationBuilder: LocationBuilder = LocationBuilder()


    /**
     * @param [drawing] the SecondaryStructureDrawing to be saved
     * @param [rnartistElement] if not null, a dedicated dsl script will be created for the drawing saved
     */
    abstract fun build(drawing: SecondaryStructureDrawing, rnartistElement: DSLElement?)

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }
}

class PNGBuilder : OutputFileBuilder() {
    override val dslElement = PNGEl()
        get() {
            field.children.clear()
            this.path?.let { path ->
                val sep = getDefault().separator
                field.setPath(
                    if (!path.startsWith(sep))
                        "${Jar().path()}${sep}${path}"
                    else
                        path
                )
            }
            this.name?.let {
                field.setName(it)
            }
            field.setWidth(width)
            field.setHeight(height)
            return field
        }

    override fun build(drawing: SecondaryStructureDrawing, rnartistElement: DSLElement?) {
        path?.let { path ->
            val sep = getDefault().separator
            val fileName =
                drawing.secondaryStructure.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            //a file name can contain a dot
                            val tokens = drawing.secondaryStructure.source?.getId()?.split(sep)?.last()?.split(".")
                            tokens?.let {
                                tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                            } ?: run {
                                null
                            }
                        }

                        is BracketNotation -> {
                            drawing.secondaryStructure.name
                        }

                        else -> {
                            null
                        }

                    }

                }
            val dataPath =
                drawing.secondaryStructure.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            //a file name can contains a dot
                            val tokens = drawing.secondaryStructure.source?.getId()?.split(".")
                            tokens?.let {
                                tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                            } ?: run {
                                null
                            }
                        }

                        is BracketNotation -> {
                            drawing.secondaryStructure.name
                        }

                        else -> {
                            null
                        }

                    }

                }
            var f = if (!path.startsWith(sep))
                File("${Jar().path()}${sep}${path}${sep}${fileName}.png")
            else
                File("${path}${sep}${fileName}.png")
            if (!f.parentFile.exists())
                f.parentFile.mkdirs()
            f.createNewFile()
            locationBuilder.build()?.let { location ->
                drawing.getFrame(location)?.let { selectionFrame ->
                    drawing.asPNG(
                        frame = Rectangle2D.Double(0.0, 0.0, width, height),
                        selectionFrame = selectionFrame,
                        outputFile = f
                    )
                }
            } ?: run {
                drawing.asPNG(
                    frame = Rectangle2D.Double(0.0, 0.0, width, height),
                    outputFile = f
                )
            }
            dataPath?.let { dataPath ->
                //if rnartistElement is not null, this means that several 2Ds have been drawn and for each 2D, we generate a dedicated script
                rnartistElement?.let { rnartistElement ->
                    //now the dsl script for this 2D, if not already there
                    f = if (!dataPath.startsWith(sep))
                        File(
                            "${Jar().path()}${sep}${dataPath}.kts"
                        )
                    else
                        File(
                            "${dataPath}.kts"
                        )
                    if (!f.exists()) {
                        f.createNewFile()
                        val buff = StringBuffer()
                        rnartistElement.dump("", buff)
                        f.writeText(buff.toString())
                    }
                }
            }
        }
    }

}

class SVGBuilder : OutputFileBuilder() {

    override val dslElement = SVGEl()
        get() {
            field.children.clear()
            this.path?.let {
                field.setPath(it)
            }
            this.name?.let {
                field.setName(it)
            }
            field.setWidth(width)
            field.setHeight(height)
            return field
        }

    override fun build(drawing: SecondaryStructureDrawing, rnartistElement: DSLElement?) {
        path?.let { path ->
            val sep = getDefault().separator
            val fileName =
                drawing.secondaryStructure.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            //a file name can contains a dot
                            val tokens = drawing.secondaryStructure.source?.getId()?.split(sep)?.last()?.split(".")
                            tokens?.let {
                                tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                            } ?: run {
                                null
                            }
                        }

                        is BracketNotation -> {
                            drawing.secondaryStructure.name
                        }

                        else -> {
                            null
                        }

                    }

                }
            val dataPath =
                drawing.secondaryStructure.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            //a file name can contains a dot
                            val tokens = drawing.secondaryStructure.source?.getId()?.split(".")
                            tokens?.let {
                                tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                            } ?: run {
                                null
                            }
                        }

                        is BracketNotation -> {
                            drawing.secondaryStructure.name
                        }

                        else -> {
                            null
                        }

                    }

                }
            var f = if (!path.startsWith(sep))
                File("${Jar().path()}${sep}${path}${sep}${fileName}.svg")
            else
                File("${path}${sep}${fileName}.svg")
            if (!f.parentFile.exists())
                f.parentFile.mkdirs()
            f.createNewFile()
            locationBuilder.build()?.let { location ->
                drawing.getFrame(location)?.let { selectionFrame ->
                    drawing.asSVG(
                        frame = Rectangle2D.Double(0.0, 0.0, width, height),
                        selectionFrame = selectionFrame,
                        outputFile = f
                    )
                }
            } ?: run {
                drawing.asSVG(
                    frame = Rectangle2D.Double(0.0, 0.0, width, height),
                    outputFile = f
                )
            }
            dataPath?.let { dataPath ->
                //if rnartistElement is not null, this means that several 2Ds have been drawn and for each 2D, we generate a dedicated script
                rnartistElement?.let { rnartistElement ->
                    //now the dsl script for this 2D, if not already there
                    f = if (!dataPath.startsWith(sep))
                        File(
                            "${Jar().path()}${sep}${dataPath}.kts"
                        )
                    else
                        File(
                            "${dataPath}.kts"
                        )
                    if (!f.exists()) {
                        f.createNewFile()
                        val buff = StringBuffer()
                        rnartistElement.dump("", buff)
                        f.writeText(buff.toString())
                    }
                }
            }
        }
    }

}

class ChimeraBuilder {

    var path: String? = null
    var name: String? = null

    fun build(drawing: SecondaryStructureDrawing) {
        path?.let { path ->
            val sep = getDefault().separator
            val f = if (!path.startsWith(sep))
                File("${Jar().path()}${sep}${path}${sep}${drawing.secondaryStructure.rna.name.replace("/", "_")}.cxc")
            else
                File("${path}${sep}${drawing.secondaryStructure.rna.name.replace("/", "_")}.cxc")
            drawing.secondaryStructure.tertiaryStructure?.let {
                drawing.asChimeraScript(f)
            }
        }
    }

}

class BlenderBuilder {

    var path: String? = null

    fun build(drawing: SecondaryStructureDrawing) {
        path?.let { path ->
            val sep = getDefault().separator
            val f = if (!path.startsWith(sep))
                File("${Jar().path()}${sep}${path}${sep}${drawing.secondaryStructure.rna.name.replace("/", "_")}.py")
            else
                File("${path}${sep}${drawing.secondaryStructure.rna.name.replace("/", "_")}.py")
            drawing.secondaryStructure.tertiaryStructure?.let { tertiaryStructure ->
                drawing.asBlenderScript(
                    tertiaryStructure,
                    f
                )
            }
        }
    }

}

abstract class InputFileBuilder {
    abstract val dslElement: DSLElement
    var file: String? = null
    var path: String? = null

    abstract fun build(): List<SecondaryStructure>
}

class PDBBuilder {

    var file: String? = null
    var name: String? = null
    var id: String? = null

    fun build(): List<SecondaryStructure> {
        val structures = mutableListOf<SecondaryStructure>()
        if (this.id != null) {
            val pdbFile = File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.absolutePath
        }
        this.file?.let { file ->
            val sep = getDefault().separator
            val f = if (!file.startsWith(sep))
                File("${Jar().path()}${sep}${file}")
            else
                File(file)
            try {
                structures.addAll(Annotate3D().annotate(f))
                structures.forEach {
                    it.source = if (this.id != null) PDBSource(this.id!!) else FileSource(this.file!!)
                }
            } catch (e: Exception) {
                println(e.message)
            }
            if (this.name != null) {
                structures.forEach {
                    if (it.rna.name == this.name)
                        return arrayListOf(it)
                }
            }
            return structures
        }
        return listOf()
    }
}

class ViennaBuilder : InputFileBuilder() {
    override val dslElement = ViennaEl()
        get() {
            this.file?.let {
                field.setFile(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val sep = getDefault().separator
            val f = if (!file.startsWith(sep))
                File("${Jar().path()}${sep}${file}")
            else
                File(file)
            val ss = parseVienna(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)
        }
        this.path?.let { path ->
            val sep = getDefault().separator
            val f = if (!path.startsWith(sep)) {
                File("${Jar().path()}${sep}${path}")
            } else
                File(path)
            val structures = mutableListOf<SecondaryStructure>()
            f.listFiles { _, name -> name.endsWith(".vienna") }?.forEach { viennaFile ->
                val ss = parseVienna(FileReader(viennaFile))
                ss.source = FileSource(viennaFile.absolutePath)
                structures.add(ss)
            }
            return structures
        }
        return listOf()
    }
}

class BPSeqBuilder : InputFileBuilder() {
    override val dslElement = BPSeqEl()
        get() {
            this.file?.let {
                field.setFile(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val sep = getDefault().separator
            val f = if (!file.startsWith(sep))
                File("${Jar().path()}${sep}${file}")
            else
                File(file)
            val ss = parseBPSeq(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)
        }
        return listOf()
    }
}

class CTBuilder : InputFileBuilder() {
    override val dslElement = CTEl()
        get() {
            this.file?.let {
                field.setFile(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val sep = getDefault().separator
            val f = if (!file.startsWith(sep))
                File("${Jar().path()}${sep}${file}")
            else
                File(file)
            val ss = parseCT(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)

        }
        return listOf()
    }
}

class StockholmBuilder : InputFileBuilder() {
    override val dslElement = StockholmEl()
        get() {
            this.file?.let {
                field.setFile(it)
            }
            return field
        }
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
            val sep = getDefault().separator
            val f = if (!file.startsWith(sep))
                File("${Jar().path()}${sep}${file}")
            else
                File(file)
            val secondaryStructures = parseStockholm(FileReader(f), withConsensus2D = true).third
            secondaryStructures.forEach {
                it.source = FileSource(file)
                it.rna.useAlignmentNumberingSystem = useAlignmentNumbering
            }
            if (this.name != null) {
                secondaryStructures.forEach {
                    if (it.rna.name == this.name)
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
            rfam.getEntry(id)?.let { reader ->
                val secondaryStructures = try {
                    parseStockholm(reader, withConsensus2D = true).third
                } catch (e: IOException) {
                    println("RFAM Entry $id not found"); arrayListOf()
                }
                if (secondaryStructures.isNotEmpty()) {
                    val ids = secondaryStructures.map { it.name.split("/").first() }
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
                        if ("consensus" == name)
                            return arrayListOf(secondaryStructures.first())
                        else {
                            secondaryStructures.forEach {
                                if (name.equals(it.name))
                                    return arrayListOf(it)
                            }
                        }
                    }
                }
                return secondaryStructures
            }
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

class BooquetBuilder {
    var path: String? = null
    var width = 600.0
    var height = 600.0
    private var junctionDiameter = 25.0
    var color = getHTMLColorString(Color.BLACK)
    private var secondaryStructures = mutableListOf<SecondaryStructure>()
    var line = 2.0

    fun build() {
        this.path?.let { path ->
            this.secondaryStructures.forEach { ss ->
                val svgOutput = Booquet(
                    ss,
                    this.width,
                    this.height,
                    junction_diameter = junctionDiameter,
                    lineWidth = line,
                    color = if (color.startsWith("#")) getAWTColor(color) else getAWTColor(getColorCode(color))
                )
                val sep = getDefault().separator
                val f = if (!path.startsWith(sep))
                    File("${Jar().path()}${sep}${path}${sep}${ss.rna.name.replace("/", "_")}.svg")
                else
                    File("${path}${sep}${ss.rna.name.replace("/", "_")}.svg")
                if (!f.parentFile.exists())
                    f.parentFile.mkdirs()
                f.createNewFile()
                f.writeText(svgOutput)
            }
        }
    }

    fun ss(setup: SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build())
    }

}

class RNArtistBuilder {
    private val rnartistElement = RNArtistEl()
    private var svgOutputBuilder: SVGBuilder? = null
    private var pngOutputBuilder: PNGBuilder? = null
    private var chimeraOutputBuilder: ChimeraBuilder? = null
    private var blenderOutputBuilder: BlenderBuilder? = null
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var theme: Theme? = null
    var data: MutableMap<String, Double> = mutableMapOf()
    private var layout: Layout? = null

    fun build(): Pair<List<SecondaryStructureDrawing>, RNArtistEl> {
        val drawings = mutableListOf<SecondaryStructureDrawing>()
        this.secondaryStructures.forEachIndexed { _, ss ->
            val drawing = SecondaryStructureDrawing(ss, WorkingSession())
            //at this point all the junctions have found their layout. We can store them DSLELement tree in order to not recompute them during the next loads
            /*drawing.allJunctions.forEach {

            }*/
            this.theme?.let { theme ->
                drawing.applyTheme(theme)
            }
            this.layout?.let { layout ->
                drawing.applyLayout(layout)
            }
            this.pngOutputBuilder?.let { pngOutputBuilder ->
                ss.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            if (source.getId().endsWith(".vienna")) {
                                val ssElement =
                                    this.rnartistElement.addSS() //only a single ss element is allowed the previous one is removed
                                val viennaElement = ssElement.addVienna()
                                viennaElement.setFile(source.getId())
                            }
                        }

                        is BracketNotation -> {
                            val ssElement =
                                this.rnartistElement.addSS() //only a single ss element is allowed the previous one is removed
                            val bnElement = ssElement.addBracketNotation()
                            bnElement.setSeq(ss.rna.seq)
                            bnElement.setValue(ss.toBracketNotation())
                            bnElement.setName(ss.name)
                        }

                        else -> {

                        }

                    }

                }
                this.rnartistElement.addPNG(pngOutputBuilder.dslElement)
                pngOutputBuilder.name?.let { chainName ->
                    if (chainName == ss.rna.name)
                        pngOutputBuilder.build(
                            drawing,
                            rnartistElement
                        ) //if several secondary structures computed, we will generate a script dedicated to each 2D. Then we need to send the rnartistElement to the function
                } ?: run {
                    pngOutputBuilder.build(
                        drawing,
                        rnartistElement
                    ) //if several secondary structures computed, we will generate a script dedicated to each 2D. Then we need to send the rnartistElement to the function
                }
            }
            this.svgOutputBuilder?.let { svgOutputBuilder ->
                ss.source?.let { source ->
                    when (source) {
                        is FileSource -> {
                            if (source.getId().endsWith(".vienna")) {
                                val ssElement =
                                    this.rnartistElement.addSS() //only a single ss element is allowed the previous one is removed
                                val viennaElement = ssElement.addVienna()
                                viennaElement.setFile(source.getId())
                            }
                        }

                        is BracketNotation -> {
                            val ssElement =
                                this.rnartistElement.addSS() //only a single ss element is allowed the previous one is removed
                            val bnElement = ssElement.addBracketNotation()
                            bnElement.setSeq(ss.rna.seq)
                            bnElement.setValue(ss.toBracketNotation())
                            bnElement.setName(ss.name)
                        }

                        else -> {
                        }

                    }

                }
                this.rnartistElement.addSVG(svgOutputBuilder.dslElement)
                svgOutputBuilder.name?.let { chainName ->
                    if (chainName == ss.rna.name)
                        svgOutputBuilder.build(
                            drawing,
                            rnartistElement
                        )
                } ?: run {
                    svgOutputBuilder.build(
                        drawing,
                        rnartistElement
                    )
                }
            }
            this.chimeraOutputBuilder?.name?.let { chainName ->
                if (chainName == ss.rna.name)
                    this.chimeraOutputBuilder?.build(drawing)
            } ?: run {
                this.chimeraOutputBuilder?.build(drawing)
            }
            drawings.add(drawing)
        }
        return Pair(drawings, rnartistElement)
    }

    fun ss(setup: SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build())
        this.rnartistElement.children.add(secondaryStructureBuilder.dslElement)
    }

    fun theme(setup: ThemeBuilder.() -> Unit) {
        val themeBuilder = ThemeBuilder(data)
        themeBuilder.setup()
        this.theme = themeBuilder.build()
        this.rnartistElement.addTheme(themeBuilder.dslElement)
    }

    fun layout(setup: LayoutBuilder.() -> Unit) {
        val layoutBuilder = LayoutBuilder()
        layoutBuilder.setup()
        this.layout = layoutBuilder.build()
        this.rnartistElement.addLayout(layoutBuilder.dslElement)
    }

    fun data(setup: DataBuilder.() -> Unit) {
        val dataBuilder = DataBuilder()
        dataBuilder.setup()
        data = dataBuilder.data
    }

    fun svg(setup: SVGBuilder.() -> Unit) {
        this.svgOutputBuilder = SVGBuilder()
        this.svgOutputBuilder!!.setup()
    }

    fun png(setup: PNGBuilder.() -> Unit) {
        this.pngOutputBuilder = PNGBuilder()
        this.pngOutputBuilder!!.setup()
    }

    fun output(setup: PNGBuilder.() -> Unit) {
        this.pngOutputBuilder = PNGBuilder()
        this.pngOutputBuilder!!.setup()
    }

    fun chimera(setup: ChimeraBuilder.() -> Unit) {
        this.chimeraOutputBuilder = ChimeraBuilder()
        this.chimeraOutputBuilder!!.setup()
    }

    fun blender(setup: BlenderBuilder.() -> Unit) {
        this.blenderOutputBuilder = BlenderBuilder()
        this.blenderOutputBuilder!!.setup()
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
    val dslElement = LayoutEl()
        get() {
            return field
        }

    fun junction(setup: JunctionLayoutBuilder.() -> Unit) {
        val junctionLayoutBuilder = JunctionLayoutBuilder()
        junctionLayoutBuilder.setup()
        this.dslElement.addJunction(junctionLayoutBuilder.dslElement)
        this.junctionLayoutBuilders.add(junctionLayoutBuilder)
    }

    fun build(): Layout {
        /*if needed, to debug this code section, use Rfam entry RF00011 and RNA X69982.1/45-449. It has a 6-way junction in the consensus that becomes 5-way for this RNA.
          if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88)
          println("Search to apply out_ids ${out_ids} to junction ${junctionDrawing.location}")
          if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                        println("############## bp in junction ${helicalBpInJunction}")
//                                    }
if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                            println("According to the ns this bp should be located in ${helixLocation}")
//                                        }
                                                //If the basepair in junction is contained in this helix location, we catched the helix that should have the out_id orientation at i
if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88) {
//                                                    println("-------> Got it")
//                                                }
//                                if ("X69982.1/45-449".equals(junctionDrawing.ssDrawing.secondaryStructure.rna.name) && junctionDrawing.location.start == 88)
//                                    println("out ids recomputed to ${new_out_ids.toString().trim()}")
          */

        val layout = Layout()
        junctionLayoutBuilders.forEach { junctionLayoutBuilder ->
            val selection = junctionLayoutBuilder.buildSelection()
            if (!junctionLayoutBuilder.isGlobalLayout) { //the layout targets specific junctions
                junctionLayoutBuilder.radius?.let { radius ->
                    layout.addConfigurationFor(selection, LayoutProperty.radius, radius.toString())
                }
                junctionLayoutBuilder.out_ids?.let { out_ids ->
                    layout.addConfigurationFor(selection, LayoutProperty.out_ids, out_ids)
                }
            } else { //we change the default behavior for all junctions in this type before to plot 2D
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
                        else -> null
                    }
                    junctionType?.let {
                        junctionsBehaviors[junctionType] = { junctionDrawing: JunctionDrawing, helixRank: Int ->
                            val newLayout = junctionLayoutBuilder.out_ids!!.split(" ").map {
                                ConnectorId.valueOf(it)
                            }.toList()

                            ConnectorId.entries
                                .first { it.value == (junctionDrawing.inId.value + newLayout[helixRank - 1].value) % ConnectorId.entries.size }
                        }
                    }

                }
            }
        }
        return layout
    }

}

class JunctionLayoutBuilder {
    val dslElement = JunctionEl()
        get() {
            name?.let {
                field.setName(it)
            }
            radius?.let {
                field.setRadius(it)
            }
            out_ids?.let {
                field.setOutIds(it)
            }
            type?.let {
                field.setType(it)
            }
            this.locationBuilder.dslElement?.let {
                field.children.add(it)
            }
            return field
        }

    var name: String? = null
    private val locationBuilder = LocationBuilder()
    var type: Int? = null
    var out_ids: String? = null
    var radius: Double? = null
    var isGlobalLayout = true
        get() = this.locationBuilder.isEmpty() && this.name == null //if true, this layout targets all the junctions for this junction type, not specific ones

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    /**
     * Build the function that will be used check each DrawingElement to be selected or not. If selected, the Theme will be applied
     */
    fun buildSelection(): (el: DrawingElement) -> Boolean {
        val location = this.locationBuilder.build()
        val junctionType = when (this.type) {
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
            else -> null
        }
        return { el: DrawingElement ->
            junctionType?.equals((el as? JunctionDrawing)?.junctionType) ?: true && this.name?.equals(el.name) ?: true && location?.contains(
                el.location
            ) ?: true
        }
    }
}

class ThemeBuilder(data: MutableMap<String, Double> = mutableMapOf()) {
    private val themeConfigurationBuilders = mutableListOf<ThemeConfigurationBuilder>()
    private val data = data.toMutableMap()
    var details: Int? = null
        set(value) {
            value?.let {
                val db = DetailsBuilder(this.data, it)
                this.themeConfigurationBuilders.add(db)
                this.dslElement.addDetails(it)
            }
        }
    var scheme: String? = null
        set(value) {
            value?.let {
                val sb = SchemeBuilder(this.data, it)
                this.themeConfigurationBuilders.add(sb)
                this.dslElement.addScheme(it)
            }
        }
    val dslElement = ThemeEl()
        get() {
            return field
        }

    fun show(setup: ShowBuilder.() -> Unit) {
        val showBuilder = ShowBuilder(this.data)
        showBuilder.setup()
        this.themeConfigurationBuilders.add(showBuilder)
        this.dslElement.addShow(showBuilder.dslElement)
    }

    fun color(setup: ColorBuilder.() -> Unit) {
        val colorBuilder = ColorBuilder(this.data)
        colorBuilder.setup()
        this.themeConfigurationBuilders.add(colorBuilder)
        this.dslElement.addColor(colorBuilder.dslElement)
    }

    fun line(setup: LineBuilder.() -> Unit) {
        val lineBuilder = LineBuilder(this.data)
        lineBuilder.setup()
        this.themeConfigurationBuilders.add(lineBuilder)
        this.dslElement.children.add(lineBuilder.dslElement)
    }

    fun hide(setup: HideBuilder.() -> Unit) {
        val hideBuilder = HideBuilder(this.data)
        hideBuilder.setup()
        this.themeConfigurationBuilders.add(hideBuilder)
        this.dslElement.addHide(hideBuilder.dslElement)
    }

    fun build(): Theme {
        val t = Theme()
        this.themeConfigurationBuilders.forEach { configurationBuilder ->
            when (configurationBuilder) {
                is DetailsBuilder -> {
                    when (configurationBuilder.dslProperty.value.toInt()) {
                        1 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { el -> "false" },
                                SecondaryStructureType.entries
                            )
                        }

                        2 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond)
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(
                                    SecondaryStructureType.InteractionSymbol,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape)
                            )
                        }

                        3 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape)
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(SecondaryStructureType.InteractionSymbol,
                                    SecondaryStructureType.A,
                                    SecondaryStructureType.U,
                                    SecondaryStructureType.G,
                                    SecondaryStructureType.C,
                                    SecondaryStructureType.X)
                            )
                        }

                        4 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape,
                                    SecondaryStructureType.A,
                                    SecondaryStructureType.U,
                                    SecondaryStructureType.G,
                                    SecondaryStructureType.C,
                                    SecondaryStructureType.X)
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(SecondaryStructureType.InteractionSymbol)
                            )
                        }

                        else -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                SecondaryStructureType.entries
                            )
                        }
                    }
                }
                is SchemeBuilder -> {
                    if ("Structural Domains" == configurationBuilder.dslProperty.value) {
                        val randomColors = (1..20).map { getHTMLColorString(randomColor()) }
                        t.addConfiguration(
                            ThemeProperty.color,
                            {el ->
                                when (el) {
                                    is HelixDrawing -> randomColors.get(Random.nextInt(0,19))
                                    is JunctionDrawing -> randomColors.get(Random.nextInt(0,19))
                                    is SingleStrandDrawing -> randomColors.get(Random.nextInt(0,19))
                                    is SecondaryInteractionDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is InteractionSymbolDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is ResidueDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is PhosphodiesterBondDrawing -> el.parent?.let {
                                        getHTMLColorString(it.getColor())
                                    } ?: run {
                                        getHTMLColorString(el.getColor())
                                    }
                                    is ResidueLetterDrawing-> getHTMLColorString(Color.WHITE)
                                    else -> getHTMLColorString(Color.RED)
                                }
                            },
                            SecondaryStructureType.entries
                        )
                    } else
                        RnartistConfig.colorSchemes.get(configurationBuilder.dslProperty.value)?.let { scheme ->
                            scheme.forEach { type, color ->
                                t.addConfiguration(
                                    ThemeProperty.color,
                                    color,
                                    listOf(type)
                                )
                            }
                        }
                }
                is ColorBuilder -> {
                    configurationBuilder.value?.let {
                        if (data.isNotEmpty() && (configurationBuilder.filtered || configurationBuilder.to != null)) { //if we have some data and the user filtered them OR the color to has been set
                            configurationBuilder.data.forEach { (position, value) ->
                                val fromColor = getAWTColor(configurationBuilder.value.toString())
                                val min = configurationBuilder.data.values.minOrNull()
                                val max = configurationBuilder.data.values.maxOrNull()
                                configurationBuilder.to?.let { to ->
                                    val toColor = getAWTColor(to)
                                    if (min != max) {
                                        val p = (value - min!!) / (max!! - min)
                                        val r = fromColor.red * (1 - p) + toColor.red * p
                                        val g = fromColor.green * (1 - p) + toColor.green * p
                                        val b = fromColor.blue * (1.toFloat() - p) + toColor.blue * p
                                        configurationBuilder.locationBuilder.build()?.let { loc ->
                                            //if a location has been defined, a configuration is added only if the position of the residue is inside this location
                                            if (loc.contains(position.toInt()))
                                                t.addConfiguration(
                                                    configurationBuilder.buildSelection(Location(position)),
                                                    ThemeProperty.color,
                                                { getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt())) })
                                        } ?: run {
                                            t.addConfiguration(
                                                configurationBuilder.buildSelection(Location(position)),
                                                ThemeProperty.color,
                                            { getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt())) })
                                        }
                                    }
                                } ?: run {
                                    t.addConfiguration(
                                        configurationBuilder.buildSelection(),
                                        ThemeProperty.color,
                                    { configurationBuilder.value!! })
                                }
                            }
                        } else {
                            t.addConfiguration(
                                configurationBuilder.buildSelection(),
                                ThemeProperty.color,
                            { configurationBuilder.value!! })
                        }
                    }
                }

                is ShowBuilder -> {
                    t.addConfiguration(
                        configurationBuilder.buildSelection(),
                        ThemeProperty.fulldetails,
                    { "true" })
                }

                is HideBuilder -> {
                    t.addConfiguration(
                        configurationBuilder.buildSelection(),
                        ThemeProperty.fulldetails,
                    { "false" })
                }

                is LineBuilder -> {
                    val lineBuilder = configurationBuilder
                    t.addConfiguration(
                        lineBuilder.buildSelection(),
                        ThemeProperty.linewidth,
                    { lineBuilder.value.toString() })
                }
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

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    /**
     * Build the function that will be used check each DrawingElement to be selected or not. If selected, the Theme will be applied
     */
    fun buildSelection(userDefinedLocation: Location? = null): (el: DrawingElement) -> Boolean {
        val selectors = mutableListOf<(el: DrawingElement, l: Location?) -> Boolean>()
        this.type?.let { type ->
            type.split(" ").map { type ->
                selectors.add(when (type) {
                    "A" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.AShape }
                    "A@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.AShape }
                    "A@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.AShape }
                    "A@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.AShape }
                    "A@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.AShape }
                    "A@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.AShape }
                    "A@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.AShape }
                    "A@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.AShape }

                    "U" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.UShape }
                    "U@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.UShape }
                    "U@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.UShape }
                    "U@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.UShape }
                    "U@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.UShape }
                    "U@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.UShape }
                    "U@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.UShape }
                    "U@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.UShape }

                    "G" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.GShape }
                    "G@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.GShape }
                    "G@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.GShape }
                    "G@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.GShape }
                    "G@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.GShape }
                    "G@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.GShape }
                    "G@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.GShape }
                    "G@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.GShape }

                    "C" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.CShape }
                    "C@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.CShape }
                    "C@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.CShape }
                    "C@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.CShape }
                    "C@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.CShape }
                    "C@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.CShape }
                    "C@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.CShape }
                    "C@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.CShape }

                    "X" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.XShape }
                    "X@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.XShape }
                    "X@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.XShape }
                    "X@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.XShape }
                    "X@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.XShape }
                    "X@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.XShape }
                    "X@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.XShape }
                    "X@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.XShape }

                    "N" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "R" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape
                    }

                    "R@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "Y" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape
                    }

                    "Y@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "a" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.A }
                    "a@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.A }
                    "a@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.A }
                    "a@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.A }
                    "a@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.A }
                    "a@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.A }
                    "a@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.A }
                    "a@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.A }

                    "u" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.U }
                    "u@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.U }
                    "u@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.U }
                    "u@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.U }
                    "u@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.U }
                    "u@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.U }
                    "u@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.U }
                    "u@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.U }

                    "g" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.G }
                    "g@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.G }
                    "g@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.G }
                    "g@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.G }
                    "g@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.G }
                    "g@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.G }
                    "g@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.G }
                    "g@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.G }

                    "c" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.C }
                    "c@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.C }
                    "c@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.C }
                    "c@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.C }
                    "c@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.C }
                    "c@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.C }
                    "c@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.C }
                    "c@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.C }

                    "x" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.X }
                    "x@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.X }
                    "x@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.X }
                    "x@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.X }
                    "x@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.X }
                    "x@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.X }
                    "x@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.X }
                    "x@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.X }

                    "n" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "r" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G
                    }

                    "r@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "y" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C
                    }

                    "y@helix" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@junction" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Helix }

                    "single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.SingleStrand }

                    "junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Junction }
                    "apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop }
                    "inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop }
                    "3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay }
                    "4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay }

                    "secondary_interaction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.SecondaryInteraction }

                    "tertiary_interaction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.TertiaryInteraction }

                    "phosphodiester_bond" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@helix" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Helix && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@junction" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@apical_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@inner_loop" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@3_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@4_way" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@single_strand" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.PhosphodiesterBond }

                    "interaction_symbol" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.InteractionSymbol }

                    "pknot" -> { el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true && el.type == SecondaryStructureType.PKnot }
                    else -> { _: DrawingElement, _: Location? -> false } //unknown, nothing can be selected
                })
            }
        } ?: run {
            selectors.add({ el: DrawingElement, l: Location? -> l?.contains(el.location) ?: true }) //no type? Any element is selected (and has to be in the location if the user set one)
        }

        return userDefinedLocation?.let {
            { el: DrawingElement -> selectors.any { it(el, userDefinedLocation) } }
        } ?: run {
            var defaultLocation = this.locationBuilder.build()
            if (this.filtered) { //if we have some data and the user filtered them...
                defaultLocation?.let { loc -> //if a location has been defined, only the positions in the filtered data are conserved
                    val dataInSelectedLocation = mutableListOf<Int>()
                    this.data.keys.forEach {
                        val pos = it.toInt()
                        if (loc.contains(pos))
                            dataInSelectedLocation.add(pos)
                    }
                    defaultLocation = Location(dataInSelectedLocation.sorted().toIntArray())
                } ?: run { //if no location has been defined, all the positions in the filtered data are conserved
                    defaultLocation = Location(this.data.keys.map { it.toInt() }.toIntArray())
                }
            }
            { el: DrawingElement -> selectors.any { it(el, defaultLocation) } }
        }
    }

}

class SchemeBuilder(data: MutableMap<String, Double>, schemeName:String) : ThemeConfigurationBuilder(data) {
    val dslProperty = StringProperty("scheme",schemeName)

}

class DetailsBuilder(data: MutableMap<String, Double>, detailsLvl:Int) : ThemeConfigurationBuilder(data) {
    val dslProperty = Property("details", "$detailsLvl")

}

class ColorBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = ColorEl()
        get() {
            value?.let {
                field.setValue(it)
            }
            to?.let {
                field.setTo(it)
            }
            type?.let {
                field.setType(it)
            }
            this.locationBuilder.dslElement?.let {
                field.children.add(it)
            }
            return field
        }
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

}

class LineBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = LineEl()
        get() {
            field.setValue(value)

            this.locationBuilder.dslElement?.let {
                field.children.add(it)
            }
            return field
        }
    var value = 2.0
}

class ShowBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = ShowEl()
        get() {
            this.locationBuilder.dslElement?.let {
                field.children.add(it)
            }
            return field
        }
}

class HideBuilder(data: MutableMap<String, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = HideEl()
        get() {
            this.locationBuilder.dslElement?.let {
                field.children.add(it)
            }
            return field
        }
}

class LocationBuilder {

    val dslElement: LocationEl?
        get() {
            return if (blocks.isEmpty())
                null
            else {
                val locationEl = LocationEl()
                blocks.forEach { (start, end) ->
                    locationEl.addBlock(start, end)
                }
                locationEl
            }

        }
    val blocks = mutableMapOf<Int, Int>()

    infix fun Int.to(i: Int) {
        blocks[this] = i
    }

    fun build(): Location? = if (isEmpty())
        null
    else
        Location(this.blocks.map { "${it.key}:${it.value - it.key + 1}" }.joinToString(","))

    fun isEmpty(): Boolean {
        return this.blocks.isEmpty()
    }

}

fun ss(setup: SecondaryStructureBuilder.() -> Unit) = SecondaryStructureBuilder().apply { setup() }.build()

fun bn(setup: BracketNotationBuilder.() -> Unit) = BracketNotationBuilder().apply { setup() }.build()

fun booquet(setup: BooquetBuilder.() -> Unit) = BooquetBuilder().apply { setup() }.build()

fun rnartist(setup: RNArtistBuilder.() -> Unit) = RNArtistBuilder().apply { setup() }.build()

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
