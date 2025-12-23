package com.wonderland.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wonderland.services.ToyService;

@RestController
public class BrandController {

    @Autowired
    private ToyService toyService;

    @GetMapping("/api/brands")
    public List<String> getBrands() {
        return toyService.getUniqueBrands();
    }
}