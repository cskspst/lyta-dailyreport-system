package com.techacademy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    // ログイン画面表示
    @GetMapping(value = "/login")
    public String login() {
        return "login/login";
    }

    // ログイン後のトップページ表示
    @GetMapping(value = "/")
    public String top() {
        // 開始 : 変更 : Lesson 34 Chapter 7 課題
        return "redirect:/reports";
        // return "redirect:/employees";
        // 終了 : 変更 : Lesson 34 Chapter 7 課題
    }

}
