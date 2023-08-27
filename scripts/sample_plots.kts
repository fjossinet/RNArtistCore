rnartist {
    svg {
        path = "outputs/"
    }
    ss {
        bn {
            seq = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
            value = "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
        }

    }

    theme {
        details = 5

        color {
            type = "A"
            value = "#A0ECF5"
        }

        color {
            type = "a"
            value = "black"
        }

        color {
            type = "U"
            value = "#9157E5"
        }

        color {
            type = "G"
            value = "darkgreen"
        }

        color {
            type = "C"
            value = "#E557E5"
        }

    }
}

rnartist {
    svg {
        path = "outputs/"
    }

    ss {
        pdb {
            name = "B"
            file = "samples/1gid.pdb"
        }
    }
    theme {
        details = 5

        color {
            type = "A"
            value = "#A0ECF5"
        }

        color {
            type = "a"
            value = "black"
        }

        color {
            type = "U"
            value = "#9157E5"
        }

        color {
            type = "G"
            value = "darkgreen"
        }

        color {
            type = "C"
            value = "#E557E5"
        }

    }
}

rnartist {
    svg {
        path = "outputs/"
    }

    ss {
        stockholm {
            file = "samples/RF02001.stockholm"
        }
    }

    theme {
        details = 3

        color {
            type = "A"
            value = "#A0ECF5"
        }

        color {
            type = "a"
            value = "black"
        }

        color {
            type = "U"
            value = "#9157E5"
        }

        color {
            type = "G"
            value = "darkgreen"
        }

        color {
            type = "C"
            value = "#E557E5"
        }

    }
}

rnartist {
    ss {
        rfam {
            use alignment numbering
        }
    }
}