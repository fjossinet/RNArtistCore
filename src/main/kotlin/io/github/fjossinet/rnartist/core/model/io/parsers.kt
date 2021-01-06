package io.github.fjossinet.rnartist.core.model.io

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.fjossinet.rnartist.core.model.*
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.*
import java.text.NumberFormat
import java.util.*
import java.util.stream.Collectors

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
            val moleculeName = "A"
            val sequence = child.getChild("sequence")
            if (sequence != null) {
                val seqdata = sequence.getChild("seq-data")
                if (seqdata != null) moleculeSequence = seqdata.value.trim { it <= ' ' }.replace("\\s+".toRegex(), "")
            }
            m = RNA(moleculeName, moleculeSequence.toUpperCase())
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
                    orientation = when (bp.getChild("bond-orientation").text.toUpperCase().toCharArray()[0]) {
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
            secondaryStructures.add(SecondaryStructure(m, null, bps))
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
            line!!.matches(Regex("^[A-Z]+$")) -> {
                sequence.append(line)
            }
            line!!.matches(Regex("^[\\.\\(\\)\\{\\}\\[\\]A-Za-z]+$")) -> {
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

    if (generateRandomSeq)
        ss.randomizeSeq()

    return ss
}

fun toSVG(drawing:SecondaryStructureDrawing, frame:Rectangle, at:AffineTransform, tertiariesDisplayLevel: TertiariesDisplayLevel): String {
    val svgBuffer = StringBuffer("""<svg viewBox="0 0 ${frame.width} ${frame.height}" xmlns="http://www.w3.org/2000/svg">""" + "\n")

    drawing.workingSession.junctionsDrawn.forEach { junction ->
        svgBuffer.append(junction.asSVG(at))
    }

    drawing.workingSession.helicesDrawn.forEach { helix ->
        svgBuffer.append(helix.asSVG(at))
    }

    drawing.workingSession.singleStrandsDrawn.forEach { ss ->
        svgBuffer.append(ss.asSVG(at))
    }

    drawing.workingSession.phosphoBondsLinkingBranchesDrawn.forEach { phospho ->
        svgBuffer.append(phospho.asSVG(at))
    }

    if (tertiariesDisplayLevel == TertiariesDisplayLevel.All) {
        drawing.allTertiaryInteractions.forEach { tertiary ->
            svgBuffer.append(tertiary.asSVG(at))
        }
    }

    if (tertiariesDisplayLevel >= TertiariesDisplayLevel.Pknots) {
        drawing.pknots.forEach { pknot ->
            pknot.tertiaryInteractions.forEach { tertiary ->
                svgBuffer.append(tertiary.asSVG(at))
            }
        }
    }

    svgBuffer.append("</svg>")
    return svgBuffer.toString()
}

fun toJSON(drawing:SecondaryStructureDrawing): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    var json = mapOf(
        "rna" to dumpRNA(drawing),
        "structure" to dumpSecondaryStructure(drawing),
        "layout" to dumpLayout(drawing),
        "theme" to dumpTheme(drawing),
        "session" to dumpWorkingSession(drawing)
    )

    return gson.toJson(json)
}

/**
 * This function reconstructs the SecondaryStructure object and delegates the work to the function parseProject to reconstruct and apply the layout, theme and working session.
 */
