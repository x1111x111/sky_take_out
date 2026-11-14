package com.example.skyserver.service;

import com.example.skyserver.dto.EmployeeLoginDTO;
import com.example.skyserver.entity.Employee;
import com.example.skyserver.dto.EmployeePageQueryDTO;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.vo.EmployeeVO;
import java.util.List;
import com.example.skyserver.dto.EmployeeSaveDTO;
import com.example.skyserver.dto.EmployeeUpdateDTO;

public interface EmployeeService {

    List<Employee> list();

    Employee login(EmployeeLoginDTO employeeLoginDTO);

    PageResult<EmployeeVO> pageQuery(
            EmployeePageQueryDTO employeePageQueryDTO
    );

    void save(EmployeeSaveDTO employeeSaveDTO);

    EmployeeVO getById(Long id);

    void update(EmployeeUpdateDTO employeeUpdateDTO);

    void updateStatus(Integer status, Long id);

    void deleteById(Long id);
}