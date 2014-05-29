java dv_routing A 2000 configs/configA.txt &
echo "$!" > pids
java dv_routing B 2001 configs/configB.txt &
echo "$!" >> pids
java dv_routing C 2002 configs/configC.txt &
echo "$!" >> pids
java dv_routing D 2003 configs/configD.txt &
echo "$!" >> pids
java dv_routing E 2004 configs/configE.txt &
echo "$!" >> pids
java dv_routing F 2005 configs/configF.txt 
echo "$!" >> pids

wait
