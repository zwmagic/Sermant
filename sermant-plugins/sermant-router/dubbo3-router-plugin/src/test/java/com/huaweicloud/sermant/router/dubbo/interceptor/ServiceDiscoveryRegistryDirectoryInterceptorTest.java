/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.service.InvokerRuleStrategyService;

import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.MetadataInfo;
import org.apache.dubbo.registry.client.DefaultServiceInstance;
import org.apache.dubbo.registry.client.InstanceAddressURL;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * dubbo3.x instance注册类型时设置应用名
 *
 * @author chengyouling
 * @since 2024-03-18
 */
public class ServiceDiscoveryRegistryDirectoryInterceptorTest {
    private static final URL APACHE_URL = URL
            .valueOf("dubbo://localhost:8081/com.demo.foo.FooTest?foo=bar&version=0.0.1");

    private static final String SERVICE_NAME = "test-service";

    private static final String SERVICE_INTERFACE = "com.demo.foo.FooTest";

    private final ServiceDiscoveryRegistryDirectoryInterceptor interceptor;

    private final InstanceAddressURL instanceAddressURL;

    private static MockedStatic<PluginServiceManager> mockServiceManager;

    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    /**
     * 构造方法
     */
    public ServiceDiscoveryRegistryDirectoryInterceptorTest() {
        RpcContext.getServiceContext().setConsumerUrl(APACHE_URL);
        InvokerRuleStrategyService invokerRuleStrategyService = Mockito.mock(InvokerRuleStrategyService.class);
        mockServiceManager = Mockito.mockStatic(PluginServiceManager.class);
        mockServiceManager.when(() -> PluginServiceManager.getPluginService(InvokerRuleStrategyService.class))
                .thenReturn(invokerRuleStrategyService);
        interceptor = new ServiceDiscoveryRegistryDirectoryInterceptor();
        DefaultServiceInstance instance = new DefaultServiceInstance();
        instance.setHost("127.0.0.1");
        instance.setPort(8090);
        instance.setServiceName("test-service");
        Map<String, String> meta = new HashMap<>();
        meta.put("az", "az1");
        meta.put("region", "region1");
        instance.setMetadata(meta);
        Map<String, MetadataInfo.ServiceInfo > services = new HashMap<>();
        MetadataInfo.ServiceInfo serviceInfo = new MetadataInfo.ServiceInfo();
        Map<String, String> params = new HashMap<>();
        params.put("az2", "az2");
        params.put("region2", "region2");
        serviceInfo.setParams(params);
        services.put("com.demo.foo.FooTest:0.0.1:dubbo", serviceInfo);
        MetadataInfo metadataInfo = new MetadataInfo("app", "0.0.1", services);
        instanceAddressURL = new InstanceAddressURL(instance, metadataInfo);
    }

    /**
     * 测试设置应用名
     */
    @Test
    public void testBefore() {
        Object[] arguments = new Object[1];
        arguments[0] = instanceAddressURL;
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null,
                arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(SERVICE_NAME, DubboCache.INSTANCE.getApplication(SERVICE_INTERFACE));
    }
}