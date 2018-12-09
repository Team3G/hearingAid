package com.example.aaa1.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

public class AndroidAudioPlayer implements AudioProcessor {
    private AudioTrack audioTrack;

    AndroidAudioPlayer(TarsosDSPAudioFormat audioFormat, int bufferSize) {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                (int) audioFormat.getSampleRate(),
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        short[] shorts = new short[audioEvent.getBufferSize() / 2];
        ByteBuffer.wrap(audioEvent.getByteBuffer()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        audioTrack.write(shorts, 0, shorts.length);
        audioTrack.play();
        Log.d("fatal-player", "하하");
        return true;
    }

    @Override
    public void processingFinished() {
    }
}