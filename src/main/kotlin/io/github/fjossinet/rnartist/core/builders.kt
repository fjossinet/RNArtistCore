package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.*
import io.github.fjossinet.rnartist.core.model.*
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.Exception
import kotlin.random.Random

class RNArtistBuilder {
    private val rnartistElement = RNArtistEl()
    private val outputFileBuilders = mutableListOf<OutputFileBuilder>()
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    var theme: Theme? = null
    var data: MutableMap<Int, Double> = mutableMapOf()
    private var layout: Layout? = null

    fun build(): Pair<List<SecondaryStructureDrawing>, RNArtistEl> {
        val drawings = mutableListOf<SecondaryStructureDrawing>()
        var issues = 0
        this.secondaryStructures.forEachIndexed { _, ss ->
            try {
                val alternatives = mutableListOf<SecondaryStructureDrawing>()
                val outIdsForLongest = listOf(
                    ConnectorId.n,
                    ConnectorId.nne,
                    ConnectorId.nnw,
                    ConnectorId.ne,
                    ConnectorId.nw,
                    ConnectorId.ene,
                    ConnectorId.wnw,
                    ConnectorId.e,
                    ConnectorId.w,
                    ConnectorId.ese,
                    ConnectorId.wsw,
                    ConnectorId.se,
                    ConnectorId.sw,
                    ConnectorId.sse,
                    ConnectorId.ssw,
                    ConnectorId.s
                )

                FOR@ for (outIdLongest in outIdsForLongest) {
                    val lastReferenceDrawing = SecondaryStructureDrawing(ss, outIdForLongest = { outIdLongest }, layout = this.layout/*the layout stored in the script, if any, has the priority*/)
                    alternatives.add(lastReferenceDrawing)
                    lastReferenceDrawing.computeOverlapping()
                    if  (lastReferenceDrawing.overlappingScore == 0)
                        break@FOR
                    for (backward in 0..4) {
                        var junctionsToImprove = lastReferenceDrawing.junctionsToImprove(backward)
                        if (junctionsToImprove.isNotEmpty()) {
                            val innerLoopsToImprove =
                                junctionsToImprove.filter { it.junctionType == JunctionType.InnerLoop }
                            val targetedBehaviours = innerLoopsToImprove.map {
                                Pair(
                                    it.junction.location,
                                    { _: JunctionDrawing, _: Int, outIdLongest: ConnectorId -> outIdLongest })
                            }
                            var index = outIdsForLongest.indexOf(outIdLongest) + 1
                            while (index <= outIdsForLongest.size - 1) {
                                val drawingAttempt = SecondaryStructureDrawing(
                                    ss,
                                    outIdForLongest = {
                                        if (junctionsToImprove.any { junctionToImprove ->
                                                junctionToImprove.junction.location.contains(
                                                    it
                                                )
                                            })
                                            outIdsForLongest.get(index)
                                        else
                                            outIdLongest
                                    },
                                    targetedJunctionBehaviors = targetedBehaviours,
                                    layout = this.layout //the layout stored in the script, if any, has the priority
                                )
                                alternatives.add(drawingAttempt)
                                drawingAttempt.computeOverlapping()
                                if (drawingAttempt.overlappingScore == 0)
                                    break@FOR
                                index++
                            }
                        }
                    }
                }
                var bestDrawing = alternatives.sortedBy { it.overlappingScore }.first()
                bestDrawing.computeOverlapping()

                //with the junction layouts (computed or defined in the script), the junctions have pushed the branches at the right places.But if some branches were described in the script we apply them. Can be useful if the script wanted to have a branch at a different location that the one computed
                layout?.let {
                    bestDrawing.branches.forEach { branch ->
                        branch.applyLayout(layout = it)
                    }
                }

                this.theme?.let { theme ->
                    bestDrawing.applyTheme(theme)
                }

                this.outputFileBuilders.add(KtsScriptBuilder(layout == null)) //this output builder needs to be at the end to be sure to save in the script all the other output builders

                //at this point all the junctions have their layout (computed or defined in the script). We can store them DSLELement tree in order to not recompute them during the next loads
                if (layout == null) // but only if wa had no layout defined in the script otherwise we keep it
                    rnartistElement.addLayout(bestDrawing.getLayoutEl())

                this.outputFileBuilders.forEach { outputFileBuilder ->
                    outputFileBuilder.build(bestDrawing, this.rnartistElement)
                }

                drawings.add(bestDrawing)

            }
            catch (e: CoreException) {
                println(e.message)
                issues++
            }
            catch (e: Exception) {
                e.printStackTrace()
                issues++
            }
        }
        if (issues > 0)
            println("!!!!!!! $issues drawings with issues !!!!!!!!!!!")
        return Pair(drawings, rnartistElement)
    }

