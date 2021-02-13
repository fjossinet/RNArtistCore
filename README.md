RNArtistCore
============

RNArtistCore provides a DSL (Domain Specific Language) and a Kotlin library to describe and plot RNA secondary structures. As a library it is used in the projects [RNArtist](https://github.com/fjossinet/RNArtist) and [RNArtistBackend](https://github.com/fjossinet/RNArtistBackEnd).

![](media/booquet_from_pdb_0.png)

<p float="left">
  <img src="/media/details_lvl5_colored_A.png" width="300" />
  <img src="/media/hide_pyrimidines_A.png" width="300" />
</p>

# Installation

You need to have the build tool [Maven](https://maven.apache.org) and a [Java distribution](https://www.oracle.com/java/technologies/javase-downloads.html) to be installed (type the commands ```mvn``` and ```java``` from a command line to check). 

Clone this repository and inside its root directory type:

<pre>mvn clean package</pre>

Once done, in the subdirectory named "target", you will find the file rnartistcore-{version}-jar-with-dependencies.jar. 

# The RNArtistCore DSL

RNArtistCore provides a domain-specific language (DSL) to write scripts more easily. You can have a look at examples in the file scripts/dsl.kts

To run a script, you need to have the [kotlin command installed on you computer](https://kotlinlang.org/docs/tutorials/command-line.html).

To run a script, type the following command:

<pre>kotlin -cp target/rnartistcore-{version}-jar-with-dependencies.jar your_script.kts</pre>

## How to write your scripts

Using pseudo-code, here is the structure that your script has to follow:

```kotlin
drawing_algorithm {

  parameter_1 = "value"
  parameter_2 = value

  secondary_structure {

    parameter_3 = "value"
    parameter_4 = value

    rna {
        parameter_5 = value
        parameter_6 = "value"
    }
  
  }

}
```

As you can see, you need to describe an RNA molecule, on which is constructed a secondary structure, used by an algorithm to produce a drawing.

Here is a real example:

```kotlin
rnartist {
    file = "media/real_example.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
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
```

![](media/real_example_A.png)

In the next paragraphs, we will detail the elements available to describe an RNA molecule, a secondary structure and a drawing algorithm.

### How to define an RNA molecule

Using the element ```rna```, you can create an RNA molecule from scratch. The parameters available are:

* **name**: the name of the molecule (default value: "A")
* **sequence**: the sequence of your molecule. If the parameter length is not provided, the sequence is mandatory
* **length**: the length of your sequence. If this parameter is provided, a random sequence will be computed. If the parameter sequence is not provided, the length is mandatory

Examples:

```kotlin
rna {
  name = "My Fav RNA"
  sequence = "GGGACCGCCCGGGAAACGGGCGAAAAACGAGGUGCGGGCACCUCGUGACGACGGGAGUUCGACCGUGACGCAUGCGGAAAUUGGAGGUGAGUUCGCGAAUACAAAAGCAAGCGAAUACGCCCUGCUUACCGAAGCAAGCG"
}
```

```kotlin
rna {
  name = "My Fav RNA 2"
  length = 50
}
```

```kotlin
rna {
  length = 200
}
```

### How to define a Secondary Structure

You have three different ways to define a seconday structure:
* from scratch using the element **ss**
* from a file using elements like **vienna**, **bpseq**, **ct**, **stockholm**
* from a public database using elements like **rfam**, **rnacentral**, **pdb**

***From scratch***

The parameters available are:
* **rna**: an rna molecule described with the **```rna```** element (see previous paragraph). If you don't provide any ```rna``` element, it will be computed for you with the default name and a random sequence fitting the base-pairing constraints.
* **bracket_notation**: the secondary structure described with the dot-bracket notation

Examples:

```kotlin
ss {
  
  rna {
    name = "My Fav RNA"
    length = 12
  }
  
  bracket_notation = "((((....))))"
  
}
```

```kotlin
ss {
  
  bracket_notation = "((((....))))"
  
}
```

***From a file***

You don't need to provide any ```rna``` element, it will be constructed automatically from the data stored in the file. 

To be able to use the PDB format, you need to have the RNAVIEW algorithm installed with the [Docker container assemble2](https://hub.docker.com/r/fjossinet/assemble2/). RNArtistCore will delegate to RNAVIEW the annotation of the 3D structure into a 2D.

The parameters available are:
* **file**: the absolute path and the name of your file
* **name**: if the file contains several molecular chains, this parameter allows to precise the one needed. If no name is provided, all the molecular chains will be processed.

Examples:
```kotlin
ss {
  bpseq {
    file = "/home/bwayne/myrna.bpseq"
  }
}
```

```kotlin
ss {
  ct {
    file = "/home/bwayne/myrna.ct"
  }
}
```

```kotlin
ss {
  vienna {
    file = "/home/bwayne/myrna.vienna"
  }
}
```

```kotlin
ss {
  pdb {
    file = "/home/bwayne/myrna.pdb"
    name = "A"
  }
}
```


```kotlin
ss {
  stockholm {
    file = "/home/bwayne/RF00072.stk"
    name = "consensus"
  }
}
```

```kotlin
ss {
  stockholm {
    file = "/home/bwayne/RF00072.stk"
    name = "AJ009730.1/1-133"
  }
}
```

```kotlin
ss {
  stockholm {
    file = "/home/bwayne/RF00072.stk"
  }
}
```
***From a public database***

You don't need to provide any ```rna``` element, it will be constructed automatically from the data stored in the database entry.

The parameters available are:
* **```id```**: the id of your database entry
* **```name```**: if the entry contains several molecular chains, this parameter allows to precise the one needed.  If no name is provided, all the molecular chains will be processed.

Examples:

```kotlin
ss {
  rfam {
    id = "RF00072"
    name = "AJ009730.1/1-133"
  }
}
```

```kotlin
ss {
  rfam {
    id = "RF00072"
    name = "consensus"
  }
}
```

```kotlin
ss {
  rfam {
    id = "RF00072"
  }
}
```

```kotlin
ss {
  pdb {
    id = "1EHZ"
  }
}
```

```kotlin
ss {
  pdb {
    id = "1JJ2"
    name = "0"
  }
}
```

```kotlin
ss {
  pdb {
    id = "1JJ2"
  }
}
```

### How to define a drawing algorithm

Two algorithms are available:
* the one used by the graphical tool [RNArtist](https://github.com/fjossinet/RNArtist)
* booquet

Both algorithms need a secondary structure element (see previous paragraph) and save their results in SVG files. Each molecular chain will be exported in its own SVG file. Each algorithm has its own parameters to configure the drawing process and the final result.

***The RNArtist algorithm***

The parameters available are:
* **```file```**: the absolute path and the name of the SVG output file. The name of the molecular chain will be merged to the file name.
* **```ss```**: a secondary structure element (see above)
* **```data```**: a dataset (see above)
* **```theme```**: see below

The size of the picture will fit the size of the drawing (with a minimum size of 1024x768 to see the residue letters).

____The **```data```** element____

Datasets can be linked to a RNA secondary structure. You can either fill the dataset in the script, or load it from a file.

```kotlin
rnartist {
    file = "example1.svg"
    ss {
        rna {
            sequence = "GCUUCAUAUAAUCCUAAUGAUAUGGUUUGGGAGUUUCUACCAAGAGCCUUAAACUCUUGAUUAUGAAGUG"
        }
        bracket_notation = "((((((((...((((((.........))))))((....))((((((.......))))))..))))))))."
    }
    data {
        "1" to 200.7
        "2" to 192.3
        "3" to 143.6
    }
}
```

```kotlin
rnartist {
    file = "example1.svg"
    ss {
        rna {
            sequence = "GCUUCAUAUAAUCCUAAUGAUAUGGUUUGGGAGUUUCUACCAAGAGCCUUAAACUCUUGAUUAUGAAGUG"
        }
        bracket_notation = "((((((((...((((((.........))))))((....))((((((.......))))))..))))))))."
    }
    data {
        file = "QuSHAPE_01_shape_mode_reactivities.txt"
    }
}
```

The values linked to each residue can be used as a selection criteria to define the colors, line width and details level (see below). 

____The **```theme```** element____

Using a **```theme```**, you can define your drawing options for any elements, from single residues to entire structural domains like helices or junctions.

To quickly change the details level of your entire 2D, you can use the parameter named **```details_lvl```**. Five details level are available:

```kotlin
rnartist {
    file = "media/details_lvl1.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 1
    }
}
```

![](media/details_lvl1_A.png)

```kotlin
rnartist {
    file = "media/details_lvl2.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 2
    }
}
```

![](media/details_lvl2_A.png)

```kotlin
rnartist {
    file = "media/details_lvl3.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 3
    }
}
```

![](media/details_lvl3_A.png)

```kotlin
rnartist {
    file = "media/details_lvl4.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 4
    }
}
```

![](media/details_lvl4_A.png)

```kotlin
rnartist {
    file = "media/details_lvl5.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 5
    }
}
```

![](media/details_lvl5_A.png)

Inside a ```theme``` element, you can also add several times the following elements:
* **```details```**: define the resolution of the element
  * **```value```**: "full" to draw all the details
  * **```type```**: the type of the elements targeted
  * **```location```**: the location of the elements targeted
* **```hide```**: hide residues
  * **```type```**: can only be a lower or upper letter (default is "N"). Lower or upper letter will produce the same results (the letter and the shape of the delected residues is hidden)
  * **```location```**: the location of the residues to hide
  * **```data```**: selection based on the values linked to the residues
* **```highlight```**: highlight residues
  * **```type```**: can only be a lower or upper letter (default is "N"). Lower or upper letter will produce the same results (the letter and the shape of the delected residues is hidden)
  * **```location```**: the location of the residues to hide
  * **```data```**: selection based on the values linked to the residues
  * **```color```**: an HTML color code or predefined color name (see below)
  * **```width```**: the line width
* **```color```**: define the color of the element
  * **```value```**: an HTML color code or predefined color name (see below)
  * **```from```**: first color in a gradient (HTML color code or predefined color name (see below))
  * **```to```**: last color in a gradient (HTML color code or predefined color name (see below))
  * **```type```**: the type of the elements targeted
  * **```location```**: the location of the elements
   targeted
  * **```data```**: selection based on the values linked to the residues
* **```line```**: define the width of the line
  * **```value```**: the line width
  * **```type```**: the type of the elements targeted
  * **```location```**: the location of the elements targeted

The parameter **```type```** can have the following values:
  * "A", "U", "G", "C", "X", "N", "R", "Y": using capital letters for residues target the circle surrounding the residue letter. "N" is for any residue, "R" for purines, and "Y" for pyrimidines 
  * "a", "u", "g", "c", "x", "n", "ry", "y": using lowercase letters for residues target the letter inside the circle. "n" is for any residue, "r" for purines, and "y" for pyrimidines 
  * "helix"
  * "single_strand"
  * "junction"
  * "secondary_interaction"
  * "tertiary_interaction"
  * "phosphodiester_bond"
  * "interaction_symbol"
  * "pknot"

The parameter **```location```** needs to have the following format: "start_position_1:length, start_position_2:length, ..."

____The **```details```** element____

If a dataset is linked to the RNA secondary structure, the values can be used as a selection criteria. Using the parameter  **```data```**, you can select values lower than a value (lt), greater than a value (gt) or between two values (between).

```kotlin
rnartist {
    file = "media/dataset_hide.svg"
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
```

![](media/dataset_hide_A.png)

```kotlin
rnartist {
    file = "media/hide_purines.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
    }
    theme {
        details_lvl = 5

        details {
            type = "R"
            location="12:20"
            value = "none"
        }

        details {
            type = "r"
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
```

![](media/hide_purines_A.png)

____The **```hide```** element____

```kotlin
rnartist {
    file = "media/hide_pyrimidines.svg"
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
```

![](media/hide_pyrimidines_A.png)

____The **```color```** element____

The parameters **```value```**, **```from```** and **```to```** can be an HTML color code or a predefined color name (see [the end of this file](https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/src/main/kotlin/io/github/fjossinet/rnartist/core/builders.kt) for an updated list of color names).

Examples:

```kotlin
rnartist {
    file = "media/details_lvl5_colored.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
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
```

![](media/details_lvl5_colored_A.png)

If a dataset is linked to the RNA secondary structure, a colored gradient can be defined inside the **```color```** element. You need to use the parameters  **```from```** and  **```to```**. To restrict the distribution of values to be used, you can use the parameter  **```data```**. You can select values lower than a value (lt), greater than a value (gt) or between two values (between).

```kotlin
rnartist {
    file = "media/dataset.svg"
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
            from = "lightyellow"
            to = "firebrick"
            data between 10.0..350.0
        }
        color {
            type = "n"
            from = "black"
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
```

![](media/dataset_A.png)

____Mix kotlin code____

If you know Kotlin, you can embed Kotlin instructions to power your script.

```kotlin
rnartist {
    file = "media/kotlin_powered.svg"
    ss {
        bracket_notation =
            ".(((.(((..........(((((((..(((....)))......(((....)))...)))))))...))).)))"
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
            from = "lightyellow"
            to = "firebrick"
        }

        color {
            type = "r"
            from = "black"
            to = "white"
        }

        hide {
            type = "Y"
        }

    }
}
```

![](media/kotlin_powered_A.png)

***The Booquet algorithm***

This algorithm has less options than the rnartist one. The parameters available are:
* **```file```**: the absolute path and the name of the SVG output file
* **```width```**: the width of the view containing the drawing (default: 600)
* **```height```**: the height of the view containing the drawingg (default: 600)
* **```color```**: an HTML color code or color name
* **```line```**: the width for the lines
* **```junction_diameter```**: the diameter of the circles
* **```ss```**: a secondary structure element (see above) 

The drawing will be automatically zoomed to fit the view.

```kotlin
booquet {
    file = "media/booquet_from_rfam.svg"
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
```

![](media/booquet_from_rfam_AJ009730.1_1-133.png)

```kotlin
booquet {
    file = "media/booquet_from_vienna.svg"
    junction_diameter = 15.0
    color = "olive"
    line = 3.0
    ss {
        vienna {
            file = "samples/rna.vienna"
        }
    }
}
```

![](media/booquet_from_vienna_A.png)

```kotlin
booquet {
    file = "media/booquet_from_ct.svg"
    junction_diameter = 15.0
    color = "darkorchid"
    ss {
        ct {
            file = "samples/ASE_00010_from_RNA_STRAND_database.ct"
        }
    }
}
```

![](media/booquet_from_ct_A.png)

```kotlin
booquet {
    file = "media/booquet_from_pdb.svg"
    junction_diameter = 15.0
    color = "darkmagenta"
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
```

![](media/booquet_from_pdb_0.png)

# The RNArtistCore library

You need to have the build tool [Maven](https://maven.apache.org) installed. 
No stable release for now, only snapshots. To use RNArtistCore in a Java application, just add the below dependency in your file pom.xml:

```xml
    <repositories>
        <repository>
            <id>maven-snapshots</id>
            <url>http://oss.sonatype.org/content/repositories/snapshots</url>
            <layout>default</layout>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.github.fjossinet.rnartist.core</groupId>
            <artifactId>rnartistcore</artifactId>
            <version>0.2.5-SNAPSHOT</version>
        </dependency>
    </dependencies>
```