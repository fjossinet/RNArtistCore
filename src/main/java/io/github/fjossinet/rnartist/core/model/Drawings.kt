package io.github.fjossinet.rnartist.core.model

import io.github.fjossinet.rnartist.core.model.RnartistConfig.defaultConfiguration
import io.github.fjossinet.rnartist.core.model.RnartistConfig.defaultTheme
import java.awt.*
import java.awt.Color
import java.awt.geom.*
import java.awt.image.BufferedImage
import kotlin.math.hypot

//parameters that can be modified
val radiusConst: Double = 7.0
val spaceBetweenResidues: Double = 5.0
val deltaHelixWidth: Double = 5.0
val deltaPhosphoShift: Double = 0.0
val deltaLWSymbols: Double = 1.2

val minimalCircumference: Float = 360F / ((ConnectorId.values().size) * radiusConst * 3).toFloat()
val minimalRadius: Float = minimalCircumference / (2F * Math.PI).toFloat()
val radiansToDegrees = 180 / Math.PI
val degreesToRadians = Math.PI / 180

fun helixDrawingLength(h: Helix): Double {
    return (h.length - 1).toDouble() * radiusConst * 2.0 + (h.length - 1).toDouble() * spaceBetweenResidues
}

fun helixDrawingWidth(): Double {
    return radiusConst * deltaHelixWidth
}

class Project(var secondaryStructure: SecondaryStructure, var tertiaryStructure: TertiaryStructure?, var theme: Map<String, String>, var graphicsContext: Map<String, String>) {

}

class WorkingSession() {
    var viewX = 0.0
    var viewY = 0.0
    var finalZoomLevel = 1.0
    var screen_capture = false
    var screen_capture_area: Rectangle2D? = null
    val selectedResidues = mutableListOf<ResidueDrawing>()
    val selectionBounds: Rectangle2D?
        get() {
            var selectionBounds: Rectangle2D? = null
            for (element in this.selectedResidues) {
                if (selectionBounds == null)
                    selectionBounds = element.bounds2D
                else
                    selectionBounds = selectionBounds.createUnion(element.bounds2D)
            }
            return selectionBounds
        }
    val selectedAbsPositions: List<Int>
        get() {
            val selectedAbsPositions = mutableListOf<Int>()
            if (this.selectedResidues.isEmpty())
                return selectedAbsPositions
            selectedAbsPositions.addAll(this.selectedResidues.first().location.positions)
            for (i in 1 until this.selectedResidues.size)
                selectedAbsPositions.addAll(this.selectedResidues.get(i).location.positions)
            return selectedAbsPositions.distinct().sorted()
        }

    fun clear() {
        viewX = 0.0
        viewY = 0.0
        finalZoomLevel = 1.0
        screen_capture = false
        screen_capture_area = null
        selectedResidues.clear()
    }

    fun moveView(transX: Double, transY: Double) {
        viewX += transX
        viewY += transY
    }

    fun setZoom(zoomFactor: Double) {
        finalZoomLevel *= zoomFactor
    }
}

fun transparentColor(source: Color, alpha: Int) = Color(source.red, source.green, source.blue, alpha);

@JvmField
var DASHED = "dashed"

@JvmField
var SOLID = "solid"

enum class DrawingConfigurationParameter {
    Color, CharColor, LineWidth, LineShift, Opacity, TertiaryInteractionStyle, FontName, DeltaXRes, DeltaYRes, DeltaFontSize
}


interface DrawingConfigurationListener {

    /**
     * COonfiguration parameter add
     */
    fun configurationChange(parameter: DrawingConfigurationParameter, value: String)
    fun configurationChange(parameter: DrawingConfigurationParameter, value: Color)
    fun configurationChange(parameter: DrawingConfigurationParameter, value: Double)
    fun configurationChange(parameter: DrawingConfigurationParameter, value: Int)

    /**
     * Configuration parameter removal
     */
    fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean = false)

    /**
     * Full Configuration modification
     */
    fun applyConfiguration(drawingConfiguration: DrawingConfiguration)
    fun applyTheme(theme: Map<String, Map<String, String>>)
    fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean = false);
}

interface DrawingConfigurator {
    fun addDrawingConfigurationListener(listener: DrawingConfigurationListener)
    fun removeDrawingConfigurationListener(listener: DrawingConfigurationListener)
    fun removeDrawingConfigurationListeners()

    /**
     * Configuration parameter add
     */
    fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: String)
    fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Int)
    fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Double)
    fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Color)

    /**
     * Configuration parameter removal
     */
    fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, fireChildren: Boolean = false)

    /**
     * Full Configuration modification
     */
    fun applyDrawingConfiguration(drawingConfiguration: DrawingConfiguration)
    fun applyTheme(theme: Map<String, Map<String, String>>)
    fun clearDrawingConfiguration(clearChildrenDrawingConfiguration: Boolean = false)
}

abstract class AbstractDrawingConfigurator() : DrawingConfigurator {

    protected val listeners = mutableListOf<DrawingConfigurationListener>()
    var muted: Boolean = false //to make theme configuration we don't want to fire

    override fun addDrawingConfigurationListener(listener: DrawingConfigurationListener) {
        this.listeners.add(listener)
    }

    override fun removeDrawingConfigurationListener(listener: DrawingConfigurationListener) {
        this.listeners.remove(listener)
    }

    override fun removeDrawingConfigurationListeners() {
        this.listeners.clear()
    }

    override fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: String) {
        this.listeners.forEach {
            it.configurationChange(param, newValue)
        }
    }

    override fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Int) {
        this.listeners.forEach {
            it.configurationChange(param, newValue)
        }
    }

    override fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Double) {
        this.listeners.forEach {
            it.configurationChange(param, newValue)
        }
    }

    override fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, newValue: Color) {
        this.listeners.forEach {
            it.configurationChange(param, newValue)
        }
    }

    override fun fireDrawingConfigurationChange(param: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.listeners.forEach {
            it.configurationChange(param, fireChildren)
        }
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        this.listeners.forEach {
            it.applyTheme(theme)
        }
    }

    override fun applyDrawingConfiguration(drawingConfiguration: DrawingConfiguration) {
        this.listeners.forEach {
            it.applyConfiguration(DrawingConfiguration(defaultParams = drawingConfiguration.params.toMutableMap())) //each listener needs to have its own theme instance to modify them independently afterwards
        }
    }

    override fun clearDrawingConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.listeners.forEach {
            it.clearConfiguration(clearChildrenDrawingConfiguration)
        }
    }

}

class Theme(defaultConfigurations: MutableMap<String, Map<String, String>> = defaultTheme) {

    var configurations: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    fun setConfigurationFor(elementType: SecondaryStructureType? = null, parameter: DrawingConfigurationParameter, parameterValue: String) {
        if (elementType == null)
            this.setConfigurationFor(SecondaryStructureType.Full2D.toString(), parameter.toString(), parameterValue)
        else
            this.setConfigurationFor(elementType.toString(), parameter.toString(), parameterValue)
    }

    private fun setConfigurationFor(elementType: String, parameter: String, parameterValue: String) {
        if (elementType == null) {
            if (!configurations.containsKey(SecondaryStructureType.Full2D.toString()))
                configurations[SecondaryStructureType.Full2D.toString()] = mutableMapOf<String, String>()
            configurations[SecondaryStructureType.Full2D.toString()]!![parameter] = parameterValue
        } else {
            if (!configurations.containsKey(elementType))
                configurations[elementType] = mutableMapOf<String, String>()
            configurations[elementType]!![parameter] = parameterValue
        }
    }

    init {
        for ((elementType, parameters) in defaultConfigurations.entries)
            for ((name, value) in parameters.entries)
                this.setConfigurationFor(elementType = elementType, parameter = name, parameterValue = value)
    }
}

class DrawingConfiguration(defaultParams: MutableMap<String, String> = defaultConfiguration.toMutableMap()) {

    val params: MutableMap<String, String> = mutableMapOf()

    var opacity: Int? = null
        get() = this.params.get(DrawingConfigurationParameter.Opacity.toString())?.toInt()

    var tertiaryInteractionStyle: String? = null
        get() = this.params.get(DrawingConfigurationParameter.TertiaryInteractionStyle.toString())

    var lineShift: Double? = null
        get() = this.params.get(DrawingConfigurationParameter.LineShift.toString())?.toDouble()

    var deltaXRes: Int? = null
        get() = this.params.get(DrawingConfigurationParameter.DeltaXRes.toString())?.toInt()

    var deltaYRes: Int? = null
        get() = this.params.get(DrawingConfigurationParameter.DeltaYRes.toString())?.toInt()

    var deltaFontSize: Int? = null
        get() = this.params.get(DrawingConfigurationParameter.DeltaFontSize.toString())?.toInt()

    var lineWidth: Double? = null
        get() = this.params.get(DrawingConfigurationParameter.LineWidth.toString())?.toDouble()

    var color: Color? = null
        get() = this.params.get(DrawingConfigurationParameter.Color.toString())?.let { getAWTColor(it) }

    var charColor: Color? = null
        get() = this.params.get(DrawingConfigurationParameter.CharColor.toString())?.let { getAWTColor(it) }

    var fontName: String? = null
        get() = this.params.get(DrawingConfigurationParameter.FontName.toString())

    var displayResidueNames = true
    var fontStyle = Font.PLAIN
    var fontSize = 12 //not user-defined. Computed by the function computeOptimalFontSize()

    init {
        defaultParams.forEach { (k, v) ->
            this.params.put(k, v)
        }
    }

    fun clear() {
        this.params.clear()
    }

}

fun computeOptimalFontSize(g: Graphics2D, gc: WorkingSession, drawingConfiguration: DrawingConfiguration, title: String, width: Double, height: Double): Int {
    var dimension: Dimension2D
    var fontSize = (100 * gc.finalZoomLevel).toInt() //initial value
    do {
        fontSize--
        val font = Font(drawingConfiguration.fontName, drawingConfiguration.fontStyle, fontSize)
        dimension = getStringBoundsRectangle2D(g, title, font)
    } while (dimension.width >= width - width * 0.5 + width * drawingConfiguration.deltaFontSize!!.toDouble() / 20.0 && dimension.height >= height - height * 0.5 + height * drawingConfiguration.deltaFontSize!!.toDouble() / 20.0)
    return fontSize;
}

fun getStringBoundsRectangle2D(g: Graphics2D, title: String, font: Font): Dimension2D {
    g.font = font
    val fm = g.fontMetrics
    val lm = font.getLineMetrics(title, g.fontRenderContext);
    val r = fm.getStringBounds(title, g)
    return Dimension(r.getWidth().toInt(), (lm.ascent - lm.descent).toInt())
}

abstract class SecondaryStructureElement(val ssDrawing: SecondaryStructureDrawing, var parent: SecondaryStructureElement?, val name: String, val location: Location, var type: SecondaryStructureType) : DrawingConfigurationListener {

    var drawingConfiguration: DrawingConfiguration

    abstract val bounds2D: Rectangle2D?

    open val residues: List<ResidueDrawing>
        get() = this.ssDrawing.getResiduesFromAbsPositions(*this.location.positions.toIntArray())

    abstract fun draw(g: Graphics2D, at: AffineTransform)

    abstract fun asSVG(indentChar: String = "\t", indentLevel: Int = 1, transX: Double = 0.0, transY: Double = 0.0): String

    fun getColor(): Color {
        val color = if (this.drawingConfiguration.color != null) this.drawingConfiguration.color!! else if (this.parent != null) this.parent!!.getColor() else ssDrawing.drawingConfiguration.color!!
        return Color(color.red, color.green, color.blue,this.getOpacity())
    }

    fun getOpacity(): Int {
        return if (this.drawingConfiguration.opacity != null) this.drawingConfiguration.opacity!! else if (this.parent != null) parent!!.getOpacity() else ssDrawing.drawingConfiguration.opacity!!
    }

    fun getCharColor(): Color {
        val color = if (this.drawingConfiguration.charColor != null) this.drawingConfiguration.charColor!! else if (this.parent != null) this.parent!!.getCharColor() else ssDrawing.drawingConfiguration.charColor!!
        return Color(color.red, color.green, color.blue,this.getOpacity())
    }

    fun getLineWidth(): Double {
        return if (this.drawingConfiguration.lineWidth != null) this.drawingConfiguration.lineWidth!! else if (this.parent != null) this.parent!!.getLineWidth() else ssDrawing.drawingConfiguration.lineWidth!!
    }

    fun getLineShift(): Double {
        return if (this.drawingConfiguration.lineShift != null) this.drawingConfiguration.lineShift!! else if (this.parent != null) this.parent!!.getLineShift() else ssDrawing.drawingConfiguration.lineShift!!
    }

    fun getTertiaryInteractionStyle(): String {
        return if (this.drawingConfiguration.tertiaryInteractionStyle != null) this.drawingConfiguration.tertiaryInteractionStyle!! else if (this !is HelixDrawing && this !is JunctionDrawing && this.parent != null) (this.parent as SecondaryStructureElement).getTertiaryInteractionStyle() else ssDrawing.drawingConfiguration.tertiaryInteractionStyle!!
    }