    fun ss(setup: SecondaryStructureBuilder.() -> Unit) {
        val secondaryStructureBuilder = SecondaryStructureBuilder()
        secondaryStructureBuilder.setup()
        secondaryStructures.addAll(secondaryStructureBuilder.build())
        this.rnartistElement.addSS(secondaryStructureBuilder.dslElement)
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
        this.rnartistElement.addData(dataBuilder.dslElement)
    }

    //############ outputs #####################

    fun svg(setup: SVGBuilder.() -> Unit) {
        val svgOutputBuilder = SVGBuilder()
        svgOutputBuilder.setup()
        this.outputFileBuilders.add(svgOutputBuilder)
    }

    fun png(setup: PNGBuilder.() -> Unit) {
        val pngOutputBuilder = PNGBuilder()
        pngOutputBuilder.setup()
        this.outputFileBuilders.add(pngOutputBuilder)
    }

    fun traveler(setup: TravelerBuilder.() -> Unit) {
        val travelerBuilder = TravelerBuilder()
        travelerBuilder.setup()
        this.outputFileBuilders.add(travelerBuilder)
    }

    fun chimera(setup: ChimeraBuilder.() -> Unit) {
        val chimeraOutputBuilder = ChimeraBuilder()
        chimeraOutputBuilder.setup()
        this.outputFileBuilders.add(chimeraOutputBuilder)
    }

    fun blender(setup: BlenderBuilder.() -> Unit) {
        val blenderOutputBuilder = BlenderBuilder()
        blenderOutputBuilder.setup()
        this.outputFileBuilders.add(blenderOutputBuilder)
    }

}

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
            rnaBuilder.build()?.let { rna ->
                val ss = SecondaryStructure(rna, helices = helices, source = PartsSource())
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
    var name: String = "A"
    var seq: String? = null
    var value: String = "(((...)))"
    val dslElement = BracketNotationEl()
        get() {
            field.setName(this.name)
            field.setValue(this.value)
            this.seq?.let {
                field.setSeq(it)
            }
            return field
        }

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
        ss.randomizeSeq() //to have now a seq that fits the structural constraints
        this.seq = ss.rna.seq
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
            this.dslElement.addBracketNotation(bnBuilder.dslElement)
        }
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
        this.dslElement.addBPSeq(bpSeqBuilder.dslElement)
    }

    fun ct(setup: CTBuilder.() -> Unit) {
        val ctBuilder = CTBuilder()
        ctBuilder.setup()
        secondaryStructures.addAll(ctBuilder.build())
        this.dslElement.addCT(ctBuilder.dslElement)
    }

    fun pdb(setup: PDBBuilder.() -> Unit) {
        val pdbBuilder = PDBBuilder()
        pdbBuilder.setup()
        val result = pdbBuilder.build()
        secondaryStructures.addAll(result)
        this.dslElement.addPDB(pdbBuilder.dslElement)
    }

    fun stockholm(setup: StockholmBuilder.() -> Unit) {
        val stockholmBuilder = StockholmBuilder()
        stockholmBuilder.setup()
        secondaryStructures.addAll(stockholmBuilder.build())
        this.dslElement.addStockholm(stockholmBuilder.dslElement)
    }

    fun rnacentral(setup: RNACentralBuilder.() -> Unit) {
        val rnaCentralBuilder = RNACentralBuilder()
        rnaCentralBuilder.setup()
        secondaryStructures.addAll(rnaCentralBuilder.build())
        this.dslElement.addRnaCentral(rnaCentralBuilder.dslElement)
    }

    fun rfam(setup: RfamBuilder.() -> Unit) {
        val rfamBuilder = RfamBuilder()
        rfamBuilder.setup()
        secondaryStructures.addAll(rfamBuilder.build())
        this.dslElement.addRfam(rfamBuilder.dslElement)
    }

}

abstract class OutputFileBuilder {
    abstract val dslElement: DSLElement
    var path: String? = null
    var name: String? = null
    var locationBuilder: LocationBuilder = LocationBuilder()

