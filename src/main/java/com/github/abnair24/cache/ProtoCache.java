package com.github.abnair24.cache;

import com.github.abnair24.exception.DescriptorBinaryException;
import com.github.abnair24.util.ProtoUtility;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;

@Slf4j
public class ProtoCache {

    private static final LoadingCache<FileDescriptorProto, Descriptors.FileDescriptor> FD_CACHE =
            CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build(new CacheLoader<FileDescriptorProto, Descriptors.FileDescriptor>() {
                @Override
                public Descriptors.FileDescriptor load(FileDescriptorProto key) {
                    return ProtoUtility.findDependentFileDescriptors(key);
                }
            });

    private static final Cache<String, Path> DESCRIPTOR_BINARY_CACHE = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30,TimeUnit.MINUTES)
            .recordStats()
            .build();

    public static Optional<Descriptors.FileDescriptor> getFileDescriptorOptional(FileDescriptorProto fileDescriptorProto) {

        Descriptors.FileDescriptor fileDescriptor=null;

        try {
            fileDescriptor = FD_CACHE.get(fileDescriptorProto);
        }catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            log.info("Stats:{}", FD_CACHE.stats());
            return Optional.ofNullable(fileDescriptor);
        }
    }

    public static Set<FileDescriptorProto> getAllFileDescriptorFromCache() {
        return FD_CACHE.asMap().keySet();
    }

    public static Path getBinary(final String protoPath, final List<String> protoFiles) {

        log.info("DescriptorCache Stats: {}", DESCRIPTOR_BINARY_CACHE.stats());

        return Optional.ofNullable(DESCRIPTOR_BINARY_CACHE.getIfPresent("descriptor.desc"))
                .orElseGet(() ->
                {
                    try {
                        final Path value = ProtoUtility.generateDescriptorBinary(protoPath, protoFiles);
                        DESCRIPTOR_BINARY_CACHE.put("descriptor.desc", value);
                        return value;
                    } catch (DescriptorBinaryException ex) {
                        log.error(ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                });
    }
}
