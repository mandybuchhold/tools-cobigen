package com.devonfw.cobigen.unittest.downloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;

import com.devonfw.cobigen.impl.downloader.TemplateDownloader;

/**
 * Testing {@link TemplateDownloader}
 */
public class TemplateDownloaderTest {

    @Test
    public void testDownloadLatestOasp4JTemplates() {
        Path latestOaspTemplates = new TemplateDownloader().retrieveLatestOaspTemplates();
        assertThat(latestOaspTemplates).isNotNull();
    }
}