    /**
     * @param [drawing] the SecondaryStructureDrawing to be saved
     */
    abstract fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl)

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    open protected fun getOutputFile(drawing: SecondaryStructureDrawing, suffix:String):File? {
        path?.let { path ->
            val fileName =
                drawing.secondaryStructure.source?.let { source ->
                    when (source) {
                        is DatabaseSource -> {
                            when (source) {
                                is RfamSource -> {
                                    "${source.getId()}_${drawing.secondaryStructure.name.replace("/", "_")}"//since we can have several rnas in an entry
                                }
                                is PDBSource -> {
                                    "${source.getId()}_${drawing.secondaryStructure.name}"//since we can have several rnas in an entry
                                }
                                is RnaCentralSource -> {
                                    source.getId()//since we can only have a single rna in an entry
                                }
                                else -> {
                                    source.getId()
                                }
                            }

                        }
                        is FileSource -> {
                            //a file name can contain a dot
                            val tokens = source.getId().split("/").last().split(".")
                            val fileName = tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                            if (source.getId().endsWith(".pdb")) {
                                "${fileName}_${drawing.secondaryStructure.name}"//since we can have several rnas in the file
                            }
                            else if (source.getId().endsWith(".sto") || source.getId().endsWith(".stk") || source.getId().endsWith(".stockholm")) {
                                "${fileName}_${drawing.secondaryStructure.name.replace("/", "_")}"//since we can have several rnas in the file
                            }
                            //for vienna, we restrict it to a single molecule (if used as an alignment, rather use stockholm)
                            else  {
                                fileName
                            }
                        }

                        is BracketNotation -> {
                            drawing.secondaryStructure.name
                        }

                        is PartsSource -> {
                            drawing.secondaryStructure.name
                        }

                        else -> {
                            null
                        }

                    }

                }
            var f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File("${path}/${fileName}.$suffix")
            else
                File("${Jar().path()}/${path}/${fileName}.$suffix")
            if (!f.parentFile.exists())
                f.parentFile.mkdirs()
            try {
                f.createNewFile()
            } catch(e:java.io.IOException) {
                throw CoreException("Cannot create output file ${f.path}")
            }
            return f
        }
        return null
    }
}

class KtsScriptBuilder(val noLayoutInFormerScript:Boolean):OutputFileBuilder() {
    override val dslElement = object: DSLElement(name="") {} //useless
        get() = field

    override protected fun getOutputFile(drawing: SecondaryStructureDrawing, suffix:String):File? {
        drawing.secondaryStructure.source?.let { source ->
            when (source) {
                is FileSource -> {
                    //a file name can contain a dot
                    val tokens = source.getId().split("/").last().split(".")
                    var fileName = tokens.subList(0, tokens.size - 1).joinToString(separator = ".")
                    if (source.getId().endsWith(".pdb")) {
                        fileName = "${fileName}_${drawing.secondaryStructure.name}"//since we can have several rnas in the file
                    }
                    else if (source.getId().endsWith(".sto") || source.getId().endsWith(".stk") || source.getId().endsWith(".stockholm")) {
                        fileName = "${fileName}_${drawing.secondaryStructure.name.replace("/", "_")}"//since we can have several rnas in the file
                    }
                    //for vienna, we restrict it to a single molecule (if used as an alignment, rather use stockholm), so we keep the filename without appending RNA name
                    return File(File(source.getId()).parentFile,"${fileName}.$suffix")
                }

                else -> {
                    null
                }

            }

        }
        return null
    }

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        getOutputFile(drawing, "kts")?.let { f ->
            if (!f.exists() || noLayoutInFormerScript) { //if the script with the name we want doesn't exist or did not contain any Layout
                f.createNewFile()
                //since the name of the script will contains the name of the 2D (so the RNA chain name) for PDB and Stockholm files,
                //this information needs to be added to the input element to focus the script on this RNA chain only
                rnartistEl.getSSOrNull()?.let { ssEl ->
                    ssEl.getPDBOrNull()?.let {
                        it.setName(drawing.secondaryStructure.name)
                    }
                    ssEl.getStockholmOrNull()?.let {
                        it.setName(drawing.secondaryStructure.name)
                    }
                }
                f.writeText(rnartistEl.dump().toString())
            }
        }
    }
}

abstract class GraphicFileBuilder:OutputFileBuilder() {
    var width: Double? = null
    var height: Double? = null
}

class PNGBuilder : GraphicFileBuilder() {
    override val dslElement = PNGEl()
        get() {
            this.path?.let { path ->
                field.setPath(
                    if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                        path
                    else
                        "${Jar().path()}/${path}"
                )
            }
            this.name?.let {
                field.setName(it)
            }
            this.width?.let {
                field.setWidth(it)
            }
            this.height?.let {
                field.setHeight(it)
            }

            return field
        }

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        getOutputFile(drawing, "png")?.let { f ->
            locationBuilder.build()?.let { location ->
                drawing.getFrame(location)?.let { selectionFrame ->
                    drawing.asPNG(
                        frame = Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, height ?: drawing.getFrame()!!.height*1.1),
                        selectionFrame = selectionFrame,
                        outputFile = f
                    )
                }
            } ?: run {
                drawing.asPNG(
                    frame = Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, height ?: drawing.getFrame()!!.height*1.1),
                    outputFile = f
                )
            }
        }
        rnartistEl.addPNG(this.dslElement)
    }

}

