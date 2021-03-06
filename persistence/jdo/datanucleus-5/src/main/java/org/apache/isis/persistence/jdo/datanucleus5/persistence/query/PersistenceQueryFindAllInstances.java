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

package org.apache.isis.persistence.jdo.datanucleus5.persistence.query;

import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Corresponds to {@link QueryFindAllInstances}
 */
public class PersistenceQueryFindAllInstances extends PersistenceQueryAbstract  {

//    protected long index;
//    protected long countedSoFar;

    public PersistenceQueryFindAllInstances(
            final ObjectSpecification specification,
            final long... range) {
        super(specification, range);
//        index=0;
//        countedSoFar=0;
    }

//TODO not used, remove?
//    public boolean matches(final ManagedObject object) {
//
//        if (getCount() == 0 && getStart() == 0){
//            return true;
//        }
//        if (index++ < start){
//            return false;
//        }
//        if (countedSoFar++ < count){
//            return true;
//        }
//        return false;
//    }

    @Override
    public String toString() {
        final ToString str = ToString.createAnonymous(this);
        str.append("spec", getSpecification().getShortIdentifier());
        return str.toString();
    }
}
