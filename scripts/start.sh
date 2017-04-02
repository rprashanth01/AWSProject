#!/bin/bash
javac -cp com.cloud_prml.jar ScriptRun.java
java -cp .:com.cloud_prml.jar ScriptRun 1
echo "You will ssh into the web-tier in 30 seconds. 
      ###########################
      Please run the run_script.sh once you are there to start the application
      ###########################"
sleep 30
chmod 400 FirstInstance.pem
ssh -i FirstInstance.pem ec2-user@52.42.84.201
