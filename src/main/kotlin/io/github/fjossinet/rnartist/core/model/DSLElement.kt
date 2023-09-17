package io.github.fjossinet.rnartist.core.model

import io.github.fjossinet.rnartist.core.layout
import io.github.fjossinet.rnartist.core.theme
import java.awt.Color

fun setJunction(
    rnArtistEl: RNArtistEl,
    radius: Double? = null,
    outIds: String? = null,
    type: Int? = null,
    location: Location? = null,
    step: Int? = null
) {
    val layout = rnArtistEl.getLayoutOrNew()
    with(layout.addJunction()) {
        radius?.let {
            this.setRadius(radius)
        }
        location?.let {
            this.addLocation().setLocation(it)
        }
        type?.let {
            this.setType(it)
        }
        outIds?.let {
            this.setOutIds(outIds)
        }
        step?.let {
            this.setStep(step)
        }
    }
}

fun setDetailsLvlForFull2D(rnArtistEl: RNArtistEl, lvl: Int) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.addDetails().setValue(lvl)
}

fun setDetailsLvl(
    rnArtistEl: RNArtistEl,
    isfullDetails: Boolean,
    location: Location? = null,
    types: String? = null,
    step: Int? = null
) {
    val theme = rnArtistEl.getThemeOrNew()
    if (isfullDetails) {
        with(theme.addShow()) {
            location?.let {
                this.addLocation().setLocation(it)
            }
            types?.let {
                this.setType(it)
            }
            step?.let {
                this.setStep(it)
            }
            this
        }
    } else {
        with(theme.addHide()) {
            location?.let { l ->
                this.addLocation().setLocation(l)
            }
            types?.let {
                this.setType(it)
            }
            step?.let {
                this.setStep(it)
            }
            this
        }
    }
}

fun setSchemeForFull2D(rnArtistEl: RNArtistEl, scheme: String) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.addScheme().setValue(scheme)
}

fun setColorForFull2D(rnArtistEl: RNArtistEl, onFont: Boolean = false, color: Color, step: Int? = null) {
    val theme = rnArtistEl.getThemeOrNew()
    if (onFont) {
        val types = "n"
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
        colorEl.setType(types)
        step?.let {
            colorEl.setStep(it)
        }
    } else {
        val types =
            "helix secondary_interaction single_strand junction phosphodiester_bond interaction_symbol N tertiary_interaction"
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
        colorEl.setType(types)
        step?.let {
            colorEl.setStep(it)
        }
    }
}

fun setColor(
    rnArtistEl: RNArtistEl,
    color: Color,
    location: Location? = null,
    types: String? = null,
    step: Int? = null
) {
    val theme = rnArtistEl.getThemeOrNew()
    with(theme.addColor()) {
        this.setValue(getHTMLColorString(color))
        location?.let { l ->
            with(this.addLocation()) {
                this.setLocation(l)
            }
        }
        types?.let {
            this.setType(it)
        }
        step?.let {
            this.setStep(it)
        }
    }
}

/**
 * Remove all the line elements and create a new one.
 */
fun setLineWidthForFull2D(rnArtistEl: RNArtistEl, width: Double, step: Int? = null) {
    val theme = rnArtistEl.getThemeOrNew()
    with(theme.addLine()) {
        this.setValue(width)
        step?.let {
            this.setStep(it)
        }
    }
}

fun setLineWidth(
    rnArtistEl: RNArtistEl,
    width: Double,
    location: Location? = null,
    types: String? = null,
    step: Int? = null
) {
    val theme = rnArtistEl.getThemeOrNew()
    with(theme.addLine()) {
        this.setValue(width)
        location?.let {
            this.addLocation().setLocation(it)
        }
        types?.let {
            this.setType(it)
        }
        step?.let {
            this.setStep(it)
        }
    }
}

abstract class DSLNode(val name: String) {
    abstract fun dump(indent: String = "", buffer: StringBuffer = StringBuffer()): StringBuffer
}

open class DSLProperty(name: String, var value: String, val operator: String = "=") : DSLNode(name) {
    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent ${this.name} ${this.operator} ${this.value}")
        return buffer
    }
}

class StringDSLProperty(name: String, value: String, operator: String = "=") : DSLProperty(name, value, operator) {
    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent ${this.name} ${this.operator} \"${this.value}\"")
        return buffer
    }
}

abstract class DSLElement(name: String) : DSLNode(name) {
    protected val children = mutableListOf<DSLNode>()

    fun getProperties(): List<DSLProperty> = this.children.filterIsInstance<DSLProperty>()