    fun getDeltaXRes(): Int {
        return if (this.drawingConfiguration.deltaXRes != null) this.drawingConfiguration.deltaXRes!! else if (this !is HelixDrawing && this !is JunctionDrawing && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaXRes() else ssDrawing.drawingConfiguration.deltaXRes!!
    }

    fun getDeltaYRes(): Int {
        return if (this.drawingConfiguration.deltaYRes != null) this.drawingConfiguration.deltaYRes!! else if (this !is HelixDrawing && this !is JunctionDrawing && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaYRes() else ssDrawing.drawingConfiguration.deltaYRes!!
    }

    fun getDeltaFontSize(): Int {
        return if (this.drawingConfiguration.deltaFontSize != null) this.drawingConfiguration.deltaFontSize!! else if (this !is HelixDrawing && this !is JunctionDrawing && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaFontSize() else ssDrawing.drawingConfiguration.deltaFontSize!!
    }

    fun getFontName(): String {
        return if (this.drawingConfiguration.fontName != null) this.drawingConfiguration.fontName!! else if (this !is HelixDrawing && this !is JunctionDrawing && this.parent != null) (this.parent as SecondaryStructureElement).getFontName() else ssDrawing.drawingConfiguration.fontName!!
    }

    fun getSinglePositions(): IntArray {
        return this.location.positions.toIntArray()
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: String) {
        this.drawingConfiguration.params.put(parameter.toString(), value)
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Color) {
        this.drawingConfiguration.params.put(parameter.toString(), getHTMLColorString(value))
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Double) {
        this.drawingConfiguration.params.put(parameter.toString(), value.toString())
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Int) {
        this.drawingConfiguration.params.put(parameter.toString(), value.toString())
    }

    override fun applyConfiguration(configuration: DrawingConfiguration) {
        this.drawingConfiguration = configuration
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        theme[this.type.toString()]?.let { parameters ->
            for ((name, value) in parameters) {
                if (value == null)
                    this.drawingConfiguration.params.remove(name)
                else
                    this.drawingConfiguration.params[name] = value
            }
        }
    }

    init {
        this.drawingConfiguration = DrawingConfiguration(this.ssDrawing.theme.configurations.getOrDefault(this.type.toString(), mutableMapOf()))
        if (!this.ssDrawing.theme.configurations.containsKey(this.type.toString()))
            this.ssDrawing.theme.configurations[this.type.toString()] = mutableMapOf<String, String>()
    }

}

class SecondaryStructureDrawing(val secondaryStructure: SecondaryStructure, frame: Rectangle2D = Rectangle(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height), var theme: Theme = Theme(), val workingSession: WorkingSession = WorkingSession()) : DrawingConfigurationListener {

    var name: String
        get() = this.secondaryStructure.name
        set(name) {
            this.secondaryStructure.name = name
        }
    val branches = mutableListOf<JunctionDrawing>()
    val helices = mutableListOf<HelixDrawing>()
    val pknots = mutableListOf<PKnotDrawing>()
    val singleStrands = mutableListOf<SingleStrandDrawing>()
    val residues = mutableListOf<ResidueDrawing>()
    val tertiaryInteractions = mutableListOf<TertiaryInteractionDrawing>()
    var drawingConfiguration = DrawingConfiguration(defaultParams = mutableMapOf())
    var fitToResiduesBetweenBranches = true
    var NumberTransX: Float = 0F
    var NumberTransY: Float = 0F
    var ATransX: Float = 0F
    var ATransY: Float = 0F
    var UTransX: Float = 0F
    var UTransY: Float = 0F
    var GTransX: Float = 0F
    var GTransY: Float = 0F
    var CTransX: Float = 0F
    var CTransY: Float = 0F
    var XTransX: Float = 0F
    var XTransY: Float = 0F
    var quickDraw = false

    val allSecondaryInteractions:List<BaseBaseInteractionDrawing>
        get () {
            val interactions = mutableListOf<BaseBaseInteractionDrawing>()
            for (h in this.allHelices) {
                interactions.addAll(h.secondaryInteractions)
            }
            return interactions
        }

    val allRegularSymbols:List<LWSymbolDrawing>
    get() {
        val symbols = mutableListOf<LWSymbolDrawing>()
        for (interaction in this.allSecondaryInteractions)
            symbols.addAll(interaction.regularSymbols)
        for (interaction in this.allTertiaryInteractions)
            symbols.addAll(interaction.regularSymbols)
        return symbols
    }

    val allLWSymbols:List<LWSymbolDrawing>
        get() {
            val symbols = mutableListOf<LWSymbolDrawing>()
            for (interaction in this.allSecondaryInteractions)
                symbols.addAll(interaction.lwSymbols)
            for (interaction in this.allTertiaryInteractions)
                symbols.addAll(interaction.lwSymbols)
            return symbols
        }

    val allJunctions: List<JunctionDrawing>
        get() {
            val allJunctions =  mutableListOf<JunctionDrawing>()
            for (branch in this.branches) {
                allJunctions.add(branch)
                allJunctions.addAll(branch.junctionsFromBranch())
            }
            return allJunctions
        }

    val allTertiaryInteractions: List<TertiaryInteractionDrawing>
        get() {
            if (this.pknots.isEmpty())
                return this.allTertiaryInteractions
            val allTertiaryInteractions =  mutableListOf<TertiaryInteractionDrawing>()
            allTertiaryInteractions.addAll(this.tertiaryInteractions)
            for (pknot in this.pknots)
                allTertiaryInteractions.addAll(pknot.tertiaryInteractions)
            return allTertiaryInteractions
        }

    val allPhosphoBonds: List<PhosphodiesterBondDrawing>
        get() {
            val allPhosphoBonds =  mutableListOf<PhosphodiesterBondDrawing>()
            for (h in this.allHelices)
                allPhosphoBonds.addAll(h.phosphoBonds)
            for (j in this.allJunctions)
                allPhosphoBonds.addAll(j.phosphoBonds)
            for (ss in this.singleStrands)
                allPhosphoBonds.addAll(ss.phosphoBonds)
            return allPhosphoBonds
        }

    val allHelices: List<HelixDrawing>
        get() {
            val allHelices = mutableListOf<HelixDrawing>()
            allHelices.addAll(this.helices)
            for (branch in this.branches) {
                allHelices.addAll(branch.helicesFromBranch())
            }
            for (pknot in this.pknots) {
                allHelices.add(pknot.helix)
            }
            return allHelices.sortedBy { it.start }
        }

    val viewX: Double
        get() = this.workingSession.viewX

    val viewY: Double
        get() = this.workingSession.viewY

    val finalZoomLevel: Double
        get() = this.workingSession.finalZoomLevel

    val selection: MutableList<ResidueDrawing>
        get() = this.workingSession.selectedResidues

    val length: Int
        get() = this.secondaryStructure.length

    init {
        this.drawingConfiguration = DrawingConfiguration(theme.configurations.getOrDefault(SecondaryStructureType.Full2D.toString(), mutableMapOf()))
        this.theme.configurations[SecondaryStructureType.Full2D.toString()] = this.drawingConfiguration.params
        this.secondaryStructure.rna.seq.forEachIndexed { index, char ->
            this.residues.add(
                    when (char) {
                        'A' -> A(null, this, index + 1)
                        'U' -> U(null, this, index + 1)
                        'G' -> G(null, this, index + 1)
                        'C' -> C(null, this, index + 1)
                        else -> X(null, this, index + 1)
                    }
            )
        }
        //we start the drawing with the helices with no junction on one side
        var currentPos = 0
        lateinit var lastBranchConstructed: JunctionDrawing
        lateinit var bottom: Point2D
        lateinit var top: Point2D

        do {
            val nextHelix = this.secondaryStructure.getNextHelixEnd(currentPos)

            if (nextHelix == null) { // no next helix, do we have any remaining residues?
                currentPos += 1
                val remaining: Float = (this.secondaryStructure.length - currentPos + 1).toFloat()
                if (remaining > 0) {
                    this.singleStrands.add(
                            SingleStrandDrawing(
                                    this,
                                    SingleStrand(name = "SS${this.singleStrands.size + 1}",
                                            start = currentPos,
                                            end = this.secondaryStructure.length
                                    ),
                                    start = bottom,
                                    end = if (this.fitToResiduesBetweenBranches) Point2D.Double(
                                            bottom.x + radiusConst * 2 * (remaining + 1),
                                            bottom.y
                                    ) else Point2D.Double(bottom.x + 200, bottom.y)
                            )
                    )
                    this.residues[this.secondaryStructure.length - 1].center = if (this.fitToResiduesBetweenBranches)
                        Point2D.Double(bottom.x + radiusConst * 2 * (remaining + 1), bottom.y)
                    else Point2D.Double(bottom.x + 200, bottom.y)
                }
                break
            }

            val junction: Junction = (nextHelix.third.junctionsLinked.first
                    ?: nextHelix.third.junctionsLinked.second) as Junction
            val residuesBeforeHelix = nextHelix.first - currentPos - 1

            if (currentPos == 0) {
                bottom = Point2D.Double(frame.width / 2, frame.height - 50)
                top = Point2D.Double(frame.width / 2, frame.height - 50 - helixDrawingLength(
                        nextHelix.third
                )
                )

                var circles = mutableListOf<Triple<Point2D, Double, Ellipse2D>>()
                var lines = mutableListOf<List<Point2D>>()

                val h = HelixDrawing(null,
                        this,
                        nextHelix.third,
                        bottom,
                        top
                )

                lastBranchConstructed = JunctionDrawing(null,
                        this,
                        circles,
                        lines,
                        null,
                        ConnectorId.s,
                        top,
                        nextHelix.third,
                        junction
                )

                this.helices.add(h)

                if (residuesBeforeHelix > 0) {
                    this.singleStrands.add(
                            SingleStrandDrawing(
                                    this,
                                    SingleStrand(name = "SS${this.singleStrands.size + 1}",
                                            start = currentPos + 1,
                                            end = residuesBeforeHelix
                                    ),
                                    bottom,
                                    if (this.fitToResiduesBetweenBranches) Point2D.Double(
                                            bottom.x - radiusConst * 2 * (residuesBeforeHelix + 1),
                                            bottom.y
                                    ) else Point2D.Double(bottom.x - 200, bottom.y)
                            )
                    )
                    this.residues[0].center = if (this.fitToResiduesBetweenBranches) Point2D.Double(bottom.x - radiusConst * 2 * (residuesBeforeHelix + 1), bottom.y) else Point2D.Double(bottom.x - 200, bottom.y)
                }

                this.branches.add(lastBranchConstructed)

            } else {
                //for the moment the new branch is located at the same location than the previous one
                //the computing of the branch is done twice
                //a first one to compute the transX to avoid overlaps with the previous branch
                //a second one to compute the placements of each graphical object of the new branch to avoid overlaps with any previous objects
                bottom = Point2D.Double(bottom.x, bottom.y)
                top = Point2D.Double(bottom.x, bottom.y - helixDrawingLength(
                        nextHelix.third
                )
                )

                //first we want to find the maxX of the previous branch to avoid an overlap with the new branch
                var lastJunctions = mutableListOf<JunctionDrawing>()
                lastJunctions.addAll(this.branches.last().junctionsFromBranch())

                var circles = mutableListOf<Triple<Point2D, Double, Ellipse2D>>()
                var lines = mutableListOf<List<Point2D>>()
                val newBranchConstructed = JunctionDrawing(null,
                        this,
                        circles,
                        lines,
                        null,
                        ConnectorId.s,
                        top,
                        nextHelix.third,
                        junction
                )
                var minY: Double = newBranchConstructed.minY //to search for the circles from the previous branches at the same level
                //first we check the circles from the last branch constructed at the same level than the new branch constructed
                var circlesAtTheSameLevel = mutableListOf<Ellipse2D>()
                for (lastC in lastJunctions) {
                    if (lastC.circle.bounds.minY >= minY) {
                        circlesAtTheSameLevel.add(lastC.circle)
                    }
                }

                var maxX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.maxBy { it.bounds.maxX }!!.bounds.maxX
                maxX += 2 * radiusConst //we take care of the occupancy of a residue

                //now we search for the circles of the new branch that are at the same level than the circles recovered in the step before var maxX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.maxBy { it.bounds.maxX }!!.bounds.maxX
                val newJunctions = newBranchConstructed.junctionsFromBranch()

                circlesAtTheSameLevel = mutableListOf<Ellipse2D>()
                minY = this.branches.last().minY

                for (newC in newJunctions) {
                    if (newC.circle.bounds.minY >= minY) {
                        circlesAtTheSameLevel.add(newC.circle)
                    }
                }

                var minX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.minBy { it.bounds.minX }!!.bounds.minX

                minX -= 2 * radiusConst //we take care of the occupancy of a residue

                var transX = maxX - bottom.x

                if (minX + transX < maxX) { //if despite the transX that will be applied, thhe minX of the new branch is still on the left of the maxX of the previous branches
                    transX += maxX - (minX + transX)
                }

                if (this.fitToResiduesBetweenBranches) {
                    val minimalTransX = (nextHelix.first - currentPos + 2) * radiusConst * 2

                    if (transX < minimalTransX) {
                        transX += (minimalTransX - transX)
                    }
                }

                if (currentPos + 1 <= nextHelix.first - 1) {
                    this.singleStrands.add(
                            SingleStrandDrawing(
                                    this,
                                    SingleStrand(name = "SS${this.singleStrands.size + 1}",
                                            start = currentPos + 1,
                                            end = nextHelix.first - 1
                                    ),
                                    bottom,
                                    Point2D.Double(bottom.x + transX, bottom.y)
                            )
                    )
                }

                bottom = Point2D.Double(bottom.x + transX, bottom.y)
                top = Point2D.Double(bottom.x, bottom.y - helixDrawingLength(
                        nextHelix.third
                )
                )

                circles = arrayListOf<Triple<Point2D, Double, Ellipse2D>>()
                lines = arrayListOf<List<Point2D>>()

                val h = HelixDrawing(null,
                        this,
                        nextHelix.third,
                        bottom,
                        top
                )

                lastBranchConstructed = JunctionDrawing(null,
                        this,
                        circles,
                        lines,
                        null,
                        ConnectorId.s,
                        top,
                        nextHelix.third,
                        junction
                )

                this.helices.add(h)

                this.branches.add(lastBranchConstructed)
            }

            currentPos = nextHelix.second

        } while (currentPos < this.secondaryStructure.rna.seq.length)

        //now the residues
        for (helix in this.helices) {
            this.computeResidues(helix)
            for (interaction in helix.helix.secondaryInteractions) {
                helix.secondaryInteractions.add(
                        SecondaryInteractionDrawing(helix,
                                interaction,
                                this
                        )
                )
            }
        }

        for (singleStrand in this.singleStrands) {
            if (singleStrand.start == 1) {
                if (singleStrand.length != 1) {
                    val step = distance(
                            this.residues[0].center!!,
                            this.residues[singleStrand.end].center!!
                    ) / (singleStrand.ss.length).toDouble()
                    for (i in singleStrand.start + 1..singleStrand.end) {
                        val (p1_1, _) = pointsFrom(
                                this.residues[0].center!!,
                                this.residues[singleStrand.end].center!!,
                                step * (i - singleStrand.start).toDouble()
                        )
                        this.residues[i - 1].center = p1_1
                    }
                }
            } else if (singleStrand.end == this.secondaryStructure.length) {
                if (singleStrand.length != 1) {
                    val step = distance(
                            this.residues[singleStrand.start - 2].center!!,
                            this.residues[this.secondaryStructure.length - 1].center!!
                    ) / (singleStrand.length).toDouble()
                    for (i in singleStrand.start until singleStrand.end) {
                        val (p1_1, _) = pointsFrom(
                                this.residues[singleStrand.start - 2].center!!,
                                this.residues[this.secondaryStructure.length - 1].center!!,
                                step * (i - (singleStrand.start - 1).toDouble())
                        )
                        this.residues[i - 1].center = p1_1
                    }
                }
            } else {
                val step = distance(
                        this.residues[singleStrand.start - 2].center!!,
                        this.residues[singleStrand.end].center!!
                ) / (singleStrand.length + 1).toDouble()
                for (i in singleStrand.start..singleStrand.end) {
                    val (p1_1, _) = pointsFrom(
                            this.residues[singleStrand.start - 2].center!!,
                            this.residues[singleStrand.end].center!!,
                            step * (i - (singleStrand.start - 1).toDouble())
                    )
                    this.residues[i - 1].center = p1_1
                }
            }
        }

        for (branch in this.branches) {
            this.computeResidues(branch)

            for (helix in branch.helicesFromBranch()) {
                for (interaction in helix.helix.secondaryInteractions) {
                    helix.secondaryInteractions.add(
                            SecondaryInteractionDrawing(helix,
                                    interaction,
                                    this
                            )
                    )
                }
            }
        }

        for (i in 1 until this.secondaryStructure.length) {
            val phosphoBond = PhosphodiesterBondDrawing(null, this,
                    Location(Location(i), Location(i + 1))
            )
            for (h in this.allHelices) {
                if (h.location.contains(i) && h.location.contains(i + 1)) {
                    phosphoBond.parent = h
                    h.phosphoBonds.add(phosphoBond)
                    break;
                }
            }
            if (phosphoBond.parent == null) {
                for (j in this.allJunctions) {
                    if (j.location.contains(i) && j.location.contains(i + 1)) {
                        phosphoBond.parent = j
                        j.phosphoBonds.add(phosphoBond)
                        break;
                    }
                }
            }
            if (phosphoBond.parent == null) {
                for (ss in this.singleStrands) {
                    if (ss.location.contains(i) && ss.location.contains(i + 1)) {
                        phosphoBond.parent = ss
                        ss.phosphoBonds.add(phosphoBond)
                        break;
                    }
                }
            }

        }

        for (interaction in this.secondaryStructure.tertiaryInteractions) {
            this.tertiaryInteractions.add(
                    TertiaryInteractionDrawing(null,
                            interaction,
                            this
                    )
            )
        }

        for (pknot in this.secondaryStructure.pknots) {
            val pknotDrawing = PKnotDrawing(this, pknot)

            this.pknots.add(pknotDrawing)

            for (h in this.helices)
                if (h.helix.equals(pknot.helix)) {
                    pknotDrawing.helix = h
                    h.parent = pknotDrawing
                    break
                }

            for (interaction in pknot.tertiaryInteractions) {
                pknotDrawing.tertiaryInteractions.add(TertiaryInteractionDrawing(pknotDrawing,
                        interaction,
                        this
                ))
            }
        }

        for (r in this.residues) {
            OUTER@ for (pknot in this.pknots) {
                for (interaction in pknot.tertiaryInteractions) {
                    if (interaction.location.contains(r.absPos)) {
                        r.parent = interaction
                        break@OUTER;
                    }
                }
            }
            if (r.parent == null)
                OUTER@ for (h in this.allHelices) {
                    for (interaction in h.secondaryInteractions)
                        if (interaction.location.contains(r.absPos)) {
                            r.parent = interaction
                            break@OUTER;
                        }
                }
            if (r.parent == null) {
                for (j in this.allJunctions) {
                    if (j.locationWithoutSecondaries.contains(r.absPos)) {
                        r.parent = j
                        break;
                    }
                }
            }
            if (r.parent == null) {
                for (ss in this.singleStrands) {
                    if (ss.location.contains(r.absPos)) {
                        r.parent = ss
                        break;
                    }
                }
            }
        }
    }

    fun getResiduesFromAbsPositions(vararg positions: Int): List<ResidueDrawing> {
        val _residues: MutableList<ResidueDrawing> = mutableListOf<ResidueDrawing>()
        for (r: ResidueDrawing in residues) {
            if (r.absPos in positions) {
                _residues.add(r)
            }
        }
        return _residues
    }

    fun getBounds(): Rectangle2D {
        val minX = this.residues.minBy { it.circle!!.minX }!!.circle!!.minX - drawingConfiguration.lineWidth!!
        val minY = this.residues.minBy { it.circle!!.minY }!!.circle!!.minY - drawingConfiguration.lineWidth!!
        val maxX = this.residues.maxBy { it.circle!!.maxX }!!.circle!!.maxX + drawingConfiguration.lineWidth!!
        val maxY = this.residues.maxBy { it.circle!!.maxY }!!.circle!!.maxY + drawingConfiguration.lineWidth!!
        return Rectangle2D.Double(minX.toInt() - drawingConfiguration.lineWidth!! / 2.0, minY.toInt() - drawingConfiguration.lineWidth!! / 2.0, maxX - minX + drawingConfiguration.lineWidth!!, maxY - minY + drawingConfiguration.lineWidth!!)
    }

    fun draw(g: Graphics2D) {
        val at = AffineTransform()
        at.translate(workingSession.viewX, workingSession.viewY)
        at.scale(workingSession.finalZoomLevel, workingSession.finalZoomLevel)
        if (!quickDraw) {
            val _c = at.createTransformedShape(this.residues.first().circle)
            drawingConfiguration.fontSize = computeOptimalFontSize(
                    g,
                    workingSession,
                    this.drawingConfiguration,
                    this.residues.first().type.name,
                    _c.bounds2D.width,
                    _c.bounds2D.height
            )
            g.font = Font(this.drawingConfiguration.fontName, this.drawingConfiguration.fontStyle, drawingConfiguration.fontSize)
            var r2d = getStringBoundsRectangle2D(g, "A", g.font)
            this.ATransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.ATransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "U", g.font)
            this.UTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.UTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "G", g.font)
            this.GTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.GTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "C", g.font)
            this.CTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.CTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "X", g.font)
            this.XTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.XTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
        }

