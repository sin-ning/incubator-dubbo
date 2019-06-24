package com.joysrun.extension.t1;

import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

import java.util.List;

/**
 * run
 *
 * @author Sin
 * @time 2019/6/14 5:36 PM
 */
@SPI("run1")
public interface Run {

    @Adaptive
    void run(List<String> users);
}
