package org.zalando.spring.boot.fahrschein.nakadi;

import org.slf4j.Logger;
import org.zalando.fahrschein.EventAlreadyProcessedException;
import org.zalando.spring.boot.fahrschein.nakadi.stereotype.NakadiEventListener;

import java.io.IOException;
import java.util.List;

@NakadiEventListener("example")
public class ExampleNakadiListener implements NakadiListener<ExampleEvent> {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExampleNakadiListener.class);

    @Override
    public void accept(List<ExampleEvent> events) throws IOException, EventAlreadyProcessedException {
        log.info("GOT EXAMPLE_EVENT : {}", events);
    }

    @Override
    public Class<ExampleEvent> getEventType() {
        return ExampleEvent.class;
    }

}
