package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.getScriptContentForDataFile
import io.github.fjossinet.rnartist.core.model.*
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import kotlin.io.path.invariantSeparatorsPathString

val usage = """
#####################################################################
RNArtistCore: a commandline tool to create and plot RNA 2D structures
#####################################################################

Usage #1: with a prior RNArtistCore script
==========================================

    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar /path/to/your/script
    
Usage #2: without any prior RNArtistCore script
===============================================

Depending on the mandatory option chosen (-f, -d or -e), you can:

* plot a single local structural file: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [--non-mandatory-options] -f /path/to/your/structural_file
    
* plot several local structural files:  
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [--non-mandatory-options] -d /path/to/the/root_folder/
    
* plot a database entry: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [--non-mandatory-options] -e database_entry_id -o output_directory
    
In each case, an RNArtistCore script will be created for each structural file found. Each script stores the instructions to plot the 2D.
Non mandatory options can be used to change the default parameters stored in the scripts (see below). 
The scripts can be modified with your favorite text editor and re-run from the commandline (see usage #1).

Mandatory options to define the location of the structural data
---------------------------------------------------------------
-f <arg>                Compute the 2D plot for a single structural file whose path is given as argument.
                        An RNArtistCore script with default parameters will be created in the same folder as the structural file.

-d <arg>                Compute the 2D plots from structural files stored in subfolders inside 
                        the root folder given as argument. If some files have already been processed, they will be ignored.
                        At the end of the process, the root folder can be loaded with the graphical tool RNArtist to explore and manipulate the 2D plots.

-e <arg> -o <arg>       Compute the 2D plot for a database entry (PDB, RNACentral and Rfam supported). 
                        The argument for option -e has to be a valid ID for the database selected (like 1EHZ for PDB, RF00177 for Rfam or URS00000CFF65 for RNACentral).
                        An RNArtistCore script will be created in the folder defined with the mandatory option -o.

Non mandatory options to change the default parameters in the script
--------------------------------------------------------------------
--no-png                The RNArtistCore script will not export its 2D plot(s) in PNG files. This option should not be used to 
                        create a database fully compliant with the graphical tool RNArtist. RNArtist needs PNG files to preview the 2Ds.
                        
--png-width             The width for the PNG files. The default value is the width of the drawing.

--png-height            The height for the PNG files. The default value is the height of the drawing.

--with-svg              The RNArtistCore script will export its 2D plot(s) in SVG files

--svg-width             The width for the SVG files. The default value is the width of the drawing.

--svg-height            The height for the SVG files. The default value is the height of the drawing.

--with-data <arg>       The RNArtistCore script will link the 2D to the quantitative values described in the file given as argument. 
                        Min/max values and min/max colors can be defined with the options below.

--min-color <arg>       Define the first color for the gradient color. The gradient color is used to 
                        display quantitative values in 2D plots (default: lightyellow)

--max-color <arg>       Define the last color for the gradient color. The gradient color is used to 
                        display quantitative values in 2D plots (default: firebrick)

--min-value <arg>       Define the min value to be used to compute the gradient color between 
                        min-color and max-color (default: 0.0)

--max-value <arg>       Define the max value to be used to compute the gradient color between 
                        min-color and max-color (default: 1.0)
                        
Non mandatory options to plot several local structural files
------------------------------------------------------------

--from <arg>            If you're batch processing several structural files, this option allows to restart the process from the file given as argument, without its suffix (if the file is named my_rna_67.vienna, then you need to type --from my_rna_67).
                        If some files have already been processed after this file, they will be recomputed.
""".trimIndent()

