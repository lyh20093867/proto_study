#!/bin/bash
#description: 所有任务的进入脚本，用于判断任务在哪台服务器执行，获取配置信息，切换执行服务器
#createTime:2021-05-27
#author:lyh
####参数说明：

start_time=`date +"%Y-%m-%d %H:%M:%S"`
script=1
server_location=2
path_location=1
job_name="dau_source"
project_exec_frequency=1
server_name=1
projectid=-1
flowid=-1
agent_location=/home/hadoop/test_lyh/azkaban_test/web_app/agent.sh
execid=-1
yesDateYYMMDD=`date -d -1day +%Y%m%d`
POSITIONAL=()
while [[ $# -gt 0 ]]
    do
    key="$1"
    case $1 in
        --script)
            script=$2
            shift
            shift
            ;;
        --server_location)
            server_location=$2
            shift
            shift
            ;;
        --path_location)
            path_location=$2
            shift
            shift
            ;;
        --job_name)
            job_name=$2
            shift
            shift
            ;;
        --project_exec_frequency)
            project_exec_frequency=$2
            shift
            shift
            ;;
        --server_name)
            server_name=$2
            shift
            shift
            ;;
        --projectid)
            projectid=$2
            shift
            shift
            ;;
        --flowid)
            flowid=$2
            shift
            shift
            ;;
        --execid)
            execid=$2
            shift
            shift
            ;;
        --agent_location)
            agent_location=$2
            shift
            shift
            ;;
        *)
            POSITIONAL+=("$1")
            shift
            ;;
    esac
done
config=$(cat <<EOF
{\
script:"${script}",\
server_location:"${server_location}",\
path_location:"${path_location}",\
job_name:"${job_name}",\
project_exec_frequency:"${project_exec_frequency}",\
server_name:"${server_name}",\
projectid:"${projectid}",\
execid:"${execid}",\
agent_location:"${agent_location}",\
flowid:"${flowid}"\
}
EOF
)
echo "config:${config}"
function run() {
   ssh $server_location "$script"
}
run
end_time=`date +"%Y-%m-%d %H:%M:%S"`
start_time_second=`date -d  "$start_time" +%s`
end_time_second=`date -d  "$end_time" +%s`
interval=`expr $end_time_second - $start_time_second`
echo "start_time :"$start_time
echo "end_time :"$end_time
echo "interval :"$interval s  = $((interval/3600)) h $(($((interval/60))%60)) min $((interval%60)) s