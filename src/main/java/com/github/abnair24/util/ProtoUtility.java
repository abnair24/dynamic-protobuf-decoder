package com.github.abnair24.util;

import com.github.abnair24.cache.ProtoCache;
import com.github.abnair24.exception.CacheLoadingException;
import com.github.abnair24.exception.DescriptorBinaryException;
import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProtoUtility {

    public static Path generateDescriptorBinary(final String protoPath, final List<String> protoFiles) throws DescriptorBinaryException {
        int status;
        Path descFilePath;

        try {
            descFilePath = Files.createTempFile("ProtoDesc", ".desc");

            log.debug("Descriptor Binary path: {} ", descFilePath.toAbsolutePath().toString());

            ImmutableList<String> protocArgs = ImmutableList.<String>builder()
                    .add("--include_imports")
                    .add("--include_std_types")
                    .add("--proto_path=" + protoPath)
                    .add("--descriptor_set_out=" + descFilePath.toAbsolutePath().toString())
                    .addAll(protoFiles)
                    .build();

            status = Protoc.runProtoc(protocArgs.toArray(new String[0]));

            if (status != 0) {
                log.error("Binary file generation failed with status : {} ", status);

                throw new RuntimeException("Protoc binary file generation failed");
            }
        }catch (IOException | InterruptedException ex) {
            log.error("Protoc invokation failed");

            throw new DescriptorBinaryException(ex);
        }
        return descFilePath;
    }

    public static List<Descriptors.FileDescriptor> getFileDescriptorsList(String path){
        List<Descriptors.FileDescriptor> fdList = new ArrayList<>();
        try {
            DescriptorProtos.FileDescriptorSet fdSet = DescriptorProtos
                    .FileDescriptorSet.parseFrom(new FileInputStream(path));

            for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : fdSet.getFileList()) {
                fdList.add(ProtoCache.getFileDescriptor(fileDescriptorProto));
            }
        } catch (CacheLoadingException ex) {
            log.warn("Error loading from cache",ex.getMessage());
        } catch (IOException ex) {
            log.error("Descriptor file not found in path : {}",path);
            throw new RuntimeException(ex);
        }
        return fdList;
    }

    public static Descriptors.Descriptor findMethodDescriptor(List<Descriptors.FileDescriptor>fileDescriptorList,ProtoDetail protoDetail) {

        String methodName = protoDetail.getMessageName();
        String packageName = protoDetail.getPackageName();
        Descriptors.Descriptor descriptor = null;

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

    private static boolean isPackageSame(Descriptors.FileDescriptor fileDescriptor,String packageName) {

        if(StringUtils.isBlank(fileDescriptor.getPackage())) {
            log.error("Filedescriptor loading failed for file :{}",fileDescriptor.getName());
            throw new IllegalArgumentException("Package name empty in "+fileDescriptor.getName());
        }
        return StringUtils.equalsIgnoreCase(packageName, fileDescriptor.getPackage());
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
