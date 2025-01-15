package wiki.chiu.micro.user.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.repository.MenuRepository;
import wiki.chiu.micro.user.valid.DeleteMenu;

import java.util.List;


public class DeleteMenuConstraintValidator implements ConstraintValidator<DeleteMenu, Long> {

    private final MenuRepository menuRepository;

    public DeleteMenuConstraintValidator(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }


    @Override
    public boolean isValid(Long id, ConstraintValidatorContext constraintValidatorContext) {
        List<MenuEntity> menus = menuRepository.findByParentId(id);
        return menus.isEmpty();
    }
}
