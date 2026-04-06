package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;


import jakarta.validation.Valid;

import io.github.darlene.leakdetectionapplication.dto.request.RegisterRequest;
import io.github.darlene.leakdetectionapplication.dto.request.LoginRequest;
import io.github.darlene.leakdetectionapplication.dto.request.RefreshTokenRequest;


import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("")
@Slf4j
@Validated
@Tag    // OpenAI/ Swagger documentation

public class StatusController{

}