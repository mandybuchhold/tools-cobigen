package com.devonfw.cobigen.impl.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.devonfw.cobigen.api.exception.CobiGenRuntimeException;

/**
 * Implementation of {@link TemplateDownloader}.
 */
public class TemplateDownloader {

    /**
     * Retrieve template jar from maven central.
     * @return the path of the templates jar
     */
    public Path retrieveLatestOaspTemplates() {
        return retrieveTemplates("com.devonfw.cobigen", "templates-oasp4j", "2.4.3");
    }

    /**
     * Retrieve template jar from maven central.
     * @return the path of the templates jar
     */
    public Path retrieveTemplates(String groupId, String artifactId, String version) {
        // RetrieveReport report = downloadWithIvy(groupId, artifactId, version);
        //
        // Collection<File> copiedFiles = report.getRetrievedFiles();
        // Optional<File> findFirst = copiedFiles.stream()
        // .filter(e -> (!e.getName().matches(".*-javadoc.jar.*") &&
        // !e.getName().matches(".*-sources.jar.*")))
        // .findFirst();
        //
        // File templatesJar;
        // if (findFirst.isPresent()) {
        // templatesJar = findFirst.get();
        // } else {
        // throw new InvalidConfigurationException("Templates could not be downloaded successfully!");
        // }
        //
        // return templatesJar.toPath();
        return downloadWithIvy(groupId, artifactId, version);
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
    private Path downloadWithIvy(String groupId, String artifactId, String version) {
        // IvySettings ivySettings = new IvySettings();
        // ivySettings.setDefaultCache(Paths.get(System.getProperty("user.home"), ".cobigen",
        // "ivy-cache").toFile());
        //
        // IBiblioResolver br = new IBiblioResolver();
        // br.setM2compatible(true);
        // br.setUsepoms(true);
        // br.setName("central");
        //
        // ivySettings.addResolver(br);
        // ivySettings.setDefaultResolver(br.getName());
        //
        // Ivy ivy = Ivy.newInstance(ivySettings);
        //
        // ResolveOptions ro = new ResolveOptions();
        // ro.setTransitive(true);
        // ro.setDownload(true);
        //
        // DefaultModuleDescriptor md = DefaultModuleDescriptor
        // .newDefaultInstance(ModuleRevisionId.newInstance(groupId, artifactId + "-envelope", version));
        //
        // ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        // DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, false);
        //
        // dd.addDependencyConfiguration("default", "master");
        // md.addDependency(dd);
        //
        // ResolveReport rr;
        // try {
        // rr = ivy.resolve(md, ro);
        // } catch (IOException | ParseException e) {
        // throw new CobiGenRuntimeException(
        // "Unable to resolve " + MavenMetadata.GROUP_ID + ":templates-oasp4j:" + version, e);
        // }
        // if (rr.hasError()) {
        // throw new RuntimeException(rr.getAllProblemMessages().toString());
        // }
        //
        // ModuleDescriptor m = rr.getModuleDescriptor();
        // try {
        // return ivy.retrieve(m.getModuleRevisionId(),
        // new RetrieveOptions().setConfs(new String[] { "default" })
        // .setDestIvyPattern("[organisation]/[module](/[branch])/ivy-[revision].xml").setDestArtifactPattern(
        // "[organisation]/[module](/[branch])/[type]s/[artifact]-[revision](-[classifier])(.[ext])"));
        // } catch (IOException e) {
        // throw new CobiGenRuntimeException(
        // "Unable to resolve " + MavenMetadata.GROUP_ID + ":templates-oasp4j:" + version, e);
        // }

        String mavenUrl =
            "https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.devonfw.cobigen&a=templates-oasp4j&v=LATEST";
        if (false) {
            mavenUrl = mavenUrl + "&c=sources";
        }
        File directory = Paths.get(System.getProperty("user.home"), ".cobigen", "templates-cache").toFile();
        if (!directory.exists()) {
            directory.mkdir();
        }
        URL url;
        try {
            url = new URL(mavenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            try (InputStream inputStream = conn.getInputStream()) {
                String fileName = conn.getURL().getFile().substring(conn.getURL().getFile().lastIndexOf("/") + 1);
                File file = new File(directory.getPath() + File.separator + fileName);
                Path targetPath = file.toPath();
                if (!file.exists()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                return directory.toPath().resolve(fileName);
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            throw new CobiGenRuntimeException("Could not retrieve templates artifact.", e);
        }
    }

}
