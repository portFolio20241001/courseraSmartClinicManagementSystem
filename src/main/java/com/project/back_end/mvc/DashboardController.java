package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.CommonService;

@Controller
public class DashboardController {

    @Autowired
    private CommonService commonservice;

    // 管理者ダッシュボード
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        boolean isValid = commonservice.isTokenValid(token, "admin");

        if (isValid) {
            return "admin/adminDashboard"; // templates/admin/adminDashboard.html
        } else {
            return "redirect:/"; // トップページ（ログイン画面など）にリダイレクト
        }
    }

    // 医師ダッシュボード
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        boolean isValid = commonservice.isTokenValid(token, "doctor");

        if (isValid) {
            return "doctor/doctorDashboard"; // templates/doctor/doctorDashboard.html
        } else {
            return "redirect:/";
        }
    }
}
