#!/bin/sh

# This script should only be run once on a new EC2
# Downloads things and sets up systemctl

sudo su

sudo yum update -y

sudo yum install -y lsof

sudo yum install -y git

git clone https://github.com/yaylinda/my-day-backend.git

sudo sh -c "echo '[mongodb-org-4.0]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2013.03/mongodb-org/4.0/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-4.0.asc' > /etc/yum.repos.d/mongodb-org-4.0.repo"

sudo yum install -y mongodb-org

sudo yum install -y java-1.8.0-openjdk-devel

sudo chmod u+x /home/ec2-user/my-day-backend/scripts/start-app.service

sudo chmod u+x /home/ec2-user/my-day-backend/scripts/start-app.sh

sudo cp /home/ec2-user/my-day-backend/scripts/start-app.service /etc/systemd/system/start-app.service

sudo systemctl enable start-app

sudo systemctl start start-app