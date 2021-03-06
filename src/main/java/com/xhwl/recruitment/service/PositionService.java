package com.xhwl.recruitment.service;

import com.xhwl.recruitment.dao.PositionRepository;
import com.xhwl.recruitment.domain.AdminAuthEntity;
import com.xhwl.recruitment.domain.PositionEntity;
import com.xhwl.recruitment.vo.PositionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author: guiyu
 * @Description: 处理职位的增删改查
 * @Date: Create in 上午10:49 2018/4/23
 **/
@Service
@Slf4j
public class PositionService {
    @Autowired
    PositionRepository positionRepository;

    /**
     * 管理员添加或修改职位，添加时传入id为null
     *
     * @param positionVo
     * @return
     */
    public PositionEntity addPosition(PositionVo positionVo) {
        PositionEntity positionEntity = new PositionEntity();
        if (positionVo.getId() == null) {
            positionVo.setId(0L);
        }
        BeanUtils.copyProperties(positionVo, positionEntity);

        //写入截止日期
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String deadline = positionVo.getDeadline();
        try {
            positionEntity.setDeadline(new Date(dateFormat.parse(deadline).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //写入发布状态,直接发布
        positionEntity.setPublishType(1);

        //创建时，写入发布时间，就是当前时间
        if (positionVo.getId() == 0) {
            Date currentDate = new java.sql.Date(System.currentTimeMillis());
            positionEntity.setPublishDate(currentDate);
        } else {
            positionEntity.setPublishDate(positionRepository.findOne(positionVo.getId()).getPublishDate());
        }

        //写入创建时间和修改时间
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        //第一次创建修改GmtCreate
        if (positionVo.getId() == 0) {
            positionEntity.setGmtCreate(timestamp);
        } else {
            positionEntity.setGmtCreate(positionRepository.findOne(positionVo.getId()).getGmtCreate());
        }

        positionEntity.setGmtModified(timestamp);

//        log.info("the result of addPosition:{}", positionEntity);
        return positionRepository.save(positionEntity);
    }

    /**
     * 管理员按position获取岗位详情，较用户获取更多的信息
     *
     * @param positionId
     * @return
     */
    public HashMap<String, String> adminGetPosition(Long positionId) {
        PositionEntity position = positionRepository.findOne(positionId);
        if (position == null) return null;

        HashMap<String, String> hashMap = new LinkedHashMap<>();
        hashMap.put("id", new Long(position.getId()).toString());
        hashMap.put("positionName", position.getPositionName());
        hashMap.put("department", String.valueOf(position.getDepartment()));
        hashMap.put("resumeAuditDepartment", String.valueOf(position.getResumeAuditDepartment()));
        hashMap.put("assessmentDepartment", String.valueOf(position.getAssessmentDepartment()));
        hashMap.put("positionType", position.getPositionType());
        hashMap.put("recruitmentType", String.valueOf(position.getRecruitmentType()));
        hashMap.put("workPlace", position.getWorkPlace());
        hashMap.put("education", position.getEducation());
        hashMap.put("recruitingNumbers", position.getRecruitingNumbers().toString());
        hashMap.put("deadline", String.valueOf(position.getDeadline()));
        hashMap.put("jobResponsibilities", position.getJobResponsibilities());
        hashMap.put("jobRequirements", position.getJobRequirements());
        return hashMap;
    }

    /**
     * 普通管理员查看发布中的工作，限本部门
     *
     * @return
     */
    public Page<HashMap> adminGetDepartmentPositions(Pageable pageable, Long department, Integer publicType) {
        Page<PositionEntity> positionEntityPage = positionRepository.findAllByDepartmentAndPublishType(pageable, department, publicType);
        List<PositionEntity> positions = positionEntityPage.getContent();

        List<HashMap> res = new ArrayList<>();
        for (PositionEntity position : positions) {
            HashMap<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("id", new Long(position.getId()).toString());
            hashMap.put("positionName", position.getPositionName());
            hashMap.put("department", String.valueOf(position.getDepartment()));
            hashMap.put("recruitmentType", String.valueOf(position.getRecruitmentType()));
            hashMap.put("workPlace", position.getWorkPlace());
            hashMap.put("publishDate", String.valueOf(position.getPublishDate()));
            hashMap.put("deadline", String.valueOf(position.getDeadline()));
            res.add(hashMap);
        }
        Page<HashMap> resPage = new PageImpl<>(res, pageable, positionEntityPage.getTotalElements());
        return resPage;
    }

    /**
     * 人事部门查看所有已经发布的工作
     *
     * @param pageable
     * @param publicType
     * @return
     */
    public Page<HashMap> adminGetAllPublishPositions(Pageable pageable, Integer publicType) {
        Page<PositionEntity> positionEntityPage = positionRepository.findAllByPublishType(pageable, publicType);
        List<PositionEntity> positions = positionEntityPage.getContent();

        List<HashMap> res = new ArrayList<>();
        for (PositionEntity position : positions) {
            HashMap<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("id", new Long(position.getId()).toString());
            hashMap.put("positionName", position.getPositionName());
            hashMap.put("department", String.valueOf(position.getDepartment()));
            hashMap.put("recruitmentType", String.valueOf(position.getRecruitmentType()));
            hashMap.put("workPlace", position.getWorkPlace());
            hashMap.put("publishDate", String.valueOf(position.getPublishDate()));
            hashMap.put("deadline", String.valueOf(position.getDeadline()));
            res.add(hashMap);
        }
        Page<HashMap> resPage = new PageImpl<>(res, pageable, positionEntityPage.getTotalElements());
        return resPage;
    }

    /**
     * 管理员删除岗位
     *
     * @param positionId
     */
    public void adminDeletePosition(Long positionId) {
        positionRepository.delete(positionId);
    }


    /**
     * 根据类型查询不同的校招岗位 1.校招 2.社招 3.实习
     *
     * @param recruitmentType
     * @return
     */
    public List<HashMap> getUnderwayPositions(Integer recruitmentType) {
        List<PositionEntity> positions = positionRepository.findAll();

        List<HashMap> res = new ArrayList<>();
        for (PositionEntity position : positions) {
            //简历已经发布
            if (position.getPublishType() == 1) {
                //判断简历类型
                if (position.getRecruitmentType() == recruitmentType) {
                    HashMap<String, String> hashMap = new LinkedHashMap<>();
                    hashMap.put("id", new Long(position.getId()).toString());
                    hashMap.put("positionName", position.getPositionName());
                    hashMap.put("positionType", position.getPositionType());
                    hashMap.put("workPlace", position.getWorkPlace());
                    hashMap.put("recruitingNumbers", position.getRecruitingNumbers().toString());
                    hashMap.put("publishDate", position.getPublishDate().toString());
                    hashMap.put("department", String.valueOf(position.getDepartment()));
                    hashMap.put("jobResponsibilities", position.getJobResponsibilities());
                    hashMap.put("jobRequirements", position.getJobRequirements());
                    res.add(hashMap);
                }
            }
        }
        return res;
    }

    /**
     * 按照岗位id号码发出岗位详细信息，用户访问
     *
     * @param positionId
     * @return
     */
    public HashMap<String, String> getPosition(Long positionId) {
        PositionEntity position = positionRepository.findOne(positionId);
        if (position == null) return null;
        if (position.getPublishType() == 1) {
            //简历已经处于被发布状态
            HashMap<String, String> hashMap = new LinkedHashMap<>();
            hashMap.put("id", new Long(position.getId()).toString());
            hashMap.put("positionName", position.getPositionName());
            hashMap.put("positionType", position.getPositionType());
            hashMap.put("department", String.valueOf(position.getDepartment()));
            hashMap.put("recruitingNumbers", position.getRecruitingNumbers().toString());
            hashMap.put("workPlace", position.getWorkPlace());
            hashMap.put("education", position.getEducation());
            hashMap.put("publishDate", position.getPublishDate().toString());
            hashMap.put("jobResponsibilities", position.getJobResponsibilities());
            hashMap.put("jobRequirements", position.getJobRequirements());
            return hashMap;
        }
        return null;
    }


}
