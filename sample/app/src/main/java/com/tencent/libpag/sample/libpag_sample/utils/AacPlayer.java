package com.tencent.libpag.sample.libpag_sample.utils;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AacPlayer {
    private static final String TAG = "AacPlayer";
    private static final String MIME_TYPE = "audio/mp4a-latm"; // AAC LC
    private static final int SAMPLE_RATE = 44100; // 根据实际 AAC 数据的采样率进行调整
    private static final int CHANNEL_COUNT = 2; // 根据实际通道数进行调整
    private static final int AUDIO_FORMAT = AudioFormat.CHANNEL_OUT_STEREO; // 根据 CHANNEL_COUNT 调整
    private static final int BUFFER_SIZE = 1024 * 1024 ; // 缓冲区大小
    private ByteBuffer srcByteBuffer = null;

    private MediaCodec codec;
    private AudioTrack audioTrack;
    @SuppressLint("NewApi")
    private MediaCodec.Callback codecCallback = new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    if (srcByteBuffer.remaining() <= 0) {
                        return;
                    }
                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                    inputBuffer.clear();
                    Log.i(TAG, "remaining:" + inputBuffer.remaining() + " and acc remaining:" + srcByteBuffer.remaining());
                    byte[] data = new byte[inputBuffer.remaining()];
                    srcByteBuffer.get(data, 0, Math.min(srcByteBuffer.remaining(), data.length));
                    inputBuffer.put(data);
                    codec.queueInputBuffer(index, 0, data.length, 0, 0);
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index,
                                                    @NonNull MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                    Log.i(TAG, "on output available, inde:" + index + " info size:" + info.size + " offset:" + info.offset);
                    final byte[] chunk = new byte[info.size];
                    outputBuffer.get(chunk); // Read the buffer all at once
                    outputBuffer.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN
                    audioTrack.write(chunk, info.offset, info.offset + info.size); // AudioTrack write data
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            };

    @SuppressLint("NewApi")
    public AacPlayer(ByteBuffer byteBuffer) {
        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AUDIO_FORMAT, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AUDIO_FORMAT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);
        audioTrack.play();
        srcByteBuffer = byteBuffer;
        try {
            codec = MediaCodec.createDecoderByType(MIME_TYPE);
            codec.setCallback(codecCallback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT);
        codec.configure(format, null, null, 0);
        codec.start();
    }


    public void release() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
