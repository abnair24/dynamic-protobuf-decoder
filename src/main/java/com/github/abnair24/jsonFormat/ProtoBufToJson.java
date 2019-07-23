package com.github.abnair24.jsonFormat;

import com.google.gson.JsonObject;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtoBufToJson {

    private final Descriptors.Descriptor descriptor;

    public ProtoBufToJson(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public JsonObject protobufToJsonObject(byte[] inputData) {

        DynamicMessage dynamicMessage = null;
        try {
            dynamicMessage = DynamicMessage.parseFrom(descriptor,inputData);
        } catch (InvalidProtocolBufferException e) {
            log.error("Dynamic message parsing failed: {}",e.getMessage());
        }
        return JsonFormater.toJsonObject(dynamicMessage);
    }
}
