package io.github.darlene.leakdetectionapplication.domain;

import lombok.Getter;


@Getter
public enum UserRole{

    ROLE_OPERATOR("operator"),
    ROLE_VIEWER("viewer");

    String description;


    UserRole(String description){
        this.description = description;
    }
}