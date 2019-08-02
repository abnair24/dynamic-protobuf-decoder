package com.github.abnair24.protobufDecoder;

import com.github.abnair24.dynamicMessage.DynamicMessageProcessor;
import com.github.abnair24.util.ProtoDetail;
import com.github.abnair24.util.ProtoUtility;
import com.google.gson.JsonObject;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Getter
public class ProtobufDecoder {

    private final ProtoDetail protoDetail;
    private DynamicMessageProcessor dynamicMessageProcessor;

    private ProtobufDecoder(String protoPath, String fullMessageName) {
        this.protoDetail = new ProtoDetail(protoPath,fullMessageName);
    }

    public static ProtobufDecoder create(String protoPath, String fullMessageName) {
        return new ProtobufDecoder(protoPath,fullMessageName);
    }

    public Descriptors.Descriptor invoke() {
        List<Descriptors.FileDescriptor> fileDescriptorList = ProtoUtility.getFileDescriptorsList(protoDetail.getDescriptorBinaryPath());
        return ProtoUtility.findMethodDescriptor(fileDescriptorList,protoDetail);
    }


    public JsonObject decode(byte[] input) {
        JsonObject jsonObject = null;
        try {
            Descriptors.Descriptor descriptor = invoke();
            dynamicMessageProcessor = new DynamicMessageProcessor(descriptor);
            jsonObject = dynamicMessageProcessor.parse(input);
        }catch(InvalidProtocolBufferException ex) {
            log.error("Invalid protocol buffer parser : {}",ex.getMessage());
            throw new RuntimeException(ex);
        }
        return jsonObject;
    }

    public <T> T decode(byte[] input, Class <T> response) {
        T outputClass = null ;
        try {
            Descriptors.Descriptor descriptor = invoke();
            dynamicMessageProcessor = new DynamicMessageProcessor(descriptor);
            outputClass =  dynamicMessageProcessor.parse(input,response);
        } catch (InvalidProtocolBufferException ex) {
            log.error("Invalid protocol buffer parser : {}",ex.getMessage());
            throw new RuntimeException(ex);
        }
        return  outputClass;
    }




}
