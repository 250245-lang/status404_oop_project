package parkinglot.utils;

import parkinglot.users.Account;

public record LoginResponse(String token, Account user) {}