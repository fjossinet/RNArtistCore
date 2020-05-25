package io.github.fjossinet.rnartist.core.model

import io.github.fjossinet.rnartist.core.model.RnartistConfig.defaultThemeParams
import java.awt.*
import java.awt.Color
import java.awt.geom.*
import java.awt.image.BufferedImage
import kotlin.math.hypot

//parameters that can be modified
val radiusConst:Double = 7.0
val spaceBetweenResidues:Double = 5.0
val deltaHelixWidth:Double = 5.0
val deltaPhosphoShift:Double = 0.0
val deltaLWSymbols:Double = 1.2

val minimalCircumference:Float = 360F/((ConnectorId.values().size)* radiusConst *3).toFloat()
val minimalRadius:Float = minimalCircumference /(2F*Math.PI).toFloat()
val radiansToDegrees = 180 / Math.PI
val degreesToRadians = Math.PI / 180

fun helixDrawingLength(h: Helix):Double {
    return (h.length-1).toDouble() * radiusConst * 2.0 + (h.length-1).toDouble() * spaceBetweenResidues
}

fun helixDrawingWidth():Double {
    return radiusConst * deltaHelixWidth
}

class Project(var secondaryStructure: SecondaryStructure, var tertiaryStructure: TertiaryStructure?, var theme:Map<String,String>, var graphicsContext:Map<String,String>) {

}

class WorkingSession() {

    var viewX = 0.0
    var viewY = 0.0
    var finalZoomLevel = 1.0
    var screen_capture = false
    var screen_capture_area: Rectangle2D? = null
    val selection = mutableListOf<SecondaryStructureElement>()
    val selectionBounds:Rectangle2D?
        get() {
            var selectionBounds:Rectangle2D? = null
            for (element in this.selection) {
                if (selectionBounds == null)
                    selectionBounds = element.bounds2D
                else
                    selectionBounds = selectionBounds.createUnion(element.bounds2D)
            }
            return selectionBounds
        }
    val selectedAbsPositions:List<Int>
        get() {
            val selectedAbsPositions = mutableListOf<Int>()
            if (this.selection.isEmpty())
                return selectedAbsPositions
            selectedAbsPositions.addAll(this.selection.first().location.positions)
            for (i in 1 until this.selection.size)
                selectedAbsPositions.addAll(this.selection.get(i).location.positions)
            return selectedAbsPositions.distinct().sorted()
        }

    fun clear() {
        viewX = 0.0
        viewY = 0.0
        finalZoomLevel = 1.0
        screen_capture = false
        screen_capture_area = null
        selection.clear()
    }

    fun moveView(transX: Double, transY: Double) {
        viewX += transX
        viewY += transY
    }

    fun setZoom(zoomFactor: Double) {
        finalZoomLevel *= zoomFactor
    }
}

interface ThemeConfigurator {
    fun addThemeConfiguratorListener(listener:ThemeConfiguratorListener)
    fun removeThemeConfiguratorListener(listener:ThemeConfiguratorListener)
    fun fireThemeChange(param:ThemeParameter, newValue:String)
    fun fireThemeChange(param:ThemeParameter, newValue:Int)
    fun fireThemeChange(param:ThemeParameter, newValue:Double)
    fun fireThemeChange(param:ThemeParameter, newValue:Color)
    fun removeThemeConfiguratorListeners()
}

abstract class AbstractThemeConfigurator():ThemeConfigurator {

    protected val themeConfiguratorListeners = mutableListOf<ThemeConfiguratorListener>()
    var muted:Boolean = false //to make theme configuration we don't want to fire

    override fun addThemeConfiguratorListener(listener:ThemeConfiguratorListener) {
        this.themeConfiguratorListeners.add(listener)
    }

    override fun removeThemeConfiguratorListener(listener:ThemeConfiguratorListener) {
        this.themeConfiguratorListeners.remove(listener)
    }

    override fun removeThemeConfiguratorListeners() {
        this.themeConfiguratorListeners.clear()
    }

    override fun fireThemeChange(param: ThemeParameter, newValue: String) {
            this.themeConfiguratorListeners.forEach {
                it.newParameterValue(param, newValue)
            }
    }

    override fun fireThemeChange(param: ThemeParameter, newValue: Int) {
            this.themeConfiguratorListeners.forEach {
                it.newParameterValue(param, newValue)
            }
    }

    override fun fireThemeChange(param: ThemeParameter, newValue: Double) {
            this.themeConfiguratorListeners.forEach {
                it.newParameterValue(param, newValue)
            }
    }

    override fun fireThemeChange(param: ThemeParameter, newValue: Color) {
            this.themeConfiguratorListeners.forEach {
                it.newParameterValue(param, newValue)
            }
    }

}

fun transparentColor(source:Color, alpha:Int) = Color(source.red, source.green, source.blue, alpha);

@JvmField
var DASHED = "dashed"
@JvmField
var SOLID = "solid"

enum class ThemeParameter {
    AColor, AChar, UColor, UChar, GColor, GChar, CColor, CChar, XColor, XChar, SecondaryColor, TertiaryColor, ResidueCharOpacity, HaloWidth, TertiaryOpacity, PhosphodiesterWidth, SecondaryInteractionShift, SecondaryInteractionWidth, TertiaryInteractionWidth, TertiaryInteractionStyle, ResidueBorder, FontName, DeltaXRes, DeltaYRes, DeltaFontSize, DisplayLWSymbols
}

interface ThemeConfiguratorListener {
    fun newParameterValue(parameter: ThemeParameter, value:String)
    fun newParameterValue(parameter: ThemeParameter, value:Color)
    fun newParameterValue(parameter: ThemeParameter, value:Double)
    fun newParameterValue(parameter: ThemeParameter, value:Int)
}

class Theme(defaultParams:MutableMap<String,String> = defaultThemeParams) {

    val params:MutableMap<String,String> = mutableMapOf()

    var haloWidth:Double? = null
        get() = this.params.get(ThemeParameter.HaloWidth.toString())?.toDouble()

    var tertiaryOpacity:Int? = null
        get() = this.params.get(ThemeParameter.TertiaryOpacity.toString())?.toInt()

    var residueCharOpacity:Int? = null
        get() = this.params.get(ThemeParameter.ResidueCharOpacity.toString())?.toInt()

    var tertiaryInteractionStyle:String? = null
        get() = this.params.get(ThemeParameter.TertiaryInteractionStyle.toString())

    var residueBorder:Double? = null
        get() = this.params.get(ThemeParameter.ResidueBorder.toString())?.toDouble()

    var secondaryInteractionShift:Double? = null
        get() = this.params.get(ThemeParameter.SecondaryInteractionShift.toString())?.toDouble()

    var secondaryInteractionWidth:Double? = null
        get() = this.params.get(ThemeParameter.SecondaryInteractionWidth.toString())?.toDouble()

    var tertiaryInteractionWidth:Double? = null
        get() = this.params.get(ThemeParameter.TertiaryInteractionWidth.toString())?.toDouble()

    var phosphoDiesterWidth:Double? = null
        get() = this.params.get(ThemeParameter.PhosphodiesterWidth.toString())?.toDouble()

    var deltaXRes:Int? = null
        get() = this.params.get(ThemeParameter.DeltaXRes.toString())?.toInt()

    var deltaYRes:Int? = null
        get() = this.params.get(ThemeParameter.DeltaYRes.toString())?.toInt()

    var deltaFontSize:Int? = null
        get() = this.params.get(ThemeParameter.DeltaFontSize.toString())?.toInt()

    var AColor:Color? = null
        get() = this.params.get(ThemeParameter.AColor.toString())?.let { getAWTColor(it) }

    var AChar:Color? = null
        get() = this.params.get(ThemeParameter.AChar.toString())?.let { getAWTColor(it) }

    var UColor:Color? = null
        get() = this.params.get(ThemeParameter.UColor.toString())?.let { getAWTColor(it) }

    var UChar:Color? = null
        get() = this.params.get(ThemeParameter.UChar.toString())?.let { getAWTColor(it) }

    var GColor:Color? = null
        get() = this.params.get(ThemeParameter.GColor.toString())?.let { getAWTColor(it) }

    var GChar:Color? = null
        get() = this.params.get(ThemeParameter.GChar.toString())?.let { getAWTColor(it) }

    var CColor:Color? = null
        get() = this.params.get(ThemeParameter.CColor.toString())?.let { getAWTColor(it) }

    var CChar:Color? = null
        get() = this.params.get(ThemeParameter.CChar.toString())?.let { getAWTColor(it) }

    var XColor:Color? = null
        get() = this.params.get(ThemeParameter.XColor.toString())?.let { getAWTColor(it) }

    var XChar:Color? = null
        get() = this.params.get(ThemeParameter.XChar.toString())?.let { getAWTColor(it) }

    var SecondaryColor:Color? = null
        get() = this.params.get(ThemeParameter.SecondaryColor.toString())?.let { getAWTColor(it) }

    var TertiaryColor:Color? = null
        get() = this.params.get(ThemeParameter.TertiaryColor.toString())?.let { getAWTColor(it) }

    var fontName:String? = null
        get() = this.params.get(ThemeParameter.FontName.toString())

    var displayLWSymbols:Boolean? = null
        get() = this.params.get(ThemeParameter.DisplayLWSymbols.toString())?.equals("yes")

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
    var displayResidueNames = true
    var fitToResiduesBetweenBranches = true
    var fontStyle = Font.PLAIN
    var fontSize = 12 //not user-defined. Computed by the function computeOptimalFontSize()
    var quickDraw = false

    init {
        defaultParams.forEach { (k,v) ->
            this.params.put(k,v)
        }
    }

    fun clear() {
        this.params.clear()
    }

}

fun computeOptimalFontSize(g: Graphics2D, gc: WorkingSession, theme: Theme, title: String, width: Double, height: Double): Int {
    var dimension: Dimension?
    var fontSize = (100*gc.finalZoomLevel).toInt() //initial value
    do {
        fontSize--
        val font = Font(theme.fontName, theme.fontStyle, fontSize)
        dimension = getStringBoundsRectangle2D(g, title, font)
    } while (dimension!!.width >= width-width*0.5+width*theme.deltaFontSize!!.toDouble()/20.0 && dimension.height >= height-height*0.5+height*theme.deltaFontSize!!.toDouble()/20.0)
    return fontSize;
}

