package com.fastcampus.fcboard.controller.dto

import org.springframework.web.bind.annotation.RequestParam

class PostSearchRequest(
    @RequestParam
    val title: String?,
    @RequestParam
    val createdBy: String?,
)