fun parseJSON(reader: Reader): SecondaryStructureDrawing? {
    val gson = Gson()
    var map: Map<String, Any> = HashMap()
    val doc = gson.fromJson(reader, map.javaClass)
    val rna = doc.get("rna") as Map<String,String>
    val secondaryStructure = SecondaryStructure(
        RNA(
            rna["name"] as String,
            rna["seq"] as String
        ))

    val structure = doc.get("structure") as Map<String, Map<String,Map<String, String>>>
    val helices = structure.get("helices") as Map<String,Map<String, String>>
    val secondaries = structure.get("secondaries") as Map<String,Map<String, String>>
    val tertiaries = structure.get("tertiaries") as Map<String,Map<String, String>>

    for ((location, tertiary) in tertiaries) {
        secondaryStructure.tertiaryInteractions.add(BasePair(Location(location), Edge.valueOf(tertiary.get("edge5")!!), Edge.valueOf(tertiary.get("edge3")!!), Orientation.valueOf(tertiary.get("orientation")!!)))
    }

    for ((_, helix) in helices) {
        val h = Helix(helix["name"]!!)
        val location = Location(helix["location"]!!)
        for (i in location.start..location.start+location.length/2-1) {
            val l = Location(Location(i), Location(location.end-(i-location.start)))
            val secondary = secondaries.get(l.toString())!!
            h.secondaryInteractions.add(BasePair(l, Edge.valueOf(secondary.get("edge5")!!), Edge.valueOf(secondary.get("edge3")!!), Orientation.valueOf(secondary.get("orientation")!!)))
        }
        secondaryStructure.helices.add(h)
    }

    val junctions = structure.get("junctions") as Map<String,Map<String, String>>
    for ((_, junction) in junctions) {
        val linkedHelices = mutableListOf<Helix>()
        junction["linked-helices"]!!.split(" ").forEach {
            for (h in secondaryStructure.helices) {
                if (h.start == Integer.parseInt(it)) {
                    linkedHelices.add(h)
                    break
                }
            }
        }
        val location = Location(junction["location"]!!)
        val j = Junction(junction["name"]!!, location, linkedHelices)
        secondaryStructure.junctions.add(j)
    }

    //We link the helices to their junctions
    for ((start, helix) in helices) {
        val h = secondaryStructure.helices.find { it.start == Integer.parseInt(start) }!!
        helix.get("first-junction-linked")?.let { startPos ->
            h.setJunction(secondaryStructure.junctions.find { it.location.start == Integer.parseInt(startPos) }!!)
        }

        helix.get("second-junction-linked")?.let { startPos ->
            h.setJunction(secondaryStructure.junctions.find { it.location.start == Integer.parseInt(startPos) }!!)
        }
    }

    val pknots = structure.get("pknots") as Map<String,Map<String, String>>

    for ((_, pknot) in pknots) {

        val pk = Pknot(pknot.get("name")!!)

        for (h in secondaryStructure.helices) {
            if (h.start ==  Integer.parseInt(pknot.get("helix")!!)) {
                pk.helix = h
                break
            }
        }
        pknot.get("tertiaries")?.split(" ")?.forEach { l ->
            val location = Location(l)
            for (tertiary in secondaryStructure.tertiaryInteractions) {
                if (tertiary.location.start == location.start && tertiary.location.end == location.end) {
                    pk.tertiaryInteractions.add(tertiary)
                    break;
                }
            }
        }
        secondaryStructure.pknots.add(pk)
        secondaryStructure.tertiaryInteractions.removeAll(pk.tertiaryInteractions)
    }

    secondaryStructure.source = "Project ${doc.get("name")}"

    val layout = doc.get("layout") as Map<String, Map<String, String>>

    val theme = doc.get("theme") as Map<String, Map<String, Map<String, String>>>

    val workingSession = doc.get("session") as Map<String, String>

    val ws = WorkingSession()
    ws.viewX = workingSession.get("view-x")!!.toDouble()
    ws.viewY = workingSession.get("view-y")!!.toDouble()
    ws.finalZoomLevel = workingSession.get("final-zoom-lvl")!!.toDouble()

    return parseProject(Project(secondaryStructure, layout, theme, ws,null));
}

class Project(val secondaryStructure: SecondaryStructure, val layout: Map<String, Map<String, String>>, val theme: Map<String, Map<String, Map<String, String>>>, val workingSession:WorkingSession, val tertiaryStructure: TertiaryStructure?)

