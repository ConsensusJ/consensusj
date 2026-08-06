package org.consensusj.jsonrpc.help;

import java.util.Map;

/**
 *
 */
public class JsonRpcHelp {
    private final String summary;
    private final  String detail;

    public JsonRpcHelp(String summary, String detail) {
        this.summary = summary;
        this.detail = detail;
    }

    /**
     * Extracts method summaries from an array of methods annotated with JsonRpcHelp.
     *
     * @param apiClass class with annotated methods
     * @return a Map where keys are method names and values are their JsonRpcHelp records.
     *         Methods without the JsonRpcHelp annotation are excluded from the result.
     * @throws NullPointerException if methods array is null
     */
    public static Map<String, JsonRpcHelp> mapOf(Class<?> apiClass) {
        return java.util.Arrays.stream(apiClass.getMethods())
                .filter(method -> method.isAnnotationPresent(JsonRpcHelpText.class))
                .collect(java.util.stream.Collectors.toMap(
                        java.lang.reflect.Method::getName,
                        method -> JsonRpcHelp.of(method.getAnnotation(JsonRpcHelpText.class)),
                        (v1, v2) -> v1,
                        java.util.TreeMap::new
                ));
    }

    public static String allMethodsHelp(Map<String, JsonRpcHelp> help) {
        StringBuilder all = new StringBuilder();
        for (String method : help.keySet()) {
            all.append(method).append("    ").append(help.get(method).summary);
        }
        return all.toString();
    }

    public static JsonRpcHelp of(JsonRpcHelpText annotation) {
        return new JsonRpcHelp(annotation.summary(), annotation.details());
    }

    public String summary() {
        return summary;
    }

    public String detail() {
        return detail;
    }
}
