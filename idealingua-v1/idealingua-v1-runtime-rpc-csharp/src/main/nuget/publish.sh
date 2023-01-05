#!/usr/bin/env bash
set -xe

export THISDIR="$( cd "$(dirname "$0")" ; pwd -P )"

pushd .
cd $THISDIR

rm -rf *.nupkg

NUSPEC=irt.tmp.nuspec
cat irt.nuspec | sed 's/0.0.1-UNSET/'${IZUMI_VERSION}'/g' > $NUSPEC
cat $NUSPEC
nuget pack $NUSPEC
rm $NUSPEC

#nuget setapikey $NUGET_TOKEN

for TRG in $(find . -name '*.nupkg' -type f -print)
do
    dotnet nuget push $TRG -k $NUGET_TOKEN --source https://api.nuget.org/v3/index.json || exit 1
done

popd
