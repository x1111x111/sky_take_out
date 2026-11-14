package com.example.skyserver.controller;

import com.example.skyserver.result.Result;
import com.example.skyserver.service.WorkspaceService;
import com.example.skyserver.vo.BusinessDataVO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SecurityRequirement(name = "token")
@Tag(name = "后台相关接口")
@RestController
@RequestMapping("/admin/workspace")
@RequiredArgsConstructor
public class WorkspaceController {


    private final WorkspaceService workspaceService;



    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){


        return Result.success(
                workspaceService.businessData()
        );

    }

}

