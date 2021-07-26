#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJ_DIR=$SCRIPT_DIR/..

# --------------------------------------------------------------------------------

BUILD_PROJECT="false"
#LOCAL_FILE_PATH=/Users/sachin/tmp/t5/sample.csv
#DELAY_IN_MS=1000
#TOPIC_NAME=t-321

# --------------------------------------------------------------------------------

function helpFunction() {
    echo 'Use [-b] [Optional] Build the project Docker Image'
    echo 'Use [-d] [Optional] Delay in milliseconds'
    echo 'Use [-f] [Required] The CSV file path argument'
    echo 'Use [-h] option to see the help'
    echo 'Use [-t] [Required] The Kafka Topic Name'
    exit 0;
}


while getopts "bd:f:ht:" opt
do
   case "$opt" in
      b ) BUILD_PROJECT="true" ;;
      d ) DELAY_IN_MS=${OPTARG};;
      f ) LOCAL_FILE_PATH=${OPTARG};;
      h ) helpFunction && exit 0;; # Usage
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

echo "TOPIC {${TOPIC_NAME}}, DELAY_IN_MS {${DELAY_IN_MS}}, LOCAL_FILE_PATH {${LOCAL_FILE_PATH}}....."

validateDetails


cd $PROJ_DIR

if [[ "$BUILD_PROJECT" == "true" ]]; then
    mvn clean package docker:build -DskipTests
fi




# --------------------------------------------------------------------------------

docker run -v ${LOCAL_FILE_PATH}:/data.csv \
    -e LOCAL_FILE_PATH=/data.csv \
    -e DELAY_IN_MS=${DELAY_IN_MS} \
    -e TOPIC_NAME=${TOPIC_NAME} \
    kafkastreamproject:v1