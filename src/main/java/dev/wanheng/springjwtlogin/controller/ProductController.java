package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.dto.PlainResult;
import dev.wanheng.springjwtlogin.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Resource
    private ProductService productService;

    //商品详情
    @GetMapping("/detail/{id}")
    public PlainResult<Product> detail(@PathVariable Long id) {
        Product product = productService.getDetailById(id);
        return product != null ? PlainResult.success(product) : PlainResult.fail(404, "商品不存在");
    }
}