        if (!quickDraw) {
            this.workingSession.selectedResidues.forEach {
                it.drawSelectionHalo(g, at)
            }
            this.allSecondaryInteractions.forEach {
                if (it.selected)
                    it.drawSelectionHalo(g, at)
            }
            this.allTertiaryInteractions.forEach {
                if (it.selected)
                    it.drawSelectionHalo(g, at)
            }
        }

        this.helices.forEach {
            it.draw(g, at)
        }

        this.branches.forEach {
            it.draw(g, at)
        }

        if (!quickDraw) {
            this.allTertiaryInteractions.forEach {
                it.draw(g, at)
            }
        }

        this.residues.forEach {
            it.draw(g, at)
        }

    }

    fun computeResidues(branch: JunctionDrawing) {
        for (helix in branch.helicesFromBranch()) {
            this.computeResidues(helix)
        }

        for (j in branch.junctionsFromBranch()) {
            for (b in j.junction.location.blocks) {
                var angle = angleFrom(
                        j.center,
                        this.residues[b.start - 1].center!!,
                        this.residues[b.end - 1].center!!
                )
                val cp = crossProduct(
                        sharedPoint = j.center,
                        p2 = this.residues[b.start - 1].center!!,
                        p3 = this.residues[b.end - 1].center!!
                )
                if (cp < 0) {
                    angle -= 360
                } else {
                    angle = -angle
                }
                val step = -angle / (b.end - b.start).toDouble()
                for (i in b.start + 1 until b.end) {
                    this.residues[i - 1].center = rotatePoint(
                            this.residues[b.start - 1].center!!,
                            j.center,
                            step * (i - b.start).toDouble()
                    )
                    this.residues[i - 1].updated = true
                }
            }
        }

    }

    /**
    Compute the coordinates for all the residues in an helix
     */
    fun computeResidues(helix: HelixDrawing) {
        val (p1_5, p2_3) = getPerpendicular(
                helix.line.p1,
                helix.line.p1,
                helix.line.p2,
                helixDrawingWidth() / 2.0
        )
        var cp = crossProduct(helix.line.p1, helix.line.p2, p1_5)
        if (cp < 0) {
            this.residues[helix.helix.ends[0] - 1].center = p1_5
            this.residues[helix.helix.ends[3] - 1].center = p2_3
        } else {
            this.residues[helix.helix.ends[0] - 1].center = p2_3
            this.residues[helix.helix.ends[3] - 1].center = p1_5
        }
        this.residues[helix.helix.ends[0] - 1].updated = true
        this.residues[helix.helix.ends[3] - 1].updated = true
        val (p1_3, p2_5) = getPerpendicular(
                helix.line.p2,
                helix.line.p1,
                helix.line.p2,
                helixDrawingWidth() / 2.0
        )
        cp = crossProduct(helix.line.p2, helix.line.p1, p1_3)
        if (cp > 0) {
            this.residues[helix.helix.ends[1] - 1].center = p1_3
            this.residues[helix.helix.ends[2] - 1].center = p2_5
        } else {
            this.residues[helix.helix.ends[1] - 1].center = p2_5
            this.residues[helix.helix.ends[2] - 1].center = p1_3
        }
        this.residues[helix.helix.ends[1] - 1].updated = true
        this.residues[helix.helix.ends[2] - 1].updated = true

        val step = helixDrawingLength(helix.helix).toDouble() / (helix.helix.length - 1).toDouble()

        for (i in 1 until helix.length - 1) {
            val (p1_1, _) = pointsFrom(
                    this.residues[helix.helix.ends[0] - 1].center!!,
                    this.residues[helix.helix.ends[1] - 1].center!!,
                    step * i
            )
            this.residues[helix.helix.ends[0] + i - 1].center = p1_1
            this.residues[helix.helix.ends[0] + i - 1].updated = true

            val (_, p1_2) = pointsFrom(
                    this.residues[helix.helix.ends[2] - 1].center!!,
                    this.residues[helix.helix.ends[3] - 1].center!!,
                    step * i
            )

            this.residues[helix.helix.ends[3] - i - 1].center = p1_2
            this.residues[helix.helix.ends[3] - i - 1].updated = true

        }

    }

    fun hasSingleHBonds(): Boolean {
        return false
    }

    fun asSVG(): String {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB) //just to get a Graphics object
        val g = image.createGraphics()
        drawingConfiguration.fontSize = computeOptimalFontSize(
                g,
                WorkingSession(),
                drawingConfiguration,
                "A",
                residues.first().circle!!.width,
                residues.first().circle!!.height
        )
        val font = Font(drawingConfiguration.fontName, drawingConfiguration.fontStyle, drawingConfiguration.fontSize)
        var r2d = getStringBoundsRectangle2D(g, "A", font)
        this.ATransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.ATransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "U", font)
        this.UTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.UTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "G", font)
        this.GTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.GTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "C", font)
        this.CTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.CTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "X", font)
        this.XTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.XTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F

        val bounds = getBounds()
        val svgBuffer = StringBuffer("""<svg viewBox="0 0 ${bounds.width} ${bounds.height}" xmlns="http://www.w3.org/2000/svg">""" + "\n")

        helices.map { helix ->
            helix.secondaryInteractions.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
            helix.phosphoBonds.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
        }

        allJunctions.map { junction ->
            junction.helices.map { helix ->
                helix.secondaryInteractions.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
                helix.phosphoBonds.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
            }
            junction.phosphoBonds.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
        }

        singleStrands.map { ss ->
            ss.phosphoBonds.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
        }

        tertiaryInteractions.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
        residues.map { it.asSVG(indentLevel = 1, transX = -bounds.minX, transY = -bounds.minY) }.forEach { svgBuffer.append(it) }
        svgBuffer.append("</svg>")
        return svgBuffer.toString()
    }

    override fun toString(): String {
        return this.secondaryStructure.toString()
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: String) {
        this.drawingConfiguration.params.put(parameter.toString(), value)
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Color) {
        this.drawingConfiguration.params.put(parameter.toString(), getHTMLColorString(value))
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Double) {
        this.drawingConfiguration.params.put(parameter.toString(), value.toString())
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, value: Int) {
        this.drawingConfiguration.params.put(parameter.toString(), value.toString())
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        //we restore the default parameter value, the full 2D needs to have any theme parameter defined. No deletion allowed.
        this.drawingConfiguration.params.put(parameter.toString(), RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString())!!.getValue(parameter.toString()))
        if (fireChildren) {
            for (pk in this.pknots)
                pk.configurationChange(parameter, fireChildren)
            for (jc in this.allJunctions)
                jc.configurationChange(parameter, fireChildren)
            for (ss in this.singleStrands)
                ss.configurationChange(parameter, fireChildren)
            for (h in this.allHelices)
                h.configurationChange(parameter, fireChildren)
            for (i in this.allSecondaryInteractions)
                i.configurationChange(parameter, fireChildren)
            for (i in this.tertiaryInteractions)
                i.configurationChange(parameter, fireChildren)
            for (r in this.residues)
                r.configurationChange(parameter, fireChildren)
        }
    }

    override fun applyConfiguration(drawingConfiguration: DrawingConfiguration) {
        this.drawingConfiguration = drawingConfiguration
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        theme[SecondaryStructureType.Full2D.toString()]?.let { parameters ->
            for ((name, value) in parameters) {
                if (value == null)
                    this.theme.configurations[SecondaryStructureType.Full2D.toString()]!!.remove(name)
                else
                    this.theme.configurations[SecondaryStructureType.Full2D.toString()]!![name] = value
            }
        }
        for (pk in this.pknots)
            pk.applyTheme(theme)
        for (jc in this.allJunctions)
            jc.applyTheme(theme)
        for (ss in this.singleStrands)
            ss.applyTheme(theme)
        for (h in this.allHelices)
            h.applyTheme(theme)
        for (i in this.allSecondaryInteractions)
            i.applyTheme(theme)
        for (i in this.tertiaryInteractions)
            i.applyTheme(theme)
        for (r in this.residues)
            r.applyTheme(theme)
    }

    /**
     *  this function clears all the params for the themes attached to this element and any secondary structure subelement.
     */
    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration = DrawingConfiguration() //we come back to the default theme
        if (clearChildrenDrawingConfiguration) {
            for (pk in this.pknots)
                pk.clearConfiguration(clearChildrenDrawingConfiguration)
            for (jc in this.allJunctions)
                jc.clearConfiguration(clearChildrenDrawingConfiguration)
            for (ss in this.singleStrands)
                ss.clearConfiguration(clearChildrenDrawingConfiguration)
            for (h in this.allHelices)
                h.clearConfiguration(clearChildrenDrawingConfiguration)
            for (i in this.allSecondaryInteractions)
                i.clearConfiguration(clearChildrenDrawingConfiguration)
            for (i in this.tertiaryInteractions)
                i.clearConfiguration(clearChildrenDrawingConfiguration)
            for (r in this.residues)
                r.clearConfiguration(clearChildrenDrawingConfiguration)
        }
    }

}

