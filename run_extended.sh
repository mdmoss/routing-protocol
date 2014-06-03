java dv_routing A 8000 configeA.txt -e &
echo "$!" > pids
java dv_routing B 8001 configeB.txt -e &
echo "$!" >> pids
java dv_routing C 8002 configeC.txt -e &
echo "$!" >> pids
java dv_routing D 8003 configeD.txt -e &
echo "$!" >> pids
java dv_routing E 8004 configeE.txt -e &
echo "$!" >> pids

wait
