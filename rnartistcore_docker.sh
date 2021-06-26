#!/bin/bash

BASEDIR=$(dirname "$1")
FILENAME=$(basename "$1")

docker run -v "$BASEDIR:/docker" fjossinet/rnartistcore java -jar target/rnartistcore-0.2.8-SNAPSHOT-jar-with-dependencies.jar /docker/$FILENAME