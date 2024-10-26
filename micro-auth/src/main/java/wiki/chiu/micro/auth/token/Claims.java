package wiki.chiu.micro.auth.token;

import java.util.List;

public record Claims(

        String userId,

        List<String> roles) {
}
