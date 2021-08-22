

`git clone https://github.com/skshukla/KafkaStreamProject.git`

`cd KafkaStreamProject`

`alias .ks=$(pwd)/scripts/run.sh`

###### To Expose the KeyValue Store data as web end point, don't forget to checkout the branch
`git checkout dev-keystore-as-web-endpoint-example`

###### Run the below commad (Only first time required)

`.ks -b`

###### Run below command to push the csv file to your kafka broker/topic
`.ks -t mytopic -d 2000 -f $(pwd)/src/main/data/company_data/department.csv -s localhost:9092`



