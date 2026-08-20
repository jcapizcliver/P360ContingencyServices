package mx.com.liverpool.p360.core.restservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.com.liverpool.p360.services.core.PropertiesManager;

/**
 * GetProposals servlet with same-JVM single-flight request coalescing.
 *
 * Identical logical inputs share one execution. A successful result remains in
 * the single-flight map for a short grace window so requests that were already
 * queued by Tomcat while the leader was running can reuse the completed result
 * instead of starting a new execution in the next connector-thread wave.
 *
 * This is intentionally a tiny coalescing window, not a general-purpose cache.
 * Failed executions are removed immediately so the next request can retry.
 */
@WebServlet("/public/rt/GetProposals")
public class GetProposals extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Keep successful responses around just long enough for requests that were
     * already queued behind Tomcat's worker pool to drain through the servlet.
     */
    private static final long SUCCESS_LINGER_MILLIS = 60_000L;

    private static final java.util.concurrent.atomic.AtomicLong SEQ =
            new java.util.concurrent.atomic.AtomicLong();

    private static final java.util.concurrent.ConcurrentHashMap<
            String,
            java.util.concurrent.CompletableFuture<String>> IN_FLIGHT =
                    new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Removes successful completed entries after the grace window. One daemon
     * thread is sufficient because these tasks only remove map entries.
     */
    private static final java.util.concurrent.ScheduledThreadPoolExecutor
            COMPLETED_ENTRY_REAPER = createCompletedEntryReaper();

    private static java.util.concurrent.ScheduledThreadPoolExecutor
            createCompletedEntryReaper() {

        java.util.concurrent.ScheduledThreadPoolExecutor executor =
                new java.util.concurrent.ScheduledThreadPoolExecutor(
                        1,
                        runnable -> {
                            Thread thread = new Thread(
                                    runnable,
                                    "GetProposals-singleflight-reaper");
                            thread.setDaemon(true);
                            return thread;
                        });

        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }

    public GetProposals() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String baseUrl = PropertiesManager.get("p360.contingency.base_url");
        String encoded = PropertiesManager.get("p360.contingency.basic_token_auth");

        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader br = request.getReader()) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        String rawResponse;
        try {
            org.json.JSONObject rootRequest = new org.json.JSONObject(sb.toString());
            String input = rootRequest.getString("input");
            rawResponse = executeSingleFlight(
                    canonicalInput(input),
                    input,
                    baseUrl,
                    encoded,
                    SEQ.incrementAndGet());
        } catch (org.json.JSONException e) {
            rawResponse = new org.json.JSONObject()
                    .put("Error", "Input was not a json object.")
                    .toString();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rawResponse = new org.json.JSONObject()
                    .put("Error", "Request interrupted while waiting for identical in-flight execution.")
                    .toString();
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            rawResponse = new org.json.JSONObject()
                    .put("Error", "GetProposals execution failed.")
                    .toString();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.setHeader("Content-Type", "application/json");
        response.setHeader("Accept", "application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(rawResponse);
    }

    private String executeSingleFlight(
            String key,
            String input,
            String baseUrl,
            String encoded,
            long requestId) throws Exception {

        java.util.concurrent.CompletableFuture<String> mine =
                new java.util.concurrent.CompletableFuture<>();
        java.util.concurrent.CompletableFuture<String> existing =
                IN_FLIGHT.putIfAbsent(key, mine);

        if (existing != null) {
            return await(existing);
        }

        try (mx.com.liverpool.p360.services.core.GetProposals gp =
                new mx.com.liverpool.p360.services.core.GetProposals(
                        baseUrl,
                        encoded,
                        requestId)) {

            String result = gp.run(new String[] { input });

            /*
             * Complete first so current followers wake immediately. Do NOT
             * remove the map entry here: requests that Tomcat has not admitted
             * to this servlet yet must still see the completed future.
             */
            mine.complete(result);
            scheduleSuccessfulRemoval(key, mine);
            return result;
        } catch (Throwable t) {
            mine.completeExceptionally(t);

            /* Failed work is never retained. A later request may retry. */
            IN_FLIGHT.remove(key, mine);

            if (t instanceof Exception) {
                throw (Exception) t;
            }
            throw new RuntimeException(t);
        }
    }

    private String await(java.util.concurrent.CompletableFuture<String> future)
            throws Exception {

        try {
            return future.get();
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    private void scheduleSuccessfulRemoval(
            String key,
            java.util.concurrent.CompletableFuture<String> completed) {

        try {
            COMPLETED_ENTRY_REAPER.schedule(
                    () -> IN_FLIGHT.remove(key, completed),
                    SUCCESS_LINGER_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            /* Servlet is shutting down; do not retain the entry indefinitely. */
            IN_FLIGHT.remove(key, completed);
        }
    }

    /**
     * Normalizes object-key order and whitespace, but intentionally preserves
     * JSONArray order because response order follows request order.
     */
    private String canonicalInput(String input) {
        try {
            return canonicalJson(new org.json.JSONObject(input));
        } catch (org.json.JSONException e) {
            return input == null ? "" : input.trim();
        }
    }

    private String canonicalJson(Object value) {
        if (value == null || value == org.json.JSONObject.NULL) {
            return "null";
        }
        if (value instanceof org.json.JSONObject) {
            org.json.JSONObject object = (org.json.JSONObject) value;
            java.util.List<String> keys = new java.util.ArrayList<>(object.keySet());
            java.util.Collections.sort(keys);
            StringBuilder out = new StringBuilder("{");
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                String key = keys.get(i);
                out.append(org.json.JSONObject.quote(key));
                out.append(':');
                out.append(canonicalJson(object.get(key)));
            }
            return out.append('}').toString();
        }
        if (value instanceof org.json.JSONArray) {
            org.json.JSONArray array = (org.json.JSONArray) value;
            StringBuilder out = new StringBuilder("[");
            for (int i = 0; i < array.length(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(canonicalJson(array.get(i)));
            }
            return out.append(']').toString();
        }
        if (value instanceof String) {
            return org.json.JSONObject.quote((String) value);
        }
        return String.valueOf(value);
    }

    @Override
    public void destroy() {
        COMPLETED_ENTRY_REAPER.shutdownNow();
        IN_FLIGHT.clear();
        super.destroy();
    }
}
