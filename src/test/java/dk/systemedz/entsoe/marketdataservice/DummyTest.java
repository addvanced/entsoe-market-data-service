package dk.systemedz.entsoe.marketdataservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DummyTest {

    @Test
    public void dummy() {
        assertEquals("000","000");
    }
}
