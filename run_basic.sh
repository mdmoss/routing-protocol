java dv_routing A 8000 configeA.txt -p &
echo "$!" > pids
java dv_routing B 8001 configeB.txt -p &
echo "$!" >> pids
java dv_routing C 8002 configeC.txt -p &
echo "$!" >> pids
java dv_routing D 8003 configeD.txt -p &
echo "$!" >> pids
java dv_routing E 8004 configeE.txt -p &
echo "$!" >> pids

wait
