package org.zalando.spring.boot.fahrschein.nakadi;

import org.slf4j.Logger;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SimpleBean implements SmartLifecycle {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SimpleBean.class);
	private final AtomicBoolean running = new AtomicBoolean(false);

	@Override
	public void start() {
		log.info("START ....");
		if (isRunning()) {
			log.info("ALREADY STARTED, SKIP START ....");
			return;
		}
		running.set(true);
		log.info("STARTED : {}", isRunning());
	}

	@Override
	public void stop() {
		log.info("STOP ...");
		if (!isRunning()) {
			log.info("ALREADY STOPPED, SKIP STOP ...");
			return;
		}
		running.set(false);
		log.info("STOPPED : {}", !isRunning());
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

}
