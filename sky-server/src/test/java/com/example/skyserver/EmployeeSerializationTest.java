package com.example.skyserver;

import com.example.skyserver.entity.Employee;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeSerializationTest {
    @Test
    void employeeResponseDoesNotExposePasswordHash() {
        var employee = new Employee();
        employee.setUsername("example");
        employee.setPassword("sensitive-hash");
        var tree = JsonMapper.builder().build().valueToTree(employee);
        assertFalse(tree.has("password"));
        assertEquals("example", tree.get("username").asText());
        assertEquals("sensitive-hash", employee.getPassword());
    }
}
