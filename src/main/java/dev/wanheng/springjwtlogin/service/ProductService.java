package dev.wanheng.springjwtlogin.service;

import dev.wanheng.springjwtlogin.domain.Product;

//商品服务
public interface ProductService {
    //根据id获取商品详情
    Product getDetailById(Long id);
}
