#!/bin/bash

while getopts i: flag
do
    case "${flag}" in
        i) ip=${OPTARG};;
    esac
done

applicationConfPath="/home/ubuntu/Desktop/hailstorm/src/main/resources/application.conf"
original_applicationConfPath="/home/ubuntu/Desktop/hailstorm/src/main/resources/original_application.conf"
cached_applicationConfPath="/home/ubuntu/Desktop/hailstorm/target/scala-2.12/classes/application.conf"
data_folder_path="/home/ubuntu/Desktop/hailstorm/data/"

echo "IP: $ip";
# copy the application.conf file from the IP
scp -i /home/ubuntu/Desktop/ECE1724Project.pem ubuntu@$ip:$applicationConfPath $applicationConfPath
#scp -i /home/ubuntu/Desktop/ECE1724Project.pem ubuntu@$ip:$applicationConfPath $original_applicationConfPath
#scp -i /home/ubuntu/Desktop/ECE1724Project.pem ubuntu@$ip:$applicationConfPath $cached_applicationConfPath
cp -f $applicationConfPath $original_applicationConfPath
cp -f $applicationConfPath $cached_applicationConfPath

found=False
count=0

#extract local machine IP
local_ip=`hostname -I`
local_ip=$(echo $local_ip | tr -d ' ') # removing white space

semi=":"

#get all the port and IP combinition that can be take over.
s_exc='/home/ubuntu/.jdks/corretto-15.0.2/bin/java -javaagent:/home/ubuntu/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.7148.57/lib/idea_rt.jar=39625:/home/ubuntu/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.7148.57/bin -Dfile.encoding=UTF-8 -classpath /home/ubuntu/Desktop/hailstorm/target/scala-2.12/classes:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jffi/1.2.23/jffi-1.2.23.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jffi/1.2.23/jffi-1.2.23-native.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jnr-a64asm/1.0.0/jnr-a64asm-1.0.0.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jnr-constants/0.9.15/jnr-constants-0.9.15.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jnr-ffi/2.1.12/jnr-ffi-2.1.12.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jnr-posix/3.0.54/jnr-posix-3.0.54.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/jnr/jnr-x86asm/1.0.2/jnr-x86asm-1.0.2.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/pathikrit/better-files-akka_2.12/3.5.0/better-files-akka_2.12-3.5.0.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/pathikrit/better-files_2.12/3.5.0/better-files_2.12-3.5.0.jar:/home/ubuntu/.cache/coursier/v1/https/jcenter.bintray.com/com/github/serceman/jnr-fuse/0.5.4/jnr-fuse-0.5.4.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-actor_2.12/2.5.31/akka-actor_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-agent_2.12/2.5.31/akka-agent_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-cluster-metrics_2.12/2.5.31/akka-cluster-metrics_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-cluster-tools_2.12/2.5.31/akka-cluster-tools_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-cluster_2.12/2.5.31/akka-cluster_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-coordination_2.12/2.5.31/akka-coordination_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-multi-node-testkit_2.12/2.5.31/akka-multi-node-testkit_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-protobuf_2.12/2.5.31/akka-protobuf_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-remote_2.12/2.5.31/akka-remote_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-slf4j_2.12/2.5.31/akka-slf4j_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-stream_2.12/2.5.31/akka-stream_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/akka/akka-testkit_2.12/2.5.31/akka-testkit_2.12-2.5.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/config/1.3.3/config-1.3.3.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/ssl-config-core_2.12/0.3.8/ssl-config-core_2.12-0.3.8.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/io/aeron/aeron-client/1.15.1/aeron-client-1.15.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/io/aeron/aeron-driver/1.15.1/aeron-driver-1.15.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/io/jvm/uuid/scala-uuid_2.12/0.2.4/scala-uuid_2.12-0.2.4.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/io/kamon/sigar-loader/1.6.6-rev002/sigar-loader-1.6.6-rev002.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/io/netty/netty/3.10.6.Final/netty-3.10.6.Final.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/net/java/dev/jna/jna/4.0.0/jna-4.0.0.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/net/smacke/jaydio/0.1/jaydio-0.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/agrona/agrona/0.9.31/agrona-0.9.31.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-analysis/7.1/asm-analysis-7.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-commons/7.1/asm-commons-7.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-tree/7.1/asm-tree-7.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-util/7.1/asm-util-7.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm/7.1/asm-7.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.2/reactive-streams-1.0.2.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/rogach/scallop_2.12/3.1.5/scallop_2.12-3.1.5.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-java8-compat_2.12/0.8.0/scala-java8-compat_2.12-0.8.0.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-parser-combinators_2.12/1.1.2/scala-parser-combinators_2.12-1.1.2.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.11/scala-library-2.12.11.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-stm/scala-stm_2.12/0.9.1/scala-stm_2.12-0.9.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.30.1/sqlite-jdbc-3.30.1.jar:/home/ubuntu/.cache/coursier/v1/https/repo1.maven.org/maven2/uk/org/lidalia/sysout-over-slf4j/1.0.2/sysout-over-slf4j-1.0.2.jar ConsistentHashingStandalone'
s=$($s_exc)
echo $s

IFS=', ' read -r -a ip_port_array <<< "$s"
port_list=()
comma=','
#go through ip_port array and replace desired port
mkdir $data_folder_path
for ip_port in "${ip_port_array[@]}"; do

  IFS=':' read -r -a ip_port_tuple <<< "$ip_port"
  ip="${ip_port_tuple[0]}"
  port="${ip_port_tuple[1]}"
  port_list+=$port$comma
  echo $port
  replace_string="$local_ip$semi$port"
  #echo $data_folder_path$port
  scp -i /home/ubuntu/Desktop/ECE1724Project.pem -r ubuntu@$ip:$data_folder_path$port $data_folder_path
  #echo $ip_port
  echo $replace_string
  sed -i -e 's/'"$ip_port"'/'"$replace_string"'/' $applicationConfPath
done

#change the frontend ip in application conf
s=":3553"
sed -i -e 's/'"$ip$s"'/'"$local_ip$s"'/' $applicationConfPath
#change dev mode to scaling mode
sed -i -e 's/dev/scl/g' $applicationConfPath

echo $port_list
sbt "run -m /home/ubuntu/HS/ -v -a $port_list"

