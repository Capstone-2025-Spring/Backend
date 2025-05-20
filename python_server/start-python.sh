#!/bin/bash

# 포트 5000을 점유 중인 프로세스가 있다면 종료
PID=$(lsof -ti:5000)
if [ -n "$PID" ]; then
  echo "🔻 포트 5000 점유 프로세스 종료: $PID"
  kill -9 $PID
fi

# 가상환경 활성화 후 Flask 실행
source /home/ec2-user/python_server/venv/bin/activate
python /home/ec2-user/python_server/app.py
