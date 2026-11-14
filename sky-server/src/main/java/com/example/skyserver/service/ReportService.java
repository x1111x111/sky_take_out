package com.example.skyserver.service;

import com.example.skyserver.dto.ReportQueryDTO;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.SalesTop10ReportVO;
import com.example.skyserver.vo.TurnoverReportVO;
import com.example.skyserver.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    TurnoverReportVO turnoverStatistics(
            LocalDate begin,
            LocalDate end
    );
    OrderReportVO ordersStatistics(
            LocalDate begin,
            LocalDate end
    );
    SalesTop10ReportVO salesTop10(
            LocalDate begin,
            LocalDate end
    );
    UserReportVO userReport(
            ReportQueryDTO dto
    );
}