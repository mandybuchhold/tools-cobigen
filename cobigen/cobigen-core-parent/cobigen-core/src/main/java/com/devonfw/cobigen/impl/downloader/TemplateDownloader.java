package com.devonfw.cobigen.impl.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.devonfw.cobigen.api.exception.CobiGenRuntimeException;
import com.devonfw.cobigen.impl.config.constant.MavenMetadata;

/**
 * Implementation of {@link TemplateDownloader}.
 */
public class TemplateDownloader {

    private static final String SOURCE_SUFFIX = "&c=sources";

    private static final String MAVEN_DOWNLOAD_TEMPLATE =
        "https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=%s&a=%s&v=%s";

    /**
     * Retrieve template jar from maven central.
     * @return the path of the templates jar
     */
    public Path retrieveLatestOaspTemplates() {
        return retrieveTemplates(MavenMetadata.GROUP_ID, "templates-oasp4j", "LATEST");
    }

    /**
     * Retrieve template jar from maven central.
     * @return the path of the templates jar
     */
    public Path retrieveTemplates(String groupId, String artifactId, String version) {
        return downloadMavenArtifact(groupId, artifactId, version);
    }

    /**
     * Retrieve or even download template artifacts from maven central
     * @param groupId
     *            of the artifact to retrieve
     * @param artifactId
     *            of the artifact to retrieve
     * @param version
     *            of the artifact to retrieve
     * @return the {@link RetrieveReport} of the Ivy retrieval
     */
    private Path downloadMavenArtifact(String groupId, String artifactId, String version) {

        String mavenUrl = String.format(
            "https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=%s&a=%s&v=%s",
            groupId, artifactId, version);

        Path directory = Paths.get(System.getProperty("user.home"), ".cobigen", "templates-cache");
        if (!Files.exists(directory)) {
            directory.toFile().mkdirs();
        }
        URL url;
        try {
            url = new URL(mavenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            try (InputStream inputStream = conn.getInputStream()) {
                String fileName = conn.getURL().getFile().substring(conn.getURL().getFile().lastIndexOf("/") + 1);
                Path file = directory.resolve(fileName);
                Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
                return directory.resolve(fileName);
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            throw new CobiGenRuntimeException("Could not retrieve templates artifact.", e);
        }
    }

}
