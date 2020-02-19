#!/bin/sh
sudo service mongod start
cd /home/ec2-user/my-day-backend
git checkout master
git pull
./gradlew clean build
./gradlew bootRun