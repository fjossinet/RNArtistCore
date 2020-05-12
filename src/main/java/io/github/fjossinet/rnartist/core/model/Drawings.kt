package io.github.fjossinet.rnartist.core.model

import io.github.fjossinet.rnartist.core.model.RnartistConfig.defaultTheme
import java.awt.*
import java.awt.Color
import java.awt.geom.*
import java.awt.image.BufferedImage
import kotlin.math.hypot


val radiusConst:Double = 15.0
val spaceAfter:Double = 5.0
val minimalCircumference:Float = 360F/((ConnectorId.values().size)* radiusConst *3).toFloat()
val minimalRadius:Float = minimalCircumference /(2F*Math.PI).toFloat()
val radiansToDegrees = 180 / Math.PI
val degreesToRadians = Math.PI / 180

fun helixDrawingLength(h: Helix):Double {
    return (h.length-1).toDouble() * radiusConst * 2.0 + (h.length-1).toDouble() * spaceAfter
}

fun helixDrawingWidth():Double {
    return radiusConst *8.0 /* each residue = 4*radius + 4*radius for the base-pair symbol (1 radius for a shape (squarre, triangle, circle, line) + 2*0.5 radius to have space between the residue circles and the symbols)*/
}

class Project(var secondaryStructure: SecondaryStructure, var tertiaryStructure: TertiaryStructure?, var theme:Map<String,String>, var graphicsContext:Map<String,String>) {

}

class WorkingSession() {

    var viewX = 0.0
    var viewY = 0.0
    var finalZoomLevel = 1.0
    var screen_capture = false
    var screen_capture_area: Rectangle2D? = null
    val selectedResidues = mutableListOf<ResidueCircle>()

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

    fun centerFrameOnSelection(frame: Rectangle2D) {
        val at = AffineTransform()
        at.translate(viewX, viewY)
        at.scale(finalZoomLevel, finalZoomLevel)

        val centers = mutableListOf<Point2D>()
        for (res in this.selectedResidues) {
            val bounds = at.createTransformedShape(res.circle).bounds2D
            centers.add(Point2D.Double(bounds.centerX, bounds.centerY))
        }

        val centroid = centroid(centers)

        viewX += frame.bounds2D.centerX - centroid.x
        viewY += frame.bounds2D.centerY - centroid.y
    }
}

interface ThemeConfigurator {
    fun getHaloWidth():Int
    fun getTertiaryOpacity():Int
    fun getResidueCharOpacity(): Int
    fun getTertiaryInteractionStyle():String
    fun getResidueBorder():Int
    fun getSecondaryInteractionWidth():Int
    fun getTertiaryInteractionWidth():Int
    fun getDeltaXRes():Int
    fun getDeltaYRes():Int
    fun getDeltaFontSize():Int
    fun getAColor():String //HTML Color code
    fun getAChar():String //HTML Color code
    fun getUColor():String //HTML Color code
    fun getUChar():String //HTML Color code
    fun getGColor():String //HTML Color code
    fun getGChar():String //HTML Color code
    fun getCColor():String //HTML Color code
    fun getCChar():String //HTML Color code
    fun getXColor():String //HTML Color code
    fun getXChar():String //HTML Color code
    fun getSecondaryInteractionColor():String //HTML Color code
    fun getTertiaryInteractionColor():String //HTML Color code
    fun getFontName():String
}

fun transparentColor(source:Color, alpha:Int) = Color(source.red, source.green, source.blue, alpha);

@JvmField
var DASHED = "dashed"
@JvmField
var SOLID = "solid"

enum class ThemeParameter {
    AColor, AChar, UColor, UChar, GColor, GChar, CColor, CChar, XColor, XChar, SecondaryColor, TertiaryColor, ResidueCharOpacity, HaloWidth, TertiaryOpacity, SecondaryInteractionWidth, TertiaryInteractionWidth, TertiaryInteractionStyle, ResidueBorder, FontName, DeltaXRes, DeltaYRes, DeltaFontSize
}

class Theme(defaultParams:MutableMap<String,String> = defaultTheme, val themeConfigurator: ThemeConfigurator? = null) {