fun getStringBoundsRectangle2D(g: Graphics2D, title: String, font: Font): Dimension {
    g.font = font
    val fm = g.fontMetrics
    val lm = font.getLineMetrics(title, g.fontRenderContext);
    val r = fm.getStringBounds(title, g)
    return Dimension(r.getWidth().toInt(), (lm.ascent-lm.descent).toInt())
}

abstract class SecondaryStructureElement(val ssDrawing:SecondaryStructureDrawing, var parent:SecondaryStructureElement?, val name:String, val location:Location):ThemeConfiguratorListener {
    //the own theme for this object. Allow to overwrite the parameters that we want to be specific for this structural element.
    var theme = Theme(defaultParams = mutableMapOf())

    abstract val bounds2D:Rectangle2D?

    abstract val type:String

    abstract val isSelected:Boolean

    //return a new Theme merging the parameters defined in this element an all its parents
    val mergedTheme: Theme
        get() {
            val params: MutableMap<String, String> = mutableMapOf(
                    ThemeParameter.AColor.toString() to "" + getHTMLColorString(this.getAColor()),
                    ThemeParameter.AChar.toString() to "" + getHTMLColorString(this.getAChar()),
                    ThemeParameter.UColor.toString() to "" + getHTMLColorString(this.getUColor()),
                    ThemeParameter.UChar.toString() to "" + getHTMLColorString(this.getUChar()),
                    ThemeParameter.GColor.toString() to "" + getHTMLColorString(this.getGColor()),
                    ThemeParameter.GChar.toString() to "" + getHTMLColorString(this.getGChar()),
                    ThemeParameter.CColor.toString() to "" + getHTMLColorString(this.getCColor()),
                    ThemeParameter.CChar.toString() to "" + getHTMLColorString(this.getCChar()),
                    ThemeParameter.XColor.toString() to "" + getHTMLColorString(this.getXColor()),
                    ThemeParameter.SecondaryColor.toString() to "" + getHTMLColorString(this.getSecondaryColor()),
                    ThemeParameter.TertiaryColor.toString() to "" + getHTMLColorString(this.getTertiaryColor()),
                    ThemeParameter.TertiaryOpacity.toString() to "" + this.getTertiaryOpacity(),
                    ThemeParameter.TertiaryOpacity.toString() to "" + this.getTertiaryOpacity(),
                    ThemeParameter.ResidueCharOpacity.toString() to "" + this.getResidueCharOpacity(),
                    ThemeParameter.PhosphodiesterWidth.toString() to "" + this.getPhosphodiesterWidth(),
                    ThemeParameter.SecondaryInteractionWidth.toString() to "" + this.getSecondaryInteractionWidth(),
                    ThemeParameter.SecondaryInteractionShift.toString() to "" + this.getSecondaryInteractionShift(),
                    ThemeParameter.TertiaryInteractionWidth.toString() to "" + this.getTertiaryInteractionWidth(),
                    ThemeParameter.HaloWidth.toString() to "" + this.getHaloWidth(),
                    ThemeParameter.ResidueBorder.toString() to "" + this.getResidueBorder(),
                    ThemeParameter.TertiaryInteractionStyle.toString() to this.getTertiaryInteractionStyle(),
                    ThemeParameter.DeltaXRes.toString() to "" + this.getDeltaXRes(),
                    ThemeParameter.DeltaYRes.toString() to "" + this.getDeltaYRes(),
                    ThemeParameter.DeltaFontSize.toString() to "" + this.getDeltaFontSize(),
                    ThemeParameter.FontName.toString() to this.getFontName(),
                    ThemeParameter.DisplayLWSymbols.toString() to if (this.displayLWSymbols()) "yes" else "no"
            )
            return Theme(params)
        }

    /**
     *  this function clears all the params for the themes attached to this element and any secondary structure subelement.
     */
    abstract fun clearThemes();

    fun getAColor():Color {
        return if (this.theme.AColor != null) this.theme.AColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getAColor() else ssDrawing.theme.AColor!!
    }

    fun getUColor():Color {
        return if (this.theme.UColor != null) this.theme.UColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getUColor() else ssDrawing.theme.UColor!!
    }

    fun getGColor():Color {
        return if (this.theme.GColor != null) this.theme.GColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getGColor() else ssDrawing.theme.GColor!!
    }

    fun getCColor():Color {
        return if (this.theme.CColor != null) this.theme.CColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getCColor() else ssDrawing.theme.CColor!!
    }

    fun getXColor():Color {
        return if (this.theme.XColor != null) this.theme.XColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getXColor() else ssDrawing.theme.XColor!!
    }

    fun getHaloWidth():Double {
        return if (this.theme.haloWidth != null) this.theme.haloWidth!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getHaloWidth() else ssDrawing.theme.haloWidth!!
    }

    fun getResidueBorder():Double {
        return if (this.theme.residueBorder != null) this.theme.residueBorder!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getResidueBorder() else ssDrawing.theme.residueBorder!!
    }

    fun getResidueCharOpacity():Int {
        return if (this.theme.residueCharOpacity != null) this.theme.residueCharOpacity!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getResidueCharOpacity() else ssDrawing.theme.residueCharOpacity!!
    }

    fun getAChar():Color {
        return if (this.theme.AChar != null) this.theme.AChar!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getAChar() else ssDrawing.theme.AChar!!
    }

    fun getUChar():Color {
        return if (this.theme.UChar != null) this.theme.UChar!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getUChar() else ssDrawing.theme.UChar!!
    }

    fun getGChar():Color {
        return if (this.theme.GChar != null) this.theme.GChar!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getGChar() else ssDrawing.theme.GChar!!
    }

    fun getCChar():Color {
        return if (this.theme.CChar != null) this.theme.CChar!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getCChar() else ssDrawing.theme.CChar!!
    }

    fun getXChar():Color {
        return if (this.theme.XChar != null) this.theme.XChar!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getXChar() else ssDrawing.theme.XChar!!
    }

    fun getSecondaryColor():Color {
        return if (this.theme.SecondaryColor != null) this.theme.SecondaryColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getSecondaryColor() else ssDrawing.theme.SecondaryColor!!
    }

    fun getTertiaryColor():Color {
        return if (this.theme.TertiaryColor != null) this.theme.TertiaryColor!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getTertiaryColor() else ssDrawing.theme.TertiaryColor!!
    }

    fun getTertiaryOpacity():Int {
        return if (this.theme.tertiaryOpacity != null) this.theme.tertiaryOpacity!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getTertiaryOpacity() else ssDrawing.theme.tertiaryOpacity!!
    }

    fun getSecondaryInteractionWidth():Double {
        return if (this.theme.secondaryInteractionWidth != null) this.theme.secondaryInteractionWidth!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getSecondaryInteractionWidth() else ssDrawing.theme.secondaryInteractionWidth!!
    }

    fun getSecondaryInteractionShift():Double {
        return if (this.theme.secondaryInteractionShift != null) this.theme.secondaryInteractionShift!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getSecondaryInteractionShift() else ssDrawing.theme.secondaryInteractionShift!!
    }

    fun getTertiaryInteractionWidth():Double {
        return if (this.theme.tertiaryInteractionWidth != null) this.theme.tertiaryInteractionWidth!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getTertiaryInteractionWidth() else ssDrawing.theme.tertiaryInteractionWidth!!
    }

    fun getTertiaryInteractionStyle():String {
        return if (this.theme.tertiaryInteractionStyle != null) this.theme.tertiaryInteractionStyle!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getTertiaryInteractionStyle() else ssDrawing.theme.tertiaryInteractionStyle!!
    }

    fun getPhosphodiesterWidth():Double {
        return if (this.theme.phosphoDiesterWidth != null) this.theme.phosphoDiesterWidth!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getPhosphodiesterWidth() else ssDrawing.theme.phosphoDiesterWidth!!
    }

    fun getDeltaXRes():Int {
        return if (this.theme.deltaXRes != null) this.theme.deltaXRes!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaXRes() else ssDrawing.theme.deltaXRes!!
    }

    fun getDeltaYRes():Int {
        return if (this.theme.deltaYRes != null) this.theme.deltaYRes!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaYRes() else ssDrawing.theme.deltaYRes!!
    }

    fun getDeltaFontSize():Int {
        return if (this.theme.deltaFontSize != null) this.theme.deltaFontSize!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getDeltaFontSize() else ssDrawing.theme.deltaFontSize!!
    }

    fun getFontName():String {
        return if (this.theme.fontName != null) this.theme.fontName!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).getFontName() else ssDrawing.theme.fontName!!
    }

    fun displayLWSymbols():Boolean {
        return if (this.theme.displayLWSymbols != null) this.theme.displayLWSymbols!! else if (this !is HelixLine && this !is JunctionCircle && this.parent != null) (this.parent as SecondaryStructureElement).displayLWSymbols() else ssDrawing.theme.displayLWSymbols!!
    }

    fun getSinglePositions(): IntArray {
        return this.location.positions.toIntArray()
    }

    override fun newParameterValue(parameter: ThemeParameter, value: String) {
        this.theme.params.put(parameter.toString(), value)

    }

    override fun newParameterValue(parameter: ThemeParameter, value: Color) {
        this.theme.params.put(parameter.toString(), getHTMLColorString(value))
    }

    override fun newParameterValue(parameter: ThemeParameter, value: Double) {
        this.theme.params.put(parameter.toString(), value.toString())
    }

    override fun newParameterValue(parameter: ThemeParameter, value: Int) {
        this.theme.params.put(parameter.toString(), value.toString())
    }

}

class SecondaryStructureDrawing(val secondaryStructure: SecondaryStructure, frame:Rectangle2D = Rectangle(0,0,Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height), var theme: Theme = Theme(), val workingSession: WorkingSession = WorkingSession()) : ThemeConfiguratorListener {

