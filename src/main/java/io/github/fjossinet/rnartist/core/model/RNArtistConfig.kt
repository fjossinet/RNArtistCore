package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.model.io.getUserDir
import org.apache.commons.lang3.tuple.MutablePair
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.awt.Color
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.SimpleDateFormat
import java.util.*
import java.util.prefs.BackingStoreException

object RnartistConfig {

    @JvmField
    val defaultColorSchemes: Map<String, Map<String, String>> = mapOf(

        "Persian Carolina" to mapOf(
            ThemeParameter.AColor.toString() to "#D741A7",
            ThemeParameter.UColor.toString() to "#3A1772",
            ThemeParameter.GColor.toString() to "#5398BE",
            ThemeParameter.CColor.toString() to "#F2CD5D",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to "#4d4d4d",
            ThemeParameter.TertiaryColor.toString() to "#cc3333"
        ),

        "Snow Lavender" to mapOf(
            ThemeParameter.AColor.toString() to "#A31621",
            ThemeParameter.UColor.toString() to "#FCF7F8",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#CED3DC",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#4E8098",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#90C2E7"
        ),

        "Fuzzy French" to mapOf(
            ThemeParameter.AColor.toString() to "#731DD8",
            ThemeParameter.UColor.toString() to "#48A9A6",
            ThemeParameter.GColor.toString() to "#E4DFDA",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#D4B483",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#C1666B"
        ),

        "Chestnut Navajo" to mapOf(
            ThemeParameter.AColor.toString() to "#CA2E55",
            ThemeParameter.UColor.toString() to "#FFE0B5",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#8A6552",
            ThemeParameter.CColor.toString() to "#462521",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#BDB246"
        ),

        "Irresistible Turquoise" to mapOf(
            ThemeParameter.AColor.toString() to "#9D44B5",
            ThemeParameter.UColor.toString() to "#B5446E",
            ThemeParameter.GColor.toString() to "#525252",
            ThemeParameter.CColor.toString() to "#BADEFC",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#0AFFED"
        ),

        "Charm Jungle" to mapOf(
            ThemeParameter.AColor.toString() to "#E08DAC",
            ThemeParameter.UColor.toString() to "#6A7FDB",
            ThemeParameter.GColor.toString() to "#57E2E5",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#45CB85",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#153131"
        ),

        "Atomic Xanadu" to mapOf(
            ThemeParameter.AColor.toString() to "#EF946C",
            ThemeParameter.UColor.toString() to "#C4A77D",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#70877F",
            ThemeParameter.CColor.toString() to "#454372",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#2F2963"
        ),

        "Pale Coral" to mapOf(
            ThemeParameter.AColor.toString() to "#987284",
            ThemeParameter.UColor.toString() to "#75B9BE",
            ThemeParameter.GColor.toString() to "#D0D6B5",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#F9B5AC",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#EE7674"
        ),

        "Golden Honolulu" to mapOf(
            ThemeParameter.AColor.toString() to "#B1740F",
            ThemeParameter.UColor.toString() to "#FFD07B",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#FDB833",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#296EB4",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to "#4d4d4d",
            ThemeParameter.TertiaryColor.toString() to "#1789FC"
        ),

        "Maximum Salmon" to mapOf(
            ThemeParameter.AColor.toString() to "#301A4B",
            ThemeParameter.UColor.toString() to "#6DB1BF",
            ThemeParameter.GColor.toString() to "#FFEAEC",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#F39A9D",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#3F6C51"
        ),

        "Pacific Dream" to mapOf(
            ThemeParameter.AColor.toString() to "#42F2F7",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#46ACC2",
            ThemeParameter.GColor.toString() to "#498C8A",
            ThemeParameter.CColor.toString() to "#4B6858",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#4D4730"
        ),

        "New York Camel" to mapOf(
            ThemeParameter.AColor.toString() to "#ECC8AF",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#E7AD99",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#CE796B",
            ThemeParameter.CColor.toString() to "#C18C5D",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#495867"
        ),

        "Screamin' Olive" to mapOf(
            ThemeParameter.AColor.toString() to "#494947",
            ThemeParameter.UColor.toString() to "#35FF69",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#44CCFF",
            ThemeParameter.CColor.toString() to "#7494EA",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#D138BF"
        ),

        "Aero Green" to mapOf(
            ThemeParameter.AColor.toString() to "#FCFFFD",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#64B6AC",
            ThemeParameter.GColor.toString() to "#C0FDFB",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#DAFFEF",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#5D737E"
        ),

        "Baby Lilac" to mapOf(
            ThemeParameter.AColor.toString() to "#9D858D",
            ThemeParameter.UColor.toString() to "#BBA0B2",
            ThemeParameter.GColor.toString() to "#A4A8D1",
            ThemeParameter.CColor.toString() to "#A4BFEB",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#8CABBE"
        ),

        "Celeste Olivine" to mapOf(
            ThemeParameter.AColor.toString() to "#9CFFFA",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#ACF39D",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#B0C592",
            ThemeParameter.CColor.toString() to "#A97C73",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#AF3E4D"
        ),

        "Blood Celadon" to mapOf(
            ThemeParameter.AColor.toString() to "#A7D49B",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#92AC86",
            ThemeParameter.GColor.toString() to "#696047",
            ThemeParameter.CColor.toString() to "#55251D",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#5A1807"
        ),

        "Space Blizzard" to mapOf(
            ThemeParameter.AColor.toString() to "#25283D",
            ThemeParameter.UColor.toString() to "#8F3985",
            ThemeParameter.GColor.toString() to "#98DFEA",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#EFD9CE",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#07BEB8"
        ),

        "Midnight Paradise" to mapOf(
            ThemeParameter.AColor.toString() to "#EF476F",
            ThemeParameter.UColor.toString() to "#FFD166",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#06D6A0",
            ThemeParameter.CColor.toString() to "#118AB2",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#073B4C"
        ),

        "African Lavender" to mapOf(
            ThemeParameter.AColor.toString() to "#D8D8F6",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#B18FCF",
            ThemeParameter.GColor.toString() to "#978897",
            ThemeParameter.CColor.toString() to "#494850",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#2C2C34"
        ),

        "Charcoal Lazuli" to mapOf(
            ThemeParameter.AColor.toString() to "#2F4858",
            ThemeParameter.UColor.toString() to "#33658A",
            ThemeParameter.GColor.toString() to "#86BBD8",
            ThemeParameter.CColor.toString() to "#F6AE2D",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#F26419"
        ),

        "Tyrian Yale" to mapOf(
            ThemeParameter.AColor.toString() to "#E3B505",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#95190C",
            ThemeParameter.GColor.toString() to "#610345",
            ThemeParameter.CColor.toString() to "#107E7D",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#044B7F"
        ),

        "Cheese Cinnabar" to mapOf(
            ThemeParameter.AColor.toString() to "#EF3E36",
            ThemeParameter.UColor.toString() to "#17BEBB",
            ThemeParameter.GColor.toString() to "#2E282A",
            ThemeParameter.CColor.toString() to "#EDB88B",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#FAD8D6"
        ),

        "Razzmic Granite" to mapOf(
            ThemeParameter.AColor.toString() to "#CDF7F6",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#8FB8DE",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#9A94BC",
            ThemeParameter.CColor.toString() to "#9B5094",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#6A605C"
        ),

        "Aero Violet" to mapOf(
            ThemeParameter.AColor.toString() to "#BDEDE0",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#BBDBD1",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#B6B8D6",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#7E78D2",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#6F58C9"
        ),

        "Jet Flame" to mapOf(
            ThemeParameter.AColor.toString() to "#000000",
            ThemeParameter.UColor.toString() to "#353531",
            ThemeParameter.GColor.toString() to "#FF9505",
            ThemeParameter.CColor.toString() to "#016FB9",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#EC4E20"
        ),

        "Opal Blue" to mapOf(
            ThemeParameter.AColor.toString() to "#12263A",
            ThemeParameter.UColor.toString() to "#06BCC1",
            ThemeParameter.GColor.toString() to "#C5D8D1",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#F4EDEA",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#F4D1AE"
        ),

        "Pumpkin Vegas" to mapOf(
            ThemeParameter.AColor.toString() to "#FA7921",
            ThemeParameter.UColor.toString() to "#FE9920",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#B9A44C",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#566E3D",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#0C4767"
        ),

        "Cyber Tropical" to mapOf(
            ThemeParameter.AColor.toString() to "#000F08",
            ThemeParameter.UColor.toString() to "#136F63",
            ThemeParameter.GColor.toString() to "#E0CA3C",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#F34213",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#3E2F5B"
        ),
        "Polished Piggy" to mapOf(
            ThemeParameter.AColor.toString() to "#FF9FB2",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#FBDCE2",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#0ACDFF",
            ThemeParameter.CColor.toString() to "#60AB9A",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#DEDEE0"
        ),

        "Burnished Melon" to mapOf(
            ThemeParameter.AColor.toString() to "#484A47",
            ThemeParameter.UColor.toString() to "#5C6D70",
            ThemeParameter.GColor.toString() to "#A37774",
            ThemeParameter.CColor.toString() to "#E88873",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#E0AC9D"
        ),

        "Spanish Tea" to mapOf(
            ThemeParameter.AColor.toString() to "#88D18A",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#CCDDB7",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#6A5B6E",
            ThemeParameter.CColor.toString() to "#F0B7B3",
            ThemeParameter.CChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#A833B9"
        ),

        "Russian Sandy" to mapOf(
            ThemeParameter.AColor.toString() to "#F2DC5D",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#F2A359",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#DB9065",
            ThemeParameter.GChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.CColor.toString() to "#A4031F",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#240B36"
        ),

        "Crimson Maroon" to mapOf(
            ThemeParameter.AColor.toString() to "#D62839",
            ThemeParameter.UColor.toString() to "#BA324F",
            ThemeParameter.GColor.toString() to "#175676",
            ThemeParameter.CColor.toString() to "#4BA3C3",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#CCE6F4"
        ),

        "Rust Purple" to mapOf(
            ThemeParameter.AColor.toString() to "#FCDE9C",
            ThemeParameter.AChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.UColor.toString() to "#FFA552",
            ThemeParameter.UChar.toString() to getHTMLColorString(
                Color.BLACK
            ),
            ThemeParameter.GColor.toString() to "#BA5624",
            ThemeParameter.CColor.toString() to "#381D2A",
            ThemeParameter.XColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.SecondaryColor.toString() to getHTMLColorString(
                Color.LIGHT_GRAY
            ),
            ThemeParameter.TertiaryColor.toString() to "#C4D6B0"
        )
    )

