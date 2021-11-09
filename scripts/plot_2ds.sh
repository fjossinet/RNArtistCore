#!/bin/bash

docker run -v "$PWD:/project" fjossinet/rnartistcore java -jar target/rnartistcore-0.2.8-SNAPSHOT-jar-with-dependencies.jar /project/$1