    var name:String
        get() = this.secondaryStructure.name
        set(name) {
            this.secondaryStructure.name = name
        }
    val branches = mutableListOf<JunctionCircle>()
    val helices = mutableListOf<HelixLine>()
    val singleStrands = mutableListOf<SingleStrandLine>()
    val residues = mutableListOf<ResidueCircle>()
    val secondaryInteractions = mutableListOf<SecondaryInteractionLine>()
    val phosphodiesterBonds = mutableListOf<PhosphodiesterBondLine>()
    val tertiaryInteractions = mutableListOf<TertiaryInteractionLine>()

    val allJunctions:List<JunctionCircle>
        get() {
            val allJunctions = mutableSetOf<JunctionCircle>()
            for (branch in this.branches) {
                allJunctions.add(branch)
                allJunctions.addAll(branch.junctionsFromBranch())
            }
            return allJunctions.toList()
        }

    val allHelices:List<HelixLine>
        get() {
            val allHelices = mutableSetOf<HelixLine>()
            allHelices.addAll(this.helices)
            for (branch in this.branches) {
                allHelices.addAll(branch.helicesFromBranch())
            }
            return allHelices.toList().sortedBy { it.start }
        }

    val viewX:Double
        get() = this.workingSession.viewX

    val viewY:Double
        get() = this.workingSession.viewY

    val finalZoomLevel:Double
        get() = this.workingSession.finalZoomLevel

    val selection:MutableList<SecondaryStructureElement>
        get() = this.workingSession.selection

    val length:Int
        get() = this.secondaryStructure.length


    init {
        this.secondaryStructure.rna.seq.forEachIndexed { index,char -> this.residues.add(
            ResidueCircle(null, this,
                index + 1,
                char
            )
        )}
        //we start the drawing with the helices with no junction on one side
        var currentPos = 0
        lateinit var lastBranchConstructed: JunctionCircle
        lateinit var bottom:Point2D
        lateinit var top:Point2D

        do {
            val nextHelix = this.secondaryStructure.getNextHelixEnd(currentPos)

            if (nextHelix == null) { // no next helix, do we have any remaining residues?
                currentPos += 1
                val remaining:Float = (this.secondaryStructure.length - currentPos + 1).toFloat()
                if (remaining > 0) {
                    this.singleStrands.add(
                        SingleStrandLine(
                            this,
                            SingleStrand(name="SS${this.singleStrands.size+1}",
                                start = currentPos,
                                end = this.secondaryStructure.length
                            ),
                            start = bottom,
                            end = if (this.theme.fitToResiduesBetweenBranches) Point2D.Double(
                                bottom.x + radiusConst * 2 * (remaining + 1),
                                bottom.y
                            ) else Point2D.Double(bottom.x + 200, bottom.y)
                        )
                    )
                    this.residues[this.secondaryStructure.length-1].center = if (this.theme.fitToResiduesBetweenBranches)
                                        Point2D.Double(bottom.x + radiusConst *2 * (remaining+1), bottom.y)
                                        else Point2D.Double( bottom.x + 200, bottom.y)
                }
                break
            }

            val junction: Junction = (nextHelix.third.junctionsLinked.first ?: nextHelix.third.junctionsLinked.second) as Junction
            val residuesBeforeHelix = nextHelix.first - currentPos - 1

            if (currentPos == 0) {
                bottom = Point2D.Double(frame.width / 2, frame.height-50)
                top = Point2D.Double(frame.width / 2,  frame.height-50 - helixDrawingLength(
                    nextHelix.third
                )
                )

                var circles = mutableListOf<Triple<Point2D, Double, Ellipse2D>>()
                var lines = mutableListOf<List<Point2D>>()

                val h = HelixLine(null,
                        this,
                        nextHelix.third,
                        bottom,
                        top
                )

                lastBranchConstructed = JunctionCircle(h,
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
                        SingleStrandLine(
                            this,
                            SingleStrand(name="SS${this.singleStrands.size+1}",
                                start = currentPos + 1,
                                end = residuesBeforeHelix
                            ),
                            bottom,
                            if (this.theme.fitToResiduesBetweenBranches) Point2D.Double(
                                bottom.x - radiusConst * 2 * (residuesBeforeHelix + 1),
                                bottom.y
                            ) else Point2D.Double(bottom.x - 200, bottom.y)
                        )
                    )
                    this.residues[0].center = if (this.theme.fitToResiduesBetweenBranches) Point2D.Double(bottom.x - radiusConst * 2 * (residuesBeforeHelix+1), bottom.y) else Point2D.Double(bottom.x - 200, bottom.y)
                }

                this.branches.add(lastBranchConstructed)

            } else {
                //for the moment the new branch is located at the same location than the previous one
                //the computing of the branch is done twice
                //a first one to compute the transX to avoid overlaps with the previous branch
                //a second one to compute the placements of each graphical object of the new branch to avoid overlaps with any previous objects
                bottom = Point2D.Double(bottom.x, bottom.y)
                top = Point2D.Double(bottom.x , bottom.y - helixDrawingLength(
                    nextHelix.third
                )
                )

                //first we want to find the maxX of the previous branch to avoid an overlap with the new branch
                var lastJunctions = mutableListOf<JunctionCircle>()
                lastJunctions.addAll(this.branches.last().junctionsFromBranch())

                var circles = mutableListOf<Triple<Point2D, Double, Ellipse2D>>()
                var lines = mutableListOf<List<Point2D>>()
                val newBranchConstructed = JunctionCircle(null,
                    this,
                    circles,
                    lines,
                    null,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )
                var minY:Double = newBranchConstructed.minY //to search for the circles from the previous branches at the same level
                //first we check the circles from the last branch constructed at the same level than the new branch constructed
                var circlesAtTheSameLevel = mutableListOf<Ellipse2D>()
                for (lastC in lastJunctions) {
                    if (lastC.circle.bounds.minY >= minY) {
                        circlesAtTheSameLevel.add(lastC.circle)
                    }
                }

                var maxX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.maxBy { it.bounds.maxX }!!.bounds.maxX
                maxX += 2* radiusConst //we take care of the occupancy of a residue

                //now we search for the circles of the new branch that are at the same level than the circles recovered in the step before var maxX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.maxBy { it.bounds.maxX }!!.bounds.maxX
                val newJunctions = newBranchConstructed.junctionsFromBranch()

                circlesAtTheSameLevel = mutableListOf<Ellipse2D>()
                minY = this.branches.last().minY

                for (newC in newJunctions) {
                    if (newC.circle.bounds.minY >= minY) {
                        circlesAtTheSameLevel.add(newC.circle)
                    }
                }

                var minX = if (circlesAtTheSameLevel.isEmpty()) bottom.x else circlesAtTheSameLevel.minBy {it.bounds.minX}!!.bounds.minX

                minX -= 2* radiusConst //we take care of the occupancy of a residue

                var transX = maxX-bottom.x

                if (minX + transX < maxX) { //if despite the transX that will be applied, thhe minX of the new branch is still on the left of the maxX of the previous branches
                    transX += maxX - (minX+transX)
                }

                if (this.theme.fitToResiduesBetweenBranches) {
                    val minimalTransX = (nextHelix.first-currentPos+2) * radiusConst *2

                    if (transX < minimalTransX) {
                        transX += (minimalTransX-transX)
                    }
                }

                if (currentPos+1 <= nextHelix.first-1) {
                    this.singleStrands.add(
                        SingleStrandLine(
                            this,
                            SingleStrand(name="SS${this.singleStrands.size+1}",
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

                val h = HelixLine(null,
                        this,
                        nextHelix.third,
                        bottom,
                        top
                )

                lastBranchConstructed = JunctionCircle(h,
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
                    SecondaryInteractionLine(helix,
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
                    ) /(singleStrand.ss.length).toDouble()
                    for (i in singleStrand.start+1..singleStrand.end) {
                        val (p1_1,_) = pointsFrom(
                            this.residues[0].center!!,
                            this.residues[singleStrand.end].center!!,
                            step * (i - singleStrand.start).toDouble()
                        )
                        this.residues[i-1].center = p1_1
                    }
                }
            } else if (singleStrand.end == this.secondaryStructure.length) {
                if (singleStrand.length != 1) {
                    val step = distance(
                        this.residues[singleStrand.start - 2].center!!,
                        this.residues[this.secondaryStructure.length - 1].center!!
                    ) /(singleStrand.length).toDouble()
                    for (i in singleStrand.start until singleStrand.end) {
                        val (p1_1,_) = pointsFrom(
                            this.residues[singleStrand.start - 2].center!!,
                            this.residues[this.secondaryStructure.length - 1].center!!,
                            step * (i - (singleStrand.start - 1).toDouble())
                        )
                        this.residues[i-1].center = p1_1
                    }
                }
            } else {
                val step = distance(
                    this.residues[singleStrand.start - 2].center!!,
                    this.residues[singleStrand.end].center!!
                ) /(singleStrand.length+1).toDouble()
                for (i in singleStrand.start..singleStrand.end) {
                    val (p1_1,_) = pointsFrom(
                        this.residues[singleStrand.start - 2].center!!,
                        this.residues[singleStrand.end].center!!,
                        step * (i - (singleStrand.start - 1).toDouble())
                    )
                    this.residues[i-1].center = p1_1
                }
            }
        }

        for (branch in this.branches) {
            this.computeResidues(branch)

            for (helix in branch.helicesFromBranch()) {
                for (interaction in helix.helix.secondaryInteractions) {
                    helix.secondaryInteractions.add(
                        SecondaryInteractionLine(helix,
                            interaction,
                            this
                        )
                    )
                }
            }
        }

        for (r in this.residues) {
            OUTER@for (h in this.allHelices) {
                for (interaction in h.secondaryInteractions)
                    if (interaction.location.contains(r.absPos))   {
                        r.parent = interaction
                        break@OUTER;
                    }
            }
            if (r.parent == null) {
                for (j in this.allJunctions) {
                    if (j.locationWithoutSecondaries.contains(r.absPos))   {
                        r.parent = j
                        break;
                    }
                }
            }
            if (r.parent == null) {
                for (ss in this.singleStrands) {
                    if (ss.location.contains(r.absPos))   {
                        r.parent = ss
                        break;
                    }
                }
            }
        }

        for (i in 1 until this.secondaryStructure.length) {
            val phosphoBond = PhosphodiesterBondLine(null,this,
                Location(Location(i),Location(i+1))
            )
            for (h in this.allHelices) {
                if (h.location.contains(i) && h.location.contains(i+1))   {
                    phosphoBond.parent = h
                    break;
                }
            }
            if (phosphoBond.parent == null) {
                for (j in this.allJunctions) {
                    if (j.location.contains(i) && j.location.contains(i+1))   {
                        phosphoBond.parent = j
                        break;
                    }
                }
            }

            this.phosphodiesterBonds.add(phosphoBond)
        }

        for (interaction in this.secondaryStructure.tertiaryInteractions) {
            this.tertiaryInteractions.add(
                TertiaryInteractionLine(null,
                    interaction,
                    this
                )
            )
        }
    }

    /**
     *  this function clears all the params for the themes attached to this element and any secondary structure subelement.
     */
    fun clearThemes() {
        for (jc in this.allJunctions)
            jc.clearThemes()
        for (h in this.allHelices)
            h.clearThemes()
        for (i in this.secondaryInteractions)
            i.clearThemes()
        for (i in this.tertiaryInteractions)
            i.clearThemes()
        for (p in this.phosphodiesterBonds)
            p.clearThemes()
        for (r in this.residues)
            r.clearThemes()
    }

    fun getResiduesFromAbsPositions(vararg positions:Int): List<ResidueCircle> {
        val _residues:MutableList<ResidueCircle> = mutableListOf<ResidueCircle>()
        for (r: ResidueCircle in residues) {
            if (r.absPos in positions) {
                _residues.add(r)
            }
        }
        return _residues
    }

    fun getBounds():Rectangle2D {
        val minX = this.residues.minBy { it.circle!!.minX }!!.circle!!.minX-theme.residueBorder!!
        val minY = this.residues.minBy { it.circle!!.minY }!!.circle!!.minY-theme.residueBorder!!
        val maxX = this.residues.maxBy { it.circle!!.maxX }!!.circle!!.maxX+theme.residueBorder!!
        val maxY = this.residues.maxBy { it.circle!!.maxY }!!.circle!!.maxY+theme.residueBorder!!
        return Rectangle2D.Double(minX.toInt()-theme.residueBorder!!/2.0, minY.toInt()-theme.residueBorder!!/2.0, maxX-minX+theme.residueBorder!!, maxY-minY+theme.residueBorder!!)
    }

    fun draw (g: Graphics2D) {
        val at = AffineTransform()
        at.translate(workingSession.viewX, workingSession.viewY)
        at.scale(workingSession.finalZoomLevel, workingSession.finalZoomLevel)
        if (!theme.quickDraw) {
            val _c = at.createTransformedShape(this.residues.first().circle)
            theme.fontSize = computeOptimalFontSize(
                g,
                workingSession,
                this.theme,
                this.residues.first().label.name,
                _c.bounds2D.width,
                _c.bounds2D.height
            )
            g.font = Font(this.theme.fontName, this.theme.fontStyle, theme.fontSize)
            var r2d = getStringBoundsRectangle2D(g, "A", g.font)
            this.theme.ATransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.theme.ATransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "U", g.font)
            this.theme.UTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.theme.UTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "G", g.font)
            this.theme.GTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.theme.GTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "C", g.font)
            this.theme.CTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.theme.CTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
            r2d = getStringBoundsRectangle2D(g, "X", g.font)
            this.theme.XTransX = (_c.bounds2D.width - r2d.width).toFloat() / 2F
            this.theme.XTransY = (_c.bounds2D.height + r2d.height).toFloat() / 2F
        }

        this.phosphodiesterBonds.forEach {
            it.draw(g, at)
        }

        this.helices.forEach {
            it.draw(g, at)
        }

        this.branches.forEach {
            it.draw(g, at)
        }

        if (!theme.quickDraw && selection.isEmpty())
            this.tertiaryInteractions.forEach {
                it.drawHalo(g, at)
            }

        this.residues.forEach {
            it.draw(g, at)
        }

        if (!theme.quickDraw && RnartistConfig.displayTertiariesInSelection)
            this.tertiaryInteractions.forEach {
                if (it.getTertiaryInteractionWidth() != 0.0)
                    it.draw(g, at)
            }

    }

