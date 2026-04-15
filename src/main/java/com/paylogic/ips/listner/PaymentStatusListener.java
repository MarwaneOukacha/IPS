package com.paylogic.ips.listner;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gms.utils.exception.BusinessException;
import com.paylogic.ips.event.PaymentSentEvent;
import com.paylogic.ips.services.OutGoingIpsService;
import com.paylogic.ips.util.CoreUtil;




@Component
public class PaymentStatusListener {

    private static final Logger LOG = Logger.getLogger(PaymentStatusListener.class);

    @Autowired
    private OutGoingIpsService service;

    @Async
    @EventListener
    public void handlePaymentSent(PaymentSentEvent event) {
        LOG.info("Thread: " + Thread.currentThread().getName());

        String traceReference = event.getTraceReference();
        LOG.info("Received PaymentSentEvent for traceReference: " + traceReference);

        try {
            int[] delays = {100000, 150000, 200000};

            Thread.sleep(100000); 

            for (int i = 0; i < delays.length; i++) {

                LOG.info("Attempt " + (i + 1) + " for traceReference: " + traceReference);

                try {
                    boolean success = service.processStatusFromIPS(traceReference);

                    if (success) {
                        LOG.info("Status processed successfully on attempt " + (i + 1));
                        return;
                    }

                } catch (BusinessException e) {

                    if ("Transaction not found".equals(e.getMessage())) {
                        LOG.warn("Transaction not found, retrying after 1 minute...");

                        Thread.sleep(60000); //  1 minute

                        continue; // retry same loop
                    } else {
                        throw e; // autre erreur → stop
                    }
                } 

                LOG.info("Attempt failed, retrying after " + delays[i] + "ms");
                Thread.sleep(delays[i]);
            }

            LOG.warn("All attempts failed for traceReference: " + traceReference);

        } catch (Exception e) {
            LOG.error("Error processing PaymentSentEvent for traceReference: " + traceReference, e);
        }
    }
    
}   