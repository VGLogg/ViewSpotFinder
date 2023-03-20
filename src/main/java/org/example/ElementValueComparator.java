package org.example;

import java.util.Comparator;

public class ElementValueComparator implements Comparator<Element> {

    @Override
    public int compare(Element e1, Element e2) {
        return Double.compare(e1.getValue(), e2.getValue());
    }

}
