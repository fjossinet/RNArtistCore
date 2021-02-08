import java.io.*
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import io.github.fjossinet.rnartist.core.rnartist
import java.awt.Color
import java.awt.Rectangle
import java.awt.geom.Rectangle2D

val structuresSelected = mapOf(
    "1FFK_9" to listOf("J0_J0", "J1_J0"),
    "1HC8_C" to listOf("J2_H4"),
    "1L9A_B" to listOf("J1_H2"),
    "2CKY_A" to listOf("J0_H2"),
    "2OIU_Q" to listOf("J2_J2"),
    "2QUS_A" to listOf("J1_J2"),
    "3OWI_A" to listOf("J2_H3"),
    "4WFL_A" to listOf("J0_J0"),
    "5U3G_B" to listOf("J5_J5"),
    "3E5C_A" to listOf("J1_H2")
)

if (args.size < 3) {
    println(
        """To run this script:
kotlin -cp location_of_your_rnartistcore-jar-with-dependencies.jar srmp.kts annotate pdbDir outputdir
kotlin -cp location_of_your_rnartistcore-jar-with-dependencies.jar srmp.kts generate outputdir groupName
        """.trimMargin())
    System.exit(0)
} else {
    when (args[0]) {
        "annotate" -> annotatePDBfiles(args[1], args[2])
        "generate" -> generateExam(args[1], args[2])
    }
}

