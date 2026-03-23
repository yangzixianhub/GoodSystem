package dev.wanheng.springjwtlogin.repository;

import dev.wanheng.springjwtlogin.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}
