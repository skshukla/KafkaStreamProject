git clone https://github.com/skshukla/KafkaStreamProject.git
cd KafkaStreamProject
alias .ks=`pwd`/scripts/run.sh

.ks -t mytopic -d 2000 -f <PATH OF CSV FILE YOU WANT TO PUSH TO KAFKA> -s localhost:9092
