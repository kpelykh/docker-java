/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.kpelykh.docker.client.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
@JsonSerialize(using=BoundHostVolumes.Serializer.class)
@JsonDeserialize(using=BoundHostVolumes.Deserializer.class)
public class BoundHostVolumes {
    private static final String[] STRING_ARRAY = new String[0];
    private final String[] dests, binds;

    /**
     * 
     * @param specs Iterable of String binding specs, each of form "{host-path}:{container-patch}:[rw|ro]"
     * @throws MalformedVolumeSpecException if any specs are null or empty
     */
    public BoundHostVolumes(final Iterable<String> specs) {
        final List<String> dests = new ArrayList<String>(), binds = new ArrayList<String>();
        for (final String spec : specs) {
            if (null == spec || "".equals(spec)) {
                // skip empty spec lines
            } else {
                final String[] sspec = spec.split(":");
                dests.add(sspec.length > 1 ? sspec[1] : sspec[0]);
                binds.add(spec);
            }
        }
        this.dests = dests.toArray(STRING_ARRAY);
        this.binds = binds.toArray(STRING_ARRAY);
    }

    public String[] asBinds() {
        return binds;
    }

    private BoundHostVolumes writeVolumes(final JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        for (final String dest : dests) {
            jg.writeObjectFieldStart(dest);
            jg.writeEndObject();
        }
        jg.writeEndObject();
        return this;
    }

    /**
     * This is an ugly hack. We assume that the serializer only gets called when
     * a containing ContainerConfig gets serialized, when POSTing to
     * /containers/create . In that context, we pass only the container-path
     * part (the key in the volumes map).
     * 
     * @author Kevin A. Archie <karchie@wustl.edu>
     * 
     */
    public static class Serializer extends JsonSerializer<BoundHostVolumes> {
        /* (non-Javadoc)
         * @see org.codehaus.jackson.map.JsonSerializer#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
         */
        @Override
        public void serialize(final BoundHostVolumes volumes, final JsonGenerator jg, final SerializerProvider sp)
                throws IOException {
            volumes.writeVolumes(jg);
        }       
    }

    public static class Deserializer extends JsonDeserializer<BoundHostVolumes> {
        @Override
        public BoundHostVolumes deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            BoundHostVolumes volumes = null;
 /*
            boolean started = false;
            String name, map;
            ArrayList<String> specs = new ArrayList<String>();

            while ( !jsonParser.isClosed() ) {
                JsonToken token = jsonParser.nextToken();
                if ( token != null ) {
                    System.out.println( token );
                    if ( !started ) {
                        if ( JsonToken.START_OBJECT.equals(token) ) {
                            started = true;
                        }
                        else if ( JsonToken.VALUE_NULL.equals(token) ) {
                            specs = null;
                            break;
                        }
                    }
                    else {
                        if ( JsonToken.FIELD_NAME.equals(token) ) {
                            name = jsonParser.getCurrentName();
                            specs.add( name );
                            token = jsonParser.nextToken();
                            if ( JsonToken.START_OBJECT.equals(token) ) {
                                jsonParser.skipChildren();
                            }
                        }
                        else if ( JsonToken.END_OBJECT.equals(token) ) {
                            break;
                        }
                    }
                }
                else {
                    break;
                }
            }

            if ( specs != null ) {
                volumes = new BoundHostVolumes( specs );
            }
 */
            while ( !jsonParser.isClosed() ) {
                JsonToken token = jsonParser.getCurrentToken();
                if (token != null) {
                    if (JsonToken.START_OBJECT.equals(token) || JsonToken.START_ARRAY.equals(token)) {
                        jsonParser.skipChildren();
                        break;
                    }
                    else if ( JsonToken.END_OBJECT.equals(token) || JsonToken.END_ARRAY.equals(token) ) {
                        break;
                    }
                    else {
                        System.out.println( "Unexpected volume object token: " + token + ", " +
                                                "name=" + jsonParser.getCurrentName() );
                        jsonParser.nextToken();
                    }
                }
            }
            volumes = new BoundHostVolumes( new ArrayList<String>() );
            return volumes;
        }
    }
}
