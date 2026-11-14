package com.example.skyserver.controller;

import com.example.skyserver.dto.EmployeeLoginDTO;
import com.example.skyserver.entity.Employee;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.EmployeeService;
import com.example.skyserver.utils.JwtUtil;
import com.example.skyserver.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import com.example.skyserver.dto.EmployeePageQueryDTO;
import com.example.skyserver.result.PageResult;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import com.example.skyserver.vo.EmployeeVO;
import com.example.skyserver.dto.EmployeeSaveDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.skyserver.dto.EmployeeUpdateDTO;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name="员工相关接口")
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;

    public EmployeeController(EmployeeService employeeService,
                              JwtUtil jwtUtil) {
        this.employeeService = employeeService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public Result<EmployeeLoginVO> login(
            @RequestBody EmployeeLoginDTO employeeLoginDTO) {

        // 1. 管理员登录
        Employee employee =
                employeeService.login(employeeLoginDTO);

        // 2. 生成 JWT 声明
        Map<String, Object> claims = new HashMap<>();

        claims.put("empId", employee.getId());

        // 增加管理员身份
        claims.put(JwtUtil.CLAIM_ROLE, JwtUtil.ROLE_ADMIN);

        // 3. 生成 Token
        String token = jwtUtil.createJWT(claims);

        // 4. 返回登录信息
        EmployeeLoginVO employeeLoginVO =
                new EmployeeLoginVO();

        employeeLoginVO.setId(employee.getId());
        employeeLoginVO.setUsername(employee.getUsername());
        employeeLoginVO.setName(employee.getName());
        employeeLoginVO.setToken(token);

        return Result.success(employeeLoginVO);
    }

    @GetMapping("/list")
    public Result<List<Employee>> list(

            @Parameter(
                    name = "token",
                    description = "登录成功后返回的JWT Token",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("token") String token) {

        return Result.success(employeeService.list());
    }
    @GetMapping("/page")
    @SecurityRequirement(name = "token")
    public Result<PageResult<EmployeeVO>> page(

            @Parameter(
                    description = "JWT Token",
                    required = true,
                    in = ParameterIn.HEADER
            )
            @RequestHeader("token") String token,

            @ParameterObject
            EmployeePageQueryDTO employeePageQueryDTO) {

        PageResult<EmployeeVO> pageResult =
                employeeService.pageQuery(employeePageQueryDTO);

        return Result.success(pageResult);
    }
    @PostMapping
    @SecurityRequirement(name = "token")
    public Result<String> save(
            @RequestBody EmployeeSaveDTO employeeSaveDTO) {

        employeeService.save(employeeSaveDTO);

        return Result.success();
    }
    @GetMapping("/{id}")
    @SecurityRequirement(name = "token")
    public Result<EmployeeVO> getById(
            @PathVariable Long id) {

        EmployeeVO employeeVO =
                employeeService.getById(id);

        return Result.success(employeeVO);
    }
    @PutMapping
    @SecurityRequirement(name = "token")
    public Result<String> update(
            @RequestBody EmployeeUpdateDTO employeeUpdateDTO) {

        employeeService.update(employeeUpdateDTO);

        return Result.success();
    }
    @PostMapping("/status/{status}")
    @SecurityRequirement(name = "token")
    public Result<String> updateStatus(
            @PathVariable Integer status,
            @RequestParam Long id) {

        employeeService.updateStatus(status, id);

        return Result.success();
    }
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "token")
    public Result<String> delete(
            @PathVariable Long id) {

        employeeService.deleteById(id);

        return Result.success();
    }
}