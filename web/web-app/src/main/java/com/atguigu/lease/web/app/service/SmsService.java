package com.atguigu.lease.web.app.service;

public interface SmsService {
    String getCode(String phone) throws Exception;
}
