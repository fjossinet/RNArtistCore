package io.github.fjossinet.rnartist.core.io

import io.github.fjossinet.rnartist.core.model.*
import java.io.File
import java.io.FileReader
import kotlin.math.pow
import kotlin.math.sqrt

class Annotate3D {

    @Throws(Exception::class)
    fun annotate(pdb: File): List<SecondaryStructure> {
        val annotatedStructures = mutableListOf<SecondaryStructure>()
        val tertiaryStructures = parsePDB(FileReader(pdb))
        tertiaryStructures.forEach { ts ->
            val basePairs = mutableListOf<BasePair>()
            for (r in ts.residues) {
                when (r) {
                    is Adenine3D -> {
                        ts.residues.filter { it is Uracil3D }.forEach {
                            val dist1 = distance(r.getAtom("N6")?.coordinates!!, it.getAtom("O4")?.coordinates!!)
                            val dist2 = distance(r.getAtom("N1")?.coordinates!!, it.getAtom("N3")?.coordinates!!)
                            val dist3 = distance(r.getAtom("C1'")?.coordinates!!, it.getAtom("C1'")?.coordinates!!)
                            val mean = (dist1+dist2)/2.0
                            if (mean in 2.5..3.5 && dist3 in 8.0..12.0) {
                                basePairs.add(BasePair(Location(Location(r.absolutePosition), Location(it.absolutePosition))))
                            }
                        }
                    }
                    is Uracil3D -> {
                        ts.residues.filter {it is Guanine3D }.forEach {
                            val dist1 = distance(r.getAtom("N3")?.coordinates!!, it.getAtom("O6")?.coordinates!!)
                            val dist2 = distance(r.getAtom("O2")?.coordinates!!, it.getAtom("N1")?.coordinates!!)
                            val dist3 = distance(r.getAtom("C1'")?.coordinates!!, it.getAtom("C1'")?.coordinates!!)
                            val mean = (dist1+dist2)/2.0
                            if (mean in 2.5..3.5 && dist3 in 8.0..12.0) {
                                basePairs.add(BasePair(Location(Location(r.absolutePosition), Location(it.absolutePosition))))
                            }
                        }
                    }
                    is Cytosine3D -> {
                        ts.residues.filter {it is Guanine3D }.forEach {
                            val dist1 = distance(r.getAtom("N4")?.coordinates!!, it.getAtom("O6")?.coordinates!!)
                            val dist2 = distance(r.getAtom("N3")?.coordinates!!, it.getAtom("N1")?.coordinates!!)
                            val dist3 = distance(r.getAtom("O2")?.coordinates!!, it.getAtom("N2")?.coordinates!!)
                            val dist4 = distance(r.getAtom("C1'")?.coordinates!!, it.getAtom("C1'")?.coordinates!!)
                            val mean = (dist1+dist2+dist3)/3.0
                            if (mean in 2.5..3.5 && dist4 in 8.0..12.0) {
                                basePairs.add(BasePair(Location(Location(r.absolutePosition), Location(it.absolutePosition))))
                            }
                        }
                    }
                }
            }
            basePairs.sortBy { it.start }
            val ss = SecondaryStructure(RNA(ts.rna.name, StringBuffer(ts.rna.seq).toString()), basePairs = basePairs)
            ss.name = ts.rna.name
            val ns = mutableMapOf<Int,String>()
            ts.getNumberingSystem().forEachIndexed { index, label ->
                ns[index] =  label
            }
            ss.rna.tertiary_structure_numbering_system = ns
            ss.tertiaryStructure = ts
            annotatedStructures.add(ss)
        }
        return annotatedStructures
    }

    fun distance(p1:FloatArray, p2:FloatArray) = sqrt((p1[0].toDouble() - p2[0].toDouble()).pow(2.0) + (p1[1].toDouble() - p2[1].toDouble()).pow(2.0) + (p1[2].toDouble() - p2[2].toDouble()).pow(2.0))

}