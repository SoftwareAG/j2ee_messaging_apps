package com.softwareaggov.messaging.simplejmssendoneway.ejb.publish;

import com.softwareaggov.messaging.libs.utils.JMSHelper;
import com.softwareaggov.messaging.simplejmssendoneway.ejb.utils.CounterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Map;

/*
 * Simple JMS Publisher bean relying on the connection factory to create the JMS connection on each call
 * Created by fabien.sanglier on 6/15/16.
 */
public abstract class JmsPublisherBase implements JmsPublisher {
    private static Logger log = LoggerFactory.getLogger(JmsPublisherBase.class);

    @EJB
    protected CounterLocal messageProcessingCounter;

    @Resource(name = "jmsSessionTransacted")
    private Boolean jmsSessionTransacted = Boolean.FALSE;

    @Resource(name = "jmsSessionAcknowledgeMode")
    private Integer jmsSessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    @Resource(name = "jmsDeliveryMode")
    private Integer jmsDeliveryMode = null;

    @Resource(name = "jmsPriority")
    private Integer jmsPriority = null;

    @Resource(name = "jmsReplyDestinationName")
    private String jmsReplyDestinationName = null;

    @Resource(name = "jmsReplyDestinationType")
    private String jmsReplyDestinationType = null;

    @Resource(name = "jmsSendEnabled")
    private Boolean isEnabled;

    private volatile boolean init = false;

    protected transient JMSHelper jmsHelper = null;

    private transient Destination jmsReplyTo = null;

    @PostConstruct
    public void ejbCreate() {
        log.info("ejbCreate()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-create");
    }

    @PreDestroy
    public void ejbRemove() throws EJBException {
        log.info("ejbRemove()");
        messageProcessingCounter.incrementAndGet(getBeanName() + "-remove");
        jmsReplyTo = null;
        init = false;
        if (null != jmsHelper)
            jmsHelper.cleanup();
        jmsHelper = null;
    }

    protected String getBeanName() {
        return this.getClass().getSimpleName();
    }

    protected abstract ConnectionFactory getJmsConnectionFactory();

    protected abstract Destination getJmsDestination();

    protected abstract String sendMessage(Destination destination, boolean sessionTransacted, int sessionAcknowledgeMode, final Object payload, final Map<String, Object> headerProperties, Integer deliveryMode, Integer priority, String correlationID, Destination replyTo) throws JMSException;

    @Override
    public boolean isEnabled() {
        return (null != isEnabled) ? isEnabled : false;
    }

    // Initializing resource outside the EJB creation to make sure these lookups get retried until they work
    // (eg. if UM is not available yet when EJB is created)
    private void initJMS() throws JMSException {
        if (!init) {
            synchronized (this.getClass()) {
                if (!init) {
                    try {
                        this.jmsHelper = JMSHelper.createSender(getJmsConnectionFactory());

                        //JMS reply destination
                        if (null != jmsReplyDestinationName && !"".equals(jmsReplyDestinationName) &&
                                null != jmsReplyDestinationType && !"".equals(jmsReplyDestinationType)) {
                            jmsReplyTo = jmsHelper.lookupDestination(jmsReplyDestinationName, jmsReplyDestinationType);
                        }

                        init = true;
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initSuccess");
                    } catch (JMSException e) {
                        messageProcessingCounter.incrementAndGet(getBeanName() + "-initErrors");
                        throw e;
                    }
                }
            }
        }
    }

    public String sendTextMessage(final Object msgTextPayload, final Map<String, Object> msgHeaderProperties) throws JMSException {
        String returnText = "";
        if (log.isDebugEnabled())
            log.debug("in EJB: sendTextMessage");

        try {
            //Initialize JMS objects...once done, will not do it again
            initJMS();

            returnText = sendMessage(getJmsDestination(), jmsSessionTransacted, jmsSessionAcknowledgeMode, msgTextPayload, msgHeaderProperties, jmsDeliveryMode, jmsPriority, null, jmsReplyTo);

            //increment processing counter
            messageProcessingCounter.incrementAndGet(getBeanName() + "-messageSent");

            if (null == returnText) {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseIsNull");
            } else {
                messageProcessingCounter.incrementAndGet(getBeanName() + "-responseNotNull");
            }
        } catch (Exception e) {
            log.error("Exception occurred", e);
            messageProcessingCounter.incrementAndGet(getBeanName() + "-errors");
            throw new EJBException(e);
        }

        return returnText;
    }
}
