#!/bin/bash

# Hard-coded script to test the call graph generation for the library-application project

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.MainCallGraphGenerator" -Dexec.args="/Users/yoran/dev/library-application/target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\" cha"

echo "✅ Done!"