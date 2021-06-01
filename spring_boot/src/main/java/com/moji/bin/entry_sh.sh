#!/bin/bash
#description: 所有任务的进入脚本，用于判断任务在哪台服务器执行，获取配置信息，切换执行服务器；判断是否需要自定义执行；判断不同的执行频率；
# 如果跟当前的服务器为同一台服务器，不做切换
#createTime:2021-05-27
#author:lyh
####参数说明：

start_time=`date +"%Y-%m-%d %H:%M:%S"`
script=1
server_location=2
path_location=1
job_name="dau_source"
project_exec_frequency='d'
server_name=1
projectid=-1
flowid=-1
agent_location=/home/hadoop/test_lyh/azkaban_test/web_app/agent.sh
execid=-1
step=1
start_dt=""
end_dt="-1"
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
        --start_dt)
            start_dt=$2
            shift
            shift
            ;;
        --end_dt)
            end_dt=$2
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
start_dt:"${start_dt}",\
end_dt:"${end_dt}",\
flowid:"${flowid}"\
}
EOF
)
echo "config:${config}"

check_result() {
    if [ $? -ne 0 ];then
        echo "==========$job_name Failed=========="
        exit 1
    fi
}
run_hour(){
   # 按小时执行
   start_hour=$(date "+%Y-%m-%d %H:00:00" -d"${start_dt}")
   end_hour=$(date "+%Y-%m-%d %H:00:00" -d"${end_dt}")
   n=0
   while true
   do
       execute_hour=$(date "+%Y-%m-%d %H:00:00" -d"${start_dt} ${n} hour")
       echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>execute_hour=${execute_hour}>>>>>>>end_hour=${end_hour}"
       if (( "$execute_hour" \> "${end_hour}" -o "$execute_hour" == "${end_hour}" ));then
           echo "===========finished========"
       else
           check_flag $1
       fi
       n=$((n+${step}))
   done
}
check_flag(){
   if (( $1 == 1 ));then
       run_ssh
   else
       `${script}`
   fi
}
run_day(){
    # 按天执行
    start_day=$(date "+%Y-%m-%d 00:00:00" -d"${start_dt}")
    end_day=$(date "+%Y-%m-%d 00:00:00" -d"${end_dt}")
    n=0
    while true
    do
        execute_day=$(date "+%Y-%m-%d 00:00:00" -d"${start_dt} ${n} day")
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>execute_day=${execute_day}>>>>>>>end_day=${end_day}"
        if (( "$execute_day" \> "${end_day}" -o "$execute_day" == "${end_day}" ));then
            echo "===========finished========"
        else
            check_flag $1
        fi
        n=$((n+${step}))
    done
}
run_month(){
    # 按月执行
    start_month=$(date "+%Y-%m-01 00:00:00" -d"${start_dt}")
    end_month=$(date "+%Y-%m-01 00:00:00" -d"${end_dt}")
    n=0
    while true
    do
        execute_month=$(date "+%Y-%m-01 00:00:00" -d"${start_dt} ${n} month")
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>execute_month=${execute_month}>>>>>>>end_month=${end_month}"
        if (( "$execute_month" \> "${end_month}" -o "$execute_month" == "${end_month}" ));then
            echo "===========finished========"
        else
            check_flag $1
        fi
        n=$((n+${step}))
    done
}
run_minute(){
    # 按分钟执行
    start_minute=$(date "+%Y-%m-%d %H:%M:00" -d"${start_dt}")
    end_minute=$(date "+%Y-%m-%d %H:%M:00" -d"${end_dt}")
    n=0
    while true
    do
        execute_minute=$(date "+%Y-%m-01 00:00:00" -d"${start_dt} ${n} month")
        echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>execute_minute=${execute_minute}>>>>>>>end_minute=${end_minute}"
        if (( "$execute_minute" \> "${end_minute}" -o "$execute_minute" == "${end_minute}" ));then
            echo "===========finished========"
        else
            check_flag $1
        fi
        n=$((n+${step}))
    done
}
check_frequency_run(){
    # 判断执行频率 按小时，按天、按分钟和按月
    if (( "$1" == "d" ));then
        run_day $2
    elif (( "$1" == "h" ));then
        run_hour $2
    elif (( "$1" == "m" ));then
        run_minute $2
    elif (( "$1" == "M" ));then
        run_month $2
    else
        echo "<<<<<<<<<<<<<<<<<<<<<<<<project_exec_frequency=$1 不支持<<<<<<<<<<<<<<<<<<<<<"
    fi
}

function run_ssh() {
   ssh $server_location "$script;exit"
}

function checkserver() {
    # 检查当前的服务器跟配置的job服务器是否相同，如果不同则切换服务器
    current_ip=`hostname -i`
    echo "======================current_ip:$current_ip====================="
    grep_str=`echo ${server_location} | grep ${current_ip}`
    if [[ -z ${grep_str} ]];then
       echo "=====================任务目标服务器跟当前服务器不一致，执行ssh命令====================="
       # 判断是不是自定义执行，如果不是自定义执行，会在pre_job中将end_dt设置为-1，就根据azkaban自己调度执行
       if (( $end_dt == "-1" ));then
           run_ssh
       else
           check_frequency_run ${project_exec_frequency} 1
       fi
    else
       if (( $end_dt == "-1" ));then
           `${script}`
       else
           check_frequency_run ${project_exec_frequency} 0
       fi
    fi
}
checkserver
end_time=`date +"%Y-%m-%d %H:%M:%S"`
start_time_second=`date -d  "$start_time" +%s`
end_time_second=`date -d  "$end_time" +%s`
interval=`expr $end_time_second - $start_time_second`
echo "start_time :"$start_time
echo "end_time :"$end_time
echo "interval :"$interval s  = $((interval/3600)) h $(($((interval/60))%60)) min $((interval%60)) s