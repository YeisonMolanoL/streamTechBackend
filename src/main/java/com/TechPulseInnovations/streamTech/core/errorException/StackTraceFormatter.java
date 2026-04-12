package com.TechPulseInnovations.streamTech.core.errorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para formatear stack traces de excepciones
 * Proporciona formatos legibles y comprensibles
 */
public class StackTraceFormatter {

    /**
     * Convierte un stack trace de excepción en una lista de strings formateados
     * 
     * Ejemplo de salida:
     * [
     *   "com.TechPulseInnovations.streamTech.service.CodeService.getCode (CodeService.java:45)",
     *   "com.TechPulseInnovations.streamTech.controller.CodeController.getCodeByEmail (CodeController.java:120)",
     *   "sun.reflect.NativeMethodAccessorImpl.invoke0 (NativeMethodAccessorImpl.java line: -2)"
     * ]
     *
     * @param exception La excepción a formatear
     * @param maxLines Número máximo de líneas del stack trace a incluir
     * @return Lista de strings con formato legible
     */
    public static List<String> formatStackTrace(Throwable exception, int maxLines) {
        List<String> formattedStackTrace = new ArrayList<>();

        if (exception == null || exception.getStackTrace() == null) {
            return formattedStackTrace;
        }

        StackTraceElement[] stackTrace = exception.getStackTrace();
        int linesToInclude = Math.min(maxLines, stackTrace.length);

        for (int i = 0; i < linesToInclude; i++) {
            StackTraceElement element = stackTrace[i];
            formattedStackTrace.add(formatStackTraceElement(element));
        }

        // Si hay más líneas, agregar indicador
        if (stackTrace.length > maxLines) {
            formattedStackTrace.add("... y " + (stackTrace.length - maxLines) + " líneas más");
        }

        return formattedStackTrace;
    }

    /**
     * Convierte un StackTraceElement individual en string legible
     * 
     * Formato: "com.package.Class.method (FileName.java:123)"
     * 
     * @param element El elemento del stack trace
     * @return String formateado y legible
     */
    private static String formatStackTraceElement(StackTraceElement element) {
        String className = element.getClassName();
        String methodName = element.getMethodName();
        String fileName = element.getFileName() != null ? element.getFileName() : "Unknown";
        int lineNumber = element.getLineNumber();

        // Simplificar nombre de clase si es muy largo
        String shortClassName = shortenClassName(className);

        // Formato legible
        if (lineNumber >= 0) {
            return String.format("%s.%s (%s:%d)",
                    shortClassName, methodName, fileName, lineNumber);
        } else if (element.isNativeMethod()) {
            return String.format("%s.%s (Método nativo)",
                    shortClassName, methodName);
        } else {
            return String.format("%s.%s (%s)",
                    shortClassName, methodName, fileName);
        }
    }

    /**
     * Acorta nombres de clases muy largos manteniendo claridad
     * 
     * Ejemplo:
     * "com.TechPulseInnovations.streamTech.service.CodeReceptionService" 
     * → "streamTech.service.CodeReceptionService"
     * 
     * @param className Nombre completo de la clase
     * @return Nombre acortado
     */
    private static String shortenClassName(String className) {
        String[] parts = className.split("\\.");
        if (parts.length > 3) {
            // Tomar último package significativo + nombre de clase
            String basePackage = parts[parts.length - 2];
            String clazz = parts[parts.length - 1];
            return basePackage + "." + clazz;
        }
        return className;
    }

    /**
     * Obtiene el mensaje de error más detallado de la excepción
     * Busca en la cadena de causas para encontrar el mensaje más específico
     * 
     * @param exception La excepción
     * @return Mensaje detallado o "Unknown error" si no hay
     */
    public static String getDetailedMessage(Throwable exception) {
        if (exception == null) {
            return "Unknown error";
        }

        // Buscar el mensaje más específico en la cadena de causas
        Throwable current = exception;
        String lastMessage = exception.getClass().getSimpleName();

        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isEmpty()) {
                lastMessage = current.getMessage();
                break; // Primera causa con mensaje, ésa es la más específica
            }
            current = current.getCause();
        }

        return lastMessage;
    }

    /**
     * Obtiene el tipo de error simplificado (nombre de la clase sin package)
     * 
     * @param exception La excepción
     * @return Nombre de la clase de excepción
     */
    public static String getErrorType(Throwable exception) {
        if (exception == null) {
            return "UnknownError";
        }

        String className = exception.getClass().getSimpleName();
        // Convertir CamelCase a UPPER_SNAKE_CASE para códigos de error
        return camelToSnake(className).toUpperCase();
    }

    /**
     * Convierte CamelCase a snake_case
     * 
     * @param input String en CamelCase
     * @return String en snake_case
     */
    private static String camelToSnake(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2");
    }
}
