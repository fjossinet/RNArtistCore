package io.github.fjossinet.rnartist.core.model

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.RnartistConfig.defaultConfiguration
import io.github.fjossinet.rnartist.core.RnartistConfig.defaultResidueLetterConfiguration
import java.awt.*
import java.awt.geom.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.PrintWriter
import javax.imageio.ImageIO
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt


//parameters that can be modified
val radiusConst: Double = 7.0
val spaceBetweenResidues: Double = 5.0
val deltaHelixWidth: Double = 5.0
val deltaPhosphoShift: Double = 0.0
val deltaLWSymbols: Double = 1.2

val minimalCircumference: Float = 360F / ((ConnectorId.entries.size) * radiusConst * 3).toFloat()
val minimalRadius: Float = minimalCircumference / (2F * Math.PI).toFloat()
val radiansToDegrees = 180 / Math.PI
val degreesToRadians = Math.PI / 180

enum class SecondaryStructureType {
    A {
        override fun toString(): String {
            return "a"
        }
    },
    U {
        override fun toString(): String {
            return "u"
        }
    },
    G {
        override fun toString(): String {
            return "g"
        }
    },
    C {
        override fun toString(): String {
            return "c"
        }
    },
    X {
        override fun toString(): String {
            return "x"
        }
    },
    AShape {
        override fun toString(): String {
            return "A"
        }
    },
    UShape {
        override fun toString(): String {
            return "U"
        }
    },
    GShape {
        override fun toString(): String {
            return "G"
        }
    },
    CShape {
        override fun toString(): String {
            return "C"
        }
    },
    XShape {
        override fun toString(): String {
            return "X"
        }
    },
    SecondaryInteraction {
        override fun toString(): String {
            return "secondary_interaction"
        }
    },
    TertiaryInteraction {
        override fun toString(): String {
            return "tertiary_interaction"
        }
    },
    InteractionSymbol {
        override fun toString(): String {
            return "interaction_symbol"
        }
    },
    PhosphodiesterBond {
        override fun toString(): String {
            return "phosphodiester_bond"
        }
    },
    Helix {
        override fun toString(): String {
            return "helix"
        }
    },
    PKnot {
        override fun toString(): String {
            return "pknot"
        }
    },
    Junction {
        override fun toString(): String {
            return "junction"
        }
    },
    SingleStrand {
        override fun toString(): String {
            return "single_strand"
        }
    },
    LWSymbol
}

enum class ThemeProperty {
    fulldetails, color, linewidth, lineshift, opacity
}

enum class LayoutProperty {
    radius, out_ids, rollBack
}

fun helixDrawingLength(h: Helix) =
    (h.length - 1).toDouble() * radiusConst * 2.0 + (h.length - 1).toDouble() * spaceBetweenResidues

fun helixDrawingWidth() = radiusConst * deltaHelixWidth

/**
 * Stores everything related to the current working session:
 * - the current translation and zoom of the view (finalZoomLevel, viewX, viewY)
 * - the selection
 * - the elements drawn
 * - the level to decide which tertiaires are displayed (None, Pknots-only, All)
 */
class WorkingSession {
    var viewX = 0.0
    var viewY = 0.0
    var zoomLevel = 1.0

    val branchesDrawn = mutableListOf<JunctionDrawing>()
    val phosphoBondsLinkingBranchesDrawn = mutableListOf<BranchesLinkingPhosphodiesterBondDrawing>()
    val helicesDrawn = mutableListOf<HelixDrawing>()
    val junctionsDrawn = mutableListOf<JunctionDrawing>()
    val singleStrandsDrawn = mutableListOf<SingleStrandDrawing>()
    lateinit var locationDrawn: Location

    var fontName = "Arial"
    var fontStyle = Font.PLAIN
    var fontSize = 12
    var deltafontx = 0
    var deltafonty = 0
    var deltafontsize = 0

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

    fun moveView(transX: Double, transY: Double) {
        viewX += transX
        viewY += transY
    }

    fun zoomView(x: Double, y: Double, on: Boolean) {
        val realPointBeforeZoom =
            Point2D.Double(
                (x - viewX) / zoomLevel,
                (y - viewY) / zoomLevel
            )
        if (on)
            setZoom(1.25)
        else
            setZoom(1.0 / 1.25)
        val realPointAfterZoom =
            Point2D.Double(
                (x - viewX) / zoomLevel,
                (y - viewY) / zoomLevel
            )
        moveView(
            (realPointAfterZoom.getX() - realPointBeforeZoom.getX()) * zoomLevel,
            (realPointAfterZoom.getY() - realPointBeforeZoom.getY()) * zoomLevel
        )
    }

    fun setZoom(zoomFactor: Double) {
        zoomLevel *= zoomFactor
    }


    fun setFont(g: Graphics2D, residue: ResidueDrawing) {
        val at = AffineTransform()
        at.translate(this.viewX, this.viewY)
        at.scale(this.zoomLevel, this.zoomLevel)
        val word2Fit = residue.type.name
        val _c = at.createTransformedShape(residue.circle)
        var dimension: Dimension2D
        var fontSize = (100 * this.zoomLevel).toInt() //initial value
        do {
            fontSize--
            val font = Font(fontName, fontStyle, fontSize)
            dimension = getStringBoundsRectangle2D(g, word2Fit, font)
        } while (dimension.width >= _c.bounds2D.width - _c.bounds2D.width * 0.5 + _c.bounds2D.width * deltafontsize / 20.0 && dimension.height >= _c.bounds2D.height - _c.bounds2D.height * 0.5 + _c.bounds2D.height * deltafontsize / 20.0)

        this.fontSize = fontSize
        g.font = Font(this.fontName, this.fontStyle, this.fontSize)
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
}

/**
 * If the (selector is true (true if null) AND (the drawing element type is at least one of the secondaryStructureTypesAllowed (true if null))
 * then the drawing element is selected to record the theme configuration
 */
class ThemeConfiguration(
    private val selector: ((DrawingElement) -> Boolean)? = null,
    val propertyName: String,
    var propertyValue: (DrawingElement) -> String,
    val secondaryStructureTypesAllowed:List<SecondaryStructureType>? = null
) {
    //store the drawing elements themed with this configuration
    var themedElements:MutableList<DrawingElement>? = null
    var gatherThemedSelements:Boolean = false
        set(value) {
            field = value
            if (field)
                themedElements = mutableListOf()
            else
                themedElements = null
        }
    fun select(el:DrawingElement):Boolean {
        return this.secondaryStructureTypesAllowed?.any {
            el.type == it
        } ?: true && (this.selector?.invoke(el) ?: true)
    }

    fun themedTypesAsString():String {
        val selectedTypes = mutableSetOf<String>()
        themedElements?.let { elements ->
            elements.forEach { element ->
                selectedTypes.add(
                    when (element) {
                        is HelixDrawing -> "helix"
                        is JunctionDrawing -> "junction"
                        is SingleStrandDrawing -> "single_strand"
                        is SecondaryInteractionDrawing -> "secondary_interaction"
                        is PhosphodiesterBondDrawing -> "phosphodiester_bond"
                        is ResidueDrawing -> "N"
                        is ResidueLetterDrawing -> "n"
                        is InteractionSymbolDrawing -> "interaction_symbol"
                        else -> ""
                    }
                )
            }
        }
        return selectedTypes.toList().sorted().joinToString(separator = " ")
    }

}

class Theme {

    var configurations: MutableList<ThemeConfiguration> =
        mutableListOf()

    fun addConfiguration(
        property: ThemeProperty,
        propertyValue: (DrawingElement) -> String
    ):ThemeConfiguration {
        return this.addConfiguration(null, property, propertyValue, null)
    }

    fun addConfiguration(
        property: ThemeProperty,
        propertyValue: (DrawingElement) -> String,
        secondaryStructureTypesAllowed:List<SecondaryStructureType>? = null
    ):ThemeConfiguration {
        return this.addConfiguration(null, property, propertyValue, secondaryStructureTypesAllowed)
    }

    fun addConfiguration(
        selector: ((DrawingElement) -> Boolean),
        property: ThemeProperty,
        propertyValue: (DrawingElement) -> String
    ):ThemeConfiguration {
        return this.addConfiguration(selector, property, propertyValue, null)
    }

    fun addConfiguration(
        selector: ((DrawingElement) -> Boolean)? = null,
        property: ThemeProperty,
        propertyValue: (DrawingElement) -> String,
        secondaryStructureTypesAllowed:List<SecondaryStructureType>? = null
    ):ThemeConfiguration {
        val c = ThemeConfiguration(selector, property.toString(), propertyValue, secondaryStructureTypesAllowed)
        configurations.add(c)
        return c
    }

    fun clear() = this.configurations.clear()
}

class LayoutConfiguration(
    val selector: (DrawingElement) -> Boolean,
    val propertyName: String,
    val propertyValue: String?
)

class Layout {

    var configurations: MutableList<LayoutConfiguration> = mutableListOf()

    fun addConfigurationFor(
        selector: (DrawingElement) -> Boolean,
        property: LayoutProperty,
        propertyValue: String?
    ) {
        configurations.add(LayoutConfiguration(selector, property.toString(), propertyValue))
    }

    fun clear() = this.configurations.clear()
}


class DrawingConfiguration(val defaultParams: Map<String, String>) {

    val params: MutableMap<String, String> = mutableMapOf()

    var opacity: Int = defaultParams[ThemeProperty.opacity.toString()]!!.toInt()
        get() = this.params.getOrDefault(
            ThemeProperty.opacity.toString(),
            defaultParams[ThemeProperty.opacity.toString()]!!
        ).toInt()

    var fullDetails: Boolean = defaultParams[ThemeProperty.fulldetails.toString()]!!.toBoolean()
        get() = this.params.getOrDefault(
            ThemeProperty.fulldetails.toString(),
            defaultParams[ThemeProperty.fulldetails.toString()]!!
        ).toBoolean()

    var lineShift: Double = defaultParams[ThemeProperty.lineshift.toString()]!!.toDouble()
        get() = this.params.getOrDefault(
            ThemeProperty.lineshift.toString(),
            defaultParams[ThemeProperty.lineshift.toString()]!!
        ).toDouble()

    var lineWidth: Double = defaultParams[ThemeProperty.linewidth.toString()]!!.toDouble()
        get() = this.params.getOrDefault(
            ThemeProperty.linewidth.toString(),
            defaultParams[ThemeProperty.linewidth.toString()]!!
        ).toDouble()

    var color: Color = getAWTColor(defaultParams[ThemeProperty.color.toString()]!!)
        get() = getAWTColor(
            this.params.getOrDefault(
                ThemeProperty.color.toString(),
                defaultParams[ThemeProperty.color.toString()]!!
            )
        )

    fun clear() {
        this.params.clear()
    }

}

fun getStringBoundsRectangle2D(g: Graphics2D, title: String, font: Font): Dimension2D {
    g.font = font
    val fm = g.fontMetrics
    val lm = font.getLineMetrics(title, g.fontRenderContext)
    val r = fm.getStringBounds(title, g)
    return Dimension(r.getWidth().toInt(), (lm.ascent - lm.descent).toInt())
}

abstract class DrawingElement(
    val ssDrawing: SecondaryStructureDrawing,
    var parent: DrawingElement?,
    val name: String,
    val location: Location,
    var type: SecondaryStructureType
) {

    abstract val children: List<DrawingElement>

    var drawingConfiguration: DrawingConfiguration = DrawingConfiguration((this as? ResidueLetterDrawing)?.let { defaultResidueLetterConfiguration } ?: run { defaultConfiguration})

    open val bounds: List<Point2D> = mutableListOf()

    open val selectionShape: Shape
        get() {
            return getRoundedGeneralPath(bounds.toMutableList())
        }

    val allChildren: List<DrawingElement>
        get() {
            val allChildren = mutableListOf<DrawingElement>()
            allChildren.addAll(this.children)
            this.children.forEach {
                allChildren.addAll(it.allChildren)
            }
            return allChildren
        }

    /**
     * Tests if the drawing element is inside the [location]. If the parameter useNumberingSystem is true for the [RNA] molecule, the location of the drawing element is computed according to the numbering system (if any) linked to the RNA moelcule.
     *
     * @param [location] the location that should contains the drawing element
     *
     * @return true if the drawing element is inside the [location]
     */
    abstract fun inside(location: Location): Boolean

    var residues: List<ResidueDrawing> =
        this.ssDrawing.getResiduesFromAbsPositions(*this.location.positions.toIntArray())

    abstract fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>? = null
    )

    fun pathToRoot(): List<DrawingElement> {
        val l = mutableListOf<DrawingElement>()
        l.add(this)
        while (l.last().parent != null)
            l.add(l.last().parent!!)
        return l
    }

    fun pathToStructuralDomain(): List<DrawingElement> {
        val l = mutableListOf<DrawingElement>()
        l.add(this)
        if (this is HelixDrawing || this is JunctionDrawing || this is SingleStrandDrawing)
            return l
        while (l.last().parent != null) {
            val p = l.last().parent!!
            l.add(p)
            if (p is HelixDrawing || p is JunctionDrawing || p is SingleStrandDrawing)
                return l
        }
        return l
    }

    /**
     * The element become fully detailed and so all its parents
     */
    open fun show():List<DrawingElement>? {
        var l = mutableListOf<DrawingElement>()
        this.pathToStructuralDomain().forEach {
            if (it != this && !it.drawingConfiguration.fullDetails) {
                l.add(it)
                val t = Theme()
                t.addConfiguration(ThemeProperty.fulldetails, {_ -> "true"})
                it.applyTheme(t,{true}, {true})
            }
        }
        return if (l.isEmpty()) null else l.toList()
    }

    fun getColor(selectedDrawings: List<DrawingElement>? = null): Color {
        return selectedDrawings?.let { selection ->
            if (selectedDrawings.isEmpty() || !this.pathToStructuralDomain().none { selection.contains(it) }) {
                val _c = this.drawingConfiguration.color
                Color(_c.red, _c.green, _c.blue, this.getOpacity())
            } else
                Color(229, 229, 229)
        } ?: run {
            val _c = this.drawingConfiguration.color
            Color(_c.red, _c.green, _c.blue, this.getOpacity())
        }
    }

    open fun isFullDetails() = this.drawingConfiguration.fullDetails

    fun getOpacity() = this.drawingConfiguration.opacity

    fun getLineWidth() = this.drawingConfiguration.lineWidth

    fun getLineShift() = this.drawingConfiguration.lineShift

    fun getSinglePositions() = this.location.positions.toIntArray()

    fun showUntilNextLevel():List<DrawingElement> {
        val themesElements = mutableListOf<DrawingElement>()
        if (!this.isFullDetails()) {
            val t = Theme()
            t.addConfiguration(ThemeProperty.fulldetails, {el -> "true"})
            this.applyTheme(t, {true}, null)
            //this.drawingConfiguration.params[ThemeProperty.fulldetails.toString()] = "true"
            themesElements.add(this)
        }
        else if (this.children.any { !it.isFullDetails() }) {
            this.children.forEach {
                if (!it.isFullDetails()) {
                    val t = Theme()
                    t.addConfiguration(ThemeProperty.fulldetails, {el -> "true"})
                    it.applyTheme(t, {true}, null)
                    //it.drawingConfiguration.params[ThemeProperty.fulldetails.toString()] = "true"
                    themesElements.add(it)
                }
            }
        }
        else
            this.children.forEach {
                themesElements.addAll(it.showUntilNextLevel())
            }
        return themesElements
    }

    fun hideDetailsUntilNextLevel():List<DrawingElement> {
        val themesElements = mutableListOf<DrawingElement>()
        if (this.children.any { it.isFullDetails() }) {
            this.children.forEach {
                if (it.isFullDetails())
                    themesElements.addAll(it.hideDetailsUntilNextLevel())
            }
        } else if (this.isFullDetails()) {
            themesElements.add(this)
            val t = Theme()
            t.addConfiguration(ThemeProperty.fulldetails, {el -> "false"})
            this.applyTheme(t, {true}, null)
            //this.drawingConfiguration.params[ThemeProperty.fulldetails.toString()] = "false"
        }
        return themesElements
    }

    open fun applyTheme(theme: Theme, checkStopBefore:((DrawingElement) -> Boolean)? = null, checkStopAfter:((DrawingElement) -> Boolean)? = null) {
        val stop1 = checkStopBefore?.let {
          it(this)
        } ?: run {
            false
        }
        theme.configurations.forEach { configuration ->
            if (configuration.select(this)) {
                this.drawingConfiguration.params[configuration.propertyName] = configuration.propertyValue(this)
                if (configuration.gatherThemedSelements)
                    configuration.themedElements?.add(this)
            }
        }
        val stop2 = checkStopAfter?.let {
            it(this)
        } ?: run {
            false
        }
        if (!stop1 && !stop2)
            this.children.map { it.applyTheme(theme, checkStopBefore, checkStopAfter) }
    }

    open fun clearTheme() {
        this.drawingConfiguration.clear()
        this.children.map { it.clearTheme() }
    }

    open fun select(selector:(DrawingElement) -> Boolean, selection:MutableList<DrawingElement> = mutableListOf()): List<DrawingElement>{
        if (selector(this))
            selection.add(this)
        this.children.forEach { it.select(selector,selection) }
        return selection
    }
}

