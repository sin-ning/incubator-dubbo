package com.joysrun.extension.t1;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sin
 * @time 2019/6/14 5:37 PM
 */
public class Run1 implements Run {

    AtomicInteger count = new AtomicInteger(0);

    @Override
    public void run(List<String> users) {
        for (String user : users) {
            System.err.println(user + " -> " + String.valueOf(count.incrementAndGet()));
        }
    }
}
