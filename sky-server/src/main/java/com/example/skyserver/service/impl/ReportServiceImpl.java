package com.example.skyserver.service.impl;

import com.example.skyserver.dto.ReportQueryDTO;
import com.example.skyserver.mapper.OrderDetailMapper;
import com.example.skyserver.mapper.OrdersMapper;
import com.example.skyserver.mapper.ReportMapper;
import com.example.skyserver.service.ReportService;
import com.example.skyserver.vo.OrderReportVO;
import com.example.skyserver.vo.SalesTop10ReportVO;
import com.example.skyserver.vo.TurnoverReportVO;
import com.example.skyserver.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ReportMapper reportMapper;
    @Override
    public TurnoverReportVO turnoverStatistics(
            LocalDate begin,
            LocalDate end) {

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> turnoverList = new ArrayList<>();

        LocalDate current = begin;

        while (!current.isAfter(end)) {

            dateList.add(current.toString());

            // 当前日期 00:00:00
            LocalDateTime beginTime =
                    current.atStartOfDay();

            // 第二天 00:00:00
            LocalDateTime endTime =
                    current.plusDays(1)
                            .atStartOfDay();

            BigDecimal turnover =
                    ordersMapper.sumByDate(
                            beginTime,
                            endTime
                    );

            // 当天没有完成订单时，sum会返回null
            if (turnover == null) {
                turnover = BigDecimal.ZERO;
            }

            turnoverList.add(turnover);

            current = current.plusDays(1);
        }

        TurnoverReportVO vo =
                new TurnoverReportVO();

        vo.setDateList(dateList);
        vo.setTurnoverList(turnoverList);

        return vo;
    }
    @Override
    public OrderReportVO ordersStatistics(
            LocalDate begin,
            LocalDate end) {

        List<String> dateList = new ArrayList<>();

        List<Integer> orderCountList =
                new ArrayList<>();

        List<Integer> validOrderCountList =
                new ArrayList<>();


        int totalOrderCount = 0;

        int validOrderCount = 0;


        LocalDate current = begin;


        while (!current.isAfter(end)) {

            // 当前日期
            dateList.add(current.toString());


            LocalDateTime beginTime =
                    current.atStartOfDay();

            LocalDateTime endTime =
                    current
                            .plusDays(1)
                            .atStartOfDay();


            // 查询当天全部订单
            Integer orderCount =
                    ordersMapper.countByDate(
                            beginTime,
                            endTime,
                            null
                    );


            // 查询当天已完成订单
            Integer validCount =
                    ordersMapper.countByDate(
                            beginTime,
                            endTime,
                            5
                    );


            orderCountList.add(orderCount);

            validOrderCountList.add(validCount);


            totalOrderCount += orderCount;

            validOrderCount += validCount;


            current = current.plusDays(1);
        }


        // 计算完成率
        double orderCompletionRate = 0.0;

        if (totalOrderCount != 0) {

            orderCompletionRate =
                    (double) validOrderCount
                            / totalOrderCount;
        }


        OrderReportVO vo =
                new OrderReportVO();

        vo.setDateList(dateList);

        vo.setOrderCountList(
                orderCountList
        );

        vo.setValidOrderCountList(
                validOrderCountList
        );

        vo.setTotalOrderCount(
                totalOrderCount
        );

        vo.setValidOrderCount(
                validOrderCount
        );

        vo.setOrderCompletionRate(
                orderCompletionRate
        );


        return vo;
    }
    @Override
    public SalesTop10ReportVO salesTop10(
            LocalDate begin,
            LocalDate end) {


        LocalDateTime beginTime =
                begin.atStartOfDay();


        LocalDateTime endTime =
                end.plusDays(1)
                        .atStartOfDay();


        List<Map<String,Object>> list =
                orderDetailMapper.getSalesTop10(
                        beginTime,
                        endTime
                );


        List<String> nameList =
                new ArrayList<>();


        List<Integer> numberList =
                new ArrayList<>();


        for(Map<String,Object> map : list){

            nameList.add(
                    (String)map.get("name")
            );


            numberList.add(
                    ((Number)map.get("number"))
                            .intValue()
            );
        }


        SalesTop10ReportVO vo =
                new SalesTop10ReportVO();


        vo.setNameList(nameList);

        vo.setNumberList(numberList);


        return vo;
    }
    @Override
    public UserReportVO userReport(
            ReportQueryDTO dto) {


        List<Map<String,Object>> list =
                reportMapper.userReport(dto);



        List<String> dates =
                new ArrayList<>();


        List<Integer> counts =
                new ArrayList<>();


        for(Map<String,Object> map:list){


            dates.add(
                    map.get("date")
                            .toString()
            );


            counts.add(
                    Integer.valueOf(
                            map.get("count")
                                    .toString()
                    )
            );

        }



        UserReportVO vo =
                new UserReportVO();


        vo.setDateList(dates);

        vo.setNewUserList(counts);


        return vo;
    }
}