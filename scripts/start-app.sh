#!/bin/sh
sudo service mongod start
cd /home/ec2-user/my-day-backend
git checkout dev
git pull
./gradlew spring-boot:run