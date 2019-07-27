package com.github.abnair24.protobufDecoder;

import com.github.abnair24.cache.ProtoCache;
import com.github.abnair24.exception.CacheLoadingException;
import com.github.abnair24.jsonFormat.ProtoBufToJson;
import com.github.abnair24.util.ProtoDetail;
import com.google.gson.JsonObject;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProtobufDecoder {

    private ProtoDetail protoDetail;
    private List<Descriptors.FileDescriptor> fdList;
    private Descriptors.Descriptor descriptor;
    private ProtoBufToJson protoBufToJson;

    public ProtobufDecoder(String protoPath, String fullMethodName) {
        this.protoDetail = new ProtoDetail(protoPath,fullMethodName);
    }

    public ProtoDetail getProtoDetail() {
        return protoDetail;
    }

    public JsonObject decode(byte[] input) {
        invokeProtoc();
        getFileDescriptorsList();
        descriptor = findMethodDescriptor(fdList);
        protoBufToJson = new ProtoBufToJson(descriptor);
        return protoBufToJson.protobufToJsonObject(input);
    }

//    public String decode(String input) {
//        invokeProtoc();
//        getFileDescriptorsList();
//        descriptor = findMethodDescriptor(fdList);
//        protoBufToJson = new ProtoBufToJson(descriptor);
//        protoBufToJson.protobufToJsonObject();
//    }

    private void invokeProtoc() {
        Path binaryPath = ProtoCache.getBinary(protoDetail);
        protoDetail.setDescriptorBinaryPath(binaryPath.toAbsolutePath().toString());
    }

    private void getFileDescriptorsList(){
        fdList = new ArrayList<>();
        try {
            DescriptorProtos.FileDescriptorSet fdSet = DescriptorProtos
                    .FileDescriptorSet.parseFrom(new FileInputStream(protoDetail.getDescriptorBinaryPath()));

            for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : fdSet.getFileList()) {
                fdList.add(ProtoCache.getFileDescriptor(fileDescriptorProto));
            }
        } catch (CacheLoadingException ex) {
            log.warn("Error loading from cache",ex.getMessage());
        } catch (IOException ex) {
            log.error("Descriptor file not found in path : {}",protoDetail.getDescriptorBinaryPath());
            throw new RuntimeException(ex);
        }
    }

    private Descriptors.Descriptor findMethodDescriptor(List<Descriptors.FileDescriptor>fileDescriptorList) {
        String methodName = protoDetail.getMethodName();
        String packageName = protoDetail.getPackageName();

        for (Descriptors.FileDescriptor fileDescriptor : fileDescriptorList) {
            if (!isPackageSame(fileDescriptor, packageName)) {
                continue;
            }
            descriptor = fileDescriptor.findMessageTypeByName(methodName);
            break;
        }

        if(descriptor == null) {
            log.error("Method name not found : {}",methodName);
            throw new IllegalArgumentException("Method name not found :"+ methodName);
        }
        return descriptor;
    }

    private boolean isPackageSame(Descriptors.FileDescriptor fileDescriptor,String packageName) {
        boolean status;
        if(fileDescriptor.getPackage() == "") {
            log.error("Filedescriptor loading failed for file :{}",fileDescriptor.getName());
            throw new IllegalArgumentException("Package name empty in "+fileDescriptor.getName());
        }
        if(packageName != null && packageName.equals(fileDescriptor.getPackage())) {
            status = true;
        } else {
            status  = false;
        }
        return status;
    }


    public static Descriptors.FileDescriptor findDependentFileDescriptors(DescriptorProtos.FileDescriptorProto fileDescriptorProto) {

        Descriptors.FileDescriptor fileDescriptor =null;
        List<String>dependencies = fileDescriptorProto.getDependencyList();

        List<Descriptors.FileDescriptor> fdlist = new ArrayList<>();
        try {

            for (String dep : dependencies) {
                Descriptors.FileDescriptor fd = null;
                for (DescriptorProtos.FileDescriptorProto fdp : ProtoCache.getAllFileDescriptorFromCache()) {
                    if (dep.equals(fdp.getName())) {
                        fd = ProtoCache.getFileDescriptor(fdp);
                    }
                }
                if (fd != null) {
                    fdlist.add(fd);
                }
            }
            if (fdlist.size() == dependencies.size()) {
                Descriptors.FileDescriptor[] fds = new Descriptors.FileDescriptor[fdlist.size()];
                fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, fdlist.toArray(fds));
            }
        } catch (Descriptors.DescriptorValidationException ex) {
            log.error("Field mismatch in descriptor : {}",fileDescriptorProto.getName());
            throw new RuntimeException(ex);

        } catch (CacheLoadingException ex) {
            log.warn("Error loading from cache",ex.getMessage());
        }
        return fileDescriptor;
    }
}
