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
package org.apache.isis.commons.ioc;

import org.apache.isis.commons.internal.base._Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum LifecycleContext {
    ApplicationScoped,
    Singleton,
    SessionScoped,
    RequestScoped,
    ConversationScoped,
    Dependent,
    NotSpecified,
    ;

    public boolean isApplicationScoped() {
        return this == ApplicationScoped;
    }
    
    public boolean isSingleton() {
        return this == Singleton;
    }
    
    public boolean isRequestScoped() {
        return this == RequestScoped;
    }

    public static LifecycleContext parse(String scope) {
        
        if(_Strings.isNullOrEmpty(scope)) {
            return LifecycleContext.NotSpecified;
        }
        
        for(LifecycleContext candidate : LifecycleContext.values()) {
            if(candidate.name().equalsIgnoreCase(scope)) {
                return candidate;
            }
        }
        
        log.warn("unrecogniced scope '{}'", scope);
        return LifecycleContext.NotSpecified;

    }

}