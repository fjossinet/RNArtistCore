/**
 * This script has to be used with Docker. It recovers Rfam entries and produces 2D drawing based on both drawing algorithms (Booquet and the one used in the RNArtist app)
 */

import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.rnartist

(1..100).forEach {
    val rfamID = "RF${"%05d".format(it)}"
    println(rfamID)
    try {
        booquet {
            file = "/docker/${rfamID}_booquet.svg"
            junction_diameter = 15.0
            ss {
                rfam {
                    id = rfamID
                    name="consensus"
                }
            }
        }
        rnartist {
            file = "/docker/${rfamID}_rnartist.svg"
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
}