    val themeParams:MutableMap<String,String> = mutableMapOf()
    var haloWidth: Int
        set(value) {
            this.themeParams.set(ThemeParameter.HaloWidth.toString(), "${value}")
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.HaloWidth.toString(), "${it.getHaloWidth()}")
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.HaloWidth.toString()))
        }
    var tertiaryOpacity: Int
        set(value) {
            this.themeParams.set(ThemeParameter.TertiaryOpacity.toString(), "${value}")
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.TertiaryOpacity.toString(), "${it.getTertiaryOpacity()}")
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.TertiaryOpacity.toString()))
        }
    var residueCharOpacity: Int
        set(value) {
            this.themeParams.set(ThemeParameter.ResidueCharOpacity.toString(), "${value}")
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.ResidueCharOpacity.toString(), "${it.getResidueCharOpacity()}")
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.ResidueCharOpacity.toString()))
        }
    var tertiaryInteractionStyle: String
        set(value) {
            this.themeParams.set(ThemeParameter.TertiaryInteractionStyle.toString(), value)
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.TertiaryInteractionStyle.toString(), it.getTertiaryInteractionStyle())
            }
            return this.themeParams.get(ThemeParameter.TertiaryInteractionStyle.toString())!!
        }
    var residueBorder: Int
        set(value) {
            this.themeParams.set(ThemeParameter.ResidueBorder.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.ResidueBorder.toString(), it.getResidueBorder().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.ResidueBorder.toString()))
        }
    var secondaryInteractionWidth: Int
        set(value) {
            this.themeParams.set(ThemeParameter.SecondaryInteractionWidth.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.SecondaryInteractionWidth.toString(), it.getSecondaryInteractionWidth().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.SecondaryInteractionWidth.toString()))
        }
    var tertiaryInteractionWidth: Int
        set(value) {
            this.themeParams.set(ThemeParameter.TertiaryInteractionWidth.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.TertiaryInteractionWidth.toString(), it.getTertiaryInteractionWidth().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.TertiaryInteractionWidth.toString()))
        }
    var deltaXRes: Int
        set(value) {
            this.themeParams.set(ThemeParameter.DeltaXRes.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.DeltaXRes.toString(), it.getDeltaXRes().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.DeltaXRes.toString()))
        }
    var deltaYRes: Int
        set(value) {
            this.themeParams.set(ThemeParameter.DeltaYRes.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.DeltaYRes.toString(), it.getDeltaYRes().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.DeltaYRes.toString()))
        }
    var deltaFontSize: Int
        set(value) {
            this.themeParams.set(ThemeParameter.DeltaFontSize.toString(), value.toString())
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.DeltaFontSize.toString(), it.getDeltaFontSize().toString())
            }
            return Integer.parseInt(this.themeParams.get(ThemeParameter.DeltaFontSize.toString()))
        }
    var AColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.AColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.AColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getAColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.AColor.toString()
                )!!
            )!!
        }
    var AChar: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.AChar.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.AChar.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getAChar())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.AChar.toString()
                )!!
            )!!
        }
    var UColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.UColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.UColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getUColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.UColor.toString()
                )!!
            )!!
        }
    var UChar: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.UChar.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.UChar.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getUChar())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.UChar.toString()
                )!!
            )!!
        }
    var GColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.GColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.GColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getGColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.GColor.toString()
                )!!
            )!!
        }
    var GChar: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.GChar.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.GChar.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getGChar())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.GChar.toString()
                )!!
            )!!
        }
    var CColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.CColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.CColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getCColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.CColor.toString()
                )!!
            )!!
        }
    var CChar: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.CChar.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.CChar.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getCChar())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.CChar.toString()
                )!!
            )!!
        }
    var XColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.XColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.XColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getXColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.XColor.toString()
                )!!
            )!!
        }
    var XChar: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.XChar.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.XChar.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getXChar())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.XChar.toString()
                )!!
            )!!
        }
    var SecondaryColor: Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.SecondaryColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.SecondaryColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getSecondaryInteractionColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.SecondaryColor.toString()
                )!!
            )!!
        }
    var TertiaryColor:Color
        set(value) {
            this.themeParams.set(
                ThemeParameter.TertiaryColor.toString(),
                getHTMLColorString(value)
            )
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(
                    ThemeParameter.TertiaryColor.toString(),
                    getHTMLColorString(
                        getAWTColor(it.getTertiaryInteractionColor())!!
                    )
                )
            }
            return getAWTColor(
                this.themeParams.get(
                    ThemeParameter.TertiaryColor.toString()
                )!!
            )!!
        }
    var fontName:String
        set(value) {
            this.themeParams.set(ThemeParameter.FontName.toString(), value)
        }
        get() {
            themeConfigurator?.let {
                this.themeParams.set(ThemeParameter.FontName.toString(), it.getFontName())
            }
            return this.themeParams.get(ThemeParameter.FontName.toString())!!
        }
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
            this.themeParams.put(k,v)
        }
    }

}

