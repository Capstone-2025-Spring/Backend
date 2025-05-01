# python_server/python_server/app.py

import io
import json
import os
import uuid

from flask import Flask, jsonify, request, send_file

#from vocab_checker import check_vocab
from audio_analysis import analyze_audio
from video_caption_generator.run_captioning import run_captioning_from_json

app = Flask(__name__)
UPLOAD_FOLDER = './uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/vocab-check', methods=['POST'])
def vocab_check():
    data = request.get_json()
    text = data.get('text', '')
    return jsonify({"message": "vocab check 임시 비활성화 중. JVM 깔기 시러서"})
    #result = check_vocab(text)
    #return jsonify(result)

@app.route('/analyze-audio', methods=['POST'])
def analyze_audio_route():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    file.save(file_path)

    try:
        result = analyze_audio(file_path)
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/generate-caption', methods=['POST']) #POST 요청API
def generate_caption():
    if 'file' not in request.files: #파일 업로드가 안됨
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file'] 
    if file.filename == '': #파일 이름이 비어있음
        return jsonify({"error": "No selected file"}), 400

    try:
        json_data = json.load(file) #json파일 로드드
        captions = run_captioning_from_json(json_data) #딥러닝 기반 예측 수행 및 캡션 생성

        # 메모리 내 텍스트 파일 생성. 디스크에 저장하지 않음
        caption_text = "\n".join(captions)
        buffer = io.BytesIO()
        buffer.write(caption_text.encode('utf-8'))
        buffer.seek(0)

        return send_file(
            buffer,
            as_attachment=True,
            download_name="caption.txt",
            mimetype='text/plain'
        )

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
