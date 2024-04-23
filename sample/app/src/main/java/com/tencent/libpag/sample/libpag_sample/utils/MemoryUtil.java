package com.tencent.libpag.sample.libpag_sample.utils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * Created by Yongle on 2019-12-10.
 */
public class MemoryUtil {
    private static final String TAG = "MemoryUtil";

    public static MemoryFile writeToMemoryFile(byte[] data) {
        MemoryFile mf = null;
        try {
            mf = new MemoryFile(Thread.currentThread().getName(), data.length);
            mf.writeBytes(data, 0, 0, data.length);

        } catch (IOException e) {
            e.printStackTrace();
            if (mf != null) {
                mf.close();
            }
        }
        return mf;
    }

    public static ParcelFileDescriptor getFileDescriptor(MemoryFile mf) {
        ParcelFileDescriptor pfd = null;
        try {
            Method mGetDeclaredField = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
            mGetDeclaredField.setAccessible(true);
            FileDescriptor fd = (FileDescriptor) mGetDeclaredField.invoke(mf);
            pfd = fd != null ? ParcelFileDescriptor.dup(fd) : null;

        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            if (mf != null) {
                mf.close();
            }
        }
        return pfd;
    }

    public static ParcelFileDescriptor write2Memory(byte[] data) {
        MemoryFile mf = null;
        try {
            mf = new MemoryFile(Thread.currentThread().getName(), data.length);
            mf.writeBytes(data, 0, 0, data.length);
            Method mGetDeclaredField = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
            mGetDeclaredField.setAccessible(true);
            FileDescriptor fileDescriptor = (FileDescriptor) mGetDeclaredField.invoke(mf);
            return fileDescriptor != null ? ParcelFileDescriptor.dup(fileDescriptor) : null;
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            if (mf != null) {
                mf.close();
            }
        }
        return null;
    }

    public static byte[] getFromMemory(ParcelFileDescriptor descriptor, int length) {
        FileInputStream fis = new FileInputStream(descriptor.getFileDescriptor());
        byte[] data = new byte[length];
        try {
            fis.read(data);
        } catch (IOException e) {
            Log.d(TAG, "getFromMemory IOException: ");
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                Log.d(TAG, "getFromMemory fis.close Exception: ");
                e.printStackTrace();
            }
        }
        return data.length > 0 ? data : null;
    }

}