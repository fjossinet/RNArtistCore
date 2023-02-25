package io.github.fjossinet.rnartist.core

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.io.getUserDir
import io.github.fjossinet.rnartist.core.io.randomColor
import io.github.fjossinet.rnartist.core.model.*
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

    val defaultConfiguration = mutableMapOf(
        ThemeParameter.color.toString() to getHTMLColorString(Color.DARK_GRAY),
        ThemeParameter.linewidth.toString() to "1.0",
        ThemeParameter.lineshift.toString() to "1.0",
        ThemeParameter.opacity.toString() to "255", //alpha value goes from 0 to 255
        ThemeParameter.fulldetails.toString() to "false"
    )

    data class GlobalProperties(var website: Map<String,String>)

    @JvmStatic
    var website: String? = null

    @JvmStatic
    fun exportSVGWithBrowserCompatibility(): Boolean {
        /*val e = document?.rootElement?.getChild("export-SVG-with-browser-compatibility")
        return e != null*/
        return false
    }

    @JvmStatic
    fun exportSVGWithBrowserCompatibility(compatibility: Boolean) {
        /*if (compatibility && !exportSVGWithBrowserCompatibility() /*the element is not already there*/)
            document!!.rootElement.addContent(Element("export-SVG-with-browser-compatibility"))
        else if (!compatibility)
            document!!.rootElement.removeChild("export-SVG-with-browser-compatibility")*/
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