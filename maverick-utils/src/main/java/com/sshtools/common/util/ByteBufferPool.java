/*
 *    _           _             _   _
 *   (_) __ _  __| | __ _ _ __ | |_(_)_   _____
 *   | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
 *   | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
 *  _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
 * |__/                  |_|
 *
 * This file is part of the Maverick Synergy Hotfixes Java SSH API
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * Copyright (C) 2002-2021 JADAPTIVE Limited - All Rights Reserved
 *
 * Use of this software may also be covered by third-party licenses depending on the choices you make about what features to use.
 *
 * Please visit the link below to see additional third-party licenses and copyrights
 *
 * https://www.jadaptive.com/app/manpage/en/article/1565029/What-third-party-dependencies-does-the-Maverick-Synergy-API-have
 */

package com.sshtools.common.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *  This class provides a pool for either direct or non direct ByteBuffers.
 */
public class ByteBufferPool
{
    private ArrayList<ByteBuffer> pool = new ArrayList<ByteBuffer>();
    private int capacity = 4096;
    private int allocated = 0;
    private long totalDirectMemoryAllocated = 0;
    
    /**
     * Create a default pool of ByteBuffers with 4k capacity
     */
    public ByteBufferPool() {
    }

    /**
     * Create a pool of ByteBuffers.
     *
     * @param capacity int
     * @param direct boolean
     */
    public ByteBufferPool(int capacity, boolean direct) {
        this.capacity=capacity;
    }

    /**
     * Get the capacity of buffers in this pool.
     *
     * @return int
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Get the number of buffers currently allocated out.
     *
     * @return int
     */
    public int getAllocatedBuffers() {
        return allocated;
    }

    /**
     * Get the number of buffers that are ready to be allocated.
     *
     * @return int
     */
    public int getFreeBuffers() {
        return pool.size();
    }

    /**
     * Calculate the total memory in use by this pool.
     *
     * @return long
     */
    public long getTotalMemoryInUse() {
        return totalDirectMemoryAllocated - (pool.size() * capacity);
    }

    /**
     * Calculate the total memory allocated by this pool.
     *
     * @return long
     */
    public synchronized long getTotalMemoryAllocated() {
        return totalDirectMemoryAllocated;
    }

    /**
     * Get a free buffer from the pool.
     *
     * @return ByteBuffer
     */
    public synchronized ByteBuffer get() {
        if (pool.isEmpty()) {

            allocated++;
            ByteBuffer buf = ByteBuffer.allocate(capacity);
            totalDirectMemoryAllocated += capacity;
            return buf;
        }

        
        ByteBuffer buffer = (ByteBuffer)pool.remove(pool.size()-1);
        buffer.clear();
        return buffer;
    }

    /**
     * Add a buffer back to the pool.
     *
     * @param buffer ByteBuffer
     */
    public synchronized void add(ByteBuffer buffer)
    {
        if(buffer==null)
            return;
        if (buffer.capacity()==capacity) {
            buffer.clear();
            pool.add(buffer);
        }
    }



}
