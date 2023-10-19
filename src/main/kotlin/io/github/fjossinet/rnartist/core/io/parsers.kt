package io.github.fjossinet.rnartist.core.io

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.rnartist
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import java.io.*
import java.text.NumberFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.math.pow
import kotlin.math.sqrt

@Throws(java.lang.Exception::class)
fun parseRnaml(f: File?): List<SecondaryStructure> {
    val secondaryStructures = mutableListOf<SecondaryStructure>()
    val builder = SAXBuilder(false)
    builder.validation = false
    builder.setFeature("http://xml.org/sax/features/validation", false)
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val document = builder.build(f)
    val root = document.rootElement
    var child: Element?
    var name: String?
    var m: RNA?
    val i: Iterator<*> = root.children.iterator()
    while (i.hasNext()) {
        child = i.next() as Element?
        name = child!!.name
        if (name == "molecule") {
            var moleculeSequence = ""
            val moleculeName = child.getAttribute("id").value
            val sequence = child.getChild("sequence")
            if (sequence != null) {
                val seqdata = sequence.getChild("seq-data")
                if (seqdata != null) moleculeSequence = seqdata.value.trim { it <= ' ' }.replace("\\s+".toRegex(), "")
            }
            m = RNA(moleculeName, moleculeSequence.uppercase())
            val bps: MutableList<BasePair> = ArrayList()
            val structure = child.getChild("structure")
            if (structure != null) {
                val str_annotation = structure.getChild("model").getChild("str-annotation")
                for (e in str_annotation.getChildren("base-pair")) {
                    val bp = e
                    var edge1:Edge
                    var edge2:Edge
                    edge1 = when (bp!!.getChild("edge-5p").text[0]) {
                        'S', 's' -> Edge.Sugar
                        'H' -> Edge.Hoogsteen
                        'W', '+', '-' -> Edge.WC
                        '!', '?' -> Edge.SingleHBond
                        else -> Edge.Unknown
                    }
                    edge2 = when (bp.getChild("edge-3p").text[0]) {
                        'S', 's' -> Edge.Sugar
                        'H' -> Edge.Hoogsteen
                        'W', '+', '-' -> Edge.WC
                        '!', '?' -> Edge.SingleHBond
                        else -> Edge.Unknown
                    }
                    var orientation:Orientation
                    orientation = when (bp.getChild("bond-orientation").text.uppercase().toCharArray()[0]) {
                        'C' -> Orientation.cis
                        'T' -> Orientation.trans
                        else -> Orientation.Unknown
                    }
                    val l = Location(
                        Location(
                            bp.getChild("base-id-5p").getChild("base-id").getChild("position").text.toInt()
                        ),
                        Location(
                            bp.getChild("base-id-3p").getChild("base-id").getChild("position").text.toInt()
                        )
                    )
                    bps.add(
                        BasePair(
                            l,
                            edge1,
                            edge2,
                            orientation
                        )
                    )
                }
            }
            secondaryStructures.add(SecondaryStructure(m, basePairs =  bps))
        }
    }
    return secondaryStructures
}

@Throws(java.lang.Exception::class)
fun parseVienna(reader: Reader): SecondaryStructure {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val name = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String?
    while (`in`.readLine().also { line = it } != null) {
        when {
            line!!.startsWith(">") -> name.append(line!!.substring(1))
            line!!.matches(Regex("^[a-zA-Z\\-]+$")) -> {
                sequence.append(line)
            }
            line!!.matches(Regex("^[\\.\\(\\)\\{\\}\\[\\]A-Za-z\\-]+$")) -> {
                bn.append(line)
            }
        }
    }
    var generateRandomSeq = false
    if (sequence.isEmpty()) { //we will produce a fake sequence, then a random sequence fitting the base-pairs
        sequence.append((1..bn.length).map { listOf("A", "U", "G", "C").random()}.joinToString(separator = ""))
        generateRandomSeq = true
    }
    val ss = SecondaryStructure(
        RNA(
            name.toString(),
            sequence.toString()
        ), bracketNotation = bn.toString()
    )

    if (generateRandomSeq) {
        ss.randomizeSeq()
    }

    return ss
}

