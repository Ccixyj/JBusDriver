package me.jbusdriver.plugin.magnet;

import kotlin.text.Charsets;
import org.junit.Test;

import java.util.Base64;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String key = "1";
        String encode = Base64.getEncoder().encodeToString(key.getBytes(Charsets.UTF_8));

        System.out.println(encode);
    }
}