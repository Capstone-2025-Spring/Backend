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

@app.route('/generate-caption', methods=['POST'])
def generate_caption():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    try:
        json_data = json.load(file)
        caption_string = run_captioning_from_json(json_data)  # caption_lines를 문자열로 합침

        return jsonify({"motionInfo": caption_string})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

    
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
