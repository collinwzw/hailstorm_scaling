#!/bin/bash
while getopts i: flag
do
    case "${flag}" in
        i) ip=${OPTARG};;
    esac
done

applicationConfPath="/home/ubuntu/Desktop/hailstorm/src/main/resources/application.conf"

echo "IP: $ip";
# copy the application.conf file from the IP
scp -i /home/ubuntu/Desktop/ECE1724Project.pem ubuntu@$ip:$applicationConfPath $applicationConfPath

found=False
count=0

ip_port_array=()
#read copied application.conf, store all the IP and port to the ip_port_array
while IFS= read -r line
do
  if [ "$line" = "]" ]
  then
    found=False
  fi
  if [ $found = true ]
  then
    ip_port_array+="$line"
    ((count++))
    fi
  if [ "$line" = "hailstorm.backend.nodes = [" ]
  then
    found=true
  fi
done < "$applicationConfPath"

echo $count

#extract local machine IP
local_ip=`hostname -I`
local_ip=$(echo $local_ip | tr -d ' ') # removing white space

semi=":"

#go through ip_port array and replace desired port
for ip_port in $ip_port_array; do
  a="${ip_port#*:}";
  port="${a%\"*}"
  if [ $port = "2552" ]
  then
    replace_string="$local_ip$semi$port"
    ip_port="${ip_port#*\"}";
    echo $ip_port
    ip_port="${ip_port%\"*}"
    echo $ip_port
    echo $replace_string
    sed -i -e 's/'"$ip_port"'/'"$replace_string"'/' $applicationConfPath
    fi
done

#sbt

#sed -i -e 's/abc/XYZ/g' /tmp/file.txt