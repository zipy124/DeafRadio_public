package com.example.zmorgan.deafradio;

interface BufferListener {

    void finished(byte[] data,int sampleRate);
}
