package io.github.fjossinet.rnartist.core.model

import java.awt.Color

fun setJunction(rnArtistEl: RNArtistEl, radius: Double? = null, outIds:String? = null, type:Int? = null, location: Location? = null) {
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
    }
}

fun setDetailsLvlForFull2D(rnArtistEl: RNArtistEl, lvl: Int) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.addDetails(lvl)
}

fun setDetailsLvl(rnArtistEl: RNArtistEl, isfullDetails: Boolean, rnaLength:Int, location: Location? = null, types: String? = null) {
    val theme = rnArtistEl.getThemeOrNew()
    if (isfullDetails) {
        with(theme.addShow()) {
            location?.let {
                this.addLocation().setLocation(it)
            }
            types?.let {
                this.setType(it)
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
            this
        }
    }
}

fun setSchemeForFull2D(rnArtistEl: RNArtistEl, scheme: String) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.addScheme(scheme)
}

fun setColorForFull2D(rnArtistEl: RNArtistEl, onFont: Boolean = false, color: Color) {
    val theme = rnArtistEl.getThemeOrNew()
    if (onFont) {
        val types = "n"
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
        colorEl.setType(types)
    } else {
        val types =
            "helix secondary_interaction single_strand junction phosphodiester_bond interaction_symbol N tertiary_interaction"
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
        colorEl.setType(types)
    }
}

fun setColor(
    rnArtistEl: RNArtistEl,
    color: Color,
    location: Location? = null,
    types: String? = null
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
    }
}

/**
 * Remove all the line elements and create a new one.
 */
fun setLineWidthForFull2D(rnArtistEl: RNArtistEl, width: Double) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.addLine().setValue(width)
}

fun setLineWidth(rnArtistEl: RNArtistEl, width: Double, location: Location? = null, types: String? = null) {
    val theme = rnArtistEl.getThemeOrNew()
    with(theme.addLine()) {
        this.setValue(width)
        location?.let {
            this.addLocation().setLocation(it)
        }
        types?.let {
            this.setType(it)
        }
    }
}

abstract class DSLNode(val name: String) {
    abstract fun dump(indent: String = "", buffer: StringBuffer = StringBuffer()): StringBuffer
}

open class Property(name:String, val value:String, val operator:String = "="):DSLNode(name) {
    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent ${this.name} ${this.operator} ${this.value}")
        return buffer
    }
}

class StringProperty(name:String, value:String, operator:String = "="):Property(name, value, operator) {
    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent ${this.name} ${this.operator} \"${this.value}\"")
        return buffer
    }
}

abstract class DSLElement(name: String):DSLNode(name) {
    val children = mutableListOf<DSLNode>()

    fun getProperties():List<Property> = this.children.filterIsInstance<Property>()

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
    /*fun inside(location:Location?, types:String?):Boolean {
        var sameLocation = inside(location)
        val sameTypes =  inside(types)
        //if sametypes but the new location is null (meaning we target all the types whatever the location) and the location in child not null, it is inside
        if (sameTypes && location == null)
            sameLocation = true
        return sameLocation && sameTypes
    }

    fun inside(location:Location?):Boolean {
        var sameLocation = location == null && this.getLocationOrNUll() == null
        location?.let { l1 ->
            this.getLocationOrNUll()?.let { l2 ->
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

    fun inside(types:String?):Boolean {
        var sameTypes = types == null && this.getTypeOrNull() == null
        types?.let { t1 ->
            this.getTypeOrNull()?.let { t2 ->
                sameTypes = (t2.split(" ")
                    .all { it in t1.split(" ") }) //if all the types of the current element are described in the new element, same element that will be needed to be replaced
            }
        }
        return sameTypes
    }*/


    /**
     * Return the first child using its name as criteria
     */
    protected fun getChild(name: String) = this.children.filter { it.name.equals(name) }.firstOrNull()

    /**
     * Return children using their name as criteria
     */
    protected fun getChildren(name: String) = this.children.filter { it.name == name }

    /**
     * Return children with the same name and :
     * - same location: if the location given as argument is same or larger than the location in the child, they are is the same
     * - same types: if the types given as argument is contains all the types in the child (and perhaps more), they are the same
     * If location as argument AND in the child are both null, they are the same
     * If types as argument AND in the child are both null, they are the same
     * If types are the same and location as argument is null, they are the same. Location null means all the elements of these types whatever their location (so whatever the location in the child, the null location larger)
     */
    /*protected fun getChildrenByLocationAndTypes(
        name: String,
        location: Location? = null,
        types: String? = null,
        children: MutableList<DSLElement> = mutableListOf(),
    ): List<DSLElement> {
        //first we get all the children with that name
        this.children.forEach { child ->
            if (child.name.equals(name)) {
                children.add(child)
                if (!child.inside(location, types))
                    children.removeLastOrNull()
            }
        }
        return children
    }*/

