package wiki.chiu.micro.exhibit.adapter.out.http;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.api.UserHttpService;

class UserHttpServiceWrapperTest {

    private final UserHttpService users = org.mockito.Mockito.mock(UserHttpService.class);
    private final UserHttpServiceWrapper wrapper = new UserHttpServiceWrapper(users);

    @Test
    void missingAuthorBecomesPublicBlogNotFound() {
        when(users.findById(42L)).thenReturn(Result.fail(ExceptionMessage.USER_MISS, "missing"));

        assertThrows(MissException.class, () -> wrapper.findById(42L));
    }

    @Test
    void downstreamFailureIsNotHiddenAsMissingAuthor() {
        when(users.findById(42L))
            .thenReturn(Result.fail(CommonErrorCode.DOWNSTREAM_ERROR, "user service unavailable"));

        RemoteServiceException failure =
            assertThrows(RemoteServiceException.class, () -> wrapper.findById(42L));
        assertSame(CommonErrorCode.DOWNSTREAM_ERROR, failure.errorCode());
    }
}
