rm -R --force felix-cache
echo
echo "Running OSGiBroker"
echo "Deleting cache" & EPID=$!
wait $EPID
java -Dsmslib.serial.polling -Dfelix.config.properties=file:config.properties -jar felix/bin/felix.jar
