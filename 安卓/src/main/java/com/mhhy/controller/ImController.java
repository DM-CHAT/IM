package com.mhhy.controller;

import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/im")
@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class ImController {




}
