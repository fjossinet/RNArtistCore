#/bin/sh

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

download_file() {
   if command_exists curl; then
      curl "$@"
   elif command_exists wget; then
      wget -O - "$@"
   fi
}

main() {

   setup_color

   printf '\033[32m'

   cat <<'EOF'


██████╗░███╗░░██╗░█████╗░██████╗░████████╗██╗░██████╗████████╗░█████╗░░█████╗░██████╗░███████╗
██╔══██╗████╗░██║██╔══██╗██╔══██╗╚══██╔══╝██║██╔════╝╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗██╔════╝
██████╔╝██╔██╗██║███████║██████╔╝░░░██║░░░██║╚█████╗░░░░██║░░░██║░░╚═╝██║░░██║██████╔╝█████╗░░
██╔══██╗██║╚████║██╔══██║██╔══██╗░░░██║░░░██║░╚═══██╗░░░██║░░░██║░░██╗██║░░██║██╔══██╗██╔══╝░░
██║░░██║██║░╚███║██║░░██║██║░░██║░░░██║░░░██║██████╔╝░░░██║░░░╚█████╔╝╚█████╔╝██║░░██║███████╗
╚═╝░░╚═╝╚═╝░░╚══╝╚═╝░░╚═╝╚═╝░░╚═╝░░░╚═╝░░░╚═╝╚═════╝░░░░╚═╝░░░░╚════╝░░╚════╝░╚═╝░░╚═╝╚══════╝

EOF

   check_docker

   if command_exists curl; then
      message "curl found"
   elif command_exists wget; then
      message "wget found"
   else
     error "curl or wget commands not found"
     exit 1
   fi

   printf "${YELLOW}Choose a name and a location for your project (like $HOME/project_1)${RESET}  "
   read opt
   DIR="${opt/#~/$HOME}"
   DIR=$(echo "$(cd "$(dirname "$DIR")"; pwd)/$(basename "$DIR")")

   if [ -d $DIR ]
   then
     error "Directory $DIR exists."
     exit 1
   fi

   printf "${YELLOW}Create directory $DIR? [Y/n]${RESET}  "
   read opt
   case $opt in
      n*|N*)  error "Aborted!"; exit 1;;
   esac

   mkdir $DIR

   if [ $? -ne 0 ] ; then
      error "Cannot create directory $DIR."
      exit 1
   fi

   if [ -d $DIR ]
   then
      step_to_do "Downloading script rnartistcore_docker.sh"
      download_file "https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/rnartistcore_docker.sh" > $DIR/rnartistcore_docker.sh
      chmod u+x $DIR/rnartistcore_docker.sh
      step_to_do "Downloading notebook rnartistcore_demo.ipynb"
      download_file "https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/rnartistcore_demo.ipynb" > $DIR/rnartistcore_demo.ipynb
      step_to_do "Downloading sample script"
      download_file "https://raw.githubusercontent.com/fjossinet/RNArtistCore/master/scripts/example.kts" > $DIR/example.kts
      step_to_do "Downloading data"
      download_file "https://files.rcsb.org/download/1GID.pdb" > $DIR/1gid.pdb
   fi

   printf '\033[32m'

   cat <<'EOF'

Installation Successfull!

Your first steps:
-----------------

- go into your project directory
- type: ./rnartistcore_docker.sh $PWD/example.kts
- This will produce two SVG files. One from an RNA 2D described from scratch in the sample script (bracket_notation_A.svg) and one from the 3D structure 1GID (1gid_B.svg)
- to use the sample notebook, install Jupyter and from your project directory type: jupyter notebook .

To stay in touch ---> https://twitter.com/rnartist_app

Enjoy!

EOF

if command_exists jupyter; then
    message "Jupyter found"
    printf "${YELLOW}Do you want to launch Jupyter? [y/N]${RESET}  "
    read opt
     case $opt in
        y*|Y*)  cd $DIR ; jupyter notebook .;;
     esac
 fi

}

main "$@"