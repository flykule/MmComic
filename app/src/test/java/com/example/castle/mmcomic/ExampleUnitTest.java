package com.example.castle.mmcomic;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testOrder() throws Exception {
        NaturalOrderComparator comparator = new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((String) o);
            }
        };
        String s1 = "死亡笔记001.jpg";
        String s2 = "死亡笔002.jpg";
        System.out.println(comparator.compare(s1, s2));
    }
}