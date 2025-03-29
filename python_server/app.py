from flask import Flask, request, jsonify
from vocab_checker import check_vocab

app = Flask(__name__)

@app.route('/vocab-check', methods=['POST'])
def vocab_check():
    data = request.get_json()
    text = data.get('text', '')
    result = check_vocab(text)
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
