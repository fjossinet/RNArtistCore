package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.model.io.getUserDir
import org.apache.commons.lang3.tuple.MutablePair
import org.dizitart.no2.NitriteId
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
    val residuesThemes: Map<String, Map<String, Map<String, String>>> = mapOf(
        "Persian Carolina" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D741A7"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#3A1772"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5398BE"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2CD5D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mutableMapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Snow Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A31621"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCF7F8"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CED3DC"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4E8098"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Fuzzy French" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E4DFDA"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D4B483"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Chestnut Navajo" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFE0B5"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8A6552"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#462521"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Irresistible Turquoise" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D44B5"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B5446E"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#525252"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BADEFC"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Charm Jungle" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6A7FDB"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#57E2E5"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#45CB85"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Atomic Xanadu" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#70877F"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#454372"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Pale Coral" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#987284"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D0D6B5"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F9B5AC"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Golden Honolulu" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B1740F"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B1740F"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFD07B"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#296EB4"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Maximum Salmon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#301A4B"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFEAEC"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F39A9D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Pacific Dream" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#42F2F7"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#498C8A"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4B6858"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "New York Camel" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ECC8AF"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E7AD99"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CE796B"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C18C5D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Screamin' Olive" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494947"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#35FF69"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#44CCFF"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#7494EA"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Aero Green" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCFFFD"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#64B6AC"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C0FDFB"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#DAFFEF"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Baby Lilac" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D858D"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4A8D1"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4BFEB"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Celeste Olivine" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9CFFFA"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ACF39D"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B0C592"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A97C73"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Blood Celadon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A7D49B"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#92AC86"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#696047"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#55251D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Space Blizzard" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#25283D"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8F3985"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#98DFEA"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EFD9CE"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Midnight Paradise" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFD166"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#06D6A0"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#118AB2"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "African Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D8D8F6"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#978897"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494850"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Charcoal Lazuli" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#2F4858"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#33658A"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#86BBD8"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F6AE2D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Tyrian Yale" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E3B505"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#610345"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#107E7D"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#95190C"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Cheese Cinnabar" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF3E36"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#17BEBB"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#2E282A"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EDB88B"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Razzmic Granite" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CDF7F6"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8FB8DE"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9A94BC"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9B5094"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Aero Violet" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BDEDE0"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBDBD1"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B6B8D6"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#7E78D2"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Jet Flame" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#000000"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#353531"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FF9505"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#016FB9"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Opal Blue" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#12263A"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#06BCC1"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C5D8D1"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F4EDEA"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Pumpkin Vegas" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FE9920"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B9A44C"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#566E3D"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Cyber Tropical" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#000F08"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#136F63"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E0CA3C"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F34213"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Polished Piggy" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FF9FB2"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FBDCE2"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#0ACDFF"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#60AB9A"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Burnished Melon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#484A47"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5C6D70"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A37774"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E88873"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Spanish Tea" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#88D18A"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CCDDB7"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F0B7B3"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6A5B6E"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Russian Sandy" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2DC5D"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2A359"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#DB9065"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4031F"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Crimson Maroon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D62839"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BA324F"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#175676"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4BA3C3"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        ),
        "Rust Purple" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCDE9C"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFA552"
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BA5624"
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#381D2A"
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    )
        )

    );

    @JvmField
    val structuralDomainsThemes: Map<String, Map<String, Map<String, String>>> = mapOf(
        "Persian Carolina" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D741A7"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#3A1772"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5398BE"
                    )
        ),
        "Snow Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A31621"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCF7F8"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CED3DC"
                    )
        ),
        "Fuzzy French" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E4DFDA"
                    )
        ),
        "Chestnut Navajo" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFE0B5"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8A6552"
                    )
        ),
        "Irresistible Turquoise" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D44B5"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B5446E"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#525252"
                    )
        ),
        "Charm Jungle" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6A7FDB"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#57E2E5"
                    )
        ),
        "Atomic Xanadu" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#70877F"
                    )
        ),
        "Pale Coral" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#987284"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D0D6B5"
                    )
        ),
        "Golden Honolulu" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B1740F"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B1740F"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFD07B"
                    )
        ),
        "Maximum Salmon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#301A4B"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFEAEC"
                    )
        ),
        "Pacific Dream" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#42F2F7"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#498C8A"
                    )
        ),
        "New York Camel" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ECC8AF"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E7AD99"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CE796B"
                    )
        ),
        "Screamin' Olive" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494947"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#35FF69"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#44CCFF"
                    )
        ),
        "Aero Green" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCFFFD"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#64B6AC"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C0FDFB"
                    )
        ),
        "Baby Lilac" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D858D"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4A8D1"
                    )
        ),
        "Celeste Olivine" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9CFFFA"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ACF39D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B0C592"
                    )
        ),
        "Blood Celadon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A7D49B"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#92AC86"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#696047"
                    )
        ),
        "Space Blizzard" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#25283D"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8F3985"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#98DFEA"
                    )
        ),
        "Midnight Paradise" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFD166"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#06D6A0"
                    )
        ),
        "African Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D8D8F6"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#978897"
                    )
        ),
        "Charcoal Lazuli" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#2F4858"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#33658A"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#86BBD8"
                    )
        ),
        "Tyrian Yale" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E3B505"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#610345"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#107E7D"
                    )
        ),
        "Cheese Cinnabar" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF3E36"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#17BEBB"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#2E282A"
                    )
        ),
        "Razzmic Granite" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CDF7F6"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8FB8DE"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9A94BC"
                    )
        ),
        "Aero Violet" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BDEDE0"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBDBD1"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B6B8D6"
                    )
        ),
        "Jet Flame" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#000000"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#353531"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FF9505"
                    )
        ),
        "Opal Blue" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#12263A"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#06BCC1"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C5D8D1"
                    )
        ),
        "Pumpkin Vegas" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FE9920"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B9A44C"
                    )
        ),
        "Polished Piggy" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FF9FB2"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FBDCE2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#0ACDFF"
                    )
        ),
        "Burnished Melon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#484A47"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5C6D70"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A37774"
                    )
        ),
        "Spanish Tea" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#88D18A"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CCDDB7"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F0B7B3"
                    )
        ),
        "Russian Sandy" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2DC5D"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2A359"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#DB9065"
                    )
        ),
        "Crimson Maroon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D62839"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BA324F"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#175676"
                    )
        ),
        "Rust Purple" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCDE9C"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFA552"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BA5624"
                    )
        )

    );

    @JvmStatic
    private var document: Document? = null

    @JvmStatic
    var lastThemeSavedId: org.apache.commons.lang3.tuple.Pair<String, NitriteId>? = null

    var defaultConfiguration = mutableMapOf<String, String>(
        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.DARK_GRAY),
        DrawingConfigurationParameter.LineWidth.toString() to "0.5",
        DrawingConfigurationParameter.LineShift.toString() to "1.0",
        DrawingConfigurationParameter.Opacity.toString() to "255", //alpha value goes from 0 to 255
        DrawingConfigurationParameter.TertiaryInteractionStyle.toString() to DASHED,
        DrawingConfigurationParameter.FontName.toString() to "Arial",
        DrawingConfigurationParameter.DeltaXRes.toString() to "0",
        DrawingConfigurationParameter.DeltaYRes.toString() to "0",
        DrawingConfigurationParameter.DeltaFontSize.toString() to "0",
        DrawingConfigurationParameter.FullDetails.toString() to "false"
    )

    @JvmField
    var defaultTheme = mutableMapOf<String, Map<String, String>>(
        SecondaryStructureType.Full2D.toString() to defaultConfiguration.toMutableMap(),

        SecondaryStructureType.Helix.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.Helix.toString()]!!.toMutableMap(),

        SecondaryStructureType.SingleStrand.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.SingleStrand.toString()]!!.toMutableMap(),

        SecondaryStructureType.Junction.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.Junction.toString()]!!.toMutableMap(),

        SecondaryStructureType.TertiaryInteraction.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Opacity.toString() to "130"
                ),

        SecondaryStructureType.TertiaryInteraction.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.DARK_GRAY),
                    DrawingConfigurationParameter.LineWidth.toString() to "0.5"
                ),

        SecondaryStructureType.A.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                ),

        SecondaryStructureType.U.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                ),

        SecondaryStructureType.G.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                ),

        SecondaryStructureType.C.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                ),

        SecondaryStructureType.X.toString() to
                mutableMapOf<String, String>(
                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                )
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
                val theme = document!!.getRootElement().getChild("theme")
                if (theme != null) {
                    defaultTheme.clear()
                    if (theme.hasAttributes())
                        lastThemeSavedId = org.apache.commons.lang3.tuple.Pair.of(
                            theme.getAttributeValue("name"),
                            NitriteId.createId(theme.getAttributeValue("id").toLong())
                        )
                    for (e in theme.getChildren()) {
                        val drawingConfiguration = mutableMapOf<String, String>()
                        for (_e in e.getChildren())
                            drawingConfiguration.put(_e.name, _e.value)
                        defaultTheme[e.name] = drawingConfiguration
                    }
                }
            } catch (e: JDOMException) {
                e.printStackTrace()
            }
        } else {
            val root = Element("rnartist-config")
            root.setAttribute(
                "release",
                getRnartistRelease()
            )
            root.addContent(Element("displayTertiariesInSelection"))
            root.addContent(Element("displayLWSymbols"))
            root.addContent(Element("save-current-theme-on-exit"))
            document = Document(root)
        }
        recoverWebsite()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun save(
        theme: Map<String, Map<String, String>>?,
        savedTheme: org.apache.commons.lang3.tuple.Pair<String, NitriteId>?
    ) {
        theme?.let {
            var themeElement = document!!.rootElement.getChild("theme")
            if (themeElement == null) {
                themeElement = Element("theme")
                document!!.rootElement.addContent(themeElement)
            } else {
                themeElement.removeContent()
                themeElement.attributes.clear()
            }
            savedTheme?.let { //the current theme was a saved one. We keep this information to recover it next launch
                themeElement.setAttribute("name", savedTheme.key)
                themeElement.setAttribute("id", savedTheme.value.idValue.toString())
            }
            for ((elementType, parameters) in theme) {
                val e = Element(elementType)
                themeElement.addContent(e)
                for ((name, value) in parameters) {
                    val _e = Element(name)
                    _e.setText(value)
                    e.addContent(_e)
                }
            }
            defaultTheme = theme.toMutableMap()
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
            val response: HttpResponse<String> = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )
            val properties = Gson().fromJson<GlobalProperties>(response.body() as String, GlobalProperties::class.java)
            website = "http://${properties.website}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                for (f in files) if (f.getLeft() == entry!!.getAttributeValue("id") && f.getRight() == entry.getAttributeValue(
                        "type"
                    )
                ) {
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
    var displayTertiariesInSelection: Boolean
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
    var centerDisplayOnSelection: Boolean
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
    var fitDisplayOnSelection: Boolean
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
    var displayLWSymbols: Boolean
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
    fun saveCurrentThemeOnExit(): Boolean {
        var e = document!!.rootElement.getChild("save-current-theme-on-exit")
        return e != null
    }

    @JvmStatic
    fun saveCurrentThemeOnExit(save: Boolean) {
        if (save && !saveCurrentThemeOnExit() /*the element is not already there*/)
            document!!.rootElement.addContent(Element("save-current-theme-on-exit"))
        else if (!save)
            document!!.rootElement.removeChild("save-current-theme-on-exit")
    }

    @JvmStatic
    var selectionColor: Color
        get() = getAWTColor(selectionColorCode, selectionOpacity)!!
        set(value) {
            selectionColorCode = getHTMLColorString(value)
            selectionOpacity = value.alpha
        }

    @JvmStatic
    var selectionOpacity: Int
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-opacity")
            if (e == null) {
                e = Element("selection-opacity")
                e.text = "200"
                document!!.rootElement.addContent(e)
            }
            return e.value.toInt()
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-opacity")
            if (e == null) {
                e = Element("selection-opacity")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = "${value}"
        }

    @JvmStatic
    var selectionColorCode: String
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-color")
            if (e == null) {
                e = Element("selection-color")
                e.text = getHTMLColorString(Color.GRAY)
                document!!.rootElement.addContent(e)
            }
            return e.value
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-color")
            if (e == null) {
                e = Element("selection-color")
                document!!.rootElement.addContent(e)
            }
            (e as Element).text = value
        }

    @JvmStatic
    var selectionSize: Int
        get() {
            var e: Element? = document!!.rootElement.getChild("selection-size")
            if (e == null) {
                e = Element("selection-size")
                e.text = "10"
                document!!.rootElement.addContent(e)
            }
            return e.value.toInt()
        }
        set(value) {
            var e: Element? = document!!.rootElement.getChild("selection-size")
            if (e == null) {
                e = Element("selection-size")
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
            "RNArtist Development Release (" + format.format(Calendar.getInstance().time) + ")"
        } catch (e: java.lang.Exception) {
            null
        }
    }
}