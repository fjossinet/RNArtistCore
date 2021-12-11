package io.github.fjossinet.rnartist.core.model

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.model.io.getUserDir
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
import java.util.prefs.BackingStoreException

object RnartistConfig {

    @JvmField
    val colorSchemes: Map<String, Map<String, Map<String, String>>> = mapOf(
        "Persian Carolina" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#5398BE"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F2CD5D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#D741A7"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#3A1772"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#5398BE"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F2CD5D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mutableMapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Snow Lavender" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#4E8098"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A31621"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A31621"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FCF7F8"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#CED3DC"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#4E8098"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Fuzzy French" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#E4DFDA"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#D4B483"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Chestnut Navajo" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#8A6552"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FFE0B5"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#8A6552"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#462521"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Irresistible Turquoise" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#9D44B5"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B5446E"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#9D44B5"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B5446E"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#525252"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#BADEFC"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Charm Jungle" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#45CB85"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#6A7FDB"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#57E2E5"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#45CB85"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Atomic Xanadu" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#70877F"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#454372"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Pale Coral" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#987284"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#987284"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#D0D6B5"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F9B5AC"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Maximum Salmon" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F39A9D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#301A4B"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FFEAEC"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F39A9D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "Pacific Dream" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#498C8A"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#42F2F7"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#498C8A"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#4B6858"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "New York Camel" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#CE796B"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#C18C5D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#ECC8AF"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#E7AD99"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#CE796B"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#C18C5D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "Screamin' Olive" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#7494EA"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#494947"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#494947"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#35FF69"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#44CCFF"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#7494EA"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Baby Lilac" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A4A8D1"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#9D858D"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A4A8D1"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A4BFEB"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Celeste Olivine" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B0C592"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A97C73"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#9CFFFA"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#ACF39D"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B0C592"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#A97C73"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Midnight Paradise" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#118AB2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FFD166"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#06D6A0"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#118AB2"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "African Lavender" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#978897"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#D8D8F6"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#978897"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#494850"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Charcoal Lazuli" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#33658A"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F6AE2D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#2F4858"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#33658A"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#86BBD8"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#F6AE2D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Pumpkin Vegas" to mapOf(
            SecondaryStructureType.Helix.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#566E3D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#FE9920"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#B9A44C"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to "#566E3D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf(
                        ThemeParameter.color.toString() to getHTMLColorString(Color.BLACK)
                    )
        )

    )

    @JvmStatic
    private var document: Document? = null

    val defaultConfiguration = mutableMapOf<String, String>(
        ThemeParameter.color.toString() to getHTMLColorString(Color.DARK_GRAY),
        ThemeParameter.linewidth.toString() to "1.0",
        ThemeParameter.lineshift.toString() to "1.0",
        ThemeParameter.opacity.toString() to "255", //alpha value goes from 0 to 255
        ThemeParameter.fulldetails.toString() to "false"
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
        //recoverWebsite()
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
    var rnaGalleryPath: String?
        get() {
            var e = document!!.rootElement.getChild("external-tools")
            if (e == null) {
                e = Element("external-tools")
                document!!.rootElement.addContent(e)
            }
            val _e = e.getChild("rna-gallery")
            if (_e == null) {
                val rnaGallery = Element("rna-gallery")
                rnaGallery.addContent(Element("path"))
                val useOnline = Element("use-online")
                useOnline.text = "true"
                rnaGallery.addContent(useOnline)
                e.addContent(rnaGallery)
            }
            return document!!.rootElement.getChild("external-tools").getChild("rna-gallery").getChild("path").value
        }
        set(path) {
            document!!.rootElement.getChild("external-tools").getChild("rna-gallery").getChild("path").text = path
        }

    @JvmStatic
    var useOnlineRNAGallery: Boolean
        get() {
            var e = document!!.rootElement.getChild("external-tools")
            if (e == null) {
                e = Element("external-tools")
                document!!.rootElement.addContent(e)
            }
            val _e = e.getChild("rna-gallery")
            if (_e == null) {
                val rnaGallery = Element("rna-gallery")
                rnaGallery.addContent(Element("path"))
                val useOnline = Element("use-online")
                useOnline.text = "true"
                rnaGallery.addContent(useOnline)
                e.addContent(rnaGallery)
            }
            return "true".equals(document!!.rootElement.getChild("external-tools").getChild("rna-gallery").getChild("use-online").value)
        }
        set(useOnline) {
            document!!.rootElement.getChild("external-tools").getChild("rna-gallery").getChild("use-online").text = useOnline.toString()
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