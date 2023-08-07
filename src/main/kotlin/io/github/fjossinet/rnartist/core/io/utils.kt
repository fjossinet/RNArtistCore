package io.github.fjossinet.rnartist.core.io

import java.awt.Color
import java.io.*
import java.util.*

fun getTmpDirectory(): File {
    return File(getUserDir(), "tmp")
}

@Throws(IOException::class)
fun createTemporaryFile(fileName: String): File {
    val f = File(getTmpDirectory(), fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
}

fun randomColor():Color {
    val random = Random()
    val red: Int = random.nextInt(256)
    val green: Int = random.nextInt(256)
    val blue: Int = random.nextInt(256)
    return Color(red, green, blue)
}


@Throws(IOException::class)
fun getUserDir(): File {
    val root = File(StringBuffer(System.getProperty("user.home")).append(System.getProperty("file.separator")).append(".rnartist").toString())
    if (!root.exists())
        root.mkdir()
    val tmp = File(root, "tmp")
    if (!tmp.exists())
        tmp.mkdir()
    val db = File(root, "db")
    if (!db.exists())
        db.mkdir()
    val images = File(db, "images")
    if (!images.exists())
        images.mkdir()
    val userImages = File(images, "user")
    if (!userImages.exists())
        userImages.mkdir()
    val pdbImages = File(images, "pdb")
    if (!pdbImages.exists())
        pdbImages.mkdir()
    val chimera_sessions = File(db, "chimera_sessions")
    if (!chimera_sessions.exists())
        chimera_sessions.mkdir()
    return root
}

@Throws(IOException::class)
fun copyFile(source: File, target: File) {
    if (source.isDirectory) {
        if (!target.exists()) {
            target.mkdir()
        }
        val children = source.list()
        children?.indices?.forEach { i ->
            copyFile(File(source, children[i]), File(target, children[i]))
        }
    } else {
        val `in`: InputStream = FileInputStream(source)
        val out: OutputStream = FileOutputStream(target)

        // Copy the bits from instream to outstream
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }
}