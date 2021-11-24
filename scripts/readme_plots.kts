/**
 * This file is used to produce the pictures for the README file. If you want to produce them yourself:
 * - create an rnartistcore project with the script rnartistcore.sh (see README for details)
 * - copy this script in the project directory
 * - create an empty folder named media
 * - copy the folder samples from https://github.com/fjossinet/RNArtistCore/tree/master/samples
 * - type the command: ./plot_2ds.sh readme_plots.kts
 * All the output files will be stored in the media subfolder.
 */
import io.github.fjossinet.rnartist.core.*

rnartist {
    file = "/project/media/real_example.svg"
    ss {
        rna {
            sequence = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
        }
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 5

        color {
            type="A"
            value = "#A0ECF5"
        }

        color {
            type="a"
            value = "black"
        }

        color {
            type="U"
            value = "#9157E5"
        }

        color {
            type="G"
            value = "darkgreen"
        }

        color {
            type="C"
            value = "#E557E5"
        }

    }
}

rnartist {
    file = "/project/media/dataset.svg"
    ss {
        rna {
            sequence = "GCGAAAAAUCGC"
        }
        bracket_notation =
            "((((....))))"
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
        "4" to 34.8
        "5" to 4.5
        "6" to 234.9
        "7" to 12.3
        "8" to 56.8
        "9" to 59.8
        "10" to 140.5
        "11" to 0.2
        "12" to 345.8
    }
    theme {
        details_lvl = 4
        color {
            type = "N"
            value = "lightyellow"
            to = "firebrick"
            data between 10.0..350.0
        }
        color {
            type = "n"
            value = "black"
            to = "white"
            data between 10.0..350.0
        }
        color {
            type = "N"
            value = "black"
            data lt 10.0
        }
        color {
            type = "n"
            value = "white"
            data lt 10.0
        }
    }
}

rnartist {
    file = "/project/media/details_lvl1.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }
}

rnartist {
    file = "/project/media/details_lvl2.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 2
    }
}


rnartist {
    file = "/project/media/details_lvl3.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 3
    }
}

rnartist {
    file = "/project/media/details_lvl4.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 4
    }
}

rnartist {
    file = "/project/media/details_lvl5.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 5
    }
}

rnartist {
    file = "/project/media/details_lvl5_colored.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 5

        color {
            type = "Y"
            value = "lavenderblush"
        }

        color {
            type = "y"
            value = "black"
        }

        color {
            type = "R"
            value = "green"
        }

    }

}

rnartist {
    file = "/project/media/hide_purines.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
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
    file = "/project/media/hide_pyrimidines.svg"
    ss {
        rna {
            sequence = "GCGAAAAAUCGC"
        }
        bracket_notation =
            "((((....))))"
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
        "4" to 34.8
        "5" to 4.5
        "6" to 234.9
        "7" to 12.3
        "8" to 56.8
        "9" to 59.8
        "10" to 140.5
        "11" to 0.2
        "12" to 345.8
    }
    theme {
        details_lvl = 5

        hide {
            location="5:4"
        }

        hide {
            type = "Y"
            data lt 150.0
        }

        color {
            type = "R"
            value = "deepskyblue"
        }

        color {
            type = "Y"
            value = "darkgreen"
        }

        color {
            type = "Y"
            value = "firebrick"
            data gt 200.0
        }

    }
}

rnartist {
    file = "/project/media/all_red.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 4

        color {
            value = "red"
        }

        color {
            type = "a c"
            value = "white"
        }

        color {
            type = "g u"
            value = "black"
            location = "10:10"
        }

    }
}

rnartist {
    file = "/project/media/partially_detailed.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1

        details {
            location = "6:3,53:3"
            value = "full"
        }

        details {
            type ="helix secondary_interaction phosphodiester_bond"
            location = "16:6,24:6"
            value = "full"
        }

        details {
            location = "21:4"
            value = "full"
        }

    }
}

rnartist {
    file = "/project/media/helix_details1.svg"
    ss {
        bracket_notation =
            "..((..((((....))))..))"
    }
    theme {
        details_lvl = 1
    }
}

