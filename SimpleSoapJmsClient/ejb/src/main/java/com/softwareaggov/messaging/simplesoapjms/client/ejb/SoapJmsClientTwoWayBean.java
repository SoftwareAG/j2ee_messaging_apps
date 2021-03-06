/*
 * Copyright © 2016 - 2018 Software AG, Darmstadt, Germany and/or its licensors
 *
 * SPDX-License-Identifier: Apache-2.0
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

package com.softwareaggov.messaging.simplesoapjms.client.ejb;

import com.softwareaggov.messaging.libs.interop.MessageInterop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;

/**
 * Created by fabien.sanglier on 6/20/18.
 */
@Stateless(name = "SoapJmsClientTwoWayService")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(SoapClientLocal.class)
@Remote(MessageInterop.class)
public class SoapJmsClientTwoWayBean extends BaseSoapJmsClient {
    private static Logger log = LoggerFactory.getLogger(SoapJmsClientTwoWayBean.class);

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String callWS(String content) {
        if(log.isDebugEnabled())
            log.debug("WS content:" + content);

        messageProcessingCounter.incrementAndGet(getBeanName() + "-callWS");

        String result = testWebServicePort.processAndReply(content);
        if(log.isDebugEnabled())
            log.debug("WS result:" + ((null != result)?result:"null"));

        return result;
    }
}
