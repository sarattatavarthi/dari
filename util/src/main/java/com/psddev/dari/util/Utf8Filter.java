package com.psddev.dari.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Forces {@link StringUtils#UTF_8} character encoding on all requests
 * and responses.
 */
public class Utf8Filter extends AbstractFilter {

    public static final String CHECK_PARAMETER = "_u";
    public static final String CHECK_VALUE = "\u2713";

    // --- AbstractFilter support ---

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String encoding = StringUtils.UTF_8.name();

        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        String check = request.getParameter(CHECK_PARAMETER);

        if (check != null && !CHECK_VALUE.equals(check)) {
            request = new HttpServletRequestWrapper(request) {

                private final Map<String, String[]> reEncoded;

                {
                    @SuppressWarnings("unchecked")
                    Map<String, String[]> oldMap = (Map<String, String[]>) getRequest().getParameterMap();
                    Map<String, String[]> newMap  = new CompactMap<String, String[]>();

                    for (Map.Entry<String, String[]> entry : oldMap.entrySet()) {
                        String[] values = entry.getValue();
                        String[] copy = new String[values.length];

                        for (int i = 0, length = values.length; i < length; ++ i) {
                            copy[i] = reEncode(values[i]);
                        }

                        newMap.put(reEncode(entry.getKey()), copy);
                    }

                    reEncoded = Collections.unmodifiableMap(newMap);
                }

                private String reEncode(String string) {
                    return new String(string.getBytes(StringUtils.ISO_8859_1), StringUtils.UTF_8);
                }

                @Override
                public String getParameter(String name) {
                    String[] values = reEncoded.get(name);

                    return values != null && values.length > 0 ? values[0] : null;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public Map getParameterMap() {
                    return reEncoded;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public Enumeration getParameterNames() {
                    return Collections.enumeration(reEncoded.keySet());
                }

                @Override
                public String[] getParameterValues(String name) {
                    return reEncoded.get(name);
                }
            };
        }

        chain.doFilter(request, response);
    }

    // --- Deprecated ---

    /** @deprecated Use {@link StringUtils#UTF_8} instead. */
    @Deprecated
    public static final String ENCODING = StringUtils.UTF_8.name();
}
