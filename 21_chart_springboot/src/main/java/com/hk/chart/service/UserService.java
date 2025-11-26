package com.hk.chart.service;

import com.hk.chart.dto.Register;
import com.hk.chart.entity.User;
import java.util.Optional;

public interface UserService {
    User register(Register dto) throws IllegalArgumentException;
    Optional<User> login(String username, String rawPassword);
}