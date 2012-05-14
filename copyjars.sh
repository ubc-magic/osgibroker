cp fragment.driver.jdbc/target/*.jar broker
cp osgibroker.api/target/*.jar broker
cp osgibroker.impl/target/*.jar broker
mkdir broker/sms
cp osgibroker.sms/target/*.jar broker/sms/
cp osgibroker.sms.installer/target/*.jar broker
cp osgibroker.storage/target/*.jar broker
cp subscriber.servlet/target/*.jar broker
cp subscriber.tcp/target/*.jar broker
