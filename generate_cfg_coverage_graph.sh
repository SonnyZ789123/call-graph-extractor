#!/bin/bash

set -e

mvn -q -DskipTests=true package
mvn exec:java -Dexec.mainClass="com.kuleuven.coverage.GenerateCFGCoverageGraph" -Dexec.args="../../jdart-examples/out/production/jdart-examples out/cfg_block_mapping.json ../data/coverage.out"

echo "✅ Done!"

echo "--- GENERATE SVG FROM CLEANED DOT FILE ---"

dot -Tsvg out/cfg_coverage0.dot -o out/cfg_coverage0.svg

open out/cfg_coverage0.svg

echo "✅ Done!"