abstract class ResidueDrawing(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int, name: String, type: SecondaryStructureType) : SecondaryStructureElement(ssDrawing, parent, name, Location(absPos), type) {

    var updated = true //to force the recomputation of interaction symbols
    val absPos: Int
        get() = this.location.start

    var circle: Ellipse2D? = null

    var center: Point2D? = null
        set(value) {
            field = value
            this.circle = Ellipse2D.Double(value!!.x - radiusConst, value.y - radiusConst, (radiusConst * 2F).toDouble(), (radiusConst * 2F).toDouble())
        }

    override val bounds2D: Rectangle2D?
        get() = this.circle?.bounds2D

    val selected: Boolean
        get() = this in this.ssDrawing.selection

    fun drawSelectionHalo(g: Graphics2D, at: AffineTransform) {
        if (this.circle != null) {
            val _c = at.createTransformedShape(this.circle)
            g.color = RnartistConfig.selectionColor
            val newWidth = (_c.bounds2D.width) + this.getLineWidth() / 2.0 + this.ssDrawing.finalZoomLevel.toFloat() * RnartistConfig.selectionSize.toFloat()/2f
            var newCircle = Ellipse2D.Double(_c.bounds2D.centerX - newWidth / 2.0, _c.bounds2D.centerY - newWidth / 2.0, newWidth, newWidth)
            g.fill(newCircle)
        }
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        if (this.circle != null) {
            val _c = at.createTransformedShape(this.circle)
            g.color = this.getColor()
            g.fill(_c)
            if (!this.ssDrawing.quickDraw) {
                val previousStroke: Stroke = g.getStroke()
                g.stroke = BasicStroke(this.ssDrawing.workingSession.finalZoomLevel.toFloat() * this.getLineWidth().toFloat())
                g.color = Color(Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
                        this.getOpacity()
                )
                g.draw(_c)
                g.stroke = previousStroke
                if (g.font.size > 5 && this.getOpacity() > 0) { //the conditions to draw a letter
                    g.color = this.getCharColor()
                    this.drawResidueName(g, _c)
                }
                if ((absPos % 5 == 0 || absPos == 1 || absPos == ssDrawing.length))
                    this.drawNumbering(g, at)
            }
        }
        this.updated = false
    }

    protected fun drawNumbering(g: Graphics2D, at: AffineTransform) {
        g.color = Color(Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
                this.getOpacity()
        )
        val n = "$absPos".length
        var p: Pair<Point2D, Point2D>? = null
        var e: Shape? = null
        g.font = Font(g.font.fontName, g.font.style, g.font.size - 4)
        val numberDim = getStringBoundsRectangle2D(g, "0", g.font)
        (this.parent as? SecondaryInteractionDrawing)?.let {
            val pairedCenter = (if (it.residue == this) it.pairedResidue else it.residue).center
            p = pointsFrom(this.center!!, pairedCenter!!, -getLineWidth() / 2.0 - radiusConst - radiusConst / 3.0)
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - radiusConst / 3.0, (p as Pair<Point2D, Point2D>).first.y - radiusConst / 3.0, 2.0 * radiusConst / 3.0, 2.0 * radiusConst / 3.0))
            g.fill(e)

            p = pointsFrom(this.center!!, pairedCenter!!, -getLineWidth() / 2.0 - radiusConst - radiusConst - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel))
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), numberDim.width / ssDrawing.workingSession.finalZoomLevel, numberDim.width / ssDrawing.workingSession.finalZoomLevel))

        }
        (this.parent as? JunctionDrawing)?.let {
            p = pointsFrom(this.center!!, it.center!!, -getLineWidth() / 2.0 - radiusConst - radiusConst / 3.0)
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - 2, (p as Pair<Point2D, Point2D>).first.y - 2, 2.0 * radiusConst / 3.0, 2.0 * radiusConst / 3.0))
            g.fill(e)

            p = pointsFrom(this.center!!, it.center!!, -getLineWidth() / 2.0 - radiusConst - radiusConst - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel))
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), numberDim.width.toDouble() / ssDrawing.workingSession.finalZoomLevel, numberDim.width.toDouble() / ssDrawing.workingSession.finalZoomLevel))
        }
        (this.parent as? SingleStrandDrawing)?.let {
            p = pointsFrom(Point2D.Double(this.center!!.x, this.center!!.y + radiusConst), Point2D.Double(this.center!!.x, this.center!!.y - radiusConst), -getLineWidth() / 2.0 - radiusConst / 3.0)
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - 2, (p as Pair<Point2D, Point2D>).first.y - 2, 2.0 * radiusConst / 3.0, 2.0 * radiusConst / 3.0))
            g.fill(e)

            p = pointsFrom(Point2D.Double(this.center!!.x, this.center!!.y + radiusConst), Point2D.Double(this.center!!.x, this.center!!.y - radiusConst), -getLineWidth() / 2.0 - radiusConst - radiusConst / 2.0 - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel))
            e = at.createTransformedShape(Ellipse2D.Double((p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), numberDim.width.toDouble() / ssDrawing.workingSession.finalZoomLevel, numberDim.width.toDouble() / ssDrawing.workingSession.finalZoomLevel))
        }

        if (e != null && p != null) {
            val transX = (e!!.bounds2D.width - numberDim.width.toDouble()).toFloat() / 2F
            val transY = (e!!.bounds2D.height + numberDim.height.toDouble()).toFloat() / 2F
            val cp = crossProduct(center as Point2D, Point2D.Double((center as Point2D).x, (center as Point2D).y - 20), (p as Pair<Point2D, Point2D>).first)
            if (cp >= 0) {
                g.drawString("$absPos".substring(0, 1), e!!.bounds2D.minX.toFloat() + transX, e!!.bounds2D.minY.toFloat() + transY)
                var i = 1
                while (i < n) {
                    var _p = pointsFrom(Point2D.Double((p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y), Point2D.Double((p as Pair<Point2D, Point2D>).first.x + numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y), -(2 * (i - 1) + 1) * numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel))
                    e = at.createTransformedShape(Ellipse2D.Double(_p.second.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), _p.second.y - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), numberDim.width / ssDrawing.workingSession.finalZoomLevel, numberDim.width / ssDrawing.workingSession.finalZoomLevel))
                    g.drawString("$absPos".substring(i, i + 1), e!!.bounds2D.minX.toFloat() + transX, e!!.bounds2D.minY.toFloat() + transY)
                    i++
                }
            } else {
                g.drawString("$absPos".substring(n - 1, n), e!!.bounds2D.minX.toFloat() + transX, e!!.bounds2D.minY.toFloat() + transY)
                var i = 1
                while (i < n) {
                    var _p = pointsFrom(Point2D.Double((p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y), Point2D.Double((p as Pair<Point2D, Point2D>).first.x + numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), (p as Pair<Point2D, Point2D>).first.y), -(2 * (i - 1) + 1) * numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel))
                    e = at.createTransformedShape(Ellipse2D.Double(_p.first.x - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), _p.first.y - numberDim.width / (2.0 * ssDrawing.workingSession.finalZoomLevel), numberDim.width / ssDrawing.workingSession.finalZoomLevel, numberDim.width / ssDrawing.workingSession.finalZoomLevel))
                    g.drawString("$absPos".substring(n - 1 - i, n - i), e!!.bounds2D.minX.toFloat() + transX, e!!.bounds2D.minY.toFloat() + transY)
                    i++
                }
            }

        }

        g.font = Font(g.font.fontName, g.font.style, g.font.size + 4)

    }

    abstract protected fun drawResidueName(g: Graphics2D, c: Shape)

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.name)
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
    }

}

class A(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) : ResidueDrawing(parent, ssDrawing, absPos, "A", SecondaryStructureType.A) {

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val buff = StringBuffer(indentChar.repeat(indentLevel) + "<g>\n")
        buff.append(indentChar.repeat(indentLevel + 1) + """<circle cx="${this.circle!!.centerX + transX}" cy="${this.circle!!.centerY + transY}" r="${this.circle!!.width / 2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getLineWidth()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />""" + "\n")

        val charColor = this.getCharColor()

        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.centerX + transX + this.getDeltaXRes()}" y="${this.circle!!.centerY + transY + this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${this.drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        else
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.bounds2D.minX.toFloat() + transX + this.getDeltaXRes() + ssDrawing.ATransX}" y="${this.circle!!.bounds2D.minY.toFloat() + transY + this.getDeltaYRes() + ssDrawing.ATransY}" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        buff.append(indentChar.repeat(indentLevel) + "</g>\n")
        return buff.toString()
    }

    override fun drawResidueName(g: Graphics2D, c: Shape) {
        g.drawString(this.type.name, c.bounds2D.minX.toFloat() + this.ssDrawing.ATransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), c.bounds2D.minY.toFloat() + this.ssDrawing.ATransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
    }
}

class U(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) : ResidueDrawing(parent, ssDrawing, absPos, "U", SecondaryStructureType.U) {

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val buff = StringBuffer(indentChar.repeat(indentLevel) + "<g>\n")
        buff.append(indentChar.repeat(indentLevel + 1) + """<circle cx="${this.circle!!.centerX + transX}" cy="${this.circle!!.centerY + transY}" r="${this.circle!!.width / 2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getLineWidth()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />""" + "\n")

        val charColor = this.getCharColor()

        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.centerX + transX + this.getDeltaXRes()}" y="${this.circle!!.centerY + transY + this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${this.drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        else
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.bounds2D.minX.toFloat() + transX + this.getDeltaXRes() + ssDrawing.UTransX}" y="${this.circle!!.bounds2D.minY.toFloat() + transY + this.getDeltaYRes() + ssDrawing.UTransY}" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        buff.append(indentChar.repeat(indentLevel) + "</g>\n")
        return buff.toString()
    }

    override fun drawResidueName(g: Graphics2D, c: Shape) {
        g.drawString(this.type.name, c.bounds2D.minX.toFloat() + this.ssDrawing.UTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), c.bounds2D.minY.toFloat() + this.ssDrawing.UTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
    }

}


class G(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) : ResidueDrawing(parent, ssDrawing, absPos, "G", SecondaryStructureType.G) {

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val buff = StringBuffer(indentChar.repeat(indentLevel) + "<g>\n")
        buff.append(indentChar.repeat(indentLevel + 1) + """<circle cx="${this.circle!!.centerX + transX}" cy="${this.circle!!.centerY + transY}" r="${this.circle!!.width / 2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getLineWidth()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />""" + "\n")

        val charColor = this.getCharColor()

        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.centerX + transX + this.getDeltaXRes()}" y="${this.circle!!.centerY + transY + this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${this.drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        else
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.bounds2D.minX.toFloat() + transX + this.getDeltaXRes() + ssDrawing.GTransX}" y="${this.circle!!.bounds2D.minY.toFloat() + transY + this.getDeltaYRes() + ssDrawing.GTransY}" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        buff.append(indentChar.repeat(indentLevel) + "</g>\n")
        return buff.toString()
    }

    override fun drawResidueName(g: Graphics2D, c: Shape) {
        g.drawString(this.type.name, c.bounds2D.minX.toFloat() + this.ssDrawing.GTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), c.bounds2D.minY.toFloat() + this.ssDrawing.GTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
    }
}

class C(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) : ResidueDrawing(parent, ssDrawing, absPos, "C", SecondaryStructureType.C) {

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val buff = StringBuffer(indentChar.repeat(indentLevel) + "<g>\n")
        buff.append(indentChar.repeat(indentLevel + 1) + """<circle cx="${this.circle!!.centerX + transX}" cy="${this.circle!!.centerY + transY}" r="${this.circle!!.width / 2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getLineWidth()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />""" + "\n")

