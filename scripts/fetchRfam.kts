import java.nio.file.Files
import java.nio.file.Paths
import io.github.fjossinet.rnartist.core.model.getHTMLColorString

(1..4108).forEach {
    val rfamID = "RF${"%05d".format(it)}"

    val consensusStructure = rnartist {
        svg {
            path = "rfam/rnartist/$rfamID"
        }

        ss {
            rfam {
                id = rfamID
                name = "consensus"
                use alignment numbering
            }

        }

        theme {
            details {
                value = 3
            }
            color {
                scheme = "Structural Domains"
            }
        }
    }.first()

    rnartist {
        svg {
            path = "rfam/rnartist/$rfamID"
        }

        ss {
            rfam {
                id = rfamID
                use alignment numbering
            }

        }

        theme {
            details {
                value = 3
            }
            consensusStructure.allHelices.forEach { helix ->
                color {
                    location {
                        helix.location.blocks.first().start to helix.location.blocks.first().end
                        helix.location.blocks.last().start to helix.location.blocks.last().end
                    }
                    value = "${getHTMLColorString(helix.getColor())}"
                }
            }
            consensusStructure.allJunctions.forEach { junction ->
                color {
                    location {
                        junction.location.blocks.forEach { block ->
                            block.start+1 to block.end-1
                        }
                    }
                    value = "${getHTMLColorString(junction.getColor())}"
                }
            }
            consensusStructure.singleStrands.forEach { sstrand ->
                color {
                    location {
                        sstrand.location.blocks.first().start to sstrand.location.blocks.first().end
                    }
                    value = "${getHTMLColorString(sstrand.getColor())}"
                }
            }
        }
    }.first()

    /*booquet {
        path = "rfam/booquet/$rfamID"
        junction_diameter = 15.0
        color = "midnightblue"
        line = 1.0
        ss {
            rfam {
                id = rfamID
                name = "consensus"
                use alignment numbering
            }
        }
    }*/
}



