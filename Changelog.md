### 0.2.15-SNAPSHOT

- **define an RNA 2D with a bracket notation**: the bracket notation becomes an element ```bn```. It is at the same level that the input file formats (```vienna```, ```bpseq```,...). Instead to precise the filename, you need to provide the bracket notation. If no sequence is set, a random one is generated, fitting the base-pairing constraints. The default name for the sequence is 'A'. 
```kotlin
//before
rnartist {
  rna {
    sequence = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
  }
  bracket_notation =
    "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
}

//now
rnartist {
  bn {
    seq = "CAACAUCAUACGUACUGCGCCCAAGCGUAACGCGAACACCACGAGUGGUGACUGGUGCUUG"
    name = "my_rna"
    value =
      "(((..(((..(((..(((((....)))))..)))..(((((....)))))..)))...)))"
  }
}
```
- **2D plot saving in PNG or SVG file for the drawing algorithm ```rnartist```**: the file format is now the name of an element (```svg``` or ```png```) containing the saving path. The name of the RNA molecule exported is used for the filename. 
```kotlin
//before
rnartist {
  file = "media/real_example.svg"
}

//now
rnartist {
  svg {
    path = "media/"
  }
  
  ss {
    bn {
        value = "(((...)))"
        name = "real_example"
    }
  }
}
```



