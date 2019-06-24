package com.joysrun.extension;

import com.joysrun.extension.t1.Run;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * extension 测试
 *
 * @author Sin
 * @time 2019/6/14 5:36 PM
 */
public class ExtensionLoaderTest {

    List<String> users = new ArrayList<>();

    @BeforeEach
    public void setup() {
        users.add("a1");
        users.add("a2");
        users.add("a3");
        users.add("a4");
        users.add("a5");
    }

    @Test
    public void runWrapperInvokerTest() {
        ExtensionLoader<Run> loader = ExtensionLoader.getExtensionLoader(Run.class);
        Run run = loader.getExtension("run2");
        run.run(users);
    }

    @Test
    public void loaderDefaultNameTest() {
        ExtensionLoader<Run> loader = ExtensionLoader.getExtensionLoader(Run.class);

        // 可以获取默认
        Run defaultRun = loader.getDefaultExtension();
        // 也可以指定获取
        Run run2 = loader.getExtension("run2");
        defaultRun.run(users);
        run2.run(users);
    }
}