        val charColor = this.getCharColor()

        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.centerX + transX + this.getDeltaXRes()}" y="${this.circle!!.centerY + transY + this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${this.drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        else
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.bounds2D.minX.toFloat() + transX + this.getDeltaXRes() + ssDrawing.CTransX}" y="${this.circle!!.bounds2D.minY.toFloat() + transY + this.getDeltaYRes() + ssDrawing.CTransY}" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        buff.append(indentChar.repeat(indentLevel) + "</g>\n")
        return buff.toString()
    }

    override fun drawResidueName(g: Graphics2D, c: Shape) {
        g.drawString(this.type.name, c.bounds2D.minX.toFloat() + this.ssDrawing.CTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), c.bounds2D.minY.toFloat() + this.ssDrawing.CTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
    }

}


class X(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) : ResidueDrawing(parent, ssDrawing, absPos, "X", SecondaryStructureType.X) {

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val buff = StringBuffer(indentChar.repeat(indentLevel) + "<g>\n")
        buff.append(indentChar.repeat(indentLevel + 1) + """<circle cx="${this.circle!!.centerX + transX}" cy="${this.circle!!.centerY + transY}" r="${this.circle!!.width / 2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getLineWidth()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />""" + "\n")

        val charColor = this.getCharColor()

        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.centerX + transX + this.getDeltaXRes()}" y="${this.circle!!.centerY + transY + this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${this.drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        else
            buff.append(indentChar.repeat(indentLevel + 1) + """<text x="${this.circle!!.bounds2D.minX.toFloat() + transX + this.getDeltaXRes() + ssDrawing.XTransX}" y="${this.circle!!.bounds2D.minY.toFloat() + transY + this.getDeltaYRes() + ssDrawing.XTransY}" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${drawingConfiguration.fontSize};">${this.type.name}</text>""" + "\n")
        buff.append(indentChar.repeat(indentLevel) + "</g>\n")
        return buff.toString()
    }

    override fun drawResidueName(g: Graphics2D, c: Shape) {
        g.drawString(this.type.name, c.bounds2D.minX.toFloat() + this.ssDrawing.XTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), c.bounds2D.minY.toFloat() + this.ssDrawing.XTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
    }
}

class PKnotDrawing(ssDrawing: SecondaryStructureDrawing, val pknot: Pknot) : SecondaryStructureElement(ssDrawing, null, pknot.name, pknot.location, SecondaryStructureType.PKnot) {

    val tertiaryInteractions = mutableListOf<TertiaryInteractionDrawing>()
    lateinit var helix: HelixDrawing

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.name)
        if (fireChildren) {
            this.helix.configurationChange(parameter, fireChildren)
            for (interaction in this.tertiaryInteractions)
                interaction.configurationChange(parameter, fireChildren)
        }
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
        if (clearChildrenDrawingConfiguration) {
            this.helix.clearConfiguration(clearChildrenDrawingConfiguration)
            for (interaction in this.tertiaryInteractions)
                interaction.clearConfiguration(clearChildrenDrawingConfiguration)
        }
    }

    override val bounds2D: Rectangle2D?
        get() {
            var bounds2D = this.helix.bounds2D!!
            for (interaction in this.tertiaryInteractions)
                bounds2D = bounds2D.createUnion(interaction.bounds2D)
            return bounds2D
        }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        this.helix.draw(g, at)
        for (interaction in this.tertiaryInteractions)
            interaction.draw(g, at)
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        TODO("Not yet implemented")
    }
}

class HelixDrawing(parent: PKnotDrawing? = null, ssDrawing: SecondaryStructureDrawing, val helix: Helix, start: Point2D, end: Point2D) : SecondaryStructureElement(ssDrawing, parent, helix.name, helix.location, SecondaryStructureType.Helix) {

    var line: Line2D = Line2D.Double(start, end)
    val secondaryInteractions = mutableListOf<SecondaryInteractionDrawing>()
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()
    val start: Int
        get() = this.location.start

    val end: Int
        get() = this.location.end

    val length: Int
        get() = this.helix.length

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.secondaryInteractions.first()!!.bounds2D
            for (i in 1 until this.secondaryInteractions.size)
                bounds = bounds?.createUnion(this.secondaryInteractions.get(i).bounds2D)
            return bounds
        }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.name)
        if (fireChildren) {
            for (p in this.phosphoBonds)
                p.configurationChange(parameter, fireChildren)
            for (i in this.secondaryInteractions) {
                i.configurationChange(parameter, fireChildren)
            }
        }

    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
        if (clearChildrenDrawingConfiguration) {
            for (p in this.phosphoBonds)
                p.clearConfiguration()
            for (i in this.secondaryInteractions) {
                i.clearConfiguration()
            }
        }
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        super.applyTheme(theme)
        for (p in this.phosphoBonds)
            p.applyTheme(theme)
        for (i in this.secondaryInteractions) {
            i.applyTheme(theme)
        }
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        this.phosphoBonds.forEach {
            it.draw(g, at)
        }
        this.secondaryInteractions.forEach {
            it.draw(g, at)
        }
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        TODO("Not yet implemented")
    }
}

class SingleStrandDrawing(ssDrawing: SecondaryStructureDrawing, val ss: SingleStrand, start: Point2D, end: Point2D) : SecondaryStructureElement(ssDrawing, null, ss.name, ss.location, SecondaryStructureType.SingleStrand) {

    var line = Line2D.Double(start, end)
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()

    val start: Int
        get() = this.location.start

    val end: Int
        get() = this.location.end

    val length: Int
        get() = this.ss.length

    override val bounds2D: Rectangle2D?
        get() = this.line.bounds2D

    override fun draw(g: Graphics2D, at: AffineTransform) {
        this.phosphoBonds.forEach {
            it.draw(g, at)
        }
        g.draw(at.createTransformedShape(this.line))
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.name)
        if (fireChildren) {
            for (p in this.phosphoBonds)
                p.configurationChange(parameter, fireChildren)
            for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
                r.configurationChange(parameter, fireChildren)
        }
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
        if (clearChildrenDrawingConfiguration) {
            for (p in this.phosphoBonds)
                p.clearConfiguration()
            for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
                r.clearConfiguration()
        }
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        super.applyTheme(theme)
        for (p in this.phosphoBonds)
            p.applyTheme(theme)
        for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
            r.applyTheme(theme)
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        TODO("Not yet implemented")
    }
}

class JunctionDrawing(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, circlesFromBranchSoFar: MutableList<Triple<Point2D, Double, Ellipse2D>>, linesFromBranchSoFar: MutableList<List<Point2D>>, previousJunction: JunctionDrawing? = null, var inId: ConnectorId, inPoint: Point2D, inHelix: Helix, val junction: Junction) : SecondaryStructureElement(ssDrawing, parent, junction.name, junction.location, SecondaryStructureType.Junction) {

    val noOverlapWithLines = true
    val noOverlapWithCircles = true
    override val residues: List<ResidueDrawing>
        get() = this.ssDrawing.getResiduesFromAbsPositions(*this.locationWithoutSecondaries.positions.toIntArray())
    var helices = mutableListOf<HelixDrawing>()
    var connectedJunctions = mutableMapOf<ConnectorId, JunctionDrawing>()
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()
    val connectors: Array<Point2D> = Array(ConnectorId.values().size, { Point2D.Float(0F, 0F) }) //the connector points on the circle
    var layout: MutableList<ConnectorId>? = defaultLayouts[this.junction.type]?.toMutableList()
        set(value) {
            //we order the helices according to the start but with inHelix as the first one
            val sortedHelix = this.junction.helicesLinked.sortedBy { it.start - this.inHelix.start }
            field = value
            //we change the entry point for each connected circle, we update the self.connectedCircles dict and we warn the connected circles that their entry point has been changed (we call their setEntryPoint() function)
            var newConnectedJunctions = mutableMapOf<ConnectorId, JunctionDrawing>() //we need to store the new connections in a temp dict otherwise the update of a connection could remove an old connection stored and not already checked.
            //this.helices = mutableListOf<HelixLine>()
            var helixRank = 0
            for (helix in sortedHelix) {
                if (helix != this.inHelix) {
                    helixRank += 1
                    var inPoint: Point2D?
                    val outId = getOutId(helixRank)

                    if (outId != null) {
                        //we compute the inPoint (center - self.connectors[connectorId] -[length helix*offset]- inPoint)
                        inPoint = pointsFrom(
                                this.center,
                                this.connectors[outId.value],
                                -helixDrawingLength(helix)
                        ).second

                        if (helix.junctionsLinked.first != null && helix.junctionsLinked.first != this.junction) {
                            //we search the circle already connected for this helix
                            lateinit var connectedJunction: MutableMap.MutableEntry<ConnectorId, JunctionDrawing>
                            for (c in this.connectedJunctions) {
                                if (c.value.junction.location == helix.junctionsLinked.first!!.location) {
                                    connectedJunction = c
                                    break
                                }
                            }

                            //we record its outId
                            newConnectedJunctions[outId] = connectedJunction.value
                            for (h in this.helices) {
                                if (h.helix == helix) {
                                    h.line = Line2D.Double(this.connectors[outId.value], inPoint)
                                    break
                                }
                            }

                            connectedJunction.value.setEntryPoint(
                                    oppositeConnectorId(
                                            outId
                                    ), inPoint)
                        } else if (helix.junctionsLinked.second != null && helix.junctionsLinked.second != this.junction) {
                            //we search the circle already connected for this helix
                            lateinit var connectedJunction: MutableMap.MutableEntry<ConnectorId, JunctionDrawing>
                            for (c in this.connectedJunctions) {
                                if (c.value.junction.location == helix.junctionsLinked.second!!.location) {
                                    connectedJunction = c
                                    break
                                }
                            }

                            //we record its outId
                            newConnectedJunctions[outId] = connectedJunction.value
                            for (h in this.helices) {
                                if (h.helix == helix) {
                                    h.line = Line2D.Double(this.connectors[outId.value], inPoint)
                                    break
                                }
                            }

                            connectedJunction.value.setEntryPoint(
                                    getConnectorId(
                                            (outId.value + ConnectorId.values().size / 2) % ConnectorId.values().size
                                    ), inPoint)
                        }

                    }
                }
            }
            //last step, we substitute the connected circles for the new ones
            this.connectedJunctions = newConnectedJunctions
        }
    var radius: Double = 0.0
        set(value) {
            field = value
            this.center = centerFrom(
                    this.inId,
                    this.connectors[this.inId.value],
                    this.radius
            )
            this.circle = Ellipse2D.Double(this.center.x - this.radius, this.center.y - this.radius, this.radius * 2.toDouble(), this.radius * 2.toDouble())
            //the (x,y) coords for the connectors
            for (i in 1 until ConnectorId.values().size) {
                this.connectors[(this.inId.value + i) % ConnectorId.values().size] =
                        rotatePoint(
                                this.connectors[this.inId.value],
                                this.center,
                                i * 360.0 / ConnectorId.values().size.toDouble()
                        )
            }
        }
    lateinit var center: Point2D
    lateinit var circle: Ellipse2D
    val inHelix = inHelix
    val previousJunction = previousJunction //allows to get some info backward. For example, useful for an InnerLoop to check the previous orientation in order to keep it if the inID is .o or .e (instead to choose .n in any case)

    val minX: Double
        get() {
            return this.junctionsFromBranch().minBy { it.circle.bounds.minX }!!.circle.bounds.minX
        }

    val minY: Double
        get() {
            return this.junctionsFromBranch().minBy { it.circle.bounds.minY }!!.circle.bounds.minY
        }

    val maxX: Double
        get() {
            return this.junctionsFromBranch().maxBy { it.circle.bounds.maxX }!!.circle.bounds.maxX
        }

    val maxY: Double
        get() {
            return this.junctionsFromBranch().maxBy { it.circle.bounds.maxY }!!.circle.bounds.maxY
        }

    val locationWithoutSecondaries: Location
        get() {
            return this.junction.locationWithoutSecondaries
        }

    val junctionCategory: JunctionType
        get() {
            return this.junction.type
        }

    override val bounds2D: Rectangle2D?
        get() = this.circle.bounds2D

