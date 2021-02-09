/**
 * To run this script: kotlin -cp target/rnartistcore-{version}-jar-with-dependencies.jar scripts/dsl/dsl.kts
 */
import io.github.fjossinet.rnartist.core.*

rnartist {
    file = "media/example1.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
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
            type = "R"
            location="12:10"
            value = "full"
        }

        details {
            type = "Y"
            location="25:10, 40:5"
            value = "full"
        }

        details {
            type = "r"
            location="12:10"
            value = "full"
        }

        color {
            type="A"
            value = "#A0ECF5"
        }

        color {
            type="a"
            value = "#000000"
        }

        color {
            type="U"
            value = "#9157E5"
        }

        color {
            type="G"
            value = "#93E557"
        }

        color {
            type="C"
            value = "#E557E5"
        }

    }
}

rnartist {
    file = "media/example2.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"

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
            type = "tertiary_interaction"
            value = "none"
        }

        details {
            type = "N"
            value = "full"
        }

        details {
            type = "r"
            value = "full"
        }

        details {
            type = "y"
            value = "full"
        }

        color {
            type = "R"
            value = "#15BD1A"
        }

        color {
            type = "Y"
            value = "#FFC300"
        }

        color {
            type = "r"
            value = "#FFFFFF"
        }

        color {
            type = "u"
            value = "#000000"
        }

        color {
            type = "c"
            value = "#FE1102"
        }

        line {
            type = "phosphodiester_bond"
            value = 5.0
        }

        line {
            type = "secondary_interaction"
            value = 1.0
        }
    }
}

rnartist {
    file = "media/example3.svg"
    ss {
        pdb {
            file = "/Volumes/Data/Projets/RNArtistCore/samples/1u6b.pdb"
            name = "B"
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

booquet {
    file = "media/example4.svg"
    junction_diameter = 15.0
    color = "#000000"
    line = 1.0
    ss {
        rfam {
            id = "RF00072"
            name = "AJ009730.1/1-133"
        }
    }
}

booquet {
    file = "media/example5.svg"
    junction_diameter = 15.0
    color = "#15BD15"
    line = 5.0
    ss {
        vienna {
            file = "samples/rna.vienna"
        }
    }
}

booquet {
    file = "media/example6.svg"
    junction_diameter = 15.0
    color = "#BD8515"
    ss {
        ct {
            file = "samples/ASE_00010_from_RNA_STRAND_database.ct"
        }
    }
}

booquet {
    file = "media/example7.svg"
    junction_diameter = 15.0
    color = "#BD8515"
    width = 1200.0
    height = 800.0
    line = 0.5
    ss {
        pdb {
            file = "/Volumes/Data/Projets/RNArtistCore/samples/1jj2.pdb"
            name = "0"
        }
    }
}

booquet {
    file = "media/example8.svg"
    junction_diameter = 15.0
    color = "#BD1576"
    ss {
        bpseq {
            file = "samples/SRP_00001_from_RNA_STRAND_database.bpseq"
        }
    }
}

booquet {
    file = "media/example9.svg"
    junction_diameter = 15.0
    color = "#000000"
    line = 1.0
    ss {
        rfam {
            id = "RF00072"
        }
    }
}

rnartist {
    file = "media/example10.svg"
    ss {
        rfam {
            id = "RF00072"
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