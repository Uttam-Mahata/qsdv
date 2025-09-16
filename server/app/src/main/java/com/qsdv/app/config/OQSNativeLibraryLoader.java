package com.qsdv.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Native library loader for Open Quantum Safe (OQS) JNI library
 * 
 * This class handles loading the native liboqs-jni library required
 * for the Java wrapper to interface with the C implementation of OQS.
 */
@Configuration
public class OQSNativeLibraryLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(OQSNativeLibraryLoader.class);
    
    @Value("${qsdv.oqs.native.library.path:}")
    private String nativeLibraryPath;
    
    @Value("${qsdv.oqs.load.from.classpath:true}")
    private boolean loadFromClasspath;
    
    private static boolean libraryLoaded = false;
    
    @PostConstruct
    public void loadNativeLibrary() {
        if (libraryLoaded) {
            logger.debug("OQS native library already loaded");
            return;
        }
        
        try {
            // First, try to load from specified path if provided
            if (!nativeLibraryPath.isEmpty()) {
                loadFromPath(nativeLibraryPath);
                return;
            }
            
            // Try to load from classpath if enabled
            if (loadFromClasspath) {
                loadFromClasspath();
                return;
            }
            
            // Try to load from system library path
            loadFromSystem();
            
        } catch (Exception e) {
            logger.error("Failed to load OQS native library. Please ensure liboqs is installed.", e);
            logger.info("To use OQS algorithms, you need to:");
            logger.info("1. Build liboqs from https://github.com/open-quantum-safe/liboqs");
            logger.info("2. Build liboqs-java from https://github.com/open-quantum-safe/liboqs-java");
            logger.info("3. Set qsdv.oqs.native.library.path to the location of liboqs-jni library");
            logger.info("   OR place the library in your system library path");
        }
    }
    
    private void loadFromPath(String path) throws Exception {
        File libFile = new File(path);
        if (!libFile.exists()) {
            throw new RuntimeException("Native library not found at: " + path);
        }
        
        System.load(libFile.getAbsolutePath());
        libraryLoaded = true;
        logger.info("Successfully loaded OQS native library from: {}", path);
    }
    
    private void loadFromClasspath() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String libName = getLibraryName(osName);
        
        // Try to load from classpath resources
        String resourcePath = "/native/" + libName;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        
        if (is == null) {
            // Try alternate path
            resourcePath = "/" + libName;
            is = getClass().getResourceAsStream(resourcePath);
        }
        
        if (is != null) {
            // Extract to temp directory and load
            Path tempDir = Files.createTempDirectory("oqs-native");
            Path tempLib = tempDir.resolve(libName);
            Files.copy(is, tempLib, StandardCopyOption.REPLACE_EXISTING);
            is.close();
            
            System.load(tempLib.toAbsolutePath().toString());
            libraryLoaded = true;
            logger.info("Successfully loaded OQS native library from classpath");
            
            // Clean up on exit
            tempLib.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
        } else {
            throw new RuntimeException("Native library not found in classpath: " + resourcePath);
        }
    }
    
    private void loadFromSystem() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        String libName = getLibraryNameWithoutExtension();
        
        try {
            System.loadLibrary(libName);
            libraryLoaded = true;
            logger.info("Successfully loaded OQS native library from system path");
        } catch (UnsatisfiedLinkError e) {
            // Try with full library name
            System.loadLibrary("oqs-jni");
            libraryLoaded = true;
            logger.info("Successfully loaded OQS native library from system path");
        }
    }
    
    private String getLibraryName(String osName) {
        if (osName.contains("win")) {
            return "liboqs-jni.dll";
        } else if (osName.contains("mac")) {
            return "liboqs-jni.dylib";
        } else {
            return "liboqs-jni.so";
        }
    }
    
    private String getLibraryNameWithoutExtension() {
        return "oqs-jni";
    }
    
    public static boolean isLibraryLoaded() {
        return libraryLoaded;
    }
}