fun computeOptimalFontSize(g: Graphics2D, gc: WorkingSession, theme: Theme, title: String, width: Double, height: Double): Int {
    var dimension: Dimension?
    var fontSize = (100*gc.finalZoomLevel).toInt() //initial value
    do {
        fontSize--
        val font = Font(theme.fontName, theme.fontStyle, fontSize)
        dimension = getStringBoundsRectangle2D(g, title, font)
    } while (dimension!!.width >= width-width*0.5+width*theme.deltaFontSize.toDouble()/20.0 && dimension.height >= height-height*0.5+height*theme.deltaFontSize.toDouble()/20.0)
    return fontSize;
}

fun getStringBoundsRectangle2D(g: Graphics2D, title: String, font: Font): Dimension {
    g.font = font
    val fm = g.fontMetrics
    val lm  = font.getLineMetrics(title, g.fontRenderContext);
    val r = fm.getStringBounds(title, g)
    return Dimension(r.getWidth().toInt(), (lm.ascent-lm.descent).toInt())
}

class SecondaryStructureDrawing(val secondaryStructure: SecondaryStructure, frame:Rectangle2D = Rectangle(0,0,Toolkit.getDefaultToolkit().getScreenSize().width,Toolkit.getDefaultToolkit().getScreenSize().height), val theme: Theme = Theme(), val workingSession: WorkingSession = WorkingSession()) {

    var name:String? = null
    val branches = mutableListOf<JunctionCircle>()
    val helices = mutableListOf<HelixLine>()
    val singleStrands = mutableListOf<SingleStrandLine>()
    val residues = mutableListOf<ResidueCircle>()
    val phosphodiesterBonds = mutableListOf<PhosphodiesterBondLine>()
    val secondaryInteractions = mutableListOf<SecondaryInteractionLine>()
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