    fun getStep(): Int? = this.getProperties().firstOrNull { it.name.equals("step") }?.value?.toIntOrNull()

    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        children.forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
        return buffer
    }

    /**
     * Test if the location (if any) and the 2D types (if any) for this element are inside into those given as arguments
     * No location (null, meaning any location or no types (null, meaning any types) contain any location or types.
     */
    fun inside(location: Location?, types: String?): Boolean {
        var sameLocation = inside(location)
        val sameTypes = inside(types)
        //if sametypes, if the parameter location is null (meaning we target all the types whatever the location) and the location of this not null, it is inside
        if (sameTypes && location == null)
            sameLocation = true
        return sameLocation && sameTypes
    }

    fun inside(location: Location?): Boolean {
        var sameLocation = location == null && this.getLocationOrNull() == null
        location?.let { l1 ->
            this.getLocationOrNull()?.let { l2 ->
                sameLocation =
                    l1 == l2.toLocation() || l1.contains(l2.toLocation()) //it is the same element if the location is the same, or if the new location contains the current location stored in this element
            } ?: run {
                sameLocation = false
            }
        } ?: run {
            sameLocation = true
        }
        return sameLocation
    }

    fun inside(types: String?): Boolean {
        var sameTypes = types == null && this.getTypesOrNull() == null
        types?.let { t1 ->
            this.getTypesOrNull()?.let { t2 ->
                sameTypes = (t2.value.split(" ")
                    .all { it in t1.split(" ") }) //if all the types of the current element are described in the new element, same element that will be needed to be replaced
            }
        }
        return sameTypes
    }


    /**
     * Return the first child using its name as criteria
     */
    protected fun getChildOrNull(name: String) = this.children.filter { it.name.equals(name) }.firstOrNull()

    protected fun getPropertyOrNull(name: String) =
        this.children.filterIsInstance<DSLProperty>().filter { it.name.equals(name) }.firstOrNull()

    /**
     * Return children using their name as criteria
     */
    protected fun getChildren(name: String) = this.children.filter { it.name == name }

    fun removeChild(child: DSLNode) {
        this.children.remove(child)
    }

    fun addLocation(locationEl: LocationEl? = null): LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }

    fun removeLocation() {
        this.getChildOrNull("location")?.let {
            this.removeChild(it)
        }
    }

    fun getLocationOrNew(): LocationEl = this.getChildOrNull("location") as? LocationEl ?: addLocation()

    fun getLocationOrNull(): LocationEl? = this.getChildOrNull("location") as LocationEl?

    fun getTypesOrNull() = this.getPropertyOrNull("type")

    fun addStringProperty(name: String, value: String, operator: String = "=") =
        this.children.add(StringDSLProperty(name, value, operator))

    fun addProperty(name: String, value: Double, operator: String = "=") =
        this.children.add(DSLProperty(name, "$value", operator))

    fun addProperty(name: String, value: Int, operator: String = "=") =
        this.children.add(DSLProperty(name, "$value", operator))

}

class RNArtistEl : DSLElement("rnartist") {

    fun addLayout(layoutEl: LayoutEl? = null): LayoutEl {
        this.children.removeIf { it.name.equals("layout") } //only a single element allowed
        val el = layoutEl ?: LayoutEl()
        this.children.add(el)
        return el
    }

    fun getLayoutOrNew(): LayoutEl = this.getChildOrNull("layout") as? LayoutEl ?: addLayout()

    fun addTheme(themeEl: ThemeEl? = null): ThemeEl {
        this.children.removeIf { it.name.equals("theme") } //only a single element allowed
        val el = themeEl ?: ThemeEl()
        this.children.add(el)
        return el
    }

    fun getThemeOrNew(): ThemeEl = this.getChildOrNull("theme") as? ThemeEl ?: addTheme()

    fun addSS(ssEl: SSEl? = null): SSEl {
        this.children.removeIf { it.name.equals("ss") } //only a single element allowed
        val el = ssEl ?: SSEl()
        this.children.add(el)
        return el
    }

    fun getSSOrNew(): SSEl = this.getChildOrNull("ss") as? SSEl ?: addSS()

    fun getSSOrNull(): SSEl? = this.getChildOrNull("ss") as? SSEl

    fun addPNG(pngEl: PNGEl? = null): PNGEl {
        this.children.removeIf { it.name.equals("png") } //only a single element allowed
        val el = pngEl ?: PNGEl()
        this.children.add(el)
        return el
    }

    fun getPNGOrNew(): PNGEl = this.getChildOrNull("png") as? PNGEl ?: addPNG()

    fun getPNGOrNull(): PNGEl? = this.getChildOrNull("png") as? PNGEl

    fun addSVG(svgEl: SVGEl? = null): SVGEl {
        this.children.removeIf { it.name.equals("svg") } //only a single element allowed
        val el = svgEl ?: SVGEl()
        this.children.add(el)
        return el
    }

    fun getSVGOrNew(): SVGEl = this.getChildOrNull("svg") as? SVGEl ?: addSVG()

    fun getSVGOrNull(): SVGEl? = this.getChildOrNull("svg") as? SVGEl

    fun removeSVG() = this.getSVGOrNull()?.let { this.removeChild(it) }

    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("import io.github.fjossinet.rnartist.core.*")
        buffer.appendLine()
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        this.getSSOrNew().dump(newIndent, buffer)
        this.getThemeOrNew().dump(newIndent, buffer)
        this.getLayoutOrNew().dump(newIndent, buffer)
        this.getPNGOrNull()?.dump(newIndent, buffer)
        this.getSVGOrNull()?.dump(newIndent, buffer)
        buffer.appendLine("$indent }")
        return buffer
    }
}

abstract class StepableDSLElement(name: String) : DSLElement(name) {
    fun setStep(step: Int) {
        this.getPropertyOrNull("step")?.let {
            it.value = "$step"
        } ?: run {
            this.children.add(DSLProperty("step", "$step"))
        }
    }

    fun getStepOrNull() = this.getPropertyOrNull("step")
}

abstract class UndoRedoDSLElement(name: String) : DSLElement(name) {
    var undoRedoCursor = 0 //the number of children we keep during dump
    var historyLength: Int = this.children.size
        get() = this.children.size

    fun decreaseUndoRedoCursor() {
        (this.children.get(undoRedoCursor - 1) as? DSLElement)?.getStep()?.let { s ->
            undoRedoCursor -= this.children.filterIsInstance<DSLElement>().count { it.getStep() == s }
        } ?: run {
            undoRedoCursor--
        }

    }

    fun increaseUndoRedoCursor() {
        (this.children.get(undoRedoCursor) as? DSLElement)?.getStep()?.let { s ->
            undoRedoCursor += this.children.filterIsInstance<DSLElement>().count { it.getStep() == s }
        } ?: run {
            undoRedoCursor++
        }

    }

