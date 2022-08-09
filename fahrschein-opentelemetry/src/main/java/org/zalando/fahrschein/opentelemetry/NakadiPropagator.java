package org.zalando.fahrschein.opentelemetry;

import java.util.Collection;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

public class NakadiPropagator implements TextMapPropagator {

	public static final String FIELD_TRACE_ID = "trace-id";
	
	public static final String FIELD_SPAN_ID = "span-id";
	
	@Override
	public Collection<String> fields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
		// TODO Auto-generated method stub

	}

	@Override
	public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
		// TODO Auto-generated method stub
		return null;
	}

}
