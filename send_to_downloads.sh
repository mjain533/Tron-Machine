#!/bin/bash

cd target

quantFile=$(ls -p | grep -v / | sort -V | tail -n 1)
echo $quantFile


curl -X POST "https://${BB_AUTH_STRING}@api.bitbucket.org/2.0/repositories/${BITBUCKET_REPO_OWNER}/${BITBUCKET_REPO_SLUG}/downloads" --form files=@"target/$quantFile"


