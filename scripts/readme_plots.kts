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
    png {
        path = "media/"
    }
    ss {
        bn {
            seq = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "concrete_example"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            seq = "GCGAAAAAUCGC"
            value =
                "((((....))))"
            name = "dataset"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "details_lvl1"
        }
    }
    theme {
        details_lvl = 1
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "details_lvl2"
        }
    }
    theme {
        details_lvl = 2
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "details_lvl3"
        }
    }
    theme {
        details_lvl = 3
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "details_lvl4"
        }
    }
    theme {
        details_lvl = 4
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "details_lvl5"
        }
    }
    theme {
        details_lvl = 5
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            seq = "CAGAUAAGAAGGUUCCCCGAUAUGUUGGGCAACCAAAGAAUUCAUGUUCUUCCUUUGUUUG"
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
            name = "nice_colors"
        }
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
            value = "red"
        }

        color {
            type = "r"
            value = "white"
        }

        color {
            type = "G g"
            value = "#ed781f"
            location {
                5 to 20
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl0"
        }
    }

    theme {
        details_lvl = 1
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl1"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl2"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix secondary_interaction phosphodiester_bond"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl3"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix secondary_interaction  phosphodiester_bond N"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl4"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix secondary_interaction phosphodiester_bond N n"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_lvl5"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix secondary_interaction phosphodiester_bond N n interaction_symbol"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_mixed_lvl"
        }
    }

    theme {
        details_lvl = 1

        show {
            type = "helix secondary_interaction phosphodiester_bond"
            location {
                7 to 10
                15 to 18
            }
        }

        show {
            type = "N n"
            location {
                7 to 8
                17 to 18
            }
        }

        show {
            type = "interaction_symbol"
            location {
                8 to 8
                17 to 17
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "CUUACUCGAGUGACCUUGCUUG"
            value = "..((..((((....))))..))"
            name = "helix_hidden_parts"
        }
    }

    theme {
        details_lvl = 5

        hide {
            type = "n"
            location {
                9 to 9
            }
        }

        hide {
            type = "secondary_interaction"
            location {
                7 to 8
                10 to 10
                15 to 15
                17 to 18
            }
        }
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_0"
        }
    }
    theme {
        details_lvl = 1
    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_1"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_2"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_3"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_4"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_5"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_6"
        }
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
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_full_details"
        }
    }
    theme {
        details_lvl = 5
    }

    layout {

        junction {
            type = 3
            out_ids ="wnw n"
        }

    }
}

rnartist {
    png {
        path = "media/"
    }
    ss {
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "3way_full_details_fixed"
        }
    }
    theme {
        details_lvl = 5
    }
    layout {
        junction {
            type = 3
            out_ids = "wnw n"
        }
        junction {
            location {
                206 to 209
                232 to 235
                248 to 251
            }
            out_ids = "n e"
        }
    }
}


rnartist {

    png {
        path = "media/"
    }

    ss {
        bn {
            seq = "UCUUUCGUUUAUCAGGUCCGUCGCUGGGCUUUCCGUAAGAUUCUCACGUCGAAUGGUGUUCGGAGACUGAACUUUUUUAGCUUUAUGAGGGGGGUUACAGACUUCCGUCUGCUACGUGCGGGGGAACCGUACCACUGUCGGAUGUGGUCCCUUGCGCUCAAGGUGCUGCGACGCGCAGGUGCGUGAUCCAGAUAGGCAACACCCAUAUCAAUGCUAUCUGGAGGUAGUAUUGAUAGCCGUGGCUGGCUAUGUGUUUUGUGCUGAUAACCAUCAGACGGUGCCGGU"
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
            name = "rnartist_preview"
        }
    }
    theme {
        details_lvl = 5
        color {
            value = "#800080"
            type = "R"
        }
        color {
            value = "#ffcc80"
            type = "Y"
        }
        color {
            value = "#000000"
            type = "y"
        }
        color {
            value = "#99cc99"
            location {
                145 to 168
            }
        }
        color {
            value = "#b31a1a"
            location {
                199 to 235
                248 to 259
            }
        }
        hide {
            location {
                38 to 83
            }
            type = "N n"
        }
        hide {
            location {
                98 to 111
            }
            type = "R r"
        }
        hide {
            location {
                235 to 239
                244 to 248
            }
            type = "N n"
        }
        hide {
            location {
                145 to 168
            }
            type = "helix junction"
        }
        hide {
            location {
                199 to 235
                248 to 259
            }
            type = "N n interaction_symbol"
        }
        line {
            type = "phosphodiester_bond"
            value = 2.2
        }
        line {
            location {
                145 to 168
            }
            value = 5.0
        }
        line {
            location {
                199 to 235
                248 to 259
            }
            type = "secondary_interaction phosphodiester_bond interaction_symbol"
            value = 5.0
        }
    }
    layout {
        junction {
            type = 3
            out_ids = "wnw n"
        }
        junction {
            location {
                206 to 209
                232 to 235
                248 to 251
            }
            out_ids = "n e"
        }
    }
}


