package com.example.skyserver.service;

import com.example.skyserver.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    Long save(AddressBook addressBook);

    List<AddressBook> list();
}