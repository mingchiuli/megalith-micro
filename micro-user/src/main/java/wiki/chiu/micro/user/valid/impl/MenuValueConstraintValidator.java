package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.lang.TypeEnum;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.req.MenuEntityReq;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.user.valid.MenuValue;

import java.util.Objects;

import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

public class MenuValueConstraintValidator implements ConstraintValidator<MenuValue, MenuEntityReq> {

    private final MenuRepository menuRepository;

    public MenuValueConstraintValidator(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public boolean isValid(MenuEntityReq menu, ConstraintValidatorContext context) {
        if (isInvalidBasicFields(menu)) {
            return false;
        }

        TypeEnum typeEnum = TypeEnum.getInstance(menu.type());
        TypeEnum parentTypeEnum = getParentTypeEnum(menu.parentId());

        context.disableDefaultConstraintViolation();
        return !isInvalidParentChildRelation(typeEnum, parentTypeEnum, context);
    }

    private boolean isInvalidBasicFields(MenuEntityReq menu) {
        return Objects.isNull(menu.parentId()) ||
                !StringUtils.hasLength(menu.title()) ||
                Objects.isNull(menu.orderNum()) ||
                Objects.isNull(menu.type()) ||
                (!StatusEnum.NORMAL.getCode().equals(menu.status()) && !StatusEnum.HIDE.getCode().equals(menu.status()));
    }

    private TypeEnum getParentTypeEnum(Long parentId) {
        if (Long.valueOf(0).equals(parentId)) {
            return TypeEnum.CATALOGUE;
        }
        MenuEntity parentMenu = menuRepository.findById(parentId)
                .orElseThrow(() -> new MissException(NO_FOUND.toString()));
        return TypeEnum.getInstance(parentMenu.getType());
    }

    private boolean isInvalidParentChildRelation(TypeEnum typeEnum, TypeEnum parentTypeEnum, ConstraintValidatorContext context) {
        if (TypeEnum.BUTTON.equals(parentTypeEnum)) {
            context.buildConstraintViolationWithTemplate(BUTTON_MUST_NOT_PARENT.getMsg()).addConstraintViolation();
            return true;
        }
        if (TypeEnum.MENU.equals(parentTypeEnum) && !TypeEnum.BUTTON.equals(typeEnum)) {
            context.buildConstraintViolationWithTemplate(MENU_CHILDREN_MUST_BE_BUTTON.getMsg()).addConstraintViolation();
            return true;
        }
        if (TypeEnum.CATALOGUE.equals(parentTypeEnum) && TypeEnum.BUTTON.equals(typeEnum)) {
            context.buildConstraintViolationWithTemplate(CATALOGUE_CHILD_MUST_NOT_BUTTON.getMsg()).addConstraintViolation();
            return true;
        }
        if (TypeEnum.CATALOGUE.equals(typeEnum) && !TypeEnum.CATALOGUE.equals(parentTypeEnum)) {
            context.buildConstraintViolationWithTemplate(CATALOGUE_PARENT_MUST_PARENT.getMsg()).addConstraintViolation();
            return true;
        }
        return false;
    }
}
