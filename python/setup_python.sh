#!/bin/sh

echo "################################description##################################"
echo "# Setup environment for all python modules under current path, by appending path item for the"
echo "# subdirectory to environment variable called PYTHONPATH in ~/.bash_profile."
echo "#"
echo "# [Note] After this script run successfully, you can execute python file under subdirectory"
echo "# from any path on this computer, by using the following command:"
echo "# \"python -m python_file [params]\"."
echo "#"
echo "# [Example] Generate both view and presenter for a component called Test under module"
echo "# watchclient from project livesdk, as follows:"
echo "# \"cd ~/Development/huyu/livesdk/watchclient\""
echo "# \"python -m create_view_with_component Test"\"
echo "#"
echo "# You can also import python file under subdirectory by using the following statement:"
echo "# \"import python_file\" or \"from python_file import *\"."
echo "#############################################################################"

echo "\n###################################setup#####################################"
modulePath=$PWD

baseProfile=~/.bash_profile
source $baseProfile

pythonPath=$PYTHONPATH
echo "current PYTHONPATH is: $PYTHONPATH"

# modules to be added
modules=("component" "copyres")
echo "modules to be added is: ${modules[@]}"

newPath=
for elem in ${modules[@]};
do
    currPath=$modulePath/$elem
    if [[ $pythonPath =~ $currPath ]]; then
        echo "waring: PYTHONPATH already contains $currPath"
    else
        newPath=$newPath$currPath:
    fi
done

if [ $newPath ]; then
    echo "add path:$newPath to PYTHONPATH in $baseProfile"
    if [ ! $PYTHONPATH ]; then
        echo "export PYTHONPATH=$newPath\$PYTHONPATH" >> $baseProfile
    else
        echo "export PYTHONPATH=$newPath\$PYTHONPATH" >> $baseProfile
    fi
    source $baseProfile
    echo "result PYTHONPATH is $PYTHONPATH"
else
    echo "waring: no path need to be added to PYTHONPATH"
fi

echo "####################################done#####################################"