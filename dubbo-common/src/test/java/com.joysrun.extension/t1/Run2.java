package com.joysrun.extension.t1;

import org.apache.dubbo.common.extension.Adaptive;

import java.util.List;

/**
 * @author Sin
 * @time 2019/6/14 5:37 PM
 */
public class Run2 implements Run {

    @Override
    public void run(List<String> users) {
        System.err.println("run2 -> " + users.size());
    }
}
