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
package com.ecyrd.jspwiki.xmlrpc;

import java.util.*;
import org.apache.xmlrpc.XmlRpcException;
import com.ecyrd.jspwiki.LinkCollector;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;
import com.ecyrd.jspwiki.auth.permissions.PermissionFactory;

/**
 * Provides handlers for all RPC routines. These routines are used by the UTF-8
 * interface.
 * 
 * @since 1.6.13
 */

@SuppressWarnings("unchecked")
public class RPCHandlerUTF8
        extends AbstractRPCHandler {
	public String getApplicationName() {
		checkPermission(PagePermission.VIEW);

		return m_engine.getApplicationName();
	}

	public Vector getAllPages() {
		checkPermission(PagePermission.VIEW);

		Collection pages = m_engine.getRecentChanges();
		Vector<String> result = new Vector<String>();

		for(Iterator i = pages.iterator(); i.hasNext();) {
			WikiPage p = (WikiPage)i.next();

			if(!(p instanceof Attachment)) {
				result.add(p.getName());
			}
		}

		return result;
	}

	/**
	 * Encodes a single wiki page info into a Hashtable.
	 */
	@Override
	@SuppressWarnings("boxing")
	protected Hashtable<String, Object> encodeWikiPage(WikiPage page) {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();

		ht.put("name", page.getName());

		Date d = page.getLastModified();

		//
		// Here we reset the DST and TIMEZONE offsets of the
		// calendar. Unfortunately, I haven't thought of a better
		// way to ensure that we're getting the proper date
		// from the XML-RPC thingy, except to manually adjust the date.
		//

		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MILLISECOND,
		        -(cal.get(Calendar.ZONE_OFFSET) +
		        (cal.getTimeZone().inDaylightTime(d) ? cal.get(Calendar.DST_OFFSET) : 0)));

		ht.put("lastModified", cal.getTime());
		ht.put("version", page.getVersion());

		if(page.getAuthor() != null) {
			ht.put("author", page.getAuthor());
		}

		return ht;
	}

	@Override
	public Vector getRecentChanges(Date since) {
		checkPermission(PagePermission.VIEW);

		Collection pages = m_engine.getRecentChanges();
		Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

		Calendar cal = Calendar.getInstance();
		cal.setTime(since);

		//
		// Convert UTC to our time.
		//
		cal.add(Calendar.MILLISECOND,
		        (cal.get(Calendar.ZONE_OFFSET) +
		        (cal.getTimeZone().inDaylightTime(since) ? cal.get(Calendar.DST_OFFSET) : 0)));
		final Date newSince = cal.getTime();

		for(Iterator i = pages.iterator(); i.hasNext();) {
			WikiPage page = (WikiPage)i.next();

			if(page.getLastModified().after(newSince) && !(page instanceof Attachment)) {
				result.add(encodeWikiPage(page));
			}
		}

		return result;
	}

	/**
	 * Simple helper method, turns the incoming page name into normal Java
	 * string, then checks page condition.
	 * 
	 * @param pagename
	 *            Page Name as an RPC string (URL-encoded UTF-8)
	 * @return Real page name, as Java string.
	 * @throws XmlRpcException
	 *             , if there is something wrong with the page.
	 */
	private String parsePageCheckCondition(String pagename) throws XmlRpcException {
		if(!m_engine.pageExists(pagename)) {
			throw new XmlRpcException(ERR_NOPAGE, "No such page '" + pagename + "' found, o master.");
		}

		WikiPage p = m_engine.getPage(pagename);

		checkPermission(PermissionFactory.getPagePermission(p, PagePermission.VIEW_ACTION));
		return pagename;
	}

	public Hashtable getPageInfo(String pagename) throws XmlRpcException {
		final String pn = parsePageCheckCondition(pagename);
		return encodeWikiPage(m_engine.getPage(pn));
	}

	public Hashtable getPageInfoVersion(String pn, int version) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);
		return encodeWikiPage(m_engine.getPage(pagename, version));
	}

	public String getPage(String pn) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);
		final String text = m_engine.getPureText(pagename, -1);
		return text;
	}

	public String getPageVersion(String pn, int version) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);
		return m_engine.getPureText(pagename, version);
	}

	public String getPageHTML(String pn) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);
		return m_engine.getHTML(pagename);
	}

	public String getPageHTMLVersion(String pn, int version) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);
		return m_engine.getHTML(pagename, version);
	}

	public Vector listLinks(String pn) throws XmlRpcException {
		final String pagename = parsePageCheckCondition(pn);

		WikiPage page = m_engine.getPage(pagename);
		String pagedata = m_engine.getPureText(page);

		LinkCollector localCollector = new LinkCollector();
		LinkCollector extCollector = new LinkCollector();
		LinkCollector attCollector = new LinkCollector();

		WikiContext context = new WikiContext(m_engine, page);
		context.setVariable(WikiEngine.PROP_REFSTYLE, "absolute");

		m_engine.textToHTML(context,
		        pagedata,
		        localCollector,
		        extCollector,
		        attCollector);

		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

		// FIXME: Contains far too much common with RPCHandler. Refactor!

		//
		// Add local links.
		//
		for(Iterator i = localCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.put("page", link);
			ht.put("type", LINK_LOCAL);

			if(m_engine.pageExists(link)) {
				ht.put("href", context.getViewURL(link));
			} else {
				ht.put("href", context.getURL(WikiContext.EDIT, link));
			}

			result.add(ht);
		}

		//
		// Add links to inline attachments
		//
		for(Iterator i = attCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();

			Hashtable<String, String> ht = new Hashtable<String, String>();

			ht.put("page", link);
			ht.put("type", LINK_LOCAL);
			ht.put("href", context.getURL(WikiContext.ATTACH, link));

			result.add(ht);
		}

		//
		// External links don't need to be changed into XML-RPC strings,
		// simply because URLs are by definition ASCII.
		//

		for(Iterator i = extCollector.getLinks().iterator(); i.hasNext();) {
			String link = (String)i.next();

			Hashtable<String, String> ht = new Hashtable<String, String>();

			ht.put("page", link);
			ht.put("type", LINK_EXTERNAL);
			ht.put("href", link);

			result.add(ht);
		}

		return result;
	}
}