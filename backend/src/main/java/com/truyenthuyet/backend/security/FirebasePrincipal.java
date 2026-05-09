package com.truyenthuyet.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FirebasePrincipal {
    private final String uid;
    private final String displayName;
    private final String email;
}