    init {
        this.secondaryStructure.rna.seq.forEachIndexed { index,char -> this.residues.add(
            ResidueCircle(
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
                            SingleStrand(
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

                lastBranchConstructed = JunctionCircle(
                    circles,
                    lines,
                    null,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )

                this.helices.add(
                    HelixLine(
                        nextHelix.third,
                        bottom,
                        top
                    )
                )

                if (residuesBeforeHelix > 0) {
                    this.singleStrands.add(
                        SingleStrandLine(
                            SingleStrand(
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
                val newBranchConstructed = JunctionCircle(
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
                            SingleStrand(
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

                lastBranchConstructed = JunctionCircle(
                    circles,
                    lines,
                    null,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )

                this.helices.add(
                    HelixLine(
                        nextHelix.third,
                        bottom,
                        top
                    )
                )

                this.branches.add(lastBranchConstructed)
            }

            currentPos = nextHelix.second

        } while (currentPos < this.secondaryStructure.rna.seq.length)


        //now the residues
        for (helix in this.helices) {
            this.computeResidues(helix)
            for (interaction in helix.helix.secondaryInteractions) {
                this.secondaryInteractions.add(
                    SecondaryInteractionLine(
                        interaction,
                        this
                    )
                )
            }
        }

        for (singleStrand in this.singleStrands) {
            if (singleStrand.ss.location.start == 1) {
                if (singleStrand.ss.location.length != 1) {
                    val step = distance(
                        this.residues[0].center!!,
                        this.residues[singleStrand.ss.location.end].center!!
                    ) /(singleStrand.ss.location.length).toDouble()
                    for (i in singleStrand.ss.location.start+1..singleStrand.ss.location.end) {
                        val (p1_1,_) = pointsFrom(
                            this.residues[0].center!!,
                            this.residues[singleStrand.ss.location.end].center!!,
                            step * (i - singleStrand.ss.location.start).toDouble()
                        )
                        this.residues[i-1].center = p1_1
                    }
                }
            } else if (singleStrand.ss.location.end == this.secondaryStructure.length) {
                if (singleStrand.ss.location.length != 1) {
                    val step = distance(
                        this.residues[singleStrand.ss.location.start - 2].center!!,
                        this.residues[this.secondaryStructure.length - 1].center!!
                    ) /(singleStrand.ss.location.length).toDouble()
                    for (i in singleStrand.ss.location.start until singleStrand.ss.location.end) {
                        val (p1_1,_) = pointsFrom(
                            this.residues[singleStrand.ss.location.start - 2].center!!,
                            this.residues[this.secondaryStructure.length - 1].center!!,
                            step * (i - (singleStrand.ss.location.start - 1).toDouble())
                        )
                        this.residues[i-1].center = p1_1
                    }
                }
            } else {
                val step = distance(
                    this.residues[singleStrand.ss.location.start - 2].center!!,
                    this.residues[singleStrand.ss.location.end].center!!
                ) /(singleStrand.ss.location.length+1).toDouble()
                for (i in singleStrand.ss.location.start..singleStrand.ss.location.end) {
                    val (p1_1,_) = pointsFrom(
                        this.residues[singleStrand.ss.location.start - 2].center!!,
                        this.residues[singleStrand.ss.location.end].center!!,
                        step * (i - (singleStrand.ss.location.start - 1).toDouble())
                    )
                    this.residues[i-1].center = p1_1
                }
            }
        }

        for (branch in this.branches) {
            this.computeResidues(branch)

            for (helix in branch.helicesFromBranch()) {
                for (interaction in helix.helix.secondaryInteractions) {
                    this.secondaryInteractions.add(
                        SecondaryInteractionLine(
                            interaction,
                            this
                        )
                    )
                }
            }
        }

        for (i in 1 until this.secondaryStructure.length) {
            this.phosphodiesterBonds.add(
                PhosphodiesterBondLine(
                    i,
                    i + 1,
                    this
                )
            )
        }

        for (interaction in this.secondaryStructure.tertiaryInteractions) {
            this.tertiaryInteractions.add(
                TertiaryInteractionLine(
                    interaction,
                    this
                )
            )
        }
    }

    fun getResiduesFromAbsPositions(positions:List<Int>): List<ResidueCircle> {
        val _residues:MutableList<ResidueCircle> = mutableListOf<ResidueCircle>()
        for (r: ResidueCircle in residues) {
            if (r.absPos in positions) {
                _residues.add(r)
            }
        }
        return _residues
    }

    fun getBounds():Rectangle {
        val minX = this.residues.minBy { it.circle!!.minX }!!.circle!!.minX-theme.residueBorder
        val minY = this.residues.minBy { it.circle!!.minY }!!.circle!!.minY-theme.residueBorder
        val maxX = this.residues.maxBy { it.circle!!.maxX }!!.circle!!.maxX+theme.residueBorder
        val maxY = this.residues.maxBy { it.circle!!.maxY }!!.circle!!.maxY+theme.residueBorder
        return Rectangle(minX.toInt(), minY.toInt(), (maxX-minX).toInt(), (maxY-minY).toInt())
    }

    fun draw (g: Graphics2D) {
        if (!theme.quickDraw) {
            val at = AffineTransform()
            at.translate(workingSession.viewX, workingSession.viewY)
            at.scale(workingSession.finalZoomLevel, workingSession.finalZoomLevel)
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
        /*for (jC in this.branches) {
            jC.draw(g, gc)
        }

        for (h in this.helices) {
            h.draw(g,gc)
        }

        for (ss in this.singleStrands) {
            ss.draw(g,gc)
        }*/

        for (bond in this.phosphodiesterBonds) {
            bond.draw(g, workingSession, this.theme)
        }


        for (interaction in this.secondaryInteractions) {
            interaction.draw(g, workingSession, this.theme)
        }

        if (!theme.quickDraw && theme.tertiaryOpacity > 0)
            for (interaction in this.tertiaryInteractions) {
                interaction.draw(g, workingSession, this.theme)
            }

        for (residue in this.residues) {
            residue.draw(g, workingSession, this.theme)
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
            radiusConst * 2.0
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
            radiusConst * 2.0
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
        secondaryInteractions.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        phosphodiesterBonds.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        tertiaryInteractions.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        residues.map { it.asSVG(indentLevel = 1, theme = theme, transX = -bounds.minX, transY = -bounds.minY)}.forEach { svgBuffer.append(it) }
        svgBuffer.append("</svg>")
        return svgBuffer.toString()
    }

    override fun toString(): String {
        return if (this.name != null) this.name!! else this.secondaryStructure.rna.toString()
    }
}

class ResidueCircle(val absPos:Int, label:Char) {

    val label: SecondaryStructureElement
    var circle: Ellipse2D? = null
    var center:Point2D? = null
        set(value) {
            field = value
            this.circle = Ellipse2D.Double(value!!.x- radiusConst, value.y- radiusConst, (radiusConst *2F).toDouble(), (radiusConst *2F).toDouble())
        }

    init {
        when(label) {
            'A' -> this.label = SecondaryStructureElement.A
            'U' -> this.label = SecondaryStructureElement.U
            'G' -> this.label = SecondaryStructureElement.G
            'C' -> this.label = SecondaryStructureElement.C
            else ->  this.label = SecondaryStructureElement.X
        }
    }

    fun draw(g: Graphics2D, gc: WorkingSession, theme: Theme) {
        if (this.circle != null) {
            val at = AffineTransform()
            at.translate(gc.viewX,gc.viewY)
            at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
            val _c = at.createTransformedShape(this.circle)
            g.color = getColor(theme)
            if (!gc.selectedResidues.isEmpty() && !(this in gc.selectedResidues)) {
                g.color = Color(g.color.red, g.color.green, g.color.blue,
                    RnartistConfig.selectionFading
                )
            }
            g.fill(_c)
            if (!theme.quickDraw) {
                val previousStroke: Stroke = g.getStroke()
                g.stroke = BasicStroke(gc.finalZoomLevel.toFloat() * theme.residueBorder)
                g.color = Color.DARK_GRAY
                if (!gc.selectedResidues.isEmpty() && !(this in gc.selectedResidues)) {
                    g.color = Color(g.color.red, g.color.green, g.color.blue,
                        RnartistConfig.selectionFading
                    )
                }
                g.draw(_c)
                g.stroke = previousStroke
            }
            if (g.font.size > 5 && !theme.quickDraw && theme.residueCharOpacity  > 0) { //the conditions to draw a letter
                when (this.label.name) {
                    "A" -> g.color = theme.AChar
                    "U" -> g.color = theme.UChar
                    "G" -> g.color = theme.GChar
                    "C" -> g.color = theme.CChar
                    "X" -> g.color = theme.XChar
                }
                g.color = Color(g.color.red, g.color.green, g.color.blue, theme.residueCharOpacity) //we fade the residue letter
                if (!gc.selectedResidues.isEmpty() && !(this in gc.selectedResidues)) //we fade it even more if unselected
                    g.color = Color(g.color.red, g.color.green, g.color.blue, (RnartistConfig.selectionFading /255.0*theme.residueCharOpacity).toInt()) //the residue fading of an unselected residue is reduced by x%, x is the % of decrease of selection fading (according to full opacity which is 255).
                when (this.label.name) {
                    "A" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + theme.ATransX + (theme.deltaXRes * gc.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + theme.ATransY - (theme.deltaYRes * gc.finalZoomLevel).toFloat())
                    "U" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + theme.UTransX + (theme.deltaXRes * gc.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + theme.UTransY - (theme.deltaYRes * gc.finalZoomLevel).toFloat())
                    "G" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + theme.GTransX + (theme.deltaXRes * gc.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + theme.GTransY - (theme.deltaYRes * gc.finalZoomLevel).toFloat())
                    "C" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + theme.CTransX + (theme.deltaXRes * gc.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + theme.CTransY - (theme.deltaYRes * gc.finalZoomLevel).toFloat())
                    "X" -> g.drawString(this.label.name, _c.bounds2D.minX.toFloat() + theme.XTransX + (theme.deltaXRes * gc.finalZoomLevel).toFloat(), _c.bounds2D.minY.toFloat() + theme.XTransY - (theme.deltaYRes * gc.finalZoomLevel).toFloat())
                }
            }
        }
    }

    fun getColor(theme: Theme):Color {
       return when (this.label) {
            SecondaryStructureElement.A -> theme.AColor
            SecondaryStructureElement.U -> theme.UColor
            SecondaryStructureElement.G -> theme.GColor
            SecondaryStructureElement.C -> theme.CColor
            else -> theme.XColor
        }
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val buff = StringBuffer(indentChar.repeat(indentLevel)+"<g>\n")
        buff.append(indentChar.repeat(indentLevel+1)+"""<circle cx="${this.circle!!.centerX+transX}" cy="${this.circle!!.centerY+transY}" r="${this.circle!!.width/2}" stroke="rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue})" stroke-width="${theme.residueBorder}" fill="rgb(${getColor(theme).red}, ${getColor(theme).green}, ${getColor(theme).blue})" />"""+"\n")
        if (RnartistConfig.exportSVGWithBrowserCompatibility())
            buff.append(indentChar.repeat(indentLevel+1)+"""<text x="${this.circle!!.centerX+transX+theme.deltaXRes}" y="${this.circle!!.centerY+transY+theme.deltaYRes}" text-anchor="middle" dy=".3em" style="fill:rgb(${Color.WHITE.red}, ${Color.WHITE.green}, ${Color.WHITE.blue});font-family:${theme.fontName};font-size:${theme.fontSize};">${this.label.name}</text>"""+"\n")
        else
            buff.append(indentChar.repeat(indentLevel+1)+"""<text x="${this.circle!!.bounds2D.minX.toFloat()+transX+theme.deltaXRes + when (this.label) { SecondaryStructureElement.A -> theme.ATransX ; SecondaryStructureElement.U -> theme.UTransX ; SecondaryStructureElement.G -> theme.GTransX ; SecondaryStructureElement.C -> theme.CTransX ; else -> theme.XTransX } }" y="${this.circle!!.bounds2D.minY.toFloat()+transY+theme.deltaYRes + when (this.label) { SecondaryStructureElement.A -> theme.ATransY ; SecondaryStructureElement.U -> theme.UTransY ; SecondaryStructureElement.G -> theme.GTransY ; SecondaryStructureElement.C -> theme.CTransY ; else -> theme.XTransY } }" style="fill:rgb(${Color.WHITE.red}, ${Color.WHITE.green}, ${Color.WHITE.blue});font-family:${theme.fontName};font-size:${theme.fontSize};">${this.label.name}</text>"""+"\n")
        buff.append(indentChar.repeat(indentLevel)+"</g>\n")
        return buff.toString()
    }

}

class HelixLine(val helix: Helix, start:Point2D, end:Point2D) {

    val line:Line2D = Line2D.Double(start,end)

    fun draw(g: Graphics2D, gc: WorkingSession) {
        val at = AffineTransform()
        at.translate(gc.viewX,gc.viewY)
        at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
        g.draw(at.createTransformedShape(this.line));
    }
}

class SingleStrandLine(val ss: SingleStrand, start:Point2D, end:Point2D) {

    var line = Line2D.Double(start,end)

    fun draw(g: Graphics2D, gc: WorkingSession) {
        val at = AffineTransform()
        at.translate(gc.viewX,gc.viewY)
        at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
        g.draw(at.createTransformedShape(this.line))
    }
}

class JunctionCircle (circlesFromBranchSoFar: MutableList<Triple<Point2D, Double, Ellipse2D>>, linesFromBranchSoFar: MutableList<List<Point2D>>, previousJunction: JunctionCircle? = null, var inId: ConnectorId, inPoint:Point2D, inHelix: Helix, val junction: Junction) {

    val noOverlapWithLines = true
    val noOverlapWithCircles = true
    var helices = mutableListOf<HelixLine>()
    var connectedJunctions = mutableMapOf<ConnectorId, JunctionCircle>()
    val connectors:Array<Point2D> = Array(ConnectorId.values().size, { Point2D.Float(0F,0F) } ) //the connector points on the circle
    var layout:MutableList<ConnectorId>? = defaultLayouts[this.junction.type]?.toMutableList()
        set(value) {
            //we order the helices according to the start but with inHelix as the first one
            val sortedHelix = this.junction.helicesLinked.sortedBy { it.location.start - this.inHelix.location.start}
            field = value
            //we change the entry point for each connected circle, we update the self.connectedCircles dict and we warn the connected circles that their entry point has been changed (we call their setEntryPoint() function)
            var newConnectedJunctions = mutableMapOf<ConnectorId, JunctionCircle>() //we need to store the new connections in a temp dict otherwise the update of a connection could remove an old connection stored and not already checked.
            this.helices = mutableListOf<HelixLine>()
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
                            this.helices.add(
                                HelixLine(
                                    helix,
                                    this.connectors[outId.value],
                                    inPoint
                                )
                            )
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
                            this.helices.add(
                                HelixLine(
                                    helix,
                                    this.connectors[outId.value],
                                    inPoint
                                )
                            )
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

    init {
        this.connectors[this.inId.value] = inPoint
        //we compute the initial radius according to the junction length and type
        val circumference = (this.junction.length.toFloat() - this.junction.type.value*2).toFloat() * (radiusConst * 2).toFloat() + this.junction.type.value* helixDrawingWidth()
        this.radius = circumference/(2F*Math.PI).toDouble()
        circlesFromBranchSoFar.add(Triple<Point2D,Double, Ellipse2D>(this.center, this.radius, this.circle)) //this array allows to get easily the shapes already drawn for the branch in order to avoid overlaps with the shapes for this junction

        val sortedHelix = this.junction.helicesLinked.sortedBy { it.location.start }

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
                this.helices.add(
                    HelixLine(
                        helix,
                        this.connectors[outId.value],
                        inPoint
                    )
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

                this.connectedJunctions[outId] = JunctionCircle(
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

    fun draw(g: Graphics2D, gc: WorkingSession) {
        val at = AffineTransform()
        at.translate(gc.viewX,gc.viewY)
        at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
        g.draw(at.createTransformedShape(this.circle));

        for (helix in this.helices) {
            helix.draw(g,gc)
        }

        for ((_, junction) in this.connectedJunctions) {
            junction.draw(g,gc)
        }

    }

}

class SecondaryInteractionLine(val interaction: BasePair, val ssDrawing: SecondaryStructureDrawing) {

    fun draw(g: Graphics2D, gc: WorkingSession, theme: Theme) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(gc.finalZoomLevel.toFloat()*theme.secondaryInteractionWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)
        g.color = theme.SecondaryColor
        if (!gc.selectedResidues.isEmpty() && ( !(this.ssDrawing.residues[this.interaction.location.start-1] in gc.selectedResidues) || !(this.ssDrawing.residues[this.interaction.location.end-1] in gc.selectedResidues))) {
            g.color = Color(g.color.red, g.color.green, g.color.blue,
                RnartistConfig.selectionFading
            )
        }
        val center1 = this.ssDrawing.residues[this.interaction.location.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.location.end-1].center
        if (center1 != null && center2 != null) {
            val (p1,p2) = pointsFrom(
                center1,
                center2,
                radiusConst * 1.4
            )
            val at = AffineTransform()
            at.translate(gc.viewX,gc.viewY)
            at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
            g.draw(at.createTransformedShape(Line2D.Double(p1,p2)))
        }
        g.stroke = previousStroke
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val center1 = this.ssDrawing.residues[this.interaction.location.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.location.end-1].center
        if (center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${theme.SecondaryColor.red}, ${theme.SecondaryColor.green}, ${theme.SecondaryColor.blue});stroke-width:${theme.secondaryInteractionWidth};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }

}

class TertiaryInteractionLine(val interaction: BasePair, val ssDrawing: SecondaryStructureDrawing) {

    fun draw(g: Graphics2D, gc: WorkingSession, theme: Theme) {
        val previousStroke = g.stroke
        val at = AffineTransform()
        at.translate(gc.viewX, gc.viewY)
        at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
        val center1 = this.ssDrawing.residues[this.interaction.location.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.location.end-1].center
        if (theme.tertiaryInteractionWidth != 0 && center1 != null && center2 != null) {
            g.color = Color(theme.TertiaryColor.red, theme.TertiaryColor.green, theme.TertiaryColor.blue, theme.tertiaryOpacity)
            if (!gc.selectedResidues.isEmpty() && ( !(this.ssDrawing.residues[this.interaction.location.start-1] in gc.selectedResidues) || !(this.ssDrawing.residues[this.interaction.location.end-1] in gc.selectedResidues))) {
                g.color = Color(g.color.red, g.color.green, g.color.blue,
                    RnartistConfig.selectionFading
                )
            }
            if (theme.tertiaryInteractionStyle == DASHED)
                g.stroke = BasicStroke(gc.finalZoomLevel.toFloat() * theme.tertiaryInteractionWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0F, floatArrayOf(gc.finalZoomLevel.toFloat() * theme.tertiaryInteractionWidth * 2), 0F)
            else
                g.stroke = BasicStroke(gc.finalZoomLevel.toFloat()*theme.tertiaryInteractionWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst
            )
            g.draw(at.createTransformedShape(Line2D.Double(p1, p2)))
        }
        g.stroke = BasicStroke(gc.finalZoomLevel.toFloat()*theme.haloWidth)
        g.color = Color(theme.TertiaryColor.red, theme.TertiaryColor.green, theme.TertiaryColor.blue, theme.tertiaryOpacity)
        if (!gc.selectedResidues.isEmpty() && ( !(this.ssDrawing.residues[this.interaction.location.start-1] in gc.selectedResidues) || !(this.ssDrawing.residues[this.interaction.location.end-1] in gc.selectedResidues))) {
            g.color = Color(g.color.red, g.color.green, g.color.blue,
                RnartistConfig.selectionFading
            )
        }
        g.draw(at.createTransformedShape(this.ssDrawing.residues[this.interaction.location.start-1].circle))
        g.draw(at.createTransformedShape(this.ssDrawing.residues[this.interaction.location.end-1].circle))
        g.stroke = previousStroke
    }

    fun asSVG(indentChar:String ="\t", indentLevel:Int = 1, theme: Theme, transX:Double= 0.0, transY:Double = 0.0):String {
        val center1 = this.ssDrawing.residues[this.interaction.location.start-1].center
        val center2 = this.ssDrawing.residues[this.interaction.location.end-1].center
        if (theme.tertiaryInteractionWidth != 0 && center1 != null && center2 != null) {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst * 1.4
            )
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${theme.SecondaryColor.red}, ${theme.SecondaryColor.green}, ${theme.SecondaryColor.blue});stroke-width:${theme.secondaryInteractionWidth};stroke-linecap:round;" />""" + "\n"
        }
        return ""
    }

}

class PhosphodiesterBondLine(val start:Int, val end:Int, val ssDrawing: SecondaryStructureDrawing) {

    fun draw(g: Graphics2D, gc: WorkingSession, theme: Theme) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(gc.finalZoomLevel.toFloat()*2)
        g.color = Color.DARK_GRAY
        if (!gc.selectedResidues.isEmpty() && ( !(this.ssDrawing.residues[this.start-1] in gc.selectedResidues) || !(this.ssDrawing.residues[this.end-1] in gc.selectedResidues))) {
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
                radiusConst
            )
            if (!this.ssDrawing.residues[this.start-1].circle!!.contains(p2) && distance(
                    p1,
                    p2
                ) > spaceAfter /2.0) {
                val at = AffineTransform()
                at.translate(gc.viewX,gc.viewY)
                at.scale(gc.finalZoomLevel, gc.finalZoomLevel)
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
            return indentChar.repeat(indentLevel) + """<path d="M${p1.x+transX},${p1.y+transY}l${p2.x-p1.x},${p2.y-p1.y}" style="fill:none;stroke:rgb(${Color.DARK_GRAY.red}, ${Color.DARK_GRAY.green}, ${Color.DARK_GRAY.blue});stroke-width:2;" />""" + "\n"
        }
        return ""
    }

}

enum class SecondaryStructureElement {
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

typealias ColorScheme = Map<SecondaryStructureElement, Color>

val Fall: ColorScheme = mapOf(
        Pair(SecondaryStructureElement.A,Color.BLACK),
        Pair(SecondaryStructureElement.U,Color.BLACK),
        Pair(SecondaryStructureElement.G,Color.BLACK),
        Pair(SecondaryStructureElement.C,Color.BLACK),
        Pair(SecondaryStructureElement.X,Color.BLACK),
        Pair(SecondaryStructureElement.SecondaryInteraction,Color.BLACK),
        Pair(SecondaryStructureElement.TertiaryInteraction,Color.BLACK),
        Pair(SecondaryStructureElement.PhosphodiesterBond,Color.BLACK)
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

fun centroid(points: List<Point2D>): Point2D {
    return if (points.size == 1) {
        points.first()
    }
    else {
        val centroid = doubleArrayOf(0.0, 0.0)
        var i = 0
        while (i < points.size) {
            centroid[0] += points[i].x
            centroid[1] += points[i].y
            i += 2
        }
        val totalPoints = points.size / 2
        centroid[0] = centroid[0] / totalPoints
        centroid[1] = centroid[1] / totalPoints
        Point2D.Double(centroid[0], centroid[1])
    }
}