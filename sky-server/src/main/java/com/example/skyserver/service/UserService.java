package com.example.skyserver.service;

import com.example.skyserver.dto.UserLoginDTO;

public interface UserService {


    String login(
            UserLoginDTO dto
    );

}