/*
 * Copyright (c) 2011, 2017, Oracle and/or its affiliates. All rights reserved.
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.org.apache.xalan.internal.xsltc;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @LastModified: Oct 2017
 */
public interface Translet {

    public void transform(DOM document, SerializationHandler handler)
            throws TransletException;

    public void transform(DOM document, SerializationHandler[] handlers)
            throws TransletException;

    public void transform(DOM document, DTMAxisIterator iterator,
                          SerializationHandler handler)
            throws TransletException;

    public Object addParameter(String name, Object value);

    public void buildKeys(DOM document, DTMAxisIterator iterator,
                          SerializationHandler handler, int root)
            throws TransletException;

    public void addAuxiliaryClass(Class<?> auxClass);

    public Class<?> getAuxiliaryClass(String className);

    public String[] getNamesArray();

    public String[] getUrisArray();

    public int[] getTypesArray();

    public String[] getNamespaceArray();

    public boolean overrideDefaultParser();

    public void setOverrideDefaultParser(boolean flag);

}