class SVGBuilder : GraphicFileBuilder() {

    override val dslElement = SVGEl()
        get() {
            this.path?.let {
                field.setPath(it)
            }
            this.name?.let {
                field.setName(it)
            }
            this.width?.let {
                field.setWidth(it)
            }
            this.height?.let {
                field.setHeight(it)
            }
            return field
        }

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        getOutputFile(drawing, "svg")?.let { f ->
            locationBuilder.build()?.let { location ->
                drawing.getFrame(location)?.let { selectionFrame ->
                    drawing.asSVG(
                        frame = Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, height ?: drawing.getFrame()!!.height*1.1),
                        selectionFrame = selectionFrame,
                        outputFile = f
                    )
                }
            } ?: run {
                drawing.fitViewTo(Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, (height ?: drawing.getFrame()!!.height*1.1)-60))

                drawing.asSVG(
                    frame = Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, height ?: drawing.getFrame()!!.height*1.1),
                    outputFile = f
                )
            }
        }
        rnartistEl.addSVG(this.dslElement)
    }

}

class TravelerBuilder:GraphicFileBuilder() {
    override val dslElement = TravelerEl()
        get() {
            this.path?.let { path ->
                field.setPath(
                    if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                        path
                    else
                        "${Jar().path()}/${path}"
                )
            }
            this.name?.let {
                field.setName(it)
            }
            this.width?.let {
                field.setWidth(it)
            }
            this.height?.let {
                field.setHeight(it)
            }
            return field
        }

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        getOutputFile(drawing, "traveler")?.let { f ->
            drawing.fitViewTo(Rectangle2D.Double(0.0, 0.0, width ?: drawing.getFrame()!!.width*1.1, height ?: drawing.getFrame()!!.height*1.1))
            val builder = StringBuilder()
            builder.appendLine("<structure>")
            val at = AffineTransform()
            at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
            at.scale(drawing.workingSession.zoomLevel, drawing.workingSession.zoomLevel)
            drawing.residues.sortedBy { it.absPos }.forEach {
                val _c = at.createTransformedShape(it.circle)
                builder.appendLine("<point x=\"${"%.2f".format(Locale.ENGLISH, _c.bounds2D.centerX)}\" y=\"${"%.2f".format(Locale.ENGLISH, _c.bounds2D.centerY)}\" b=\"${it.name}\"/>")
            }
            builder.appendLine("</structure>")
            f.writeText(builder.toString())
        }
        rnartistEl.addTraveler(this.dslElement)
    }
}

class ChimeraBuilder: OutputFileBuilder() {
    override val dslElement: DSLElement
        get() = TODO("Not yet implemented")

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.cxc")
            else
                File("${Jar().path()}/${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.cxc")
            drawing.secondaryStructure.tertiaryStructure?.let {
                drawing.asChimeraScript(f)
            }
        }
    }

}

class BlenderBuilder: OutputFileBuilder() {
    override val dslElement: DSLElement
        get() = TODO("Not yet implemented")

