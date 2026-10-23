package com.example.textileMultiModalAgent.service.ai;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.textileMultiModalAgent.domain.AiFabricItem;
import com.example.textileMultiModalAgent.domain.vo.FabricItemVo;
import java.util.List;

/**
* @author Jim
* @description 针对表【ai_fabric_item(布号主数据表)】的数据库操作Service
*/
public interface AiFabricItemService extends IService<AiFabricItem> {

    /**
     * 上架面料列表（智能报价下拉数据源），按布号升序
     *
     * @return 前端契约 VO 列表（含成本五项拆分）
     */
    List<FabricItemVo> listOnSale();
}
