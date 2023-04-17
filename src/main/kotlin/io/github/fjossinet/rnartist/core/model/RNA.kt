package io.github.fjossinet.rnartist.core.model

import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Represents a set of contiguous positions, from start to end.
 *
 * @param start the first position
 * @param end the last position
 */
class Block(start: Int, end: Int) : Serializable {

    /**
     * Returns the first position for the block
     */
    val start = if (start < end) start else end

    /**
     * Returns the last position for the block
     */
    val end = if (start > end) start else end

    /**
     * Returns the length of the Location
     */
    val length = end - start + 1

    /**
     * Returns the single positions making the block
     */
    val positions = (start..end).toList()

    /**
     * Tests if a [position] is inside the block
     *
     * @param [position] the single position to test
     *
     * @return true if the position is inside the block
     */
    fun contains(position: Int) = position in start..end

    /**
     * Returns the description of the block complying the pattern "start:length"
     */
    override fun toString() = if (length == 1) "$start" else "$start:$length"

    override fun equals(other: Any?): Boolean {
        return other is Block && other.start == this.start && other.end == this.end
    }
}

/**
 * Represents a set of positions, all contiguous or not.
 *
 * A location is made with a list of [Block]. Each [Block] is a set of contiguous positions for the location.
 *
 */
class Location : Serializable {

    /**
     * The [Block] making the location
     */
    var blocks = mutableListOf<Block>()

    val positions: List<Int>
        /**
         * Returns the single positions making the location
         */
        get() {
            val positions = arrayListOf<Int>()
            this.blocks.forEach {
                positions.addAll(it.positions)
            }
            return positions.sorted()
        }

    val start: Int
        /**
         * Returns the first position for the location
         */
        get() {
            return this.blocks.first().start
        }

    val end: Int
        /**
         * Returns the last position for the location
         */
        get() {
            return this.blocks.last().end
        }

    val ends: List<Int>
        /**
         * Returns the start and end positions for each [Block] in the location
         */
        get() {
            val positions = arrayListOf<Int>()
            this.blocks.forEach {
                positions.add(it.start)
                positions.add(it.end)
            }
            return positions.sorted()
        }

    val length: Int
        /**
         * Returns the length of the Location
         */
        get() {
            return this.blocks.sumBy { it.length }
        }

    val description: String
        /**
         * Returns the description of the Location complying the pattern "start:length,start:length,start:length"
         */
        get() {
            return this.blocks.joinToString(separator = ",") { it.toString() }
        }

    /**
     * Creates an empty Location
     *
     */
    constructor() {
    }

    /**
     * Creates a Location containing a single [Block], defined by its start and end positions.
     *
     * @param [start] the start position
     * @param [end] the end position
     */
    constructor(start: Int, end: Int) : this() {
        this.blocks.add(Block(start, end))
    }

    /**
     * Creates a Location made with a single position.
     *
     * @param [pos] the single position making the location
     */
    constructor(pos: Int) : this(pos, pos) {
    }

    /**
     * Creates a Location defined by a String.
     *
     * @param [description] must comply the following pattern "start:length,start:length,start:length"
     */
    constructor(description: String) : this() {
        for (s in description.split(",")) {
            if (s.contains(':')) {
                val (start, length) = s.trim().split(':').map { it.toInt() }
                this.blocks.add(Block(start, start + length - 1))
            } else {
                this.blocks.add(
                    Block(
                        s.trim().toInt(),
                        s.trim().toInt()
                    )
                )
            }
        }
        this.blocks.sortBy { it.start }
    }

    /**
     * Creates a Location defined by a list of single positions
     *
     * @param [positions] the single positions contained in the location.
     */
    constructor(positions: IntArray) : this() {
        this.blocks.addAll(toBlocks(positions))
    }

    /**
     * Creates a Location defined by a list of blocks
     *
     * @param [blocks] the list of [Block}.
     */
    constructor(blocks: List<Block>) : this() {
        this.blocks.addAll(blocks)
    }

    /**
     * Creates a Location merging two Location objects
     *
     * @param [l1] first location to merge
     * @param [l2] second location to merge
     */
    constructor(l1: Location, l2: Location) : this((l1.positions + l2.positions).distinct().sorted().toIntArray()) {
    }

    /**
     * Extends this location with a new one
     *
     * @param [l] the Location to merge to this one
     *
     * @return a new Location
     */
    fun addLocation(l: Location): Location {
        val mutableSet = mutableSetOf<Int>()
        for (pos in l.positions)
            mutableSet.add(pos)
        for (pos in this.positions)
            mutableSet.add(pos)
        return Location(mutableSet.toIntArray())
    }

    /**
     * Shrinks this location with a new one
     *
     * @param [l] the Location to substract to this one
     *
     * @return a new Location
     */
    fun differenceOf(l: Location) = Location((this.positions - l.positions).toIntArray())

    /**
     * Tests if a single position is inside in this location
     *
     * @param [position] the single position to test
     *
     * @return true is the position is inside this location
     */
    fun contains(position: Int) = this.blocks.any { it.contains(position) }

    /**
     * Tests if a location is inside this one
     *
     * @param [location] the location to test
     * @return true is all the single positions of the location to test are inside this location
     */
    fun contains(location: Location) = location.positions.all { this.contains(it) }

    /**
     * Returns the description of the Location complying the pattern "start:length,start:length,start:length"
     */
    override fun toString() = this.description

    override fun equals(other: Any?): Boolean {
        return other is Location && other.blocks.toTypedArray().contentEquals(this.blocks.toTypedArray())
    }
}

/**
 * Represents an RNA molecule.
 *
 * @param [seq] the sequence of the RNA molecule
 * @property [name] the name of the RNA molecule
 * @param [source] where the RNA molecule comes from
 *
 */
class RNA(var name: String = "A", seq: String, var source: DataSource? = null) : Serializable {

    val length: Int
        /**
         * Returns the length of the RNA
         */
        get() {
            return this.seq.length
        }

    private var _seq = java.lang.StringBuilder(seq)

    var seq: String
        /**
         * Sets the sequence of the RNA
         */
        set(value) {
            this._seq = java.lang.StringBuilder(value)
        }
        /**
         * Returns the sequence of the RNA
         */
        get() {
            return this._seq.toString()
        }

    var alignment_numbering_system: Map<Int, Int>? = null

    /**
     * If true, some functions will compute the location according to the alignment numbering system
     */
    var useAlignmentNumberingSystem = false

    var tertiary_structure_numbering_system: Map<Int, String>? = null

    /**
     * If true, some functions will compute the location according to the 3D Structure numbering system
     */
    var useTertiaryStructureNumberingSystem = false

    /**
     * Adds a single residue to the end of the RNA sequence
     *
     * @param [residue] the name of the residue
     */
    fun addResidue(residue: String) {
        val unModifiedNucleotide = modifiedNucleotides[residue];
        if (unModifiedNucleotide != null)
            this._seq.append(unModifiedNucleotide);
        else {
            if ("ADE".equals(residue) || "A".equals(residue))
                this._seq.append("A");
            else if ("URA".equals(residue) || "URI".equals(residue) || "U".equals(residue))
                this._seq.append("U");
            else if ("GUA".equals(residue) || "G".equals(residue))
                this._seq.append("G");
            else if ("CYT".equals(residue) || "C".equals(residue))
                this._seq.append("C");
            else if ("a".equals(residue) || "u".equals(residue) || "g".equals(residue) || "c".equals(residue) || "t".equals(
                    residue
                )
            )
                this._seq.append(residue);
            else if ("X".equals(residue))
                this._seq.append("X")
            else if ("N".equals(residue))
                this._seq.append("N")
            else
                throw Exception("Unknown Residue ${residue}")
        }
    }

