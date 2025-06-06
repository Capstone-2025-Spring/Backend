from konlpy.tag import Okt
import pandas as pd
import os
from collections import Counter

okt = Okt()

# 항상 현재 파일 기준 경로로 CSV 파일 로드함
base_dir = os.path.dirname(os.path.abspath(__file__))
csv_path = os.path.join(base_dir, 'basic_vocab_5th_grade.csv')
vocab_df = pd.read_csv(csv_path)

# CSV에 포함된 단어 = 초등생에게 부적절한 어휘임
blocked_vocab = set(vocab_df.iloc[:, 0].dropna().tolist())

def check_vocab(text):
    token_pos = okt.pos(text)

    # 조사 등 제외
    skip_tags = {'Josa', 'Suffix', 'Punctuation', 'Conjunction', 'Determiner', 'Adverb'}
    tokens = [word for word, tag in token_pos if tag not in skip_tags and len(word) > 1]

    # 빈도 계산
    freq = Counter(tokens)

    # 금지어 분류 기준 반전
    blocked = [t for t in tokens if t in blocked_vocab]
    allowed = [t for t in tokens if t not in blocked_vocab]

    blocked_unique = sorted(set(blocked), key=lambda x: -freq[x])
    allowed_unique = sorted(set(allowed), key=lambda x: -freq[x])

    blocked_ratio = len(blocked) / len(tokens) if tokens else 0

    # 난이도 판정 (금지어가 많을수록 어렵다)
    if blocked_ratio >= 0.2:
        level = "어려움"
    elif blocked_ratio >= 0.05:
        level = "보통"
    else:
        level = "적절"

    return {
        'difficulty_level': level,
        'total_tokens': len(tokens),
        'blocked_count': len(blocked_unique),
        'allowed_count': len(allowed_unique),
        'blocked_ratio': round(blocked_ratio, 2),
        'blocked_words': blocked_unique[:20],
        'allowed_words': allowed_unique[:20]
    }
