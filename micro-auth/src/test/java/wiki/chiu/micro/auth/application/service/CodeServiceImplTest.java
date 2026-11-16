package wiki.chiu.micro.auth.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;
import wiki.chiu.micro.auth.application.port.out.MailSender;
import wiki.chiu.micro.auth.application.port.out.SmsSender;
import wiki.chiu.micro.auth.application.port.out.UserDirectory;
import wiki.chiu.micro.common.exception.CodeException;

class CodeServiceImplTest {
    private final LoginCodeStore codes = mock(LoginCodeStore.class);
    private final MailSender mails = mock(MailSender.class);
    private final SmsSender sms = mock(SmsSender.class);
    private final CodeServiceImpl service = new CodeServiceImpl(mails, codes, mock(UserDirectory.class), sms);

    @Test
    void reservesBeforeSendingAndRejectsDuplicateRequests() {
        when(codes.reserve(eq(LoginCodeStore.LoginChannel.EMAIL), eq("a@example.com"), anyString(), anyString()))
            .thenReturn(true).thenReturn(false);
        service.createEmailCode("a@example.com");
        var order = inOrder(codes, mails);
        order.verify(codes).reserve(eq(LoginCodeStore.LoginChannel.EMAIL), eq("a@example.com"), anyString(), anyString());
        order.verify(mails).sendLoginCode(eq("a@example.com"), anyString());
        assertThatThrownBy(() -> service.createEmailCode("a@example.com")).isInstanceOf(CodeException.class);
        verifyNoMoreInteractions(mails);
    }

    @Test
    void failedDeliveryCancelsOnlyItsOwnReservation() {
        when(codes.reserve(eq(LoginCodeStore.LoginChannel.PHONE), eq("123"), anyString(), anyString()))
            .thenReturn(true);
        doThrow(new IllegalStateException("delivery failed")).when(sms).sendLoginCode(eq("123"), anyString());
        assertThatThrownBy(() -> service.createSMSCode("123")).hasMessage("delivery failed");
        var reservation = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(codes).reserve(eq(LoginCodeStore.LoginChannel.PHONE), eq("123"), anyString(), reservation.capture());
        verify(codes).cancel(LoginCodeStore.LoginChannel.PHONE, "123", reservation.getValue());
    }

    @Test
    void unavailableStoreDoesNotSend() {
        when(codes.reserve(any(), anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(() -> service.createEmailCode("a@example.com")).hasMessage("offline");
        verifyNoInteractions(mails);
    }
}
