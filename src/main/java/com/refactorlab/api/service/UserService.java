package com.refactorlab.api.service;

public interface UserService {

    // Kullanıcı yoksa parolasını Argon2id ile hash'leyip rolleriyle birlikte oluşturur
    void createUserIfMissing(String theUsername, String theRawPassword, String... theRoles);
}
