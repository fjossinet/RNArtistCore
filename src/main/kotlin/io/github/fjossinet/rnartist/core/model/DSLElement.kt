package io.github.fjossinet.rnartist.core.model

abstract class DSLElement(val name:String) {
    val children = mutableListOf<DSLElement>()
    val properties = mutableMapOf<String,String?>()
    open fun dump(indent:String = "", buffer:StringBuffer) {
        buffer.appendLine("$indent $name {")
        val newIndent =  "$indent   "
        properties.forEach { key, value ->
            value?.let {
                buffer.appendLine("$newIndent $key = $value")
            }
        }
        children.forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
    }

    protected fun getChild(name:String): DSLElement? {
        if (this.name.equals(name))
            return this
        else
            this.children.forEach { child ->
                child.getChild(name)?.let {
                    return it
                }
            }
        return null
    }

    protected fun getChild(name:String, propName:String, propValue:String): DSLElement? {
        if (this.name.equals(name) && this.properties.contains(propName) && this.properties[propName]!! == propValue)
            return this
        else
            this.children.forEach { child ->
                child.getChild(name, propName, propValue)?.let {
                    return it
                }
            }
        return null
    }

    protected fun getChildren(name:String, children:MutableList<DSLElement> = mutableListOf()): List<DSLElement> {
        if (this.name.equals(name))
            children.add(this)
        else
            this.children.forEach { child ->
                child.getChildren(name, children)
            }
        return children.toList()
    }

    fun removeChild(child:DSLElement) {
        this.children.remove(child)
    }
}

class RNArtistEl:DSLElement("rnartist") {

    fun addLayoutEl(layoutEl:LayoutEl? = null):LayoutEl {
        this.children.removeIf { it.name.equals("layout") } //only a single element allowed
        val el = layoutEl ?: LayoutEl()
        this.children.add(el)
        return el
    }

    fun getLayoutEl():LayoutEl  = this.getChild("layout") as? LayoutEl ?: addLayoutEl()

    fun addThemeEl(themeEl:ThemeEl? = null):ThemeEl {
        this.children.removeIf { it.name.equals("theme") } //only a single element allowed
        val el = themeEl ?: ThemeEl()
        this.children.add(el)
        return el
    }

    fun getThemeEl():ThemeEl  = this.getChild("theme") as? ThemeEl ?: addThemeEl()

    fun addSSEl(ssEl:SSEl? = null):SSEl {
        this.children.removeIf { it.name.equals("ss") } //only a single element allowed
        val el = ssEl ?: SSEl()
        this.children.add(el)
        return el
    }

    fun getSSEl():SSEl  = this.getChild("ss") as? SSEl ?: addSSEl()

    fun addPNGEl(pngEl:PNGEl? = null):PNGEl {
        this.children.removeIf { it.name.equals("png") } //only a single element allowed
        val el = pngEl ?: PNGEl()
        this.children.add(el)
        return el
    }

    fun getPNGEl():PNGEl = this.getChild("png") as? PNGEl ?: addPNGEl()

    fun addSVGEl(svgEl:SVGEl? = null):SVGEl {
        this.children.removeIf { it.name.equals("svg") } //only a single element allowed
        val el = svgEl ?: SVGEl()
        this.children.add(el)
        return el
    }
}

class LayoutEl:DSLElement("layout") {

}

class ThemeEl:DSLElement("theme") {

    fun setDetails(details:Int) {
        this.properties["details"] = "$details"
    }

    fun setScheme(scheme:String) {
        this.properties["scheme"] = "\"$scheme\""
    }

    fun addColorEl(colorEl: ColorEl? = null):ColorEl {
        val el = colorEl ?: ColorEl()
        this.children.add(el)
        return el
    }

    fun getColorEl():List<ColorEl>  = this.getChildren("color").map { it as ColorEl }

    fun addLineEl(lineEl: LineEl? = null):LineEl {
        val el = lineEl ?: LineEl()
        this.children.add(el)
        return el
    }

    fun getLineEl():List<LineEl>  = this.getChildren("line").map { it as LineEl }

    fun addShowEl(showEl: ShowEl? = null):ShowEl {
        val el = showEl ?: ShowEl()
        this.children.add(el)
        return el
    }

    fun getShowEl():List<ShowEl>  = this.getChildren("show").map { it as ShowEl }

