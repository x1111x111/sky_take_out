package com.example.skyserver.controller;

import com.example.skyserver.dto.ReportQueryDTO;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.ReportService;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.SalesTop10ReportVO;
import com.example.skyserver.vo.TurnoverReportVO;
import com.example.skyserver.vo.UserReportVO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
@Tag(name="统计相关接口")
@SecurityRequirement(name="token")
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end) {

        TurnoverReportVO vo =
                reportService.turnoverStatistics(
                        begin,
                        end
                );

        return Result.success(vo);
    }
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end) {

        OrderReportVO vo =
                reportService.ordersStatistics(
                        begin,
                        end
                );

        return Result.success(vo);
    }
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(

            @RequestParam
            @DateTimeFormat(pattern="yyyy-MM-dd")
            LocalDate begin,


            @RequestParam
            @DateTimeFormat(pattern="yyyy-MM-dd")
            LocalDate end){

        SalesTop10ReportVO vo =
                reportService.salesTop10(
                        begin,
                        end
                );


        return Result.success(vo);
    }
    @GetMapping("/user")
    public Result<UserReportVO> user(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate end) {

        ReportQueryDTO dto = new ReportQueryDTO();
        dto.setBegin(begin);
        dto.setEnd(end);

        return Result.success(
                reportService.userReport(dto)
        );
    }
}