    override fun build(drawing: SecondaryStructureDrawing, rnartistEl:RNArtistEl) {
        path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File("${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.py")
            else
                File("${Jar().path()}/${path}/${drawing.secondaryStructure.rna.name.replace("/", "_")}.py")
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

    val dslElement = PDBEl()
        get() {
            this.file?.let {
                field.setFile(it)
            }
            this.name?.let {
                field.setName(it)
            }
            this.id?.let {
                field.setId(it)
            }
            return field
        }
    var file: String? = null
    var path: String? = null
    var name: String? = null
    var id: String? = null

    fun build(): List<SecondaryStructure> {
        val structures = mutableListOf<SecondaryStructure>()
        if (this.id != null) {
            val pdbFile = File.createTempFile(this.id!!, ".pdb")
            pdbFile.writeText(PDB().getEntry(this.id!!).readText())
            this.file = pdbFile.invariantSeparatorsPath
        }
        this.file?.let { file ->
            val f = if (file.startsWith("/") || file.matches(Regex("^[A-Z]:/.+$")))
                File(file)
            else
                File("${Jar().path()}/${file}")
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
        this.path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$"))) {
                File(path)
            } else
                File("${Jar().path()}/${path}")
            f.listFiles { _, name -> name.endsWith(".pdb") }?.forEach { pdbFile ->
                val _structures = Annotate3D().annotate(pdbFile)
                _structures.forEach {
                    it.source = if (this.id != null) PDBSource(this.id!!) else FileSource(pdbFile.invariantSeparatorsPath)
                }
                structures.addAll(_structures)
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
            this.path?.let {
                field.setPath(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val f = if (file.startsWith("/") || file.matches(Regex("^[A-Z]:/.+$")))
                File(file)
            else
                File("${Jar().path()}/${file}")
            val ss = parseVienna(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)
        }
        this.path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$"))) {
                File(path)
            } else
                File("${Jar().path()}/${path}")
            val structures = mutableListOf<SecondaryStructure>()
            f.listFiles { _, name -> name.endsWith(".vienna") }?.forEach { viennaFile ->
                val ss = parseVienna(FileReader(viennaFile))
                ss.source = FileSource(viennaFile.invariantSeparatorsPath)
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
            this.path?.let {
                field.setPath(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val f = if (file.startsWith("/") || file.matches(Regex("^[A-Z]:/.+$")))
                File(file)
            else
                File("${Jar().path()}/${file}")
            val ss = parseBPSeq(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)
        }
        this.path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File(path)
            else
                File("${Jar().path()}/${path}")

            val structures = mutableListOf<SecondaryStructure>()
            f.listFiles { _, name -> name.endsWith(".bpseq") }?.forEach { bpseqFile ->
                val ss = parseBPSeq(FileReader(bpseqFile))
                ss.source = FileSource(bpseqFile.invariantSeparatorsPath)
                structures.add(ss)
            }
            return structures
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
            this.path?.let {
                field.setPath(it)
            }
            return field
        }

    override fun build(): List<SecondaryStructure> {
        this.file?.let { file ->
            val f = if (file.startsWith("/") || file.matches(Regex("^[A-Z]:/.+$")))
                File(file)
            else
                File("${Jar().path()}/${file}")
            val ss = parseCT(FileReader(f))
            ss.source = FileSource(file)
            return arrayListOf(ss)
        }
        this.path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File(path)
            else
                File("${Jar().path()}/${path}")
            val structures = mutableListOf<SecondaryStructure>()
            f.listFiles { _, name -> name.endsWith(".ct") }?.forEach { ctFile ->
                val ss = parseCT(FileReader(ctFile))
                ss.source = FileSource(ctFile.invariantSeparatorsPath)
                structures.add(ss)
            }
            return structures
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
            this.path?.let {
                field.setPath(it)
            }
            this.name?.let {
                field.setName(it)
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
            val f = if (file.startsWith("/") || file.matches(Regex("^[A-Z]:/.+$")))
                File(file)
            else
                File("${Jar().path()}/${file}")
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
        this.path?.let { path ->
            val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                File(path)
            else
                File("${Jar().path()}/${path}")
            val structures = mutableListOf<SecondaryStructure>()
            f.listFiles { _, name -> name.endsWith(".sto") }?.forEach { stoFile ->
                val secondaryStructures = parseStockholm(FileReader(stoFile), withConsensus2D = true).third
                secondaryStructures.forEach {
                    it.source = FileSource(stoFile.invariantSeparatorsPath)
                    it.rna.useAlignmentNumberingSystem = useAlignmentNumbering
                }
                structures.addAll(secondaryStructures)
            }
            return structures
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

    val dslElement = RfamEl()
        get() {
            this.id?.let {
                field.setId(it)
            }
            this.name?.let {
                field.setId(it)
            }
            return field
        }
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

    val dslElement = RnaCentralEl()
        get() {
            this.id?.let {
                field.setId(it)
            }
            return field
        }

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
                val f = if (path.startsWith("/") || path.matches(Regex("^[A-Z]:/.+$")))
                    File("${path}/${ss.rna.name.replace("/", "_")}.svg")
                else
                    File("${Jar().path()}/${path}/${ss.rna.name.replace("/", "_")}.svg")
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

class DataBuilder {

    val dslElement: DataEl?
        get() {
            return if (data.isEmpty())
                null
            else {
                val dataEl = DataEl()
                data.forEach { (pos, value) ->
                    dataEl.addValue(pos, value)
                }
                dataEl
            }

        }
    var file: String? = null
        set(value) {
            field = value
            File(value).readLines().forEach {
                val tokens = it.split(" ")
                data[tokens[0].toInt()] = tokens[1].toDouble()
            }
        }
    var data = mutableMapOf<Int, Double>()

    infix fun Int.to(i: Double) {
        data[this] = i
    }

}

class LayoutBuilder {
    private val junctionLayoutBuilders = mutableListOf<JunctionLayoutBuilder>()
    private val branchLayoutBuilders = mutableListOf<BranchLayoutBuilder>()
    var alternatives = false
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

    fun branch(setup: BranchLayoutBuilder.() -> Unit) {
        val branchLayoutBuilder = BranchLayoutBuilder()
        branchLayoutBuilder.setup()
        this.dslElement.addBranch(branchLayoutBuilder.dslElement)
        this.branchLayoutBuilders.add(branchLayoutBuilder)
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
            if (!junctionLayoutBuilder.isGlobalLayout) {
                //we can have no parameters to set (radius, out_ids), this will indicate to the drawing engine to come back to the initial values for this junction
                if (junctionLayoutBuilder.radius == null && junctionLayoutBuilder.out_ids == null)
                    layout.addConfigurationFor(selection, LayoutProperty.rollBack, null)
                else {//the layout targets specific junctions
                    junctionLayoutBuilder.radius?.let { radius ->
                        layout.addConfigurationFor(selection, LayoutProperty.radius, radius.toString())
                    }
                    junctionLayoutBuilder.out_ids?.let { out_ids ->
                        layout.addConfigurationFor(selection, LayoutProperty.out_ids, out_ids)
                    }
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
                        junctionsBehaviors[junctionType] =
                            { junctionDrawing: JunctionDrawing, helixRank: Int, outIdForLongest: ConnectorId ->
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
        branchLayoutBuilders.forEach { branchLayoutBuilder ->
            layout.addConfigurationFor(
                branchLayoutBuilder.buildSelection(),
                LayoutProperty.branch,
                branchLayoutBuilder.value.toString()
            )
        }
        return layout
    }

}

class BranchLayoutBuilder {
    val dslElement = BranchEl()
        get() {
            value?.let {
                field.setValue(it)
            }
            this.locationBuilder.dslElement?.let {
                field.addLocation(it)
            }
            step?.let {
                field.setStep(it)
            }
            return field
        }
    var value: Double? = null
    var step: Int? = null
    private val locationBuilder = LocationBuilder()

    fun location(setup: LocationBuilder.() -> Unit) {
        this.locationBuilder.setup()
    }

    fun buildSelection(): (el: DrawingElement) -> Boolean {
        val location = this.locationBuilder.build()
        return { el: DrawingElement ->
            location!!.contains(
                el.location
            )
        }
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
                field.addLocation(it)
            }
            step?.let {
                field.setStep(it)
            }
            return field
        }

    var name: String? = null
    private val locationBuilder = LocationBuilder()
    var type: Int? = null
    var out_ids: String? = null
    var radius: Double? = null
    var step: Int? = null
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

class ThemeBuilder(data: MutableMap<Int, Double> = mutableMapOf()) {
    private val themeConfigurationBuilders = mutableListOf<ThemeConfigurationBuilder>()
    private val data = data.toMutableMap()
    val dslElement = ThemeEl()
        get() {
            return field
        }

    fun details(setup: DetailsBuilder.() -> Unit) {
        val detailsBuilder = DetailsBuilder(this.data)
        detailsBuilder.setup()
        this.themeConfigurationBuilders.add(detailsBuilder)
        this.dslElement.addDetails(detailsBuilder.dslElement)
    }

    fun scheme(setup: SchemeBuilder.() -> Unit) {
        val schemeBuilder = SchemeBuilder(this.data)
        schemeBuilder.setup()
        this.themeConfigurationBuilders.add(schemeBuilder)
        this.dslElement.addScheme(schemeBuilder.dslElement)
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
        this.dslElement.addLine(lineBuilder.dslElement)
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
                    when (configurationBuilder.value?.toInt()) {
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
                                listOf(
                                    SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond
                                )
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
                                    SecondaryStructureType.XShape
                                )
                            )
                        }

                        3 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(
                                    SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape
                                )
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(
                                    SecondaryStructureType.InteractionSymbol,
                                    SecondaryStructureType.A,
                                    SecondaryStructureType.U,
                                    SecondaryStructureType.G,
                                    SecondaryStructureType.C,
                                    SecondaryStructureType.X
                                )
                            )
                        }

                        4 -> {
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(
                                    SecondaryStructureType.Helix,
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
                                    SecondaryStructureType.X
                                )
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
                    if ("Structural Domains" == configurationBuilder.value) {
                        val randomColors = (1..20).map { getHTMLColorString(randomColor()) }
                        t.addConfiguration(
                            ThemeProperty.color,
                            { el ->
                                when (el) {
                                    is HelixDrawing -> randomColors.get(Random.nextInt(0, 19))
                                    is JunctionDrawing -> randomColors.get(Random.nextInt(0, 19))
                                    is SingleStrandDrawing -> randomColors.get(Random.nextInt(0, 19))
                                    is SecondaryInteractionDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is InteractionSymbolDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is ResidueDrawing -> getHTMLColorString(el.parent!!.getColor())
                                    is PhosphodiesterBondDrawing -> el.parent?.let {
                                        getHTMLColorString(it.getColor())
                                    } ?: run {
                                        getHTMLColorString(el.getColor())
                                    }

                                    is ResidueLetterDrawing -> getHTMLColorString(Color.WHITE)
                                    else -> getHTMLColorString(Color.RED)
                                }
                            },
                            SecondaryStructureType.entries
                        )
                    } else
                        RnartistConfig.colorSchemes.get(configurationBuilder.value)?.let { scheme ->
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
                                val min = configurationBuilder.minValue ?: configurationBuilder.data.values.minOrNull()
                                val max = configurationBuilder.maxValue ?: configurationBuilder.data.values.maxOrNull()
                                configurationBuilder.to?.let { to ->
                                    val toColor = getAWTColor(to)
                                    if (min != max) {
                                        val p = (value - min!!) / (max!! - min)
                                        val r = fromColor.red * (1 - p) + toColor.red * p
                                        val g = fromColor.green * (1 - p) + toColor.green * p
                                        val b = fromColor.blue * (1.toFloat() - p) + toColor.blue * p

                                        //println("$position $value $p ${getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt()))}")

                                        val colorCharacter = if (p < 0.5) Color.BLACK else Color.WHITE

                                        configurationBuilder.locationBuilder.build()?.let { loc ->
                                            //if a location has been defined, a configuration is added only if the position of the residue is inside this location
                                            if (loc.contains(position.toInt())) {
                                                t.addConfiguration(
                                                    configurationBuilder.buildSelection(Location(position)),
                                                    ThemeProperty.color,
                                                    { getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt())) })

                                                t.addConfiguration(
                                                    {e:DrawingElement -> (e.type == SecondaryStructureType.A || e.type == SecondaryStructureType.U || e.type == SecondaryStructureType.G || e.type == SecondaryStructureType.C || e.type == SecondaryStructureType.X) && e.inside(Location(position))},
                                                    ThemeProperty.color,
                                                    { getHTMLColorString(colorCharacter) })

                                            }
                                        } ?: run {
                                            t.addConfiguration(
                                                configurationBuilder.buildSelection(Location(position)),
                                                ThemeProperty.color,
                                                { getHTMLColorString(Color(r.toInt(), g.toInt(), b.toInt())) })

                                            t.addConfiguration(
                                                {e:DrawingElement -> (e.type == SecondaryStructureType.A || e.type == SecondaryStructureType.U || e.type == SecondaryStructureType.G || e.type == SecondaryStructureType.C || e.type == SecondaryStructureType.X) && e.inside(Location(position))},
                                                ThemeProperty.color,
                                                { getHTMLColorString(colorCharacter) })
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

open class ThemeConfigurationBuilder(data: MutableMap<Int, Double>) {
    val locationBuilder = LocationBuilder()
    var type: String? = null
    var step: Int? = null
    val data = data.toMutableMap()
    var filtered = false
    var minValue:Double? = null
    var maxValue:Double? = null

    infix fun MutableMap<Int, Double>.gt(min: Double) {
        val excluded = this.filter { it.value <= min }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
        minValue = min
    }

    infix fun MutableMap<Int, Double>.lt(max: Double) {
        val excluded = this.filter { it.value >= max }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
        maxValue = max
    }

    infix fun MutableMap<Int, Double>.eq(value: Double) {
        val excluded = this.filter { it.value != value }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
    }

    infix fun MutableMap<Int, Double>.between(range: ClosedFloatingPointRange<Double>) {
        val excluded = this.filter { it.value !in range }
        excluded.forEach {
            remove(it.key)
        }
        filtered = true
        minValue = range.start
        maxValue = range.endInclusive
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
                    "A" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.AShape }
                    "A@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.AShape }
                    "A@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.AShape }
                    "A@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.AShape }
                    "A@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.AShape }
                    "A@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.AShape }
                    "A@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.AShape }
                    "A@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.AShape }

                    "U" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.UShape }
                    "U@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.UShape }
                    "U@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.UShape }
                    "U@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.UShape }
                    "U@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.UShape }
                    "U@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.UShape }
                    "U@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.UShape }
                    "U@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.UShape }

                    "G" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.GShape }
                    "G@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.GShape }
                    "G@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.GShape }
                    "G@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.GShape }
                    "G@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.GShape }
                    "G@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.GShape }
                    "G@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.GShape }
                    "G@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.GShape }

                    "C" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.CShape }
                    "C@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.CShape }
                    "C@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.CShape }
                    "C@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.CShape }
                    "C@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.CShape }
                    "C@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.CShape }
                    "C@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.CShape }
                    "C@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.CShape }

                    "X" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.XShape }
                    "X@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.XShape }
                    "X@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.XShape }
                    "X@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.XShape }
                    "X@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.XShape }
                    "X@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.XShape }
                    "X@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.XShape }
                    "X@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.XShape }

                    "N" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "N@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.GShape ||
                                el.type == SecondaryStructureType.CShape ||
                                el.type == SecondaryStructureType.XShape)
                    }

                    "R" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape
                    }

                    "R@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "R@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.AShape ||
                                el.type == SecondaryStructureType.GShape)
                    }

                    "Y" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape
                    }

                    "Y@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "Y@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.UShape ||
                                el.type == SecondaryStructureType.CShape)
                    }

