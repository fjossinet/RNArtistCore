import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.parseStockholm
import io.github.fjossinet.rnartist.core.rnartist
import java.awt.Color
import java.awt.geom.Point2D
import java.io.File

(1..100).forEach {
    val rfamID = "RF${"%05d".format(it)}"
    println(rfamID)
    try {
        booquet {
            file = "/Users/fjossinet/Downloads/${rfamID}_booquet.svg"
            junction_diameter = 15.0
            ss {
                rfam {
                    id = rfamID
                    name="consensus"
                }
            }
        }
        rnartist {
            file = "/Users/fjossinet/Downloads/${rfamID}_rnartist.svg"
            ss {
                rfam {
                    id = rfamID
                    name="consensus"
                }
            }
            theme {
                details {
                    type = "helix"
                    value = "full"
                }

                details {
                    type = "junction"
                    value = "full"
                }

                details {
                    type = "single_strand"
                    value = "full"
                }

                details {
                    type = "secondary_interaction"
                    value = "full"
                }

                details {
                    type = "phosphodiester_bond"
                    value = "full"
                }

                details {
                    type = "interaction_symbol"
                    value = "full"
                }

                details {
                    type = "N"
                    value = "full"
                }

                details {
                    type = "n"
                    value = "full"
                }

                color {
                    type = "R"
                    value = "#EF946C"
                }

                color {
                    type = "Y"
                    value = "#C4A77D"
                }

                color {
                    type = "n"
                    value = "#000000"
                }

                line {
                    type = "phosphodiester_bond"
                    value = 2.0
                }

                line {
                    type = "secondary_interaction"
                    value = 4.0
                }
            }
        }
    } catch (e:Exception) {
        println("Exception for $rfamID: ${e.message}")
    }
}

