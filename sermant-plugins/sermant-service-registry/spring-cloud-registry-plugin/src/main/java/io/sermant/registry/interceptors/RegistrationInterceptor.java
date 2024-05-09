/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.RegisterServiceCommonConfig;
import io.sermant.registry.config.grace.GraceHelper;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.entity.FixedResult;
import io.sermant.registry.services.RegisterCenterService;
import io.sermant.registry.support.RegisterSwitchSupport;
import io.sermant.registry.utils.CommonUtils;
import io.sermant.registry.utils.ZoneUtils;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * Intercept to get a list of services
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class RegistrationInterceptor extends RegisterSwitchSupport {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!(context.getArguments()[0] instanceof Registration)) {
            return context;
        }
        final Registration registration = (Registration) context.getArguments()[0];
        GraceHelper.configWarmUpParams(registration.getMetadata(),
                PluginConfigManager.getPluginConfig(GraceConfig.class));
        ZoneUtils.setZone(registration.getMetadata());
        fillClientInfo(registration);
        if (super.isEnabled()) {
            final RegisterCenterService service = PluginServiceManager.getPluginService(RegisterCenterService.class);
            final FixedResult fixedResult = new FixedResult();
            service.register(fixedResult);
            if (fixedResult.isSkip()) {
                context.skip(fixedResult.getResult());
            }
        }
        return context;
    }

    private void fillClientInfo(Registration registration) {
        RegisterContext.INSTANCE.getClientInfo().setHost(registration.getHost());
        RegisterServiceCommonConfig config = PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        RegisterContext.INSTANCE.getClientInfo().setMeta(CommonUtils.putSecureToMetaData(registration.getMetadata(),
                config));
        RegisterContext.INSTANCE.getClientInfo().setPort(registration.getPort());
        RegisterContext.INSTANCE.getClientInfo().setServiceId(registration.getServiceId());
    }

    @Override
    protected boolean isEnabled() {
        return super.isEnabled() || isEnableGrace();
    }

    private boolean isEnableGrace() {
        return PluginConfigManager.getPluginConfig(GraceConfig.class).isEnableSpring();
    }
}