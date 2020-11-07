package net.stegard.make.java;

import static org.mockito.Mockito.*;
import static net.stegard.make.java.Logger.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

interface Blackbox {
    int version();
    int compute(int input);
}

public class MockitoTest {

    @Test
    public void testBlackbox() {
        Blackbox bb = mock(Blackbox.class);
        when(bb.version()).thenReturn(1);
        when(bb.compute(10)).thenReturn(20);
        
        assertEquals(1, bb.version());
        assertEquals(20, bb.compute(10));

        log("Blackbox instance: %s", bb);
    }

    
}
