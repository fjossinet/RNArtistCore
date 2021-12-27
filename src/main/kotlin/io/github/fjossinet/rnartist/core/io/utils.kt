package io.github.fjossinet.rnartist.core.io

import java.io.*
import java.io.InputStream
import java.io.OutputStream

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

@Throws(IOException::class)
fun createTemporaryFile(dir: File?, fileName: String): File {
    val f = File(dir, fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
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

/*fun sendFile(out: OutputStream, name: String, `in`: InputStream, fileName: String) {
    val o = """
        Content-Disposition: form-data; name="${
        URLEncoder.encode(
            name,
            "UTF-8"
        )
    }"; filename="${URLEncoder.encode(fileName, "UTF-8")}"
        """.trimIndent()

    out.write(URLEncoder.encode(o,"UTF-8").toByteArray())
    val buffer = ByteArray(2048)
    var n = 0
    while (n >= 0) {
        out.write(buffer, 0, n)
        n = `in`.read(buffer)
    }
    out.write(URLEncoder.encode("\r\n","UTF-8").toByteArray())
}

fun sendField(out: OutputStream, name: String, field: String) {
    val o = """
        Content-Disposition: form-data; name="${URLEncoder.encode(name, "UTF-8")}"
        """.trimIndent()
    out.write(URLEncoder.encode(o,"UTF-8").toByteArray())
    out.write(URLEncoder.encode(field, "UTF-8").toByteArray())
    out.write(URLEncoder.encode("\r\n","UTF-8").toByteArray())
}*/