    fun computeResidues(branch: JunctionCircle) {
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
                val step = -angle/(b.end-b.start).toDouble()
                for (i in b.start+1 until b.end) {
                    this.residues[i-1].center = rotatePoint(
                        this.residues[b.start - 1].center!!,
                        j.center,
                        step * (i - b.start).toDouble()
                    )
                }
            }
        }

    }

    /**
    Compute the coordinates for all the residues in an helix
     */
    fun computeResidues(helix: HelixLine) {
        val (p1_5,p2_3) = getPerpendicular(
            helix.line.p1,
            helix.line.p1,
            helix.line.p2,
            helixDrawingWidth()/2.0
        )
        var cp = crossProduct(helix.line.p1, helix.line.p2, p1_5)
        if (cp < 0) {
            this.residues[helix.helix.ends[0]-1].center = p1_5
            this.residues[helix.helix.ends[3]-1].center = p2_3
        } else {
            this.residues[helix.helix.ends[0]-1].center = p2_3
            this.residues[helix.helix.ends[3]-1].center = p1_5
        }
        val (p1_3,p2_5) = getPerpendicular(
            helix.line.p2,
            helix.line.p1,
            helix.line.p2,
            helixDrawingWidth()/2.0
        )
        cp = crossProduct(helix.line.p2, helix.line.p1, p1_3)
        if (cp > 0) {
            this.residues[helix.helix.ends[1]-1].center = p1_3
            this.residues[helix.helix.ends[2]-1].center = p2_5
        } else {
            this.residues[helix.helix.ends[1]-1].center = p2_5
            this.residues[helix.helix.ends[2]-1].center = p1_3
        }

        val step = helixDrawingLength(helix.helix).toDouble()/(helix.helix.length-1).toDouble()

        for (i in helix.helix.ends[0]+1 until helix.helix.ends[1]) {
            val (p1_1,_) = pointsFrom(
                this.residues[helix.helix.ends[0] - 1].center!!,
                this.residues[helix.helix.ends[1] - 1].center!!,
                step * (i - helix.helix.ends[0]).toDouble()
            )
            this.residues[i-1].center = p1_1
        }

        for (i in helix.helix.ends[2]+1 until helix.helix.ends[3]) {
            val (p1_1,_) = pointsFrom(
                this.residues[helix.helix.ends[2] - 1].center!!,
                this.residues[helix.helix.ends[3] - 1].center!!,
                step * (i - helix.helix.ends[2]).toDouble()
            )
            this.residues[i-1].center = p1_1
        }
    }

    fun hasSingleHBonds(): Boolean {
        return false
    }

    fun asSVG():String {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB) //just to get a Graphics object
        val g = image.createGraphics()
        theme.fontSize = computeOptimalFontSize(
            g,
            WorkingSession(),
            theme,
            "A",
            residues.first().circle!!.width,
            residues.first().circle!!.height
        )
        val font = Font(theme.fontName, theme.fontStyle, theme.fontSize)
        var r2d = getStringBoundsRectangle2D(g, "A", font)
        this.theme.ATransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.theme.ATransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "U", font)
        this.theme.UTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.theme.UTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "G", font)
        this.theme.GTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.theme.GTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "C", font)
        this.theme.CTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.theme.CTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        r2d = getStringBoundsRectangle2D(g, "X", font)
        this.theme.XTransX = (residues.first().circle!!.bounds2D.width - r2d.width).toFloat() / 2F
        this.theme.XTransY = (residues.first().circle!!.bounds2D.height + r2d.height).toFloat() / 2F
        val bounds = getBounds()
        val svgBuffer = StringBuffer("""<svg viewBox="0 0 ${bounds.width} ${bounds.height}" xmlns="http://www.w3.org/2000/svg">"""+"\n")
        helices.map { helix ->
            helix.secondaryInteractions.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        }
        allJunctions.map { junction ->
            junction.helices.map { helix ->
                helix.secondaryInteractions.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
            }
        }
        phosphodiesterBonds.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        tertiaryInteractions.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        residues.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        svgBuffer.append("</svg>")
        return svgBuffer.toString()
    }

    override fun toString(): String {
        return this.secondaryStructure.toString()
    }

    override fun newParameterValue(parameter: ThemeParameter, value: String) {
        this.theme.params.put(parameter.toString(), value)
    }

    override fun newParameterValue(parameter: ThemeParameter, value: Color) {
        this.theme.params.put(parameter.toString(), getHTMLColorString(value))
    }

    override fun newParameterValue(parameter: ThemeParameter, value: Double) {
        this.theme.params.put(parameter.toString(), value.toString())
    }

    override fun newParameterValue(parameter: ThemeParameter, value: Int) {
        this.theme.params.put(parameter.toString(), value.toString())
    }
}

class ResidueCircle(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, absPos:Int, label:Char): SecondaryStructureElement(ssDrawing, parent, ""+label, Location(absPos)) {

    val absPos:Int
        get() = this.location.start

    val label: SecondaryStructureType

    var circle: Ellipse2D? = null

    var center:Point2D? = null
        set(value) {
            field = value
            this.circle = Ellipse2D.Double(value!!.x- radiusConst, value.y- radiusConst, (radiusConst *2F).toDouble(), (radiusConst *2F).toDouble())
        }

    override val bounds2D: Rectangle2D?
        get() = this.circle?.bounds2D

    override val type: String  = "Residue"

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection || if (this.parent!=null) this.parent!!.isSelected else false

    init {
        when(label) {
            'A' -> this.label = SecondaryStructureType.A
            'U' -> this.label = SecondaryStructureType.U
            'G' -> this.label = SecondaryStructureType.G
            'C' -> this.label = SecondaryStructureType.C
            else ->  this.label = SecondaryStructureType.X
        }
    }

