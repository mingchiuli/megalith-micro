package wiki.chiu.micro.common.lang;


import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2021-12-13 10:46 AM
 */
public record BlogOperateMessage(

        Long blogId,

        Integer typeEnumCode) implements Serializable {
}
