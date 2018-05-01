package com.xhwl.recruitment.service;

import com.xhwl.recruitment.dao.AdminAuthRepository;
import com.xhwl.recruitment.dao.DepartmentRepository;
import com.xhwl.recruitment.dao.UserRepository;
import com.xhwl.recruitment.domain.AdminAuthEntity;
import com.xhwl.recruitment.domain.UserEntity;
import com.xhwl.recruitment.dto.AdminAuthDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: guiyu
 * @Description: 提供管理员的权限服务
 * @Date: Create in 下午9:46 2018/4/30
 **/
@Service
public class AdminAuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminAuthRepository adminAuthRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 人事部门的id
     */
    private static final Long PersonnelDepartmentId = 1L;

    private static final String SeniorAdminRole = "seniorAdmin";

    private static final String NormalAdminRole = "normalAdmin";

    /**
     * 人事经理添加管理员
     *
     * @param userName
     * @param passWord
     * @param departmentId
     */
    @Transactional
    public void addAdmin(String userName, String passWord, Long departmentId) {
        //保存到user表
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userName);
        userEntity.setPassword(passWord);
        userEntity.setRole("admin");
        UserEntity resUser = userRepository.save(userEntity);

        AdminAuthEntity adminAuthEntity = new AdminAuthEntity();
        adminAuthEntity.setUserId(resUser.getId());
        adminAuthEntity.setUserName(resUser.getUsername());
        adminAuthEntity.setDepartmentId(departmentId);

        if (departmentId == PersonnelDepartmentId) {
            adminAuthEntity.setRole(SeniorAdminRole);
        }else {
            adminAuthEntity.setRole(NormalAdminRole);
        }

        adminAuthRepository.save(adminAuthEntity);

    }

    /**
     * 超级管理员查看全部人员信息，分页
     * @param pageable
     * @return
     */
    public Page<AdminAuthDto> findAll(Pageable pageable){
        Page<AdminAuthEntity> adminAuthEntityPage = adminAuthRepository.findAll(pageable);
        List<AdminAuthEntity> adminAuthEntityList = adminAuthEntityPage.getContent();

        List<AdminAuthDto> adminAuthDtoList = new ArrayList<>();
        for(int i=0;i<adminAuthEntityList.size();i++){
            AdminAuthDto adminAuthDto = new AdminAuthDto();

            String department = String.valueOf(departmentRepository.findOne(adminAuthEntityList.get(i).getDepartmentId()).getId());
            String password = userRepository.findOne(adminAuthEntityList.get(i).getUserId()).getPassword();
            String username = adminAuthEntityList.get(i).getUserName();

            adminAuthDto.setId(adminAuthEntityList.get(i).getId());
            adminAuthDto.setUserName(username);
            adminAuthDto.setDepartment(department);
            adminAuthDto.setPassword(password);

            adminAuthDtoList.add(adminAuthDto);
        }

        Page<AdminAuthDto> adminAuthDtoPage = new PageImpl<AdminAuthDto>(adminAuthDtoList,pageable,adminAuthEntityPage.getTotalElements());


        return adminAuthDtoPage;
    }
}