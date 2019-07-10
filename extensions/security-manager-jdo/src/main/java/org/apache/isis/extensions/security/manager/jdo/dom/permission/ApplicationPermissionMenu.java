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
package org.apache.isis.extensions.security.manager.jdo.dom.permission;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.security.manager.api.SecurityModule;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isissecurity.ApplicationPermissionMenu"
)
@DomainServiceLayout(
        named="Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class ApplicationPermissionMenu {

        // -- domain event classes
        public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationPermissions, T> {
			private static final long serialVersionUID = 1L;}

        public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationPermissions, T> {
			private static final long serialVersionUID = 1L;}

        public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationPermissions> {
			private static final long serialVersionUID = 1L;}
        

        // -- iconName
        public String iconName() {
            return "applicationPermission";
        }
        

        // -- findOrphanedPermissions (action)
        public static class FindOrphanedPermissionsDomainEvent extends ActionDomainEvent {
			private static final long serialVersionUID = 1L;}

        @Action(
                domainEvent=FindOrphanedPermissionsDomainEvent.class,
                semantics = SemanticsOf.SAFE
        )
        @MemberOrder(sequence = "100.50.1")
        public List<ApplicationPermission> findOrphanedPermissions() {
                return applicationPermissionRepository.findOrphaned();
        }
        

        // -- allPermissions (action)
        public static class AllPermissionsDomainEvent extends ActionDomainEvent {
			private static final long serialVersionUID = 1L;}

        @Action(
                domainEvent=AllPermissionsDomainEvent.class,
                semantics = SemanticsOf.SAFE,
                restrictTo = RestrictTo.PROTOTYPING
        )
        @MemberOrder(sequence = "100.50.2")
        public List<ApplicationPermission> allPermissions() {
                return applicationPermissionRepository.allPermissions();
        }
        
        // -- DEPENDENCIES
        @Inject private ApplicationPermissionRepository applicationPermissionRepository;
        

}