class SecondaryStructureDrawing(
    val secondaryStructure: SecondaryStructure,
    val workingSession: WorkingSession = WorkingSession(), anchorX: Double = 0.0, anchorY: Double = 0.0
) {

    val phosphoBonds =
        mutableListOf<BranchesLinkingPhosphodiesterBondDrawing>() //bonds linking branch-branch and single-strand-branch
    var name: String
        get() = this.secondaryStructure.name
        set(name) {
            this.secondaryStructure.name = name
        }

    val residuesUpdated =
        mutableListOf<Int>() //a list used by the tertiary interactions to check if they need to recompute their shapes

    val branches = mutableListOf<Branch>() //the first junctions in each branch
    val pknots = mutableListOf<PKnotDrawing>()
    val allSingleStrands = mutableListOf<SingleStrandDrawing>() // the single-strands connecting the branches
    val residues = mutableListOf<ResidueDrawing>()
    val tertiaryInteractions = mutableListOf<TertiaryInteractionDrawing>()

    private var fitToResiduesBetweenBranches = true
    var quickDraw = false

    val allChildren: List<DrawingElement>
        get() {
            val allChildren = mutableListOf<DrawingElement>()
            this.allJunctions.forEach {
                allChildren.addAll(it.allChildren)
            }
            this.allHelices.forEach {
                allChildren.addAll(it.allChildren)
            }
            this.allSingleStrands.forEach {
                allChildren.addAll(it.allChildren)
            }
            this.allTertiaryInteractions.forEach {
                allChildren.addAll(it.allChildren)
            }
            this.phosphoBonds.forEach {
                allChildren.addAll(it.allChildren)
            }
            return allChildren
        }

    val allSecondaryInteractions: List<BaseBaseInteractionDrawing>
        get() {
            val interactions = mutableListOf<BaseBaseInteractionDrawing>()
            for (h in this.allHelices)
                interactions.addAll(h.secondaryInteractions)
            return interactions
        }

    val allDefaultSymbols: List<LWSymbolDrawing>
        get() {
            val symbols = mutableListOf<LWSymbolDrawing>()
            for (interaction in this.allSecondaryInteractions)
                interaction.interactionSymbol.defaultSymbol?.let {
                    symbols.add(it)
                }

            for (interaction in this.allTertiaryInteractions)
                interaction.interactionSymbol.defaultSymbol?.let {
                    symbols.add(it)
                }

            return symbols
        }

    val allLWSymbols: List<LWSymbolDrawing>
        get() {
            val symbols = mutableListOf<LWSymbolDrawing>()
            for (interaction in this.allSecondaryInteractions)
                symbols.addAll(interaction.interactionSymbol.lwSymbols)
            for (interaction in this.allTertiaryInteractions)
                symbols.addAll(interaction.interactionSymbol.lwSymbols)
            return symbols
        }

    val allJunctions: List<JunctionDrawing>
        get() {
            val allJunctions = mutableListOf<JunctionDrawing>()
            for (branch in this.branches)
                allJunctions.addAll(branch.junctionsFromBranch())
            return allJunctions
        }

    val allTertiaryInteractions: List<TertiaryInteractionDrawing>
        get() {
            if (this.pknots.isEmpty())
                return this.tertiaryInteractions
            val allTertiaryInteractions = mutableListOf<TertiaryInteractionDrawing>()
            allTertiaryInteractions.addAll(this.tertiaryInteractions)
            for (pknot in this.pknots)
                allTertiaryInteractions.addAll(pknot.tertiaryInteractions)
            return allTertiaryInteractions
        }

    val allPhosphoBonds: List<PhosphodiesterBondDrawing>
        get() {
            val allPhosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()
            for (h in this.allHelices)
                allPhosphoBonds.addAll(h.phosphoBonds)
            for (j in this.allJunctions)
                allPhosphoBonds.addAll(j.phosphoBonds)
            for (ss in this.allSingleStrands)
                allPhosphoBonds.addAll(ss.phosphoBonds)
            allPhosphoBonds.addAll(this.phosphoBonds)
            return allPhosphoBonds
        }

    val allHelices: List<HelixDrawing>
        get() {
            val allHelices = mutableListOf<HelixDrawing>()
            for (branch in this.branches)
                allHelices.addAll(branch.helicesFromBranch())
            for (pknot in this.pknots)
                allHelices.add(pknot.helix)
            return allHelices.sortedBy { it.start }
        }

    val viewX: Double
        get() = this.workingSession.viewX

    val viewY: Double
        get() = this.workingSession.viewY

    val zoomLevel: Double
        get() = this.workingSession.zoomLevel

    val length: Int
        get() = this.secondaryStructure.length

    init {
        this.secondaryStructure.rna.seq.forEachIndexed { index, char ->
            this.residues.add(
                when (char) {
                    'A', 'a' -> AShapeDrawing(null, this, index + 1)
                    'U', 'u' -> UShapeDrawing(null, this, index + 1)
                    'G', 'g' -> GShapeDrawing(null, this, index + 1)
                    'C', 'c' -> CShapeDrawing(null, this, index + 1)
                    else -> XShapeDrawing(null, this, index + 1)
                }
            )
        }

        //++++++ We compute the squeleton for the 2D (lines for helices and ellipses for junctions)

        //we start the drawing with the helices with no junction on one side
        var currentPos = 0
        lateinit var lastBranchConstructed: Branch
        var bottom = Point2D.Double(0.0, 0.0)
        lateinit var top: Point2D

        do {
            val nextHelix = this.secondaryStructure.getNextHelixEnd(currentPos)

            if (nextHelix == null) { // no next helix, do we have any remaining residues?
                currentPos += 1
                val remaining = this.secondaryStructure.length - currentPos + 1
                if (remaining > 0) {
                    val ss = this.secondaryStructure.singleStrands.find { it.start == currentPos }!!
                    this.allSingleStrands.add(
                        SingleStrandDrawing(
                            this,
                            ss,
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
                top = Point2D.Double(
                    0.0, 0.0 - helixDrawingLength(
                        nextHelix.third
                    )
                )

                val circles = this.allJunctions.toMutableList()
                val lines = this.allHelices.toMutableList()

                val h = HelixDrawing(
                    null,
                    this,
                    nextHelix.third,
                    bottom,
                    top
                )

                lastBranchConstructed = Branch(
                    h,
                    this,
                    circles,
                    lines,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )

                if (residuesBeforeHelix > 0) {
                    val ss = this.secondaryStructure.singleStrands.find { it.start == currentPos + 1 }!!
                    this.allSingleStrands.add(
                        SingleStrandDrawing(
                            this,
                            ss,
                            if (this.fitToResiduesBetweenBranches) Point2D.Double(
                                bottom.x - radiusConst * 2 * (residuesBeforeHelix + 1),
                                bottom.y
                            ) else Point2D.Double(bottom.x - 200, bottom.y),
                            bottom
                        )
                    )
                    this.residues[0].center = if (this.fitToResiduesBetweenBranches) Point2D.Double(
                        bottom.x - radiusConst * 2 * (residuesBeforeHelix + 1),
                        bottom.y
                    ) else Point2D.Double(bottom.x - 200, bottom.y)
                }

                this.branches.add(lastBranchConstructed)

            } else {

                //for the moment the new branch is located at the same location than the previous one
                //the computing of the branch is done twice
                //a first one to compute the transX to avoid overlaps with the previous branch
                //a second one to compute the placements of each graphical object of the new branch to avoid overlaps with any previous objects
                bottom = Point2D.Double(bottom.x, bottom.y)
                top = Point2D.Double(
                    bottom.x, bottom.y - helixDrawingLength(
                        nextHelix.third
                    )
                )

                var h = HelixDrawing(
                    null,
                    this,
                    nextHelix.third,
                    bottom,
                    top
                )

                //first we want to find the maxX of the previous branches to avoid an overlap with the new branch
                val lastJunctions = mutableListOf<JunctionDrawing>()
                this.branches.forEach {
                    lastJunctions.addAll(it.junctionsFromBranch())
                }

                var circles = mutableListOf<JunctionDrawing>()
                var lines = mutableListOf<HelixDrawing>()
                val newBranchConstructed = Branch(
                    h,
                    this,
                    circles,
                    lines,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )

                //first we check the circles from the last branch constructed at the same level than the new branch constructed

                val allTransX = arrayListOf<Double>()
                allTransX.add(branches.last().maxX - bottom.x + 6.0 * radiusConst) //the minimal transX is half of the width of the previous branch

                for (newJunction in newBranchConstructed.junctionsFromBranch()) {
                    val circlesAtTheSameLevel = mutableListOf<JunctionDrawing>()
                    for (lastC in lastJunctions) {
                        if (lastC.circle.bounds.maxY >= newJunction.circle.bounds.minY && lastC.circle.bounds.minY <= newJunction.circle.bounds.maxY) {
                            circlesAtTheSameLevel.add(lastC)
                        }
                    }
                    for (cATTheSameLevel in circlesAtTheSameLevel)
                        if (cATTheSameLevel.circle.bounds.maxX > newJunction.circle.bounds.minX)
                            allTransX.add(cATTheSameLevel.circle.bounds.maxX - newJunction.circle.bounds.minX + 6.0 * radiusConst)
                }

                var transX = allTransX.maxOrNull()!!

                if (this.fitToResiduesBetweenBranches) {
                    val minimalTransX = (nextHelix.first - currentPos + 2) * radiusConst * 2

                    if (transX < minimalTransX) {
                        transX += (minimalTransX - transX)
                    }
                }

                if (currentPos + 1 <= nextHelix.first - 1) {
                    val ss = this.secondaryStructure.singleStrands.find { it.start == currentPos + 1 }!!
                    this.allSingleStrands.add(
                        SingleStrandDrawing(
                            this,
                            ss,
                            bottom,
                            Point2D.Double(bottom.x + transX, bottom.y)
                        )
                    )
                }

                bottom = Point2D.Double(bottom.x + transX, bottom.y)
                top = Point2D.Double(
                    bottom.x, bottom.y - helixDrawingLength(
                        nextHelix.third
                    )
                )

                h = HelixDrawing(
                    null,
                    this,
                    nextHelix.third,
                    bottom,
                    top
                )

                circles = this.allJunctions.toMutableList()
                lines = this.allHelices.toMutableList()

                lastBranchConstructed = Branch(
                    h,
                    this,
                    circles,
                    lines,
                    ConnectorId.s,
                    top,
                    nextHelix.third,
                    junction
                )

                this.branches.add(lastBranchConstructed)
            }

            currentPos = nextHelix.second

        } while (currentPos < this.secondaryStructure.rna.seq.length)
        //++++++ END squeleton computing

        //+++++ we create the phospho bonds and tertiary interactions
        for (i in 1 until this.secondaryStructure.length) {
            var phosphoBond: PhosphodiesterBondDrawing? = null

            //inside an helix
            for (h in this.allHelices) {
                if (h.location.contains(i) && h.location.contains(i + 1)) {
                    phosphoBond = HelicalPhosphodiesterBondDrawing(h, this, Location(Location(i), Location(i + 1)))
                    break
                }
            }
            //inside a junction
            if (phosphoBond == null) {
                for (j in this.allJunctions) {
                    if (j.junction.location.contains(i) && j.junction.location.contains(i + 1)) {
                        for (b in j.junction.location.blocks)
                            if (i == b.start && i + 1 == b.end) { //direct link between two helices
                                phosphoBond = HelicesDirectLinkPhosphodiesterBondDrawing(
                                    j,
                                    this,
                                    Location(Location(i), Location(i + 1)),
                                    if (i in j.inHelix.ends) i else (if (i + 1 in j.inHelix.ends) i + 1 else -1)
                                )
                                break
                            }
                        if (phosphoBond == null) {
                            if (i in j.inHelix.ends)
                                phosphoBond = InHelixClosingPhosphodiesterBondDrawing(
                                    j,
                                    this,
                                    Location(Location(i), Location(i + 1)),
                                    i
                                )
                            else if (i + 1 in j.inHelix.ends)
                                phosphoBond = InHelixClosingPhosphodiesterBondDrawing(
                                    j,
                                    this,
                                    Location(Location(i), Location(i + 1)),
                                    i + 1
                                )
                            else for (h in j.outHelices) {
                                if (i in h.ends) {
                                    phosphoBond = OutHelixClosingPhosphodiesterBondDrawing(
                                        j,
                                        this,
                                        Location(Location(i), Location(i + 1)), i
                                    )
                                    break
                                } else if (i + 1 in h.ends) {
                                    phosphoBond = OutHelixClosingPhosphodiesterBondDrawing(
                                        j,
                                        this,
                                        Location(Location(i), Location(i + 1)), i + 1
                                    )
                                    break
                                }
                            }
                        }
                        if (phosphoBond == null) {
                            phosphoBond = PhosphodiesterBondDrawing(j, this, Location(Location(i), Location(i + 1)))
                            j.phosphoBonds.add(phosphoBond)
                        }
                        break
                    }
                }
            }
            //inside a single-strand
            if (phosphoBond == null) {
                for (ss in this.allSingleStrands) {
                    if (ss.location.contains(i) && ss.location.contains(i + 1)) {
                        phosphoBond = PhosphodiesterBondDrawing(ss, this, Location(Location(i), Location(i + 1)))
                        ss.phosphoBonds.add(phosphoBond)
                        break
                    }
                }
            }
            //linking a single-strand and an helix starting a branch
            if (phosphoBond == null) {
                SINGLESTRANDS@ for (ss in this.allSingleStrands) {
                    if (ss.location.contains(i)) {
                        for (j in this.branches)
                            if (j.inHelix.location.contains(i + 1)) {
                                phosphoBond =
                                    SingleStrandLinkingBranchPhosphodiesterBondDrawing(
                                        ss,
                                        this,
                                        Location(Location(i), Location(i + 1)),
                                        i + 1
                                    )
                                break@SINGLESTRANDS
                            }
                    } else if (ss.location.contains(i + 1)) {
                        for (j in this.branches)
                            if (j.inHelix.location.contains(i)) {
                                phosphoBond =
                                    SingleStrandLinkingBranchPhosphodiesterBondDrawing(
                                        ss,
                                        this,
                                        Location(Location(i), Location(i + 1)),
                                        i
                                    )
                                break@SINGLESTRANDS
                            }
                    }
                }
            }

            //linking two helices starting a branch
            if (phosphoBond == null) {
                BRANCHES@ for (j in this.branches)
                    if (j.inHelix.location.contains(i)) {
                        for (k in this.branches)
                            if (k.inHelix.location.contains(i + 1)) {
                                BranchesLinkingPhosphodiesterBondDrawing(
                                    this,
                                    Location(Location(i), Location(i + 1)),
                                    j,
                                    k
                                )
                                break@BRANCHES
                            }

                    }
            }

        }

        for (interaction in this.secondaryStructure.tertiaryInteractions) {
            this.tertiaryInteractions.add(
                TertiaryInteractionDrawing(
                    null,
                    interaction,
                    this
                )
            )
        }

        //+++++ now we compute the pknots
        for (pknot in this.secondaryStructure.pknots) {
            val pknotDrawing = PKnotDrawing(this, pknot)
            this.pknots.add(pknotDrawing)
        }

        for (branch in this.branches)
            this.computeResidues(branch)

        if (this.allSingleStrands.size == 1 && this.allHelices.isEmpty()) { // an RNA made with a single single-strand.
            val singleStrand = this.allSingleStrands.first()
            this.residues[0].center =
                Point2D.Double(bottom.x - radiusConst * 2 * (singleStrand.length / 2.0 + 1), bottom.y)
            for (i in singleStrand.start + 1..singleStrand.end)
                this.residues[i - 1].center =
                    Point2D.Double(this.residues[i - 2].center.x + radiusConst * 2.0, this.residues[i - 2].center.y)
        } else {
            for (singleStrand in this.allSingleStrands) {
                for ((i, branch) in this.branches.withIndex())
                    if (branch.inHelix.location.end == singleStrand.location.start - 1) {
                        singleStrand.previousBranch = branch
                        if (i + 1 < this.branches.size)
                            singleStrand.nextBranch = this.branches[i + 1]
                        break
                    } else if (branch.inHelix.location.start == singleStrand.location.end + 1) {
                        singleStrand.nextBranch = branch
                        if (i - 1 >= 0)
                            singleStrand.previousBranch = this.branches[i - 1]
                        break
                    }

                if (singleStrand.start == 1) {
                    if (singleStrand.length != 1) {
                        val step = distance(
                            this.residues[0].center,
                            this.residues[singleStrand.end].center
                        ) / (singleStrand.ss.length).toDouble()
                        for (i in singleStrand.start + 1..singleStrand.end) {
                            val (p1_1, _) = pointsFrom(
                                this.residues[0].center,
                                this.residues[singleStrand.end].center,
                                step * (i - singleStrand.start).toDouble()
                            )
                            this.residues[i - 1].center = p1_1
                        }
                    }
                } else if (singleStrand.end == this.secondaryStructure.length) {
                    if (singleStrand.length != 1) {
                        val step = distance(
                            this.residues[singleStrand.start - 2].center,
                            this.residues[this.secondaryStructure.length - 1].center
                        ) / (singleStrand.length).toDouble()
                        for (i in singleStrand.start until singleStrand.end) {
                            val (p1_1, _) = pointsFrom(
                                this.residues[singleStrand.start - 2].center,
                                this.residues[this.secondaryStructure.length - 1].center,
                                step * (i - (singleStrand.start - 1).toDouble())
                            )
                            this.residues[i - 1].center = p1_1
                        }
                    }
                } else {
                    val step = distance(
                        this.residues[singleStrand.start - 2].center,
                        this.residues[singleStrand.end].center
                    ) / (singleStrand.length + 1).toDouble()
                    for (i in singleStrand.start..singleStrand.end) {
                        val (p1_1, _) = pointsFrom(
                            this.residues[singleStrand.start - 2].center,
                            this.residues[singleStrand.end].center,
                            step * (i - (singleStrand.start - 1).toDouble())
                        )
                        this.residues[i - 1].center = p1_1
                    }
                }
            }
        }

        //++++ we set the parent element for residues
        for (r in this.residues) {
            OUTER@ for (h in this.allHelices) {
                for (interaction in h.secondaryInteractions)
                    if (interaction.location.contains(r.absPos)) {
                        r.parent = interaction
                        break@OUTER
                    }
            }
            if (r.parent == null) {
                for (j in this.allJunctions) {
                    if (j.location.contains(r.absPos)) {
                        r.parent = j
                        break
                    }
                }
            }
            if (r.parent == null) {
                for (ss in this.allSingleStrands) {
                    if (ss.location.contains(r.absPos)) {
                        r.parent = ss
                        break
                    }
                }
            }
        }
        //we init the working session. This will be recomputed during the first draw. But we need to do it, if the draw() methods ae not called (for exemple for a drawing exported to an SVG on the server side)
        this.workingSession.junctionsDrawn.addAll(this.allJunctions)
        this.workingSession.helicesDrawn.addAll(this.allHelices)
        this.workingSession.singleStrandsDrawn.addAll(this.allSingleStrands)
        this.workingSession.phosphoBondsLinkingBranchesDrawn.addAll(this.phosphoBonds)
        this.workingSession.locationDrawn = Location(1, this.secondaryStructure.length)
        this.workingSession.viewX = anchorX
        this.workingSession.viewY = anchorY
    }

    fun fitViewTo(frame: Rectangle2D) {
        val drawingFrame = this.getFrame()
        drawingFrame?.let { drawingFrame ->
            val widthRatio = (drawingFrame.bounds2D!!.width + 25) / frame.bounds2D.width
            val heightRatio = (drawingFrame.bounds2D!!.height + 25) / frame.bounds2D.height
            this.workingSession.zoomLevel =
                if (widthRatio > heightRatio) 1.0 / widthRatio else 1.0 / heightRatio
            val at = AffineTransform()
            at.scale(this.zoomLevel, this.zoomLevel)
            val transformedBounds = at.createTransformedShape(drawingFrame)
            this.workingSession.viewX = frame.bounds2D.centerX - transformedBounds.bounds2D.centerX
            this.workingSession.viewY = frame.bounds2D.centerY - transformedBounds.bounds2D.centerY

            //We compute the new font parameters
            val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            this.workingSession.setFont(g, this.residues.first())
        }
    }

    fun fitViewTo(frame: Rectangle2D, selectionFrame: Rectangle2D, ratio: Double = 1.0) {
        val widthRatio = selectionFrame.bounds2D!!.width * ratio / frame.bounds2D.width
        val heightRatio = selectionFrame.bounds2D!!.height * ratio / frame.bounds2D.height
        this.workingSession.zoomLevel =
            if (widthRatio > heightRatio) 1.0 / widthRatio else 1.0 / heightRatio
        val at = AffineTransform()
        at.scale(this.zoomLevel, this.zoomLevel)
        val transformedBounds = at.createTransformedShape(selectionFrame)
        this.workingSession.viewX = frame.bounds2D.centerX - transformedBounds.bounds2D.centerX
        this.workingSession.viewY = frame.bounds2D.centerY - transformedBounds.bounds2D.centerY

        //We compute the new font parameters
        val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        this.workingSession.setFont(g, this.residues.first())
    }

    fun getResiduesFromAbsPositions(vararg positions: Int): List<ResidueDrawing> {
        val _residues: MutableList<ResidueDrawing> = mutableListOf()
        for (r: ResidueDrawing in residues) {
            if (r.absPos in positions) {
                _residues.add(r)
            }
        }
        return _residues
    }

    fun getFrame(): Rectangle2D? {
        val firstShape = allHelices.firstOrNull()?.selectionShape ?: run {
            allSingleStrands.firstOrNull()?.selectionShape
        }
        firstShape?.let { firstShape ->
            var frame = firstShape.bounds2D
            this.allSingleStrands.forEach {
                frame = frame.createUnion(it.selectionShape.bounds2D)
            }
            this.allHelices.forEach {
                frame = frame.createUnion(it.selectionShape.bounds2D)
            }
            this.allJunctions.forEach {
                frame = frame.createUnion(it.selectionShape.bounds2D)
            }
            return frame
        }
        return null
    }

    fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>? = null
    ) {
        if (!quickDraw)
            workingSession.setFont(g, this.residues.first())

        with(this.workingSession) {
            branchesDrawn.clear()
            singleStrandsDrawn.clear()
            helicesDrawn.clear()
            junctionsDrawn.clear()
            phosphoBondsLinkingBranchesDrawn.clear()
            locationDrawn = Location()
        }

        if (this.allSingleStrands.isEmpty() && this.phosphoBonds.isEmpty() || this.branches.size == 1) { //if a single branch, always drawn, and single strands too
            this.workingSession.singleStrandsDrawn.addAll(this.allSingleStrands)
            this.workingSession.branchesDrawn.addAll(this.branches)
        } else {

            for (ss in this.allSingleStrands) {
                val s = Point2D.Double(0.0, 0.0)
                val e = Point2D.Double(0.0, 0.0)
                at.transform(ss.line.p1, s)
                at.transform(ss.line.p2, e)
                if (e.x < 0.0)
                    continue
                if (s.x > drawingArea.width)
                    break
                if (s.x >= 0.0 && s.x <= drawingArea.width || e.x >= 0.0 && e.x <= drawingArea.width || s.x < 0.0 && e.x > drawingArea.width) {
                    ss.previousBranch?.let {
                        if (!this.workingSession.branchesDrawn.contains(it))
                            this.workingSession.branchesDrawn.add(it)
                    }
                    ss.nextBranch?.let {
                        if (!this.workingSession.branchesDrawn.contains(it))
                            this.workingSession.branchesDrawn.add(it)
                    }
                    //the y coordinate allows to decide if the single-strand will be drawn
                    if (s.y >= 0.0 && s.y <= drawingArea.height) { //not necessary to check the end point
                        this.workingSession.singleStrandsDrawn.add(ss)
                    }
                }
            }

            if (this.workingSession.branchesDrawn.isEmpty()) { //if the display is before or after the first/last residue, we draw at least the first or last branch//sinlge-strand. If the user is there, this could means that a branch is drawn before or after the first or last residues in the sequence

                var p = Point2D.Double(0.0, 0.0)
                when (this.residues.first().parent) {
                    is SecondaryInteractionDrawing -> {
                        val h = (this.residues.first().parent as SecondaryInteractionDrawing).parent as HelixDrawing
                        at.transform(h.line.p1, p)
                        if (p.x >= drawingArea.width) {
                            this.workingSession.branchesDrawn.add(branches.first())
                        }
                    }

                    is SingleStrandDrawing -> {
                        val ss = this.residues.first().parent as SingleStrandDrawing
                        at.transform(ss.line.p1, p)
                        if (p.x >= drawingArea.width) {
                            this.workingSession.singleStrandsDrawn.add(ss)
                            ss.nextBranch?.let {
                                this.workingSession.branchesDrawn.add(it)
                            }

                        }
                    }
                }

                p = Point2D.Double(0.0, 0.0)

                when (this.residues.last().parent) {
                    is SecondaryInteractionDrawing -> {
                        val h = (this.residues.last().parent as SecondaryInteractionDrawing).parent as HelixDrawing
                        at.transform(h.line.p2, p)
                        if (p.x <= 0.0) {
                            this.workingSession.branchesDrawn.add(branches.last())
                        }
                    }

                    is SingleStrandDrawing -> {
                        val ss = this.residues.last().parent as SingleStrandDrawing
                        at.transform(ss.line.p2, p)
                        if (p.x <= 0.0) {
                            this.workingSession.singleStrandsDrawn.add(ss)
                            ss.previousBranch?.let {
                                this.workingSession.branchesDrawn.add(it)
                            }

                        }
                    }
                }
            }

            for (phospho in this.phosphoBonds) {
                val s = Point2D.Double(0.0, 0.0)
                val e = Point2D.Double(0.0, 0.0)
                at.transform((phospho.previousBranch.parent as HelixDrawing).line.p1, s)
                at.transform((phospho.nextBranch.parent as HelixDrawing).line.p1, e)
                if (e.x < 0.0)
                    continue
                if (s.x > drawingArea.width)
                    break
                if (s.x >= 0.0 && s.x <= drawingArea.width || e.x >= 0.0 && e.x <= drawingArea.width || s.x < 0.0 && e.x > drawingArea.width) {
                    phospho.previousBranch.let {
                        if (!this.workingSession.branchesDrawn.contains(it))
                            this.workingSession.branchesDrawn.add(it)
                    }
                    phospho.nextBranch.let {
                        if (!this.workingSession.branchesDrawn.contains(it))
                            this.workingSession.branchesDrawn.add(it)
                    }
                    //the y coordinate allows to decide if the single-strand will be drawn
                    if (s.y >= 0.0 && s.y <= drawingArea.height) //not necessary to check the end point
                        this.workingSession.phosphoBondsLinkingBranchesDrawn.add(phospho)
                }
            }
        }

        with(this.workingSession) {

            singleStrandsDrawn.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }

            phosphoBondsLinkingBranchesDrawn.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }

            branchesDrawn.forEach {
                JUNCTIONSFROMBRANCHES@ for (j in it.junctionsFromBranch()) {
                    val p = Point2D.Double(0.0, 0.0)
                    at.transform((j.parent!! as HelixDrawing).line.p2, p)
                    if (p.x >= 0.0 && p.x <= drawingArea.width && p.y >= 0.0 && p.y <= drawingArea.height) {
                        j.draw(g, at, drawingArea, selectedDrawings)
                        junctionsDrawn.add(j)
                        continue@JUNCTIONSFROMBRANCHES
                    }
                    val helices = mutableListOf<HelixDrawing>()
                    helices.addAll(j.outHelices)
                    for (h in helices) {
                        at.transform(h.line.p1, p)
                        if (p.x >= 0.0 && p.x <= drawingArea.width && p.y >= 0.0 && p.y <= drawingArea.height) {
                            j.draw(g, at, drawingArea, selectedDrawings)
                            junctionsDrawn.add(j)
                            continue@JUNCTIONSFROMBRANCHES
                        }
                    }
                }
            }

            branchesDrawn.forEach {
                for (h in it.helicesFromBranch()) {
                    val s = Point2D.Double(0.0, 0.0)
                    val e = Point2D.Double(0.0, 0.0)
                    at.transform(h.line.p1, s)
                    at.transform(h.line.p2, e)
                    if (s.x >= 0.0 && s.x <= drawingArea.width || e.x >= 0.0 && e.x <= drawingArea.width || s.x < 0.0 && e.x > drawingArea.width) {
                        helicesDrawn.add(h)
                        //this.workingSession.locationDrawn = this.workingSession.locationDrawn.addLocation(h.location)
                        h.draw(g, at, drawingArea, selectedDrawings)
                    }
                }
            }
        }

        if (!quickDraw) {

            this.workingSession.junctionsDrawn.forEach {
                this.workingSession.locationDrawn.blocks.addAll(it.location.blocks)
            }

            this.workingSession.singleStrandsDrawn.forEach {
                this.workingSession.locationDrawn.blocks.addAll(it.location.blocks)
            }

            this.workingSession.helicesDrawn.forEach {
                this.workingSession.locationDrawn.blocks.addAll(it.location.blocks)
            }

            this.pknots.forEach {
                it.tertiaryInteractions.forEach {
                    it.draw(g, at, drawingArea, selectedDrawings)
                }
            }

            this.tertiaryInteractions.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }

            this.residuesUpdated.clear()
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
                    this.residues[b.start - 1].center,
                    this.residues[b.end - 1].center
                )
                val cp = crossProduct(
                    sharedPoint = j.center,
                    p2 = this.residues[b.start - 1].center,
                    p3 = this.residues[b.end - 1].center
                )
                if (cp < 0) {
                    angle -= 360
                    //we need to check if we are in the case where an helix crossed another one and then place the residues in between wrongly. The trick is to measure if the distance on the first residue in between is larger than the distance between the ends of the block
                    if (j.junctionType != JunctionType.ApicalLoop && b.length >= 3) {//at least one residue
                        val distance = distance(
                            this.residues[b.start - 1].center, rotatePoint(
                                this.residues[b.start - 1].center,
                                j.center,
                                -angle / (b.end - b.start).toDouble()
                            )
                        )
                        if (distance > distance(this.residues[b.start - 1].center, this.residues[b.end - 1].center))
                            angle += 360
                    }
                } else {
                    angle = -angle
                }
                val step = -angle / (b.end - b.start).toDouble()
                for (i in b.start + 1 until b.end) {
                    this.residues[i - 1].center = rotatePoint(
                        this.residues[b.start - 1].center,
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
    private fun computeResidues(helix: HelixDrawing) {
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
                this.residues[helix.helix.ends[0] - 1].center,
                this.residues[helix.helix.ends[1] - 1].center,
                step * i
            )
            this.residues[helix.helix.ends[0] + i - 1].center = p1_1
            this.residues[helix.helix.ends[0] + i - 1].updated = true

            val (_, p1_2) = pointsFrom(
                this.residues[helix.helix.ends[2] - 1].center,
                this.residues[helix.helix.ends[3] - 1].center,
                step * i
            )

            this.residues[helix.helix.ends[3] - i - 1].center = p1_2
            this.residues[helix.helix.ends[3] - i - 1].updated = true

        }
    }

    override fun toString() = this.secondaryStructure.toString()

    fun applyTheme(theme: Theme, checkStopBefore:((DrawingElement) -> Boolean)? = null, checkStopAfter:((DrawingElement) -> Boolean)? = null) {
        for (phospho in this.phosphoBonds)
            phospho.applyTheme(theme, checkStopBefore, checkStopAfter)
        for (pk in this.pknots)
            pk.applyTheme(theme, checkStopBefore, checkStopAfter)
        for (jc in this.allJunctions)
            jc.applyTheme(theme, checkStopBefore, checkStopAfter)
        for (ss in this.allSingleStrands)
            ss.applyTheme(theme, checkStopBefore, checkStopAfter)
        for (h in this.allHelices)
            h.applyTheme(theme, checkStopBefore, checkStopAfter)
        for (i in this.tertiaryInteractions)
            i.applyTheme(theme, checkStopBefore, checkStopAfter)
    }

    fun clearTheme() {
        for (phospho in this.phosphoBonds)
            phospho.clearTheme()
        for (pk in this.pknots)
            pk.clearTheme()
        for (jc in this.allJunctions)
            jc.clearTheme()
        for (ss in this.allSingleStrands)
            ss.clearTheme()
        for (h in this.allHelices)
            h.clearTheme()
        for (i in this.tertiaryInteractions)
            i.clearTheme()
    }

    fun applyLayout(layout: Layout) {
        for (jc in this.allJunctions)
            jc.applyLayout(layout)
    }

    fun clearLayout() {
        for (jc in this.allJunctions)
            jc.clearLayout()
    }

    fun select(selector:(DrawingElement) -> Boolean, selection:MutableList<DrawingElement> = mutableListOf()): List<DrawingElement>{
        for (phospho in this.phosphoBonds)
            phospho.select(selector, selection)
        for (pk in this.pknots)
            pk.select(selector, selection)
        for (jc in this.allJunctions)
            jc.select(selector, selection)
        for (ss in this.allSingleStrands)
            ss.select(selector, selection)
        for (h in this.allHelices)
            h.select(selector, selection)
        for (i in this.tertiaryInteractions)
            i.select(selector, selection)
        return selection
    }

    fun asPNG(frame: Rectangle2D, selectionFrame: Rectangle2D? = null, outputFile: File) {
        val previousViewX = this.workingSession.viewX
        val previousViewY = this.workingSession.viewY
        val previousZoomLevel = this.workingSession.zoomLevel

        selectionFrame?.let {
            this.fitViewTo(frame, selectionFrame)
        } ?: run {
            this.fitViewTo(frame)
        }
        val at = AffineTransform()
        at.translate(this.workingSession.viewX, this.workingSession.viewY)
        at.scale(this.workingSession.zoomLevel, this.workingSession.zoomLevel)

        val bufferedImage: BufferedImage?
        bufferedImage = BufferedImage(
            frame.width.toInt(),
            frame.height.toInt(),
            BufferedImage.TYPE_INT_ARGB
        )
        val g2 = bufferedImage.createGraphics()
        g2.color = Color.WHITE
        g2.fill(
            Rectangle2D.Double(
                0.0, 0.0, frame.width,
                frame.height
            )
        )
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.background = Color.white

        this.draw(g2, at, Rectangle2D.Double(0.0, 0.0, frame.width, frame.height))
        g2.dispose()
        ImageIO.write(bufferedImage, "PNG", outputFile)
        this.workingSession.viewX = previousViewX
        this.workingSession.viewY = previousViewY
        this.workingSession.zoomLevel = previousZoomLevel
    }

    fun asSVG(frame: Rectangle2D, selectionFrame: Rectangle2D? = null, outputFile: File? = null): String {
        val previousViewX = this.workingSession.viewX
        val previousViewY = this.workingSession.viewY
        val previousZoomLevel = this.workingSession.zoomLevel

        //We simulate a draw with a graphics object. This allows to call the draw() functions and then to set everything fine (like for example the numbering labels that are created only if the draw() functions have been called.
        selectionFrame?.let {
            this.fitViewTo(frame, selectionFrame)
        } ?: run {
            this.fitViewTo(frame)
        }
        val at = AffineTransform()
        at.translate(this.workingSession.viewX, this.workingSession.viewY)
        at.scale(this.workingSession.zoomLevel, this.workingSession.zoomLevel)

        val bufferedImage: BufferedImage?
        bufferedImage = BufferedImage(
            frame.width.toInt(),
            frame.height.toInt(),
            BufferedImage.TYPE_INT_ARGB
        )
        val g2 = bufferedImage.createGraphics()
        g2.color = Color.WHITE
        g2.fill(
            Rectangle2D.Double(
                0.0, 0.0, frame.width,
                frame.height
            )
        )
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.background = Color.white

        this.draw(g2, at, Rectangle2D.Double(0.0, 0.0, frame.width, frame.height))
        g2.dispose()

        val svgBuffer =
            StringBuffer("""<svg width="${frame.width}" height="${frame.height}" viewBox="0 0 ${frame.width} ${frame.height}"  xmlns="http://www.w3.org/2000/svg">""")

        workingSession.junctionsDrawn.forEach { junction ->
            svgBuffer.append(junction.asSVG(at, frame))
        }

        workingSession.helicesDrawn.forEach { helix ->
            svgBuffer.append(helix.asSVG(at, frame))
        }

        workingSession.singleStrandsDrawn.forEach { ss ->
            svgBuffer.append(ss.asSVG(at, frame))
        }

        workingSession.phosphoBondsLinkingBranchesDrawn.forEach { phospho ->
            svgBuffer.append(phospho.asSVG(at, frame))
        }

        pknots.forEach { pknot ->
            pknot.tertiaryInteractions.forEach { tertiary ->
                svgBuffer.append(tertiary.asSVG(at, frame))
            }
        }

        tertiaryInteractions.forEach { tertiary ->
            svgBuffer.append(tertiary.asSVG(at, frame))
        }

        svgBuffer.append("</svg>")

        outputFile?.let {
            val writer = PrintWriter(outputFile)
            writer.println(svgBuffer.toString())
            writer.close()
        }

        this.workingSession.viewX = previousViewX
        this.workingSession.viewY = previousViewY
        this.workingSession.zoomLevel = previousZoomLevel

        return svgBuffer.toString()

    }

    fun asChimeraScript(outputFile: File) {
        val chainName: String = this.secondaryStructure.rna.name
        //if no numbering system, we generate a fake one to be able to generate the ChimeraX script
        val numberingSystem: List<String> =
            this.secondaryStructure.rna.tertiary_structure_numbering_system?.values?.toList()
                ?: (1..this.secondaryStructure.rna.length).map { it.toString() }
        val colors2residues = mutableMapOf<String, MutableList<ResidueDrawing>>()
        for (r in this.residues) {
            val coloredResidues =
                colors2residues.getOrDefault(getHTMLColorString(r.getColor()), mutableListOf())
            coloredResidues.add(r)
            colors2residues[getHTMLColorString(r.getColor())] = coloredResidues
        }
        when (this.secondaryStructure.source) {
            is PDBSource -> {
                var command =
                    StringBuffer("open \"https://files.rcsb.org/download/${(this.secondaryStructure.source as PDBSource).pdbId}.pdb\"${System.lineSeparator()}")
                colors2residues.forEach { (colorCode, residues) ->
                    command.append("color /${chainName}:")
                    residues.forEach {
                        command.append("${numberingSystem[it.absPos - 1]},")
                        //command.append("${it.absPos},")
                    }
                    command = StringBuffer(command.removeSuffix(","))
                    command.append(" ${colorCode}${System.lineSeparator()}")
                }
                outputFile.writeText(command.toString())
            }

            is FileSource -> {
                if ((this.secondaryStructure.source as FileSource).fileName.endsWith("pdb")) {
                    var command =
                        StringBuffer("open \"${(this.secondaryStructure.source as FileSource).fileName}\"${System.lineSeparator()}")
                    colors2residues.forEach { (colorCode, residues) ->
                        command.append("color /${chainName}:")
                        residues.forEach {
                            command.append("${numberingSystem[it.absPos - 1]},")
                            //command.append("${it.absPos},")
                        }
                        command = StringBuffer(command.removeSuffix(","))
                        command.append(" ${colorCode}${System.lineSeparator()}")
                    }
                    outputFile.writeText(command.toString())
                }
            }
        }
        this.secondaryStructure.tertiaryStructure?.let { tertiaryStructure ->
            //we output the file with the geometric shapes simplifying the 3D structure
            val bldFile = File(outputFile.parent, "${outputFile.name.split(".cxc").first()}.bld")
            val bldInstructions = StringBuffer()
            val helices2Ends = mutableMapOf<String, Pair<Triple<Float, Float, Float>, Triple<Float, Float, Float>>>()
            this.secondaryStructure.helices.forEach { helix ->
                val firstbp = helix.secondaryInteractions.first()
                val r = tertiaryStructure.residues.filter { it.absolutePosition == firstbp.start }.first()
                val paired = tertiaryStructure.residues.filter { it.absolutePosition == firstbp.end }.first()
                val lastbp = helix.secondaryInteractions.last()
                val _r = tertiaryStructure.residues.filter { it.absolutePosition == lastbp.start }.first()
                val _paired = tertiaryStructure.residues.filter { it.absolutePosition == lastbp.end }.first()

                var atom1: Atom? = null
                var atom2: Atom? = null
                var pairedAtom1: Atom? = null
                var pairedAtom2: Atom? = null

                var cylinderEnd1: Triple<Float, Float, Float>? = null
                var cylinderEnd2: Triple<Float, Float, Float>? = null

                when (r) {
                    is Adenine3D -> {
                        atom1 = r.atoms.find { it.name == "N1" }
                        atom2 = r.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        atom1 = r.atoms.find { it.name == "N1" }
                        atom2 = r.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        atom1 = r.atoms.find { it.name == "N3" }
                        atom2 = r.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        atom1 = r.atoms.find { it.name == "N3" }
                        atom2 = r.atoms.find { it.name == "O4" }
                    }
                }

                when (paired) {
                    is Adenine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = paired.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = paired.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = paired.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = paired.atoms.find { it.name == "O4" }
                    }
                }

                atom1?.let { atom1 ->
                    atom2?.let { atom2 ->
                        pairedAtom1?.let { pairedAtom1 ->
                            pairedAtom2?.let { pairedAtom2 ->
                                val middle1 = Triple(
                                    (atom1.x!! + pairedAtom1.x!!) / 2f,
                                    (atom1.y!! + pairedAtom1.y!!) / 2f,
                                    (atom1.z!! + pairedAtom1.z!!) / 2f
                                )
                                val middle2 = Triple(
                                    (atom2.x!! + pairedAtom2.x!!) / 2f,
                                    (atom2.y!! + pairedAtom2.y!!) / 2f,
                                    (atom2.z!! + pairedAtom2.z!!) / 2f
                                )
                                val distX = middle2.first - middle1.first
                                val distY = middle2.second - middle1.second
                                val distZ = middle2.third - middle1.third
                                cylinderEnd1 = Triple(
                                    middle1.first + 3f * distX,
                                    middle1.second + 3f * distY,
                                    middle1.third + 3f * distZ
                                )
                            }
                        }
                    }
                }

                when (_r) {
                    is Adenine3D -> {
                        atom1 = _r.atoms.find { it.name == "N1" }
                        atom2 = _r.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        atom1 = _r.atoms.find { it.name == "N1" }
                        atom2 = _r.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        atom1 = _r.atoms.find { it.name == "N3" }
                        atom2 = _r.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        atom1 = _r.atoms.find { it.name == "N3" }
                        atom2 = _r.atoms.find { it.name == "O4" }
                    }
                }

                when (_paired) {
                    is Adenine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = _paired.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = _paired.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = _paired.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = _paired.atoms.find { it.name == "O4" }
                    }
                }

                atom1?.let {
                    atom2?.let {
                        pairedAtom1?.let {
                            pairedAtom2?.let {
                                val middle1 = Triple(
                                    (atom1.x!! + pairedAtom1.x!!) / 2f,
                                    (atom1.y!! + pairedAtom1.y!!) / 2f,
                                    (atom1.z!! + pairedAtom1.z!!) / 2f
                                )
                                val middle2 = Triple(
                                    (atom2.x!! + pairedAtom2.x!!) / 2f,
                                    (atom2.y!! + pairedAtom2.y!!) / 2f,
                                    (atom2.z!! + pairedAtom2.z!!) / 2f
                                )
                                val distX = middle2.first - middle1.first
                                val distY = middle2.second - middle1.second
                                val distZ = middle2.third - middle1.third
                                cylinderEnd2 = Triple(
                                    middle1.first + 3f * distX,
                                    middle1.second + 3f * distY,
                                    middle1.third + 3f * distZ
                                )
                            }
                        }
                    }
                }

                cylinderEnd1?.let { cylinderEnd1 ->
                    cylinderEnd2?.let { cylinderEnd2 ->
                        helices2Ends[helix.name] = Pair(cylinderEnd1, cylinderEnd2)
                        val color = this.allHelices.find { it.helix == helix }!!.getColor()
                        bldInstructions.append(".color ${color.red.toFloat() / 255f} ${color.green.toFloat() / 255f} ${color.blue.toFloat() / 255f}${System.lineSeparator()}")
                        bldInstructions.append(".cylinder ${cylinderEnd1.first} ${cylinderEnd1.second} ${cylinderEnd1.third} ${cylinderEnd2.first} ${cylinderEnd2.second} ${cylinderEnd2.third} 3${System.lineSeparator()}")
                    }
                }
            }

            val junctions2Center = mutableMapOf<String, Triple<Float, Float, Float>>()

            this.secondaryStructure.junctions.forEach { junction ->

                val allX = mutableListOf<Float>()
                val allY = mutableListOf<Float>()
                val allZ = mutableListOf<Float>()

                junction.location.positions.forEach { pos ->
                    val r = tertiaryStructure.residues.filter { pos == it.absolutePosition }.first()
                    val a = r.atoms.find { it.name == "C1'" }
                    a?.let {
                        allX.add(a.x!!)
                        allY.add(a.y!!)
                        allZ.add(a.z!!)
                    }

                }

                val color = this.allJunctions.find { it.junction == junction }!!.getColor()
                bldInstructions.append(".color ${color.red.toFloat() / 255f} ${color.green.toFloat() / 255f} ${color.blue.toFloat() / 255f}${System.lineSeparator()}")
                bldInstructions.append(".sphere ${allX.average()} ${allY.average()} ${allZ.average()} 3${System.lineSeparator()}")
                junctions2Center[junction.name] =
                    Triple(allX.average().toFloat(), allY.average().toFloat(), allZ.average().toFloat())

            }

            val helicesProcessed = mutableListOf<Helix>()
            secondaryStructure.helices.forEach { helix ->
                if (!helicesProcessed.contains(helix)) {
                    var currentHelix = helix
                    val currentDomains = mutableListOf<StructuralDomain>()
                    currentDomains.add(currentHelix)
                    helix.junctionsLinked.first?.let { junction ->
                        if (junction.start < currentHelix.start) {
                            currentDomains.add(0, junction)
                        } else {
                            var currentJunction = junction
                            if (currentJunction.junctionType == JunctionType.ApicalLoop)
                                currentDomains.add(currentJunction)
                            else {
                                while (currentJunction.junctionType == JunctionType.InnerLoop) {
                                    currentDomains.add(currentJunction)
                                    currentJunction.helicesLinked.find { it != currentHelix }?.let { h ->
                                        currentHelix = h
                                        currentDomains.add(currentHelix)
                                        currentJunction =
                                            if (currentJunction == currentHelix.junctionsLinked.first) currentHelix.junctionsLinked.second!! else currentHelix.junctionsLinked.first!!
                                    }
                                }
                                currentDomains.add(currentJunction)
                            }
                        }
                    }

                    helix.junctionsLinked.second?.let { junction ->
                        if (junction.start < currentHelix.start) {
                            currentDomains.add(0, junction)
                        } else {
                            var currentJunction = junction
                            if (currentJunction.junctionType == JunctionType.ApicalLoop)
                                currentDomains.add(currentJunction)
                            else {
                                while (currentJunction.junctionType == JunctionType.InnerLoop) {
                                    currentDomains.add(currentJunction)
                                    currentJunction.helicesLinked.find { it != currentHelix }?.let { h ->
                                        currentHelix = h
                                        currentDomains.add(currentHelix)
                                        currentJunction =
                                            if (currentJunction == currentHelix.junctionsLinked.first) currentHelix.junctionsLinked.second!! else currentHelix.junctionsLinked.first!!
                                    }
                                }
                                currentDomains.add(currentJunction)
                            }
                        }
                    }

                    currentDomains.filter { it is Helix }.forEach { helicesProcessed.add(it as Helix) }

                    currentDomains.forEachIndexed { index, sd ->
                        when (sd) {

                            is Junction -> {
                                this.allJunctions.find { it.junction == sd }?.let { junctionDrawing ->
                                    bldInstructions.append(".color 1.0 1.0 1.0${System.lineSeparator()}")
                                    val newEnd = getPoint(
                                        helices2Ends[junctionDrawing.inHelix.name]!!.second,
                                        junctions2Center[sd.name]!!,
                                        -3.0f
                                    )
                                    bldInstructions.append(".arrow ${helices2Ends[junctionDrawing.inHelix.name]!!.second.first} ${helices2Ends[junctionDrawing.inHelix.name]!!.second.second} ${helices2Ends[junctionDrawing.inHelix.name]!!.second.third} ${newEnd.first} ${newEnd.second} ${newEnd.third} 0.5 1.0${System.lineSeparator()}")
                                    sd.helicesLinked.forEach {
                                        if (it != junctionDrawing.inHelix) {
                                            bldInstructions.append(".color 1.0 1.0 1.0${System.lineSeparator()}")
                                            bldInstructions.append(".arrow ${junctions2Center[sd.name]!!.first} ${junctions2Center[sd.name]!!.second} ${junctions2Center[sd.name]!!.third} ${helices2Ends[it.name]!!.first.first} ${helices2Ends[it.name]!!.first.second} ${helices2Ends[it.name]!!.first.third} 0.5 1.0${System.lineSeparator()}")
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
            }

            secondaryStructure.singleStrands.filter { it.start != 1 && it.end != secondaryStructure.rna.length }
                .forEach { single_strand ->
                    val h1 = secondaryStructure.helices.find { it.ends.contains(single_strand.start - 1) }!!
                    val h2 = secondaryStructure.helices.find { it.ends.contains(single_strand.end + 1) }!!
                    bldInstructions.append(".color 1.0 1.0 1.0${System.lineSeparator()}")
                    bldInstructions.append(".arrow ${helices2Ends[h1.name]!!.first.first} ${helices2Ends[h1.name]!!.first.second} ${helices2Ends[h1.name]!!.first.third} ${helices2Ends[h2.name]!!.first.first} ${helices2Ends[h2.name]!!.first.second} ${helices2Ends[h2.name]!!.first.third} 0.5 1.0${System.lineSeparator()}")
                }

            val branches =
                secondaryStructure.helices.filter { it.junctionsLinked.first == null || it.junctionsLinked.second == null }

            branches.forEach { branch ->
                branches.find { it.start == branch.end + 1 }?.let {
                    //two helices linked directly
                    bldInstructions.append(".color 1.0 1.0 1.0${System.lineSeparator()}")
                    bldInstructions.append(".arrow ${helices2Ends[branch.name]!!.first.first} ${helices2Ends[branch.name]!!.first.second} ${helices2Ends[branch.name]!!.first.third} ${helices2Ends[it.name]!!.first.first} ${helices2Ends[it.name]!!.first.second} ${helices2Ends[it.name]!!.first.third} 0.5 1.0${System.lineSeparator()}")
                }
            }

            bldFile.writeText(bldInstructions.toString())
        }

    }

    fun getPoint(
        p1: Triple<Float, Float, Float>,
        p2: Triple<Float, Float, Float>,
        distance: Float
    ): Triple<Float, Float, Float> {
        val distX = p2.first - p1.first
        val distY = p2.second - p1.second
        val distZ = p2.third - p1.third

        val normalizedVector = Triple(
            distX / sqrt(distX * distX + distY * distY + distZ * distZ),
            distY / sqrt(distX * distX + distY * distY + distZ * distZ),
            distZ / sqrt(distX * distX + distY * distY + distZ * distZ)
        )

        return Triple(
            p2.first + distance * normalizedVector.first,
            p2.second + distance * normalizedVector.second,
            p2.third + distance * normalizedVector.third
        )

    }

    fun asBlenderScript(tertiaryStructure: TertiaryStructure, outputFile: File) {

        if (this.secondaryStructure.helices.isEmpty())
            return

        val allX = mutableListOf<Float>()
        val allY = mutableListOf<Float>()
        val allZ = mutableListOf<Float>()

        val script = StringBuilder(
            """import bpy, bmesh, math, random, mathutils
from bpy.app import handlers

def on_frame_change(scene):
    for collection in collections_list:
        if bpy.context.scene.frame_current in collection['toggle_visibility_frame']:
            collection.hide_render = not collection.hide_render
            collection.hide_viewport = not collection.hide_viewport
    for object in objects_list:
        if bpy.context.scene.frame_current in object['toggle_visibility_frame']:
            object.hide_render = not object.hide_render
            object.hide_viewport = not object.hide_viewport

if on_frame_change not in handlers.frame_change_pre:
    handlers.frame_change_pre.append(on_frame_change)
            
def helix_as_cylinder(x1, y1, z1, x2, y2, z2, r):
    dx = x2 - x1
    dy = y2 - y1
    dz = z2 - z1    
    dist = math.sqrt(dx**2 + dy**2 + dz**2)

    bpy.ops.mesh.primitive_cylinder_add(
        radius = r, 
        depth = dist,
        location = (dx/2 + x1, dy/2 + y1, dz/2 + z1)   
    ) 

    phi = math.atan2(dy, dx) 
    theta = math.acos(dz/dist) 

    bpy.context.object.rotation_euler[1] = theta 
    bpy.context.object.rotation_euler[2] = phi 

for obj in bpy.data.objects:
    bpy.data.objects.remove(obj)

for obj in bpy.data.collections:
    bpy.data.collections.remove(obj)
    
backbone_bevel_depth = 0.8
residues_bevel_depth = 0.3
full_curve_bevel_depth = 0.3
cylinder_radius = 2.5
sphere_radius = 2.5
    
structure_collection = bpy.data.collections.new("3D structure")
bpy.context.scene.collection.children.link(structure_collection)
helices_collection = bpy.data.collections.new("helices")
structure_collection.children.link(helices_collection)
cylinders_collection = bpy.data.collections.new("cylinders")
structure_collection.children.link(cylinders_collection)
junctions_collection = bpy.data.collections.new("junctions")
structure_collection.children.link(junctions_collection)"""
        )

        val helices2Ends = mutableMapOf<String, Pair<Triple<Float, Float, Float>, Triple<Float, Float, Float>>>()

        if (this.secondaryStructure.source is PDBSource || this.secondaryStructure.source is FileSource && (this.secondaryStructure.source as FileSource).fileName.endsWith(
                "pdb"
            )
        ) {
            this.secondaryStructure.helices.forEach { helix ->
                script.append(
                    """
                        
helix_collection = bpy.data.collections.new("${helix.name}")
helices_collection.children.link(helix_collection)

backbone_curve = bpy.data.curves.new('backbone', type='CURVE')
backbone_curve.dimensions = '3D'
backbone_curve.resolution_u = 3
backbone_curve.bevel_depth = backbone_bevel_depth
h = bpy.data.objects.new("backbone", backbone_curve)
helix_collection.objects.link(h)
h.color = (random.random(),random.random() ,random.random(),1)
mat = bpy.data.materials.new("Color")
mat.use_nodes = True
principled = mat.node_tree.nodes['Principled BSDF']
principled.inputs['Base Color'].default_value = h.color
h.data.materials.append(mat)

residues_curve = bpy.data.curves.new('residues', type='CURVE')
residues_curve.dimensions = '3D'
residues_curve.resolution_u = 3
residues_curve.bevel_depth = residues_bevel_depth
h_residues = bpy.data.objects.new("residues", residues_curve)
helix_collection.objects.link(h_residues)
h_residues.color = h.color
mat = bpy.data.materials.new("Color")
mat.use_nodes = True
principled = mat.node_tree.nodes['Principled BSDF']
principled.inputs['Base Color'].default_value = h.color
h_residues.data.materials.append(mat)"""
                )

                val firstbp = helix.secondaryInteractions.first()
                val r = tertiaryStructure.residues.filter { it.absolutePosition == firstbp.start }.first()
                val paired = tertiaryStructure.residues.filter { it.absolutePosition == firstbp.end }.first()
                val lastbp = helix.secondaryInteractions.last()
                val _r = tertiaryStructure.residues.filter { it.absolutePosition == lastbp.start }.first()
                val _paired = tertiaryStructure.residues.filter { it.absolutePosition == lastbp.end }.first()

                var atom1: Atom? = null
                var atom2: Atom? = null
                var pairedAtom1: Atom? = null
                var pairedAtom2: Atom? = null

                var cylinderEnd1: Triple<Float, Float, Float>? = null
                var cylinderEnd2: Triple<Float, Float, Float>? = null

                when (r) {
                    is Adenine3D -> {
                        atom1 = r.atoms.find { it.name == "N1" }
                        atom2 = r.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        atom1 = r.atoms.find { it.name == "N1" }
                        atom2 = r.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        atom1 = r.atoms.find { it.name == "N3" }
                        atom2 = r.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        atom1 = r.atoms.find { it.name == "N3" }
                        atom2 = r.atoms.find { it.name == "O4" }
                    }
                }

                when (paired) {
                    is Adenine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = paired.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = paired.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = paired.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        pairedAtom1 = paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = paired.atoms.find { it.name == "O4" }
                    }
                }

                atom1?.let { atom1 ->
                    atom2?.let { atom2 ->
                        pairedAtom1?.let { pairedAtom1 ->
                            pairedAtom2?.let { pairedAtom2 ->
                                val middle1 = Triple(
                                    (atom1.x!! + pairedAtom1.x!!) / 2f,
                                    (atom1.y!! + pairedAtom1.y!!) / 2f,
                                    (atom1.z!! + pairedAtom1.z!!) / 2f
                                )
                                val middle2 = Triple(
                                    (atom2.x!! + pairedAtom2.x!!) / 2f,
                                    (atom2.y!! + pairedAtom2.y!!) / 2f,
                                    (atom2.z!! + pairedAtom2.z!!) / 2f
                                )
                                val distX = middle2.first - middle1.first
                                val distY = middle2.second - middle1.second
                                val distZ = middle2.third - middle1.third
                                cylinderEnd1 = Triple(
                                    middle1.first + 3f * distX,
                                    middle1.second + 3f * distY,
                                    middle1.third + 3f * distZ
                                )
                            }
                        }
                    }
                }

                when (_r) {
                    is Adenine3D -> {
                        atom1 = _r.atoms.find { it.name == "N1" }
                        atom2 = _r.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        atom1 = _r.atoms.find { it.name == "N1" }
                        atom2 = _r.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        atom1 = _r.atoms.find { it.name == "N3" }
                        atom2 = _r.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        atom1 = _r.atoms.find { it.name == "N3" }
                        atom2 = _r.atoms.find { it.name == "O4" }
                    }
                }

                when (_paired) {
                    is Adenine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = _paired.atoms.find { it.name == "N6" }
                    }

                    is Guanine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N1" }
                        pairedAtom2 = _paired.atoms.find { it.name == "O6" }
                    }

                    is Cytosine3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = _paired.atoms.find { it.name == "N4" }
                    }

                    is Uracil3D -> {
                        pairedAtom1 = _paired.atoms.find { it.name == "N3" }
                        pairedAtom2 = _paired.atoms.find { it.name == "O4" }
                    }
                }

                atom1?.let {
                    atom2?.let {
                        pairedAtom1?.let {
                            pairedAtom2?.let {
                                val middle1 = Triple(
                                    (atom1.x!! + pairedAtom1.x!!) / 2f,
                                    (atom1.y!! + pairedAtom1.y!!) / 2f,
                                    (atom1.z!! + pairedAtom1.z!!) / 2f
                                )
                                val middle2 = Triple(
                                    (atom2.x!! + pairedAtom2.x!!) / 2f,
                                    (atom2.y!! + pairedAtom2.y!!) / 2f,
                                    (atom2.z!! + pairedAtom2.z!!) / 2f
                                )
                                val distX = middle2.first - middle1.first
                                val distY = middle2.second - middle1.second
                                val distZ = middle2.third - middle1.third
                                cylinderEnd2 = Triple(
                                    middle1.first + 3f * distX,
                                    middle1.second + 3f * distY,
                                    middle1.third + 3f * distZ
                                )
                            }
                        }
                    }
                }

                cylinderEnd1?.let { cylinderEnd1 ->
                    cylinderEnd2?.let { cylinderEnd2 ->
                        helices2Ends[helix.name] = Pair(cylinderEnd1, cylinderEnd2)
                        script.append(
                            """
helix_as_cylinder(${cylinderEnd1.first}, ${cylinderEnd1.second}, ${cylinderEnd1.third}, ${cylinderEnd2.first}, ${cylinderEnd2.second}, ${cylinderEnd2.third}, cylinder_radius)
bpy.context.object.name = "${helix.name}"
bpy.context.object.color = h.color
mat = bpy.data.materials.new("Color")
mat.use_nodes = True
principled = mat.node_tree.nodes['Principled BSDF']
principled.inputs['Base Color'].default_value = h.color
bpy.context.object.data.materials.append(mat)
cylinders_collection.objects.link(bpy.context.object)
bpy.context.collection.objects.unlink(bpy.context.object)"""
                        )
                    }
                }


                helix.location.blocks.forEachIndexed { _, strandLocation ->

                    script.append(
                        """

backbone_spline = backbone_curve.splines.new('NURBS')
backbone_spline.use_endpoint_u = True
backbone_spline.points.add(${strandLocation.length - 1})"""
                    )

                    var residue_index = 0
                    tertiaryStructure.residues.filter { it.absolutePosition in strandLocation.start..strandLocation.end }
                        .forEach {

                            it.atoms.find { it.name == "C4'" }?.let {
                                allX.add(it.x!!)
                                allY.add(it.y!!)
                                allZ.add(it.z!!)
                                script.append(
                                    """
backbone_spline.points[${residue_index}].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                )

                                residue_index++
                            }
                            if (it is Adenine3D || it is Guanine3D) {
                                script.append(
                                    """         
residue_spline = residues_curve.splines.new('NURBS')
residue_spline.use_endpoint_u = True
residue_spline.points.add(1)
"""
                                )
                                it.atoms.find { it.name == "C4'" }?.let {
                                    allX.add(it.x!!)
                                    allY.add(it.y!!)
                                    allZ.add(it.z!!)
                                    script.append(
                                        """
residue_spline.points[0].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )
                                }
                                it.atoms.find { it.name == "N1" }?.let {
                                    script.append(
                                        """
residue_spline.points[1].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )

                                }
                            }
                            if (it is Uracil3D || it is Cytosine3D) {
                                script.append(
                                    """         
residue_spline = residues_curve.splines.new('NURBS')
residue_spline.use_endpoint_u = True
residue_spline.points.add(1)
"""
                                )
                                it.atoms.find { it.name == "C4'" }?.let {
                                    allX.add(it.x!!)
                                    allY.add(it.y!!)
                                    allZ.add(it.z!!)
                                    script.append(
                                        """
residue_spline.points[0].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )
                                }
                                it.atoms.find { it.name == "N3" }?.let {
                                    script.append(
                                        """
residue_spline.points[1].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )

                                }
                            }
                        }
                }
            }
            this.secondaryStructure.junctions.forEach { junction ->
                script.append(
                    """

junction_collection = bpy.data.collections.new("${junction.name}")
junctions_collection.children.link(junction_collection)

backbone_curve = bpy.data.curves.new("backbone", type='CURVE')
backbone_curve.dimensions = '3D'
backbone_curve.resolution_u = 3
backbone_curve.bevel_depth = backbone_bevel_depth
j = bpy.data.objects.new("backbone", backbone_curve)
junction_collection.objects.link(j)
j.color = (random.random(),random.random() ,random.random(),1)
mat = bpy.data.materials.new("Color")
mat.use_nodes = True
principled = mat.node_tree.nodes['Principled BSDF']
principled.inputs['Base Color'].default_value = j.color
j.data.materials.append(mat)

residues_curve = bpy.data.curves.new("residues", type='CURVE')
residues_curve.dimensions = '3D'
residues_curve.resolution_u = 3
residues_curve.bevel_depth = residues_bevel_depth
j_residues = bpy.data.objects.new("residues", residues_curve)
junction_collection.objects.link(j_residues)
j_residues.color = j.color
mat = bpy.data.materials.new("Color")
mat.use_nodes = True
principled = mat.node_tree.nodes['Principled BSDF']
principled.inputs['Base Color'].default_value = j.color
j_residues.data.materials.append(mat)"""
                )
                junction.location.blocks.forEachIndexed { _, strandLocation ->

                    script.append(
                        """
                        
backbone_spline = backbone_curve.splines.new('NURBS')
backbone_spline.use_endpoint_u = True
backbone_spline.points.add(${strandLocation.length - 1})"""
                    )

                    var residue_index = 0
                    tertiaryStructure.residues.filter { it.absolutePosition in strandLocation.start..strandLocation.end }
                        .forEach {
                            it.atoms.find { it.name == "C4'" }?.let {
                                allX.add(it.x!!)
                                allY.add(it.y!!)
                                allZ.add(it.z!!)
                                script.append(
                                    """
backbone_spline.points[${residue_index}].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                )
                                residue_index++
                            }
                            if (it is Adenine3D || it is Guanine3D) {
                                script.append(
                                    """         
residue_spline = residues_curve.splines.new('NURBS')
residue_spline.use_endpoint_u = True
residue_spline.points.add(1)"""
                                )
                                it.atoms.find { it.name == "C4'" }?.let {
                                    allX.add(it.x!!)
                                    allY.add(it.y!!)
                                    allZ.add(it.z!!)
                                    script.append(
                                        """
residue_spline.points[0].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )
                                }
                                it.atoms.find { it.name == "N1" }?.let {
                                    script.append(
                                        """
residue_spline.points[1].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )

                                }
                            }
                            if (it is Uracil3D || it is Cytosine3D) {
                                script.append(
                                    """         
residue_spline = residues_curve.splines.new('NURBS')
residue_spline.use_endpoint_u = True
residue_spline.points.add(1)"""
                                )
                                it.atoms.find { it.name == "C4'" }?.let {
                                    allX.add(it.x!!)
                                    allY.add(it.y!!)
                                    allZ.add(it.z!!)
                                    script.append(
                                        """
residue_spline.points[0].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )
                                }
                                it.atoms.find { it.name == "N3" }?.let {
                                    script.append(
                                        """
residue_spline.points[1].co = (${it.x}, ${it.y}, ${it.z}, 1)"""
                                    )

                                }
                            }
                        }
                }
            }
        }

        val minX = allX.minOf { it }
        val minY = allY.minOf { it }
        val minZ = allZ.minOf { it }

        val maxX = allX.maxOf { it }
        val maxY = allY.maxOf { it }
        val maxZ = allZ.maxOf { it }

        script.append(
            """
for o in bpy.context.scene.objects:
    o.select_set(True)
bpy.ops.transform.translate(value=(-${(maxX - minX) / 2.0 + minX}, -${(maxY - minY) / 2.0 + minY}, ${-((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}))
bpy.context.scene.cursor.location = (0.0, 0.0, ${(maxZ - minZ) / 2.0})
bpy.ops.object.origin_set(type='ORIGIN_GEOMETRY', center='MEDIAN')
bpy.context.scene.tool_settings.transform_pivot_point = 'CURSOR'
for o in bpy.context.scene.objects:
    o.select_set(False)
    
spheres_collection = bpy.data.collections.new("spheres")
structure_collection.children.link(spheres_collection)

for c in junctions_collection.children:
    backbone_junction = c.objects[0]
    bpy.ops.mesh.primitive_uv_sphere_add(radius = sphere_radius, location = backbone_junction.location)
    bpy.context.object.name = c.name
    spheres_collection.objects.link(bpy.context.object)
    bpy.context.collection.objects.unlink(bpy.context.object)
    bpy.context.object.color = backbone_junction.color
    mat = bpy.data.materials.new("Color")
    mat.use_nodes = True
    principled = mat.node_tree.nodes['Principled BSDF']
    principled.inputs['Base Color'].default_value = bpy.context.object.color
    bpy.context.object.data.materials.append(mat)"""
        )


        script.append(
            """
full_curve = bpy.data.curves.new('full_curve', type='CURVE')
full_curve.dimensions = '3D'
full_curve.resolution_u = 10
full_curve.bevel_depth = full_curve_bevel_depth
full_curve_object = bpy.data.objects.new("full_curve", full_curve)
structure_collection.objects.link(full_curve_object)"""
        )

        val helicesProcessed = mutableListOf<Helix>()
        secondaryStructure.helices.forEach { helix ->
            if (!helicesProcessed.contains(helix)) {
                script.append(
                    """
full_curve_spline = full_curve.splines.new('NURBS')
full_curve_spline.use_endpoint_u = True"""
                )
                var currentHelix = helix
                val currentDomains = mutableListOf<StructuralDomain>()
                currentDomains.add(currentHelix)
                helix.junctionsLinked.first?.let { junction ->
                    if (junction.start < currentHelix.start) {
                        currentDomains.add(0, junction)
                    } else {
                        var currentJunction = junction
                        if (currentJunction.junctionType == JunctionType.ApicalLoop)
                            currentDomains.add(currentJunction)
                        else {
                            while (currentJunction.junctionType == JunctionType.InnerLoop) {
                                currentDomains.add(currentJunction)
                                currentJunction.helicesLinked.find { it != currentHelix }?.let { h ->
                                    currentHelix = h
                                    currentDomains.add(currentHelix)
                                    currentJunction =
                                        if (currentJunction == currentHelix.junctionsLinked.first) currentHelix.junctionsLinked.second!! else currentHelix.junctionsLinked.first!!
                                }
                            }
                            currentDomains.add(currentJunction)
                        }
                    }
                }

                helix.junctionsLinked.second?.let { junction ->
                    if (junction.start < currentHelix.start) {
                        currentDomains.add(0, junction)
                    } else {
                        var currentJunction = junction
                        if (currentJunction.junctionType == JunctionType.ApicalLoop)
                            currentDomains.add(currentJunction)
                        else {
                            while (currentJunction.junctionType == JunctionType.InnerLoop) {
                                currentDomains.add(currentJunction)
                                currentJunction.helicesLinked.find { it != currentHelix }?.let { h ->
                                    currentHelix = h
                                    currentDomains.add(currentHelix)
                                    currentJunction =
                                        if (currentJunction == currentHelix.junctionsLinked.first) currentHelix.junctionsLinked.second!! else currentHelix.junctionsLinked.first!!
                                }
                            }
                            currentDomains.add(currentJunction)
                        }
                    }
                }

                currentDomains.filter { it is Helix }.forEach { helicesProcessed.add(it as Helix) }

                script.append(
                    """
full_curve_spline.points.add(${currentDomains.filter { it is Helix }.size * 2 + currentDomains.filter { it is Junction }.size - 1})
full_curve_index = 0"""
                )
                currentDomains.forEach {
                    when (it) {
                        is Helix -> {

                            script.append(
                                """
# helix ${it.name}
full_curve_spline.points[full_curve_index].co = (${helices2Ends[it.name]!!.first.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[it.name]!!.first.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[it.name]!!.first.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)
full_curve_index += 1
full_curve_spline.points[full_curve_index].co = (${helices2Ends[it.name]!!.second.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[it.name]!!.second.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[it.name]!!.second.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)
full_curve_index += 1"""
                            )

                        }

                        is Junction -> {

                            script.append(
                                """
backbone_junction = spheres_collection.objects["${it.name}"]
full_curve_spline.points[full_curve_index].co = (backbone_junction.location.x, backbone_junction.location.y, backbone_junction.location.z, 1)
full_curve_index += 1"""
                            )

                        }
                    }
                }

            }
        }

        secondaryStructure.singleStrands.filter { it.start != 1 && it.end != secondaryStructure.rna.length }
            .forEach { single_strand ->
                script.append(
                    """
# single-strand ${single_strand.name}
full_curve_spline = full_curve.splines.new('NURBS')
full_curve_spline.use_endpoint_u = True
full_curve_spline.points.add(1)"""
                )
                val h1 = secondaryStructure.helices.find { it.ends.contains(single_strand.start - 1) }!!
                val h2 = secondaryStructure.helices.find { it.ends.contains(single_strand.end + 1) }!!
                script.append(
                    """
#helix ${h1.name}
full_curve_spline.points[0].co = (${helices2Ends[h1.name]!!.first.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[h1.name]!!.first.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[h1.name]!!.first.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)
#helix ${h2.name}
full_curve_spline.points[1].co = (${helices2Ends[h2.name]!!.first.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[h2.name]!!.first.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[h2.name]!!.first.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)"""
                )
            }

        val branches =
            secondaryStructure.helices.filter { it.junctionsLinked.first == null || it.junctionsLinked.second == null }

        branches.forEach { branch ->
            branches.find { it.start == branch.end + 1 }?.let {
                //two helices linked directly
                script.append(
                    """
# helices linked directly
full_curve_spline = full_curve.splines.new('NURBS')
full_curve_spline.use_endpoint_u = True
full_curve_spline.points.add(1)
#helix ${branch.name}
full_curve_spline.points[0].co = (${helices2Ends[branch.name]!!.first.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[branch.name]!!.first.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[branch.name]!!.first.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)
#helix ${it.name}
full_curve_spline.points[1].co = (${helices2Ends[it.name]!!.first.first - ((maxX - minX) / 2.0 + minX)}, ${helices2Ends[it.name]!!.first.second - ((maxY - minY) / 2.0 + minY)}, ${helices2Ends[it.name]!!.first.third - ((maxZ - minZ) / 2.0 + minZ) + (maxZ - minZ) / 2.0}, 1)"""
                )
            }
        }

        script.append(
            """
#lights
bpy.ops.object.light_add(type='SUN', align='WORLD', location=(0, 0, 50), scale=(1, 1, 1))
bpy.ops.object.light_add(type='SUN', align='WORLD', location=(0, 50, 0), scale=(1, 1, 1))
bpy.ops.object.light_add(type='SUN', align='WORLD', location=(0, -50, 0), scale=(1, 1, 1))
    
#animation
bpy.ops.object.camera_add()
camera = bpy.data.objects['Camera']
camera.data.type = 'ORTHO'
camera.data.ortho_scale = 450 
bpy.context.scene.camera = camera

#we place and orient the camera
camera.location = (bpy.context.scene.cursor.location.x-100 , bpy.context.scene.cursor.location.y, bpy.context.scene.cursor.location.z)
direction = camera.location - bpy.context.scene.cursor.location   
camera.rotation_euler = direction.to_track_quat('Z').to_euler()


for o in bpy.context.scene.objects:
    o.animation_data_clear()

total_time = 10
fps = 24
bpy.context.scene.frame_start = 0
bpy.context.scene.frame_end = int(total_time*fps)+1

bpy.data.collections['cylinders'].hide_render = True
bpy.data.collections['cylinders'].hide_viewport = True
bpy.data.collections['spheres'].hide_render = True
bpy.data.collections['spheres'].hide_viewport = True
bpy.data.objects['full_curve'].hide_render = True
bpy.data.objects['full_curve'].hide_viewport = True

bpy.data.collections['helices']['toggle_visibility_frame'] = [60, 110, 180, 230]
bpy.data.collections['junctions']['toggle_visibility_frame'] = [60, 110, 180, 230]
bpy.data.collections['cylinders']['toggle_visibility_frame'] = [50, 120, 170, 240]
bpy.data.collections['spheres']['toggle_visibility_frame'] = [50, 120, 170, 240]
bpy.data.objects['full_curve']['toggle_visibility_frame'] = [50, 120, 170, 240]

collections_list = [collection for collection in bpy.data.collections if 'toggle_visibility_frame' in collection]
objects_list = [object for object in bpy.data.objects if 'toggle_visibility_frame' in object]

nlast = bpy.context.scene.frame_end
for n in range(nlast):
    bpy.context.scene.frame_set(n)
    for o in bpy.context.scene.objects:
        #if o != camera:
        #    if n >= 50 and n < 65:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1.03
        #    elif n >= 65 and n < 70:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1.01
        #    elif n >= 70 and n < 75:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1.005
        #    elif n >= 200 and n < 215:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1/1.03
        #    elif n >= 215 and n < 220:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1/1.01
        #    elif n >= 220 and n < 225:
        #        o.location = bpy.context.scene.cursor.location+(o.location-bpy.context.scene.cursor.location)*1/1.005
        #    o.keyframe_insert(data_path="location")
        if o == camera:
            radius = (bpy.context.scene.cursor.location - camera.location).length
            x = radius * math.cos(n / (nlast-1) * (2 * math.pi))
            y = radius * math.sin(n / (nlast-1) * (2 * math.pi))
            camera.location = (x,y, camera.location.z) 
            camera.keyframe_insert(data_path="location")
            
            direction = camera.location - bpy.context.scene.cursor.location
            camera.rotation_euler = direction.to_track_quat('Z').to_euler()
            camera.keyframe_insert(data_path="rotation_euler")"""
        )


        outputFile.writeText(script.toString())
    }

    fun getFrame(location: Location): Rectangle2D? {
        val selectedResidues = this.getResiduesFromAbsPositions(*location.positions.toIntArray())
        val firstShape = selectedResidues.firstOrNull()?.selectionShape
        firstShape?.let { firstShape ->
            var frame = firstShape.bounds2D
            selectedResidues.forEach {
                frame = frame.createUnion(it.selectionShape.bounds2D)
            }
            return frame
        }
        return null
    }


}

abstract class ResidueDrawing(
    parent: DrawingElement?,
    residueLetter: Char,
    ssDrawing: SecondaryStructureDrawing,
    absPos: Int,
    type: SecondaryStructureType
) : DrawingElement(ssDrawing, parent, residueLetter.toString(), Location(absPos), type) {

    override val children: List<DrawingElement>
        get() = listOf(this.residueLetter)

    var updated = true //to force the recomputation of base-base interaction shapes
        set(value) {
            field = value
            if (value)
                ssDrawing.residuesUpdated.add(this.absPos)
        }
    val absPos: Int
        get() = this.location.start

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem) {
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(this.location.start))
    } else {
        location.contains(this.location.start)
    }

    lateinit var circle: Ellipse2D

    lateinit var residueLetter: ResidueLetterDrawing

    var letterNumbering = mutableListOf<Triple<String, Float, Float>>()
    var shapesNumbering = mutableListOf<Shape>()

    var center: Point2D = Point2D.Double(0.0, 0.0)
        set(value) {
            field = value
            this.circle = Ellipse2D.Double(
                value.x - radiusConst,
                value.y - radiusConst,
                (radiusConst * 2F),
                (radiusConst * 2F)
            )
        }

    override val bounds: List<Point2D>
        get() {
            val b = this.circle.bounds.bounds2D
            val shift = this.getLineWidth()+radiusConst*0.1
            return listOf(
                Point2D.Double(b.minX - shift, b.minY - shift),
                Point2D.Double(b.maxX + shift, b.minY - shift),
                Point2D.Double(b.maxX + shift, b.maxY + shift),
                Point2D.Double(b.minX - shift, b.maxY + shift)
            )
        }


    /**
     * is full details if its own drawing configuration is true and if its parent (Secondary Interaction, Junction, SingleStrand) is full details
     */
    //override fun isFullDetails() = this.parent!!.isFullDetails() && super.isFullDetails()

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        this.letterNumbering.clear()
        this.shapesNumbering.clear()
        if (this.isFullDetails()) {
            val _c = at.createTransformedShape(this.circle)
            g.color = this.getColor(selectedDrawings)
            g.fill(_c)
            val previousStroke: Stroke = g.getStroke()
            g.stroke =
                BasicStroke(this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat())
            g.color = this.getLineColor(selectedDrawings)
            g.draw(_c)
            g.stroke = previousStroke
            if ((absPos % 5 == 0 || absPos == 1 || absPos == ssDrawing.length) && g.font.size - 4 > 4 && this.getOpacity() > 0)
                this.drawNumbering(g, at)
            this.residueLetter.draw(g, at, drawingArea, selectedDrawings)
        }
    }

    override fun show():List<DrawingElement> {
        var l = mutableListOf<DrawingElement>()
        this.pathToStructuralDomain().forEach {
            if (!it.drawingConfiguration.fullDetails) {
                l.add(it)
                val t = Theme()
                t.addConfiguration(ThemeProperty.fulldetails, { _ -> "true" })
                it.applyTheme(t, { true }, { true })
            }
        }
        return l
    }

    fun getLineColor(selectedDrawings: List<DrawingElement>? = null): Color {
        return selectedDrawings?.let { selection ->
            if (selectedDrawings.isEmpty() || !this.pathToStructuralDomain().none { selection.contains(it) }) {
                val _c = this.drawingConfiguration.color
                Color(
                    _c.darker().red, _c.darker().green, _c.darker().blue,
                    this.getOpacity()
                )
            } else
                Color(229, 229, 229)
        } ?: run {
            val _c = this.drawingConfiguration.color
            Color(
                _c.darker().red, _c.darker().green, _c.darker().blue,
                this.getOpacity()
            )
        }
    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val buffer = StringBuffer("<g>")
        val _c = at.createTransformedShape(this.circle)
        if (this.isFullDetails()) {
            val strokeColor = Color(
                this.getColor().darker().red, this.getColor().darker().green, this.getColor().darker().blue,
                this.getOpacity()
            )
            if (frame.contains(_c.bounds2D))
                buffer.append(
                    """<circle cx="${_c.bounds.centerX}" cy="${_c.bounds.centerY}" r="${_c.bounds.width / 2}" stroke="${
                        getHTMLColorString(
                            strokeColor
                        )
                    }" stroke-width="${
                        this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                    }" fill="${getHTMLColorString(this.getColor())}"/>"""
                )
            if (this.getOpacity() > 0 && frame.contains(_c.bounds2D)) { //the conditions to draw a letter
                buffer.append(this.residueLetter.asSVG(at))
            }
        }
        buffer.append("</g>")
        letterNumbering.forEach {
            buffer.append("<g>")
            buffer.append(
                if (RnartistConfig.exportSVGWithBrowserCompatibility())
                    """<text x="${it.second}" y="${it.third}" text-anchor="middle" dy=".3em" style="fill:${
                        getHTMLColorString(
                            Color(
                                Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
                                this.getOpacity()
                            )
                        )
                    };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize - 4};">${it.first}</text>"""
                else
                    """<text x="${it.second}" y="${it.third}" style="fill:${
                        getHTMLColorString(
                            Color(
                                Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
                                this.getOpacity()
                            )
                        )
                    };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize - 4};">${it.first}</text>"""
            )
            buffer.append("</g>")
        }
        shapesNumbering.forEach {
            buffer.append("<g>")
            buffer.append(
                """<circle cx="${it.bounds.centerX}" cy="${it.bounds.centerY}" r="${it.bounds.width / 2}"  stroke-width="0.0" fill="${
                    getHTMLColorString(
                        Color(
                            Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
                            this.getOpacity()
                        )
                    )
                }"/>"""
            )
            buffer.append("</g>")
        }
        return buffer.toString()
    }

    private fun drawNumbering(g: Graphics2D, at: AffineTransform) {
        g.color = Color(
            Color.DARK_GRAY.red, Color.DARK_GRAY.green, Color.DARK_GRAY.blue,
            this.getOpacity()
        )
        val n = "$absPos".length
        var p: Pair<Point2D, Point2D>? = null
        var e: Shape? = null
        g.font = Font(g.font.fontName, g.font.style, g.font.size - 4)
        val numberDim = getStringBoundsRectangle2D(g, "0", g.font)
        (this.parent as? SecondaryInteractionDrawing)?.let {
            val pairedCenter = (if (it.residue == this) it.pairedResidue else it.residue).center
            p = pointsFrom(this.center, pairedCenter, -getLineWidth() / 2.0 - radiusConst - radiusConst / 3.0)
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - radiusConst / 3.0,
                    (p as Pair<Point2D, Point2D>).first.y - radiusConst / 3.0,
                    2.0 * radiusConst / 3.0,
                    2.0 * radiusConst / 3.0
                )
            )
            g.fill(e)
            shapesNumbering.add(e as Shape)

            p = pointsFrom(
                this.center,
                pairedCenter,
                -getLineWidth() / 2.0 - radiusConst - radiusConst - numberDim.width / (2.0 * ssDrawing.zoomLevel)
            )
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    numberDim.width / ssDrawing.zoomLevel,
                    numberDim.width / ssDrawing.zoomLevel
                )
            )

        }
        (this.parent as? JunctionDrawing)?.let {
            p = pointsFrom(this.center, it.center, -getLineWidth() / 2.0 - radiusConst - radiusConst / 3.0)
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - 2,
                    (p as Pair<Point2D, Point2D>).first.y - 2,
                    2.0 * radiusConst / 3.0,
                    2.0 * radiusConst / 3.0
                )
            )
            g.fill(e)
            shapesNumbering.add(e as Shape)

            p = pointsFrom(
                this.center,
                it.center,
                -getLineWidth() / 2.0 - radiusConst - radiusConst - numberDim.width / (2.0 * ssDrawing.zoomLevel)
            )
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    numberDim.width / ssDrawing.zoomLevel,
                    numberDim.width / ssDrawing.zoomLevel
                )
            )
        }
        (this.parent as? SingleStrandDrawing)?.let {
            p = pointsFrom(
                Point2D.Double(this.center.x, this.center.y + radiusConst),
                Point2D.Double(this.center.x, this.center.y - radiusConst),
                -getLineWidth() / 2.0 - radiusConst / 3.0
            )
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - 2,
                    (p as Pair<Point2D, Point2D>).first.y - 2,
                    2.0 * radiusConst / 3.0,
                    2.0 * radiusConst / 3.0
                )
            )
            g.fill(e)
            shapesNumbering.add(e as Shape)

            p = pointsFrom(
                Point2D.Double(this.center.x, this.center.y + radiusConst),
                Point2D.Double(this.center.x, this.center.y - radiusConst),
                -getLineWidth() / 2.0 - radiusConst - radiusConst / 2.0 - numberDim.width / (2.0 * ssDrawing.zoomLevel)
            )
            e = at.createTransformedShape(
                Ellipse2D.Double(
                    (p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    (p as Pair<Point2D, Point2D>).first.y - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                    numberDim.width / ssDrawing.zoomLevel,
                    numberDim.width / ssDrawing.zoomLevel
                )
            )
        }

        if (e != null && p != null) {
            val transX = (e!!.bounds2D.width - numberDim.width).toFloat() / 2F
            val transY = (e!!.bounds2D.height + numberDim.height).toFloat() / 2F
            val cp =
                crossProduct(center, Point2D.Double(center.x, center.y - 20), (p as Pair<Point2D, Point2D>).first)
            if (cp >= 0) {
                g.drawString(
                    "$absPos".substring(0, 1),
                    e!!.bounds2D.minX.toFloat() + transX,
                    e!!.bounds2D.minY.toFloat() + transY
                )
                letterNumbering.add(
                    Triple(
                        "$absPos".substring(0, 1),
                        e!!.bounds2D.minX.toFloat() + transX,
                        e!!.bounds2D.minY.toFloat() + transY
                    )
                )
                var i = 1
                while (i < n) {
                    val _p = pointsFrom(
                        Point2D.Double(
                            (p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            (p as Pair<Point2D, Point2D>).first.y
                        ),
                        Point2D.Double(
                            (p as Pair<Point2D, Point2D>).first.x + numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            (p as Pair<Point2D, Point2D>).first.y
                        ),
                        -(2 * (i - 1) + 1) * numberDim.width / (2.0 * ssDrawing.zoomLevel)
                    )
                    e = at.createTransformedShape(
                        Ellipse2D.Double(
                            _p.second.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            _p.second.y - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            numberDim.width / ssDrawing.zoomLevel,
                            numberDim.width / ssDrawing.zoomLevel
                        )
                    )
                    g.drawString(
                        "$absPos".substring(i, i + 1),
                        e!!.bounds2D.minX.toFloat() + transX,
                        e!!.bounds2D.minY.toFloat() + transY
                    )
                    letterNumbering.add(
                        Triple(
                            "$absPos".substring(i, i + 1),
                            e!!.bounds2D.minX.toFloat() + transX,
                            e!!.bounds2D.minY.toFloat() + transY
                        )
                    )
                    i++
                }
            } else {
                g.drawString(
                    "$absPos".substring(n - 1, n),
                    e!!.bounds2D.minX.toFloat() + transX,
                    e!!.bounds2D.minY.toFloat() + transY
                )
                letterNumbering.add(
                    Triple(
                        "$absPos".substring(n - 1, n),
                        e!!.bounds2D.minX.toFloat() + transX,
                        e!!.bounds2D.minY.toFloat() + transY
                    )
                )
                var i = 1
                while (i < n) {
                    val _p = pointsFrom(
                        Point2D.Double(
                            (p as Pair<Point2D, Point2D>).first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            (p as Pair<Point2D, Point2D>).first.y
                        ),
                        Point2D.Double(
                            (p as Pair<Point2D, Point2D>).first.x + numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            (p as Pair<Point2D, Point2D>).first.y
                        ),
                        -(2 * (i - 1) + 1) * numberDim.width / (2.0 * ssDrawing.zoomLevel)
                    )
                    e = at.createTransformedShape(
                        Ellipse2D.Double(
                            _p.first.x - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            _p.first.y - numberDim.width / (2.0 * ssDrawing.zoomLevel),
                            numberDim.width / ssDrawing.zoomLevel,
                            numberDim.width / ssDrawing.zoomLevel
                        )
                    )
                    g.drawString(
                        "$absPos".substring(n - 1 - i, n - i),
                        e!!.bounds2D.minX.toFloat() + transX,
                        e!!.bounds2D.minY.toFloat() + transY
                    )
                    letterNumbering.add(
                        Triple(
                            "$absPos".substring(n - 1 - i, n - i),
                            e!!.bounds2D.minX.toFloat() + transX,
                            e!!.bounds2D.minY.toFloat() + transY
                        )
                    )
                    i++
                }
            }

        }

        g.font = Font(g.font.fontName, g.font.style, g.font.size + 4)

    }

    override fun applyTheme(theme: Theme, checkStopBefore:((DrawingElement) -> Boolean)?, checkStopAfter:((DrawingElement) -> Boolean)?) {
        theme.configurations.forEach { configuration ->
            if (configuration.select(this) && configuration.propertyName.equals(ThemeProperty.fulldetails.toString()) && !configuration.propertyValue(this).equals(this.isFullDetails().toString()))
                //if the parameter fullDetails is modified, the residue becomes updated to force the recomputation of the  interaction symbol
                this.updated = true
        }
        super.applyTheme(theme, checkStopBefore, checkStopAfter)
    }
}

class AShapeDrawing(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueDrawing(parent, 'A', ssDrawing, absPos, SecondaryStructureType.AShape) {
    init {
        this.residueLetter = A(this, ssDrawing, absPos)
    }
}

class UShapeDrawing(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueDrawing(parent, 'U', ssDrawing, absPos, SecondaryStructureType.UShape) {
    init {
        this.residueLetter = U(this, ssDrawing, absPos)
    }
}

class GShapeDrawing(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueDrawing(parent, 'G', ssDrawing, absPos, SecondaryStructureType.GShape) {
    init {
        this.residueLetter = G(this, ssDrawing, absPos)
    }
}

class CShapeDrawing(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueDrawing(parent, 'C', ssDrawing, absPos, SecondaryStructureType.CShape) {
    init {
        this.residueLetter = C(this, ssDrawing, absPos)
    }
}

class XShapeDrawing(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueDrawing(parent, 'X', ssDrawing, absPos, SecondaryStructureType.XShape) {
    init {
        this.residueLetter = X(this, ssDrawing, absPos)
    }
}

abstract class ResidueLetterDrawing(
    parent: ResidueDrawing?,
    ssDrawing: SecondaryStructureDrawing,
    type: SecondaryStructureType,
    absPos: Int
) : DrawingElement(ssDrawing, parent, type.toString(), Location(absPos), type) {

    override val children: List<DrawingElement>
        get() = listOf()

    init {
        this.drawingConfiguration.params[ThemeProperty.color.toString()] = getHTMLColorString(Color.WHITE)
    }

    override val bounds: List<Point2D>
        get() {
            return this.parent!!.bounds
        }
    override val selectionShape: Shape
        get() {
            return (this.parent as ResidueDrawing).circle
        }

    abstract fun asSVG(at: AffineTransform): String

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(this.location.start))
    else
        location.contains(this.location.start)

    /**
     * is full details if its own drawing configuration is true and if its parent (ResidueDrawing) is full details
     */
    //override fun isFullDetails() = this.parent!!.isFullDetails() && super.isFullDetails()
}

class A(parent: ResidueDrawing, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueLetterDrawing(parent, ssDrawing, SecondaryStructureType.A, absPos) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            g.color = this.getColor(selectedDrawings)
            g.drawString(
                this.type.name,
                c.bounds2D.minX.toFloat() + this.ssDrawing.workingSession.ATransX + (this.ssDrawing.workingSession.deltafontx * this.ssDrawing.zoomLevel).toFloat(),
                c.bounds2D.minY.toFloat() + this.ssDrawing.workingSession.ATransY - (this.ssDrawing.workingSession.deltafonty * this.ssDrawing.zoomLevel).toFloat()
            )
        }
    }

    override fun asSVG(at: AffineTransform): String {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            return if (RnartistConfig.exportSVGWithBrowserCompatibility())
                """<text x="${c.bounds2D.centerX}" y="${c.bounds2D.centerY}" text-anchor="middle" dy=".3em" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
            else
                """<text x="${c.bounds2D.minX + this.ssDrawing.workingSession.ATransX}" y="${c.bounds2D.minY + this.ssDrawing.workingSession.ATransY}" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
        }
        return ""
    }
}

class U(parent: ResidueDrawing, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueLetterDrawing(parent, ssDrawing, SecondaryStructureType.U, absPos) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            g.color = this.getColor(selectedDrawings)
            g.drawString(
                this.type.name,
                c.bounds2D.minX.toFloat() + this.ssDrawing.workingSession.UTransX + (this.ssDrawing.workingSession.deltafontx * this.ssDrawing.zoomLevel).toFloat(),
                c.bounds2D.minY.toFloat() + this.ssDrawing.workingSession.UTransY - (this.ssDrawing.workingSession.deltafonty * this.ssDrawing.zoomLevel).toFloat()
            )
        }
    }

    override fun asSVG(at: AffineTransform): String {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            return if (RnartistConfig.exportSVGWithBrowserCompatibility())
                """<text x="${c.bounds2D.centerX}" y="${c.bounds2D.centerY}" text-anchor="middle" dy=".3em" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
            else
                """<text x="${c.bounds2D.minX + this.ssDrawing.workingSession.UTransX}" y="${c.bounds2D.minY + this.ssDrawing.workingSession.UTransY}" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
        }
        return ""
    }

}

class G(parent: ResidueDrawing, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueLetterDrawing(parent, ssDrawing, SecondaryStructureType.G, absPos) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            g.color = this.getColor(selectedDrawings)
            g.drawString(
                this.type.name,
                c.bounds2D.minX.toFloat() + this.ssDrawing.workingSession.GTransX + (this.ssDrawing.workingSession.deltafontx * this.ssDrawing.zoomLevel).toFloat(),
                c.bounds2D.minY.toFloat() + this.ssDrawing.workingSession.GTransY - (this.ssDrawing.workingSession.deltafonty * this.ssDrawing.zoomLevel).toFloat()
            )
        }
    }

    override fun asSVG(at: AffineTransform): String {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            return if (RnartistConfig.exportSVGWithBrowserCompatibility())
                """<text x="${c.bounds2D.centerX}" y="${c.bounds2D.centerY}" text-anchor="middle" dy=".3em" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
            else
                """<text x="${c.bounds2D.minX + this.ssDrawing.workingSession.GTransX}" y="${c.bounds2D.minY + this.ssDrawing.workingSession.GTransY}" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
        }
        return ""
    }
}