    var lastStep = 0
        get() = this.children.filterIsInstance<DSLElement>().filter { it.getStep() != null }.lastOrNull()?.getStep()
            ?: 0

    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        children.subList(0, undoRedoCursor).forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
        return buffer
    }

    fun addChild(child: DSLNode) {
        if (undoRedoCursor <= this.children.size - 1) {// this means that the user add a new step from here and is not intesrested in the next steps stored
            this.children.removeAll(this.children.subList(undoRedoCursor, this.children.size))
            undoRedoCursor = this.children.size
        }
        this.children.add(child)
        undoRedoCursor++
    }

    /**
     * Remove former children with the same name, location and types
     */
    fun cleanHistory() {
        this.children.removeIf { this.children.indexOf(it) + 1 > undoRedoCursor } //we remove the children after the current position of the undoRedoCursor as chosen by the user
        val childrenCopy = this.children.subList(0, this.children.size)
        childrenCopy.reversed().forEach { childCopy ->
            if (this.children.contains(childCopy)) {
                val children2Remove =
                    this.children.subList(0, this.children.indexOf(childCopy)).reversed().filter { formerChild ->
                        when (formerChild) {
                            is DSLElement -> when (childCopy) {
                                is DSLElement -> formerChild.name.equals(childCopy.name) && formerChild.inside(
                                    childCopy.getLocationOrNull()?.toLocation(),
                                    childCopy.getTypesOrNull()?.value
                                )

                                else -> false
                            }

                            else -> when (childCopy) {
                                is DSLElement -> false
                                else -> formerChild.name.equals(childCopy.name)
                            }
                        }
                    }
                this.children.removeIf { children2Remove.contains(it) }
            }
        }
        this.undoRedoCursor = this.children.size
        //all the remaining children will correspond to the step 1 now
        this.children.filterIsInstance<StepableDSLElement>().forEach {
            it.setStep(1)
        }
    }
}

class LayoutEl : UndoRedoDSLElement("layout") {
    fun addJunction(junctionEl: JunctionEl? = null): JunctionEl {
        val el = junctionEl ?: JunctionEl()
        this.addChild(el)
        return el
    }

    fun getFormerLayoutInHistory(): List<Layout> {
        val layouts = mutableListOf<Layout>()
        if (this.undoRedoCursor > 0) {
            val formerCursorPosition = this.undoRedoCursor
            this.decreaseUndoRedoCursor()
            children.filterIsInstance<JunctionEl>()
                .subList(formerCursorPosition - (formerCursorPosition - undoRedoCursor), formerCursorPosition)
                .forEach { j ->
                    //we search if this junction has already been modified before in the history
                    if (undoRedoCursor == 0) { //nothing before, default layout
                        layouts.add(layout {
                            junction {
                                j.getLocationOrNull()?.let {
                                    location {
                                        it.toLocation().blocks.forEach {
                                            it.start to it.end
                                        }
                                    }
                                }
                                j.getNameOrNull()?.let {
                                    name = it.value
                                }
                                j.getTypeOrNull()?.let {
                                    type = it.value.toInt()
                                }
                            }
                        })
                    } else {
                        children.subList(0, undoRedoCursor).filterIsInstance<JunctionEl>().filter {
                            it.getNameOrNull()?.value?.equals(j.getNameOrNull()?.value) ?: (j.getNameOrNull() == null) &&
                                    it.getLocationOrNull()?.toLocation()
                                        ?.equals(
                                            j.getLocationOrNull()?.toLocation()
                                        ) ?: (j.getLocationOrNull() == null) &&
                                    it.getTypeOrNull()?.value?.equals(j.getTypeOrNull()?.value) ?: (j.getTypeOrNull() == null)

                        }.lastOrNull()?.let { j ->
                            //We got the previous layout for this junction, we apply it to erase the current one
                            layouts.add(layout {
                                junction {
                                    j.getLocationOrNull()?.let {
                                        location {
                                            it.toLocation().blocks.forEach {
                                                it.start to it.end
                                            }
                                        }
                                    }
                                    j.getNameOrNull()?.let {
                                        name = it.value
                                    }
                                    j.getTypeOrNull()?.let {
                                        type = it.value.toInt()
                                    }
                                    j.getOutIdsOrNull()?.let {
                                        out_ids = it.value
                                    }
                                    j.getRadiusOrNull()?.let {
                                        radius = it.value.toDouble()
                                    }
                                }
                            })
                        } ?: run {
                            //if no previous layout for this junction, its layout is set back to its initial parameters
                            //by applying a layout with empty parameters, the RNArtistCore drawing engine will know to come back to initial parameters for this junction
                            layouts.add(layout {
                                junction {
                                    j.getLocationOrNull()?.let {
                                        location {
                                            it.toLocation().blocks.forEach {
                                                it.start to it.end
                                            }
                                        }
                                    }
                                    j.getNameOrNull()?.let {
                                        name = it.value
                                    }
                                    j.getTypeOrNull()?.let {
                                        type = it.value.toInt()
                                    }
                                }
                            })
                        }
                    }
                }
        }
        return layouts
    }

