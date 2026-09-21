package com.simple.service;


import com.simple.model.dto.AdminDTO;
import com.simple.result.Result;

public interface IAdminService {

    Result login(AdminDTO AdminDto);

    Result logout(String token);
}
