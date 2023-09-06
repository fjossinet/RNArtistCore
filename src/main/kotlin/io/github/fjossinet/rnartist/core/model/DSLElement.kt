package io.github.fjossinet.rnartist.core.model

import java.awt.Color


fun setDetailsLvlForFull2D(rnArtistEl: RNArtistEl, lvl: Int) {
    rnArtistEl.getThemeOrNew().setDetails(lvl)
}

fun setSchemeForFull2D(rnArtistEl: RNArtistEl, scheme: String) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.getColors().forEach { el ->
        theme.removeChild(el)
    }
    theme.setScheme(scheme)
}

fun setColorForFull2D(rnArtistEl: RNArtistEl, fontOnly: Boolean = false, color: Color) {
    val theme = rnArtistEl.getThemeOrNew()
    if (fontOnly) {
        theme.getColors().forEach { el ->
            if (el.properties.size == 2 && el.properties.contains("value") && el.properties.contains("type") && (el.properties["type"] == "n" || el.properties["type"]!!.split(" ").all { it in mutableListOf("a","u","g","c","x") }))
                theme.removeChild(el)
        }
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
        colorEl.setType("n")
    } else {
        theme.getColors().forEach { el ->
            theme.removeChild(el)
        }
        val colorEl = theme.addColor()
        colorEl.setValue(getHTMLColorString(color))
    }
}

fun setColor(
    rnArtistEl: RNArtistEl,
    fontOnly: Boolean = false,
    color: Color,
    location: Location? = null,
    type: List<SecondaryStructureType>? = null
) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.getColors(location, type).firstOrNull()?.let { colorEl ->
        colorEl.setValue(getHTMLColorString(color))
        location?.let {//the colorEl could have been catched thanks to a different location but containing the colorEl. So we need to update its location
            val l = colorEl.getLocationOrNew()
            it.blocks.forEach {
                l.addBlock(it.start, it.end)
            }
        }
    } ?: run {
        with(theme.addColor()) {
            this.setValue(getHTMLColorString(color))
            location?.let { l ->
                with(this.addLocation()) {
                    l.blocks.forEach {
                        this.addBlock(it.start, it.end)
                    }
                }
            }
            type?.let { l ->
                this.setType(type.map { it.toString() }.joinToString(separator = " "))
            }
        }
    }
}

fun setLineWidthForFull2D(rnArtistEl: RNArtistEl, width: Double) {
    rnArtistEl.getThemeOrNew().getLines().forEach { el ->
        if (el.properties.size == 1 && el.properties.contains("value") && el.children.size == 0)
            rnArtistEl.getThemeOrNew().removeChild(el)
    }
    rnArtistEl.getThemeOrNew().addLine().setValue(width)
}

fun setLineWidth(rnArtistEl: RNArtistEl, width: Double, location: Location? = null, type: List<SecondaryStructureType>? = null) {
    val theme = rnArtistEl.getThemeOrNew()
    theme.getLines(location, type).firstOrNull()?.let { lineEl ->
        lineEl.setValue(width)
        location?.let {//the lineEl could have been catched thanks to a different location but containing the lineEl. So we need to update its location
            val l = lineEl.getLocationOrNew()
            it.blocks.forEach {
                l.addBlock(it.start, it.end)
            }
        }
    } ?: run {
        with(theme.addLine()) {
            this.setValue(width)
            location?.let { l ->
                with(this.addLocation()) {
                    l.blocks.forEach {
                        this.addBlock(it.start, it.end)
                    }
                }
            }
            type?.let { l ->
                this.setType(type.map { it.toString() }.joinToString(separator = " "))
            }
        }
    }
}

abstract class DSLElement(val name: String) {
    val children = mutableListOf<DSLElement>()
    val properties = mutableMapOf<String, String?>()
    open fun dump(indent: String = "", buffer: StringBuffer = StringBuffer()): StringBuffer {
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        properties.forEach { key, value ->
            value?.let {
                buffer.appendLine("$newIndent $key = $value")
            }
        }
        children.forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
        return buffer
    }

    protected fun getChild(name: String) = this.children.filter { it.name.equals(name) }.firstOrNull()


    protected fun getChild(name: String, propName: String, propValue: String) =
        this.children.filter { it.name.equals(name) && it.properties.contains(propName) && it.properties[propName]!! == propValue }
            .firstOrNull()

    protected fun getChildren(
        name: String,
        location: Location? = null,
        type: List<SecondaryStructureType>? = null,
        children: MutableList<DSLElement> = mutableListOf(),
    ): List<DSLElement> {
        //first we get all the children with that name
        this.children.forEach { child ->
            if (child.name.equals(name)) {
                children.add(child)
                location?.let { l1 ->
                    child.getLocationOrNUll()?.let { l2 ->
                        if (l1 != l2.toLocation() && !l1.contains(l2.toLocation())) //it is the same element if the location is the same, or if the new location contains the current location stored in this element
                            children.removeFirstOrNull()
                    } ?: run {
                        children.removeFirstOrNull()
                    }
                }
                type?.let { t1 ->
                    child.getTypeOrNull()?.let { t2 ->
                        val childTypes = t2.removePrefix("\"").removeSuffix("\"").split(" ")
                        val newTypes = t1.map { it.toString() }
                        if (!childTypes.containsAll(newTypes) || !newTypes.containsAll(childTypes))
                            children.removeFirstOrNull()
                    } ?: run {
                        children.removeFirstOrNull()
                    }
                }
            }
        }
        return children
    }

