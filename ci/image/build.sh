#!/bin/bash -e
#
# Build Docker images
#
set -o pipefail
THIS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $THIS_DIR/../_init7.sh

cp -p $THIS_DIR/../../pom.xml $THIS_DIR
cp -rp $THIS_DIR/../../dependencies/ $THIS_DIR/dependencies
#export DOCKER_BUILDKIT=0

for name in "${!BUILD_IMAGE_NAMES[@]}"; do
    docker build \
        --pull \
        --file $THIS_DIR/Dockerfile.$name-build \
        --label snowflake \
        --label $DRIVER_NAME \
        --tag ${BUILD_IMAGE_NAMES[$name]} .
done


docker build \
        --pull \
        --file $THIS_DIR/Dockerfile.$TEST_IMAGE_NAME-test \
        --label snowflake \
        --label $DRIVER_NAME \
        --tag $TEST_IMAGE_VAL .


