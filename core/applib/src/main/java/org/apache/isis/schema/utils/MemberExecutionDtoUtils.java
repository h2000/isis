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
package org.apache.isis.schema.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.isis.schema.ixn.v1.MemberExecutionDto;

public final class MemberExecutionDtoUtils {

    public static <T extends MemberExecutionDto> T clone(final T dto) {
        final Class<T> aClass = (Class)dto.getClass();
        return clone(dto, aClass);
    }

    private static <T> T clone(final T dto, final Class<T> dtoClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(dtoClass);

            final Marshaller marshaller = jaxbContext.createMarshaller();

            final QName name = new QName("", dtoClass.getSimpleName());
            final JAXBElement<T> jaxbElement = new JAXBElement<>(name, dtoClass, null, dto);
            final StringWriter stringWriter = new StringWriter();

            marshaller.marshal(jaxbElement, stringWriter);

            final StringReader reader = new StringReader(stringWriter.toString());

            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final JAXBElement<T> root = unmarshaller.unmarshal(new StreamSource(reader), dtoClass);

            return root.getValue();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
