package org.github.sprofile.servlet;

import org.github.sprofile.Details;
import org.github.sprofile.Profiler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: pgm
 * Date: 12/29/12
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestFilter implements Filter {
    Profiler profiler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.profiler = ProfilerFactory.getInstance();
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        String uri = "unknown";
        String remoteAddr = "unknown";
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            uri = request.getRequestURI();
            remoteAddr = request.getRemoteAddr();
        }
        Details context = new Details("uri", uri, "remoteAddr", remoteAddr);
        try {
            this.profiler.callWithContext(context, new Runnable() {
                @Override
                public void run() {
                    try {
                        filterChain.doFilter(servletRequest, servletResponse);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            } else if (ex.getCause() instanceof ServletException) {
                throw (ServletException) ex.getCause();
            }
        }
    }

    @Override
    public void destroy() {
        ProfilerFactory.release(profiler);
    }
}