    fun draw(g: Graphics2D, at:AffineTransform) {
        if (this.circle != null) {
            val _c = at.createTransformedShape(this.circle)
            g.color = getColor()
            if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) {
                g.color = Color(g.color.red, g.color.green, g.color.blue,
                    RnartistConfig.selectionFading
                )
            }
            g.fill(_c)
            if (!this.ssDrawing.theme.quickDraw) {
                val previousStroke: Stroke = g.getStroke()
                g.stroke = BasicStroke(this.ssDrawing.workingSession.finalZoomLevel.toFloat() * this.getResidueBorder().toFloat())
                g.color = Color.DARK_GRAY
                if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) {
                    g.color = Color(g.color.red, g.color.green, g.color.blue,
                        RnartistConfig.selectionFading
                    )
                }
                g.draw(_c)
                g.stroke = previousStroke
            }
            if (g.font.size > 5 && !this.ssDrawing.theme.quickDraw && this.getResidueCharOpacity() > 0) { //the conditions to draw a letter
                when (this.label.name) {
                    "A" -> g.color = this.getAChar()
                    "U" -> g.color = this.getUChar()
                    "G" -> g.color = this.getGChar()
                    "C" -> g.color = this.getCChar()
                    "X" -> g.color = this.getXChar()
                }
                g.color = Color(g.color.red, g.color.green, g.color.blue, this.getResidueCharOpacity()) //we fade the residue letter
                if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) //we fade it even more if unselected
                    g.color = Color(g.color.red, g.color.green, g.color.blue, (RnartistConfig.selectionFading /255.0*this.getResidueCharOpacity()).toInt()) //the residue fading of an unselected residue is reduced by x%, x is the % of decrease of selection fading (according to full opacity which is 255).
                when (this.label.name) {
                    "A" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + this.ssDrawing.theme.ATransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + this.ssDrawing.theme.ATransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
                    "U" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + this.ssDrawing.theme.UTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + this.ssDrawing.theme.UTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
                    "G" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + this.ssDrawing.theme.GTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + this.ssDrawing.theme.GTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
                    "C" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + this.ssDrawing.theme.CTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + this.ssDrawing.theme.CTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
                    "X" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + this.ssDrawing.theme.XTransX + (this.getDeltaXRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + this.ssDrawing.theme.XTransY - (this.getDeltaYRes() * this.ssDrawing.workingSession.finalZoomLevel).toFloat())
                }
            }
        }
    }

    override fun clearThemes() {
        this.theme.params.clear()
    }

    fun getColor():Color {
       return when (this.label) {
            SecondaryStructureType.A -> this.getAColor()
            SecondaryStructureType.U -> this.getUColor()
            SecondaryStructureType.G -> this.getGColor()
            SecondaryStructureType.C -> this.getCColor()
            else -> this.getXColor()
        }
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val buff = StringBuffer(indentChar.repeat(indentLevel)+"<g>\n")
        buff.append(indentChar.repeat(indentLevel+1)+"""<circle cx="${this.circle!!.centerX+transX}" cy="${this.circle!!.centerY+transY}" r="${this.circle!!.width/2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${this.getResidueBorder()}" fill="rgb(${getColor().red}, ${getColor().green}, ${getColor().blue})" />"""+"\n")
        val charColor = when (this.label.name) {
            "A" -> this.getAChar()
            "U" -> this.getUChar()
            "G" -> this.getGChar()
            "C" -> this.getCChar()
            "X" -> this.getXChar()
            else -> Color.WHITE
        }
        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel+1)+"""<text x="${this.circle!!.centerX+transX+this.getDeltaXRes()}" y="${this.circle!!.centerY+transY+this.getDeltaYRes()}" text-anchor="middle" dy=".3em" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${theme.fontSize};">${this.label.name}</text>"""+"\n")
        else
            buff.append(indentChar.repeat(indentLevel+1)+"""<text x="${this.circle!!.bounds2D.minX.toFloat()+transX+this.getDeltaXRes() + when (this.label) { SecondaryStructureType.A -> theme.ATransX ; SecondaryStructureType.U -> theme.UTransX ; SecondaryStructureType.G -> theme.GTransX ; SecondaryStructureType.C -> theme.CTransX ; else -> theme.XTransX } }" y="${this.circle!!.bounds2D.minY.toFloat()+transY+this.getDeltaYRes() + when (this.label) { SecondaryStructureType.A -> theme.ATransY ; SecondaryStructureType.U -> theme.UTransY ; SecondaryStructureType.G -> theme.GTransY ; SecondaryStructureType.C -> theme.CTransY ; else -> theme.XTransY } }" style="fill:rgb(${charColor.red}, ${charColor.green}, ${charColor.blue});font-family:${this.getFontName()};font-size:${theme.fontSize};">${this.label.name}</text>"""+"\n")
        buff.append(indentChar.repeat(indentLevel)+"</g>\n")
        return buff.toString()
    }

}

class HelixLine(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, val helix: Helix, start:Point2D, end:Point2D): SecondaryStructureElement(ssDrawing, parent, helix.name, helix.location) {

    var line:Line2D = Line2D.Double(start,end)
    val secondaryInteractions = mutableListOf<SecondaryInteractionLine>()
    val start:Int
        get() = this.location.start

    val end:Int
        get() = this.location.end

    val length:Int
        get() = this.helix.length

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection

    override val type: String  = "Helix"

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.secondaryInteractions.first()!!.bounds2D
            for (i in 1 until this.secondaryInteractions.size)
                bounds = bounds?.createUnion( this.secondaryInteractions.get(i).bounds2D)
            return bounds
        }

    override fun clearThemes() {
        this.theme.params.clear()
        for (i in this.secondaryInteractions) {
            i.clearThemes()
        }
    }

    fun draw(g: Graphics2D, at:AffineTransform) {
        this.secondaryInteractions.forEach {
            it.draw(g, at)
        }
    }
}

class SingleStrandLine(ssDrawing: SecondaryStructureDrawing, val ss: SingleStrand, start:Point2D, end:Point2D): SecondaryStructureElement(ssDrawing, null, ss.name, ss.location) {

    var line = Line2D.Double(start,end)

    val start:Int
        get() = this.location.start

    val end:Int
        get() = this.location.end

    val length:Int
        get() = this.ss.length

    override val bounds2D: Rectangle2D?
        get() = this.line.bounds2D

    override val type: String  = "Single Strand"

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection

    fun draw(g: Graphics2D, at:AffineTransform) {
        g.draw(at.createTransformedShape(this.line))
    }

    override fun clearThemes() {
    }
}

class JunctionCircle (parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, circlesFromBranchSoFar: MutableList<Triple<Point2D, Double, Ellipse2D>>, linesFromBranchSoFar: MutableList<List<Point2D>>, previousJunction: JunctionCircle? = null, var inId: ConnectorId, inPoint:Point2D, inHelix: Helix, val junction: Junction): SecondaryStructureElement(ssDrawing, parent, junction.name, junction.location) {