    fun removeChild(child: DSLNode) {
        this.children.remove(child)
    }

    fun addLocation(locationEl: LocationEl? = null): LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }

    fun removeLocation() {
        this.getChild("location")?.let {
            this.removeChild(it)
        }
    }

    fun getLocationOrNew(): LocationEl = this.getChild("location") as? LocationEl ?: addLocation()

    fun getLocationOrNUll(): LocationEl? = this.getChild("location") as LocationEl?

    fun addStringProperty(name:String, value:String, operator: String="=") = this.children.add(StringProperty(name,value, operator))

    fun addProperty(name:String, value:Double, operator: String="=") = this.children.add(Property(name,"$value", operator))
    fun addProperty(name:String, value:Int, operator: String="=") = this.children.add(Property(name,"$value", operator))

}

class RNArtistEl : DSLElement("rnartist") {

    fun addLayout(layoutEl: LayoutEl? = null): LayoutEl {
        this.children.removeIf { it.name.equals("layout") } //only a single element allowed
        val el = layoutEl ?: LayoutEl()
        this.children.add(el)
        return el
    }

    fun getLayoutOrNew(): LayoutEl = this.getChild("layout") as? LayoutEl ?: addLayout()

    fun addTheme(themeEl: ThemeEl? = null): ThemeEl {
        this.children.removeIf { it.name.equals("theme") } //only a single element allowed
        val el = themeEl ?: ThemeEl()
        this.children.add(el)
        return el
    }

    fun getThemeOrNew(): ThemeEl = this.getChild("theme") as? ThemeEl ?: addTheme()

    fun addSS(ssEl: SSEl? = null): SSEl {
        this.children.removeIf { it.name.equals("ss") } //only a single element allowed
        val el = ssEl ?: SSEl()
        this.children.add(el)
        return el
    }

    fun getSSOrNew(): SSEl = this.getChild("ss") as? SSEl ?: addSS()

    fun addPNG(pngEl: PNGEl? = null): PNGEl {
        this.children.removeIf { it.name.equals("png") } //only a single element allowed
        val el = pngEl ?: PNGEl()
        this.children.add(el)
        return el
    }

    fun getPNGOrNew(): PNGEl = this.getChild("png") as? PNGEl ?: addPNG()

    fun getPNGOrNull(): PNGEl? = this.getChild("png") as? PNGEl

    fun addSVG(svgEl: SVGEl? = null): SVGEl {
        this.children.removeIf { it.name.equals("svg") } //only a single element allowed
        val el = svgEl ?: SVGEl()
        this.children.add(el)
        return el
    }

    fun getSVGOrNew(): SVGEl = this.getChild("svg") as? SVGEl ?: addSVG()

    fun getSVGOrNull(): PNGEl? = this.getChild("svg") as? PNGEl

    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
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

abstract class UndoRedoDSLElement(name:String):DSLElement(name) {
    var undoRedoStep = 0

    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        children.subList(0, undoRedoStep).forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
        return buffer
    }

    fun addChild(child:DSLNode) {
        if (undoRedoStep <= this.children.size-1) {// this means that the user add a new step from here and is not intesrested in the next steps stored
            this.children.removeAll(this.children.subList(undoRedoStep, this.children.size))
            undoRedoStep = this.children.size
        }
        this.children.add(child)
        undoRedoStep++

    }
}

class LayoutEl : UndoRedoDSLElement("layout") {
    fun addJunction(junctionEl: JunctionEl? = null): JunctionEl {
        val el = junctionEl ?: JunctionEl()
        this.addChild(el)
        return el
    }
}

class JunctionEl : DSLElement("junction") {

    fun setName(name: String) {
        this.children.add(StringProperty("name", name))
    }

    fun setRadius(radius: Double) {
        this.children.add(Property("radius", "$radius"))
    }

    fun setType(type: Int) {
        this.children.add(Property("type", "$type"))
    }

    fun setOutIds(value: String) {
        this.children.add(StringProperty("out_ids", value))
    }
}

class ThemeEl() : UndoRedoDSLElement("theme") {

    fun addDetails(details: Int) {
        this.addChild(Property("details", "$details"))
    }

    fun addScheme(scheme: String) {
        this.addChild(Property("scheme", "\"$scheme\""))
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

    fun getViennaOrNew(): ViennaEl = this.getChild("vienna") as? ViennaEl ?: addVienna()

    fun addBPSeq(bpSeqEl: BPSeqEl? = null): BPSeqEl {
        val el = bpSeqEl ?: BPSeqEl()
        this.children.add(el)
        return el
    }

    fun addCT(ctEl: CTEl? = null): CTEl {
        val el = ctEl ?: CTEl()
        this.children.add(el)
        return el
    }

}

class BracketNotationEl : DSLElement("bn") {
    fun setSeq(seq: String) {
        this.children.add(StringProperty("seq", seq))
    }