    fun getNextLayoutInHistory(): Layout? {
        if (this.undoRedoCursor < this.children.size) {
            val formerCursorPosition = this.undoRedoCursor
            this.increaseUndoRedoCursor()
            val layout = layout {
                children.subList(undoRedoCursor - (undoRedoCursor - formerCursorPosition), undoRedoCursor)
                    .filterIsInstance<JunctionEl>().forEach { j ->
                    junction {
                        j.getLocationOrNull()?.let {
                            location {
                                it.toLocation().blocks.forEach {
                                    it.start to it.end
                                }
                            }
                        }
                        j.getNameOrNull()?.let {
                            name = it.value
                        }
                        j.getTypeOrNull()?.let {
                            type = it.value.toInt()
                        }
                        j.getOutIdsOrNull()?.let {
                            out_ids = it.value
                        }
                        j.getRadiusOrNull()?.let {
                            radius = it.value.toDouble()
                        }
                    }

                }
            }
            return layout
        }
        return null
    }

    fun getLayoutInHistoryFromNextToEnd(): Layout? {
        if (this.undoRedoCursor < this.children.size) {
            val layout = layout {
                children.subList(undoRedoCursor, children.size).filterIsInstance<JunctionEl>().forEach { j ->
                    junction {
                        j.getLocationOrNull()?.let {
                            location {
                                it.toLocation().blocks.forEach {
                                    it.start to it.end
                                }
                            }
                        }
                        j.getNameOrNull()?.let {
                            name = it.value
                        }
                        j.getTypeOrNull()?.let {
                            type = it.value.toInt()
                        }
                        j.getOutIdsOrNull()?.let {
                            out_ids = it.value
                        }
                        j.getRadiusOrNull()?.let {
                            radius = it.value.toDouble()
                        }
                    }

                }
            }
            this.undoRedoCursor = this.children.size
            return layout
        }
        return null
    }

    fun toLayout(): Layout {
        return layout {
            children.subList(0, undoRedoCursor).filterIsInstance<JunctionEl>().forEach { j ->
                junction {
                    j.getLocationOrNull()?.let {
                        location {
                            it.toLocation().blocks.forEach {
                                it.start to it.end
                            }
                        }
                    }
                    j.getNameOrNull()?.let {
                        name = it.value
                    }
                    j.getTypeOrNull()?.let {
                        type = it.value.toInt()
                    }
                    j.getOutIdsOrNull()?.let {
                        out_ids = it.value
                    }
                    j.getRadiusOrNull()?.let {
                        radius = it.value.toDouble()
                    }
                }

            }

        }
    }
}

class JunctionEl : StepableDSLElement("junction") {

    fun setName(name: String) {
        this.children.add(StringDSLProperty("name", name))
    }

    fun getNameOrNull() = this.getPropertyOrNull("name")

    fun setRadius(radius: Double) {
        this.children.add(DSLProperty("radius", "$radius"))
    }

    fun getRadiusOrNull() = this.getPropertyOrNull("radius")

    fun setType(type: Int) {
        this.children.add(DSLProperty("type", "$type"))
    }

    fun getTypeOrNull() = this.getPropertyOrNull("type")

    fun setOutIds(value: String) {
        this.children.add(StringDSLProperty("out_ids", value))
    }

    fun getOutIdsOrNull() = this.getPropertyOrNull("out_ids")
}

class ThemeEl() : UndoRedoDSLElement("theme") {

    fun addDetails(detailsEl: DetailsEl? = null): DetailsEl {
        val el = detailsEl ?: DetailsEl()
        this.addChild(el)
        return el
    }

    fun addScheme(schemeEl: SchemeEl? = null): SchemeEl {
        val el = schemeEl ?: SchemeEl()
        this.addChild(el)
        return el
    }

    fun addColor(colorEl: ColorEl? = null): ColorEl {
        val el = colorEl ?: ColorEl()
        this.addChild(el)
        return el
    }

    fun getColors() = this.getChildren("color")

    /*fun getColorsByLocationAndTypes(location: Location? = null, types: String? = null): List<ColorEl> =
        this.getChildrenByLocationAndTypes("color", location, types).map { it as ColorEl }*/

    fun addLine(lineEl: LineEl? = null): LineEl {
        val el = lineEl ?: LineEl()
        this.addChild(el)
        return el
    }

    fun getLines() = this.getChildren("line")

    /*fun getLinesByLocationAndTypes(location: Location? = null, types: String? = null): List<LineEl> =
        this.getChildrenByLocationAndTypes("line", location, types).map { it as LineEl }*/

    fun addShow(showEl: ShowEl? = null): ShowEl {
        val el = showEl ?: ShowEl()
        this.addChild(el)
        return el
    }

    fun getShows() = this.getChildren("show")

    /*fun getShowsByLocationAndTypes(location: Location? = null, types: String? = null): List<ShowEl> =
        this.getChildrenByLocationAndTypes("show", location, types).map { it as ShowEl }*/

    fun addHide(hideEl: HideEl? = null): HideEl {
        val el = hideEl ?: HideEl()
        this.addChild(el)
        return el
    }

    fun getHides() = this.getChildren("hide")

    fun getFormerThemeInHistory(): Theme? {
        return if (this.undoRedoCursor > 1) { // if at 1, then we will return null, meaning clear theme
            this.decreaseUndoRedoCursor()
            this.toTheme()
        } else {
            this.undoRedoCursor = 0
            null
        }
    }

    fun getNextThemeInHistory(): Theme? {
        if (this.undoRedoCursor < this.children.size) {
            this.increaseUndoRedoCursor()
            return this.toTheme()
        }
        return null
    }

    fun getThemeInHistoryFromNextToEnd(): Theme? {
        if (this.undoRedoCursor < this.children.size) {
            this.undoRedoCursor = this.children.size
            return this.toTheme()
        }
        return null
    }


