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
package org.apache.isis.extensions.security.manager.jdo.seed.scripts;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScript;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.security.manager.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRoleRepository;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class AbstractRoleAndPermissionsFixtureScript extends FixtureScript {

    private final String roleName;
    private final String roleDescription;

    protected AbstractRoleAndPermissionsFixtureScript(
            final String roleName,
            final String roleDescriptionIfAny) {
        this.roleName = roleName;
        this.roleDescription = roleDescriptionIfAny;
    }

    /**
     * Subclasses should override and call any of
     * {@link #newPackagePermissions(org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule, org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode, String...)},
     * {@link #newClassPermissions(org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule, org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode, Class[])} or
     * {@link #newMemberPermissions(org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule, org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode, Class, String...)}.
     */
    @Override
    protected abstract void execute(ExecutionContext executionContext);

    // -- newPackagePermissions, newClassPermissions, newMemberPermissions

    /**
     * For subclasses to call in {@link #execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
     */
    protected void newPackagePermissions(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String... featureFqns) {

        newPermissions(rule, mode, ApplicationFeatureType.PACKAGE, Arrays.asList(featureFqns));
    }

    /**
     * For subclasses to call in {@link #execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
     */
    protected void newClassPermissions(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final Class<?>... classes) {

        newPermissions(rule, mode, ApplicationFeatureType.CLASS, asFeatureFqns(classes));
    }


    /**
     * For subclasses to call in {@link #execute(org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext)}.
     */
    protected void newMemberPermissions(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final Class<?> cls,
            final String... members) {
        newPermissions(rule, mode, ApplicationFeatureType.MEMBER, asFeatureFqns(cls, members));
    }

    


    // -- helpers

    private void newPermissions(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final Iterable<String> featureFqns) {

        if(featureFqns == null) {
            return;
        }

        ApplicationRole securityRole = applicationRoleRepository.findByName(roleName);
        if(securityRole == null) {
            securityRole = applicationRoleRepository.newRole(roleName, roleDescription);
        }
        for (String featureFqn : featureFqns) {
            // can't use role#addPackage because that does a check for existence of the package, which is
            // not guaranteed to exist yet (the SecurityFeatures#init() may not have run).
            applicationPermissionRepository.newPermissionNoCheck(
                    securityRole,
                    rule,
                    mode,
                    featureType, featureFqn);
        }
    }

    private static Iterable<String> asFeatureFqns(Class<?>[] classes) {
        return Iterables.transform(Arrays.asList(classes), new Function<Class<?>, String>(){
            @Override
            public String apply(Class<?> input) {
                return input.getName();
            }
        });
    }

    private static Iterable<String> asFeatureFqns(final Class<?> cls, final String[] members) {
        return Iterables.transform(Arrays.asList(members), new Function<String,String>(){
            @Override
            public String apply(String memberName) {
                final StringBuilder buf = new StringBuilder(cls.getName());
                if(!memberName.startsWith("#")) {
                    buf.append("#");
                }
                buf.append(memberName);
                return buf.toString();
            }
        });
    }

    //region  >  (injected)
    @Inject
    ApplicationRoleRepository applicationRoleRepository;
    @Inject
    ApplicationPermissionRepository applicationPermissionRepository;
    
}
