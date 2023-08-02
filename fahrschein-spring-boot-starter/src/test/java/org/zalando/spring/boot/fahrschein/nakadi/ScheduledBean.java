package org.zalando.spring.boot.fahrschein.nakadi;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledBean {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScheduledBean.class);

	@Scheduled(fixedRate = 250)
	public void scheduledMethod() {
		log.info("RUNNING SCHEDULED METHOD :::");
	}

	@Scheduled(fixedRate = 250)
	public void scheduledSleepMethod() {
		try {
			log.info("RUNNING SLEEP_SCHEDULED METHOD :::");
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}
}