    fun toTheme(): Theme {
        return theme {
            children.subList(0, undoRedoCursor).forEach {
                when (it) {

                    is DetailsEl -> {
                        details {
                            it.getValueOrNull()?.let {
                                value = it.value.toInt()
                            }
                            it.getStepOrNull()?.let {
                                step = it.value.toInt()
                            }
                        }
                    }

                    is SchemeEl -> {
                        scheme {
                            it.getValueOrNull()?.let {
                                value = it.value
                            }
                            it.getStepOrNull()?.let {
                                step = it.value.toInt()
                            }
                        }
                    }

                    is ColorEl -> {
                        color {
                            it.getLocationOrNull()?.let {
                                location {
                                    it.toLocation().blocks.forEach {
                                        it.start to it.end
                                    }
                                }
                            }
                            it.getTypeOrNull()?.let {
                                type = it.value
                            }
                            it.getValueOrNull()?.let {
                                value = it.value
                            }
                            it.getStepOrNull()?.let {
                                step = it.value.toInt()
                            }
                            it.getToOrNull()?.let {
                                to = it.value
                            }
                        }
                    }

                    is LineEl -> {
                        line {
                            it.getLocationOrNull()?.let {
                                location {
                                    it.toLocation().blocks.forEach {
                                        it.start to it.end
                                    }
                                }
                            }
                            it.getTypeOrNull()?.let {
                                type = it.value
                            }
                            it.getValueOrNull()?.let {
                                value = it.value.toDouble()
                            }
                        }
                    }

                    is ShowEl -> {
                        show {
                            it.getLocationOrNull()?.let {
                                location {
                                    it.toLocation().blocks.forEach {
                                        it.start to it.end
                                    }
                                }
                            }
                            it.getTypeOrNull()?.let {
                                type = it.value
                            }
                        }
                    }

                    is HideEl -> {
                        hide {
                            it.getLocationOrNull()?.let {
                                location {
                                    it.toLocation().blocks.forEach {
                                        it.start to it.end
                                    }
                                }
                            }
                            it.getTypeOrNull()?.let {
                                type = it.value
                            }
                        }
                    }
                }
            }
        }
    }

    /*fun getHidesByLocationAndTypes(location: Location? = null, types: String? = null): List<HideEl> =
        this.getChildrenByLocationAndTypes("hide", location, types).map { it as HideEl }*/
}

class SSEl : DSLElement("ss") {

    fun addBracketNotation(bnEl: BracketNotationEl? = null): BracketNotationEl {
        val el = bnEl ?: BracketNotationEl()
        this.children.add(el)
        return el
    }

    fun addVienna(viennaEl: ViennaEl? = null): ViennaEl {
        val el = viennaEl ?: ViennaEl()
        this.children.add(el)
        return el
    }

    fun getViennaOrNew(): ViennaEl = this.getChildOrNull("vienna") as? ViennaEl ?: addVienna()

    fun getViennaOrNull(): ViennaEl? = this.getChildOrNull("vienna") as? ViennaEl

    fun addBPSeq(bpSeqEl: BPSeqEl? = null): BPSeqEl {
        val el = bpSeqEl ?: BPSeqEl()
        this.children.add(el)
        return el
    }

    fun getBPSeqOrNew(): BPSeqEl = this.getChildOrNull("bpseq") as? BPSeqEl ?: addBPSeq()

    fun getBPSeqOrNull(): BPSeqEl? = this.getChildOrNull("bpseq") as? BPSeqEl

    fun addCT(ctEl: CTEl? = null): CTEl {
        val el = ctEl ?: CTEl()
        this.children.add(el)
        return el
    }

    fun getCTOrNew(): CTEl = this.getChildOrNull("ct") as? CTEl ?: addCT()

    fun getCTOrNull(): CTEl? = this.getChildOrNull("ct") as? CTEl

    fun addPDB(pdbEl: PDBEl? = null): PDBEl {
        val el = pdbEl ?: PDBEl()
        this.children.add(el)
        return el
    }

    fun getPDBOrNew(): PDBEl = this.getChildOrNull("pdb") as? PDBEl ?: addPDB()

    fun getPDBOrNull(): PDBEl? = this.getChildOrNull("pdb") as? PDBEl

}

class BracketNotationEl : DSLElement("bn") {
    fun setSeq(seq: String) {
        this.getPropertyOrNull("seq")?.let {
            it.value = seq
        } ?: run {
            this.children.add(StringDSLProperty("seq", seq))
        }
    }

    fun setValue(value: String) {
        this.getPropertyOrNull("value")?.let {
            it.value = value
        } ?: run {
            this.children.add(StringDSLProperty("seq", value))
        }
    }

    fun setName(name: String) {
        this.getPropertyOrNull("name")?.let {
            it.value = name
        } ?: run {
            this.children.add(StringDSLProperty("name", name))
        }
    }
}

abstract class InputEl(name: String) : DSLElement(name) {
    fun setFile(file: String) {
        this.getPropertyOrNull("file")?.let {
            it.value = file
        } ?: run {
            this.children.add(StringDSLProperty("file", file))
        }
    }

    fun setPath(path: String) {
        this.getPropertyOrNull("path")?.let {
            it.value = path
        } ?: run {
            this.children.add(StringDSLProperty("path", path))
        }
    }

    fun getFile() = this.getPropertyOrNull("file")
}

class ViennaEl : InputEl("vienna")

class StockholmEl : InputEl("stockholm")

class CTEl : InputEl("ct")

class BPSeqEl : InputEl("bpseq")

class PDBEl : InputEl("pdb") {
    fun setId(id: String) {
        this.getPropertyOrNull("id")?.let {
            it.value = id
        } ?: run {
            this.children.add(StringDSLProperty("id", id))
        }
    }

