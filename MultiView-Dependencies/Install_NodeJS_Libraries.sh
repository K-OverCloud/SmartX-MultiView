#!/bin/bash
#
# Copyright 2016 SmartX Collaboration (GIST NetCS). All rights reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#
# Name          : Install_NodeJS_Libraries.sh
# Description   : Script for Installing Java
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : November, 2016

echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Libraries Installing .................... "
sudo npm install npm -g 
currentDir=`pwd`
cd Visibility-Visualization/pvcT-Visualization
npm config set registry https://registry.npmjs.org/
npm cache clean
npm install
sudo npm install -g nodemon
echo -e "Done.\n"
cd $currentDir
