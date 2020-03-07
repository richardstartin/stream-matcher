package io.github.richardstartin.streammatcher.search.shiftand.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

class UnsafeAccess {

    static final Unsafe UNSAFE;
    static final int BYTE_ARRAY_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