class C(parent: ResidueDrawing, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueLetterDrawing(parent, ssDrawing, SecondaryStructureType.C, absPos) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            g.color = this.getColor(selectedDrawings)
            g.drawString(
                this.type.name,
                c.bounds2D.minX.toFloat() + this.ssDrawing.workingSession.CTransX + (this.ssDrawing.workingSession.deltafontx * this.ssDrawing.zoomLevel).toFloat(),
                c.bounds2D.minY.toFloat() + this.ssDrawing.workingSession.CTransY - (this.ssDrawing.workingSession.deltafonty * this.ssDrawing.zoomLevel).toFloat()
            )
        }
    }

    override fun asSVG(at: AffineTransform): String {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            return if (RnartistConfig.exportSVGWithBrowserCompatibility())
                """<text x="${c.bounds2D.centerX}" y="${c.bounds2D.centerY}" text-anchor="middle" dy=".3em" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
            else
                """<text x="${c.bounds2D.minX + this.ssDrawing.workingSession.CTransX}" y="${c.bounds2D.minY + this.ssDrawing.workingSession.CTransY}" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
        }
        return ""
    }

}

class X(parent: ResidueDrawing, ssDrawing: SecondaryStructureDrawing, absPos: Int) :
    ResidueLetterDrawing(parent, ssDrawing, SecondaryStructureType.X, absPos) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            g.color = this.getColor(selectedDrawings)
            g.drawString(
                this.type.name,
                c.bounds2D.minX.toFloat() + this.ssDrawing.workingSession.XTransX + (this.ssDrawing.workingSession.deltafontx * this.ssDrawing.zoomLevel).toFloat(),
                c.bounds2D.minY.toFloat() + this.ssDrawing.workingSession.XTransY - (this.ssDrawing.workingSession.deltafonty * this.ssDrawing.zoomLevel).toFloat()
            )
        }
    }

    override fun asSVG(at: AffineTransform): String {
        if (this.isFullDetails()) {
            val c = at.createTransformedShape((this.parent as ResidueDrawing).circle)
            return if (RnartistConfig.exportSVGWithBrowserCompatibility())
                """<text x="${c.bounds2D.centerX}" y="${c.bounds2D.centerY}" text-anchor="middle" dy=".3em" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
            else
                """<text x="${c.bounds2D.minX + this.ssDrawing.workingSession.XTransX}" y="${c.bounds2D.minY + this.ssDrawing.workingSession.XTransY}" style="fill:${
                    getHTMLColorString(
                        this.getColor()
                    )
                };font-family:${ssDrawing.workingSession.fontName};font-size:${ssDrawing.workingSession.fontSize};">${this.type.name}</text>"""
        }
        return ""
    }
}

