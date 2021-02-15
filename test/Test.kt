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
            file = "media/helix_details1.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1
            }
        }

        rnartist {
            file = "media/helix_details2.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix"
                    value = "full"
                    location="7:4,15:4"
                }
            }
        }

        rnartist {
            file = "media/helix_details3.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix phosphodiester_bond"
                    value = "full"
                    location="7:4,15:4"
                }
            }
        }

        rnartist {
            file = "media/helix_details4.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix phosphodiester_bond secondary_interaction"
                    value = "full"
                    location="7:4,15:4"
                }
            }
        }

        rnartist {
            file = "media/helix_details5.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix phosphodiester_bond secondary_interaction N"
                    value = "full"
                    location="7:4,15:4"
                }
            }
        }

        rnartist {
            file = "media/helix_details6.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix phosphodiester_bond secondary_interaction N n"
                    value = "full"
                    location="7:4,15:4"
                }
            }
        }

        rnartist {
            file = "media/helix_combination_details.svg"
            ss {
                bracket_notation =
                    "..((..((((....))))..))"
            }
            theme {
                details_lvl = 1

                details {
                    type = "helix phosphodiester_bond"
                    value = "full"
                    location="7:4,15:4"
                }

                details {
                    type = "secondary_interaction"
                    value = "full"
                    location="8,17"
                }

                details {
                    type = "N"
                    value = "full"
                    location="8"
                }

                details {
                    type = "secondary_interaction N"
                    value = "full"
                    location="9,16"
                }

                details {
                    type = "n"
                    value = "full"
                    location="16"
                }
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