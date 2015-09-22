package com.appsandlabs.audiotools;

import java.io.ByteArrayOutputStream;

/**
 * Created by abhinav on 9/21/15.
 */
public class TByteArrayOutputStream extends ByteArrayOutputStream {

    public TByteArrayOutputStream(int i) {
        super(i);
    }

    public byte[] getBuffer(){
        return buf;
    }
}
