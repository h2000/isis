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
package org.apache.isis.applib.services.queryresultscache;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.isis.core.commons.handler.MethodReferences.Call0;
import org.apache.isis.core.commons.handler.MethodReferences.Call1;
import org.apache.isis.core.commons.handler.MethodReferences.Call2;
import org.apache.isis.core.commons.handler.MethodReferences.Call3;
import org.apache.isis.core.commons.handler.MethodReferences.Call4;
import org.apache.isis.core.commons.handler.MethodReferences.Call5;

import lombok.Data;
import lombok.Getter;


/**
 * This service (API and implementation) provides a mechanism by which idempotent query results can be cached for the duration of an interaction.
 * Most commonly this allows otherwise &quot;naive&quot; - eg that makes a repository call many times within a loop - to
 * be performance tuned.  The benefit is that the algorithm of the business logic can remain easy to understand.
 *
 * <p>
 * This implementation has no UI and there is only one implementation (this class) in applib, it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 */
// tag::refguide[]
public interface QueryResultsCache {

    <T> T execute(Callable<T> callable, Class<?> callingClass, String methodName, Object... keys);

    <R> R execute(Call0<? extends R> action, Class<?> callingClass, String methodName);
    
    <R, A0> R execute(Call1<? extends R, A0> action, Class<?> callingClass, String methodName,
            A0 arg0);
    
    <R, A0, A1> R execute(Call2<? extends R, A0, A1> action, Class<?> callingClass, String methodName, 
            A0 arg0, A1 arg1);
    
    <R, A0, A1, A2> R execute(Call3<? extends R, A0, A1, A2> action, Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2);
    
    <R, A0, A1, A2, A3> R execute(Call4<? extends R, A0, A1, A2, A3> action, Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2, A3 arg3);
    
    <R, A0, A1, A2, A3, A4> R execute(Call5<? extends R, A0, A1, A2, A3, A4> action, Class<?> callingClass, String methodName,
            A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    
    void resetForNextTransaction();

// end::refguide[]
// tag::refguide-1[]
    class Key {

        @Getter
        private final Class<?> callingClass;
        @Getter
        private final String methodName;
        @Getter
        private final Object[] keys;

        public Key(Class<?> callingClass, String methodName, Object... keys) {
            this.callingClass = callingClass;
            this.methodName = methodName;
            this.keys = keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;

            // compare callingClass
            if (callingClass == null) {
                if (other.callingClass != null)
                    return false;
            } else if (!callingClass.equals(other.callingClass))
                return false;

            // compare methodName
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;

            // compare keys
            if (!Arrays.equals(keys, other.keys))
                return false;

            // ok, matches
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((callingClass == null) ? 0 : callingClass.hashCode());
            result = prime * result + Arrays.hashCode(keys);
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return callingClass.getName() + "#" + methodName  + Arrays.toString(keys);
        }
    }
// end::refguide-1[]

// tag::refguide-2[]
    @Data
    class Value<T> {
        private final T result;
    }
// end::refguide-2[]

// tag::refguide[]
}
// end::refguide[]
