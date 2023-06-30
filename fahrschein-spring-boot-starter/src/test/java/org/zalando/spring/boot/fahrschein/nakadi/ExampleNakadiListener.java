package org.zalando.spring.boot.fahrschein.nakadi;

import lombok.extern.slf4j.Slf4j;
import org.zalando.fahrschein.EventAlreadyProcessedException;
import org.zalando.spring.boot.fahrschein.nakadi.stereotype.NakadiEventListener;

import java.io.IOException;
import java.util.List;

@Slf4j
@NakadiEventListener("example")
public class ExampleNakadiListener implements NakadiListener<ExampleEvent>{

    @Override
    public void accept(List<ExampleEvent> events) throws IOException, EventAlreadyProcessedException {
        log.info("GOT EXAMPLE_EVENT : {}", events);
    }

    @Override
    public Class<ExampleEvent> getEventType() {
        return ExampleEvent.class;
    }

}
