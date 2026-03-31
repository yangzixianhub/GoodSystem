package dev.wanheng.springjwtlogin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.wanheng.springjwtlogin.domain.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET stock = stock - #{qty}, updated_at = NOW() WHERE id = #{productId} AND stock >= #{qty}")
    int decreaseStock(@Param("productId") Long productId, @Param("qty") int qty);

    @Update("UPDATE product SET stock = stock + #{qty}, updated_at = NOW() WHERE id = #{productId}")
    int increaseStock(@Param("productId") Long productId, @Param("qty") int qty);
}
