/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.contrib.authentication.internal;

import org.apache.commons.lang3.StringUtils;
import org.securityfilter.filter.URLPattern;
import org.securityfilter.filter.URLPatternFactory;
import org.securityfilter.filter.URLPatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.web.XWikiRequest;

/**
 * Helper class to match request against a given pattern using the same pattern syntax that is used by the the form
 * authenticator. This overly complex matching is caused by the usage of the SecurityFilter which itself has a very
 * special matcher (based on Perl5Matcher) to match URLs, that is not following the usual regular expression syntax.
 *
 * @version $Id$
 */
public class RequestMatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMatcher.class);

    /**
     * Cache of the logout pattern.
     */
    private URLPattern pattern;

    /**
     * Construct a new matcher for the provided pattern.
     *
     * @param pattern the pattern that will be matched by this matcher.
     */
    public RequestMatcher(String pattern)
    {
        URLPatternFactory patternFactory = new URLPatternFactory();
        if (!StringUtils.isBlank(pattern)) {
            try {
                this.pattern = patternFactory.createURLPattern(pattern, null, null, 0);
            } catch (Exception e) {
                LOGGER.warn("Unable to compile pattern [{}] for request matching, it will never match.", pattern, e);
            }
        }
    }

    /**
     * @param request the request to match.
     * @return true if the current request match the current logout path.
     */
    public boolean match(XWikiRequest request)
    {
        if (pattern == null) {
            return false;
        }

        String requestPath = getRequestPath(request);
        boolean matched = false;
        try {
            matched = new URLPatternMatcher().match(requestPath, pattern);
        } catch (Exception e) {
            LOGGER.warn("Unexpected exception during request matching", e);
        }
        LOGGER.debug("Matching [{}] against [{}] has resulted to a {} match", requestPath, pattern.getPattern(),
            matched ? "successful" : "failed");
        return matched;
    }

    /**
     * @param request the request to extract the path from.
     * @return the current request path (servletPath + pathInfo) for easy matching.
     */
    private String getRequestPath(XWikiRequest request)
    {
        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            path += pathInfo;
        }
        return path;
    }
}
