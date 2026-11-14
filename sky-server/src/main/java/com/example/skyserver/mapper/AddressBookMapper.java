package com.example.skyserver.mapper;

import com.example.skyserver.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    AddressBook getById(Long id);
    void insert(AddressBook addressBook);

    List<AddressBook> list(Long userId);
}