class PKnotDrawing(ssDrawing: SecondaryStructureDrawing, private val pknot: Pknot) :
    DrawingElement(ssDrawing, null, pknot.name, pknot.location, SecondaryStructureType.PKnot) {

    override val children: List<DrawingElement>
        get() = listOf(this.helix, *this.tertiaryInteractions.toTypedArray())

    val tertiaryInteractions = mutableListOf<TertiaryInteractionDrawing>()
    lateinit var helix: HelixDrawing

    override val bounds: List<Point2D>
        get() {
            return listOf<Point2D>()
        }

    override fun inside(location: Location) = helix.inside(location) && tertiaryInteractions.all { it.inside(location) }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.isFullDetails())
            for (interaction in this.tertiaryInteractions)
                interaction.draw(g, at, drawingArea, selectedDrawings)
        else {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
            g.color = this.getColor(selectedDrawings)
            val middlePoint1: Point2D
            val middlePoint2: Point2D
            if (tertiaryInteractions.size % 2 == 0) {
                val middleInteraction1 = tertiaryInteractions[tertiaryInteractions.size / 2 - 1]
                val middleInteraction2 = tertiaryInteractions[tertiaryInteractions.size / 2]
                middlePoint1 = centerFrom(middleInteraction1.residue.center, middleInteraction2.residue.center)
                middlePoint2 = centerFrom(middleInteraction1.pairedResidue.center, middleInteraction2.pairedResidue.center)
            } else {
                val middleInteraction = tertiaryInteractions[tertiaryInteractions.size / 2]
                middlePoint1 = middleInteraction.residue.center
                middlePoint2 = middleInteraction.pairedResidue.center
            }
            val c1 = Point2D.Double()
            at.transform(middlePoint1, c1)
            val c2 = Point2D.Double()
            at.transform(middlePoint2, c2)
            val intermediatePoint = Point2D.Double(c2.x, c1.y)
            g.draw(Line2D.Double(c1, intermediatePoint))
            g.draw(Line2D.Double(intermediatePoint, c2))
            g.stroke = previousStroke
        }
    }

    init {
        for (h in ssDrawing.allHelices) {
            if (h.helix == pknot.helix) {
                this.helix = h
                break
            }
        }

        for (interaction in pknot.tertiaryInteractions) {
            this.tertiaryInteractions.add(
                TertiaryInteractionDrawing(
                    this,
                    interaction,
                    ssDrawing
                )
            )
        }
    }
}

