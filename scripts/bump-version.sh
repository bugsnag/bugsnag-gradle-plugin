#!/bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$1" != "" ]]; then
  VERSION=$1
elif [[ "$BRANCH" =~ ^release\/v.*$ ]]; then
  VERSION=${BRANCH#*release/v}
else
  echo "$BRANCH does not appear to be a release branch, please specify VERSION manually"
  echo "$(basename $0) <version-number>"
  exit 1
fi

echo Bumping the version number to $VERSION
sed -i '' "s/VERSION_NAME=.*/VERSION_NAME=$VERSION/" gradle.properties
sed -i '' "s/## TBD/## $VERSION ($(date '+%Y-%m-%d'))/" CHANGELOG.md
