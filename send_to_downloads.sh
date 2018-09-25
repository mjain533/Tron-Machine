#!/bin/bash

cd target

quantFile=$(ls -p | grep -v / | sort -V | tail -n 1)
echo $quantFile
cd ..
mv target/$quantFile /
ls