fun generateExam(outputdir:String, groupName:String) {
    var groupDir = File(outputdir, groupName)
    groupDir.mkdir()
    val template = File(outputdir, "template.md")
    template.writeText("""
# Controle continu de SRMB Acides nucléiques

Vous avez 2h pour réaliser cet examen.

Ouvrez avec le logiciel Chimera le fichier PDB joint à cet email (structure.pdb). Explorez votre molécule et répondez aux questions suivantes.

--------------
####Question 1 (2,5 points)

Parmi ces 3 structures secondaires, laquelle correspond à votre structure 3D?

* **Réponse 1**
<img src="2d_1.svg" width=300/>
* **Réponse 2**
<img src="2d_2.svg" width=300/>
* **Réponse 3**
<img src="2d_3.svg" width=300/>
--------------

####Question 2 (2,5 points)
Dans votre structure, combien observez-vous de jonctions suivantes (donnez juste un chiffre à chaque fois, la réponse peut-être 0):

* boucles apicales :
* bulles internes :
* jonctions triples :
* jonctions quadruples : 

--------------
####Question 3 (2,5 points)
Parmi ces 3 régions, laquelle se trouve dans votre structure 3D?

_Attention: toutes les interactions présentes dans la 3D ne sont pas nécessairement dessinées. Servez vous de celles dessinées pour trouver la bonne réponse._

* **Réponse 1**
<img src="domain_1.svg" width=300>
* **Réponse 2**
<img src="domain_2.svg" width=300/>
* **Réponse 3**
<img src="domain_3.svg" width=300/>

--------------
####Question 4 (2,5 points)

Pour la région présente dans votre structure 3D, indiquez les positions des résidus établissant les interactions tertiaires dessinées en rouge dans le dessin précédent. Précisez également le type d'interaction.

--------------

## Texte à copier, compléter et coller dans votre email de réponse.

_A la place des X mettez un chiffre. Pour la question 4, entre les deux chiffres des positions des résidus il faut aussi mettre le type d'interaction que ces résidus établissent (j'ai mis deux exemples)._

Question 1
X 

Question 2
    boucles apicales : X
    bulles internes : X
    jonctions triples : X
    jonctions quadruples : X

Question 3
X

Question 4
X-cisWCHoogsteen-X
X-transHoogsteenHoogsteen-X       
""".trimIndent())

    val pdbIds = structuresSelected.keys.toList()
    val pdbIdsChosen = mutableListOf<String>()
    (1..20).forEach { i ->
        var studentDir = File(groupDir, "sujet$i")
        studentDir.mkdir()
        template.copyTo(File(studentDir,"sujet.md"))

        //the 2D structures
        var pdbId = pdbIds[(pdbIds.indices).random()]
        while (pdbIdsChosen.filter { it == pdbId }.size == 2 ) {
            pdbId = pdbIds[(pdbIds.indices).random()]
        }
        pdbIdsChosen.add(pdbId)
        var correctAnswer = (1..3).random()
        File(outputdir, "$pdbId.svg").copyTo(File(studentDir,"2d_${correctAnswer}.svg"))
        File(outputdir, "$pdbId.pdb").copyTo(File(studentDir,"structure.pdb"))
        File(outputdir, "$pdbId.json").copyTo(File(studentDir,"structure.json"))

        val correction = File(studentDir,"correction.txt")
        correction.appendText("Structure étudiée: ${pdbId}\n\n\n")

        correction.appendText("Question 1: \n${correctAnswer}\n\n")
        correction.appendText("Question 2:\n")
        correction.appendText(File(outputdir, "${pdbId}.txt").readText())
        correction.appendText("\n\n")

        var remainingAnswers = mutableListOf(1,2,3)
        remainingAnswers.remove(correctAnswer)

        var wrongpdbId_1 = pdbIds[(pdbIds.indices).random()]
        while (wrongpdbId_1 == pdbId) {
            wrongpdbId_1 = pdbIds[(pdbIds.indices).random()]
        }

        File(outputdir, "$wrongpdbId_1.svg").copyTo(File(studentDir,"2d_${remainingAnswers.first()}.svg"))

        var wrongpdbId_2 = pdbIds[(pdbIds.indices).random()]
        while (wrongpdbId_2 == pdbId || wrongpdbId_2 == wrongpdbId_1) {
            wrongpdbId_2 = pdbIds[(pdbIds.indices).random()]
        }

        File(outputdir, "$wrongpdbId_2.svg").copyTo(File(studentDir,"2d_${remainingAnswers.last()}.svg"))

        //the 2D domains
        val domainId = structuresSelected[pdbId]!![(structuresSelected[pdbId]!!.indices).random()]
        correctAnswer = (1..3).random()
        File(outputdir, "${pdbId}_${domainId}.svg").copyTo(File(studentDir,"domain_${correctAnswer}.svg"))
        File(outputdir, "${pdbId}_${domainId}.pdb").copyTo(File(studentDir,"domain_${correctAnswer}.pdb"))
        File(outputdir, "${pdbId}_${domainId}.json").copyTo(File(studentDir,"domain_${correctAnswer}.json"))


        correction.appendText("Question 3: \n${correctAnswer}\n\n")
        correction.appendText("Question 4: \n")
        correction.appendText(File(outputdir, "${pdbId}_${domainId}.txt").readText())
        correction.appendText("\n\n")

        remainingAnswers = mutableListOf(1,2,3)
        remainingAnswers.remove(correctAnswer)

        wrongpdbId_1 = pdbIds[(pdbIds.indices).random()]
        while (wrongpdbId_1 == pdbId) {
            wrongpdbId_1 = pdbIds[(pdbIds.indices).random()]
        }

        val wrongDomainId_1 = structuresSelected[wrongpdbId_1]!![(structuresSelected[wrongpdbId_1]!!.indices).random()]

        File(outputdir, "${wrongpdbId_1}_${wrongDomainId_1}.svg").copyTo(File(studentDir,"domain_${remainingAnswers.first()}.svg"))

        wrongpdbId_2 = pdbIds[(pdbIds.indices).random()]
        while (wrongpdbId_2 == pdbId || wrongpdbId_2 == wrongpdbId_1) {
            wrongpdbId_2 = pdbIds[(pdbIds.indices).random()]
        }

        val wrongDomainId_2 = structuresSelected[wrongpdbId_2]!![(structuresSelected[wrongpdbId_2]!!.indices).random()]

        File(outputdir, "${wrongpdbId_2}_${wrongDomainId_2}.svg").copyTo(File(studentDir,"domain_${remainingAnswers.last()}.svg"))

    }
}

