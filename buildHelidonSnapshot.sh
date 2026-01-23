#!/bin/bash
DIR=./helidon-snapshot

rm -rf ${DIR}
mkdir ${DIR}
cd ${DIR}
git clone -b kec/agentic-poc --single-branch git@github.com:danielkec/helidon.git
cd helidon
mvn install -T4 -DskipTests -Dmaven.javadoc.skip=true

printf "app:
 docs-path:
   se: $(pwd)/docs/src/main/asciidoc/se
   mp: $(pwd)/docs/src/main/asciidoc/mp
 inclusions: \"*.adoc\"";