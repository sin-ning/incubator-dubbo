/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.cluster.configurator;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.cluster.Configurator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AbstractOverrideConfigurator
 */
public abstract class AbstractConfigurator implements Configurator {

    private final URL configuratorUrl;

    public AbstractConfigurator(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("configurator url == null");
        }
        this.configuratorUrl = url;
    }

    @Override
    public URL getUrl() {
        return configuratorUrl;
    }

    @Override
    public URL configure(URL url) {
        // 如果未启用覆盖url或无效，则只返回。
        // If override url is not enabled or is invalid, just return.
        if (!configuratorUrl.getParameter(Constants.ENABLED_KEY, true) || configuratorUrl.getHost() == null || url == null || url.getHost() == null) {
            return url;
        }
        /**
         * 这个if分支是从2.7.0开始创建的。
         *
         * This if branch is created since 2.7.0.
         */
        String apiVersion = configuratorUrl.getParameter(Constants.CONFIG_VERSION_KEY);
        if (StringUtils.isNotEmpty(apiVersion)) {
            // old 和 current url 的 side
            String currentSide = url.getParameter(Constants.SIDE_KEY);
            String configuratorSide = configuratorUrl.getParameter(Constants.SIDE_KEY);

            // port=0：获取本地ip
            if (currentSide.equals(configuratorSide) && Constants.CONSUMER.equals(configuratorSide) && 0 == configuratorUrl.getPort()) {
                url = configureIfMatch(NetUtils.getLocalHost(), url);
                // 服务提供者
            } else if (currentSide.equals(configuratorSide) && Constants.PROVIDER.equals(configuratorSide) && url.getPort() == configuratorUrl.getPort()) {
                url = configureIfMatch(url.getHost(), url);
            }
        }
        /**
         * 不推荐使用此else分支，仅保留与2.7.0之前版本的兼容性
         *
         * This else branch is deprecated and is left only to keep compatibility with versions before 2.7.0
         */
        else {
            url = configureDeprecated(url);
        }
        return url;
    }

    @Deprecated
    private URL configureDeprecated(URL url) {
        // If override url has port, means it is a provider address. We want to control a specific provider with this override url, it may take effect on the specific provider instance or on consumers holding this provider instance.
        if (configuratorUrl.getPort() != 0) {
            if (url.getPort() == configuratorUrl.getPort()) {
                return configureIfMatch(url.getHost(), url);
            }
        } else {// override url don't have a port, means the ip override url specify is a consumer address or 0.0.0.0
            // 1.If it is a consumer ip address, the intention is to control a specific consumer instance, it must takes effect at the consumer side, any provider received this override url should ignore;
            // 2.If the ip is 0.0.0.0, this override url can be used on consumer, and also can be used on provider
            if (url.getParameter(Constants.SIDE_KEY, Constants.PROVIDER).equals(Constants.CONSUMER)) {
                return configureIfMatch(NetUtils.getLocalHost(), url);// NetUtils.getLocalHost is the ip address consumer registered to registry.
            } else if (url.getParameter(Constants.SIDE_KEY, Constants.CONSUMER).equals(Constants.PROVIDER)) {
                return configureIfMatch(Constants.ANYHOST_VALUE, url);// take effect on all providers, so address must be 0.0.0.0, otherwise it won't flow to this if branch
            }
        }
        return url;
    }

    private URL configureIfMatch(String host, URL url) {

        // 0.0.0.0 如果是配置的 url，进入匹配
        if (Constants.ANYHOST_VALUE.equals(configuratorUrl.getHost()) || host.equals(configuratorUrl.getHost())) {
            // TODO, to support wildcards
            // 提供者地址
            String providers = configuratorUrl.getParameter(Constants.OVERRIDE_PROVIDERS_KEY);
            if (StringUtils.isEmpty(providers) || providers.contains(url.getAddress()) || providers.contains(Constants.ANYHOST_VALUE)) {
                // 配置文件程序名称
                String configApplication = configuratorUrl.getParameter(Constants.APPLICATION_KEY,
                        configuratorUrl.getUsername());
                // 当前程序名称
                String currentApplication = url.getParameter(Constants.APPLICATION_KEY, url.getUsername());
                if (configApplication == null || Constants.ANY_VALUE.equals(configApplication)
                        || configApplication.equals(currentApplication)) {

                    // 需要删除的 keys
                    Set<String> conditionKeys = new HashSet<String>();
                    conditionKeys.add(Constants.CATEGORY_KEY);
                    conditionKeys.add(Constants.CHECK_KEY);
                    conditionKeys.add(Constants.DYNAMIC_KEY);
                    conditionKeys.add(Constants.ENABLED_KEY);
                    conditionKeys.add(Constants.GROUP_KEY);
                    conditionKeys.add(Constants.VERSION_KEY);
                    conditionKeys.add(Constants.APPLICATION_KEY);
                    conditionKeys.add(Constants.SIDE_KEY);
                    conditionKeys.add(Constants.CONFIG_VERSION_KEY);

                    // 值从 configuration 中复制到
                    for (Map.Entry<String, String> entry : configuratorUrl.getParameters().entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        // ~ 开头、application、side 才能进来
                        if (key.startsWith("~") || Constants.APPLICATION_KEY.equals(key) || Constants.SIDE_KEY.equals(key)) {
                            conditionKeys.add(key);
                            // value != null and value != * and value != key
                            if (value != null && !Constants.ANY_VALUE.equals(value)
                                    // value != key 本身
                                    && !value.equals(url.getParameter(key.startsWith("~") ? key.substring(1) : key))) {
                                return url;
                            }
                        }
                    }

                    // 删除 configuration 中的 keys
                    return doConfigure(url, configuratorUrl.removeParameters(conditionKeys));
                }
            }
        }
        return url;
    }

    protected abstract URL doConfigure(URL currentUrl, URL configUrl);

}
