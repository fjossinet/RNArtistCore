import io.github.fjossinet.rnartist.core.booquet
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import io.github.fjossinet.rnartist.core.rnartist
import io.github.fjossinet.rnartist.core.ss
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.*

class Test {

    fun testDsl() {

        rnartist {
            file = "media/3way_0.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            }
            theme {
                details_lvl = 1
            }
        }

        rnartist {
            file = "media/3way_2.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            }
            theme {
                details_lvl = 5

                color {
                    type = "C"
                    value = "deepskyblue"
                }

                color {
                    type = "U"
                    value = "darkgreen"
                }
            }

            layout {

                junction {
                    type = 3
                    out_ids ="nnw nne"
                }

                junction {
                    type = 1
                    radius = 50.0
                }
            }
        }

        rnartist {
            file = "media/3way_3.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
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

        rnartist {
            file = "media/3way_4.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            }
            theme {
                details_lvl = 5

                details {
                    type = "r R"
                    location="12:20"
                    value = "none"
                }

                color {
                    type = "C"
                    value = "deepskyblue"
                }

                color {
                    type = "U"
                    value = "darkgreen"
                }
            }

        }

        rnartist {
            file = "media/3way_5.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            }
            theme {
                details_lvl = 1
            }

        }

        rnartist {
            file = "media/3way_6.svg"
            ss {
                bracket_notation =
                    "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            }
            theme {
                details_lvl = 1
            }

        }

    }

    fun tPDB() {
        val pdb = PDB()
        pdb.query().subList(7,10).forEach { pdbId ->
            val pdbFile = java.io.File.createTempFile(pdbId, ".pdb")
            pdbFile.writeText(pdb.getEntry(pdbId).readText())
            for (ts in parsePDB(FileReader(pdbFile))) {
                rnartist {
                    file = "${System.getProperty("user.home")}/${pdbId}_${ ts.rna.name}.svg"
                    ss {
                        pdb {
                            id = pdbId
                            name = ts.rna.name
                        }
                    }
                    theme {
                        details {
                            type = "helix"
                            value = "full"
                        }
                        details {
                            type = "secondary_interaction"
                            value = "full"
                        }
                        details {
                            type = "single_strand"
                            value = "full"
                        }
                        details {
                            type = "pknot"
                            value = "full"
                        }
                        details {
                            type = "tertiary_interaction"
                            value = "full"
                        }
                        details {
                            type = "interaction_symbol"
                            value = "full"
                        }
                        details {
                            type = "phosphodiester_bond"
                            value = "full"
                        }
                        details {
                            type = "junction"
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
                        RnartistConfig.colorSchemes["Persian Carolina"]!!.forEach { elementType, config ->
                            config.forEach {
                                color {
                                    type = elementType
                                    value = it.value
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun tRNACentral() {
        val id = "URS000044DFF6"
        RNACentral().fetch(id)?.let {
            rnartist {
                file = "${System.getProperty("user.home")}/${id}.svg"
                secondaryStructures.add(it)
                theme {
                    details {
                        type = "helix"
                        value = "full"
                    }
                    details {
                        type = "secondary_interaction"
                        value = "full"
                    }
                    details {
                        type = "single_strand"
                        value = "full"
                    }
                    details {
                        type = "pknot"
                        value = "full"
                    }
                    details {
                        type = "tertiary_interaction"
                        value = "full"
                    }
                    details {
                        type = "interaction_symbol"
                        value = "full"
                    }
                    details {
                        type = "phosphodiester_bond"
                        value = "full"
                    }
                    details {
                        type = "junction"
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
                    RnartistConfig.colorSchemes["Persian Carolina"]!!.forEach { elementType, config ->
                        config.forEach {
                            color {
                                type = elementType
                                value = it.value
                            }
                        }
                    }
                }
            }
        }
    }
}