#!/bin/bash


echo "Uploading files to HDFS"
hadoop fs -copyFromLocal -f /home/ubuntu/klucar/*.gz /user/klucar/.
hadoop fs -copyFromLocal -f /home/ubuntu/klucar/libaccumulo.so /user/klucar/.
