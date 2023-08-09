package com.infotel.mantis_api.model;

import java.util.ArrayList;
import java.util.List;

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

