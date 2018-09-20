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

package org.apache.isis.core.metamodel.facets.object.mask.annotation;

import org.apache.isis.applib.annotation.Mask;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.mask.MaskFacet;
import org.apache.isis.core.metamodel.progmodel.DeprecatedMarker;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

/**
 * @deprecated
 */
@Deprecated
public class MaskFacetOnTypeAnnotationFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner, DeprecatedMarker {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(Mask.class);


    public MaskFacetOnTypeAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    /**
     * In readiness for supporting <tt>@Value</tt> in the future.
     */
    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final Mask annotation = Annotations.getAnnotation(processClassContaxt.getCls(), Mask.class);
        final MaskFacet facet = createMaskFacet(annotation, processClassContaxt.getFacetHolder());
        FacetUtil.addFacet(validator.flagIfPresent(facet));
    }

    private MaskFacet createMaskFacet(final Mask annotation, final FacetHolder holder) {
        return annotation != null ? new MaskFacetOnTypeAnnotation(annotation.value(), null, holder) : null;
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        validator.setConfiguration((IsisConfiguration)servicesInjector.lookupService(ConfigurationServiceInternal.class));
    }


}