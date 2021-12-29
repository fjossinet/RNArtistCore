/**
 * This file is one of the file available from a new project directory created with the sscript rnartistcore.sh (see README for details).
 */

import io.github.fjossinet.rnartist.core.*

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
        details {
            value = 5
        }

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
            file = "/project/inputs/1gid.pdb"
        }
    }
    theme {
        details {
            value = 5
        }

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
            file = "/project/inputs/RF02001.stockholm"
        }
    }

    theme {
        details {
            value = 3
        }

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