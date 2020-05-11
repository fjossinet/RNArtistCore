RNArtistCore
============

RNArtistCore is a commandline tool and a Kotlin library to describe and plot RNA secondary structures. As a library it is used in the projects RNArtist and RNArtistBackend.

# The command-line tool

You need to have the build tool [Maven](https://maven.apache.org) installed. Clone this repository and inside its root directory type:

<pre>mvn package</pre>

Once done, in the subdirectory named "target", you will find the file rnartistcore-{version}-jar-with-dependencies.jar. In a terminal type:

<pre>java -jar rnartistcore-{version}-jar-with-dependencies.jar</pre>

You can rename this file if you want.

## Usage: 

<pre>java -jar RNArtistCore-{version}-jar-with-dependencies.jar [options]  [-f file_name] [-id database_id]</pre>

## Description:
RNArtistCore is a Java/Kotlin library and a commandline tool. As a tool, it exports an RNA secondary structure
in an SVG file. The secondary structure is computed from data stored in a local file or recovered from databases
like Rfam using an ID.

The SVG plot can be configured through several options (lines width, font name,...). Using the option -s, these
user-defined values can be saved in a configuration file and become the default values for the next runs.

## Mandatory Options:

* -f file_name

    Either this option or -id is mandatory. The local file needs to describe an RNA secondary structure (BPSEQ, 
    VIENNA, CT and STOCKHOLM formats). Several file names are allowed. If the option -o is not used, the SVG files 
    are stored in the working directory.

* -id database_entry_id

    Either this option or -f is mandatory.  If the option -o is not used, the SVG files are stored in the working 
    directory. The database_entry_id has to conform to:
    
    * RFXXXXX: an entry from the RFAM database (https://rfam.xfam.org/). A 2D structure is derived from the consensus
             one for each RNA member of the family and exported in the ouput directory as an SVG file

## Other Options:

* --browser-fix
  --no-browser-fix

    If you display your SVG files in a browser and observe some issues concerning the centering of residue characters,
    try to add this option. If this doesn't fix the problem, you can improve the centering by yourself with the options
    "dxr" and "dyr". Use "--no-browser-fix -s" if you ahve saved the option --browser-fix.

* -cA "HTML_color_code"<br/>
  -cU "HTML_color_code"<br/>
  -cG "HTML_color_code"<br/>
  -cC "HTML_color_code"<br/>
  
    These options define the color to use for each residue. The HTML code can be defined like "#99ccff", \#99ccff or
    99ccff. You can find a list of HTML color codes here: https://www.w3schools.com/colors/colors_picker.asp

* -c2d "HTML_color_code"<br/>
  -c3d "HTML_color_code"
  
    These options define the color to use for the secondary (-c2d) or the tertiary (-c3d) interactions. The HTML code
    can be defined like "#99ccff", \#99ccff or 99ccff. You can find a list of HTML color codes here:
    https://www.w3schools.com/colors/colors_picker.asp

* -dxr number<br/>
  --deltaXRes=number<br/>
  -dyr number<br/>
  --deltaYRes=number<br/>
  
    These options translate the residue characters along the X- or Y-axis (for example, to move it by 5 pixels along
    the X-axis, type "-dxr 5"). The X- and Y-axis are in the "classical" orientations (0,0 is the bottom left corner).
    To push the characters on the left (X-axis) or to the bottom (Y-Axis), you need to use the two hypens syntax (like
    "--deltaXRes=-5"). If you saved a user-defined value for one of these options, you can erase it by doing "-dxr 0 -s". 
    The number for these options has to be a positive or negative integer.

* -df number<br/>
  --deltaFontSize=number
  
    Modifies the residue character size. To decrease it by 5 in size, you need to use the two hypens syntax (like 
    "--deltaFontSize=-5"). If you saved a user-defined value for this option, you can erase it by doing "-df 0 -s". 
    The number for this option has to be a positive or negative integer.

* --font=font_name

    The name of the font to use. Check the fonts available for your system to make a choice.

* -h<br/>
  --help
  
    Print this help message.

* -hw number<br/>
  --halo-width=number
  
    [NOT IMPLEMENTED, TO COME] Define the size of the halo around residues making tertiary interactions. The number
    has to be an integer greater of equal to 0.

* -o dir_name

    The directory to output the SVG files. The directory has to exist. If - is used as dir_name, SVG data will
    be printed to standard output.

* -o3d number<br/>
  --opacity-3d=number
  
    [NOT IMPLEMENTED, TO COME] Define the % of opacity of the halo around residues making tertiary interactions. The
    number has to be an integer between 0 and 100.

* -p<br/>
  --print
  
    Print the current user-defined options to plot the 2D structures.

* -rb number<br/>
  --residueBorder=number
  
    Change the width for the border of the residues circles. The number has to be an integer greater of equal to 0.

* -s<br/>
  --save
  
    Save the options defined as default ones. Use option -p to print current default options.

* -s3d style<br/>
  --style-3d=style
  
    [NOT IMPLEMENTED, TO COME] Define the line style for the tertiary interactions. The value can be dashed or solid.

* -t theme_id<br/>
  --theme=theme_id
  
    [NOT IMPLEMENTED, TO COME] Use a theme shared by the community. Take a look at this page to see all the themes
     shared : http://to_come

* -w2d number<br/>
  --width-2d=number<br/>
  -w3d number<br/>
  --width-3d=number<br/>
  
These options define the width for the secondary (-w2d) or the tertiary (-w3d) interactions lines. The number has to
be an integer greater of equal to 0.

## Examples:
<pre>
    java -jar rnartistcore.jar -f ~/data/* -o ~/svg_files --font="Andale Mono" --browser-fix
    java -jar rnartistcore.jar -f ~/data/* -o ~/svg_files --font="DIN Condensed" -rb 2 -dxr 0 --deltaYRes=-5
    java -jar rnartistcore.jar -f ~/data/rna.bpseq -o ~/svg_files --font="Arial" -rb 0 --deltaFontSize=-10
    java -jar rnartistcore.jar -f ~/data/rna.bpseq -o ~/svg_files --font="Herculanum" -cU "#ffcc66" -s
    java -jar rnartistcore.jar -f ~/data/rna.bpseq -o ~/svg_files -w2d 10 -w3d 1 -s
    java -jar rnartistcore.jar -f ~/data/*.ct -o ~/svg_files -w2d 5 --font="Futura" --deltaFontSize=-2 -cA 0066ff -cG "#ff9900" -cU 009933 -cC \#cc00cc
</pre>

# The library

You need to have the build tool [Maven](https://maven.apache.org) installed. To use RNArtistCore in any Java application, just add the below dependency in your file pom.xml:

```xml
<dependency>
    <groupId>io.github.fjossinet.rnartist.core</groupId>
    <artifactId>RNArtistCore</artifactId>
    <version>{version}</version>
</dependency>
```
## Get a secondary structure
### from a file
```kotlin
val ss = parseBPSeq(FileReader("my_file.bpseq"))
```
### from scratch
```kotlin
val ss1 = parseVienna(StringReader(">myRNA\nCGCUGAAUUCAGCG\n((((......))))"))
//or
val ss2 = SecondaryStructure(RNA(name="myRNA",seq = "CGCUGAAUUCAGCG"), bracketNotation = "((((......))))")
```

## Get a plot
```kotlin
val theme = Theme()
//we tweak the default theme
theme.fontName = "Futura"
theme.secondaryInteractionWidth = 4
theme.residueBorder = 1
theme.GColor = Color(223, 1, 1)
var drawing = SecondaryStructureDrawing(secondaryStructure = ss2, theme = theme)

var writer = FileWriter("media/myRNA.svg")
writer.write(drawing.asSVG())
writer.close()
```
And you get:

<img src="https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/media/myRNA.svg" width="144">

And now something larger
```kotlin

val seq = "AUACUUACCUGGCAGGGGAGAUACCAUGAUCACGAAGGUGGUUUUCCCAGGGCGAGGCUUAUCCAUUGCACUCCGGAUGUGCUGACCCCUGCGAUUUCCCCAAAUGUGGGAAACUCGACUGCAUAAUUUGUGGUAGUGGGGGACUGCGUUCGCGCUUUCCCCUG"
val bn = "...........((((((((((.(((((..........))))))))))).(((......(((.((..........)).)).).....)))..(((.((.((.......))))...))).))))...............(((((.(((((....))))).)))))."

val ss = SecondaryStructure(RNA(name="myRNA2",seq = seq), bracketNotation = bn)
val theme = Theme()
//we tweak the default theme
theme.fontName = "Futura"
theme.secondaryInteractionWidth = 4
theme.residueBorder = 1
theme.GColor = Color(223, 1, 1)
val drawing = SecondaryStructureDrawing(secondaryStructure = ss, theme = theme)

val writer = FileWriter("media/myRNA2.svg")
writer.write(drawing.asSVG())
writer.close()
```
And you get:

<img src="https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/media/myRNA2.svg" width="1376">

Now you can pursue with vector graphics editor like Affinity Designer or Inkscape.