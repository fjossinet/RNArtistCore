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

Usage with a prior RNArtistCore script:
======================================

    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar /path/to/your/script
    
Usages without any prior RNArtistCore script:
============================================

* to process a single local structural file: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [options] -f /path/to/your/structural_file
    
* to process several local structural files:  
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [options] -d /path/to/the/root_folder/
    
* to process a database entry: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [options] -e database_entry_id -o output_directory
    
The RNArtistCore script created stores the isntructions for a theme with default parameters and for a non-overlapping layout. 
This script can now be modified and re-run as a prior RNArtistCore script.

Primary options to define the location of the structural data:
-------------------------------------------------------------
-f <arg>                Compute the 2D plot for a single structural file whose path is given as argument.
                        An RNArtistCore script with default parameters will be created in the same folder as the structural file.

-d <arg>                Compute the 2D plots from structural files stored in subfolders inside 
                        the root folder given as argument. If some files have already been processed, they will be ignored.
                        At the end of the process, the root folder can be loaded with the graphical tool RNArtist to explore and manipulate the 2D plots.

-e <arg> -o <arg>       Compute the 2D plot for a database entry (PDB, RNACentral and Rfam supported). 
                        The argument for option -e has to be a valid ID for the database (like 1EHZ for PDB, RF00177 for Rfam or URS00000CFF65 for RNACentral).
                        An RNArtistCore script with default parameters will be created in the folder defined with the mandatory option -o.

Secondary options to change default parameters in the script:
------------------------------------------------------------
--no-png                The RNArtistCore script will not export its 2D plot(s) in PNG files. This option should not be used to 
                        create a database fully compliant with the graphical tool RNArtist. RNArtist needs PNG files to preview the 2Ds.

--with-svg              The RNArtistCore script will export its 2D plot(s) in SVG files

--min-color <arg>       Define the first color for the gradient color. The gradient color is used to 
                        incorporate quantitative values into 2D plots (default: lightyellow)

--max-color <arg>       Define the last color for the gradient color. The gradient color is used to 
                        incorporate quantitative values into 2D plots (default: firebrick)

--min-value [<arg>]     Define the min value to be used to compute the gradient color between 
                        min-color and max-color (default: 0.0)

--max-value [<arg>]     Define the max value to be used to compute the gradient color between 
                        min-color and max-color (default: 1.0)
                        
Secondary options to process several local structural files:
-----------------------------------------------------------

--from <arg>            If you're batch processing several structural files, this option allow to restart the process from the file whose name without suffix 
                        is given as argument (if file named my_rna_67.vienna, then you need to type --from my_rna_67).
                        If some files have already been processed after this start file, they will be recomputed.
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
                        println("You have to combine option -e with option -o to define the path where all the files will be stored")
                        System.exit(-1)
                    }
                } else {
                     println("The files will be stored in the same folder as your structural file.")
                     dataFile = File(args.get(args.indexOf("-f") + 1))
                 }

                dataFile?.let { dataFile->

                    val scriptContent = getScriptContentForDataFile(
                        dataFile,
                        dataFile.parentFile,
                        args.contains("--no-png"),
                        args.contains("--with-svg"),
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
                            ".pdb"
                        )
                    }).forEach { dataFile ->
                        if (!foundStart)
                            foundStart = dataFile.name.startsWith(args.get(args.indexOf("--from") + 1))
                        if (foundStart) {
                            var scriptDataFile = File(dataFile.parentFile, "${dataFile.name.split(Regex(".(vienna|bpseq|ct|pdb|stk|sto|stockholm)")).first()}.kts")

                            //we restart from here and if a script exists we delete it to be sure to have someting clean

                            if (scriptDataFile.exists())
                                scriptDataFile.delete()

                            scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                args.contains("--no-png"),
                                args.contains("--with-svg"),
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
                        var scriptDataFile = db.getScriptForDataFile(
                            dataFile,
                            args.contains("--no-png"),
                            args.contains("--with-svg"),
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
                                args.contains("--no-png"),
                                args.contains("--with-svg"),
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