abstract class StructuralDomainDrawing(
    ssDrawing: SecondaryStructureDrawing,
    parent: DrawingElement?,
    name: String,
    location: Location,
    type: SecondaryStructureType
) : DrawingElement(ssDrawing, parent, name, location, type)

class HelixDrawing(
    parent: DrawingElement? = null,
    ssDrawing: SecondaryStructureDrawing,
    val helix: Helix,
    start: Point2D,
    end: Point2D
) : StructuralDomainDrawing(ssDrawing, parent, helix.name, helix.location, SecondaryStructureType.Helix) {

    override val children: List<DrawingElement>
        get() = listOf(*this.phosphoBonds.toTypedArray(), *this.secondaryInteractions.toTypedArray())

    var line: Line2D = Line2D.Double(start, end)
    var distanceBetweenPairedResidues =
        0.0 //each helix computes this value before to draw the secondary interactions. Each secondary will use it for its own drawing.
    val secondaryInteractions = mutableListOf<SecondaryInteractionDrawing>()
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()

    val start: Int
        get() = this.location.start

    val end: Int
        get() = this.location.end

    val ends = intArrayOf(this.start, this.start + this.length - 1, this.end - this.length + 1, this.location.end)

    val length: Int
        get() = this.helix.length

    val maxBranchLength: Int
        get() = this.helix.maxBranchLength

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        ends.all { location.contains(ssDrawing.secondaryStructure.rna.mapPosition(it)) }
    else
        ends.all { location.contains(it) }

    override val bounds: List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            if (this.isFullDetails()) {
                val c = centerFrom(line.p1, line.p2)

                var bp = this.secondaryInteractions.first()
                var outsidePoints = pointsFrom(bp.residue.center, bp.pairedResidue.center, -(bp.residue.getLineWidth()+radiusConst*1.1))
                var p1 = getPerpendicular(outsidePoints.first, outsidePoints.first, outsidePoints.second, -(bp.residue.getLineWidth()+radiusConst*1.1))
                var p2 = getPerpendicular(outsidePoints.second, outsidePoints.first, outsidePoints.second, -(bp.residue.getLineWidth()+radiusConst*1.1))
                l.add(p1.toList().maxBy { distance(c,it) })
                l.add(p2.toList().maxBy { distance(c,it) })

                bp = this.secondaryInteractions.last()
                outsidePoints = pointsFrom(bp.residue.center, bp.pairedResidue.center, -(bp.residue.getLineWidth()+radiusConst*1.1))
                p1 = getPerpendicular(outsidePoints.first, outsidePoints.first, outsidePoints.second, -(bp.residue.getLineWidth()+radiusConst*1.1))
                p2 = getPerpendicular(outsidePoints.second, outsidePoints.first, outsidePoints.second, -(bp.residue.getLineWidth()+radiusConst*1.1))
                l.add(p2.toList().maxBy { distance(c,it) })
                l.add(p1.toList().maxBy { distance(c,it) })
            } else {
                val outsidePoints = pointsFrom(line.p1, line.p2, -(radiusConst+this.getLineWidth()))
                val p1 = getPerpendicular(outsidePoints.first, outsidePoints.first, line.p1, radiusConst+this.getLineWidth())
                val p2 = getPerpendicular(outsidePoints.second, outsidePoints.second, line.p2, radiusConst+this.getLineWidth())
                l.addAll(p1.toList())
                l.addAll(p2.toList().reversed())
            }
            return l
        }

    init {
        for (interaction in helix.secondaryInteractions) {
            this.secondaryInteractions.add(
                SecondaryInteractionDrawing(
                    this,
                    interaction,
                    ssDrawing
                )
            )
        }
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)

        if (ssDrawing.quickDraw || !this.isFullDetails() && this.getLineWidth() > 0) {
            g.draw(at.createTransformedShape(this.line))
        } else {
            this.phosphoBonds.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
            distanceBetweenPairedResidues = distance(
                this.secondaryInteractions.first().residue.center,
                this.secondaryInteractions.first().pairedResidue.center
            )
            this.secondaryInteractions.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
        }

        g.stroke = previousStroke
    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val svgBuffer = StringBuffer()
        val p1 = Point2D.Double()
        val p2 = Point2D.Double()
        at.transform(this.line.p1, p1)
        at.transform(this.line.p2, p2)
        if (!this.isFullDetails() && this.getLineWidth() > 0) {
            if (frame.contains(p1) && frame.contains(p2))
                svgBuffer.append(
                    """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                        getHTMLColorString(
                            this.getColor()
                        )
                    }" stroke-width="${this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()}"/>"""
                )
        } else {
            this.phosphoBonds.forEach {
                svgBuffer.append(it.asSVG(at, frame))
            }
            distanceBetweenPairedResidues = distance(
                this.secondaryInteractions.first().residue.center,
                this.secondaryInteractions.first().pairedResidue.center
            )
            this.secondaryInteractions.forEach {
                svgBuffer.append(it.asSVG(at, frame))
            }
        }
        return svgBuffer.toString()
    }
}


