#!/bin/bash
DIR=./helidon-snapshot

rm -rf ${DIR}
mkdir ${DIR}
cd ${DIR}
git clone -b kec/agentic-poc --single-branch git@github.com:danielkec/helidon.git
cd helidon

mvn install -T4 -DskipTests -Dmaven.javadoc.skip=true
# Trigger config docs generation ...
cd docs && mvn package && cd ..

printf "app:
 helidon-repo-path: $(pwd)
 inclusions: \"*.adoc\"";