fun parseProject(project: Project): SecondaryStructureDrawing {
    val drawing = SecondaryStructureDrawing(
        project.secondaryStructure,
        WorkingSession()
    )

    //LAYOUT
    val layout: Map<String, Map<String, String>> = project.layout
    val junctions = drawing.allJunctions
    for (junction in junctions) {
        val l = layout["" + junction.location.start]!!
        junction.inId = ConnectorId.valueOf(l["in-id"]!!)
        if (l.containsKey("out-ids")) junction.layout =
            Arrays.stream(l["out-ids"]!!.split(" ").toTypedArray()).map { c: String? ->
                ConnectorId.valueOf(c!!)
            }.collect(Collectors.toList())
        junction.radius = l["radius"]!!.toDouble()
    }

    drawing.branches.forEach {
        drawing.computeResidues(it)
    }

    //THEME
    val theme: Map<String, Map<String, Map<String, String>>> = project.theme
    val residueShapes = theme["residue-shapes"]!!
    val residueLetters = theme["residue-letters"]!!

    val helices = theme["helices"]!!

    for (h in drawing.allHelices) {
        h.drawingConfiguration = DrawingConfiguration(helices["" + h.start]!!)
        for (r in h.residues) {
            r.drawingConfiguration = DrawingConfiguration(residueShapes["" + r.location.start]!!)
            r.residueLetter.drawingConfiguration = DrawingConfiguration(residueLetters["" + r.location.start]!!)
        }
    }
    val _junctions = theme["junctions"]!!

    for (j in drawing.allJunctions) {
        j.drawingConfiguration = DrawingConfiguration(_junctions["" + j.location.start]!!)
        for (r in j.residues) {
            r.drawingConfiguration = DrawingConfiguration(residueShapes["" + r.location.start]!!)
            r.residueLetter.drawingConfiguration = DrawingConfiguration(residueLetters["" + r.location.start]!!)
        }
    }
    val singlestrands = theme["single-strands"]!!
    for (ss in drawing.singleStrands) {
        ss.drawingConfiguration = DrawingConfiguration(singlestrands["" + ss.start]!!)
        for (r in ss.residues) {
            r.drawingConfiguration = DrawingConfiguration(residueShapes["" + r.location.start]!!)
            r.residueLetter.drawingConfiguration = DrawingConfiguration(residueLetters["" + r.location.start]!!)
        }
    }

    val interactions = theme["interactions"]!!
    val interactionSymbols = theme["interaction-symbols"]!!

    for (bp in drawing.allSecondaryInteractions) {
        bp.drawingConfiguration = DrawingConfiguration(interactions["" + bp.location]!!)
        bp.interactionSymbol.drawingConfiguration = DrawingConfiguration(interactionSymbols["" + bp.location]!!)
    }
    for (bp in drawing.tertiaryInteractions) {
        bp.drawingConfiguration = DrawingConfiguration(interactions["" + bp.location]!!)
        bp.interactionSymbol.drawingConfiguration = DrawingConfiguration(interactionSymbols["" + bp.location]!!)
    }

    val phosphos = theme["phosphobonds"]!!

    for (p in drawing.allPhosphoBonds) {
        p.drawingConfiguration = DrawingConfiguration(phosphos["" + p.start]!!)
    }

    val pknots =
        theme["pknots"]!!
    for (p in drawing.pknots) {
        p.drawingConfiguration = DrawingConfiguration(pknots["" + p.location.start]!!)
        for (bp in p.tertiaryInteractions) {
            bp.drawingConfiguration = DrawingConfiguration(interactions["" + bp.location]!!)
            bp.interactionSymbol.drawingConfiguration = DrawingConfiguration(interactionSymbols["" + bp.location]!!)
        }
    }

    //WORKING SESSION
    drawing.workingSession.viewX = project.workingSession.viewX
    drawing.workingSession.viewY = project.workingSession.viewY
    drawing.workingSession.finalZoomLevel = project.workingSession.finalZoomLevel

    return drawing
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

fun parseStockholm(reader: Reader): List<SecondaryStructure> {
    var secondaryStructures = mutableListOf<SecondaryStructure>()
    val alignedMolecules: MutableMap<String, StringBuffer> = HashMap()
    val bn = StringBuffer()
    var familyName: String? = null
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
            bn.append(tokens[2].replace("<", "(").replace(">", ")").replace(":", ".").replace(",", ".").replace("-", ".").replace("_", "."))
        else if (line!!.trim { it <= ' ' }.startsWith("#=GF DE"))
            familyName = line!!.split("#=GF DE".toRegex()).toTypedArray()[1].trim { it <= ' ' }
    }
    `in`.close()
    for ((key, value) in alignedMolecules) {
        var rna = RNA(key, value.toString())
        var _bn = bn.toString()
        var consensusSS = SecondaryStructure(rna, _bn)

        for (bp in consensusSS.secondaryInteractions) {
            if (rna.seq[bp.start-1] == '-')
                _bn = _bn.replaceRange(bp.end-1,bp.end,".")
            if (rna.seq[bp.end-1] == '-')
                _bn = _bn.replaceRange(bp.start-1,bp.start,".")
        }
        for (bp in consensusSS.tertiaryInteractions) {
            if (rna.seq[bp.start-1] == '-')
                _bn = _bn.replaceRange(bp.end-1,bp.end,".")
            if (rna.seq[bp.end-1] == '-')
                _bn = _bn.replaceRange(bp.start-1,bp.start,".")
        }


        var gapPositions = mutableListOf<Int>()
        var pos: Int = value.indexOf("-")
        while (pos >= 0) {
            gapPositions.add(pos)
            pos = value.indexOf("-", pos+1)
        }
        rna = RNA(key, value.toString().replace("-", ""))

        gapPositions.reverse()
        gapPositions.forEach { _bn = _bn.replaceRange(it,it+1,"") }
        secondaryStructures.add(SecondaryStructure(rna, _bn))
    }
    return secondaryStructures
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
                if (exportNumberingSystem) pw.print(formatPDBField(26 - 23 + 1, residue.label!!, RIGHT_ALIGN)) else pw.print(formatPDBField(26 - 23 + 1, "" + residue.absolutePosition, RIGHT_ALIGN))
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
        var t = title.toString().toLowerCase()
        t = t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase()
        tertiaryStructure.title = t
        t = authors.toString().split(",".toRegex()).toTypedArray()[0]
        tertiaryStructure.authors = t
        tertiaryStructure.pubDate = pubDate.toString()
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

fun dumpRNA(drawing: SecondaryStructureDrawing): Map<String, String> {
    return mapOf(
        "name" to drawing.secondaryStructure.rna.name,
        "seq" to drawing.secondaryStructure.rna.seq
    )
}

fun dumpSecondaryStructure(drawing: SecondaryStructureDrawing): Map<String, Map<String,Map<String, String>>> {
    val structure = mutableMapOf<String, Map<String,Map<String, String>>>()

    val pknots = mutableMapOf<String,Map<String, String>>()
    structure["pknots"] = pknots
    val tertiaries = mutableMapOf<String,Map<String, String>>()
    structure["tertiaries"] = tertiaries
    val junctions = mutableMapOf<String,Map<String, String>>()
    structure["junctions"] = junctions
    val helices = mutableMapOf<String,Map<String, String>>()
    structure["helices"] = helices
    val singleStrands = mutableMapOf<String,Map<String, String>>()
    structure["single-strands"] = singleStrands
    val secondaries = mutableMapOf<String,Map<String, String>>()
    structure["secondaries"] = secondaries

    val previousDisplayLevel = drawing.workingSession.tertiariesDisplayLevel

    drawing.workingSession.tertiariesDisplayLevel = TertiariesDisplayLevel.All

    drawing.allTertiaryInteractions.forEach {
        val tertiary = mapOf<String, String>(
            "edge5" to it.interaction.edge5.toString(),
            "edge3" to it.interaction.edge3.toString(),
            "orientation" to it.interaction.orientation.toString()
        )
        tertiaries[it.location.toString()] = tertiary
    }

    drawing.workingSession.tertiariesDisplayLevel = previousDisplayLevel

    drawing.branches.forEach { branch ->
        branch.junctionsFromBranch().forEach {
            val junction = mapOf<String, String>(
                "name" to it.name,
                "location" to it.location.toString(),
                "in-helix" to it.inHelix.start.toString(),
                "linked-helices" to it.junction.helicesLinked.map { it.start.toString() }.joinToString(separator = " ")
            )
            junctions[it.location.start.toString()] = junction

            val helix = mutableMapOf<String, String>(
                "name" to it.inHelix.name,
                "location" to it.inHelix.location.toString()
            )

            //println("stored ${helix["name"]} ${helix["location"]}")

            it.inHelix.junctionsLinked.first?.let {
                helix["first-junction-linked"] = it.location.start.toString()
            }

            it.inHelix.junctionsLinked.second?.let {
                helix["second-junction-linked"] = it.location.start.toString()
            }

            helices[it.inHelix.start.toString()] = helix

            it.inHelix.secondaryInteractions.forEach {
                val secondary = mapOf<String, String>(
                    "edge5" to it.edge5.toString(),
                    "edge3" to it.edge3.toString(),
                    "orientation" to it.orientation.toString()
                )
                secondaries[it.location.toString()] = secondary
            }

        }
    }

    drawing.singleStrands.forEach { ss ->
        val singlestrand = mapOf<String, String>(
            "name" to ss.name,
            "location" to ss.location.toString()
        )
        singleStrands[ss.location.start.toString()] = singlestrand
    }

    drawing.pknots.forEach {
        val pknot = mutableMapOf<String, String>(
            "name" to it.name,
            "helix" to it.helix.start.toString(),
            "tertiaries" to it.tertiaryInteractions.map {it.location.toString()}.joinToString(separator = " ")
        )

        pknots[it.helix.start.toString()] = pknot
    }

    return structure

}

fun dumpLayout(drawing: SecondaryStructureDrawing): Map<String,Map<String, String>> {
    val layout = mutableMapOf<String,Map<String, String>>()

    drawing.branches.forEach {
        it.junctionsFromBranch().forEach {
            val junction = mutableMapOf<String, String>(
                "radius" to "%.2f".format(Locale.UK, it.radius),
                "in-id" to it.inId.toString(),
                "center-x" to "%.2f".format(Locale.UK, it.center.x),
                "center-y" to "%.2f".format(Locale.UK, it.center.y),
                "p1-x" to "%.2f".format(Locale.UK, (it.parent as HelixDrawing).line.p1.x),
                "p1-y" to "%.2f".format(Locale.UK, (it.parent as HelixDrawing).line.p1.y),
                "p2-x" to "%.2f".format(Locale.UK, (it.parent as HelixDrawing).line.p2.x),
                "p2-y" to "%.2f".format(Locale.UK, (it.parent as HelixDrawing).line.p2.y),
            )
            it.layout?.let {
                junction["out-ids"] = (it.map { it.toString() }).joinToString(separator = " ")
            }
            layout[it.location.start.toString()] =  junction
        }
    }
    drawing.singleStrands.forEach {
        val ss = mutableMapOf<String, String>(
            "p1-x" to "%.2f".format(Locale.UK, it.line.p1.x),
            "p1-y" to "%.2f".format(Locale.UK, it.line.p1.y),
            "p2-x" to "%.2f".format(Locale.UK, it.line.p2.x),
            "p2-y" to "%.2f".format(Locale.UK, it.line.p2.y),
        )
        layout[it.location.start.toString()] =  ss
    }
    return layout
}

fun dumpTheme(drawing: SecondaryStructureDrawing): Map<String, Map<String, Map<String, String>>> {
    val theme = mutableMapOf<String, Map<String, Map<String, String>>>()

    val interactions = mutableMapOf<String, Map<String, String>>()
    theme.put("interactions", interactions)

    val interactionSymbols = mutableMapOf<String, Map<String, String>>()
    theme.put("interaction-symbols", interactions)

    val residueShapes = mutableMapOf<String, Map<String, String>>()
    theme.put("residue-shapes", residueShapes)

    val residueLetters = mutableMapOf<String, Map<String, String>>()
    theme.put("residue-letters", residueLetters)

    val helices = mutableMapOf<String, Map<String, String>>()
    theme.put("helices", helices)
    drawing.allHelices.forEach { helix ->
        helices.put(helix.start.toString(), helix.drawingConfiguration.params)
        helix.residues.forEach { residue ->
            residueShapes.put(residue.location.start.toString(), residue.drawingConfiguration.params)
            residueLetters.put(residue.location.start.toString(), residue.residueLetter.drawingConfiguration.params)
        }
    }
    val junctions = mutableMapOf<String, Map<String, String>>()
    theme.put("junctions", junctions)
    drawing.allJunctions.forEach { junction ->
        junctions.put(junction.location.start.toString(), junction.drawingConfiguration.params)
        junction.residues.forEach { residue ->
            residueShapes.put(residue.location.start.toString(), residue.drawingConfiguration.params)
            residueLetters.put(residue.location.start.toString(), residue.residueLetter.drawingConfiguration.params)
        }
    }
    val singlestrands = mutableMapOf<String, Map<String, String>>()
    theme.put("single-strands", singlestrands)
    drawing.singleStrands.forEach { ss ->
        singlestrands.put(ss.location.start.toString(), ss.drawingConfiguration.params)
        ss.residues.forEach { residue ->
            residueShapes.put(residue.location.start.toString(), residue.drawingConfiguration.params)
            residueLetters.put(residue.location.start.toString(), residue.residueLetter.drawingConfiguration.params)
        }
    }

    val pknots = mutableMapOf<String, Map<String, String>>()
    theme.put("pknots", pknots)
    drawing.pknots.forEach { pk ->
        pknots.put(pk.location.start.toString(), pk.drawingConfiguration.params)
        pk.tertiaryInteractions.forEach {
            interactions.put(it.location.toString(), it.drawingConfiguration.params)
            interactionSymbols.put(it.location.toString(), it.interactionSymbol.drawingConfiguration.params)
        }
    }

    drawing.allSecondaryInteractions.forEach { secondary ->
        interactions.put(secondary.location.toString(), secondary.drawingConfiguration.params)
        interactionSymbols.put(secondary.location.toString(), secondary.interactionSymbol.drawingConfiguration.params)
    }

    drawing.tertiaryInteractions.forEach { tertiary ->
        interactions.put(tertiary.location.toString(), tertiary.drawingConfiguration.params)
        interactionSymbols.put(tertiary.location.toString(), tertiary.interactionSymbol.drawingConfiguration.params)
    }

    val phosphos = mutableMapOf<String, Map<String, String>>()
    theme.put("phosphobonds", phosphos)
    drawing.allPhosphoBonds.forEach { phospho ->
        phosphos.put(phospho.start.toString(), phospho.drawingConfiguration.params)
    }
    return theme
}

fun dumpWorkingSession(drawing: SecondaryStructureDrawing): Map<String, String> {
    val workingSession = mutableMapOf<String, String>(
        "view-x" to "%.2f".format(Locale.UK, drawing.workingSession.viewX),
        "view-y" to "%.2f".format(Locale.UK, drawing.workingSession.viewY),
        "final-zoom-lvl" to "%.2f".format(Locale.UK, drawing.workingSession.finalZoomLevel)
    )
    return workingSession
}