    /**
     * Returns a residue for a given position
     *
     * @param [pos] the absolute position for the residue to get
     *
     * @return the name of the residue
     *
     * @throws [RuntimeException] if the [pos] is outside the ends of the RNA molecule
     *
     */
    fun getResidue(pos: Int) = if (pos <= 0 || pos > this.length)
        throw RuntimeException("The position asked for is outside the ends of your RNA")
    else
        this._seq[pos - 1]

    /**
     * Returns the subsequence for a given [Location]
     *
     * @param [l] the [Location] corresponding to the subsequence
     *
     * @return the subsequence as a String
     */
    fun subSequence(l: Location) = this._seq.substring(l.start - 1, l.end).toString()

    /**
     * Returns the location with the positions defined in the [alignment_numbering_system]
     *
     * @param [l] the [Location] to map against the [alignment_numbering_system]
     *
     * @return the same location if the [alignment_numbering_system] is null
     * @return the modified location
     */
    fun mapLocation(l: Location): Location {
        return alignment_numbering_system?.let { ns ->
            var blocks = mutableListOf<Block>()
            l.blocks.forEach {
                ns[it.start]?.let { newStart ->
                    ns[it.end]?.let { newEnd ->
                        blocks.add(Block(newStart, newEnd))
                    }
                }
            }
            Location(blocks)
        } ?: run {
            l
        }
    }

    /**
     * Returns the single position defined in the [alignment_numbering_system]
     *
     * @param [p] the single position to map against the [alignment_numbering_system]
     *
     * @return the same position if the [alignment_numbering_system] is null
     * @return the modified position
     */
    fun mapPosition(p: Int): Int {
        return alignment_numbering_system?.let { ns ->
            ns[p]
        } ?: run {
            p
        }
    }

    override fun toString() = "RNA \"${this.name}\" (from ${this.source})"
}

/**
 * Describes a basepair in an RNA secondary structure
 *
 * @property [location] the absolute positions for the two interacting residues
 * @property [edge5] the edge for the first residue (first meaning according to the 5'->3' orientation)
 * @property [edge3] the edge for the second residue (first meaning according to the 5'->3' orientation)
 * @property [orientation] the orientation for this base-pair
 * */
class BasePair(
    val location: Location,
    val edge5: Edge = Edge.WC,
    val edge3: Edge = Edge.WC,
    val orientation: Orientation = Orientation.cis
) : Serializable {

    /**
     * Returns the absolute position of the first residue (first meaning according to the 5'->3' orientation)
     */
    val start: Int
        get() {
            return this.location.start
        }

    /**
     * Returns the absolute position of the second residue (first meaning according to the 5'->3' orientation)
     */
    val end: Int
        get() {
            return this.location.end
        }

    override fun toString() =
        if (edge5 == Edge.SingleHBond && edge3 == Edge.SingleHBond) "$edge5" else "$orientation:$edge5:$edge3"

}

class SingleStrand(val name: String = "MySingleStrand", start: Int, end: Int) : Serializable {

    val location = Location(start, end)

    val start: Int
        get() {
            return this.location.start
        }

    val end: Int
        get() {
            return this.location.end
        }

    val length: Int
        get() {
            return this.location.length
        }
}

class Pknot(
    val name: String = "MyPknot",
    helix1: Helix? = null,
    helix2: Helix? = null,
    pknotsSoFar: MutableList<Pknot> = mutableListOf(),
    helicesInPknots2Keep: List<Location> = listOf()
) : Serializable {

    val tertiaryInteractions = mutableListOf<BasePair>()
    lateinit var helix: Helix

    init {
        helix1?.let {
            helix2?.let {
                if (pknotsSoFar.filter { it.helix == helix1 }.isNotEmpty()) {
                    this.helix = helix1
                    this.tertiaryInteractions.addAll(helix2.secondaryInteractions)
                } else if (pknotsSoFar.filter { it.helix == helix2 }.isNotEmpty()) {
                    this.helix = helix2
                    this.tertiaryInteractions.addAll(helix1.secondaryInteractions)
                } else if (helicesInPknots2Keep.contains(helix1.location)) {
                    this.helix = helix1
                    this.tertiaryInteractions.addAll(helix2.secondaryInteractions)
                } else if (helicesInPknots2Keep.contains(helix2.location)) {
                    this.helix = helix2
                    this.tertiaryInteractions.addAll(helix1.secondaryInteractions)
                } else {
                    this.helix = if (helix1.end - helix1.start > helix2.end - helix2.start) helix2 else helix1
                    this.tertiaryInteractions.addAll((if (helix1.end - helix1.start > helix2.end - helix2.start) helix1 else helix2).secondaryInteractions)
                }
            }

        }

    }

    val location: Location
        get() {
            var l = helix.location
            for (interaction in this.tertiaryInteractions)
                l = l.addLocation(interaction.location)
            return l
        }
}

interface StructuralDomain {
    val location: Location
    val start: Int
    val end: Int
    val length: Int
    var maxBranchLength: Int

    /**
     * The standard deviation for the length of this structural domain
     */
    var lengthStd: Double
}

abstract class AbstractStructuralDomain : StructuralDomain {
    override var maxBranchLength = 0
        set(value) {
            if (value > field)
                field = value
        }
    override var lengthStd = 0.0
}

class Helix(val name: String = "MyHelix") : AbstractStructuralDomain() {

    val secondaryInteractions = mutableListOf<BasePair>()
    var junctionsLinked = Pair<Junction?, Junction?>(null, null)

    override val location: Location
        get() {
            val positionsInHelix =
                this.secondaryInteractions.map { bp -> arrayOf(bp.start, bp.end) }.toTypedArray().flatten()
            return Location(positions = positionsInHelix.toIntArray())
        }

    override val start: Int
        get() {
            return this.location.start
        }

    override val end: Int
        get() {
            return this.location.end
        }

    override val length: Int
        get() {
            return this.location.length / 2
        }

    val ends: List<Int>
        get() {
            val ends = arrayListOf<Int>()
            if (this.location.blocks.size == 1) {
                val b = this.location.blocks[0]
                ends.add(b.start)
                ends.add(b.start + b.length / 2 - 1)
                ends.add(b.start + b.length / 2)
                ends.add(b.end)
            } else
                for (b in this.location.blocks) {
                    ends.add(b.start)
                    ends.add(b.end)
                }
            return ends.sorted()
        }

    constructor(location:Location):this() {
        for (i in 0 until location.blocks.first().length)
            this.secondaryInteractions.add(BasePair(location = Location("${location.blocks.first().start+i},${location.blocks.last().end-i}")))
    }

    fun setJunction(junction: Junction) {
        this.junctionsLinked =
            if (this.junctionsLinked.first == null) this.junctionsLinked.copy(first = junction) else this.junctionsLinked.copy(
                second = junction
            )
    }

