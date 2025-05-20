#!/bin/bash
source /home/ec2-user/python_server/venv/bin/activate

# 🔍 5000번 포트가 사용 중이면 강제 종료
if command -v lsof > /dev/null 2>&1; then
  PID=$(lsof -ti:5000)
  if [ -n "$PID" ]; then
    echo "🔫 Killing existing process on port 5000 (PID: $PID)"
    kill -9 $PID
  fi
fi

# 🚀 Flask 서버 실행
python /home/ec2-user/python_server/app.py
