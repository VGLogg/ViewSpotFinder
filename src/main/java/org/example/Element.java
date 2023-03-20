package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Element implements Comparable<Element>{

    @JsonIgnore
    private int id;

    private List<Integer> nodes;

    private double value;

    @Override
    public int compareTo(Element element) {
        return Double.compare(this.value, element.value);
    }

    @JsonProperty("element_id")
    public int getId(){
        return this.id;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    @JsonProperty("value")
    public double getValue() {
        return value;
    }
}
