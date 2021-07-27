#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJ_DIR=$SCRIPT_DIR/..

source $SCRIPT_DIR/others/dummy-info.sh

# --------------------------------------------------------------------------------

BUILD_PROJECT="false"
KAFKA_SERVERS="localhost:9092"

# --------------------------------------------------------------------------------

function build() {
    mvn clean package docker:build -DskipTests
    exit 0;
}

function helpFunction() {
    echo 'This utility pushes csv data to a kafka topic, e.g. use command : "kafka-stream -f <csv file> -t topic-name -d 1000 -s localhost:9092" to push csv data to topic with each record delay of 1 second using the kafka server at localhost:9092'
    echo '--'
    echo 'Use [-b] [Optional] Build the project Docker Image'
    echo 'Use [-d] [Optional] Delay in milliseconds'
    echo 'Use [-f] [Required] The CSV file path argument'
    echo 'Use [-h] option to see the help'
    echo 'Use [-s] [Optional] Kafka Servers, if not given, it would fallback to localhost:9092'
    echo 'Use [-t] [Required] The Kafka Topic Name, used only while populating csv data runner, and ignored for other runners (would be conditional optional in future)'
    exit 0;
}


while getopts "bd:f:hs:t:" opt
do
   case "$opt" in
      b ) BUILD_PROJECT="true" ;;
      d ) DELAY_IN_MS=${OPTARG};;
      f ) LOCAL_FILE_PATH=${OPTARG};;
      h ) helpFunction && exit 0;; # Usage
      s ) KAFKA_SERVERS=${OPTARG};;
      t ) TOPIC_NAME=${OPTARG};;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done



# --------------------------------------------------------------------------------

function validateDetails() {
    if [[ -z ${LOCAL_FILE_PATH} ]]; then
        echo 'Argument Local CSV File Path is not passed..'
        helpFunction
        exit 0;
    fi

    if [[ -z ${TOPIC_NAME} ]]; then
        echo 'Argument Topic Name is not passed..'
        helpFunction
        exit 0;
    fi

    if [[ -z ${DELAY_IN_MS} ]]; then
        echo 'Argument Delay is not passed, taking default value as 1000 ms..'
        DELAY_IN_MS=1000
    fi

}

echo "SCRIPT_DIR={${SCRIPT_DIR}}, PROJ_DIR={${PROJ_DIR}}"
echo "TOPIC {${TOPIC_NAME}}, DELAY_IN_MS {${DELAY_IN_MS}}, LOCAL_FILE_PATH {${LOCAL_FILE_PATH}}, KAFKA_SERVERS {${KAFKA_SERVERS}}....."


cd $PROJ_DIR

if [[ "$BUILD_PROJECT" == "true" ]]; then
    build
fi

validateDetails

# --------------------------------------------------------------------------------

docker run -v ${LOCAL_FILE_PATH}:/data.csv \
    -e RUNNER="populate-csv-data" \
    -e LOCAL_FILE_PATH=/data.csv \
    -e DELAY_IN_MS=${DELAY_IN_MS} \
    -e TOPIC_NAME=${TOPIC_NAME} \
    -e KAFKA_SERVERS=${KAFKA_SERVERS} \
    -e COUNT_STREAM_APP_ID=${COUNT_STREAM_APP_ID} -e COUNT_STREAM_TOPIC_DEP=${COUNT_STREAM_TOPIC_DEP} -e COUNT_STREAM_TOPIC_DEP_COUNT=${COUNT_STREAM_TOPIC_DEP_COUNT} -e COUNT_STREAM_TOPIC_EMPLOYEE=${COUNT_STREAM_TOPIC_EMPLOYEE} -e COUNT_STREAM_TOPIC_EMPLOYEE_COUNT=${COUNT_STREAM_TOPIC_EMPLOYEE_COUNT} \
    kafkastreamproject:v1

