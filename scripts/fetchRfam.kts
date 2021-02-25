import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.parseStockholm
import io.github.fjossinet.rnartist.core.rnartist
import java.awt.Color
import java.awt.geom.Point2D
import java.io.File

listOf<Int>(11, 17, 36, 457).forEach {
    val rfamID = "RF${"%05d".format(it)}"
    println(rfamID)
    val outputFile = "/Users/fjossinet/Downloads/${rfamID}_rnartist.svg"
    if (!File("/Users/fjossinet/Downloads/${rfamID}_rnartist_consensus.svg").exists()) {
        try {
            /*booquet {
            file = "/Users/fjossinet/Downloads/${rfamID}_booquet.svg"
            junction_diameter = 15.0
            ss {
                rfam {
                    id = rfamID
                    name="consensus"
                }
            }
        }*/
            rnartist {
                file = outputFile
                ss {
                    rfam {
                        id = rfamID
                        name = "consensus"
                    }
                }
                theme {
                    details_lvl = 5

                    color {
                        type = "R"
                        value = "deepskyblue"
                    }

                    color {
                        type = "Y"
                        value = "darkgreen"
                    }
                }
            }
        } catch (e: Exception) {
            println("Exception for $rfamID: ${e.message}")
        }
    } else {
        println("$rfamID already processed")
    }
}

