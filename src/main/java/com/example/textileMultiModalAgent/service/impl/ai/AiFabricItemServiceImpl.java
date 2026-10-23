package com.example.textileMultiModalAgent.service.impl.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.textileMultiModalAgent.domain.AiFabricItem;
import com.example.textileMultiModalAgent.domain.vo.FabricItemVo;
import com.example.textileMultiModalAgent.mapper.AiFabricItemMapper;
import com.example.textileMultiModalAgent.service.ai.AiFabricItemService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【ai_fabric_item(布号主数据表)】的数据库操作Service实现
*/
@Service
public class AiFabricItemServiceImpl extends ServiceImpl<AiFabricItemMapper, AiFabricItem>
    implements AiFabricItemService {

    @Override
    public List<FabricItemVo> listOnSale() {
        LambdaQueryWrapper<AiFabricItem> wrapper = new LambdaQueryWrapper<AiFabricItem>()
                .eq(AiFabricItem::getStatus, 1)
                .eq(AiFabricItem::getDeleted, 0)
                .orderByAsc(AiFabricItem::getFabric_code);
        return list(wrapper).stream().map(this::toVo).toList();
    }

    /** 实体转前端契约 VO（snake_case 列 -> camelCase + 嵌套 cost） */
    private FabricItemVo toVo(AiFabricItem item) {
        FabricItemVo vo = new FabricItemVo();
        vo.setCode(item.getFabric_code());
        vo.setName(item.getFabric_name());
        vo.setNameEn(item.getFabric_name_en());
        vo.setComposition(item.getComposition());
        vo.setSpec(item.getSpec());
        vo.setWeight(item.getWeight());
        vo.setWidth(item.getWidth());
        vo.setLeadTime(item.getLead_time());
        vo.setMoq(item.getMoq());
        FabricItemVo.Cost cost = new FabricItemVo.Cost();
        cost.setMaterial(item.getCost_material());
        cost.setProcess(item.getCost_process());
        cost.setDyeing(item.getCost_dyeing());
        cost.setTest(item.getCost_test());
        cost.setPacking(item.getCost_packing());
        vo.setCost(cost);
        return vo;
    }
}
