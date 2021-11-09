#!/bin/bash

setup_color() {
  RED=$(printf '\033[31m\xe2\x9d\x8c')
  GREEN=$(printf '\033[32m\xe2\x9c\x85')
  YELLOW=$(printf '\033[33m\xe2\x9d\x97')
  BLUE=$(printf '\033[34m')
  BOLD=$(printf '\033[1m')
  RESET=$(printf '\033[m')
}

command_exists() {
	command -v "$@" >/dev/null 2>&1
}

error() {
  echo ${RED}" $@"${RESET} >&2
}

message() {
  echo ${GREEN}" $@"${RESET} >&2
}

step_to_do() {
  echo ${YELLOW}"$@"${RESET} >&2
}

check_docker() {
  command_exists docker || {
    error "Docker not installed or not running"
    exit 1
  }

  message "Docker installed."

  result=$(docker ps)

  if [[ -n "$result" ]]; then
    message "Docker daemon is running."
  else
    error "It seems that the docker daemon is not running."
    exit 1
  fi

  result=$(docker images -q fjossinet/rnartistcore)

  if [[ -n "$result" ]]; then
     message "Container fjossinet/rnartistcore installed."
  else
    step_to_do "Container fjossinet/rnartistcore not found. Installation launched..."
    docker pull fjossinet/rnartistcore
    result=$(docker images -q fjossinet/rnartistcore)
    if [[ -n "$result" ]]; then
       message "Container fjossinet/rnartistcore installed."
    else
      error "It seems that i was not able to install the container fjossinet/rnartistcore."
      exit 1
    fi
  fi

}

main() {

  setup_color

  if [ $# -eq 0 ]; then

      printf '\033[32m'

      cat <<'EOF'
This script parses and executes plotting instructions for one or several RNA secondary structures. These instructions have to be stored in a kts file. This script can also update your local copy of the plotting engine RNArtistCore.

Usage: plot_2ds.sh [options] plotting_instructions_file.kts
  Options:
    -u  update your local copy of RNArtistCore

EOF
      exit 1
  fi

  check_docker

  case $1 in
        -u)
          printf "${YELLOW}Ready to update your local copy of RNArtistCore? [Y/n]${RESET}  "
             read opt
             case $opt in
                n*|N*)  error "Aborted!"; exit 1;;
             esac

          step_to_do "Before to update, some cleaning..."

          for CONTAINER_ID in $(docker ps -a  | grep "fjossinet/rnartistcore" | awk '{printf("%s\n", $1)}')
          do
            step_to_do "stop & remove docker container $CONTAINER_ID"
            docker stop $CONTAINER_ID
            docker rm $CONTAINER_ID
          done

          for IMAGE_ID in $(docker images | grep "fjossinet/rnartistcore" | awk '{printf("%s\n", $3)}')
          do
            step_to_do "remove docker image $IMAGE_ID"
            docker rmi $IMAGE_ID ;
          done

          docker pull fjossinet/rnartistcore ;
          exit 0;;
     esac

  docker run -v "$PWD:/project" fjossinet/rnartistcore java -jar target/rnartistcore-0.2.8-SNAPSHOT-jar-with-dependencies.jar /project/$1
}

main "$@"