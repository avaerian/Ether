import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleTest {

    @Test
    public void helloTest() {

        String str = "Hello, world!";
        assertEquals("Hello, world!", str);

    }

    @Test
    public void goodbyeTest() {

        // This test should fail
        String str = "Hello, world!";
        assertEquals("Goodbye, world!", str);

    }

}
