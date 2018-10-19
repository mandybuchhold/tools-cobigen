package com.devonfw.cobigen.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.cobigen.api.CobiGen;
import com.devonfw.cobigen.api.HealthCheck;
import com.devonfw.cobigen.api.exception.InvalidConfigurationException;
import com.devonfw.cobigen.impl.aop.BeanFactory;
import com.devonfw.cobigen.impl.aop.ProxyFactory;
import com.devonfw.cobigen.impl.config.ConfigurationHolder;
import com.devonfw.cobigen.impl.config.ContextConfiguration;
import com.devonfw.cobigen.impl.downloader.TemplateDownloader;
import com.devonfw.cobigen.impl.extension.ServiceLookup;
import com.devonfw.cobigen.impl.healthcheck.HealthCheckImpl;
import com.devonfw.cobigen.impl.util.FileSystemUtil;

/**
 * CobiGen's Factory to create new instances of {@link CobiGen}.
 */
public class CobiGenFactory {

    /** Logger instance. */
    private static final Logger LOG = LoggerFactory.getLogger(CobiGenFactory.class);

    static {
        ServiceLookup.detectServices();
    }

    /**
     * Creates a new {@link CobiGen} without providing a configuration folder. This constructor will handle
     * the automatic download of the latest oasp4j templates from maven central and creates a new instance
     * making use of the last downloaded templates. If there are already templates available, it will use the
     * latest download.
     *
     * @return a new instance of {@link CobiGen}
     * @throws InvalidConfigurationException
     *             if the context configuration could not be read properly.
     */
    public static CobiGen create() {
        LOG.info("Creating a new instance of CobiGen loading latest oasp4j templates...");

        TemplateDownloader templateDownloader = new TemplateDownloader();

        ConfigurationHolder configurationHolder =
            new ConfigurationHolder(templateDownloader.retrieveLatestOaspTemplates());
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.addManuallyInitializedBean(configurationHolder);
        return beanFactory.createBean(CobiGen.class);
    }

    /**
     * Creates a new {@link CobiGen} without providing a configuration folder. This constructor will handle
     * the automatic download of the templates encoded by the ivy reference.
     *
     * @return a new instance of {@link CobiGen}
     * @throws InvalidConfigurationException
     *             if the context configuration could not be read properly.
     */
    public static CobiGen create(String groupId, String artifactId, String version) {
        LOG.info("Creating a new instance of CobiGen with an ivy template reference...");

        TemplateDownloader templateDownloader = new TemplateDownloader();

        ConfigurationHolder configurationHolder =
            new ConfigurationHolder(templateDownloader.retrieveTemplates(groupId, artifactId, version));
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.addManuallyInitializedBean(configurationHolder);
        return beanFactory.createBean(CobiGen.class);
    }

    /**
     * Creates a new {@link CobiGen} with a given {@link ContextConfiguration}.
     *
     * @param configFileOrFolder
     *            the root folder containing the context.xml and all templates, configurations etc.
     * @return a new instance of {@link CobiGen}
     * @throws IOException
     *             if the {@link URI} points to a file or folder, which could not be read.
     * @throws InvalidConfigurationException
     *             if the context configuration could not be read properly.
     */
    public static CobiGen create(URI configFileOrFolder) throws InvalidConfigurationException, IOException {
        LOG.info("Creating a new instance of CobiGen...");
        Objects.requireNonNull(configFileOrFolder, "The URI pointing to the configuration could not be null.");

        Path configFolder = FileSystemUtil.createFileSystemDependentPath(configFileOrFolder);

        ConfigurationHolder configurationHolder = new ConfigurationHolder(configFolder);
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.addManuallyInitializedBean(configurationHolder);
        return beanFactory.createBean(CobiGen.class);
    }

    /**
     * Creates a new {@link HealthCheck}.
     * @return a new {@link HealthCheck} instance
     */
    public static HealthCheck createHealthCheck() {
        return ProxyFactory.getProxy(new HealthCheckImpl());
    }

}