    fun setName(name: String) {
        this.getPropertyOrNull("name")?.let {
            it.value = name
        } ?: run {
            this.children.add(StringDSLProperty("name", name))
        }
    }
}

abstract class OutputFileEl(name: String) : DSLElement(name) {

    fun setPath(path: String) {
        this.getPropertyOrNull("path")?.let {
            it.value = path
        } ?: run {
            this.children.add(StringDSLProperty("path", path))
        }
    }

    fun setName(name: String) {
        this.getPropertyOrNull("name")?.let {
            it.value = name
        } ?: run {
            this.children.add(StringDSLProperty("name", name))
        }
    }

    fun setWidth(width: Double) {
        this.getPropertyOrNull("width")?.let {
            it.value = "$width"
        } ?: run {
            this.children.add(DSLProperty("width", "$width"))
        }
    }

    fun setHeight(height: Double) {
        this.getPropertyOrNull("height")?.let {
            it.value = "$height"
        } ?: run {
            this.children.add(DSLProperty("height", "$height"))
        }
    }
}

class PNGEl : OutputFileEl("png")

class SVGEl : OutputFileEl("svg")

abstract class ThemeConfigurationEl(name: String) : StepableDSLElement(name) {
    fun setType(type: String) {
        this.getPropertyOrNull("type")?.let {
            it.value = type
        } ?: run {
            this.children.add(StringDSLProperty("type", type))
        }
    }

    fun getTypeOrNull() = this.getPropertyOrNull("type")
}

class SchemeEl : ThemeConfigurationEl("scheme") {
    fun setValue(value: String) {
        this.getPropertyOrNull("value")?.let {
            it.value = value
        } ?: run {
            this.children.add(StringDSLProperty("value", value))
        }
    }

    fun getValueOrNull() = this.getPropertyOrNull("value")
}

class DetailsEl : ThemeConfigurationEl("details") {
    fun setValue(value: Int) {
        this.getPropertyOrNull("value")?.let {
            it.value = "$value"
        } ?: run {
            this.children.add(DSLProperty("value", "$value"))
        }
    }

    fun getValueOrNull() = this.getPropertyOrNull("value")
}

class ShowEl : ThemeConfigurationEl("show")

class HideEl : ThemeConfigurationEl("hide")

class ColorEl : ThemeConfigurationEl("color") {

    fun setValue(value: String) {
        this.getPropertyOrNull("value")?.let {
            it.value = value
        } ?: run {
            this.children.add(StringDSLProperty("value", value))
        }
    }

    fun getValueOrNull() = this.getPropertyOrNull("value")

    fun setTo(to: String) {
        this.getPropertyOrNull("to")?.let {
            it.value = to
        } ?: run {
            this.children.add(StringDSLProperty("to", to))
        }
    }

    fun getToOrNull() = this.getPropertyOrNull("to")

}

class LineEl : ThemeConfigurationEl("line") {
    fun setValue(value: Double) {
        this.getPropertyOrNull("value")?.let {
            it.value = "$value"
        } ?: run {
            this.children.add(DSLProperty("value", "$value"))
        }
    }

    fun getValueOrNull() = this.getPropertyOrNull("value")

}

class LocationEl : DSLElement("location") {

    fun addBlock(start: Int, end: Int) {
        this.children.add(DSLProperty("$start", "$end", operator = "to"))
    }

    fun setLocation(l: Location) = l.blocks.forEach { this.addBlock(it.start, it.end) }

    fun toLocation(): Location =
        Location(this.getProperties().map {
            "${it.name}:${it.value.toInt() - it.name.toInt() + 1}"
        }.joinToString(separator = ","))
}

/**
 * This function is used to map each DrawingElement in a list into a pair containing:
 * - the type as the string to be saved in the script
 * - the location to be saved in the script. If the location is null, all elements of the same type in the 2D are stored in the list.
 * Several tests are made to be able to apply the script on different RNA 2Ds :
 * - if all the junctions/helices/... are selected, then no location is saved (saying apply on all junctions whatever its location)
 * - if all residues in all junctions are selected, then no location is saved and the type is "N@junction"
 * - and so on....
 *
 * The SecondaryStructureDrawing parameter is needed to check if all the helices, junctions, residues in helices,... are selected in order to reduce the description to save
 **/
