#!/bin/bash
DIR=./helidon-snapshot

rm -rf ${DIR}
mkdir ${DIR}
cd ${DIR}
git clone -b main --single-branch git@github.com:helidon-io/helidon.git
cd helidon

mvn install -T4 -DskipTests -Dmaven.javadoc.skip=true