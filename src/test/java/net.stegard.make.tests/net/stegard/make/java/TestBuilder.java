package net.stegard.make.java;

import static net.stegard.make.java.Logger.*;
import static net.stegard.make.java.Main.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestBuilder {

    @Test
    public void builderValuesHaveDescription() {
        for (Builder val: Builder.values()) {
            assertNotNull(val.description);
            assertFalse(val.description.trim().isEmpty());
            log("%s: %s", val.name(), val.description);
        }
    }

}

