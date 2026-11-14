package com.example.skyserver.mapper;

import com.example.skyserver.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.util.List;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface EmployeeMapper {

    // 查询全部员工
    @Select("select * from employee")
    List<Employee> list();

    // 根据用户名查询员工
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(@Param("username") String username);

    // 根据姓名条件统计员工数量
    @Select("""
            select count(*)
            from employee
            where (
                #{name} is null
                or #{name} = ''
                or name like concat('%', #{name}, '%')
            )
            """)
    Long count(@Param("name") String name);

    // 分页查询员工
    @Select("""
            select *
            from employee
            where (
                #{name} is null
                or #{name} = ''
                or name like concat('%', #{name}, '%')
            )
            order by create_time desc
            limit #{offset}, #{pageSize}
            """)
    List<Employee> pageQuery(
            @Param("name") String name,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );
    @Insert("""
        insert into employee
        (
            name,
            username,
            password,
            phone,
            sex,
            id_number,
            status,
            create_time,
            update_time,
            create_user,
            update_user
        )
        values
        (
            #{name},
            #{username},
            #{password},
            #{phone},
            #{sex},
            #{idNumber},
            #{status},
            #{createTime},
            #{updateTime},
            #{createUser},
            #{updateUser}
        )
        """)
    void insert(Employee employee);
    @Select("select * from employee where id = #{id}")
    Employee getById(@Param("id") Long id);

    @Update("""
        <script>
        update employee
        <set>
            <if test="name != null">
                name = #{name},
            </if>

            <if test="username != null">
                username = #{username},
            </if>

            <if test="status != null">
                status = #{status},
            </if>

            <if test="phone != null">
                phone = #{phone},
            </if>

            <if test="sex != null">
                sex = #{sex},
            </if>

            <if test="idNumber != null">
                id_number = #{idNumber},
            </if>

            <if test="updateTime != null">
                update_time = #{updateTime},
            </if>

            <if test="updateUser != null">
                update_user = #{updateUser},
            </if>
        </set>

        where id = #{id}
        </script>
        """)
    void update(Employee employee);

    @Delete("delete from employee where id = #{id}")
    void deleteById(@Param("id") Long id);
}