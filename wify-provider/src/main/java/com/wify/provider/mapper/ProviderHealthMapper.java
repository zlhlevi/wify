package com.wify.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wify.provider.entity.ProviderHealth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface ProviderHealthMapper extends BaseMapper<ProviderHealth> {

    @Select("SELECT * FROM provider_health WHERE provider_id = #{providerId}")
    Optional<ProviderHealth> findByProviderId(Long providerId);
}
