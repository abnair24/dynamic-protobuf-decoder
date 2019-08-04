package com.github.abnair24.util;

import com.github.abnair24.cache.ProtoCache;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        try {
            DescriptorProtos.FileDescriptorSet fdSet = DescriptorProtos
                    .FileDescriptorSet.parseFrom(new FileInputStream(path));

            return fdSet.getFileList()
                    .stream()
                    .map(ProtoCache::getFileDescriptorOptional)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
        catch (IOException ex) {
            log.error("Descriptor file not found in path : {}",path);
            throw new RuntimeException(ex);
        }
    }

    public static Descriptors.Descriptor findMethodDescriptor(List<Descriptors.FileDescriptor>fileDescriptorList,ProtoDetail protoDetail) {

        String methodName = protoDetail.getMessageName();
        String packageName = protoDetail.getPackageName();

        return fileDescriptorList
                .stream()
                .filter(fileDescriptor -> isPackageSame(fileDescriptor, packageName))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Method name not found : {}",methodName);
                    return new IllegalArgumentException("Method name not found :" + methodName); })
                .findMessageTypeByName(methodName);
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

        try {
            List<Descriptors.FileDescriptor> fdlist = dependencies
                    .stream()
                    .map(dependency -> getFdList(dependency))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (fdlist.size() == dependencies.size()) {
                fileDescriptor = Descriptors.FileDescriptor
                        .buildFrom(fileDescriptorProto,
                                fdlist.toArray(new Descriptors.FileDescriptor[fdlist.size()]));
            }
        }
        catch (Descriptors.DescriptorValidationException ex) {
            log.error("Field mismatch in descriptor : {}",fileDescriptorProto.getName());
            throw new RuntimeException(ex);
        }
        return fileDescriptor;
    }

    private static List<Descriptors.FileDescriptor> getFdList(String dependency) {
        return ProtoCache.getAllFileDescriptorFromCache()
                .stream()
                .filter(fileDescriptorProto1 -> dependency.equals(fileDescriptorProto1.getName()))
                .map(ProtoCache::getFileDescriptorOptional)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