    val noOverlapWithLines = true
    val noOverlapWithCircles = true
    var helices = mutableListOf<HelixLine>()
    var connectedJunctions = mutableMapOf<ConnectorId, JunctionCircle>()
    val connectors:Array<Point2D> = Array(ConnectorId.values().size, { Point2D.Float(0F,0F) } ) //the connector points on the circle
    var layout:MutableList<ConnectorId>? = defaultLayouts[this.junction.type]?.toMutableList()
        set(value) {
            //we order the helices according to the start but with inHelix as the first one
            val sortedHelix = this.junction.helicesLinked.sortedBy { it.start - this.inHelix.start}
            field = value
            //we change the entry point for each connected circle, we update the self.connectedCircles dict and we warn the connected circles that their entry point has been changed (we call their setEntryPoint() function)
            var newConnectedJunctions = mutableMapOf<ConnectorId, JunctionCircle>() //we need to store the new connections in a temp dict otherwise the update of a connection could remove an old connection stored and not already checked.
            //this.helices = mutableListOf<HelixLine>()
            var helixRank = 0
            for (helix in sortedHelix) {
                if (helix != this.inHelix) {
                    helixRank += 1
                    var inPoint:Point2D?
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
                            lateinit var connectedJunction: MutableMap.MutableEntry<ConnectorId, JunctionCircle>
                            for (c in this.connectedJunctions) {
                                if  (c.value.junction.location == helix.junctionsLinked.first!!.location) {
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
                        }
                        else if (helix.junctionsLinked.second != null && helix.junctionsLinked.second != this.junction) {
                            //we search the circle already connected for this helix
                            lateinit var connectedJunction: MutableMap.MutableEntry<ConnectorId, JunctionCircle>
                            for (c in this.connectedJunctions) {
                                if  (c.value.junction.location == helix.junctionsLinked.second!!.location) {
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
    var radius:Double = 0.0
        set(value) {
            field = value
            this.center = centerFrom(
                this.inId,
                this.connectors[this.inId.value],
                this.radius
            )
            this.circle = Ellipse2D.Double(this.center.x-this.radius, this.center.y-this.radius, this.radius*2.toDouble(), this.radius*2.toDouble())
            //the (x,y) coords for the connectors
            for (i in 1 until ConnectorId.values().size) {
                this.connectors[(this.inId.value+i)% ConnectorId.values().size] =
                    rotatePoint(
                        this.connectors[this.inId.value],
                        this.center,
                        i * 360.0 / ConnectorId.values().size.toDouble()
                    )
            }
        }
    lateinit var center:Point2D
    lateinit var circle:Ellipse2D
    val inHelix = inHelix
    val previousJunction = previousJunction //allows to get some info backward. For example, useful for an InnerLoop to check the previous orientation in order to keep it if the inID is .o or .e (instead to choose .n in any case)

    val minX:Double
        get() {
            return this.junctionsFromBranch().minBy{ it.circle.bounds.minX}!!.circle.bounds.minX
        }

    val minY:Double
        get() {
            return this.junctionsFromBranch().minBy{ it.circle.bounds.minY}!!.circle.bounds.minY
        }

    val maxX:Double
        get() {
            return this.junctionsFromBranch().maxBy{ it.circle.bounds.maxX}!!.circle.bounds.maxX
        }

    val maxY:Double
        get() {
            return this.junctionsFromBranch().maxBy{ it.circle.bounds.maxY}!!.circle.bounds.maxY
        }

    val locationWithoutSecondaries:Location
        get() {
            return this.junction.locationWithoutSecondaries
        }

    val junctionCategory:JunctionType
        get() {
            return this.junction.type
        }

    override val bounds2D: Rectangle2D?
        get()  = this.circle.bounds2D

    override val type: String
        get() {
            var typeName: String = this.junction.type.name
            if (typeName.endsWith("Way")) {
                typeName = typeName.split("Way".toRegex()).toTypedArray()[0] + " Way Junction"
            } else if (typeName.endsWith("Loop")) {
                typeName = typeName.split("Loop".toRegex()).toTypedArray()[0] + " Loop"
            }
            return typeName
        }

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection

    init {
        this.connectors[this.inId.value] = inPoint
        //we compute the initial radius according to the junction length and type
        val circumference = (this.junction.length.toFloat() - this.junction.type.value*2).toFloat() * (radiusConst * 2).toFloat() + this.junction.type.value* helixDrawingWidth()
        this.radius = circumference/(2F*Math.PI).toDouble()
        circlesFromBranchSoFar.add(Triple<Point2D,Double, Ellipse2D>(this.center, this.radius, this.circle)) //this array allows to get easily the shapes already drawn for the branch in order to avoid overlaps with the shapes for this junction

        val sortedHelix = this.junction.helicesLinked.sortedBy { it.start }

        var helixRank = 0

        for (k in 1..sortedHelix.size+1) {
            val helix = sortedHelix[(sortedHelix.indexOf(inHelix)+k)%sortedHelix.size]
            if (helix == this.inHelix) {
                break
            }

            helixRank += 1
            var inPoint:Point2D

            var outId = getOutId(helixRank)

            if (this.junction.type == JunctionType.InnerLoop && (this.junction.location.blocks[0].length < 5 || this.junction.location.blocks[1].length < 5)) {
                outId = oppositeConnectorId(inId)
            }
            else if (this.junction.type == JunctionType.InnerLoop) {
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
                                }
                                else {
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
                            }
                            else {
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

                if (helixRank == sortedHelix.size-1) {
                    val newRawValue = if (inId.value - 1 < 0) ConnectorId.values().size-1 else inId.value - 1
                    to = getConnectorId(newRawValue)
                } else {
                    val newRawValue = if (getOutId(helixRank+1)!!.value - 1 < 0) ConnectorId.values().size-1 else getOutId(helixRank+1)!!.value - 1
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
                var fine:Boolean
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

                    val nextCircumference = (junction!!.length.toFloat() - (junction.type.value)*2).toFloat() *  (radiusConst * 2) + (junction.type.value).toFloat()* helixDrawingWidth()
                    val nextRadius = nextCircumference/(2F*Math.PI)
                    val nextCenter = pointsFrom(
                        this.center,
                        this.connectors[outId.value],
                        -helixDrawingLength(helix) - nextRadius
                    ).second
                    val nextCircle = Ellipse2D.Double(nextCenter.x-nextRadius, nextCenter.y-nextRadius, nextRadius*2F, nextRadius*2F)

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
                        for (i in 1..helix.length-2) {
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
                } while (!fine && i < orientationsToTest.size )

                if (!fine) { //if we were not able to find any non-overlapping orientation, we come back to the initial orientation (which is the first one in the orientationsToTest array) and we recompute the inPoint for this orientation
                    outId = orientationsToTest.first()
                    inPoint = pointsFrom(
                        p1 = this.center,
                        p2 = this.connectors[outId.value],
                        dist = -helixDrawingLength(helix)
                    ).second
                }

                //we need to update the layout with the orientation chosen
                this.layout!!.set(helixRank-1,
                    getConnectorId((outId!!.value + ConnectorId.values().size - inId.value) % ConnectorId.values().size)
                )
                val h = HelixLine(this,
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
                    for (i in 1..helix.length-2) {
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

                this.connectedJunctions[outId] = JunctionCircle(h,
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

    fun junctionsFromBranch(): List<JunctionCircle> {
        val junctions = mutableListOf<JunctionCircle>()
        junctions.add(this)
        for ((_,j) in this.connectedJunctions) {
            junctions.addAll(j.junctionsFromBranch())
        }
        return junctions
    }

    fun helicesFromBranch(): List<HelixLine> {
        var helices = mutableListOf<HelixLine>()
        helices.addAll(this.helices)
        for ((_,j) in this.connectedJunctions) {
            helices.addAll(j.helicesFromBranch())
        }
        return helices
    }

    fun getOutId(helixRank:Int): ConnectorId?  {
        when (this.junction.type) {
            JunctionType.ApicalLoop -> return null
            //the int in the array are the offset to be added to reach the next connectorId according to the type of junction. The new connector ID is the connector Id for the entry point + the offset of the corresponding helix rank (max:ConnectorId.count-1, if above we restart at 0). In the absolute layout, the connector ID for the entry point is lowerMiddle (0) and for the relative layout anything else.
            JunctionType.Flower -> return null
            else -> return ConnectorId.values().first { it.value == (this.inId.value + this.layout!!.get(helixRank-1).value)% ConnectorId.values().size}
        }
    }

    //the previous JunctionCircle has modified its link with this one.
    fun setEntryPoint(inId: ConnectorId, inPoint:Point2D) {
        this.inId = inId
        this.connectors[this.inId.value] = inPoint
        //the (x,y) coords for the center
        this.center = centerFrom(
            this.inId,
            this.connectors[this.inId.value],
            this.radius
        )
        this.circle = Ellipse2D.Double(this.center.x-this.radius, this.center.y-this.radius, this.radius*2.toDouble(), this.radius*2.toDouble())

        //the (x,y) coords for the connectors
        for (i in 1..ConnectorId.values().size) {
            this.connectors[(this.inId.value+i)% ConnectorId.values().size] =
                rotatePoint(
                    this.connectors[this.inId.value],
                    this.center,
                    i * 360.0 / ConnectorId.values().size.toDouble()
                )
        }

        this.layout = this.layout //a trick to warn the connected circles, and so on...
    }

    fun draw(g: Graphics2D, at:AffineTransform) {

        this.helices.forEach {
            it.draw(g, at)
        }

        this.connectedJunctions.forEach { _, junction ->
            junction.draw(g, at)
        }

    }

    override fun clearThemes() {
        this.theme.params.clear()
        for (r in this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()))
            r.clearThemes()
    }

}

abstract class LWSymbol(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name:String, location:Location, val inTertiaries: Boolean):SecondaryStructureElement(ssDrawing, parent, name, location) {

    lateinit protected var shape:Shape

    open fun draw(g: Graphics2D, at:AffineTransform) {
        g.color = this.getSecondaryColor()
        if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) {
            g.color = Color(g.color.red, g.color.green, g.color.blue,
                    if (inTertiaries) (RnartistConfig.selectionFading /255.0*this.getTertiaryOpacity()).toInt() else RnartistConfig.selectionFading
            )
        } else
            g.color = Color(g.color.red, g.color.green, g.color.blue,
                    if (inTertiaries) this.getTertiaryOpacity() else 255
            )
    }

    abstract fun setSymbol(p1:Point2D, p2: Point2D)

    override val bounds2D: Rectangle2D?
        get() = this.shape.bounds2D

    override val type: String  = "LW Symbol"

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection || if (this.parent!=null) this.parent!!.isSelected else false

    override fun clearThemes() {
        this.theme.params.clear()
    }
}

abstract class WC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name:String, location:Location, inTertiaries: Boolean):LWSymbol(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start,end)
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
}

class CisWC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean):WC(parent, ssDrawing, "cisWC", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "cisWC"
    }
}

class TransWC(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean):WC(parent, ssDrawing, "transWC", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        if (inTertiaries)
            g.stroke = BasicStroke(
                this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
        else
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getSecondaryInteractionWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "transWC"
    }

}

abstract class LeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name:String, location:Location, inTertiaries: Boolean):LWSymbol(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start,end)
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
}

abstract class RightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name:String, location:Location, inTertiaries: Boolean = false): LWSymbol(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start,end)
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

}

class CisRightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false): RightSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "cisSugar"
    }

}

class CisLeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false): LeftSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "cisSugar"
    }

}

class TransRightSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false): RightSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        if (inTertiaries)
            g.stroke = BasicStroke(
                this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
        else
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getSecondaryInteractionWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "transSugar"
    }

}

class TransLeftSugar(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false):LeftSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        if (inTertiaries)
            g.stroke = BasicStroke(
                this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
        else
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getSecondaryInteractionWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "transSugar"
    }

}

abstract class Hoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, name:String, location:Location, inTertiaries: Boolean = false):LWSymbol(parent, ssDrawing, name, location, inTertiaries) {

    override fun setSymbol(start: Point2D, end: Point2D) {
        val symbolWidth = distance(start,end)
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


}

class CisHoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false):Hoogsteen(parent, ssDrawing, "cisHoogsteen", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "cisHoogsteen"
    }

}

class TransHoogsteen(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries: Boolean = false):Hoogsteen(parent, ssDrawing, "transHoogsteen", location, inTertiaries) {

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        if (inTertiaries)
            g.stroke = BasicStroke(
                this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
        else
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getSecondaryInteractionWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun toString(): String {
        return "transHoogsteen"
    }

}

enum class VSymbolPos {
    BOTTOM, MIDDLE, TOP
}

class LWLine(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location, inTertiaries:Boolean = false, val vpos:VSymbolPos = VSymbolPos.MIDDLE  ):LWSymbol(parent, ssDrawing, "Line", location, inTertiaries) {

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
            else -> this.shape = Line2D.Double(p1,p2)
        }

    }

    override fun draw(g: Graphics2D, at:AffineTransform) {
        super.draw(g, at)
        if (!this.inTertiaries) {
            g.stroke = BasicStroke(
                    this.ssDrawing.finalZoomLevel.toFloat() * this.getSecondaryInteractionWidth().toFloat(),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            )
            g.draw(at.createTransformedShape(this.shape))
        }
        else {
            ssDrawing?.let {
                if (this.getTertiaryInteractionStyle() == DASHED)
                    g.stroke = BasicStroke(
                        this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        0F,
                        floatArrayOf(this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat() * 2),
                        0F
                    )
                else
                    g.stroke = BasicStroke(
                        this.ssDrawing.finalZoomLevel.toFloat() * this.getTertiaryInteractionWidth().toFloat(),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                    )
                g.draw(at.createTransformedShape(this.shape))
            }
        }
    }

    override fun toString(): String {
        return "Line"
    }

}