@Throws(Exception::class)
fun parseCT(reader: Reader): SecondaryStructure {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String?
    while (`in`.readLine().also { line = it } != null) {
        line = line!!.trim { it <= ' ' }
        val tokens = line!!.split("\\s+".toRegex()).toTypedArray()
        if (tokens.size != 6 || !tokens[0].matches(Regex("-?\\d+(.\\d+)?"))) continue
        sequence.append(tokens[1])
        var base5: Int
        var base3: Int
        base5 = tokens[0].toInt()
        base3 = tokens[4].toInt()
        if (base3 != 0) {
            if (base5 <= base3) bn.append("(") else bn.append(")")
        } else {
            bn.append(".")
        }
    }
    return SecondaryStructure(
        RNA(
            "A",
            sequence.toString()
        ), bn.toString(), null
    )
}

/**
 * The first 2D is the consensus 2D with a fake seq
 */
fun parseStockholm(reader: Reader, withConsensus2D:Boolean = false): Triple<Pair<String,String>, Map<String, String>, List<SecondaryStructure>> {
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    val alignedMolecules: MutableMap<String, StringBuffer> = HashMap()
    val bn = StringBuffer()
    lateinit var familyDescr: String
    lateinit var familyType: String
    val `in` = BufferedReader(reader)
    var line: String?
    while (`in`.readLine().also { line = it } != null) {
        val tokens = line!!.trim { it <= ' ' }.split("\\s+".toRegex()).toTypedArray()
        if (line!!.trim { it <= ' ' }.length != 0 && !line!!.startsWith("# ") && tokens.size == 2) {
            if (alignedMolecules.containsKey(tokens[0]))
                alignedMolecules[tokens[0]] = alignedMolecules[tokens[0]]!!.append(tokens[1])
            else
                alignedMolecules[tokens[0]] = StringBuffer(tokens[1])
        } else if (line!!.trim { it <= ' ' }.length != 0 && line!!.startsWith("#=GC SS_cons"))
            bn.append(
                tokens[2].replace("<", "(").replace(">", ")").replace(":", ".").replace(",", ".").replace("-", ".")
                    .replace("_", ".")
            )
        else if (line!!.trim { it <= ' ' }.startsWith("#=GF DE"))
            familyDescr = line!!.split("#=GF DE".toRegex()).toTypedArray()[1].trim { it <= ' ' }
        else if (line!!.trim { it <= ' ' }.startsWith("#=GF TP"))
            familyType = line!!.split("#=GF TP".toRegex()).toTypedArray()[1].trim { it <= ' ' }
    }
    `in`.close()
    val rna = RNA(
        "consensus",
        (1..bn.length).map { listOf("A", "U", "G", "C").random() }.joinToString(separator = "")
    )

    var consensusSS = SecondaryStructure(rna, bn.toString())

    val helices2keep = mutableListOf<Location>()

    consensusSS.pknots.forEach { pknot ->
        val helicalLengths = mutableListOf<Int>()
        val tertiariesLengths = mutableListOf<Int>()

        val tertiariesLocation = Location(pknot.tertiaryInteractions.map { it.start }.toSet()
            .union(pknot.tertiaryInteractions.map { it.end }).toIntArray()
        )

        alignedMolecules.forEach { alignedMolecule ->
            pknot.helix.location.blocks.forEach { block ->
                helicalLengths.add(alignedMolecule.value.substring(block.start - 1, block.end).replace("-", "").length)
            }
            tertiariesLocation.blocks.forEach { block ->
                tertiariesLengths.add(
                    alignedMolecule.value.substring(block.start - 1, block.end).replace("-", "").length
                )
            }
        }

        val deletedHelixCount = helicalLengths.count { it == 0 }

        val deletedtertiariesCount = tertiariesLengths.count { it == 0 }

        if (deletedHelixCount < deletedtertiariesCount)
            helices2keep.add(pknot.helix.location)
        else
            helices2keep.add(tertiariesLocation)
    }

    consensusSS = SecondaryStructure(rna, bn.toString(), helicesInPknots2Keep = helices2keep)

    if (withConsensus2D) {
        secondaryStructures.add(consensusSS)
        //we store in the structural domains of the consensus2D their standard deviation derived from the alignment

        consensusSS.helices.forEach { h ->
            val domainLengths = mutableListOf<Int>()

            alignedMolecules.forEach { alignedMolecule ->
                var totalLength = 0
                h.location.blocks.forEach { block ->
                    totalLength += alignedMolecule.value.substring(block.start - 1, block.end).replace("-", "").length
                }
                domainLengths.add(totalLength)
            }

            val mean = domainLengths.average()
            h.lengthStd = domainLengths
                .fold(0.0) { accumulator, next -> accumulator + (next - mean).pow(2.0) }
                .let {
                    sqrt(it / domainLengths.size)
                }
        }

        consensusSS.junctions.forEach { j ->
            val domainLengths = mutableListOf<Int>()

            alignedMolecules.forEach { alignedMolecule ->
                var totalLength = 0
                j.location.blocks.forEach { block ->
                    totalLength += alignedMolecule.value.substring(block.start - 1, block.end).replace("-", "").length
                }
                domainLengths.add(totalLength)
            }

            val mean = domainLengths.average()
            j.lengthStd = domainLengths
                .fold(0.0) { accumulator, next -> accumulator + (next - mean).pow(2.0) }
                .let {
                    sqrt(it / domainLengths.size)
                }
        }
    }
    for ((key, value) in alignedMolecules) {
        var rna = RNA(key, value.toString())
        var _bn = bn.toString()

        for (bp in consensusSS.secondaryInteractions) {
            if (rna.seq[bp.start - 1] == '-' || rna.seq[bp.end - 1] == '-') {
                _bn = _bn.replaceRange(bp.start - 1, bp.start, ".")
                _bn = _bn.replaceRange(bp.end - 1, bp.end, ".")
            }
        }

        for (bp in consensusSS.tertiaryInteractions) {
            if (rna.seq[bp.start - 1] == '-' || rna.seq[bp.end - 1] == '-') {
                _bn = _bn.replaceRange(bp.start - 1, bp.start, ".")
                _bn = _bn.replaceRange(bp.end - 1, bp.end, ".")
            }
        }

        for (pknot in consensusSS.pknots) {
            for (bp in pknot.helix.secondaryInteractions) {
                if (rna.seq[bp.start - 1] == '-' || rna.seq[bp.end - 1] == '-') {
                    _bn = _bn.replaceRange(bp.start - 1, bp.start, ".")
                    _bn = _bn.replaceRange(bp.end - 1, bp.end, ".")
                }
            }
            for (bp in pknot.tertiaryInteractions) {
                if (rna.seq[bp.start - 1] == '-' || rna.seq[bp.end - 1] == '-') {
                    _bn = _bn.replaceRange(bp.start - 1, bp.start, ".")
                    _bn = _bn.replaceRange(bp.end - 1, bp.end, ".")
                }
            }

        }

        var gapPositions = mutableListOf<Int>()
        var pos: Int = value.indexOf("-")
        while (pos >= 0) {
            gapPositions.add(pos)
            pos = value.indexOf("-", pos + 1)
        }
        rna = RNA(key, value.toString().replace("-", ""))

        val numbering_system: MutableMap<Int, Int> = mutableMapOf()

        var gapCounts = 0
        var currentPos = 0
        value.toString().forEach { residueSymbol ->
            if (residueSymbol == '-')
                gapCounts++
            else {
                currentPos++
                numbering_system[currentPos] = currentPos + gapCounts
                //println("${key} ${currentPos} ${numbering_system[currentPos]}")
            }
        }

        rna.alignment_numbering_system = numbering_system

        gapPositions.reverse()
        gapPositions.forEach { _bn = _bn.replaceRange(it, it + 1, "") }

        //we need to remap the location of the helices to keep in the pknots from the numbering system of the alignment to the absolute position for the RNA molecule
        val _helices2keep = mutableListOf<Location>()
        helices2keep.forEach { helix2Keep ->
            var blocks = mutableListOf<Block>()
            helix2Keep.blocks.forEach here@{ block2Keep ->
                //the position in the alignment can have no correspondence in the numbering system for this RNA (if this RNA had a gap at this alignment position), so...
                var _start = block2Keep.start
                while (!numbering_system.values.contains(_start) && _start > 0)
                    _start--
                var _end = block2Keep.end
                while (!numbering_system.values.contains(_end) && _end < consensusSS.rna.length)
                    _end++
                if (_start == 0 || _end == consensusSS.rna.length) { //this means that the block (helix strand), and consequently the helix, doesn't exist in this RNA
                    return@here
                }
                blocks.add(
                    Block(
                        numbering_system.filter { it.value == _start }.keys.first(),
                        numbering_system.filter { it.value == _end }.keys.first()
                    )
                )
            }
            if (blocks.size == 2) //to check if the 2 helix strand were found in this RNA
                _helices2keep.add(Location(blocks))
        }

        secondaryStructures.add(SecondaryStructure(rna, _bn, helicesInPknots2Keep = _helices2keep))

    }

    return Triple(Pair(familyDescr,familyType), alignedMolecules.map { (key, value) ->
        key to value.toString()
    }.toMap(), secondaryStructures)
}


