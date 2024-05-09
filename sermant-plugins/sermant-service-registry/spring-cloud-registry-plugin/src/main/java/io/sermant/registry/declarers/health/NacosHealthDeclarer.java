/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.declarers.health;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.registry.declarers.AbstractDoubleRegistryDeclarer;
import io.sermant.registry.interceptors.health.NacosHealthInterceptor;

/**
 * Nacos Health Check Enhancements NamingProxy 1.x and NamingHttpClientProxy 2.x
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class NacosHealthDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * Nacos heartbeat sending class
     */
    private static final String[] ENHANCE_CLASSES = new String[] {
        "com.alibaba.nacos.client.naming.net.NamingProxy",
        "com.alibaba.nacos.client.naming.remote.http.NamingHttpClientProxy"
    };

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = NacosHealthInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameEquals("sendBeat"), INTERCEPT_CLASS)
        };
    }
}