    fun getPairedPosition(position: Int): Int? {
        for (bp in this.secondaryInteractions) {
            if (bp.start == position) {
                return bp.end
            }
            if (bp.end == position) {
                return bp.start
            }
        }
        return null
    }
}

class Junction(
    var name: String = "MyJunction",
    override val location: Location,
    val helicesLinked: MutableList<Helix>
) : AbstractStructuralDomain() {

    override val length: Int
        get() {
            return this.location.length
        }

    override val start: Int
        get() {
            return this.location.start
        }

    override val end: Int
        get() {
            return this.location.end
        }

    val junctionType: JunctionType
        get() {
            return JunctionType.values().first { it.value == this.location.blocks.size }
        }

    val locationWithoutSecondaries: Location

    init {
        this.helicesLinked.sortBy { it.start }
        for (h in helicesLinked)
            h.setJunction(this)
        val j = Location()
        for (b in this.location.blocks) {
            if (b.length > 2) //The block is deleted. Otherwise we will invert start and end and get the same Block in the end
                j.blocks.add(Block(b.start + 1, b.end - 1))
        }
        this.locationWithoutSecondaries = j
    }

}

class TertiaryStructure(val rna: RNA) : Serializable {

    val residues: MutableList<Residue3D> = mutableListOf<Residue3D>()
    var title: String? = null
    var authors: String? = null
    var pubDate: String = "To be published"
    var pdbId: String? = null
    var source: DataSource? = null

    fun addResidue3D(absolutePosition: Int): Residue3D {
        var r: Residue3D = when (this.rna.getResidue(absolutePosition)) {
            'A' -> Adenine3D(absolutePosition)
            'U' -> Uracil3D(absolutePosition)
            'G' -> Guanine3D(absolutePosition)
            'C' -> Cytosine3D(absolutePosition)
            else -> UnknownResidue3D(absolutePosition)
        }
        this.removeResidue3D(absolutePosition)
        residues.add(r)
        return r
    }

    private fun removeResidue3D(absolutePosition: Int) {
        for (r in residues)
            if (r.absolutePosition == absolutePosition) {
                residues.remove(r)
                return
            }
    }

    fun getResidue3DAt(position: Int): Residue3D? {
        for (r in residues) if (r.absolutePosition == position) return r
        return null
    }

    fun getNumberingSystem() = this.residues.map { it.label }

}

abstract class Residue3D(val name: String, val absolutePosition: Int) : Serializable {

    val atoms: MutableList<Atom> = mutableListOf<Atom>()
    lateinit var label: String
    var sugarPucker = 0

    open fun setAtomCoordinates(atomName: String, x: Float, y: Float, z: Float): Atom? {
        var _atomName = atomName.replace('*', '\'')
        if (_atomName == "OP1" || _atomName == "O1P") _atomName =
            RiboNucleotide3D.O1P
        if (_atomName == "OP2" || _atomName == "O2P") _atomName =
            RiboNucleotide3D.O2P
        if (_atomName == "OP3" || _atomName == "O3P") _atomName =
            RiboNucleotide3D.O3P
        val a: Atom? = this.getAtom(_atomName)
        a?.setCoordinates(x, y, z)
        return a
    }

    fun getAtom(atomName: String): Atom? {
        for (a in atoms) if (a.name == atomName) return a
        return null
    }

}

abstract class RiboNucleotide3D(name: String, absolutePosition: Int) : Residue3D(name, absolutePosition) {

    override fun setAtomCoordinates(atomName: String, x: Float, y: Float, z: Float): Atom? {
        val a = super.setAtomCoordinates(atomName, x, y, z)
        //TODO each time some Atom coordinates are registered, check if a new TorsionAngle can be calculated
        return a
    }

    protected abstract fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom>

    companion object {
        const val C3ENDO = 0
        const val C2ENDO = 1
        const val C1 = "C1'"
        const val C2 = "C2'"
        const val C3 = "C3'"
        const val C4 = "C4'"
        const val C5 = "C5'"
        const val O2 = "O2'"
        const val O3 = "O3'"
        const val O4 = "O4'"
        const val O5 = "O5'"
        const val O1P = "O1P"
        const val O2P = "O2P"
        const val O3P = "O3P"
        const val O1A = "O1A"
        const val O2A = "O2A"
        const val O3A = "O3A"
        const val O1B = "O1B"
        const val O2B = "O2B"
        const val O3B = "O3B"
        const val O1G = "O1G"
        const val O2G = "O2G"
        const val O3G = "O3G"
        val P = arrayOf("P", "PA", "PB", "PG")
    }

    init {
        sugarPucker = C3ENDO
        for (p in P) atoms.add(
            Atom(p)
        )
        atoms.add(Atom(O1P))
        atoms.add(Atom(O2P))
        atoms.add(Atom(O3P))
        //if tri-phosphate (i.e. SARS PDB provided with S2S)
        atoms.add(Atom(O1A))
        atoms.add(Atom(O2A))
        atoms.add(Atom(O3A))
        atoms.add(Atom(O1B))
        atoms.add(Atom(O2B))
        atoms.add(Atom(O3B))
        atoms.add(Atom(O1G))
        atoms.add(Atom(O2G))
        atoms.add(Atom(O3G))
        atoms.add(Atom(C1))
        atoms.add(Atom(C2))
        atoms.add(Atom(O2))
        atoms.add(Atom(C3))
        atoms.add(Atom(O3))
        atoms.add(Atom(C4))
        atoms.add(Atom(O4))
        atoms.add(Atom(C5))
        atoms.add(Atom(O5))
        atoms.addAll(getDefaultBaseAtoms(false))
    }
}

class UnknownResidue3D(absolutePosition: Int) : Residue3D("X", absolutePosition)