    fun addHideEl(hideEl: HideEl? = null):HideEl {
        val el = hideEl ?: HideEl()
        this.children.add(el)
        return el
    }

    fun getHideEl():List<HideEl>  = this.getChildren("hide").map { it as HideEl }

}

class SSEl:DSLElement("ss") {

    fun addBracketNotationEl(bnEl:BracketNotationEl? = null):BracketNotationEl {
        val el = bnEl ?: BracketNotationEl()
        this.children.add(el)
        return el
    }

    fun addViennaEl(viennaEl:ViennaEl? = null):ViennaEl {
        val el = viennaEl ?: ViennaEl()
        this.children.add(el)
        return el
    }

    fun getViennaEl():ViennaEl = this.getChild("vienna") as? ViennaEl ?: addViennaEl()

    fun addBPSeqEl(bpSeqEl:BPSeqEl? = null):BPSeqEl {
        val el = bpSeqEl ?: BPSeqEl()
        this.children.add(el)
        return el
    }

    fun addCTEl(ctEl:CTEl? = null):CTEl {
        val el = ctEl ?: CTEl()
        this.children.add(el)
        return el
    }

}

class BracketNotationEl:DSLElement("bn") {
    fun setSeq(seq:String) {
        this.properties["seq"] = "\"$seq\""
    }

    fun setValue(value:String) {
        this.properties["value"] = "\"$value\""
    }

    fun setName(name:String) {
        this.properties["name"] = "\"$name\""
    }
}

class ViennaEl:DSLElement("vienna") {
    fun setFile(file:String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }
}

class StockholmEl:DSLElement("stockholm") {
    fun setFile(file:String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }
}

class CTEl:DSLElement("ct") {
    fun setFile(file:String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }
}

class BPSeqEl:DSLElement("ct") {
    fun setFile(file:String) {
        this.properties["file"] = "\"$file\""
    }

    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }
}

class PNGEl:DSLElement("png") {
    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }

    fun setName(name:String) {
        this.properties["name"] = "\"$name\""
    }

    fun setWidth(width:Double) {
        this.properties["width"] = "$width"
    }

    fun setHeight(height:Double) {
        this.properties["height"] = "$height"
    }
}

class SVGEl:DSLElement("svg") {
    fun setPath(path:String) {
        this.properties["path"] = "\"$path\""
    }

    fun setName(name:String) {
        this.properties["name"] = "\"$name\""
    }

    fun setWidth(width:Double) {
        this.properties["width"] = "$width"
    }

    fun setHeight(height:Double) {
        this.properties["height"] = "$height"
    }
}

class ShowEl:DSLElement("show") {
    fun addLocationEl(locationEl:LocationEl? = null):LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }
}

class HideEl:DSLElement("hide") {
    fun addLocationEl(locationEl:LocationEl? = null):LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }
}

class ColorEl:DSLElement("color") {
    fun setScheme(scheme:String) {
        this.properties["scheme"] = "\"$scheme\""
    }

    fun setType(type:String) {
        this.properties["type"] = "\"$type\""
    }

    fun setValue(value:String) {
        this.properties["value"] = "\"$value\""
    }

    fun setTo(to:String) {
        this.properties["to"] = "\"$to\""
    }

    fun addLocationEl(locationEl:LocationEl? = null):LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }

}

class LineEl:DSLElement("line") {
    fun setValue(value:Double) {
        this.properties["value"] = "$value"
    }

    fun addLocationEl(locationEl:LocationEl? = null):LocationEl {
        val el = locationEl ?: LocationEl()
        this.children.add(el)
        return el
    }
}

class LocationEl:DSLElement("location") {
    fun setValue(value:Int) {
        this.properties["value"] = "$value"
    }

    //a location DSL element dumps its properties with the "to" instead of "=
    override fun dump(indent:String, buffer:StringBuffer) {
        buffer.appendLine("$indent $name {")
        val newIndent =  "$indent   "
        properties.forEach { key, value ->
            value?.let {
                buffer.appendLine("$newIndent $key to $value")
            }
        }
        children.forEach { child ->
            child.dump(newIndent, buffer)
        }
        buffer.appendLine("$indent }")
    }

    fun setBlock(start:Int, end:Int) {
        this.properties["$start"] = "$end"
    }
}