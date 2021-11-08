#!/bin/bash

docker run -v "$PWD:/docker" fjossinet/rnartistcore java -jar target/rnartistcore-0.2.8-SNAPSHOT-jar-with-dependencies.jar /docker/$1