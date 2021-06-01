#!/bin/bash
#description: 所有工程结束的最终脚本，用于回写参数到mysql中，需要实现的功能：1、读取当前工程执行结束的时间、执行的exec_id${azkaban.flow.execid};2、将结果写回到mysql中
#createTime:2021-05-27
#author:lyh
####参数说明：本脚本中的execid为post_job的execid

start_time=`date +"%Y-%m-%d %H:%M:%S"`
project_name=1
execid=-1
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
        --execid)
            execid=$2
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
{project_name:"${project_name}",execid:"${execid}"}
EOF
)
echo "config:${config}"
check_result() {
    if [ $? -ne 0 ];then
        echo "==========${project_name} Failed=========="
        exit 1
    else
        echo "===========${project_name} finished=========="
    fi
}
function insert_to_mysql() {
    mysql_test='mysql -h192.168.9.55 -uroot -P3306 -Dmoji_dam -Nse'
    azkaban_test='mysql -h172.16.19.147 -uazkaban -pazkaban -P3306 -Dazkaban -Nse'
    azkaban_sql="select start_time from execution_flows where exec_id = $execid"

    while read num
    do
        last_succ_exec_begin_dt=$( date -d "@$((num/1000))" +"%Y-%m-%d %H:%M:%S" )
        echo "==================mills=$num========last_succ_exec_begin_dt=$last_succ_exec_begin_dt=======================execid=$execid=================="
        insert_sql="insert into moji_project (last_succ_exec_id,last_succ_exec_begin_dt) values($execid,$last_succ_exec_begin_dt)"
        echo "insert_sql:$insert_sql"
        $mysql_test "$insert_sql"
    done <<< `$azkaban_test "$azkaban_sql"`
}
insert_to_mysql
check_result
end_time=`date +"%Y-%m-%d %H:%M:%S"`
start_time_second=`date -d  "$start_time" +%s`
end_time_second=`date -d  "$end_time" +%s`
interval=`expr $end_time_second - $start_time_second`
echo "start_time :"$start_time
echo "end_time :"$end_time
echo "interval :"$interval s  = $((interval/3600)) h $(($((interval/60))%60)) min $((interval%60)) s