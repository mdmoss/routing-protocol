java dv_routing X 6000 configs/configX.txt -p &
echo "$!" > pids
java dv_routing Y 6001 configs/configY.txt -p &
echo "$!" >> pids
java dv_routing Z 6002 configs/configZ.txt -p &
echo "$!" >> pids

wait
