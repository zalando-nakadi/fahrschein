package org.zalando.fahrschein;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.util.Collection;

@SuppressWarnings("serial")
public class MetadataTypeResolver implements TypeResolverBuilder<MetadataTypeResolver> {
    private Class<?> defaultImpl;

    @Override
    public Class<?> getDefaultImpl() {
        return defaultImpl;
    }

    @Override
    public MetadataTypeResolver defaultImpl(Class<?> defaultImpl) {
        this.defaultImpl = defaultImpl;
        return this;
    }

    @Override
    public MetadataTypeResolver typeIdVisibility(boolean isVisible) {
        return this;
    }

    @Override
    public MetadataTypeResolver inclusion(JsonTypeInfo.As includeAs) {
        return this;
    }

    @Override
    public MetadataTypeResolver typeProperty(String propName) {
        return this;
    }

    @Override
    public MetadataTypeResolver init(JsonTypeInfo.Id idType, TypeIdResolver res) {
        return this;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        return null;
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
        final TypeNameIdResolver typeNameIdResolver = TypeNameIdResolver.construct(config, baseType, subtypes, false, true);
        return new TypeDeserializerBase(baseType, typeNameIdResolver, null, false, defaultImpl) {

            @Override
            public TypeDeserializer forProperty(BeanProperty prop) {
                return this;
            }

            @Override
            public JsonTypeInfo.As getTypeInclusion() {
                return JsonTypeInfo.As.EXISTING_PROPERTY;
            }

            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
                return deserializeTypedFromObject(p, ctxt);
            }

            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
                return deserializeTypedFromObject(p, ctxt);
            }

            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
                return deserializeTypedFromObject(p, ctxt);
            }

            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
                JsonToken t = p.getCurrentToken();
                if (t == JsonToken.START_OBJECT) {
                    t = p.nextToken();
                } else if (t != JsonToken.FIELD_NAME) {
                    throw new JsonMappingException(p, "Could not extract event type from non-object");
                }

                TokenBuffer tb = new TokenBuffer(p, ctxt);

                for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
                    String name = p.getCurrentName();
                    tb.writeFieldName(name);
                    t = p.nextToken();
                    if (name.equals("metadata")) {
                        if (t != JsonToken.START_OBJECT) {
                            throw new JsonMappingException(p, "Could not extract event type from invalid metadata");
                        }
                        tb.writeStartObject();
                        t = p.nextToken();
                        for (; t == JsonToken.FIELD_NAME; t = p.nextToken()) {
                            name = p.getCurrentName();
                            tb.writeFieldName(name);
                            t = p.nextToken();
                            if (name.equals("event_type")) {
                                if (t != JsonToken.VALUE_STRING) {
                                    throw new JsonMappingException(p, "Could not extract event type from non-string property");
                                }
                                String typeId = p.getText();
                                tb.writeString(typeId);
                                JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
                                JsonParser pb = JsonParserSequence.createFlattened(tb.asParser(p), p);
                                pb.nextToken();
                                return deser.deserialize(pb, ctxt);
                            } else {
                                tb.copyCurrentStructure(p);
                            }
                        }
                    } else {
                        tb.copyCurrentStructure(p);
                    }
                }
                throw new JsonMappingException(p, "Could not find metadata property to extract event type");
            }
        };

    }
}
