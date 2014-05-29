while read pid; do
  kill $pid
done < pids
