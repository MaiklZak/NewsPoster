#!/bin/bash
printf build maven project
mvn clean install
printf run project
java -jar target/newsposter-1.0.jar