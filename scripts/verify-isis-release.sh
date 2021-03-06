#!/bin/bash
#
# usage: ./verify_isis_release.sh [nexus_repo_number] [isis_version]
#
# where nexus_repo_number and isis_version are as advised in RC vote message.
#
#    eg: ./verify_isis_release.sh 1086 1.17.0
#
#
# prereqs:
#    curl
#    gpg
#    unzip
#    jdk 8
#    mvn 3.6.0+
#

# shellcheck disable=SC2164

_execmustpass(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] Exiting..." >&2
    exit 10
}

_execmayfail(){
    echo $@
    $@
    status=$?
    if [ $status -eq 0 ] || [ $? -eq 0 ]; then
        return;
    fi
    echo "Command $@ failed! [error $status] But continuing anyway..." >&2
}

_download(){
    for fil in `cat /tmp/url.txt | grep -v ^#`
    do
        echo 'Downloading '$fil
        _execmustpass $download_cmd $fil
        _execmustpass $download_cmd $fil.asc
    done
}

_verify(){
    for zip in *.zip
    do
        echo 'Verifying '$zip
        _execmustpass gpg --verify $zip.asc $zip
    done
}

_unpack(){
    echo 'Unpacking '
    set -f
    _execmustpass unzip -q '*.zip'
    set +f
}

_build(){
    echo 'Removing Isis from local repo '$module
    rm -rf ~/.m2/repository/org/apache/isis

    echo 'Building'
    # previously there were multiple directories, now just the one.
    for module in ./isis*/
    do
        pushd $module
        _execmustpass mvn clean install -Dskip.git
	    popd
    done
}

_download_simpleapp(){
    APP=simpleapp
    BRANCH=master

    rm -rf test-$APP
    mkdir test-$APP
    pushd test-$APP

    REPO=isis-app-$APP
    curl "https://codeload.github.com/apache/$REPO/zip/$BRANCH" | jar xv
    mv $REPO-$BRANCH $REPO

    pushd $REPO
    _execmustpass mvn clean install
    popd; popd
}

_download_helloworld(){
    APP=helloworld
    BRANCH=master

    rm -rf test-$APP
    mkdir test-$APP
    pushd test-$APP

    REPO=isis-app-$APP
    curl "https://codeload.github.com/apache/$REPO/zip/$BRANCH" | jar xv
    mv $REPO-$BRANCH $REPO

    pushd $REPO
    _execmustpass mvn clean install
    popd; popd
}


# check the environment
# Check for curl or wget
download_cmd=
curl --version > /dev/null 2>&1
if [[ $? -eq 0 ]]; then
    download_cmd="curl -L -O"
fi
if [[ -z "$download_cmd" ]]; then
    wget --version > /dev/null 2>&1
    if [[ $? -eq 0 ]]; then
        download_cmd=wget
    else
        echo "No downloader found.. exiting.."
        exit 11
    fi
fi

NEXUSREPONUM=$1
shift
VERSION=$1
shift

if [[ -z "$NEXUSREPONUM" || -z "$VERSION" ]]; then
    echo "usage: `basename $0` [nexus_repo_number] [isis_version]" >&2
    exit 1
fi

cat <<EOF >/tmp/url.txt
http://repository.apache.org/content/repositories/orgapacheisis-$NEXUSREPONUM/org/apache/isis/core/isis/$VERSION/isis-$VERSION-source-release.zip
EOF

# The work starts here
_download
_verify
_unpack
_build
_download_simpleapp
_download_helloworld

# print instructions for final testing
clear
cat <<EOF

# Test out helloworld using:
pushd test-helloworld/isis-app-helloworld
mvn clean install
mvn spring-boot:run
popd

# Test out simpleapp using:
pushd test-simpleapp/isis-app-simpleapp
mvn clean install
mvn -pl webapp spring-boot:run
popd

EOF