package com.infotel.mantis_api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Relations {
    List<Integer> parentOf;
    List<Integer> childOf;
    List<Integer> duplicateOf;
    List<Integer> hasDuplicate;
    List<Integer> relatedTo;
    
    public Relations () {
        parentOf = new ArrayList<>();
        childOf = new ArrayList<>();
        duplicateOf = new ArrayList<>();
        hasDuplicate = new ArrayList<>();
        relatedTo = new ArrayList<>();
    }
}
