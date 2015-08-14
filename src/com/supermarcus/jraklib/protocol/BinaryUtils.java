package com.supermarcus.jraklib.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class BinaryUtils {
    public static final byte[] MAGIC = {0, -1, -1, 0, -2, -2, -2, -2, -3, -3, -3, -3, 18, 52, 86, 120};

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private ByteBuffer buffer;

    public BinaryUtils(ByteBuffer buffer){
        this.buffer = Objects.requireNonNull(buffer);
    }

    public void putMagic(){
        this.getBuffer().put(BinaryUtils.MAGIC);
    }

    public void putLTriad(int value){
        this.getBuffer().put((byte)(value & 0xFF));
        this.getBuffer().put((byte)((value >> 8) & 0xFF));
        this.getBuffer().put((byte)((value >> 16) & 0xFF));
    }

    public void putString(String value){
        this.putString(value, BinaryUtils.DEFAULT_CHARSET);
    }

    public void putString(String value, Charset charset){
        byte[] bytes = value.getBytes(charset);
        this.getBuffer().putShort((short) bytes.length);
        this.getBuffer().put(bytes);
    }

    public void getMagic(){
        this.getBytes(BinaryUtils.MAGIC.length);
    }

    public String getString(){
        return this.getString(this.getBuffer().getShort());
    }

    public String getString(int length){
        return this.getString(length, BinaryUtils.DEFAULT_CHARSET);
    }

    public String getString(int length, Charset charset){
        return new String(this.getBytes(length), charset);
    }

    public boolean checkMagic(){
        return Arrays.equals(this.getBytes(BinaryUtils.MAGIC.length), BinaryUtils.MAGIC);
    }

    public int getLTriad(){
        return (this.getBuffer().get() & 0xFF) | ((this.getBuffer().get() & 0xFF) << 8) | ((this.getBuffer().get() & 0x0F) << 16);
    }

    public byte[] getBytes(int length){
        byte[] buffer = new byte[length];
        this.getBuffer().get(buffer);
        return buffer;
    }

    private ByteBuffer getBuffer(){
        return this.buffer;
    }
}