class Adenine3D(absolutePosition: Int) : RiboNucleotide3D("A", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
            "N9", 5.671f, -4.305f, 1.390f,
            "C8", 4.358f, -4.673f, 1.330f,
            "N7", 3.565f, -3.717f, 0.950f,
            "C5", 4.410f, -2.640f, 0.750f,
            "N6", 2.967f, -0.828f, 0.050f,
            "C6", 4.189f, -1.313f, 0.340f,
            "N1", 5.256f, -0.506f, 0.240f,
            "C2", 6.465f, -0.989f, 0.540f,
            "N3", 6.800f, -2.209f, 0.930f,
            "C4", 5.707f, -2.984f, 1.010f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom =
                Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates(
                (atoms[i + 1] as Float),
                (atoms[i + 2] as Float),
                (atoms[i + 3] as Float)
            )
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Cytosine3D(absolutePosition: Int) : RiboNucleotide3D("C", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
            "N1", 5.671f, -4.305f, 1.390f,
            "C6", 4.403f, -4.822f, 1.380f,
            "C5", 3.339f, -4.065f, 1.030f,
            "N4", 2.610f, -1.903f, 0.310f,
            "C4", 3.603f, -2.696f, 0.670f,
            "N3", 4.845f, -2.198f, 0.680f,
            "O2", 7.062f, -2.556f, 1.060f,
            "C2", 5.900f, -2.980f, 1.040f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom =
                Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates(
                (atoms[i + 1] as Float),
                (atoms[i + 2] as Float),
                (atoms[i + 3] as Float)
            )
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Guanine3D(absolutePosition: Int) : RiboNucleotide3D("G", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
            "N9", 5.671f, -4.305f, 1.390f,
            "C8", 4.338f, -4.651f, 1.320f,
            "N7", 3.550f, -3.676f, 0.940f,
            "C5", 4.420f, -2.604f, 0.740f,
            "O6", 3.067f, -0.759f, 0.040f,
            "C6", 4.148f, -1.276f, 0.330f,
            "N1", 5.325f, -0.513f, 0.260f,
            "N2", 7.579f, -0.093f, 0.420f,
            "C2", 6.597f, -0.986f, 0.550f,
            "N3", 6.848f, -2.225f, 0.940f,
            "C4", 5.712f, -2.974f, 1.010f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom =
                Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates(
                (atoms[i + 1] as Float),
                (atoms[i + 2] as Float),
                (atoms[i + 3] as Float)
            )
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Uracil3D(absolutePosition: Int) : RiboNucleotide3D("U", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
            "N1", 5.671f, -4.305f, 1.390f,
            "C6", 4.402f, -4.837f, 1.380f,
            "C5", 3.337f, -4.092f, 1.040f,
            "O4", 2.584f, -1.954f, 0.320f,
            "C4", 3.492f, -2.709f, 0.660f,
            "N3", 4.805f, -2.261f, 0.690f,
            "O2", 7.028f, -2.502f, 1.040f,
            "C2", 5.913f, -3.000f, 1.040f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom =
                Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates(
                (atoms[i + 1] as Float),
                (atoms[i + 2] as Float),
                (atoms[i + 3] as Float)
            )
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Atom(val name: String) : Serializable {

    var coordinates: FloatArray? = null
    var x: Float? = null
        get() {
            return this.coordinates?.get(0)
        }
    var y: Float? = null
        get() {
            return this.coordinates?.get(1)
        }
    var z: Float? = null
        get() {
            return this.coordinates?.get(2)
        }

    fun setCoordinates(x: Float, y: Float, z: Float) {
        if (this.coordinates == null) {
            this.coordinates = floatArrayOf(x, y, z)
        } else {
            (this.coordinates as FloatArray)[0] = x
            (this.coordinates as FloatArray)[1] = y
            (this.coordinates as FloatArray)[2] = z
        }
    }

    fun hasCoordinatesFilled() = this.coordinates != null

}

class SecondaryStructure(
    var rna: RNA,
    bracketNotation: String? = null,
    basePairs: List<BasePair>? = null,
    helices: List<Helix>? = null,
    var source: DataSource? = null,
    helicesInPknots2Keep: List<Location> = listOf()
) : Serializable {

    var tertiaryStructure:TertiaryStructure? = null
    var name: String = this.rna.name
    val tertiaryInteractions = mutableSetOf<BasePair>()
    val helices = mutableListOf<Helix>()
    val singleStrands = mutableListOf<SingleStrand>()
    val pknots = mutableListOf<Pknot>()
    val junctions = mutableListOf<Junction>()

    val secondaryInteractions: List<BasePair>
        get() {
            val interactions = mutableListOf<BasePair>()
            for (h in this.helices) {
                interactions.addAll(h.secondaryInteractions)
            }
            return interactions
        }

    val length: Int
        get() {
            return this.rna.seq.length
        }

    init {
        var bps: MutableList<BasePair>

        bps = when {
            basePairs != null -> {
                basePairs.toMutableList()
            }
            bracketNotation != null -> {
                toBasePairs(bracketNotation)
            }
            else -> {
                arrayListOf()
            }
        }

        helices?.let {
            this.helices.addAll(it)
        }

        //do we have an aligned RNA sequence containing gaps??
        if (rna.seq.contains('-')) {
            var bps2Remove = arrayListOf<BasePair>()
            bps.forEach {
                if (rna.getResidue(it.start) == '-' || rna.getResidue(it.end) == '-') //pairing with a gap
                    bps2Remove.add(it)
            }
            bps.removeAll(bps2Remove)
        }

        if (bracketNotation != null || basePairs != null || this.helices.isNotEmpty()) { // to be sure to create a uniq single-strand only if no base-pairs in the bracket or basepairs list. If the secondary structure is created only with an RNA molecule, nothing is done at the structural level
            if (bps.isNotEmpty()) {
                bps.sortBy { it.start }
                val bpInHelix = mutableSetOf<BasePair>()
                BASEPAIRS@ for (i in 0 until bps.size - 1) { //for each basepair with gather the successive stacked basepairs
                    if (bps[i].edge3 == Edge.SingleHBond || bps[i].edge5 == Edge.SingleHBond) //we can increase the stringency here, like only the canonical
                        continue
                    var start1 = bps[i].start
                    var end1 = bps[i].end
                    for (h in this.helices) { //if an helix as already the same basepair, we will construct the same helix with less basepairs, so stop
                        for (bb in h.secondaryInteractions) {
                            if (bb.start == start1 && bb.end == end1)
                                continue@BASEPAIRS
                        }
                    }
                    var j = i + 1
                    while (j < bps.size) {
                        val start2 = bps[j].start
                        val end2 = bps[j].end
                        if (bps[j].edge3 == Edge.SingleHBond || bps[j].edge5 == Edge.SingleHBond) { //we can increase the stringency here, like only the canonical
                            j++
                            continue
                        }
                        if (start1 + 1 == start2 && end1 - 1 == end2) { //if successive basepair with the last one, extension of the current helix
                            bpInHelix.add(bps[i])
                            bpInHelix.add(bps[j])
                            start1 = bps[j].start
                            end1 = bps[j].end
                        } else if (start2 > start1 + 1) { //since the base-pairs are sorted, we will never get more succcessive stacked bp. We can restart with the next basepairs in the list
                            if (!bpInHelix.isEmpty()) {
                                val h = Helix("H${this.helices.size + 1}")
                                for (bp in bpInHelix) {
                                    h.secondaryInteractions.add(bp)
                                }
                                this.helices.add(h)
                                bpInHelix.clear()
                            }
                            continue@BASEPAIRS
                        }
                        j++
                    }
                    if (!bpInHelix.isEmpty()) {
                        val h = Helix("H${this.helices.size + 1}")
                        for (bp in bpInHelix) {
                            h.secondaryInteractions.add(bp)
                        }
                        this.helices.add(h)
                        bpInHelix.clear()
                    }
                }
                if (!bpInHelix.isEmpty()) {
                    val h = Helix("H${this.helices.size + 1}")
                    for (bp in bpInHelix) {
                        h.secondaryInteractions.add(bp)
                    }
                    this.helices.add(h)
                    bpInHelix.clear()
                }
            }

            if (this.helices.isNotEmpty()) {
                //since a residue can interact through 3 edges, it can be in several helices (it makes two different interactions, each one consecutive to another one, and then making two different helices)
                //then we need to check if several helices contains the same position, and keep the longest one
                for (i in 0 until this.rna.length) {
                    var pknots = mutableListOf<Helix>()
                    for (h in this.helices) {
                        if (h.location.contains(i)) {
                            pknots.add(h)
                        }
                    }
                    val longest = pknots.maxByOrNull { it -> it.length }
                    for (h in pknots) {
                        if (h != longest)
                            this.helices.remove(h)
                    }
                }

                var foundPknot: Boolean

                do {
                    foundPknot = false
                    I@ for (i in 0 until this.helices.size - 1) {
                        for (j in i + 1 until this.helices.size) {
                            if (this.helices[i].location.start > this.helices[j].location.start && this.helices[i].location.start < this.helices[j].location.end && this.helices[i].location.end > this.helices[j].location.end || this.helices[j].location.start > this.helices[i].location.start && this.helices[j].location.start < this.helices[i].location.end && this.helices[j].location.end > this.helices[i].location.end) {
                                this.pknots.add(
                                    Pknot(
                                        "PK${this.pknots.size + 1}",
                                        this.helices[i],
                                        this.helices[j],
                                        this.pknots,
                                        helicesInPknots2Keep
                                    )
                                )
                                foundPknot = true
                                break@I
                            }
                        }
                    }

                    for (pknot in this.pknots) {
                        this.helices.removeAll {
                            !it.secondaryInteractions.intersect(pknot.tertiaryInteractions).isEmpty()
                        }
                        bps.removeAll { pknot.tertiaryInteractions.contains(it) }
                    }

                    //now the remaining interactions as tertiary interactions
                    bps.removeAll { secondaryInteractions.contains(it) }
                    for (bp in bps)
                        this.tertiaryInteractions.add(bp)

                } while (foundPknot)
                //now the junctions
                this.findJunctions()

                //the max branch length for each
                this.junctions.forEach { junction ->
                    if (junction.junctionType == JunctionType.ApicalLoop) {
                        var length = 1
                        var currentJunction = junction
                        currentJunction.maxBranchLength = length++
                        currentJunction.helicesLinked[0].maxBranchLength = length++
                        var previousJunction =
                            currentJunction.helicesLinked[0].junctionsLinked.toList().find { it != currentJunction }
                        while (previousJunction != null) {
                            currentJunction = previousJunction
                            currentJunction.maxBranchLength = length++
                            currentJunction.helicesLinked[0].maxBranchLength = length++
                            previousJunction =
                                currentJunction.helicesLinked[0].junctionsLinked.toList().find { it != currentJunction }
                        }

                    }
                }

            }

            var currentPosition = 1

            for (h in this.helices) {
                val junctionsLinked = h.junctionsLinked
                if (junctionsLinked.first == null || junctionsLinked.second == null) {
                    if (currentPosition <= h.location.start - 1)
                        this.singleStrands.add(
                            SingleStrand(
                                "SS${this.singleStrands.size}",
                                currentPosition,
                                h.location.start - 1
                            )
                        )
                    currentPosition = h.end + 1
                }
            }

            if (currentPosition <= this.length) //We create a uniq single-strand along the full sequence, but only if a bracket or a list of base pairs has been provided
                this.singleStrands.add(SingleStrand("SS${this.singleStrands.size}", currentPosition, this.length))
        }

    }

    /**
    Return the position paired to the position given as argument. Return nil if this position is not paired at all.
     **/
    fun getPairedPosition(position: Int): Int? {
        for (h in this.helices) {
            for (bp in h.secondaryInteractions) {
                if (bp.start == position) {
                    return bp.end
                }
                if (bp.end == position) {
                    return bp.start
                }
            }
        }
        for (bp in tertiaryInteractions) {
            if (bp.start == position) {
                return bp.end
            }
            if (bp.end == position) {
                return bp.start
            }
        }
        return null
    }

    //compute the location from an apical loop to a 3-way or greater
    fun getStemLoopLocation(apicalLoop: Junction): Location {
        var l = apicalLoop.location
        var previousHelix = this.helices.find { l.start in it.ends }
        var previousJunction = this.junctions.find { it.location.contains(l.start) }
        while (previousHelix != null || previousJunction != null && previousJunction.junctionType == JunctionType.InnerLoop) {
            l = l.addLocation(if (previousHelix != null) previousHelix.location else previousJunction!!.location)
            previousHelix = this.helices.find { l.start in it.ends && it != previousHelix }
            previousJunction = this.junctions.find { it.location.contains(l.start) }
        }
        return l
    }

    fun getPreviousJunction(helix: Helix) = this.junctions.find { it.location.contains(helix.start) }

    fun getPreviousHelix(junction: Junction) = this.helices.find { it.ends.contains(junction.start) }!!

    fun getPreviousStructuralDomain(domain: StructuralDomain): StructuralDomain? {
        return when (domain) {
            is Helix -> this.getPreviousJunction(domain)
            is Junction -> this.getPreviousHelix(domain)
            else -> null
        }
    }

    fun getJunctionLevel(junction: Junction): Int {
        var lvl = 1
        var domain = getPreviousStructuralDomain(junction)
        while (domain != null) {
            lvl++
            domain = getPreviousStructuralDomain(domain)
        }
        return lvl
    }

    /**
    Return the next end of an helix (its paired position and the helix itself) after the position given as argument (along the sequence).
    Useful to get the next helix after an helix.
     **/
    fun getNextHelixEnd(position: Int): Triple<Int, Int, Helix>? {
        var minNextEnd =
            this.length //the next end is the lowest 3' position of an helix right after the position given as argument
        var pairedPosition = -1
        lateinit var helix: Helix

        for (h in this.helices) {
            if (h.ends[0] in (position + 1) until minNextEnd) {
                minNextEnd = h.ends[0]
                pairedPosition = h.ends[3]
                helix = h
            }
            if (h.ends[2] in (position + 1) until minNextEnd) {
                minNextEnd = h.ends[2]
                pairedPosition = h.ends[1]
                helix = h
            }
        }
        if (minNextEnd == length) {
            return null
        }
        return Triple(minNextEnd, pairedPosition, helix)
    }

    private fun findJunctions() {
        this.junctions.clear()
        var junctionCount = 0
        this.helices.forEach {
            it.junctionsLinked = Pair<Junction?, Junction?>(null, null)
        }
        for (h in this.helices) {
            var positionsInJunction = mutableListOf<Int>()
            var helicesLinked = mutableListOf<Helix>()
            //one side of the helix
            var pos = h.ends[1] //3'-end
            if (this.junctions.filter { it.location.contains(pos) }.isEmpty()) { //already in a junction?
                LOOP@ do {
                    val nextHelix = this.getNextHelixEnd(pos)
                    if (nextHelix != null) {
                        positionsInJunction.addAll(pos..nextHelix.first)
                        helicesLinked.add(nextHelix.third)
                        pos = nextHelix.second
                    } else { //not a junction
                        positionsInJunction = mutableListOf<Int>()
                        helicesLinked = mutableListOf<Helix>()
                        break@LOOP
                    }
                } while (pos != h.ends[1])

                if (!positionsInJunction.isEmpty()) {
                    this.junctions.add(
                        Junction(
                            name = "J${junctionCount++}",
                            location = Location(positions = positionsInJunction.toIntArray()),
                            helicesLinked = helicesLinked
                        )
                    )
                }
            }

            //the other side (of the river ;-) )
            positionsInJunction = mutableListOf()
            helicesLinked = mutableListOf()
            pos = h.ends[3] //3'-end
            if (this.junctions.filter { it.location.contains(pos) }.isEmpty()) { //already in a junction?
                LOOP@ do {
                    val nextHelix = this.getNextHelixEnd(pos)
                    if (nextHelix != null) {
                        positionsInJunction.addAll(pos..nextHelix.first)
                        helicesLinked.add(nextHelix.third)
                        pos = nextHelix.second
                    } else { //not a junction
                        positionsInJunction = mutableListOf<Int>()
                        helicesLinked = mutableListOf<Helix>()
                        break@LOOP
                    }
                } while (pos != h.ends[3])

                if (!positionsInJunction.isEmpty()) {
                    this.junctions.add(
                        Junction(
                            name = "J${junctionCount++}",
                            location = Location(positions = positionsInJunction.toIntArray()),
                            helicesLinked = helicesLinked
                        )
                    )
                }
            }
        }
    }

    fun toBracketNotation(): String {
        val bn = CharArray(this.rna.length)
        for (i in 0 until this.rna.length) bn[i] = '.'
        for (helix in this.helices) {
            for (bp in helix.secondaryInteractions) {
                bn[bp.start - 1] = '('
                bn[bp.end - 1] = ')'
            }
        }
        for (bp in this.tertiaryInteractions) {
            bn[bp.start - 1] = '('
            bn[bp.end - 1] = ')'
        }
        return String(bn)
    }

    override fun toString() = this.name

    fun randomizeSeq() {
        val newSeq = StringBuffer(this.rna.length)
        val residues = listOf('A', 'U', 'G', 'C')
        val purines = listOf('A', 'G')
        val pyrimidines = listOf('U', 'C')
        newSeq.append((1..this.rna.length).map { residues.random() }.joinToString(separator = ""))
        this.secondaryInteractions.forEach {
            val r = residues.random()
            val paired = when (r) {
                'A' -> 'U'
                'U' -> purines.random()
                'G' -> pyrimidines.random()
                'C' -> 'G'
                else -> 'N'
            }
            newSeq.setCharAt(it.start - 1, r)
            newSeq.setCharAt(it.end - 1, paired)
        }
        this.pknots.forEach { pknot ->
            pknot.tertiaryInteractions.forEach {
                val r = residues.random()
                val paired = when (r) {
                    'A' -> 'U'
                    'U' -> purines.random()
                    'G' -> pyrimidines.random()
                    'C' -> 'G'
                    else -> 'N'
                }
                newSeq.setCharAt(it.start - 1, r)
                newSeq.setCharAt(it.end - 1, paired)
            }
        }
        this.rna.seq = newSeq.toString()
    }

}

fun toBlocks(positions: IntArray): MutableList<Block> {
    val blocks = arrayListOf<Block>()
    val sortedPositions = positions.sorted()
    var length = 0
    var i = 0
    var start = sortedPositions.first()

    while (i < sortedPositions.size - 1) {
        if (sortedPositions[i] + 1 == sortedPositions[i + 1]) {
            length += 1
        } else {
            blocks.add(Block(start, start + length))
            length = 0
            start = sortedPositions[i + 1]
        }
        i += 1
    }
    blocks.add(Block(start, start + length))
    return blocks
}

fun toBasePairs(bracketNotation: String): MutableList<BasePair> {
    val basePairs = arrayListOf<BasePair>()
    var pos = 0
    val firstStrands = mutableMapOf<Char, Pair<MutableList<Int>, MutableList<Edge>>>()

    ('A'..'Z').forEach {
        firstStrands.put(it, Pair(mutableListOf<Int>(), mutableListOf<Edge>()))
    }

    firstStrands.put('(', Pair(mutableListOf<Int>(), mutableListOf<Edge>()))
    firstStrands.put('{', Pair(mutableListOf<Int>(), mutableListOf<Edge>()))
    firstStrands.put('[', Pair(mutableListOf<Int>(), mutableListOf<Edge>()))
    firstStrands.put('<', Pair(mutableListOf<Int>(), mutableListOf<Edge>()))

    loop@ for (c in bracketNotation) {
        pos++
        when (c) {
            in 'A'..'Z' -> {
                firstStrands[c]!!.first.add(pos)
                firstStrands[c]!!.second.add(Edge.WC)
            }

            in listOf('(', '[', '{', '<') -> {
                firstStrands[c]!!.first.add(pos)
                firstStrands[c]!!.second.add(Edge.WC)
            }

            ')' -> {
                if (firstStrands['(']!!.first.size - 1 >= 0) {
                    val _lastPos = firstStrands['(']!!.first.removeAt(firstStrands['(']!!.first.size - 1)
                    val _location = Location(Location(_lastPos), Location(pos))
                    val _lastLeft = firstStrands['(']!!.second.removeAt(firstStrands['(']!!.second.size - 1)
                    basePairs.add(
                        BasePair(
                            location = _location,
                            edge5 = _lastLeft,
                            edge3 = Edge.WC
                        )
                    )
                }
            }
            '}' -> {
                if (firstStrands['{']!!.first.size - 1 >= 0) {
                    val _lastPos = firstStrands['{']!!.first.removeAt(firstStrands['{']!!.first.size - 1)
                    val _location = Location(Location(_lastPos), Location(pos))
                    val _lastLeft = firstStrands['{']!!.second.removeAt(firstStrands['{']!!.second.size - 1)
                    basePairs.add(
                        BasePair(
                            location = _location,
                            edge5 = _lastLeft,
                            edge3 = Edge.WC
                        )
                    )
                }
            }
            ']' -> {
                if (firstStrands['[']!!.first.size - 1 >= 0) {
                    val _lastPos = firstStrands['[']!!.first.removeAt(firstStrands['[']!!.first.size - 1)
                    val _location = Location(Location(_lastPos), Location(pos))
                    val _lastLeft = firstStrands['[']!!.second.removeAt(firstStrands['[']!!.second.size - 1)
                    basePairs.add(
                        BasePair(
                            location = _location,
                            edge5 = _lastLeft,
                            edge3 = Edge.WC
                        )
                    )
                }
            }
            '>' -> {
                if (firstStrands['<']!!.first.size - 1 >= 0) {
                    val _lastPos = firstStrands['<']!!.first.removeAt(firstStrands['<']!!.first.size - 1)
                    val _location = Location(Location(_lastPos), Location(pos))
                    val _lastLeft = firstStrands['<']!!.second.removeAt(firstStrands['<']!!.second.size - 1)
                    basePairs.add(
                        BasePair(
                            location = _location,
                            edge5 = _lastLeft,
                            edge3 = Edge.WC
                        )
                    )
                }
            }
            in 'a'..'z' -> {
                val upperChar = c.uppercaseChar()
                if (firstStrands[upperChar]!!.first.size - 1 >= 0) {
                    val _lastPos = firstStrands[upperChar]!!.first.removeAt(firstStrands[upperChar]!!.first.size - 1)
                    val _location = Location(Location(_lastPos), Location(pos))
                    val _lastLeft = firstStrands[upperChar]!!.second.removeAt(firstStrands[upperChar]!!.second.size - 1)
                    basePairs.add(
                        BasePair(
                            location = _location,
                            edge5 = _lastLeft,
                            edge3 = Edge.WC
                        )
                    )
                }
            }
            else -> continue@loop
        }
    }
    return basePairs
}

fun randomRNA(size: Int): RNA {
    val residues = listOf<Char>('A', 'U', 'G', 'C')
    val seq = (1..size)
        .map { _ -> kotlin.random.Random.nextInt(0, residues.size) }
        .map(residues::get)
        .joinToString("")
    return RNA("random rna", seq)
}

fun getSource(s: String): DataSource? {
    val tokens = s.split(":")
    return when (tokens.first()) {
        "db" -> when (tokens[1]) {
            "pdb" -> PDBSource(tokens.last())
            "rnacentral" -> RnaCentralSource(tokens.last())
            "rfam" -> RfamSource(tokens.last())
            else -> null
        }
        "local" -> when (tokens[1]) {
            "file" -> FileSource(s.split("local:file:").last())
            else -> null
        }
        else -> null
    }
}

interface DataSource {
    fun getId(): String?
}

class BracketNotation : DataSource {
    override fun getId(): String? {
        return null
    }

    override fun toString(): String {
        return "local:bn"
    }
}

abstract class DatabaseSource : DataSource

class PDBSource(val pdbId: String) : DatabaseSource() {
    override fun getId(): String {
        return this.pdbId
    }

    override fun toString(): String {
        return "db:pdb:${this.pdbId}"
    }
}

class RnaCentralSource(val rnacentralId: String) : DatabaseSource() {
    override fun getId(): String {
        return this.rnacentralId
    }

    override fun toString(): String {
        return "db:rnacentral:${this.rnacentralId}"
    }
}

class RfamSource(val rfamId: String) : DatabaseSource() {
    override fun getId(): String {
        return this.rfamId
    }

    override fun toString(): String {
        return "db:rfam:${this.rfamId}"
    }
}

class FileSource(val fileName: String) : DataSource {

    override fun getId(): String {
        return this.fileName
    }

    override fun toString(): String {
        return "local:file:${this.fileName}"
    }

}

abstract class ToolSource(val toolVersion: String) : DataSource {

    override fun getId(): String {
        return this.toolVersion
    }

}

enum class Edge {
    WC, Hoogsteen, Sugar, SingleHBond, Unknown;
}

enum class Orientation {
    cis, trans, Unknown
}

enum class JunctionType(val value: Int) {
    ApicalLoop(1),
    InnerLoop(2),
    ThreeWay(3),
    FourWay(4),
    FiveWay(5),
    SixWay(6),
    SevenWay(7),
    EightWay(8),
    NineWay(9),
    TenWay(10),
    ElevenWay(11),
    TwelveWay(12),
    ThirteenWay(13),
    FourteenWay(14),
    FifthteenWay(15),
    SixteenWay(16),
    Flower(17)
}

val modifiedNucleotides: MutableMap<String, String> = mutableMapOf<String, String>(
    "T" to "U",
    "PSU" to "U",
    "I" to "A",
    "+A" to "A",
    "+C" to "C",
    "+G" to "G",
    "+I" to "A",
    "+T" to "U",
    "+U" to "U",
    "PU" to "A",
    "YG" to "G",
    "1AP" to "G",
    "1MA" to "A",
    "1MG" to "G",
    "2DA" to "A",
    "2DT" to "U",
    "2MA" to "A",
    "2MG" to "G",
    "4SC" to "C",
    "4SU" to "U",
    "5IU" to "U",
    "5MC" to "C",
    "5MU" to "U",
    "5NC" to "C",
    "6MP" to "A",
    "7MG" to "G",
    "A23" to "A",
    "AD2" to "A",
    "AET" to "A",
    "AMD" to "A",
    "AMP" to "A",
    "APN" to "A",
    "ATP" to "A",
    "AZT" to "U",
    "CCC" to "C",
    "CMP" to "A",
    "CPN" to "C",
    "DAD" to "A",
    "DCT" to "C",
    "DDG" to "G",
    "DG3" to "G",
    "DHU" to "U",
    "DOC" to "C",
    "EDA" to "A",
    "G7M" to "G",
    "GDP" to "G",
    "GNP" to "G",
    "GPN" to "G",
    "GTP" to "G",
    "GUN" to "G",
    "H2U" to "U",
    "HPA" to "A",
    "IPN" to "U",
    "M2G" to "G",
    "MGT" to "G",
    "MIA" to "A",
    "OMC" to "C",
    "OMG" to "G",
    "OMU" to "U",
    "ONE" to "U",
    "P2U" to "U",
    "PGP" to "G",
    "PPU" to "A",
    "PRN" to "A",
    "PST" to "U",
    "QSI" to "A",
    "QUO" to "G",
    "RIA" to "A",
    "SAH" to "A",
    "SAM" to "A",
    "T23" to "U",
    "T6A" to "A",
    "TAF" to "U",
    "TLC" to "U",
    "TPN" to "U",
    "TSP" to "U",
    "TTP" to "U",
    "UCP" to "U",
    "VAA" to "A",
    "YYG" to "G",
    "70U" to "U",
    "12A" to "A",
    "2MU" to "U",
    "127" to "U",
    "125" to "U",
    "126" to "U",
    "MEP" to "U",
    "TLN" to "U",
    "ADP" to "A",
    "TTE" to "U",
    "PYO" to "U",
    "SUR" to "U",
    "PSD" to "A",
    "S4U" to "U",
    "CP1" to "C",
    "TP1" to "U",
    "NEA" to "A",
    "GCK" to "C",
    "CH" to "C",
    "EDC" to "G",
    "DFC" to "C",
    "DFG" to "G",
    "DRT" to "U",
    "2AR" to "A",
    "8OG" to "G",
    "IG" to "G",
    "IC" to "C",
    "IGU" to "G",
    "IMC" to "C",
    "GAO" to "G",
    "UAR" to "U",
    "CAR" to "C",
    "PPZ" to "A",
    "M1G" to "G",
    "ABR" to "A",
    "ABS" to "A",
    "S6G" to "G",
    "HEU" to "U",
    "P" to "G",
    "DNR" to "C",
    "MCY" to "C",
    "TCP" to "U",
    "LGP" to "G",
    "GSR" to "G",
    "E" to "A",
    "GSS" to "G",
    "THX" to "U",
    "6CT" to "U",
    "TEP" to "G",
    "GN7" to "G",
    "FAG" to "G",
    "PDU" to "U",
    "MA6" to "A",
    "UMP" to "U",
    "SC" to "C",
    "GS" to "G",
    "TS" to "U",
    "AS" to "A",
    "ATD" to "U",
    "T3P" to "U",
    "5AT" to "U",
    "MMT" to "U",
    "SRA" to "A",
    "6HG" to "G",
    "6HC" to "C",
    "6HT" to "U",
    "6HA" to "A",
    "55C" to "C",
    "U8U" to "U",
    "BRO" to "U",
    "BRU" to "U",
    "5IT" to "U",
    "ADI" to "A",
    "5CM" to "C",
    "IMP" to "G",
    "THM" to "U",
    "URI" to "U",
    "AMO" to "A",
    "FHU" to "U",
    "TSB" to "A",
    "CMR" to "C",
    "RMP" to "A",
    "SMP" to "A",
    "5HT" to "U",
    "RT" to "U",
    "MAD" to "A",
    "OXG" to "G",
    "UDP" to "U",
    "6MA" to "A",
    "5IC" to "C",
    "SPT" to "U",
    "TGP" to "G",
    "BLS" to "A",
    "64T" to "U",
    "CB2" to "C",
    "DCP" to "C",
    "ANG" to "G",
    "BRG" to "G",
    "Z" to "A",
    "AVC" to "A",
    "5CG" to "G",
    "UDP" to "U",
    "UMS" to "U",
    "BGM" to "G",
    "SMT" to "U",
    "DU" to "U",
    "CH1" to "C",
    "GH3" to "G",
    "GNG" to "G",
    "TFT" to "U",
    "U3H" to "U",
    "MRG" to "G",
    "ATM" to "U",
    "GOM" to "A",
    "UBB" to "U",
    "A66" to "A",
    "T66" to "U",
    "C66" to "C",
    "3ME" to "A",
    "A3P" to "A",
    "ANP" to "A",
    "FA2" to "A",
    "9DG" to "G",
    "GMU" to "U",
    "UTP" to "U",
    "5BU" to "U",
    "APC" to "A",
    "DI" to "A",
    "UR3" to "U",
    "3DA" to "A",
    "DDY" to "C",
    "TTD" to "U",
    "TFO" to "U",
    "TNV" to "U",
    "MTU" to "U",
    "6OG" to "G",
    "E1X" to "A",
    "FOX" to "A",
    "CTP" to "C",
    "D3T" to "U",
    "TPC" to "C",
    "7DA" to "A",
    "7GU" to "U",
    "2PR" to "A",
    "CBR" to "C",
    "I5C" to "C",
    "5FC" to "C",
    "GMS" to "G",
    "2BT" to "U",
    "8FG" to "G",
    "MNU" to "U",
    "AGS" to "A",
    "NMT" to "U",
    "NMS" to "U",
    "UPG" to "U",
    "G2P" to "G",
    "2NT" to "U",
    "EIT" to "U",
    "TFE" to "U",
    "P2T" to "U",
    "2AT" to "U",
    "2GT" to "U",
    "2OT" to "U",
    "BOE" to "U",
    "SFG" to "G",
    "CSL" to "A",
    "PPW" to "G",
    "IU" to "U",
    "D5M" to "A",
    "ZDU" to "U",
    "DGT" to "U",
    "UD5" to "U",
    "S4C" to "C",
    "DTP" to "A",
    "5AA" to "A",
    "2OP" to "A",
    "PO2" to "A",
    "DC" to "C",
    "DA" to "A",
    "LOF" to "A",
    "ACA" to "A",
    "BTN" to "A",
    "PAE" to "A",
    "SPS" to "A",
    "TSE" to "A",
    "A2M" to "A",
    "NCO" to "A",
    "A5M" to "C",
    "M5M" to "C",
    "S2M" to "U",
    "MSP" to "A",
    "P1P" to "A",
    "N6G" to "G",
    "MA7" to "A",
    "FE2" to "G",
    "AKG" to "G",
    "SIN" to "G",
    "PR5" to "G",
    "GOL" to "G",
    "XCY" to "G",
    "5HU" to "U",
    "CME" to "C",
    "EGL" to "G",
    "LC" to "C",
    "LHU" to "U",
    "LG" to "G",
    "PUY" to "U",
    "PO4" to "U",
    "PQ1" to "U",
    "ROB" to "U",
    "O2C" to "C",
    "C30" to "C",
    "C31" to "C",
    "C32" to "C",
    "C33" to "C",
    "C34" to "C",
    "C35" to "C",
    "C36" to "C",
    "C37" to "C",
    "C38" to "C",
    "C39" to "C",
    "C40" to "C",
    "C41" to "C",
    "C42" to "C",
    "C43" to "C",
    "C44" to "C",
    "C45" to "C",
    "C46" to "C",
    "C47" to "C",
    "C48" to "C",
    "C49" to "C",
    "C50" to "C",
    "A30" to "A",
    "A31" to "A",
    "A32" to "A",
    "A33" to "A",
    "A34" to "A",
    "A35" to "A",
    "A36" to "A",
    "A37" to "A",
    "A38" to "A",
    "A39" to "A",
    "A40" to "A",
    "A41" to "A",
    "A42" to "A",
    "A43" to "A",
    "A44" to "A",
    "A45" to "A",
    "A46" to "A",
    "A47" to "A",
    "A48" to "A",
    "A49" to "A",
    "A50" to "A",
    "G30" to "G",
    "G31" to "G",
    "G32" to "G",
    "G33" to "G",
    "G34" to "G",
    "G35" to "G",
    "G36" to "G",
    "G37" to "G",
    "G38" to "G",
    "G39" to "G",
    "G40" to "G",
    "G41" to "G",
    "G42" to "G",
    "G43" to "G",
    "G44" to "G",
    "G45" to "G",
    "G46" to "G",
    "G47" to "G",
    "G48" to "G",
    "G49" to "G",
    "G50" to "G",
    "T30" to "U",
    "T31" to "U",
    "T32" to "U",
    "T33" to "U",
    "T34" to "U",
    "T35" to "U",
    "T36" to "U",
    "T37" to "U",
    "T38" to "U",
    "T39" to "U",
    "T40" to "U",
    "T41" to "U",
    "T42" to "U",
    "T43" to "U",
    "T44" to "U",
    "T45" to "U",
    "T46" to "U",
    "T47" to "U",
    "T48" to "U",
    "T49" to "U",
    "T50" to "U",
    "U30" to "U",
    "U31" to "U",
    "U32" to "U",
    "U33" to "U",
    "U34" to "U",
    "U35" to "U",
    "U36" to "U",
    "U37" to "U",
    "U38" to "U",
    "U39" to "U",
    "U40" to "U",
    "U41" to "U",
    "U42" to "U",
    "U43" to "U",
    "U44" to "U",
    "U45" to "U",
    "U46" to "U",
    "U47" to "U",
    "U48" to "U",
    "U49" to "U",
    "U50" to "U",
    "UFP" to "U",
    "UFR" to "U",
    "UCL" to "U",
    "3DR" to "U",
    "CBV" to "C",
    "HFA" to "A",
    "MMA" to "A",
    "DCZ" to "C",
    "GNE" to "C",
    "A1P" to "A",
    "6IA" to "A",
    "CTG" to "G",
    "5FU" to "U",
    "2AD" to "A",
    "T2T" to "U",
    "XUG" to "G",
    "2ST" to "U",
    "5PY" to "U",
    "4PC" to "C",
    "US1" to "U",
    "M5C" to "C",
    "DG" to "G",
    "DA" to "A",
    "DT" to "U",
    "DC" to "C",
    "P5P" to "A",
    "FMU" to "U",
    "YMP" to "A",
    "RTP" to "G",
    "TM2" to "U",
    "SSU" to "U",
    "N5M" to "C",
    "N5C" to "C",
    "8AN" to "A",
    "3AT" to "A",
    "DM1" to "X",
    "DM2" to "X",
    "DM5" to "X",
    "B8N" to "X",
    "4AC" to "C",
    "6MZ" to "A",
    "PYY" to "X"
)
