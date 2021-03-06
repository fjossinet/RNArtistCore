#!/bin/bash

BASEDIR=$(dirname "$1")
FILENAME=$(basename "$1")

docker run -v "$BASEDIR:/output" fjossinet/rnartistcore java -jar target/rnartistcore-0.2.7-SNAPSHOT-jar-with-dependencies.jar /output/$FILENAME