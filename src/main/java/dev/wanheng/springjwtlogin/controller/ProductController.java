package dev.wanheng.springjwtlogin.controller;

import dev.wanheng.springjwtlogin.document.ProductDocument;
import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.dto.PlainResult;
import dev.wanheng.springjwtlogin.service.DataSourceTestService;
import dev.wanheng.springjwtlogin.service.ProductSearchService;
import dev.wanheng.springjwtlogin.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Resource
    private ProductService productService;
    @Resource
    private DataSourceTestService dataSourceTestService;
    @Resource
    private ProductSearchService productSearchService;

    @GetMapping("/detail/{id}")
    public PlainResult<Product> detail(@PathVariable Long id) {
        Product product = productService.getDetailById(id);
        return product != null ? PlainResult.success(product) : PlainResult.fail(404, "商品不存在");
    }

    //读写分离测试
    @GetMapping("/ds-test")
    public PlainResult<Map<String, Object>> dsTest() {
        Long readServerId = dataSourceTestService.getReadServerId();
        Long writeServerId = dataSourceTestService.getWriteServerId();
        return PlainResult.success(Map.of(
                "readServerId", readServerId != null ? readServerId : 0,
                "writeServerId", writeServerId != null ? writeServerId : 0,
                "readFromSlave", readServerId != null && readServerId > 1,
                "writeFromMaster", writeServerId != null && writeServerId == 1
        ));
    }

    //商品搜索
    @GetMapping("/search")
    public PlainResult<List<ProductDocument>> search(@RequestParam(required = false) String keyword) {
        List<ProductDocument> list = productSearchService.search(keyword);
        return PlainResult.success(list);
    }

    //将MySQL商品数据同步到ES
    @PostMapping("/sync")
    public PlainResult<Long> syncToEs() {
        long count = productSearchService.syncFromDb();
        return PlainResult.success(count);
    }
}
