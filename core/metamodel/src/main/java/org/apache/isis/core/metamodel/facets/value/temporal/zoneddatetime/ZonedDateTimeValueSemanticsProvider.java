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

package org.apache.isis.core.metamodel.facets.value.temporal.zoneddatetime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalAdjust;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueSemanticsProviderAbstract;

import lombok.val;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ZonedDateTimeValueSemanticsProvider
extends TemporalValueSemanticsProviderAbstract<ZonedDateTime> {

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = 22;
    
    public ZonedDateTimeValueSemanticsProvider(final FacetHolder holder) {
        super(TemporalValueFacet.class,
                TemporalCharacteristic.DATE_TIME, OffsetCharacteristic.OFFSET,
                holder, ZonedDateTime.class, TYPICAL_LENGTH, MAX_LENGTH,
                ZonedDateTime::from,
                TemporalAdjust::adjustZonedDateTime);
        
        val basicDateTimeNoMillis = "yyyyMMdd'T'HHmmssZ";
        val basicDateTime = "yyyyMMdd'T'HHmmss.SSSZ";
        
        super.addNamedFormat("long", "LL");
        super.addNamedFormat("medium", "MM");
        super.addNamedFormat("short", "SS");
        super.addNamedFormat("iso", basicDateTimeNoMillis);
        super.addNamedFormat("iso_encoding", basicDateTime);
        
        super.updateParsers();

        setEncodingFormatter(lookupNamedFormatterElseFail("iso_encoding"));
        setTitleFormatter(formatterFromConfig());
        
    }

    private DateTimeFormatter formatterFromConfig() {

        val configuredNameOrPattern = getConfiguration().getValueTypes().getJavaTime().getZonedDateTime().getFormat();

        val formatter = lookupNamedFormatter(configuredNameOrPattern).orElse(null);
        if(formatter!=null) {
            return formatter;
        }

        try {
            return DateTimeFormatter.ofPattern(configuredNameOrPattern, Locale.getDefault());
        } catch (Exception e) {
            log.warn(e);
        }

        return lookupNamedFormatterElseFail("medium");

    }


}
