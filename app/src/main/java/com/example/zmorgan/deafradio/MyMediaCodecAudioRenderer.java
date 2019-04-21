package com.example.zmorgan.deafradio;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class MyMediaCodecAudioRenderer extends MediaCodecAudioRenderer {

    private byte[] bufferBytes =new byte[0];
    private long timeStart = 0;
    private long timeCurrent;
    private BufferListener bl;
    AudioProcessor[] audioProcessors;

    public MyMediaCodecAudioRenderer(BufferListener bl, Context applicationContext, MediaCodecSelector aDefault, Object o, boolean b, Handler handler, MainActivity.ComponentListener cl, AudioCapabilities capabilities, AudioProcessor[] audioProcessors) {
        super(applicationContext,aDefault, (DrmSessionManager<FrameworkMediaCrypto>) o,b,handler,cl,capabilities,audioProcessors);
        this.bl = bl;
        this.audioProcessors = audioProcessors;
    }

    public boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodec codec, ByteBuffer buffer, int bufferIndex, int bufferFlags, long bufferPresentationTimeUs, boolean shouldSkip, Format format) throws ExoPlaybackException {
        //Create a new buffer which copies the data before passing it on to the parent function
        int index = buffer.position();
        byte[] arr = new byte[buffer.remaining()];
        buffer.get(arr);
        buffer.position(index);
        if(super.processOutputBuffer(positionUs,elapsedRealtimeUs, codec, buffer, bufferIndex, bufferFlags, bufferPresentationTimeUs, shouldSkip, format)) {
            //If we've processed the buffer correctly then add it to our buffer
            if (timeStart == 0) {
                timeStart = System.currentTimeMillis();
            }
            timeCurrent = System.currentTimeMillis();
            if (timeCurrent - timeStart > 15 * 1000) {
                //If we reach the threshold of 15 seconds we convert it to Mono audio and then call the finished method and reset the buffer
                timeStart = timeCurrent;
                if(format.channelCount > 1) {
                    bufferBytes = stereoToMonoMix(bufferBytes);
                }
                bl.finished(bufferBytes,format.sampleRate);
                bufferBytes = new byte[0];
            }
            byte[] c = new byte[bufferBytes.length + arr.length];
            System.arraycopy(bufferBytes, 0, c, 0, bufferBytes.length);
            System.arraycopy(arr, 0, c, bufferBytes.length, arr.length);
            bufferBytes = c;
            //arr now contains the raw data
            return true;
        }
        else{
            return false;
        }

    }

    private byte[] stereoToMonoMix(byte[] input){
        //Converts from stereo to mono
        byte[] output = new byte[input.length / 2];
        int outputIndex = 0;
        int val1;
        int val2;
        for (int n = 0; n < input.length-4; n+= 4){
            val1 = (input[n+1] << 8) + (input[n]);
            val2 = (input[n+3] << 8) + (input[n+2]);
            val1 = (val1+val2)/2;
            output[outputIndex++] = (byte)(val1);
            output[outputIndex++] = (byte)(val1 >> 8);
        }
        return output;
    }
}
