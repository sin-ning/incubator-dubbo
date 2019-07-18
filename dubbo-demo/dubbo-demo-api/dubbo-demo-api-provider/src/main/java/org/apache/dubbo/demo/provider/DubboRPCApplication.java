package org.apache.dubbo.demo.provider;

import com.google.common.collect.Lists;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.demo.DemoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DubboRPCApplication {

    public static void main(String[] args) throws IOException {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("DubboRPCApplication");

        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");

        RegistryConfig registry2 = new RegistryConfig();
        registry2.setAddress("zookeeper://127.0.0.1:2182");

        // 服务提供者协议配置
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setPort(12345);
        protocol.setThreads(200);

        List<RegistryConfig> registries = new ArrayList<RegistryConfig>(1);
        registries.add(registry);
        registries.add(registry2);

        // 服务提供者暴露服务配置
        DemoService demoService = new DemoServiceImpl();
        ServiceConfig<DemoService> service = new ServiceConfig<DemoService>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
        service.setApplication(application);
        service.setRegistries(registries); // 多个注册中心可以用setRegistries()
        service.setProtocol(protocol); // 多个协议可以用setProtocols()
        service.setInterface(DemoService.class);
        service.setRef(demoService);
        service.setVersion("1.0.0");

        // 暴露及注册服务
        service.export();

        System.in.read();
    }
}
