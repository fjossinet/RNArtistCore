package io.github.fjossinet.rnartist.core

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.io.getUserDir
import io.github.fjossinet.rnartist.core.io.randomColor
import io.github.fjossinet.rnartist.core.model.*
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.awt.Color
import java.io.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.SimpleDateFormat
import java.util.*

object RnartistConfig {

    @JvmField
    val colorSchemes: Map<String, Map<(DrawingElement) -> Boolean, (DrawingElement) -> String>> = mapOf(
        "Structural Domains" to mapOf(
            { e:DrawingElement ->  e is HelixDrawing } to
                    { e:DrawingElement ->  getHTMLColorString( randomColor() )},
            { e:DrawingElement ->  e is SecondaryInteractionDrawing  } to
                    { e:DrawingElement ->  getHTMLColorString(e.parent!!.getColor())},
            { e:DrawingElement -> e is ResidueDrawing && e.parent is SecondaryInteractionDrawing } to
                    { e:DrawingElement ->  getHTMLColorString(e.parent!!.getColor())},
            { e:DrawingElement -> e is JunctionDrawing} to
                    { e:DrawingElement ->  getHTMLColorString( randomColor() )},
            { e:DrawingElement -> e is ResidueDrawing && e.parent is JunctionDrawing } to
                    { e:DrawingElement ->  getHTMLColorString(e.parent!!.getColor())},
            { e:DrawingElement -> e is SingleStrandDrawing } to
                    { e:DrawingElement ->  getHTMLColorString( randomColor() )},
            { e:DrawingElement -> e is ResidueDrawing && e.parent is SingleStrandDrawing } to
                    { e:DrawingElement ->  getHTMLColorString(e.parent!!.getColor())}
        ),
        "Persian Carolina" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#D741A7"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#3A1772"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#5398BE"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement -> getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#F2CD5D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Snow Lavender" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#A31621"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#FCF7F8"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#CED3DC"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#4E8098"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Fuzzy French" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#731DD8"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#48A9A6"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#E4DFDA"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#D4B483"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Chestnut Navajo" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#CA2E55"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#FFE0B5"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#8A6552"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#462521"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Irresistible Turquoise" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#9D44B5"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#B5446E"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#525252"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#BADEFC"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Charm Jungle" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#E08DAC"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#6A7FDB"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#57E2E5"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#45CB85"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Atomic Xanadu" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#EF946C"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#C4A77D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#70877F"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#454372"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Pale Coral" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#987284"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#75B9BE"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#D0D6B5"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#F9B5AC"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Maximum Salmon" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#301A4B"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#6DB1BF"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#FFEAEC"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#F39A9D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
        ),
        "Pacific Dream" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#42F2F7"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#46ACC2"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#498C8A"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#4B6858"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "New York Camel" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#ECC8AF"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#E7AD99"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#CE796B"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#C18C5D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Screamin' Olive" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#494947"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#35FF69"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#44CCFF"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#7494EA"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Baby Lilac" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#9D858D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#BBA0B2"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#A4A8D1"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#A4BFEB"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Celeste Olivine" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#9CFFFA"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#ACF39D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#B0C592"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#A97C73"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Midnight Paradise" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#EF476F"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#FFD166"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#06D6A0"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#118AB2"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "African Lavender" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  "#D8D8F6"},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#B18FCF"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#978897"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#494850"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Charcoal Lazuli" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#2F4858"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#33658A"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#86BBD8"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#F6AE2D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        ),
        "Pumpkin Vegas" to mapOf(
            { e:DrawingElement -> e.type == SecondaryStructureType.AShape } to
                    { e:DrawingElement ->  "#FA7921"},
            { e:DrawingElement -> e.type == SecondaryStructureType.A } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.UShape } to
                    { e:DrawingElement ->  "#FE9920"},
            { e:DrawingElement -> e.type == SecondaryStructureType.U } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.GShape } to
                    { e:DrawingElement ->  "#B9A44C"},
            { e:DrawingElement -> e.type == SecondaryStructureType.G } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.CShape } to
                    { e:DrawingElement ->  "#566E3D"},
            { e:DrawingElement -> e.type == SecondaryStructureType.C } to
                    { e:DrawingElement ->  getHTMLColorString(Color.WHITE)},
            { e:DrawingElement -> e.type == SecondaryStructureType.XShape } to
                    { e:DrawingElement ->  getHTMLColorString(Color.LIGHT_GRAY)},
            { e:DrawingElement -> e.type == SecondaryStructureType.X } to
                    { e:DrawingElement ->  getHTMLColorString(Color.BLACK)}
        )
    )

    @JvmStatic
    private var document: Document? = null

    val defaultConfiguration = mutableMapOf(
        ThemeParameter.color.toString() to getHTMLColorString(Color.DARK_GRAY),
        ThemeParameter.linewidth.toString() to "1.0",
        ThemeParameter.lineshift.toString() to "1.0",
        ThemeParameter.opacity.toString() to "255", //alpha value goes from 0 to 255
        ThemeParameter.fulldetails.toString() to "false"
    )

    @JvmStatic
    fun load() {
        if (document != null) return
        val configFile = File(getUserDir(), "config.xml")
        if (configFile.exists()) {
            val builder = SAXBuilder()
            try {
                document = builder.build(configFile)
            } catch (e: JDOMException) {
                e.printStackTrace()
            }
        } else {
            val root = Element("rnartist-config")
            root.setAttribute(
                "release",
                getRnartistRelease()
            )
            document = Document(root)
        }
    }

    @JvmStatic
    fun save() {
        val outputter = XMLOutputter(Format.getPrettyFormat())
        val writer = FileWriter(File(getUserDir(), "config.xml"))
        outputter.output(document, writer)
    }

    @JvmStatic
    fun clearRecentFiles() {
        val e = document!!.rootElement.getChild("recent-files")
        e?.removeChildren("file")
    }

    @JvmStatic
    var projectsFolder:String?
        get() {
            var e = document!!.rootElement.getChild("projects-folder")
            if (e == null) {
                return null
            }
            return e.text
        }
        set(folder) {
            var e = document!!.rootElement.getChild("projects-folder")
            if (e == null) {
                e = Element("projects-folder")
                document!!.rootElement.addContent(e)
            }
            e.text = folder
        }

    data class GlobalProperties(var website: Map<String,String>)

    @JvmStatic
    private fun recoverWebsite(status:String="dev") {
        val client: HttpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://raw.githubusercontent.com/fjossinet/RNArtist/master/properties.json"))
            .build()
            val response: HttpResponse<String> = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )
            val properties = Gson().fromJson<GlobalProperties>(response.body() as String, GlobalProperties::class.java)
            website = "http://${properties.website[status]}"
    }

    @JvmStatic
    var website: String? = null

    @JvmStatic
    var fragmentsLibrary: String?
        get() {
            var e = document!!.rootElement.getChild("fragments-library")
            if (e == null) {
                e = Element("fragments-library")
                e.text = "Non redundant"
                document!!.rootElement.addContent(e)
            }
            return e.textTrim
        }
        set(library) {
            document!!.rootElement.getChild("fragments-library").text = library
        }

    @JvmStatic
    val recentEntries: List<Pair<String, String>>
        get() {
            var e = document!!.rootElement.getChild("recent-entries")
            if (e == null) {
                e = Element("recent-entries")
                document!!.rootElement.addContent(e)
            }
            val files: MutableList<Pair<String, String>> = ArrayList()
            UPPERFOR@ for (o in e.getChildren("entry")) {
                val entry = o
                for (f in files) if (f.first == entry!!.getAttributeValue("id") && f.second == entry.getAttributeValue(
                        "type"
                    )
                ) {
                    continue@UPPERFOR
                }
                files.add(Pair(entry!!.getAttributeValue("id"), entry.getAttributeValue("type")))
            }
            document!!.rootElement.removeContent(e)
            e = Element("recent-entries")
            document!!.rootElement.addContent(e)
            for (f in files) {
                val file = Element("entry")
                file.setAttribute("id", f.first)
                file.setAttribute("type", f.second)
                e.addContent(file)
            }
            return files
        }

    @JvmStatic
    fun addRecentEntry(id: String, type: String) {
        var e = document!!.rootElement.getChild("recent-entries")
        if (e == null) {
            e = Element("recent-entries")
            document!!.rootElement.addContent(e)
        }
        val file = Element("entry")
        file.setAttribute("id", id)
        file.setAttribute("type", type)
        val files: List<*> = ArrayList(e.getChildren("entry"))
        if (files.size == 10) {
            e.removeContent(files[files.size - 1] as Element?)
            e.addContent(0, file)
        } else {
            for (o in files) {
                val _f = o as Element
                if (_f.getAttributeValue("id") == id && _f.getAttributeValue("type") == type) {
                    e.removeContent(_f)
                }
            }
            e.addContent(0, file)
        }
    }

    @JvmStatic
    var chimeraHost: String
        get() {
            var e = document!!.rootElement.getChild("external-tools")
            if (e == null) {
                e = Element("external-tools")
                document!!.rootElement.addContent(e)
            }
            var _e = e.getChild("chimera")
            if (_e == null) {
                _e = Element("chimera")
                _e.addContent(Element("isX"))
                _e.addContent(Element("host"))
                _e.addContent(Element("port"))
                e.addContent(_e)
                _e.getChild("isX").text = "false"
                _e.getChild("host").text = "127.0.0.1"
                _e.getChild("port").text = "50000"
            }

            return _e.getChild("host").value
        }
        set(host) {
            document!!.rootElement.getChild("external-tools").getChild("chimera").getChild("host").text = host
        }

    @JvmStatic
    var chimeraPort: Int
        get() {
            var e = document!!.rootElement.getChild("external-tools")
            if (e == null) {
                e = Element("external-tools")
                document!!.rootElement.addContent(e)
            }
            var _e = e.getChild("chimera")
            if (_e == null) {
                _e = Element("chimera")
                _e.addContent(Element("isX"))
                _e.addContent(Element("host"))
                _e.addContent(Element("port"))
                e.addContent(_e)
                _e.getChild("isX").text = "false"
                _e.getChild("host").text = "127.0.0.1"
                _e.getChild("port").text = "50000"
            }

            return Integer.parseInt(_e.getChild("port").value)
        }
        set(port) {
            document!!.rootElement.getChild("external-tools").getChild("chimera").getChild("port").text = port.toString()
        }

    @JvmStatic
    var isChimeraX: Boolean
        get() {
            var e = document!!.rootElement.getChild("external-tools")
            if (e == null) {
                e = Element("external-tools")
                document!!.rootElement.addContent(e)
            }
            var _e = e.getChild("chimera")
            if (_e == null) {
                _e = Element("chimera")
                _e.addContent(Element("isX"))
                _e.addContent(Element("host"))
                _e.addContent(Element("port"))
                e.addContent(_e)
                _e.getChild("isX").text = "false"
                _e.getChild("host").text = "127.0.0.1"
                _e.getChild("port").text = "50000"
            }

            if (_e.getChild("isX") == null) { //retrocompatibility
                _e.addContent(Element("isX"))
                _e.getChild("isX").text = "false"
            }

            return _e.getChild("isX") != null && _e.getChild("isX").value.equals("true")
        }
        set(isX) {
            document!!.rootElement.getChild("external-tools").getChild("chimera").getChild("isX").text = isX.toString()
        }

    @JvmStatic
    var userID: String?
        get() = document!!.rootElement.getChild("userID")?.text
        set(userID) {
            var e: Element? = document!!.rootElement.getChild("userID")
            if (e == null) {
                e = Element("userID")
                document!!.rootElement.addContent(e)
            }
            e.addContent(userID)
        }

    @JvmStatic
    fun exportSVGWithBrowserCompatibility(): Boolean {
        val e = document?.rootElement?.getChild("export-SVG-with-browser-compatibility")
        return e != null
    }

    @JvmStatic
    fun exportSVGWithBrowserCompatibility(compatibility: Boolean) {
        if (compatibility && !exportSVGWithBrowserCompatibility() /*the element is not already there*/)
            document!!.rootElement.addContent(Element("export-SVG-with-browser-compatibility"))
        else if (!compatibility)
            document!!.rootElement.removeChild("export-SVG-with-browser-compatibility")
    }

    @JvmStatic
    var editorFontSize:Int
        get() {
            var e: Element? = document!!.rootElement.getChild("editor-fontsize")
            if (e == null) {
                e = Element("editor-fontsize")
                e.text = "20"
                document!!.rootElement.addContent(e)
            }
            return e.value.toInt()
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("editor-fontsize")
            if (e == null) {
                e = Element("editor-fontsize")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = value.toString()
        }

    @JvmStatic
    var editorFontName:String
        get() {
            var e: Element? = document!!.rootElement.getChild("editor-fontname")
            if (e == null) {
                e = Element("editor-fontname")
                e.text = "Arial"
                document!!.rootElement.addContent(e)
            }
            return e.value
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("editor-fontname")
            if (e == null) {
                e = Element("editor-fontname")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = value
        }

    @JvmStatic
    var backgroundEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("bg-editor-color")
            if (e == null) {
                e = Element("bg-editor-color")
                e.text = getHTMLColorString(Color.BLACK)
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("bg-editor-color")
            if (e == null) {
                e = Element("bg-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var keywordEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("kw-editor-color")
            if (e == null) {
                e = Element("kw-editor-color")
                e.text = "#8099ff"
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("kw-editor-color")
            if (e == null) {
                e = Element("bg-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var bracesEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("braces-editor-color")
            if (e == null) {
                e = Element("braces-editor-color")
                e.text = getHTMLColorString(Color.WHITE)
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("braces-editor-color")
            if (e == null) {
                e = Element("braces-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var keyParamEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("keyParam-editor-color")
            if (e == null) {
                e = Element("keyParam-editor-color")
                e.text = "#ff9966"
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("keyParam-editor-color")
            if (e == null) {
                e = Element("keyParam-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var operatorParamEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("operatorParam-editor-color")
            if (e == null) {
                e = Element("operatorParam-editor-color")
                e.text = getHTMLColorString(Color.WHITE)
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("operatorParam-editor-color")
            if (e == null) {
                e = Element("operatorParam-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var valueParamEditorColor:Color
        get() {
            var e: Element? = document!!.rootElement.getChild("valueParam-editor-color")
            if (e == null) {
                e = Element("valueParam-editor-color")
                e.text = "#669999"
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("valueParam-editor-color")
            if (e == null) {
                e = Element("valueParam-editor-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var selectionColor: Color
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-color")
            if (e == null) {
                e = Element("selection-color")
                e.text = getHTMLColorString(Color.RED)
                document!!.rootElement.addContent(e)
            }
            return getAWTColor(e.value)
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-color")
            if (e == null) {
                e = Element("selection-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = getHTMLColorString(value)
        }

    @JvmStatic
    var selectionWidth: Int
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-width")
            if (e == null) {
                e = Element("selection-width")
                e.text = "1"
                document!!.rootElement.addContent(e)
            }
            return e.value.toInt()
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-width")
            if (e == null) {
                e = Element("selection-width")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = "${value}"
        }

    @JvmStatic
    fun getRnartistRelease(): String? {
        return try {
            val format = SimpleDateFormat("MMM dd, yyyy", Locale("EN","en"))
            "RNArtist Development Release (" + format.format(Calendar.getInstance().time) + ")"
        } catch (e: java.lang.Exception) {
            null
        }
    }

    @JvmStatic
    fun isDockerInstalled():Boolean {
        return try {
            val pb  = ProcessBuilder("docker");
            val p = pb.start();
            val result = InputStreamReader(p.errorStream).buffered().use(BufferedReader::readText);
            Regex("Usage:  docker").containsMatchIn(result.trim())
        } catch (e:Exception ) {
            false;
        }
    }

    @JvmStatic
    fun isDockerImageInstalled():Boolean {
        return try {
            val pb  = ProcessBuilder("docker", "images");
            val p = pb.start();
            val result = InputStreamReader(p.getInputStream()).buffered().use(BufferedReader::readText);
            "fjossinet/rnartistcore".toRegex().containsMatchIn(result.trim())
        } catch (e:Exception ) {
            false;
        }
    }
}