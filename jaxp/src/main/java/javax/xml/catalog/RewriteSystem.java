/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package javax.xml.catalog;

import java.net.URL;

/**
 * Represents a rewriteSystem entry.
 *
 * @since 9
 */
final class RewriteSystem extends BaseEntry {
    String systemIdStartString;
    URL rewritePrefix;

    /**
     * Construct a rewriteSystem entry.
     *
     * @param systemIdStartString The systemIdStartString attribute.
     * @param rewritePrefix       The rewritePrefix attribute.
     */
    public RewriteSystem(String base, String systemIdStartString, String rewritePrefix) {
        super(CatalogEntryType.REWRITESYSTEM, base);
        setSystemIdStartString(systemIdStartString);
        setRewritePrefix(rewritePrefix);
    }

    /**
     * Set the systemIdStartString attribute.
     *
     * @param systemIdStartString The systemIdStartString attribute value.
     */
    public void setSystemIdStartString(String systemIdStartString) {
        CatalogMessages.reportNPEOnNull("systemIdStartString", systemIdStartString);
        this.systemIdStartString = Normalizer.normalizeURI(systemIdStartString);
    }

    /**
     * Set the rewritePrefix attribute. If the value of the rewritePrefix attribute
     * is relative, it must be made absolute with respect to the base URI currently in effect.
     *
     * @param rewritePrefix The rewritePrefix attribute value.
     */
    public void setRewritePrefix(String rewritePrefix) {
        this.rewritePrefix = verifyURI("setRewritePrefix", baseURI, rewritePrefix);
    }

    /**
     * Get the systemIdStartString attribute.
     *
     * @return The systemIdStartString
     */
    public String getSystemIdStartString() {
        return systemIdStartString;
    }

    /**
     * Get the rewritePrefix attribute.
     *
     * @return The rewritePrefix attribute value.
     */
    public URL getRewritePrefix() {
        return rewritePrefix;
    }

    /**
     * Try to match the specified systemId with the entry. Return the match if it
     * is successful and the length of the systemIdStartString is longer than the
     * longest of any previous match.
     *
     * @param systemId     The systemId to be matched.
     * @param currentMatch The length of systemIdStartString of previous match if any.
     * @return The replacement URI if the match is successful, null if not.
     */
    public String match(String systemId, int currentMatch) {
        if (systemIdStartString.length() < systemId.length() &&
                systemIdStartString.equals(systemId.substring(0, systemIdStartString.length()))) {
            if (currentMatch < systemIdStartString.length()) {
                String prefix = rewritePrefix.toExternalForm();
                String sysId;
                if (systemIdStartString.endsWith(SLASH)) {
                    sysId = systemId.substring(systemIdStartString.length());
                } else {
                    sysId = systemId.substring(systemIdStartString.length() + 1);
                }
                if (prefix.endsWith(SLASH)) {
                    return prefix + sysId;
                } else {
                    return prefix + SLASH + sysId;
                }
            }
        }
        return null;
    }

    /**
     * Try to match the specified systemId with the entry.
     *
     * @param systemId The systemId to be matched.
     * @return The replacement URI if the match is successful, null if not.
     */
    @Override
    public String match(String systemId) {
        return match(systemId, 0);
    }
}
