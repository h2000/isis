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
package org.apache.isis.persistence.jdo.datanucleus5.metrics;

import java.util.concurrent.atomic.LongAdder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.WithTransactionScope;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.core.runtime.persistence.transaction.ChangedObjectsService;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoDn5.MetricsServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequestScoped
@Log4j2
public class MetricsServiceDefault 
implements MetricsService, InstanceLifecycleListener, LoadLifecycleListener, WithTransactionScope {

// end::refguide[]
    public MetricsServiceDefault() {
        log.debug("init");
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("postConstruct");
    }

    @PreDestroy
    public void preDestroy() {
        log.debug("preDestroy");
    }

// tag::refguide[]
    @Inject private ChangedObjectsService changedObjectsServiceInternal;
    
    private LongAdder numberLoaded = new LongAdder();

    @Override
    public int numberObjectsLoaded() {
        return Math.toIntExact(numberLoaded.longValue());
    }

    @Override
    public int numberObjectsDirtied() {
        return changedObjectsServiceInternal.numberObjectsDirtied();
    }

    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        numberLoaded.increment();
    }

    /**
     * Intended to be called at the end of a transaction.  (This service really ought to be considered
     * a transaction-scoped service; since that isn't yet supported by the framework, we have to manually reset).
     */
    @Override
    public void resetForNextTransaction() {
        numberLoaded.reset();
    }
    

}
