# python_server/python_server/app.py

from flask import Flask, request, jsonify
from vocab_checker import check_vocab
from audio_analysis import analyze_audio
import os

app = Flask(__name__)
UPLOAD_FOLDER = './uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/vocab-check', methods=['POST'])
def vocab_check():
    data = request.get_json()
    text = data.get('text', '')
    result = check_vocab(text)
    return jsonify(result)

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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
