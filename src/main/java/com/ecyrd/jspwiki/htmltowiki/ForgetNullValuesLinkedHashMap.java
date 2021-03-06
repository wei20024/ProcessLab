/*
  Copyright (c) Inexas 2010

  Modifications licensed under the Inexas Software License V1.0. You
  may not use this file except in compliance with the License.

  The License is available at: http://www.inexas.com/ISL-V1.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  The original file and contents are licensed under a separate license:
  see below.
*/
/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package com.ecyrd.jspwiki.htmltowiki;

import java.util.LinkedHashMap;

/**
 * A LinkedHashMap that does not put null values into the map.
 * 
 * @param <K>
 *            {@inheritDoc}
 * @param <V>
 *            {@inheritDoc}
 * 
 * @author Sebastian Baltes (sbaltes@gmx.com)
 */
public class ForgetNullValuesLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 0L;

	/**
	 * {@inheritDoc}
	 */
	public V put(K key, V value) {
		if(value != null) {
			return super.put(key, value);
		}

		return null;
	}
}
