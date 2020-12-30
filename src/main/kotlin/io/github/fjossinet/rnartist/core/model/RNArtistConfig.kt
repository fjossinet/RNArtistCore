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
    val themeDetailsLevel = listOf<Map<String, Map<String, String>>>(
        mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.SecondaryInteraction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.Junction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.SingleStrand.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.PhosphodiesterBond.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.AShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.A.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.UShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.U.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.GShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.G.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.CShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.C.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.XShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.X.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.InteractionSymbol.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false")
        ),
        mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SecondaryInteraction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.Junction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SingleStrand.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.PhosphodiesterBond.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.AShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.A.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.UShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.U.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.GShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.G.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.CShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.C.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.XShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.X.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.InteractionSymbol.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false")
        ),
        mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SecondaryInteraction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.Junction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SingleStrand.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.PhosphodiesterBond.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.AShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.A.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.UShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.U.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.GShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.G.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.CShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.C.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.XShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.X.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false"),
            SecondaryStructureType.InteractionSymbol.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false")
        ),
        mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SecondaryInteraction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.Junction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SingleStrand.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.PhosphodiesterBond.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.AShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.A.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.UShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.U.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.GShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.G.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.CShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.C.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.XShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.X.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.InteractionSymbol.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "false")
        ),
        mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SecondaryInteraction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.Junction.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.SingleStrand.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.PhosphodiesterBond.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.AShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.A.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.UShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.U.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.GShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.G.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.CShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.C.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.XShape.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.X.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true"),
            SecondaryStructureType.InteractionSymbol.toString() to mapOf<String, String>(DrawingConfigurationParameter.FullDetails.toString() to "true")
        )
    )

    @JvmField
    val colorSchemes: Map<String, Map<String, Map<String, String>>> = mapOf(
        "Persian Carolina" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5398BE"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2CD5D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D741A7"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#3A1772"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#5398BE"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F2CD5D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mutableMapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Snow Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4E8098"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A31621"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A31621"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FCF7F8"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CED3DC"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4E8098"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Fuzzy French" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#731DD8"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#48A9A6"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E4DFDA"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D4B483"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Chestnut Navajo" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8A6552"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CA2E55"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFE0B5"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#8A6552"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#462521"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
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
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D44B5"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B5446E"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#525252"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BADEFC"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Charm Jungle" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#45CB85"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E08DAC"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6A7FDB"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#57E2E5"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#45CB85"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Atomic Xanadu" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF946C"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C4A77D"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#70877F"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#454372"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Pale Coral" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#987284"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#987284"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#75B9BE"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D0D6B5"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F9B5AC"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Maximum Salmon" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F39A9D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#301A4B"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#6DB1BF"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFEAEC"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F39A9D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "Pacific Dream" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#498C8A"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#42F2F7"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#46ACC2"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#498C8A"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#4B6858"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "New York Camel" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CE796B"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C18C5D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ECC8AF"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#E7AD99"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#CE796B"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#C18C5D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
        ),
        "Screamin' Olive" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#7494EA"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494947"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494947"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#35FF69"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#44CCFF"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#7494EA"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Baby Lilac" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4A8D1"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9D858D"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#BBA0B2"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4A8D1"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A4BFEB"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Celeste Olivine" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B0C592"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A97C73"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#9CFFFA"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#ACF39D"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B0C592"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#A97C73"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Midnight Paradise" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#118AB2"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#EF476F"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FFD166"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#06D6A0"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#118AB2"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "African Lavender" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#978897"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#D8D8F6"
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B18FCF"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#978897"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#494850"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Charcoal Lazuli" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#33658A"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F6AE2D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#2F4858"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#33658A"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#86BBD8"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#F6AE2D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        ),
        "Pumpkin Vegas" to mapOf<String, Map<String, String>>(
            SecondaryStructureType.Helix.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.Junction.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#566E3D"
                    ),
            SecondaryStructureType.SingleStrand.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    ),
            SecondaryStructureType.AShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FA7921"
                    ),
            SecondaryStructureType.A.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.UShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#FE9920"
                    ),
            SecondaryStructureType.U.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.GShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#B9A44C"
                    ),
            SecondaryStructureType.G.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.CShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to "#566E3D"
                    ),
            SecondaryStructureType.C.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.WHITE)
                    ),
            SecondaryStructureType.XShape.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY)
                    ),
            SecondaryStructureType.X.toString() to
                    mapOf<String, String>(
                        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.BLACK)
                    )
        )

    );

    @JvmStatic
    private var document: Document? = null

    @JvmStatic
    var lastThemeSavedId: org.apache.commons.lang3.tuple.Pair<String, NitriteId>? = null

    val defaultConfiguration = mutableMapOf<String, String>(
        DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.DARK_GRAY),
        DrawingConfigurationParameter.LineWidth.toString() to "1.0",
        DrawingConfigurationParameter.LineShift.toString() to "1.0",
        DrawingConfigurationParameter.Opacity.toString() to "255", //alpha value goes from 0 to 255
        DrawingConfigurationParameter.FontName.toString() to "Arial",
        DrawingConfigurationParameter.DeltaXRes.toString() to "0",
        DrawingConfigurationParameter.DeltaYRes.toString() to "0",
        DrawingConfigurationParameter.DeltaFontSize.toString() to "0",
        DrawingConfigurationParameter.FullDetails.toString() to "false"
    )

    private val defaultColorScheme = "Pumpkin Vegas"

    @JvmField
    var defaultTheme = mutableMapOf(
        Pair(SecondaryStructureType.Full2D.toString(), defaultConfiguration.toMutableMap()),
        Pair(SecondaryStructureType.Helix.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.Helix.toString()]!!),
        Pair(SecondaryStructureType.Junction.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.Junction.toString()]!!),
        Pair(SecondaryStructureType.SingleStrand.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.SingleStrand.toString()]!!),
        Pair(SecondaryStructureType.AShape.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.AShape.toString()]!!),
        Pair(SecondaryStructureType.A.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.A.toString()]!!),
        Pair(SecondaryStructureType.UShape.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.UShape.toString()]!!),
        Pair(SecondaryStructureType.U.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.U.toString()]!!),
        Pair(SecondaryStructureType.GShape.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.GShape.toString()]!!),
        Pair(SecondaryStructureType.G.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.G.toString()]!!),
        Pair(SecondaryStructureType.CShape.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.CShape.toString()]!!),
        Pair(SecondaryStructureType.C.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.C.toString()]!!),
        Pair(SecondaryStructureType.XShape.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.XShape.toString()]!!),
        Pair(SecondaryStructureType.X.toString(), colorSchemes[defaultColorScheme]!![SecondaryStructureType.X.toString()]!!),
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
            root.addContent(Element("save-current-theme-on-exit"))
            document = Document(root)
        }
        recoverWebsite()
    }

    @JvmStatic
    fun clearRecentFiles() {
        val e = document!!.rootElement.getChild("recent-files")
        e?.removeChildren("file")
    }

    data class GlobalProperties(var website: String)

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
    var chimeraPath: String?
        get() {
            var e = document!!.rootElement.getChild("external-viewers")
            if (e == null) {
                val osName = System.getProperty("os.name")
                e = Element("external-viewers")
                e.addContent(Element("chimera-path"))
                document!!.rootElement.addContent(e)
                when {
                    osName.startsWith("Mac OS") -> {
                        e.getChild("chimera-path").text = "/Applications/Chimera.app/Contents/MacOS/chimera"
                    }
                    osName.startsWith("Windows") -> {
                        e.getChild("chimera-path").text = "C:\\Program Files\\Chimera\\bin\\chimera.exe"
                    }
                    else -> {
                        e.getChild("chimera-path").text = "/usr/local/chimera/bin/chimera"
                    }
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
    var selectionColor: Color
        get() = getAWTColor(selectionColorCode, selectionOpacity)
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