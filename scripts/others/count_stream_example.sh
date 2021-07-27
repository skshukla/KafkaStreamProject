#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJ_DIR=$SCRIPT_DIR/../..

# --------------------------------------------------------------------------------

BUILD_PROJECT="false"
KAFKA_SERVERS="vm-minikube:30092,vm-minikube:30093,vm-minikube:30094"

COUNT_STREAM_APP_ID="count-stream-app-xyz-0001"
COUNT_STREAM_TOPIC_DEP="dept-0001"
COUNT_STREAM_TOPIC_DEP_COUNT="department-count"

# --------------------------------------------------------------------------------

function helpFunction() {
    echo 'Use [-b] [Optional] Build the project Docker Image'
    echo 'Use [-h] option to see the help'
    echo 'Use [-s] [Optional] Kafka Servers, if not given, it would fallback to localhost:9092'
    exit 0;
}


while getopts "bhs:" opt
do
   case "$opt" in
      b ) BUILD_PROJECT="true" ;;
      h ) helpFunction && exit 0;; # Usage
      s ) KAFKA_SERVERS=${OPTARG};;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done


# --------------------------------------------------------------------------------

function validateDetails() {
    echo 'There is no such validation required for this..'
}

echo "COUNT_STREAM_APP_ID {${COUNT_STREAM_APP_ID}}, COUNT_STREAM_TOPIC_DEP {${COUNT_STREAM_TOPIC_DEP}}, COUNT_STREAM_TOPIC_DEP_COUNT {${COUNT_STREAM_TOPIC_DEP_COUNT}}, KAFKA_SERVERS {${KAFKA_SERVERS}}....."


cd $PROJ_DIR

if [[ "$BUILD_PROJECT" == "true" ]]; then
    mvn clean package docker:build -DskipTests
fi

#validateDetails

# --------------------------------------------------------------------------------

DUMMY="dummy"

docker run \
    -e RUNNER="count-stream" \
    -e KAFKA_SERVERS=${KAFKA_SERVERS} \
    -e COUNT_STREAM_APP_ID=${COUNT_STREAM_APP_ID} \
    -e COUNT_STREAM_TOPIC_DEP=${COUNT_STREAM_TOPIC_DEP} \
    -e COUNT_STREAM_TOPIC_DEP_COUNT=${COUNT_STREAM_TOPIC_DEP_COUNT} \
    -e LOCAL_FILE_PATH=${DUMMY} -e DELAY_IN_MS=${DUMMY} -e TOPIC_NAME=${DUMMY} \
    kafkastreamproject:v1