@Throws(java.lang.Exception::class)
fun parseBPSeq(reader: Reader): SecondaryStructure {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String?
    while (`in`.readLine().also { line = it } != null) {
        line = line!!.trim { it <= ' ' }
        val tokens = line!!.split(" ".toRegex()).toTypedArray()
        if (tokens.size != 3 || !tokens[0].matches(Regex("-?\\d+(.\\d+)?"))) continue
        sequence.append(tokens[1])
        var base5: Int
        var base3: Int
        base5 = tokens[0].toInt()
        base3 = tokens[2].toInt()
        if (base3 != 0) {
            if (base5 <= base3) bn.append("(") else bn.append(")")
        } else {
            bn.append(".")
        }
    }
    return SecondaryStructure(
        RNA(
            "A",
            sequence.toString()
        ), bn.toString(), null
    )
}

fun writeVienna(ss:SecondaryStructure, writer: Writer) {
    val pw = PrintWriter(writer)
    pw.println(">${ss.rna.name}")
    pw.println(ss.rna.seq)
    pw.println(ss.toBracketNotation())
    pw.close()
}

fun writePDB(ts: TertiaryStructure, exportNumberingSystem: Boolean, writer: Writer) {
    val pw = PrintWriter(writer)
    var atomID = 0
    val coordFormat = NumberFormat.getInstance(Locale.ENGLISH)
    coordFormat.minimumFractionDigits = 3
    coordFormat.maximumFractionDigits = 3
    for (residue in ts.residues) {
        for (a in residue.atoms) {
            if (a.hasCoordinatesFilled()) {
                pw.print(formatPDBField(6, "ATOM", LEFT_ALIGN))
                pw.print(formatPDBField(11 - 7 + 1, "" + ++atomID, RIGHT_ALIGN))
                pw.print("  ")
                pw.print(formatPDBField(16 - 13 + 1, a.name.replace('\'', '*'), LEFT_ALIGN))
                pw.print(formatPDBField(20 - 18 + 1, residue.name, RIGHT_ALIGN))
                pw.print(formatPDBField(1, " " + ts.rna.name[0], LEFT_ALIGN))
                if (exportNumberingSystem) pw.print(formatPDBField(26 - 23 + 1, residue.label, RIGHT_ALIGN)) else pw.print(
                    formatPDBField(26 - 23 + 1, "" + residue.absolutePosition, RIGHT_ALIGN)
                )
                pw.print(formatPDBField(1, "", LEFT_ALIGN))
                pw.print("   ")
                pw.print(formatPDBField(38 - 31 + 1, "" + coordFormat.format(a.x), RIGHT_ALIGN))
                pw.print(formatPDBField(46 - 39 + 1, "" + coordFormat.format(a.y), RIGHT_ALIGN))
                pw.print(formatPDBField(54 - 47 + 1, "" + coordFormat.format(a.z), RIGHT_ALIGN))
                pw.print(formatPDBField(60 - 55 + 1, "1.00", RIGHT_ALIGN))
                pw.print(formatPDBField(66 - 61 + 1, "100.00", RIGHT_ALIGN))
                pw.print(formatPDBField(10, "", LEFT_ALIGN))
                pw.print(formatPDBField(78 - 77 + 1, "" + a.name[0], RIGHT_ALIGN))
                pw.println(formatPDBField(2, "", LEFT_ALIGN))
            }
        }
    }
    pw.println("END   ")
    pw.close()
}

