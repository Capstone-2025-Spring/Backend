# python_server/python_server/audio_analysis.py

import os
import math
import librosa
import librosa.display
import matplotlib.pyplot as plt
import numpy as np
import parselmouth

def get_shimmer(file_path):
    try:
        snd = parselmouth.Sound(file_path)
        point_process = parselmouth.praat.call(snd, "To PointProcess (periodic, cc)", 75, 500)
        shimmer = parselmouth.praat.call(
            [snd, point_process],
            "Get shimmer (local)",
            0, 0,      # time range
            75, 500,   # pitch floor/ceiling
            1.3, 1.6   # period/amplitude factor
        )
        return shimmer
    except Exception:
        return None

def analyze_audio(file_path):
    try:
        y, sr = librosa.load(file_path, sr=None)
        duration = librosa.get_duration(y=y, sr=sr)
        zcr = librosa.feature.zero_crossing_rate(y)
        zcr_mean = float(np.mean(zcr))

        # 스펙트로그램 저장
        stft = librosa.stft(y)
        db = librosa.amplitude_to_db(np.abs(stft), ref=np.max)
        spectrogram_path = file_path.replace(".mp3", "_spectrogram.png")

        plt.figure(figsize=(10, 4))
        librosa.display.specshow(db, sr=sr, x_axis='time', y_axis='log')
        plt.colorbar(format='%+2.0f dB')
        plt.title('Spectrogram (dB)')
        plt.tight_layout()
        plt.savefig(spectrogram_path)
        plt.close()

        shimmer_val = get_shimmer(file_path)
        if shimmer_val is None or math.isnan(shimmer_val):
            shimmer_val = "unavailable"

        return {
            "sample_rate": sr,
            "duration": duration,
            "zcr_mean": zcr_mean,
            "shimmer": shimmer_val,
            "spectrogram_path": os.path.basename(spectrogram_path)
        }

    except Exception as e:
        return {"error": str(e)}
