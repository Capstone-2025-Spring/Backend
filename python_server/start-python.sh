#!/bin/bash
source /home/ec2-user/python_server/venv/bin/activate

# 🔽 lsof이 설치되어 있으면 포트 5000 점유 프로세스를 종료
if command -v lsof > /dev/null 2>&1; then
  lsof -ti:5000 | xargs -r kill -9
fi

# Flask 서버 실행
python /home/ec2-user/python_server/app.py
