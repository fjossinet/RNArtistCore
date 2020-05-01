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
val ss1 = parseVienna(StringReader(">test\nGGGAAACCC\n(((...)))"))
val ss2 = SecondaryStructure(RNA(name="myRNA",seq = "GGGAAACCC"),bracketNotation = "(((...)))")
```

# Get a plot
```kotlin
val drawing = SecondaryStructureDrawing(secondaryStructure = ss, frame = Rectangle(0,0,400,400))
```