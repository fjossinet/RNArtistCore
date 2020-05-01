package fr.unistra.rnartist.model.io

import fr.unistra.rnartist.model.SecondaryStructure
import fr.unistra.rnartist.model.TertiaryStructure
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.PrintWriter

class Rnaview : Computation() {

    @Throws(Exception::class)
    fun annotate(ts: TertiaryStructure): SecondaryStructure? {
        return if (this.isDockerInstalled()) {
            val temp = createTemporaryFile("rnaview")
            val writer = PrintWriter(temp)
            writePDB(ts, true, writer)
            val pb = ProcessBuilder("docker", "run", "-v", temp!!.parent + ":/data", "fjossinet/assemble2", "rnaview", "-p", "/data/" + temp.name)
            val p = pb.start()
            p.waitFor()
            val ss = parseRnaml(File(temp.parent, temp.name + ".xml"))
            println(temp.absolutePath)
            println(ss!!.rna.seq)
            println(ts.rna.seq)
            //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
            ss
        } else if (System.getProperty("os.name") == "Windows 10") {
            val temp = createTemporaryFile("rnaview")
            val writer = PrintWriter(temp)
            writePDB(ts, true, writer)
            val pb = ProcessBuilder(File(getUserDir(), "rnaview.bat").absolutePath, temp!!.absolutePath)
            val p = pb.start()
            p.waitFor()
            //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
            parseRnaml(File(temp.parent, temp.name + ".xml"))
        } else {
            null
        }
    }

    init {
        if (System.getProperty("os.name") == "Windows 10") {
            try {
                val destFile = File(getUserDir(), "rnaview.bat")
                if (!destFile.exists()) {
                    destFile.createNewFile()
                    val inputUrl = Rnaview::class.java.getResource("/fr/unistra/ibmc/rnartist/model/io/rnaview.bat")
                    FileUtils.copyURLToFile(inputUrl, destFile)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}