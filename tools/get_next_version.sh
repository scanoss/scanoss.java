#!/bin/bash
###
# SPDX-License-Identifier: MIT
#
#   Copyright (c) 2024, SCANOSS
#
#   Permission is hereby granted, free of charge, to any person obtaining a copy
#   of this software and associated documentation files (the "Software"), to deal
#   in the Software without restriction, including without limitation the rights
#   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#   copies of the Software, and to permit persons to whom the Software is
#   furnished to do so, subject to the following conditions:
#
#   The above copyright notice and this permission notice shall be included in
#   all copies or substantial portions of the Software.
#
#   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
#   THE SOFTWARE.
###
#
# Get the defined package version and compare to the latest tag. Echo the new tag if it doesn't already exist.
#

# Get current directory
export dir=$(dirname "$0")
if [ "$dir" = "" ] ; then
  export dir=.
fi


# Get latest git tagged version
version=$(git describe --tags --abbrev=0)
if [[ -z "$version" ]] ; then
  version=$(git describe --tags "$(git rev-list --tags --max-count=1)")
fi
if [[ -z "$version" ]] ; then
  echo "Error: Failed to determine a valid version number" >&2
  exit 1
fi

# Get latest SCANOSS Java version
scanoss_java_version=$(cd "$dir"/.. && ./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
if [ -z "$scanoss_java_version" ]; then
    echo "Error: version not found" >&2
    exit 1
fi


semver_scanoss_java="v$scanoss_java_version"

echo "Latest Tag: $version, SCANOSS Java Version: $semver_scanoss_java" >&2

# If the two versions are the same abort, as we don't want to apply the same tag again
if [[ "$version" == "$semver_scanoss_java" ]] ; then
  echo "Latest tag and SCANOSS Java version are the same: $version" >&2
  exit 1
fi
echo "$semver_scanoss_java"
exit 0