FROM gcc:latest
MAINTAINER Fabrice Jossinet (fjossinet@gmail.com)
RUN apt-get update && apt-get install -y git wget build-essential default-jdk maven

#rnaview
RUN wget -qO RNAVIEW.tar.gz "http://ndbserver.rutgers.edu/ndbmodule/services/download/RNAVIEW.tar.gz"
RUN tar -xzvf RNAVIEW.tar.gz
WORKDIR RNAVIEW/
RUN make && make clean
ENV RNAVIEW /RNAVIEW

ENV PATH /RNAVIEW/bin:$PATH

WORKDIR /

#rnartistcore
RUN git clone https://github.com/fjossinet/RNArtistCore.git
WORKDIR RNArtistCore/
RUN git pull
RUN mvn clean package

