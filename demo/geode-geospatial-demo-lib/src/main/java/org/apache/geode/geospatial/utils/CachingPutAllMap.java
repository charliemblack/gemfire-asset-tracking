/*
 * Copyright [2016] Charlie Black
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geode.geospatial.utils;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Charlie Black on 5/2/16.
 */
public class CachingPutAllMap implements ConcurrentMap, InitializingBean, BeanNameAware {
    private String beanName = "sendToGemfire";
    private Map wrappedMap;
    private HashMap bulkMap = new HashMap();

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = reentrantReadWriteLock.readLock();
    private final Lock writeLock = reentrantReadWriteLock.writeLock();

    private int batchSize = 100;
    private long timeoutms = 1000;
    private ThreadPoolTaskExecutor executor;
    private ThreadPoolTaskScheduler scheduler;
    private boolean callerSends = true;

    private final MetricRegistry metrics = new MetricRegistry();
    private final Timer sendToGemfire = metrics.timer("sendToGemFire");
    private final Meter putMeter = metrics.meter("puts");
    private final Meter getMeter = metrics.meter("gets");
    private final Histogram sendToGemFireSize = metrics.histogram("sendToGemFireSize");


    public void setCallerSends(boolean callerSends) {
        this.callerSends = callerSends;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduler.scheduleAtFixedRate((Runnable) () -> push(), timeoutms);
        JmxReporter reporter = JmxReporter.forRegistry(metrics).inDomain(beanName).build();
        reporter.start();
    }

    private void push() {
        writeLock.lock();
        try {
            if (!bulkMap.isEmpty()) {
                sendToGemFireSize.update(bulkMap.size());
                if (callerSends) {
                    callerBlocksPush();
                } else {
                    asyncPush();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void callerBlocksPush() {
        Timer.Context context = sendToGemfire.time();
        try {
            wrappedMap.putAll(bulkMap);
        } finally {
            context.stop();
        }
        bulkMap.clear();
    }

    private void asyncPush() {
        final HashMap temp = bulkMap;
        executor.execute(() -> {
            Timer.Context context = sendToGemfire.time();
            try {
                wrappedMap.putAll(temp);
            } finally {
                context.stop();
            }
        });
        bulkMap = new HashMap();
    }


    @Required
    public void setWrappedMap(Map wrappedMap) {
        this.wrappedMap = wrappedMap;
    }

    @Required
    public void setScheduler(ThreadPoolTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Required
    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setTimeout(long timeoutms) {
        this.timeoutms = timeoutms;
    }

    public void setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeoutms = timeUnit.toMillis(timeout);
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return keySet().size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return bulkMap.isEmpty() || wrappedMap.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return bulkMap.containsKey(key) || wrappedMap.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return bulkMap.containsValue(value) || wrappedMap.containsValue(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Object get(Object key) {
        getMeter.mark();
        readLock.lock();
        try {
            Object value = bulkMap.get(key);
            if (value == null) {
                value = wrappedMap.get(key);
            }
            return value;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Object put(Object key, Object value) {
        putMeter.mark();
        writeLock.lock();
        try {
            Object returnValue = bulkMap.put(key, value);
            if (bulkMap.size() >= batchSize) {
                push();
            }
            if (returnValue == null) {
                returnValue = wrappedMap.get(key);
            }
            return returnValue;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Object remove(Object key) {
        writeLock.lock();
        try {
            Object value = bulkMap.remove(key);
            if (value == null) {
                value = wrappedMap.remove(key);
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map m) {
        putMeter.mark(m.size());
        writeLock.lock();
        try {
            bulkMap.putAll(m);
            if (bulkMap.size() >= batchSize) {
                push();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            bulkMap.clear();
            wrappedMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set keySet() {
        readLock.lock();
        try {
            Set keys = new HashSet(wrappedMap.keySet());
            keys.addAll(bulkMap.keySet());
            return keys;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection values() {
        readLock.lock();
        try {
            HashMap map = new HashMap(wrappedMap);
            map.putAll(bulkMap);
            return map.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<Entry> entrySet() {
        readLock.lock();
        try {
            HashMap map = new HashMap(wrappedMap);
            map.putAll(bulkMap);
            return map.entrySet();
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public Object putIfAbsent(Object key, Object value) {
        putMeter.mark();
        Object returnValue = null;
        writeLock.lock();
        try {
            returnValue = bulkMap.get(key);
            if (returnValue == null) {
                returnValue = wrappedMap.get(key);
                if (returnValue == null) {
                    bulkMap.put(key, value);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return returnValue;
    }

    @Override
    public boolean remove(Object key, Object value) {
        writeLock.lock();
        try {
            if ((bulkMap.containsKey(key) && Objects.equals(bulkMap.get(key), value)) ||
                    (wrappedMap.containsKey(key) && Objects.equals(wrappedMap.get(key), value))) {
                bulkMap.remove(key);
                wrappedMap.remove(key);
                return true;
            } else {
                return false;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        putMeter.mark();
        writeLock.lock();
        try {
            if ((bulkMap.containsKey(key) && Objects.equals(bulkMap.get(key), oldValue)) ||
                    (wrappedMap.containsKey(key) && Objects.equals(wrappedMap.get(key), oldValue))) {
                bulkMap.put(key, newValue);
                return true;
            } else {
                return false;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Object replace(Object key, Object value) {
        putMeter.mark();
        writeLock.lock();
        try {
            if (bulkMap.containsKey(key)) {
                return bulkMap.put(key, value);
            } else if (wrappedMap.containsKey(key)) {
                Object returnValue = wrappedMap.get(key);
                bulkMap.put(key, value);
                return returnValue;
            } else {
                return null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
