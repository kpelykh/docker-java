package com.kpelykh.docker.client.utils;

import org.testng.annotations.Test;

import java.net.URISyntaxException;

import static org.testng.Assert.*;

public class ResourceUtilTest {
    @Test
    public void isFileResourceForFilename() throws URISyntaxException {
        assertTrue(ResourceUtil.isFileResource("myfile.jar"));
    }

    @Test
    public void isFileResourceForFilenameInDirectory() throws URISyntaxException {
        assertTrue(ResourceUtil.isFileResource("/home/user/myfile.jar"));
    }

    @Test
    public void isFileResourceForFileURL() throws URISyntaxException {
        assertTrue(ResourceUtil.isFileResource("file:///home/user/myfile.jar"));
    }

    @Test
    public void isFileResourceForDirectoryURL() throws URISyntaxException {
        assertTrue(ResourceUtil.isFileResource("file:///home/user"));
    }

    @Test
    public void isNotFileResourceForHttpURL() throws URISyntaxException {
        assertFalse(ResourceUtil.isFileResource("http://my.server.com/file.zip"));
    }

    @Test
    public void isNotFileResourceForHttpsURL() throws URISyntaxException {
        assertFalse(ResourceUtil.isFileResource("https://my.server.com/file.zip"));
    }
}
