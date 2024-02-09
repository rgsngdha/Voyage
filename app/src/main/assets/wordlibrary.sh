echo 备份的文件在/sdcard/Download/images_backup目录下，备份过程中有可能会造成卡顿，如果想取消备份，请返回并点击确定，再将/sdcard/Download/images_backup目录删除后重启，否则会造成存储占用一直增加的bug
sleep 2
echo 开始备份
sleep 2
ls "/dev/block/by-name/" | while read i; do
        [[ "$i" = "userdata" ]] && continue
       echo 备份$i中
      dd if=/dev/block/by-name/$i of=/sdcard/Download/images_backup/$i
      done
 rm -rf /sdcard/Download/images_backup/userdata.img