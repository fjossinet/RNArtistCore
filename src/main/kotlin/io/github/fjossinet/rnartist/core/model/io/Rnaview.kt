package io.github.fjossinet.rnartist.core.model.io

import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileReader
import java.io.IOException

class Rnaview : Computation() {

    @Throws(Exception::class)
    fun annotate(pdb:File): List<SecondaryStructure> {
        return when {
            RnartistConfig.isDockerInstalled() && RnartistConfig.isDockerImageInstalled() -> {
                val pb = ProcessBuilder(
                    "docker",
                    "run",
                    "-v",
                    pdb.parent + ":/data",
                    "fjossinet/rnartistcore",
                    "rnaview",
                    "-p",
                    "/data/" + pdb.name
                )
                val p = pb.start()
                p.waitFor()
                val secondaryStructures = parseRnaml(File(pdb.parent, pdb.name + ".xml"))
                secondaryStructures.forEach {
                    it.source = "tool:rnaview"
                }
                File(pdb.parent, pdb.name + ".ps").delete()
                File(pdb.parent, pdb.name + ".out").delete()
                File(pdb.parent, pdb.name + ".xml").delete()
                var found = false
                val tertiaryStructures = parsePDB(FileReader(pdb))
                for (ss in secondaryStructures) {
                    for (ts in tertiaryStructures)
                        if (tertiaryStructures.indexOf(ts) + 1 == Integer.parseInt(ss.rna.name)) {
                            ss.rna.name = ts.rna.name
                            found = true
                            if (ss.rna.length != ts.rna.length) {
                                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                            }
                            break
                        }
                    if (!found)
                        ss.rna.name = "?"  //should never happen
                }
                secondaryStructures
            }
            System.getProperty("os.name") == "Windows 10" -> {
                val pb = ProcessBuilder(File(getUserDir(), "rnaview.bat").absolutePath, pdb.absolutePath)
                val p = pb.start()
                p.waitFor()
                val secondaryStructures = parseRnaml(File(pdb.parent, pdb.name + ".xml"))
                secondaryStructures.forEach {
                    it.source = "tool:rnaview"
                }
                File(pdb.parent, pdb.name + ".ps").delete()
                File(pdb.parent, pdb.name + ".out").delete()
                File(pdb.parent, pdb.name + ".xml").delete()
                var found = false
                val tertiaryStructures = parsePDB(FileReader(pdb))
                for (ss in secondaryStructures) {
                    for (ts in tertiaryStructures)
                        if (tertiaryStructures.indexOf(ts) + 1 == Integer.parseInt(ss.rna.name)) {
                            ss.rna.name = ts.rna.name
                            found = true
                            if (ss.rna.length != ts.rna.length) {
                                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                            }
                            break
                        }
                    if (!found)
                        ss.rna.name = "?" //should never happen
                }
                secondaryStructures
            }
            else -> {
                println(pdb.absolutePath)
                try {
                    val pb = ProcessBuilder(
                        "rnaview",
                        "-p",
                        pdb.absolutePath
                    )
                    val p = pb.start()
                    p.waitFor()
                    val secondaryStructures = parseRnaml(File(pdb.parent, pdb.name + ".xml"))
                    secondaryStructures.forEach {
                        it.source = "tool:rnaview"
                    }
                    File(pdb.parent, pdb.name + ".ps").delete()
                    File(pdb.parent, pdb.name + ".out").delete()
                    File(pdb.parent, pdb.name + ".xml").delete()
                    var found = false
                    val tertiaryStructures = parsePDB(FileReader(pdb))
                    for (ss in secondaryStructures) {
                        for (ts in tertiaryStructures)
                            if (tertiaryStructures.indexOf(ts) + 1 == Integer.parseInt(ss.rna.name)) {
                                ss.rna.name = ts.rna.name
                                found = true
                                if (ss.rna.length != ts.rna.length) {
                                    //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                                }
                                break
                            }
                        if (!found)
                            ss.rna.name = "?"  //should never happen
                    }
                    secondaryStructures
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                listOf<SecondaryStructure>()
            }
        }
    }

    init {
        if (System.getProperty("os.name") == "Windows 10") {
            try {
                val destFile = File(getUserDir(), "rnaview.bat")
                if (!destFile.exists()) {
                    destFile.createNewFile()
                    val inputUrl = Rnaview::class.java.getResource("/io/github/fjossinet/rnartist/core/model/io/rnaview.bat")
                    FileUtils.copyURLToFile(inputUrl, destFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}