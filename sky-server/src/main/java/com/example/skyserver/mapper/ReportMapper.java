package com.example.skyserver.mapper;

import com.example.skyserver.dto.ReportQueryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {


    /*List<Map<String,Object>> turnover(
            ReportQueryDTO dto
    );*/


    List<Map<String,Object>> userReport(
            ReportQueryDTO dto
    );

}