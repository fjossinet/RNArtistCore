import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.parseStockholm
import java.awt.Color
import java.awt.geom.Point2D
import java.io.File

val junction_diameter = 15

(1..3941).forEach {

    val rfamID = "RF${"%05d".format(it)}"
    println(rfamID)
    try {
        val ss = parseStockholm(Rfam().getEntry(rfamID), withConsensus2D = true)[0]
        if (ss.helices.isNotEmpty()) {
            val booquet = Booquet(ss, step = 25, residue_occupancy = 10, junction_diameter = junction_diameter)

            val minX = booquet.values.minBy<IntArray, Int> { it[0] }!!.get(0) - junction_diameter.toDouble()
            val minY =
                booquet.values.minBy { it[1] }!!.get(1) - junction_diameter.toDouble() - junction_diameter.toDouble()

            var maxX = booquet.values.maxBy<IntArray, Int> { it[0] }!!.get(0) + junction_diameter.toDouble()
            if (maxX < 50.0)
                maxX = 50.0
            val maxY = booquet.values.maxBy { it[1] }!!.get(1)

            val svgBuffer =
                StringBuffer("""<svg width="${maxX - minX}" height="${maxY - minY}" xmlns="http://www.w3.org/2000/svg">""" + "\n")

            ss.helices.forEach { helix ->
                booquet[helix.name]?.let { coords ->
                    svgBuffer.append(
                        """<line x1="${coords[0] - minX}" y1="${coords[1] - minY}" x2="${coords[2] - minX}" y2="${coords[3] - minY}" stroke="${
                            getHTMLColorString(
                                Color.BLACK
                            )
                        }" stroke-width="2" stroke-linecap="round"/>"""
                    )
                }
            }

            ss.singleStrands.forEach { singleStrand ->
                booquet[singleStrand.name]?.let { coords ->
                    svgBuffer.append(
                        """<line x1="${coords[0] - minX}" y1="${coords[1] - minY}" x2="${coords[2] - minX}" y2="${coords[3] - minY}" stroke="${
                            getHTMLColorString(
                                Color.BLACK
                            )
                        }" stroke-width="2" stroke-linecap="round"/>"""
                    )
                }
            }

            ss.junctions.forEach { junction ->
                booquet[junction.name]?.let { junctionCoords ->
                    when (junction.junctionType) {
                        in setOf(JunctionType.ApicalLoop, JunctionType.InnerLoop) -> {
                            svgBuffer.append(
                                """<circle cx="${junctionCoords[0] - minX}" cy="${junctionCoords[1] - minY}" r="${junction_diameter / 2.0}" stroke="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }" stroke-width="2" fill="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }"/>"""
                            )

                            svgBuffer.append(
                                """<circle cx="${junctionCoords[0] - minX}" cy="${junctionCoords[1] - minY}" r="${1.5 * junction_diameter / 2.0}" stroke="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }" stroke-width="2" fill="none"/>"""
                            )
                        }

                        else -> {
                            svgBuffer.append(
                                """<circle cx="${junctionCoords[0] - minX}" cy="${junctionCoords[1] - minY}" r="${junction_diameter / 2.0}" stroke="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }" stroke-width="2" fill="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }"/>"""
                            )

                            svgBuffer.append(
                                """<circle cx="${junctionCoords[0] - minX}" cy="${junctionCoords[1] - minY}" r="${1.5 * junction_diameter / 2.0}" stroke="${
                                    getHTMLColorString(
                                        Color.BLACK
                                    )
                                }" stroke-width="2" fill="none"/>"""
                            )
                            for (i in 0 until junction.location.blocks.size - 1) {
                                for (h in ss.helices) {
                                    if (h.location.start == junction.location.blocks[i].end) {
                                        booquet[h.name]?.let { helixCoords ->
                                            if (helixCoords[1] != junctionCoords[1]) {
                                                val (_, p2) = pointsFrom(
                                                    Point2D.Double(
                                                        helixCoords[0].toDouble(),
                                                        helixCoords[1].toDouble()
                                                    ),
                                                    Point2D.Double(
                                                        junctionCoords[0].toDouble(),
                                                        junctionCoords[1].toDouble()
                                                    ),
                                                    (junction_diameter.toDouble() + 10.0) / 2.0
                                                )
                                                svgBuffer.append(
                                                    """<line x1="${helixCoords[0] - minX}" y1="${helixCoords[1] - minY}" x2="${p2.x - minX}" y2="${p2.y - minY}" stroke="${
                                                        getHTMLColorString(
                                                            Color.BLACK
                                                        )
                                                    }" stroke-width="2" stroke-linecap="round"/>"""
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            svgBuffer.append("</svg>")

            val f = File("/Users/fjossinet/Downloads/${rfamID}.svg")
            f.createNewFile()
            f.writeText(svgBuffer.toString())
        }
    } catch (e:Exception) {
        println("Exception for $rfamID: ${e.message}")
    }
}

