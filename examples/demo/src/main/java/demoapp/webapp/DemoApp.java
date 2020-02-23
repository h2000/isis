/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package demoapp.webapp;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.server.Constants;
import com.vaadin.flow.spring.RootMappedCondition;
import com.vaadin.flow.spring.SpringBootAutoConfiguration;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import com.vaadin.flow.spring.VaadinServletContextInitializer;
import com.vaadin.flow.spring.VaadinWebsocketEndpointExporter;
import com.vaadin.flow.spring.annotation.EnableVaadin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.cors.impl.IsisModuleExtCorsImpl;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisModuleExtSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisModuleExtSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisModuleExtSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisModuleExtSecmanRealmShiro;
import org.apache.isis.extensions.viewer.wicket.exceldownload.ui.IsisModuleExtExcelDownloadUi;
import org.apache.isis.incubator.model.metamodel.IsisModuleIncModelMetaModel;
import org.apache.isis.persistence.jdo.datanucleus5.IsisModuleJdoDataNucleus5;
import org.apache.isis.security.shiro.IsisModuleSecurityShiro;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.valuetypes.asciidoc.ui.IsisModuleValAsciidocUi;
import org.apache.isis.valuetypes.sse.ui.IsisModuleValSseUi;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleViewerRestfulObjectsViewer;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import demoapp.dom.DemoModule;
import demoapp.utils.LibraryPreloadingService;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * Bootstrap the application.
 */
@Configuration
// disable standard vaadin spring boot bootstrapping
@EnableAutoConfiguration(exclude = { SpringBootAutoConfiguration.class })
@ComponentScan
@Import({
        DemoApp.AppManifest.class,
        VaadinConfigurationProperties.class
})
@EnableVaadin("demoapp.webapp")
@Log4j2
public class DemoApp extends SpringBootServletInitializer {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private VaadinConfigurationProperties configurationProperties;

    /**
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an entry point by
     * searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {
        //IsisPresets.prototyping();
        SpringApplication.run(new Class[] { DemoApp.class }, args);
    }

    static String makeContextRelative(String url) {
        // / -> context://
        // foo -> context://foo
        // /foo -> context://foo
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return "context://" + url;
    }

    /**
     * Creates a {@link ServletContextInitializer} instance.
     *
     * @return a custom ServletContextInitializer instance
     */
    @Bean
    public ServletContextInitializer contextInitializer() {
        return new VaadinServletContextInitializer(context);
    }

    /**
     * Creates a {@link ServletRegistrationBean} instance with Spring aware Vaadin servlet.
     *
     * @return a custom ServletRegistrationBean instance
     */
    @Bean
    public ServletRegistrationBean<SpringServlet> servletRegistrationBean() {
        String mapping = configurationProperties.getUrlMapping();
        final Map<String, String> initParameters = new HashMap<>();
        final boolean rootMapping = RootMappedCondition.isRootMapping(mapping);
        if (rootMapping) {
            mapping = "/vaadinServlet/*";
            initParameters.put(Constants.SERVLET_PARAMETER_PUSH_URL,
                    makeContextRelative(mapping.replace("*", "")));
        }
        final ServletRegistrationBean<SpringServlet> registration = new ServletRegistrationBean<>(
                new SpringServlet(context, rootMapping), mapping);
        registration.setInitParameters(initParameters);
        registration
                .setAsyncSupported(configurationProperties.isAsyncSupported());
        registration.setName(
                ClassUtils.getShortNameAsProperty(SpringServlet.class));
        return registration;
    }

    /**
     * Deploys JSR-356 websocket endpoints when Atmosphere is available.
     *
     * @return the server endpoint exporter which does the actual work.
     */
    @Bean
    public ServerEndpointExporter websocketEndpointDeployer() {
        return new VaadinWebsocketEndpointExporter();
    }

    /**
     * Makes the integral parts of the 'demo' web application.
     */
    @Configuration
    @PropertySources({
            @PropertySource(IsisPresets.HsqlDbInMemory),
            @PropertySource(IsisPresets.NoTranslations),
            @PropertySource(IsisPresets.SilenceWicket),
            @PropertySource(IsisPresets.DataNucleusAutoCreate),
    })
    @Import({
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityShiro.class,
            IsisModuleJdoDataNucleus5.class,
            IsisModuleViewerWicketViewer.class,
            IsisModuleValSseUi.class, // server sent events
            IsisModuleValAsciidocUi.class, // ascii-doc rendering support

            // REST
            IsisModuleViewerRestfulObjectsViewer.class,
            IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,

            // CORS
            IsisModuleExtCorsImpl.class,

            // Security Manager Extension (secman)
            IsisModuleExtSecmanModel.class,
            IsisModuleExtSecmanRealmShiro.class,
            IsisModuleExtSecmanPersistenceJdo.class,
            IsisModuleExtSecmanEncryptionJbcrypt.class,

            IsisModuleTestingFixturesApplib.class,

            IsisModuleIncModelMetaModel.class, // @Model support (incubator)
            IsisModuleExtExcelDownloadUi.class, // allows for collection download as excel

            LibraryPreloadingService.class // just a performance enhancement

    })
    @ComponentScan(
            basePackageClasses = {
                    DemoModule.class
            }
    )
    public static class AppManifest {

        @Bean
        public SecurityModuleConfig securityModuleConfigBean() {
            return SecurityModuleConfig.builder()
                    .adminUserName("sven")
                    .adminAdditionalPackagePermission("demoapp.dom")
                    .adminAdditionalPackagePermission("org.apache.isis")
                    .build();
        }

        @Bean
        public PermissionsEvaluationService permissionsEvaluationService() {
            return new PermissionsEvaluationServiceAllowBeatsVeto();
        }

        /**
         * If available from {@code System.getProperty("ContextPath")} or {@code System.getenv("ContextPath")}, sets the
         * context path for the web server. The context should start with a "/" character but not end with a "/"
         * character. The default context path can be specified using an empty string.
         */
        @Bean
        public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
            return factory -> {
                val contextPath = Optional
                        .ofNullable(System.getProperty("ContextPath"))
                        .orElse(System.getenv("ContextPath")); // fallback
                if (contextPath != null) {
                    factory.setContextPath(contextPath);
                    log.info("Setting context path to '{}'", contextPath);
                }
            };
        }

    }

}