abstract class BaseBaseInteraction(parent: SecondaryStructureElement?, val interaction: BasePair, ssDrawing:SecondaryStructureDrawing): SecondaryStructureElement(ssDrawing, parent, interaction.toString(), interaction.location) {

    protected var p1:Point2D? = null
    protected var p2:Point2D? = null
    var lwSymbols = mutableListOf<LWSymbol>()
    var regularSymbols = mutableListOf<LWSymbol>()
    val residue:ResidueCircle
        get() {
            return ssDrawing.getResiduesFromAbsPositions(this.start).first()
        }
    val pairedResidue:ResidueCircle
        get() {
            return ssDrawing.getResiduesFromAbsPositions(this.end).first()
        }
    val start:Int
        get() {
            return this.location.start
        }

    val end:Int
        get() {
            return this.location.end
        }

    val isCanonical:Boolean
        get() {
            return this.interaction.edge5 == Edge.WC && this.interaction.edge3 == Edge.WC && this.interaction.orientation == Orientation.cis && (
                                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.A && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.U ||
                                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.U && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.A ||
                                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.G && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.C ||
                                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.C && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.G
                    )
        }

    val isDoublePaired:Boolean
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.G && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.C ||
                this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start).first()?.label == SecondaryStructureType.C && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end).first()?.label == SecondaryStructureType.G

    protected fun generateSingleSymbol(location:Location, inTertiaries: Boolean= false, edge:Edge, orientation: Orientation, right:Boolean=true):LWSymbol {
        return when (edge) {
            Edge.WC -> {
                when(orientation) {
                    Orientation.cis -> CisWC(this, this.ssDrawing, location, inTertiaries)
                    Orientation.trans -> TransWC(this, this.ssDrawing, location, inTertiaries)
                    else -> CisWC(this, this.ssDrawing, location, inTertiaries)
                }
            }
            Edge.Hoogsteen -> {
                when(orientation) {
                    Orientation.cis -> CisHoogsteen(this, this.ssDrawing, location, inTertiaries)
                    Orientation.trans -> TransHoogsteen(this, this.ssDrawing, location, inTertiaries)
                    else -> CisHoogsteen(this, this.ssDrawing, location, inTertiaries)
                }
            }
            Edge.Sugar -> {
                when(orientation) {
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

    override fun clearThemes() {
        this.theme.params.clear()
        this.residue.clearThemes()
        this.pairedResidue.clearThemes()
        for (s in this.regularSymbols) {
            s.clearThemes()
        }
        for (s in this.lwSymbols) {
            s.clearThemes()
        }
    }
}

class SecondaryInteractionLine(parent: SecondaryStructureElement?, interaction: BasePair, ssDrawing: SecondaryStructureDrawing): BaseBaseInteraction(parent, interaction, ssDrawing) {

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion( this.pairedResidue.bounds2D)
            return bounds
        }

    override val type: String = "Secondary Interaction"

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection || if (this.parent!=null) this.parent!!.isSelected else false

    init {
        this.regularSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
        if (this.isCanonical) {
            if (isDoublePaired) {
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false, vpos = VSymbolPos.TOP))
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false, vpos = VSymbolPos.BOTTOM))
            } else
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
        } else {
            if (this.interaction.edge5 == this.interaction.edge3) { //uniq central symbol
                //+++++left symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, Location(this.location.start),false))
                //++++++middle symbol
                this.lwSymbols.add(this.generateSingleSymbol(this.location, false, this.interaction.edge5, this.interaction.orientation))
                //+++++right symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, Location(this.location.end),false))
            } else {
                //+++++left symbol
                this.lwSymbols.add(this.generateSingleSymbol( Location(this.location.start),false, this.interaction.edge5, this.interaction.orientation, right = false))
                //++++++middle symbol
                this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                //+++++right symbol
                this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.end),false, this.interaction.edge3, this.interaction.orientation))
            }
        }
    }

    fun draw(g: Graphics2D, at:AffineTransform) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(this.ssDrawing.finalZoomLevel.toFloat()*this.getSecondaryInteractionWidth().toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val center1 = this.residue.center
        val center2 = this.pairedResidue.center

        if (center1 != null && center2 != null) {
            val shift = radiusConst+this.getSecondaryInteractionShift()+this.getResidueBorder()/2.0+this.getSecondaryInteractionWidth()/2.0
            if (distance(center1,center2) > 2*shift) {
                val points = pointsFrom(
                    center1,
                    center2,
                    shift
                )
                this.p1 = points.first
                this.p2 = points.second
                if (!displayLWSymbols()) {
                    regularSymbols.forEach { nonLwSymbol ->
                        nonLwSymbol.setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                        nonLwSymbol.draw(g, at)
                    }
                } else {
                    if (this.isCanonical) {
                        if (isDoublePaired) {
                            this.lwSymbols[0].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                            this.lwSymbols[0].draw(g, at)

                            this.lwSymbols[1].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                            this.lwSymbols[1].draw(g, at)
                        } else {
                            this.lwSymbols[0].setSymbol(this.p1 as Point2D, this.p2 as Point2D)
                            this.lwSymbols[0].draw(g, at)
                        }
                    } else {
                        val distance = distance(this.p1 as Point2D, this.p2 as Point2D)
                        val symbolWidth = distance / 3.0
                        if (interaction.edge5 == interaction.edge3) {
                            val (p1_inner, p2_inner) = pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth / 2.0)

                            this.lwSymbols[0].setSymbol(p1!!, p1_inner)
                            this.lwSymbols[0].draw(g, at)

                            this.lwSymbols[2].setSymbol(p2_inner, p2!!)
                            this.lwSymbols[2].draw(g, at)

                            //to have the central symbol above
                            this.lwSymbols[1].setSymbol(p1_inner, p2_inner)
                            this.lwSymbols[1].draw(g, at)

                        } else {
                            val (p1_inner, p2_inner) =  pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth + symbolWidth / 4.0)
                            this.lwSymbols[0].setSymbol(p1!!, p1_inner)
                            this.lwSymbols[0].draw(g, at)

                            this.lwSymbols[2].setSymbol(p2_inner, p2!!)
                            this.lwSymbols[2].draw(g, at)

                            //to have the central symbol above
                            this.lwSymbols[1].setSymbol(p1_inner, p2_inner)
                            this.lwSymbols[1].draw(g, at)

                        }
                    }
                }
            }
        }
        g.stroke = previousStroke
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val center1 = this.ssDrawing.residues[this.interaction.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.end-1].center
        if (center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${this.getSecondaryColor().red}, ${this.getSecondaryColor().green}, ${this.getSecondaryColor().blue});stroke-width:${this.getSecondaryInteractionWidth()};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }

}

class TertiaryInteractionLine(parent: SecondaryStructureElement?, interaction: BasePair, ssDrawing: SecondaryStructureDrawing): BaseBaseInteraction(parent, interaction, ssDrawing) {

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion( this.pairedResidue.bounds2D)
            return bounds
        }

    override val type: String = "Tertiary Interaction"

    override val isSelected: Boolean
        get() = this in this.ssDrawing.selection || this.residue in this.ssDrawing.selection && this.pairedResidue in this.ssDrawing.selection

    init {
        this.regularSymbols.add(LWLine(this, this.ssDrawing, this.location, true))
        //+++++left symbol
        this.lwSymbols.add(this.generateSingleSymbol( Location(this.location.start),true, this.interaction.edge5, this.interaction.orientation, right = false))
        //++++++middle symbol
        this.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, true))
        //+++++right symbol
        this.lwSymbols.add(this.generateSingleSymbol(Location(this.location.end),true, this.interaction.edge3, this.interaction.orientation))
    }

    fun drawHalo(g: Graphics2D, at:AffineTransform) {
        val previousStroke = g.stroke
        g.color = Color(this.getTertiaryColor().red, this.getTertiaryColor().green, this.getTertiaryColor().blue, this.getTertiaryOpacity())
        if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) //we fade it even more if unselected
            g.color = Color(g.color.red, g.color.green, g.color.blue, (RnartistConfig.selectionFading /255.0*this.getTertiaryOpacity()).toInt()) //the fading of an unselected interaction is reduced by x%, x is the % of decrease of selection fading (according to full opacity which is 255).
        val shift = this.getHaloWidth()+this.getResidueBorder()/2.0
        val newWidth = (this.ssDrawing.residues[this.interaction.start - 1].circle!!.width) + 2*shift
        var newCircle = Ellipse2D.Double(this.ssDrawing.residues[this.interaction.start - 1].circle!!.centerX-newWidth/2.0, this.ssDrawing.residues[this.interaction.start - 1].circle!!.centerY-newWidth/2.0, newWidth,newWidth)
        g.fill(at.createTransformedShape(newCircle))
        newCircle = Ellipse2D.Double(this.ssDrawing.residues[this.interaction.end - 1].circle!!.centerX-newWidth/2.0, this.ssDrawing.residues[this.interaction.end - 1].circle!!.centerY-newWidth/2.0 , newWidth,newWidth)
        g.fill(at.createTransformedShape(newCircle))
        g.color = Color(
            this.getTertiaryColor().red,
            this.getTertiaryColor().green,
            this.getTertiaryColor().blue,
            255
        )
        g.stroke = previousStroke
    }

    fun draw(g: Graphics2D, at:AffineTransform) {
        val previousStroke = g.stroke
        if (this.isSelected) {
            g.color = Color(this.getTertiaryColor().red, this.getTertiaryColor().green, this.getTertiaryColor().blue, this.getTertiaryOpacity())
            val center1 = this.ssDrawing.residues[this.interaction.start-1].center
            val center2 = this.ssDrawing.residues[this.interaction.end-1].center
            if (center1 != null && center2 != null) {

                val shift = radiusConst + this.getResidueBorder().toDouble() / 2.0
                if (distance(center1, center2) > 2 * shift) {
                    val (p1,p2) = pointsFrom(
                            center1,
                            center2,
                            shift
                    )
                    if (!displayLWSymbols()) {
                        regularSymbols.forEach { nonLwSymbol ->
                            nonLwSymbol.setSymbol(p1,p2)
                            nonLwSymbol.draw(g, at)
                        }
                    } else {
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
                                this.lwSymbols[0].draw(g, at)
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
                                this.lwSymbols[2].draw(g, at)
                            }
                        }

                        //+++++ central line linking the two symbols
                        this.lwSymbols[1].setSymbol(forLine_1, forLine_2)
                        this.lwSymbols[1].draw(g, at)

                    }
                }
            }
        }
        g.color = Color(
            this.getTertiaryColor().red,
            this.getTertiaryColor().green,
            this.getTertiaryColor().blue,
            255
        )
        g.stroke = previousStroke
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val center1 = this.ssDrawing.residues[this.interaction.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.end-1].center
        if (this.getTertiaryInteractionWidth() != 0.0 && center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${this.getSecondaryColor().red}, ${this.getSecondaryColor().green}, ${this.getSecondaryColor().blue});stroke-width:${this.getSecondaryInteractionWidth()};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }

}

