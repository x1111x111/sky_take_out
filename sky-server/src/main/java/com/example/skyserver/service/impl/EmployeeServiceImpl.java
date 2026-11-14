package com.example.skyserver.service.impl;

import com.example.skyserver.dto.EmployeeLoginDTO;
import com.example.skyserver.dto.EmployeePageQueryDTO;
import com.example.skyserver.entity.Employee;
import com.example.skyserver.exception.BusinessException;
import com.example.skyserver.mapper.EmployeeMapper;
import com.example.skyserver.result.PageResult;
import com.example.skyserver.service.EmployeeService;
import org.springframework.stereotype.Service;
import com.example.skyserver.vo.EmployeeVO;
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;
import java.util.List;
import com.example.skyserver.dto.EmployeeSaveDTO;
import java.time.LocalDateTime;
import com.example.skyserver.dto.EmployeeUpdateDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(
            EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder) {

        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> list() {
        return employeeMapper.list();
    }

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {

        // 1. 根据用户名查询员工
        Employee employee =
                employeeMapper.getByUsername(employeeLoginDTO.getUsername());

        // 2. 判断账号是否存在
        if (employee == null) {
            throw new BusinessException("账号不存在");
        }

        // 3. 判断密码是否正确
        if (!passwordEncoder.matches(
                employeeLoginDTO.getPassword(),
                employee.getPassword())) {

            throw new BusinessException("密码错误");
        }
        // 4. 判断账号是否被禁用
        if (employee.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 5. 登录成功
        return employee;
    }
    @Override
    public PageResult<EmployeeVO> pageQuery(
            EmployeePageQueryDTO dto) {

        // 1. 查询符合条件的员工总数
        Long total =
                employeeMapper.count(dto.getName());

        // 2. 计算分页起点
        Integer offset =
                (dto.getPage() - 1) * dto.getPageSize();

        // 3. 查询当前页
        List<Employee> employeeList =
                employeeMapper.pageQuery(
                        dto.getName(),
                        offset,
                        dto.getPageSize()
                );

        // 4. Entity 转 VO
        List<EmployeeVO> records = new ArrayList<>();

        for (Employee employee : employeeList) {

            EmployeeVO employeeVO = new EmployeeVO();

            BeanUtils.copyProperties(
                    employee,
                    employeeVO
            );

            records.add(employeeVO);
        }

        return new PageResult<>(total, records);
    }
    @Override
    public void save(EmployeeSaveDTO employeeSaveDTO) {

        // 1. 查询用户名是否已经存在
        Employee existEmployee =
                employeeMapper.getByUsername(
                        employeeSaveDTO.getUsername()
                );

        if (existEmployee != null) {
            throw new BusinessException("用户名已经存在");
        }

        // 2. 创建数据库实体对象
        Employee employee = new Employee();

        // 3. 把DTO中的数据复制到Employee
        BeanUtils.copyProperties(
                employeeSaveDTO,
                employee
        );

        // 4. 设置服务器控制的默认值
        employee.setPassword(
                passwordEncoder.encode("123456")
        );

        employee.setStatus(1);

        employee.setCreateTime(LocalDateTime.now());

        employee.setUpdateTime(LocalDateTime.now());

        // 暂时不处理当前登录人的ID
        employee.setCreateUser(null);
        employee.setUpdateUser(null);

        // 5. 写入数据库
        employeeMapper.insert(employee);
    }
    @Override
    public EmployeeVO getById(Long id) {

        // 1. 查询数据库
        Employee employee =
                employeeMapper.getById(id);

        // 2. 判断员工是否存在
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 3. Entity转换成VO
        EmployeeVO employeeVO =
                new EmployeeVO();

        BeanUtils.copyProperties(
                employee,
                employeeVO
        );

        return employeeVO;
    }
    @Override
    public void update(EmployeeUpdateDTO dto) {

        // 1. 根据ID查询员工
        Employee oldEmployee =
                employeeMapper.getById(dto.getId());

        // 2. 判断员工是否存在
        if (oldEmployee == null) {
            throw new BusinessException("员工不存在");
        }

        // 3. 如果修改了用户名，检查用户名是否重复
        if (dto.getUsername() != null) {

            Employee sameUsernameEmployee =
                    employeeMapper.getByUsername(
                            dto.getUsername()
                    );

            if (sameUsernameEmployee != null
                    && !sameUsernameEmployee.getId().equals(dto.getId())) {

                throw new BusinessException("用户名已经存在");
            }
        }

        // 4. DTO转换成Entity
        Employee employee = new Employee();

        BeanUtils.copyProperties(dto, employee);

        // 5. 设置修改时间
        employee.setUpdateTime(LocalDateTime.now());

        // 目前暂时不记录修改人
        employee.setUpdateUser(null);

        // 6. 更新数据库
        employeeMapper.update(employee);
    }
    @Override
    public void updateStatus(Integer status, Long id) {

        // 1. 检查状态是否合法
        if (status != 0 && status != 1) {
            throw new BusinessException("员工状态不合法");
        }

        // 2. 检查员工是否存在
        Employee oldEmployee = employeeMapper.getById(id);

        if (oldEmployee == null) {
            throw new BusinessException("员工不存在");
        }

        // 3. 创建只包含需要修改字段的Employee
        Employee employee = new Employee();

        employee.setId(id);
        employee.setStatus(status);
        employee.setUpdateTime(LocalDateTime.now());

        // 当前暂时还没保存登录人的ID
        employee.setUpdateUser(null);

        // 4. 调用之前的动态update
        employeeMapper.update(employee);
    }
    @Override
    public void deleteById(Long id) {

        // 1. 先检查员工是否存在
        Employee employee = employeeMapper.getById(id);

        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 2. 执行删除
        employeeMapper.deleteById(id);
    }
}