rnartist {
    file = "/project/media/helix_details2.svg"
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
    file = "/project/media/helix_details3.svg"
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
    file = "/project/media/helix_details4.svg"
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
    file = "/project/media/helix_details5.svg"
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
    file = "/project/media/helix_details6.svg"
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
    file = "/project/media/helix_combination_details.svg"
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

rnartist {
    file = "/project/media/lines.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 3

        color {
            type = "Y"
            value = "chartreuse"
        }

        color {
            type = "R"
            value = "turquoise"
        }

        line {
            type = "phosphodiester_bond interaction_symbol"
            value = 0.1
        }

        line {
            type = "phosphodiester_bond N"
            value = 5.0
            location = "8:6"
        }

    }
}

rnartist {
    file = "/project/media/kotlin_powered.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    data {
        (1..secondaryStructures[0].length).forEach {
            "${it}" to Math.random()
        }
    }
    theme {
        details_lvl = 5

        color {
            type = "R"
            value = "lightyellow"
            to = "firebrick"
        }

        color {
            type = "r"
            value = "black"
            to = "white"
        }

        hide {
            type = "Y"
        }

    }
}

rnartist {
    file = "/project/media/dataset.svg"
    ss {
        rna {
            sequence = "GCGAAAAAUCGC"
        }
        bracket_notation =
            "((((....))))"
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
        "4" to 34.8
        "5" to 4.5
        "6" to 234.9
        "7" to 12.3
        "8" to 56.8
        "9" to 59.8
        "10" to 140.5
        "11" to 0.2
        "12" to 345.8
    }
    theme {
        details_lvl = 4
        color {
            type = "N"
            value = "lightyellow"
            to = "firebrick"
            data between 10.0..350.0
        }
        color {
            type = "n"
            value = "black"
            to = "white"
            data between 10.0..350.0
        }
        color {
            type = "N"
            value = "black"
            data lt 10.0
        }
        color {
            type = "n"
            value = "white"
            data lt 10.0
        }
    }
}

rnartist {
    file = "/project/media/several_types.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 5

        line {
            type = "phosphodiester_bond interaction_symbol"
            value = 0.1
        }

        line {
            type = "phosphodiester_bond N"
            value = 5.0
            location = "8:6"
        }
    }
}

rnartist {
    file = "/project/media/dataset_hide.svg"
    ss {
        bracket_notation =
            "((((....))))"
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
        "4" to 34.8
        "5" to 4.5
        "6" to 234.9
        "7" to 12.3
        "8" to 56.8
        "9" to 59.8
        "10" to 140.5
        "11" to 0.2
        "12" to 345.8
    }
    theme {
        details_lvl = 4

        details {
            type = "N"
            value = "none"
            data between 10.0..350.0
        }

        details {
            type = "n"
            value = "none"
            data between 10.0..350.0
        }

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
    file = "/project/media/3way_1.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="nnw nne"
        }

    }
}

rnartist {
    file = "/project/media/3way_2.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="nw ne"
        }
    }
}

rnartist {
    file = "/project/media/3way_3.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="wnw ene"
        }
    }
}

rnartist {
    file = "/project/media/3way_4.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="w e"
        }
    }
}

rnartist {
    file = "/project/media/3way_5.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="w n"
        }
    }
}

rnartist {
    file = "/project/media/3way_6.svg"
    ss {
        bracket_notation =
            "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 1
    }

    layout {

        junction {
            type = 3
            out_ids ="n e"
        }
    }
}

rnartist {
    file = "/project/media/3way_full_details.svg"
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

    layout {

        junction {
            type = 3
            out_ids ="nw ne"
        }

    }
}

booquet {
    file = "/project/media/booquet_from_rfam.svg"
    junction_diameter = 15.0
    color = "midnightblue"
    line = 1.0
    ss {
        rfam {
            id = "RF00072"
            name = "AJ009730.1/1-133"
        }
    }
}

booquet {
    file = "/project/media/booquet_from_vienna.svg"
    junction_diameter = 15.0
    color = "olive"
    line = 3.0
    ss {
        vienna {
            file = "/project/samples/rna.vienna"
        }
    }
}

booquet {
    file = "/project/media/booquet_from_ct.svg"
    junction_diameter = 15.0
    color = "darkorchid"
    ss {
        ct {
            file = "/project/samples/ASE_00010_from_RNA_STRAND_database.ct"
        }
    }
}

booquet {
    file = "/project/media/booquet_from_pdb.svg"
    junction_diameter = 15.0
    color = "darkmagenta"
    width = 1200.0
    height = 800.0
    line = 0.5
    ss {
        pdb {
            file = "/project/samples/1jj2.pdb"
            name = "0"
        }
    }
}
