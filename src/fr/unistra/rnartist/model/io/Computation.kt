package fr.unistra.rnartist.model.io

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.net.URLEncoder

abstract class Computation {

    fun isDockerInstalled():Boolean {
        try {
            val pb  = ProcessBuilder("which", "docker");
            val p = pb.start();
            val result = InputStreamReader(p.getInputStream()).buffered().use(BufferedReader::readText);
            return result.trim().matches(Regex("^.+docker$"));
        } catch (e:Exception ) {
            return false;
        }
    }

    fun isAssemble2DockerImageInstalled():Boolean {
        try {
            val pb  = ProcessBuilder("docker", "images");
            val p = pb.start();
            val result = InputStreamReader(p.getInputStream()).buffered().use(BufferedReader::readText);
            return "fjossinet/assemble2".toRegex().find(result.trim()) != null;
        } catch (e:Exception ) {
            return false;
        }
    }
}