fun annotatePDBfiles(pdbDir:String, outputdir:String) {

    val t = Theme()
    t.setConfigurationFor(
        SecondaryStructureType.Helix,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.SecondaryInteraction,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.SingleStrand,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.PKnot,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.InteractionSymbol,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t.setConfigurationFor(
        SecondaryStructureType.PhosphodiesterBond,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.Junction,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t.setConfigurationFor(
        SecondaryStructureType.AShape,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t.setConfigurationFor(
        SecondaryStructureType.AShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )

    t.setConfigurationFor(
        SecondaryStructureType.UShape,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t.setConfigurationFor(
        SecondaryStructureType.UShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )

    t.setConfigurationFor(
        SecondaryStructureType.GShape,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t.setConfigurationFor(
        SecondaryStructureType.GShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )


    t.setConfigurationFor(
        SecondaryStructureType.CShape,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t.setConfigurationFor(
        SecondaryStructureType.CShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )

    t.setConfigurationFor(
        SecondaryStructureType.XShape,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t.setConfigurationFor(
        SecondaryStructureType.XShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )

    t.setConfigurationFor(
        SecondaryStructureType.A,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t.setConfigurationFor(
        SecondaryStructureType.U,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t.setConfigurationFor(
        SecondaryStructureType.G,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t.setConfigurationFor(
        SecondaryStructureType.C,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t.setConfigurationFor(
        SecondaryStructureType.X,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t.setConfigurationFor(
        SecondaryStructureType.TertiaryInteraction,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    val t2 = Theme()
    t2.setConfigurationFor(
        SecondaryStructureType.Helix,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.SecondaryInteraction,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.SingleStrand,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.InteractionSymbol,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.PhosphodiesterBond,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.Junction,
        DrawingConfigurationParameter.fulldetails,
        "true"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.AShape,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.UShape,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.UShape,
        DrawingConfigurationParameter.color,
        getHTMLColorString(Color.LIGHT_GRAY)
    )

    t2.setConfigurationFor(
        SecondaryStructureType.GShape,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )


    t2.setConfigurationFor(
        SecondaryStructureType.CShape,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.XShape,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.A,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.U,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.G,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.C,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )
    t2.setConfigurationFor(
        SecondaryStructureType.X,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    t2.setConfigurationFor(
        SecondaryStructureType.TertiaryInteraction,
        DrawingConfigurationParameter.fulldetails,
        "false"
    )

    val rnaview = Rnaview()

    File(pdbDir).listFiles(FilenameFilter { _, name -> name.endsWith(".pdb") }).forEach { pdbFile ->
//File(pdbDir).listFiles(FilenameFilter { _, name -> name.endsWith("1GID.pdb") }).forEach { pdbFile ->
        try {
            println("Annotating ${pdbFile.name}")
            val tertiaryStructures = parsePDB(FileReader(pdbFile))
            rnaview.annotate(pdbFile).forEach { ss ->
                var ok = false
                for (ts in tertiaryStructures)
                    if (ss.rna.name.equals(ts.rna.name)) {
                        ok = ss.rna.length == ts.rna.length
                        break
                    }
                if (ok) {
                    if (ss.rna.length in 51..149) {
                        rnartist {
                            secondaryStructure = ss
                        }?.let { drawing ->
                            val frame = Rectangle(0, 0, 600, 600)
                            drawing.applyTheme(t)
                            drawing.fitTo(frame)
                            if (!drawing.allTertiaryInteractions.isEmpty()) {
                                drawing.allTertiaryInteractions.forEach {
                                    it.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                        "false"
                                    it.residues.forEach {
                                        it.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                            "true"
                                    }
                                }
                                File("${outputdir}/${pdbFile.name.split(".pdb").first()}_${ss.rna.name}.svg").writeText(
                                    toSVG(drawing, frame.width.toDouble(), frame.height.toDouble())
                                )
                                File("${outputdir}/${
                                    pdbFile.name.split(".pdb").first()
                                }_${ss.rna.name}.json").writeText(
                                    toJSON(drawing)
                                )
                                val junctionsList =
                                    File("${outputdir}/${pdbFile.name.split(".pdb").first()}_${ss.rna.name}.txt")
                                junctionsList.appendText("""boucles apicales : ${drawing.allJunctions.filter { it.junctionType == JunctionType.ApicalLoop }.size}
bulles internes : ${drawing.allJunctions.filter { it.junctionType == JunctionType.InnerLoop }.size}
jonctions triples : ${drawing.allJunctions.filter { it.junctionType == JunctionType.ThreeWay }.size}
jonctions quadruples : ${drawing.allJunctions.filter { it.junctionType == JunctionType.FourWay }.size}""")
                                var outputPDB =
                                    File("${outputdir}/${pdbFile.name.split(".pdb").first()}_${ss.rna.name}.pdb")
                                pdbFile.readLines().forEach { line ->
                                    if (line.matches(Regex("^(ATOM.{17}|HETATM.{15})${ss.rna.name}.+$"))) {
                                        outputPDB.appendText("$line\n")
                                    }
                                }

                                drawing.allJunctions.forEach {
                                    it.radius = it.radius * 2.0
                                    it.layout = it.layout
                                    drawing.computeResidues(it)
                                }

                                drawing.applyTheme(t2)

                                drawing.allJunctions.forEach { junctionDrawing ->
                                    for (i in drawing.allTertiaryInteractions.indices) {
                                        var bounds: Rectangle2D? = null
                                        var domains = mutableListOf<StructuralDomainDrawing>()
                                        var tertiariesDisplayed = mutableListOf<TertiaryInteractionDrawing>()
                                        val interaction = drawing.allTertiaryInteractions[i]
                                        if (!interaction.isSingleHBond && interaction.start != interaction.end - 1) {
                                            if (junctionDrawing.junction.locationWithoutSecondaries.contains(interaction.start) && junctionDrawing.junction.locationWithoutSecondaries.contains(
                                                    interaction.end
                                                )
                                            ) {
                                                domains.add(junctionDrawing)
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                tertiariesDisplayed.add(interaction)
                                            } else if (junctionDrawing.junction.locationWithoutSecondaries.contains(
                                                    interaction.start
                                                )
                                            ) {
                                                domains.add(junctionDrawing)
                                                when (interaction.pairedResidue.parent) {
                                                    is SecondaryInteractionDrawing -> {
                                                        val h =
                                                            (interaction.pairedResidue.parent as SecondaryInteractionDrawing).parent as HelixDrawing
                                                        domains.add(h)
                                                    }
                                                    is JunctionDrawing -> {
                                                        val j = interaction.pairedResidue.parent as JunctionDrawing
                                                        if (!domains.contains(j))
                                                            domains.add(j)
                                                    }
                                                    is SingleStrandDrawing -> {
                                                        val singlestrand =
                                                            interaction.pairedResidue.parent as SingleStrandDrawing
                                                        domains.add(singlestrand)
                                                    }
                                                }
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                tertiariesDisplayed.add(interaction)
                                            } else if (junctionDrawing.junction.locationWithoutSecondaries.contains(
                                                    interaction.end
                                                )
                                            ) {
                                                domains.add(junctionDrawing)
                                                when (interaction.residue.parent) {
                                                    is SecondaryInteractionDrawing -> {
                                                        val h =
                                                            (interaction.residue.parent as SecondaryInteractionDrawing).parent as HelixDrawing
                                                        domains.add(h)
                                                    }
                                                    is JunctionDrawing -> {
                                                        val j = interaction.residue.parent as JunctionDrawing
                                                        domains.add(j)
                                                    }
                                                    is SingleStrandDrawing -> {
                                                        val singlestrand =
                                                            interaction.residue.parent as SingleStrandDrawing
                                                        domains.add(singlestrand)
                                                    }
                                                }
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.LIGHT_GRAY)
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                    "true"
                                                interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                    getHTMLColorString(Color.RED)
                                                tertiariesDisplayed.add(interaction)
                                            }

                                            if (!domains.isEmpty()) {

                                                for (j in i + 1 until drawing.allTertiaryInteractions.size) {
                                                    val _interaction = drawing.allTertiaryInteractions[j]
                                                    if (!_interaction.isSingleHBond && _interaction.start != _interaction.end - 1) {
                                                        if (domains.first().location.contains(_interaction.start) && domains.first().location.contains(
                                                                _interaction.end
                                                            ) || domains.last().location.contains(_interaction.start) && domains.last().location.contains(
                                                                _interaction.end
                                                            )
                                                        ) {
                                                            _interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                                "true"
                                                            _interaction.residue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                                getHTMLColorString(Color.LIGHT_GRAY)
                                                            _interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                                "true"
                                                            _interaction.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                                getHTMLColorString(Color.LIGHT_GRAY)
                                                            _interaction.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                                "true"
                                                            _interaction.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                                getHTMLColorString(Color.RED)
                                                            _interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                                "true"
                                                            _interaction.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.color.toString()] =
                                                                getHTMLColorString(Color.RED)
                                                            tertiariesDisplayed.add(_interaction)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!domains.isEmpty() && !File(
                                                "${outputdir}/${
                                                    pdbFile.name.split(".pdb").first()
                                                }_${ss.rna.name}_${domains.first().name}_${domains.last().name}.svg"
                                            ).exists() && !File(
                                                "${outputdir}/${
                                                    pdbFile.name.split(".pdb").first()
                                                }_${ss.rna.name}_${domains.last().name}_${domains.first().name}.svg"
                                            ).exists()
                                        ) {

                                            domains.forEach { domain ->
                                                bounds = if (bounds == null) {
                                                    domain.selectionFrame?.bounds
                                                } else
                                                    bounds!!.createUnion(domain.selectionFrame?.bounds)
                                            }

                                            bounds?.let {
                                                drawing.fitTo(frame, it)

                                                File(
                                                    "${outputdir}/${
                                                        pdbFile.name.split(".pdb").first()
                                                    }_${ss.rna.name}_${domains.first().name}_${domains.last().name}.svg"
                                                ).writeText(toSVG(drawing,
                                                    frame.width.toDouble(),
                                                    frame.height.toDouble()))

                                                val f = File(
                                                    "${outputdir}/${
                                                        pdbFile.name.split(".pdb").first()
                                                    }_${ss.rna.name}_${domains.first().name}_${domains.last().name}.txt"
                                                )

                                                File("${outputdir}/${
                                                    pdbFile.name.split(".pdb").first()
                                                }_${ss.rna.name}_${domains.first().name}_${domains.last().name}.json").writeText(
                                                    toJSON(drawing)
                                                )
                                                var outputPDB = File("${outputdir}/${
                                                    pdbFile.name.split(".pdb").first()
                                                }_${ss.rna.name}_${domains.first().name}_${domains.last().name}.pdb")
                                                pdbFile.readLines().forEach { line ->
                                                    if (line.matches(Regex("^(ATOM.{17}|HETATM.{15})${ss.rna.name}.+$"))) {
                                                        outputPDB.appendText("$line\n")
                                                    }
                                                }

                                                tertiariesDisplayed.forEach {
                                                    f.appendText("${it.start}-${it.interaction.toString()}-${it.end}\n")
                                                }
                                            }
                                        }
                                        tertiariesDisplayed.forEach {
                                            it.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                "false"
                                            it.interactionSymbol.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                "false"
                                            it.residue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                "false"
                                            it.pairedResidue.drawingConfiguration.params[DrawingConfigurationParameter.fulldetails.toString()] =
                                                "false"
                                        }
                                    }


                                }
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}