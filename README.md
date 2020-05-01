RNArtist Core library
=====================

A Kotlin library used in the projects RNArtist and RNArtistBackend. This library provides a model to describe and plot RNA secondary structures.

# Get a secondary structure
## from a file
```kotlin
val ss = parseBPSeq(FileReader("my_file.bpseq"))
```
## from scratch
```kotlin
val ss1 = parseVienna(StringReader(">test\nCGCUGAAUUCAGCG\n((((......))))"))
val ss2 = SecondaryStructure(RNA(name="myRNA",seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
```

# Get a plot
```kotlin
var drawing = SecondaryStructureDrawing(secondaryStructure = ss2)
//we tweak the default theme
drawing.theme.fontName = "Arial"
drawing.theme.residueBorder = 3
drawing.theme.AColor = Color.RED

var writer = FileWriter("media/myRNA.svg")
writer.write(drawing.asSVG())
writer.close()
```
And you get:

![myRNA capture](media/myRNA.png)

And now something larger
```kotlin

val seq = "GGGACCGCCCGGGAAACGGGCGAAAAACGAGGUGCGGGCACCUCGUGACGACGGGAGUUCGACCGUGACGCAUGCGGAAAUUGGAGGUGAGUUCCCUGCUUACCGAAGCAAGCG"
val bn = ".....((((((.....))))))....((((((((....))))))))....((((........))))..(((.(((..........(((((((.....)))))))...))).)))"

val ss = SecondaryStructure(RNA(name="myRNA2",seq = seq), bracketNotation = bn)
val drawing = SecondaryStructureDrawing(secondaryStructure = ss)
//we tweak the default theme
drawing.theme.fontName = "Courier New"
drawing.theme.residueBorder = 1
drawing.theme.AColor = Color.RED

val writer = FileWriter("media/myRNA2.svg")
writer.write(drawing.asSVG())
writer.close()
```
And you get:

![myRNA2 capture](media/myRNA2.png)

Now you can pursue with Affinity Designer or Inkscape:

![Affinity Designer capture](media/AffinityDesigner.png)

![Inkscape capture](media/inkscape.png)