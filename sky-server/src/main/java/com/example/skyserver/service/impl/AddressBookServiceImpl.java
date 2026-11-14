package com.example.skyserver.service.impl;

import com.example.skyserver.context.UserContext;
import com.example.skyserver.entity.AddressBook;
import com.example.skyserver.mapper.AddressBookMapper;
import com.example.skyserver.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    @Override
    public Long save(AddressBook addressBook) {

        Long userId = UserContext.getUserId();

        // 用户ID必须从登录token中获取，不能相信前端传来的userId
        addressBook.setUserId(userId);

        addressBookMapper.insert(addressBook);

        return addressBook.getId();
    }

    @Override
    public List<AddressBook> list() {

        Long userId = UserContext.getUserId();

        return addressBookMapper.list(userId);
    }
}