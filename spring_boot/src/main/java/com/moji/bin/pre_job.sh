#!/bin/bash
#description: 所有任务的进入脚本，用于判断任务在哪台服务器执行，获取配置信息，切换执行服务器;向pre_job.sh传递的参数是project级的，比如需要传递self_start_dt,self_end_dt,self_active
#createTime:2021-05-27
#author:lyh
####参数说明：

start_time=`date +"%Y-%m-%d %H:%M:%S"`
project_name="test_lyh_a"
step=1
project_exec_frequency="d"
self_active="off"
last_succ_exec_begin_dt=1
self_start_dt=`date -d -1day +"%Y-%m-%d %H:%M:%S"`
self_end_dt=`date -d 0day +"%Y-%m-%d %H:%M:%S"`
yesDateYYMMDD=`date -d -1day +%Y%m%d`
POSITIONAL=()
while [[ $# -gt 0 ]]
    do
    key="$1"
    case $1 in
        --project_name)
            project_name=$2
            shift
            shift
            ;;
        --step)
            step=$2
            shift
            shift
            ;;
        --project_exec_frequency)
            project_exec_frequency=$2
            shift
            shift
            ;;
        --self_active)
            self_active=$2
            shift
            shift
            ;;
        --last_succ_exec_begin_dt)
            last_succ_exec_begin_dt=$2
            shift
            shift
            ;;
        --self_start_dt)
            self_start_dt=$2
            shift
            shift
            ;;
        --self_end_dt)
            self_end_dt=$2
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
project_name:"${project_name}",\
step:"${step}",\
project_exec_frequency:"${project_exec_frequency}",\
self_active:"${self_active}",\
project_exec_frequency:"${project_exec_frequency}",\
self_start_dt:"${self_start_dt}",\
self_end_dt:"${self_end_dt}",\
last_succ_exec_begin_dt:"${last_succ_exec_begin_dt}"\
}
EOF
)
echo "config:${config}"

function check_self_active() {
    azkaban_test='mysql -h172.16.19.147 -uazkaban -pazkaban -P3306 -Dazkaban -Nse'
    project_sql="select id,version from projects where name='$project_name' and active = 1"
    while read project_info
    do
        echo "===========================project_info:$project_info=================================="
        azkaban_projectid=`echo $project_info | cut -d ' ' -f 1`
        version_id=`echo $project_info | cut -d ' ' -f 2`
        date_file="/data1/moji/soft/azkaban-exec-server/projects/$azkaban_projectid.$version_id/temp/date.properties"
        echo "===========================date_file:$date_file=================================="
        if (( $self_active == "off" ));then
            echo "off"
            start_dt=`date -d -1day +"%Y-%m-%d %H:%M:%S"`
            end_dt="-1"
        else
            # 开启自定义执行
            echo "on"
            start_dt=${self_start_dt}
            end_dt=${self_end_dt}
        fi
        # 清空文件内容
        :> ${date_file}
        echo "start_dt=$start_dt" >> ${date_file}
        echo "end_dt=$end_dt" >> ${date_file}
    done <<< `$azkaban_test "$project_sql"`
}
check_self_active
end_time=`date +"%Y-%m-%d %H:%M:%S"`
start_time_second=`date -d  "$start_time" +%s`
end_time_second=`date -d  "$end_time" +%s`
interval=`expr $end_time_second - $start_time_second`
echo "start_time :"$start_time
echo "end_time :"$end_time
echo "interval :"$interval s  = $((interval/3600)) h $(($((interval/60))%60)) min $((interval%60)) s