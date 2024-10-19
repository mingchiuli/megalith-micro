package org.chiu.micro.user.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.lang.StatusEnum;
import org.chiu.micro.common.lang.TypeEnum;
import org.chiu.micro.user.utils.SpringUtils;
import org.chiu.micro.user.entity.MenuEntity;
import org.chiu.micro.user.repository.MenuRepository;
import org.chiu.micro.user.req.MenuEntityReq;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static org.chiu.micro.common.lang.ExceptionMessage.*;


public class MenuValueConstraintValidator implements ConstraintValidator<MenuValue, MenuEntityReq> {

    @Override
    public boolean isValid(MenuEntityReq menu, ConstraintValidatorContext context) {
        if (Objects.isNull(menu.parentId())) {
            return false;
        }

        if (!StringUtils.hasLength(menu.title())) {
            return false;
        }

        if (Objects.isNull(menu.orderNum())) {
            return false;
        }

        if (Objects.isNull(menu.type())) {
            return false;
        }

        if (!StatusEnum.NORMAL.getCode().equals(menu.status()) && !StatusEnum.HIDE.getCode().equals(menu.status())) {
            return false;
        }

        MenuRepository menuRepository = SpringUtils.getBean(MenuRepository.class);

        Integer type = menu.type();
        TypeEnum typeEnum = TypeEnum.getInstance(type);
        Long parentId = menu.parentId();
        TypeEnum parentTypeEnum;
        if (!Long.valueOf(0).equals(parentId)) {
            MenuEntity parentMenu = menuRepository.findById(parentId)
                    .orElseThrow(() -> new MissException(NO_FOUND.toString()));
            parentTypeEnum = TypeEnum.getInstance(parentMenu.getType());
        } else {
            parentTypeEnum = TypeEnum.CATALOGUE;
        }

        //按钮不能有子元素
        context.disableDefaultConstraintViolation();
        if (TypeEnum.BUTTON.equals(parentTypeEnum)) {
            context.buildConstraintViolationWithTemplate(BUTTON_MUST_NOT_PARENT.getMsg()).addConstraintViolation();
            return false;
        }

        //菜单的子元素不能是菜单或分类
        if (TypeEnum.MENU.equals(parentTypeEnum) && !TypeEnum.BUTTON.equals(typeEnum)) {
            context.buildConstraintViolationWithTemplate(MENU_CHILDREN_MUST_BE_BUTTON.getMsg()).addConstraintViolation();
            return false;
        }

        //分类的子元素不能是按钮
        if (TypeEnum.CATALOGUE.equals(parentTypeEnum) && TypeEnum.BUTTON.equals(typeEnum)) {
            context.buildConstraintViolationWithTemplate(CATALOGUE_CHILD_MUST_NOT_BUTTON.getMsg()).addConstraintViolation();
            return false;
        }

        //分类的父元素只能是分类
        if (TypeEnum.CATALOGUE.equals(typeEnum) && !TypeEnum.CATALOGUE.equals(parentTypeEnum)) {
            context.buildConstraintViolationWithTemplate(CATALOGUE_PARENT_MUST_PARENT.getMsg()).addConstraintViolation();
            return false;
        }

        return true;
    }
}
