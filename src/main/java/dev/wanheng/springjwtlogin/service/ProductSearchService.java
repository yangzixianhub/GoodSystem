package dev.wanheng.springjwtlogin.service;

import dev.wanheng.springjwtlogin.document.ProductDocument;
import dev.wanheng.springjwtlogin.domain.Product;
import dev.wanheng.springjwtlogin.mapper.ProductMapper;
import dev.wanheng.springjwtlogin.repository.ProductSearchRepository;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProductSearchService {

    @Resource
    private ProductSearchRepository productSearchRepository;
    @Resource
    private ProductMapper productMapper;

    //按关键词搜索商品
    public List<ProductDocument> search(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return StreamSupport.stream(productSearchRepository.findAll().spliterator(), false)
                        .limit(100).collect(Collectors.toList());
            }
            return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword.trim(), keyword.trim());
        } catch (NoSuchIndexException e) {
            //ES索引尚未创建/尚未同步数据时，返回空列表以便前端提示用户同步
            return List.of();
        }
    }

    //将MySQL商品表数据同步到Elasticsearch
    public long syncFromDb() {
        List<Product> list = productMapper.selectList(null);
        List<ProductDocument> docs = list.stream().map(this::toDocument).collect(Collectors.toList());
        productSearchRepository.saveAll(docs);
        return docs.size();
    }

    private ProductDocument toDocument(Product p) {
        ProductDocument d = new ProductDocument();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setStock(p.getStock());
        return d;
    }
}
