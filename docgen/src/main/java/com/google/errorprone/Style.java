package com.google.errorprone;

import com.google.errorprone.BugPattern.SeverityLevel;

public final class Style {

    public static String styleSeverity (SeverityLevel severityLevel) {
        return String.format("%s\n {: .label .label-%s}", severityLevel.toString(), getSeverityLabelColour(severityLevel));
    }

    private static String getSeverityLabelColour (SeverityLevel severityLevel) {
        switch (severityLevel) {
            case ERROR:
                return "red";
            case WARNING:
                return "yellow";
            case SUGGESTION:
                return "green";
            default:
                return "blue";
        }
    }

    public static String styleTag (String tagName) {
        return String.format("%s\n {: .label }", tagName);
    }
}
