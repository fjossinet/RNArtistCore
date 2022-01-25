package io.github.fjossinet.rnartist.core.io

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.core.model.TertiaryStructure
import io.github.fjossinet.rnartist.core.model.ToolSource
import java.io.File
import java.io.FileReader

class Rnaview : Computation() {

    @Throws(Exception::class)
    fun annotate(pdb:File): List<Pair<TertiaryStructure,SecondaryStructure>> {
        var annotatedStructures = mutableListOf<Pair<TertiaryStructure,SecondaryStructure>>()
        if (RnartistConfig.isDockerInstalled() && RnartistConfig.isDockerImageInstalled()) {
            val pb = ProcessBuilder(
                "docker",
                "run",
                "-v",
                pdb.parent + ":/project",
                "fjossinet/rnartistcore",
                "rnaview",
                "-p",
                "/project/" + pdb.name
            )
            val p = pb.start()
            p.waitFor()
            val secondaryStructures = parseRnaml(File("${pdb.absolutePath}.xml"))
            secondaryStructures.forEach {
                it.source = RnaviewSource()
            }
            File("${pdb.absolutePath}.ps").delete()
            File("${pdb.absolutePath}.out").delete()
            File("${pdb.absolutePath}.xml").delete()
            var found = false
            val tertiaryStructures = parsePDB(FileReader(pdb))
            for (ss in secondaryStructures) {
                for (ts in tertiaryStructures)
                    if (tertiaryStructures.indexOf(ts) + 1 == Integer.parseInt(ss.rna.name)) {
                        ss.rna.name = ts.rna.name
                        ss.name = ts.rna.name
                        val ns = mutableMapOf<Int,String>()
                        ts.getNumberingSystem().forEachIndexed { index, label ->
                            ns[index] =  label
                        }
                        ss.rna.tertiary_structure_numbering_system = ns
                        found = true
                        annotatedStructures.add(Pair(ts,ss))
                        if (ss.rna.length != ts.rna.length) {
                            //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                        }
                        break
                    }
                if (!found)
                    ss.rna.name = "?"  //should never happen
            }
        }
        else {
            try {
                val pb = ProcessBuilder(
                    "rnaview",
                    "-p",
                    pdb.absolutePath
                )
                val p = pb.start()
                p.waitFor()
                val secondaryStructures = parseRnaml(File("${pdb.absolutePath}.xml"))
                secondaryStructures.forEach {
                    it.source = RnaviewSource()
                }
                File("${pdb.absolutePath}.ps").delete()
                File("${pdb.absolutePath}.out").delete()
                File("${pdb.absolutePath}.xml").delete()
                var found = false
                val tertiaryStructures = parsePDB(FileReader(pdb))
                for (ss in secondaryStructures) {
                    for (ts in tertiaryStructures)
                        if (tertiaryStructures.indexOf(ts) + 1 == Integer.parseInt(ss.rna.name)) {
                            ss.rna.name = ts.rna.name
                            ss.name = ts.rna.name
                            val ns = mutableMapOf<Int,String>()
                            ts.getNumberingSystem().forEachIndexed { index, label ->
                                ns[index] =  label
                            }
                            ss.rna.tertiary_structure_numbering_system = ns
                            found = true
                            annotatedStructures.add(Pair(ts,ss))
                            if (ss.rna.length != ts.rna.length) {
                                //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
                            }
                            break
                        }
                    if (!found)
                        ss.rna.name = "?"  //should never happen
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return annotatedStructures
    }

}

class RnaviewSource:ToolSource("no version available") {
    override fun toString(): String {
        return "computed with Rnaview"
    }
}