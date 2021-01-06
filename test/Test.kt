import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.StringReader

class Test {
    fun testRNACentral() {
        val id = "URS00005AB4A7"
        RNACentral().fetch(id)?.let {
            val drawing = SecondaryStructureDrawing(it, WorkingSession())
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.PKnot, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true")

            RnartistConfig.colorSchemes.get("Persian Carolina")!!.forEach { elementType, config ->
                config.forEach {
                    t.setConfigurationFor(SecondaryStructureType.valueOf(elementType), DrawingConfigurationParameter.valueOf(it.key), it.value)
                }
            }

            drawing.applyTheme(t)

            val frame = Rectangle(0, 0, 1920, 1080)

            //we compute the zoomLevel to fit the structure in the frame of the canvas2D
            val widthRatio = drawing.getBounds().bounds2D.width / frame.bounds2D.width
            val heightRatio = drawing.getBounds().bounds2D.height / frame.bounds2D.height
            drawing.workingSession.finalZoomLevel =
                if (widthRatio > heightRatio) 1.0 / widthRatio else 1.0 / heightRatio
            var at = AffineTransform()
            at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
            val transformedBounds = at.createTransformedShape(drawing.getBounds())
            drawing.workingSession.viewX = frame.bounds2D.centerX - transformedBounds.bounds2D.centerX
            drawing.workingSession.viewY = frame.bounds2D.centerY - transformedBounds.bounds2D.centerY

            val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()

            drawing.workingSession.setFont(g, drawing.residues.first())

            drawing.workingSession.junctionsDrawn.addAll(drawing.allJunctions)
            drawing.workingSession.helicesDrawn.addAll(drawing.allHelices)
            drawing.workingSession.singleStrandsDrawn.addAll(drawing.singleStrands)
            drawing.workingSession.phosphoBondsLinkingBranchesDrawn.addAll(drawing.phosphoBonds)
            drawing.workingSession.locationDrawn = Location(1, drawing.secondaryStructure.length)

            at = AffineTransform()
            at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
            at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)

            //File(System.getProperty("user.home"), "${id}.svg").writeText(toSVG(drawing, frame, at, TertiariesDisplayLevel.All))
        }
    }
}