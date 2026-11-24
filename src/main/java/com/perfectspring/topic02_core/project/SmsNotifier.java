package com.perfectspring.topic02_core.project;

import org.springframework.stereotype.Component;

/** Candidate #2, with the bean name "smsNotifier". Chosen with @Qualifier("smsNotifier"). */
@Component("smsNotifier")
public class SmsNotifier implements Notifier {

    @Override
    public void notify(String student, String message) {
        System.out.println("   [SMS to " + student + "] " + message);
    }
}
