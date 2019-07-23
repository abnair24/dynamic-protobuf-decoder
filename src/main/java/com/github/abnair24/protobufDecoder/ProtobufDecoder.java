package com.github.abnair24.protobufDecoder;

import com.github.abnair24.cache.ProtoCache;
import com.github.abnair24.util.ProtoDetail;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProtobufDecoder {

    private final ProtoDetail protoDetail;

    public ProtobufDecoder(ProtoDetail protoDetail) {
        this.protoDetail = protoDetail;
    }

    public Descriptors.Descriptor invokeDescriptorBinary() throws Exception {
        Path binaryPath = ProtoCache.getBinary(protoDetail);
        protoDetail.setDescriptorBinaryPath(binaryPath.toAbsolutePath().toString());
        return getDescriptor();
    }

    private Descriptors.Descriptor getDescriptor() throws Exception {

        DescriptorProtos.FileDescriptorSet fdSet = DescriptorProtos
                .FileDescriptorSet.parseFrom(new FileInputStream(protoDetail.getDescriptorBinaryPath()));

        List<Descriptors.FileDescriptor> fdList = new ArrayList<>();

        for(DescriptorProtos.FileDescriptorProto fileDescriptorProto: fdSet.getFileList()) {
            fdList.add(ProtoCache.getFileDescriptor(fileDescriptorProto));
        }
        return resolveMethodDescriptor(fdList);
    }


    public static Descriptors.FileDescriptor getAllFileDescriptors(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Exception {

        Descriptors.FileDescriptor fileDescriptor =null;
        List<String>dependencies = fileDescriptorProto.getDependencyList();

        List<Descriptors.FileDescriptor> fdlist = new ArrayList<>();
        for(String dep : dependencies) {
            Descriptors.FileDescriptor fd = null;
            for(DescriptorProtos.FileDescriptorProto fdp : ProtoCache.getAllFileDescriptorFromCache()) {
                if (dep.equals(fdp.getName())) {
                    fd = ProtoCache.getFileDescriptor(fdp);
                }
            }
            if(fd!=null) {
                fdlist.add(fd);
            }
        }
        if(fdlist.size()== dependencies.size()) {
            Descriptors.FileDescriptor[] fds = new Descriptors.FileDescriptor[fdlist.size()];
            fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto,fdlist.toArray(fds));
        }
        return fileDescriptor;
    }


    private Descriptors.Descriptor resolveMethodDescriptor(List<Descriptors.FileDescriptor>fileDescriptorList) {
        String methodName = protoDetail.getMethodName();
        String packageName = protoDetail.getPackageName();

        Descriptors.Descriptor descriptor = null;

        for (Descriptors.FileDescriptor fileDescriptor : fileDescriptorList) {
            if (!isPackageSame(fileDescriptor, packageName)) {
                continue;
            }
            descriptor = getMethodDescriptor(fileDescriptor, methodName);
            break;
        }

        return descriptor;
    }

    private static Descriptors.Descriptor getMethodDescriptor(Descriptors.FileDescriptor fileDescriptor, String methodName) {
        Descriptors.Descriptor descriptor = fileDescriptor.findMessageTypeByName(methodName);
        if(descriptor == null) {
            throw new IllegalArgumentException("Method name not found :"+ methodName);
        }
        return descriptor;
    }

    private static boolean isPackageSame(Descriptors.FileDescriptor fileDescriptor,String packageName) {
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
}
