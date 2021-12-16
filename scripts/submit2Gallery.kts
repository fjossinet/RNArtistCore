import io.github.fjossinet.rnartist.core.*

rnartist {
    png {
        path = "C:/Users/fjoss/Downloads/"
        width = 600.0
        height = 300.0
        location {
            12 to 14
        }
    }
    ss {
        bn {
            seq = "AUGAUCAAGGAAUUGAUGU"
            value = "(((..(((....))).)))"
        }
    }
    theme {
        details_lvl = 5
        color {
            value = "red"
            type = "N"
            location {
                12 to 14
            }
        }
    }
}

rnartist {
    svg {
        path = "C:/Users/fjoss/Downloads/"
        width = 1200.0
        height = 100.0
        location {
            1 to 2
            4 to 6
        }
    }
    ss {
        bn {
            seq = "AUGAUCAAGGAAUUGAUGU"
            value = "(((..(((....))).)))"
        }
    }
    theme {
        details_lvl = 5

        color {
            value = "green"
            type = "N"
            location {
                1 to 2
                4 to 6
            }
        }
    }
}

rnartist {
    svg {
        path = "C:/Users/fjoss/Downloads/"
        width = 500.0
        height = 100.0
        location {
            10 to 20
        }
    }
    ss {
        rfam {
            id = "RF00100"
        }
    }
    theme {
        details_lvl = 5

        color {
            value = "green"
            type = "N"
            location {
                10 to 20
            }
        }
    }
}