class PhosphodiesterBondLine(parent: SecondaryStructureElement?, ssDrawing: SecondaryStructureDrawing, location:Location): SecondaryStructureElement(ssDrawing, parent, "PhosphoDiester Bond", location) {


    val residue:ResidueCircle
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.start).first()

    val nextResidue:ResidueCircle
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.end).first()

    val start:Int
        get() {
            return this.location.start
        }

    val end:Int
        get() {
            return this.location.end
        }

    override val bounds2D: Rectangle2D?
        get() {
            var bounds = this.residue.bounds2D
            bounds = bounds?.createUnion( this.nextResidue.bounds2D)
            return bounds
        }

    override val type: String= "PhosphoDiester Bond"

    override val isSelected: Boolean
        get() = this in ssDrawing.selection || if (this.parent!=null) this.parent!!.isSelected else false


    fun draw(g: Graphics2D, at:AffineTransform) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(this.ssDrawing.finalZoomLevel.toFloat()*this.getPhosphodiesterWidth().toFloat())
        g.color = Color.DARK_GRAY
        if (!this.ssDrawing.selection.isEmpty() && !this.isSelected) {
            g.color = Color(g.color.red, g.color.green, g.color.blue,
                RnartistConfig.selectionFading
            )
        }
        val center1 = this.ssDrawing.residues[this.start-1].center
        val center2 = this.ssDrawing.residues[this.end-1].center
        if (center1 != null && center2 != null) {
            val (p1,p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            if (!this.ssDrawing.residues[this.start-1].circle!!.contains(p2) && distance(
                    p1,
                    p2
                ) > spaceBetweenResidues /2.0) {
                g.draw(at.createTransformedShape(Line2D.Double(p1,p2)))
            }
        }
        g.stroke = previousStroke
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val center1 = this.ssDrawing.residues[this.start-1].center
        val center2 = this.ssDrawing.residues[this.end-1].center
        if (center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue});stroke-width:${this.getPhosphodiesterWidth()};" />""" + "\n"
        }
        return ""
    }

    override fun clearThemes() {
        this.theme.params.clear()
    }
}

enum class SecondaryStructureType {
    A,T,U,G,C,X,SecondaryInteraction,TertiaryInteraction,PhosphodiesterBond
}

enum class ConnectorId(val value:Int) {
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

fun getConnectorId(value:Int): ConnectorId = ConnectorId.values().first { it.value == value}

fun nextConnectorId(c: ConnectorId): ConnectorId = ConnectorId.values().first { it.value == (c.value +1)% ConnectorId.values().size }

fun previousConnectorId(c: ConnectorId): ConnectorId = if (c.value - 1 < 0)  ConnectorId.values().first { it.value == ConnectorId.values().size -1 } else ConnectorId.values().first { it.value == c.value -1 }

fun oppositeConnectorId(c: ConnectorId): ConnectorId = ConnectorId.values().first { it.value == (c.value+ ConnectorId.values().size/2)% ConnectorId.values().size}

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
        Pair(SecondaryStructureType.A,Color.BLACK),
        Pair(SecondaryStructureType.U,Color.BLACK),
        Pair(SecondaryStructureType.G,Color.BLACK),
        Pair(SecondaryStructureType.C,Color.BLACK),
        Pair(SecondaryStructureType.X,Color.BLACK),
        Pair(SecondaryStructureType.SecondaryInteraction,Color.BLACK),
        Pair(SecondaryStructureType.TertiaryInteraction,Color.BLACK),
        Pair(SecondaryStructureType.PhosphodiesterBond,Color.BLACK)
)

/**
Compute the center of a circle according to the entry point
 **/
fun centerFrom(inId: ConnectorId, inPoint:Point2D, radius:Double): Point2D {
    when (inId) {
        ConnectorId.s -> return Point2D.Double(inPoint.x, inPoint.y - radius)
        ConnectorId.sso -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (-3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (-3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.so -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (-2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (-2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.oso -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (-360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (-360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.o -> return Point2D.Double(inPoint.x + radius, inPoint.y)
        ConnectorId.ono -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.no -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.nno -> return Point2D.Double(inPoint.x + adjacentSideFrom(
            (3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y + oppositeSideFrom(
            (3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.n -> return Point2D.Double(inPoint.x, inPoint.y + radius)
        ConnectorId.nne -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (-3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (-3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.ne -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (-2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (-2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.ene -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (-360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (-360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.e -> return Point2D.Double(inPoint.x - radius, inPoint.y)
        ConnectorId.ese -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.se -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (2 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
        ConnectorId.sse -> return Point2D.Double(inPoint.x - adjacentSideFrom(
            (3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        ),inPoint.y - oppositeSideFrom(
            (3 * 360 / ConnectorId.values().size).toDouble(),
            radius
        )
        )
    }
}

fun rotatePoint(start:Point2D, center:Point2D, degrees:Double): Point2D {
    //we set the rotation
    val rot = AffineTransform()
    rot.setToRotation(degrees* degreesToRadians, center.x, center.y)
    //we get the rotated point with this transformation
    val pointRot = rot.transform(start, null)
    return pointRot
}

fun <T> interleaveArrays(first: List<T>, second:List<T>): List<T> {
    val commonLength = Math.min(first.size, second.size)
    return (first zip second).flatMap { it.toList() } + first.subList(commonLength, first.size) + second.subList(commonLength, second.size)
}

fun ccw(A: Point2D, B: Point2D, C: Point2D): Boolean {
    return (C.y-A.y)*(B.x-A.x) > (B.y-A.y)*(C.x-A.x)
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

fun intersects(center1: Point2D, radius1: Double, center2: Point2D, radius2: Double) : Boolean {
    return hypot(center1.x - center2.x, center1.y - center2.y) <= (radius1 + radius2);
}

/**
Return two new points far from p1 and b2 by dist.
 **/
fun pointsFrom(p1:Point2D, p2:Point2D, dist:Double): Pair<Point2D, Point2D> {
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

    return Pair(Point2D.Double(newX1,newY1), Point2D.Double(newX2,newY2))
}

fun distance(p1:Point2D, p2:Point2D): Double {
    val h = p2.x-p1.x
    val v = p2.y-p1.y
    return Math.sqrt(h*h+v*v)
}

fun angleFrom(oppositeSide: Double, adjacentSide: Double ) : Double {
    return Math.atan(oppositeSide/adjacentSide)* radiansToDegrees
}

fun angleFrom(p1: Point2D, p2: Point2D, p3: Point2D) : Double {
    val a = distance(p1, p2)
    val b = distance(p2, p3)
    val c = distance(p1, p3)
    return Math.acos((a*a+c*c-b*b)/(2*a*c))* radiansToDegrees
}

fun adjacentSideFrom(degrees:Double, hypotenuse:Double) :Double {
    return Math.cos(degrees* degreesToRadians)*hypotenuse
}

fun oppositeSideFrom(degrees:Double, hypotenuse:Double) :Double {
    return Math.sin(degrees* degreesToRadians)*hypotenuse
}

fun crossProduct(sharedPoint: Point2D, p2: Point2D, p3: Point2D) :  Double {
    val a1 = p2.x - sharedPoint.x
    val a2 = p2.y - sharedPoint.y
    val b1 = p3.x - sharedPoint.x
    val b2 = p3.y - sharedPoint.y
    return a1 * b2 - a2 * b1
}

fun getPerpendicular(p0:Point2D, p1:Point2D, p2:Point2D, distance:Double) : Pair<Point2D,Point2D> {
    val angle = angleFrom(p1.y - p2.y, p1.x - p2.x)
    if (angle < 0) {
        return Pair<Point2D,Point2D>(Point2D.Double(p0.x + oppositeSideFrom(
            angle,
            distance
        ), p0.y - adjacentSideFrom(angle, distance)
        ), Point2D.Double(p0.x - oppositeSideFrom(
            angle,
            distance
        ), p0.y + adjacentSideFrom(angle, distance)
        ))
    } else {
        return Pair<Point2D,Point2D>(Point2D.Double(p0.x - oppositeSideFrom(
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

fun getAWTColor(htmlColor: String): Color? {
    val r: Int
    val g: Int
    val b: Int
    require(!(htmlColor.length != 7 || htmlColor[0] != '#')) { "$htmlColor is not an HTML color string" }
    r = htmlColor.substring(1, 3).toInt(16)
    g = htmlColor.substring(3, 5).toInt(16)
    b = htmlColor.substring(5, 7).toInt(16)
    return Color(r, g, b)
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