    fun removeChild(child: DSLElement) {
        this.children.remove(child)
    }

    fun addLocation(locationEl: LocationEl? = null): LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }

    fun getLocationOrNew(): LocationEl = this.getChild("location") as? LocationEl ?: addLocation()

    fun addType(type: List<SecondaryStructureType>) {
        this.properties["type"] = type.map { it.toString() }.joinToString(separator = " ")
    }

    fun getTypeOrNull() = this.properties.getOrDefault("type", null)

    protected fun getLocationOrNUll(): LocationEl? = this.getChild("location") as LocationEl?

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

    fun addSVG(svgEl: SVGEl? = null): SVGEl {
        this.children.removeIf { it.name.equals("svg") } //only a single element allowed
        val el = svgEl ?: SVGEl()
        this.children.add(el)
        return el
    }

    fun getSVGOrNew(): SVGEl = this.getChild("svg") as? SVGEl ?: addSVG()
}

class LayoutEl : DSLElement("layout") {

}

class ThemeEl : DSLElement("theme") {

    fun setDetails(details: Int) {
        this.properties["details"] = "$details"
    }

    fun setScheme(scheme: String) {
        this.properties["scheme"] = "\"$scheme\""
    }

    fun addColor(colorEl: ColorEl? = null): ColorEl {
        val el = colorEl ?: ColorEl()
        this.children.add(el)
        return el
    }

    fun getColors(location: Location? = null, type: List<SecondaryStructureType>? = null): List<ColorEl> =
        this.getChildren("color", location, type).map { it as ColorEl }

    fun addLine(lineEl: LineEl? = null): LineEl {
        val el = lineEl ?: LineEl()
        this.children.add(el)
        return el
    }

    fun getLines(location: Location? = null, type: List<SecondaryStructureType>? = null): List<LineEl> =
        this.getChildren("line", location, type).map { it as LineEl }

    fun addShow(showEl: ShowEl? = null): ShowEl {
        val el = showEl ?: ShowEl()
        this.children.add(el)
        return el
    }

    fun getShows(): List<ShowEl> = this.getChildren("show").map { it as ShowEl }

    fun addHide(hideEl: HideEl? = null): HideEl {
        val el = hideEl ?: HideEl()
        this.children.add(el)
        return el
    }

    fun getHides(): List<HideEl> = this.getChildren("hide").map { it as HideEl }

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
        this.properties["seq"] = "\"$seq\""
    }

    fun setValue(value: String) {
        this.properties["value"] = "\"$value\""
    }

    fun setName(name: String) {
        this.properties["name"] = "\"$name\""
    }
}

class ViennaEl : DSLElement("vienna") {
    fun setFile(file: String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }
}

class StockholmEl : DSLElement("stockholm") {
    fun setFile(file: String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }
}

class CTEl : DSLElement("ct") {
    fun setFile(file: String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }
}

class BPSeqEl : DSLElement("ct") {
    fun setFile(file: String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }
}

class PNGEl : DSLElement("png") {
    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }

    fun setName(name: String) {
        this.properties["name"] = "\"$name\""
    }

    fun setWidth(width: Double) {
        this.properties["width"] = "$width"
    }

    fun setHeight(height: Double) {
        this.properties["height"] = "$height"
    }
}

class SVGEl : DSLElement("svg") {
    fun setPath(path: String) {
        this.properties["path"] = "\"$path\""
    }

    fun setName(name: String) {
        this.properties["name"] = "\"$name\""
    }

    fun setWidth(width: Double) {
        this.properties["width"] = "$width"
    }

    fun setHeight(height: Double) {
        this.properties["height"] = "$height"
    }
}

class ShowEl : DSLElement("show")

class HideEl : DSLElement("hide")

class ColorEl : DSLElement("color") {
    fun setScheme(scheme: String) {
        this.properties["scheme"] = "\"$scheme\""
    }

    fun setType(type: String) {
        this.properties["type"] = "\"$type\""
    }

    fun setValue(value: String) {
        this.properties["value"] = "\"$value\""
    }

    fun setTo(to: String) {
        this.properties["to"] = "\"$to\""
    }

}

class LineEl : DSLElement("line") {
    fun setValue(value: Double) {
        this.properties["value"] = "$value"
    }

    fun setType(type: String) {
        this.properties["type"] = "\"$type\""
    }
}

class LocationEl : DSLElement("location") {

    //a location DSL element dumps its properties with the "to" instead of "=
    override fun dump(indent: String, buffer: StringBuffer): StringBuffer {
        buffer.appendLine("$indent $name {")
        val newIndent = "$indent   "
        val blockStart = properties.keys.map { it.removePrefix("\"").removeSuffix("\"").toInt() }.toList().sorted()
        blockStart.forEach { start->
            buffer.appendLine("$newIndent $start to ${properties["$start"]}")
        }
        children.forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
        return buffer
    }

    fun addBlock(start: Int, end: Int) {
        this.properties["$start"] = "$end"
    }

    fun toLocation(): Location =
        Location(this.properties.map {
            "${it.key}:${it.value!!.toInt() - it.key.toInt() + 1}"
        }.joinToString(separator = ","))
}