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
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D741A7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#3A1772",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#5398BE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F2CD5D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Snow Lavender" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A31621",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCF7F8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CED3DC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#4E8098",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Fuzzy French" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#731DD8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#48A9A6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E4DFDA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D4B483",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Chestnut Navajo" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CA2E55",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFE0B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8A6552",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#462521",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Irresistible Turquoise" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9D44B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B5446E",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#525252",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BADEFC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Charm Jungle" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E08DAC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#6A7FDB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#57E2E5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#45CB85",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Atomic Xanadu" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF946C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C4A77D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#70877F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#454372",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Pale Coral" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#987284",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#75B9BE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D0D6B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F9B5AC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Golden Honolulu" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B1740F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B1740F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFD07B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#296EB4",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Maximum Salmon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#301A4B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#6DB1BF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFEAEC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F39A9D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Pacific Dream" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#42F2F7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#46ACC2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#498C8A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#4B6858",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "New York Camel" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#ECC8AF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E7AD99",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CE796B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C18C5D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Screamin' Olive" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#494947",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#35FF69",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#44CCFF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#7494EA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Aero Green" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCFFFD",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#64B6AC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C0FDFB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#DAFFEF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Baby Lilac" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9D858D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BBA0B2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A4A8D1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A4BFEB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Celeste Olivine" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9CFFFA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#ACF39D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B0C592",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A97C73",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Blood Celadon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A7D49B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#92AC86",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#696047",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#55251D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Space Blizzard" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#25283D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8F3985",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#98DFEA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EFD9CE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Midnight Paradise" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF476F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFD166",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#06D6A0",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#118AB2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "African Lavender" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D8D8F6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B18FCF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#978897",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#494850",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Charcoal Lazuli" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#2F4858",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#33658A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#86BBD8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F6AE2D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Tyrian Yale" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E3B505",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#610345",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#107E7D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#95190C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Cheese Cinnabar" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF3E36",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#17BEBB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#2E282A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EDB88B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Razzmic Granite" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CDF7F6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8FB8DE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9A94BC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9B5094",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Aero Violet" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BDEDE0",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BBDBD1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B6B8D6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#7E78D2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Jet Flame" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#000000",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#353531",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FF9505",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#016FB9",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Opal Blue" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#12263A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#06BCC1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C5D8D1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F4EDEA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Pumpkin Vegas" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FA7921",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FE9920",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B9A44C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#566E3D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Cyber Tropical" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#000F08",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#136F63",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E0CA3C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F34213",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Polished Piggy" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FF9FB2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FBDCE2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#0ACDFF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#60AB9A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Burnished Melon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#484A47",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#5C6D70",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A37774",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E88873",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Spanish Tea" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#88D18A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CCDDB7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F0B7B3",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#6A5B6E",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Russian Sandy" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F2DC5D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F2A359",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#DB9065",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A4031F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Crimson Maroon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D62839",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BA324F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#175676",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#4BA3C3",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Rust Purple" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.A.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCDE9C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.U.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFA552",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.G.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BA5624",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.C.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#381D2A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.X.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.LIGHT_GRAY),
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            )

    );

    @JvmField
    val structuralDomainsThemes: Map<String, Map<String, Map<String, String>>> = mapOf(
            "Persian Carolina" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D741A7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#3A1772",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#5398BE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Snow Lavender" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A31621",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCF7F8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CED3DC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Fuzzy French" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#731DD8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#48A9A6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E4DFDA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Chestnut Navajo" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CA2E55",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFE0B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8A6552",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Irresistible Turquoise" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9D44B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B5446E",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#525252",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Charm Jungle" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E08DAC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#6A7FDB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#57E2E5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Atomic Xanadu" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF946C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C4A77D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#70877F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Pale Coral" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#987284",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#75B9BE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D0D6B5",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Golden Honolulu" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B1740F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B1740F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFD07B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Maximum Salmon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#301A4B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#6DB1BF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFEAEC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Pacific Dream" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#42F2F7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#46ACC2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#498C8A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "New York Camel" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#ECC8AF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E7AD99",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CE796B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Screamin' Olive" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#494947",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#35FF69",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#44CCFF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Aero Green" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCFFFD",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#64B6AC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C0FDFB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Baby Lilac" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9D858D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BBA0B2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A4A8D1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Celeste Olivine" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9CFFFA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#ACF39D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B0C592",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Blood Celadon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A7D49B",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#92AC86",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#696047",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Space Blizzard" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#25283D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8F3985",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#98DFEA",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Midnight Paradise" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF476F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFD166",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#06D6A0",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "African Lavender" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D8D8F6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B18FCF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#978897",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Charcoal Lazuli" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#2F4858",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#33658A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#86BBD8",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Tyrian Yale" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#E3B505",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#610345",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#107E7D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Cheese Cinnabar" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#EF3E36",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#17BEBB",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#2E282A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Razzmic Granite" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CDF7F6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#8FB8DE",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#9A94BC",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Aero Violet" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BDEDE0",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BBDBD1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B6B8D6",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Jet Flame" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#000000",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#353531",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FF9505",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Opal Blue" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#12263A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#06BCC1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#C5D8D1",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Pumpkin Vegas" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FA7921",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FE9920",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#B9A44C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Cyber Tropical" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#000F08",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Polished Piggy" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FF9FB2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FBDCE2",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#0ACDFF",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Burnished Melon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#484A47",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#5C6D70",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#A37774",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Spanish Tea" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#88D18A",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#CCDDB7",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F0B7B3",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Russian Sandy" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F2DC5D",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#F2A359",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#DB9065",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            )
            ),
            "Crimson Maroon" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#D62839",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BA324F",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#175676",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            ),
            "Rust Purple" to mapOf<String, Map<String, String>>(
                    SecondaryStructureType.Helix.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FCDE9C",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.Junction.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#FFA552",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.BLACK)
                            ),
                    SecondaryStructureType.SingleStrand.toString() to
                            mapOf<String, String>(
                                    DrawingConfigurationParameter.Color.toString() to "#BA5624",
                                    DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE)
                            )
            )

    );

    @JvmStatic
    private var document: Document? = null

    @JvmStatic
    var lastThemeSavedId: org.apache.commons.lang3.tuple.Pair<String, NitriteId>? = null

    var defaultConfiguration = mutableMapOf<String, String>(
            DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.DARK_GRAY),
            DrawingConfigurationParameter.CharColor.toString() to getHTMLColorString(Color.WHITE),
            DrawingConfigurationParameter.LineWidth.toString() to "0.5",
            DrawingConfigurationParameter.LineShift.toString() to "1.0",
            DrawingConfigurationParameter.Opacity.toString() to "255", //alpha value goes from 0 to 255
            DrawingConfigurationParameter.TertiaryInteractionStyle.toString() to DASHED,
            DrawingConfigurationParameter.FontName.toString() to "Arial",
            DrawingConfigurationParameter.DeltaXRes.toString() to "0",
            DrawingConfigurationParameter.DeltaYRes.toString() to "0",
            DrawingConfigurationParameter.DeltaFontSize.toString() to "0"
    )

    @JvmField
    var defaultTheme = mutableMapOf<String, Map<String, String>>(
            SecondaryStructureType.Full2D.toString() to defaultConfiguration.toMutableMap(),

            SecondaryStructureType.Helix.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.Helix.toString()]!!.toMutableMap(),

            SecondaryStructureType.SingleStrand.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.SingleStrand.toString()]!!.toMutableMap(),

            SecondaryStructureType.Junction.toString() to structuralDomainsThemes["Persian Carolina"]!![SecondaryStructureType.Junction.toString()]!!.toMutableMap(),

            SecondaryStructureType.TertiaryInteraction.toString() to
                    mutableMapOf<String, String>(
                            DrawingConfigurationParameter.Opacity.toString() to "130"),

            SecondaryStructureType.TertiaryInteraction.toString() to
                    mutableMapOf<String, String>(
                            DrawingConfigurationParameter.Color.toString() to getHTMLColorString(Color.DARK_GRAY),
                            DrawingConfigurationParameter.LineWidth.toString() to "1.0"
                    ),

            SecondaryStructureType.LWSymbol.toString() to
                    mutableMapOf<String, String>(
                            DrawingConfigurationParameter.LineWidth.toString() to "0")
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
                        lastThemeSavedId = org.apache.commons.lang3.tuple.Pair.of(theme.getAttributeValue("name"), NitriteId.createId(theme.getAttributeValue("id").toLong()))
                    for (e in theme.getChildren()) {
                        val drawingConfiguration = mutableMapOf<String,String>()
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
            root.setAttribute("release",
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
    fun save(theme: Map<String, Map<String, String>>?, savedTheme: org.apache.commons.lang3.tuple.Pair<String, NitriteId>?) {
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
            val response: HttpResponse<String> = client.send(request,
                    HttpResponse.BodyHandlers.ofString())
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