#!/bin/bash

##########################################
#
# Launch the SCANOSS CLI jar from this project area
# The package has to be built before it can run
#
##########################################

b_dir=$(dirname "$0") # script location
if [ "$b_dir" = "" ]; then
  b_dir=.
fi
export b_dir

# Search in the 'target' directory for the CLI jar file to execute
jar_file=$(find "$b_dir/target" -name "scanoss*jar-with-dependencies.jar" -print | sort | tail -1)
if [ "$jar_file" = "" ] ; then
  # Nothing there, so search the full subfolder tree
  jar_file=$(find "$b_dir" -name "scanoss*jar-with-dependencies.jar" -print | sort | tail -1)
  if [ "$jar_file" = "" ] ; then
    echo "ERROR: Failed to find SCANOSS jar file to run."
    exit 1
  fi
fi
export jar_file
exec java -jar "$jar_file" "$@"