    fun setValue(value: String) {
        this.children.add(StringProperty("value", value))
    }

    fun setName(name: String) {
        this.children.add(StringProperty("name", name))
    }
}

class ViennaEl : DSLElement("vienna") {
    fun setFile(file: String) {
        this.children.add(StringProperty("file", file))
    }

    fun setPath(path: String) {
        this.children.add(StringProperty("path", path))
    }
}

class StockholmEl : DSLElement("stockholm") {
    fun setFile(file: String) {
        this.children.add(StringProperty("file", file))
    }

    fun setPath(path: String) {
        this.children.add(StringProperty("path", path))
    }
}

class CTEl : DSLElement("ct") {
    fun setFile(file: String) {
        this.children.add(StringProperty("file", file))
    }

    fun setPath(path: String) {
        this.children.add(StringProperty("path", path))
    }
}

class BPSeqEl : DSLElement("ct") {
    fun setFile(file: String) {
        this.children.add(StringProperty("file", file))
    }

    fun setPath(path: String) {
        this.children.add(StringProperty("path", path))
    }
}

abstract class OutputFileEl(name:String) : DSLElement(name) {

    fun setPath(path: String) {
        this.children.add(StringProperty("path", path))
    }

    fun setName(name: String) {
        this.children.add(StringProperty("name", name))
    }

    fun setWidth(width: Double) {
        this.children.add(Property("width", "$width"))
    }

    fun setHeight(height: Double) {
        this.children.add(Property("height", "$height"))
    }
}

class PNGEl : OutputFileEl("png")

class SVGEl : OutputFileEl("svg")

class ShowEl : DSLElement("show") {
    fun setType(type: String) {
        this.children.add(StringProperty("type", type))
    }
}

class HideEl : DSLElement("hide") {
    fun setType(type: String) {
        this.children.add(StringProperty("type", type))
    }

}

class ColorEl : DSLElement("color") {
    fun setScheme(scheme: String) {
        this.children.add(StringProperty("scheme", scheme))
    }

    fun setType(type: String) {
        this.children.add(StringProperty("type", type))
    }

    fun setValue(value: String) {
        this.children.add(StringProperty("value", value))
    }

    fun setTo(to: String) {
        this.children.add(StringProperty("to", to))
    }

}

class LineEl : DSLElement("line") {
    fun setValue(value: Double) {
        this.children.add(Property("value", "$value"))
    }

    fun setType(type: String) {
        this.children.add(StringProperty("type", type))
    }
}

class LocationEl : DSLElement("location") {

    fun addBlock(start: Int, end: Int) {
        this.children.add(Property("$start", "$end", operator = "to"))
    }

    fun setLocation(l:Location) = l.blocks.forEach{  this.addBlock(it.start, it.end) }

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
                    val drawingElements = elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.ApicalLoop }
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
                    val drawingElements = elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.InnerLoop }
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
                    val drawingElements = elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.ThreeWay }
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
                    val drawingElements = elements.filter { it is JunctionDrawing && it.junctionType == JunctionType.FourWay }
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
                        val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && (it.parent as?  JunctionDrawing)?.junctionType == JunctionType.ApicalLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }.sumOf{it.phosphoBonds.size})
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
                        val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && (it.parent as?  JunctionDrawing)?.junctionType == JunctionType.InnerLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }.sumOf{it.phosphoBonds.size})
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
                        val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && (it.parent as?  JunctionDrawing)?.junctionType == JunctionType.ThreeWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }.sumOf{it.phosphoBonds.size})
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
                        val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && (it.parent as?  JunctionDrawing)?.junctionType == JunctionType.FourWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }.sumOf{it.phosphoBonds.size})
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
                        val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && it.parent is JunctionDrawing }
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
                    val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && element.parent is HelixDrawing}
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
                    val drawingElements = elements.filter { it is PhosphodiesterBondDrawing && element.parent is SingleStrandDrawing}
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
            }
            else {
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
                    val drawingElements = elements.filter { it is ResidueDrawing && it.parent is SecondaryInteractionDrawing}
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
                        val drawingElements = elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.ApicalLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }.sumOf{it.location.length})
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
                        val drawingElements = elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.InnerLoop }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }.sumOf{it.location.length})
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
                        val drawingElements = elements.filter { it is ResidueDrawing && (it.parent as?  JunctionDrawing)?.junctionType == JunctionType.ThreeWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }.sumOf{it.location.length})
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
                        val drawingElements = elements.filter { it is ResidueDrawing && (it.parent as? JunctionDrawing)?.junctionType == JunctionType.FourWay }
                        if (drawingElements.size == secondaryStructureDrawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }.sumOf{it.location.length})
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
fun clearToBeShown(lvl:Int?, element:DSLElement): String? {
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