package com.devonfw.cobigen.impl.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collection;
import java.util.Optional;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.IBiblioResolver;

import com.devonfw.cobigen.api.exception.CobiGenRuntimeException;
import com.devonfw.cobigen.api.exception.InvalidConfigurationException;
import com.devonfw.cobigen.impl.config.constant.MavenMetadata;

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
        RetrieveReport report = downloadWithIvy(groupId, artifactId, version);

        Collection<File> copiedFiles = report.getRetrievedFiles();
        Optional<File> findFirst = copiedFiles.stream()
            .filter(e -> (!e.getName().matches(".*-javadoc.jar.*") && !e.getName().matches(".*-sources.jar.*")))
            .findFirst();

        File templatesJar;
        if (findFirst.isPresent()) {
            templatesJar = findFirst.get();
        } else {
            throw new InvalidConfigurationException("Templates could not be downloaded successfully!");
        }

        return templatesJar.toPath();
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
    private RetrieveReport downloadWithIvy(String groupId, String artifactId, String version) {
        IvySettings ivySettings = new IvySettings();
        ivySettings.setDefaultCache(Paths.get(System.getenv("user.home"), ".cobigen", "ivy-cache").toFile());

        IBiblioResolver br = new IBiblioResolver();
        br.setM2compatible(true);
        br.setUsepoms(true);
        br.setName("central");

        ivySettings.addResolver(br);
        ivySettings.setDefaultResolver(br.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);

        ResolveOptions ro = new ResolveOptions();
        ro.setTransitive(true);
        ro.setDownload(true);

        DefaultModuleDescriptor md = DefaultModuleDescriptor
            .newDefaultInstance(ModuleRevisionId.newInstance(groupId, artifactId + "-envelope", version));

        ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, false);

        dd.addDependencyConfiguration("default", "master");
        md.addDependency(dd);

        ResolveReport rr;
        try {
            rr = ivy.resolve(md, ro);
        } catch (IOException | ParseException e) {
            throw new CobiGenRuntimeException(
                "Unable to resolve " + MavenMetadata.GROUP_ID + ":templates-oasp4j:" + version, e);
        }
        if (rr.hasError()) {
            throw new RuntimeException(rr.getAllProblemMessages().toString());
        }

        ModuleDescriptor m = rr.getModuleDescriptor();
        try {
            return ivy.retrieve(m.getModuleRevisionId(),
                new RetrieveOptions().setConfs(new String[] { "default" })
                    .setDestIvyPattern("[organisation]/[module](/[branch])/ivy-[revision].xml").setDestArtifactPattern(
                        "[organisation]/[module](/[branch])/[type]s/[artifact]-[revision](-[classifier])(.[ext])"));
        } catch (IOException e) {
            throw new CobiGenRuntimeException(
                "Unable to resolve " + MavenMetadata.GROUP_ID + ":templates-oasp4j:" + version, e);
        }
    }

}
