#!/bin/sh

ARTIFACT=xsdwalker-1.0
JAR=$ARTIFACT.jar

java -cp target/$JAR XSDWalker "$@"

# eof
