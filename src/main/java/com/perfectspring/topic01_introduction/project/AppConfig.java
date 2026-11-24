package com.perfectspring.topic01_introduction.project;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The HIRING PLAN for Spring's HR office (the IoC container).
 *
 * Spring's rule:   "write your plan in a @Configuration class with @Bean methods."
 * Your job:        follow that rule and describe what to create.
 * Spring's job:    read the plan, create the beans, plug them together.
 */
@Configuration
public class AppConfig {

    /** Which way do announcements travel? Change ONE line to switch. */
    @Bean
    public MessageSender messageSender() {
        return new EmailSender();
        // return new SmsSender();     // 👈 TASK: comment the line above, uncomment this one
    }

    /** The service needs a MessageSender. Spring passes in the bean created above. */
    @Bean
    public AnnouncementService announcementService(MessageSender messageSender) {
        return new AnnouncementService(messageSender);
    }
}