/*rnartist {
    file = "/project/media/hide_purines.svg"
    bn {
        value =
            "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
    }
    theme {
        details_lvl = 5

        show {
            type = "r R"

            location {
                12 to 32
            }
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
        bn {
            seq = "GCGAAAAAUCGC"
            value =
                "((((....))))"
        }
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
            location {
                5 to 9
            }
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
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }
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
            location {
                10 to 20
            }
        }
    }
}

rnartist {
    file = "/project/media/partially_detailed.svg"
    ss {
        bn {
            value =
                "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type ="helix secondary_interaction phosphodiester_bond"
            location {
                16 to 21
                24 to 29
            }
        }

    }
}

rnartist {
    file = "/project/media/helix_details1.svg"
    ss {
        bn {
            value =
            "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1
    }
}

rnartist {
    file = "/project/media/helix_details2.svg"
    ss {
        bn {
            value =
            "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type = "helix"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    file = "/project/media/helix_details3.svg"
    bn {
        value =
            "..((..((((....))))..))"
    }
    theme {
        details_lvl = 1

        show {
            type = "helix phosphodiester_bond"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    file = "/project/media/helix_details4.svg"
    ss {
        bn  {
            value = "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type = "helix phosphodiester_bond secondary_interaction"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    file = "/project/media/helix_details5.svg"
    ss {
        bn  {
            value = "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type = "helix phosphodiester_bond secondary_interaction N"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    file = "/project/media/helix_details6.svg"
    ss {
        bn  {
            value = "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type = "helix phosphodiester_bond secondary_interaction N n"
            location {
                7 to 10
                15 to 18
            }
        }
    }
}

rnartist {
    file = "/project/media/helix_combination_details.svg"
    ss {
        bn  {
            value = "..((..((((....))))..))"
        }
    }
    theme {
        details_lvl = 1

        show {
            type = "helix phosphodiester_bond"
            location {
                7 to 10
                15 to 18
            }
        }

        show {
            type = "secondary_interaction"
            location {
                8 to 8
                17 to 17
            }
        }

        show {
            type = "N"
            location {
                8 to 8
            }
        }

        show {
            type = "n"
            location {
                16 to 16
            }
        }
    }
}

rnartist {
    file = "/project/media/lines.svg"
    ss {
        bn {
            value = "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }
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
            location {
                8 to 13
            }
        }

    }
}

rnartist {
    file = "/project/media/kotlin_powered.svg"
    ss {
        bn {
            value = "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }
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
        bn {
            seq = "GCGAAAAAUCGC"
            value = "((((....))))"
        }

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
        bn {
            value = "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }
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
            location {
                8 to 13
            }
        }
    }
}

rnartist {
    file = "/project/media/dataset_hide.svg"
    ss {
       bn {
            value ="((((....))))"
       }
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

        hide {
            type = "N"
            data between 10.0..350.0
        }

        hide {
            type = "n"
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
        }
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
        bn {
            value = "(((..(((..(((..(((((....))))).(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).)))..(((((....)))))..)))...)))...(((..(((.(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))...(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...))).(((((....)))))..)))...)))"
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
}*/