    init {
        this.connectors[this.inId.value] = inPoint
        //we compute the initial radius according to the junction length and type
        val circumference = (this.junction.length.toFloat() - this.junction.type.value * 2).toFloat() * (radiusConst * 2).toFloat() + this.junction.type.value * helixDrawingWidth()
        this.radius = circumference / (2F * Math.PI).toDouble()
        circlesFromBranchSoFar.add(Triple<Point2D, Double, Ellipse2D>(this.center, this.radius, this.circle)) //this array allows to get easily the shapes already drawn for the branch in order to avoid overlaps with the shapes for this junction

        val sortedHelix = this.junction.helicesLinked.sortedBy { it.start }

        var helixRank = 0

        for (k in 1..sortedHelix.size + 1) {
            val helix = sortedHelix[(sortedHelix.indexOf(inHelix) + k) % sortedHelix.size]
            if (helix == this.inHelix) {
                break
            }

            helixRank += 1
            var inPoint: Point2D

            var outId = getOutId(helixRank)

            if (this.junction.type == JunctionType.InnerLoop && (this.junction.location.blocks[0].length < 5 || this.junction.location.blocks[1].length < 5)) {
                outId = oppositeConnectorId(inId)
            } else if (this.junction.type == JunctionType.InnerLoop) {
                when (inId) {
                    ConnectorId.sso -> outId =
                            ConnectorId.n
                    ConnectorId.so -> outId =
                            ConnectorId.n
                    ConnectorId.oso -> outId =
                            ConnectorId.n
                    ConnectorId.o ->
                        if (this.previousJunction != null && this.previousJunction.inId.value > ConnectorId.o.value && this.previousJunction.inId.value < ConnectorId.e.value) { //we want the same orientation than for the previous junction
                            outId = ConnectorId.s
                        } else {
                            outId = ConnectorId.n
                        }
                    ConnectorId.ono -> outId =
                            ConnectorId.s
                    ConnectorId.no -> outId =
                            ConnectorId.s
                    ConnectorId.nno -> outId =
                            ConnectorId.s
                    ConnectorId.n -> outId =
                            ConnectorId.s
                    ConnectorId.nne -> outId =
                            ConnectorId.s
                    ConnectorId.ne -> outId =
                            ConnectorId.s
                    ConnectorId.ene -> outId =
                            ConnectorId.s
                    ConnectorId.e ->
                        if (this.previousJunction != null && this.previousJunction.inId.value > ConnectorId.o.value && this.previousJunction.inId.value < ConnectorId.e.value) { //we want the same orientation than for the previous junction
                            outId = ConnectorId.s
                        } else {
                            outId = ConnectorId.n
                        }
                    ConnectorId.ese -> outId =
                            ConnectorId.n
                    ConnectorId.se -> outId =
                            ConnectorId.n
                    ConnectorId.sse -> outId =
                            ConnectorId.n
                    ConnectorId.s -> outId =
                            ConnectorId.n
                }
            }

            var junction: Junction? = null
            if (helix.junctionsLinked.first != null && helix.junctionsLinked.first != this.junction) {
                junction = helix.junctionsLinked.first!!
            } else if (helix.junctionsLinked.second != null && helix.junctionsLinked.second != this.junction) {
                junction = helix.junctionsLinked.second!!
            }

            if (outId != null) {
                var from: ConnectorId
                var to: ConnectorId

                if (helixRank == 1) {
                    from =
                            getConnectorId((inId.value + 1) % ConnectorId.values().size)
                } else {
                    from =
                            getConnectorId((getOutId(helixRank - 1)!!.value + 1) % ConnectorId.values().size)
                }

                if (helixRank == sortedHelix.size - 1) {
                    val newRawValue = if (inId.value - 1 < 0) ConnectorId.values().size - 1 else inId.value - 1
                    to = getConnectorId(newRawValue)
                } else {
                    val newRawValue = if (getOutId(helixRank + 1)!!.value - 1 < 0) ConnectorId.values().size - 1 else getOutId(helixRank + 1)!!.value - 1
                    to = getConnectorId(newRawValue)
                }

                var orientationsToTest = mutableListOf<ConnectorId>(outId) //we test outId first before to check the remaining orientations in order to avoid any overlap (if possible)

                var afterOrientations = mutableListOf<ConnectorId>()
                if (to != outId) {
                    afterOrientations.add(nextConnectorId(outId))
                    while (afterOrientations.last() != to) {
                        afterOrientations.add(
                                nextConnectorId(
                                        afterOrientations.last()
                                )
                        )
                    }
                }

                var beforeOrientations = mutableListOf<ConnectorId>()
                if (from != outId) {
                    beforeOrientations.add(
                            previousConnectorId(
                                    outId
                            )
                    )
                    while (beforeOrientations.last() != from) {
                        beforeOrientations.add(
                                previousConnectorId(
                                        beforeOrientations.last()
                                )
                        )
                    }
                }
                //then we alternate between the after and before positions
                orientationsToTest.addAll(
                        interleaveArrays(
                                afterOrientations,
                                beforeOrientations
                        )
                )
                var fine: Boolean
                var i = 0
                do {
                    fine = true
                    outId = orientationsToTest[i]
                    //we compute the inPoint (center - self.connectors[outId.rawValue] -[length helix*offset]- inPoint)
                    inPoint = pointsFrom(
                            this.center,
                            this.connectors[outId.value],
                            -helixDrawingLength(helix)
                    ).second

                    val nextCircumference = (junction!!.length.toFloat() - (junction.type.value) * 2).toFloat() * (radiusConst * 2) + (junction.type.value).toFloat() * helixDrawingWidth()
                    val nextRadius = nextCircumference / (2F * Math.PI)
                    val nextCenter = pointsFrom(
                            this.center,
                            this.connectors[outId.value],
                            -helixDrawingLength(helix) - nextRadius
                    ).second
                    val nextCircle = Ellipse2D.Double(nextCenter.x - nextRadius, nextCenter.y - nextRadius, nextRadius * 2F, nextRadius * 2F)

                    val nextLine = Line2D.Double(this.connectors[outId.value], inPoint)

                    var nextPoints = mutableListOf<Point2D>()
                    val points = pointsFrom(
                            this.connectors[outId.value],
                            inPoint,
                            radiusConst
                    ) //i cannot use the ends of the line since they are also in their connected circles (so overlap de facto)
                    nextPoints.add(points.first)
                    nextPoints.add(points.second)

                    if (helix.length > 2) {
                        for (i in 1..helix.length - 2) {
                            val p = pointsFrom(
                                    this.connectors[outId.value],
                                    inPoint,
                                    i.toFloat() * helixDrawingLength(
                                            helix
                                    ) / helix.length.toFloat()
                            ).second
                            nextPoints.add(p)
                        }
                    }

                    if (this.noOverlapWithCircles) {
                        outerloop@ for ((center, radius, circle) in circlesFromBranchSoFar) {
                            if (intersects(
                                            nextCenter,
                                            nextRadius + radiusConst * 2,
                                            center,
                                            radius
                                    )
                            ) {
                                fine = false
                                break@outerloop
                            }
                            for (p in nextPoints) {
                                if (circle.contains(p)) {
                                    fine = false
                                    break@outerloop
                                }
                            }
                        }
                    }

                    if (fine && this.noOverlapWithLines) {
                        outerloop@ for (points in linesFromBranchSoFar) {
                            if (!nextPoints.isEmpty() && intersects(
                                            points.first(),
                                            points.last(),
                                            nextPoints.first(),
                                            nextPoints.last()
                                    )
                            ) {
                                fine = false
                                break@outerloop
                            }
                            for (p in points)
                                if (nextCircle.contains(p)) {
                                    fine = false
                                    break@outerloop
                                }
                        }
                    }
                    i += 1
                } while (!fine && i < orientationsToTest.size)

                if (!fine) { //if we were not able to find any non-overlapping orientation, we come back to the initial orientation (which is the first one in the orientationsToTest array) and we recompute the inPoint for this orientation
                    outId = orientationsToTest.first()
                    inPoint = pointsFrom(
                            p1 = this.center,
                            p2 = this.connectors[outId.value],
                            dist = -helixDrawingLength(helix)
                    ).second
                }

                //we need to update the layout with the orientation chosen
                this.layout!!.set(helixRank - 1,
                        getConnectorId((outId!!.value + ConnectorId.values().size - inId.value) % ConnectorId.values().size)
                )
                val h = HelixDrawing(null,
                        ssDrawing,
                        helix,
                        this.connectors[outId.value],
                        inPoint
                )
                this.helices.add(
                        h
                )

                var points = mutableListOf<Point2D>(this.connectors[outId.value])
                if (helix.length > 2) {
                    for (i in 1..helix.length - 2) {
                        val p = pointsFrom(
                                p1 = this.connectors[outId.value],
                                p2 = inPoint,
                                dist = i.toFloat() * helixDrawingLength(
                                        helix
                                ) / helix.length.toFloat()
                        ).second
                        points.add(Point2D.Double(p.x, p.y))
                    }
                }
                points.add(inPoint)

                linesFromBranchSoFar.add(points)

                this.connectedJunctions[outId] = JunctionDrawing(null,
                        ssDrawing,
                        circlesFromBranchSoFar = circlesFromBranchSoFar,
                        linesFromBranchSoFar = linesFromBranchSoFar,
                        previousJunction = this,
                        inId = oppositeConnectorId(outId),
                        inPoint = inPoint,
                        inHelix = helix,
                        junction = junction
                )
            }
        }
    }

    fun junctionsFromBranch(): List<JunctionDrawing> {
        val junctions = mutableListOf<JunctionDrawing>()
        junctions.add(this)
        for ((_, j) in this.connectedJunctions) {
            junctions.addAll(j.junctionsFromBranch())
        }
        return junctions
    }

    fun helicesFromBranch(): List<HelixDrawing> {
        var helices = mutableListOf<HelixDrawing>()
        helices.addAll(this.helices)
        for ((_, j) in this.connectedJunctions) {
            helices.addAll(j.helicesFromBranch())
        }
        return helices
    }

    fun getOutId(helixRank: Int): ConnectorId? {
        when (this.junction.type) {
            JunctionType.ApicalLoop -> return null
            //the int in the array are the offset to be added to reach the next connectorId according to the type of junction. The new connector ID is the connector Id for the entry point + the offset of the corresponding helix rank (max:ConnectorId.count-1, if above we restart at 0). In the absolute layout, the connector ID for the entry point is lowerMiddle (0) and for the relative layout anything else.
            JunctionType.Flower -> return null
            else -> return ConnectorId.values().first { it.value == (this.inId.value + this.layout!!.get(helixRank - 1).value) % ConnectorId.values().size }
        }
    }

    //the previous JunctionCircle has modified its link with this one.
    fun setEntryPoint(inId: ConnectorId, inPoint: Point2D) {
        this.inId = inId
        this.connectors[this.inId.value] = inPoint
        //the (x,y) coords for the center
        this.center = centerFrom(
                this.inId,
                this.connectors[this.inId.value],
                this.radius
        )
        this.circle = Ellipse2D.Double(this.center.x - this.radius, this.center.y - this.radius, this.radius * 2.toDouble(), this.radius * 2.toDouble())

        //the (x,y) coords for the connectors
        for (i in 1..ConnectorId.values().size) {
            this.connectors[(this.inId.value + i) % ConnectorId.values().size] =
                    rotatePoint(
                            this.connectors[this.inId.value],
                            this.center,
                            i * 360.0 / ConnectorId.values().size.toDouble()
                    )
        }

        this.layout = this.layout //a trick to warn the connected circles, and so on...
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {

        this.phosphoBonds.forEach {
            it.draw(g, at)
        }

        this.helices.forEach {
            it.draw(g, at)
        }

        this.connectedJunctions.forEach { _, junction ->
            junction.draw(g, at)
        }

    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.toString())
        if (fireChildren) {
            for (p in this.phosphoBonds)
                p.configurationChange(parameter, fireChildren)
            for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
                r.configurationChange(parameter, fireChildren)
        }
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
        if (clearChildrenDrawingConfiguration) {
            for (p in this.phosphoBonds)
                p.clearConfiguration()
            for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
                r.clearConfiguration()
        }
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        super.applyTheme(theme)
        for (p in this.phosphoBonds)
            p.applyTheme(theme)
        for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
            r.applyTheme(theme)
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        TODO("Not yet implemented")
    }
}

abstract class LWSymbolDrawing(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name: String, location: Location, val inTertiaries: Boolean, type: SecondaryStructureType = SecondaryStructureType.LWSymbol) : SecondaryStructureElement(ssDrawing, parent, name, location, type) {

    protected var shape: Shape? = null

    override fun draw(g: Graphics2D, at: AffineTransform) {
        if (inTertiaries) {
            g.color = this.getColor()
            if (this.getTertiaryInteractionStyle() == DASHED)
                g.stroke = BasicStroke(
                        this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat(),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        0F,
                        floatArrayOf(this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat() * 2),
                        0F
                )
            else
                g.stroke = BasicStroke(
                        this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat(),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        } else {
            g.color = this.getColor()
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
        }
    }

    fun drawSelectionHalo(g: Graphics2D, at: AffineTransform) {
        g.color = RnartistConfig.selectionColor
        g.stroke = BasicStroke(
                this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat()+this.ssDrawing.finalZoomLevel.toFloat() * RnartistConfig.selectionSize.toFloat()/2f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        )
        if (this.shape != null)
            g.draw(at.createTransformedShape(this.shape))
    }

    abstract fun setSymbol(p1: Point2D, p2: Point2D)

    override val bounds2D: Rectangle2D?
        get() = this.shape?.bounds2D

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.toString())
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        TODO("Not yet implemented")
    }
}

abstract class WC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name: String, location: Location, inTertiaries: Boolean) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start, end)
        val (start_1, start_2) = getPerpendicular(start, start, end, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(end, start, end, symbolWidth / 2.0)
        val squarre = GeneralPath()
        squarre.moveTo(start_1.x, start_1.y)
        squarre.lineTo(end_1.x, end_1.y)
        squarre.lineTo(end_2.x, end_2.y)
        squarre.lineTo(start_2.x, start_2.y)
        squarre.lineTo(start_1.x, start_1.y)
        squarre.closePath()
        val centerX = squarre.bounds2D.centerX
        val centerY = squarre.bounds2D.centerY;
        this.shape = Ellipse2D.Double(
                centerX - symbolWidth / 2.0,
                centerY - symbolWidth / 2.0,
                symbolWidth,
                symbolWidth
        )
    }

    override fun toString(): String {
        return "Circle"
    }
}

class CisWC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean) : WC(parent, ssDrawing, "cisWC", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.fill(at.createTransformedShape(this.shape))
        }
    }
}

class TransWC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean) : WC(parent, ssDrawing, "transWC", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.draw(at.createTransformedShape(this.shape))
        }
    }

}

abstract class LeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name: String, location: Location, inTertiaries: Boolean) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start, end)
        val (start_1, start_2) = getPerpendicular(start, start, end, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(end, start, end, symbolWidth / 2.0)
        val triangle = GeneralPath()
        triangle.moveTo(start.x, start.y)
        triangle.lineTo(end_1.x, end_1.y)
        triangle.lineTo(end_2.x, end_2.y)
        triangle.lineTo(start.x, start.y)
        triangle.closePath()
        this.shape = triangle
    }

    override fun toString(): String {
        return "Triangle"
    }
}

abstract class RightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name: String, location: Location, inTertiaries: Boolean = false) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start, end)
        val (start_1, start_2) = getPerpendicular(start, start, end, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(end, start, end, symbolWidth / 2.0)
        val triangle = GeneralPath()
        triangle.moveTo(start_1.x, start_1.y)
        triangle.lineTo(start_2.x, start_2.y)
        triangle.lineTo(end.x, end.y)
        triangle.lineTo(start_1.x, start_1.y)
        triangle.closePath()
        this.shape = triangle
    }

    override fun toString(): String {
        return "Triangle"
    }

}

class CisRightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : RightSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.fill(at.createTransformedShape(this.shape))
        }
    }

}

class CisLeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : LeftSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.fill(at.createTransformedShape(this.shape))
        }

    }

}

class TransRightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : RightSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.draw(at.createTransformedShape(this.shape))
        }
    }

}

class TransLeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : LeftSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.draw(at.createTransformedShape(this.shape))
        }
    }

}

abstract class Hoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name: String, location: Location, inTertiaries: Boolean = false) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start, end)
        val (start_1, start_2) = getPerpendicular(start, start, end, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(end, start, end, symbolWidth / 2.0)
        val squarre = GeneralPath()
        squarre.moveTo(start_1.x, start_1.y)
        squarre.lineTo(end_1.x, end_1.y)
        squarre.lineTo(end_2.x, end_2.y)
        squarre.lineTo(start_2.x, start_2.y)
        squarre.lineTo(start_1.x, start_1.y)
        squarre.closePath()
        this.shape = squarre
    }

    override fun toString(): String {
        return "Squarre"
    }


}

class CisHoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : Hoogsteen(parent, ssDrawing, "cisHoogsteen", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.fill(at.createTransformedShape(this.shape))
        }
    }

}

class TransHoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false) : Hoogsteen(parent, ssDrawing, "transHoogsteen", location, inTertiaries) {

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.draw(at.createTransformedShape(this.shape))
        }
    }

}

enum class VSymbolPos {
    BOTTOM, MIDDLE, TOP
}

class LWLine(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean = false, type: SecondaryStructureType = SecondaryStructureType.LWSymbol, val vpos: VSymbolPos = VSymbolPos.MIDDLE) : LWSymbolDrawing(parent, ssDrawing, "Line", location, inTertiaries, type) {

    override fun setSymbol(p1: Point2D, p2: Point2D) {
        val distance = distance(p1, p2);
        val symbolWidth = distance
        when (this.vpos) {
            VSymbolPos.TOP -> {
                val (p1_1, p1_2) = getPerpendicular(p1, p1, p2, symbolWidth / 6.0)
                val (p2_1, p2_2) = getPerpendicular(p2, p1, p2, symbolWidth / 6.0)
                this.shape = Line2D.Double(p1_1, p2_1)
            }
            VSymbolPos.BOTTOM -> {
                val (p1_1, p1_2) = getPerpendicular(p1, p1, p2, symbolWidth / 6.0)
                val (p2_1, p2_2) = getPerpendicular(p2, p1, p2, symbolWidth / 6.0)
                this.shape = Line2D.Double(p1_2, p2_2)
            }
            else -> this.shape = Line2D.Double(p1, p2)
        }

    }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        super.draw(g, at)
        this.shape?.let {
            g.draw(at.createTransformedShape(this.shape))
        }
    }

    override fun toString(): String {
        return "Line"
    }

}

abstract class BaseBaseInteractionDrawing(parent: SecondaryStructureElement?, val interaction: BasePair, ssDrawing: SecondaryStructureDrawing, type: SecondaryStructureType) : SecondaryStructureElement(ssDrawing, parent, interaction.toString(), interaction.location, type) {

    protected var p1: Point2D? = null
    protected var p2: Point2D? = null
    var lwSymbols = mutableListOf<LWSymbolDrawing>()
    var regularSymbols = mutableListOf<LWSymbolDrawing>()

    val residue: ResidueDrawing
        get() {
            return ssDrawing.getResiduesFromAbsPositions(this.start).first()
        }
    val pairedResidue: ResidueDrawing
        get() {
            return ssDrawing.getResiduesFromAbsPositions(this.end).first()
        }
    val start: Int
        get() {
            return this.location.start
        }

    val end: Int
        get() {
            return this.location.end
        }

    val isCanonical: Boolean
        get() {
            return this.interaction.edge5 == Edge.WC && this.interaction.edge3 == Edge.WC && this.interaction.orientation == Orientation.cis && (
                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.A && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.U ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.U && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.A ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.G && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.C ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.C && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.G
                    )
        }

    val isDoublePaired: Boolean
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.G && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.C ||
                this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.type == SecondaryStructureType.C && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.type == SecondaryStructureType.G

    val selected: Boolean
        get() = !this.ssDrawing.selection.isEmpty() && this.residue.selected && this.pairedResidue.selected

    fun drawSelectionHalo(g: Graphics2D, at: AffineTransform) {
        regularSymbols.forEach { regularSymbol ->
            if (regularSymbol.getLineWidth() > 0)
                regularSymbol.drawSelectionHalo(g, at)
        }
        lwSymbols.forEach { lwSymbol ->
            if (lwSymbol.getLineWidth() > 0)
                lwSymbol.drawSelectionHalo(g, at)
        }
    }

    protected fun generateSingleSymbol(location: Location, inTertiaries: Boolean = false, edge: Edge, orientation: Orientation, right: Boolean = true): LWSymbolDrawing {
        return when (edge) {
            Edge.WC -> {
                when (orientation) {
                    Orientation.cis -> CisWC(this, this.ssDrawing, location, inTertiaries)
                    Orientation.trans -> TransWC(this, this.ssDrawing, location, inTertiaries)
                    else -> CisWC(this, this.ssDrawing, location, inTertiaries)
                }
            }
            Edge.Hoogsteen -> {
                when (orientation) {
                    Orientation.cis -> CisHoogsteen(this, this.ssDrawing, location, inTertiaries)
                    Orientation.trans -> TransHoogsteen(this, this.ssDrawing, location, inTertiaries)
                    else -> CisHoogsteen(this, this.ssDrawing, location, inTertiaries)
                }
            }
            Edge.Sugar -> {
                when (orientation) {
                    Orientation.cis -> if (right) CisRightSugar(this, this.ssDrawing, location, inTertiaries) else CisLeftSugar(this, this.ssDrawing, location, inTertiaries)
                    Orientation.trans -> if (right) TransRightSugar(this, this.ssDrawing, location, inTertiaries) else TransLeftSugar(this, this.ssDrawing, location, inTertiaries)
                    else -> if (right) CisRightSugar(this, this.ssDrawing, location, inTertiaries) else CisLeftSugar(this, this.ssDrawing, location, inTertiaries)
                }
            }
            else -> { //if edge unknown
                LWLine(this, this.ssDrawing, location, inTertiaries)
            }
        }
    }

    override fun toString(): String {
        return this.interaction.toString()
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.name)
        if (fireChildren) {
            this.residue.configurationChange(parameter, fireChildren)
            this.pairedResidue.configurationChange(parameter, fireChildren)
            for (s in this.regularSymbols)
                s.configurationChange(parameter, fireChildren)
            for (s in this.lwSymbols)
                s.configurationChange(parameter, fireChildren)
        }

    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
        if (clearChildrenDrawingConfiguration) {
            this.residue.clearConfiguration()
            this.pairedResidue.clearConfiguration()
            for (s in this.regularSymbols)
                s.clearConfiguration()
            for (s in this.lwSymbols)
                s.clearConfiguration()
        }
    }

    override fun applyTheme(theme: Map<String, Map<String, String>>) {
        super.applyTheme(theme)
        this.residue.applyTheme(theme)
        this.pairedResidue.applyTheme(theme)
        for (s in this.regularSymbols) {
            s.applyTheme(theme)
        }
        for (s in this.lwSymbols) {
            s.applyTheme(theme)
        }
    }
}

class SecondaryInteractionDrawing(parent: SecondaryStructureElement?, interaction: BasePair, ssDrawing: SecondaryStructureDrawing) : BaseBaseInteractionDrawing(parent, interaction, ssDrawing, SecondaryStructureType.SecondaryInteraction) {

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion(this.pairedResidue.bounds2D)
            return bounds
        }


    init {
        this.regularSymbols.add(LWLine(this, this.ssDrawing, this.location, false, type = SecondaryStructureType.RegularSymbol))
        if (this.isCanonical) {
            if (isDoublePaired) {
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false, vpos = VSymbolPos.TOP))
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false, vpos = VSymbolPos.BOTTOM))
            } else
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
        } else {
            if (this.interaction.edge5 == this.interaction.edge3) { //uniq central symbol
                //+++++left symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, Location(this.location.start), false))
                //++++++middle symbol
                this.lwSymbols.add(this.generateSingleSymbol(this.location, false, this.interaction.edge5, this.interaction.orientation))
                //+++++right symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, Location(this.location.end), false))
            } else {
                //+++++left symbol
                this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.start), false, this.interaction.edge5, this.interaction.orientation, right = false))
                //++++++middle symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                //+++++right symbol
                this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.end), false, this.interaction.edge3, this.interaction.orientation))
            }
        }
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        if (residue.updated) {//the paired residue is de facto updated too
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center

            if (center1 != null && center2 != null) {
                val shift = radiusConst + this.getLineShift() + this.residue.getLineWidth() / 2.0 + this.getLineWidth() / 2.0
                if (distance(center1, center2) > 2 * shift) {
                    val points = pointsFrom(
                            center1,
                            center2,
                            shift
                    )
                    this.p1 = points.first
                    this.p2 = points.second
                    regularSymbols.forEach { nonLwSymbol ->
                        nonLwSymbol.setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                    }
                    if (this.isCanonical) {
                        if (isDoublePaired) {
                            this.lwSymbols[0].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                            this.lwSymbols[1].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                        } else {
                            this.lwSymbols[0].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                        }
                    } else {
                        val distance = distance(this.p1 as Point2D, this.p2 as Point2D)
                        val symbolWidth = distance / 3.0
                        if (interaction.edge5 == interaction.edge3) {
                            val (p1_inner, p2_inner) = pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth / 2.0)
                            this.lwSymbols[0].setSymbol(p1!!, p1_inner)
                            this.lwSymbols[2].setSymbol(p2_inner, p2!!)
                            this.lwSymbols[1].setSymbol(p1_inner, p2_inner)

                        } else {
                            val (p1_inner, p2_inner) = pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth + symbolWidth / 4.0)
                            this.lwSymbols[0].setSymbol(p1!!, p1_inner)
                            this.lwSymbols[2].setSymbol(p2_inner, p2!!)
                            this.lwSymbols[1].setSymbol(p1_inner, p2_inner)
                        }
                    }
                }
            }
        }

        regularSymbols.forEach { regularSymbol ->
            if (regularSymbol.getLineWidth() > 0) {
                val _previousColor = g.color
                val _previousStroke = g.stroke
                regularSymbol.draw(g, at)
                g.color = _previousColor
                g.stroke = _previousStroke
            }
        }

        this.lwSymbols.forEach { lwSymbol ->
            if (lwSymbol.getLineWidth() > 0) {
                val _previousColor = g.color
                val _previousStroke = g.stroke
                lwSymbol.draw(g, at)
                g.color = _previousColor
                g.stroke = _previousStroke
            }
        }
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val center1 = this.ssDrawing.residues[this.interaction.start - 1].center
        val center2 = this.ssDrawing.residues[this.interaction.end - 1].center
        if (center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x + transX},${p1.y + transY}l${p2.x - p1.x},${p2.y - p1.y}" style="fill:none;stroke:rgb(${this.getColor().red}, ${this.getColor().green}, ${this.getColor().blue});stroke-width:${this.getLineWidth()};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }
}

class TertiaryInteractionDrawing(parent: PKnotDrawing? = null, interaction: BasePair, ssDrawing: SecondaryStructureDrawing) : BaseBaseInteractionDrawing(parent, interaction, ssDrawing, SecondaryStructureType.TertiaryInteraction) {

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion(this.pairedResidue.bounds2D)
            return bounds
        }

    init {
        this.regularSymbols.add(LWLine(this, this.ssDrawing, this.location, true, type = SecondaryStructureType.RegularSymbol))
        //+++++left symbol
        this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.start), true, this.interaction.edge5, this.interaction.orientation, right = false))
        //++++++middle symbol
        this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, true))
        //+++++right symbol
        this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.end), true, this.interaction.edge3, this.interaction.orientation))
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {

        if (this.residue.updated || this.pairedResidue.updated) {
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center
            if (center1 != null && center2 != null) {

                val shift = radiusConst + this.residue.getLineWidth().toDouble() / 2.0
                if (distance(center1, center2) > 2 * shift) {
                    val (p1, p2) = pointsFrom(
                            center1,
                            center2,
                            shift
                    )

                    regularSymbols.forEach { nonLwSymbol ->
                        nonLwSymbol.setSymbol(p1, p2)
                    }

                    //LW Symbols now
                    this.p1 = pointsFrom(
                            center1,
                            center2,
                            shift
                    ).first

                    this.p2 = pointsFrom(
                            center1,
                            center2,
                            shift + radiusConst * 1.5
                    ).first
                    val forLine_1 = this.p2!!

                    this.p1?.let { p1 ->
                        this.p2?.let { p2 ->
                            this.lwSymbols[0].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                        }
                    }

                    this.p1 = pointsFrom(
                            center1,
                            center2,
                            shift
                    ).second

                    this.p2 = pointsFrom(
                            center1,
                            center2,
                            shift + radiusConst * 1.5
                    ).second

                    val forLine_2 = this.p2!!

                    this.p1?.let { p1 ->
                        this.p2?.let { p2 ->
                            this.lwSymbols[2].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                        }
                    }

                    //+++++ central line linking the two symbols
                    this.lwSymbols[1].setSymbol(forLine_1, forLine_2)

                }
            }
        }

        if (getLineWidth() > 0) {
            val previousColor = g.color
            g.color = getColor()

            regularSymbols.forEach { regularSymbol ->
                if (regularSymbol.getLineWidth() > 0) {
                    val _previousColor = g.color
                    regularSymbol.draw(g, at)
                    g.color = _previousColor
                }
            }
            lwSymbols.forEach { lwSymbol ->
                if (lwSymbol.getLineWidth() > 0) {
                    val _previousColor = g.color
                    lwSymbol.draw(g, at)
                    g.color = _previousColor
                }
            }

            g.color = previousColor
        }
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val center1 = this.ssDrawing.residues[this.interaction.start - 1].center
        val center2 = this.ssDrawing.residues[this.interaction.end - 1].center
        if (this.drawingConfiguration.lineWidth != 0.0 && center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x + transX},${p1.y + transY}l${p2.x - p1.x},${p2.y - p1.y}" style="fill:none;stroke:rgb(${this.getColor().red}, ${this.getColor().green}, ${this.getColor().blue});stroke-width:${this.getLineWidth()};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }

}

