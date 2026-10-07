package com.wcy.system.mapper;

import java.util.List;
import com.wcy.system.domain.NursingProjectPlan;

/**
 * 护理计划和项目关联Mapper接口
 * 
 * @author ruoyi
 * @date 2026-10-07
 */
public interface NursingProjectPlanMapper 
{
    /**
     * 查询护理计划和项目关联
     * 
     * @param id 护理计划和项目关联主键
     * @return 护理计划和项目关联
     */
    public NursingProjectPlan selectNursingProjectPlanById(Long id);

    /**
     * 查询护理计划和项目关联列表
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 护理计划和项目关联集合
     */
    public List<NursingProjectPlan> selectNursingProjectPlanList(NursingProjectPlan nursingProjectPlan);

    /**
     * 新增护理计划和项目关联
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 结果
     */
    public int insertNursingProjectPlan(NursingProjectPlan nursingProjectPlan);

    /**
     * 修改护理计划和项目关联
     * 
     * @param nursingProjectPlan 护理计划和项目关联
     * @return 结果
     */
    public int updateNursingProjectPlan(NursingProjectPlan nursingProjectPlan);

    /**
     * 删除护理计划和项目关联
     * 
     * @param id 护理计划和项目关联主键
     * @return 结果
     */
    public int deleteNursingProjectPlanById(Long id);

    /**
     * 批量删除护理计划和项目关联
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNursingProjectPlanByIds(Long[] ids);

    /**
     * 根据计划id查询关联的护理项目明细列表
     *
     * @param planId 护理计划id
     * @return 护理计划和项目关联集合
     */
    public List<NursingProjectPlan> selectNursingProjectPlanByPlanId(Long planId);

    /**
     * 根据计划id批量删除关联的护理项目明细
     *
     * @param planIds 护理计划id集合
     * @return 结果
     */
    public int deleteNursingProjectPlanByPlanIds(Long[] planIds);

    /**
     * 批量新增护理计划和项目关联
     *
     * @param list 护理计划和项目关联集合
     * @return 结果
     */
    public int batchNursingProjectPlan(List<NursingProjectPlan> list);
}
