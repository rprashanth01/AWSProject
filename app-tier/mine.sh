#!/bin/bash
make clean
echo $1 >> file
make
./pifft file >> output
aws s3api wait bucket-exists --bucket pifftcomputed
aws s3api put-object --bucket pifftcomputed --key $1 --body output >> validate