class SingleStrandDrawing(ssDrawing: SecondaryStructureDrawing, val ss: SingleStrand, start: Point2D, end: Point2D) :
    StructuralDomainDrawing(ssDrawing, null, ss.name, ss.location, SecondaryStructureType.SingleStrand) {

    override val children: List<DrawingElement>
        get() = listOf(
            *this.phosphoBonds.toTypedArray(),
            *this.ssDrawing.getResiduesFromAbsPositions(*this.getSinglePositions()).toTypedArray()
        )

    var line = Line2D.Double(start, end)
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()

    val start: Int
        get() = this.location.start

    val end: Int
        get() = this.location.end

    val length: Int
        get() = this.ss.length

    var previousBranch: JunctionDrawing? = null
    var nextBranch: JunctionDrawing? = null

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(this.start)) && location.contains(
            ssDrawing.secondaryStructure.rna.mapPosition(
                this.end
            )
        )
    else
        location.contains(this.start) && location.contains(this.end)

    override val bounds: List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            val firstResidue = this.residues.first()
            val lastResidue = this.residues.last()
            if (firstResidue == lastResidue) {
                l.addAll(firstResidue.bounds)
            } else {
                val center = centerFrom(this.line.p1, this.line.p2)
                l.addAll(
                   firstResidue.bounds.sortedBy { distance(it,center) }.subList(2,4)
                )
                l.addAll(
                    lastResidue.bounds.sortedBy { distance(it,center) }.subList(2,4)
                )
            }
            return l
        }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)
        if (ssDrawing.quickDraw) { // a simple line
            g.draw(at.createTransformedShape(this.line))
        } else if (!this.isFullDetails()) { //a line fitted to the display mode of the residues at the end
            val firstResidue =
                if (this.location.start == 1) this.residues.first() else this.ssDrawing.getResiduesFromAbsPositions(this.location.start - 1)
                    .first()
            val lastResidue =
                if (this.location.end == this.ssDrawing.length) this.residues.last() else this.ssDrawing.getResiduesFromAbsPositions(
                    this.location.end + 1
                ).first()
            val center1 = if (this.location.start == 1) firstResidue.center else (
                    if (firstResidue.parent!!.parent!!.isFullDetails()) firstResidue.center else (firstResidue.parent?.parent as HelixDrawing).line.p1
                    )
            val center2 = if (this.location.end == this.ssDrawing.length) lastResidue.center else (
                    if (lastResidue.parent!!.parent!!.isFullDetails()) lastResidue.center else (lastResidue.parent?.parent as HelixDrawing).line.p1
                    )

            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            g.draw(
                at.createTransformedShape(
                    Line2D.Double(
                        if (firstResidue.isFullDetails()) p1 else center1,
                        if (lastResidue.isFullDetails()) p2 else center2
                    )
                )
            )
        } else {
            this.phosphoBonds.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }

            this.residues.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
        }
        g.stroke = previousStroke
    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val buffer = StringBuffer()
        val p1 = Point2D.Double()
        val p2 = Point2D.Double()
        at.transform(this.line.p1, p1)
        at.transform(this.line.p2, p2)
        if (!this.isFullDetails() && frame.contains(p1) && frame.contains(p2)) {
            buffer.append(
                """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${getHTMLColorString(this.getColor())}" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            )
        } else {
            this.phosphoBonds.forEach {
                buffer.append(it.asSVG(at, frame))
            }

            this.residues.forEach {
                buffer.append(it.asSVG(at, frame))
            }
        }
        return buffer.toString()
    }

}

open class JunctionDrawing(
    parent: HelixDrawing,
    ssDrawing: SecondaryStructureDrawing,
    circlesFromBranchSoFar: MutableList<JunctionDrawing>,
    linesFromBranchSoFar: MutableList<HelixDrawing>,
    previousJunction: JunctionDrawing? = null,
    var inId: ConnectorId,
    inPoint: Point2D,
    val inHelix: Helix,
    val junction: Junction
) : StructuralDomainDrawing(
    ssDrawing,
    parent,
    junction.name,
    junction.locationWithoutSecondaries/*the location of the JunctionDrawing is the location without the secondaries, the real location*/,
    SecondaryStructureType.Junction
) {

    private var noOverlapWithLines = true
    private var noOverlapWithCircles = true
    var outHelices = mutableListOf<HelixDrawing>()
    var connectedJunctions = mutableMapOf<ConnectorId, JunctionDrawing>()
    val phosphoBonds = mutableListOf<PhosphodiesterBondDrawing>()
    val connectors: Array<Point2D> =
        Array(ConnectorId.entries.size, { Point2D.Float(0F, 0F) }) //the connector points on the circle
    private val residuesWithClosingBasePairs: List<ResidueDrawing> =
        this.ssDrawing.getResiduesFromAbsPositions(*this.junction.location.positions.toIntArray())

    override val children: List<DrawingElement>
        get() = listOf(*this.phosphoBonds.toTypedArray(), *this.residues.toTypedArray())

    /**
     * The absolute layout defined by the user or computed after the init. Each connector id is recomputed according to the position of the inId for this junction.
     */
    var currentLayout = mutableListOf<ConnectorId>()
        set(value) {
            field = value
            this.update()
        }

    var initialLayout =  mutableListOf<ConnectorId>()

    var radius: Double = 0.0
        set(value) {
            field = value
            this.update()
        }

    var initialRadius: Double = 0.0 //to recompute the currentRadius more precisely
    var radiusRatio = 1.0

    lateinit var center: Point2D
    lateinit var circle: Ellipse2D
    val previousJunction =
        previousJunction //allows to get some info backward. For example, useful for an InnerLoop to check the previous orientation in order to keep it if the inID is .o or .e (instead to choose .n in any case)

    val minX: Double
        get() {
            return this.junctionsFromBranch().minByOrNull { it.circle.bounds.minX }!!.circle.bounds.minX
        }

    val minY: Double
        get() {
            return this.junctionsFromBranch().minByOrNull { it.circle.bounds.minY }!!.circle.bounds.minY
        }

    val maxX: Double
        get() {
            return this.junctionsFromBranch().maxByOrNull { it.circle.bounds.maxX }!!.circle.bounds.maxX
        }

    val maxY: Double
        get() {
            return this.junctionsFromBranch().maxByOrNull { it.circle.bounds.maxY }!!.circle.bounds.maxY
        }

    val junctionType = this.junction.junctionType

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        this.location.ends.all {
            location.contains(
                ssDrawing.secondaryStructure.rna.mapPosition(
                    it
                )
            )
        }
    else
        this.location.ends.all { location.contains(it) }

    override val bounds: List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            if (this.isFullDetails()) {
                this.children.sortedWith(compareBy( //we sort first according to the location and then the residues before the phospho bonds
                    { el -> el.location.start },
                    { el ->
                        when (el) {
                            is ResidueDrawing -> 0
                            is PhosphodiesterBondDrawing -> 1
                            else -> {
                                Integer.MAX_VALUE
                            }
                        }
                    }
                )).forEach { child ->
                    when (child) {
                        is ResidueDrawing -> {
                            l.add(pointsFrom(center, child.center, -radiusConst * 2.0).second)
                        }

                        is PhosphodiesterBondDrawing -> {
                            if (!this.location.contains(child.location.start) && !this.location.contains(child.location.end)) {
                                l.add(child.residue.center)
                                l.add(child.nextResidue.center)
                            } else if (!this.location.contains(child.location.end))
                                l.add(child.nextResidue.center)
                            else if (!this.location.contains(child.location.start))
                                l.add(child.residue.center)
                        }
                    }
                }
            }
            return l
        }

    override val selectionShape: Shape
        get() {
            return if (this.isFullDetails())
                super.selectionShape
            else {
                Ellipse2D.Double(
                    this.center.x - this.radius - radiusConst,
                    this.center.y - this.radius - radiusConst,
                    (this.radius+ radiusConst) * 2.toDouble(),
                    (this.radius+ radiusConst) * 2.toDouble()
                )
            }
        }
    val start: Int
        get() = this.junction.start

    val end: Int
        get() = this.junction.end

    val maxBranchLength: Int
        get() = this.junction.maxBranchLength

    init {
        this.residues =
            this.ssDrawing.getResiduesFromAbsPositions(*this.junction.locationWithoutSecondaries.positions.toIntArray())
        this.connectors[this.inId.value] = inPoint
        //we compute the initial radius according to the junction length and type
        val circumference =
            (this.junction.length.toFloat() - this.junction.junctionType.value * 2).toFloat() * (radiusConst * 2).toFloat() + this.junction.junctionType.value * helixDrawingWidth()
        this.radius = circumference / (2F * Math.PI).toDouble()
        this.initialRadius = circumference / (2F * Math.PI).toDouble()

        var helixRank = 0

        for (k in 1..this.junction.helicesLinked.size + 1) {
            val helix =
                this.junction.helicesLinked[(this.junction.helicesLinked.indexOf(inHelix) + k) % this.junction.helicesLinked.size]
            if (helix == (this.parent as HelixDrawing).helix) {
                circlesFromBranchSoFar.add(this)
                break
            }
            helixRank += 1
            var inPoint: Point2D

            var outId = junctionsBehaviors[this.junctionType]?.let { it(this, helixRank) }

            var nextJunction: Junction? = null
            if (helix.junctionsLinked.first != null && helix.junctionsLinked.first != this.junction) {
                nextJunction = helix.junctionsLinked.first!!
            } else if (helix.junctionsLinked.second != null && helix.junctionsLinked.second != this.junction) {
                nextJunction = helix.junctionsLinked.second!!
            }

            if (outId != null) {
                var from: ConnectorId
                var to: ConnectorId

                from = if (helixRank == 1) {
                    getConnectorId((inId.value + 1) % ConnectorId.entries.size)
                } else {
                    getConnectorId(
                        (junctionsBehaviors[this.junctionType]!!(
                            this,
                            helixRank - 1
                        )!!.value + 1) % ConnectorId.entries.size
                    )
                }

                to = if (helixRank == this.junction.helicesLinked.size - 1) {
                    val newRawValue = if (inId.value - 1 < 0) ConnectorId.entries.size - 1 else inId.value - 1
                    getConnectorId(newRawValue)
                } else {
                    val newRawValue = if (junctionsBehaviors[this.junctionType]!!(
                            this,
                            helixRank + 1
                        )!!.value - 1 < 0
                    ) ConnectorId.entries.size - 1 else junctionsBehaviors[this.junctionType]!!(
                        this,
                        helixRank + 1
                    )!!.value - 1
                    getConnectorId(newRawValue)
                }

                val orientationsToTest =
                    mutableListOf(outId) //we test outId first before to check the remaining orientations in order to avoid any overlap (if possible)

                val afterOrientations = mutableListOf<ConnectorId>()
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

                val beforeOrientations = mutableListOf<ConnectorId>()
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

                    val nextCircumference =
                        (nextJunction!!.length.toFloat() - (nextJunction.junctionType.value) * 2).toFloat() * (radiusConst * 2) + (nextJunction.junctionType.value).toFloat() * helixDrawingWidth()
                    val nextRadius = nextCircumference / (2F * Math.PI)
                    val nextCenter = pointsFrom(
                        this.center,
                        this.connectors[outId.value],
                        -helixDrawingLength(helix) - nextRadius
                    ).second
                    val nextCircle = Ellipse2D.Double(
                        nextCenter.x - nextRadius,
                        nextCenter.y - nextRadius,
                        nextRadius * 2F,
                        nextRadius * 2F
                    )

                    val nextPoints = mutableListOf<Point2D>()
                    nextPoints.add(this.connectors[outId.value])
                    nextPoints.add(inPoint)

                    if (this.noOverlapWithCircles) {
                        outerloop@ for (junctionDrawing in circlesFromBranchSoFar) {
                            if (junctionDrawing.circle.bounds2D.intersects(nextCircle.bounds2D)) {
                                fine = false
                                break@outerloop
                            }
                            val diameter1 = Line2D.Double(
                                junctionDrawing.center.x,
                                junctionDrawing.center.y - junctionDrawing.radius,
                                junctionDrawing.center.x,
                                junctionDrawing.center.y + junctionDrawing.radius
                            )
                            val diameter2 = Line2D.Double(
                                junctionDrawing.center.x - junctionDrawing.radius,
                                junctionDrawing.center.y,
                                junctionDrawing.center.x + junctionDrawing.radius,
                                junctionDrawing.center.y
                            )
                            if (intersects(
                                    nextPoints.first(),
                                    nextPoints.last(),
                                    diameter1.p1,
                                    diameter1.p2
                                ) || intersects(
                                    nextPoints.first(),
                                    nextPoints.last(),
                                    diameter2.p1,
                                    diameter2.p2
                                )
                            ) {
                                fine = false
                                break@outerloop
                            }
                        }
                    }

                    if (fine && this.noOverlapWithLines) {
                        outerloop@ for (helixDrawing in linesFromBranchSoFar) {
                            if (!nextPoints.isEmpty() && intersects(
                                    helixDrawing.line.p1,
                                    helixDrawing.line.p2,
                                    nextPoints.first(),
                                    nextPoints.last()
                                )
                            ) {
                                fine = false
                                break@outerloop
                            }
                            val diameter1 = Line2D.Double(
                                nextCenter.x,
                                nextCenter.y - nextRadius,
                                nextCenter.x,
                                nextCenter.y + nextRadius
                            )
                            val diameter2 = Line2D.Double(
                                nextCenter.x - nextRadius,
                                nextCenter.y,
                                nextCenter.x + nextRadius,
                                nextCenter.y
                            )
                            if (intersects(
                                    helixDrawing.line.p1,
                                    helixDrawing.line.p2,
                                    diameter1.p1,
                                    diameter1.p2
                                ) || intersects(
                                    helixDrawing.line.p1,
                                    helixDrawing.line.p2,
                                    diameter2.p1,
                                    diameter2.p2
                                )
                            ) {
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


                this.currentLayout.add(
                    if (outId!!.value < this.inId.value)
                        ConnectorId.entries
                            .first { it.value == outId.value + ConnectorId.entries.size - this.inId.value }
                    else
                        ConnectorId.entries
                            .first { it.value == Math.abs(outId.value - this.inId.value) }
                )

                val h = HelixDrawing(
                    this,
                    ssDrawing,
                    helix,
                    this.connectors[outId.value],
                    inPoint
                )
                this.outHelices.add(h)

                circlesFromBranchSoFar.add(this)
                linesFromBranchSoFar.add(h)

                this.connectedJunctions[outId] = JunctionDrawing(
                    h,
                    ssDrawing,
                    circlesFromBranchSoFar = circlesFromBranchSoFar,
                    linesFromBranchSoFar = linesFromBranchSoFar,
                    previousJunction = this,
                    inId = oppositeConnectorId(outId),
                    inPoint = inPoint,
                    inHelix = helix,
                    junction = nextJunction
                )
            }
        }
        this.initialLayout.addAll(this.currentLayout)
    }

    /**
     * Update the junction if the radius, the currentLayout (outIds) or the entry point (inId) have been modified
     */
    fun update() {
        this.center = centerFrom(
            this.inId,
            this.connectors[this.inId.value],
            this.radius
        )
        this.circle = Ellipse2D.Double(
            this.center.x - this.radius,
            this.center.y - this.radius,
            this.radius * 2.toDouble(),
            this.radius * 2.toDouble()
        )
        //the (x,y) coords for the connectors
        for (i in 1 until ConnectorId.entries.size) {
            this.connectors[(this.inId.value + i) % ConnectorId.entries.size] =
                rotatePoint(
                    this.connectors[this.inId.value],
                    this.center,
                    i * 360.0 / ConnectorId.entries.size.toDouble()
                )
        }
        if (currentLayout.isNotEmpty()) {
            val sortedHelix =
                this.junction.helicesLinked.sortedBy { it.start - (this.parent as HelixDrawing).helix.start }
            val newConnectedJunctions =
                mutableMapOf<ConnectorId, JunctionDrawing>() //we need to store the new connections in a temp dict otherwise the update of a connection could remove an old connection stored and not already checked.
            var helixRank = 0
            for (helix in sortedHelix) {
                if (helix != (this.parent as HelixDrawing).helix) {
                    helixRank += 1
                    var inPoint: Point2D?
                    val outId = ConnectorId.entries
                        .firstOrNull { it.value == (this.inId.value + this.currentLayout[helixRank - 1].value) % ConnectorId.entries.size }

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
                            for (h in this.outHelices) {
                                if (h.helix == helix) {
                                    h.line = Line2D.Double(this.connectors[outId.value], inPoint)
                                    break
                                }
                            }

                            connectedJunction.value.setEntryPoint(
                                oppositeConnectorId(
                                    outId
                                ), inPoint
                            )
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
                            for (h in this.outHelices) {
                                if (h.helix == helix) {
                                    h.line = Line2D.Double(this.connectors[outId.value], inPoint)
                                    break
                                }
                            }

                            connectedJunction.value.setEntryPoint(
                                getConnectorId(
                                    (outId.value + ConnectorId.entries.size / 2) % ConnectorId.entries.size
                                ), inPoint
                            )
                        }

                    }
                }
            }
            //last step, we substitute the connected circles for the new ones
            this.connectedJunctions = newConnectedJunctions
        }
    }

    fun applyLayout(layout: Layout) {
        layout.configurations.forEach { configuration ->
            if (configuration.selector(this)) {
                when (configuration.propertyName) {
                    LayoutProperty.out_ids.toString() -> {
                        val out_ids = configuration.propertyValue!!
                        var new_layout: String?
                        if (out_ids.startsWith("+")) {
                            var cycles = out_ids.split("+").last().toInt()
                            new_layout = clockwise()
                            while (cycles > 0) {
                                new_layout?.let { layout ->
                                    val connectors = layout.split(" ").map {
                                        ConnectorId.valueOf(it)
                                    }
                                    currentLayout = connectors.toMutableList()
                                    ssDrawing.computeResidues(this)
                                    new_layout = clockwise()
                                }
                                cycles -= 1
                            }
                        } else if (out_ids.startsWith("-")) {
                            var cycles = out_ids.split("-").last().toInt()
                            new_layout = anticlockwise()
                            while (cycles > 0) {
                                new_layout?.let { layout ->
                                    val connectors = layout.split(" ").map {
                                        ConnectorId.valueOf(it)
                                    }
                                    currentLayout = connectors.toMutableList()
                                    ssDrawing.computeResidues(this)
                                    new_layout = anticlockwise()
                                }
                                cycles -= 1
                            }
                        } else {
                            val connectors = out_ids.split(" ").map {
                                ConnectorId.valueOf(it)
                            }
                            currentLayout = connectors.toMutableList()
                            ssDrawing.computeResidues(this)
                        }

                    }

                    LayoutProperty.radius.toString() -> {
                        radius = configuration.propertyValue!!.toDouble()
                        radiusRatio = radius/initialRadius
                        ssDrawing.computeResidues(this)
                    }

                    LayoutProperty.rollBack.toString() -> { // no parameters means
                        this.clearLayout()
                    }
                }
            }
        }
    }

    fun clearLayout() {
        if (radius != this.initialRadius || !currentLayout.containsAll(this.initialLayout)) {
            this.radius = this.initialRadius
            this.radiusRatio = 1.0
            this.currentLayout = this.initialLayout.toMutableList()
            this.ssDrawing.computeResidues(this)
        }
    }

    fun clockwise(): String? {
        val currentLayout = this.currentLayout
        if (currentLayout.last() != ConnectorId.sse) {
            val newLayout = mutableListOf<ConnectorId>()
            currentLayout.forEach {
                newLayout.add(getConnectorId(it.value + 1))
            }
            return newLayout.map { it.toString() }.joinToString(separator = " ")
        } else
            return null
    }

    fun anticlockwise(): String? {
        val currentLayout = this.currentLayout
        if (currentLayout.first() != ConnectorId.ssw) {
            val newLayout = mutableListOf<ConnectorId>()
            currentLayout.forEach {
                newLayout.add(getConnectorId(it.value - 1))
            }
            return newLayout.map { it.toString() }.joinToString(separator = " ")
        } else
            return null
    }


    //the previous JunctionCircle has modified its link with this one.
    private fun setEntryPoint(inId: ConnectorId, inPoint: Point2D) {
        this.inId = inId
        this.connectors[this.inId.value] = inPoint
        this.update()
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
        val helices = mutableSetOf<HelixDrawing>()
        helices.add(parent as HelixDrawing)
        helices.addAll(this.outHelices)
        for ((_, j) in this.connectedJunctions) {
            helices.addAll(j.helicesFromBranch())
        }
        return helices.toList()
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)

        if (ssDrawing.quickDraw || !this.isFullDetails() && this.getLineWidth() > 0) {
            g.draw(at.createTransformedShape(this.circle))
        } else {
            this.phosphoBonds.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
            this.residues.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
        }

        g.stroke = previousStroke

    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val buffer = StringBuffer()
        val _c = at.createTransformedShape(this.circle)
        if (!this.isFullDetails() && this.getLineWidth() > 0 && frame.contains(_c.bounds2D)) {
            buffer.append(
                """<circle cx="${_c.bounds.centerX}" cy="${_c.bounds.centerY}" r="${_c.bounds.width / 2}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()}" fill="none"/>"""
            )
        } else {
            this.phosphoBonds.forEach {
                buffer.append(it.asSVG(at, frame))
            }

            this.residues.forEach {
                buffer.append(it.asSVG(at, frame))
            }
        }
        return buffer.toString()
    }

}

class Branch(
    parent: HelixDrawing,
    ssDrawing: SecondaryStructureDrawing,
    circlesFromBranchSoFar: MutableList<JunctionDrawing>,
    linesFromBranchSoFar: MutableList<HelixDrawing>,
    inId: ConnectorId,
    inPoint: Point2D,
    inHelix: Helix,
    junction: Junction
) : JunctionDrawing(
    parent,
    ssDrawing,
    circlesFromBranchSoFar,
    linesFromBranchSoFar,
    null,
    inId,
    inPoint,
    inHelix,
    junction
) {

}

abstract class LWSymbolDrawing(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    name: String,
    location: Location,
    val inTertiaries: Boolean
) : DrawingElement(ssDrawing, parent, name, location, SecondaryStructureType.LWSymbol) {

    override val children: List<DrawingElement>
        get() = listOf()

    lateinit var shape: Shape

    abstract fun setShape(p1: Point2D, p2: Point2D)

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.ends.all { location.contains(ssDrawing.secondaryStructure.rna.mapPosition(it)) }
    else
        location.ends.all { location.contains(it) }

    override val bounds: List<Point2D>
        get() {
            val points = mutableListOf<Point2D>()
            val pathIterator = shape.getPathIterator(AffineTransform())

            val coords = FloatArray(6)
            while (!pathIterator.isDone()) {
                when (pathIterator.currentSegment(coords)) {
                    PathIterator.SEG_LINETO -> {
                        points.add(Point2D.Double(coords[0].toDouble(), coords[1].toDouble()))
                    }

                    PathIterator.SEG_MOVETO -> {
                        points.add(Point2D.Double(coords[0].toDouble(), coords[1].toDouble()))
                    }
                }
                pathIterator.next()
            }
            return points
        }

    abstract fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String

}

abstract class WC(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    name: String,
    location: Location,
    inTertiaries: Boolean
) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    override fun setShape(p1: Point2D, p2: Point2D) {
        val symbolWidth = distance(p1, p2)
        val (start_1, start_2) = getPerpendicular(p1, p1, p2, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(p2, p1, p2, symbolWidth / 2.0)

        val squarre = GeneralPath().apply {
            moveTo(start_1.x, start_1.y)
            lineTo(end_1.x, end_1.y)
            lineTo(end_2.x, end_2.y)
            lineTo(start_2.x, start_2.y)
            lineTo(start_1.x, start_1.y)
            closePath()
        }

        val centerX = squarre.bounds2D.centerX
        val centerY = squarre.bounds2D.centerY

        this.shape = Ellipse2D.Double(
            centerX - symbolWidth / 2.0,
            centerY - symbolWidth / 2.0,
            symbolWidth,
            symbolWidth
        )
    }

    override fun toString() = "Circle"
}

class CisWC(parent: DrawingElement?, ssDrawing: SecondaryStructureDrawing, location: Location, inTertiaries: Boolean) :
    WC(parent, ssDrawing, "cisWC", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val circle = at.createTransformedShape(this.shape)
        if (frame.contains(circle.bounds2D))
            return """<circle cx="${circle.bounds2D.centerX}" cy="${circle.bounds2D.centerY}" r="${circle.bounds2D.width / 2.0}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="$strokeWidth" fill="${getHTMLColorString(color)}"/>"""
        return ""
    }
}

class TransWC(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean
) : WC(parent, ssDrawing, "transWC", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val circle = at.createTransformedShape(this.shape)
        if (frame.contains(circle.bounds2D))
            return """<circle cx="${circle.bounds2D.centerX}" cy="${circle.bounds2D.centerY}" r="${circle.bounds2D.width / 2.0}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="$strokeWidth" fill="none"/>"""
        return ""
    }

}

abstract class LeftSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    name: String,
    location: Location,
    inTertiaries: Boolean
) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    protected lateinit var p1: Point2D
    protected lateinit var p2: Point2D
    protected lateinit var p3: Point2D

    override fun setShape(p1: Point2D, p2: Point2D) {
        val symbolWidth = distance(p1, p2)
        val (end_1, end_2) = getPerpendicular(p2, p1, p2, symbolWidth / 2.0)
        val triangle = GeneralPath()
        this.p1 = p1
        triangle.moveTo(p1.x, p1.y)
        this.p2 = end_1
        triangle.lineTo(end_1.x, end_1.y)
        this.p3 = end_2
        triangle.lineTo(end_2.x, end_2.y)
        triangle.lineTo(p1.x, p1.y)
        triangle.closePath()
        this.shape = triangle
    }

    override fun toString() = "Triangle"
}

abstract class RightSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    name: String,
    location: Location,
    inTertiaries: Boolean = false
) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    protected lateinit var p1: Point2D
    protected lateinit var p2: Point2D
    protected lateinit var p3: Point2D

    override fun setShape(p1: Point2D, p2: Point2D) {
        val symbolWidth = distance(p1, p2)
        val (start_1, start_2) = getPerpendicular(p1, p1, p2, symbolWidth / 2.0)
        val triangle = GeneralPath()
        this.p1 = start_1
        triangle.moveTo(start_1.x, start_1.y)
        this.p2 = start_2
        triangle.lineTo(start_2.x, start_2.y)
        this.p3 = p2
        triangle.lineTo(p2.x, p2.y)
        triangle.lineTo(start_1.x, start_1.y)
        triangle.closePath()
        this.shape = triangle
    }

    override fun toString() = "Triangle"

}

class CisRightSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : RightSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        this.shape.let {
            g.fill(at.createTransformedShape(this.shape))
        }
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="${strokeWidth}" fill="${getHTMLColorString(color)}"/>"""
        return ""
    }

}

class CisLeftSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : LeftSugar(parent, ssDrawing, "cisSugar", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="${strokeWidth}" fill="${getHTMLColorString(color)}"/>"""
        return ""
    }

}

class TransRightSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : RightSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="$strokeWidth" fill="none"/>"""
        return ""
    }

}

class TransLeftSugar(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : LeftSugar(parent, ssDrawing, "transSugar", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="$strokeWidth" fill="none"/>"""
        return ""
    }

}

abstract class Hoogsteen(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    name: String,
    location: Location,
    inTertiaries: Boolean = false
) : LWSymbolDrawing(parent, ssDrawing, name, location, inTertiaries) {

    protected lateinit var p1: Point2D
    protected lateinit var p2: Point2D
    protected lateinit var p3: Point2D
    protected lateinit var p4: Point2D

    override fun setShape(p1: Point2D, p2: Point2D) {
        val symbolWidth = distance(p1, p2)
        val (start_1, start_2) = getPerpendicular(p1, p1, p2, symbolWidth / 2.0)
        val (end_1, end_2) = getPerpendicular(p2, p1, p2, symbolWidth / 2.0)
        val squarre = GeneralPath()
        this.p1 = start_1
        squarre.moveTo(start_1.x, start_1.y)
        this.p2 = end_1
        squarre.lineTo(end_1.x, end_1.y)
        this.p3 = end_2
        squarre.lineTo(end_2.x, end_2.y)
        this.p4 = start_2
        squarre.lineTo(start_2.x, start_2.y)
        squarre.lineTo(start_1.x, start_1.y)
        squarre.closePath()
        this.shape = squarre
    }

    override fun toString() = "Squarre"

}

class CisHoogsteen(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : Hoogsteen(parent, ssDrawing, "cisHoogsteen", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.fill(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        val _p4 = Point2D.Double()
        at.transform(p4, _p4)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3) && frame.contains(_p4))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}, ${_p4.x} ${_p4.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="$strokeWidth" fill="${getHTMLColorString(color)}"/>"""
        return ""
    }

}

class TransHoogsteen(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false
) : Hoogsteen(parent, ssDrawing, "transHoogsteen", location, inTertiaries) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val _p1 = Point2D.Double()
        at.transform(p1, _p1)
        val _p2 = Point2D.Double()
        at.transform(p2, _p2)
        val _p3 = Point2D.Double()
        at.transform(p3, _p3)
        val _p4 = Point2D.Double()
        at.transform(p4, _p4)
        if (frame.contains(_p1) && frame.contains(_p2) && frame.contains(_p3) && frame.contains(_p4))
            return """<polygon points="${_p1.x} ${_p1.y}, ${_p2.x} ${_p2.y}, ${_p3.x} ${_p3.y}, ${_p4.x} ${_p4.y}" stroke="${
                getHTMLColorString(
                    color
                )
            }" stroke-width="${strokeWidth}" fill="none"/>"""
        return ""
    }


}

enum class VSymbolPos {
    BOTTOM, MIDDLE, TOP
}

class LWLine(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    inTertiaries: Boolean = false,
    val vpos: VSymbolPos = VSymbolPos.MIDDLE
) : LWSymbolDrawing(parent, ssDrawing, "Line", location, inTertiaries) {

    override fun setShape(p1: Point2D, p2: Point2D) {
        val distance = distance(p1, p2)
        val symbolWidth = distance
        when (this.vpos) {
            VSymbolPos.TOP -> {
                val (p1_1, _) = getPerpendicular(p1, p1, p2, symbolWidth / 6.0)
                val (p2_1, _) = getPerpendicular(p2, p1, p2, symbolWidth / 6.0)
                this.shape = Line2D.Double(p1_1, p2_1)
            }

            VSymbolPos.BOTTOM -> {
                val (_, p1_2) = getPerpendicular(p1, p1, p2, symbolWidth / 6.0)
                val (_, p2_2) = getPerpendicular(p2, p1, p2, symbolWidth / 6.0)
                this.shape = Line2D.Double(p1_2, p2_2)
            }

            else -> this.shape = Line2D.Double(p1, p2)
        }

    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) = this.shape.let {
        g.draw(at.createTransformedShape(this.shape))
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D, strokeWidth: Float, color: Color): String {
        val p1 = Point2D.Double()
        val p2 = Point2D.Double()
        at.transform((this.shape as Line2D).p1, p1)
        at.transform((this.shape as Line2D).p2, p2)
        if (frame.contains(p1) && frame.contains(p2))
            return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${getHTMLColorString(color)}" stroke-width="$strokeWidth"/>"""
        return ""
    }

    override fun toString() = "Line"

}

abstract class BaseBaseInteractionDrawing(
    parent: DrawingElement?,
    val interaction: BasePair,
    ssDrawing: SecondaryStructureDrawing,
    type: SecondaryStructureType
) : DrawingElement(ssDrawing, parent, interaction.toString(), interaction.location, type) {

    override val children: List<DrawingElement>
        get() = listOf(this.residue, this.pairedResidue, this.interactionSymbol)

    protected var p1: Point2D? = null
    protected var p2: Point2D? = null
    var interactionSymbol = InteractionSymbolDrawing(this, interaction, ssDrawing)

    val residue: ResidueDrawing
    val pairedResidue: ResidueDrawing

    val start: Int
        get() {
            return this.location.start
        }

    val end: Int
        get() {
            return this.location.end
        }

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(this.start)) && location.contains(
            ssDrawing.secondaryStructure.rna.mapPosition(
                this.end
            )
        )
    else
        location.contains(this.start) && location.contains(this.end)

    val isCanonical: Boolean
        get() {
            return this.interaction.edge5 == Edge.WC && this.interaction.edge3 == Edge.WC && this.interaction.orientation == Orientation.cis && (
                    this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
                        .first().type == SecondaryStructureType.AShape && this.ssDrawing.getResiduesFromAbsPositions(
                        this.interaction.end
                    ).first().type == SecondaryStructureType.UShape ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
                                .first().type == SecondaryStructureType.UShape && this.ssDrawing.getResiduesFromAbsPositions(
                        this.interaction.end
                    ).first().type == SecondaryStructureType.AShape ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
                                .first().type == SecondaryStructureType.GShape && this.ssDrawing.getResiduesFromAbsPositions(
                        this.interaction.end
                    ).first().type == SecondaryStructureType.CShape ||
                            this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
                                .first().type == SecondaryStructureType.CShape && this.ssDrawing.getResiduesFromAbsPositions(
                        this.interaction.end
                    ).first().type == SecondaryStructureType.GShape
                    )
        }

    val isDoublePaired: Boolean
        get() = this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
            .first().type == SecondaryStructureType.GShape && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end)
            .first().type == SecondaryStructureType.CShape ||
                this.ssDrawing.getResiduesFromAbsPositions(this.interaction.start)
                    .first().type == SecondaryStructureType.CShape && this.ssDrawing.getResiduesFromAbsPositions(this.interaction.end)
            .first().type == SecondaryStructureType.GShape

    val isSingleHBond: Boolean
        get() = this.interaction.edge5 == Edge.SingleHBond && this.interaction.edge3 == Edge.SingleHBond


    override val bounds:List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            val outsidePoints = pointsFrom(this.residue.center, this.pairedResidue.center, -(this.residue.getLineWidth()+radiusConst*1.1))
            val p1 = getPerpendicular(outsidePoints.first, outsidePoints.first, outsidePoints.second, -(this.residue.getLineWidth()+radiusConst*1.1))
            val p2 = getPerpendicular(outsidePoints.second, outsidePoints.first, outsidePoints.second, -(this.residue.getLineWidth()+radiusConst*1.1))
            l.addAll(p1.toList())
            l.addAll(p2.toList().reversed())
            return l
        }

    init {
        this.residue = ssDrawing.getResiduesFromAbsPositions(this.start).first()
        this.pairedResidue = ssDrawing.getResiduesFromAbsPositions(this.end).first()
    }

    protected fun generateSingleSymbol(
        location: Location,
        inTertiaries: Boolean = false,
        edge: Edge,
        orientation: Orientation,
        right: Boolean = true
    ): LWSymbolDrawing {
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
                    Orientation.cis -> if (right) CisRightSugar(
                        this,
                        this.ssDrawing,
                        location,
                        inTertiaries
                    ) else CisLeftSugar(this, this.ssDrawing, location, inTertiaries)

                    Orientation.trans -> if (right) TransRightSugar(
                        this,
                        this.ssDrawing,
                        location,
                        inTertiaries
                    ) else TransLeftSugar(this, this.ssDrawing, location, inTertiaries)

                    else -> if (right) CisRightSugar(this, this.ssDrawing, location, inTertiaries) else CisLeftSugar(
                        this,
                        this.ssDrawing,
                        location,
                        inTertiaries
                    )
                }
            }

            else -> { //if edge unknown
                LWLine(this, this.ssDrawing, location, inTertiaries)
            }
        }
    }

    override fun toString() = this.interaction.toString()
}

