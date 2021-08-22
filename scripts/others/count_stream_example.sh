#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJ_DIR=$SCRIPT_DIR/../..
source $SCRIPT_DIR/dummy-info.sh
# --------------------------------------------------------------------------------

BUILD_PROJECT="false"
KAFKA_SERVERS="vm-minikube:30092,vm-minikube:30093,vm-minikube:30094"

COUNT_STREAM_APP_ID="count-stream-app-xyz-0012"
COUNT_STREAM_TOPIC_DEP="dept-0003"
COUNT_STREAM_TOPIC_EMPLOYEE="emp-2-012"
COUNT_STREAM_STORE_NAME="my-store"
COUNT_STREAM_TOPIC_EMPLOYEE_COUNT="emp-2-count-012"
COUNT_STREAM_TOPIC_DEP_COUNT="department-count"

# --------------------------------------------------------------------------------

function build() {
    mvn clean package docker:build -DskipTests
    exit 0;
}

function helpFunction() {
    echo 'Use [-b] [Optional] Build the project Docker Image'
    echo 'Use [-h] option to see the help'
    echo 'Use [-s] [Optional] Kafka Servers, if not given, it would fallback to localhost:9092'
    echo 'Use curl command to read from Store: curl -w '\n'  http://localhost:8080/read-store/24'
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
    build
fi

#validateDetails

# --------------------------------------------------------------------------------

docker run \
    -e RUNNER="count-stream" \
    -e KAFKA_SERVERS=${KAFKA_SERVERS} \
    -e COUNT_STREAM_APP_ID=${COUNT_STREAM_APP_ID} \
    -e COUNT_STREAM_TOPIC_DEP=${COUNT_STREAM_TOPIC_DEP} \
    -e COUNT_STREAM_TOPIC_EMPLOYEE=${COUNT_STREAM_TOPIC_EMPLOYEE} \
    -e COUNT_STREAM_STORE_NAME=${COUNT_STREAM_STORE_NAME} \
    -e COUNT_STREAM_TOPIC_EMPLOYEE_COUNT=${COUNT_STREAM_TOPIC_EMPLOYEE_COUNT} \
    -e COUNT_STREAM_TOPIC_DEP_COUNT=${COUNT_STREAM_TOPIC_DEP_COUNT} \
    -e LOCAL_FILE_PATH=${LOCAL_FILE_PATH} -e DELAY_IN_MS=${DELAY_IN_MS} -e TOPIC_NAME=${TOPIC_NAME} \
    kafkastreamproject:v1