fun dumpIntoTypeAndLocation(
    elements: List<DrawingElement>,
    secondaryStructureDrawing: SecondaryStructureDrawing
): List<Pair<String, Location?>> {
    val selectedTypes = mutableListOf<Pair<String, Location?>>()
    val typesDone = mutableListOf<String>() //trick to avoid to do the job for each element
    elements.forEach { element ->
        if (element is HelixDrawing) {
            if (!typesDone.contains("helix")) {
                val drawingElements = elements.filter { it is HelixDrawing }
                if (drawingElements.size == secondaryStructureDrawing.allHelices.size)
                    selectedTypes.add(Pair("helix", null))
                else
                    selectedTypes.add(Pair(
                        "helix",
                        Location(drawingElements.flatMap { it.location.positions }
                            .toIntArray())
                    ))
                typesDone.add("helix")
            }
        } else if (element is JunctionDrawing) {

            if (element.junctionType == JunctionType.ApicalLoop) {
                if (!typesDone.contains("apical_loop")) {
                    val drawingElements =
                        elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.ApicalLoop }
                    if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }.size)
                        selectedTypes.add(Pair("apical_loop", null))
                    else
                        selectedTypes.add(Pair(
                            "apical_loop",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("apical_loop")
                }
            } else if (element.junctionType == JunctionType.InnerLoop) {
                if (!typesDone.contains("inner_loop")) {
                    val drawingElements =
                        elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.InnerLoop }
                    if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }.size)
                        selectedTypes.add(Pair("inner_loop", null))
                    else
                        selectedTypes.add(Pair(
                            "inner_loop",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("inner_loop")
                }
            } else if (element.junctionType == JunctionType.ThreeWay) {
                if (!typesDone.contains("3_way")) {
                    val drawingElements =
                        elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.ThreeWay }
                    if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }.size)
                        selectedTypes.add(Pair("3_way", null))
                    else
                        selectedTypes.add(Pair(
                            "3_way",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("3_way")
                }
            } else if (element.junctionType == JunctionType.FourWay) {
                if (!typesDone.contains("4_way")) {
                    val drawingElements =
                        elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.FourWay }
                    if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }.size)
                        selectedTypes.add(Pair("4_way", null))
                    else
                        selectedTypes.add(Pair(
                            "4_way",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("4_way")
                }
            } else {
                if (!typesDone.contains("junction")) {
                    val drawingElements = elements.filter { it is JunctionDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allJunctions.size)
                        selectedTypes.add(Pair("junction", null))
                    else
                        selectedTypes.add(Pair(
                            "junction",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("junction")
                }
            }

        } else if (element is SingleStrandDrawing) {
            if (!typesDone.contains("single_strand")) {
                val drawingElements = elements.filter { it is SingleStrandDrawing }
                if (drawingElements.size == secondaryStructureDrawing.allSingleStrands.size)
                    selectedTypes.add(Pair("single_strand", null))
                else
                    selectedTypes.add(Pair(
                        "single_strand",
                        Location(drawingElements.flatMap { it.location.positions }
                            .toIntArray())
                    ))
                typesDone.add("single_strand")
            }
        } else if (element is PhosphodiesterBondDrawing) {
            if (element.parent is JunctionDrawing) {
                if ((element.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop) {
                    if (!typesDone.contains("phosphodiester_bond@apical_loop")) {
                        val drawingElements =
                            elements.filter { it is PhosphodiesterBondDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.ApicalLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }
                                .sumOf { it.phosphoBonds.size })
                            selectedTypes.add(Pair("phosphodiester_bond@apical_loop", null))
                        else
                            selectedTypes.add(Pair(
                                "phosphodiester_bond@apical_loop",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("phosphodiester_bond@apical_loop")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop) {
                    if (!typesDone.contains("phosphodiester_bond@inner_loop")) {
                        val drawingElements =
                            elements.filter { it is PhosphodiesterBondDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.InnerLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }
                                .sumOf { it.phosphoBonds.size })
                            selectedTypes.add(Pair("phosphodiester_bond@inner_loop", null))
                        else
                            selectedTypes.add(Pair(
                                "phosphodiester_bond@inner_loop",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("phosphodiester_bond@inner_loop")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay) {
                    if (!typesDone.contains("phosphodiester_bond@3_way")) {
                        val drawingElements =
                            elements.filter { it is PhosphodiesterBondDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.ThreeWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }
                                .sumOf { it.phosphoBonds.size })
                            selectedTypes.add(Pair("phosphodiester_bond@3_way", null))
                        else
                            selectedTypes.add(Pair(
                                "phosphodiester_bond@3_way",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("phosphodiester_bond@3_way")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.FourWay) {
                    if (!typesDone.contains("phosphodiester_bond@4_way")) {
                        val drawingElements =
                            elements.filter { it is PhosphodiesterBondDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.FourWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }
                                .sumOf { it.phosphoBonds.size })
                            selectedTypes.add(Pair("phosphodiester_bond@4_way", null))
                        else
                            selectedTypes.add(Pair(
                                "phosphodiester_bond@4_way",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@4_way")
                    }
                } else {
                    if (!typesDone.contains("N@junction")) {
                        val drawingElements =
                            elements.filter { it is PhosphodiesterBondDrawing && it.parent is JunctionDrawing }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.sumOf { it.phosphoBonds.size }) {
                            selectedTypes.add(Pair("phosphodiester_bond@junction", null))
                        } else
                            selectedTypes.add(Pair(
                                "phosphodiester_bond@junction",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("phosphodiester_bond@junction")
                    }
                }
            } else if (element.parent is HelixDrawing) {
                if (!typesDone.contains("phosphodiester_bond@helix")) {
                    val drawingElements =
                        elements.filter { it is PhosphodiesterBondDrawing && element.parent is HelixDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allHelices.sumOf { it.phosphoBonds.size }) {
                        selectedTypes.add(Pair("phosphodiester_bond@helix", null))
                    } else
                        selectedTypes.add(Pair(
                            "phosphodiester_bond@helix",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("phosphodiester_bond@helix")
                }
            } else if (element.parent is SingleStrandDrawing) {
                if (!typesDone.contains("phosphodiester_bond@single_strand")) {
                    val drawingElements =
                        elements.filter { it is PhosphodiesterBondDrawing && element.parent is SingleStrandDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allSingleStrands.sumOf { it.phosphoBonds.size }) {
                        selectedTypes.add(Pair("phosphodiester_bond@single_strand", null))
                    } else
                        selectedTypes.add(Pair(
                            "phosphodiester_bond@single_strand",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("phosphodiester_bond@single_strand")
                }
            } else {
                if (!typesDone.contains("phosphodiester_bond")) {
                    val drawingElements = elements.filter { it is PhosphodiesterBondDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allPhosphoBonds.size)
                        selectedTypes.add(Pair("phosphodiester_bond", null))
                    else
                        selectedTypes.add(Pair(
                            "phosphodiester_bond",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("phosphodiester_bond")
                }
            }
        } else if (element is SecondaryInteractionDrawing) {
            if (!typesDone.contains("secondary_interaction")) {
                val drawingElements = elements.filter { it is SecondaryInteractionDrawing }
                if (drawingElements.size == secondaryStructureDrawing.allSecondaryInteractions.size)
                    selectedTypes.add(Pair("secondary_interaction", null))
                else
                    selectedTypes.add(Pair(
                        "secondary_interaction",
                        Location(drawingElements.flatMap { it.location.positions }
                            .toIntArray())
                    ))
                typesDone.add("secondary_interaction")
            }
        } else if (element is InteractionSymbolDrawing) {
            if (!typesDone.contains("interaction_symbol")) {
                val drawingElements = elements.filter { it is InteractionSymbolDrawing }
                if (drawingElements.size == secondaryStructureDrawing.allDefaultSymbols.size)
                    selectedTypes.add(Pair("interaction_symbol", null))
                else
                    selectedTypes.add(Pair(
                        "interaction_symbol",
                        Location(drawingElements.flatMap { it.location.positions }
                            .toIntArray())
                    ))
                typesDone.add("interaction_symbol")
            }
        } else if (element is ResidueDrawing) {
            if (element.parent is SecondaryInteractionDrawing) {
                if (!typesDone.contains("N@helix")) {
                    val drawingElements =
                        elements.filter { it is ResidueDrawing && it.parent is SecondaryInteractionDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allHelices.sumOf { it.length * 2 }) {
                        selectedTypes.add(Pair("N@helix", null))
                    } else
                        selectedTypes.add(Pair(
                            "N@helix",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("N@helix")
                }
            } else if (element.parent is JunctionDrawing) {
                if ((element.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop) {
                    if (!typesDone.contains("N@apical_loop")) {
                        val drawingElements =
                            elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.ApicalLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }
                                .sumOf { it.location.length })
                            selectedTypes.add(Pair("N@apical_loop", null))
                        else
                            selectedTypes.add(Pair(
                                "N@apical_loop",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@apical_loop")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop) {
                    if (!typesDone.contains("N@inner_loop")) {
                        val drawingElements =
                            elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.InnerLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }
                                .sumOf { it.location.length })
                            selectedTypes.add(Pair("N@inner_loop", null))
                        else
                            selectedTypes.add(Pair(
                                "N@inner_loop",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@inner_loop")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay) {
                    if (!typesDone.contains("N@3_way")) {
                        val drawingElements =
                            elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.ThreeWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }
                                .sumOf { it.location.length })
                            selectedTypes.add(Pair("N@3_way", null))
                        else
                            selectedTypes.add(Pair(
                                "N@3_way",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@3_way")
                    }
                } else if ((element.parent as JunctionDrawing).junctionType == JunctionType.FourWay) {
                    if (!typesDone.contains("N@4_way")) {
                        val drawingElements =
                            elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.FourWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }
                                .sumOf { it.location.length })
                            selectedTypes.add(Pair("N@4_way", null))
                        else
                            selectedTypes.add(Pair(
                                "N@4_way",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@4_way")
                    }
                } else {
                    if (!typesDone.contains("N@junction")) {
                        val drawingElements = elements.filter { it is ResidueDrawing && it.parent is JunctionDrawing }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.sumOf { it.location.length }) {
                            selectedTypes.add(Pair("N@junction", null))
                        } else
                            selectedTypes.add(Pair(
                                "N@junction",
                                Location(drawingElements.flatMap { it.location.positions }
                                    .toIntArray())
                            ))
                        typesDone.add("N@junction")
                    }
                }
            } else if (element.parent is SingleStrandDrawing) {
                if (!typesDone.contains("N@single_strand")) {
                    val drawingElements = elements.filter { it is ResidueDrawing && it.parent is SingleStrandDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.allSingleStrands.sumOf { it.location.length }) {
                        selectedTypes.add(Pair("N@single_strand", null))
                    } else
                        selectedTypes.add(Pair(
                            "N@single_strand",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("N@single_strand")
                }
            } else {
                if (!typesDone.contains("N")) {
                    val drawingElements = elements.filter { it is ResidueDrawing }
                    if (drawingElements.size == secondaryStructureDrawing.secondaryStructure.rna.length)
                        selectedTypes.add(Pair("N", null))
                    else
                        selectedTypes.add(Pair(
                            "N",
                            Location(drawingElements.flatMap { it.location.positions }
                                .toIntArray())
                        ))
                    typesDone.add("N")
                }
            }
        } else if (element is ResidueLetterDrawing) {
            if (!typesDone.contains("n")) {
                val drawingElements = elements.filter { it is ResidueLetterDrawing }
                if (drawingElements.size == secondaryStructureDrawing.secondaryStructure.rna.length)
                    selectedTypes.add(Pair("n", null))
                else
                    selectedTypes.add(Pair(
                        "n",
                        Location(drawingElements.flatMap { it.location.positions }
                            .toIntArray())
                    ))
                typesDone.add("n")
            }
        }
    }
    return selectedTypes
}

/**
 * This function removes the unecessary types to show according to the details level in the script
 */
fun clearToBeShown(lvl: Int?, element: DSLElement): String? {
    /*val detailsLvl = lvl ?: 1
    val types = element.getTypeOrNull()?.split(" ")
    val typesDisplayedAtThisLevel = when(detailsLvl) {
        1 -> listOf("helix", "junction", "single_strand")
        2 -> listOf("helix", "junction", "single_strand")
        3 -> listOf("helix", "junction", "single_strand")
        4 -> listOf("helix", "junction", "single_strand")
        else -> listOf("helix", "junction", "single_strand")
    }
    types.dropWhile { it in typesDisplayedAtThisLevel }*/
    return null
}