val LEFT_ALIGN = 0
val RIGHT_ALIGN = 1

private fun formatPDBField(finalSize: Int, word: String, align: Int): String {
    val field = StringBuffer()
    if (align == LEFT_ALIGN) {
        field.append(word)
        for (i in 0 until finalSize - word.length) {
            field.append(" ")
        }
    } else {
        for (i in 0 until finalSize - word.length) {
            field.append(" ")
        }
        field.append(word)
    }
    return field.toString()
}

fun parsePDB(reader: Reader): List<TertiaryStructure> {
    val tertiaryStructures: MutableList<TertiaryStructure> = ArrayList()
    val title = StringBuffer()
    val authors = StringBuffer()
    val pubDate = StringBuffer()
    val fullContent = StringBuffer()
    var ts: TertiaryStructure? = null
    var tag:String?
    var nucleic_chain_id = 1
    var protein_chain_id = 1
    var molecule_label = "fake"
    var rna: RNA? = null
    var nt_id = 0
    var aa_id = 0
    var resId = "fake"
    var r: Residue3D? = null
    //necessary to store the atoms parameters until the parser knowns if it parses a nucleotide or an aminoacid
    val atoms: MutableMap<String, FloatArray> = HashMap()
    var isInsideNucleicAcid = false
    var isInsideProtein = false
    var ter_tag = false
    var line: String?
    val input = BufferedReader(reader)
    while (input.readLine().also { line = it } != null) {
        fullContent.append(line)
        tag = try {
            line!!.substring(0, 6).trim { it <= ' ' }
        } catch (e: IndexOutOfBoundsException) {
            "fake"
        }
        if ((tag.equals("ATOM", ignoreCase = true) || tag.equals("HETATM", ignoreCase = true)) && !chainsIgnored.contains(line!!.substring(17, 21).trim { it <= ' ' }) && !residuesIgnored.contains(line!!.substring(12, 16).trim { it <= ' ' }) && line!!.substring(21, 22).trim { it <= ' ' }.length != 0 /*only if the ATOM or HETATM line precises a molecule name*/) {
            //with the following statement we're testing if we have a new molecule
            //we have a new molecule if the molecule name has changed (even if no TER tag has been meet the line before
            //we have a new molecule if the TER tag has been meet AND if the molecule name has changed (some PDB or PDB exported by PyMOL can have a TER tag in a middle of a molecular chain
            if (!molecule_label.equals(line!!.substring(21, 22).trim { it <= ' ' }, ignoreCase = true) || ter_tag && !molecule_label.equals(line!!.substring(21, 22).trim { it <= ' ' }, ignoreCase = true)) {
                //name.length==0 for H2O, Magnesium ions, .... For residues not inside a macromolecule
                if ((isInsideNucleicAcid || isInsideProtein) && molecule_label.length > 0) {
                    if (isInsideNucleicAcid) {
                        nucleic_chain_id++
                        isInsideNucleicAcid = false
                    } else {
                        protein_chain_id++
                        isInsideProtein = false
                    }
                }
                molecule_label = line!!.substring(21, 22).trim { it <= ' ' }
                rna = null
                ts = null
                nt_id = 0
                aa_id = 0
                resId = line!!.substring(22, 27).trim { it <= ' ' }
                r = null
                ter_tag = false
            } else if (!resId.equals(line!!.substring(22, 27).trim { it <= ' ' }, ignoreCase = true) && isInsideNucleicAcid) r = null
            //residue is a nucleotide if the 04' atom is detected
            if (line!!.substring(12, 16).trim { it <= ' ' } == "O4*" || line!!.substring(12, 16).trim { it <= ' ' } == "O4'") {
                nt_id++
                resId = line!!.substring(22, 27).trim { it <= ' ' }
                if (!isInsideNucleicAcid) {
                    isInsideNucleicAcid = true
                    isInsideProtein = false
                }
                if (rna == null && ts == null) {
                    rna = RNA(molecule_label, "")
                    ts = TertiaryStructure(rna)
                    tertiaryStructures.add(ts)
                }
                rna?.addResidue(line!!.substring(17, 21).trim { it <= ' ' }.toUpperCase())
                r = ts?.addResidue3D(nt_id)
                r?.label = resId
                for ((key, value) in atoms) r?.setAtomCoordinates(key, value[0], value[1], value[2])
                atoms.clear()
            } else if (line!!.substring(12, 16).trim { it <= ' ' } == "CA") {
                aa_id++
                isInsideProtein = true
                isInsideNucleicAcid = false
            }
            val coord = floatArrayOf(line!!.substring(30, 38).trim { it <= ' ' }.toFloat(), line!!.substring(38, 46).trim { it <= ' ' }.toFloat(), line!!.substring(46, 54).trim { it <= ' ' }.toFloat())
            if (r != null) {
                r.setAtomCoordinates(line!!.substring(12, 16).trim { it <= ' ' }, coord[0], coord[1], coord[2])
            } else atoms[line!!.substring(12, 16).trim { it <= ' ' }] = coord
        } else if (tag.equals("TER", ignoreCase = true)) {
            ter_tag = true
        } else if (tag.equals("TITLE", ignoreCase = true)) {
            title.append(line!!.substring(6).trim { it <= ' ' })
        } else if (tag.equals("AUTHOR", ignoreCase = true)) {
            authors.append(line!!.substring(6).trim { it <= ' ' })
        } else if (tag.equals("JRNL", ignoreCase = true) && line!!.substring(12, 16).trim { it <= ' ' } == "REF") {
            if (line!!.substring(19, 34) != "TO BE PUBLISHED") {
                pubDate.append(line!!.substring(62, 66).trim { it <= ' ' })
            } else {
                pubDate.append("To be published")
            }
        }
    }
    for (tertiaryStructure in tertiaryStructures) {
        if (title.isNotEmpty()) {
            var t = title.toString().toLowerCase()
            t = t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase()
            tertiaryStructure.title = t
            t = authors.toString().split(",".toRegex()).toTypedArray()[0]
            tertiaryStructure.authors = t
            tertiaryStructure.pubDate = pubDate.toString()
        }
    }
    return tertiaryStructures
}

var residuesIgnored: MutableList<String> = mutableListOf(
        "MG",
        "K",
        "NA",
        "CL",
        "SR",
        "CD",
        "ACA"
)

var chainsIgnored: MutableList<String> = mutableListOf(
        "FMN",
        "PRF",
        "HOH",
        "MG",
        "OHX",
        "MN",
        "ZN",
        "SO4",
        "CA",
        "UNK",
        "N"
)

fun dumpWorkingSession(drawing: SecondaryStructureDrawing): Map<String, String> {
    val workingSession = mutableMapOf<String, String>(
        "view-x" to "%.2f".format(Locale.UK, drawing.viewX),
        "view-y" to "%.2f".format(Locale.UK, drawing.viewY),
        "final-zoom-lvl" to "%.2f".format(Locale.UK, drawing.zoomLevel)
    )
    return workingSession
}