                    "a" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.A }
                    "a@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.A }
                    "a@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.A }
                    "a@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.A }
                    "a@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.A }
                    "a@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.A }
                    "a@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.A }
                    "a@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.A }

                    "u" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.U }
                    "u@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.U }
                    "u@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.U }
                    "u@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.U }
                    "u@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.U }
                    "u@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.U }
                    "u@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.U }
                    "u@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.U }

                    "g" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.G }
                    "g@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.G }
                    "g@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.G }
                    "g@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.G }
                    "g@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.G }
                    "g@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.G }
                    "g@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.G }
                    "g@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.G }

                    "c" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.C }
                    "c@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.C }
                    "c@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.C }
                    "c@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.C }
                    "c@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.C }
                    "c@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.C }
                    "c@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.C }
                    "c@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.C }

                    "x" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.X }
                    "x@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && el.type == SecondaryStructureType.X }
                    "x@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.X }
                    "x@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.X }
                    "x@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.X }
                    "x@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.X }
                    "x@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.X }
                    "x@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.X }

                    "n" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "n@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.G ||
                                el.type == SecondaryStructureType.C ||
                                el.type == SecondaryStructureType.X)
                    }

                    "r" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G
                    }

                    "r@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "r@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.A ||
                                el.type == SecondaryStructureType.G)
                    }

                    "y" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C
                    }

                    "y@helix" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@junction" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@apical_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@inner_loop" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@3_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@4_way" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "y@single_strand" -> { el: DrawingElement, l: Location? ->
                        l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && (el.type == SecondaryStructureType.U ||
                                el.type == SecondaryStructureType.C)
                    }

                    "helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Helix }

                    "single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.SingleStrand }

                    "junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Junction }
                    "apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop }
                    "inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop }
                    "3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay }
                    "4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay }

                    "secondary_interaction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.SecondaryInteraction }

                    "tertiary_interaction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.TertiaryInteraction }

                    "phosphodiester_bond" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@helix" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Helix && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@junction" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@apical_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@inner_loop" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@3_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@4_way" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay && el.type == SecondaryStructureType.PhosphodiesterBond }
                    "phosphodiester_bond@single_strand" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.parent?.type == SecondaryStructureType.SingleStrand && el.type == SecondaryStructureType.PhosphodiesterBond }

                    "interaction_symbol" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.InteractionSymbol }

                    "pknot" -> { el: DrawingElement, l: Location? -> l?.let { el.inside(it) } ?: run {true} && el.type == SecondaryStructureType.PKnot }
                    else -> { _: DrawingElement, _: Location? -> false } //unknown, nothing can be selected
                })
            }
        } ?: run {
            selectors.add({ el: DrawingElement, l: Location? ->
                l?.let { el.inside(it) } ?: run {true}
            }) //no type? Any element is selected (and has to be in the location if the user set one)
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

class SchemeBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    var value: String? = null
    val dslElement = SchemeEl()
        get() {
            value?.let {
                field.setValue(it)
            }
            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }

            return field
        }
}

class DetailsBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    var value: Int? = null
    val dslElement = DetailsEl()
        get() {
            value?.let {
                field.setValue(it)
            }
            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }

            return field
        }

}

class ColorBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = ColorEl()
        get() {
            value?.let {
                field.setValue(it)
            }

            to?.let {
                field.setTo(it, minValue, maxValue)
            }

            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }

            this.locationBuilder.dslElement?.let {
                field.addLocation(it)
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

class LineBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = LineEl()
        get() {
            field.setValue(value)

            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }

            this.locationBuilder.dslElement?.let {
                field.addLocation(it)
            }
            return field
        }
    var value = 2.0
}

class ShowBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = ShowEl()
        get() {
            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }
            this.locationBuilder.dslElement?.let {
                field.addLocation(it)
            }
            return field
        }
}

class HideBuilder(data: MutableMap<Int, Double>) : ThemeConfigurationBuilder(data) {
    val dslElement = HideEl()
        get() {

            type?.let {
                field.setType(it)
            }

            step?.let {
                field.setStep(it)
            }

            this.locationBuilder.dslElement?.let {
                field.addLocation(it)
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