    @JvmStatic
    private var document: Document? = null

    @JvmField
    var defaultTheme = mutableMapOf<String,String>(
        ThemeParameter.AColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.AColor.toString())!!,
        ThemeParameter.AChar.toString() to defaultColorSchemes.get("Persian Carolina")!!.getOrDefault(
            ThemeParameter.AChar.toString(),
            getHTMLColorString(Color.WHITE)
        ),
        ThemeParameter.UColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.UColor.toString())!!,
        ThemeParameter.UChar.toString() to defaultColorSchemes.get("Persian Carolina")!!.getOrDefault(
            ThemeParameter.UChar.toString(),
            getHTMLColorString(Color.WHITE)
        ),
        ThemeParameter.GColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.GColor.toString())!!,
        ThemeParameter.GChar.toString() to defaultColorSchemes.get("Persian Carolina")!!.getOrDefault(
            ThemeParameter.GChar.toString(),
            getHTMLColorString(Color.WHITE)
        ),
        ThemeParameter.CColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.CColor.toString())!!,
        ThemeParameter.CChar.toString() to defaultColorSchemes.get("Persian Carolina")!!.getOrDefault(
            ThemeParameter.CChar.toString(),
            getHTMLColorString(Color.WHITE)
        ),
        ThemeParameter.XColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.XColor.toString())!!,
        ThemeParameter.XChar.toString() to defaultColorSchemes.get("Persian Carolina")!!.getOrDefault(
            ThemeParameter.XChar.toString(),
            getHTMLColorString(Color.WHITE)
        ),
        ThemeParameter.SecondaryColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.SecondaryColor.toString())!!,
        ThemeParameter.TertiaryColor.toString() to defaultColorSchemes.get("Persian Carolina")!!.get(
            ThemeParameter.TertiaryColor.toString())!!,
        ThemeParameter.ResidueBorder.toString() to "0.75",
        ThemeParameter.PhosphodiesterWidth.toString() to "3.0",
        ThemeParameter.SecondaryInteractionWidth.toString() to "1.0",
        ThemeParameter.SecondaryInteractionShift.toString() to "1.0",
        ThemeParameter.TertiaryInteractionWidth.toString() to "1.0",
        ThemeParameter.HaloWidth.toString() to "5.0",
        ThemeParameter.TertiaryOpacity.toString() to "130", //alpha value goes from 0 to 255
        ThemeParameter.ResidueCharOpacity.toString() to "255", //alpha value goes from 0 to 255
        ThemeParameter.TertiaryInteractionStyle.toString() to DASHED,
        ThemeParameter.FontName.toString() to "Arial",
        ThemeParameter.DeltaXRes.toString() to "0",
        ThemeParameter.DeltaYRes.toString() to "0",
        ThemeParameter.DeltaFontSize.toString() to "0"
    )

    @JvmStatic
    @Throws(BackingStoreException::class, IOException::class)
    fun load() {
        if (document != null) return
        val configFile = File(getUserDir(), "config.xml")
        if (configFile.exists()) {
            val builder = SAXBuilder()
            try {
                document = builder.build(configFile)
                val drawing = document!!.getRootElement().getChild("theme")
                if (drawing != null) {
                    for (c in drawing.getChildren()) {
                        defaultTheme[(c as Element).name] =  c.text
                    }
                }
            } catch (e: JDOMException) {
                e.printStackTrace()
            }
        } else {
            val root = Element("rnartist-config")
            root.setAttribute("release",
                getRnartistRelease()
            )
            root.addContent(Element("displayTertiariesInSelection"))
            root.addContent(Element("displayLWSymbols"))
            document = Document(root)
        }
        recoverWebsite()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun save(theme:Map<String,String>?) {
        theme?.let {
            var drawing = document!!.rootElement.getChild("theme")
            if (drawing == null) {
                drawing = Element("theme")
                document!!.rootElement.addContent(drawing)
            } else
                drawing.removeContent()

            for ((k, v) in theme) {
                val e = Element(k)
                e.setText(v)
                drawing.addContent(e)
                defaultTheme[k] =  v //we don't forget to save it in the defaultTheme map
            }
        }
        val outputter = XMLOutputter(Format.getPrettyFormat())
        val writer = FileWriter(File(getUserDir(), "config.xml"))
        outputter.output(document, writer)
    }

    @JvmStatic
    fun clearRecentFiles() {
        val e = document!!.rootElement.getChild("recent-files")
        e?.removeChildren("file")
    }

    data class GlobalProperties(var website: String) {
    }

    @JvmStatic
    private fun recoverWebsite() {
        val client: HttpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://raw.githubusercontent.com/fjossinet/RNArtist/master/properties.json"))
            .build()
        try {
            val response: HttpResponse<String> = client.send(request,
                HttpResponse.BodyHandlers.ofString())
            val properties = Gson().fromJson<GlobalProperties>(response.body() as String, GlobalProperties::class.java)
            website = "http://${properties.website}"
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    var website:String? = null

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
    val recentEntries: List<MutablePair<String, String>>
        get() {
            var e = document!!.rootElement.getChild("recent-entries")
            if (e == null) {
                e = Element("recent-entries")
                document!!.rootElement.addContent(e)
            }
            val files: MutableList<MutablePair<String, String>> = ArrayList()
            UPPERFOR@ for (o in e.getChildren("entry")) {
                val entry = o
                for (f in files) if (f.getLeft() == entry!!.getAttributeValue("id") && f.getRight() == entry.getAttributeValue("type")) {
                    continue@UPPERFOR
                }
                files.add(MutablePair(entry!!.getAttributeValue("id"), entry.getAttributeValue("type")))
            }
            document!!.rootElement.removeContent(e)
            e = Element("recent-entries")
            document!!.rootElement.addContent(e)
            for (f in files) {
                val file = Element("entry")
                file.setAttribute("id", f.getLeft())
                file.setAttribute("type", f.getRight())
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
    var displayTertiariesInSelection:Boolean
        get() {
            var e = document!!.rootElement.getChild("displayTertiariesInSelection")
            return e != null
        }

        set(display) {
            if (display && !displayTertiariesInSelection /*the element is not already there*/)
                document!!.rootElement.addContent(Element("displayTertiariesInSelection"))
            else if (!display)
                document!!.rootElement.removeChild("displayTertiariesInSelection")
        }

    @JvmStatic
    var centerDisplayOnSelection:Boolean
        get() {
            var e = document!!.rootElement.getChild("centerDisplayOnSelection")
            return e != null
        }

        set(center) {
            if (center && !centerDisplayOnSelection /*the element is not already there*/)
                document!!.rootElement.addContent(Element("centerDisplayOnSelection"))
            else if (!center)
                document!!.rootElement.removeChild("centerDisplayOnSelection")
        }

    @JvmStatic
    var fitDisplayOnSelection:Boolean
        get() {
            var e = document!!.rootElement.getChild("fitDisplayOnSelection")
            return e != null
        }

        set(center) {
            if (center && !fitDisplayOnSelection /*the element is not already there*/)
                document!!.rootElement.addContent(Element("fitDisplayOnSelection"))
            else if (!center)
                document!!.rootElement.removeChild("fitDisplayOnSelection")
        }

    @JvmStatic
    var displayLWSymbols:Boolean
        get() {
            var e = document!!.rootElement.getChild("displayLWSymbols")
            return e != null
        }

        set(display) {
            if (display && !displayLWSymbols /*the element is not already there*/)
                document!!.rootElement.addContent(Element("displayLWSymbols"))
            else if (!display)
                document!!.rootElement.removeChild("displayLWSymbols")
        }

    @JvmStatic
    var chimeraPath: String?
        get() {
            var e = document!!.rootElement.getChild("external-viewers")
            if (e == null) {
                val osName = System.getProperty("os.name")
                e = Element("external-viewers")
                e.addContent(Element("chimera-path"))
                document!!.rootElement.addContent(e)
                if (osName.startsWith("Mac OS")) {
                    e.getChild("chimera-path").text = "/Applications/Chimera.app/Contents/MacOS/chimera"
                } else if (osName.startsWith("Windows")) {
                    e.getChild("chimera-path").text = "C:\\Program Files\\Chimera\\bin\\chimera.exe"
                } else {
                    e.getChild("chimera-path").text = "/usr/local/chimera/bin/chimera"
                }
            } else {
                val _e = e.getChild("chimera-path")
                if (_e == null) e.addContent(Element("chimera-path"))
            }
            return document!!.rootElement.getChild("external-viewers").getChild("chimera-path").value
        }
        set(path) {
            document!!.rootElement.getChild("external-viewers").getChild("chimera-path").text = path
        }

    @JvmStatic
    var userID: String?
        get() {
            var e = document!!.rootElement.getChild("userID")
            return if (e == null) null else e.text
        }
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
        var e = document!!.rootElement.getChild("export-SVG-with-browser-compatibility")
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
    var selectionFading: Int
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-fading")
            if (e == null) {
                e = Element("selection-fading")
                e.text = "130"
                document!!.rootElement.addContent(e)
            }
            return e.value.toInt()
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-fading")
            if (e == null) {
                e = Element("selection-fading")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = "${value}"
        }

    @JvmStatic
    fun launchChimeraAtStart(): Boolean {
        var e = document!!.rootElement.getChild("launch-chimera")
        if (e == null) {
            e = Element("launch-chimera")
            e.text = "false"
            document!!.rootElement.addContent(e)
        }
        return e.text == "true"
    }

    @JvmStatic
    fun launchChimeraAtStart(launch: Boolean) {
        document!!.rootElement.getChild("launch-chimera").text = "" + launch
    }

    @JvmStatic
    fun useLocalAlgorithms(): Boolean {
        var e = document!!.rootElement.getChild("local-algorithms")
        if (e == null) {
            e = Element("local-algorithms")
            e.text = "false"
            document!!.rootElement.addContent(e)
        }
        return e.text == "true"
    }

    @JvmStatic
    fun useLocalAlgorithms(use: Boolean) {
        document!!.rootElement.getChild("local-algorithms").text = "" + use
    }

    @JvmStatic
    fun getRnartistRelease(): String? {
        return try {
            val format = SimpleDateFormat("MMM dd, yyyy")
            "RNArtist Development Release (" + format.format(Calendar.getInstance().time)+")"
        } catch (e: java.lang.Exception) {
            null
        }
    }
}