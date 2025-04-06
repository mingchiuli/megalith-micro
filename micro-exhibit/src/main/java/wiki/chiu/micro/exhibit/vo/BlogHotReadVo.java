package wiki.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-03-30 3:15 am
 */
public record BlogHotReadVo(

        Long id,

        String title,

        Long readCount) {

    public static BlogHotReadVoBuilder builder() {
        return new BlogHotReadVoBuilder(null, null, null);
    }

    public record BlogHotReadVoBuilder(
            Long id,
            String title,
            Long readCount
    ) {

        public BlogHotReadVoBuilder id(Long id) {
            return new BlogHotReadVoBuilder(id, title, readCount);
        }

        public BlogHotReadVoBuilder title(String title) {
            return new BlogHotReadVoBuilder(id, title, readCount);
        }

        public BlogHotReadVoBuilder readCount(Long readCount) {
            return new BlogHotReadVoBuilder(id, title, readCount);
        }

        public BlogHotReadVo build() {
            return new BlogHotReadVo(id, title, readCount);
        }

    }
}
