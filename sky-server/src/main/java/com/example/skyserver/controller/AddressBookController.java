package com.example.skyserver.controller;

import com.example.skyserver.entity.AddressBook;
import com.example.skyserver.result.Result;
import com.example.skyserver.service.AddressBookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name="支付订单相关接口")
@SecurityRequirement(name="token")
@RestController
@RequestMapping("/user/addressBook")
@RequiredArgsConstructor
public class AddressBookController {

    private final AddressBookService addressBookService;

    /**
     * 新增收货地址
     */
    @PostMapping
    public Result<Long> save(
            @RequestBody AddressBook addressBook) {

        Long id = addressBookService.save(addressBook);

        return Result.success(id);
    }

    /**
     * 查询当前用户的地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {

        return Result.success(
                addressBookService.list()
        );
    }
}