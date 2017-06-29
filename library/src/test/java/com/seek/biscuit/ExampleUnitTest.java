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

    }

    private int calculateInSampleSize(int width) {
        int inSampleSize = 1;
        float ratio = width / Utils.REFERENCE_WIDTH;
        if (ratio > 1.5f && ratio <= 3) {
            inSampleSize = inSampleSize << 1;
        } else if (ratio > 3) {
            inSampleSize = inSampleSize << 2;
        }
        return inSampleSize;
    }
}