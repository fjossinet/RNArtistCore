import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.parseStockholm
import java.awt.Color
import java.awt.geom.Point2D
import java.io.File

(1..100).forEach {
    val rfamID = "RF${"%05d".format(it)}"
    println(rfamID)
    try {
        val ss = parseStockholm(Rfam().getEntry(rfamID), withConsensus2D = true)[0]
        if (ss.helices.isNotEmpty()) {
            booquet {
                file = "/Users/fjossinet/Downloads/${rfamID}.svg"
                junction_diameter = 15.0
                ss {
                    rfam {
                        id = rfamID
                        name="consensus"
                    }

                }
            }
        }
    } catch (e:Exception) {
        println("Exception for $rfamID: ${e.message}")
    }
}

