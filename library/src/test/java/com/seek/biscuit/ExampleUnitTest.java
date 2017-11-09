package com.seek.biscuit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        ImagePath imagePath = new ImagePath("apk/bbs123.apk");
        System.out.println("name : "+imagePath.name);
        System.out.println("type : "+imagePath.type);
        System.out.println("type : "+imagePath.type);
    }
}