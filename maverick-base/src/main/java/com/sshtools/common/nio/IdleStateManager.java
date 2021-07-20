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


package com.sshtools.common.nio;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that checks the idle state of another class.
 *
 * @author Lee David Painter
 */
public class IdleStateManager  {

    Map<IdleStateListener, Long> listeners = new ConcurrentHashMap<IdleStateListener, Long>(50, 0.9f, 1);
    int numSecondsBeforeIdle;
    int servicePeriodSeconds;
    int numInactiveServicesPeriodsPerIdle;
    long lastService = 0;
    boolean servicing = false;

    public IdleStateManager(int servicePeriodSeconds,
                            int numInactiveServicesPeriodsPerIdle) {
        this.servicePeriodSeconds = servicePeriodSeconds;
        this.numInactiveServicesPeriodsPerIdle = numInactiveServicesPeriodsPerIdle;
    }

    /**
     * Called by a listener when they want their idle state to be reset.
     *
     * @param obj IdleStateListener
     */
    public synchronized void reset(IdleStateListener obj) {
    	if(listeners.containsKey(obj))
    		listeners.put(obj, new Long(System.currentTimeMillis()));
    }
    
    public synchronized void register(IdleStateListener obj) {
    	listeners.put(obj, new Long(System.currentTimeMillis()));
    }

    /**
     * Called by a listener when they want to remove themselves
     * @param obj IdleStateListener
     */
    public synchronized void remove(IdleStateListener obj) {
    	if(!servicing)
    		listeners.remove(obj);
    }

    /**
     * Called by a selector to determine when the service run is ready to
     * be executed.
     *
     * @return boolean
     */
    public boolean isReady() {
        return ((System.currentTimeMillis() - lastService) / 1000) >= servicePeriodSeconds;
    }

    /**
     * Called by a thread which is managing idle states
     */
    public synchronized void service() {

        lastService = System.currentTimeMillis();

        Map.Entry<IdleStateListener, Long> entry;

        servicing = true;
        for(Iterator<Map.Entry<IdleStateListener, Long>> it = listeners.entrySet().iterator();
                          it.hasNext();) {
            entry = it.next();
            long start = entry.getValue().longValue();
            long current = System.currentTimeMillis();
            long elasped = (current - start) / 1000;
            if(elasped >= (servicePeriodSeconds * numInactiveServicesPeriodsPerIdle)) {
               if((entry.getKey()).idle())
                   it.remove();
           }
        }
        servicing = false;

    }


}
