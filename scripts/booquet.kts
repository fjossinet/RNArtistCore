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
            val svgOutput = Booquet(ss, frameWidth = 300.0, frameHeight = 300.0, step = 25.0, residue_occupancy = 10.0, junction_diameter = 15.0)

            val f = File("/Users/fjossinet/Downloads/${rfamID}.svg")
            f.createNewFile()
            f.writeText(svgOutput)
        }
    } catch (e:Exception) {
        println("Exception for $rfamID: ${e.message}")
    }
}

