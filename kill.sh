kill -9 `jobs -l | awk '{print $2}' | xargs -0`
