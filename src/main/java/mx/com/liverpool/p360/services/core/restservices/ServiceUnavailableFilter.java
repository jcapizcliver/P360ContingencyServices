package mx.com.liverpool.p360.services.core.restservices;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.ServiceUnavailableException;

/**
 * Servlet Filter implementation class ServiceUnavailableFilter
 */
@WebFilter("/*")
public class ServiceUnavailableFilter extends HttpFilter implements Filter {
	
	private static final java.util.concurrent.atomic.AtomicInteger ACTIVE_REQUESTS = new java.util.concurrent.atomic.AtomicInteger(0);
	private static final java.util.concurrent.atomic.AtomicInteger MAX_ACTIVE_REQUESTS = new java.util.concurrent.atomic.AtomicInteger(0);    

	/**
	 * 
	 */
	private static final long serialVersionUID = 3550448684628517978L;

	/**
     * @see HttpFilter#HttpFilter()
     */
    public ServiceUnavailableFilter() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}
	
	private static final java.util.logging.Logger trafficLog = java.util.logging.Logger.getLogger("TrafficMonitor");

	static {
	    try {
	        java.util.logging.FileHandler fh = new java.util.logging.FileHandler(
	            "/u01/workshop/logs/traffic-monitor.log",
	            50 * 1024 * 1024,
	            10,
	            true
	        );
	        fh.setEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
	        fh.setFormatter(new java.util.logging.SimpleFormatter());
	        trafficLog.setUseParentHandlers(false);
	        trafficLog.addHandler(fh);
	        trafficLog.setLevel(java.util.logging.Level.INFO);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {

	  javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest) request;
	  HttpServletResponse resp = (HttpServletResponse) response;

	  long start = System.currentTimeMillis();
	  int currentActive = ACTIVE_REQUESTS.incrementAndGet();
	  MAX_ACTIVE_REQUESTS.accumulateAndGet(currentActive, Math::max);

	  String uri = req.getRequestURI();
	  String method = req.getMethod();
	  String query = req.getQueryString();
	  String fullPath = query == null ? uri : uri + "?" + query;

	  String remoteAddr = req.getHeader("X-Forwarded-For");
	  if (remoteAddr == null || remoteAddr.trim().isEmpty()) {
	    remoteAddr = req.getRemoteAddr();
	  }

	  String userAgent = req.getHeader("User-Agent");

	  try {
	    chain.doFilter(request, response);

	  } catch (ServiceUnavailableException e) {
		  trafficLog.warning(
	        "SERVICE_UNAVAILABLE"
	        + " method=" + method
	        + " uri=" + fullPath
	        + " active=" + currentActive
	        + " maxActive=" + MAX_ACTIVE_REQUESTS.get()
	        + " durationMs=" + (System.currentTimeMillis() - start)
	        + " thread=" + Thread.currentThread().getName()
	        + " remoteAddr=" + remoteAddr
	        + " userAgent=" + userAgent
	        + " message=" + e.getMessage()
	    );

	    if (resp.isCommitted()) {
	      throw e;
	    }

	    resp.reset();
	    resp.setStatus(503);
	    resp.setHeader("Retry-After", "5");
	    resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
	    resp.setContentType("application/json");
	    resp.getWriter().write("{\"error\":\"SERVICE_UNAVAILABLE\",\"message\":\"Favor de reintentar después de 5s\"}");

	  } finally {
	    long duration = System.currentTimeMillis() - start;
	    int remaining = ACTIVE_REQUESTS.decrementAndGet();

	    if (duration >= 3000 || currentActive >= 20) {
	    	trafficLog.info(
	          "REQUEST"
	          + " method=" + method
	          + " uri=" + fullPath
	          + " status=" + resp.getStatus()
	          + " durationMs=" + duration
	          + " activeAtEntry=" + currentActive
	          + " activeNow=" + remaining
	          + " maxActive=" + MAX_ACTIVE_REQUESTS.get()
	          + " thread=" + Thread.currentThread().getName()
	          + " remoteAddr=" + remoteAddr
	          + " userAgent=" + userAgent
	      );
	    }
	  }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
