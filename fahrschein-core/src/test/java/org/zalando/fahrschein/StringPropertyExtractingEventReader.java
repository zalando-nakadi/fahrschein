package org.zalando.fahrschein;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public class StringPropertyExtractingEventReader extends PropertyExtractingEventReader<String> {

    public StringPropertyExtractingEventReader(String propertyName) {
        super(propertyName);
    }

    @Override
    protected String getValue(JsonParser jsonParser) throws IOException {
        return jsonParser.getText();
    }
}
