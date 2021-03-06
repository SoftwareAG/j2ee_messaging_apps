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

package com.softwareaggov.messaging.simplejmsconsume.ejb.subscribe.processor;

import com.softwareaggov.messaging.libs.jms.processor.MessageProcessor;
import com.softwareaggov.messaging.libs.jms.processor.ProcessorOutput;
import com.softwareaggov.messaging.libs.jms.processor.impl.MessageCloneProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by fabien.sanglier on 6/23/16.
 */

@Stateless(name = "MessageCloneProcessor")
@TransactionManagement(TransactionManagementType.BEAN)
@Local(MessageProcessorLocal.class)
public class MessageCloneBean implements MessageProcessorLocal {
    private static Logger log = LoggerFactory.getLogger(MessageCloneBean.class);

    private static final String PROPS_KEYVAL_DELIM = "=";
    private static final String PROPS_DELIM = ";";

    @Resource(name = "mockSleepTimeInMillis")
    private Long mockSleepTimeInMillis = 0L;

    @Resource(name = "overwritePayloadEnabled")
    private Boolean overwritePayloadEnabled;

    @Resource(name = "msgPayloadOverride")
    private String msgPayloadOverride;

    @Resource(name = "msgPayloadOverrideFilePath")
    private String msgPayloadOverrideFilePath;

    @Resource(name = "overwritePropertiesEnabled")
    private Boolean overwritePropertiesEnabled;

    @Resource(name = "msgPropertiesOverride")
    private String msgPropertiesOverride;

    @Resource(name = "mergePropertiesEnabled")
    private Boolean mergePropertiesEnabled;

    private MessageProcessor processor;

    @PostConstruct
    public void initialize() {
        //if msgPayloadOverrideFilePath is set, try and read the file
        String msgPayloadOverrideStr = null;
        if (null != msgPayloadOverrideFilePath && !"".equals(msgPayloadOverrideFilePath)) {
            InputStream inputStream = null;
            try {
                if (msgPayloadOverrideFilePath.toLowerCase().startsWith("classpath:")) {
                    inputStream = getClass().getResourceAsStream(
                            msgPayloadOverrideFilePath.substring("classpath:".length())
                    );
                } else {
                    if (msgPayloadOverrideFilePath.toLowerCase().startsWith("file:"))
                        inputStream = new FileInputStream(msgPayloadOverrideFilePath.substring("file:".length()));
                    else
                        inputStream = new FileInputStream(msgPayloadOverrideFilePath);
                }

                if (null != inputStream) {
                    StringBuilder textBuilder = new StringBuilder();
                    Reader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader
                                (inputStream, Charset.forName("UTF-8")));
                        int c = 0;
                        while ((c = reader.read()) != -1) {
                            textBuilder.append((char) c);
                        }
                    } finally {
                        if (null != reader) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                throw new EJBException("Could not close the reader", e);
                            }
                            reader = null;
                        }
                    }

                    msgPayloadOverrideStr = textBuilder.toString();
                }
            } catch (IOException e) {
                throw new EJBException("Could not load the content of file identified by path: " + msgPayloadOverrideFilePath, e);
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        throw new EJBException("Could not close the input stream", e);
                    }
                    inputStream = null;
                }
            }
        } else {
            msgPayloadOverrideStr = msgPayloadOverride;
        }

        //need to parse message properties from a string into a map, following format: key1=val1;key2=val2;key3=val3
        Map<String, Object> props = new HashMap();
        if (null != msgPropertiesOverride && !"".equals(msgPropertiesOverride)) {
            StringTokenizer st = new StringTokenizer(msgPropertiesOverride, PROPS_DELIM, false);

            String propPairStr;
            String[] propPair;
            while (st.hasMoreTokens()) {
                propPairStr = st.nextToken();
                propPair = propPairStr.split(PROPS_KEYVAL_DELIM);
                if (propPair.length == 1)
                    props.put(propPair[0], null);
                else if (propPair.length > 1)
                    props.put(propPair[0], propPair[1]);
            }
        }

        processor = new MessageCloneProcessor(
                overwritePayloadEnabled,
                msgPayloadOverrideStr,
                overwritePropertiesEnabled,
                props,
                mergePropertiesEnabled
        );
    }

    @Override
    public ProcessorOutput processMessage(Message msg) throws JMSException {
        return processor.processMessage(msg);
    }
}