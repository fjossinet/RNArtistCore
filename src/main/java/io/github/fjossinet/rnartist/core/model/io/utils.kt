package io.github.fjossinet.rnartist.core.model.io

import java.io.*

fun getTmpDirectory(): File? {
    val tmpDir = File(StringBuffer(getUserDir().absolutePath).append(System.getProperty("file.separator")).append("tmp").toString())
    if (!tmpDir.exists()) tmpDir.mkdir()
    return tmpDir
}

@Throws(IOException::class)
fun createTemporaryFile(fileName: String): File? {
    val f = File(getTmpDirectory(), fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
}

@Throws(IOException::class)
fun createTemporaryFile(dir: File?, fileName: String): File? {
    val f = File(dir, fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
}


@Throws(IOException::class)
fun getUserDir(): File {
    val f = File(StringBuffer(System.getProperty("user.home")).append(System.getProperty("file.separator")).append(".rnartist").toString())
    if (!f.exists()) {
        f.mkdir()
        File(f, "tmp").mkdir()
    }
    return f
}

@Throws(IOException::class)
fun copyFile(source: File, target: File) {
    if (source.isDirectory) {
        if (!target.exists()) {
            target.mkdir()
        }
        val children = source.list()
        for (i in children.indices) {
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