class Jar() {
    fun path() = Paths.get(
        Jar::class.java.getProtectionDomain().getCodeSource().getLocation()
            .toURI()
    ).parent.invariantSeparatorsPathString
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(usage)
        System.exit(-1)
    }
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")
    val options = args.filter { it.startsWith("-") }
    if (options.isEmpty()) {
        println("Run RNArtistCore script ${args.last()}")
        val text = FileReader(File(args.last())).readText()
        println("Script loaded...")
        engine.eval(
            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                System.getProperty(
                    "line.separator"
                )
            } ${text}"
        )
    }
    else
        if (options.contains("-h"))
            println(usage)
        else if (options.contains("-e") || options.contains("-f")) {
            if (options.contains("-f") && args.get(args.indexOf("-f")+1).endsWith(".kts")) {
                println("You cannot use the option -f to run an RNArtistCore script. Please retry!")
            } else {
                var dataFile:File? = null
                 if (options.contains("-e")) {
                    if (options.contains("-o")) {
                        val outputDir = File(args.get(args.indexOf("-o") + 1).trim().trim())
                        val dbEntryId = args.get(args.indexOf("-e") + 1).trim()
                        if (dbEntryId.startsWith("RF")) {
                            Rfam().getEntry(dbEntryId)?.let {
                                dataFile = File(outputDir, "${dbEntryId}.sto")
                                dataFile!!.writeText(it.readText())
                            } ?: run {
                                println("Cannot get data for Rfam entry $dbEntryId")
                            }
                        } else if (dbEntryId.startsWith("URS")) {
                            RNACentral().getEntry(dbEntryId)?.let {
                                dataFile = File(outputDir, "${dbEntryId}.vienna")
                                dataFile!!.writeText(it.readText())
                            } ?: run {
                                println("Cannot get data for RNACentral entry $dbEntryId")
                            }
                        } else if (dbEntryId.length == 4) {
                            PDB().getEntry(dbEntryId).let {
                                dataFile = File(outputDir, "${dbEntryId}.pdb")
                                dataFile!!.writeText(it.readText())
                            }
                        } else {
                            println("Unknown entry ID! PDB, RNACentral and Rfam IDs are supported. Examples of valid IDs: 1EHZ for PDB, RF00177 for Rfam or URS00000CFF65 for RNACentral.")
                        }
                    } else {
                        println("You have to combine option -e with option -o to define the location of the RNArtistCore script to be created")
                        System.exit(-1)
                    }
                } else {
                     println("The files will be stored in the same folder as your structural file.")
                     dataFile = File(args.get(args.indexOf("-f") + 1))
                 }

                dataFile?.let { dataFile->

                    var quantitativeDataFile:File? = File(dataFile.parentFile, "${dataFile.name.split(Regex(".(vienna|bpseq|ct|pdb|stk|sto|stockholm)")).first()}.txt")
                    if (quantitativeDataFile!!.exists() && args.contains("--with-data"))
                        quantitativeDataFile = File(args.get(args.indexOf("--with-data") + 1))
                    else
                        quantitativeDataFile = null

                    val scriptContent = getScriptContentForDataFile(
                        dataFile,
                        dataFile.parentFile,
                        quantitativeDataFile,
                        args.contains("--no-png"),
                        if (args.contains("--png-width"))
                            args.get(args.indexOf("--png-width") + 1).toDouble()
                        else
                            null,
                        if (args.contains("--png-height"))
                            args.get(args.indexOf("--png-height") + 1).toDouble()
                        else
                            null,
                        args.contains("--with-svg"),
                        if (args.contains("--svg-width"))
                            args.get(args.indexOf("--svg-width") + 1).toDouble()
                        else
                            null,
                        if (args.contains("--svg-height"))
                            args.get(args.indexOf("--svg-height") + 1).toDouble()
                        else
                            null,
                        if (args.contains("--min-color"))
                            args.get(args.indexOf("--min-color") + 1)
                        else
                            "lightyellow",
                        if (args.contains("--min-value"))
                            args.get(args.indexOf("--min-value") + 1).toDouble()
                        else
                            0.0,
                        if (args.contains("--max-color"))
                            args.get(args.indexOf("--max-color") + 1)
                        else
                            "firebrick",
                        if (args.contains("--max-value"))
                            args.get(args.indexOf("--max-value") + 1).toDouble()
                        else
                            1.0
                    )

                    println("Drawing 2D for ${dataFile.name}")
                    engine.eval(
                        "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                            System.getProperty(
                                "line.separator"
                            )
                        } ${scriptContent}"
                    )
                }
            }
        }
        else if (options.contains("-d")) {
            if (args.contains("--from")) {
                val db = RNArtistDB(args.get(args.indexOf("-d")+1))
                var foundStart = false
                db.indexedDirs.forEach { it ->
                    println("Processing folder $it")
                    val dataDir = File(it)
                    dataDir.listFiles(FileFilter {
                        it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct") || it.name.endsWith(
                            ".pdb") || it.name.endsWith(".sto")
                    }).forEach { dataFile ->
                        if (!foundStart)
                            foundStart = dataFile.name.startsWith(args.get(args.indexOf("--from") + 1))
                        if (foundStart) {
                            var scriptDataFile = File(dataFile.parentFile, "${dataFile.name.split(Regex(".(vienna|bpseq|ct|pdb|stk|sto|stockholm)")).first()}.kts")

                            //we restart from here and if a script exists we delete it to be sure to have someting clean

                            if (scriptDataFile.exists())
                                scriptDataFile.delete()

                            var quantitativeDataFile:File? = File(dataFile.parentFile, "${dataFile.name.split(Regex(".(vienna|bpseq|ct|pdb|stk|sto|stockholm)")).first()}.txt")
                            if (!quantitativeDataFile!!.exists() && args.contains("--with-data"))
                                quantitativeDataFile = File(args.get(args.indexOf("--with-data") + 1))
                            else if (!quantitativeDataFile!!.exists())
                                quantitativeDataFile = null

                            scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                quantitativeDataFile,
                                args.contains("--no-png"),
                                if (args.contains("--png-width"))
                                    args.get(args.indexOf("--png-width") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--png-height"))
                                    args.get(args.indexOf("--png-height") + 1).toDouble()
                                else
                                    null,
                                args.contains("--with-svg"),
                                if (args.contains("--svg-width"))
                                    args.get(args.indexOf("--svg-width") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--svg-height"))
                                    args.get(args.indexOf("--svg-height") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--min-color"))
                                    args.get(args.indexOf("--min-color")+1)
                                else
                                    "lightyellow",
                                if (args.contains("--min-value"))
                                    args.get(args.indexOf("--min-value")+1).toDouble()
                                else
                                    0.0,
                                if (args.contains("--max-color"))
                                    args.get(args.indexOf("--max-color")+1)
                                else
                                    "firebrick",
                                if (args.contains("--max-value"))
                                    args.get(args.indexOf("--max-value")+1).toDouble()
                                else
                                    1.0
                            )
                            println("Drawing 2D for ${scriptDataFile.name.split(".kts").first()}")
                            (engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${FileReader(scriptDataFile).readText()}"
                            ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                                //we overwrite the initial script without any layout with the best layout found and stored in the DSL elements tree
                                scriptDataFile.writeText(it.second.dump("", StringBuffer()).toString())
                            }
                        }
                    }
                }
            } else { //we create the DB
                val db = RNArtistDB(args.get(args.indexOf("-d")+1))
                //the database is indexed and the scripts for the data files are created now to be able to print the progress of the database construction
                //otherwise all the 2Ds for each data dir will be generated at once during the evaluation of the script for the data dir
                db.indexDatabase(true, true, args.contains("--no-png"),  args.contains("--with-svg"))
                db.indexedDirs.forEach { it ->
                    println("Processing folder $it")
                    val dataDir = File(it)
                    dataDir.listFiles(FileFilter {
                        it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct") || it.name.endsWith(
                            ".pdb"
                        )
                    }).forEach { dataFile ->

                        var quantitativeDataFile:File? = File(dataFile.parentFile, "${dataFile.name.split(Regex(".(vienna|bpseq|ct|pdb|stk|sto|stockholm)")).first()}.txt")
                        if (!quantitativeDataFile!!.exists() && args.contains("--with-data"))
                            quantitativeDataFile = File(args.get(args.indexOf("--with-data") + 1))
                        else if (!quantitativeDataFile!!.exists())
                            quantitativeDataFile = null

                        var scriptDataFile = db.getScriptForDataFile(
                            dataFile,
                            quantitativeDataFile,
                            args.contains("--no-png"),
                            if (args.contains("--png-width"))
                                args.get(args.indexOf("--png-width") + 1).toDouble()
                            else
                                null,
                            if (args.contains("--png-height"))
                                args.get(args.indexOf("--png-height") + 1).toDouble()
                            else
                                null,
                            args.contains("--with-svg"),
                            if (args.contains("--svg-width"))
                                args.get(args.indexOf("--svg-width") + 1).toDouble()
                            else
                                null,
                            if (args.contains("--svg-height"))
                                args.get(args.indexOf("--svg-height") + 1).toDouble()
                            else
                                null,
                            if (args.contains("--min-color"))
                                args.get(args.indexOf("--min-color")+1)
                            else
                                "lightyellow",
                            if (args.contains("--min-value"))
                                args.get(args.indexOf("--min-value")+1).toDouble()
                            else
                                0.0,
                            if (args.contains("--max-color"))
                                args.get(args.indexOf("--max-color")+1)
                            else
                                "firebrick",
                            if (args.contains("--max-value"))
                                args.get(args.indexOf("--max-value")+1).toDouble()
                            else
                                1.0
                        )
                        if (!args.contains("--no-png") && !db.getPreviewForDataFile(scriptDataFile).exists() || args.contains("--with-svg") && !db.getSVGForDataFile(scriptDataFile).exists()) {
                            //the user asked for something that doesn't exist for the current structural file. We need to run the script
                            if (scriptDataFile.exists())//if a script already exists, we delete it to be sure to have someting clean (we could have a script without the SVG export and the user asked now for one)
                                scriptDataFile.delete()

                            scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                quantitativeDataFile,
                                args.contains("--no-png"),
                                if (args.contains("--png-width"))
                                    args.get(args.indexOf("--png-width") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--png-height"))
                                    args.get(args.indexOf("--png-height") + 1).toDouble()
                                else
                                    null,
                                args.contains("--with-svg"),
                                if (args.contains("--svg-width"))
                                    args.get(args.indexOf("--svg-width") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--svg-height"))
                                    args.get(args.indexOf("--svg-height") + 1).toDouble()
                                else
                                    null,
                                if (args.contains("--min-color"))
                                    args.get(args.indexOf("--min-color")+1)
                                else
                                    "lightyellow",
                                if (args.contains("--min-value"))
                                    args.get(args.indexOf("--min-value")+1).toDouble()
                                else
                                    0.0,
                                if (args.contains("--max-color"))
                                    args.get(args.indexOf("--max-color")+1)
                                else
                                    "firebrick",
                                if (args.contains("--max-value"))
                                    args.get(args.indexOf("--max-value")+1).toDouble()
                                else
                                    1.0
                            )
                            println("Drawing 2D for ${scriptDataFile.name.split(".kts").first()}")
                            (engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${FileReader(scriptDataFile).readText()}"
                            ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                                //we overwrite the initial script without any layout with the best layout found and stored in the DSL elements tree
                                scriptDataFile.writeText(it.second.dump("", StringBuffer()).toString())
                            }
                        }
                    }
                }
            }

        }
}

