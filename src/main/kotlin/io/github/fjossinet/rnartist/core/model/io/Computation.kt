package io.github.fjossinet.rnartist.core.model.io

import java.io.BufferedReader
import java.io.InputStreamReader

abstract class Computation {

    fun isDockerInstalled():Boolean {
        return try {
            val pb  = ProcessBuilder("which", "docker");
            val p = pb.start();
            val result = InputStreamReader(p.getInputStream()).buffered().use(BufferedReader::readText);
            result.trim().matches(Regex("^.+docker$"));
        } catch (e:Exception ) {
            false;
        }
    }

    fun isAssemble2DockerImageInstalled():Boolean {
        return try {
            val pb  = ProcessBuilder("docker", "images");
            val p = pb.start();
            val result = InputStreamReader(p.getInputStream()).buffered().use(BufferedReader::readText);
            "fjossinet/assemble2".toRegex().find(result.trim()) != null;
        } catch (e:Exception ) {
            false;
        }
    }
}