class SecondaryInteractionDrawing(
    parent: DrawingElement?,
    interaction: BasePair,
    ssDrawing: SecondaryStructureDrawing
) : BaseBaseInteractionDrawing(parent, interaction, ssDrawing, SecondaryStructureType.SecondaryInteraction) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (residue.updated) {//the paired residue is de facto updated too
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center

            val shift =
                radiusConst + this.getLineShift() + this.residue.getLineWidth() / 2.0 + this.getLineWidth() / 2.0
            if ((parent as HelixDrawing).distanceBetweenPairedResidues > 2 * shift) {
                var points = pointsFrom(
                    center1,
                    center2,
                    shift
                )
                this.p1 = if (this.residue.isFullDetails()) points.first else center1
                this.p2 = if (this.pairedResidue.isFullDetails()) points.second else center2
                this.interactionSymbol.defaultSymbol = LWLine(this, this.ssDrawing, this.location, false)
                this.interactionSymbol.defaultSymbol!!.setShape(this.p1 as Point2D, this.p2 as Point2D)

                //now the LW symbols
                this.interactionSymbol.lwSymbols.clear()
                if (this.isCanonical) {
                    if (isDoublePaired) {
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                this.location,
                                false,
                                vpos = VSymbolPos.TOP
                            )
                        )
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                this.location,
                                false,
                                vpos = VSymbolPos.BOTTOM
                            )
                        )
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
                        this.interactionSymbol.lwSymbols[1].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    } else {
                        this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    }
                } else {
                    val distance = distance(this.p1 as Point2D, this.p2 as Point2D)
                    val symbolWidth = distance / 3.0

                    if (this.interaction.edge5 == this.interaction.edge3) { //single central symbol
                        //+++++left symbol
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                Location(this.location.start),
                                false
                            )
                        )
                        //++++++middle symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                this.location,
                                false,
                                this.interaction.edge5,
                                this.interaction.orientation
                            )
                        )
                        //+++++right symbol
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                Location(this.location.end),
                                false
                            )
                        )
                        val (p1_inner, p2_inner) = pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth / 2.0)
                        this.interactionSymbol.lwSymbols[0].setShape(p1!!, p1_inner)
                        this.interactionSymbol.lwSymbols[2].setShape(p2_inner, p2!!)
                        this.interactionSymbol.lwSymbols[1].setShape(p1_inner, p2_inner)
                    } else {
                        //+++++left symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                Location(this.location.start),
                                false,
                                this.interaction.edge5,
                                this.interaction.orientation,
                                right = false
                            )
                        )
                        //++++++middle symbol
                        this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                        //+++++right symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                Location(this.location.end),
                                false,
                                this.interaction.edge3,
                                this.interaction.orientation
                            )
                        )
                        val (p1_inner, p2_inner) = pointsFrom(
                            p1 as Point2D,
                            p2 as Point2D,
                            symbolWidth + symbolWidth / 4.0
                        )
                        this.interactionSymbol.lwSymbols[0].setShape(p1!!, p1_inner)
                        this.interactionSymbol.lwSymbols[2].setShape(p2_inner, p2!!)
                        this.interactionSymbol.lwSymbols[1].setShape(p1_inner, p2_inner)
                    }
                }

            }
            this.residue.updated = false
            this.pairedResidue.updated = false
        }

        if (this.isFullDetails()) {
            this.interactionSymbol.draw(g, at, drawingArea, selectedDrawings)
            this.residues.forEach {
                it.draw(g, at, drawingArea, selectedDrawings)
            }
        }
    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (residue.updated) {//the paired residue is de facto updated too
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center

            val shift =
                radiusConst + this.getLineShift() + this.residue.getLineWidth() / 2.0 + this.getLineWidth() / 2.0
            if ((parent as HelixDrawing).distanceBetweenPairedResidues > 2 * shift) {
                val points = pointsFrom(
                    center1,
                    center2,
                    shift
                )
                this.p1 = if (this.residue.isFullDetails()) points.first else center1
                this.p2 = if (this.pairedResidue.isFullDetails()) points.second else center2
                this.interactionSymbol.defaultSymbol = LWLine(this, this.ssDrawing, this.location, false)
                this.interactionSymbol.defaultSymbol!!.setShape(this.p1 as Point2D, this.p2 as Point2D)

                //now the LW symbols
                this.interactionSymbol.lwSymbols.clear()
                if (this.isCanonical) {
                    if (isDoublePaired) {
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                this.location,
                                false,
                                vpos = VSymbolPos.TOP
                            )
                        )
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                this.location,
                                false,
                                vpos = VSymbolPos.BOTTOM
                            )
                        )
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
                        this.interactionSymbol.lwSymbols[1].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    } else {
                        this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    }
                } else {
                    val distance = distance(this.p1 as Point2D, this.p2 as Point2D)
                    val symbolWidth = distance / 3.0

                    if (this.interaction.edge5 == this.interaction.edge3) { //single central symbol
                        //+++++left symbol
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                Location(this.location.start),
                                false
                            )
                        )
                        //++++++middle symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                this.location,
                                false,
                                this.interaction.edge5,
                                this.interaction.orientation
                            )
                        )
                        //+++++right symbol
                        this.interactionSymbol.lwSymbols.add(
                            LWLine(
                                this,
                                this.ssDrawing,
                                Location(this.location.end),
                                false
                            )
                        )
                        val (p1_inner, p2_inner) = pointsFrom(p1 as Point2D, p2 as Point2D, symbolWidth / 2.0)
                        this.interactionSymbol.lwSymbols[0].setShape(p1!!, p1_inner)
                        this.interactionSymbol.lwSymbols[2].setShape(p2_inner, p2!!)
                        this.interactionSymbol.lwSymbols[1].setShape(p1_inner, p2_inner)
                    } else {
                        //+++++left symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                Location(this.location.start),
                                false,
                                this.interaction.edge5,
                                this.interaction.orientation,
                                right = false
                            )
                        )
                        //++++++middle symbol
                        this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, false))
                        //+++++right symbol
                        this.interactionSymbol.lwSymbols.add(
                            this.generateSingleSymbol(
                                Location(this.location.end),
                                false,
                                this.interaction.edge3,
                                this.interaction.orientation
                            )
                        )
                        val (p1_inner, p2_inner) = pointsFrom(
                            p1 as Point2D,
                            p2 as Point2D,
                            symbolWidth + symbolWidth / 4.0
                        )
                        this.interactionSymbol.lwSymbols[0].setShape(p1!!, p1_inner)
                        this.interactionSymbol.lwSymbols[2].setShape(p2_inner, p2!!)
                        this.interactionSymbol.lwSymbols[1].setShape(p1_inner, p2_inner)
                    }
                }

            }
            this.residue.updated = false
            this.pairedResidue.updated = false
        }
        val buffer = StringBuffer()
        if (this.isFullDetails()) {
            buffer.append(this.interactionSymbol.asSVG(at, frame))
            this.residues.forEach {
                buffer.append(it.asSVG(at, frame))
            }
        }
        return buffer.toString()
    }

    /**
     * is full details if its own drawing configuration is true and if its parent (can be null, depending on the type of phosphobond) is full details
     */
    //override fun isFullDetails() = this.parent?.isFullDetails() ?: true && super.isFullDetails()
}

class InteractionSymbolDrawing(
    parent: BaseBaseInteractionDrawing,
    val interaction: BasePair,
    ssDrawing: SecondaryStructureDrawing
) : DrawingElement(
    ssDrawing,
    parent,
    interaction.toString(),
    interaction.location,
    SecondaryStructureType.InteractionSymbol
) {

    override val children: List<DrawingElement>
        get() = this.defaultSymbol?.let { listOf(it, *this.lwSymbols.toTypedArray()) }
            ?: run { listOf(*this.lwSymbols.toTypedArray()) }

    var defaultSymbol: LWLine? = null
    var lwSymbols = mutableListOf<LWSymbolDrawing>()

    override val bounds:List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            val center1 = (this.parent as BaseBaseInteractionDrawing).residue.center
            val center2 = (this.parent as BaseBaseInteractionDrawing).pairedResidue.center
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            l.addAll(getPerpendicular(p1, center1, center2, radiusConst * 0.5).toList())
            l.addAll(getPerpendicular(p2, center1, center2, radiusConst * 0.5).toList().reversed())
            return l
        }

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(interaction.start)) && location.contains(
            ssDrawing.secondaryStructure.rna.mapPosition(interaction.end)
        )
    else
        location.contains(interaction.start) && location.contains(interaction.end)

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.getLineWidth() > 0) {
            if (this.isFullDetails()) {
                this.lwSymbols.forEach { lwSymbol ->
                    val _previousColor = g.color
                    val _previousStroke = g.stroke
                    g.stroke =
                        BasicStroke(
                            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(),
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                        )
                    g.color = this.getColor(selectedDrawings)
                    lwSymbol.draw(g, at, drawingArea, selectedDrawings)
                    g.color = _previousColor
                    g.stroke = _previousStroke
                }
            } else {
                this.defaultSymbol?.let {
                    val _previousColor = g.color
                    val _previousStroke = g.stroke
                    g.stroke =
                        BasicStroke(
                            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(),
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                        )
                    g.color = this.getColor(selectedDrawings)
                    defaultSymbol?.draw(g, at, drawingArea, selectedDrawings)
                    g.color = _previousColor
                    g.stroke = _previousStroke
                }
            }
        }
    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val buffer = StringBuffer()
        if (this.getLineWidth() > 0) {
            if (this.isFullDetails()) {
                this.lwSymbols.forEach { lwSymbol ->
                    buffer.append(
                        lwSymbol.asSVG(
                            at,
                            frame,
                            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(),
                            this.getColor()
                        )
                    )
                }
            } else {
                this.defaultSymbol?.let {
                    buffer.append(
                        it.asSVG(
                            at,
                            frame,
                            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(),
                            this.getColor()
                        )
                    )
                }
            }
        }
        return buffer.toString()
    }

}

class TertiaryInteractionDrawing(
    parent: PKnotDrawing? = null,
    interaction: BasePair,
    ssDrawing: SecondaryStructureDrawing
) : BaseBaseInteractionDrawing(parent, interaction, ssDrawing, SecondaryStructureType.TertiaryInteraction) {

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        if (this.residue.absPos in ssDrawing.residuesUpdated || this.pairedResidue.absPos in ssDrawing.residuesUpdated) {
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center
            val shift = radiusConst + this.residue.getLineWidth().toDouble() / 2.0
            if (distance(center1, center2) > 2 * shift) {
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    shift
                )
                this.interactionSymbol.defaultSymbol = LWLine(this, this.ssDrawing, this.location, true)
                this.interactionSymbol.defaultSymbol!!.setShape(p1, p2)

                //LW Symbols now
                this.interactionSymbol.lwSymbols.clear()
                //+++++left symbol
                this.interactionSymbol.lwSymbols.add(
                    this.generateSingleSymbol(
                        Location(this.location.start),
                        true,
                        this.interaction.edge5,
                        this.interaction.orientation,
                        right = false
                    )
                )
                //++++++middle symbol
                this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, true))
                //+++++right symbol
                this.interactionSymbol.lwSymbols.add(
                    this.generateSingleSymbol(
                        Location(this.location.end),
                        true,
                        this.interaction.edge3,
                        this.interaction.orientation
                    )
                )

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

                this.p1?.let {
                    this.p2?.let {
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
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

                this.p1?.let {
                    this.p2?.let {
                        this.interactionSymbol.lwSymbols[2].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    }
                }

                //+++++ central line linking the two symbols
                this.interactionSymbol.lwSymbols[1].setShape(forLine_1, forLine_2)

            }

        }

        if (this.isFullDetails() && (this.ssDrawing.workingSession.locationDrawn.contains(this.residue.absPos) && this.ssDrawing.workingSession.locationDrawn.contains(
                this.pairedResidue.absPos
            ))
        ) {
            val previousColor = g.color
            g.color = getColor(selectedDrawings)
            this.interactionSymbol.draw(g, at, drawingArea, selectedDrawings)
            g.color = previousColor
        }

    }

    fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.residue.absPos in ssDrawing.residuesUpdated || this.pairedResidue.absPos in ssDrawing.residuesUpdated) {
            val center1 = this.residue.center
            val center2 = this.pairedResidue.center
            val shift = radiusConst + this.residue.getLineWidth().toDouble() / 2.0
            if (distance(center1, center2) > 2 * shift) {
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    shift
                )
                this.interactionSymbol.defaultSymbol = LWLine(this, this.ssDrawing, this.location, true)
                this.interactionSymbol.defaultSymbol!!.setShape(p1, p2)

                //LW Symbols now
                this.interactionSymbol.lwSymbols.clear()
                //+++++left symbol
                this.interactionSymbol.lwSymbols.add(
                    this.generateSingleSymbol(
                        Location(this.location.start),
                        true,
                        this.interaction.edge5,
                        this.interaction.orientation,
                        right = false
                    )
                )
                //++++++middle symbol
                this.interactionSymbol.lwSymbols.add(LWLine(this, this.ssDrawing, this.location, true))
                //+++++right symbol
                this.interactionSymbol.lwSymbols.add(
                    this.generateSingleSymbol(
                        Location(this.location.end),
                        true,
                        this.interaction.edge3,
                        this.interaction.orientation
                    )
                )

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

                this.p1?.let {
                    this.p2?.let {
                        this.interactionSymbol.lwSymbols[0].setShape(this.p1 as Point2D, this.p2 as Point2D)
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

                this.p1?.let {
                    this.p2?.let {
                        this.interactionSymbol.lwSymbols[2].setShape(this.p1 as Point2D, this.p2 as Point2D)
                    }
                }

                //+++++ central line linking the two symbols
                this.interactionSymbol.lwSymbols[1].setShape(forLine_1, forLine_2)

            }
        }
        if (this.isFullDetails() && (this.ssDrawing.workingSession.locationDrawn.contains(this.residue.absPos) && this.ssDrawing.workingSession.locationDrawn.contains(
                this.pairedResidue.absPos
            ))
        ) {
            return this.interactionSymbol.asSVG(at, frame)
        }
        return ""
    }

}

open class PhosphodiesterBondDrawing(
    parent: DrawingElement?,
    ssDrawing: SecondaryStructureDrawing,
    location: Location
) : DrawingElement(ssDrawing, parent, "PhosphoDiester Bond", location, SecondaryStructureType.PhosphodiesterBond) {

    val residue: ResidueDrawing
    val nextResidue: ResidueDrawing

    override val children: List<DrawingElement>
        get() = listOf()

    val start: Int
        get() {
            return this.location.start
        }

    val end: Int
        get() {
            return this.location.end
        }

    override val bounds: List<Point2D>
        get() {
            val l = mutableListOf<Point2D>()
            val center1 = this.residue.center
            val center2 = this.nextResidue.center
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            l.addAll(getPerpendicular(p1, center1, center2, radiusConst * 0.5).toList())
            l.addAll(getPerpendicular(p2, center1, center2, radiusConst * 0.5).toList().reversed())
            return l
        }

    override fun inside(location: Location) = if (ssDrawing.secondaryStructure.rna.useAlignmentNumberingSystem)
        location.contains(ssDrawing.secondaryStructure.rna.mapPosition(this.start)) && location.contains(
            ssDrawing.secondaryStructure.rna.mapPosition(
                this.end
            )
        )
    else
        location.contains(this.start) && location.contains(this.end)

    init {
        this.residue = this.ssDrawing.getResiduesFromAbsPositions(this.start).first()
        this.nextResidue = this.ssDrawing.getResiduesFromAbsPositions(this.end).first()
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
            g.color = this.getColor(selectedDrawings)
            val center1 = this.residue.center
            val center2 = this.nextResidue.center
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )

            g.draw(
                at.createTransformedShape(
                    Line2D.Double(
                        if (!residue.isFullDetails()) center1 else p1,
                        if (!nextResidue.isFullDetails()) center2 else p2
                    )
                )
            )
            g.stroke = previousStroke
    }

    open fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 = this.residue.center
            val center2 = this.nextResidue.center

            val _p1 = Point2D.Double()
            val _p2 = Point2D.Double()
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            at.transform(
                if (!residue.isFullDetails()) center1 else p1,
                _p1
            )
            at.transform(
                if (!nextResidue.isFullDetails()) center2 else p2,
                _p2
            )
            if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                getHTMLColorString(
                    this.getColor()
                )
            }" stroke-width="${
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
            }"/>"""
        }
        return ""
    }

}

class HelicalPhosphodiesterBondDrawing(parent: HelixDrawing, ssDrawing: SecondaryStructureDrawing, location: Location) :
    PhosphodiesterBondDrawing(parent, ssDrawing, location) {

    init {
        parent.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)
        val center1 = this.residue.center
        val center2 = this.nextResidue.center

        val (p1, p2) = pointsFrom(
            center1,
            center2,
            radiusConst + deltaPhosphoShift
        )

        g.draw(
            at.createTransformedShape(
                Line2D.Double(
                    if (!residue.isFullDetails()) center1 else p1,
                    if (!nextResidue.isFullDetails()) center2 else p2
                )
            )
        )
        g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 = this.residue.center
            val center2 = this.nextResidue.center

            val _p1 = Point2D.Double()
            val _p2 = Point2D.Double()
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            at.transform(
                if (!residue.isFullDetails()) center1 else p1,
                _p1
            )
            at.transform(
                if (!nextResidue.isFullDetails()) center2 else p2,
                _p2
            )
            if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                getHTMLColorString(
                    this.getColor()
                )
            }" stroke-width="${
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
            }"/>"""
        }
        return ""
    }
}

class InHelixClosingPhosphodiesterBondDrawing(
    parent: JunctionDrawing,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    private val posInhelix: Int
) : PhosphodiesterBondDrawing(parent, ssDrawing, location) {

    init {
        parent.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
            g.color = this.getColor(selectedDrawings)
            val center1 =
                if (this.posInhelix == this.residue.absPos && !this.residue.parent!!.parent!!.isFullDetails()) (this.residue.parent?.parent as HelixDrawing).line.p2 else this.residue.center
            val center2 =
                if (this.posInhelix == this.nextResidue.absPos && !this.nextResidue.parent!!.parent!!.isFullDetails()) (this.nextResidue.parent?.parent as HelixDrawing).line.p2 else this.nextResidue.center

            if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
                g.draw(at.createTransformedShape(Line2D.Double(center1, center2)))
            } else {
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                g.draw(
                    at.createTransformedShape(
                        Line2D.Double(
                            if (this.residue.absPos != this.posInhelix && residue.isFullDetails()) p1 else center1,
                            if (this.nextResidue.absPos != this.posInhelix && nextResidue.isFullDetails()) p2 else center2
                        )
                    )
                )
            }
            g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 =
                if (this.posInhelix == this.residue.absPos && !this.residue.parent!!.parent!!.isFullDetails()) (this.residue.parent?.parent as HelixDrawing).line.p2 else this.residue.center
            val center2 =
                if (this.posInhelix == this.nextResidue.absPos && !this.nextResidue.parent!!.parent!!.isFullDetails()) (this.nextResidue.parent?.parent as HelixDrawing).line.p2 else this.nextResidue.center

            if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
                val p1 = Point2D.Double()
                val p2 = Point2D.Double()
                at.transform(center1, p1)
                at.transform(center2, p2)
                if (frame.contains(p1) && frame.contains(p2)) return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            } else {
                val _p1 = Point2D.Double()
                val _p2 = Point2D.Double()
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                at.transform(
                    if (this.residue.absPos != this.posInhelix && residue.isFullDetails()) p1 else center1,
                    _p1
                )
                at.transform(
                    if (this.nextResidue.absPos != this.posInhelix && nextResidue.isFullDetails()) p2 else center2,
                    _p2
                )
                if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            }
        }
        return ""
    }
}

class OutHelixClosingPhosphodiesterBondDrawing(
    parent: JunctionDrawing,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    private val posInhelix: Int
) : PhosphodiesterBondDrawing(parent, ssDrawing, location) {

    init {
        parent.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
            g.color = this.getColor(selectedDrawings)

            val center1 =
                if (this.posInhelix == this.residue.absPos && !this.residue.parent!!.parent!!.isFullDetails()) (this.residue.parent?.parent as HelixDrawing).line.p1 else this.residue.center
            val center2 =
                if (this.posInhelix == this.nextResidue.absPos && !this.nextResidue.parent!!.parent!!.isFullDetails()) (this.nextResidue.parent?.parent as HelixDrawing).line.p1 else this.nextResidue.center

            if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
                g.draw(at.createTransformedShape(Line2D.Double(center1, center2)))
            } else {
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                g.draw(
                    at.createTransformedShape(
                        Line2D.Double(
                            if (this.residue.absPos != this.posInhelix && residue.isFullDetails()) p1 else center1,
                            if (this.nextResidue.absPos != this.posInhelix && nextResidue.isFullDetails()) p2 else center2
                        )
                    )
                )
            }
            g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 =
                if (this.posInhelix == this.residue.absPos && !this.residue.parent!!.parent!!.isFullDetails()) (this.residue.parent?.parent as HelixDrawing).line.p1 else this.residue.center
            val center2 =
                if (this.posInhelix == this.nextResidue.absPos && !this.nextResidue.parent!!.parent!!.isFullDetails()) (this.nextResidue.parent?.parent as HelixDrawing).line.p1 else this.nextResidue.center

            if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
                val p1 = Point2D.Double()
                val p2 = Point2D.Double()
                at.transform(center1, p1)
                at.transform(center2, p2)
                if (frame.contains(p1) && frame.contains(p2)) return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            } else {
                val _p1 = Point2D.Double()
                val _p2 = Point2D.Double()
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                at.transform(
                    if (this.residue.absPos != this.posInhelix && residue.isFullDetails()) p1 else center1,
                    _p1
                )
                at.transform(
                    if (this.nextResidue.absPos != this.posInhelix && nextResidue.isFullDetails()) p2 else center2,
                    _p2
                )
                if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            }
        }
        return ""
    }
}

class SingleStrandLinkingBranchPhosphodiesterBondDrawing(
    parent: SingleStrandDrawing,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    private val posInHelix: Int
) : PhosphodiesterBondDrawing(parent, ssDrawing, location) {

    init {
        parent.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)

        val center1 =
            when {
                this.residue.absPos == this.posInHelix && !this.residue.parent!!.parent!!.isFullDetails() -> (this.residue.parent!!.parent as HelixDrawing).line.p1
                else -> this.residue.center
            }
        val center2 =
            when {
                this.nextResidue.absPos == this.posInHelix && !this.nextResidue.parent!!.parent!!.isFullDetails() -> (this.nextResidue.parent!!.parent as HelixDrawing).line.p1
                else -> this.nextResidue.center
            }

        if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
            g.draw(at.createTransformedShape(Line2D.Double(center1, center2)))
        } else {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            g.draw(
                at.createTransformedShape(
                    Line2D.Double(
                        if (this.residue.isFullDetails()) p1 else center1,
                        if (this.nextResidue.isFullDetails()) p2 else center2
                    )
                )
            )
        }
        g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 =
                when {
                    this.residue.absPos == this.posInHelix && !this.residue.parent!!.parent!!.isFullDetails() -> (this.residue.parent!!.parent as HelixDrawing).line.p1
                    else -> this.residue.center
                }
            val center2 =
                when {
                    this.nextResidue.absPos == this.posInHelix && !this.nextResidue.parent!!.parent!!.isFullDetails() -> (this.nextResidue.parent!!.parent as HelixDrawing).line.p1
                    else -> this.nextResidue.center
                }
            if (!this.residue.isFullDetails() && !this.nextResidue.isFullDetails()) {
                val p1 = Point2D.Double()
                val p2 = Point2D.Double()
                at.transform(center1, p1)
                at.transform(center2, p2)
                if (frame.contains(p1) && frame.contains(p2)) return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            } else {
                val _p1 = Point2D.Double()
                val _p2 = Point2D.Double()
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                at.transform(
                    if (this.residue.isFullDetails()) p1 else center1,
                    _p1
                )
                at.transform(
                    if (this.nextResidue.isFullDetails()) p2 else center2,
                    _p2
                )
                if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            }
        }
        return ""
    }
}

class BranchesLinkingPhosphodiesterBondDrawing(
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    val previousBranch: JunctionDrawing,
    val nextBranch: JunctionDrawing
) : PhosphodiesterBondDrawing(null, ssDrawing, location) {

    init {
        ssDrawing.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
        val previousStroke = g.stroke
        g.stroke = BasicStroke(
            this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        )
        g.color = this.getColor(selectedDrawings)

        val center1 =
            if (!this.ssDrawing.quickDraw && this.residue.parent!!.parent!!.isFullDetails()) this.residue.center else (this.residue.parent!!.parent as HelixDrawing).line.p1
        val center2 =
            if (!this.ssDrawing.quickDraw && this.nextResidue.parent!!.parent!!.isFullDetails()) this.nextResidue.center else (this.nextResidue.parent!!.parent as HelixDrawing).line.p1

        if (this.ssDrawing.quickDraw || !this.residue.isFullDetails() && !this.residue.residueLetter.isFullDetails() && !this.nextResidue.isFullDetails() && !this.nextResidue.residueLetter.isFullDetails()) {
            g.draw(at.createTransformedShape(Line2D.Double(center1, center2)))
        } else {
            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            g.draw(
                at.createTransformedShape(
                    Line2D.Double(
                        if (residue.parent!!.isFullDetails() && (residue.isFullDetails() || residue.residueLetter.isFullDetails())) p1 else center1,
                        if (nextResidue.parent!!.isFullDetails() && (nextResidue.isFullDetails() || nextResidue.residueLetter.isFullDetails())) p2 else center2
                    )
                )
            )
        }
        g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        val center1 =
            if (this.residue.parent!!.parent!!.isFullDetails()) this.residue.center else (this.residue.parent!!.parent as HelixDrawing).line.p1
        val center2 =
            if (this.nextResidue.parent!!.parent!!.isFullDetails()) this.nextResidue.center else (this.nextResidue.parent!!.parent as HelixDrawing).line.p1

        if (!this.residue.isFullDetails() && !this.residue.residueLetter.isFullDetails() && !this.nextResidue.isFullDetails() && !this.nextResidue.residueLetter.isFullDetails()) {
            val p1 = Point2D.Double()
            val p2 = Point2D.Double()
            at.transform(center1, p1)
            at.transform(center2, p2)
            if (frame.contains(p1) && frame.contains(p2)) return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                getHTMLColorString(
                    this.getColor()
                )
            }" stroke-width="${this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()}"/>"""
        } else {
            val _p1 = Point2D.Double()
            val _p2 = Point2D.Double()

            val (p1, p2) = pointsFrom(
                center1,
                center2,
                radiusConst + deltaPhosphoShift
            )
            at.transform(
                if (residue.parent!!.isFullDetails() && (residue.isFullDetails() || residue.residueLetter.isFullDetails())) p1 else center1,
                _p1
            )
            at.transform(
                if (nextResidue.parent!!.isFullDetails() && (nextResidue.isFullDetails() || nextResidue.residueLetter.isFullDetails())) p2 else center2,
                _p2
            )
            if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                getHTMLColorString(
                    this.getColor()
                )
            }" stroke-width="${this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()}"/>"""
        }
        return ""
    }
}

