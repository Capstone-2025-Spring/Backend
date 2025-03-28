from konlpy.tag import Okt
import pandas as pd

# CSV에서 어휘 불러오기
vocab_df = pd.read_csv('basic_vocab_5th_grade.csv')
vocab_list = vocab_df.iloc[:, 0].dropna().tolist()

okt = Okt()

def check_vocab(text):
    tokens = okt.nouns(text)  # 명사 기준 분석 (또는 okt.morphs로 더 광범위하게)
    known = [t for t in tokens if t in vocab_list]
    unknown = [t for t in tokens if t not in vocab_list]

    return {
        'known_words': known,
        'unknown_words': unknown,
        'total_tokens': len(tokens),
        'known_count': len(known),
        'unknown_count': len(unknown)
    }
