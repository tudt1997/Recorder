package com.example.tudt1997.recorder.renderer;

import android.graphics.Canvas;

public interface WaveformRenderer {
    void render(Canvas canvas, byte[] waveform);
    void render(Canvas canvas, short[] waveform);
}