class HelicesDirectLinkPhosphodiesterBondDrawing(
    parent: JunctionDrawing,
    ssDrawing: SecondaryStructureDrawing,
    location: Location,
    private val posForP2: Int = -1
) : PhosphodiesterBondDrawing(parent, ssDrawing, location) {

    init {
        parent.phosphoBonds.add(this)
    }

    override fun draw(
        g: Graphics2D,
        at: AffineTransform,
        drawingArea: Rectangle2D,
        selectedDrawings: List<DrawingElement>?
    ) {
            val previousStroke = g.stroke
            g.stroke = BasicStroke(
                this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
            g.color = this.getColor(selectedDrawings)

            val center1 =
                if (!this.residue.parent!!.parent!!.isFullDetails()) (if (this.posForP2 == this.residue.absPos) (this.residue.parent?.parent as HelixDrawing).line.p2 else (this.residue.parent?.parent as HelixDrawing).line.p1) else this.residue.center
            val center2 =
                if (!this.nextResidue.parent!!.parent!!.isFullDetails()) (if (this.posForP2 == this.nextResidue.absPos) (this.nextResidue.parent?.parent as HelixDrawing).line.p2 else (this.nextResidue.parent?.parent as HelixDrawing).line.p1) else this.nextResidue.center

            if (!(this.residue.isFullDetails() || !residue.parent!!.isFullDetails() || !residue.parent!!.parent!!.isFullDetails()) && (!this.nextResidue.isFullDetails() || !nextResidue.parent!!.isFullDetails()) || !nextResidue.parent!!.parent!!.isFullDetails()) {
                g.draw(at.createTransformedShape(Line2D.Double(center1, center2)))
            } else {
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                g.draw(
                    at.createTransformedShape(
                        Line2D.Double(
                            if (!residue.isFullDetails()) center1 else p1,
                            if (!nextResidue.isFullDetails()) center2 else p2
                        )
                    )
                )
            }
            g.stroke = previousStroke
    }

    override fun asSVG(at: AffineTransform, frame: Rectangle2D): String {
        if (this.isFullDetails()) {
            val center1 =
                if (!this.residue.parent!!.parent!!.isFullDetails()) (if (this.posForP2 == this.residue.absPos) (this.residue.parent?.parent as HelixDrawing).line.p2 else (this.residue.parent?.parent as HelixDrawing).line.p1) else this.residue.center
            val center2 =
                if (!this.nextResidue.parent!!.parent!!.isFullDetails()) (if (this.posForP2 == this.nextResidue.absPos) (this.nextResidue.parent?.parent as HelixDrawing).line.p2 else (this.nextResidue.parent?.parent as HelixDrawing).line.p1) else this.nextResidue.center

            if (!(this.residue.isFullDetails() || !residue.parent!!.isFullDetails() || !residue.parent!!.parent!!.isFullDetails()) && (!this.nextResidue.isFullDetails() || !nextResidue.parent!!.isFullDetails()) || !nextResidue.parent!!.parent!!.isFullDetails()) {
                val p1 = Point2D.Double()
                val p2 = Point2D.Double()
                at.transform(center1, p1)
                at.transform(center2, p2)
                if (frame.contains(p1) && frame.contains(p2)) return """<line x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            } else {
                val _p1 = Point2D.Double()
                val _p2 = Point2D.Double()
                val (p1, p2) = pointsFrom(
                    center1,
                    center2,
                    radiusConst + deltaPhosphoShift
                )
                at.transform(
                    if (!residue.isFullDetails()) center1 else p1,
                    _p1
                )
                at.transform(
                    if (!nextResidue.isFullDetails()) center2 else p2,
                    _p2
                )
                if (frame.contains(_p1) && frame.contains(_p2)) return """<line x1="${_p1.x}" y1="${_p1.y}" x2="${_p2.x}" y2="${_p2.y}" stroke="${
                    getHTMLColorString(
                        this.getColor()
                    )
                }" stroke-width="${
                    this.ssDrawing.zoomLevel.toFloat() * this.getLineWidth().toFloat()
                }"/>"""
            }
        }
        return ""
    }
}

/***********************************************
 *              Junctions stuff                *
 ***********************************************/

enum class ConnectorId(val value: Int) {
    s(0),
    ssw(1),
    sw(2),
    wsw(3),
    w(4),
    wnw(5),
    nw(6),
    nnw(7),
    n(8),
    nne(9),
    ne(10),
    ene(11),
    e(12),
    ese(13),
    se(14),
    sse(15);
}

fun getConnectorId(value: Int) = ConnectorId.entries.first { it.value == (value % ConnectorId.entries.size) }

fun nextConnectorId(c: ConnectorId) =
    ConnectorId.entries.first { it.value == (c.value + 1) % ConnectorId.entries.size }

fun previousConnectorId(c: ConnectorId) = if (c.value - 1 < 0) ConnectorId.entries
    .first { it.value == ConnectorId.entries.size - 1 } else ConnectorId.entries.first { it.value == c.value - 1 }

fun oppositeConnectorId(c: ConnectorId) =
    ConnectorId.entries.first { it.value == (c.value + ConnectorId.entries.size / 2) % ConnectorId.entries.size }

//the different behaviors to compute the outId of an helix according to its rank for a given junction type
val junctionsBehaviors = mutableMapOf(
    Pair(JunctionType.ApicalLoop, { junctionDrawing: JunctionDrawing, helixRank: Int -> null }),
    Pair(JunctionType.InnerLoop, { junctionDrawing: JunctionDrawing, helixRank: Int ->
        /*if (junctionDrawing.junction.location.blocks[0].length < 5 || junctionDrawing.junction.location.blocks[1].length < 5) {
            oppositeConnectorId(junctionDrawing.inId)
        } else {*/
        when (junctionDrawing.inId) {
            ConnectorId.ssw -> ConnectorId.n
            ConnectorId.sw -> ConnectorId.n
            ConnectorId.wsw -> ConnectorId.n
            ConnectorId.w ->
                if (junctionDrawing.previousJunction != null && junctionDrawing.previousJunction.inId.value > ConnectorId.w.value && junctionDrawing.previousJunction.inId.value < ConnectorId.e.value) { //we want the same orientation than for the previous junction
                    ConnectorId.s
                } else {
                    ConnectorId.n
                }

            ConnectorId.wnw -> ConnectorId.s
            ConnectorId.nw -> ConnectorId.s
            ConnectorId.nnw -> ConnectorId.s
            ConnectorId.n -> ConnectorId.s
            ConnectorId.nne -> ConnectorId.s
            ConnectorId.ne -> ConnectorId.s
            ConnectorId.ene -> ConnectorId.s
            ConnectorId.e ->
                if (junctionDrawing.previousJunction != null && junctionDrawing.previousJunction.inId.value > ConnectorId.w.value && junctionDrawing.previousJunction.inId.value < ConnectorId.e.value) { //we want the same orientation than for the previous junction
                    ConnectorId.s
                } else {
                    ConnectorId.n
                }

            ConnectorId.ese -> ConnectorId.n
            ConnectorId.se -> ConnectorId.n
            ConnectorId.sse -> ConnectorId.n
            ConnectorId.s -> ConnectorId.n
        }
        /* }*/
    }),
    Pair(JunctionType.ThreeWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.FourWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.FiveWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.SixWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.SevenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.EightWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.NineWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.TenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.ElevenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.TwelveWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.ThirteenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.FourteenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.FifthteenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.SixteenWay,
        { junctionDrawing: JunctionDrawing, helixRank: Int ->
            var outIdForLongest = ConnectorId.n
            val longest =
                junctionDrawing.junction.helicesLinked.sortedByDescending { it.maxBranchLength }[1] //the inHelix is the longest, so we want the second one once sorted
            val longestRank = junctionDrawing.junction.helicesLinked.indexOf(longest)
            var dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val helicesBeforelongest = longestRank - 1
            val helicesAfterlongest = junctionDrawing.junction.helicesLinked.size - longestRank - 1
            if (dist_InId_OutIdLongest < helicesBeforelongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value + helicesBeforelongest - (dist_InId_OutIdLongest))
            } else if (16 - dist_InId_OutIdLongest - 2 < helicesAfterlongest) {
                outIdForLongest =
                    getConnectorId(outIdForLongest.value - helicesAfterlongest - (16 - dist_InId_OutIdLongest - 2))
            }
            dist_InId_OutIdLongest =
                if (junctionDrawing.inId.value > outIdForLongest.value) 15 - junctionDrawing.inId.value + outIdForLongest.value else outIdForLongest.value - junctionDrawing.inId.value - 1
            val step = dist_InId_OutIdLongest / longestRank
            if (helixRank == longestRank) {
                outIdForLongest
            } else if (helixRank < longestRank) {
                getConnectorId(junctionDrawing.inId.value + helixRank * (step + 1))
            } else {
                getConnectorId(outIdForLongest.value + ((helixRank - longestRank) * (16 - dist_InId_OutIdLongest) / (helicesAfterlongest + 1)))
            }
        }),
    Pair(JunctionType.Flower, { junctionDrawing: JunctionDrawing, helixRank: Int -> null })
)

val currentJunctionBehaviors = mutableMapOf<JunctionType, (JunctionDrawing, Int) -> ConnectorId?>()

/***********************************************
 *              Geometry stuff                 *
 ***********************************************/

fun centerFrom(p1:Point2D, p2:Point2D):Point2D = Point2D.Double((p1.x+p2.x)/2.0, (p1.y+p2.y)/2.0)

/**
Compute the center of a circle according to the entry point
 **/
fun centerFrom(inId: ConnectorId, inPoint: Point2D, radius: Double): Point2D {
    when (inId) {
        ConnectorId.s -> return Point2D.Double(inPoint.x, inPoint.y - radius)
        ConnectorId.ssw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (-3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (-3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.sw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (-2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (-2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.wsw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (-360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (-360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.w -> return Point2D.Double(inPoint.x + radius, inPoint.y)
        ConnectorId.wnw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.nw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.nnw -> return Point2D.Double(
            inPoint.x + adjacentSideFrom(
                (3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y + oppositeSideFrom(
                (3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.n -> return Point2D.Double(inPoint.x, inPoint.y + radius)
        ConnectorId.nne -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (-3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (-3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.ne -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (-2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (-2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.ene -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (-360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (-360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.e -> return Point2D.Double(inPoint.x - radius, inPoint.y)
        ConnectorId.ese -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.se -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (2 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            )
        )

        ConnectorId.sse -> return Point2D.Double(
            inPoint.x - adjacentSideFrom(
                (3 * 360 / ConnectorId.entries.size).toDouble(),
                radius
            ), inPoint.y - oppositeSideFrom(
                (3 * 360 / ConnectorId.entries.size).toDouble(),
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
    return (first zip second).flatMap { it.toList() } + first.subList(commonLength, first.size) + second.subList(
        commonLength,
        second.size
    )
}

fun ccw(A: Point2D, B: Point2D, C: Point2D) = (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x)

fun intersects(A: Point2D, B: Point2D, C: Point2D, D: Point2D) = ccw(
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

fun intersects(center1: Point2D, radius1: Double, center2: Point2D, radius2: Double) =
    hypot(center1.x - center2.x, center1.y - center2.y) <= (radius1 + radius2)

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

fun distance(p1: Point2D, p2: Point2D): Double = sqrt((p1.x - p2.x).pow(2.0) + (p1.y - p2.y).pow(2.0))

fun angleFrom(oppositeSide: Double, adjacentSide: Double) = Math.atan(oppositeSide / adjacentSide) * radiansToDegrees

fun angleFrom(p1: Point2D, p2: Point2D, p3: Point2D): Double {
    val a = distance(p1, p2)
    val b = distance(p2, p3)
    val c = distance(p1, p3)
    return Math.acos((a * a + c * c - b * b) / (2 * a * c)) * radiansToDegrees
}

fun adjacentSideFrom(degrees: Double, hypotenuse: Double) = Math.cos(degrees * degreesToRadians) * hypotenuse

fun oppositeSideFrom(degrees: Double, hypotenuse: Double) = Math.sin(degrees * degreesToRadians) * hypotenuse

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
        return Pair<Point2D, Point2D>(
            Point2D.Double(
                p0.x + oppositeSideFrom(
                    angle,
                    distance
                ), p0.y - adjacentSideFrom(angle, distance)
            ), Point2D.Double(
                p0.x - oppositeSideFrom(
                    angle,
                    distance
                ), p0.y + adjacentSideFrom(angle, distance)
            )
        )
    } else {
        return Pair<Point2D, Point2D>(
            Point2D.Double(
                p0.x - oppositeSideFrom(
                    angle,
                    distance
                ), p0.y + adjacentSideFrom(angle, distance)
            ), Point2D.Double(
                p0.x + oppositeSideFrom(
                    angle,
                    distance
                ), p0.y - adjacentSideFrom(angle, distance)
            )
        )
    }
}

fun getRoundedGeneralPath(l: MutableList<Point2D>): GeneralPath {
    l.add(l[0])
    l.add(l[1])
    val calculatePoint = { p1: Point2D, p2: Point2D ->
        val d_x = (p1.x - p2.x) * 0.1
        val d_y = (p1.y - p2.y) * 0.1
        Point2D.Double(p2.x + d_x, p2.y + d_y)
    }
    val p = GeneralPath()
    val firstPoint = calculatePoint(l[l.size - 1], l[l.size - 2])
    p.moveTo(firstPoint.x, firstPoint.y)
    for (i in 1 until l.size - 1) {
        val p1 = l[i - 1]
        val p2 = l[i]
        val p3 = l[i + 1]
        var mPoint = calculatePoint(p1, p2)
        p.lineTo(mPoint.x, mPoint.y)
        mPoint = calculatePoint(p3, p2)
        p.curveTo(
            p2.x, p2.y, p2.x, p2.y, mPoint.x,
            mPoint.y
        )
    }
    return p
}

fun getAWTColor(htmlColor: String, alpha: Int = 255): Color {
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

fun Booquet(
    ss: SecondaryStructure,
    frameWidth: Double,
    frameHeight: Double,
    step: Double = 25.0,
    lineWidth: Double = 2.0,
    residue_occupancy: Double = 5.0,
    junction_diameter: Double = 20.0,
    color: Color = Color.BLACK
): String {

    val booquet = mutableMapOf<String, DoubleArray>()
    var x = 0.0
    val apicalLoops_x_coords = mutableListOf<Double>()
    apicalLoops_x_coords.add(x)

    val apical_loops = ss.junctions.filter { it.junctionType == JunctionType.ApicalLoop }

    //the space between apical loops
    for (i in 0 until apical_loops.size - 1) {
        val junctionsPerLevel = mutableListOf<Int>()
        for (i in 0..20)
            junctionsPerLevel.add(1)
        val l1 = ss.getStemLoopLocation(apical_loops[i])
        val l2 = ss.getStemLoopLocation(apical_loops[i + 1])

        val before = l1.end
        val after = l2.start
        var total_residues = 0
        var total_junctions = 0
        for (junction in ss.junctions) {
            if (junction.junctionType != JunctionType.ApicalLoop && junction.junctionType != JunctionType.InnerLoop) {
                for (single_strand_location in junction.location.blocks.subList(1, junction.location.blocks.size - 1))
                    if (before <= single_strand_location.start && after >= single_strand_location.end) {
                        total_residues += single_strand_location.length
                        total_junctions += 1
                    }
            }
        }
        if (total_junctions == 0) {
            var l = (after - before + 1) * residue_occupancy * junction_diameter
            if (l > 2 * junction_diameter)
                l = 2 * junction_diameter
            x += l
        } else
            x += total_junctions * step
        apicalLoops_x_coords.add(x)
    }

    val branches = ss.helices.filter { it.junctionsLinked.second == null }

    for (branch in branches) {
        val current_y = 200.0
        drawBooquetBranch(
            booquet,
            ss,
            branch,
            apicalLoops_x_coords,
            current_y,
            residue_occupancy,
            junction_diameter,
            apical_loops
        )
    }

    for (singleStrand in ss.singleStrands) {
        if (singleStrand.start == 1) {
            for (h in ss.helices) {
                if (h.start == singleStrand.end + 1) {
                    var l = h.start * residue_occupancy
                    if (l > 2 * junction_diameter)
                        l = 2 * junction_diameter
                    booquet[singleStrand.name] = doubleArrayOf(
                        booquet[h.name]!![0] - l,
                        booquet[h.name]!![1],
                        booquet[h.name]!![0],
                        booquet[h.name]!![1]
                    )
                    break
                }
            }
        } else if (singleStrand.end == ss.length) {
            for (h in ss.helices) {
                if (h.end == singleStrand.start - 1) {
                    var l = (ss.length - h.end + 1) * residue_occupancy
                    if (l > 2 * junction_diameter)
                        l = 2 * junction_diameter
                    booquet[singleStrand.name] = doubleArrayOf(
                        booquet[h.name]!![0],
                        booquet[h.name]!![1],
                        booquet[h.name]!![0] + l,
                        booquet[h.name]!![1]
                    )
                    break
                }
            }
        } else {
            var first_helix: Helix? = null
            var second_helix: Helix? = null
            for (h in ss.helices) {
                if (h.end == singleStrand.start - 1)
                    first_helix = h
                if (h.start == singleStrand.end + 1)
                    second_helix = h
                if (first_helix != null && second_helix != null) {
                    booquet[singleStrand.name] = doubleArrayOf(
                        booquet[first_helix.name]!![0],
                        booquet[first_helix.name]!![1],
                        booquet[second_helix.name]!![0],
                        booquet[second_helix.name]!![1]
                    )
                    break
                }
            }
        }
    }

    var minX = booquet.values.minByOrNull { it[0] }!!.get(0) - junction_diameter - lineWidth
    var minY =
        booquet.values.minByOrNull { it[1] }!!.get(1) - junction_diameter - lineWidth

    var maxX = booquet.values.maxByOrNull { it[0] }!!.get(0) + junction_diameter + lineWidth
    var maxY = booquet.values.maxByOrNull { it[1] }!!.get(1) + lineWidth

    var width = maxX - minX
    var height = maxY - minY

    val ratio = listOf(frameWidth / width, frameHeight / height).minOrNull()!!

    minX *= ratio
    maxX *= ratio
    minY *= ratio
    maxY *= ratio

    width = maxX - minX
    height = maxY - minY

    val svgBuffer =
        StringBuffer("""<svg width="$width" height="$height" viewBox="0 0 $width $height" xmlns="http://www.w3.org/2000/svg">""" + "\n")

    ss.helices.forEach { helix ->
        booquet[helix.name]?.let { coords ->
            svgBuffer.append(
                """<line x1="${coords[0] * ratio - minX}" y1="${coords[1] * ratio - minY}" x2="${coords[2] * ratio - minX}" y2="${coords[3] * ratio - minY}" stroke="${
                    getHTMLColorString(
                        color
                    )
                }" stroke-width="$lineWidth" stroke-linecap="round"/>"""
            )
        }
    }

    //phospho bonds between two branches
    val startOfBranches = ss.helices.filter { it.junctionsLinked.second == null }.sortedBy { it.start }

    for (i in 0 until startOfBranches.size - 1) {
        if (startOfBranches[i].end + 1 == startOfBranches[i + 1].start) { // direct link
            booquet[startOfBranches[i].name]?.let { coordsH1 ->
                booquet[startOfBranches[i + 1].name]?.let { coordsH2 ->
                    svgBuffer.append(
                        """<line x1="${coordsH1[0] * ratio - minX}" y1="${coordsH1[1] * ratio - minY}" x2="${coordsH2[0] * ratio - minX}" y2="${coordsH2[1] * ratio - minY}" stroke="${
                            getHTMLColorString(
                                color
                            )
                        }" stroke-width="${lineWidth}" stroke-linecap="round"/>"""
                    )
                }
            }
        }
    }

    ss.singleStrands.forEach { singleStrand ->
        booquet[singleStrand.name]?.let { coords ->
            svgBuffer.append(
                """<line x1="${coords[0] * ratio - minX}" y1="${coords[1] * ratio - minY}" x2="${coords[2] * ratio - minX}" y2="${coords[3] * ratio - minY}" stroke="${
                    getHTMLColorString(
                        color
                    )
                }" stroke-width="$lineWidth" stroke-linecap="round"/>"""
            )
        }
    }

    ss.junctions.forEach { junction ->
        booquet[junction.name]?.let { junctionCoords ->
            when (junction.junctionType) {
                in setOf(JunctionType.ApicalLoop, JunctionType.InnerLoop) -> {
                    svgBuffer.append(
                        """<circle cx="${junctionCoords[0] * ratio - minX}" cy="${junctionCoords[1] * ratio - minY}" r="${junction_diameter / 2.0 * ratio}" stroke="${
                            getHTMLColorString(
                                color
                            )
                        }" stroke-width="$lineWidth" fill="${
                            getHTMLColorString(
                                color
                            )
                        }"/>"""
                    )

                    svgBuffer.append(
                        """<circle cx="${junctionCoords[0] * ratio - minX}" cy="${junctionCoords[1] * ratio - minY}" r="${1.5 * junction_diameter / 2.0 * ratio}" stroke="${
                            getHTMLColorString(
                                color
                            )
                        }" stroke-width="$lineWidth" fill="none"/>"""
                    )
                }

                else -> {
                    svgBuffer.append(
                        """<circle cx="${junctionCoords[0] * ratio - minX}" cy="${junctionCoords[1] * ratio - minY}" r="${junction_diameter / 2.0 * ratio}" stroke="${
                            getHTMLColorString(
                                color
                            )
                        }" stroke-width="$lineWidth" fill="${
                            getHTMLColorString(
                                color
                            )
                        }"/>"""
                    )

                    svgBuffer.append(
                        """<circle cx="${junctionCoords[0] * ratio - minX}" cy="${junctionCoords[1] * ratio - minY}" r="${1.5 * junction_diameter / 2.0 * ratio}" stroke="${
                            getHTMLColorString(
                                color
                            )
                        }" stroke-width="$lineWidth" fill="none"/>"""
                    )
                    for (i in 0 until junction.location.blocks.size - 1) {
                        for (h in ss.helices) {
                            if (h.location.start == junction.location.blocks[i].end) {
                                booquet[h.name]?.let { helixCoords ->
                                    if (helixCoords[1] != junctionCoords[1]) {
                                        val (_, p2) = pointsFrom(
                                            Point2D.Double(
                                                helixCoords[0],
                                                helixCoords[1]
                                            ),
                                            Point2D.Double(
                                                junctionCoords[0],
                                                junctionCoords[1]
                                            ),
                                            (1.5 * junction_diameter) / 2.0
                                        )
                                        svgBuffer.append(
                                            """<line x1="${helixCoords[0] * ratio - minX}" y1="${helixCoords[1] * ratio - minY}" x2="${p2.x * ratio - minX}" y2="${p2.y * ratio - minY}" stroke="${
                                                getHTMLColorString(
                                                    color
                                                )
                                            }" stroke-width="$lineWidth" stroke-linecap="round"/>"""
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    svgBuffer.append("</svg>")

    return svgBuffer.toString()
}

private fun drawBooquetBranch(
    booquet: MutableMap<String, DoubleArray>,
    ss: SecondaryStructure,
    helix: Helix,
    x_coords: MutableList<Double>,
    current_y: Double,
    residue_occupancy: Double,
    junction_diameter: Double,
    apical_loops: List<Junction>
) {
    val enclosed_stem_loops = mutableListOf<Junction>()
    var next_junction: Junction? = null

    for (junction in ss.junctions) {
        if (junction.location.blocks.size >= 3 && helix.location.blocks.first().end == junction.start) {
            next_junction = junction
            for (i in 0 until junction.location.blocks.size - 1) {
                for (h in ss.helices) {
                    if (h.start == junction.location.blocks[i].end) {
                        drawBooquetBranch(
                            booquet,
                            ss,
                            h,
                            x_coords,
                            current_y - helix.length * residue_occupancy - 1.5 * junction_diameter,
                            residue_occupancy,
                            junction_diameter,
                            apical_loops
                        )
                    }
                }
                for (apical_loop in apical_loops) {
                    val l = ss.getStemLoopLocation(apical_loop)
                    if (l.start >= junction.location.blocks[i].end && l.end <= junction.location.blocks[i + 1].start)
                        enclosed_stem_loops.add(apical_loop)
                }
            }
        } else if (junction.location.blocks.size == 2 && helix.location.blocks.first().end == junction.start) {
            next_junction = junction
            for (i in 0 until junction.location.blocks.size - 1) {
                for (h in ss.helices) {
                    if (h.start == junction.location.blocks.first().end) {
                        drawBooquetBranch(
                            booquet,
                            ss,
                            h,
                            x_coords,
                            current_y - helix.length * residue_occupancy - 1.5 * junction_diameter,
                            residue_occupancy,
                            junction_diameter,
                            apical_loops
                        )
                    }
                }
                for (apical_loop in apical_loops) {
                    val l = ss.getStemLoopLocation(apical_loop)
                    if (l.start >= junction.location.blocks.first().start && l.end <= junction.end)
                        enclosed_stem_loops.add(apical_loop)
                }
            }
        } else if (junction.location.blocks.size == 1 && helix.location.blocks.first().end == junction.start) {
            next_junction = junction
        }
    }

    if (enclosed_stem_loops.isEmpty()) {
        for (apical_loop in apical_loops) {
            val l = ss.getStemLoopLocation(apical_loop)
            if (helix.start >= l.start && helix.end <= l.end)
                enclosed_stem_loops.add(apical_loop)
        }
    }
    val _x_coords = mutableListOf<Double>()

    for (stem_loop in enclosed_stem_loops)
        _x_coords.add(x_coords[apical_loops.indexOf(stem_loop)])

    val m = _x_coords.average()

    booquet[helix.name] = doubleArrayOf(m, current_y, m, current_y - helix.length * residue_occupancy)

    next_junction?.let {
        booquet[it.name] =
            doubleArrayOf(m, current_y - helix.length * residue_occupancy - 1.5 * junction_diameter / 2.0)
    }
}