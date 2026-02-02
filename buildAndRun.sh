#!/bin/bash
mvn clean package
# Using native accessed ONNX model for embedding
java --sun-misc-unsafe-memory-access=allow \
--enable-native-access=ALL-UNNAMED \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.base/java.io=ALL-UNNAMED \
-Ddeclarative.ignore-incubating=true \
-jar ./target/*.jar