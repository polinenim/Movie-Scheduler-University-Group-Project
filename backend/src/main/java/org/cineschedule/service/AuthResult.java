package org.cineschedule.service;

import org.cineschedule.domain.User;

public record AuthResult(User user, String token) {}
