package net.stegard.xml.utils;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import java.text.*;

/**
 * <p>This utility function can be used to mask/hide any content of named XML elements
 *    in an XML character sequence.
 *
 * <p>The code does not actually parse XML and only does lightweight pattern matching
 * on the input. This strategy comes with advantages and disadvantages:
 *
 * <ol>
 * <li>It will accept syntactically incorrect or invalid XML as input without
 *     failing.
 *  <li>It will be able to mask content, even though an element is not properly
 *      closed or the content is truncated or the XML is invalid.
 *  <li>Performance impact should be neglible.
 *  <li>It does understand XML nesting, in the sense that element masking will
 *      be applied to the proper hierarchical scope of an element, even though
 *      the XML is not parsed to a tree structure.
 *   <li>No support for handling attributes on elements to mask, and a presence
 *       of those will cause an element to <em>not be masked</em>.
 *   <li>Things inside CDATA sections will also be processed, no support for
         handling those in any special way.
 * </ol>
 *
 * <p>These properties may or may not be a problem for your use case.
 */
public class XmlElementContentMasker<T extends CharSequence> implements Function<T,String> {

    public static final String MASKED_CONTENT_REPLACEMENT = "***";
    public static final String TRUNCATION_MARKER = "[...TRUNCATED XML]";
        
    private final Pattern matchStartElements;
    private final Map<String,Pattern> startOrClosePatterns = new HashMap<>();

    public XmlElementContentMasker(String...maskElementNames) {
        if (maskElementNames == null || maskElementNames.length == 0) {
            throw new IllegalArgumentException(
                "maskElementNames must be non-null and contain at least one element"
            );
        }

        final StringJoiner startReBuilder = new StringJoiner("|", "<(", ")>");
        for (final String name: maskElementNames) {
            if (name == null) {
                throw new IllegalArgumentException("null names are not allowed");
            }
            final String reQuotedName = Pattern.quote(name);
            startReBuilder.add(reQuotedName);
            this.startOrClosePatterns.put(name, Pattern.compile("<(/?)" + reQuotedName + ">"));
        }

        this.matchStartElements = Pattern.compile(startReBuilder.toString());
    }

    @Override
    public String apply(CharSequence content) {
        if (content == null) return null;

        final StringBuilder filteredContent = new StringBuilder(content.length());
        int contentPosition = 0;
        final Matcher matcher = matchStartElements.matcher(content);
        while (contentPosition < content.length() && matcher.find(contentPosition)) {
            final String matchedElementName = matcher.group(1);
            filteredContent
                .append(content, contentPosition, matcher.end())
                .append(MASKED_CONTENT_REPLACEMENT);

            int closingEndPosition = findClosingElementEnd(matcher.group(1), matcher.start(), content);
            if (closingEndPosition == -1) {
                contentPosition = content.length();
                filteredContent.append(TRUNCATION_MARKER);
            } else {
                filteredContent.append("</").append(matchedElementName).append(">");
                contentPosition = closingEndPosition;
            }
        }
        if (contentPosition < content.length()) {
            filteredContent.append(content, contentPosition, content.length());
        }
            
        return filteredContent.toString();
    }

    private int findClosingElementEnd(String elementName, int from, CharSequence content) {
        final Pattern startOrClose = this.startOrClosePatterns.get(elementName);
        if (startOrClose == null) {
            throw new IllegalStateException(
                "No pre-compiled startOrClose pattern for element '<" + elementName + ">'"
            );
        }
            
        final Matcher m = startOrClose.matcher(content);
        if (!m.find(from) || m.group(1).length() > 0 || m.start() != from) {
            throw new IllegalArgumentException(
                "Expected 'from' position to be at start of element '<" + elementName + ">'"
            );
        }

        // Scan for closing tag at same level of nesting depth
        int depth = 1;
        while (depth > 0 && m.find()) {
            depth += m.group(1).length() == 0 ? 1 : -1;
        }

        return depth == 0 ? m.end() : -1;
    }
}
