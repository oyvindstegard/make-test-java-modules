package net.stegard.xml.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import java.text.*;

public class XmlElementContentMaskerTest {

    @Test
    public void masking_multiple_element_names() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush", "secret");

        String result = masker.apply("<payload><foo>1</foo><hush><t>HUSH</t></hush><secret>SECRET</secret></payload>");
        assertEquals("<payload><foo>1</foo><hush>***</hush><secret>***</secret></payload>", result);
    }
    
    @Test
    public void masking_simple_case() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");

        String result = masker.apply("<hush>something</hush>");
        assertEquals("<hush>***</hush>", result);
    }
    
    @Test
    public void masking_elements_that_contain_themselves() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");

        String result = masker.apply("<hush><hush>SECRET2</hush>SECRET1</hush>");
        assertEquals("<hush>***</hush>", result);
    }

    @Test
    public void masking_deeper_nesting() {
        XmlElementContentMasker masker = new XmlElementContentMasker("C");

        String result = masker.apply("<A></A><B><C><C></C><C><C><C>secret</C></C></C></C></B><C></C>");
        assertEquals("<A></A><B><C>***</C></B><C>***</C>", result);
    }
    
    
    @Test
    public void maskingAllWithNestedMaskElements() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");

        String result = masker.apply("<hush><foo>1</foo><hush><t>x</t></hush><secret>SECRET</secret></hush>");
        assertEquals("<hush>***</hush>", result);
    }
    
    @Test
    public void maskingAndTruncation() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");

        String result = masker.apply("<payload><foo>1</foo><hush>AAAA");
        assertEquals("<payload><foo>1</foo><hush>***[...TRUNCATED XML]", result);
    }
    
    @Test
    public void noMatchingElements() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "<payload><foo>1</foo></payload>";

        String result = masker.apply(content);
        assertEquals(content, result);
    }
    
    @Test
    public void noContentToMask() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "<payload><hush></hush></payload>";

        String result = masker.apply(content);
        assertEquals("<payload><hush>***</hush></payload>", result);
    }

    @Test
    public void emptyElementNameToMask() {
        XmlElementContentMasker masker = new XmlElementContentMasker("");
        final String content = "<payload><foo>1</foo></payload>";

        String result = masker.apply(content);
        assertEquals(content, result);
    }
    
    @Test
    public void xml_immediately_closed_empty_element() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "<payload><hush /><foo>1</foo></payload>";

        String result = masker.apply(content);
        assertEquals(content, result);
    }

    @Test
    public void onlyOneUnclosedElementToMask() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        String result = masker.apply("<hush>");
        assertEquals("<hush>***[...TRUNCATED XML]", result);
    }
    
    @Test
    public void invalidXml_nesting() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "<payload><foo><hush></foo>1</hush></payload>";

        String result = masker.apply(content);
        assertEquals("<payload><foo><hush>***</hush></payload>", result);
    }
    
    @Test
    public void invalidXml_onlyClosingTags() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "</hush></hush>";

        String result = masker.apply(content);
        assertEquals(content, result);
    }
    
    @Test
    public void invalidXml_extraClosingTag() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "<hush>secret</hush></hush>";

        String result = masker.apply(content);
        assertEquals("<hush>***</hush></hush>", result);
    }
    
    @Test
    public void invalidXml_closeStartClose() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        final String content = "</hush><hush>secret</hush>";

        String result = masker.apply(content);
        assertEquals("</hush><hush>***</hush>", result);
    }
    
    @Test
    public void invalid_xml_empty_element_name() {
        XmlElementContentMasker masker = new XmlElementContentMasker("");
        assertEquals("<some><>***</></some>", masker.apply("<some><>weird</></some>"));
    }
    
    @Test
    public void invalid_xml_empty_element_name_nomask() {
        XmlElementContentMasker masker = new XmlElementContentMasker("x");
        assertEquals("<some><>weird</></some>", masker.apply("<some><>weird</></some>"));
    }
    
    @Test
    public void regexp_syntax_in_element_name() {
        XmlElementContentMasker masker = new XmlElementContentMasker("a.c");
        assertEquals("<X><abc>y</abc><a.c>***</a.c></X>", masker.apply("<X><abc>y</abc><a.c>secret</a.c></X>"));
    }
    
    @Test
    public void null_content() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");
        assertNull(masker.apply(null));
    }
    
    @Test
    public void empty_content() {
        Function<String,String> masker = new XmlElementContentMasker<>("hush");
        assertEquals("", masker.apply(""));
    }
    
    @Test
    @Disabled("Not supported")
    public void masking_and_element_attributes() {
        XmlElementContentMasker masker = new XmlElementContentMasker("hush");

        String result = masker.apply("<payload><hush x=\"y\">secret</hush></payload>");
        assertEquals("<payload><hush>***</hush></payload>", result);
    }
    
}
