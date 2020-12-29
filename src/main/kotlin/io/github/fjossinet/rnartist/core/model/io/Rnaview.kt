package io.github.fjossinet.rnartist.core.model.io

import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.core.model.TertiaryStructure
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.PrintWriter

class Rnaview : Computation() {

    @Throws(Exception::class)
    fun annotate(ts: TertiaryStructure): List<SecondaryStructure> {
        return when {
            this.isDockerInstalled() -> {
                val temp = createTemporaryFile("rnaview")
                val writer = PrintWriter(temp)
                writePDB(ts, true, writer)
                val pb = ProcessBuilder("docker", "run", "-v", temp!!.parent + ":/data", "fjossinet/assemble2", "rnaview", "-p", "/data/" + temp.name)
                val p = pb.start()
                p.waitFor()
                val structures = parseRnaml(File(temp.parent, temp.name + ".xml"))
                structures.forEach {
                    it.rna.name = ts.rna.name
                }
                structures
                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
            }
            System.getProperty("os.name") == "Windows 10" -> {
                val temp = createTemporaryFile("rnaview")
                val writer = PrintWriter(temp)
                writePDB(ts, true, writer)
                val pb = ProcessBuilder(File(getUserDir(), "rnaview.bat").absolutePath, temp!!.absolutePath)
                val p = pb.start()
                p.waitFor()
                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                val structures = parseRnaml(File(temp.parent, temp.name + ".xml"))
                structures.forEach {
                    it.rna.name = ts.rna.name
                }
                structures
            }
            else -> listOf<SecondaryStructure>()
        }
    }

    @Throws(Exception::class)
    fun annotate(pdb:File): List<SecondaryStructure> {
        return when {
            this.isDockerInstalled() -> {
                val pb = ProcessBuilder("docker", "run", "-v", pdb.parent + ":/data", "fjossinet/assemble2", "rnaview", "-p", "/data/" + pdb.name)
                val p = pb.start()
                p.waitFor()
                val ss = parseRnaml(File(pdb.parent, pdb.name + ".xml"))
                File(pdb.parent, pdb.name + ".ps").delete()
                File(pdb.parent, pdb.name + ".out").delete()
                File(pdb.parent, pdb.name + ".xml").delete()
                ss
                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
            }
            System.getProperty("os.name") == "Windows 10" -> {
                val pb = ProcessBuilder(File(getUserDir(), "rnaview.bat").absolutePath, pdb.absolutePath)
                val p = pb.start()
                p.waitFor()
                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                val ss = parseRnaml(File(pdb.parent, pdb.name + ".xml"))
                File(pdb.parent, pdb.name + ".ps").delete()
                File(pdb.parent, pdb.name + ".out").delete()
                File(pdb.parent, pdb.name + ".xml").delete()
                ss
            }
            else -> listOf<SecondaryStructure>()
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