class PhosphodiesterBondDrawing(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location: Location) : SecondaryStructureElement(ssDrawing, parent, "PhosphoDiester Bond", location, SecondaryStructureType.PhosphodiesterBond) {

    val residue: ResidueDrawing
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.start).first()

    val nextResidue: ResidueDrawing
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.end).first()

    val start: Int
        get() {
            return this.location.start
        }

    val end: Int
        get() {
            return this.location.end
        }

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion(this.nextResidue.bounds2D)
            return bounds
        }

    val selected: Boolean
        get() = this.residue in this.ssDrawing.selection && this.nextResidue in this.ssDrawing.selection

    override fun draw(g: Graphics2D, at: AffineTransform) {
        if (this.getLineWidth() > 0) {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(this.ssDrawing.finalZoomLevel.toFloat() * this.getLineWidth().toFloat())
            g.color = this.getColor()
            val center1 = this.residue.center
            val center2 = this.nextResidue.center
            if (center1 != null && center2 != null) {
                val (p1, p2) = pointsFrom(
                        center1,
                        center2,
                        radiusConst + deltaPhosphoShift
                )
                if (!this.residue.circle!!.contains(p2) && distance(
                                p1,
                                p2
                        ) > spaceBetweenResidues / 2.0) {
                    g.draw(at.createTransformedShape(Line2D.Double(p1, p2)))
                }
            }
            g.stroke = previousStroke
        }
    }

    override fun asSVG(indentChar: String, indentLevel: Int, transX: Double, transY: Double): String {
        val center1 = this.residue.center
        val center2 = this.nextResidue.center
        if (center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x + transX},${p1.y + transY}l${p2.x - p1.x},${p2.y - p1.y}" style="fill:none;stroke:rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue});stroke-width:${this.getLineWidth()};" />""" + "\n"
        }
        return ""
    }

    override fun configurationChange(parameter: DrawingConfigurationParameter, fireChildren: Boolean) {
        this.drawingConfiguration.params.remove(parameter.toString())
    }

    override fun clearConfiguration(clearChildrenDrawingConfiguration: Boolean) {
        this.drawingConfiguration.params.clear()
    }
}

enum class SecondaryStructureType {
    Full2D, A, U, G, C, X, SecondaryInteraction, TertiaryInteraction, PhosphodiesterBond, Helix, PKnot, Junction, SingleStrand, LWSymbol, RegularSymbol, Numbering
}

enum class ConnectorId(val value: Int) {
    s(0),
    sso(1),
    so(2),
    oso(3),
    o(4),
    ono(5),
    no(6),
    nno(7),
    n(8),
    nne(9),
    ne(10),
    ene(11),
    e(12),
    ese(13),
    se(14),
    sse(15);
}

fun getConnectorId(value: Int): ConnectorId = ConnectorId.values().first { it.value == value }

fun nextConnectorId(c: ConnectorId): ConnectorId = ConnectorId.values().first { it.value == (c.value + 1) % ConnectorId.values().size }

fun previousConnectorId(c: ConnectorId): ConnectorId = if (c.value - 1 < 0) ConnectorId.values().first { it.value == ConnectorId.values().size - 1 } else ConnectorId.values().first { it.value == c.value - 1 }

fun oppositeConnectorId(c: ConnectorId): ConnectorId = ConnectorId.values().first { it.value == (c.value + ConnectorId.values().size / 2) % ConnectorId.values().size }

typealias Layout = List<ConnectorId>

/**
Each layout direction will be reevaluated according to the inId and the helixRank. The layout is always RELATIVE to the inId.
 **/
val defaultLayouts = mapOf<JunctionType, Layout>(
        Pair(JunctionType.InnerLoop, listOf(ConnectorId.n)),
        Pair(
                JunctionType.ThreeWay, listOf(
                ConnectorId.n,
                ConnectorId.e
        )),
        Pair(
                JunctionType.FourWay, listOf(
                ConnectorId.o,
                ConnectorId.n,
                ConnectorId.e
        )),
        Pair(
                JunctionType.FiveWay, listOf(
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.n,
                ConnectorId.e
        )),
        Pair(
                JunctionType.SixWay, listOf(
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.n,
                ConnectorId.ne,
                ConnectorId.e
        )),
        Pair(
                JunctionType.SevenWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.n,
                ConnectorId.ne,
                ConnectorId.e
        )),
        Pair(
                JunctionType.EightWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.n,
                ConnectorId.ne,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.NineWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.ne,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.TenWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.ElevenWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.TwelveWay, listOf(
                ConnectorId.so,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.ene,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.ThirteenWay, listOf(
                ConnectorId.so,
                ConnectorId.oso,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.ene,
                ConnectorId.e,
                ConnectorId.se
        )),
        Pair(
                JunctionType.FourteenWay, listOf(
                ConnectorId.so,
                ConnectorId.oso,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.ene,
                ConnectorId.e,
                ConnectorId.ese,
                ConnectorId.se
        )),
        Pair(
                JunctionType.FifthteenWay, listOf(
                ConnectorId.sso,
                ConnectorId.so,
                ConnectorId.oso,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.ene,
                ConnectorId.e,
                ConnectorId.ese,
                ConnectorId.se
        )),
        Pair(
                JunctionType.SixteenWay, listOf(
                ConnectorId.sso,
                ConnectorId.so,
                ConnectorId.oso,
                ConnectorId.o,
                ConnectorId.ono,
                ConnectorId.no,
                ConnectorId.nno,
                ConnectorId.n,
                ConnectorId.nne,
                ConnectorId.ne,
                ConnectorId.ene,
                ConnectorId.e,
                ConnectorId.ese,
                ConnectorId.se,
                ConnectorId.sse
        )))

typealias ColorScheme = Map<SecondaryStructureType, Color>

val Fall: ColorScheme = mapOf(
        Pair(SecondaryStructureType.A, Color.BLACK),
        Pair(SecondaryStructureType.U, Color.BLACK),
        Pair(SecondaryStructureType.G, Color.BLACK),
        Pair(SecondaryStructureType.C, Color.BLACK),
        Pair(SecondaryStructureType.X, Color.BLACK),
        Pair(SecondaryStructureType.SecondaryInteraction, Color.BLACK),
        Pair(SecondaryStructureType.TertiaryInteraction, Color.BLACK),
        Pair(SecondaryStructureType.PhosphodiesterBond, Color.BLACK)
)

/**
Compute the center of a circle according to the entry point
 **/
fun centerFrom(inId: ConnectorId, inPoint: Point2D, radius: Double): Point2D {
    when (inId) {
        ConnectorId.s -> return Point2D.Double(inPoint.x, inPoint.y - radius)
        ConnectorId.sso -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (-3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (-3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.so -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (-2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (-2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.oso -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (-360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (-360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.o -> return Point2D.Double(inPoint.x + radius, inPoint.y)
        ConnectorId.ono -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.no -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.nno -> return Point2D.Double(inPoint.x + adjacentSideFrom(
                (3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y + oppositeSideFrom(
                (3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.n -> return Point2D.Double(inPoint.x, inPoint.y + radius)
        ConnectorId.nne -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (-3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (-3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.ne -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (-2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (-2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.ene -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (-360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (-360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.e -> return Point2D.Double(inPoint.x - radius, inPoint.y)
        ConnectorId.ese -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.se -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (2 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
        ConnectorId.sse -> return Point2D.Double(inPoint.x - adjacentSideFrom(
                (3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        ), inPoint.y - oppositeSideFrom(
                (3 * 360 / ConnectorId.values().size).toDouble(),
                radius
        )
        )
    }
}

fun rotatePoint(start: Point2D, center: Point2D, degrees: Double): Point2D {
    //we set the rotation
    val rot = AffineTransform()
    rot.setToRotation(degrees * degreesToRadians, center.x, center.y)
    //we get the rotated point with this transformation
    val pointRot = rot.transform(start, null)
    return pointRot
}

fun <T> interleaveArrays(first: List<T>, second: List<T>): List<T> {
    val commonLength = Math.min(first.size, second.size)
    return (first zip second).flatMap { it.toList() } + first.subList(commonLength, first.size) + second.subList(commonLength, second.size)
}

fun ccw(A: Point2D, B: Point2D, C: Point2D): Boolean {
    return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x)
}

fun intersects(A: Point2D, B: Point2D, C: Point2D, D: Point2D): Boolean {
    return ccw(
            A,
            C,
            D
    ) != ccw(
            B,
            C,
            D
    ) && ccw(
            A,
            B,
            C
    ) != ccw(A, B, D)
}

fun intersects(center1: Point2D, radius1: Double, center2: Point2D, radius2: Double): Boolean {
    return hypot(center1.x - center2.x, center1.y - center2.y) <= (radius1 + radius2);
}

/**
Return two new points far from p1 and b2 by dist.
 **/
fun pointsFrom(p1: Point2D, p2: Point2D, dist: Double): Pair<Point2D, Point2D> {
    val angle = angleFrom(
            distance(
                    Point2D.Double(p1.x, p1.y),
                    Point2D.Double(p1.x, p2.y)
            ),
            distance(
                    Point2D.Double(p1.x, p2.y),
                    Point2D.Double(p2.x, p2.y)
            )
    )

    val newX1: Double
    val newX2: Double
    val newY1: Double
    val newY2: Double

    if (p1.x >= p2.x) {
        newX2 = p2.x + adjacentSideFrom(angle, dist)
        newX1 = p1.x - adjacentSideFrom(angle, dist)
    } else {
        newX2 = p2.x - adjacentSideFrom(angle, dist)
        newX1 = p1.x + adjacentSideFrom(angle, dist)
    }

    if (p1.y >= p2.y) {
        newY2 = p2.y + oppositeSideFrom(angle, dist)
        newY1 = p1.y - oppositeSideFrom(angle, dist)
    } else {
        newY2 = p2.y - oppositeSideFrom(angle, dist)
        newY1 = p1.y + oppositeSideFrom(angle, dist)
    }

    return Pair(Point2D.Double(newX1, newY1), Point2D.Double(newX2, newY2))
}

fun distance(p1: Point2D, p2: Point2D): Double {
    val h = p2.x - p1.x
    val v = p2.y - p1.y
    return Math.sqrt(h * h + v * v)
}

fun angleFrom(oppositeSide: Double, adjacentSide: Double): Double {
    return Math.atan(oppositeSide / adjacentSide) * radiansToDegrees
}

fun angleFrom(p1: Point2D, p2: Point2D, p3: Point2D): Double {
    val a = distance(p1, p2)
    val b = distance(p2, p3)
    val c = distance(p1, p3)
    return Math.acos((a * a + c * c - b * b) / (2 * a * c)) * radiansToDegrees
}

fun adjacentSideFrom(degrees: Double, hypotenuse: Double): Double {
    return Math.cos(degrees * degreesToRadians) * hypotenuse
}

fun oppositeSideFrom(degrees: Double, hypotenuse: Double): Double {
    return Math.sin(degrees * degreesToRadians) * hypotenuse
}

fun crossProduct(sharedPoint: Point2D, p2: Point2D, p3: Point2D): Double {
    val a1 = p2.x - sharedPoint.x
    val a2 = p2.y - sharedPoint.y
    val b1 = p3.x - sharedPoint.x
    val b2 = p3.y - sharedPoint.y
    return a1 * b2 - a2 * b1
}

fun getPerpendicular(p0: Point2D, p1: Point2D, p2: Point2D, distance: Double): Pair<Point2D, Point2D> {
    val angle = angleFrom(p1.y - p2.y, p1.x - p2.x)
    if (angle < 0) {
        return Pair<Point2D, Point2D>(Point2D.Double(p0.x + oppositeSideFrom(
                angle,
                distance
        ), p0.y - adjacentSideFrom(angle, distance)
        ), Point2D.Double(p0.x - oppositeSideFrom(
                angle,
                distance
        ), p0.y + adjacentSideFrom(angle, distance)
        ))
    } else {
        return Pair<Point2D, Point2D>(Point2D.Double(p0.x - oppositeSideFrom(
                angle,
                distance
        ), p0.y + adjacentSideFrom(angle, distance)
        ), Point2D.Double(p0.x + oppositeSideFrom(
                angle,
                distance
        ), p0.y - adjacentSideFrom(angle, distance)
        ))
    }
}

fun getAWTColor(htmlColor: String, alpha:Int = 255): Color? {
    val r: Int
    val g: Int
    val b: Int
    require(!(htmlColor.length != 7 || htmlColor[0] != '#')) { "$htmlColor is not an HTML color string" }
    r = htmlColor.substring(1, 3).toInt(16)
    g = htmlColor.substring(3, 5).toInt(16)
    b = htmlColor.substring(5, 7).toInt(16)
    return Color(r, g, b, alpha)
}

fun getHTMLColorString(color: Color): String {
    val red = Integer.toHexString(color.red)
    val green = Integer.toHexString(color.green)
    val blue = Integer.toHexString(color.blue)
    return "#" +
            (if (red.length == 1) "0$red" else red) +
            (if (green.length == 1) "0$green" else green) +
            if (blue.length == 1) "0$blue" else blue
}