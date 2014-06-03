java dv_routing A 8000 configs/configeA.txt -e &
echo "$!" > pids
java dv_routing B 8001 configs/configeB.txt -e &
echo "$!" >> pids
java dv_routing C 8002 configs/configeC.txt -e &
echo "$!" >> pids
java dv_routing D 8003 configs/configeD.txt -e &
echo "$!" >> pids
java dv_routing E 8004 configs/configeE.txt -e &
echo "$!" >> pids

wait
